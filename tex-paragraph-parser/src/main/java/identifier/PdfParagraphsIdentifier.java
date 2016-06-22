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
 * Class to identify the positions of paragraphs from tex files in pdf files.
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
   * Identifies the positions of paragraphs of given tex file.
   */
  public void identify() throws IOException {
    // Iterate through the (already obtained!) paragraphs.
    for (TeXParagraph para : this.texFile.getTeXParagraphs()) {
      // Identify the position of lines belonging to the current paragraph.
      List<SyncTeXBoundingBox> pdfLines = identifyPdfLines(para);
      // Compose the bounding boxes of paragraphs from given lines.
      List<PdfParagraph> pdfParagraphs = identifyPdfParagraphs(para, pdfLines);
      
      para.setPdfParagraphs(pdfParagraphs);
    }
  }

  /**
   * Identifies the positions of lines belonging to the given paragraph.
   */
  protected List<SyncTeXBoundingBox> identifyPdfLines(TeXParagraph paragraph)
    throws IOException {
    LinkedList<SyncTeXBoundingBox> paraLines = new LinkedList<>();
      
    // Obtain the bounding boxes of each line in the given paragraph.
    for (int i : paragraph.getTexLineNumbers()) {
      List<SyncTeXBoundingBox> pdfLines = lineIdentifier.getBoundingBoxes(i);
      
      if (pdfLines != null) {
        // Only consider still unknown pdf lines.
        List<SyncTeXBoundingBox> unknownPdfLines = new ArrayList<>();
        for (SyncTeXBoundingBox line : pdfLines) {          
          if (!alreadySeenLines.contains(line)) {
            unknownPdfLines.add(line);
            alreadySeenLines.add(line);
          }
        }

        // TODO: Do we still need to obtain the "reading order"?
        for (SyncTeXBoundingBox line : unknownPdfLines) {
          // Clean up the lines a bit: let "climb up" the line to the "correct" 
          // position (with respect to the reading order).
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
        }
      }
    }

    return paraLines;
  }

  /**
   * Composes the bounding boxes of paragraph from given lines.
   */
  protected List<PdfParagraph> identifyPdfParagraphs(TeXParagraph para,
      List<SyncTeXBoundingBox> pdfLines) throws IOException {
    List<PdfParagraph> pdfParagraphs = new ArrayList<>();

    // Split the lines into paragraphs.
    SyncTeXBoundingBox prevLine = null;
    PdfParagraph pdfParagraph = null;
    for (SyncTeXBoundingBox line : pdfLines) {
      // Decide if the line introduces a new bounding box (e.g. because it
      // is located in a different column.
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
    
    float mostCommonLineHeight = lineIdentifier.getMostCommonLineHeight();
    
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
    if (verticalDistance > 4 * mostCommonLineHeight) {
      return true;
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
