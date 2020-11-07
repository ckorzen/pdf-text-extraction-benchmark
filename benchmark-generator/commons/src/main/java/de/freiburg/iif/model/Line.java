package de.freiburg.iif.model;

/**
 * The interface that declares the methods of a line.
 * 
 * @author Claudius Korzen.
 */
public abstract class Line extends Geometric {
  /**
   * Sets the x coordinate of the start point.
   * 
   * @param x the x coordinate of the start point.
   */
  public abstract void setStartX(float x);
  
  /**
   * Sets the y coordinate of the start point.
   * 
   * @param y the y coordinate of the start point.
   */
  public abstract void setStartY(float y);
  
  /**
   * Sets the x coordinate of the end point.
   * 
   * @param x the x coordinate of the end point.
   */
  public abstract void setEndX(float x);
  
  /**
   * Sets the y coordinate of the end point.
   * 
   * @param y the y coordinate of the end point.
   */
  public abstract void setEndY(float y);
  
  /**
   * Returns the x coordinate of the start point.
   * 
   * @return the x coordinate of the start point.
   */
  public abstract float getStartX();
  
  /**
   * Returns the y coordinate of the start point.
   * 
   * @return the y coordinate of the start point.
   */
  public abstract float getStartY();
  
  /**
   * Returns the x coordinate of the end point.
   * 
   * @return the x coordinate of the end point.
   */
  public abstract float getEndX();
  
  /**
   * Returns the y coordinate of the end point.
   * 
   * @return the y coordinate of the end point.
   */
  public abstract float getEndY();
  
  /**
   * Returns the start point of this line.
   * 
   * @return the start point of this line.
   */
  public abstract Point getStartPoint();
  
  /**
   * Returns the end point of this line.
   * 
   * @return the end point of this line.
   */
  public abstract Point getEndPoint();
}
