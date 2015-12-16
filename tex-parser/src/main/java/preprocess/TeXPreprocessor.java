package preprocess;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.freiburg.iif.map.ConstantLookupList;
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
 * Class to preprocess tex files. Resolves all macro definitions.
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
   * Creates a new preprocessor for the given tex file.
   */
  public TeXPreprocessor(InputStream stream) {
    super(stream);
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
    for (Element element : document.elements) {
      handleElement(element, writer);
    }
  }
  
  /**
   * Handles the given element from parsed tex document.
   */
  protected void handleElement(Element element, BufferedWriter writer) {
    if (element instanceof MacroDefinition) {
      handleMacroDefinition((MacroDefinition) element, writer);
    } else if (element instanceof NewLine) {
      handleNewline((NewLine) element, writer);
    } else if (element instanceof Whitespace) {
      handleWhitespace((Whitespace) element, writer);
    } else if (element instanceof Command) {
      handleCommand((Command) element, writer);
    } else if (element instanceof Group) {
      handleGroup((Group) element, writer);
    } else if (element instanceof Text) {
      handleText((Text) element, writer);
    } else if (element instanceof Marker) {
      handleMarker((Marker) element, writer);
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
  public void handleCommand(Command command, BufferedWriter writer) {
    outputElements(resolve(command), writer);
    if ("\\end{document}".equals(command.toString())) {
      isEndDocument = true;
    }
  }
  
  /**
   * Handles the given group.
   */
  public void handleGroup(Group group, BufferedWriter writer) {
    outputElements(resolve(group), writer);
  }

  /**
   * Handles the given text.
   */
  public void handleText(Text text, BufferedWriter writer) {
    outputElements(resolve(text), writer);
  }
  
  /**
   * Handles the given newline.
   */
  public void handleNewline(NewLine command, BufferedWriter writer) {
    outputElements(resolve(command), writer);
  }

  /**
   * Handles the given whitespace.
   */
  public void handleWhitespace(Whitespace command, BufferedWriter writer) {
    outputElements(resolve(command), writer);
  }
  
  /**
   * Handles the given marker.
   */
  public void handleMarker(Marker marker, BufferedWriter writer) {
    // Nothing to do.
  }
   
  // ___________________________________________________________________________
    
  /**
   * Resolves the given element.
   * @throws IOException 
   */
  protected ConstantLookupList<Element> resolve(Element element) {
    Group group = new Group();
    resolveElement(element, group);
    return group.elements;
  }
  
  /**
   * Resolves the given command recursively.
   */
  protected void resolveElement(Element element, Group result) {
    if (element instanceof Group) {
      resolveGroup((Group) element, result);
    } else if (element instanceof Command) {
      resolveCommand((Command) element, result);
    } else {
      // Nothing to resolve here.
      result.addElement(element);
    }
  }
  
  /**
   * Resolves the given group.
   * @throws IOException 
   */
  protected void resolveGroup(Group group, Group result) {
    if (group != null && group.elements != null) {
      // Resolve the elements of the group in own context.
      Group resolvedGroup = new Group();
      for (Element element : group.elements) {        resolveElement(element, resolvedGroup);
      }
      // Update the elements of the group to the resolved ones.
      group.elements = resolvedGroup.elements;
    }
    result.addElement(group);
  }
  
  /**
   * Resolves the given command.
   */
  protected void resolveCommand(Command command, Group result) {    
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
        resolveElement(element, result);  
      }
      // Append a whitespace after a macro.
      result.addElement(new Whitespace());
    } else {
      // Command is not a macro, resolve its groups.
      for (Group group : command.getGroups()) {
        resolve(group);
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
  
  // ___________________________________________________________________________
  // Some old stuff.
  
// 
// /**
//  * Compiles the given tex file using the runtime environment and returns the
//  * exit code of the compile process.
//  */
// protected void compileTexFile(Path tex) throws Exception {
//   if (tex == null) {
//     return;
//   }
//   
//   // Get the working directory.
//   Path dir = tex.getParent();
//
//   // Build the pdflatex command. Set the -interaction flag to "nonstopmode"
//   // to avoid the interaction prompt of pdflatex on error.
//   String cmd = 
// String.format("/usr/bin/pdflatex -interaction=nonstopmode %s", tex);
//   // Export "TEXINPUTS" variable to provide all files in the repository.
//   String texInputs = String.format(".:%s:", STY_REPOSITORY);
//   String[] environment = { String.format("TEXINPUTS=%s", texInputs) };
//
//   // Run the command.
//   int exit = CommandLineUtils.runCommand(cmd, environment, dir, 30000);
//   if (exit != 0) {
//     throw new IllegalStateException("Couldn't compile the tex file.");
//   }
// }  
//   
// ___________________________________________________________________________
// Util methods.
// 
// /**
//  * Returns the aux file related to the given tex file.
//  */
// protected Path getAuxFile(Document texDocument) {
//   if (texDocument != null && texDocument.getFile() != null) {
//     String basename = PathsUtil.getBasename(texDocument.getFile());
//     if (basename != null) {
//       Path texFile = texDocument.getFile();
//       if (texFile != null) {
//         return texFile.getParent().resolve(basename + ".aux");  
//       }
//     }
//   }
//   return null;
// }
// 
// /**
//  * The directory containing some standard sty/cls files.
//  */
// protected static final String STY_REPOSITORY = "/nfs/raid1/arxiv/sty/";
// 
  
///**
//* Resolves all cross references and macros in the given tex document.
//*/
//protected void resolve(Document texDocument, Map<String, Group> crossRefs, 
//   Map<String, Group> macros) {
// List<Command> commands = texDocument.get(Command.class, true);
// for (int i = 0; i < commands.size(); i++) {
//   Command command = commands.get(i);
//   String commandName = command.getName();
//
//   if (commandName != null) {
//     // Read all "\cite" commands.
//     if (commandName.equals("\\cite")) {
//       // Resolve all keys in cite command. TODO: Shorten it?
//       Group argsGroup = command.getGroup(1);
//       if (argsGroup != null) {
//         String argsStr = argsGroup.next(Text.class).getText();
//         if (argsStr != null) {
//           String[] keys = argsStr.split(",");
//
//           StringBuilder citeSb = new StringBuilder();
//           for (int j = 0; j < keys.length; j++) {
//             String arg = keys[j].trim();
//             if (crossRefs.containsKey(arg)) {
//               // Append additional group to the command, containing the
//               // resolved cite.
//               Group g = crossRefs.get(arg);
//               if (citeSb.length() > 0) {
//                 citeSb.append(",");
//               }
//               Text text = g.first(Text.class);
//               citeSb.append(text.getText());
//             }
//           }
//           Group g = new Group();
//           Text text = new Text(citeSb.toString());
//           g.addElement(text);
//           command.addGroup(g);
//         }
//       }
//     }
//
//     // Read all "\ref" commands.
//     if (commandName.equals("\\ref")) {
//       Group argsGroup = command.getGroup(1);
//       if (argsGroup != null) {
//         String argsStr = argsGroup.next(Text.class).getText();
//         if (argsStr != null) {
//           String[] args = argsStr.split(",");
//
//           for (int j = 0; j < args.length; j++) {
//             String arg = args[j].trim();
//             if (crossRefs.containsKey(arg)) {
//               // Append additional group to the command, containing the
//               // resolved cite.
//               command.addGroup(crossRefs.get(arg));
//             }
//           }
//         }
//       }
//     }
//
//     // Read all other commands and check, if they represent a macro.
//     if (macros.containsKey(commandName)) {
//       Group template = macros.get(command.getName()).clone();
//       
//       for (Marker marker : template.get(Marker.class, true)) {
//         if (command.hasGroups(marker.getId())) {
//           template.replace(marker, command.getGroup(marker.getId()));
//         }
//       }
//       command.addGroup(template);
//       command.setIsMacro(true);
//     }
//   }
// }
//}
  
  // ___________________________________________________________________________
  // Parse methods.
  
//  /**
//   * Compiles the given tex file to get the related aux file and to parse
//   * the cross references from them.
//   */
//  protected void parseCrossReferences(Document texDocument) throws Exception {
//    if (texDocument == null) {
//      return;
//    }
    
    // Compile the tex file twice.
//    compileTexFile(texDocument.getFile());
//    compileTexFile(texDocument.getFile());
    
//    Document auxDocument = parser.parse(getAuxFile(texDocument));
//    
//    if (auxDocument != null) {
//      List<Command> commands = auxDocument.get(Command.class, true);
//      if (commands != null) {
//        // Parse all commands in the aux document.
//        for (Command command : commands) {
//          if (command != null) {
//            String commandName = command.getName();
//            if (commandName != null) { 
//              if (commandName.equals("\\bibcite")
//                  || commandName.equals("\\newlabel")) {
//                Group firstGroup = command.getGroup(1);
//                Group secondGroup = command.getGroup(2);
//                if (firstGroup != null) {
//                  Text label = firstGroup.next(Text.class);
//                  if (label != null) {
//                  texDocument.addCrossReference(label.getText(), secondGroup);
//                  }
//                }
//              }
//            }
//          }
//        }
//      }
//    }
//  }
}
