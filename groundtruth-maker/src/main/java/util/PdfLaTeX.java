package util;

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

  protected PdfLaTeX() {
    super("/usr/bin/pdflatex");
  }
  
  public PdfLaTeX(Path texFile) {
    this();
    
    affirm(texFile != null, "No TeX file given.");
    affirm(Files.isRegularFile(texFile), "TeX file doesn't exist.");
    
    addOption("-interaction", "nonstopmode");
    addArgument(texFile.toAbsolutePath().toString());
  }
  
  public PdfLaTeX(Path texFile, boolean withSyncTex) {
    this(texFile);
    
    if (withSyncTex) {
      addOption("-synctex", "-1");
    }
  }
  
  public PdfLaTeX(Path texFile, boolean withSyncTex, Path outputDir) {
    this(texFile, withSyncTex);
    
    if (outputDir != null) {
      addOption("-output-directory", outputDir.toAbsolutePath().toString());
    }
  }
  
  // ---------------------------------------------------------------------------
  
  public PdfLaTeX(TeXFile texFile) {
    this(texFile.getPath());
  }
  
  public PdfLaTeX(TeXFile texFile, boolean withSyncTex) {
    this(texFile.getPath(), withSyncTex);
  }
  
  public PdfLaTeX(TeXFile texFile, boolean withSyncTex, Path outputDir) {
    this(texFile.getPath(), withSyncTex, outputDir);
  }
}
