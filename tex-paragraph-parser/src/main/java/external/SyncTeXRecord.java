//package external;
//
//import de.freiburg.iif.model.HasRectangle;
//import de.freiburg.iif.model.Rectangle;
//
///**
// * A record of syntex boxes. 
// * 
// * @author Claudius korzen
// */
//public class SyncTeXRecord implements HasRectangle {
//  /**
//   * The type of the record ("v", "h", "x", "g", "k" or "$").
//   */
//  protected String type;
//  
//  /**
//   * The page number of this record.
//   */
//  protected int pageNum;
//  
//  /**
//   * The tex line number of this record.
//   */
//  protected int lineNum;
//  
//  /**
//   * The "flat" (no width and no height) rectangle of this box.
//   */
//  protected Rectangle rectangle;
//
//  /**
//   * Creates a new record.
//   */
//  public SyncTeXRecord(String type, int pageNum, int lineNum, Rectangle rect) {
//    this.type = type;
//    this.pageNum = pageNum;
//    this.lineNum = lineNum;
//    this.rectangle = rect;
//  }
//  
//  @Override
//  public Rectangle getRectangle() {
//    return this.rectangle;
//  }
//  
//  @Override
//  public String toString() {
//    return pageNum + " " + lineNum + " " + rectangle;
//  }
//}
