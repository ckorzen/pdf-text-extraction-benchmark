package model;

import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;

/**
 * A pdf paragraph.
 */
public class PdfParagraph implements HasRectangle {
  /**
   * The lines in this paragraph.
   */
  protected List<SyncTeXBoundingBox> pdfLines;
  
  /**
   * The bounding box.
   */
  protected Rectangle pdfBoundingBox;
  
  /**
   * Flag that indicates if the bounding box must be recomputed.
   */
  protected boolean needsPdfBoundingBoxUpdate;
  
  /**
   * The page number.
   */
  protected int pdfPageNumber;
  
  /**
   * Creates new pdf paragraph.
   */
  public PdfParagraph() {
    this.pdfLines = new ArrayList<>();
  }
  
  /**
   * Adds the given pdf line to this paragraph.
   */
  public void addPdfLine(SyncTeXBoundingBox line) {
    this.pdfLines.add(line);
    this.needsPdfBoundingBoxUpdate = true;
    this.pdfPageNumber = line.getPageNumber();
  }
  
  /**
   * Returns the bounding box of this paragraph.
   */
  public Rectangle getPdfBoundingBox() {
    if (this.pdfBoundingBox == null || this.needsPdfBoundingBoxUpdate) {
      this.pdfBoundingBox = computePdfBoundingBox();
      this.needsPdfBoundingBoxUpdate = false;
    }
    return this.pdfBoundingBox;
  }
  
  /**
   * Computes the bounding box for this paragraph.
   */
  public Rectangle computePdfBoundingBox() {
    return SimpleRectangle.computeBoundingBox(pdfLines);
  }

  /**
   * Returns true, if this paragraph contains no lines, false otherwise.
   */
  public boolean isEmpty() {
    return this.pdfLines.isEmpty();
  }
  
  @Override
  public Rectangle getRectangle() {
    return getPdfBoundingBox();
  }
  
  /**
   * Returns the page number.
   */
  public int getPdfPageNumber() {
    return pdfPageNumber;
  }
}
