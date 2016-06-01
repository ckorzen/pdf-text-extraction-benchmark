package paraboxes;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import drawer.PdfDrawer;
import drawer.pdfbox.PdfBoxDrawer;
import model.PdfLine;
import model.PdfParagraph;
import model.TeXFile;
import model.TexParagraph;

/**
 * Class to identify paragraphs from tex files.
 * 
 * @author Claudius Korzen
 */
public class ParagraphsIdentifier {
  /**
   * The tex file to process.
   */
  protected Path texFile;

  /**
   * The default constructor.
   */
  public ParagraphsIdentifier(Path texFile) {
    affirm(texFile != null, "No tex file given.");
    affirm(Files.isRegularFile(texFile), "Tex file doesn't exist.");

    this.texFile = texFile.toAbsolutePath();
  }

  /**
   * Identifies the bounding boxes of paragraphs in the given tex file.
   */
  public TeXFile identify() throws Exception {
    TeXFile texFile = new TeXFile(this.texFile);

    // Identify the paragraphs from tex file.
    identifyTeXParagraphs(texFile);

    // Identify the bounding boxes of paragraphs in pdf.
    identifyBoundingBoxes(texFile);

    // Visualize the bounding boxes.
//    visualizeBoundingBoxes(texFile);

    return texFile;
  }

  // ===========================================================================

  /**
   * Identifies the paragraphs in the given tex file.
   */
  protected void identifyTeXParagraphs(TeXFile texFile) throws Exception {
    new TeXParagraphsIdentifier(texFile).identify();
  }

  /**
   * Identifies the bounding boxes of paragraphs in pdf.
   */
  protected void identifyBoundingBoxes(TeXFile texFile) throws Exception {
    new PdfParagraphsIdentifier(texFile).identify();
  }

  /**
   * Visualizes the given bounding boxes.
   */
  protected void visualizeBoundingBoxes(TeXFile texFile) throws IOException {
    PdfDrawer drawer = new PdfBoxDrawer(texFile.getPdfPath());
    List<PdfLine> lines = texFile.getPdfLines();
    for (PdfLine l : lines) {
      // drawer.drawRectangle(l.getPdfBoundingBox(), l.getPdfPageNumber());
      // drawer.drawText("" + i++, l.getPdfPageNumber(),
      // l.getPdfBoundingBox().getLowerRight(), Color.BLACK, 5);
      drawer.drawText("" + l.getTexLineNumber(), l.getPdfPageNumber(),
          l.getPdfBoundingBox().getUpperRight(), Color.RED, 5);
    }

    for (TexParagraph p : texFile.getTeXParagraphs()) {
      for (PdfParagraph x : p.getPdfParagraphs()) {
        drawer.drawRectangle(x.getPdfBoundingBox(), x.getPdfPageNumber());
      }
    }

    File output = new File("/home/korzen/Downloads/paraboxes.pdf");

    FileOutputStream fos = new FileOutputStream(output);
    drawer.writeTo(fos);
    fos.close();
  }
}
