package synctex;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import de.freiburg.iif.counter.FloatCounter;
import de.freiburg.iif.math.MathUtils;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import identifier.PdfPageIdentifier;
import model.SyncTeXBoundingBox;

/**
 * A simple parser for SyncTeX files that allows forward searches. See
 * <a href="http://manpages.ubuntu.com/manpages/xenial/en/man5/synctex.5.html"/>
 * for more details about the sytnax of SyncTeX and
 * <a href="http://itexmac.sourceforge.net/SyncTeX.html" /> for more details
 * about the official SyncTeX parser.
 * 
 * @author Claudius Korzen
 */
public class SyncTeXParser {
  /**
   * The synctex path to process.
   */
  protected Path syncTexPath;

  /**
   * The page identifier to get bounding boces of pages.
   */
  protected PdfPageIdentifier pageIdentifier;

  /**
   * The magnification from preamble.
   */
  protected float magnification;

  /**
   * The x offset from preamble.
   */
  protected float xoffset;

  /**
   * The y offset from preamble.
   */
  protected float yoffset;

  /**
   * The unit from preamble.
   */
  protected float unit;

  /**
   * The current page.
   */
  protected int pageNum;

  /**
   * The stack of hboxes.
   */
  protected Stack<SyncTeXBoundingBox> hboxStack;

  /**
   * Map that stores the bounding boxes of records within hboxes per line num.
   */
  protected Map<Integer, List<SyncTeXBoundingBox>> recordBoundingBoxes;

  /**
   * Map that stores the bounding boxes of merged records per line.
   */
  protected TreeMap<Integer, List<SyncTeXBoundingBox>> mergedBoundingBoxes;

  /**
   * The heights counter.
   */
  protected FloatCounter heightsCounter;
  
  /**
   * Flag to indicate whether the tex file was laready parsed.
   */
  protected boolean parsed;
  
  /**
   * Creates a new SyncTeXParser for the given tex file.
   */
  public SyncTeXParser(Path synctexPath) throws IOException {
    this(synctexPath, null);
  }
  
  /**
   * Creates a new SyncTeXParser for the given tex file.
   */
  public SyncTeXParser(Path synctexPath, PdfPageIdentifier pageIdentifier) {
    this.syncTexPath = synctexPath;
    this.pageIdentifier = pageIdentifier;
    this.hboxStack = new Stack<>();
    this.heightsCounter = new FloatCounter();
    this.magnification = 1000;
    this.xoffset = 0;
    this.yoffset = 0;
    this.unit = 1;
  }
  
  /**
   * Returns the bounding boxes which belongs to the given tex line. If there
   * are no bounding boxes for given line number this method returns the
   * bounding boxes for the smallest larger line number.
   */
  public List<SyncTeXBoundingBox> getLineBoundingBoxes(int lineNumber)
    throws IOException {

    if (!parsed) {
      parse();
    }

    Integer ceilingKey = mergedBoundingBoxes.ceilingKey(lineNumber);
        
    return ceilingKey != null ? mergedBoundingBoxes.get(ceilingKey) : null;
  }
  
  /**
   * Returns the most common line height.
   */
  public float getMostCommonLineHeight() {
    affirm(parsed, "Synctex wasn't parsed yet");
    return this.heightsCounter.getMostFrequentFloat();
  }

  /**
   * Returns the average line height.
   */
  public float getAverageLineHeight() {
    affirm(parsed, "Synctex wasn't parsed yet");
    return this.heightsCounter.getAverageValue();
  }

  // ===========================================================================

