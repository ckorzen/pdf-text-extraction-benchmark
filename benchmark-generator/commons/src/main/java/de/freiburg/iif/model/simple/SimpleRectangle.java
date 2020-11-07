package de.freiburg.iif.model.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Point;
import de.freiburg.iif.model.Rectangle;

/**
 * Simple implementation of interface Rectangle with (0,0) in the lower left and
 * increasing coordinates to the upper right.
 * 
 * @author Claudius Korzen
 * 
 */
public class SimpleRectangle extends Rectangle {
  /**
   * The x value of the lower left.
   */
  protected float minX;

  /**
   * The y value of the lower left.
   */
  protected float maxX;

  /**
   * The width of this rectangle.
   */
  protected float minY;

  /**
   * The height of this rectangle.
   */
  protected float maxY;

  /**
   * Flag needed to distinguish, whether minX was already set.
   */
  protected boolean isMinXSet;

  /**
   * Flag needed to distinguish, whether minY was already set.
   */
  protected boolean isMinYSet;

  /**
   * Flag needed to distinguish, whether maxX was already set.
   */
  protected boolean isMaxXSet;

  /**
   * Flag needed to distinguish, whether maxY was already set.
   */
  protected boolean isMaxYSet;

  /**
   * Creates a new rectangle that is spanned by the points (0, 0) and (0, 0).
   */
  public SimpleRectangle() {
    this(0, 0, 0, 0);
  }

  /**
   * Creates a new rectangle with the same position and dimensions of the given
   * rectangle.
   */
  public SimpleRectangle(Rectangle rect) {
    this(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
  }

  /**
   * Creates a new rectangle with the same position and dimensions of the given
   * rectangle.
   */
  public SimpleRectangle(java.awt.Rectangle rect) {
    this(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
  }
    
  /**
   * Creates a new rectangle that is spanned by the points (0, 0) and (width,
   * height).
   * 
   * @param width
   *          The width of this rectangle.
   * @param height
   *          The height of this rectangle.
   */
  public SimpleRectangle(float width, float height) {
    this(0, 0, width, height);
  }

  /**
   * Creates a new rectangle that is spanned by point1 and point2.
   * 
   * @param point1
   *          the lower left point.
   * @param point2
   *          the upper right point.
   */
  public SimpleRectangle(Point point1, Point point2) {
    this(point1.getX(), point1.getY(), point2.getX(), point2.getY());
  }

  /**
   * Creates a new rectangle with the same position and dimensions of the given
   * rectangle.
   */
  public SimpleRectangle(double minX, double minY, double maxX, double maxY) {
    this((float) minX, (float) minY, (float) maxX, (float) maxY);
  }
  
  /**
   * Creates a new rectangle that is spanned by the points (minX, minY) and
   * (maxX, maxY).
   * 
   * @param minX
   *          The minimum x value.
   * @param minY
   *          The minimum y value.
   * @param maxX
   *          The maximum x value.
   * @param maxY
   *          The maximum y value.
   */
  public SimpleRectangle(float minX, float minY, float maxX, float maxY) {
    setMinX(minX);
    setMinY(minY);
    setMaxX(maxX);
    setMaxY(maxY);
  }

  /**
   * Creates a new rectangle from the 4 given vertices. The vertices need not be
   * in any special order.
   *
   * @param point1
   *          the first vertex.
   * @param point2
   *          the second vertex.
   * @param point3
   *          the third vertex.
   * @param point4
   *          the fourth vertex.
   * @return the constructed rectangle.
   */
  public static Rectangle from2Vertices(Point point1, Point point2) {
    float x1 = point1.getX();
    float y1 = point1.getY();
    float x2 = point2.getX();
    float y2 = point2.getY();

    float minX = Math.min(x1, x2);
    float minY = Math.min(y1, y2);
    float maxX = Math.max(x1, x2);
    float maxY = Math.max(y1, y2);

    return new SimpleRectangle(minX, minY, maxX, maxY);
  }
  
  /**
   * Creates a new rectangles that results from the union of the two given 
   * rectangles.
   */
  public static Rectangle fromUnion(HasRectangle hr1, HasRectangle hr2) {
    Rectangle rect1 = hr1.getRectangle();
    Rectangle rect2 = hr2.getRectangle();
    
    float minX = Math.min(rect1.getMinX(), rect2.getMinX());
    float maxX = Math.max(rect1.getMaxX(), rect2.getMaxX());
    float minY = Math.min(rect1.getMinY(), rect2.getMinY());
    float maxY = Math.max(rect1.getMaxY(), rect2.getMaxY());
    
    return new SimpleRectangle(minX, minY, maxX, maxY);
  }
  
  @Override
  public Rectangle union(Rectangle rect) {
    float minX = Math.min(getMinX(), rect.getMinX());
    float maxX = Math.max(getMaxX(), rect.getMaxX());
    float minY = Math.min(getMinY(), rect.getMinY());
    float maxY = Math.max(getMaxY(), rect.getMaxY());
    
    return new SimpleRectangle(minX, minY, maxX, maxY);
  }
  
  /**
   * Creates a new rectangle from the 4 given vertices. The vertices need not be
   * in any special order.
   *
   * @param point1
   *          the first vertex.
   * @param point2
   *          the second vertex.
   * @param point3
   *          the third vertex.
   * @param point4
   *          the fourth vertex.
   * @return the constructed rectangle.
   */
  public static Rectangle from4Vertices(Point point1, Point point2,
      Point point3, Point point4) {
    float x1 = point1.getX();
    float y1 = point1.getY();
    float x2 = point2.getX();
    float y2 = point2.getY();
    float x3 = point3.getX();
    float y3 = point3.getY();
    float x4 = point4.getX();
    float y4 = point4.getY();

    float minX = Math.min(Math.min(x1, x2), Math.min(x3, x4));
    float minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
    float maxX = Math.max(Math.max(x1, x2), Math.max(x3, x4));
    float maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));

    return new SimpleRectangle(minX, minY, maxX, maxY);
  }

