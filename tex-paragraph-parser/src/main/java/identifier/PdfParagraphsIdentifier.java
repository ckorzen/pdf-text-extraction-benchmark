package identifier;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.freiburg.iif.model.Rectangle;
import model.PdfParagraph;
import model.SyncTeXBoundingBox;
import model.TeXFile;
import model.TeXParagraph;

/**
 * Identifies the pdf paragraphs for tex paragraphs.
 * 
 * @author Claudius Korzen
 */
public class PdfParagraphsIdentifier {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;

  /**
   * The pdf line identifier.
   */
  protected PdfLineIdentifier lineIdentifier;

  /**
   * The page identifier.
   */
  protected PdfPageIdentifier pageIdentifier;

  /**
   * The set of already seen lines.
   */
  protected Set<SyncTeXBoundingBox> alreadySeenLines = new HashSet<>();

  /**
   * Creates a new pdf paragraph identifier.
   */
  public PdfParagraphsIdentifier(TeXFile texFile, List<String> texmfPaths)
    throws IOException {
    affirm(texFile != null);

    this.texFile = texFile;
    this.lineIdentifier = new PdfLineIdentifier(texFile, texmfPaths);
    this.pageIdentifier = new PdfPageIdentifier(texFile);
  }

  /**
   * Identifies the pdf paragraphs from given tex file.
   */
  public void identify() throws IOException {
    for (TeXParagraph para : this.texFile.getTeXParagraphs()) {
      List<SyncTeXBoundingBox> pdfLines = identifyPdfLines(para);
      List<PdfParagraph> pdfParagraphs = identifyPdfParagraphs(para, pdfLines);
      
      para.setPdfParagraphs(pdfParagraphs);
    }
  }

  public long sumRuntimesConstructor;
  public long sumRuntimesRun;
  public long sumRuntimesParse;
  public long sumCalls;

  /**
   * Identifies the pdf lines for the given tex paragraph.
   */
  protected List<SyncTeXBoundingBox> identifyPdfLines(TeXParagraph paragraph)
    throws IOException {
    LinkedList<SyncTeXBoundingBox> paraLines = new LinkedList<>();

    float sumLineHeights = 0;
    float numLineHeights = 0;

    for (int i : paragraph.getTexLineNumbers()) {
      List<SyncTeXBoundingBox> pdfLines = lineIdentifier.getBoundingBoxes(i);
      
      if (pdfLines != null) {
        // Only consider still unknown pdf lines. Exception: If a paragraph only
        // consists of already known lines, consider all lines of the paragraph.
        // Background: Title and authors are mostly added to pdf via \maketitle.
        // So, title and authors have the same pdf lines.
        List<SyncTeXBoundingBox> unknownPdfLines = new ArrayList<>();
        for (SyncTeXBoundingBox line : pdfLines) {          
          if (!alreadySeenLines.contains(line)) {
            unknownPdfLines.add(line);
            alreadySeenLines.add(line);
          }
        }

        pdfLines = unknownPdfLines;

        for (SyncTeXBoundingBox line : pdfLines) {
          // Clean up the lines a bit: Only consider still unknown lines and
          // let "climb up" the line to the "correct" position (with respect to
          // the reading order).
          int end = paraLines.size();
          ListIterator<SyncTeXBoundingBox> itr = paraLines.listIterator(end);

          while (itr.hasPrevious()) {
            int order = obtainReadingOrder(itr.previous(), line);
            // Check, if the lines are in correct order.
            if (order < 1) {
              itr.next();
              break;
            }
          }
          itr.add(line);

          sumLineHeights += line.getRectangle().getHeight();
          numLineHeights += 1;
        }
      }
    }

    if (numLineHeights > 0) {
      paragraph.setAverageLineHeight(sumLineHeights / numLineHeights);
    }

    return paraLines;
  }

  /**
   * Identifies the pdf paragraphs in the given pdf lines.
   */
  protected List<PdfParagraph> identifyPdfParagraphs(TeXParagraph para,
      List<SyncTeXBoundingBox> pdfLines) throws IOException {
    List<PdfParagraph> pdfParagraphs = new ArrayList<>();

    // Split the lines into paragraphs.
    SyncTeXBoundingBox prevLine = null;
    PdfParagraph pdfParagraph = null;
    for (SyncTeXBoundingBox line : pdfLines) {
      if (introducesNewPdfParagraph(para, prevLine, line)) {
        if (pdfParagraph != null && !pdfParagraph.isEmpty()) {
          pdfParagraphs.add(pdfParagraph);
        }
        pdfParagraph = new PdfParagraph();
      }
      pdfParagraph.addPdfLine(line);

      prevLine = line;
    }

    if (pdfParagraph != null && !pdfParagraph.isEmpty()) {
      pdfParagraphs.add(pdfParagraph);
    }

    return pdfParagraphs;
  }

  /**
   * Returns true, if the given line introduces a new pdf paragraph. TeX
   * paragraphs may be splitted in pdf file, e.g. by a column or page change.
   * This method checks if the both given lines belongs to two different
   * paragraphs.
   */
  protected boolean introducesNewPdfParagraph(TeXParagraph para,
      SyncTeXBoundingBox prevLine, SyncTeXBoundingBox line) {    
    if (prevLine == null && line == null) {
      return false;
    }

    if (prevLine == null) {
      return true;
    }

    if (line == null) {
      return false;
    }

    // The lines introduce a new paragraph if page numbers differ.
    if (prevLine.getPageNumber() != line.getPageNumber()) {
      return true;
    }
    
    // TODO: Theoretically, formulas, tables and figures could be splitted by
    // columns, too. But for now, don't allow column splits within formulas.
    if (para != null) {
      if ("formula".equals(para.getRole())) {
        return false;
      }

      if ("table".equals(para.getRole())) {
        return false;
      }

      if ("figure".equals(para.getRole())) {
        return false;
      }
    }

    // EXPERIMENTAL: Analyze the vertical distance between the lines.
    Rectangle prevRect = prevLine.getRectangle();
    Rectangle rect = line.getRectangle();

    if (prevRect.getHeight() > 2 && rect.getHeight() > 2) { // TODO
      // float height = prevRect.getHeight();
      // float prevMinX = prevRect.getMinX();
      // float prevMaxX = prevRect.getMaxX();
      float prevMinY = prevRect.getMinY();
      // float prevMaxY = prevRect.getMaxY();
      // float minX = rect.getMinX();
      // float maxX = rect.getMaxX();
      // float minY = rect.getMinY();
      float maxY = rect.getMaxY();
  
      // Compute the distance between the lines.
      float verticalDistance = Math.abs(prevMinY - maxY);
  
      // The lines introduce a new paragraph if verticalDistance is "too large".
      if (verticalDistance > 5 * prevRect.getHeight()) {
        return true;
      }
    }

    return false;
  }

  // ===========================================================================

  /**
   * Returns -1 if the first given line comes before the second given line.
   * Returns 0 if the both lines are equal. Returnd 1 if the second given line
   * comes before the first given line.
   */
  protected int obtainReadingOrder(SyncTeXBoundingBox line1, 
      SyncTeXBoundingBox line2) {
    return line1.getPageNumber() - line2.getPageNumber();
  }
}
