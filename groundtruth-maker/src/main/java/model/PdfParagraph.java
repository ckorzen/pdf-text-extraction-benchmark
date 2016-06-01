package model;

import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;

/**
 * A paragraph in pdf.
 */
public class PdfParagraph implements HasRectangle {
  protected List<PdfLine> pdfLines = new ArrayList<>();
  protected Rectangle pdfBoundingBox = null;
  protected boolean needsPdfBoundingBox = true;
  protected int pdfPageNumber;
  
  /**
   * Adds the given pdf line to this paragraph.
   */
  public void addPdfLine(PdfLine line) {
    pdfLines.add(line);
    needsPdfBoundingBox = true;
    this.pdfPageNumber = line.getPdfPageNumber();
  }
  
  public Rectangle getPdfBoundingBox() {
    if (this.needsPdfBoundingBox) {
      this.pdfBoundingBox = computePdfBoundingBox();
      needsPdfBoundingBox = false;
    }
    return this.pdfBoundingBox;
  }
  
  public Rectangle computePdfBoundingBox() {
    return SimpleRectangle.computeBoundingBox(pdfLines);
  }

  public int getNumLines() {
    return this.pdfLines.size();
  }
  
  @Override
  public Rectangle getRectangle() {
    return getPdfBoundingBox();
  }
  
  public int getPdfPageNumber() {
    return pdfPageNumber;
  }
}
