package identifier;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import external.PdfLaTeX;
import model.PdfLine;
import model.TeXFile;
import model.TeXParagraph;
import parser.PdfLinesParser;

/**
 * Class to identify lines (page number and rectangle) from pdf files.
 * 
 * @author Claudius Korzen
 *
 */
public class PdfLineIdentifier {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;
    
  /** 
   * The addendum we append to the end of each paragraph. 
   * Synctex has issues to identify the coordinates of a line on so called
   * "widows". So we add some (slim) text to end of paragraphs to avoid such
   * widows (paragraph bounding boxes aren't affected). 
   */
  protected static final String PARA_ADDENDUM = "\\hspace{-5pt}i";
 
  /**
   * Creates a new pdf line identifier.
   */
  public PdfLineIdentifier(TeXFile texFile) {
    this.texFile = texFile;
    
    // Handle widows.
    Path tmpTeXPath = handleWidows(texFile);
    this.texFile.setTmpPath(tmpTeXPath);
    
    // Compile the enriched version of tex file.
    Path pdfPath = compileTexFile(tmpTeXPath);
    this.texFile.setPdfPath(pdfPath);
  }
  
  /**
   * Synctex has issues to identify the coordinates of a line on so called
   * "widows". So we add some (slim) text to end of paragraphs to avoid such
   * widows (paragraph bounding boxes aren't affected). 
   */
  protected Path handleWidows(TeXFile texFile) {
    affirm(texFile != null, "No tex file given");

    List<String> texLines = readTexLines(texFile.getTmpPath());
    List<TeXParagraph> paragraphs = texFile.getTeXParagraphs();

    // Iterate through the paragraphs and identify the end line of each para.
    for (int i = 0; i < paragraphs.size(); i++) {
      TeXParagraph prevParagraph = i > 0 ? paragraphs.get(i - 1) : null;
      
      if (prevParagraph != null) {
        int lineNum = prevParagraph.getTexEndLine();

        affirm(lineNum > 0, "Line number is too small.");
        affirm(lineNum < texLines.size(), "Line number is too large.");

        String prevParagraphEndLine = texLines.get(lineNum);
        
        // Add the addendum to the end of paragraph.
        if (!prevParagraphEndLine.trim().startsWith("\\")
            && !prevParagraphEndLine.trim().endsWith("\\")
            && !prevParagraphEndLine.trim().endsWith("{")
            && !prevParagraphEndLine.trim().endsWith("}")) { // TODO
          prevParagraphEndLine += PARA_ADDENDUM;
          texLines.set(lineNum, prevParagraphEndLine);
        }
      }
    }

    // Furthermore, \label commands make trouble on identifying the coordinates
    // of lines correctly. Comment out such labels.
//    for (int i = 0; i < texLines.size(); i++) {
//      String line = texLines.get(i);
//
//      if (line != null && line.startsWith("\\label")) {
//        texLines.set(i, "%" + line);
//      }
//    }

    // Write the enriched tex file.
    Path enriched = defineTmpTexPath(texFile);
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

  // ===========================================================================

  /**
   * Parses the synctex output for given line to get the coordinates of the
   * line.
   */
  public List<PdfLine> getBoundingBoxesOfLine(int lineNum, int columnNumber) 
      throws IOException {
    return new PdfLinesParser(this.texFile).parse(lineNum, columnNumber);
  }

  // ===========================================================================
  // Compile methods.

  /**
   * Compiles the given tex file and makes sure that the related pdf- and
   * synctex-file exist.
   */
  protected Path compileTexFile(Path texPath) {
    try {
      Path outputDir = defineOutputDirectory(texFile);     
      new PdfLaTeX(texPath, true, outputDir).run(true);
    } catch (Exception e) {
      throw new IllegalStateException("Couldn't compile the tex file.");
    }

    Path pdfFile = definePdfPath(texFile);
    affirm(pdfFile != null, "No PDF file produced.");
    affirm(Files.isRegularFile(pdfFile), "No PDF file produced.");

    Path syncTexFile = defineSynctexPath(texFile);
    affirm(syncTexFile != null, "No syncTeX file produced.");
    affirm(Files.isRegularFile(syncTexFile), "No syncTeX file produced.");

    return pdfFile;
  }

  // ===========================================================================
  // Compile methods.
  
  /**
   * Returns the path to the enriched pdf file.
   */
  protected Path defineTmpTexPath(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".tmp.tex";
      Path outputDir = defineOutputDirectory(texFile);
      Path enrichedFile = Paths.get(outputDir.toString(), filename);

      return enrichedFile;
    }
    return null;
  }

  /**
   * Returns the pdf file related to the given tex file or null if it doesn't
   * exist.
   */
  protected Path definePdfPath(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".tmp.pdf";
      Path outputDir = defineOutputDirectory(texFile);
      Path pdfFile = Paths.get(outputDir.toString(), filename);

      return pdfFile;
    }
    return null;
  }

  /**
   * Returns the pdf file related to the given tex file or null if it doesn't
   * exist.
   */
  protected Path defineSynctexPath(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".tmp.synctex";
      Path outputDir = defineOutputDirectory(texFile);
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
  protected Path defineOutputDirectory(TeXFile texFile) {
    return texFile.getPath().getParent().toAbsolutePath();
  }
}
