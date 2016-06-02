package paraboxes;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import model.PdfLine;
import model.TeXFile;
import model.TexParagraph;
import util.PdfLaTeX;
import util.SyncTeX;

/**
 * Class to parse synctex file and to get the coordinates of a given line in a
 * tex file.
 * 
 * @author Claudius Korzen
 *
 */
public class LineBoundingBoxesIdentifier {
  protected TeXFile texFile;
  protected Path texPath;
  protected List<Rectangle> pageBoundingBoxes;

  protected Map<Integer, List<PdfLine>> pdfLines = new HashMap<>();
  protected Map<PdfLine, PdfLine> pdfLinesSet = new HashMap<>();

  protected static final String ADDENDUM = "\\nolinebreak\\hspace{-5pt}i";
  // protected static final String ADDENDUM = " x";

  /**
   * The default constructor.
   */
  public LineBoundingBoxesIdentifier(TeXFile texFile) {
    affirm(texFile != null, "No tex file given.");

    // Enrich the tex file.
    texFile.setEnrichedTexPath(enrichTexFile(texFile));

    // Compile the enriched version of tex file.
    texFile.setPdfPath(compileTexFile(texFile));

    // Load the bounding boxes of pages.
    this.pageBoundingBoxes = loadPageBoundingBoxes(texFile);
    affirm(this.pageBoundingBoxes != null, "No page bounding boxes given.");
    affirm(this.pageBoundingBoxes.size() > 1, "No page bounding boxes given.");

    this.texFile = texFile;
  }

