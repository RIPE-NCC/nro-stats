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
package net.nro.stats.components.merger;

import net.nro.stats.components.CSVRecordUtil;
import net.nro.stats.components.ConflictResolver;
import net.nro.stats.components.parser.IPv6Record;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IPv6MergerTest {

    private ConflictResolver resolver = new ConflictResolver(Arrays.asList("apnic,afrinic,arin,ripencc,lacnic".split(",")));
    IPv6Merger merger = new IPv6Merger(resolver);

    @Test
    public void basicOK() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read(
                new StringReader("arin||ipv6|2620:101:9800::|37||available|\n" +
                        "arin||ipv6|2620:101:a000::|47||reserved|\n"));

        List<IPv6Record> ipv6Records = getiPv6Records(lines);

        List<IPv6Record> mergedRecords = merger.merge(ipv6Records);
        Assert.assertEquals(2, mergedRecords.size());
    }

    @Test
    public void basicConflictResolutionHigherFirst() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read(
                new StringReader("arin||ipv6|2620:101:9800::|37||available|\n" +
                        "ripencc||ipv6|2620:101:9800::|37||available|\n"));

        List<IPv6Record> ipv6Records = getiPv6Records(lines);

        List<IPv6Record> mergedRecords = merger.merge(ipv6Records);
        Assert.assertEquals(1, mergedRecords.size());
    }

    @Test
    public void basicConflictResolutionLowerFirst() throws Exception {
        Iterable<CSVRecord> lines = CSVRecordUtil.read(
                new StringReader("ripencc||ipv6|2620:101:9800::|37||available|\n" +
                        "arin||ipv6|2620:101:9800::|37||available|\n"));

        List<IPv6Record> ipv6Records = getiPv6Records(lines);

        List<IPv6Record> mergedRecords = merger.merge(ipv6Records);
        Assert.assertEquals(1, mergedRecords.size());
    }

    private List<IPv6Record> getiPv6Records(Iterable<CSVRecord> lines) {
        List<IPv6Record> ipv6Records = new ArrayList<>();
        for (CSVRecord line : lines) {
            ipv6Records.add(new IPv6Record(line));
        }
        return ipv6Records;
    }
}
