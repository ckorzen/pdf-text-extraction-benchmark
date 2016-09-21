import static de.freiburg.iif.affirm.Affirm.affirm;
import static model.TeXParagraphParserSettings.TEX_EXTENSIONS;
import static model.TeXParagraphParserSettings.TMP_TEX_EXTENSIONS;
import static model.TeXParagraphParserSettings.getRoleProfiles;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import serializer.TeXParagraphTsvSerializer;
import serializer.TeXParagraphTxtSerializer;
import visualizer.TeXParagraphVisualizer;

/**
 * Class to identify paragraphs in tex files. It identifies the textual content
 * of each paragraph and obtains the related line numbers in tex file.
 * 
 * Experimental: As an experimental feature, this class is also able to 
 * identify the position (coordinates and page number) of each paragraph
 * in related pdf file.
 *
 * @author Claudius Korzen
 */
public class TeXParagraphParserMain {
  /**
   * The input as defined by the user, as string. May be a path to a tex file
   * or a path to a directory containing tex files.
   */
  protected String input;

  /**
   * The prefix to consider on parsing the input directory for input files.
   */
  protected List<String> inputPrefixes;

  /**
   * The suffix to use on creating serialization target file.
   */
  protected String serialFileSuffix;
  
  /**
   * The path to serialization file as defined by the user, as string. It 
   * denotes the file where to store the paragraphs in a serialized form.
   * May be a path to a directory or a file.
   */
  protected String serialization;

  /**
   * The path to visualization file as defined by the user, as string. It 
   * denotes the path to a pdf file where the paragraphs are visualized by
   * their bounding boxes. Is only used when the computation of positions of 
   * the paragraphs is enabled. May be a path to a directory or a pdf file.
   */
  protected String visualization;

  /**
   * The path to the texmf dir. On computing positions of paragraphs, we need 
   * to compile the tex files to pdf files. But the tex files may depend on 
   * some system-unknown documentstyles (e.g. "revtex"). 
   * To be able to compile such tex files, one can define this path to a 
   * directory where the related sty files, cls files etc. can be found. 
   */
  protected List<String> texmfPaths = Arrays.asList(
      PathUtils.getWorkingDirectory(getClass()) + "/classes/texmf");

  /**
   * Flag that indicates if we have to identify the positions of paragraphs
   * from pdf.
   */
  protected boolean identifyPdfParagraphs;

  /**
   * The input directory, resolved from users input. If the input is a file,
   * this path denotes the parent directory. If the input is a directory, this
   * path denotes exactly this directory. 
   */
  protected Path inputDirectory;

  /**
   * The list of resolved input files to process. If the input is a file, the 
   * list consists of this single file. If the input is a directory, the lists
   * consists of all tex files found in the directory.
   */
  protected List<Path> inputFiles;

  /**
   * The serialization file. Is only set, if the input is a file.
   */
  protected Path serializationFile;

  /**
   * The serialization directory, resolved from users serialization path. If 
   * the path is a file, 'serializationDirectory' denotes its parent 
   * directory. If the serialization path is a directory, 
   * 'serializationDirectory' denotes exactly this directory. 
   */
  protected Path serializationDirectory;

  /**
   * The visualization file. Is only set, if the input is a file.
   */
  protected Path visualizationFile;

  /**
   * The visualization directory, resolved from users visualization path. If the
   * path is a file, 'visualization' denotes its parent directory. 
   * If the visualization path is a directory, 'visualizationDirectory' denotes 
   * exactly this directory. 
   */
  protected Path visualizationDirectory;

  /**
   * Flag to indicate whether we have to serialize only the texts of paragraphs 
   * into text file.
   */
  protected boolean isPlainSerialization;
  
  /**
   * The roles to consider on serialization.
   */
  protected List<String> roles;
  
