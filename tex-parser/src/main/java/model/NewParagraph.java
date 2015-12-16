package model;

/**
 * Represents a new paragraph ("\n").
 *
 * @author Claudius Korzen
 *
 */
public class NewParagraph extends Text {
  /** The serial id. */
  protected static final long serialVersionUID = -3868607654772993394L;

  /**
   * Creates a new object of NewLine.
   */
  public NewParagraph() {
    super("[newparagraph]");
  }
  
  @Override
  public String toString() {
    return "\n\n";
  }
}
