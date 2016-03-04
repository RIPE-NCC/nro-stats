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

import net.nro.stats.components.RecordsMerger;
import net.nro.stats.components.RIRStatsRetriever;
import net.nro.stats.components.StatsWriter;
import net.nro.stats.components.Validator;
import net.nro.stats.components.parser.Line;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.RIRStats;
import net.nro.stats.resources.ResourceHolderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NroStatsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    List<ResourceHolderConfig> resourceHolders;

    @Autowired
    Parser parser;

    @Autowired
    RecordsMerger recordsMerger;

    @Autowired
    Validator validator;

    @Autowired
    StatsWriter writer;

    @Autowired
    RIRStatsRetriever rirStatsRetriever;


    public void generate() {
        logger.info("Generating Extended NRO Stats");
        try {
            List<RIRStats> rirStats = rirStatsRetriever.fetchAll(resourceHolders);

            List<ParsedRIRStats> parsedRIRStats = rirStats.stream().map(stat ->
                    new ParsedRIRStats(parser.parse(stat.getContent()), stat.getRir())
                ).collect(Collectors.toList());

            List<ParsedRIRStats> validatedSourceLinesPerRIR = validator.validate(parsedRIRStats);

            List<Line> targetLines = recordsMerger.merge(validatedSourceLinesPerRIR);

            writer.write(targetLines);

            logger.info("Finished Generating Extended NRO stats");
        } catch (Exception e) {
            logger.error("Failed while generating NRO Extended stats", e);
        }
    }

}
