package external;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import de.freiburg.iif.rtree.RTree;
import de.freiburg.iif.rtree.SimpleRTree;
import drawer.pdfbox.PdfBoxDrawer;
import model.PdfLine;
import model.TeXFile;

/**
 * 
 * @author korzen
 *
 */
public class SyncTeXParser {
  /**
   * The tex file..
   */
  protected TeXFile texFile;

  /**
   * The magnification.
   */
  protected float magnification;

  /**
   * The x offset.
   */
  protected float xoffset;

  /**
   * The y offset.
   */
  protected float yoffset;

  /**
   * The unit.
   */
  protected float unit;

  /**
   * The current page.
   */
  protected int currentPage;

  /**
   * The previous line number.
   */
  protected PdfLine currentLine;

  /**
   * The stack of boxes.
   */
  protected Stack<Rectangle> boxesStack;

  /**
   * Index of all boxes per line.
   */
  protected Map<Integer, List<PdfLine>> linesIndex;

  PdfBoxDrawer drawer;
  RTree<PdfLine> index = new SimpleRTree<>();

  /**
   * Creates a new synctex parser.
   * 
   * @throws IOException
   */
  public SyncTeXParser(TeXFile texFile) throws IOException {
    this.linesIndex = new HashMap<>();
    this.boxesStack = new Stack<>();
    this.texFile = texFile;
    this.magnification = 1000;
    this.xoffset = 0;
    this.yoffset = 0;
    this.unit = 1;
  }

  /**
   * Parses the given synctex file.
   * 
   * @return
   */
  public Map<Integer, List<PdfLine>> parse() throws IOException {
    Path synctexPath = texFile.getSynctexPath();
    Charset cs = StandardCharsets.UTF_8;
    BufferedReader reader = Files.newBufferedReader(synctexPath, cs);

    parse(reader);

    reader.close();

    return this.linesIndex;
  }

  /**
   * Parses the given reader.
   * 
   * @throws IOException
   */
  protected void parse(BufferedReader reader) throws IOException {
    parsePreamble(reader);
    parseContent(reader);
  }

