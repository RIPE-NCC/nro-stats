/**
 * The BSD License
 *
 * Copyright (c) 2010-2016 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.nro.stats.components;

import net.nro.stats.components.merger.ASNMerger;
import net.nro.stats.components.merger.IPv4Merger;
import net.nro.stats.components.merger.IPv6Merger;
import net.nro.stats.components.parser.*;
import net.nro.stats.resources.ParsedRIRStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Merger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IPv4Merger iPv4Merger;

    @Autowired
    IPv6Merger iPv6Merger;

    @Autowired
    ASNMerger asnMerger;

    @Value("${nro.stats.extended.order}")
    private String[] rirs;

    public List<Line> merge(List<ParsedRIRStats> parsedRIRStats) {



        List<IPv4Record> mergedIPv4Lines = new ArrayList<>();
        List<IPv6Record> mergedIPv6Lines = new ArrayList<>();
        List<ASNRecord> mergedASNLines = new ArrayList<>();

        int i = 0;
        for( String rir : rirs) {
            ParsedRIRStats stats = parsedRIRStats
                    .stream()
                    .filter(localStats -> localStats
                            .getRir()
                            .getResourceHolder()
                            .getIdentifier()
                            .equals(rir))
                    .collect(Collectors.toList())
                    .get(0);
            final int prio = i;
            mergedIPv4Lines.addAll(stats.getLines().stream()
                    .filter(record -> record instanceof IPv4Record)
                    .map(record -> (IPv4Record)record)
                    .collect(Collectors.toList()));
            mergedIPv6Lines.addAll(stats.getLines().stream()
                    .filter(record -> record instanceof IPv6Record)
                    .map(record -> (IPv6Record)record)
                    .collect(Collectors.toList()));
            mergedASNLines.addAll(stats.getLines().stream()
                    .filter(record -> record instanceof ASNRecord)
                    .map(record -> (ASNRecord)record)
                    .collect(Collectors.toList()));

            i++;
        }

        List<Line> result  = new ArrayList<>();
        result.addAll(iPv4Merger.merge(mergedIPv4Lines));
        result.addAll(iPv6Merger.merge(mergedIPv6Lines));
        result.addAll(asnMerger.merge(mergedASNLines));
        return result;
    }

    private <T extends Record> List<Line> handleConflicts(List<T> lines) {
        List<Line> results = new ArrayList<>();

        for(int i = 0; i < lines.size(); i++) {

            if (i == 0 || !lines.get(i).getRange().overlaps(lines.get(i-1).getRange())) {

                results.add(lines.get(i));
            }
            else {
                logger.info("Conflict: " + lines.get(i).toString());
            }
        }
        return results;
    }
}
