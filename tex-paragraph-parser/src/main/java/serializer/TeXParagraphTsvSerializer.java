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
import de.freiburg.iif.model.Rectangle;
import model.PdfParagraph;
import model.TeXFile;
import model.TeXParagraph;

/**
 * Class to serialize the paragraphs of a tex file.
 * 
 * @author Claudius Korzen
 *
 */
public class TeXParagraphTsvSerializer {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
    
  /**
   * The default constructor.
   */
  public TeXParagraphTsvSerializer(TeXFile texFile) {
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
   * @throws IOException 
   */
  public void serializeTeXParagraphs(BufferedWriter writer, List<String> roles) 
      throws IOException {
    writer.write(String.format("%s\t%s\t%s\t%s\t%s", 
        "feature", "start line", "end line", "bounding boxes", "text"));
    writer.newLine();
    
    for (TeXParagraph para : texFile.getTeXParagraphs()) {
      // Don't consider the paragraph if there is a list of roles given and
      // it doesn't contain the role of the paragraph.
      if (roles != null && !roles.contains(para.getRole())) {
        continue;
      }
      
      String feature = para.getRole() != null ? para.getRole() : "text";
      int startLine = para.getTexStartLine();
      int endLine = para.getTexEndLine();
      
      List<String> boxesList = new ArrayList<>();
      if (para.getPdfParagraphs() != null) {
        for (PdfParagraph pdfPara : para.getPdfParagraphs()) {
          String serialized = serializePdfParagraph(pdfPara);
          if (serialized != null && !serialized.isEmpty()) {
            boxesList.add(serialized);
          }
        }
      }
      String boxes = CollectionUtils.join(boxesList, ",");
      String text = para.getText().trim();
      
      writer.write(String.format("%s\t%d\t%d\t%s\t%s", 
          feature, startLine, endLine, boxes, text));
      writer.newLine();
    }
  }
  
  /**
   * Serializes the given pdf paragraph.
   */
  protected String serializePdfParagraph(PdfParagraph paragraph) {
    if (paragraph == null) {
      return null;
    }
    
    Rectangle box = paragraph.getRectangle();
 
    if (box == null) {
      return null;
    }
    
    return String.format("(%d;[%f;%f;%f;%f])", paragraph.getPdfPageNumber(),
        box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
  }
}
