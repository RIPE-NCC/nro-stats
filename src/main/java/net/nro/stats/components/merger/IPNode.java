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
import net.nro.stats.components.parser.Record;

import java.util.ArrayList;
import java.util.List;

public class IPNode<R extends Record> {
    IPNode<R> parent, left, right;
    private R record;

    public IPNode(IPNode parent) {
        this.parent = parent;
    }

    public IPNode<R> getLeftNode() {
        if (left == null) {
            left = new IPNode<>(this);
        }
        return left;
    }

    public IPNode<R> getRightNode() {
        if (right == null) {
            right = new IPNode<>(this);
        }
        return right;
    }

    public R getRecord() {
        return record;
    }

    public boolean claim(ConflictResolver conflictResolver, R record) {
        if (left == null && right == null) {
            this.record = record;
            return true;
        } else {
            if (defeatsAll(conflictResolver, record, getAllChildRecords())) {
                left = null;
                right = null;
                this.record = record;
                return true;
            }
        }
        return false;
    }

    private boolean defeatsAll(ConflictResolver conflictResolver, R record, List<R> childRecords) {
        for (R cr : childRecords) {
            if (conflictResolver.resolve(record, cr) == cr) {
                return false;
            }
        }
        return true;
    }

    public List<R> getAllChildRecords() {
        if (record != null) {
            List<R> records = new ArrayList<>();
            records.add(record);
            return records;
        } else {
            List<R> records = new ArrayList<>();
            if (left != null) {
                records.addAll(left.getAllChildRecords());
            }
            if (right != null) {
                records.addAll(right.getAllChildRecords());
            }
            return records;
        }
    }

    public void unclaim() {
        record = null;
    }
}