  /**
   * The main method to start the paragraphs parser.
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
    if (hasOption(cmd, TeXParserOptions.HELP)) {
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
   * Creates a new paragraph parser based on the given command line object.
   */
  public TeXParagraphParserMain(CommandLine cmd) {
    inputFiles = new ArrayList<>();

    input = getOptionValue(cmd, TeXParserOptions.INPUT, null);
    serialization = getOptionValue(cmd, TeXParserOptions.OUTPUT, null);
    visualization = getOptionValue(cmd, TeXParserOptions.VISUALIZE, null);
    inputPrefixes = getOptionValues(cmd, TeXParserOptions.PREFIX, null);
    identifyPdfParagraphs = hasOption(cmd, TeXParserOptions.BOUNDING_BOXES);
    texmfPaths = getOptionValues(cmd, TeXParserOptions.TEXMF_PATHS, texmfPaths);
    isPlainSerialization = hasOption(cmd, TeXParserOptions.PLAIN_SERIALIZATION);
    roles = resolveRoles(getOptionValues(cmd, TeXParserOptions.ROLE, null));
    serialFileSuffix = getOptionValue(cmd, TeXParserOptions.SUFFIX, ".txt");
  }

  /**
   * Runs the paragraph parser.
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
   * files as well as the serialization directory / file and 
   * visualization directory / file.
   */
  protected void initialize() {
    affirm(input != null, "No input given.");
    affirm(!input.trim().isEmpty(), "No input given.");

    Path inPath = Paths.get(input).toAbsolutePath();
    Path serPath = serialization != null
        ? Paths.get(serialization).toAbsolutePath() : null;
    Path visPath = visualization != null
        ? Paths.get(visualization).toAbsolutePath() : null;

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
      
      // Check, if there is an serialization path given.
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
   * Processes the tex files found from users input.
   */
  protected void processTexFiles() throws IOException {
    // TODO: Handle simple timeout.
    
    ExecutorService executor = Executors.newCachedThreadPool();
    
    long start = System.currentTimeMillis();
    for (Path file : this.inputFiles) {
      TexFileProcessor worker = new TexFileProcessor(file);
      
      worker.identifyPdfParagraphs = identifyPdfParagraphs;
      worker.texmfPaths = texmfPaths;
      worker.roles = roles;
      worker.isPlainSerialization = isPlainSerialization;
      worker.inputDirectory = inputDirectory;
      worker.serializationFile = serializationFile;
      worker.serialFileSuffix = serialFileSuffix;
      worker.serializationDirectory = serializationDirectory;
      worker.visualizationFile = visualizationFile;
      worker.visualizationDirectory = visualizationDirectory;
      
      executor.execute(worker);
    }
    
    executor.shutdown();
    while (!executor.isTerminated()) {
      
    }
    long end = System.currentTimeMillis();
    
    System.out.println("Finished in " + (end - start) + "ms.");
  }

  // ---------------------------------------------------------------------------

  /**
   * Reads the given directory recursively and fills the given list with found
   * tex files.
   */
  protected void readDirectory(Path directory, List<Path> res) {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
      Iterator<Path> itr = ds.iterator();
      while (itr.hasNext()) {
        Path next = itr.next();
        
        if (considerPath(next)) {
          if (Files.isDirectory(next)) {
            readDirectory(next, res);
          } else {
            res.add(next);
          }
        }
      }
      ds.close();
    } catch (Exception e) {
      System.out.println("Error on reading directory " + e.getMessage());
      e.printStackTrace();
      return;
    }
  }

