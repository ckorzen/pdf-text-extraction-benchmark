package de.freiburg.iif.model;

import java.util.List;

/**
 * The interface that declares the methods of a rectangle.
 * 
 * @author Claudius Korzen.
 */
public abstract class Rectangle extends Geometric {
  /**
   * Adds a vertex to this rectangle.
   * 
   * @param vertex
   *          The vertex to add.
   */
  public abstract void addVertex(Point vertex);

  /**
   * Returns the minimal x value of this rectangle.
   * 
   * @return the minimal x value of this rectangle.
   */
  public abstract float getMinX();

  /**
   * Sets the minimal x value of this rectangle.
   * 
   * @param minX
   *          the minimal x value of this rectangle.
   */
  public abstract void setMinX(float minX);

  /**
   * Returns the maximal x value of this rectangle.
   * 
   * @return the maximal x value of this rectangle.
   */
  public abstract float getMaxX();

  /**
   * Sets the maximal x value of this rectangle.
   * 
   * @param maxX
   *          the maximal x value of this rectangle.
   */
  public abstract void setMaxX(float maxX);

  /**
   * Returns the minimal y value of this rectangle.
   * 
   * @return the minimal y value of this rectangle.
   */
  public abstract float getMinY();

  /**
   * Sets the minimal y value of this rectangle.
   * 
   * @param minY
   *          the minimal y value of this rectangle.
   */
  public abstract void setMinY(float minY);

  /**
   * Returns the maximal y value of this rectangle.
   * 
   * @return the maximal y value of this rectangle.
   */
  public abstract float getMaxY();

  /**
   * Sets the maximal y value of this rectangle.
   * 
   * @param maxY
   *          the maximal y value of this rectangle.
   */
  public abstract void setMaxY(float maxY);

  /**
   * Returns the lower left point of this rectangle.
   * 
   * @return the lower left point of this rectangle.
   */
  public abstract Point getLowerLeft();

  /**
   * Returns the lower left point of this rectangle.
   * 
   * @return the lower left point of this rectangle.
   */
  public abstract Point getUpperLeft();

  /**
   * Returns the lower right point of this rectangle.
   * 
   * @return the lower right point of this rectangle.
   */
  public abstract Point getLowerRight();

  /**
   * Returns the upper right point of this rectangle.
   * 
   * @return the upper right point of this rectangle.
   */
  public abstract Point getUpperRight();

  /**
   * Returns the midpoint of this rectangle.
   * 
   * @return the midpoint of this rectangle.
   */
  public abstract Point getMidpoint();

  /**
   * Returns the midpoint of this rectangle in x dimension.
   * 
   * @return the midpoint of this rectangle in x dimension.
   */
  public abstract float getXMidpoint();

  /**
   * Returns the midpoint of this rectangle in y dimension.
   * 
   * @return the midpoint of this rectangle in y dimension.
   */
  public abstract float getYMidpoint();

  /**
   * Returns the width of this rectangle.
   * 
   * @return the width of this rectangle.
   */
  public abstract float getWidth();

  /**
   * Returns the height of this rectangle.
   * 
   * @return the height of this rectangle.
   */
  public abstract float getHeight();

  /**
   * Get the area of this rectangle.
   * 
   * @return the area of this rectangle.
   */
  public abstract float getArea();

  /**
   * Computes the overlap ratio of this rectangle with the given geometric.
   */
  public float computeOverlapRatio(Geometric geom) {   
    return computeOverlap(geom) / getArea();
  }
  
  /**
   * Merges this rect with given rect. Returns the minimum bounding box that 
   * contains both rectangles.
   * 
   * @param rect
   *          the other rectangle.
   * 
   * @return a new, merged rectangle object.
   */
  public abstract void unite(Rectangle rect);
  
  /**
   * Unites this rectangle with the given other rectangle.
   * 
   * @param rect
   *          the rectangle to process.
   * 
   * @return a rectangle, representing the union.
   */
  public abstract Rectangle union(Rectangle rect);
  
  /**
   * Intersects this rectangle with the given other rectangle.
   * 
   * @param rect
   *          the rectangle to process.
   * 
   * @return a rectangle, representing the intersection.
   */
  public abstract Rectangle intersection(Rectangle rect);

  /**
   * Splits this rectangle vertically at the given x coordinates.
   */
  public abstract List<Rectangle> splitVertically(float... x);
  
  /**
   * Splits this rectangle horizontally at the given y coordinates.
   */
  public abstract List<Rectangle> splitHorizontally(float... y);
  
  /**
   * Splits this rectangle vertically at the x midpoints of the given rects.
   */
  public abstract List<Rectangle> splitVertically(List<Rectangle> rects);
  
  /**
   * Splits this rectangle horizontally at the y midpoints of the given rects.
   */
  public abstract List<Rectangle> splitHorizontally(List<Rectangle> rects);
  
  /**
   * Returns true, if the given object is equal to this rectangle.
   */
  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (!(object instanceof Rectangle)) {
      return false;
    }

    Rectangle rect = (Rectangle) object;
    if (getMinX() != rect.getMinX()) {
      return false;
    }
    if (getMinY() != rect.getMinY()) {
      return false;
    }
    if (getMaxX() != rect.getMaxX()) {
      return false;
    }
    if (getMaxY() != rect.getMaxY()) {
      return false;
    }

    return true;
  }
  
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
