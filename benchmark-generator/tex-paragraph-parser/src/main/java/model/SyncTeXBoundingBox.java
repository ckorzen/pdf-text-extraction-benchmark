package model;

import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;

/**
 * A "position" (bounding box + page number) within pdf files.
 * 
 * @author Claudius Korzen
 */
public class SyncTeXBoundingBox implements HasRectangle {
  /**
   * The records of this box.
   */
  public List<SyncTeXBoundingBox> records;

  /**
   * The synctex type of the record ("[", "(", "v", "h", "x", "g", "k" or "$").
   */
  protected char type;

  /**
   * The page number of this box.
   */
  protected int pageNum;

  /**
   * The tex line number of this box.
   */
  protected int lineNum;

  /**
   * The bounding box of this box.
   */
  protected Rectangle rectangle;

  /**
   * Creates a new box.
   */
  public SyncTeXBoundingBox(char type, int pageNum, int lineNum,
      Rectangle rect) {
    this.type = type;
    this.pageNum = pageNum;
    this.lineNum = lineNum;
    this.rectangle = rect;
    this.records = new ArrayList<>();
  }

  /**
   * Extends this box by given another box.
   */
  public void extend(SyncTeXBoundingBox other) {
    getRectangle().unite(other.getRectangle());
    this.records.addAll(other.getRecords());
  }

  // ===========================================================================

  /**
   * Adds the given record to this box.
   */
  public void add(SyncTeXBoundingBox record) {
    this.records.add(record);
  }

  /**
   * Returns the records of this box.
   */
  public List<SyncTeXBoundingBox> getRecords() {
    return this.records;
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns the synctex type of this box.
   */
  public char getType() {
    return this.type;
  }

  /**
   * Returns the page number of this box.
   */
  public int getPageNumber() {
    return this.pageNum;
  }

  /**
   * Returns the tex line number of this box.
   */
  public int getTexLineNumber() {
    return this.lineNum;
  }

  /**
   * Returns the bounding box of this box.
   */
  public Rectangle getRectangle() {
    return this.rectangle;
  }

  // ---------------------------------------------------------------------------
  
  /**
   * Sets the tex line number of this box.
   */
  public void setTexLineNumber(int lineNum) {
    this.lineNum = lineNum;
  }
  
  // ---------------------------------------------------------------------------

  @Override
  public String toString() {
    return pageNum + " " + type + " " + lineNum + " " + rectangle;
  }
}
