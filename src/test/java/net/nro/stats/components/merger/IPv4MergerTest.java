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

import net.nro.stats.components.ConflictResolver;
import net.nro.stats.components.parser.IPv4Record;
import net.nro.stats.components.parser.LineTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IPv4MergerTest extends LineTestBase {


    private ConflictResolver resolver = new ConflictResolver("apnic,afrinic,arin,ripencc,lacnic".split(","));
    IPv4Merger iPv4Merger = new IPv4Merger(resolver);

    @Test
    public void mergerPreservesCidr() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("apnic", "1.1.1.0", "256"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("Merger preserves one CIDR block", 1, mergedRecords.size());
        Assert.assertEquals("Merger preserved exactly the specified CIDR block", "1.1.1.0/24", mergedRecords.get(0).getRange().toStringInCidrNotation());
    }

    @Test
    public void mergerSplitsNonAlignedCidrRange1() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("apnic", "1.1.1.128", "256"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("Merger splits block in correct nr of pieces", 2, mergedRecords.size());
        Assert.assertEquals("Merger preserved exactly the specified CIDR block", "1.1.1.128/25", mergedRecords.get(0).getRange().toStringInCidrNotation());
        Assert.assertEquals("Merger preserved exactly the specified CIDR block", "1.1.2.0/25", mergedRecords.get(1).getRange().toStringInCidrNotation());
    }

    @Test
    public void mergerSplitsNonCidrRange1() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("apnic", "1.1.1.0", "257"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("Merger preserved exactly the specified CIDR block", "1.1.1.0/24", mergedRecords.get(0).getRange().toStringInCidrNotation());
        Assert.assertEquals("Merger preserved exactly the specified CIDR block", "1.1.2.0/32", mergedRecords.get(1).getRange().toStringInCidrNotation());
    }

    @Test
    public void mergerDiscardsOlderAllocationInConflict() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("apnic", "1.1.1.0", "256"));
        inputRecords.add(createRecord("afrinic", "1.1.1.128", "4"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("Merger discards conflicting older block", 1, mergedRecords.size());
        Assert.assertEquals("Merger preserved exactly the specified CIDR block", "1.1.1.0/24", mergedRecords.get(0).getRange().toStringInCidrNotation());
    }

    @Test
    public void mergerSplitsOnNewerSubRange1() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("afrinic", "1.1.1.0", "256"));
        inputRecords.add(createRecord("apnic", "1.1.1.128", "128"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("newer claim on subrange leads to two range allocations", 2, mergedRecords.size());
        // result must contain a '1.1.1.0/25' for afrinic and a '1.1.1.128/25' for apnic
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange", allocationExists(mergedRecords, "afrinic", "1.1.1.0/25") );
        Assert.assertTrue("Merger allocates newer claim on subrange of older claim", allocationExists(mergedRecords, "apnic", "1.1.1.128/25") );
    }

    @Test
    public void mergerSplitsOnNewerSubRange2() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        // same as previous test, but added in reverse order
        // should not make a difference
        inputRecords.add(createRecord("apnic", "1.1.1.128", "128"));
        inputRecords.add(createRecord("afrinic", "1.1.1.0", "256"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("newer claim on subrange leads to two range allocations", 2, mergedRecords.size());
        // result must contain a '1.1.1.0/25' for afrinic and a '1.1.1.128/25' for apnic
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange", allocationExists(mergedRecords, "afrinic", "1.1.1.0/25"));
        Assert.assertTrue("Merger allocates newer claim on subrange of older claim", allocationExists(mergedRecords, "apnic", "1.1.1.128/25"));
    }


    @Test
    public void mergerSplitsOnNewerSubRange3() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("afrinic", "1.1.1.0", "256"));
        inputRecords.add(createRecord("apnic", "1.1.1.0", "64"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("Merger discards conflicting older block", 3, mergedRecords.size());
        Assert.assertTrue("Merger allocates newer claim on subrange of older claim", allocationExists(mergedRecords, "apnic", "1.1.1.0/26") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange", allocationExists(mergedRecords, "afrinic", "1.1.1.64/26") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange", allocationExists(mergedRecords, "afrinic", "1.1.1.128/25") );
    }

    @Test
    public void mergerOverlappingRange() {
        List<IPv4Record> inputRecords = new ArrayList<>();
        inputRecords.add(createRecord("afrinic", "1.1.1.0", "64"));
        inputRecords.add(createRecord("apnic", "1.1.1.32", "64"));
        List<IPv4Record> mergedRecords = iPv4Merger.merge(inputRecords);
        Assert.assertEquals("Merger discards conflicting older block", 3, mergedRecords.size());
        Assert.assertTrue("Merger allocates newer claim on subrange of older claim", allocationExists(mergedRecords, "afrinic", "1.1.1.0/27") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange", allocationExists(mergedRecords, "apnic", "1.1.1.32/27") );
        Assert.assertTrue("Merger demotes older claim to subrange when newer claim on subrange", allocationExists(mergedRecords, "apnic", "1.1.1.64/27") );
    }

    private boolean allocationExists(List<IPv4Record> mergedRecords, String registry, String cidr) {
        return mergedRecords.stream().filter(r -> recordHasRegistryAndCidr(r, registry, cidr)).findFirst().isPresent();
    }

    private boolean recordHasRegistryAndCidr(IPv4Record record, String registry, String cidr) {
        return registry.equals(record.getRegistry()) && cidr.equals(record.getRange().toStringInCidrNotation());
    }

    private IPv4Record createRecord(String registry, String startIp, String addressCount) {
        return new IPv4Record(registry, "NL", startIp, addressCount, null, null, null);
    }

}