  /**
   * Parses the preamble from given reader.
   */
  protected void parsePreamble(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("Magnification")) {
        this.magnification = getColonArgFloat(line, 1, this.magnification);
      } else if (line.startsWith("X Offset")) {
        this.xoffset = getColonArgFloat(line, 1, this.xoffset);
      } else if (line.startsWith("Y Offset")) {
        this.yoffset = getColonArgFloat(line, 1, this.yoffset);
      } else if (line.startsWith("Unit")) {
        this.unit = getColonArgFloat(line, 1, this.unit);
      } else if (line.startsWith("Content")) {
        break;
      }
    }
  }

  /**
   * Parses the content from given reader.
   */
  protected void parseContent(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      char first = line.charAt(0);

      switch (first) {
        case '{':
          handlePageStart(line);
          break;
        case '}':
          handlePageEnd(line);
          break;
        case '[':
          handleVBoxStart(line);
          break;
        case ']':
          handleVBoxEnd(line);
          break;
        case '(':
          handleHBoxStart(line);
          break;
        case ')':
          handleHBoxEnd(line);
          break;
        case 'x':
          handleCurrentRecord(line);
          break;
        case 'k':
          handleKernRecord(line);
          break;
        case '$':
          handleMathRecord(line);
          break;
        case 'v':
          handleVBoxRecord(line);
          break;
        case 'h':
          handleHBoxRecord(line);
          break;
        case 'g':
          handleGlueRecord(line);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Handles a page start.
   */
  protected void handlePageStart(String line) {
    this.currentPage = Integer.parseInt(line.substring(1));
  }

  /**
   * Handles a page end.
   */
  protected void handlePageEnd(String line) {
    // Do nothing.
  }

  /**
   * Handles a start of vbox.
   */
  protected void handleVBoxStart(String line) throws IOException {
    // handleNewBox(line);
  }

  /**
   * Handles an end of vbox.
   */
  protected void handleVBoxEnd(String line) throws IOException {
    // if (currentLine != null && currentLine.getNumContentRecords() > 0) {
    // if (!linesIndex.containsKey(currentLine.getTexLineNumber())) {
    // linesIndex.put(currentLine.getTexLineNumber(), new ArrayList<PdfLine>());
    // }
    //
    // drawer.drawRectangle(currentLine.getPdfBoundingBox(),
    // currentLine.getPdfPageNumber(), Color.RED);
    // drawer.drawText("" + currentLine.getTexLineNumber(),
    // currentLine.getPdfPageNumber(),
    // currentLine.getPdfBoundingBox().getUpperLeft());
    //
    // linesIndex.get(currentLine.getTexLineNumber()).add(currentLine);
    // }
    //
    // currentLine = null;
    // boxesStack.pop();
  }

  /**
   * Handles a start of hbox.
   */
  protected void handleHBoxStart(String line) throws IOException {
    // handleNewBox(line);
  }

  /**
   * Handles an end of hbox.
   */
  protected void handleHBoxEnd(String line) throws IOException {
    // if (currentLine != null && currentLine.getNumContentRecords() > 0) {
    // if (!linesIndex.containsKey(currentLine.getTexLineNumber())) {
    // linesIndex.put(currentLine.getTexLineNumber(), new ArrayList<PdfLine>());
    // }
    //
    // drawer.drawRectangle(currentLine.getPdfBoundingBox(),
    // currentLine.getPdfPageNumber(), Color.RED);
    // drawer.drawText("" + currentLine.getTexLineNumber(),
    // currentLine.getPdfPageNumber(),
    // currentLine.getPdfBoundingBox().getUpperLeft());
    //
    // linesIndex.get(currentLine.getTexLineNumber()).add(currentLine);
    // }
    //
    // currentLine = null;
    // boxesStack.pop();
  }

  /**
   * Handles a current record.
   */
  protected void handleCurrentRecord(String line) throws IOException {
    // <current record> ::= "x" <link> ":" <point> <end of record>
    line = line.substring(1);

    String[] fields = line.split(":");
    // String link = fields[0];
    String point = fields[1];

    // <link> ::= <tag> "," <line>( "," <column>)?
    // String[] linkFields = link.split(",");
    // int lineNum = Integer.parseInt(linkFields[1]);

    // <point> ::= <integer> "," <integer>
    String[] pointFields = point.split(",");
    // int x = Integer.parseInt(pointFields[0]);
    int y = Integer.parseInt(pointFields[1]);

    // float xf = toPdfCoordinate(x);
    float yf = toPdfCoordinate(y);
    yf = texFile.getPageBoundingBox(currentPage).getMaxY() - yf;
  }

  /**
   * Handles a current record.
   * 
   * @throws IOException
   */
  protected void handleKernRecord(String line) throws IOException {
    // handleContent(line);
  }

  /**
   * Handles a math record.
   */
  protected void handleMathRecord(String line) throws IOException {
    // handleContent(line);
  }

  /**
   * Handles a glue record.
   * 
   * @throws IOException
   */
  protected void handleGlueRecord(String line) throws IOException {
    // handleContent(line);
  }

  /**
   * Handles a vbox record.
   * 
   * @throws IOException
   */
  protected void handleVBoxRecord(String line) throws IOException {
    // Do nothing.
  }

  /**
   * Handles a hbox record.
   */
  protected void handleHBoxRecord(String line) throws IOException {

  }

  protected void handleNewBox(String line) throws IOException {
    // <void vbox record> ::= "v" <link> ":" <point> ":" <size> <end of record>
    line = line.substring(1);

    String[] fields = line.split(":");
    // String link = fields[0];
    String point = fields[1];
    String size = fields[2];

    // <link> ::= <tag> "," <line>( "," <column>)?
    // String[] linkFields = link.split(",");
    // int lineNum = Integer.parseInt(linkFields[1]);

    // <point> ::= <integer> "," <integer>
    String[] pointFields = point.split(",");
    int x = Integer.parseInt(pointFields[0]);
    int y = Integer.parseInt(pointFields[1]);

    // <size> ::= <Width> "," <Height> "," <Depth>
    String[] sizeFields = size.split(",");
    int width = Integer.parseInt(sizeFields[0]);
    int height = Integer.parseInt(sizeFields[1]);
    int depth = Integer.parseInt(sizeFields[2]);

    float xf = toPdfCoordinate(x);
    float yf = toPdfCoordinate(y);
    float wf = toPdfCoordinate(width);
    float hf = toPdfCoordinate(height);
    float df = toPdfCoordinate(depth);

    yf = texFile.getPageBoundingBox(currentPage).getMaxY() - yf;

    boxesStack.push(new SimpleRectangle(xf, yf, xf + wf, yf + hf + df));
  }

  protected void handleContent(String line) throws IOException {
    // <current record> ::= "x" <link> ":" <point> <end of record>
    line = line.substring(1);

    String[] fields = line.split(":");
    String link = fields[0];
    String point = fields[1];

    // <link> ::= <tag> "," <line>( "," <column>)?
    String[] linkFields = link.split(",");
    int lineNum = Integer.parseInt(linkFields[1]);

    // <point> ::= <integer> "," <integer>
    String[] pointFields = point.split(",");
    int x = Integer.parseInt(pointFields[0]);
    int y = Integer.parseInt(pointFields[1]);

    float xf = toPdfCoordinate(x);
    float yf = toPdfCoordinate(y);
    yf = texFile.getPageBoundingBox(currentPage).getMaxY() - yf;

    if (currentLine == null || currentLine.getTexLineNumber() != lineNum) {
      if (currentLine != null && currentLine.getNumContentRecords() > 0) {
        if (!linesIndex.containsKey(currentLine.getTexLineNumber())) {
          linesIndex.put(currentLine.getTexLineNumber(),
              new ArrayList<PdfLine>());
        }
        drawer.drawRectangle(currentLine.getPdfBoundingBox(),
            currentLine.getPdfPageNumber(), Color.RED);
        drawer.drawText("" + currentLine.getTexLineNumber(),
            currentLine.getPdfPageNumber(),
            currentLine.getPdfBoundingBox().getUpperLeft());

        linesIndex.get(currentLine.getTexLineNumber()).add(currentLine);
      }

      currentLine = new PdfLine(lineNum, currentPage, this.boxesStack.peek());
    }

    currentLine.addContentRecord(xf, yf);
  }

  protected void registerCurrentBox(PdfLine box) throws IOException {
    if (box != null && box.getNumContentRecords() > 0) {
      int lineNum = box.getTexLineNumber();
      if (!linesIndex.containsKey(lineNum)) {
        linesIndex.put(lineNum, new ArrayList<PdfLine>());
      }
      drawer.drawRectangle(box.getRectangle(), currentPage, Color.RED);
      drawer.drawText("" + lineNum, currentPage,
          box.getRectangle().getUpperLeft(), Color.RED);

      linesIndex.get(lineNum).add(box);
    }
  }

  protected float toPdfCoordinate(int point) {
    return (this.unit * point) / 65781.76f * (1000f / magnification);
  }

  // ===========================================================================

  /**
   * For a colon delimited string, this method returns the i-th field as int.
   */
  protected float getColonArgFloat(String line, int i, float defaultValue) {
    String field = getColonArg(line, i);
    if (field != null) {
      return Float.parseFloat(field);
    }
    return defaultValue;
  }

  /**
   * For a colon delimited string, this method returns the i-th field as string.
   */
  protected String getColonArg(String line, int i) {
    if (line != null) {
      String[] fields = line.split(":");
      if (i < fields.length) {
        return fields[i];
      }
    }
    return null;
  }

  // ===========================================================================

  public Map<Integer, List<PdfLine>> getPdfLinesIndex() {
    return this.linesIndex;
  }

  public List<PdfLine> getPdfLinesIndex(int texLineNum) {
    return this.linesIndex.get(texLineNum);
  }
}
