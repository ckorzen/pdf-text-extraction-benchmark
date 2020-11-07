package de.freiburg.iif.rtree.performance;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import de.freiburg.iif.rtree.RTree;
import de.freiburg.iif.rtree.SimpleRTree;

/**
 * Class to test the performance of RTree.
 * 
 * @author Claudius Korzen
 * 
 */
public class RTreePerformance {
  /**
   * The main method to start the performance test.
   * 
   * @param args
   *          The command line arguments.
   */
  public static void main(String[] args) {
    RTreePerformance performanceTest = new RTreePerformance();

    performanceTest.run();
  }

  /**
   * Runs the performance test.
   */
  protected void run() {
    RTree<HasRectangle> rtree = new SimpleRTree<HasRectangle>();
    long start = System.currentTimeMillis();
    // Append 100.000 rectangles to tree.
    for (int i = 0; i < 10 * 1000 * 1000; i++) {
      rtree.insert(new MyHasRectangle(i, i, 2 * (i + 1), 2 * (i + 1)));
      // Take the time after a certain amount of inserts.
      if ((i + 1) % (10 * 1000) == 0) {
        long now = System.currentTimeMillis();
        System.out.println((i + 1) + "\t" + (now - start));
      }
    }
  }

  /**
   * An internal implementation of HasRectangle.
   * 
   * @author Claudius Korzen
   * 
   */
  class MyHasRectangle implements HasRectangle {
    /**
     * The minimum x value.
     */
    float minX;
    /**
     * The minimum y value.
     */
    float minY;
    /**
     * The maximum x value.
     */
    float maxX;
    /**
     * The maximum y value.
     */
    float maxY;

    /**
     * The constructor.
     * 
     * @param minX
     *          The minimum x value.
     * @param minY
     *          The minimum y value.
     * @param maxX
     *          The maximum x value.
     * @param maxY
     *          The maximum y value.
     * 
     */
    public MyHasRectangle(float minX, float minY, float maxX, float maxY) {
      this.minX = minX;
      this.minY = minY;
      this.maxX = maxX;
      this.maxY = maxY;
    }

    @Override
    public Rectangle getRectangle() {
      return new SimpleRectangle(minX, minY, maxX, maxY);
    }
  }
}
