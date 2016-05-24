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
import net.nro.stats.resources.ASNTransfer;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.StatsSource;
import net.nro.stats.resources.URIContent;
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

    public ParsedRIRStats parseRirStats(StatsSource source, URIContent uriContent) {
        String today = dateTimeProvider.today();
        ParsedRIRStats parsedRIRStats = new ParsedRIRStats(uriContent.getIdentifier());
        try {
            Iterable<CSVRecord> lines = readCSV(uriContent.getContent(), '|');
            for (CSVRecord line : lines) {
                if (Header.fits(line)) {
                    parsedRIRStats.setHeader(new Header(source, line));
                } else if (Summary.fits(line)) {
                    parsedRIRStats.addSummary(new Summary(source, line));
                } else if (IPv4Record.fits(line)) {
                    parsedRIRStats.addIPv4Record(new IPv4Record(source, line, today));
                } else if (IPv6Record.fits(line)) {
                    parsedRIRStats.addIPv6Record(new IPv6Record(source, line, today));
                } else if (ASNRecord.fits(line)) {
                    parsedRIRStats.addAsnRecord(new ASNRecord(source, line, today));
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

    public ParsedRIRStats parseNroStats(URIContent uriContent) {
        ParsedRIRStats parsedRIRStats = new ParsedRIRStats(uriContent.getIdentifier());
        try {
            Iterable<CSVRecord> lines = readCSV(uriContent.getContent(), '|');
            for (CSVRecord line : lines) {
                if (IPv4Record.fits(line)) {
                    parsedRIRStats.addIPv4Record(new IPv4Record(StatsSource.NRO, line, null));
                } else if (IPv6Record.fits(line)) {
                    parsedRIRStats.addIPv6Record(new IPv6Record(StatsSource.NRO, line, null));
                } else if (ASNRecord.fits(line)) {
                    parsedRIRStats.addAsnRecord(new ASNRecord(StatsSource.NRO, line, null));
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Found records in NRO read: " + parsedRIRStats.getLines().count());
        return parsedRIRStats;
    }

    private Iterable<CSVRecord> readCSV(byte[] bytes, char fieldSeparator) throws IOException {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), charset);
        return CSVFormat
                .DEFAULT
                .withDelimiter(fieldSeparator)
                .withCommentMarker('#') // only recognized at start of line!
                .withRecordSeparator('\n')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .parse(reader);

    }

    public ParsedRIRStats parseRIRSwaps(StatsSource source, URIContent uriContent) {
        String today = dateTimeProvider.today();
        ParsedRIRStats parsedRIRStats = new ParsedRIRStats(uriContent.getIdentifier());
        try {
            Iterable<CSVRecord> lines = readCSV(uriContent.getContent(), ' ');
            for (CSVRecord line : lines) {
                parsedRIRStats.addIPv4Record(createSwapRecord(source, line, today));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Found records: " + parsedRIRStats.getLines().count());
        return parsedRIRStats;
    }

    private IPv4Record createSwapRecord(StatsSource source, CSVRecord line, String date) {
        String registry = line.get(4);
        String startIp = line.get(0);
        String rangeSize = line.get(2);
        return new IPv4Record(source, registry, Record.DEFAULT_COUNTRY_CODE, startIp, rangeSize, date, "available", null);
    }


    public ASNTransfer parseAsnTransfers(URIContent uriContent) {
        ASNTransfer asnTransfer = null;
        try {
            Reader in = new InputStreamReader(new ByteArrayInputStream(uriContent.getContent()), charset);
            List<ASNTransferRecord> asnTransferRecords = new ArrayList<>();
            Iterable<CSVRecord> lines = CSVFormat
                    .DEFAULT
                    .withDelimiter('\t')
                    .withCommentMarker('#') // only recognized at start of line!
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .parse(in);

            lines.forEach(line -> {
                asnTransferRecords.add(new ASNTransferRecord(line));
            });

            asnTransfer = new ASNTransfer(asnTransferRecords);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return asnTransfer;
    }
}