  @Override
  public void addVertex(Point vertex) {
    setMinX(isMinXSet ? Math.min(getMinX(), vertex.getX()) : vertex.getX());
    setMinY(isMinYSet ? Math.min(getMinY(), vertex.getY()) : vertex.getY());
    setMaxX(isMaxXSet ? Math.max(getMaxX(), vertex.getX()) : vertex.getX());
    setMaxY(isMaxYSet ? Math.max(getMaxY(), vertex.getY()) : vertex.getY());
  }

  /**
   * Returns the bounding box of all given objects, that is the smallest
   * possible rectangle that includes all the given objects.
   * 
   * @param objects
   *          the objects to include.
   * @return the bounding box.
   */
  public static Rectangle computeBoundingBox(HasRectangle... objects) {
    return computeBoundingBox(new ArrayList<HasRectangle>(
        Arrays.asList(objects)));
  }

  /**
   * Returns the bounding box of all given objects, that is the smallest
   * possible rectangle that includes all the given objects.
   * 
   * @param objects
   *          the objects to include.
   * @return the bounding box.
   */
  public static Rectangle computeBoundingBox(
      List<? extends HasRectangle> objects) {
    if (objects == null) {
      return null;
    }

    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;

    for (HasRectangle object : objects) {
      Rectangle rectangle = object.getRectangle();
      if (rectangle.getMinX() < minX) {
        minX = rectangle.getMinX();
      }

      if (rectangle.getMinY() < minY) {
        minY = rectangle.getMinY();
      }

      if (rectangle.getMaxX() > maxX) {
        maxX = rectangle.getMaxX();
      }

      if (rectangle.getMaxY() > maxY) {
        maxY = rectangle.getMaxY();
      }
    }

    return new SimpleRectangle(minX, minY, maxX, maxY);
  }

  @Override
  public float getMinX() {
    return minX;
  }

  @Override
  public void setMinX(float minX) {
    this.minX = minX;
    this.isMinXSet = true;
  }

  @Override
  public float getMinY() {
    return minY;
  }

  @Override
  public void setMinY(float minY) {
    this.minY = minY;
    this.isMinYSet = true;
  }

  @Override
  public float getMaxX() {
    return maxX;
  }

  @Override
  public void setMaxX(float maxX) {
    this.maxX = maxX;
    this.isMaxXSet = true;
  }

  @Override
  public float getMaxY() {
    return maxY;
  }

  @Override
  public void setMaxY(float maxY) {
    this.maxY = maxY;
    this.isMaxYSet = true;
  }

  @Override
  public Point getLowerLeft() {
    return new SimplePoint(minX, minY);
  }

  @Override
  public Point getUpperLeft() {
    return new SimplePoint(minX, maxY);
  }

  @Override
  public Point getLowerRight() {
    return new SimplePoint(maxX, minY);
  }

  @Override
  public Point getUpperRight() {
    return new SimplePoint(maxX, maxY);
  }

  @Override
  public Point getMidpoint() {
    return new SimplePoint(getXMidpoint(), getYMidpoint());
  }

  @Override
  public float getXMidpoint() {
    return minX + (getWidth() / 2f);
  }

  @Override
  public float getYMidpoint() {
    return minY + (getHeight() / 2f);
  }

  /**
   * Returns the width of this rectangle.
   * 
   * @return the width of this rectangle.
   */
  public float getWidth() {
    return maxX - minX;
  }

