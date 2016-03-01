package interpret;

import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.text.StringUtils;
import model.Command;
import model.Document;
import model.Element;
import model.Group;
import model.Iterator;
import model.NewLine;
import model.NewParagraph;
import model.Option;
import model.Text;
import model.Whitespace;
import preprocess.CommandReference;
import preprocess.CommandReferences;

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
  protected CommandReferences commandReferences;
  
  public TeXInterpreter() {
    this.commandReferences = new CommandReferences(COMMAND_REFERENCES_PATH, 
        COMMAND_REFERENCES_SEPARATOR);
  }
  
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
      
      if (text == null) {
        return;
      }
      
      // Don't allow text that starts with '@' to ignore commands like 
      // "\twocolumn[\csname @twocolumnfalse\endcsname]"
      // FIXME: Check, if this is doesn't ignore regular words.
      if (text.startsWith("@")) {
        return;
      }
      
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
    // Check, if the command is a cross reference.
    // if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite", "\\label")) {
    if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite")) {
      processCrossReferenceCommand(cmd, itr, context);
    }
        
    CommandReference cmdRef = commandReferences.get(cmd);
        
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
    
    if (cmdRef.definesOptionsToParse()) {
      List<Option> options = cmd.getOptions();
      for (Option option : options) {
        processElements(option.elements, context);
      }
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
          if (i < keys.length - 1) {
            context.writeText(DEFAULT_CONTEXT_NAME, " ");
          }
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
    
    CommandReference startCmdRef = commandReferences.get(cmd);
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
            CommandReference cmdRef = commandReferences.get((Command) el);
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
    CommandReference cmdRef = commandReferences.get(command);
    return cmdRef != null ? cmdRef.getEndCommand() : null;
  }
}
