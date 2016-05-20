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

import com.google.common.collect.Lists;
import net.nro.stats.components.parser.ASNRecord;
import net.nro.stats.components.resolver.Resolver;
import net.ripe.commons.ip.AsnRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Component
public class ASNMerger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Resolver resolver;

    @Autowired
    public ASNMerger(Resolver resolver) {
        this.resolver = resolver;
    }

    public ASNIntervalTree mergeToTree(List<ASNRecord> recordList) {
        logger.debug("Starting merging of ASN Records");

        ASNIntervalTree resolvedRecords = new ASNIntervalTree();
        Deque<ASNRecord> stack = new ArrayDeque<>();

        stack.addAll(recordList);

        while(!stack.isEmpty()) {
            processNextRecord(stack, resolvedRecords);
        }

        return resolvedRecords;
    }


    private void excludeRangeAndScheduleRemainingForClaiming(ASNRecord source, ASNRecord overlap, Deque<ASNRecord> stack) {
        List<AsnRange> remainingRanges = source.getRange().exclude(overlap.getRange());
        for (AsnRange range: remainingRanges) {
            stack.push(source.clone(range));
        }
    }

    private void processNextRecord(Deque<ASNRecord> stack, ASNIntervalTree claimedRanges) {
        ASNRecord newRecord = stack.pop();
        // find any previously processed record of which the range overlaps this record's range
        ASNNode overlap = claimedRanges.findFirstOverlap(newRecord.getRange());

        // if no overlapping records found then we are free to add this one
        if (overlap == null) {
            claimedRanges.add(new ASNNode(newRecord));
        }
        else {
            // we have overlapping ranges; let conflict resolver determine which record has precedence
            ASNRecord previouslyClaimedRecord = overlap.getRecord();
            resolver.recordConflict(overlap.getRecord(), Lists.newArrayList(newRecord));
            if (resolver.resolve(newRecord, previouslyClaimedRecord) == previouslyClaimedRecord) {
                excludeRangeAndScheduleRemainingForClaiming(newRecord, previouslyClaimedRecord, stack);
            }
            else {
                claimedRanges.remove(overlap);
                excludeRangeAndScheduleRemainingForClaiming(previouslyClaimedRecord, newRecord, stack);
                stack.push(newRecord);
            }
        }
    }
}
