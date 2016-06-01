package model;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a TeX file.
 * 
 * @author Claudius Korzen
 */
public class TeXFile {
  /**
   * The path to this TeX file.
   */
  protected final Path texFile;
  
  /**
   * The path to the enriched tex file.
   */
  protected Path enrichedTexFile;
  
  /**
   * The path to the related pdf file.
   */
  protected Path pdfFile;

  /**
   * The number of lines.
   */
  protected int numLines;
  
  /**
   * The list of paragraphs of this tex file.
   */
  protected List<TexParagraph> texParagraphs;

  /**
   * The list of pdf lines.
   */
  protected List<PdfLine> pdfLines;
  
  // ---------------------------------------------------------------------------
  
  /**
   * Creates a new instance of TeXFile.
   */
  public TeXFile(Path texFile) {
    affirm(texFile != null);
    affirm(Files.isRegularFile(texFile), "The tex file doesn't exist.");
    
    this.texFile = texFile;
    this.texParagraphs = new ArrayList<>();
    this.pdfLines = new ArrayList<>();
  }

  // ---------------------------------------------------------------------------
  
  /**
   * Returns the path to this tex file.
   */
  public Path getPath() {
    return texFile;
  }
  
  /**
   * Returns the path to the enriched tex file.
   */
  public Path getEnrichedTexPath() {
    return enrichedTexFile;
  }
  
  /**
   * Sets the path to the enriched tex file.
   */
  public void setEnrichedTexPath(Path enrichedPath) {
    this.enrichedTexFile = enrichedPath;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the path to the related pdf file.
   */
  public Path getPdfPath() {
    return pdfFile;
  }
  
  /**
   * Sets the path to the related pdf file.
   */
  public void setPdfPath(Path pdfPath) {
    this.pdfFile = pdfPath;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the paragraphs in this tex file.
   */
  public List<TexParagraph> getTeXParagraphs() {
    return texParagraphs;
  }

  /**
   * Sets the paragraphs of this tex file.
   */
  public void setTeXParagraphs(List<TexParagraph> paragraphs) {
    this.texParagraphs = paragraphs;
  }
  
  /**
   * Adds the given paragraph to this tex file.
   */
  public void addTeXParagraph(TexParagraph paragraph) {
    this.texParagraphs.add(paragraph);
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the lines in the pdf file.
   */
  public List<PdfLine> getPdfLines() {
    return pdfLines;
  }

  /**
   * Sets the pdf lines.
   */
  public void setPdfLines(List<PdfLine> lines) {
    this.pdfLines = lines;
  }
  
  /**
   * Adds the given pdf line.
   */
  public void addPdfLine(PdfLine line) {
    this.pdfLines.add(line);
  }

  // ---------------------------------------------------------------------------
  
  /**
   * Returns the number of lines.
   */
  public int getNumLines() {
    return numLines;
  }
  
  /**
   * Sets the number of lines.
   */
  public void setNumLines(int numLines) {
    this.numLines = numLines;
  }
}
