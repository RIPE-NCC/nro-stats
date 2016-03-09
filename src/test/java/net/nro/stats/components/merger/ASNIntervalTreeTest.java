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

import org.junit.Assert;
import net.ripe.commons.ip.AsnRange;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ASNIntervalTreeTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ASNIntervalTree tree;

    AsnRange range0 = AsnRange.from(9L).to(30L);
    AsnRange range1 = AsnRange.from(16L).to(22L);
    AsnRange range2 = AsnRange.from(20L).to(22L);
    AsnRange range3 = AsnRange.from(40L).to(48L);
    AsnRange range4 = AsnRange.from(62L).to(80L);
    AsnRange range5 = AsnRange.from(62L).to(90L);
    AsnRange range6 = AsnRange.from(100L).to(110L);
    AsnRange range7 = AsnRange.from(120L).to(130L);
    AsnRange range2a = AsnRange.from(20L).to(22L);

    @Before
    public void setup() {
        tree = new ASNIntervalTree();
        tree.add(range4);
        tree.add(range7);
        tree.add(range2);
        tree.add(range1);
        tree.add(range6);
        tree.add(range3);
        tree.add(range0);
        tree.add(range5);
        tree.add(range2a);
    }

    @Test
    public void itemsOrderedCorrectly() {

        List<AsnRange> result = tree.getOrderedRanges();
        Assert.assertEquals("Interval tree contains correct nr of items", 8, result.size());
        Assert.assertEquals("Interval tree ordered correctly", range0, result.get(0));
        Assert.assertEquals("Interval tree ordered correctly", range1, result.get(1));
        Assert.assertEquals("Interval tree ordered correctly", range2, result.get(2));
        Assert.assertEquals("Interval tree ordered correctly", range3, result.get(3));
        Assert.assertEquals("Interval tree ordered correctly", range4, result.get(4));
        Assert.assertEquals("Interval tree ordered correctly", range5, result.get(5));
    }

    @Test
    public void treeFindsLeftOverlapWhenOneExists() {
        Assert.assertEquals("Interval tree finds single left overlap", range0, tree.findFirstOverlap(AsnRange.from(5L).to(12L)).getRange());
        Assert.assertEquals("Interval tree finds single left overlap", range3, tree.findFirstOverlap(AsnRange.from(39L).to(42L)).getRange());
        Assert.assertEquals("Interval tree finds single left overlap", range4, tree.findFirstOverlap(AsnRange.from(61L).to(63L)).getRange());
        Assert.assertEquals("Interval tree finds single left overlap", range6, tree.findFirstOverlap(AsnRange.from(95L).to(105L)).getRange());
        Assert.assertEquals("Interval tree finds single left overlap", range7, tree.findFirstOverlap(AsnRange.from(115L).to(125L)).getRange());
    }

    @Test
    public void treeFindsRightOverlapWhenOneExists() {
        Assert.assertEquals("Interval tree finds single right overlap", range0, tree.findFirstOverlap(AsnRange.from(25L).to(32L)).getRange());
        Assert.assertEquals("Interval tree finds single right overlap", range3, tree.findFirstOverlap(AsnRange.from(45L).to(52L)).getRange());
        Assert.assertEquals("Interval tree finds single right overlap", range5, tree.findFirstOverlap(AsnRange.from(86L).to(93L)).getRange());
        Assert.assertEquals("Interval tree finds single right overlap", range6, tree.findFirstOverlap(AsnRange.from(105L).to(115L)).getRange());
        Assert.assertEquals("Interval tree finds single right overlap", range7, tree.findFirstOverlap(AsnRange.from(125L).to(135L)).getRange());
    }

    @Test
    public void treeFindsSingleEnclosingInterval() {
        Assert.assertEquals("Interval tree finds single enclosing interval", range0, tree.findFirstOverlap(AsnRange.from(10L).to(12L)).getRange());
        Assert.assertEquals("Interval tree finds single enclosing interval", range7, tree.findFirstOverlap(AsnRange.from(123L).to(127L)).getRange());
        Assert.assertEquals("Interval tree finds single enclosing interval", range1, tree.findFirstOverlap(AsnRange.from(17L).to(19L)).getRange());
        Assert.assertEquals("Interval tree finds single enclosing interval", range5, tree.findFirstOverlap(AsnRange.from(82L).to(87L)).getRange());
    }

    @Test
    public void treeFindsSingleSubInterval() {
        Assert.assertEquals("Interval tree finds single subinterval", range3, tree.findFirstOverlap(AsnRange.from(34L).to(52L)).getRange());
        Assert.assertEquals("Interval tree finds single subinterval", range6, tree.findFirstOverlap(AsnRange.from(95L).to(113L)).getRange());
        Assert.assertEquals("Interval tree finds single subinterval", range7, tree.findFirstOverlap(AsnRange.from(114L).to(190L)).getRange());
    }

    @Test
    public void treeFindsAllLeftOverlaps() {
        List<ASNNode> overlaps;
        // the following range should right overlap with range0, range1, range2
        overlaps = tree.findAllOverlaps(AsnRange.from(6L).to(18L));
        Assert.assertEquals("Interval tree finds correct number of overlapping intervals", 2, overlaps.size());
        Assert.assertTrue("tree collects the expected left overlaps", overlaps.stream().filter(asn -> asn.getRange() == range0).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected left overlaps", overlaps.stream().filter(asn -> asn.getRange() == range1).findFirst().isPresent());

        // the following range should right overlap with range0, range1, range2
        overlaps = tree.findAllOverlaps(AsnRange.from(56L).to(68L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 2, overlaps.size());
        Assert.assertTrue("tree collects the expected left overlaps", overlaps.stream().filter(asn -> asn.getRange() == range4).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected left overlaps", overlaps.stream().filter(asn -> asn.getRange() == range5).findFirst().isPresent());
    }

    @Test
    public void treeFindsAllRightOverlaps() {
        List<ASNNode> overlaps;
        // the following range should right overlap with range0, range1, range2
        overlaps = tree.findAllOverlaps(AsnRange.from(20L).to(32L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 3, overlaps.size());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range0).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range1).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range2).findFirst().isPresent());

        overlaps = tree.findAllOverlaps(AsnRange.from(65L).to(95L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 2, overlaps.size());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range4).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range5).findFirst().isPresent());
    }

    @Test
    public void treeFindsAllSubIntervals() {
        List<ASNNode> overlaps;
        // the following range should right overlap with range0, range1, range2
        overlaps = tree.findAllOverlaps(AsnRange.from(6L).to(32L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 3, overlaps.size());
        Assert.assertTrue("tree collects the expected subinterval", overlaps.stream().filter(asn -> asn.getRange() == range0).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected subinterval", overlaps.stream().filter(asn -> asn.getRange() == range1).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected subinterval", overlaps.stream().filter(asn -> asn.getRange() == range2).findFirst().isPresent());

        // the following range should right overlap with range6, range7
        overlaps = tree.findAllOverlaps(AsnRange.from(95L).to(135L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 2, overlaps.size());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range6).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range7).findFirst().isPresent());
    }

    @Test
    public void treeFindsAllEnclosingIntervals() {
        List<ASNNode> overlaps;
        // the following range should right overlap with range0, range1, range2
        overlaps = tree.findAllOverlaps(AsnRange.from(65L).to(70L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 2, overlaps.size());
        Assert.assertTrue("tree collects the expected subinterval", overlaps.stream().filter(asn -> asn.getRange() == range4).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected subinterval", overlaps.stream().filter(asn -> asn.getRange() == range5).findFirst().isPresent());

        overlaps = tree.findAllOverlaps(AsnRange.from(18L).to(19L));
        Assert.assertEquals("Interval tree finds right number of overlapping intervals", 2, overlaps.size());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range0).findFirst().isPresent());
        Assert.assertTrue("tree collects the expected right overlaps", overlaps.stream().filter(asn -> asn.getRange() == range1).findFirst().isPresent());
    }

    @Test
    public void treeCalculatesCorrectDepth() {
        tree = new ASNIntervalTree();
        Assert.assertEquals("empty tree has depth 0", 0, tree.depth());
        tree.add(AsnRange.from(32L).to(33L));
        Assert.assertEquals("tree gives correct depth", 1, tree.depth());
        tree.add(AsnRange.from(16L).to(17L));
        Assert.assertEquals("tree gives correct depth", 2, tree.depth());
        tree.add(AsnRange.from(48L).to(49L));
        Assert.assertEquals("tree gives correct depth", 2, tree.depth());
        tree.add(AsnRange.from(12L).to(13L));
        Assert.assertEquals("tree gives correct depth", 3, tree.depth());
        tree.add(AsnRange.from(44L).to(45L));
        Assert.assertEquals("tree gives correct depth", 3, tree.depth());
        tree.add(AsnRange.from(20L).to(21L));
        Assert.assertEquals("tree gives correct depth", 3, tree.depth());
        tree.add(AsnRange.from(52L).to(53L));
        Assert.assertEquals("tree gives correct depth", 3, tree.depth());
    }

    @Test
    public void treeBehavesAsRedBlackWhenRemovingNodes() {
        tree = new ASNIntervalTree();
        int size = 1000;
        //
        for (long i = 0L; i < size; i++) {
            tree.add(AsnRange.from(i).to(i + 1));
        }
        Assert.assertEquals(size, tree.size());
        Assert.assertTrue(tree.depth() <= maxDepthRedBlackTree(size));

        for (long i = 0L; i < size / 2; i++) {
            tree.remove(AsnRange.from(i).to(i + 1));
        }
        Assert.assertEquals(size/2, tree.size());
        Assert.assertTrue(tree.depth() <= maxDepthRedBlackTree(size/2));
    }

    @Test
    public void treeBehavesAsRedBlackWhenAddingNodes() {
        tree = new ASNIntervalTree();
        int size = 20;
        //
        for (long i = 0L; i < size; i++) {
            tree.add(createNode(i+1, i+2));
        }
        Assert.assertEquals(size, tree.size());
        Assert.assertTrue(tree.depth() <= maxDepthRedBlackTree(size));
    }

    @Test
    public void treeFindsRange() {

        ASNNode result = tree.findExact(range0);
        Assert.assertEquals("Tree finds node by range", range0, result.getRange());
        result = tree.findExact(range1);
        Assert.assertEquals("Tree finds node by range", range1, result.getRange());
        result = tree.findExact(range2);
        Assert.assertEquals("Tree finds node by range", range2, result.getRange());
        result = tree.findExact(range3);
        Assert.assertEquals("Tree finds node by range", range3, result.getRange());
        result = tree.findExact(range4);
        Assert.assertEquals("Tree finds node by range", range4, result.getRange());
        result = tree.findExact(range5);
        Assert.assertEquals("Tree finds node by range", range5, result.getRange());
        result = tree.findExact(range6);
        Assert.assertEquals("Tree finds node by range", range6, result.getRange());
    }

    @Test
    public void treeAddsAndRemovesNodeAtEdge() {
        ASNNode nodeA = createNode(4L, 6L);
        ASNNode nodeB = createNode(400L, 600L);
        tree.add(nodeA);
        tree.add(nodeB);
        tree.remove(nodeA);
        tree.remove(nodeB);
        Assert.assertNull("Tree finds node by range", tree.findExact(nodeA.getRange()));
        Assert.assertNull("Tree finds node by range", tree.findExact(nodeB.getRange()));
    }

    @Test
    public void treeAddsAndRemovesNodeWithOneSubnode() {
        ASNNode nodeA = createNode(5L, 8L);
        ASNNode nodeB = createNode(1L, 3L);
        tree.add(nodeA);
        tree.add(nodeB);
        tree.remove(nodeA);
        Assert.assertNull("Tree finds node by range", tree.findExact(nodeA.getRange()));
        Assert.assertEquals("Tree finds node by range", nodeB.getRange(), tree.findExact(nodeB.getRange()).getRange());
    }

    @Test
    public void treeFindsRightOverlap() {
        Assert.assertEquals("Interval tree finds left overlap", range4, tree.findFirstOverlap(AsnRange.from(61L).to(63L)).getRange());
    }

    private ASNNode createNode(long start, long end) {
        return createNode(AsnRange.from(start).to(end));
    }

    private ASNNode createNode(AsnRange range) {
        return new ASNNode(range);
    }

    private void logTree(ASNIntervalTree tree) {
        System.out.println("Tree:");
        logNode(tree.getRoot(), 0);
    }

    private void logNode(ASNNode node, int depth) {
        if (node.getLeft() != null)
            logNode(node.getLeft(), depth+1);

        for (int i = 0; i < depth; i++)
            System.out.print("                ");
        System.out.println("" + node);

        if (node.getRight() != null)
            logNode(node.getRight(), depth+1);
    }

    private int maxDepthRedBlackTree(int size) {
        return (int) Math.floor(2 * Math.log(size + 1.0) / Math.log(2.0));
    }
}
