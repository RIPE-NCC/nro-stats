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

import net.nro.stats.components.parser.IPv4Record;
import net.nro.stats.components.resolver.OrderedResolver;
import net.nro.stats.resources.StatsSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class OrderedResolverTest {

    private OrderedResolver resolver = new OrderedResolver("iana,e-stats,rir-swap".split(","),"apnic,afrinic,arin,ripencc,lacnic".split(","));

    @Test
    public void testBasic() throws Exception {
        Iterable<CSVRecord> lines = getCsvRecords(
                "apnic|AU|ipv4|1.0.0.0|256|20110811|assigned|A91872ED\n" +
                "ripencc|CN|ipv4|1.0.1.0|256|20110414|allocated|A92E1062|ext4|ext5|ext6\n");
        Iterator<CSVRecord> iterator = lines.iterator();
        IPv4Record rec = resolver.resolve(new IPv4Record(iterator.next(), "someDate"), new IPv4Record(iterator.next(), "someDate"));
        assertTrue(rec.getRegistry().equals("apnic"));
    }

    @Test
    public void testRirSwaps() throws Exception {
        Iterable<CSVRecord> lines = getCsvRecords(
                "apnic|AU|ipv4|1.0.0.0|256|20110811|assigned|A91872ED\n");
        Iterator<CSVRecord> iterator = lines.iterator();

        CSVRecord csvRecord = iterator.next();
        IPv4Record rec = resolver.resolve(new IPv4Record(StatsSource.ESTATS, csvRecord, "someDate"),
                new IPv4Record(StatsSource.RIRSWAP, csvRecord, "someDate"));

        assertTrue(rec.getSource() == StatsSource.ESTATS);
    }

    private Iterable<CSVRecord> getCsvRecords(String records) throws IOException {
        return CSVFormat
                    .DEFAULT
                    .withDelimiter('|')
                    .withCommentMarker('#') // only recognized at start of line!
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .parse(new StringReader(records));
    }
}
