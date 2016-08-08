package visualizer;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.freiburg.iif.model.Rectangle;
import drawer.PdfDrawer;
import drawer.pdfbox.PdfBoxDrawer;
import model.PdfParagraph;
import model.TeXFile;
import model.TeXParagraph;

/**
 * Class to visualize tex paragraphs in pdf.
 * 
 * @author Claudius Korzen
 */
public class TeXParagraphVisualizer {

  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
  
  /**
   * Creates a new visualizer.
   */
  public TeXParagraphVisualizer(TeXFile texFile) {
    this.texFile = texFile;
  }
  
  /**
   * Visualizes the given bounding boxes.
   */
  public void visualize(Path target, List<String> roles) throws IOException {
    PdfDrawer drawer = new PdfBoxDrawer(texFile.getPdfPath());
        
    for (TeXParagraph p : texFile.getTeXParagraphs()) {      
      // Don't consider the paragraph if there is a list of roles given and
      // it doesn't contain the role of the paragraph.
      if (roles != null && !roles.contains(p.getRole())) {
        continue;
      }
      
      for (PdfParagraph x : p.getPdfParagraphs()) {
        Rectangle rect = x.getPdfBoundingBox();
        int pageNum = x.getPdfPageNumber();
        Color color = Color.red;
        String role = p.getRole();
        
        drawer.drawRectangle(rect, pageNum, color);
        drawer.drawText(role, pageNum, rect.getUpperLeft(), color, 8);
      }
    }

    OutputStream os = Files.newOutputStream(target);
    drawer.writeTo(os);
    os.close();
  }
}
