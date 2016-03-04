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
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IPv6RecordTest {

    @Test
    public void testFits() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read("parser/ipv6.txt");
        for (CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit IPv6Record", line.getRecordNumber()), IPv6Record.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read(
                new StringReader("afrinic|ZA|ipv6|2001:4200::|32|20051021|assigned|F36B9F4B|ext7"));
        for (CSVRecord line : lines) {
            IPv6Record record = new IPv6Record(line, "someDate");
            assertEquals("IPv6Record not equal", "afrinic|ZA|ipv6|2001:4200::|32|20051021|assigned|F36B9F4B|ext7|e-stats", record.toString());
        }

        lines = CSVRecordUtil.read(
                new StringReader("afrinic|ZZ|ipv6|2001:4201::|32||reserved|"));
        for (CSVRecord line : lines) {
            IPv6Record record = new IPv6Record(line, "someDate");
            assertEquals("IPv6Record not equal", "afrinic|ZZ|ipv6|2001:4201::|32|someDate|reserved||e-stats", record.toString());
        }
    }

    @Test
    public void testIpRangeConversion() throws Exception {
        IPv6Record record1 = new IPv6Record("", "", "1::", "128", "", "", "");
        assertEquals("", record1.getRange(), Ipv6Range.from(Ipv6.of("1::")).to(Ipv6.of("1::")));

        IPv6Record record2 = new IPv6Record("", "", "1::", "127", "", "", "");
        assertEquals("", record2.getRange(), Ipv6Range.from(Ipv6.of("1::")).to(Ipv6.of("1::1")));

    }
}