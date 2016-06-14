import static de.freiburg.iif.affirm.Affirm.affirm;
import static model.TeXParagraphParserConstants.TEX_EXTENSIONS;
import static model.TeXParagraphParserConstants.TMP_TEX_EXTENSIONS;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.freiburg.iif.path.PathUtils;
import de.freiburg.iif.text.StringUtils;
import identifier.PdfParagraphsIdentifier;
import identifier.TeXParagraphsIdentifier;
import model.TeXFile;
import serializer.TeXParagraphSerializer;
import visualizer.TeXParagraphVisualizer;

/**
 * Class to identify text paragraphs in tex files. This class is able to 
 * identify the text of each paragraph from a given tex file, as well as the
 * start and end line of the paragraphs.
 * 
 * Experimental: As an experimental feature, this class is also able to 
 * identify the coordinates and page numbers of the related areas in pdf file.
 *
 * @author Claudius Korzen
 */
public class TeXParagraphParserMain {
  /**
   * The input defined by the user, as string.
   */
  protected String input;

  /**
   * The prefix to consider on parsing the input directory for input files.
   */
  protected String inputPrefix;
  
  /**
   * The serialization path defined by the user, as string.
   */
  protected String serialization;

  /**
   * The visualization path defined by the user, as string.
   */
  protected String visualization;
  
  /**
   * The features to extract.
   */
  protected List<String> features;
    
  /**
   * Flag that indicates if we have to identify the bounding boxes.
   */
  protected boolean identifyPdfParagraphs;
  
  /**
   * The default features to extract.
   */
  protected List<String> defaultFeatures;

  /**
   * Some predefined feature profiles.
   */
  protected Map<String, List<String>> featureProfiles;
  
  /**
   * The resolved input directory.
   */
  protected Path inputDirectory;

  /**
   * The resolved input files to process.
   */
  protected List<Path> inputFiles;

  /**
   * The serialization file (only set, if the input is a file).
   */
  protected Path serializationFile;

  /**
   * The serialization directory.
   */
  protected Path serializationDirectory;
  
  /**
   * The visualization file (only set, if the input is a file).
   */
  protected Path visualizationFile;

  /**
   * The visualization directory.
   */
  protected Path visualizationDirectory;

