package parser;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import external.SyncTeX;
import model.PdfLine;
import model.TeXFile;

/**
 * Parser to parse the synctex output.
 * 
 * @author Claudius Korzen
 */
public class PdfLinesParser {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
      
  /**
   * Creates a new pdf line parser.
   */
  public PdfLinesParser(TeXFile texFile) {
    this.texFile = texFile;
  }
      
  /**
   * Parses the synctex output for given line number and column number.
   */
  public List<PdfLine> parse(int lineNum, int columnNum) throws IOException {
    Path texPath = this.texFile.getTmpPath();
    Path pdfPath = this.texFile.getPdfPath();
    SyncTeX synctex = new SyncTeX(texPath, pdfPath);
        
    try {
      synctex.run(lineNum, columnNum);
    } catch (TimeoutException e) {
      throw new IOException(e);
    }
    List<PdfLine> pairs = parseStream(lineNum, synctex.getStream());
    synctex.close();
  
    return pairs;
  }
  
  /**
   * Parses the synctex stream.
   */
  protected List<PdfLine> parseStream(int lineNumber,
      InputStream is) throws IOException {
    affirm(lineNumber > 0, "The line number is too small.");

    List<PdfLine> pairs = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    String line;
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = Float.MIN_VALUE;
    float maxY = Float.MIN_VALUE;

    int pageNum = -1;
    float currentMinX = -1;
    float currentMaxX = -1;
    float currentMinY = -1;
    float currentMaxY = -1;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("Page:")) {
        pageNum = Integer.parseInt(line.substring(5));
      }

      if (line.startsWith("after:")) { // The last line of an entry.
        if (currentMinX < Float.MAX_VALUE && currentMinY < Float.MAX_VALUE
            && currentMaxX > Float.MIN_VALUE && currentMaxY > Float.MIN_VALUE) {
          Rectangle boundingBox = new SimpleRectangle(currentMinX, currentMinY,
              currentMaxX, currentMaxY);
          pairs.add(new PdfLine(lineNumber, pageNum, boundingBox));
        }
      }

      if (line.startsWith("h:")) {
        currentMinX = Float.parseFloat(line.substring(2));
        if (currentMinX < minX) {
          minX = currentMinX;
        }
      }

      if (line.startsWith("v:")) {
        float y = Float.parseFloat(line.substring(2));
        currentMinY = texFile.getPageBoundingBox(pageNum).getMaxY() - y;
        if (currentMinY < minY) {
          minY = currentMinY;
        }
      }

      if (line.startsWith("W:")) {
        float width = Float.parseFloat(line.substring(2));
        currentMaxX = currentMinX + width;
        if (currentMaxX > maxX) {
          maxX = currentMaxX;
        }
      }

      if (line.startsWith("H:")) {
        float height = Float.parseFloat(line.substring(2));
        currentMaxY = currentMinY + height;
        if (currentMaxY > maxY) {
          maxY = currentMaxY;
        }
      }
    }

    if (currentMinX < Float.MAX_VALUE && currentMinY < Float.MAX_VALUE
        && currentMaxX > Float.MIN_VALUE && currentMaxY > Float.MIN_VALUE) {
      Rectangle boundingBox = new SimpleRectangle(currentMinX, currentMinY,
          currentMaxX, currentMaxY);
      pairs.add(new PdfLine(lineNumber, pageNum, boundingBox));
    }

    reader.close();

    // Collections.sort(pairs);
    //
    // if (!pairs.isEmpty() && !this.texLines.get(lineNumber).isEmpty() &&
    // this.texLines.get(lineNumber).split("[ \\\\{\\}\\[\\]]").length == 1) {
    // List<PdfLine> lines = new ArrayList<>();
    // lines.add(pairs.get(0));
    // return lines;
    // }

    return pairs;
  }
}
