package de.freiburg.iif.model.simple;

import de.freiburg.iif.model.Line;
import de.freiburg.iif.model.Point;
import de.freiburg.iif.model.Rectangle;

/**
 * The default implementation of a line.
 * 
 * @author Claudius Korzen
 */
public class SimpleLine extends Line {
  /** 
   * The x value of the lower left. 
   */
  protected Point startPoint;
  
  /** 
   * The y value of the lower left. 
   */
  protected Point endPoint;
  
  /** 
   * The bounding box. 
   */
  protected Rectangle boundingBox;
  
  /** 
   * Flag to indicate, that the bounding box needs an update. 
   */
  protected boolean isBoundingBoxOutdated;

  /**
   * Creates a new line with start point (0,0) and end point (0,0).
   */
  public SimpleLine() {
    this(0, 0, 0, 0);
  }

  /**
   * Creates a new line with start point (startX, startY) and end point 
   * (endY, endY).
   * 
   * @param startX
   *          the x value of the start point.
   * @param startY
   *          the y value of the start point.
   * @param endX
   *          the x value of the end point.
   * @param endY
   *          the y value of the end point.
   */
  public SimpleLine(float startX, float startY, float endX, float endY) {
    this(new SimplePoint(startX, startY), new SimplePoint(endX, endY));
  }

  /**
   * Creates a new line which is defined by the given start point and the given 
   * endpoint.
   * 
   * @param startPoint
   *          the start point.
   * @param endPoint
   *          the end point.
   */
  public SimpleLine(Point startPoint, Point endPoint) {
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.boundingBox = new SimpleRectangle();
    this.isBoundingBoxOutdated = true;
  }

  @Override
  public Point getStartPoint() {
    return this.startPoint;
  }

  @Override
  public void setStartX(float x) {
    this.startPoint.setX(x);
    this.isBoundingBoxOutdated = true;
  }
  
  @Override
  public float getStartX() {
    return this.startPoint.getX();
  }
  
  @Override
  public void setStartY(float y) {
    this.startPoint.setY(y);
    this.isBoundingBoxOutdated = true;
  }
  
  @Override
  public float getStartY() {
    return this.startPoint.getY();
  }
  
  @Override
  public Point getEndPoint() {
    return this.endPoint;
  }

  @Override
  public void setEndX(float x) {
    this.endPoint.setX(x);
    this.isBoundingBoxOutdated = true;
  }
  
  @Override
  public float getEndX() {
    return this.endPoint.getX();
  }
  
  @Override
  public void setEndY(float y) {
    this.endPoint.setY(y);
    this.isBoundingBoxOutdated = true;
  }

  @Override
  public float getEndY() {
    return this.endPoint.getY();
  }
  
  @Override
  public Rectangle getRectangle() {
    if (this.isBoundingBoxOutdated) {
      this.boundingBox.setMinX(getStartX());
      this.boundingBox.setMinY(getStartY());
      this.boundingBox.setMaxX(getEndX());
      this.boundingBox.setMaxY(getEndY());
      this.isBoundingBoxOutdated = false;
    }
    return this.boundingBox;
  }

  @Override
  public void shiftBy(float x, float y) {
    setStartX(this.startPoint.getX() + x);
    setStartY(this.startPoint.getY() + y);
    setEndX(this.endPoint.getX() + x);
    setEndY(this.endPoint.getY() + y);
  }

  @Override
  public void moveTo(float x, float y) {
    float xDelta = x - getStartX();
    float yDelta = y - getStartY();
    setStartX(x);
    setStartY(y);
    setEndX(getEndX() + xDelta);
    setEndY(getEndY() + yDelta);
  }
  
  @Override
  public String toString() {
    return "[" + getStartPoint() + "," + getEndPoint() + "]";
  }
  
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Line)) {
      return false;
    }
    
    Line otherLine = (Line) other;
    
    return getStartX() == otherLine.getStartX()
        && getStartY() == otherLine.getStartY()
        && getEndX() == otherLine.getEndX()
        && getEndY() == otherLine.getEndY();
  }
  
  @Override
  public int hashCode() {
    return (int) (getStartX() + getStartY() + getEndX() + getEndY());
  }
}
