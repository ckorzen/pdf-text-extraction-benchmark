package model;

/**
 * An option in a tex file, that is a bunch of elements wrapped in "[...]".
 *
 * @author Claudius Korzen
 */
public class Option extends Group {
  /** The serial id. */
  private static final long serialVersionUID = 5750999941108182325L;

  /**
   * Creates a new option.
   */
  public Option() { }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(getText());
    sb.append("]");
    return sb.toString();
  }
}
