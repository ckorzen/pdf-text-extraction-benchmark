package model;

import parse.Token;

/**
 * A text element in a tex file.
 *
 * @author Claudius Korzen
 */
public class Text extends Element {
  /** The serial id. */
  protected static final long serialVersionUID = -7150317095394410328L;
  /** The text. */
  protected String text;
    
  /**
   * The constructor.
   */
  public Text(String text, Token token) {
    super(token);
    this.text = text;
  }
  
  /**
   * The constructor.
   */
  public Text(String text, int beginLine, int endLine, int beginColumn, 
      int endColumn) {
    super(beginLine, endLine, beginColumn, endColumn);
    this.text = text;
  }
  
  /**
   * Returns the textual content.
   */
  public String getText() {
    return text;
  }
  
  @Override
  public String toString() {
    return text;
  }
}
