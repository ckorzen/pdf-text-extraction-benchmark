package external;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import identifier.PdfPageIdentifier;
import model.SyncTeXBoundingBox;
import model.TeXFile;

/**
 * 
 * @author Claudius Korzen
 */
public class SyncTeX {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
  
  /**
   * The synctex parser.
   */
  protected SyncTeXParser parser;
   
  /**
   * The default constructor.
   */
  public SyncTeX(TeXFile texFile) {
    this.texFile = texFile;
  }
  
  public List<SyncTeXBoundingBox> getBoundingBoxesOfLine(int lineNum) 
      throws IOException {
    if (this.parser == null) {
      Path synctexPath = texFile.getSynctexPath();
      PdfPageIdentifier pageIdentifier = new PdfPageIdentifier(texFile);
      this.parser = new SyncTeXParser(synctexPath, pageIdentifier);
    }
    
    return this.parser.getSyncTexBoundingBoxes(lineNum);
  }
}
