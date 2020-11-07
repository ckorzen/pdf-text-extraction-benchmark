package de.freiburg.iif.rtree;

import static de.freiburg.iif.mock.HasRectangleMock.mockHasRectangle;
import static de.freiburg.iif.mock.RectangleMock.mockRectangle;
import static de.freiburg.iif.mock.LineMock.mockLine;
import static de.freiburg.iif.mock.PointMock.mockPoint;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.freiburg.iif.model.HasRectangle;

/**
 * Tests for the SimpleRTree.
 * 
 * @author Claudius Korzen
 * 
 */
public class SimpleRTreeTest {
  /**
   * The first tree to test.
   */
  protected static SimpleRTree<HasRectangle> rtree1;
  /**
   * The index entries of rtree1.
   */
  protected static List<HasRectangle> indexEntries1;
  
  /**
   * The first tree to test.
   */
  protected static SimpleRTree<HasRectangle> rtree2;
  /**
   * The index entries of rtree2.
   */
  protected static List<HasRectangle> indexEntries2;
  
  /**
   * The first tree to test.
   */
  protected static SimpleRTree<HasRectangle> rtree3;
  /**
   * The index entries of rtree3.
   */
  protected static List<HasRectangle> indexEntries3;
  
  /**
   * Setup first R-Tree, where all the index entries fits into the root node.
   */
  @BeforeClass
  public static void setupRTree1() {
    rtree1 = new SimpleRTree<HasRectangle>(2, 4);
    indexEntries1 = new ArrayList<HasRectangle>();
    
    Assert.assertEquals(2, rtree1.minNumEntriesPerNode);
    Assert.assertEquals(4, rtree1.maxNumEntriesPerNode);
    Assert.assertEquals(0, rtree1.size());
    Assert.assertNotNull(rtree1.root);
    
    HasRectangle hr1 = mockHasRectangle(1f, 1f, 3f, 3f);
    rtree1.insert(hr1);
    indexEntries1.add(hr1);
    Assert.assertEquals(1, rtree1.size());

    HasRectangle hr2 = mockHasRectangle(2f, 2f, 4f, 6f);
    rtree1.insert(hr2);
    indexEntries1.add(hr2);
    Assert.assertEquals(2, rtree1.size());

    HasRectangle hr3 = mockHasRectangle(5f, 4f, 7f, 6f);
    rtree1.insert(hr3);
    indexEntries1.add(hr3);
    Assert.assertEquals(3, rtree1.size());
  }
  
  /**
   * Setup second R-Tree, where nodes must be split.
   */
  @BeforeClass
  public static void setupRTree2() {
    rtree2 = new SimpleRTree<HasRectangle>(2, 4);
    indexEntries2 = new ArrayList<HasRectangle>();
    
    Assert.assertEquals(2, rtree2.minNumEntriesPerNode);
    Assert.assertEquals(4, rtree2.maxNumEntriesPerNode);
    Assert.assertEquals(0, rtree2.size());
    Assert.assertNotNull(rtree2.root);

    HasRectangle hr1 = mockHasRectangle(1f, 1f, 3f, 3f);
    rtree2.insert(hr1);
    indexEntries2.add(hr1);
        
    HasRectangle hr2 = mockHasRectangle(2f, 2f, 4f, 4f);
    rtree2.insert(hr2);
    indexEntries2.add(hr2);
    
    HasRectangle hr3 = mockHasRectangle(3f, 3f, 5f, 5f);
    rtree2.insert(hr3);
    indexEntries2.add(hr3);
    
    HasRectangle hr4 = mockHasRectangle(4f, 4f, 6f, 6f);
    rtree2.insert(hr4);
    indexEntries2.add(hr4);
    
    HasRectangle hr5 = mockHasRectangle(5f, 5f, 7f, 7f);
    rtree2.insert(hr5);
    indexEntries2.add(hr5);
    
    HasRectangle hr6 = mockHasRectangle(6f, 6f, 8f, 8f);
    rtree2.insert(hr6);
    indexEntries2.add(hr6);
    
    HasRectangle hr7 = mockHasRectangle(7f, 7f, 9f, 9f);
    rtree2.insert(hr7);
    indexEntries2.add(hr7);
    
    HasRectangle hr8 = mockHasRectangle(8f, 8f, 10f, 10f);
    rtree2.insert(hr8);
    indexEntries2.add(hr8);
    
    HasRectangle hr9 = mockHasRectangle(9f, 9f, 11f, 11f);
    rtree2.insert(hr9);
    indexEntries2.add(hr9);
    
    HasRectangle hr10 = mockHasRectangle(10f, 10f, 12f, 12f);
    rtree2.insert(hr10);
    indexEntries2.add(hr10);
    
    HasRectangle hr11 = mockHasRectangle(11f, 11f, 13f, 13f);
    rtree2.insert(hr11);
    indexEntries2.add(hr11);
    
    HasRectangle hr12 = mockHasRectangle(12f, 12f, 14f, 14f);
    rtree2.insert(hr12);
    indexEntries2.add(hr12);
    
    HasRectangle hr13 = mockHasRectangle(13f, 13f, 15f, 15f);
    rtree2.insert(hr13);
    indexEntries2.add(hr13);
    
    HasRectangle hr14 = mockHasRectangle(14f, 14f, 16f, 16f);
    rtree2.insert(hr14);
    indexEntries2.add(hr14);
    
    HasRectangle hr15 = mockHasRectangle(15f, 15f, 17f, 17f);
    rtree2.insert(hr15);
    indexEntries2.add(hr15);
    
    HasRectangle hr16 = mockHasRectangle(16f, 16f, 18f, 18f);
    rtree2.insert(hr16);
    indexEntries2.add(hr16);
    
    HasRectangle hr17 = mockHasRectangle(17f, 17f, 19f, 19f);
    rtree2.insert(hr17);
    indexEntries2.add(hr17);
    
    Assert.assertEquals(17, rtree2.size());
  }
  
