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
import net.nro.stats.components.parser.ASNRecord;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ASNMerger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConflictResolver conflictResolver;

    @Autowired
    public ASNMerger(ConflictResolver conflictResolver) {
        this.conflictResolver = conflictResolver;
    }

    public List<ASNRecord> merge(List<ASNRecord> recordList) {
        ConcurrentHashMap<Asn, ASNRecord> map = new ConcurrentHashMap<>();

        List<ASNRecord> finalList = new ArrayList<>();

        List<ASNRecord> asnSortedRecords = recordList.stream()
                .sorted((a1, a2) -> a1.getRange().start().compareTo(a2.getRange().start()))
                .collect(Collectors.toList());

        Deque<ASNRecord> stack = new ArrayDeque<>();
        stack.addAll(asnSortedRecords);

        ASNRecord current = stack.pop();
        while(!stack.isEmpty()) {
            ASNRecord next = stack.peek();

            while (current.getRange().contains(next.getRange().start())) {
                if (conflictResolver.resolve(current, next) == current) {
                    if (current.getRange().contains(next.getRange().end())) {
                        logger.warn("Conflict found for ASN {} b/w {} and {}", next.getRange(), current.getRegistry(), next.getRegistry());
                        stack.pop(); //Ignore this one.
                        next = stack.peek();
                        continue;
                    } else {
                        stack.pop(); //Ignore this one.
                        for (AsnRange range : next.getRange().exclude(current.getRange())) {
                            stack.push(next.clone(range));
                        }
                        next = stack.peek();
                    }
                } else {
                    if (next.getRange().contains(current.getRange().start())) {
                        //both start at the same point
                        if (next.getRange().contains(current.getRange().end())) {
                            //current can be completely ignored
                            current = stack.pop();
                            next = stack.peek();
                        } else {
                            List<AsnRange> ranges = current.getRange().exclude(next.getRange());
                            next = stack.pop();
                            for (AsnRange range : ranges) {
                                stack.push(current.clone(range));
                            }
                            current = next;
                            next = stack.peek();
                        }
                    } else {
                        List<AsnRange> ranges = current.getRange().exclude(next.getRange());

                        current = current.clone(ranges.get(0));

                        next = stack.pop();
                        for (int i=0; i< ranges.size(); i++) {
                            if (i==0) {
                                current = current.clone(ranges.get(i));
                            } else {
                                stack.push(current.clone(ranges.get(i)));
                            }
                        }
                        stack.push(next);
                        next = stack.peek();
                    }
                }
            }
            finalList.add(current);
            current = stack.pop();
        }
        finalList.add(current);
        return finalList;
    }

}
