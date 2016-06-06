package identifier;

import model.TeXFile;

/**
 * Class to identify pdf paragraphs from tex files.
 * 
 * @author Claudius Korzen
 */
public class PdfParagraphsIdentifier {
  /** 
   * The tex file to process. 
   */
  protected TeXFile texFile;
  
  /**
   * The default constructor.
   */
  public PdfParagraphsIdentifier(TeXFile texFile) {
    this.texFile = texFile;
  }
  
  /**
   * Identifies the pdf paragraphs in given tex file.
   */
  public void identify() {
    
  }
}
