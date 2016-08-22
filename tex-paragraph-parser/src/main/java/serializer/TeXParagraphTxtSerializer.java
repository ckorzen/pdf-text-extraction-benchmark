package serializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.collection.CollectionUtils;
import model.PdfParagraph;
import model.TeXFile;
import model.TeXParagraph;

/**
 * Class to serialize the paragraphs of a tex file to plain txt file.
 * 
 * @author Claudius Korzen
 */
public class TeXParagraphTxtSerializer {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
    
  /**
   * The default constructor.
   */
  public TeXParagraphTxtSerializer(TeXFile texFile) {
    this.texFile = texFile;
  }
  
  /**
   * Serializes the paragraphs of given tex file to given path.
   */
  public void serialize(Path target) throws IOException {
    serialize(target, null);
  }
  
  /**
   * Serializes the paragraphs of given tex file to given path.
   */
  public void serialize(Path target, List<String> roles) throws IOException {
    // Create the target file if it doesn't exist yet.
    if (!Files.exists(target)) {
      Files.createDirectories(target.getParent());
      Files.createFile(target);
    }
    
    OutputStream stream = Files.newOutputStream(target);
    
    serializeTeXParagraphs(stream, roles);
    
    stream.close();
  }
  
  /**
   * Serializes the paragraphs of given tex file to given output stream.
   */
  public void serializeTeXParagraphs(OutputStream stream, List<String> roles)
      throws IOException {
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
    
    serializeTeXParagraphs(writer, roles);
    
    writer.close();
  }
  
  /**
   * Serializes the paragraphs of given tex file to given writer.
   */
  public void serializeTeXParagraphs(BufferedWriter writer, 
      List<String> roles) throws IOException {
    List<TeXParagraph> paragraphs = texFile.getTeXParagraphs();
    
    if (paragraphs == null) {
      return;
    }
        
    // Create list of paragraph texts.
    List<String> paraTexts = new ArrayList<>();
    for (TeXParagraph para : paragraphs) {
      // Don't serialize any paragraphs that don't have any pdf paragraphs. 
      
      List<PdfParagraph> pdfParas = para.getPdfParagraphs();
      if (para != null && pdfParas != null) {        
        boolean serialize = !pdfParas.isEmpty() 
            || "title".equals(para.getRole());
        
        if (serialize) {
          // Don't consider the paragraph if there is a list of roles given and
          // it doesn't contain the role of the paragraph.
          
          if (roles != null && !roles.contains(para.getRole())) {
            continue;
          }
          
          String text = para.getText();
          
          if (text != null) {
            text = text.trim();
            
            if (!text.isEmpty()) {
              paraTexts.add(text);
            }
          }
        }
      }
    }
    
    // Join the text by a double newline.
    writer.write(CollectionUtils.join(paraTexts, "\n\n"));
  }
}
