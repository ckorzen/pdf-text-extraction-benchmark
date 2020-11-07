package de.freiburg.iif.rtree.util;

import de.freiburg.iif.model.Rectangle;

/**
 * Class with helper methods for R-tree.
 *
 * @author Claudius Korzen
 *
 */
public class RTreeUtil {
  /**
   * Calculates the area by which the first given rectangle would be enlarged if
   * added to the second given rectangle.
   * 
   * @param rect1
   *          The rectangle to enlarge.
   * @param rect2
   *          The rectangle to add.
   * 
   * @return The enlargement.
   */
  public static float computeEnlargement(Rectangle rect1, Rectangle rect2) {
    if (rect1 == null || rect2 == null) {
      return 0;
    }
    float enlargedArea = (Math.max(rect1.getMaxX(), rect2.getMaxX())
        - Math.min(rect1.getMinX(), rect2.getMinX()))
        * (Math.max(rect1.getMaxY(), rect2.getMaxY())
            - Math.min(rect1.getMinY(), rect2.getMinY()));
    return enlargedArea - rect1.getArea();
  }
}
