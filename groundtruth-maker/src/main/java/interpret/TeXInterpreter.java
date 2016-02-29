package interpret;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import de.freiburg.iif.path.LineReader;
import de.freiburg.iif.text.StringUtils;
import model.Command;
import model.Document;
import model.Element;
import model.Group;
import model.Iterator;
import model.NewLine;
import model.NewParagraph;
import model.Text;
import model.Whitespace;

/**
 * Interprets tex files.
 *
 * @author Claudius Korzen
 */
public class TeXInterpreter {
  /** 
   * The name of default context.
   */
  static final String DEFAULT_CONTEXT_NAME = "text";
  
  /**
   * The path to the command references file.
   */
  static final String COMMAND_REFERENCES_PATH = "/command-references.csv";
  
  /**
   * The field separator on command references file.
   */
  static final String COMMAND_REFERENCES_SEPARATOR = ",";
  
  /**
   * The parsed command references.
   */
  Map<String, CommandReference> commandReferences;
  
  // ___________________________________________________________________________
  // Public methods.
  
  /**
   * Serializes the given tex document.
   */
  public TeXHierarchy interpret(Document document) {
    return processDocument(document);
  }

  // ___________________________________________________________________________

  /**
   * Processes the given document.
   */
  protected TeXHierarchy processDocument(Document document) {
    // Create a new global context.
    TeXHierarchy hierarchy = new TeXHierarchy();
    // Process all elements of the document under the created context.
    processElements(document.elements, hierarchy);
            
    return hierarchy;
  }

  /**
   * Processes the given list of elements.
   */
  protected void processElements(List<Element> els, TeXHierarchy context) {
    Iterator<Element> itr = new Iterator<>(els);
    while (itr.hasNext()) {
      processElement(itr.next(), itr, context);
    }
  }
  
  /**
   * Processes the given element.
   */
  protected void processElement(Element element, Iterator<Element> itr, 
      TeXHierarchy context) {    
    if (element instanceof Group) {
      processElements(((Group) element).elements, context);
    } else if (element instanceof Text) {
      processText((Text) element, itr, context);
    } else if (element instanceof Command) {
      processCommand((Command) element, itr, context);
    }
  }
    
  /**
   * Processes the given text.
   */
  protected void processText(Text element, Iterator<Element> itr, 
      TeXHierarchy context) {
       
    if (element instanceof NewParagraph) {
      context.writeNewParagraph(DEFAULT_CONTEXT_NAME);
    } else if (element instanceof NewLine) {
      context.writeNewLine(DEFAULT_CONTEXT_NAME);
    } else if (element instanceof Whitespace) {
      context.writeWhiteSpace(DEFAULT_CONTEXT_NAME);
    } else {
      String text = element.getText();
      // TODO: Move the normalization to an extra method.
      text = text.replaceAll("~", " ");
      text = text.replaceAll("``", "\"");
      text = text.replaceAll("''", "\"");
      text = text.replaceAll("`", "'");
      text = text.replaceAll("&", ""); // Remove the separators in tabular
      // Replace all variant of a dash by the simple one.
      text = text.replaceAll("--", "-"); 
      // Replace all variant of a dash by the simple one.
      text = text.replaceAll("---", "-"); 
      if (!text.trim().isEmpty()) {
        context.writeText(DEFAULT_CONTEXT_NAME, text);
      }
    }
  }
      
  /**
   * Processes the given command.
   */
  protected void processCommand(Command cmd, Iterator<Element> itr, 
      TeXHierarchy context) {    
    
    System.out.println(cmd);
    
    // Check, if the command is a cross reference.
    // if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite", "\\label")) {
    if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite")) {
      processCrossReferenceCommand(cmd, itr, context);
    }
    
    CommandReference cmdRef = getCommandReference(cmd);
        
    // Skip the command, if there is no reference for the command.
    if (cmdRef == null) {
      itr.skipTo(guessEndCommand(cmd));
      return;
    }
    
    // Write placeholder, if the command introduces a placeholder.
    if (cmdRef.introducesPlaceholder()) {
      context.writeText(DEFAULT_CONTEXT_NAME, cmdRef.getPlaceholder());
      itr.skipTo(guessEndCommand(cmd));
      return;
    }
        
    // Create new context, if the command introduces a new context.
    if (cmdRef.definesContextName()) {
      TeXHierarchy newContext = new TeXHierarchy();
      // Register the new context.
      context.insertNewContext(cmdRef.getContextName(), newContext);
      // Update the context.
      context = newContext;
    }
           
