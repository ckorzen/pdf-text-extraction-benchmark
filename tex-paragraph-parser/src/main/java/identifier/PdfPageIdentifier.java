package identifier;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import model.TeXFile;

/**
 * Class to identify the bounding boxes of pages in pdf files. 
 * The coordinates provided by synctex are relative to the upper left, but we
 * need coordinates relative to the lower left. Hence, we have to "invert" the
 * coordinates. For that we need to know the dimensions of each page.
 * 
 * @author Claudius Korzen
 */
public class PdfPageIdentifier {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
  
  /** 
   * The obtained bounding boxes of pages. 
   */
  protected List<Rectangle> pageBoundingBoxes;
  
  /**
   * Creates a new pdf page identifier for given tex file.
   */
  public PdfPageIdentifier(TeXFile texFile) {
    this.texFile = texFile;
  }
  
  /**
   * Returns the bounding box of given page.
   */
  public Rectangle getBoundingBox(int pageNum) {    
    if (this.pageBoundingBoxes == null) {
      // Lazy loading: Only load the bounding boxes on first request.
      this.pageBoundingBoxes = loadPageBoundingBoxes(texFile);
    }
    
    affirm(pageNum > 0, "The page number is too small");
    affirm(pageNum < pageBoundingBoxes.size(), "The page number is too large");
    
    return this.pageBoundingBoxes.get(pageNum);
  }
  
  // ===========================================================================
  
  /**
   * Reads the bounding boxes of pages in given pdf file.
   */
  protected List<Rectangle> loadPageBoundingBoxes(TeXFile texFile) {
    List<Rectangle> pageBoundBoxes = new ArrayList<>();

    try {
      Path pdfPath = texFile.getPdfPath();
      PDDocument pdDocument = PDDocument.load(pdfPath.toFile());
      try {
        PDPageTree pages = pdDocument.getDocumentCatalog().getPages();
        pageBoundBoxes = new ArrayList<>();
        pageBoundBoxes.add(null); // add dummy because pages are 1-based.
  
        // Compute the bounding boxes.
        for (PDPage page : pages) {
          Rectangle boundingBox = new SimpleRectangle();
  
          PDRectangle box = page.getCropBox();
          if (box == null) {
            box = page.getMediaBox();
          }
          if (box != null) {
            boundingBox.setMinX(box.getLowerLeftX());
            boundingBox.setMinY(box.getLowerLeftY());
            boundingBox.setMaxX(box.getUpperRightX());
            boundingBox.setMaxY(box.getUpperRightY());
          }
  
          pageBoundBoxes.add(boundingBox);
        }
      } finally {
        pdDocument.close();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't load the pdf file.", e);
    }

    return pageBoundBoxes;
  }
}
