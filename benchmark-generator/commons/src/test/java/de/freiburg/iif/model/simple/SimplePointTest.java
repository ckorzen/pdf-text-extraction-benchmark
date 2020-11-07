package de.freiburg.iif.model.simple;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for SimplePoint.
 *
 * @author Claudius Korzen
 */
public class SimplePointTest {
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor1() {
    SimplePoint point = new SimplePoint();
    Assert.assertEquals(0f, point.getX(), 0.0001f);
    Assert.assertEquals(0f, point.getY(), 0.0001f);
    Assert.assertEquals("[0.0,0.0,0.0,0.0]", point.getRectangle().toString());
  }
 
  /**
   * Tests the default constructor.
   */
  @Test
  public void testConstructor2() {
    SimplePoint p = new SimplePoint(5, 10);
    Assert.assertEquals(5f, p.getX(), 0.0001f);
    Assert.assertEquals(10f, p.getY(), 0.0001f);
    Assert.assertEquals("[5.0,10.0,5.0,10.0]", p.getRectangle().toString());
  }
}
