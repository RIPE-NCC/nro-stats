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

import net.nro.stats.components.resolver.OrderedResolver;
import net.nro.stats.components.parser.IPv6Record;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class IPv6MergerTest {

    private OrderedResolver resolver = new OrderedResolver("apnic,afrinic,arin,ripencc,lacnic".split(","));
    IPv6Merger merger = new IPv6Merger(resolver);

    @Test
    public void basicOK() throws Exception {
        List<IPv6Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("arin", "2620:101:9800::", "37"));
        inputRecords.add(createRecord("arin", "2620:101:a000::", "47"));

        List<IPv6Record> mergedRecords = merger.treeMerge(inputRecords).getRecords();
        Assert.assertEquals(2, mergedRecords.size());
    }

    @Test
    public void basicConflictResolutionHigherFirst() throws Exception {
        List<IPv6Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("arin", "2620:101:9800::", "37"));
        inputRecords.add(createRecord("ripencc", "2620:101:9800::", "37"));

        List<IPv6Record> mergedRecords = merger.treeMerge(inputRecords).getRecords();
        // only one accepted
        Assert.assertEquals(1, mergedRecords.size());
        // the newer claim is accepted
        Assert.assertEquals("", "arin", mergedRecords.get(0).getRegistry());
    }

    @Test
    public void basicConflictResolutionLowerFirst() throws Exception {
        List<IPv6Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("ripencc", "2620:101:9800::", "37"));
        inputRecords.add(createRecord("arin", "2620:101:9800::", "37"));

        List<IPv6Record> mergedRecords = merger.treeMerge(inputRecords).getRecords();
        // only one accepted
        Assert.assertEquals(1, mergedRecords.size());
        // the newer claim is accepted
        Assert.assertEquals("", "arin", mergedRecords.get(0).getRegistry());
    }

    @Test
    public void mergerSplitsOnNewerSubRange1() {
        List<IPv6Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("afrinic",    "2001:db8::", "64"));
        inputRecords.add(createRecord("apnic",      "2001:db8::", "68"));
        List<IPv6Record> mergedRecords = merger.treeMerge(inputRecords).getRecords();
        Assert.assertEquals("newer claim on subrange leads to two range allocations", 5, mergedRecords.size());
        // apnic claim is for a subrange of afrinic claim
        Assert.assertTrue("Merger allocates newer claim on subrange of older claim",
                allocationExists(mergedRecords, "apnic", "2001:db8::/68") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange",
                allocationExists(mergedRecords, "afrinic", "2001:db8:0:0:8000::/65") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange",
                allocationExists(mergedRecords, "afrinic", "2001:db8:0:0:4000::/66") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange",
                allocationExists(mergedRecords, "afrinic", "2001:db8:0:0:2000::/67") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange",
                allocationExists(mergedRecords, "afrinic", "2001:db8:0:0:1000::/68") );
    }

    @Test
    public void mergeSplitsOnNewerSubrange2() {
        // if a newer (= higher prio) claim is done on a subrange of a older range at depth x deeper
        // then the old range should be split into x+1 subranges of which one is for the newer claimer
        // and the remaining x are for the old claimer. Here we test this for several relative depths
        for (int i = 0; i < 60; i++) {
            testWithNewerSubrangeClaimWithRelativePrefixDepth(i);
        }
    }

    private void testWithNewerSubrangeClaimWithRelativePrefixDepth(int depth) {
        List<IPv6Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("afrinic",    "2001:db8::", "64"));
        inputRecords.add(createRecord("apnic",      "2001:db8::", String.valueOf(64 + depth)));
        List<IPv6Record> mergedRecords = merger.treeMerge(inputRecords).getRecords();
        Assert.assertEquals("newer claim on subrange leads to two range allocations", depth + 1, mergedRecords.size());
        Assert.assertEquals("exactly one subrange for newer claim", 1, mergedRecords.stream().filter(r -> "apnic".equals(r.getRegistry())).count());
        Assert.assertEquals("remaining  subranges allocated to older claim", depth, mergedRecords.stream().filter(r -> "afrinic".equals(r.getRegistry())).count());
    }


    private boolean allocationExists(List<IPv6Record> mergedRecords, String registry, String cidr) {
        return mergedRecords.stream().filter(r -> recordHasRegistryAndCidr(r, registry, cidr)).findFirst().isPresent();
    }

    private boolean recordHasRegistryAndCidr(IPv6Record record, String registry, String cidr) {
        return registry.equals(record.getRegistry()) && cidr.equals(record.getRange().toStringInCidrNotation());
    }

    private IPv6Record createRecord(String registry, String startIp, String addressCount) {
        return new IPv6Record(registry, "NL", startIp, addressCount, null, null, null);
    }
}
