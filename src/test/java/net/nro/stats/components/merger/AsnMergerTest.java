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
import net.nro.stats.components.parser.ASNRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsnMergerTest {

    private OrderedResolver resolver = new OrderedResolver("apnic,afrinic,arin,ripencc,lacnic".split(","));
    ASNMerger asnMerger = new ASNMerger(resolver);

    @Test
    public void testNoConflict() {
        List<ASNRecord> records = new ArrayList<>();
        records.add(createRecord("lacnic", "4", "1"));
        records.add(createRecord("ripencc", "5", "2"));
        records.add(createRecord("apnic", "8", "1"));
        records.add(createRecord("apnic", "10", "1"));
        List<ASNRecord> mergedRecords = asnMerger.mergeToTree(records).getOrderedRecords();
        assertEquals(4, mergedRecords.size());
        verifyRecord(mergedRecords, "lacnic", "4", "1");
        verifyRecord(mergedRecords, "ripencc", "5", "2");
        verifyRecord(mergedRecords, "apnic", "8", "1");
        verifyRecord(mergedRecords, "apnic", "10", "1");
    }

    @Test
    public void testBasicConflict() {
        List<ASNRecord> records = new ArrayList<>();
        records.add(createRecord("ripencc", "11", "1"));
        records.add(createRecord("apnic", "11", "1"));
        List<ASNRecord> mergedRecords = asnMerger.mergeToTree(records).getOrderedRecords();
        assertEquals(1, mergedRecords.size());
        verifyRecord(mergedRecords, "apnic", "11", "1");
    }

    @Test
    public void testConflictOverlap() {
        List<ASNRecord> records = new ArrayList<>();
        //Start same, end same
        records.add(createRecord("ripencc", "11", "5"));
        records.add(createRecord("apnic", "11", "5"));
        //start same, end before
        records.add(createRecord("ripencc", "21", "5"));
        records.add(createRecord("apnic", "21", "3"));
        //start same, end after
        records.add(createRecord("ripencc", "31", "5"));
        records.add(createRecord("apnic", "31", "7"));
        //Start after, end before
        records.add(createRecord("ripencc", "41", "5"));
        records.add(createRecord("apnic", "42", "2"));
        //start after, end same
        records.add(createRecord("ripencc", "51", "5"));
        records.add(createRecord("apnic", "52", "4"));
        //start after, end after
        records.add(createRecord("ripencc", "61", "5"));
        records.add(createRecord("apnic", "64", "7"));

        List<ASNRecord> mergedRecords = asnMerger.mergeToTree(records).getOrderedRecords();
        assertEquals(11, mergedRecords.size());
        verifyRecord(mergedRecords, "apnic", "11", "5");
        verifyRecord(mergedRecords, "apnic", "21", "3");
        verifyRecord(mergedRecords, "ripencc", "24", "2");
        verifyRecord(mergedRecords, "apnic", "31", "7");
        verifyRecord(mergedRecords, "ripencc", "41", "1");
        verifyRecord(mergedRecords, "apnic", "42", "2");
        verifyRecord(mergedRecords, "ripencc", "44", "2");
        verifyRecord(mergedRecords, "ripencc", "51", "1");
        verifyRecord(mergedRecords, "apnic", "52", "4");
        verifyRecord(mergedRecords, "ripencc", "61", "3");
        verifyRecord(mergedRecords, "apnic", "64", "7");
    }

    @Test
    public void testConflictOverlapPriorityFirst() {
        List<ASNRecord> records = new ArrayList<>();
        //Start same, end same
        records.add(createRecord("apnic", "11", "5"));
        records.add(createRecord("ripencc", "11", "5"));
        //start same, end before
        records.add(createRecord("apnic", "21", "3"));
        records.add(createRecord("ripencc", "21", "5"));
        //start same, end after
        records.add(createRecord("apnic", "31", "7"));
        records.add(createRecord("ripencc", "31", "5"));

        List<ASNRecord> mergedRecords = asnMerger.mergeToTree(records).getOrderedRecords();
        assertEquals(4, mergedRecords.size());
        verifyRecord(mergedRecords, "apnic", "11", "5");
        verifyRecord(mergedRecords, "apnic", "21", "3");
        verifyRecord(mergedRecords, "ripencc", "24", "2");
        verifyRecord(mergedRecords, "apnic", "31", "7");
    }

    private ASNRecord createRecord(String registry, String asn, String length) {
        return new ASNRecord(registry, "NL", asn, length, "", "", "", "");
    }

    private void verifyRecord(List<ASNRecord> records, String registry, String asn, String length) {
        boolean found = false;
        for (ASNRecord record : records) {
            if (record.getRegistry().equals(registry) &&
                    record.getStart().equals(asn) &&
                    record.getValue().equals(length)) {
                found = true;
                break;
            }
        }
        assertTrue(String.format("Record missing %s , %s - %s", registry, asn, length), found);
    }
}
