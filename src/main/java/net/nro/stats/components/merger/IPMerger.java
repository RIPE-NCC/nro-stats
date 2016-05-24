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
import net.nro.stats.components.parser.Record;
import net.nro.stats.components.resolver.Resolver;
import net.ripe.commons.ip.AbstractIpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public abstract class IPMerger<T extends Record<R>, R extends AbstractIpRange> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Resolver resolver;
    int maxSize;

    public IPMerger(Resolver resolver, int maxSize) {
        this.resolver = resolver;
        this.maxSize = maxSize;
    }

    public IPNode<T> mergeToTree(List<T> recordsList) {
        logger.debug("Starting with the Ipv4 merged tree generation");

        IPNode<T> node, root = new IPNode<>(null);
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
                    node = getChildNode(node, c);
                    if (node.getRecord() != null) {
                        if (isNodeOwnerOfLessPriority(node, record)) {
                            //Move current owner to back of queue, lower priority requests have to wait.
                            records.offer(node.getRecord());
                            node.unclaim();
                        } else {
                            resolver.recordConflict(node.getRecord(), Lists.newArrayList(record));
                            //We need to ditch the current node, as it can not take any position below this node
                            break;
                        }
                    }
                }
                if (node.getRecord() == null) {
                    T modifiedRecord = record.clone(range);
                    if (defeatAll(modifiedRecord, node.getRecords())) {
                        resolver.recordConflict(modifiedRecord, node.getRecords());
                        node.claim(modifiedRecord);
                    } else {
                        ranges.addAll(splitRanges(range));
                    }
                }
            }
        }

        return root;
    }

    public List<Delta<R>> treeDiff(IPNode<T> current, IPNode<T> previous) {
        if (previous == null && current == null) {
            return null;
        }
        if (previous == null) {//current is not null
            return current.getRecords().stream().map(rec -> new Delta<>(rec, null)).collect(Collectors.toList());
        }
        if (current == null) {
            return previous.getRecords().stream().map(rec -> new Delta<>(null, rec)).collect(Collectors.toList());
        }

        List<Delta<R>> deltas = new ArrayList<>();
        Optional.ofNullable(compare(current.getRecord(), previous.getRecord())).ifPresent(deltas::add);
        Optional.ofNullable(treeDiff(current.left, previous.left)).ifPresent(deltas::addAll);
        Optional.ofNullable(treeDiff(current.right, previous.right)).ifPresent(deltas::addAll);

        return deltas;
    }

    private Delta<R> compare(Record<R> left, Record<R> right) {
        if (left != null && right != null) {
            if (!left.toString().equals(right.toString())) {
                return new Delta<>(left, right);
            } else {
                return null;
            }
        }
        if (left == null && right == null) {
            return null;
        }
        return new Delta<>(left, right);
    }

    private boolean isNodeOwnerOfLessPriority(IPNode<T> node, T record) {
        return resolver.resolve(node.getRecord(), record) == record;
    }

    public abstract List<R> prefixRanges(R range);

    public abstract String getSignificantBinaryValues(R range);

    public abstract List<R> splitRanges(R range);

    private boolean defeatAll(T modifiedRecord, List<T> records) {
        for (T cr : records) {
            if (resolver.resolve(modifiedRecord, cr) == cr) {
                return false;
            }
        }
        return true;
    }

    private IPNode<T> getChildNode(IPNode<T> node, char c) {
        return (c == '0') ? node.getLeftNode() : node.getRightNode();
    }
}
