package util;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import de.freiburg.iif.cmd.ExternalProgram;

/**
 * Class to run 'pdflatex' on command line.
 * 
 * @author Claudius Korzen
 */
public class SyncTeX extends ExternalProgram {

  protected String texFilePath;
  protected String pdfFilePath;

  protected SyncTeX() {
    // MAYBE: Introduces extra class 'SyncTexView'?
    super("/usr/bin/synctex", "view");
  }

  public SyncTeX(Path texFile, Path pdfFile) {
    this();

    affirm(texFile != null, "No TeX file given.");
    affirm(Files.isRegularFile(texFile), "TeX file doesn't exist.");
    affirm(pdfFile != null, "No Pdf file given.");
    affirm(Files.isRegularFile(pdfFile), "Pdf file doesn't exist.");

    this.texFilePath = texFile.toAbsolutePath().toString();
    this.pdfFilePath = pdfFile.toAbsolutePath().toString();
  }

  public int run(int lineNumber, int columnNumber)
      throws IOException, TimeoutException {
    List<String> options = new ArrayList<>();
    options.add("-i");
    options.add(lineNumber + ":" + columnNumber + ":" + this.texFilePath);
    options.add("-o");
    options.add(this.pdfFilePath);
    setOptions(options);
    
    return run();
  }
}
