package de.freiburg.iif.model.simple;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for SimpleLine.
 *
 * @author Claudius Korzen
 */
public class SimpleLineTest {

  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor1() {
    SimpleLine line = new SimpleLine();
    Assert.assertEquals(0f, line.getStartX(), 0.0001f);
    Assert.assertEquals(0f, line.getStartY(), 0.0001f);
    Assert.assertEquals(0f, line.getEndX(), 0.0001f);
    Assert.assertEquals(0f, line.getEndY(), 0.0001f);
    Assert.assertEquals("[0.0,0.0,0.0,0.0]", line.getRectangle().toString());
    Assert.assertEquals("(0.0,0.0)", line.getStartPoint().toString());
    Assert.assertEquals("(0.0,0.0)", line.getEndPoint().toString());
  }
 
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor2() {
    SimpleLine l = new SimpleLine(5, 10, 15, 20);
    Assert.assertEquals(5f, l.getStartX(), 0.0001f);
    Assert.assertEquals(10f, l.getStartY(), 0.0001f);
    Assert.assertEquals(15f, l.getEndX(), 0.0001f);
    Assert.assertEquals(20f, l.getEndY(), 0.0001f);
    Assert.assertEquals("[5.0,10.0,15.0,20.0]", l.getRectangle().toString());
    Assert.assertEquals("(5.0,10.0)", l.getStartPoint().toString());
    Assert.assertEquals("(15.0,20.0)", l.getEndPoint().toString());
  }
  
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor3() {
    SimplePoint startPoint = new SimplePoint(10, 20);
    SimplePoint endPoint = new SimplePoint(15, 25);
    SimpleLine l = new SimpleLine(startPoint, endPoint);
    Assert.assertEquals(10f, l.getStartX(), 0.0001f);
    Assert.assertEquals(20f, l.getStartY(), 0.0001f);
    Assert.assertEquals(15f, l.getEndX(), 0.0001f);
    Assert.assertEquals(25f, l.getEndY(), 0.0001f);
    Assert.assertEquals("[10.0,20.0,15.0,25.0]", l.getRectangle().toString());
    Assert.assertEquals("(10.0,20.0)", l.getStartPoint().toString());
    Assert.assertEquals("(15.0,25.0)", l.getEndPoint().toString());
  }
}
