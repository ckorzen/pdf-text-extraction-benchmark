package external;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import de.freiburg.iif.cmd.ExternalProgram;

/**
 * Class to run 'synctex' on command line.
 * 
 * @author Claudius Korzen
 */
public class SyncTeX extends ExternalProgram {
  /**
   * The path to the tex file.
   */
  protected String texFilePath;
  
  /**
   * The path to the pdf file.
   */
  protected String pdfFilePath;

  /**
   * Creates a new instance of SyncTeX.
   */
  protected SyncTeX() {
    super("/usr/bin/synctex", "view");
  }

  /**
   * Creates a new instance of SyncTeX based on the given tex file and pdf file.
   */
  public SyncTeX(Path texFile, Path pdfFile) {
    this();

    affirm(texFile != null, "No TeX file given.");
    affirm(Files.isRegularFile(texFile), "TeX file doesn't exist.");
    affirm(pdfFile != null, "No Pdf file given.");
    affirm(Files.isRegularFile(pdfFile), "Pdf file doesn't exist.");

    this.texFilePath = texFile.toAbsolutePath().toString();
    this.pdfFilePath = pdfFile.toAbsolutePath().toString();
  }

  /**
   * Runs synctex with given line number and column numbers as arguments.
   */
  public int run(int line, int column) throws IOException, TimeoutException {
    List<String> options = new ArrayList<>();
    options.add("-i");
    options.add(line + ":" + column + ":" + this.texFilePath);
    options.add("-o");
    options.add(this.pdfFilePath);
    setOptions(options);
    
    return run();
  }
}
