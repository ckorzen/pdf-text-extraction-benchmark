package external;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import de.freiburg.iif.cmd.ExternalProgram;
import model.TeXFile;

/**
 * Class to run 'pdflatex' on command line.
 * 
 * @author Claudius Korzen
 */
public class PdfLaTeX extends ExternalProgram {
  protected static final String TEXMFKEY = "TEXINPUTS";
  
  /** 
   * Creates a new instance of PdfLateX.
   */
  protected PdfLaTeX() {
    super("/usr/bin/pdflatex");
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file.
   */
  public PdfLaTeX(Path texFile, String texmfPath) {
    this();
        
    // Define environment variable such that needed sty files are found.
    if (texmfPath != null && !texmfPath.isEmpty()) {
      // Make sure, that the path ends with "//:"
      if (texmfPath.endsWith("/")) {
        texmfPath += "/:";
      } else {
        texmfPath += "//:";
      }
      addEnvironmentVariable(TEXMFKEY, texmfPath);
    }
            
    affirm(texFile != null, "No TeX file given.");
    affirm(Files.isRegularFile(texFile), "TeX file doesn't exist.");

    addOption("-interaction", "nonstopmode");
    addArgument(texFile.toAbsolutePath().toString());
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true.
   */
  public PdfLaTeX(Path texFile, String texmfPath, boolean withSyncTex) {
    this(texFile, texmfPath);
    
    if (withSyncTex) {
      addOption("-synctex", "-1");
    }
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true. Writes the output files to given 
   * output directory. 
   */
  public PdfLaTeX(Path texFile, String texmfPath, boolean withSyncTex, 
      Path outputDir) {
    this(texFile, texmfPath, withSyncTex);
    
    if (outputDir != null) {
      addOption("-output-directory", outputDir.toAbsolutePath().toString());
    }
  }
  
  // ---------------------------------------------------------------------------
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file.
   */
  public PdfLaTeX(TeXFile texFile, String texmfPath) {
    this(texFile.getPath(), texmfPath);
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true.
   */
  public PdfLaTeX(TeXFile texFile, String texmfPath, boolean withSyncTex) {
    this(texFile.getPath(), texmfPath, withSyncTex);
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true. Writes the output files to given 
   * output directory. 
   */
  public PdfLaTeX(TeXFile texFile, String texmfPath, boolean withSyncTex, 
      Path outputDir) {
    this(texFile.getPath(), texmfPath, withSyncTex, outputDir);
  }
}
