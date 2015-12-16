import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the TeXToGroundtruth class.
 *
 * @author Claudius Korzen
 *
 */
public class GroundtruthMakerMainTest {
  /**
   * Test some input files.
   */
  @Test
  public void testFiles() throws IOException {
    testFile(1);
//    testFile(2);
//    testFile(3);
  }
  
  /**
   * Test file i.
   */
  public void testFile(int i) throws IOException {
    String inputPath = "src/test/resources/main-input-" + i + ".tex";
    String outputPath = "src/test/resources/main-output-" + i + ".txt";
    String gtPath = "src/test/resources/main-groundtruth-" + i + ".json";
    
    GroundtruthMakerMain program = new GroundtruthMakerMain();
    program.inputPath = inputPath;
    program.outputPath = outputPath;
    program.process();
    
    Path output = Paths.get(outputPath);
    Path groundtruth = Paths.get(gtPath);
    
    String groundtruthStr = new String(Files.readAllBytes(groundtruth));
    String outputStr = new String(Files.readAllBytes(output));
        
    Assert.assertEquals(groundtruthStr, outputStr);
  }
}