  /**
   * Setup third R-Tree with the scenario given in the paper
   * (http://www-db.deis.unibo.it/courses/SI-LS/papers/Gut84.pdf).
   */
  @BeforeClass
  public static void setupRTree3() {
    rtree3 = new SimpleRTree<HasRectangle>(2, 4);
    indexEntries3 = new ArrayList<HasRectangle>();
    
    HasRectangle hr1 = mockHasRectangle(4, 1, 5, 7);
    rtree3.insert(hr1);
    indexEntries3.add(hr1);
    
    HasRectangle hr2 = mockHasRectangle(6, 5, 10, 6);
    rtree3.insert(hr2);
    indexEntries3.add(hr2);
    
    HasRectangle hr3 = mockHasRectangle(6, 1, 7, 7);
    rtree3.insert(hr3);
    indexEntries3.add(hr3);
    
    HasRectangle hr4 = mockHasRectangle(1, 5, 5, 6);
    rtree3.insert(hr4);
    indexEntries3.add(hr4);
    
    Assert.assertEquals(4, rtree3.size());
  }
  
  /**
   * Tests, if the constructor throws an exception, if the input parameters are
   * not valid (because maxNumEntriesPerNode is too small).
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor1() {
    SimpleRTree<HasRectangle> rTree = new SimpleRTree<>(2, 3);

    Assert.assertEquals(0, rTree.size());
  }

  /**
   * Tests, if the constructor doesn't throw an exception, if the input 
   * parameters are valid.
   */
  @Test()
  public void testConstructor2() {
    SimpleRTree<HasRectangle> rTree = new SimpleRTree<>(2, 4);

    Assert.assertEquals(0, rTree.size());
    Assert.assertNotNull(rTree.root);
  }
  
