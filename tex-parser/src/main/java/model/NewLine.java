package model;

import parse.Token;

/**
 * Represents a newline ("\n").
 *
 * @author Claudius Korzen
 *
 */
public class NewLine extends Text {
  /** The serial id. */
  protected static final long serialVersionUID = -3868607654772993394L;
  
  /**
   * Creates a new object of NewLine.
   */
  public NewLine(Token token) {
    super("[newline]", token);
  }
  
  @Override
  public String toString() {
    return "\n";
  }
  
  @Override
  public boolean isWhitespace() {
    return true;
  }
}
