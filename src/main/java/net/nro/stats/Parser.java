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
package net.nro.stats;

import net.nro.stats.parser.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

    public Parser() {
    }

//    public static void main(String[] args) {
//        Parser parser = new Parser();
//        parser.run();
//    }

    private void run() {
        parse("stats/delegated-apnic-extended-latest.txt");
    }

    private List<Line> parse(String fileName) {
        List<Line> fileContent = new ArrayList<>();

        try {
            Reader in = new FileReader(fileName);
            Iterable<CSVRecord> lines = CSVFormat
                    .DEFAULT
                    .withDelimiter('|')
                    .withCommentMarker('#')
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .parse(in);
            for (CSVRecord line : lines) {
                if (Header.fits(line)) {
                    fileContent.add(new Header(line));
                } else if (Summary.fits(line)) {
                    fileContent.add(new Summary(line));
                } else if (IPv4Record.fits(line)) {
                    IPv4Record record = new IPv4Record(line);
                    fileContent.add(record);
                    System.out.println("range: " + record.getRange());
                } else if (IPv6Record.fits(line)) {
                    IPv6Record record = new IPv6Record(line);
                    fileContent.add(record);
                    System.out.println("range: " + record.getRange());
                } else if (ASNRecord.fits(line)) {
                    ASNRecord record = new ASNRecord(line);
                    fileContent.add(record);
                    System.out.println("range: " + record.getRange());
                } else {
                    throw new RuntimeException("Malformed line number " + line.getRecordNumber());
                }
             }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Found records: " + fileContent.size());
        return fileContent;
    }

}
