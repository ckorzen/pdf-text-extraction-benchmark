package model;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;

/**
 * A pdf paragraph, defined by the page number and the bounding box in pdf.
 * 
 * @author Claudius Korzen
 */
public class PdfParagraph implements HasRectangle {
  /**
   * The page number of this paragraph.
   */
  protected int pageNum;
  
  /**
   * The bounding box of this paragraph.
   */
  protected Rectangle boundingBox;
  
  /**
   * Creates a new pdf paragraph.
   */
  public PdfParagraph() {
    
  }
  
  @Override
  public Rectangle getRectangle() {
    return boundingBox;
  }
}
