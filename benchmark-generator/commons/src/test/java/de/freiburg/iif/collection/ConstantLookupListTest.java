package de.freiburg.iif.collection;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the IndexArrayList.
 *
 * @author Claudius Korzen
 *
 */
public class ConstantLookupListTest {

  /**
   * Test the constructor.
   */
  @Test
  public void testConstructor() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
    Assert.assertTrue(list.map.isEmpty());
  }
  
  /**
   * Test addAll(collection).
   */
  @Test
  public void testAddAll() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
    List<String> elements = Arrays.asList("A", "B", "C", "D");
    
    list.addAll(elements);
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(4, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(1, list.indexOf("B"));
    Assert.assertEquals(2, list.indexOf("C"));
    Assert.assertEquals(3, list.indexOf("D"));
    Assert.assertEquals(-1, list.indexOf("E"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals("B", list.previous("C"));
    Assert.assertEquals("C", list.previous("D"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("C", list.next("B"));
    Assert.assertEquals("D", list.next("C"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test addAll(index, collection).
   */
  @Test
  public void testAddAll2() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
    List<String> elements = Arrays.asList("A", "B", "C");
    
    list.add("X");
    list.add("Z");
    list.addAll(1, elements);
   
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(5, list.map.size());
    Assert.assertEquals(0, list.indexOf("X"));
    Assert.assertEquals(1, list.indexOf("A"));
    Assert.assertEquals(2, list.indexOf("B"));
    Assert.assertEquals(3, list.indexOf("C"));
    Assert.assertEquals(4, list.indexOf("Z"));
    Assert.assertEquals(-1, list.indexOf("E"));
    Assert.assertEquals(null, list.previous("X"));
    Assert.assertEquals("X", list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals("B", list.previous("C"));
    Assert.assertEquals("C", list.previous("Z"));
    Assert.assertEquals("A", list.next("X"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("C", list.next("B"));
    Assert.assertEquals("Z", list.next("C"));
    Assert.assertEquals(null, list.next("Z"));
  }
  
  /**
   * Test addAll(element).
   */
  @Test
  public void testAdd() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
        
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");
   
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(4, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(1, list.indexOf("B"));
    Assert.assertEquals(2, list.indexOf("C"));
    Assert.assertEquals(3, list.indexOf("D"));
    Assert.assertEquals(-1, list.indexOf("E"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals("B", list.previous("C"));
    Assert.assertEquals("C", list.previous("D"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("C", list.next("B"));
    Assert.assertEquals("D", list.next("C"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test addAll(index, element).
   */
  @Test
  public void testAdd2() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
        
    list.add("A");
    list.add("C");
    list.add("D");
    list.add(1, "B");
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(4, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(1, list.indexOf("B"));
    Assert.assertEquals(2, list.indexOf("C"));
    Assert.assertEquals(3, list.indexOf("D"));
    Assert.assertEquals(-1, list.indexOf("E"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals("B", list.previous("C"));
    Assert.assertEquals("C", list.previous("D"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("C", list.next("B"));
    Assert.assertEquals("D", list.next("C"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test removeAll().
   */
  @Test
  public void testRemoveAll() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
        
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");
    list.removeAll(Arrays.asList("B", "C"));
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(2, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(-1, list.indexOf("B"));
    Assert.assertEquals(-1, list.indexOf("C"));
    Assert.assertEquals(1, list.indexOf("D"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals(null, list.previous("B"));
    Assert.assertEquals(null, list.previous("C"));
    Assert.assertEquals("A", list.previous("D"));
    Assert.assertEquals("D", list.next("A"));
    Assert.assertEquals(null, list.next("B"));
    Assert.assertEquals(null, list.next("C"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test remove(element).
   */
  @Test
  public void testRemove() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
        
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");
    list.remove("C");
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(3, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(1, list.indexOf("B"));
    Assert.assertEquals(-1, list.indexOf("C"));
    Assert.assertEquals(2, list.indexOf("D"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals(null, list.previous("C"));
    Assert.assertEquals("B", list.previous("D"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("D", list.next("B"));
    Assert.assertEquals(null, list.next("C"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test remove(index, element).
   */
  @Test
  public void testRemove2() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
        
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");
    list.remove(1);
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(3, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(-1, list.indexOf("B"));
    Assert.assertEquals(1, list.indexOf("C"));
    Assert.assertEquals(2, list.indexOf("D"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals(null, list.previous("B"));
    Assert.assertEquals("A", list.previous("C"));
    Assert.assertEquals("C", list.previous("D"));
    Assert.assertEquals("C", list.next("A"));
    Assert.assertEquals(null, list.next("B"));
    Assert.assertEquals("D", list.next("C"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test set(index, element).
   */
  @Test
  public void testSet() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
            
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");
    list.set(2, "X");
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(4, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(1, list.indexOf("B"));
    Assert.assertEquals(-1, list.indexOf("C"));
    Assert.assertEquals(2, list.indexOf("X"));
    Assert.assertEquals(3, list.indexOf("D"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals(null, list.previous("C"));
    Assert.assertEquals("B", list.previous("X"));
    Assert.assertEquals("X", list.previous("D"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("X", list.next("B"));
    Assert.assertEquals(null, list.next("C"));
    Assert.assertEquals("D", list.next("X"));
    Assert.assertEquals(null, list.next("D"));
  }
  
  /**
   * Test set(index, element).
   */
  @Test
  public void testReplace() {
    ConstantLookupList<String> list = new ConstantLookupList<>();
            
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");
    list.replace("C", Arrays.asList("X"));
    
    Assert.assertTrue(!list.map.isEmpty());
    Assert.assertEquals(4, list.map.size());
    Assert.assertEquals(0, list.indexOf("A"));
    Assert.assertEquals(1, list.indexOf("B"));
    Assert.assertEquals(-1, list.indexOf("C"));
    Assert.assertEquals(2, list.indexOf("X"));
    Assert.assertEquals(3, list.indexOf("D"));
    Assert.assertEquals(null, list.previous("A"));
    Assert.assertEquals("A", list.previous("B"));
    Assert.assertEquals(null, list.previous("C"));
    Assert.assertEquals("B", list.previous("X"));
    Assert.assertEquals("X", list.previous("D"));
    Assert.assertEquals("B", list.next("A"));
    Assert.assertEquals("X", list.next("B"));
    Assert.assertEquals(null, list.next("C"));
    Assert.assertEquals("D", list.next("X"));
    Assert.assertEquals(null, list.next("D"));
  }
}
