//package external;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.freiburg.iif.model.HasRectangle;
//import de.freiburg.iif.model.Rectangle;
//import de.freiburg.iif.model.simple.SimpleRectangle;
//
///**
// * Either an hbox or vbox, containing various records.
// * 
// * @author Claudius Korzen
// */
//public class PdfBoundingBox implements HasRectangle {
//  /**
//   * The records of this box.
//   */
//  public List<SyncTeXRecord> records;
//
//  /**
//   * The page number of this box.
//   */
//  protected int pageNum;
//
//  /**
//   * The tex line number of this box.
//   */
//  protected int lineNum;
//
//  /**
//   * The bounding box of this box.
//   */
//  protected Rectangle boundingBox;
//
//  /**
//   * Creates a new box.
//   */
//  public PdfBoundingBox(int pageNum, int lineNum, Rectangle boundingBox) {
//    this.pageNum = pageNum;
//    this.lineNum = lineNum;
//    this.boundingBox = boundingBox;
//    this.records = new ArrayList<>();
//  }
//
//  // ===========================================================================
//
//  /**
//   * Adds the given record to this box.
//   */
//  public void addRecord(SyncTeXRecord record) {
//    this.records.add(record);
//  }
//
//  /**
//   * Returns the records of this box.
//   */
//  public List<SyncTeXRecord> getRecords() {
//    return this.records;
//  }
//
//  // ---------------------------------------------------------------------------
//
//  /**
//   * Returns the page number of this box.
//   */
//  public int getPageNumber() {
//    return this.pageNum;
//  }
//
//  /**
//   * Returns the tex line number of this box.
//   */
//  public int getTexLineNumber() {
//    return this.lineNum;
//  }
//
//  /**
//   * Returns the bounding box of this box.
//   */
//  public Rectangle getRectangle() {
//    return this.boundingBox;
//  }
//
//  // ---------------------------------------------------------------------------
//
//  /**
//   * Computes the bounding boxes for the boxes records of type "x".
//   */
//  public List<Rectangle> computeBoundingBoxes() {
//    List<Rectangle> rects = new ArrayList<>();
//    float currentXPosition = getRectangle().getMinX();
//
//    for (SyncTeXRecord record : getRecords()) {
//      if ("x".equals(record.type)) { // TODO
//        Rectangle rect = new SimpleRectangle();
//        rect.setMinX(currentXPosition);
//        rect.setMinY(record.getRectangle().getMinY());
//        rect.setMaxX(record.getRectangle().getMinX());
//        rect.setMaxY(this.boundingBox.getMaxY());
//        rects.add(rect);
//      }
//      currentXPosition = record.getRectangle().getMinX();
//    } 
//    return rects;
//  }
//
//  @Override
//  public String toString() {
//    return pageNum + " " + lineNum + " " + boundingBox;
//  }
//}
