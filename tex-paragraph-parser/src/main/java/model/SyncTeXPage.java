package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A synctex page.
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
  
  protected Set<Integer> texLineNumbers = new HashSet<>();
  
  /**
   * Creates a new syntex page.
   */
  public SyncTeXPage(int num) {
    this.pageNum = num;
    this.records = new ArrayList<>();
  }
  
  public void addRecords(List<SyncTeXBoundingBox> records) {
    for (SyncTeXBoundingBox record : records) {
      addRecord(record);
    }
  }
  
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
  
  public int getSmallestTexLineNumber() {
    return this.smallestTexLineNumber;
  }
  
  public int getLargestTexLineNumber() {
    return this.largestTexLineNumber;
  }
  
  public int getPageNum() {
    return this.pageNum;
  }
  
  public List<SyncTeXBoundingBox> getRecords() {
    return this.records;
  }
  
  public boolean containsTexLineNumber(int line) {
    return this.texLineNumbers.contains(line);
  }
}
