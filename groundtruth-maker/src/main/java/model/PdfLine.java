package model;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;

/**
 * A simple pair holding the page number and the bounding box of a paragraph.
 * 
 * @author Claudius Korzen
 */
public class PdfLine implements Comparable<PdfLine>, HasRectangle {
  protected int texLineNumber;
  protected int pdfPageNumber;
  protected Rectangle boundingBox;

  public PdfLine(int texLineNumber, int pdfPageNumber, Rectangle boundingBox) {
    this.texLineNumber = texLineNumber;
    this.pdfPageNumber = pdfPageNumber;
    this.boundingBox = boundingBox;
  }
  
  public void setTexLineNumber(int num) {
    this.texLineNumber = num;
  }
  
  public int getTexLineNumber() {
    return texLineNumber;
  }
  
  public int getPdfPageNumber() {
    return pdfPageNumber;
  }

  public Rectangle getPdfBoundingBox() {
    return boundingBox;
  }
  
  public String toString() {
    return "[" + texLineNumber + "," + pdfPageNumber + "," + boundingBox + "]";
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PdfLine)) {
      return false;
    }
    PdfLine other = (PdfLine) object;

    return other.getPdfPageNumber() == getPdfPageNumber()
        && boundingBox.equals(other.boundingBox);
  }

  @Override
  public int hashCode() {
    int hash = Float.floatToIntBits(boundingBox.getMinX()
        + 2 * boundingBox.getMinY() + 3 * boundingBox.getMaxX()
        + 4 * boundingBox.getMaxY());

    return pdfPageNumber + hash;
  }

  @Override
  public int compareTo(PdfLine o) {
    if (getPdfPageNumber() < o.getPdfPageNumber()) {
      return -1;
    }
    if (getPdfPageNumber() > o.getPdfPageNumber()) {
      return 1;
    }

    Rectangle rect1 = getPdfBoundingBox();
    Rectangle rect2 = o.getPdfBoundingBox();
    
    if (rect1.overlapsHorizontally(rect2)) {
      if (rect1.getMinY() > rect2.getMinY()) {
        return -1;
      }
      if (rect1.getMinY() < rect2.getMinY()) {
        return 1;
      }
    } else {
      if (rect1.getMinX() < rect2.getMinX()) {
        return -1;
      }
      if (rect1.getMinX() > rect2.getMinX()) {
        return 1;
      }
    }
      
    return 0;
  }

  @Override
  public Rectangle getRectangle() {
    return boundingBox;
  }
}
