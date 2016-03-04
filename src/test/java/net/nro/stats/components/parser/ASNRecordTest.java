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

import net.nro.stats.components.CSVRecordUtil;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ASNRecordTest {

    @Test
    public void testFits() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read("parser/asn.txt");
        for (CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit ASNRecord", line.getRecordNumber()), ASNRecord.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read(new StringReader("afrinic|ZA|asn|1229|1|19910301|allocated|F36B9F4B"));

        for (CSVRecord line : lines) {
            ASNRecord record = new ASNRecord(line, "someDate");
            assertEquals("ASNRecord not matching", record.toString(), "afrinic|ZA|asn|1229|1|19910301|assigned|F36B9F4B|e-stats");
        }

        lines = CSVRecordUtil.read(new StringReader("afrinic|ZA|asn|1228|1|19910301|allocated|F36B9F4B|ext1|ext2"));

        for (CSVRecord line : lines) {
            ASNRecord record = new ASNRecord(line, "someDate");
            assertEquals("ASNRecord not matching", record.toString(), "afrinic|ZA|asn|1228|1|19910301|assigned|F36B9F4B|ext1|ext2|e-stats");
        }
    }

    @Test
    public void testASNRangeConversion() throws Exception {
        ASNRecord record1 = new ASNRecord("", "", "1", "10", "", "", "");
        assertEquals("", record1.getRange(), AsnRange.from(Asn.of(1L)).to(Asn.of(10L)));

    }
}