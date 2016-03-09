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
import net.ripe.commons.ip.AsnRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ASNIntervalTree {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ASNNode root;

    public void add(AsnRange range) {
        add(new ASNNode(range));
    }

    public void add(ASNNode newNode) {
        // if this is the first node
        if (root == null)
            root = newNode;

        ASNNode node = root;
        while(true) {
            int comparisonResult = compare(newNode.getRange(), node.getRange());
            if (comparisonResult == 0) {
                // already exists so we don't add it
                return;
            }
            else if (comparisonResult < 0) {
                if (node.getLeft() == null) {
                    node.setLeft(newNode);
                    adjustAfterInsertion(node.getLeft());
                    return;
                }
                node = node.getLeft();
            }
            else {
                if (node.getRight() == null) {
                    node.setRight(newNode);
                    adjustAfterInsertion(node.getRight());
                    return;
                }
                node = node.getRight();
            }
        }
    }

    public void remove(AsnRange range) {
        ASNNode node = findExact(range);
        if (node != null)
            remove(node);
    }

    public void remove(ASNNode node) {
        if (node == null) {
            return;
        }
        if (node.getLeft() != null && node.getRight() != null) {
            // if node has two children then we replace it by its predecessor
            // here we optimize by copying the predecessor value into this node
            // and remove the predecessor
            ASNNode predecessor = getPredecessor(node);
            node.copyFrom(predecessor);
            node = predecessor; // make predecessor target of removal
        }
        // At this point the node has at most one subnode; let's get it
        ASNNode subnode = node.getLeft() != null ? node.getLeft() : node.getRight();
        // Link subnode to node's parent node, or make it root if node was root
        if (subnode != null) {
            if (root == node) {
                setRoot(subnode);
            }
            else if (node.isLeftChild()) {
                node.getParent().setLeft(subnode);
            }
            else {
                node.getParent().setRight(subnode);
            }
            // Red-black tree specific
            if (isBlack(node)) {
                adjustAfterRemoval(subnode);
            }
        }
        else if (root == node) {
            // removing the root node
            root = null;
        }
        else {
            // Red-black tree specific
            if (isBlack(node)) {
                adjustAfterRemoval(node);
            }
            node.getParent().dropChild(node);
        }
    }

    public int depth() {
        int result = depth(root);
        return result;
    }

    private int depth(ASNNode node) {
        if (node == null)
            return 0;
        int leftDepth = depth(node.getLeft());
        int rightDepth = depth(node.getRight());
        return 1 + (leftDepth > rightDepth ? leftDepth : rightDepth);
    }

    public long size() {
        long result = size(root);
        return result;
    }

    private long size(ASNNode node) {
        if (node == null)
            return 0;
        long leftSize = size(node.getLeft());
        long rightSize = size(node.getRight());
        return leftSize + rightSize + 1;
    }



    public ASNNode findFirstOverlap(AsnRange range) {
        return findFirstOverlap(root, range);
    }

    public List<AsnRange> findOverlappingRanges(AsnRange range) {
        return findAllOverlaps(range).stream().map(node -> node.getRange()).collect(Collectors.toList());
    }

    public List<ASNNode> findAllOverlaps(AsnRange range) {
        List<ASNNode> result = new ArrayList<>();
        collectAllOverlaps(root, range, result);
        return result;
    }

    private ASNNode findFirstOverlap(ASNNode node, AsnRange range) {
        if (node == null)
            return null;
        if (node.getRange().overlaps(range))
            return node;
        if (node.getLeft() != null && node.getLeft().getMax().compareTo(range.start()) >= 0 )
            return findFirstOverlap(node.getLeft(), range);
        return findFirstOverlap(node.getRight(), range);
    }

    public ASNNode findExact(AsnRange range) {
        return findExact(root, range);
    }

    private ASNNode findExact(ASNNode node, AsnRange range) {
        if (node == null)
            return null;
        if (compare(node.getRange(), range) == 0) {
            return node;
        }
        if (node.getLeft() != null && node.getLeft().getMax().compareTo(range.start()) >= 0 )
            return findExact(node.getLeft(), range);
        return findExact(node.getRight(), range);
    }

    private void collectAllOverlaps(ASNNode node, AsnRange range, List<ASNNode> list) {
        if (node == null)
            return;
        if (node.getRange().overlaps(range))
            list.add(node);
        if (node.getLeft() != null && node.getLeft().getMax().compareTo(range.start()) >= 0 )
            collectAllOverlaps(node.getLeft(), range, list);
        collectAllOverlaps(node.getRight(), range, list);
    }

    public List<ASNRecord> getOrderedRecords() {
        return getOrderedNodes().stream().map(node -> node.getRecord()).collect(Collectors.toList());
    }


    public List<AsnRange> getOrderedRanges() {
        return getOrderedNodes().stream().map(node -> node.getRange()).collect(Collectors.toList());
    }

    private List<ASNNode> getOrderedNodes() {
        List<ASNNode> result = new ArrayList<>();
        append(result, root, 0);
        return result;
    }

    private void append(List<ASNNode> list, ASNNode node, int depth) {
        if (node == null)
            return;
        append(list, node.getLeft(), depth+1);
        list.add(node);
        append(list, node.getRight(), depth+1);
    }

    private void adjustAfterInsertion(ASNNode node) {
        setRed(node);

        ASNNode parent = getParent(node);
        ASNNode grandParent = getGrandParent(node);
        // node is red; if parent is also red so we need to do something
        if (isRed(parent)) {
            // if both parent and uncle are red then we recolor and propagate to newly red grandparent
            if (isRed(parent.sibling())) {
                setBlack(parent);
                setBlack(parent.sibling());
                setRed(grandParent);
                adjustAfterInsertion(grandParent);
            }
            else if (parent == leftChild(grandParent)) {
                if (node == rightChild(parent)) {
                    node = parent;
                    rotateLeft(node);
                }
                setBlack(getParent(node));
                setRed(getGrandParent(node));
                rotateRight(getGrandParent(node));
            }
            else if (parent == rightChild(grandParent)) {
                if (node == leftChild(parent)) {
                    node = parent;
                    rotateRight(node);
                }
                setBlack(getParent(node));
                setRed(getGrandParent(node));
                rotateLeft(getGrandParent(node));
            }
        }
        setBlack(root);
    }

    private void adjustAfterRemoval(ASNNode node) {
        if (node != null)
        while(node != root && isBlack(node)) {
            if (node.isLeftChild()) {
                if (isRed(node.sibling())) {
                    setBlack(node.sibling());
                    setRed(node.getParent());
                    rotateLeft(node.getParent());
                }
                if (isBlack(node.sibling().getLeft()) && isBlack(node.sibling().getRight())) {
                    setRed(node.sibling());
                    node = node.getParent();
                }
                else {
                    if (isBlack(node.sibling().getRight())) {
                        setBlack(node.sibling().getLeft());
                        setRed(node.sibling());
                        rotateRight(node.sibling());
                    }
                    copyColor(node.getParent(), node.sibling());
                    setBlack(node.getParent());
                    setBlack(node.sibling().getRight());
                    rotateLeft(node.getParent());
                    node = root;
                }
            }
            else if (node.isRightChild()) {
                if (isRed(node.sibling())) {
                    setBlack(node.sibling());
                    setRed(node.getParent());
                    rotateRight(node.getParent());
                }

                if (isBlack(node.sibling().getLeft()) && isBlack(node.sibling().getRight())) {
                    setRed(node.sibling());
                    node = node.getParent();
                }
                else {
                    if (isBlack(node.sibling().getLeft())) {
                        setBlack(node.sibling().getRight());
                        setRed(node.sibling());
                        rotateLeft(node.sibling());
                    }
                    copyColor(node.getParent(), node.sibling());
                    setBlack(node.getParent());
                    setBlack(node.sibling().getLeft());
                    rotateRight(node.getParent());
                    node = root;
                }
            }
            else
                throw new IllegalStateException("Node is nor left child nor right child.");
        }
        setBlack(node);
    }

     private void rotateLeft(ASNNode node) {
         ASNNode oldParent = node.getParent();
        ASNNode oldRightChild = rightChild(node);
        node.setRight(oldRightChild.getLeft());
        if (root == node)
            setRoot(oldRightChild);
        else if (node == leftChild(oldParent))
            oldParent.setLeft(oldRightChild);
        else if (node == rightChild(oldParent))
            oldParent.setRight(oldRightChild);
        else
            throw new IllegalStateException("Illegal parent-child link.");
        oldRightChild.setLeft(node);
    }

    private void rotateRight(ASNNode node) {
        ASNNode oldParent = node.getParent();
        ASNNode oldLeftChild = leftChild(node);
        node.setLeft(oldLeftChild.getRight());
        if (root == node)
            root = oldLeftChild;
        else if (node == leftChild(oldParent))
            oldParent.setLeft(oldLeftChild);
        else if (node == rightChild(oldParent))
            oldParent.setRight(oldLeftChild);
        else
            throw new IllegalStateException("Illegal parent-child link.");
        oldLeftChild.setRight(node);
    }

    private ASNNode getParent(ASNNode node) {
        return node == null ? null : node.getParent();
    }

    private ASNNode getGrandParent(ASNNode node) {
        if (node == null || node.getParent() == null) return null;
        return node.getParent().getParent();
    }

    private int compare(AsnRange range1, AsnRange range2) {
        if (range1.start().compareTo(range2.start()) < 0) return -1;
        else if (range1.start().compareTo(range2.start()) > 0)  return +1;
        else if (range1.end().compareTo(range2.end()) < 0) return -1;
        else if (range1.end().compareTo(range2.end()) > 0) return +1;
        else return 0;
    }

    private boolean isBlack(ASNNode node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(ASNNode node) {
        return node != null && node.isRed();
    }

    private ASNNode leftChild(ASNNode node) {
        return node == null ? null : node.getLeft();
    }

    private ASNNode getPredecessor(ASNNode node) {
        if (node.getLeft() != null) {
            return getMaximumNode(node.getLeft());
        }
        else {
            return node.isRightChild() ? node.getParent() : null;
        }
    }

    private ASNNode getSuccessor(ASNNode node) {
        if (node.getRight() != null) {
            return getMinimumNode(node.getRight());
        }
        else {
            return node.isLeftChild() ? node.getParent() : null;
        }
    }

    private ASNNode getMinimumNode(ASNNode node) {
        while (node.getLeft() != null)
            node = node.getLeft();
        return node;
    }

    private ASNNode getMaximumNode(ASNNode node) {
        while (node.getRight() != null)
            node = node.getRight();
        return node;
    }

    private ASNNode rightChild(ASNNode node) {
        return node == null ? null : node.getRight();
    }

    private void setBlack(ASNNode node) {
        if (node != null) node.setBlack();
    }

    private void copyColor(ASNNode source, ASNNode destination) {
        if (destination != null) {
            if (isRed(source))
                setRed(destination);
            else
                setBlack(destination);
        }
    }


    private void setRed(ASNNode node) {
        if (node != null) node.setRed();
    }

    public boolean hasRedParent(ASNNode node) {
        return node != null && node != root && isRed(node.getParent());
    }

    public boolean contains(AsnRange range) {
        return (get(range) != null);
    }

    public ASNNode get(AsnRange range) {
        return get(root, range);
    }

    private ASNNode get(ASNNode node, AsnRange range) {
        if (node == null) return null;
        int cmp = compare(range, node.getRange());
        if (cmp < 0) return get(node.getLeft(), range);
        else if (cmp > 0) return get(node.getRight(), range);
        else return node;
    }

    public ASNNode getRoot() {
        return root;
    }

    private void setRoot(ASNNode node) {
        this.root = node;
        node.setParent(null);
    }
}
