package model;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a TeX file.
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
   * The base directory.
   */
  protected Path baseDirectory;
  
  /**
   * The path to the related pdf file.
   */
  protected Path pdfFile;

  /**
   * The path to the synctex file.
   */
  protected Path synctexFile;
  
  /**
   * The number of lines.
   */
  protected int numTeXLines;
    
  /**
   * The list of paragraphs of this tex file.
   */
  protected List<TeXParagraph> texParagraphs;
      
  /**
   * The parsed document.
   */
  protected Document document;
  
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
   * Returns the relative path to the temporary tex file.
   */
  public Path getRelativeTmpPath() {
    if (baseDirectory != null) {
      return baseDirectory.relativize(tmpTexFile);  
    }
    return null;
  }
  
  /**
   * Sets the path to the temporary tex file.
   */
  public void setTmpPath(Path tmpPath) {
    this.tmpTexFile = tmpPath;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the path to the base directory.
   */
  public Path getBaseDirectory() {
    return baseDirectory;
  }
  
  /**
   * Sets the path to the base directory.
   */
  public void setBaseDirectory(Path baseDirectory) {
    this.baseDirectory = baseDirectory;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the path to the related pdf file.
   */
  public Path getPdfPath() {
    return pdfFile;
  }
  
  /**
   * Sets the path to the related pdf file and instantiate the page identifier.
   */
  public void setPdfPath(Path pdfPath) {
    this.pdfFile = pdfPath;
  }
  
  // ---------------------------------------------------------------------------
  
  /**
   * Returns the path to the related synctex file.
   */
  public Path getSynctexPath() {
    return synctexFile;
  }
  
  /**
   * Sets the path to the related synctex file.
   */
  public void setSynctexPath(Path synctexPath) {
    this.synctexFile = synctexPath;
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
  
  // ---------------------------------------------------------------------------
  
  /**
   * Sets the parsed document.
   */
  public void setDocument(Document document) {
    this.document = document;
  }
  
  /**
   * Returns the tex elements in this tex file.
   */
  public List<Element> getTeXElements() {
    return this.document != null ? document.getElements() : null;
  }
}
