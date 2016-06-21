import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import de.freiburg.iif.path.PathUtils;



/**
 * Tests for the paragraph parser.
 * 
 * @author Claudius Korzen
 *
 */
public class TeXParagraphParserTest {
  
  @Test
  public void testFile1() throws IOException {
    testFile("input1.tex", "output1.tsv", "vis1.pdf", "expected1.tsv");
  }
  
  @Test
  public void testFile2() throws IOException {
    testFile("input2.tex", "output2.tsv", "vis2.pdf", "expected2.tsv");
  }
  
  @Test
  public void testFile3() throws IOException {
    testFile("input3.tex", "output3.tsv", "vis3.pdf", "expected3.tsv");
  }
  
  /**
   * Tests the given file.
   */
  protected void testFile(String inputPath, String outputPath, 
      String visualizationPath, String expectedPath) throws IOException {
    Path input = getPath(inputPath);
    Path output = getPath(outputPath);
    Path visualization = getPath(visualizationPath);
    Path expected = getPath(expectedPath);
    
    TeXParagraphParserMain main = new TeXParagraphParserMain(null);
    main.input = input.toString();    
    main.serialization = output.toString();
    main.visualization = visualization.toString();
    main.identifyPdfParagraphs = true;
    main.run();
    
    String outputContent = PathUtils.readPathContentToString(output);
    String expectedContent = PathUtils.readPathContentToString(expected);
    
    Assert.assertEquals(outputContent, expectedContent);
  }
  
  /**
   * Returns the path to the file given by name.
   */
  protected Path getPath(String filename) {
    Path parent = Paths.get(".", "/src/test/resources/");
    return parent.resolve(filename).toAbsolutePath();
  }
}
