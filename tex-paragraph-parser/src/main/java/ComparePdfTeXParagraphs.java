

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import analyzer.PdfAnalyzer;
import analyzer.PlainPdfAnalyzer;
import de.freiburg.iif.path.PathUtils;
import drawer.PdfDrawer;
import drawer.pdfbox.PdfBoxDrawer;
import identifier.PdfParagraphsIdentifier;
import identifier.TeXParagraphsIdentifier;
import model.PdfDocument;
import model.PdfPage;
import model.PdfParagraph;
import model.PdfTextParagraph;
import model.TeXFile;
import model.TeXParagraph;
import parser.PdfExtendedParser;
import parser.PdfParser;
import parser.PdfXYCutParser;
import parser.pdfbox.PdfBoxParser;
import revise.PdfRevisor;
import visualizer.PdfVisualizer;

/**
 * Class to compare the paragraphs output from PdfParser with the paragraphs
 * output from TeX Parser.
 * 
 * @author Claudius Korzen
 */
public class ComparePdfTeXParagraphs {
  /**
   * The pdf parser to extract characters, figures and shapes.
   */
  protected PdfParser pdfParser;

  /**
   * The extended pdf parser to extract paragraphs from pdf.
   */
  protected PdfExtendedParser extendedPdfParser;
  
  /**
   * The pdf revisor.
   */
  protected PdfRevisor pdfRevisor;
 
  /**
   * The pdf visualizer to create a visualization of the extracted features.
   */
  protected PdfVisualizer pdfVisualizer;

  /**
   * The input path as it is given by the command line.
   */
  protected String texInputPath;

  /**
   * The output path as it is given by the command line.
   */
  protected String outPdfPath;
 
  /**
   * The path to the texmf dir. On computing positions of paragraphs, we need 
   * to compile the tex files to pdf files. But the tex files may depend on 
   * some system-unknown documentstyles (e.g. "revtex"). 
   * To be able to compile such tex files, one can define this path to a 
   * directory where the related sty files, cls files etc. can be found. 
   */
  protected List<String> texmfPaths = Arrays.asList(
      PathUtils.getWorkingDirectory(getClass()) + "/classes/texmf");
  
  // ___________________________________________________________________________

  /**
   * The main method.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Usage: java ... <TEX-FILE> <PDF-FILE>");
      System.exit(1);
    }
    
    for (int i = 0; i < 10; i++) {
      System.out.println(i);
      try {
        String texInputPath = "/home/korzen/Downloads/Trainings_Pdfs_David/" 
            + i + "/" + i + ".tex";
        String pdfOutputPath = "/home/korzen/Downloads/Trainings_Pdfs_David/" 
            + i + "/" + i + "_comparison.pdf";
              
        ComparePdfTeXParagraphs comparator = new ComparePdfTeXParagraphs();
        
        comparator.compare(texInputPath, pdfOutputPath);
      } catch (Exception e) {
        e.printStackTrace();
      } catch (Error e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Creates a new instance of this program based on the given arguments.
   */
  public ComparePdfTeXParagraphs() {
    this.pdfParser = new PdfBoxParser();
    this.extendedPdfParser = new PdfXYCutParser();
//    this.pdfAnalyzer = new PlainPdfAnalyzer();
    this.pdfRevisor = new PdfRevisor();
  }

  public void compare(String texInputPath, String pdfOutputPath) 
      throws IOException {
    Path file = Paths.get(texInputPath);
    TeXFile texFile = new TeXFile(file);
    
    long start = System.currentTimeMillis();
    new TeXParagraphsIdentifier(texFile).identify();
    new PdfParagraphsIdentifier(texFile, texmfPaths).identify();
    long end = System.currentTimeMillis();
    
    System.out.println("Tex: " + (end - start) + "ms");
    
    // =======================
    start = System.currentTimeMillis();
    PdfDocument document = pdfParser.parse(texFile.getPdfPath());
    // Extract words, lines and paragraphs.
    document = extendedPdfParser.parse(document);  
    new PlainPdfAnalyzer(document).analyze();
//    pdfRevisor.revise(document);
    end = System.currentTimeMillis();
    
    System.out.println("Pdf: " + (end - start) + "ms");
    
    visualize(texFile, document, pdfOutputPath);
  }
  
  protected void visualize(TeXFile texFile, PdfDocument document, 
      String pdfOutputPath) throws IOException {
    PdfDrawer drawer = new PdfBoxDrawer(texFile.getPdfPath());
    
    for (TeXParagraph texPara : texFile.getTeXParagraphs()) {
      for (PdfParagraph pdfPara : texPara.getPdfParagraphs()) {
        drawer.drawRectangle(pdfPara.getPdfBoundingBox(), 
            pdfPara.getPdfPageNumber(), Color.RED);
      }
    }
       
    for (PdfPage page : document.getPages()) {
      for (PdfTextParagraph pdfPara : page.getParagraphs()) {
        drawer.drawRectangle(pdfPara.getRectangle(), page.getPageNumber(), 
            Color.BLUE);
      }
      
//    for (PdfCharacter element : page.getTextCharacters()) {
////    drawer.drawLine(line.getColumnXRange(), page.getPageNumber());
////      drawer.drawRectangle(element.getRectangle(), page.getPageNumber());
//      drawer.drawText("" + (element.getRectangle().getMinY()), page.getPageNumber(), element.getRectangle().getLowerRight(), Color.BLACK, 2);
//    }
// 
//      for (PdfTextLine line : page.getTextLines()) {
////        drawer.drawLine(line.getColumnXRange(), page.getPageNumber());
//        drawer.drawRectangle(line.getRectangle(), page.getPageNumber());
////        drawer.drawText("" + line.getTextStatistics().getMostCommonFontsize(), page.getPageNumber(), line.getRectangle().getLowerRight(), Color.BLACK, 5);
//      }
    }
             
    try (OutputStream os = Files.newOutputStream(Paths.get(pdfOutputPath))) {
      drawer.writeTo(os);  
    }
  }
}
