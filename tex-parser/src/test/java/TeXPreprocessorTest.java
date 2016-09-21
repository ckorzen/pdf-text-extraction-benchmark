import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import parse.ParseException;
import preprocess.TeXPreprocessor;

/**
 * Tests for TeXPreprocessor.
 *
 * @author Claudius Korzen
 *
 */
public class TeXPreprocessorTest {

  /**
   * Tests the TeXPreprocessor. 
   */
  @Test
  public void test1() throws IOException, ParseException {
    Path inputFile = Paths.get("src/test/resources/preprocessor-input-1.tex");
    Path outputPath = Paths.get("src/test/resources/");
    Path outputFile = outputPath.resolve("preprocessor-output-1.tex");
    Path groundtruthFile = outputPath.resolve("preprocessor-groundtruth-1.tex");
    
    // Preprocess the input file.
    try (InputStream inputStream = Files.newInputStream(inputFile)) {
      TeXPreprocessor preprocessor = new TeXPreprocessor(inputStream);
      preprocessor.preprocess(outputFile);
    }

    // Compare the output with the groundtruth.
    String output = new String(Files.readAllBytes(outputFile));
    String groundtruth = new String(Files.readAllBytes(groundtruthFile));
    
    Assert.assertEquals(groundtruth, output);
  }
  
  /**
   * Tests the TeXPreprocessor. 
   */
  @Test
  public void test2() throws IOException, ParseException {
    Path inputFile = Paths.get("src/test/resources/preprocessor-input-2.tex");
    Path outputPath = Paths.get("src/test/resources/");
    Path outputFile = outputPath.resolve("preprocessor-output-2.tex");
    Path groundtruthFile = outputPath.resolve("preprocessor-groundtruth-2.tex");
    
    // Preprocess the input file.
    try (InputStream inputStream = Files.newInputStream(inputFile)) {
      TeXPreprocessor preprocessor = new TeXPreprocessor(inputStream);
      preprocessor.preprocess(outputFile);  
    }

    // Compare the output with the groundtruth.
    String output = new String(Files.readAllBytes(outputFile));
    String groundtruth = new String(Files.readAllBytes(groundtruthFile));
    
    Assert.assertEquals(groundtruth, output);
  }
}
