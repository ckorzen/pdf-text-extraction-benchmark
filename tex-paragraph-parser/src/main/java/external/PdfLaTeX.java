package external;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.nio.file.Files;
import java.nio.file.Path;

import de.freiburg.iif.cmd.ExternalProgram;
import model.TeXFile;

/**
 * Class to run 'pdflatex' on command line.
 * 
 * @author Claudius Korzen
 */
public class PdfLaTeX extends ExternalProgram {
  /** 
   * Creates a new instance of PdfLateX.
   */
  protected PdfLaTeX() {
    super("/usr/bin/pdflatex");
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file.
   */
  public PdfLaTeX(Path texFile) {
    this();
    
    affirm(texFile != null, "No TeX file given.");
    affirm(Files.isRegularFile(texFile), "TeX file doesn't exist.");
    
    addOption("-interaction", "nonstopmode");
    addArgument(texFile.toAbsolutePath().toString());
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true.
   */
  public PdfLaTeX(Path texFile, boolean withSyncTex) {
    this(texFile);
    
    if (withSyncTex) {
      addOption("-synctex", "-1");
    }
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true. Writes the output files to given 
   * output directory. 
   */
  public PdfLaTeX(Path texFile, boolean withSyncTex, Path outputDir) {
    this(texFile, withSyncTex);
    
    if (outputDir != null) {
      addOption("-output-directory", outputDir.toAbsolutePath().toString());
    }
  }
  
  // ---------------------------------------------------------------------------
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file.
   */
  public PdfLaTeX(TeXFile texFile) {
    this(texFile.getPath());
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true.
   */
  public PdfLaTeX(TeXFile texFile, boolean withSyncTex) {
    this(texFile.getPath(), withSyncTex);
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true. Writes the output files to given 
   * output directory. 
   */
  public PdfLaTeX(TeXFile texFile, boolean withSyncTex, Path outputDir) {
    this(texFile.getPath(), withSyncTex, outputDir);
  }
}
