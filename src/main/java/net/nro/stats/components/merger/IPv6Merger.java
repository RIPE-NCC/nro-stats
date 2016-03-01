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
import net.nro.stats.components.parser.IPv6Record;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.PrefixUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.padStart;

@Component
public class IPv6Merger {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConflictResolver conflictResolver;

    @Autowired
    public IPv6Merger(ConflictResolver conflictResolver) {
        this.conflictResolver = conflictResolver;
    }

    public List<IPv6Record> merge(List<IPv6Record> recordsList) {
        IPNode<IPv6Record> root = new IPNode<>(0, 'x', null);
        IPNode<IPv6Record> node;
        Queue<IPv6Record> records = new ConcurrentLinkedQueue<>();
        records.addAll(recordsList);
        IPv6Record record;
        while ((record = records.poll()) != null) {
            Queue<Ipv6Range> ranges = new ConcurrentLinkedQueue<>();
            ranges.addAll(record.getRange().splitToPrefixes());
            Ipv6Range range;
            while ((range = ranges.poll()) != null) {
                node = root;
                String binaryRange = padStart(range.start().asBigInteger().toString(2), 128, '0').substring(0, 129 - PrefixUtils.getPrefixLength(range));
                for (char c : binaryRange.toCharArray()) {
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
                    IPv6Record modifiedRecord = record.clone(range);

                    if (!node.claim(conflictResolver, modifiedRecord)) {
                        if (binaryRange.length() != 128) {
                            logger.warn("Unable to claim {} by {}, splitting it and will try again.", range, record.getRegistry());
                            String zeroRange = binaryRange + "0";
                            ranges.offer(Ipv6Range.from(new BigInteger(padEnd(zeroRange, 128, '0'), 2)).andPrefixLength(zeroRange.length()));
                            String oneRange = binaryRange + "1";
                            ranges.offer(Ipv6Range.from(new BigInteger(padEnd(oneRange, 128, '0'), 2)).andPrefixLength(oneRange.length()));
                        } else {
                            logger.warn("Unable to claim {} by {}", range, record.getRegistry());
                        }
                    }
                }
            }
        }

        return root.getAllChildRecords();
    }
}
