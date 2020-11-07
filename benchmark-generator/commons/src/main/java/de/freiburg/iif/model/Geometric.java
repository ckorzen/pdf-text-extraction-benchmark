package de.freiburg.iif.model;

/**
 * An interface representing a 2-dim geometric object (like a point, a line, a 
 * rectangle, etc).
 * 
 * @author Claudius Korzen
 */
public abstract class Geometric implements HasRectangle {
  /** The container of this geometric. */
  protected Object container;

  /**
   * Shifts this geometric by the given x- and y-amount.
   * 
   * @param x
   *          the x-shift.
   * @param y
   *          the y-shift.
   */
  public abstract void shiftBy(float x, float y);

  /**
   * Moves this geometric to the given position.
   * 
   * @param x
   *          the x-position.
   * @param y
   *          the y-position.
   */
  public abstract void moveTo(float x, float y);

  /**
   * Returns true, if the bounding box of this geometric object completely
   * contains the other geometric.
   * 
   * @param geometric
   *          the other geometric object.
   * 
   * @return true, if the bounding box of this geometric object completely
   *         contains the other geometric.
   */
  public boolean contains(Geometric geometric) {
    if (geometric == null) {
      return false;
    }

    if (geometric.getRectangle().getMinX() < getRectangle().getMinX()) {
      return false;
    }
    if (geometric.getRectangle().getMaxX() > getRectangle().getMaxX()) {
      return false;
    }
    if (geometric.getRectangle().getMinY() < getRectangle().getMinY()) {
      return false;
    }
    if (geometric.getRectangle().getMaxY() > getRectangle().getMaxY()) {
      return false;
    }
    return true;
  }

  /**
   * Returns true, if this geometric object overlaps the other geometric
   * horizontally and/or vertically.
   * 
   * @param geometric
   *          the other geometric object.
   * 
   * @return true, if this geometric object overlaps the other geometric.
   */
  public boolean overlaps(Geometric geometric) {
    return overlapsHorizontally(geometric) && overlapsVertically(geometric);
  }

  /**
   * Returns true, if this geometric object overlaps the other geometric
   * horizontally.
   * 
   * @param geometric
   *          the other geometric object.
   * 
   * @return true if this geometric object overlaps the other geometric
   *         horizontally.
   */
  public boolean overlapsHorizontally(Geometric geometric) {
    if (geometric == null) {
      return false;
    }
    if (geometric.getRectangle() == null) {
      return false;
    }
    return getRectangle().getMaxX() >= geometric.getRectangle().getMinX()
        && getRectangle().getMinX() <= geometric.getRectangle().getMaxX();
  }

  /**
   * Returns true, if this geometric object overlaps the other geometric
   * vertically.
   * 
   * @param geometric
   *          the other geometric object.
   * 
   * @return true if this geometric object overlaps the other geometric
   *         horizontally.
   */
  public boolean overlapsVertically(Geometric geometric) {
    if (geometric == null) {
      return false;
    }
    if (geometric.getRectangle() == null) {
      return false;
    }
    return getRectangle().getMinY() <= geometric.getRectangle().getMaxY()
        && getRectangle().getMaxY() >= geometric.getRectangle().getMinY();
  }

  /**
   * Computes the size of the overlap area between this geometric object and the
   * other geometric object.
   * 
   * @param geometric
   *          the other geometric object.
   * @return the size of the overlap area
   */
  public float computeOverlap(Geometric geometric) {
    return computeHorizontalOverlap(geometric)
        * computeVerticalOverlap(geometric);
  }

  /**
   * Computes the length of the vertical overlap between this geometric object
   * and the other geometric object.
   * 
   * @param geometric
   *          the other geometric object.
   * @return the length of the vertical overlap
   */
  public float computeVerticalOverlap(Geometric geometric) {
    float minMaxY = Math.min(getRectangle().getMaxY(),
        geometric.getRectangle().getMaxY());
    float maxMinY = Math.max(getRectangle().getMinY(),
        geometric.getRectangle().getMinY());
        
    return Math.max(0, minMaxY - maxMinY);
  }

  /**
   * Computes the length of the horizontal overlap between this geometric object
   * and the other geometric object.
   * 
   * @param geometric
   *          the other geometric object.
   * @return the length of the vertical overlap
   */
  public float computeHorizontalOverlap(Geometric geometric) {
    float minMaxX = Math.min(getRectangle().getMaxX(),
        geometric.getRectangle().getMaxX());
    float maxMinX = Math.max(getRectangle().getMinX(),
        geometric.getRectangle().getMinX());
    return Math.max(0, minMaxX - maxMinX);
  }

  @Override
  public String toString() {
    return "["
        + getRectangle().getMinX() + "," + getRectangle().getMinY() + ","
        + getRectangle().getMaxX() + "," + getRectangle().getMaxY() + "]";
  }

  /**
   * Returns the container of this geometric in which the geometric lives.
   * 
   * @return the container.
   */
  public Object getContainer() {
    return this.container;
  }

  /**
   * Sets the container of this geometric in which the geometric lives.
   * 
   * @param container
   *          the container.
   */
  public void setContainer(Object container) {
    this.container = container;
  }
}
