package de.freiburg.iif.model.simple;

import de.freiburg.iif.model.Point;
import de.freiburg.iif.model.Rectangle;

/**
 * Default implementation of Point.
 * 
 * @author Claudius Korzen
 *
 */
public class SimplePoint extends Point {
  /** 
   * The x value. 
   */
  protected float x;
  
  /** 
   * The y value. 
   */
  protected float y;
  
  /** 
   * The bounding box. 
   */
  protected Rectangle boundingBox;
  
  /** 
   * Flag to indicate, that the bounding box needs an update. 
   */
  protected boolean isBoundingBoxOutdated;
  
  /**
   * Creates a new point (0, 0).
   */
  public SimplePoint() {
    this(0, 0);
  }

  /**
   * Creates a new point (x, y).
   * 
   * @param x
   *          the x value.
   * @param y
   *          the y value.
   */
  public SimplePoint(float x, float y) {
    setX(x);
    setY(y);
    this.boundingBox = new SimpleRectangle();
  }
  
  /**
   * Creates a new point (x,y).
   * 
   * @param x
   *          the x value.
   * @param y
   *          the y value.
   */
  public SimplePoint(double x, double y) {
    this((float) x, (float) y);
  }

  @Override
  public float getX() {
    return x;
  }

  @Override
  public float getY() {
    return y;
  }
  
  @Override
  public void setX(float x) {
    this.x = x;
    this.isBoundingBoxOutdated = true;
  }

  @Override
  public void setY(float y) {
    this.y = y;
    this.isBoundingBoxOutdated = true;
  }

  @Override
  public Rectangle getRectangle() {
    if (this.isBoundingBoxOutdated) {
      this.boundingBox.setMinX(getX());
      this.boundingBox.setMinY(getY());
      this.boundingBox.setMaxX(getX());
      this.boundingBox.setMaxY(getY());
      this.isBoundingBoxOutdated = false;
    }
    return this.boundingBox;
  }

  @Override
  public void shiftBy(float x, float y) {
    setX(getX() + x);
    setY(getY() + y);
  }

  @Override
  public void moveTo(float x, float y) {
    setX(x);
    setY(y);
  }
  
  @Override
  public String toString() {
    return "(" + getX() + "," + getY() + ")";
  }
}
