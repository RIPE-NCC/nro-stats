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

import net.nro.stats.components.parser.*;
import net.nro.stats.resources.ParsedRIRStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Merger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${nro.stats.extended.order}")
    private String[] rirs;

    class PrioritizedRecord<I extends Record> {
        private int prio;
        private Record record;

        private PrioritizedRecord(int prio, Record record) {
            this.prio = prio;
            this.record = record;
        }

        public int getPrio() {
            return prio;
        }

        public Record getRecord() {
            return record;
        }
    }

    public List<Line> merge(List<ParsedRIRStats> parsedRIRStats) {



        List<PrioritizedRecord<IPv4Record>> mergedIPv4Lines = new ArrayList<>();
        List<PrioritizedRecord<IPv6Record>> mergedIPv6Lines = new ArrayList<>();
        List<PrioritizedRecord<ASNRecord>> mergedASNLines = new ArrayList<>();

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
                    .map(record -> new PrioritizedRecord<IPv4Record>(prio, (IPv4Record)record))
                    .sorted((o1, o2) -> ((IPv4Record)o1.getRecord()).getComparator().compare(o1.getRecord().getRange(), o2.getRecord().getRange()))
                    .collect(Collectors.toList()));
            mergedIPv6Lines.addAll(stats.getLines().stream()
                    .filter(record -> record instanceof IPv6Record)
                    .map(record -> new PrioritizedRecord<IPv6Record>(prio, (IPv6Record)record))
                    .sorted((o1, o2) -> ((IPv6Record)o1.getRecord()).getComparator().compare(o1.getRecord().getRange(), o2.getRecord().getRange()))
                    .collect(Collectors.toList()));
            mergedASNLines.addAll(stats.getLines().stream()
                    .filter(record -> record instanceof ASNRecord)
                    .map(record -> new PrioritizedRecord<ASNRecord>(prio, (ASNRecord)record))
                    .sorted((o1, o2) -> ((ASNRecord)o1.getRecord()).getComparator().compare(o1.getRecord().getRange(), o2.getRecord().getRange()))
                    .collect(Collectors.toList()));

            i++;
        }

        List<Line> result = handleConflicts(mergedIPv4Lines);
        result.addAll(handleConflicts(mergedIPv6Lines));
        result.addAll(handleConflicts(mergedASNLines));
        return result;
    }

    private <T extends Record> List<Line> handleConflicts(List<PrioritizedRecord<T>> lines) {
        List<Line> results = new ArrayList<>();

        for(int i = 0; i < lines.size(); i++) {

            if (i == 0 || !lines.get(i).getRecord().getRange().overlaps(lines.get(i-1).getRecord().getRange())) {

                results.add(lines.get(i).getRecord());
            }
            else {
                logger.info("Conflict: " + lines.get(i).getRecord().toString());
            }
        }
        return results;
    }
}
