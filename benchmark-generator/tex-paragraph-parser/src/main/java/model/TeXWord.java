package model;

/**
 * Introduced to be able to create an output file, where each word is
 * enriched with its linenumber and its columnnumber in the tex file.
 */
public class TeXWord {
  
  public String text;
  public int lineNumber;
  public int columnNumber;
  
  /**
   * A Tex Word.
   * 
   * @param text the textual content of word.
   * @param lineNumber the line number in tex file.
   * @param columnNumber the column number in tex file.
   */
  public TeXWord(String text, int lineNumber, int columnNumber) {
    this.text = text;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }
  
  @Override
  public String toString() {
    return text;
  }
}