  /**
   * Returns the height of this rectangle.
   * 
   * @return the height of this rectangle.
   */
  public float getHeight() {
    return maxY - minY;
  }

  /**
   * Returns true if the given point (x,y) lies inside this rectangle.
   * 
   * @param x
   *          The x-coordinate of point to test.
   * @param y
   *          The y-coordinate of point to test.
   * 
   * @return true if the point is inside this rectangle.
   */
  public boolean contains(float x, float y) { // TODO: Use Geometric.
    return x >= getMinX() && x <= getMaxX()
        && y >= getMinY() && y <= getMaxY();
  }

  @Override
  public Rectangle getRectangle() {
    return this;
  }

  @Override
  public float getArea() {
    return getWidth() * getHeight();
  }

  @Override
  public void unite(Rectangle rect) {
    float minX = Math.min(getMinX(), rect.getMinX());
    float maxX = Math.max(getMaxX(), rect.getMaxX());
    float minY = Math.min(getMinY(), rect.getMinY());
    float maxY = Math.max(getMaxY(), rect.getMaxY());
    
    setMinX(minX);
    setMaxX(maxX);
    setMinY(minY);
    setMaxY(maxY);
  }

  @Override
  public Rectangle intersection(Rectangle rect) {
    float maxMinX = Math.max(getMinX(), rect.getMinX());
    float minMaxX = Math.min(getMaxX(), rect.getMaxX());
    float maxMinY = Math.max(getMinY(), rect.getMinY());
    float minMaxY = Math.min(getMaxY(), rect.getMaxY());

    if (minMaxX <= maxMinX) {
      return null;
    }

    if (minMaxY <= maxMinY) {
      return null;
    }

    return new SimpleRectangle(maxMinX, maxMinY, minMaxX, minMaxY);
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Rectangle)) {
      return false;
    }
    Rectangle rect = (Rectangle) object;

    if (rect.getMinX() != getMinX()) {
      return false;
    }

    if (rect.getMinY() != getMinY()) {
      return false;
    }

    if (rect.getMaxX() != getMaxX()) {
      return false;
    }

    if (rect.getMaxY() != getMaxY()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Float.floatToIntBits(minX + 2 * minY + 3 * maxX + 4 * maxY);
  }

  @Override
  public void shiftBy(float x, float y) {
    setMinX(getMinX() + x);
    setMinY(getMinY() + y);
    setMaxX(getMaxX() + x);
    setMaxY(getMaxY() + y);
  }

  @Override
  public void moveTo(float x, float y) {
    float xDelta = x - getMinX();
    float yDelta = y - getMinY();
    setMinX(x);
    setMinY(y);
    setMaxX(getMaxX() + xDelta);
    setMaxY(getMaxY() + yDelta);
  }

  @Override
  public String toString() {
    return "[" + getMinX() + "," + getMinY() + ","
        + getMaxX() + "," + getMaxY() + "]";
  }

  @Override
  public List<Rectangle> splitVertically(float... xValues) {
    List<Rectangle> subRects = new ArrayList<>();
    
    float startX = getMinX();
    for (float x : xValues) {
      subRects.add(new SimpleRectangle(startX, getMinY(), x, getMaxY()));
      startX = x;
    }
    subRects.add(new SimpleRectangle(startX, getMinY(), getMaxX(), getMaxY()));
    
    return subRects;
  }

  @Override
  public List<Rectangle> splitHorizontally(float... yValues) {
    List<Rectangle> subRects = new ArrayList<>();
    
    float endY = getMaxY();
    for (float y : yValues) {
      subRects.add(new SimpleRectangle(getMinX(), y, getMaxX(), endY));
      endY = y;
    }
    subRects.add(new SimpleRectangle(getMinX(), getMinY(), getMaxX(), endY));
    
    return subRects;
  }
  
  @Override
  public List<Rectangle> splitVertically(List<Rectangle> rects) {
    List<Rectangle> subRects = new ArrayList<>();
    
    float startX = getMinX();
    for (Rectangle rect : rects) {
      float x = rect.getXMidpoint();
      subRects.add(new SimpleRectangle(startX, getMinY(), x, getMaxY()));
      startX = x;
    }
    subRects.add(new SimpleRectangle(startX, getMinY(), getMaxX(), getMaxY()));
    
    return subRects;
  }

  @Override
  public List<Rectangle> splitHorizontally(List<Rectangle> rects) {
    List<Rectangle> subRects = new ArrayList<>();
    
    float endY = getMaxY();
    for (Rectangle rect : rects) {
      float y = rect.getYMidpoint();
      subRects.add(new SimpleRectangle(getMinX(), y, getMaxX(), endY));
      endY = y;
    }
    subRects.add(new SimpleRectangle(getMinX(), getMinY(), getMaxX(), endY));
    
    return subRects;
  }
}
