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
package net.nro.stats.services;

import net.nro.stats.components.*;
import net.nro.stats.components.merger.Delta;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.config.ExtendedInputConfig;
import net.nro.stats.config.ExtendedOutputConfig;
import net.nro.stats.resources.MergedStats;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.StatsSource;
import net.nro.stats.resources.URIContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NroStatsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CURRENT = "current";
    private static final String PREVIOUS = "previous";

    @Autowired
    ExtendedInputConfig extendedInputConfig;

    @Autowired
    ExtendedOutputConfig extendedOutputConfig;

    @Autowired
    DateTimeProvider dateTimeProvider;

    @Autowired
    Parser parser;

    @Autowired
    RecordsMerger recordsMerger;

    @Autowired
    PreProcessor preProcessor;

    @Autowired
    StatsWriter writer;

    @Autowired
    URIContentRetriever uriContentRetriever;

    @Autowired
    CachedService cachedService;

    public synchronized void generate() {
        logger.info("Generating Extended NRO Stats");
        try {
            List<ParsedRIRStats> parsedRIRStats = fetchAndParseAllRirStats(extendedInputConfig.getRir());
            ParsedRIRStats parsedIANAStats = fetchAndParseIanaStats("iana", extendedInputConfig.getIana());
            ParsedRIRStats parsedRIRSwaps = fetchAndParseRirSwapStats(StatsSource.RIRSWAP.getValue(), extendedInputConfig.getSwaps());

            // some data sets need extra pre-processing
            preProcessor.processIanaStats(parsedIANAStats);

            List<ParsedRIRStats> combinedStats = new ArrayList<>();
            combinedStats.addAll(parsedRIRStats);
            combinedStats.add(parsedIANAStats);
            combinedStats.add(parsedRIRSwaps);

            ParsedRIRStats nroStats = convert(recordsMerger.merge(combinedStats));

            writer.write(nroStats);

            logger.info("Finished Generating Extended NRO stats");
        } catch (Exception e) {
            logger.error("Failed while generating NRO Extended stats", e);
        }
    }

    public List<Delta<?>> getDifferences() {
        MergedStats current = getStats(CURRENT);
        MergedStats previous = getStats(PREVIOUS);
        return recordsMerger.findDifferences(current, previous);
    }


    public MergedStats getStats(String key) {
        return cachedService.fetch(key);
    }

    private ParsedRIRStats fetchAndParseRirSwapStats(String dataSetName, String url) {
        URIContent uriContent = uriContentRetriever.fetch(dataSetName, url);
        return parser.parseRIRSwaps(StatsSource.RIRSWAP, uriContent);
    }

    private ParsedRIRStats fetchAndParseIanaStats(String dataSetName, String url) {
        return fetchAndParseRecords(StatsSource.IANA_REGISTRY, dataSetName, url);
    }

    private List<ParsedRIRStats> fetchAndParseAllRirStats(Map<String, String> urls) {
        return urls.keySet()
                .parallelStream()
                .map(rir -> fetchAndParseRecords(StatsSource.ESTATS, rir, urls.get(rir)))
                .collect(Collectors.toList());
    }

    private ParsedRIRStats convert(MergedStats mergedStats) {
        ParsedRIRStats stats = new ParsedRIRStats(extendedOutputConfig.getIdentifier());
        stats.addAllAsnRecord(mergedStats.getAsns().getOrderedRecords());
        stats.addAllIPv4Record(mergedStats.getIpv4s().getRecords());
        stats.addAllIPv6Record(mergedStats.getIpv6s().getRecords());
        stats.generateSummaryAndHeader(extendedOutputConfig, dateTimeProvider, mergedStats.getHeaderStartDate());
        return stats;
    }

    private ParsedRIRStats fetchAndParseRecords(StatsSource source, String dataSetName, String url) {
        URIContent uriContent = uriContentRetriever.fetch(dataSetName, url);
        return parser.parseRirStats(source, uriContent);
    }
}
