package external;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
  public PdfLaTeX(Path texFile, List<String> texmfPaths) {
    this();
        
    if (texmfPaths != null) {
      StringBuilder sb = new StringBuilder();
      for (String texmfPath : texmfPaths) {
        // Define environment variable such that needed sty files are found.
        if (texmfPath != null && !texmfPath.isEmpty()) {
          sb.append(texmfPath);
          // Make sure, that the path ends with "//:"
          if (texmfPath.endsWith("/")) {
            sb.append("/:");
          } else {
            sb.append("//:");
          }
        }
      }
      addEnvironmentVariable(TEXMFKEY, sb.toString());
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
  public PdfLaTeX(Path texFile, List<String> texmfPaths, boolean withSyncTex) {
    this(texFile, texmfPaths);
    
    if (withSyncTex) {
      addOption("-synctex", "-1");
    }
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true. Writes the output files to given 
   * output directory. 
   */
  public PdfLaTeX(Path texFile, List<String> texmfPaths, boolean withSyncTex, 
      Path outputDir) {
    this(texFile, texmfPaths, withSyncTex);
    
    if (outputDir != null) {
      addOption("-output-directory", outputDir.toAbsolutePath().toString());
    }
  }
  
  // ---------------------------------------------------------------------------
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file.
   */
  public PdfLaTeX(TeXFile texFile, List<String> texmfPaths) {
    this(texFile.getPath(), texmfPaths);
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true.
   */
  public PdfLaTeX(TeXFile texFile, List<String> texmfPaths, 
      boolean withSyncTex) {
    this(texFile.getPath(), texmfPaths, withSyncTex);
  }
  
  /** 
   * Creates a new instance of PdfLateX based on the given tex file. Enables
   * synctex flag if 'withSyncTex' is true. Writes the output files to given 
   * output directory. 
   */
  public PdfLaTeX(TeXFile texFile, List<String> texmfPaths, boolean withSyncTex, 
      Path outputDir) {
    this(texFile.getPath(), texmfPaths, withSyncTex, outputDir);
  }
}
