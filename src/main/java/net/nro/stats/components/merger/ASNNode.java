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

import net.nro.stats.components.parser.ASNRecord;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ASNNode {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ASNNode left;
    private ASNNode right;
    private ASNNode parent;
    private ASNRecord record;
    private AsnRange range;
    private Asn max;
    private boolean isRed;

    public ASNNode(ASNRecord record) {
        this(record.getRange());
        this.record = record;
    }

    public ASNNode(AsnRange range) {
        if (range == null)
            throw new IllegalArgumentException("Cannot create ASNNode with null range.");
        this.range = range;
        this.max = range.end();
    }

    public ASNNode getLeft() {
        return left;
    }

    public void setLeft(ASNNode left) {
        // unlink my left, but only if it is pointing to me
        if (this.left != null && this.left.getParent() == this)
            this.left.setParent(null);
        this.left = left;
        if (left != null) {
            left.setParent(this);
        }
        recalculateMax();
    }

    public ASNNode getRight() {
        return right;
    }

    public void setRight(ASNNode right) {
        // unlink my right, but only if it is pointing to me
        if (this.right != null && this.right.getParent() == this)
            this.right.setParent(null);
        this.right = right;
        if (right != null) {
            right.setParent(this);
        }
        recalculateMax();
    }

    void dropChild(ASNNode node) {
        if (node == getLeft()) setLeft(null);
        else if (node == getRight()) setRight(null);
    }

    private void recalculateMax() {
        Asn childrenMax = range.end();
        if (left != null && left.getMax().compareTo(childrenMax) > 0)
            childrenMax = left.getMax();
        if (right != null && right.getMax().compareTo(childrenMax) > 0)
            childrenMax = right.getMax();
        if (childrenMax.compareTo(max) != 0) {
            max = childrenMax;
            propagateMax();
        }

    }

    public AsnRange getRange() {
        return  range;

    }

    public void copyFrom(ASNNode node) {
        this.range = node.getRange();
        this.max = range.end();
    }

    private Asn asnMax(Asn asn1, Asn asn2) {
        return asn1.compareTo(asn2) > 0 ? asn1 : asn2;
    }

    public void propagateMax() {
        propagateMax(this.max);
    }

    public void propagateMax(Asn value) {
        if (value.compareTo(max) > 0)
            max = value;
        if (parent != null)
            parent.propagateMax(value);
    }

    public boolean isLeftChild() {
        return parent != null && this == parent.getLeft();
    }

    public boolean isRightChild() {
        return parent != null && this == parent.getRight();
    }

    public ASNNode sibling() {
        if (parent == null) return null;
        return this == parent.getLeft() ? parent.getRight(): parent.getLeft();
    }

    public String toString() {
        String parentRef = parent == null ? "o" : (isLeftChild() ? "/" : "\\");
        return parentRef + "[" + (isRed ? "*" : " ") + ":" + range + "|" + max + "]";
    }

    public ASNRecord getRecord() {
        return record;
    }

    public void setRecord(ASNRecord record) {
        this.record = record;
    }

    public void setBlack() {
        isRed = false;
    }

    public void setRed() {
        isRed = true;
    }

    public boolean isRed() {
        return this.isRed;
    }

    public boolean isBlack() {
        return !isRed;
    }

    public ASNNode getParent() {
        return parent;
    }

    public void setParent(ASNNode parent) {
        this.parent = parent;
    }

    public Asn getMax() {
        return max;
    }
}
