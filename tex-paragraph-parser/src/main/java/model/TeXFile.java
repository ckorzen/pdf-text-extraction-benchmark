package model;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A TeX file.
 * 
 * @author Claudius Korzen
 */
public class TeXFile {
  /**
   * The path to this tex file.
   */
  protected final Path texFile;
  
  /**
   * The path to a intermediate file derived from this tex file.
   */
  protected Path tmpTexFile;
  
  /**
   * The path to the related pdf file.
   */
  protected Path pdfFile;

  /**
   * The number of lines.
   */
  protected int numTeXLines;
  
  /**
   * The list of paragraphs of this tex file.
   */
  protected List<TeXParagraph> texParagraphs;
      
  /**
   * Creates a new instance of TeXFile.
   */
  public TeXFile(Path texFile) {
    affirm(texFile != null);
    affirm(Files.isRegularFile(texFile), "The tex file doesn't exist.");
    
    this.texFile = texFile;
    this.texParagraphs = new ArrayList<>();
  }

  // ---------------------------------------------------------------------------
  
  /**
   * Returns the path to this tex file.
   */
  public Path getPath() {
    return texFile;
  }
  
  /**
   * Returns the path to the temporary tex file.
   */
  public Path getTmpPath() {
    return tmpTexFile;
  }
  
  /**
   * Sets the path to the temporary tex file.
   */
  public void setTmpPath(Path tmpPath) {
    this.tmpTexFile = tmpPath;
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
   * Returns the number of lines.
   */
  public int getNumLines() {
    return numTeXLines;
  }
  
  /**
   * Sets the number of lines.
   */
  public void setNumLines(int numLines) {
    this.numTeXLines = numLines;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the paragraphs in this tex file.
   */
  public List<TeXParagraph> getTeXParagraphs() {
    return texParagraphs;
  }

  /**
   * Sets the paragraphs of this tex file.
   */
  public void setTeXParagraphs(List<TeXParagraph> paragraphs) {
    this.texParagraphs = paragraphs;
  }
  
  /**
   * Adds the given paragraph to this tex file.
   */
  public void addTeXParagraph(TeXParagraph paragraph) {
    this.texParagraphs.add(paragraph);
  }
}
