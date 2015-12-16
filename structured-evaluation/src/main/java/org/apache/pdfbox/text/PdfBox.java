package org.apache.pdfbox.text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;

/**
 * Class to evaluate the text extraction results from PdfBox. 
 *
 * @author Claudius Korzen.
 */
public class PdfBox {
  /**
   * The main method.
   * 
   * @param args the arguments.
   * 
   * @throws IOException if reading a pdf file fails. 
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.out.println("Usage: java -jar PdfBox.jar <input-dir> <output-dir>");
      System.exit(1);
    }
    
    String inputStr = "/nfs/raid5/korzen/icecite/structured-evaluation/pdfs/";
    String outputStr = "/nfs/raid5/korzen/icecite/structured-evaluation/pdfbox";
    
    File input = new File(inputStr);
    File output = new File(outputStr);
    
    if (input.isFile()) {
      handleFile(input, output);
    } else if (input.isDirectory())
      for (File file : input.listFiles()) {
        handleFile(file, output);
      }
  }
  
  /**
   * Handles a pdf file.
   * 
   * @param file the file to handle.
   * @param outputDir the directory where to write the result file to.
   * @throws IOException if reading the pdf or writing the result file fails.
   */
  public static void handleFile(File file, File outputDir) throws IOException {        
    String filename = file.getName().toLowerCase().trim();
    if (file.isFile() && filename.endsWith(".pdf")) {
      PDDocument doc = PDDocument.load(file);
      
      File output = new File(outputDir, file.getName() + ".txt");
      FileOutputStream fos = new FileOutputStream(output);
      BufferedOutputStream stream = new BufferedOutputStream(fos); 
      
      CustomPDFTextStripper stripper = new CustomPDFTextStripper(stream);
      String text = stripper.getText(doc);
      
      stripper.writeOutput(text, new SimpleRectangle(), "text");
      
      stream.close();
    }
  }
}

/**
 * Our extension of PDFTextStripper.
 *
 * @author Claudius Korzen
 *
 */
class CustomPDFTextStripper extends PDFTextStripper {
  /** The output writer. */
  protected BufferedOutputStream stream;
  /** The current page number. */
  protected int currentPageNumber = 0;
  /** The current word. */  
  protected StringBuilder word = new StringBuilder();
  /** The current word boundary. */
  protected List<Rectangle> wordElements = new ArrayList<Rectangle>();
  /** The current line. */
  protected StringBuilder line = new StringBuilder();
  /** The current line boundary. */
  protected List<Rectangle> lineElements = new ArrayList<Rectangle>();
  /** The current paragraph. */
  protected StringBuilder paragraph = new StringBuilder();
  /** The current paragraph boundary. */
  protected List<Rectangle> paragraphElements = new ArrayList<Rectangle>();
  
  /**
   * The constructor
   * 
   * @param stream the stream where to write the result file to.
   * @throws IOException if something fails.
   */
  public CustomPDFTextStripper(BufferedOutputStream stream) throws IOException {
    super();
    this.stream = stream;
  }
  
  @Override
  protected void writeString(final String text,
    List<TextPosition> textPositions) throws IOException {
    super.writeString(text, textPositions);
           
    for (TextPosition pos : textPositions) {
      if (pos != null) {
        String charText = pos.getUnicode();
        if (charText != null) {
          charText = charText.trim();
          if (charText.isEmpty()) {
            writeWordSeparator();
          } else {
            Rectangle characterBoundingBox = getBoundingBox(pos);
            
            writeOutput(pos.getUnicode(), characterBoundingBox, "character");
            this.word.append(charText);
            this.wordElements.add(characterBoundingBox);
            this.line.append(charText);
            this.lineElements.add(characterBoundingBox);
            this.paragraph.append(charText);
            this.paragraphElements.add(characterBoundingBox);  
          }  
        }
      }
    }
  }
  
