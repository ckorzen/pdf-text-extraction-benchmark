package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class containing all synctex bounding boxes of a page.
 * 
 * @author Claudius Korzen
 */
public class SyncTeXPage {
  /**
   * The number of this page.
   */
  protected int pageNum;
  
  /**
   * The records in this page.
   */
  protected List<SyncTeXBoundingBox> records;
  
  /**
   * The smallest tex line number a record belongs to. 
   */
  protected int smallestTexLineNumber = Integer.MAX_VALUE;
  
  /**
   * The largest tex line number a record belongs to. 
   */
  protected int largestTexLineNumber = Integer.MIN_VALUE;
  
  /**
   * The set of tex line numbers.
   */
  protected Set<Integer> texLineNumbers = new HashSet<>();
  
  /**
   * Creates a new synctex page.
   */
  public SyncTeXPage(int num) {
    this.pageNum = num;
    this.records = new ArrayList<>();
  }
  
  /**
   * Adds the given records to this page.
   */
  public void addRecords(List<SyncTeXBoundingBox> records) {
    for (SyncTeXBoundingBox record : records) {
      addRecord(record);
    }
  }
  
  /**
   * Adds the given record to this page.
   */
  public void addRecord(SyncTeXBoundingBox record) {
    if (record != null) {
      this.records.add(record);
      
      if (record.getTexLineNumber() < this.smallestTexLineNumber) {
        this.smallestTexLineNumber = record.getTexLineNumber();
      }
      if (record.getTexLineNumber() > this.largestTexLineNumber) {
        this.largestTexLineNumber = record.getTexLineNumber();
      }
      
      texLineNumbers.add(record.getTexLineNumber());
    }
  }
  
  /**
   * Returns the smallest tex line number for which this page holds a bounding 
   * box.
   */
  public int getSmallestTexLineNumber() {
    return this.smallestTexLineNumber;
  }
  
  /**
   * Returns the largest tex line number for which this page holds a bounding 
   * box.
   */
  public int getLargestTexLineNumber() {
    return this.largestTexLineNumber;
  }
  
  /**
   * Returns the page number.
   */
  public int getPageNum() {
    return this.pageNum;
  }
  
  /**
   * Returns the records of this page. 
   */
  public List<SyncTeXBoundingBox> getRecords() {
    return this.records;
  }
  
  /**
   * Returns true, if this page contains a bounding box for given line.
   */
  public boolean containsTexLineNumber(int line) {
    return this.texLineNumbers.contains(line);
  }
}
