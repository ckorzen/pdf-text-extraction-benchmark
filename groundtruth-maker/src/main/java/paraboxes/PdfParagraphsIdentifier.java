package paraboxes;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeoutException;

import de.freiburg.iif.model.Rectangle;
import model.PdfLine;
import model.PdfParagraph;
import model.TeXFile;
import model.TexParagraph;

/**
 * Identifies the bounding boxes of paragraphs from tex files.
 * 
 * @author Claudius Korzen
 */
public class PdfParagraphsIdentifier {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;

  /**
   * The line bounding box identifier.
   */
  protected LineBoundingBoxesIdentifier lbi;

  /**
   * Creates a new identifier.
   * 
   * @throws TimeoutException
   * @throws IOException
   */
  public PdfParagraphsIdentifier(TeXFile texFile)
    throws IOException, TimeoutException {
    affirm(texFile != null);

    this.texFile = texFile;
    this.lbi = new LineBoundingBoxesIdentifier(texFile);
  }

  /**
   * Identifies the bounding boxes of paragraphs in given tex file.
   * 
   * @throws TimeoutException
   * @throws IOException
   */
  public void identify() throws IOException, TimeoutException {
    identifyPdfParagraphs();
  }

  protected void identifyPdfParagraphs() throws IOException, TimeoutException {
    LineBoundingBoxesIdentifier lbi = new LineBoundingBoxesIdentifier(texFile);
    HashMap<PdfLine, PdfLine> alreadySeenLines = new HashMap<>();

    for (TexParagraph paragraph : this.texFile.getTeXParagraphs()) {
      LinkedList<PdfLine> paraLines = new LinkedList<>();

      for (int i : paragraph.getTexLineNumbers()) {
        List<PdfLine> pdfLines = lbi.getBoundingBoxesOfLine(i, 0);

        for (PdfLine line : pdfLines) {
          if (!alreadySeenLines.containsKey(line)) {
            ListIterator<PdfLine> linesItr =
                paraLines.listIterator(paraLines.size());

            while (linesItr.hasPrevious()) {
              int order = obtainReadingOrder(linesItr.previous(), line);
              // Check, if the lines are in correct order.
              if (order < 1) {
                linesItr.next();
                break;
              }
            }
            linesItr.add(line);
            alreadySeenLines.put(line, line);
          }
        }
      }

      this.texFile.getPdfLines().addAll(paraLines);

      PdfLine prevLine = null;
      PdfParagraph pdfParagraph = null;
      for (PdfLine line : paraLines) {
        if (introducesNewPdfParagraph(prevLine, line)) {
          if (pdfParagraph != null && pdfParagraph.getNumLines() > 0) {
            paragraph.addPdfParagraph(pdfParagraph);
          }
          pdfParagraph = new PdfParagraph();
        }
        pdfParagraph.addPdfLine(line);

        prevLine = line;
      }
      if (pdfParagraph != null && pdfParagraph.getNumLines() > 0) {
        paragraph.addPdfParagraph(pdfParagraph);
      }
    }
  }