  /**
   * Parses the given synctex file.
   */
  protected void parse() throws IOException {
    BufferedReader reader = null;

    try {
      reader = Files.newBufferedReader(syncTexPath, StandardCharsets.UTF_8);
      parse(reader);
    } catch (Exception e) {
      throw new IOException(e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Parses a synctex file given by a reader.
   */
  protected void parse(BufferedReader reader) throws IOException {
    parsePreamble(reader);
    parseContent(reader);

    this.mergedBoundingBoxes = mergeXRecords(recordBoundingBoxes);
    this.parsed = true;
  }

  /**
   * Parses the preamble of synctex file. The preamble has form:
   * 
   * SyncTeX Version:1 Input:1:/foo/bar/cond-mat0001227.tmp.tex
   * Input:2:/usr/share/texlive/texmf-dist/tex/latex/base/latex209.def
   * Input:3:/usr/share/texlive/texmf-dist/tex/latex/base/tracefnt.sty
   * Output:pdf Magnification:1000 Unit:1 X Offset:0 Y Offset:0
   * 
   * This method parses the magnification, x offset, y offset and unit.
   */
  protected void parsePreamble(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      float value = Float.NaN;
      String[] fields = line.split(":");
      if (fields.length > 1) {
        value = MathUtils.parseFloat(fields[1], value);
      }

      if (line.startsWith("Magnification")) {
        this.magnification = value != Float.NaN ? value : this.magnification;
      } else if (line.startsWith("X Offset")) {
        this.xoffset = value != Float.NaN ? value : this.xoffset;
      } else if (line.startsWith("Y Offset")) {
        this.yoffset = value != Float.NaN ? value : this.yoffset;
      } else if (line.startsWith("Unit")) {
        this.unit = value != Float.NaN ? value : this.unit;
      } else if (line.startsWith("Content")) {
        break;
      }
    }
  }

  /**
   * Parses the content from given reader. The content has form:
   * 
   * Content: !629 {1 [1,101:4736286,50149404:29089004,45413118,0
   * [1,101:6787433,50149404:27037857,48210137,0
   * [1,101:6787433,3431010:27037857,1491743,0
   * h1,101:6787433,3431010:27037857,0,0 ]
   * [1,101:6787433,48183324:27037857,42887635,0 h1,45:6787433,6082121:0,0,0
   * (1,45:6787433,9686576:27037857,943900,264379 k1,45:8764568,9686576:1977135
   * (1,45:8764568,9686576:0,0,0 g1,45:8764568,9686576 g1,45:8764568,9686576
   * g1,45:8379548,9686576 x1,45:8379548,9686576:0,0,0 g1,45:8764568,9686576 )
   * 
   * where the line "!629" introduces a byte offset. the line "{1" introduces a
   * new page (with number '1'). the line
   * "[1,101:4736286,50149404:29089004,45413118,0" introduces a new vbox by file
   * #1 in line '101' at position x=4736286, y=50149404 and dimensions
   * width=29089004, height=45413118 and depth=0. the line
   * "h1,101:6787433,3431010:27037857,0,0" introduces a *void* (i.e. empty) hbox
   * in surrounding vbox. the line
   * "(1,45:6787433,9686576:27037857,943900,264379" introduces a new hbox (with
   * same syntax as for vbox above). the line "k1,45:8764568,9686576:1977135"
   * introduces a kern record in surrounding hbox. the line
   * "g1,45:8764568,9686576" introduces a glue record. the line
   * "x1,45:8764568,9686576" introduces a "current record".
   */
  protected void parseContent(BufferedReader reader) throws IOException {
    this.recordBoundingBoxes = new HashMap<>();
    this.mergedBoundingBoxes = new TreeMap<>();

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
   * Handles a page start (type "{").
   */
  protected void handlePageStart(String line) {
    // Obtain the current page number.
    this.pageNum = Integer.parseInt(line.substring(1));
  }

  /**
   * Handles a page end (type "}").
   */
  protected void handlePageEnd(String line) throws IOException {
    // Do nothing.
  }

  /**
   * Handles a start of vbox (type "[").
   */
  protected void handleVBoxStart(String line) {
    // Do nothing (we are only interested in hboxes for now).
  }

  /**
   * Handles an end of vbox (type "]").
   */
  protected void handleVBoxEnd(String line) {
    // Do nothing.
  }

  /**
   * Handles a start of hbox (horizontal box, type "("). Roughly, it represents
   * the bounding box of a text line in pdf.
   */
  protected void handleHBoxStart(String line) {
    // Parse the line to get position and dimensions of the box and add the box
    // to current hbox.

    // <void vbox record> ::= "v" <link> ":" <point> ":" <size> <end>
    char type = line.charAt(0);
    line = line.substring(1);

    String[] fields = line.split(":");

    // <link> ::= <tag> "," <line>( "," <column>)?
    String link = fields[0];
    String[] linkFields = link.split(",");
    int lineNum = Integer.parseInt(linkFields[1]);

    // <point> ::= <integer> "," <integer>
    String point = fields[1];
    String[] pointFields = point.split(",");
    int x = Integer.parseInt(pointFields[0]);
    int y = Integer.parseInt(pointFields[1]);

    // <size> ::= <Width> "," <Height> "," <Depth>
    String size = fields[2];
    String[] sizeFields = size.split(",");
    int width = Integer.parseInt(sizeFields[0]);
    int height = Integer.parseInt(sizeFields[1]);

    Rectangle rect = toRectangle(x, y, width, height, true);
    
    hboxStack.push(new SyncTeXBoundingBox(type, pageNum, lineNum, rect));
  }

  /**
   * Handles an end of hbox (type ")").
   */
  protected void handleHBoxEnd(String line) {    
    // Prepare the bounding boxes of records of type "x" in the hbox.
    List<SyncTeXBoundingBox> boxes = prepareXRecords(hboxStack.pop());

    // Register each bounding box by its tex line number.
    for (SyncTeXBoundingBox box : boxes) {
      int lineNum = box.getTexLineNumber();
      if (!recordBoundingBoxes.containsKey(lineNum)) {
        List<SyncTeXBoundingBox> emptyList = new ArrayList<>();
        recordBoundingBoxes.put(lineNum, emptyList);
      }
      recordBoundingBoxes.get(lineNum).add(box);
    }
  }

  /**
   * Handles a current record (type "x").
   */
  protected void handleCurrentRecord(String line) {
    handleRecordStart(line);
  }

  /**
   * Handles a kern record.
   */
  protected void handleKernRecord(String line) {
    handleRecordStart(line);
  }

  /**
   * Handles a math record.
   */
  protected void handleMathRecord(String line) {
    handleRecordStart(line);
  }

  /**
   * Handles a glue record.
   */
  protected void handleGlueRecord(String line) {
    handleRecordStart(line);
  }

  /**
   * Handles a vbox record.
   */
  protected void handleVBoxRecord(String line) {
    handleRecordStart(line);
  }

  /**
   * Handles a hbox record.
   */
  protected void handleHBoxRecord(String line) {
//    handleRecordStart(line);
  }

  /**
   * Handles the start of a new record.
   */
  protected void handleRecordStart(String line) {
    // Parse the line to get the position and dimension of the record.
    // Mainly there are three different kind of syntaxes for records:
    // <current record> ::= "x" <link> ":" <point> <end>
    // <kern record> ::= "k" <link> ":" <point> ":" <Width> <end>
    // <void vbox record> ::= "v" <link> ":" <point> ":" <size> <end>
    // For now, we are only interested in the line number and the point.
    
    if (hboxStack.isEmpty()) {
      // There is no active hbox. Abort.
      return;
    }
    
    char type = line.charAt(0);
    line = line.substring(1);

    int lineNum = 0;
    int x = 0;
    int y = 0;

    String[] fields = line.split(":");

    if (fields.length > 0) {
      // Parse the line number.
      // <link> ::= <tag> "," <line>( "," <column>)?
      String link = fields[0];
      String[] linkFields = link.split(",");
      lineNum = Integer.parseInt(linkFields[1]);
    }

    if (fields.length > 1) {
      // Parse the point.
      // <point> ::= <integer> "," <integer>
      String point = fields[1];
      String[] pointFields = point.split(",");
      x = Integer.parseInt(pointFields[0]);
      y = Integer.parseInt(pointFields[1]);
    }
    
    // Add the record to the current hbox.
    Rectangle rect = toRectangle(x, y, 0, 0, true);
    hboxStack.peek().add(new SyncTeXBoundingBox(type, pageNum, lineNum, rect));
  }

  // ===========================================================================

  /**
   * Prepares the bounding box of records of type "x". Per default, synctex
   * doesn't provide dimension for such records. This methods derives the
   * bounding boxes from the dimension of surrounding hbox and the other records
   * within the hbox.
   */
  protected List<SyncTeXBoundingBox> prepareXRecords(SyncTeXBoundingBox box) {
    List<SyncTeXBoundingBox> boxes = new ArrayList<>();
    if (box != null) {
      // Iterate through the records of the given hbox and kkep track of the
      // current x position to be able to obtain minX and maxX values.
      float currentXPosition = box.getRectangle().getMinX();

      for (SyncTeXBoundingBox record : box.getRecords()) {
        if (record.getType() == 'x') { // TODO
          Rectangle rect = record.getRectangle();

          // The bounding box of record defines an x and an y value which
          // represents the lower right of the bounding box. Hence,
          // maxX = rect.minX and minY = rect.minY.
          rect.setMaxX(rect.getMinX());
          rect.setMinY(rect.getMinY());

          // minX follows from 'currentXPosition', maxY follows from box.maxY.
          rect.setMinX(currentXPosition);
          rect.setMaxY(box.getRectangle().getMaxY());

          // SyncteX holds some inconsistencies, try to fix them.
          fixSyncTeXData(boxes, record);

          if (rect.getHeight() > 0 && rect.getWidth() > 0) {
            boxes.add(record);
          }
        }
        currentXPosition = record.getRectangle().getMaxX();
      }
    }
    return boxes;
  }

  /**
   * Given the bounding boxes of records per line, this method merges all
   * consecutive bounding boxes with same tex line number and same minY value.
   */
  protected TreeMap<Integer, List<SyncTeXBoundingBox>> mergeXRecords(
      Map<Integer, List<SyncTeXBoundingBox>> boxesMap) {
    TreeMap<Integer, List<SyncTeXBoundingBox>> mergedBoxesMap = new TreeMap<>();

    // Iterate through each list in the map.
    for (Entry<Integer, List<SyncTeXBoundingBox>> e : boxesMap.entrySet()) {
      int texLineNum = e.getKey();
      List<SyncTeXBoundingBox> boundingBoxes = e.getValue();
      List<SyncTeXBoundingBox> mergedBoundingBoxes = new ArrayList<>();

      if (boundingBoxes != null && !boundingBoxes.isEmpty()) {
        SyncTeXBoundingBox mergedBoundingBox = boundingBoxes.get(0);

        // Iterate through the records of a tex line.
        for (int i = 1; i < boundingBoxes.size(); i++) {
          SyncTeXBoundingBox boundingBox = boundingBoxes.get(i);

          // float mergedBoxMinY = mergedBoundingBox.getRectangle().getMinY();
          // float boxMinY = boundingBox.getRectangle().getMinY();
          float mergedBoxTexLineNumber = mergedBoundingBox.getTexLineNumber();
          float boxTexLineNumber = boundingBox.getTexLineNumber();

          // boolean hasSameMinY = boxMinY == mergedBoxMinY;
          boolean hasSameLineNum = mergedBoxTexLineNumber == boxTexLineNumber;
          boolean overlapsVertically = mergedBoundingBox.getRectangle()
              .overlapsVertically(boundingBox.getRectangle());
          
          // Don't merge the current bounding box if minY or the line number
          // isn't equal.
          if (!hasSameLineNum || !overlapsVertically) {
            
            mergedBoundingBoxes.add(mergedBoundingBox);
            mergedBoundingBox = boundingBox;
          } else {
            mergedBoundingBox.extend(boundingBox);
          }
        }
        mergedBoundingBoxes.add(mergedBoundingBox);
      }

      // Register the lines in heightsCounter.
      for (SyncTeXBoundingBox box : mergedBoundingBoxes) {
        heightsCounter.add(MathUtils.round(box.getRectangle().getHeight(), 1));
      }
      
      mergedBoxesMap.put(texLineNum, mergedBoundingBoxes);
    }
    return mergedBoxesMap;
  }

  // ===========================================================================

  /**
   * Tries to fix some inconsistencies in SyncTeXData.
   */
  protected void fixSyncTeXData(List<SyncTeXBoundingBox> previousRecords,
      SyncTeXBoundingBox currentRecord) {
    // Iterate through the previous records from behind.
    int num = previousRecords.size();
    ListIterator<SyncTeXBoundingBox> itr = previousRecords.listIterator(num);

    while (itr.hasPrevious()) {
      SyncTeXBoundingBox previousRecord = itr.previous();

      float prevMinY = previousRecord.getRectangle().getMinY();
      float minY = currentRecord.getRectangle().getMinY();
      boolean hasSameMinY = prevMinY == minY;

      int prevTexLineNumber = previousRecord.getTexLineNumber();
      int texLineNumber = currentRecord.getTexLineNumber();
      boolean hasLargerTexLineNumber = prevTexLineNumber > texLineNumber;

      float prevMaxX = previousRecord.getRectangle().getMaxX();
      float minX = currentRecord.getRectangle().getMinX();
      boolean overlapsHorizontally = prevMaxX > minX;

      boolean affected = false;

      // Make sure, that all previous records with same minY value as the minY
      // value of current record have a tex line number which is smaller or
      // equal to the tex line number of the current record.
      if (hasSameMinY && hasLargerTexLineNumber) {
        previousRecord.setTexLineNumber(texLineNumber);
        affected = true;
      }

      // Make sure, that the current record doesn't overlap with the previous
      // record.
      if (overlapsHorizontally) {
        previousRecord.getRectangle().setMaxX(minX);
        affected = true;
      }

      // Abort if the previous record wasn't affected.
      if (!affected) {
        break;
      }
    }
  }

  // ===========================================================================

  /**
   * Computes a rectangle for given values in pdf corrdinates. If
   * 'toLowerLeftBase' is true, the coordinates are computed related to the
   * lower left of the page.
   */
  protected Rectangle toRectangle(int x, int y, int width, int height,
      boolean toLowerLeftBase) {
    float xf = toPdfCoordinate(x);
    float yf = toPdfCoordinate(y);
    if (toLowerLeftBase && pageIdentifier != null) {
      // The given values are based on upper left. Invert the y values to get
      // values based on lower left.
      Rectangle pageBoundingBox = pageIdentifier.getBoundingBox(pageNum);
      yf = pageBoundingBox.getMaxY() - yf;
    }
    float wf = toPdfCoordinate(width);
    float hf = toPdfCoordinate(height);

    return new SimpleRectangle(xf, yf, xf + wf, yf + hf);
  }

  /**
   * Computes the pdf coordinate for given point.
   */
  protected float toPdfCoordinate(int point) {
    return (this.unit * point) / 65781.76f * (1000f / magnification);
  }
}
