package model;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;

/**
 * A line in a pdf document.
 * 
 * @author Claudius Korzen
 */
public class PdfLine implements Comparable<PdfLine>, HasRectangle {
  /** 
   * The number of line in tex file in which this line starts. 
   */
  protected int texLineNumber;
  
  /**
   * The page number in the pdf document.
   */
  protected int pdfPageNumber;
  
  /**
   * The bounding box of this line.
   */
  protected Rectangle boundingBox;

  protected int numContentRecords;
  
  /**
   * Creates a new pdf line.
   */
  public PdfLine(int texLineNumber, int pdfPageNumber, Rectangle boundingBox) {
    this.texLineNumber = texLineNumber;
    this.pdfPageNumber = pdfPageNumber;
    this.boundingBox = boundingBox;
  }
  
  /**
   * Sets the tex line number.
   */
  public void setTexLineNumber(int num) {
    this.texLineNumber = num;
  }
  
  /**
   * Returns the tex line number.
   */
  public int getTexLineNumber() {
    return texLineNumber;
  }
  
  /**
   * Returns the pdf page number.
   */
  public int getPdfPageNumber() {
    return pdfPageNumber;
  }

  /**
   * Returns the bounding box of this line.
   */
  public Rectangle getPdfBoundingBox() {
    return boundingBox;
  }
  
  @Override
  public Rectangle getRectangle() {
    return boundingBox;
  }
  
  @Override
  public String toString() {
    return "[" + texLineNumber + "," + pdfPageNumber + "," + boundingBox + "]";
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PdfLine)) {
      return false;
    }
    PdfLine other = (PdfLine) object;

    // Objects are equal if page numbers and bounding boxes are equal.
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
  
  public void addContentRecord(float x, float y) {
    if (x < boundingBox.getMinX()) {
      boundingBox.setMinX(x);
    }
    if (x > boundingBox.getMaxX()) {
      boundingBox.setMaxX(x);
    }
    if (y < boundingBox.getMinY()) {
      boundingBox.setMinY(y);
    }
    if (y > boundingBox.getMaxY()) {
      boundingBox.setMaxY(y);
    }
    
    numContentRecords++;
  }
  
  public int getNumContentRecords() {
    return this.numContentRecords;
  }
}