  /**
   * The main method to start the identifcation of paragraphs.
   */
  public static void main(String[] args) {
    // Create command line options.
    Options options = buildOptions();

    // Try to parse the given command line arguments.
    CommandLine cmd = null;
    try {
      cmd = parseCommandLine(args, options);
    } catch (ParseException e) {
      printUsage(options);
      System.exit(1);
    }

    // Print usage if 'cmd' contains the help option.
    if (hasOption(cmd, TexParagraphParserOptions.HELP)) {
      printUsage(options);
      System.exit(0);
    }

    // Run the program.
    try {
      new TeXParagraphParserMain(cmd).run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates new paragraph parser based on the given command line object.
   * 
   * TODO: Define default feature and feature profiles.
   */
  public TeXParagraphParserMain(CommandLine cmd) {
    inputFiles = new ArrayList<>();
    
    input = getOptionValue(cmd, TexParagraphParserOptions.INPUT, null);
    serialization = getOptionValue(cmd, TexParagraphParserOptions.OUTPUT, null);
    visualization = getOptionValue(cmd, TexParagraphParserOptions.VISUALIZE, null);
    inputPrefix = getOptionValue(cmd, TexParagraphParserOptions.PREFIX, "");
    features = getOptionValues(cmd, TexParagraphParserOptions.FEATURE, null);
    identifyPdfParagraphs = hasOption(cmd, TexParagraphParserOptions.BOUNDING_BOXES);
  }

  /**
   * Runs this program.
   */
  public void run() throws IOException {
    // Initialize the paragraph parser. 
    initialize();
    // Process the tex files.
    processTexFiles();
  }

  // ===========================================================================
  
  /**
   * Validates the input given by the user and resolves the input directory / 
   * files as well as the output directory / file.
   */
  protected void initialize() {
    affirm(input != null, "No input given.");
    affirm(!input.trim().isEmpty(), "No input given.");

    Path inPath = Paths.get(input).toAbsolutePath();
    Path serPath = serialization != null ? Paths.get(serialization).toAbsolutePath() : null;
    Path visPath = visualization != null ? Paths.get(visualization).toAbsolutePath() : null;

    // Check, if the input path exists.
    affirm(Files.exists(inPath), "The given input doesn't exist.");
    affirm(Files.isReadable(inPath), "The given input can't be read.");
    
    // Check, if the input path is a directory or a file.
    if (Files.isRegularFile(inPath)) {
      // The input is a single file.
      // Set the input directory to its parent.
      this.inputDirectory = inPath.getParent();
      // Add the file to the files to process.
      this.inputFiles.add(inPath);

      // Check, if there is an serialiation path given.
      if (serPath != null) {
        if (Files.isDirectory(serPath)) {
          // The output is an existing directory.
          this.serializationDirectory = serPath;
        } else {
          // The output is an existing file OR the output doesn't exist.
          // If the output doesn't exist, interpret the output as a file
          // (because the input is a file).
          this.serializationFile = serPath;
          this.serializationDirectory = serPath.getParent();
        }
      }
      
      // Check, if there is an visualization path given.
      if (visPath != null) {
        if (Files.isDirectory(visPath)) {
          // The output is an existing directory.
          this.visualizationDirectory = visPath;
        } else {
          // The output is an existing file OR the output doesn't exist.
          // If the output doesn't exist, interpret the output as a file
          // (because the input is a file).
          this.visualizationFile = visPath;
          this.visualizationDirectory = visPath.getParent();
        }
      }
    } else if (Files.isDirectory(inPath)) {
      // The input is an existing directory.
      this.inputDirectory = inPath;
      // Read the directory recursively to get all files to process.
      readDirectory(inPath, this.inputFiles);

      // Check, if there is an output given.
      if (serPath != null) {
        affirm(!Files.isRegularFile(serPath),
            "An input directory can't be serialized to a file.");
        // The output is an existing directory OR doesn't exist.
        // If the output doesn't exist, interpret the output as a directory,
        // (because the input is a directory).
        this.serializationDirectory = serPath;
      }
      
      // Check, if there is an visualization path given.
      if (visPath != null) {
        affirm(!Files.isRegularFile(visPath),
            "An input directory can't be visualized to a file.");
        // The output is an existing directory OR doesn't exist.
        // If the output doesn't exist, interpret the output as a directory,
        // (because the input is a directory).
        this.visualizationDirectory = visPath;
      }
    }
  }

  /**
   * Processes the parsed tex files.
   */
  protected void processTexFiles() throws IOException {
    for (Path file : this.inputFiles) {
      processTexFile(file);
    }
  }

  /**
   * Processes the given tex file. Identifies the paragraphs in the given tex
   * file and serializes them to file. 
   */
  protected void processTexFile(Path file) throws IOException {
    TeXFile texFile = new TeXFile(file);

    // Identify the paragraphs in the given tex file.
    identifyTexParagraphs(texFile);
        
    if (this.identifyPdfParagraphs) {
      identifyPdfParagraphs(texFile);
    }

    Path serializationTargetFile = defineSerializationTargetFile(texFile);
    Path visualizationTargetFile = defineVisualizationTargetFile(texFile);
    
    if (serializationTargetFile != null) {
      serialize(texFile, serializationTargetFile);
    }
    
    if (visualizationTargetFile != null) {
      visualize(texFile, visualizationTargetFile);
    }
  }

  // ---------------------------------------------------------------------------

  /**
   * Identifies the tex paragraphs from given tex file.
   */
  protected void identifyTexParagraphs(TeXFile texFile) throws IOException {
    new TeXParagraphsIdentifier(texFile).identify();
  }

  /**
   * Identifies the pdf paragraphs for the tex paragraphs in the given tex file.
   */
  protected void identifyPdfParagraphs(TeXFile texFile) throws IOException {
    new PdfParagraphsIdentifier(texFile).identify();
  }

  /**
   * Serializes the selected features to file.
   * 
   * TODO: Take the chosen features into account.
   */
  protected void serialize(TeXFile texFile, Path target) throws IOException {
    new TeXParagraphSerializer(texFile).serialize(target);
  }

  /**
   * Visualizes the selected features to file.
   * 
   * TODO: Take the chosen features into account.
   */
  protected void visualize(TeXFile texFile, Path target) throws IOException {
    new TeXParagraphVisualizer(texFile).visualize(target);
  }
  
  // ---------------------------------------------------------------------------
  // Some util methods.

  /**
   * Obtains the path to the target file.
   */
  protected Path defineSerializationTargetFile(TeXFile texFile) {
    if (texFile == null) {
      return null;
    }

    if (serializationFile != null) {
      // If there is a output file defined explicitly, return it.
      return serializationFile;
    }

    // Obtain the basename of the parent directory.
    String basename = PathUtils.getBasename(texFile.getPath().getParent());

    Path targetDir = defineSerializationTargetDir(texFile);
    if (targetDir != null) {
      return targetDir.resolve(basename + ".txt");
    }
    return null;
  }

  /**
   * Obtains the path to the target directory.
   */
  protected Path defineSerializationTargetDir(TeXFile texFile) {
    if (serializationDirectory != null) {
      // Obtain the parent directory of the tex file.
      Path parentDirectory = texFile.getPath().getParent();
      // Obtain the parent directory of the parent directory.
      Path parentParentDirectory = parentDirectory.getParent();
      // Obtain the path of the texFile relative to the global input path.
      Path relativePath = inputDirectory.relativize(parentParentDirectory);

      return serializationDirectory.resolve(relativePath);
    }

    return null;
  }

  /**
   * Defines the path to the visualization file.
   */
  protected Path defineVisualizationTargetFile(TeXFile texFile) {
    if (texFile == null) {
      return null;
    }

    if (visualizationFile != null) {
      // If there is a output file defined explicitly, return it.
      return visualizationFile;
    }

    // Obtain the basename of the parent directory.
    String basename = PathUtils.getBasename(texFile.getPath().getParent());

    Path targetDir = defineVisualizationTargetDir(texFile);
    if (targetDir != null) {
      return targetDir.resolve(basename + ".vis.pdf");
    }
    return null;
  }

  /**
   * Defines the path to the visualization directory.
   */
  protected Path defineVisualizationTargetDir(TeXFile texFile) {
    if (visualizationDirectory != null) {
      // Obtain the parent directory of the tex file.
      Path parentDirectory = texFile.getPath().getParent();
      // Obtain the parent directory of the parent directory.
      Path parentParentDirectory = parentDirectory.getParent();
      // Obtain the path of the texFile relative to the global input path.
      Path relativePath = inputDirectory.relativize(parentParentDirectory);

      return visualizationDirectory.resolve(relativePath);
    }

    return null;
  }
  
  // ---------------------------------------------------------------------------

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
   * Reads the given directory recursively and fills the given list with found
   * tex files.
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
    if (StringUtils.startsWith(dirName, inputPrefix)) {
      // Furthermore, process only those files, which end with one of the
      // predefined extension, but don't end with a file extension of a
      // preprocessing file.
      if (StringUtils.endsWith(fileName, TEX_EXTENSIONS)
          && !StringUtils.endsWith(fileName, TMP_TEX_EXTENSIONS)) {
        return true;
      }
    }
    return false;
  }

  // ===========================================================================
  // Methods and class related to options.

  /**
   * Builds and returns the command line options.
   */
  protected static Options buildOptions() {
    // Define some options.
    Options options = new Options();

    TexParagraphParserOptions[] opts = TexParagraphParserOptions.values();
    for (TexParagraphParserOptions opt : opts) {
      Builder builder = Option.builder(opt.shortOpt);
      builder.longOpt(opt.longOpt);
      builder.desc(opt.description);
      builder.required(opt.required);
      builder.hasArg(opt.hasArg);
      builder.numberOfArgs(opt.numArgs);

      options.addOption(builder.build());
    }

    return options;
  }

  /**
   * Parses the command line options.
   */
  protected static CommandLine parseCommandLine(String[] args, Options opts)
    throws ParseException {
    return new DefaultParser().parse(opts, args);
  }

  /**
   * Prints the usage.
   */
  protected static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar GroundTruthMakerMain.jar", options);
  }

