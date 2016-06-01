package model;

import java.util.ArrayList;
import java.util.List;

/**
 * A paragraph in a tex file.
 * 
 * @author Claudius Korzen
 */
public class TexParagraph {
  protected List<Element> texElements;
  protected List<PdfParagraph> pdfParagraphs;
  protected int texStartLine;
  protected int texEndLine;
  protected int texStartColumn;
  protected int texEndColumn;
  
  /**
   * Creates a new TexParagraph.
   */
  public TexParagraph() {
    this.texElements = new ArrayList<>();
    this.pdfParagraphs = new ArrayList<>();
    this.texStartLine = Integer.MAX_VALUE;
    this.texEndLine = Integer.MIN_VALUE;
    this.texStartColumn = Integer.MAX_VALUE;
    this.texEndColumn = Integer.MIN_VALUE;
  }
  
  /**
   * Adds the given tex element to this paragraph.
   */
  public void addTexElement(Element element) {
    texElements.add(element);
    texStartLine = Math.min(texStartLine, element.getBeginLineNumber());
    texStartColumn = Math.min(texStartColumn, element.getBeginColumnNumber());
    texEndLine = Math.max(texEndLine, element.getEndLineNumber());
    texEndColumn = Math.max(texEndColumn, element.getEndColumnNumber());
  }

  // ---------------------------------------------------------------------------
  
  /**
   * Adds the given pdf paragraph to this tex paragraph.
   */
  public void addPdfParagraph(PdfParagraph paragraph) {
    this.pdfParagraphs.add(paragraph);
  }
  
  /**
   * Sets the pdf paragraphs of this tex paragraph.
   */
  public void setPdfParagraphs(List<PdfParagraph> paragraphs) {
    this.pdfParagraphs = paragraphs;
  }
  
  /**
   * Returns the pdf paragraphs of this tex paragraph.
   */
  public List<PdfParagraph> getPdfParagraphs() {
    return this.pdfParagraphs;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the number of elements.
   */
  public int getNumTexElements() {
    return this.texElements.size();
  }

  /**
   * Returns the start line of this paragraph in tex file.
   */
  public int getTexStartLine() {
    return texStartLine;
  }

  /**
   * Returns the end line of this paragraph in tex file.
   */
  public int getTexEndLine() {
    return texEndLine;
  }

  /**
   * Returns the start column of this paragraph in tex file.
   */
  public int getTexStartColumn() {
    return texStartColumn;
  }

  /**
   * Returns the end column of this paragraph in tex file.
   */
  public int getTexEndColumn() {
    return texEndColumn;
  }

  /**
   * Returns the elements of this paragraph in tex file.
   */
  public List<Element> getTexElements() {
    return texElements;
  }
  
  @Override
  public String toString() {
    return "[" + texStartLine + ", " + texEndLine + "]";
  }
}