    // The commands itself may contain groups to parse, that may indeed 
    // introduce contexts.
    // For example, the first group of the command "\section{Introduction}"
    // introduces the context "heading".
    if (cmdRef.definesGroupsToParse()) {
      List<Integer> groupsToParse = cmdRef.getGroupsToParse();
      for (int id : groupsToParse) {
        Group group = cmd.hasGroups(id + 1) ? cmd.getGroup(id + 1) : null;
        
        if (group != null) {
          String groupFieldName = cmdRef.getGroupFieldName(id);

          if (groupFieldName != null) {
            TeXHierarchy newContext = new TeXHierarchy();
            processElements(group.elements, newContext);
            context.insertNewContext(groupFieldName, newContext);
          } else {
            processElements(group.elements, context);
          }
        }
      }
    }
        
    // Process all commands within this context.
    processElements(getChildElements(cmd, itr), context);
  }
     
  /**
   * Processes a cross reference command (\cite, \label, \ref).
   */
  protected void processCrossReferenceCommand(Command cmd, 
      Iterator<Element> itr, TeXHierarchy context) {
    Group group = cmd.getGroup();
    // The group may be separated by whitespace. 
    // If next element is a group, take this group as argument.
    if (group == null) {
      Element next = itr.peek();
      if (next instanceof Group) {
        group = (Group) itr.next();
      }
    }

    if (group != null) {
      String text = group.getText();
      if (text != null) {
        // Add placeholder for each key in \cite{}.
        String[] keys = text.split(",");
        // context.writeText(DEFAULT_CONTEXT_NAME, " ");
        for (int i = 0; i < keys.length; i++) {
          context.writeText(DEFAULT_CONTEXT_NAME, "[");
          context.writeText(DEFAULT_CONTEXT_NAME, cmd.getName());
          context.writeText(DEFAULT_CONTEXT_NAME, "=");
          context.writeText(DEFAULT_CONTEXT_NAME, keys[i].trim());
          context.writeText(DEFAULT_CONTEXT_NAME, "]");
          if (i < keys.length - 1) context.writeText(DEFAULT_CONTEXT_NAME, " ");
        }
      }
    }
    return;
  }
  
  // ___________________________________________________________________________

  /**
   * Collects all elements of the environment, that is introduced by the given
   * command.
   */
  protected List<Element> getChildElements(Command cmd, Iterator<Element> i) {
    List<Element> elements = new ArrayList<>();
    
    CommandReference startCmdRef = getCommandReference(cmd);
    int outline = startCmdRef != null ? startCmdRef.getOutlineLevel() : -1; 
    String endCommand = guessEndCommand(cmd);
    
    if (endCommand != null || outline > 0) {
      while (i.hasNext()) {
        Element element = i.next();
        
        elements.add(element);
        
        if (element.toString().equals(endCommand)) {
          break;
        }
        
        // Check the outline level of the next element.
        if (i.hasNext()) {
          Element el = i.peek();
          if (el instanceof Command) {
            CommandReference cmdRef = getCommandReference((Command) el);
            if (cmdRef != null && cmdRef.definesOutlineLevel()) {
              if (cmdRef.getOutlineLevel() == outline) {
                break;
              }
            }
          }
        }
      }
    }
    
    return elements;
  }
  
  /**
   * Guesses the end command of unknown command (= command without a reference).
   */
  protected String guessEndCommand(Command command) {
    if (command == null) {
      return null;
    }
    
    // If the command is "\begin{foobar}", it must end with "\end{foobar}".
    if (command.nameEquals("\\begin")) {
      String value = command.getValue();
      return "\\end{" + value + "}";
    }
   
    // Check, if the references defines a end command for the command.
    CommandReference cmdRef = getCommandReference(command);
    return cmdRef != null ? cmdRef.getEndCommand() : null;
  }

  // ___________________________________________________________________________
  
  /**
   * Reads the commands to consider.
   * 
   * @throws URISyntaxException
   */
  protected Map<String, CommandReference> readCommandReferences() {
    final Map<String, CommandReference> references = new HashMap<>();
    
    // Read the command references file line by line.
    LineReader reader = new LineReader() {
      public void handleLine(String line) {
        // Ignore comment lines.
        if (line.startsWith("#")) {
          return;
        }

        // Ignore empty lines.
        if (line.trim().isEmpty()) {
          return;
        }

        String[] fields = line.split(COMMAND_REFERENCES_SEPARATOR, -1);
        CommandReference reference = new CommandReference(fields);
        references.put(reference.getCommandName(), reference);
      }
    };
    
    InputStream is = getClass().getResourceAsStream(COMMAND_REFERENCES_PATH);
    reader.read(is);
    
    try {
      is.close();
    } catch (IOException e) {
      return references;
    }
    
    return references;
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if the given command has a command reference.
   */
  protected boolean hasCommandReference(Command command) {
    return getCommandReference(command) != null;
  }

  /**
   * Returns the command reference for the given command.
   */
  protected CommandReference getCommandReference(Command command) {
    if (command == null) {
      return null;
    }
    
    if (commandReferences == null) {
      commandReferences = readCommandReferences();
    }
    
    if (commandReferences == null) {
      return null;
    }
    
    if (commandReferences.containsKey(command.toShortString())) {
      return commandReferences.get(command.toShortString());
    }
    return commandReferences.get(command.getName());
  }
}