  @Override
  protected void writeWordSeparator() throws IOException {
    super.writeWordSeparator();
    
    Rectangle wordBoundingBox = SimpleRectangle.computeBoundingBox(wordElements);
    this.wordElements = new ArrayList<Rectangle>();
    
    String word = this.word.toString().trim();
    if (!word.isEmpty()) {
      writeOutput(word, wordBoundingBox, "word");
    }
    
    this.word.setLength(0);
    this.line.append(getWordSeparator());
    this.paragraph.append(getWordSeparator());
  }
  
  @Override
  protected void writeLineSeparator() throws IOException {
    super.writeLineSeparator();
    
    Rectangle lineBoundingBox = SimpleRectangle.computeBoundingBox(lineElements);
    this.lineElements = new ArrayList<Rectangle>();
    
    String line = this.line.toString().trim();
    if (!line.isEmpty()) {
      writeOutput(line, lineBoundingBox, "line");
    }
    
    this.word.setLength(0);
    this.line.setLength(0);
    this.paragraph.append(getWordSeparator());
  }
  
  @Override
  protected void writeParagraphEnd() throws IOException {
    super.writeParagraphEnd();
    
    Rectangle paragraphBoundingBox = SimpleRectangle.computeBoundingBox(paragraphElements);
    this.paragraphElements = new ArrayList<Rectangle>();
    
    String paragraph = this.paragraph.toString().trim();
    if (!paragraph.isEmpty()) {
      writeOutput(paragraph, paragraphBoundingBox, "paragraph");
    }
    
    this.word.setLength(0);
    this.line.setLength(0);
    this.paragraph.setLength(0);
  }
  
  @Override
  protected void startPage(PDPage page) throws IOException {
    super.startPage(page);
    this.currentPageNumber++;
  }
   
  // ___________________________________________________________________________
  
  /**
   * Write a result line to output.
   * 
   * @param text the extracte text
   * @param box the rectangle of object.
   * @param type the type of object.
   * @throws IOException if writing fails.
   */
  protected void writeOutput(String text, Rectangle box, String type) 
      throws IOException {
    stream.write((type + "\t" + currentPageNumber + "\t" + box.getMinX() + " " 
        + box.getMinY() + " " + box.getMaxX() + " " + box.getMaxY() + "\t" 
        + text.replaceAll("[\t\n]", " ") + "\n").getBytes());
  }
  
  /**
   * Returns the bounding box of given text position.
   * 
   * @param position the text position
   * @return the bounding box of given text position.
   */
  protected Rectangle getBoundingBox(TextPosition position) {
    Rectangle boundingBox = new SimpleRectangle();
    boundingBox.setMinX(position.getX());
    boundingBox.setMinY(position.getY());
    boundingBox.setMaxX(position.getX() + position.getWidth());
    boundingBox.setMaxY(position.getY() + position.getHeight());    
    return boundingBox;
  }
  
  /**
   * Returns the bounding box of given text positions.
   * 
   * @param positions the text positions
   * @return the bounding box of given text positions.
   */
  protected Rectangle getBoundingBox(List<TextPosition> positions) {
    Rectangle wordBoundingBox = new SimpleRectangle();
    wordBoundingBox.setMinX(Float.MAX_VALUE);
    wordBoundingBox.setMinY(Float.MAX_VALUE);
    wordBoundingBox.setMaxX(-Float.MAX_VALUE);
    wordBoundingBox.setMaxY(-Float.MAX_VALUE);
    
    for (TextPosition pos : positions) {
      if (pos != null) {
        if (pos.getX() < wordBoundingBox.getMinX()) {
          wordBoundingBox.setMinX(pos.getX());
        }
        if (pos.getY() < wordBoundingBox.getMinY()) {
          wordBoundingBox.setMinY(pos.getY());
        }
        if (pos.getX() + pos.getWidth() > wordBoundingBox.getMaxX()) {
          wordBoundingBox.setMaxX(pos.getX() + pos.getWidth());
        }
        if (pos.getY() + pos.getHeight() > wordBoundingBox.getMaxY()) {
          wordBoundingBox.setMaxY(pos.getY() + pos.getHeight());
        }
      }
    }
    
    return wordBoundingBox;
  }
}