  /**
   * Returns true, if the given command line contains the given option.
   */
  protected static boolean hasOption(CommandLine cmd,
      TexParagraphParserOptions option) {
    return cmd != null && cmd.hasOption(option.shortOpt);
  }

  /**
   * Returns the value associated with given option as string. Returns the given
   * default value if the given command line doesn't contain the option.
   */
  protected static String getOptionValue(CommandLine cmd,
      TexParagraphParserOptions option, String defaultValue) {
    if (cmd != null) {
      return cmd.getOptionValue(option.shortOpt, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Returns the value associated with given option as string. Returns the given
   * default value if the given command line doesn't contain the option.
   */
  protected static List<String> getOptionValues(CommandLine cmd,
      TexParagraphParserOptions option, List<String> defaultValue) {
    if (cmd != null) {
      String[] values = cmd.getOptionValues(option.shortOpt);
      if (values != null) {
        return Arrays.asList(values);
      }
    }
    return defaultValue;
  }

  /**
   * Enumeration of all command line options.
   */
  public enum TexParagraphParserOptions {
    /**
     * Create option to define the input file / directory.
     */
    INPUT("i", "input", "The input file/directory.", true, true),

    /**
     * Create option to define the output file / directory.
     */
    OUTPUT("o", "output", "The output file/directory.", true, true),

    /**
     * Create option to define the path to visualization file / directory.
     */
    VISUALIZE("v", "visualize", "The visualization file/directory.", false, true),
    
    /**
     * Create option to define the prefix(es) to consider on parsing the input
     * directory.
     */
    PREFIX("p", "prefix",
        "The prefix(es) to consider on parsing the input directory.",
        false, true, Option.UNLIMITED_VALUES),

    /**
     * Create option to define feature(s) to output.
     */
    FEATURE("f", "feature",
        "The feature(s) to output.",
        false, true, Option.UNLIMITED_VALUES),

    /**
     * Create option to enable the identification of paragraphs bounding boxes.
     */
    BOUNDING_BOXES("bb", "boundingboxes",
        "Enable the identification of paragraphs bounding boxes.",
        false),

    /**
     * Create option to enable the identification of paragraphs bounding boxes.
     */
    HELP("h", "help", "Prints the help.", false);

    /** The short identifier for this option. */
    public String shortOpt;

    /** The long identifier for this option. */
    public String longOpt;

    /** The description for this option. */
    public String description;

    /** The flag to indicate whether this option is required. */
    public boolean required;

    /** The flag to indicate whether this option has arguments. */
    public boolean hasArg;

    /** The number of arguments of this option. */
    public int numArgs;

    /**
     * Creates a new option with given arguments.
     */
    TexParagraphParserOptions(String opt, String longOpt, String description) {
      this(opt, longOpt, description, false);
    }

    /**
     * Creates a new option with given arguments.
     */
    TexParagraphParserOptions(String opt, String longOpt, String description,
        boolean required) {
      this(opt, longOpt, description, required, false);
    }

    /**
     * Creates a new option with given arguments.
     */
    TexParagraphParserOptions(String opt, String longOpt, String description,
        boolean required, boolean hasArg) {
      this(opt, longOpt, description, required, hasArg, required ? 1 : 0);
    }

    /**
     * Creates a new option with given arguments.
     */
    TexParagraphParserOptions(String opt, String longOpt, String description,
        boolean required, boolean hasArg, int numArgs) {
      this.shortOpt = opt;
      this.longOpt = longOpt;
      this.description = description;
      this.required = required;
      this.hasArg = hasArg;
      this.numArgs = numArgs;
    }
  }
}
