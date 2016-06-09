package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A paragraph in a tex file.
 * 
 * @author Claudius Korzen
 */
public class TeXParagraph {
  /**
   * The feature role of this paragraph.
   */
  protected String role;

  /**
   * The text of this paragraph.
   */
  protected StringBuilder textBuilder;

  /**
   * The TeX elements.
   */
  protected List<Element> texElements;

  /**
   * Flag that indicates if we have to introduce a whitespace before next text.
   */
  protected boolean introduceWhitespace;

  /**
   * The line numbers of this paragraphs in a set (to avoid duplicates).
   */
  protected HashSet<Integer> texLineNumsSet;

  /**
   * The line numbers in a list (without duplicates).
   */
  protected List<Integer> texLineNums;

  /**
   * Flag to indicate whether the list of page numbers must be recomputed.
   */
  protected boolean needTexLineNumbersUpdate;

  /**
   * The associated pdf paragraphs.
   */
  protected List<PdfParagraph> pdfParagraphs;

  /**
   * The first element in this paragraph which is not a whitespace.
   */
  protected Element firstNonWhitespaceElement;

  /**
   * The last element in this paragraph which is not a whitespace.
   */
  protected Element lastNonWhitespaceElement;

  /**
   * Creates a new TexParagraph.
   */
  public TeXParagraph() {
    this(null);
  }

  /**
   * Creates a new TexParagraph.
   */
  public TeXParagraph(String role) {
    this.role = role;
    this.textBuilder = new StringBuilder();
    this.texLineNumsSet = new HashSet<>();
    this.texLineNums = new ArrayList<>();
    this.pdfParagraphs = new ArrayList<>();
    this.texElements = new ArrayList<>();
  }

  // ---------------------------------------------------------------------------

  /**
   * Sets the role of this tex paragraph.
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Returns the role of this paragraph.
   */
  public String getRole() {
    return this.role;
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
   * Registers a whitespace to introduce before the next non-whitespace text.
   * That mechanism was introduced to only write a single whitespace even if
   * there is a run of whitespaces.
   */
  public void registerWhitespace() {
    introduceWhitespace = true;
  }

  /**
   * Writes the given text to this paragraph.
   */
  public void registerText(String text) {
    // Write a whitespace if we have to.
    if (introduceWhitespace) {
      textBuilder.append(" ");
      introduceWhitespace = false;
    }
    textBuilder.append(text);
  }

  /**
   * Returns the text of this paragraph.
   */
  public String getText() {
    return textBuilder.toString();
  }

  // ---------------------------------------------------------------------------

  /**
   * Adds the given tex element to this paragraph.
   */
  public void registerTeXElements(List<Element> elements) {
    for (Element element : elements) {
      registerTeXElement(element);
    }
  }

  /**
   * Adds the given tex element to this paragraph.
   */
  public void registerTeXElement(Element element) {
    this.texElements.add(element);

    if (element instanceof Text) {
      Text text = (Text) element;

      if (text.isWhitespace()) {
        return;
      }
    }

    if (firstNonWhitespaceElement == null) {
      firstNonWhitespaceElement = element;
    }
    lastNonWhitespaceElement = element;
    
    int beginLine = element.getBeginLineNumber();
    int endLine = element.getEndLineNumber();

    if (!texLineNumsSet.contains(beginLine)) {
      texLineNumsSet.add(beginLine);
      needTexLineNumbersUpdate = true;
    }
    if (!texLineNumsSet.contains(endLine)) {
      texLineNumsSet.add(endLine);
      needTexLineNumbersUpdate = true;
    }
  }

  /**
   * Returns the first non-whitespace element.
   */
  public Element getFirstNonWhitespaceElement() {
    return firstNonWhitespaceElement;
  }
  
  /**
   * Returns the last non-whitespace element.
   */
  public Element getLastNonWhitespaceElement() {
    return lastNonWhitespaceElement;
  }

  /**
   * Returns the tex elements.
   */
  public List<Element> getTexElements() {
    return this.texElements;
  }

  /**
   * Returns the tex line numbers of this paragraph in a list. This list is
   * sorted in ascending order and contains *no* duplicates.
   */
  public List<Integer> getTexLineNumbers() {
    // The list is derived from the set of line numbers. Check if we have to
    // recompute this list (because the set of line numbers changed).
    if (this.needTexLineNumbersUpdate) {
      this.texLineNums = computeTexLineNumbers();
      this.needTexLineNumbersUpdate = false;
    }
    return this.texLineNums;
  }

  /**
   * Computes the sorted list of line numbers (with no duplicates) from the set
   * of line numbers.
   */
  public List<Integer> computeTexLineNumbers() {
    List<Integer> texLineNums = new ArrayList<>(texLineNumsSet);
    // Sort the line numbers in ascending order.
    Collections.sort(texLineNums);
    return texLineNums;
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns true, if the text in this paragraph is empty, false otherwise.
   */
  public boolean isEmpty() {
    return getTexLineNumbers().isEmpty();
  }

  /**
   * Returns the start line of this paragraph in tex file.
   */
  public int getTexStartLine() {
    List<Integer> lineNums = getTexLineNumbers();
    if (!lineNums.isEmpty()) {
      return lineNums.get(0);
    }
    return -1;
  }

  /**
   * Returns the end line of this paragraph in tex file.
   */
  public int getTexEndLine() {
    List<Integer> lineNums = getTexLineNumbers();
    if (!lineNums.isEmpty()) {
      return lineNums.get(lineNums.size() - 1);
    }
    return -1;
  }

  @Override
  public String toString() {
    return role + " " + getTexLineNumbers().toString();
  }
}
