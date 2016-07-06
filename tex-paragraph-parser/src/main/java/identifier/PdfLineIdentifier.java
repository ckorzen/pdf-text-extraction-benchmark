package identifier;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;

import model.Command;
import model.Element;
import model.Group;
import model.NewLine;
import model.SyncTeXBoundingBox;
import model.TeXFile;
import model.TeXParagraph;
import model.Text;
import model.Whitespace;
import pdflatex.PdfLaTeX;
import synctex.SyncTeX;

/**
 * Class to identify the position (page number and bounding box) of lines from
 * tex files in pdf files.
 * 
 * @author Claudius Korzen
 */
public class PdfLineIdentifier {
  /**
   * The tex file to process.
   */
  protected TeXFile texFile;

  /**
   * The path to the texmf dir.
   */
  protected List<String> texmfPaths;

  /**
   * The synctex parser. This is the core to identify the positions of 
   * paragraphs.
   */
  protected SyncTeX synctex;

  /**
   * The addendum we append to the end of each paragraph. Synctex has issues to
   * identify the coordinates of a line on so called "widows". So we add some
   * (slim) text to end of paragraphs to avoid such widows and to get reliable
   * positions.
   */
  protected static final String PARA_ADDENDUM = 
      "\\nobreak\\hspace{0pt}{\\tiny \\textbar}";
//  protected static final String PARA_ADDENDUM = "";

  /**
   * Creates a new pdf line identifier.
   */
  public PdfLineIdentifier(TeXFile texFile, List<String> texmfPaths)
    throws IOException {
    this.texFile = texFile;
    this.texmfPaths = texmfPaths;
    this.synctex = new SyncTeX(texFile);
  }

  /**
   * Returns the position of the given line from tex file in pdf file. May be a
   * list of positions, e.g. if the given line was splitted into multiple lines
   * in pdf file.
   */
  public List<SyncTeXBoundingBox> getBoundingBoxes(int lineNum) 
      throws IOException {
    if (texFile.getPdfPath() == null || texFile.getSynctexPath() == null) {
      compileTexFile();
    }
    
    return synctex.getBoundingBoxesOfLine(lineNum);
  }
  
  /**
   * Returns the most common line height in the given tex file.
   */
  public float getMostCommonLineHeight() {
    return synctex.getMostCommonLineHeight();
  }
  
  /**
   * Returns the average line height in the given tex file.
   */
  public float getAverageLineHeight() {
    return synctex.getAverageLineHeight();
  }
  
  // ===========================================================================
  // Compile methods.
  
  /**
   * Compiles the given tex file and makes sure that the related pdf- and
   * synctex-file exist.
   */
  protected Path compileTexFile() throws IOException {
    // Prepare the tex file.
    Path tmpTeXPath = prepareTeXFile(texFile);
    this.texFile.setTmpPath(tmpTeXPath);
    
    try {
      Path outputDir = defineOutputDirectory(texFile);
      new PdfLaTeX(tmpTeXPath, this.texmfPaths, true, outputDir).run(true);
    } catch (Exception e) {
      throw new IllegalStateException("Couldn't compile the tex file.");
    }

    Path pdfFile = definePdfPath(texFile);
    affirm(pdfFile != null, "No PDF file produced.");
    affirm(Files.isRegularFile(pdfFile), "No PDF file produced.");

    Path syncTexFile = defineSynctexPath(texFile);
    affirm(syncTexFile != null, "No syncTeX file produced.");
    affirm(Files.isRegularFile(syncTexFile), "No syncTeX file produced.");

    this.texFile.setPdfPath(pdfFile);
    this.texFile.setSynctexPath(syncTexFile);

    return pdfFile;
  }
  
  protected void disableFigureDefinition(Element element) {
    if (element instanceof Command) {
      Command cmd = (Command) element;
      if ("\\epsfig".equals(cmd.getName())) {
        cmd.setName("%\\epsfig");
      }
      for (Group group : cmd.getGroups()) {
        for (Element el : group.getElements()) {
          disableFigureDefinition(el);
        }
      }
    }
  }
  
  /**
   * Synctex has issues to identify the coordinates of a line on so called
   * "widows". So we add some (slim) text to end of paragraphs to avoid such
   * widows (paragraph bounding boxes aren't affected).
   */
  protected Path prepareTeXFile(TeXFile texFile) {
    affirm(texFile != null, "No tex file given");

//    // TODO: Ignore "\epsfig" commands because they can't be processed by 
//    // pdflatex.
//    ListIterator<Element> itr2 = texFile.getTeXElements().listIterator();
//    while (itr2.hasNext()) {
//      disableFigureDefinition(itr2.next());
//    }
    
    List<TeXParagraph> paragraphs = texFile.getTeXParagraphs();
    
    // Iterate through the paragraphs and identify the end line of each para.
    for (TeXParagraph paragraph : paragraphs) {
      if (paragraph != null) {
        List<Element> elements = paragraph.getTexElements();                
        int numElements = elements.size();
                
        ListIterator<Element> itr = elements.listIterator(numElements);
        
        // Iterate through the elements of paragraph until a non-whitespace
        // is found.
        while (itr.hasPrevious()) {
          Element previous = itr.previous();
          
          if (previous instanceof Whitespace) {
            continue;
          }
          
          if (previous instanceof NewLine) {
            continue;
          }
          
          if (previous instanceof Text) {
            // Append the addendum to text.
            Text text = (Text) previous;
            text.appendText(PARA_ADDENDUM);
            break;
          }
          
          if (previous instanceof Command) {
            Command cmd = (Command) previous;
            
            // TODO: Don't allow the addendum in reserved commands like
            // "\end{document}".
            if ("\\end".equals(cmd.getName())) { 
              break;
            }
            
            if (cmd.hasGroups()) {
              // Append the addendum to last group of command.
              Group group = cmd.getLastGroup();
              group.addElement(new Text(PARA_ADDENDUM, null));
            }
            break;
          }
          
          if (previous instanceof Group) {
            // Append the addendum to group.
            Group group = (Group) previous;
            group.addElement(new Text(PARA_ADDENDUM, null));
            break;
          }
        }
      }
    }

    // Write the enriched tex file.
    Path enriched = defineTmpTexPath(texFile);
    try {
      BufferedWriter writer =
          Files.newBufferedWriter(enriched, StandardCharsets.UTF_8);
      try {
        for (Element element : texFile.getTeXElements()) { // 1-based.
          writer.write(element.toString());
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

  // ===========================================================================
  // Util methods.

  /**
   * Returns the path to the enriched pdf file.
   */
  protected Path defineTmpTexPath(TeXFile texFile) {
    String baseName = getBaseName(texFile);
    if (baseName != null) {
      String filename = baseName + ".tmp.tex";
      Path outputDir = defineOutputDirectory(texFile);
      Path enrichedFile = Paths.get(outputDir.toString(), filename);

      return enrichedFile.toAbsolutePath();
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

      return pdfFile.toAbsolutePath();
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

      return syncTexFile.toAbsolutePath();
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
