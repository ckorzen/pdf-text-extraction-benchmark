package model;

/**
 * A marker in a tex document (e.g. "#1" in a macro).
 *
 * @author Claudius Korzen
 *
 */
public class Marker extends Element {
  /**
   * 
   */
  private static final long serialVersionUID = -7507243182250124048L;
  /**
   * The id of this marker.
   */
  protected int id;
  
  /**
   * Creates a new Marker with the given id. 
   */
  public Marker(int id) {
    this.id = id;
  }
  
  /**
   * Returns the id of this marker.
   */
  public int getId() {
    return id;
  }
  
  public String toString() {
    return "#" + getId();
  }
}