/**
 * The reference of a single command.
 *
 * @author Claudius Korzen
 *
 */
class CommandReference {
  /** The reference fields. */
  protected String[] fields;

  /**
   * The default constructor.
   */
  public CommandReference(String[] fields) {
    this.fields = fields;
  }

  /**
   * Returns the command name.
   */
  public String getCommandName() {
    return getString(0);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference introduces an environment.
   */
  public boolean introducesEnvironment() {
    return definesEndCommand() || definesOutlineLevel();
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines an end command.
   */
  public boolean definesEndCommand() {
    return getEndCommand() != null;
  }

  /**
   * Returns the defined end command.
   */
  public String getEndCommand() {
    return getString(1);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines an placeholder.
   */
  public boolean introducesPlaceholder() {
    return getPlaceholder() != null;
  }

  /**
   * Returns the defined placeholder.
   */
  public String getPlaceholder() {
    return getString(2);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines an outline level.
   */
  public boolean definesOutlineLevel() {
    return getOutlineLevel() > -1;
  }

  /**
   * Returns the defined outline level.
   */
  public int getOutlineLevel() {
    return getInteger(3);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines a context.
   */
  public boolean definesContextName() {
    return getContextName() != null;
  }

  /**
   * Returns the defined context name.
   */
  public String getContextName() {
    return getString(4);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines the number of groups.
   */
  public boolean definesNumberOfGroups() {
    return getNumberOfGroups() > -1;
  }

  /**
   * Returns the defined number of groups.
   */
  public int getNumberOfGroups() {
    return getInteger(5);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines the groups to parse.
   */
  public boolean definesGroupsToParse() {
    return getGroupsToParse() != null && !getGroupsToParse().isEmpty();
  }

  /**
   * Returns the defined groups to parse.
   */
  public List<Integer> getGroupsToParse() {
    return getIntegerList(6, ";");
  }

  /**
   * Returns the index of the given group id in the list of groups to parse.
   */
  protected int getIndexOfGroupIdInGroupsToParse(int groupId) {
    List<Integer> groupsToParse = getIntegerList(6, ";");
    if (groupsToParse != null) {
      for (int i = 0; i < groupsToParse.size(); i++) {
        if (groupsToParse.get(i) == groupId) {
          return i;
        }
      }
    }
    return -1;
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines fieldnames of the groups to parse.
   */
  public boolean definesGroupFieldNames() {
    return getGroupFieldNames() != null && !getGroupFieldNames().isEmpty();
  }

  /**
   * Returns the defined groups to parse.
   */
  public List<String> getGroupFieldNames() {
    return getStringList(7, ";");
  }

  /**
   * Returns the defined fieldname of the i-th groups to parse.
   */
  public String getGroupFieldName(int groupId) {
    List<String> groupFieldNames = getGroupFieldNames();
    if (groupFieldNames != null) {
      int index = getIndexOfGroupIdInGroupsToParse(groupId);
      if (index > -1 && index < groupFieldNames.size()) {
        return groupFieldNames.get(index);
      }
    }
    return null;
  }

  // ___________________________________________________________________________

  @Override
  public String toString() {
    return Arrays.toString(fields);
  }

  // ___________________________________________________________________________

  /**
   * Returns the value of the i-th field in the reference.
   */
  protected String getString(int index) {
    if (index >= 0 && index < fields.length) {
      String value = "".equals(fields[index]) ? null : fields[index];
      if (value != null) {
        return StringEscapeUtils.unescapeCsv(value);
      }
    }
    return null;
  }

  /**
   * Splits the value of the i-th field at the given delimiter and returns the
   * resulting fields as an list of strings.
   */
  protected List<String> getStringList(int index, String delimiter) {
    List<String> result = new ArrayList<>();
    String string = getString(index);
    if (string != null) {
      String[] values = string.split(delimiter, -1);
      for (String value : values) {
        result.add(value);
      }
    }
    return result;
  }

  /**
   * Returns the value of the i-th field as an integer.
   */
  protected int getInteger(int index) {
    String value = getString(index);
    return value != null ? Integer.parseInt(value) : -1;
  }

  /**
   * Splits the value of the i-th field at the given delimiter and returns the
   * resulting fields as an list of integers.
   */
  protected List<Integer> getIntegerList(int index, String delimiter) {
    List<Integer> result = new ArrayList<>();
    String string = getString(index);
    if (string != null) {
      String[] values = string.split(delimiter, -1);
      for (String value : values) {
        result.add(Integer.parseInt(value));
      }
    }
    return result;
  }
}
