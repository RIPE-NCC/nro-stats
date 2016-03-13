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
package net.nro.stats.components.parser;

import net.nro.stats.components.DateTimeProvider;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.RIRStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
public class Parser {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Charset charset;

    private DateTimeProvider dateTimeProvider;

    @Autowired
    public Parser(Charset charset, DateTimeProvider dateTimeProvider) {
        this.charset = charset;
        this.dateTimeProvider = dateTimeProvider;
    }

    public ParsedRIRStats parseRirStats(RIRStats rirStats) {
        String today = dateTimeProvider.today();
        ParsedRIRStats parsedRIRStats = new ParsedRIRStats(rirStats.getRir());
        try {
            Reader in = new InputStreamReader(new ByteArrayInputStream(rirStats.getContent()), charset);
            Iterable<CSVRecord> lines = CSVFormat
                    .DEFAULT
                    .withDelimiter('|')
                    .withCommentMarker('#') // only recognized at start of line!
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .parse(in);
            for (CSVRecord line : lines) {
                if (Header.fits(line)) {
                    parsedRIRStats.addHeader(new Header(line));
                } else if (Summary.fits(line)) {
                    parsedRIRStats.addSummary(new Summary(line));
                } else if (IPv4Record.fits(line)) {
                    parsedRIRStats.addIPv4Record(new IPv4Record(line, today));
                } else if (IPv6Record.fits(line)) {
                    parsedRIRStats.addIPv6Record(new IPv6Record(line, today));
                } else if (ASNRecord.fits(line)) {
                    parsedRIRStats.addAsnRecord(new ASNRecord(line, today));
                } else {
                    logger.warn("Malformed line number " + line.getRecordNumber() + "\n" + line.toString());
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Found records: " + parsedRIRStats.getLines().count());
        return parsedRIRStats;

    }

    public List<Line> parse(byte[] content) {
        List<Line> fileContent = new ArrayList<>();
        String today = dateTimeProvider.today();
        try {
            Reader in = new InputStreamReader(new ByteArrayInputStream(content), charset);
            Iterable<CSVRecord> lines = CSVFormat
                    .DEFAULT
                    .withDelimiter('|')
                    .withCommentMarker('#') // only recognized at start of line!
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .parse(in);
            for (CSVRecord line : lines) {
                if (Header.fits(line)) {
                    fileContent.add(new Header(line));
                } else if (Summary.fits(line)) {
                    fileContent.add(new Summary(line));
                } else if (IPv4Record.fits(line)) {
                    fileContent.add(new IPv4Record(line, today));
                } else if (IPv6Record.fits(line)) {
                    fileContent.add(new IPv6Record(line, today));
                } else if (ASNRecord.fits(line)) {
                    fileContent.add(new ASNRecord(line, today));
                } else {
                    logger.warn("Malformed line number " + line.getRecordNumber() + "\n" + line.toString());
                }
             }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Found records: " + fileContent.size());
        return fileContent;
    }

}
