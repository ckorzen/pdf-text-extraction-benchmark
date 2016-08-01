package model;

import java.io.Serializable;

import parse.Token;

/**
 * The base class for an element of a tex document (like commands, text or
 * group).
 *
 * @author Claudius Korzen
 */
public class Element implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 8883003424823038796L;
  
  /** The number of line where the element begins. */
  protected int beginLine = Integer.MAX_VALUE;

  /** The number of line where the element ends. */
  protected int endLine = Integer.MIN_VALUE;

  /** The column where the element begins. */
  protected int beginColumn = Integer.MAX_VALUE;

  /** The column where the element ends. */
  protected int endColumn = Integer.MIN_VALUE;
  
  /**
   * Creates a new element with given begin line and end line.
   */
  public Element(Token token) {
    if (token != null) {
      this.beginLine = token.beginLine;
      this.endLine = token.endLine;
      this.beginColumn = token.beginColumn;
      this.endColumn = token.endColumn;
    }
  }

  /**
   * Creates a new element with given begin line and end line.
   */
  public Element(int beginLine, int endLine, int beginColumn, int endColumn) {
    this.beginLine = beginLine;
    this.endLine = endLine;
    this.beginColumn = beginColumn;
    this.endColumn = endColumn;
  }

  /**
   * Returns the number of line where the element begins.
   */
  public int getBeginLineNumber() {
    return this.beginLine;
  }

  /**
   * Sets the number of line where the element begins.
   */
  public void setBeginLineNumber(int beginLine) {
    this.beginLine = beginLine;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the number of line where the element ends.
   */
  public int getEndLineNumber() {
    return this.endLine;
  }
  
  /**
   * Sets the number of line where the element begins.
   */
  public void setEndLineNumber(int endLine) {
    this.endLine = endLine;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the column where the element begins.
   */
  public int getBeginColumnNumber() {
    return this.beginColumn;
  }

  /**
   * Sets the number of column where the element begins.
   */
  public void setBeginColumnNumber(int beginColumn) {
    this.beginColumn = beginColumn;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the column where the element ends.
   */
  public int getEndColumnNumber() {
    return this.endColumn;
  }
  
  /**
   * Sets the number of column where the element ends.
   */
  public void setEndColumnNumber(int endColumn) {
    this.endColumn = endColumn;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the column where the element ends.
   */
  public String getElementReferenceIdentifier() {
    return this.toString();
  }
}