  /**
   * Returns true, if we have to consider the given file. False otherwise.
   */
  protected boolean considerPath(Path file) {
    if (file == null) {
      return false;
    }

    if (!Files.isReadable(file)) {
      return false;
    }

    if (Files.isDirectory(file)) {
      // Consider the path if no prefix(es) are given.
      if (this.inputPrefixes == null || this.inputPrefixes.isEmpty()) {
        return true;
      }
      
      String dirName = file.getFileName().toString().toLowerCase();
            
      return StringUtils.startsWith(dirName, this.inputPrefixes);
    }
    
    // Obtain the name of file's parent directory.
    String dirName = file.getParent().getFileName().toString();
    // Obtain the name of the file.
    String fileName = file.getFileName().toString().toLowerCase();
    
    // Process only those files, which end with one of the
    // predefined extension, but don't end with a file extension of a
    // preprocessing file.
    if (!StringUtils.endsWith(fileName, TEX_EXTENSIONS)
        || StringUtils.endsWith(fileName, TMP_TEX_EXTENSIONS)) {
      return false;
    }
    
    // Consider the path if no prefix(es) are given.
    if (this.inputPrefixes == null || this.inputPrefixes.isEmpty()) {
      return true;
    }
    
    // Process only those files, which are located in a directory, which
    // contains the given prefix.
    return StringUtils.startsWith(dirName, this.inputPrefixes);
  }

  /**
   * Resolves roles. The user is allowed to define roles by "profile names". A
   * profile defines a specific set of role names. 
   * 
   * Example: A profile could be "body" that defines roles like "text" and 
   * "headings".
   */
  protected List<String> resolveRoles(List<String> roles) {
    if (roles != null) {
      Map<String, List<String>> roleProfiles = getRoleProfiles();
      List<String> resolvedRoles = new ArrayList<>();
            
      for (String role : roles) {
        if (roleProfiles.containsKey(role)) {
          resolvedRoles.addAll(roleProfiles.get(role));
        } else {
          resolvedRoles.add(role);
        }
      }
      
      return resolvedRoles;
    }
    return null;
  }
  
  // ===========================================================================
  // Methods and class related to options.