  // protected void identifyPdfParagraphs3() {
  // HashMap<Integer, PdfLine> lines = new HashMap<>();
  //
  // for (PdfLine line : this.texFile.getPdfLines()) {
  // for (int lineNum : line.getTexLineNumbers()) {
  // if (!lines.containsKey(lineNum)) {
  // lines.put(lineNum, line);
  // }
  // }
  // }
  //
  // for (TexParagraph paragraph : this.texFile.getTeXParagraphs()) {
  // int startLine = paragraph.getTexStartLine();
  // int endLine = paragraph.getTexEndLine();
  //
  // PdfLine prevLine = null;
  // List<PdfParagraph> paragraphs = new ArrayList<>();
  // PdfParagraph pdfParagraph = new PdfParagraph();
  //
  // for (int i = startLine; i <= endLine; i++) {
  // PdfLine line = lines.get(i);
  //
  // if (line != null) {
  // if (introducesNewPdfParagraph(prevLine, line)) {
  // if (pdfParagraph.getNumLines() > 0) {
  // paragraphs.add(pdfParagraph);
  // }
  //
  // pdfParagraph = new PdfParagraph();
  // }
  // pdfParagraph.addPdfLine(line);
  //
  // prevLine = line;
  // }
  // }
  // if (pdfParagraph.getNumLines() > 0) {
  // paragraphs.add(pdfParagraph);
  // }
  //
  // paragraph.setPdfParagraphs(paragraphs);
  // }
  // }
  //
  // protected void identifyPdfParagraphs2() {
  // HashMap<Integer, TexParagraph> paragraphStarts = new HashMap<>();
  // List<TexParagraph> paragraphs = this.texFile.getTeXParagraphs();
  //
  // for (TexParagraph paragraph : paragraphs) {
  // paragraphStarts.put(paragraph.getTexStartLine(), paragraph);
  // }
  //
  // TexParagraph currentTexParagraph = null;
  // PdfParagraph currentPdfParagraph = null;
  //
  // PdfLine prevLine = null;
  // for (PdfLine line : this.texFile.getPdfLines()) {
  // for (Integer lineNum : line.getTexLineNumbers()) {
  // if (paragraphStarts.containsKey(lineNum)) {
  // if (currentPdfParagraph != null && currentTexParagraph != null &&
  // currentPdfParagraph.getNumLines() > 0) {
  // currentTexParagraph.addPdfParagraph(currentPdfParagraph);
  // }
  //
  // currentTexParagraph = paragraphStarts.get(lineNum);
  // currentPdfParagraph = new PdfParagraph();
  // }
  // }
  //
  // if (introducesNewPdfParagraph(prevLine, line)) {
  // if (currentPdfParagraph != null && currentTexParagraph != null &&
  // currentPdfParagraph.getNumLines() > 0) {
  // currentTexParagraph.addPdfParagraph(currentPdfParagraph);
  // }
  //
  // currentPdfParagraph = new PdfParagraph();
  // }
  //
  // currentPdfParagraph.addPdfLine(line);
  // prevLine = line;
  // }
  //
  // if (currentPdfParagraph != null && currentTexParagraph != null &&
  // currentPdfParagraph.getNumLines() > 0) {
  // currentTexParagraph.addPdfParagraph(currentPdfParagraph);
  // }
  // }
  //
  protected boolean introducesNewPdfParagraph(PdfLine prevLine, PdfLine line) {
    if (prevLine == null && line == null) {
      return false;
    }

    if (prevLine == null) {
      return true;
    }

    if (line == null) {
      return false;
    }

    if (prevLine.getPdfPageNumber() != line.getPdfPageNumber()) {
      return true;
    }

    Rectangle pageRect = lbi.getBoundingBoxOfPage(line.getPdfPageNumber());
    Rectangle prevRect = prevLine.getRectangle();
    Rectangle rect = line.getRectangle();

    float height = prevRect.getHeight();
    float prevMinX = prevRect.getMinX();
    float prevMaxX = prevRect.getMaxX();
    float prevMinY = prevRect.getMinY();
    float prevMaxY = prevRect.getMaxY();
    float minX = rect.getMinX();
    float maxX = rect.getMaxX();
    float minY = rect.getMinY();
    float maxY = rect.getMaxY();

    if (Math.abs(prevMinY - maxY) > 0.25 * pageRect.getHeight()) {
      return true;
    }

    // Try to identify column changes.
    // if (prevMaxX < minX && prevMaxY < minY) {
    // return true;
    // }

    return false;
  }

  // ===========================================================================

  /**
   * Returns -1 if the first given line comes before the second given line.
   * Returns 0 if the both lines are equal. Returnd 1 if the second given line
   * comes before the first given line.
   */
  protected int obtainReadingOrder(PdfLine line1, PdfLine line2) {
    return line1.compareTo(line2);
  }
}
