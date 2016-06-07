package identifier;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.freiburg.iif.model.Rectangle;
import drawer.PdfDrawer;
import drawer.pdfbox.PdfBoxDrawer;
import model.PdfLine;
import model.PdfParagraph;
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
  protected Set<PdfLine> alreadySeenLines = new HashSet<>();
  
  /**
   * Creates a new pdf paragraph identifier.
   */
  public PdfParagraphsIdentifier(TeXFile texFile) throws IOException {    
    affirm(texFile != null);
    
    this.texFile = texFile;
    this.lineIdentifier = new PdfLineIdentifier(texFile);
    this.pageIdentifier = new PdfPageIdentifier(texFile);
  }
  
  /**
   * Identifies the pdf paragraphs from given tex file.
   */
  public void identify() throws IOException {    
    for (TeXParagraph paragraph : this.texFile.getTeXParagraphs()) {      
      List<PdfLine> pdfLines = identifyPdfLines(paragraph);
      List<PdfParagraph> pdfParagraphs = identifyPdfParagraphs(pdfLines);
            
      paragraph.setPdfParagraphs(pdfParagraphs);
    }
    
    // Visualize the bounding boxes.
    visualizeBoundingBoxes(texFile);
  }
    
  /**
   * Visualizes the given bounding boxes.
   */
  protected void visualizeBoundingBoxes(TeXFile texFile) throws IOException {
    PdfDrawer drawer = new PdfBoxDrawer(texFile.getPdfPath());
    
    for (TeXParagraph p : texFile.getTeXParagraphs()) {
      for (PdfParagraph x : p.getPdfParagraphs()) {
        drawer.drawRectangle(x.getPdfBoundingBox(), x.getPdfPageNumber());
      }
    }

    File output = new File("/home/korzen/Downloads/paraboxes.pdf");

    FileOutputStream fos = new FileOutputStream(output);
    drawer.writeTo(fos);
    fos.close();
  }
  
  /**
   * Identifies the pdf lines for the given tex paragraph.
   */
  protected List<PdfLine> identifyPdfLines(TeXParagraph paragraph) 
      throws IOException {
    LinkedList<PdfLine> paraLines = new LinkedList<>();
    
    for (int i : paragraph.getTexLineNumbers()) {
      
      List<PdfLine> pdfLines = lineIdentifier.getBoundingBoxesOfLine(i, 0);
                     
      for (PdfLine line : pdfLines) {
        
        // Clean up the lines a bit: Only consider still unknown lines and
        // let "climb up" the line to the "correct" position (with respect to
        // the reading order).
        if (!alreadySeenLines.contains(line)) {
          int numParaLines = paraLines.size();
          ListIterator<PdfLine> itr = paraLines.listIterator(numParaLines);
        
          while (itr.hasPrevious()) {
            int order = obtainReadingOrder(itr.previous(), line);
            // Check, if the lines are in correct order.
            if (order < 1) {
              itr.next();
              break;
            }
          }
          itr.add(line);
          alreadySeenLines.add(line);
        }
      }
    }
    
    return paraLines;
  }
  
  /**
   * Identifies the pdf paragraphs in the given pdf lines.
   */
  protected List<PdfParagraph> identifyPdfParagraphs(List<PdfLine> pdfLines) 
      throws IOException {
    List<PdfParagraph> pdfParagraphs = new ArrayList<>();
           
    // Split the lines into paragraphs.
    PdfLine prevLine = null;
    PdfParagraph pdfParagraph = null;
    for (PdfLine line : pdfLines) {        
      if (introducesNewPdfParagraph(prevLine, line)) {
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
    
    // The lines introduce a new paragraph if page numbers differ. 
    if (prevLine.getPdfPageNumber() != line.getPdfPageNumber()) {
      return true;
    }
    
    // EXPERIMENTAL: Analyze the vertical distance between the lines.
    Rectangle pageRect = pageIdentifier.getBoundingBox(line.getPdfPageNumber());
    Rectangle prevRect = prevLine.getRectangle();
    Rectangle rect = line.getRectangle();
    
//    float height = prevRect.getHeight();
//    float prevMinX = prevRect.getMinX();
//    float prevMaxX = prevRect.getMaxX();
    float prevMinY = prevRect.getMinY();
//    float prevMaxY = prevRect.getMaxY();
//    float minX = rect.getMinX();
//    float maxX = rect.getMaxX();
//    float minY = rect.getMinY();
    float maxY = rect.getMaxY();
    
    // Compute the distance between the lines.
    float verticalDistance = Math.abs(prevMinY - maxY);
    
    // The lines introduce a new paragraph if verticalDistance is "too large".
    if (verticalDistance > 0.25 * pageRect.getHeight()) {
      return true;
    }
    
    return false;
  }
  
  // ===========================================================================
  
  /**
   * Returns -1 if the first given line comes before the second given line.
   * Returns 0 if the both lines are equal.
   * Returnd 1 if the second given line comes before the first given line.
   */
  protected int obtainReadingOrder(PdfLine line1, PdfLine line2) {
    return line1.compareTo(line2);
  }
}
