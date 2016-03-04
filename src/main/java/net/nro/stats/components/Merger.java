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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Merger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DateTimeFormatter DATE_FORMAT_NRO = DateTimeFormatter.ofPattern("yyyyMMdd");
    private DateTimeFormatter ZONE_FORMAT_NRO = DateTimeFormatter.ofPattern("Z");

    @Autowired
    private IPv4Merger iPv4Merger;

    @Autowired
    private IPv6Merger iPv6Merger;

    @Autowired
    private ASNMerger asnMerger;

    @Value("${nro.stats.extended.identifier}")
    private String identifier;

    @Value("${nro.stats.extended.version}")
    private String version;

    public List<Line> merge(List<ParsedRIRStats> parsedRIRStats) {

        Map<Class<? extends Line>, List<Line>> collect = parsedRIRStats.stream()
                .map(ParsedRIRStats::getLines)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        (Line l) -> l.getClass()
                ));

        collect.keySet().stream()
                .forEach(k -> logger.info("Collected total {} of type {}", collect.get(k).size(), k));

        List<Line> result  = new ArrayList<>();
        ZonedDateTime todayDate = ZonedDateTime.now();
        String today = todayDate.format(DATE_FORMAT_NRO);
        result.addAll(iPv4Merger.merge(collect.get(IPv4Record.class).parallelStream().map(r -> (IPv4Record)r).collect(Collectors.toList())));
        long ipv4 = result.size();
        result.add(0, new Summary(identifier, "ipv4", String.valueOf(ipv4)));
        result.addAll(iPv6Merger.merge(collect.get(IPv6Record.class).parallelStream().map(r -> (IPv6Record)r).collect(Collectors.toList())));
        long ipv6 = result.size() - ipv4 - 1;
        result.add(1, new Summary(identifier, "ipv6", String.valueOf(ipv6)));
        result.addAll(asnMerger.merge(collect.get(ASNRecord.class).parallelStream().map(r -> (ASNRecord)r).collect(Collectors.toList())));
        long asn = result.size() - ipv4 - ipv6 - 2;
        result.add(2, new Summary(identifier, "asn", String.valueOf(asn)));
        result.add(0, new Header(
                version, identifier, today,
                String.valueOf(ipv4 + ipv6 + asn), today, today, todayDate.format(ZONE_FORMAT_NRO)));

        logger.info("Number of Lines after merged {}", result.size());

        return result;
    }
}
