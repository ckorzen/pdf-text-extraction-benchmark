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
import model.TeXFile;
import model.TeXParagraph;
import model.TeXWord;

/**
 * Class to serialize the paragraphs of a tex file to plain txt file, where
 * each word is enriched with (line-number,column-number).
 * 
 * @author Claudius Korzen
 */
public class TeXParagraphExtendedTxtSerializer {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;

  /**
   * The default constructor.
   */
  public TeXParagraphExtendedTxtSerializer(TeXFile texFile) {
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

    try (OutputStream stream = Files.newOutputStream(target)) {
      serializeTeXParagraphs(stream, roles);
    }
  }

  /**
   * Serializes the paragraphs of given tex file to given output stream.
   */
  public void serializeTeXParagraphs(OutputStream s, List<String> roles)
    throws IOException {
    try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s))) {
      serializeTeXParagraphs(w, roles);  
    }
  }

  /**
   * Serializes the paragraphs of given tex file to given writer.
   */
  public void serializeTeXParagraphs(BufferedWriter writer, List<String> roles)
    throws IOException {
    List<TeXParagraph> paragraphs = texFile.getTeXParagraphs();

    if (paragraphs == null) {
      return;
    }

    // Create list of paragraph texts.
    List<String> paraTexts = new ArrayList<>();
    for (TeXParagraph para : paragraphs) {
      // Don't consider the paragraph if there is a list of roles given and
      // it doesn't contain the role of the paragraph.

      if (roles != null && !roles.contains(para.getRole())) {
        continue;
      }
      
      List<TeXWord> words = para.getWords();
      
      StringBuilder paraText = new StringBuilder();
      
      for (int i = 0; i < words.size(); i++) {
        TeXWord word = words.get(i);
        String text = word.text.trim();
        int lineNumber = word.lineNumber;
        int columnNumber = word.columnNumber;
        
        paraText.append(text);
        paraText.append("(" + lineNumber + "," + columnNumber + ")");
        
        if (i < words.size() - 1) {
          paraText.append(" ");
        }
      }
      
      String text = paraText.toString();

      if (text != null) {
        text = text.trim();

        if (!text.isEmpty()) {
          paraTexts.add(text);
        }
      }
    }

    // Join the text by a double newline.
    writer.write(CollectionUtils.join(paraTexts, "\n\n"));
  }
}