  /**
   * Builds and returns the command line options.
   */
  protected static Options buildOptions() {
    // Define some options.
    Options options = new Options();

    TeXParserOptions[] opts = TeXParserOptions.values();
    for (TeXParserOptions opt : opts) {
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
      TeXParserOptions option) {
    return cmd != null && cmd.hasOption(option.shortOpt);
  }

  /**
   * Returns the value associated with given option as string. Returns the 
   * given default value if the given command line doesn't contain the option.
   */
  protected static String getOptionValue(CommandLine cmd,
      TeXParserOptions option, String defaultValue) {
    if (cmd != null) {
      return cmd.getOptionValue(option.shortOpt, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Returns the value associated with given option as string. Returns the 
   * given default value if the given command line doesn't contain the option.
   */
  protected static List<String> getOptionValues(CommandLine cmd,
      TeXParserOptions option, List<String> defaultValue) {
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
  public enum TeXParserOptions {
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
    VISUALIZE("v", "visualize", "The visualization file/directory.", false,
        true, 1),

    /**
     * Create option to define the prefix(es) to consider on parsing the input
     * directory.
     */
    PREFIX("p", "prefix",
        "The prefix(es) to consider on parsing the input directory.",
        false, true, Option.UNLIMITED_VALUES),

    /**
     * Create option to define the suffix for serialization files to create.
     */
    SUFFIX("s", "suffix",
        "The suffix to use on creating serialization file(s).",
        false, true, 1),
    
    /**
     * Create option to enable the identification of paragraphs bounding boxes.
     */
    BOUNDING_BOXES("b", "boundingboxes",
        "Enable the identification of paragraphs bounding boxes.",
        false),

    /**
     * Create option to define roles to serialize.
     */
    ROLE("r", "role",
        "Defines roles to consider on serialization of paragraphs.",
        false, true, Option.UNLIMITED_VALUES),
    
    /**
     * Create option to serialize only the text of paragraphs into txt file.
     */
    PLAIN_SERIALIZATION("x", "plain",
        "Outputs only the text of paragraphs delmited by \n\n into txt file.",
        false),
    
    /**
     * Create option to define path to the texmf dir.
     */
    TEXMF_PATHS("t", "texmf",
        "The path to the texmf directory",
        false, true, Option.UNLIMITED_VALUES),

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
    TeXParserOptions(String opt, String longOpt, String description) {
      this(opt, longOpt, description, false);
    }

    /**
     * Creates a new option with given arguments.
     */
    TeXParserOptions(String opt, String longOpt, String description,
        boolean required) {
      this(opt, longOpt, description, required, false);
    }

    /**
     * Creates a new option with given arguments.
     */
    TeXParserOptions(String opt, String longOpt, String description,
        boolean required, boolean hasArg) {
      this(opt, longOpt, description, required, hasArg, hasArg ? 1 : 0);
    }
    
    /**
     * Creates a new option with given arguments.
     */
    TeXParserOptions(String opt, String longOpt, String description,
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

class TexFileProcessor implements Runnable {
  
  public Path file;
  public boolean identifyPdfParagraphs;
  public List<String> texmfPaths;
  public List<String> roles;
  public boolean isPlainSerialization;
  public Path inputDirectory;
  public Path serializationFile;
  public String serialFileSuffix;
  public Path serializationDirectory;
  public Path visualizationFile;
  public Path visualizationDirectory;
  
  public TexFileProcessor(Path file) {
    this.file = file;
  }

  public TexFileProcessor(Path file, boolean identifyPdfParagraphs, 
      List<String> texmfPaths) {
    this.file = file;
    this.identifyPdfParagraphs = identifyPdfParagraphs;
    this.texmfPaths = texmfPaths;
  }
  
  /**
   * Processes the given tex file. Identifies the paragraphs from given tex
   * file and their positions in pdf file if global flag 
   * 'identifyPdfParagraphs' is set to true. Serializes and visualizes the
   * paragraphs if related paths are given.
   */
  public void run() {
    try {
      processTeXFile(this.file);
    } catch (Exception e) {
      System.err.println("Error on processing: " + this.file + ": ");
      e.printStackTrace();
    }
  }
  
  public void processTeXFile(Path file) throws Exception {
    TeXFile texFile = new TeXFile(file);
    
    Path serializationTargetFile = defineSerializationTargetFile(texFile);
    Path visualizationTargetFile = defineVisualizationTargetFile(texFile);
        
    if (serializationTargetFile == null) {
      return;
    }
        
    // Identify the paragraphs in the given tex file.
    identifyTexParagraphs(texFile);
    
    // Identify the postions of tex paragraphs in tex file.
    if (this.identifyPdfParagraphs) {
      identifyPdfParagraphs(texFile, this.texmfPaths);
    }
        
    // Serialize.
    if (serializationTargetFile != null) {
      serialize(texFile, this.roles, serializationTargetFile);
    }
    
    // Visualize.
    if (visualizationTargetFile != null) {
      visualize(texFile, this.roles, visualizationTargetFile);
    }
    
    System.out.println(file + " -> " + serializationTargetFile);
  }
  
  // ---------------------------------------------------------------------------

  /**
   * Identifies the paragraphs from given tex file.
   */
  protected void identifyTexParagraphs(TeXFile texFile) throws IOException {
    new TeXParagraphsIdentifier(texFile).identify();
  }

  /**
   * Identifies the positions of paragraphs from given tex file in related pdf 
   * file.
   */
  protected void identifyPdfParagraphs(TeXFile texFile, List<String> texmfPaths)
    throws IOException {
    new PdfParagraphsIdentifier(texFile, texmfPaths).identify();
  }

  /**
   * Serializes the selected features to file.
   */
  protected void serialize(TeXFile texFile, List<String> roles, Path target) 
      throws IOException {
    if (this.isPlainSerialization) {
      new TeXParagraphTxtSerializer(texFile).serialize(target, roles);
    } else {
      new TeXParagraphTsvSerializer(texFile).serialize(target, roles);
    }
  }

  /**
   * Visualizes the selected features to file.
   */
  protected void visualize(TeXFile texFile, List<String> roles, Path target) 
      throws IOException {
    try {
      new TeXParagraphVisualizer(texFile).visualize(target, roles);
    } catch (Exception e) {
      System.out.println("WARN: Couldn't create visualization.");
    }
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
      return targetDir.resolve(basename + serialFileSuffix);
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
}
