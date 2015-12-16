import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.freiburg.iif.paths.PathsUtil;
import de.freiburg.iif.text.StringUtil;
import interpret.TeXHierarchy;
import interpret.TeXInterpreter;
import model.Document;
import parse.TeXParser;
import preprocess.TeXPreprocessor;

/**
 * The main class to extract specific features from tex files.
 *
 * @author Claudius Korzen
 */
public class GroundtruthMakerMain {
  /**
   * The main method to start this program.
   */
  public static void main(String[] args) {
    // Define some options.
    Options options = new Options();

    // Define an option for the input file / input directory.
    options.addOption(Option.builder("i")
        .longOpt("input")
        .hasArg()
        .required()
        .desc("The input file/directory.")
        .build());

    // Define an option for the output file / output directory.
    options.addOption(Option.builder("o")
        .longOpt("output")
        .hasArg()
        .desc("The output file/directory.")
        .build());

    // Define an option for the prefix to use on parsing the input directory.
    options.addOption(Option.builder("p")
        .longOpt("prefix")
        .hasArg()
        .desc("The prefix of input files to consider on parsing.")
        .build());

    // Define an option for the suffix to append to the generated output files.
    options.addOption(Option.builder("s")
        .longOpt("suffix")
        .hasArg()
        .desc("The suffix to use on generating the output files.")
        .build());

    // Define an option for the features to output.
    options.addOption(Option.builder("f")
        .longOpt("feature")
        .hasArg()
        .numberOfArgs(Option.UNLIMITED_VALUES)
        .desc("The feature to output.")
        .build());

    // Define an option to display the help.
    options.addOption(Option.builder("h")
        .longOpt("help")
        .desc("Prints this help.")
        .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;

    try {
      // Parse the command line.
      cmd = parser.parse(options, args);
    } catch (Exception e) {
      printUsage(options);
      return;
    }

    // Print the usage, if the option has the "h" flag.
    if (cmd.hasOption("h")) {
      printUsage(options);
      return;
    }

    // Instantiate the program.
    GroundtruthMakerMain main = new GroundtruthMakerMain();

    if (cmd.hasOption("i")) {
      main.inputPath = cmd.getOptionValue("i");
    }

    if (cmd.hasOption("o")) {
      main.outputPath = cmd.getOptionValue("o");
    }

    if (cmd.hasOption("f")) {
      main.features.addAll(Arrays.asList(cmd.getOptionValues("f")));
    }

    if (cmd.hasOption("p")) {
      main.inputPrefix = cmd.getOptionValue("p");
    }

    if (cmd.hasOption("s")) {
      main.outputSuffix = cmd.getOptionValue("s");
    }

    main.process();
  }
  
  // ___________________________________________________________________________
  
  /**
   * The list of file extensions to consider on scanning input directories.
   */
  protected static final List<String> PROCESS_FILE_EXTENSIONS =
      Arrays.asList("tex");

  /**
   * The file extension for intermediate files (produced on preprocessing step).
   */
  protected static final List<String> PREPROCESS_FILE_EXTENSIONS =
      Arrays.asList(".resolved.tex");

  /**
   * The input defined by the user, as string.
   */
  protected String inputPath;

  /**
   * The prefix to consider on parsing the input directory for input files.
   */
  protected String inputPrefix = "";

  /**
   * The resolved input directory.
   */
  protected Path inputDirectory;

  /**
   * The resolved input files to process.
   */
  protected List<Path> inputFiles = new ArrayList<>();

  /**
   * The output defined by the user, as string.
   */
  protected String outputPath;

  /**
   * The suffix to append to the output files to generate.
   */
  protected String outputSuffix = ".txt";

  /**
   * The output file (only set, if the input is a file).
   */
  protected Path outputFile;

  /**
   * The output directory.
   */
  protected Path outputDirectory;

  /**
   * The features to extract.
   */
  protected List<String> features;

  /**
   * The default features to extract.
   */
  protected List<String> defaultFeatures;

  /**
   * Some predefined feature profiles.
   */
  protected Map<String, List<String>> featureProfiles = new HashMap<>();

  // ___________________________________________________________________________

  /**
   * Creates a new instance of GroundTruthMaker.
   */
  public GroundtruthMakerMain() {
    this.features = new ArrayList<>();
    this.defaultFeatures = Arrays.asList("document", "text");
    this.featureProfiles = new HashMap<>();
    this.featureProfiles
      .put("body", Arrays.asList("sections", "subsections", "subsubsections"));
  }

  /**
   * Processes the given input.
   */
  public void process() {
    validateInput();
    
    System.out.println("Selected features: " + getFeatures());
    
    processTexFiles();
  }

  /**
   * Validates the input given by the user. Resolves the input directory / files
   * as well as the output directory / file.
   */
  protected void validateInput() {
    if (inputPath == null || inputPath.trim().isEmpty()) {
      throw new IllegalArgumentException("No input given.");
    }

    Path input = Paths.get(inputPath);
    Path output = outputPath != null ? Paths.get(outputPath) : null;

    // Check, if the input path exists.
    if (!Files.exists(input)) {
      throw new IllegalArgumentException("The given input doesn't exist.");
    }

    // Check, if the input path is readable.
    if (!Files.isReadable(input)) {
      throw new IllegalArgumentException("The given input can't be read.");
    }

    // Check, if the input path is a directory or a file.
    if (Files.isRegularFile(input)) {
      // The input is a single file.
      // Set the input directory to its parent.
      this.inputDirectory = input.getParent();
      // Add the file to the files to process.
      this.inputFiles.add(input);
      
      // Check, if there is an output given.
      if (output != null) {
        if (Files.isDirectory(output)) {
          // The output is an existing directory.
          this.outputDirectory = output;
        } else {
          // The output is an existing file OR the output doesn't exist.
          // If the output doesn't exist, interpret the output as a file 
          // (because the input is a file).
          this.outputFile = output;
          this.outputDirectory = output.getParent();
        }
      }
    } else if (Files.isDirectory(input)) {
      // The input is an existing directory.
      this.inputDirectory = input;
      // Read the directory recursively to get all files to process.
      readDirectory(input, this.inputFiles);
      
      // Check, if there is an output given.
      if (output != null) {
        if (Files.isRegularFile(output)) {
          // The output is an existing file, but the input is a directory.
          throw new IllegalArgumentException(
              "An input directory can't be serialized to a file.");
        } else {
          // The output is an existing directory OR doesn't exist.
          // If the output doesn't exist, interpret the output as a directory, 
          // (because the input is a directory).
          this.outputDirectory = output;
        }
      }
    } else {
      // The input is neither a file nor a directory. WTF?
      throw new IllegalArgumentException("The input isn't a file/directory.");
    }
  }

  /**
   * Processes the list of resolved tex files.
   */
  protected void processTexFiles() {
    for (Path file : this.inputFiles) {
      processTexFile(file);
    }
  }

  /**
   * Processes the given tex file. The pipeline on processing is as follows: (1)
   * Preprocess the tex file (resolve all cross references) and store the result
   * in an intermediate file. (2) Parse the intermediate file into a Document
   * object. (3) Interpret the Document object to get the elements of interest.
   * (4) Serialize the selected features to file.
   */
  protected void processTexFile(Path file) {
    // Obtain the target file.
    Path target = getTargetFile(file);
    // Obtain the features to extract.
    List<String> features = getFeatures();

    System.out.print("Processing \"" + file.getFileName().toString() + "\"");
    System.out.println(" -> " + target);
    
    try {
      // Preprocess the tex file (resolve the cross references).
      Path resolvedFile = preprocessTexFile(file);
      // Parse the resolved file.
      Document texDocument = parseTexFile(resolvedFile);
      // Interpret the parsed document.
      TeXHierarchy hierarchy = interpretTexDocument(texDocument);
      // Serialize the selected features to file.
      serialize(hierarchy, features, target);
    } catch (Exception e) {
      System.out.println("Error on processing: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // ___________________________________________________________________________

  /**
   * (1) Preprocesses the given tex file and returns the path to the produced
   * intermediate file.
   */
  protected Path preprocessTexFile(Path texFile) throws Exception {
    InputStream input = Files.newInputStream(texFile);
    // Obtain the target file for the preprocessing step.
    Path output = getPreprocessTargetFile(texFile);

    TeXPreprocessor preprocessor = new TeXPreprocessor(input);
    preprocessor.preprocess(output);
    input.close();

    return output;
  }

  /**
   * (2) Processes the intermediate file produced by the preprocessor and
   * returns the parsed Document object.
   */
  protected Document parseTexFile(Path texFile) throws Exception {
    InputStream input = Files.newInputStream(texFile);

    TeXParser parser = new TeXParser(input);
    Document document = parser.parse();
    input.close();

    return document;
  }

  /**
   * (3) Interprets the given tex document to get the elements of interest.
   */
  protected TeXHierarchy interpretTexDocument(Document doc) throws Exception {
    return new TeXInterpreter().interpret(doc);
  }

  /**
   * (4) Serializes the selected features to file.
   */
  protected void serialize(TeXHierarchy gt, List<String> features, Path file)
    throws IOException {
    // Create the file, if it doesn't exist yet.
    if (!Files.exists(file)) {
      Files.createDirectories(file.getParent());
      Files.createFile(file);
    }

    BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
    serializeObject(gt, features, w);
    w.close();
  }

  /**
   * Serializes the given object to the given writer.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void serializeObject(Object obj, List<String> features, Writer w)
    throws IOException {
    if (obj == null) {
      return;
    }

    if (obj instanceof TeXHierarchy) {
      serializeTeXHierarchy((TeXHierarchy) obj, features, w);
    } else if (obj instanceof List) {
      serializeList((List) obj, features, w);
    } else {
      serializeString(obj.toString(), w);
    }
  }

  /**
   * Serializes the given hierarchy.
   */
  protected void serializeTeXHierarchy(TeXHierarchy hierarchy,
      List<String> features, Writer w) throws IOException {
    if (hierarchy == null) {
      return;
    }

    for (String key : hierarchy.keys()) {
      // Serialize the associated value, if the related feature is selected or
      // there are no special features are defined. 
      if (features == null || features.contains(key)) {
        serializeObject(hierarchy.get(key), features, w);
      }
    }
  }

  /**
   * Serializes the given list of objects.
   */
  protected void serializeList(List<Object> list, List<String> features,
      Writer w) throws IOException {
    if (list == null) {
      return;
    }

    for (Object object : list) {
      serializeObject(object, features, w);
    }
  }

  /**
   * Serializes the given string.
   */
  protected void serializeString(String text, Writer w) throws IOException {
    if (text == null) {
      return;
    }
    w.write(text);
    w.write("\n\n");
  }

  // ___________________________________________________________________________
  // Some util methods.

  /**
   * Obtains the path to the target file.
   */
  protected Path getTargetFile(Path texFile) {
    if (texFile == null) {
      return null;
    }

    if (outputFile != null) {
      // If there is a output file defined explicitly, return it.
      return outputFile;
    }

    // Obtain the basename of the parent directory.
    String basename = PathsUtil.getBasename(texFile.getParent());

    Path targetDir = getTargetDir(texFile);
    if (targetDir != null) {
      return targetDir.resolve(basename + outputSuffix);
    }
    return null;
  }

  /**
   * Obtains the path to the target directory.
   */
  protected Path getTargetDir(Path texFile) {
    if (outputDirectory != null) {
      // Obtain the parent directory of the tex file.
      Path parentDirectory = texFile.getParent();
      // Obtain the parent directory of the parent directory.
      Path parentParentDirectory = parentDirectory.getParent();
      // Obtain the path of the texFile relative to the global input path.
      Path relativePath = inputDirectory.relativize(parentParentDirectory);

      return outputDirectory.resolve(relativePath);
    }

    return null;
  }

  /**
   * Obtains the path to the target file for the preprocessing step.
   */
  protected Path getPreprocessTargetFile(Path texFile) {
    // Obtain the basename of the file.
    String basename = PathsUtil.getBasename(texFile);
    // Obtain the filename for the target file.
    String filename = basename + PREPROCESS_FILE_EXTENSIONS.get(0);
    Path targetDir = getPreprocessTargetDir(texFile);
    if (targetDir != null) {
      return targetDir.resolve(filename);
    }
    return null;
  }

  /**
   * Obtains the path to the target directory for the preprocessing step.
   */
  protected Path getPreprocessTargetDir(Path texFile) {
    if (outputDirectory != null) {
      // Compute the parent directory of the tex file.
      Path parentDirectory = texFile.getParent();
      // Compute the path of the directory, relative to the input directory.
      Path relativePath = inputDirectory.relativize(parentDirectory);

      return outputDirectory.resolve(relativePath);
    }
    return null;
  }

  // ___________________________________________________________________________

  /**
   * Returns the list of all selected features to extract. If there are no
   * features selected, this method returns null.
   */
  protected List<String> getFeatures() {
    if (features.isEmpty()) {
      return null;
    }
    
    // Prepopulate the features with the default ones.
    List<String> allFeatures = new ArrayList<>(defaultFeatures);
    for (String feature : features) {
      if (featureProfiles.containsKey(feature)) {
        // The feature is a profile name, add all features of the profile.
        allFeatures.addAll(featureProfiles.get(feature));
      } else {
        // Add the single feature.
        allFeatures.add(feature);
      }
    }
    return allFeatures;
  }
  
  /**
   * Reads the given directory recursively and fills the given list with
   * found tex files.
   */
  protected void readDirectory(Path directory, List<Path> res) {
    DirectoryStream<Path> ds = null;

    try {
      // Process all elements in the directory.
      ds = Files.newDirectoryStream(directory);
      Iterator<Path> itr = ds.iterator();
      while (itr.hasNext()) {
        Path next = itr.next();
        if (Files.isDirectory(next)) {
          readDirectory(next, res);
        } else if (considerPath(next)) {
          res.add(next);
        }
      }
      ds.close();
    } catch (Exception e) {
      System.out.println("Error on reading directory " + e.getMessage());
      e.printStackTrace();
      return;
    } finally {
      try {
        if (ds != null) {
          ds.close();
        }
      } catch (Exception e) {
        return;
      }
    }
  }

  /**
   * Returns true, if we have to consider the given file. False otherwise.
   */
  protected boolean considerPath(Path file) {
    if (file == null) {
      return false;
    }

    if (Files.isDirectory(file)) {
      return false;
    }

    if (!Files.isReadable(file)) {
      return false;
    }

    // Obtain the name of file's parent directory.
    String dirName = file.getParent().getFileName().toString();
    // Obtain the name of the file.
    String fileName = file.getFileName().toString().toLowerCase();

    // Process only those files, which are located in a directory, which
    // contains the given prefix.
    if (StringUtil.startsWith(dirName, inputPrefix)) {
      // Furthermore, process only those files, which end with one of the
      // predefined extension, but don't end with a file extension of a
      // preprocessing file.
      if (StringUtil.endsWith(fileName, PROCESS_FILE_EXTENSIONS)
          && !StringUtil.endsWith(fileName, PREPROCESS_FILE_EXTENSIONS)) {
        return true;
      }
    }
    return false;
  }

  // ___________________________________________________________________________

  /**
   * Prints the usage.
   */
  protected static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar GroundTruthMakerMain.jar", options);
  }
}
