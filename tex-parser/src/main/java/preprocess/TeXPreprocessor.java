package preprocess;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.freiburg.iif.collection.ConstantLookupList;
import model.Command;
import model.Document;
import model.Element;
import model.Group;
import model.MacroDefinition;
import model.Marker;
import model.NewLine;
import model.Text;
import model.Whitespace;
import parse.ParseException;
import parse.TeXParser;

/**
 * Class that resolves all macros and to standardizes commands, i.e. transforms
 * commands like "\vskip 2cm" to "\vskip{2cm}.
 *
 * @author Claudius Korzen
 */
public class TeXPreprocessor extends TeXParser {          
  /** 
   * The number of consecutive whitespaces in front of the current element to 
   * output.
   */
  protected int numConsecutiveWhitespaces;
  
  /** 
   * The number of consecutive newlines in front of the current element to 
   * output.
   */
  protected int numConsecutiveNewlines;
  
  /**
   * Flag that indicates, if the command "\end{document}" was already seen.
   */
  protected boolean isEndDocument;
  
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
  
  /**
   * Creates a new preprocessor for the given tex file.
   */
  public TeXPreprocessor(InputStream stream) {
    super(stream);
    this.commandReferences = new CommandReferences(COMMAND_REFERENCES_PATH, 
        COMMAND_REFERENCES_SEPARATOR);
  }
  
  /**
   * Starts the preprocessing for the given tex file.
   */
  public void preprocess(Path target) throws IOException, ParseException {
    ensureFileExistency(target);
    BufferedWriter w = Files.newBufferedWriter(target, StandardCharsets.UTF_8); 
    preprocess(w);   
    w.close();
  }
  
  /**
   * Starts the preprocessing for the given tex file and writes the result to
   * the given writer.
   */
  public void preprocess(BufferedWriter writer) throws IOException, 
      ParseException {
    Document document = parse();
    while (document.hasNext()) {
      handleElement(document.next(), document, writer);
    }
  }
  
  /**
   * Handles the given element from parsed tex document.
   */
  protected void handleElement(Element element, Group context, 
      BufferedWriter writer) {    
    if (element instanceof MacroDefinition) {
      handleMacroDefinition((MacroDefinition) element, writer);
    } else if (element instanceof NewLine) {
      handleNewline((NewLine) element, context, writer);
    } else if (element instanceof Whitespace) {
      handleWhitespace((Whitespace) element, context, writer);
    } else if (element instanceof Command) {
      handleCommand((Command) element, context, writer);
    } else if (element instanceof Group) {
      handleGroup((Group) element, context, writer);
    } else if (element instanceof Text) {
      handleText((Text) element, context, writer);
    } else if (element instanceof Marker) {
      handleMarker((Marker) element, context, writer);
    }
  }
    
  /**
   * Handles the given macro definition.
   */
  public void handleMacroDefinition(Command command, BufferedWriter writer) {
    outputElement(command, writer);
  }
  
  /**
   * Handles the given command.
   */
  public void handleCommand(Command command, Group context,
      BufferedWriter writer) {
    outputElements(resolve(command, context), writer);
    if ("\\end{document}".equals(command.toString())) {
      isEndDocument = true;
    }
  }
  
  /**
   * Handles the given group.
   */
  public void handleGroup(Group group, Group context, BufferedWriter writer) {
    outputElements(resolve(group, context), writer);
  }

  /**
   * Handles the given text.
   */
  public void handleText(Text text, Group context, BufferedWriter writer) {
    outputElements(resolve(text, context), writer);
  }
  
  /**
   * Handles the given newline.
   */
  public void handleNewline(NewLine command, Group context, 
      BufferedWriter writer) {
    outputElements(resolve(command, context), writer);
  }

  /**
   * Handles the given whitespace.
   */
  public void handleWhitespace(Whitespace command, Group context, 
      BufferedWriter writer) {
    outputElements(resolve(command, context), writer);
  }
  
  /**
   * Handles the given marker.
   */
  public void handleMarker(Marker marker, Group context, 
      BufferedWriter writer) {
    // Nothing to do.
  }
   
  // ___________________________________________________________________________
    
  /**
   * Resolves the given element.
   * @throws IOException 
   */
  protected ConstantLookupList<Element> resolve(Element element, 
      Group context) {
    Group group = new Group();
    resolveElement(element, context, group);
    return group.elements;
  }
  
  /**
   * Resolves the given command recursively.
   */
  protected void resolveElement(Element element, Group context, Group result) {
    if (element instanceof Group) {
      resolveGroup((Group) element, context, result);
    } else if (element instanceof Command) {
      resolveCommand((Command) element, context, result);
    } else {
      // Nothing to resolve here.
      result.addElement(element);
    }
  }
  
