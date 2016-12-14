package model;

/**
 * Introduced to be able to create an output file, where each word is
 * enriched with its linenumber and its columnnumber in the tex file.
 */
public class TeXWord {
  
  public String text;
  public int lineNumber;
  public int columnNumber;
  
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
