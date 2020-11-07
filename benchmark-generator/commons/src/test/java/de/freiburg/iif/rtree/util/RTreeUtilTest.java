package de.freiburg.iif.rtree.util;

import org.junit.Assert;
import org.junit.Test;

import static de.freiburg.iif.mock.RectangleMock.mockRectangle;
import de.freiburg.iif.model.Rectangle;

/**
 * Tests the RTreeUtil.
 * 
 * @author Claudius Korzen
 * 
 */
public class RTreeUtilTest {

  /**
   * Tests the method computeEnlargment.
   */
  @Test
  public void testComputeEnlargement() {
    Rectangle rect1 = mockRectangle(1, 1, 5, 5);
    Assert.assertEquals(0, RTreeUtil.computeEnlargement(rect1, rect1), 0.0001);
    
    Rectangle rect2 = mockRectangle(2, 2, 4, 4);
    Assert.assertEquals(0, RTreeUtil.computeEnlargement(rect2, rect2), 0.0001);
    Assert.assertEquals(0, RTreeUtil.computeEnlargement(rect1, rect2), 0.0001);
    Assert.assertEquals(12, RTreeUtil.computeEnlargement(rect2, rect1), 0.0001);
    
    Rectangle rect3 = mockRectangle(6, 2, 8, 4);
    Assert.assertEquals(0, RTreeUtil.computeEnlargement(rect3, rect3), 0.0001);
    Assert.assertEquals(12, RTreeUtil.computeEnlargement(rect1, rect3), 0.0001);
    Assert.assertEquals(24, RTreeUtil.computeEnlargement(rect3, rect1), 0.0001);
    
    Assert.assertEquals(8, RTreeUtil.computeEnlargement(rect2, rect3), 0.0001);
    Assert.assertEquals(8, RTreeUtil.computeEnlargement(rect3, rect2), 0.0001);
  }
}
