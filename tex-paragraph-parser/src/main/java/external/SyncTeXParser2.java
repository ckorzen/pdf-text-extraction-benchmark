package external;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import drawer.pdfbox.PdfBoxDrawer;
import model.PdfLine;
import model.TeXFile;

public class SyncTeXParser2 {
  /**
   * The tex file to process.
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
   * Index of all boxes per line.
   */
  protected TreeMap<Integer, List<PdfLine>> linesIndex;
  
  PdfBoxDrawer drawer;
  
  /**
   * The stack of boxes.
   */
  protected Stack<SyncTeXBox> boxesStack;
  
  public SyncTeXParser2(TeXFile texFile) throws IOException {
    this.texFile = texFile;
    this.linesIndex = new TreeMap<>();
    this.boxesStack = new Stack<>();
    this.magnification = 1000;
    this.xoffset = 0;
    this.yoffset = 0;
    this.unit = 1;
    drawer = new PdfBoxDrawer(new File("/home/korzen/Downloads/arxiv/cond-mat0001227/cond-mat0001227.tmp.pdf"));
  }
  
  /**
   * Parses the given synctex file.
   */
  public void parse() throws IOException {
    Path synctexPath = texFile.getSynctexPath();
    Charset cs = StandardCharsets.UTF_8;
    BufferedReader reader = Files.newBufferedReader(synctexPath, cs);
    
    parse(reader);
    
    reader.close();
    drawer.writeTo(new FileOutputStream(new File("/home/korzen/Downloads/arxiv/cond-mat0001227/cond-mat0001227.vis.pdf")));
  }
  
  /**
   * Parses the given reader.
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
           
      switch(first) {
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
          handleCurrentNode(line);
          break;
        case 'k':
          handleKernNode(line);
          break;
        case '$':
          handleMathNode(line);
          break;
        case 'v':
          handleVBoxNode(line);
          break;
        case 'h':
          handleHBoxNode(line);
          break;
        case 'g':
          handleGlueNode(line);
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
    handleBoxStart(line);
  }
  
  /**
   * Handles an end of vbox.
   */
  protected void handleVBoxEnd(String line) throws IOException {
    handleBoxEnd(line);
  }
  
  /**
   * Handles a start of hbox.
   */
  protected void handleHBoxStart(String line) throws IOException {
    handleBoxStart(line);
  }
  
  /**
   * Handles an end of hbox.
   */
  protected void handleHBoxEnd(String line) throws IOException {
    handleBoxEnd(line);
  }
  
  /**
   * Handles a current record.
   */
  protected void handleCurrentNode(String line) throws IOException {
    handleNodeStart(line);
  }
  
  /**
   * Handles a current record.
   * @throws IOException 
   */
  protected void handleKernNode(String line) throws IOException {
    // Do nothing.
  }
  
  /**
   * Handles a math record.
   */
  protected void handleMathNode(String line) throws IOException {
    // Do nothing.
  }
  
  /**
   * Handles a glue record.
   * @throws IOException 
   */
  protected void handleGlueNode(String line) throws IOException {
    handleNodeStart(line);
  }
  
  /**
   * Handles a vbox record.
   * @throws IOException 
   */
  protected void handleVBoxNode(String line) throws IOException {
    // Do nothing.
  }
  
  /**
   * Handles a hbox record.
   */
  protected void handleHBoxNode(String line) throws IOException {
    // Do nothing.
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
  
  protected void handleBoxStart(String line) {
    // <void vbox record> ::= "v" <link> ":" <point> ":" <size> <end of record>
    line = line.substring(1);
    
    String[] fields = line.split(":");
    String link = fields[0];
    String point = fields[1];
    String size = fields[2];
    
    // <link> ::= <tag> "," <line>( "," <column>)?
    String[] linkFields = link.split(",");
    int lineNum = Integer.parseInt(linkFields[1]);
    
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
    float yf = texFile.getPageBoundingBox(currentPage).getMaxY() - toPdfCoordinate(y);
    float wf = toPdfCoordinate(width);
    float hf = toPdfCoordinate(height);
    float df = toPdfCoordinate(depth);
    
    boxesStack.push(new SyncTeXBox(currentPage, lineNum, xf, yf, wf, hf, df));
  }
  
  protected void handleBoxEnd(String line) throws IOException {
    registerSyncTeXBox(boxesStack.pop());
  }
  
  protected void handleNodeStart(String line) {
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
    float yf = texFile.getPageBoundingBox(currentPage).getMaxY() - toPdfCoordinate(y);
    
    SyncTeXNode node = new SyncTeXNode(currentPage, lineNum, xf, yf);
    boxesStack.peek().addNode(node);
  }
  
  protected void registerSyncTeXBox(SyncTeXBox box) throws IOException {
    box.reduce();
    
    if (!box.getNodes().isEmpty()) {
      List<PdfLine> lines = box.toLines();
      
      for (PdfLine line : lines) {
        drawer.drawRectangle(line.getPdfBoundingBox(), line.getPdfPageNumber(), Color.RED);
        drawer.drawText("" + line.getTexLineNumber(), line.getPdfPageNumber(), line.getRectangle().getUpperLeft());
        
        if (!linesIndex.containsKey(line.getTexLineNumber())) {
          linesIndex.put(line.getTexLineNumber(), new ArrayList<PdfLine>());
        }
        linesIndex.get(line.getTexLineNumber()).add(line);
      }
    }
  }
  
  protected float toPdfCoordinate(int point) {
    return (this.unit * point) / 65781.76f * (1000f / magnification);
  }
  
  public Map<Integer, List<PdfLine>> getPdfLinesIndex() {
    return this.linesIndex;
  }
  
  public List<PdfLine> getPdfLinesIndex(int lineNum) {
    Entry<Integer, List<PdfLine>> entry = this.linesIndex.floorEntry(lineNum);
    return entry != null ? entry.getValue() : null;
  }
}
