package model;

import org.junit.Before;
import org.junit.Test;
import treeParser.TreeParser;

import static org.junit.Assert.*;

/**
 * Created by julian on 10.06.17.
 */
public class SampleTest {
    /**
     * Create nodes with 6 nodes and the following structure:
     * 1 is parent of 2 and 3, 2 is parent of 4 and 5, 4 is parent of 6:
     * Then creates a sample where all of the nodes are put with the following abundances:
     * 1: 1
     * 2: 2
     * 3: 3
     * 4: 0
     * 5: 5
     * 6: 6
     * <p>
     * Then tests the recursive counts:
     * 1: 6+5+0+3+2+1 = 17
     * 2: 6+5+0+2 = 13
     * 3: 3
     * 4: 0+6=6
     * 5: 5
     * 6: 6
     *
     * @throws Exception
     */
    @Test
    public void testTaxonCountRecursive() throws Exception {
        TaxonNode node1, node2, node3, node4, node5, node6;
        Sample sample;
        node1 = new TaxonNode(1, null, 0);
        node2 = new TaxonNode(2, null, 1);
        node3 = new TaxonNode(3, null, 1);
        node4 = new TaxonNode(4, null, 2);
        node5 = new TaxonNode(5, null, 2);
        node6 = new TaxonNode(6, null, 5);

        node1.getChildNodeList().add(node2);
        node1.getChildNodeList().add(node3);
        node2.getChildNodeList().add(node4);
        node2.getChildNodeList().add(node5);
        node4.getChildNodeList().add(node6);


        sample = new Sample();
        sample.getTaxa2CountMap().put(node1, 1);
        sample.getTaxa2CountMap().put(node2, 2);
        sample.getTaxa2CountMap().put(node3, 3);
        sample.getTaxa2CountMap().put(node4, 0);
        sample.getTaxa2CountMap().put(node5, 5);
        sample.getTaxa2CountMap().put(node6, 6);

        assertEquals(17, sample.getTaxonCountRecursive(node1));
        assertEquals(13, sample.getTaxonCountRecursive(node2));
        assertEquals(3, sample.getTaxonCountRecursive(node3));
        assertEquals(6, sample.getTaxonCountRecursive(node4));
        assertEquals(5, sample.getTaxonCountRecursive(node5));
        assertEquals(6, sample.getTaxonCountRecursive(node6));
    }

    /**
     * Parses the tree, then tests the following situation:
     * The taxon with the id 32 has 33,34 and 35 as children (amongst others).
     * So if we create a sample with these counts:
     * 33   13
     * 34   15
     * 35   14
     *
     * we expect the recursive count of taxon 32 to be 42.
     * @throws Exception
     */
    @Test
    public void testEmptyTaxonCount() throws Exception {
        TreeParser parser = new TreeParser();
        parser.parseTree("./res/nodes.dmp", "./res/names.dmp");
        TaxonTree taxonTree = parser.getTaxonTree();
        TaxonNode node32 = taxonTree.getNodeForID(32);
        TaxonNode node33 = taxonTree.getNodeForID(33);
        TaxonNode node34 = taxonTree.getNodeForID(34);
        TaxonNode node35 = taxonTree.getNodeForID(35);
        Sample sample = new Sample();
        sample.getTaxa2CountMap().put(node33,13);
        sample.getTaxa2CountMap().put(node34,15);
        sample.getTaxa2CountMap().put(node35,14);
        assertEquals(42,sample.getTaxonCountRecursive(node32));

    }
}