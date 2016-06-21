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
  protected StringBuilder text;  
  
  /**
   * The constructor.
   */
  public Text(String text, Token token) {
    super(token);
    this.text = new StringBuilder(text);
  }
  
  /**
   * The constructor.
   */
  public Text(String text, int beginLine, int endLine, int beginColumn, 
      int endColumn) {
    super(beginLine, endLine, beginColumn, endColumn);
    this.text = new StringBuilder(text);
  }
  
  public void appendText(String text) {
    this.text.append(text);
  }
  
  /**
   * Returns the textual content.
   */
  public String getText() {
    return text.toString();
  }
  
  @Override
  public String toString() {
    return text.toString();
  }
  
  /**
   * Returns true, if this text only contains whitespaces.
   */
  public boolean isWhitespace() {
    return getText() != null && getText().trim().isEmpty();
  }
}
