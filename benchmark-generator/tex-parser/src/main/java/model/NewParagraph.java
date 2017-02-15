package model;

import parse.Token;

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
  public NewParagraph(Token token) {
    super("[newparagraph]", token);
  }
  
  @Override
  public String toString() {
    return "\n\n";
  }
  
  @Override
  public String getElementReferenceIdentifier() {
    return "\\par";
  }
  
  @Override
  public boolean isWhitespace() {
    return true;
  }
}