  /**
   * Enriches the given tex file to get better results on identification of
   * bounding boxes.
   * 
   * @throws IOException
   */
  protected Path enrichTexFile(TeXFile texFile) {
    affirm(texFile != null, "No tex file given");

    List<String> texLines = readTexLines(texFile.getPath());
    List<TexParagraph> paragraphs = texFile.getTeXParagraphs();

    for (int i = 0; i < paragraphs.size(); i++) {
      TexParagraph prevParagraph = i > 0 ? paragraphs.get(i - 1) : null;

      if (prevParagraph != null) {
        int lineNum = prevParagraph.getTexEndLine();

        affirm(lineNum > 0, "Line number is too small.");
        affirm(lineNum < texLines.size(), "Line number is too large.");

        String prevParagraphEndLine = texLines.get(lineNum);

        if (!prevParagraphEndLine.trim().startsWith("\\")
            && !prevParagraphEndLine.trim().endsWith("\\")
            && !prevParagraphEndLine.trim().endsWith("{")
            && !prevParagraphEndLine.trim().endsWith("}")) { // TODO
          prevParagraphEndLine += ADDENDUM;
          texLines.set(lineNum, prevParagraphEndLine);
        }
      }
    }

    for (int i = 0; i < texLines.size(); i++) {
      String line = texLines.get(i);

      if (line != null && line.startsWith("\\label")) {
        texLines.set(i, "%" + line);
      }
    }

    // Write the enriched tex file.
    Path enriched = getEnrichedTexFile(texFile);
    try {
      BufferedWriter writer =
          Files.newBufferedWriter(enriched, StandardCharsets.UTF_8);
      try {
        for (int i = 1; i < texLines.size(); i++) { // 1-based.
          writer.write(texLines.get(i));
          writer.newLine();
        }
      } catch (IOException e) {
        throw new IllegalStateException("Couldn't write enriched tex file.");
      } finally {
        writer.close();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't write enriched tex file.");
    }

    affirm(Files.isRegularFile(enriched), "Enriched tex file doesn't exist.");

    return enriched;
  }

  /**
   * Reads the lines of tex file into list.
   */
  protected List<String> readTexLines(Path texFile) {
    affirm(texFile != null, "No tex file given");
    affirm(Files.isRegularFile(texFile), "Given tex file doesn't exist.");

    List<String> texLines = new ArrayList<>();
    try {
      BufferedReader reader =
          Files.newBufferedReader(texFile, StandardCharsets.UTF_8);

      try {
        // Add dummy to have 1-based indices.
        texLines.add(null);

        String line;
        while ((line = reader.readLine()) != null) {
          texLines.add(line);
        }
      } finally {
        reader.close();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Couldn't read the tex file.", e);
    }

    return texLines;
  }

  /**
   * The coordinates provided by synctex are relative to the upper left, but we
   * need coordinates relative to the lower left. Hence, we have to adapt the
   * coordinates. For that we need to know the dimensions of each page.
   */
  protected List<Rectangle> loadPageBoundingBoxes(TeXFile texFile) {
    List<Rectangle> pageBoundingBoxes = new ArrayList<>();

    try {
      Path pdfPath = texFile.getPdfPath();
      PDDocument pdDocument = PDDocument.load(pdfPath.toFile());
      PDPageTree pages = pdDocument.getDocumentCatalog().getPages();
      pageBoundingBoxes = new ArrayList<>();
      pageBoundingBoxes.add(null); // add dummy because pages are 1-based.

      // Compute the bounding boxes.
      for (PDPage page : pages) {
        Rectangle boundingBox = new SimpleRectangle();

        PDRectangle box = page.getCropBox();
        if (box == null) {
          box = page.getMediaBox();
        }
        if (box != null) {
          boundingBox.setMinX(box.getLowerLeftX());
          boundingBox.setMinY(box.getLowerLeftY());
          boundingBox.setMaxX(box.getUpperRightX());
          boundingBox.setMaxY(box.getUpperRightY());
        }

        pageBoundingBoxes.add(boundingBox);
      }
      pdDocument.close();
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't load the pdf file.", e);
    }

    return pageBoundingBoxes;
  }

  // ===========================================================================

  /**
   * Returns the bounding box of given page.
   */
  public Rectangle getBoundingBoxOfPage(int pageNum) {
    return this.pageBoundingBoxes.get(pageNum);
  }

  /**
   * Parses the synctex output for given line to get the coordinates of the
   * line.
   */
  public List<PdfLine> getBoundingBoxesOfLine(int lineNum,
      int columnNumber) throws IOException, TimeoutException {

    Path texPath = this.texFile.getEnrichedTexPath();
    Path pdfPath = this.texFile.getPdfPath();
    SyncTeX synctex = new SyncTeX(texPath, pdfPath);
    int status = synctex.run(lineNum, columnNumber);

    affirm(status == 0, "Error on synctex call: " + synctex.getErrorString());

    List<PdfLine> pairs = parseStream(lineNum, synctex.getStream());

    synctex.close();

    return pairs;
  }

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

    int currentPageNumber = -1;
    float currentMinX = -1;
    float currentMaxX = -1;
    float currentMinY = -1;
    float currentMaxY = -1;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("Page:")) {
        currentPageNumber = Integer.parseInt(line.substring(5));
      }

      if (line.startsWith("after:")) { // The last line of an entry.
        if (currentMinX < Float.MAX_VALUE && currentMinY < Float.MAX_VALUE
            && currentMaxX > Float.MIN_VALUE && currentMaxY > Float.MIN_VALUE) {
          Rectangle boundingBox = new SimpleRectangle(currentMinX, currentMinY,
              currentMaxX, currentMaxY);
          pairs.add(new PdfLine(lineNumber, currentPageNumber, boundingBox));
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
        currentMinY = getBoundingBoxOfPage(currentPageNumber).getMaxY() - y;
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
      pairs.add(new PdfLine(lineNumber, currentPageNumber, boundingBox));
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

  // ===========================================================================
  // Compile methods.

  /**
   * Compiles the given tex file and makes sure that the related pdf- and
   * synctex-file exist.
   */
  protected Path compileTexFile(TeXFile texFile) {
    try {
      Path texPath = texFile.getEnrichedTexPath();
      Path outputDir = getOutputDirectory(texFile);
      new PdfLaTeX(texPath, true, outputDir).run(true);
    } catch (Exception e) {
      throw new IllegalStateException("Couldn't compile the tex file.");
    }

    Path pdfFile = getRelatedPdfFile(texFile);
    affirm(pdfFile != null, "No PDF file produced.");
    affirm(Files.isRegularFile(pdfFile), "No PDF file produced.");

    Path syncTexFile = getRelatedSyncTexFile(texFile);
    affirm(syncTexFile != null, "No syncTeX file produced.");
    affirm(Files.isRegularFile(syncTexFile), "No syncTeX file produced.");

    return pdfFile;
  }

  /**
   * Returns the path to the enriched pdf file.
   */
  protected Path getEnrichedTexFile(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".enriched.tex";
      Path outputDir = getOutputDirectory(texFile);
      Path enrichedFile = Paths.get(outputDir.toString(), filename);

      return enrichedFile;
    }
    return null;
  }

  /**
   * Returns the pdf file related to the given tex file or null if it doesn't
   * exist.
   */
  protected Path getRelatedPdfFile(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".enriched.pdf";
      Path outputDir = getOutputDirectory(texFile);
      Path pdfFile = Paths.get(outputDir.toString(), filename);

      return pdfFile;
    }
    return null;
  }

  /**
   * Returns the pdf file related to the given tex file or null if it doesn't
   * exist.
   */
  protected Path getRelatedSyncTexFile(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".enriched.synctex";
      Path outputDir = getOutputDirectory(texFile);
      Path syncTexFile = Paths.get(outputDir.toString(), filename);

      return syncTexFile;
    }
    return null;
  }

  /**
   * Returns the basepath of the given tex file (the absolute path without file
   * extension).
   */
  protected String getBaseName(TeXFile texFile) {
    String filename = texFile.getPath().getFileName().toString();
    if (filename != null) {
      int indexOfLastDot = filename.lastIndexOf(".");
      if (indexOfLastDot > 0) {
        return filename.substring(0, indexOfLastDot);
      }
    }
    return null;
  }

  /**
   * Returns the output directory for the files to produce.
   */
  protected Path getOutputDirectory(TeXFile texFile) {
    return texFile.getPath().getParent().toAbsolutePath();
  }
}
