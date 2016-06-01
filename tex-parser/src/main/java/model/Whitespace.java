package model;

import parse.Token;

/**
 * Represents a whitespace (" ").
 *
 * @author Claudius Korzen
 *
 */
public class Whitespace extends Text {
  /** The serial id. */
  protected static final long serialVersionUID = 1L;

  /**
   * Creates a new object of Whitespace.
   */
  public Whitespace(Token token) {
    super(" ", token);
  }

  /**
   * Creates a new object of Whitespace.
   */
  public Whitespace(int beginLine, int endLine, int beginColumn,
      int endColumn) {
    super(" ", beginLine, endLine, beginColumn, endColumn);
  }
}
