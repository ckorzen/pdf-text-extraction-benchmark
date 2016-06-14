package external;

import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import model.PdfLine;

/**
 * 
 * @author korzen
 *
 */
public class SyncTeXBox implements HasRectangle {
  public int pageNum;
  public int lineNum;
  public float x;
  public float y;
  public float width;
  public float height;
  public float depth;

  public List<SyncTeXNode> nodes;
  public Rectangle boundingBox;

  public SyncTeXBox(int pageNum, int lineNum, float x, float y, float width,
      float height, float depth) {
    this.pageNum = pageNum;
    this.lineNum = lineNum;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.nodes = new ArrayList<>();
    this.boundingBox = new SimpleRectangle(x, y, x + width, y + height);
  }

  // ===========================================================================

  public void addNode(SyncTeXNode node) {
    this.nodes.add(node);
  }

  public List<SyncTeXNode> getNodes() {
    return this.nodes;
  }

  // ---------------------------------------------------------------------------

  public void reduce() {
    int prevLineNumber = Integer.MAX_VALUE;
    List<SyncTeXNode> run = new ArrayList<>();
    List<SyncTeXNode> longestRun = new ArrayList<>();
    for (SyncTeXNode node : nodes) {
      if (node.lineNum != prevLineNumber
          && node.lineNum != prevLineNumber + 1) {
        if (run.size() > longestRun.size()) {
          longestRun = run;
        }
        run = new ArrayList<>();
      }

      run.add(node);
      prevLineNumber = node.lineNum;
    }

    if (run.size() > longestRun.size()) {
      longestRun = run;
    }

    this.nodes = longestRun;
  }

  public List<PdfLine> toLines() {
    SyncTeXNode prevNode = null;

    List<PdfLine> lines = new ArrayList<>();
    PdfLine line = null;

    for (SyncTeXNode node : nodes) {
      if (prevNode == null || prevNode.lineNum != node.lineNum) {
        if (line != null) {
          lines.add(line);
        }
        line = new PdfLine(node.lineNum, node.pageNum, boundingBox);
      }

      line.addContentRecord(node.x, node.y);

      prevNode = node;
    }

    if (line != null) {
      lines.add(line);
    }

    return lines;
  }

  public String toString() {
    return lineNum + " " + x + " " + y + " " + width + " " + height + " "
        + depth;
  }

  @Override
  public Rectangle getRectangle() {
    return this.boundingBox;
  }
}
