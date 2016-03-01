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
import net.nro.stats.components.parser.IPv6Record;
import net.nro.stats.components.parser.Record;
import net.ripe.commons.ip.AbstractRange;
import net.ripe.commons.ip.Ipv4Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.padStart;

public abstract class IPMerger<T extends Record<R>, R extends AbstractRange> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConflictResolver conflictResolver;

    public IPMerger(ConflictResolver conflictResolver) {
        this.conflictResolver = conflictResolver;
    }

    public List<T> merge(List<T> recordsList) {
        IPNode<T> node, root = new IPNode<>(0, 'x', null);
        Queue<T> records = new ConcurrentLinkedQueue<>();
        records.addAll(recordsList);
        T record;
        while ((record = records.poll()) != null) {
            Queue<R> ranges = new ConcurrentLinkedQueue<>();
            ranges.addAll(prefixRanges(record.getRange()));
            R range;
            while ((range = ranges.poll()) != null) {
                node = root;
                char[] significantBinaryValue = getSignificantBinaryValues(range).toCharArray();
                for (char c : significantBinaryValue) {
                    if (c == '0') {
                        node = node.getLeftNode();
                    } else { // c =='1'
                        node = node.getRightNode();
                    }
                    if (node.getRecord() != null) {
                        if (conflictResolver.resolve(node.getRecord(), record) == record) {
                            logger.warn("Conflict found for {} b/w {} and {}", node.getRecord().getRange(), node.getRecord().getRegistry(), record.getRegistry());
                            records.offer(node.getRecord());
                            node.unclaim();
                        }
                    }
                }
                if (node.getRecord() == null) {
                    T modifiedRecord = record.clone(range);
                    if (!node.claim(conflictResolver, modifiedRecord)) {
                        ranges.addAll(splitRanges(range));
                    }
                }
            }
        }

        return root.getAllChildRecords();
    }

    public abstract List<R> prefixRanges(R range);

    public abstract String getSignificantBinaryValues(R range);

    public abstract List<R> splitRanges(R range);
}