  /**
   * Tests overlappedBy(Rectangle.class) on tree 1.
   */
  @Test
  public void testOverlappedByRectangle1() {
    List<HasRectangle> rects0 = rtree1.overlappedBy(mockRectangle(5, 1, 7, 4));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree1.overlappedBy(mockRectangle(0, 0, 2, 2));
    Assert.assertEquals(1, rects1.size());
    Assert.assertTrue(rects1.contains(indexEntries1.get(0)));

    List<HasRectangle> rects2 = rtree1.overlappedBy(mockRectangle(1, 1, 4, 6));
    Assert.assertEquals(2, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects2.contains(indexEntries1.get(1)));

    List<HasRectangle> rects3 = rtree1.overlappedBy(mockRectangle(1, 5, 7, 7));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries1.get(1)));
    Assert.assertTrue(rects3.contains(indexEntries1.get(2)));

    List<HasRectangle> rects4 = rtree1.overlappedBy(mockRectangle(1, 1, 7, 7));
    Assert.assertEquals(3, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects4.contains(indexEntries1.get(1)));
    Assert.assertTrue(rects4.contains(indexEntries1.get(2)));
  }

  /**
   * Tests overlappedBy(Rectangle.class) on tree 2.
   */
  @Test
  public void testOverlappedByRectangle2() {
    List<HasRectangle> rects0 = rtree2.overlappedBy(mockRectangle(0, 4, 3, 7));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree2.overlappedBy(mockRectangle(4, 0, 7, 3));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree2.overlappedBy(mockRectangle(0, 0, 1, 1));
    Assert.assertEquals(0, rects2.size());

    List<HasRectangle> rects3 = rtree2.overlappedBy(mockRectangle(0, 0, 2, 2));
    Assert.assertEquals(1, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries2.get(0)));

    List<HasRectangle> rects4 = rtree2.overlappedBy(mockRectangle(0, 0, 3, 3));
    Assert.assertEquals(2, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries2.get(0)));
    Assert.assertTrue(rects4.contains(indexEntries2.get(1)));

    List<HasRectangle> rect5 = rtree2.overlappedBy(mockRectangle(0, 0, 19, 19));
    Assert.assertEquals(17, rect5.size());
  }

  /**
   * Tests overlappedBy(Rectangle.class) on tree 3.
   */
  @Test
  public void testOverlappedByRectangle3() {
    List<HasRectangle> rects1 = rtree3.overlappedBy(mockRectangle(0, 0, 4, 4));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree3.overlappedBy(mockRectangle(0, 0, 6, 6));
    Assert.assertEquals(2, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries3.get(0)));
    Assert.assertTrue(rects2.contains(indexEntries3.get(3)));

    List<HasRectangle> rects3 = rtree3.overlappedBy(mockRectangle(6, 1, 10, 6));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries3.get(1)));
    Assert.assertTrue(rects3.contains(indexEntries3.get(2)));

    List<HasRectangle> rect4 = rtree3.overlappedBy(mockRectangle(0, 0, 10, 10));
    Assert.assertEquals(4, rect4.size());
    Assert.assertTrue(rect4.contains(indexEntries3.get(0)));
    Assert.assertTrue(rect4.contains(indexEntries3.get(1)));
    Assert.assertTrue(rect4.contains(indexEntries3.get(2)));
    Assert.assertTrue(rect4.contains(indexEntries3.get(3)));
  }
  
  /**
   * Tests containedBy(Rectangle.class), on tree 1.
   */
  @Test
  public void testContainedByRectangle1() {
    List<HasRectangle> rects0 = rtree1.containedBy(mockRectangle(0, 0, 1, 1));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree1.containedBy(mockRectangle(0, 0, 2, 2));
    Assert.assertEquals(0, rects1.size());
    
    List<HasRectangle> rects2 = rtree1.containedBy(mockRectangle(0, 0, 3, 3));
    Assert.assertEquals(1, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries1.get(0)));
    
    List<HasRectangle> rects3 = rtree1.containedBy(mockRectangle(1, 1, 4, 6));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects3.contains(indexEntries1.get(1)));
    
    List<HasRectangle> rects4 = rtree1.containedBy(mockRectangle(4, 0, 6, 7));
    Assert.assertEquals(0, rects4.size());

    List<HasRectangle> rects5 = rtree1.containedBy(mockRectangle(4, 0, 7, 7));
    Assert.assertEquals(1, rects5.size());
    Assert.assertTrue(rects5.contains(indexEntries1.get(2)));
    
    List<HasRectangle> rects6 = rtree1.containedBy(mockRectangle(0, 0, 9, 9));
    Assert.assertEquals(3, rects6.size());
    Assert.assertTrue(rects6.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects6.contains(indexEntries1.get(1)));
    Assert.assertTrue(rects6.contains(indexEntries1.get(2)));
  }
  
  /**
   * Tests containedBy(Rectangle.class), on tree 2.
   */
  @Test
  public void testContainedByRectangle2() {    
    List<HasRectangle> rects0 = rtree2.containedBy(mockRectangle(0, 0, 1, 1));
    Assert.assertEquals(0, rects0.size());
    
    List<HasRectangle> rects1 = rtree2.containedBy(mockRectangle(0, 0, 2, 2));
    Assert.assertEquals(0, rects1.size());
    
    List<HasRectangle> rects2 = rtree2.containedBy(mockRectangle(0, 0, 3, 3));
    Assert.assertEquals(1, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries2.get(0)));
    
    List<HasRectangle> rects3 = rtree2.containedBy(mockRectangle(0, 0, 4, 4));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries2.get(0)));
    Assert.assertTrue(rects3.contains(indexEntries2.get(1)));
    
    List<HasRectangle> rects4 = rtree2.containedBy(mockRectangle(0, 0, 5, 5));
    Assert.assertEquals(3, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries2.get(0)));
    Assert.assertTrue(rects4.contains(indexEntries2.get(1)));
    Assert.assertTrue(rects4.contains(indexEntries2.get(2)));
    
    List<HasRectangle> rects5 = rtree2.containedBy(mockRectangle(2, 2, 6, 6));
    Assert.assertEquals(3, rects5.size());
    Assert.assertTrue(rects5.contains(indexEntries2.get(1)));
    Assert.assertTrue(rects5.contains(indexEntries2.get(2)));
    Assert.assertTrue(rects5.contains(indexEntries2.get(3)));
    
    List<HasRectangle> rects6 = rtree2.containedBy(mockRectangle(3, 3, 5, 5));
    Assert.assertEquals(1, rects6.size());
    Assert.assertTrue(rects6.contains(indexEntries2.get(2)));
    
    List<HasRectangle> rects7 = rtree2.containedBy(mockRectangle(0, 0, 20, 20));
    Assert.assertEquals(17, rects7.size());
    
    List<HasRectangle> rects8 = rtree2.containedBy(mockRectangle(2, 0, 4, 2));
    Assert.assertEquals(0, rects8.size());
  }
  
  /**
   * Tests containedBy(Rectangle.class) on tree 3.
   */
  @Test
  public void testContainedByRectangle3() {    
    List<HasRectangle> rects1 = rtree3.containedBy(mockRectangle(0, 0, 2, 2));
    Assert.assertEquals(0, rects1.size());
    
    List<HasRectangle> rects2 = rtree3.containedBy(mockRectangle(0, 0, 5, 7));
    Assert.assertEquals(2, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries3.get(0)));
    Assert.assertTrue(rects2.contains(indexEntries3.get(3)));
    
    List<HasRectangle> rects3 = rtree3.containedBy(mockRectangle(2, 0, 5, 7));
    Assert.assertEquals(1, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries3.get(0)));
    
    List<HasRectangle> rects4 = rtree3.containedBy(mockRectangle(6, 1, 9, 7));
    Assert.assertEquals(1, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries3.get(2)));
    
    List<HasRectangle> rects5 = rtree3.containedBy(mockRectangle(6, 1, 10, 7));
    Assert.assertEquals(2, rects5.size());
    Assert.assertTrue(rects5.contains(indexEntries3.get(1)));
    Assert.assertTrue(rects5.contains(indexEntries3.get(2)));
    
    List<HasRectangle> rects6 = rtree3.containedBy(mockRectangle(3, 2, 8, 3));
    Assert.assertEquals(0, rects6.size());
    
    List<HasRectangle> rects7 = rtree3.containedBy(mockRectangle(0, 4, 10, 7));
    Assert.assertEquals(2, rects7.size());
    Assert.assertTrue(rects7.contains(indexEntries3.get(1)));
    Assert.assertTrue(rects7.contains(indexEntries3.get(3)));
    
    List<HasRectangle> rects8 = rtree3.containedBy(mockRectangle(0, 0, 10, 10));
    Assert.assertEquals(4, rects8.size());
  }
  
  /**
   * Tests overlappedBy(Line.class) on tree 1.
   */
  @Test
  public void testOverlappedByLine1() {
    List<HasRectangle> rects0 = rtree1.overlappedBy(mockLine(0, 0, 0, 10));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree1.overlappedBy(mockLine(1, 0, 1, 10));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree1.overlappedBy(mockLine(2.5f, 0, 2.5f, 9));
    Assert.assertEquals(2, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects2.contains(indexEntries1.get(1)));

    List<HasRectangle> rects3 = rtree1.overlappedBy(mockLine(1, 5, 5, 5));
    Assert.assertEquals(1, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries1.get(1)));
    
    List<HasRectangle> rects4 = rtree1.overlappedBy(mockLine(0, 5, 10, 5));
    Assert.assertEquals(2, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries1.get(1)));
    Assert.assertTrue(rects4.contains(indexEntries1.get(2)));

    List<HasRectangle> rects5 = rtree1.overlappedBy(mockRectangle(0, 0, 5, 5));
    Assert.assertEquals(2, rects5.size());
    Assert.assertTrue(rects5.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects5.contains(indexEntries1.get(1)));
  }
  
  /**
   * Tests overlappedBy(Line.class) on tree 2.
   */
  @Test
  public void testOverlappedByLine2() {
    List<HasRectangle> rects0 = rtree2.overlappedBy(mockLine(0, 0, 0, 10));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree2.overlappedBy(mockLine(1, 0, 1, 10));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree2.overlappedBy(mockLine(2, 0, 2, 5));
    Assert.assertEquals(1, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries2.get(0)));

    List<HasRectangle> rects3 = rtree2.overlappedBy(mockLine(2.5f, 0, 2.5f, 9));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries2.get(0)));
    Assert.assertTrue(rects3.contains(indexEntries2.get(1)));

    List<HasRectangle> rects4 = rtree2.overlappedBy(mockLine(0, 3.5f, 9, 3.5f));
    Assert.assertEquals(2, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries2.get(1)));
    Assert.assertTrue(rects4.contains(indexEntries2.get(2)));

    List<HasRectangle> rect5 = rtree2.overlappedBy(mockRectangle(0, 0, 20, 20));
    Assert.assertEquals(17, rect5.size());
  }
  
  /**
   * Tests overlappedBy(Line.class) on tree 3.
   */
  @Test
  public void testOverlappedByLine3() {
    List<HasRectangle> rects1 = rtree3.overlappedBy(mockLine(1, 0, 1, 9));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree3.overlappedBy(mockLine(5.5f, 0, 5.5f, 9));
    Assert.assertEquals(0, rects2.size());

    List<HasRectangle> rects3 = rtree3.overlappedBy(mockLine(4.5f, 0, 4.5f, 9));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries3.get(0)));
    Assert.assertTrue(rects3.contains(indexEntries3.get(3)));
        
    List<HasRectangle> rects4 = rtree3.overlappedBy(mockLine(0, 2, 9, 2));
    Assert.assertEquals(2, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries3.get(0)));
    Assert.assertTrue(rects4.contains(indexEntries3.get(2)));

    List<HasRectangle> rect5 = rtree3.overlappedBy(mockLine(0, 5.5f, 10, 5.5f));
    Assert.assertEquals(4, rect5.size());
    Assert.assertTrue(rect5.contains(indexEntries3.get(0)));
    Assert.assertTrue(rect5.contains(indexEntries3.get(1)));
    Assert.assertTrue(rect5.contains(indexEntries3.get(2)));
    Assert.assertTrue(rect5.contains(indexEntries3.get(3)));
  }
  
  /**
   * Tests overlappedBy(Point.class) on tree 1.
   */
  @Test
  public void testOverlappedByPoint1() {
    List<HasRectangle> rects0 = rtree1.overlappedBy(mockPoint(0, 0));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree1.overlappedBy(mockPoint(1, 1));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree1.overlappedBy(mockPoint(2, 2));
    Assert.assertEquals(1, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries1.get(0)));

    List<HasRectangle> rects3 = rtree1.overlappedBy(mockPoint(2.5f, 2.5f));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries1.get(0)));
    Assert.assertTrue(rects3.contains(indexEntries1.get(1)));
  }
  
  /**
   * Tests overlappedBy(Point.class) on tree 2.
   */
  @Test
  public void testOverlappedByPoint2() {
    List<HasRectangle> rects0 = rtree2.overlappedBy(mockPoint(0, 0));
    Assert.assertEquals(0, rects0.size());

    List<HasRectangle> rects1 = rtree2.overlappedBy(mockPoint(1, 1));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree2.overlappedBy(mockPoint(2, 2));
    Assert.assertEquals(1, rects2.size());
    Assert.assertTrue(rects2.contains(indexEntries2.get(0)));

    List<HasRectangle> rects3 = rtree2.overlappedBy(mockPoint(4.5f, 4.5f));
    Assert.assertEquals(2, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries2.get(2)));
    Assert.assertTrue(rects3.contains(indexEntries2.get(3)));
    
    List<HasRectangle> rects4 = rtree2.overlappedBy(mockPoint(5, 1));
    Assert.assertEquals(0, rects4.size());
  }
  
  /**
   * Tests overlappedBy(Point.class) on tree 3.
   */
  @Test
  public void testOverlappedByPoint3() {
    List<HasRectangle> rects1 = rtree3.overlappedBy(mockPoint(0, 0));
    Assert.assertEquals(0, rects1.size());

    List<HasRectangle> rects2 = rtree3.overlappedBy(mockPoint(1, 1));
    Assert.assertEquals(0, rects2.size());

    List<HasRectangle> rects3 = rtree3.overlappedBy(mockPoint(4.5f, 2));
    Assert.assertEquals(1, rects3.size());
    Assert.assertTrue(rects3.contains(indexEntries3.get(0)));
        
    List<HasRectangle> rects4 = rtree3.overlappedBy(mockPoint(4.5f, 5.5f));
    Assert.assertEquals(2, rects4.size());
    Assert.assertTrue(rects4.contains(indexEntries3.get(0)));
    Assert.assertTrue(rects4.contains(indexEntries3.get(3)));

    List<HasRectangle> rects5 = rtree3.overlappedBy(mockPoint(6.5f, 5.5f));
    Assert.assertEquals(2, rects5.size());
    Assert.assertTrue(rects5.contains(indexEntries3.get(1)));
    Assert.assertTrue(rects5.contains(indexEntries3.get(2)));
  }
}
