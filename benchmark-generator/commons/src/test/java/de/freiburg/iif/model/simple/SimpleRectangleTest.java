package de.freiburg.iif.model.simple;

import org.junit.Assert;
import org.junit.Test;

import de.freiburg.iif.model.Rectangle;

/**
 * Tests for SimpleRectangle.
 *
 * @author Claudius Korzen
 */
public class SimpleRectangleTest {
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor1() {
    SimpleRectangle rectangle = new SimpleRectangle();
    Assert.assertEquals(0f, rectangle.getMinX(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getMinY(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getMaxX(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getMaxY(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getArea(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getHeight(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getWidth(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getXMidpoint(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getYMidpoint(), 0.0001f);
    Assert.assertEquals("(0.0,0.0)", rectangle.getLowerLeft().toString());
    Assert.assertEquals("(0.0,0.0)", rectangle.getLowerRight().toString());
    Assert.assertEquals("(0.0,0.0)", rectangle.getUpperLeft().toString());
    Assert.assertEquals("(0.0,0.0)", rectangle.getUpperRight().toString());
    Assert.assertEquals("(0.0,0.0)", rectangle.getMidpoint().toString());
    Assert.assertEquals("(0.0,0.0)", rectangle.getMidpoint().toString());
  }
  
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor2() {
    SimpleRectangle rectangle = new SimpleRectangle(10, 20);
    Assert.assertEquals(0f, rectangle.getMinX(), 0.0001f);
    Assert.assertEquals(0f, rectangle.getMinY(), 0.0001f);
    Assert.assertEquals(10f, rectangle.getMaxX(), 0.0001f);
    Assert.assertEquals(20f, rectangle.getMaxY(), 0.0001f);
    Assert.assertEquals(200f, rectangle.getArea(), 0.0001f);
    Assert.assertEquals(10f, rectangle.getWidth(), 0.0001f);
    Assert.assertEquals(20f, rectangle.getHeight(), 0.0001f);
    Assert.assertEquals(5f, rectangle.getXMidpoint(), 0.0001f);
    Assert.assertEquals(10f, rectangle.getYMidpoint(), 0.0001f);
    Assert.assertEquals("(0.0,0.0)", rectangle.getLowerLeft().toString());
    Assert.assertEquals("(10.0,0.0)", rectangle.getLowerRight().toString());
    Assert.assertEquals("(0.0,20.0)", rectangle.getUpperLeft().toString());
    Assert.assertEquals("(10.0,20.0)", rectangle.getUpperRight().toString());
    Assert.assertEquals("(5.0,10.0)", rectangle.getMidpoint().toString());
  }
  
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor3() {
    SimpleRectangle rectangle = new SimpleRectangle(10, 20, 20, 40);
    Assert.assertEquals(10f, rectangle.getMinX(), 0.0001f);
    Assert.assertEquals(20f, rectangle.getMinY(), 0.0001f);
    Assert.assertEquals(20f, rectangle.getMaxX(), 0.0001f);
    Assert.assertEquals(40f, rectangle.getMaxY(), 0.0001f);
    Assert.assertEquals(200f, rectangle.getArea(), 0.0001f);
    Assert.assertEquals(10f, rectangle.getWidth(), 0.0001f);
    Assert.assertEquals(20f, rectangle.getHeight(), 0.0001f);
    Assert.assertEquals(15f, rectangle.getXMidpoint(), 0.0001f);
    Assert.assertEquals(30f, rectangle.getYMidpoint(), 0.0001f);
    Assert.assertEquals("(10.0,20.0)", rectangle.getLowerLeft().toString());
    Assert.assertEquals("(20.0,20.0)", rectangle.getLowerRight().toString());
    Assert.assertEquals("(10.0,40.0)", rectangle.getUpperLeft().toString());
    Assert.assertEquals("(20.0,40.0)", rectangle.getUpperRight().toString());
    Assert.assertEquals("(15.0,30.0)", rectangle.getMidpoint().toString());
  }
  
  /**
   * Tests the default constructor.
   */
  @Test
  public void testMoveTo() {
    SimpleRectangle rectangle = new SimpleRectangle(10, 20, 20, 40);
    
    rectangle.moveTo(0, 0);
    Assert.assertEquals("[0.0,0.0,10.0,20.0]", rectangle.toString());
    
    rectangle.moveTo(5, 10);
    Assert.assertEquals("[5.0,10.0,15.0,30.0]", rectangle.toString());
    
    rectangle.moveTo(7, 2);
    Assert.assertEquals("[7.0,2.0,17.0,22.0]", rectangle.toString());
  }
  
  /**
   * Tests the default constructor.
   */
  @Test
  public void testShiftBy() {
    SimpleRectangle rectangle = new SimpleRectangle(10, 20, 20, 40);
    
    rectangle.shiftBy(0, 0);
    Assert.assertEquals("[10.0,20.0,20.0,40.0]", rectangle.toString());
    
    rectangle.shiftBy(5, 10);
    Assert.assertEquals("[15.0,30.0,25.0,50.0]", rectangle.toString());
    
    rectangle.shiftBy(7, 2);
    Assert.assertEquals("[22.0,32.0,32.0,52.0]", rectangle.toString());
    
    rectangle.shiftBy(-5, -10);
    Assert.assertEquals("[17.0,22.0,27.0,42.0]", rectangle.toString());
  }
  
  /**
   * Tests the method computeOverlapRatio().
   */
  @Test
  public void testComputeOverlapRatio() {
    Rectangle rect1 = new SimpleRectangle(0, 0, 10, 10);
    Rectangle rect2 = new SimpleRectangle(5, 5, 15, 15);
    Rectangle rect3 = new SimpleRectangle(10, 10, 20, 20);
    Assert.assertEquals(1f, rect1.computeOverlapRatio(rect1), 0.0001f);
    Assert.assertEquals(0.25f, rect2.computeOverlapRatio(rect1), 0.0001f);
    Assert.assertEquals(0f, rect3.computeOverlapRatio(rect1), 0.0001f);
  }
}