  /**
   * Resolves the given group.
   * @throws IOException 
   */
  protected void resolveGroup(Group group, Group context, Group result) {
    if (group != null && group.elements != null) {
      // Resolve the elements of the group in own context.
      Group resolvedGroup = new Group();
      for (Element element : group.elements) {        resolveElement(element, group, resolvedGroup);
      }
      // Update the elements of the group to the resolved ones.
      group.elements = resolvedGroup.elements;
    }
    result.addElement(group);
  }
  
  /**
   * Resolves the given command.
   */
  protected void resolveCommand(Command command, Group context, Group result) {
    if (command == null) {
      return;
    }

    // Check, if the command was defined via a macro.
    if (isDefinedByMacro(command)) {      
      // Resolve the macro.
      Group macro = getMacro(command).clone();
         
      // Plug in the arguments (replace the markers by arguments).
      List<Marker> markers = macro.get(Marker.class, true);
      for (Marker marker : markers) {
        if (marker != null && command.hasGroups(marker.getId())) {
          Group arg = command.getGroup(marker.getId());
          if (arg != null) {
            macro.replace(marker, arg.elements);
          }
        }
      }
      
      // Resolve the elements of the macro.
      for (Element element : macro.elements) {
        resolveElement(element, context, result);  
      }
      // Append a whitespace after a macro.
      result.addElement(new Whitespace());
    } else {
      // Command is not a macro, resolve its groups.
      for (Group group : command.getGroups()) {
        resolve(group, context);
      }
      
      // Check, if the command holds the expected number of groups.
      CommandReference cmdRef = commandReferences.get(command);
              
      if (cmdRef != null && cmdRef.definesNumberOfGroups()) {
        int expectedNumGroups = cmdRef.getNumberOfGroups();
        int actualNumGroups = command.getGroups().size();
        
        // Because the context may be a new object after resolving, adjust
        // the current position in the context.
        context.reposition(command);
               
        if (expectedNumGroups > actualNumGroups) {
          // The actual number of groups is smaller than the expected number.
          // Add the appropriate number of following elements as groups to the
          // command.
          for (int i = 0; i < expectedNumGroups - actualNumGroups; i++) {
            Element nextElement = context.nextNonWhitespace();
            
            if (nextElement instanceof Group) {
              Group nextGroup = (Group) nextElement;
              context.elements.set(context.curIndex - 1, null);
              resolve(nextGroup, context);
              // Simply add the group to the command.
              command.addGroup(nextGroup);
            } else if (nextElement instanceof Text) {
              Text textElement = (Text) nextElement;
              context.elements.set(context.curIndex - 1, null);
              resolve(textElement, context);
              String text = textElement.toString();
              if (!text.trim().isEmpty()) {
                // Create new Group and add it to the command.
                command.addGroup(new Group(textElement));
              }
            }
          }
        }
      }
      
      
      result.addElement(command);
    }
  }
  
  // ___________________________________________________________________________
     
  /**
   * Outputs the given list of elements to the given writer.
   */
  protected void outputElements(List<Element> elements, BufferedWriter w) {
    for (Element element : elements) {
      outputElement(element, w);
    }
  }
  
  /**
   * Outputs the given element to the given writer.
   */
  protected void outputElement(Element element, BufferedWriter writer) {
    if (element == null) {
      return;
    }
    
    // Don't output any elements, if the end of documents was reached.
    if (isEndDocument) {
      return;
    }
    
    if (element instanceof Whitespace) {
      // Register the whitespace and output it to the writer only if a 
      // non-whitespace element follows.
      numConsecutiveWhitespaces++;
      return;
    } else if (element instanceof NewLine) {
      // On newline, we don't have to output any registered whitespace anymore.
      numConsecutiveWhitespaces = 0; 
      numConsecutiveNewlines++;
      return;
    }
    
    try {
      // Check, if we have to introduce a whitespace.
      if (numConsecutiveWhitespaces > 0) {
        writer.write(new Whitespace().toString());
      }
      // Check, if we have to introduce a paragraph.
      if (numConsecutiveNewlines == 1) {
        writer.newLine();
      } else if (numConsecutiveNewlines > 1) {
        writer.newLine();
        writer.newLine();
      }

      writer.write(element.toString());
    } catch (IOException e) {
      System.err.print("Couldn't write to the writer: " + e.getMessage());
    } finally {
      // Unregister the whitespace and the newparagraph.
      numConsecutiveWhitespaces = 0;
      numConsecutiveNewlines = 0;
    }
  }
  
  // ___________________________________________________________________________
  
  /**
   * Creates the given file if it doesn't exist yet.
   */
  protected void ensureFileExistency(Path file) throws IOException {
    if (!Files.exists(file)) {
      Files.createDirectories(file.getParent());
      Files.createFile(file);
    }
  }
    
  /**
   * Returns true, if the given command is defined by a macro.
   */
  protected boolean isDefinedByMacro(Command command) {
    return macros.containsKey(command.getName());
  }
  
  /**
   * Returns the macro definition for the given command.
   */
  protected Group getMacro(Command command) {
    return macros.get(command.getName());
  }
}
