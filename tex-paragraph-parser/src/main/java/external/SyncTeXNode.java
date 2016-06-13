package external;

public class SyncTeXNode {
  int pageNum;
  int lineNum;
  float x;
  float y;

  public SyncTeXNode(int pageNum, int lineNum, float x, float y) {
    this.pageNum = pageNum;
    this.lineNum = lineNum;
    this.x = x;
    this.y = y;
  }
    
  public String toString() {
    return lineNum + " " + x + " " + y;
  }
}
