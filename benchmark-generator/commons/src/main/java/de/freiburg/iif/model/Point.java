package de.freiburg.iif.model;

/**
 * The interface that declares the methods of a 2-dim point.
 * 
 * @author Claudius Korzen.
 */
public abstract class Point extends Geometric {
  /**
   * Returns the x coordinate of this point.
   * 
   * @return the x coordinate of this point.
   */
  public abstract float getX();
  
  /**
   * Returns the y coordinate of this point.
   * 
   * @return the y coordinate of this point.
   */
  public abstract float getY();
  
  /**
   * Sets the x coordinate of this point.
   * 
   * @param x the x coordinate of this point.
   */
  public abstract void setX(float x);
  
  /**
   * Sets the y coordinate of this point.
   * 
   * @param y the y coordinate of this point.
   */
  public abstract void setY(float y);
}
