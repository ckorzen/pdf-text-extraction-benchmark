import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import identifier.PdfParagraphsIdentifier;
import identifier.TeXParagraphsIdentifier;
import model.TeXFile;

public class ExecutorServiceTest {
  public static void main(String[] args) throws InterruptedException {
    TexFileWorker worker = new TexFileWorker(Paths.get("/home/korzen/arxiv-benchmark/evaluation/input_sample/src/0001/hep-th0001222/hep-th0001222.tex"));
    
    worker.start();
    
    Thread.sleep(5000);

    worker.interrupt();
    
    System.out.println("XXX");
  }
}

class TexFileWorker extends Thread {

  public Path file;
  private volatile boolean running = true;
  
  public TexFileWorker(Path file) {
    this.file = file;
  }
    
  public void terminate() {
    running = false;
  }
  
  /**
   * Processes the given tex file. Identifies the paragraphs from given tex
   * file and their positions in pdf file if global flag
   * 'identifyPdfParagraphs' is set to true. Serializes and visualizes the
   * paragraphs if related paths are given.
   */
  public void run() {
    while (running) {
      try {
        System.out.println("process");
        processTeXFile(this.file);
      } catch (InterruptedException e) {
        System.err.println("Error on processing: " + this.file + ": ");
        e.printStackTrace();
        
        running = false;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void processTeXFile(Path file) throws Exception {
    TeXFile texFile = new TeXFile(file);
  
    // Identify the paragraphs in the given tex file.
    identifyTexParagraphs(texFile);

    // Identify the postions of tex paragraphs in tex file.
    identifyPdfParagraphs(texFile, null);
  }

  // -------------------------------------------------------------------------

  /**
   * Identifies the paragraphs from given tex file.
   */
  protected void identifyTexParagraphs(TeXFile texFile) throws IOException {
    new TeXParagraphsIdentifier(texFile).identify();
  }

  /**
   * Identifies the positions of paragraphs from given tex file in related pdf
   * file.
   */
  protected void identifyPdfParagraphs(TeXFile texFile,
      List<String> texmfPaths)
    throws IOException {
    new PdfParagraphsIdentifier(texFile, texmfPaths).identify();
  }
}