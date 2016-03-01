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

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HeaderTest extends LineTestBase {

    @Before
    public void setUp() throws Exception {
        createRawLines("parser/header.txt");
    }

    @Test
    public void testFits() throws Exception {
        for(CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit Header", line.getRecordNumber()), Header.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        CSVRecord line1 = lines.iterator().next();
        Header header1 = new Header(line1);
        assertTrue("Header field not correct: version", line1.get(0).equals(header1.getVersion()));
        assertTrue("Header field not correct: registry", line1.get(1).equals(header1.getRegistry()));
        assertTrue("Header field not correct: serial", line1.get(2).equals(header1.getSerial()));
        assertTrue("Header field not correct: records", line1.get(3).equals(header1.getRecords()));
        assertTrue("Header field not correct: startDate", line1.get(4).equals(header1.getStartDate()));
        assertTrue("Header field not correct: endDate", line1.get(5).equals(header1.getEndDate()));
        assertTrue("Header field not correct: utcOffset", line1.get(6).equals(header1.getUtcOffset()));
    }
}