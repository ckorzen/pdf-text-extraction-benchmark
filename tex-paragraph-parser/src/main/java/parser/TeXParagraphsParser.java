package parser;

import static model.TeXParagraphParserConstants.TEX_ELEMENT_REFERENCES_PATH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.text.StringUtils;
import model.Command;
import model.Document;
import model.Element;
import model.Group;
import model.Iterator;
import model.Option;
import model.TeXElementReference;
import model.TeXElementReferences;
import model.TeXParagraph;
import model.Text;

/**
 * Class to parse Document objects for text paragraphs.
 * 
 * @author Claudius Korzen
 *
 */
public class TeXParagraphsParser {  
  /** 
   * The document to parse. 
   */
  protected Document document;

  /** 
   * The tex element references. 
   */
  protected TeXElementReferences texElementRefs;
  
  /**
   * Creates a new paragraph parser for the given document.
   */
  public TeXParagraphsParser(Document document) throws IOException {
    this.document = document;
    this.texElementRefs = new TeXElementReferences(TEX_ELEMENT_REFERENCES_PATH);
  }

  /**
   * Identifies the paragraphs in the given document.
   */
  public List<TeXParagraph> identifyParagraphs() {
    return processDocument(document);
  }

  // ===========================================================================
  
  /**
   * Processes the given document.
   */
  protected List<TeXParagraph> processDocument(Document document) {
    List<TeXParagraph> paragraphs = new ArrayList<>();
    TeXParagraph paragraph = new TeXParagraph();

    processElements(document.elements, null, paragraph, paragraphs);
    
    return paragraphs;
  }

  /**
   * Processes the given elements. The given feature represents the current 
   * feature context, 'para' represents the current paragraph and 'paras' 
   * represents the list of paragraphs where to put identified paragraphs into. 
   */
  protected TeXParagraph processElements(List<Element> elements, 
      String feature, TeXParagraph para, List<TeXParagraph> paras) {
        
    Iterator<Element> itr = new Iterator<>(elements);
    while (itr.hasNext()) {
      Element prevElement = itr.previous();
      Element element = itr.next();
      para = processElement(prevElement, element, itr, feature, para, paras);
    }

    return para;
  }

  /**
   * Processes the given element. 'prevElement' represents the previous element,
   * 'itr' represents the iterator through the elements and can be used to 
   * traverse the elements. 'feature' represents the current feature context, 
   * 'para' represents the current paragraph and 'paras' represents the list of
   * paragraphs where to put identified paragraphs into.
   */
  protected TeXParagraph processElement(Element prevElement, Element element,
      Iterator<Element> itr, String feature, TeXParagraph para, 
      List<TeXParagraph> paras) {
        
    TeXElementReference prevRef = getTeXElementReference(prevElement);
    TeXElementReference ref = getTeXElementReference(element);
            
    // Obtain if the previous element ends a paragraph.
    boolean prevEndsParagraph = prevRef != null && prevRef.endsParagraph(); 
    // Obtain if the current element starts a paragraph.
    boolean startsParagraph = ref != null && ref.startsParagraph();
    boolean introduceNewParagraph = prevEndsParagraph || startsParagraph;
                
    if (introduceNewParagraph) {
      // The element introduces a new paragraph. Add the previous paragraph to 
      // result (if not empty) and create a new paragraph.
      if (!para.getText().trim().isEmpty()) {
        paras.add(para);
      }
      
      // Check if the element introduces a new feature. If so, take it as the
      // new feature context, otherwise take the 'predefined' feature.
      String refFeature = ref != null ? ref.getFeatureName() : null;
      feature = refFeature != null ? refFeature : feature;
      
      // Create the new paragraph.
      para = new TeXParagraph(feature);
    }

    // We differ into 2 types on introducing a new paragraph:
    // (1) The element belongs to the introducing paragraph.
    // (2) The element doesn't belong to the introducing paragraph.
    // In case (2), ignore the current element.
    boolean ignore = ref != null && ref.startsParagraphExclusiveElement();
    
    if (!ignore) {
      if (element instanceof Group) {
        Group group = (Group) element;
        para = processElements(group.elements, feature, para, paras);
      } else if (element instanceof Text) {
        Text text = (Text) element;
        para = processText(text, itr, feature, para, paras);
      } else if (element instanceof Command) {
        Command command = (Command) element;
        para = processCommand(command, itr, feature, para, paras);
      }
    }
    return para;
  }

  /**
   * Processes the given text element.
   */
  protected TeXParagraph processText(Text textElement, Iterator<Element> itr,
      String feature, TeXParagraph para, List<TeXParagraph> paras) {
    String string = textElement.toString().trim();
        
    // Don't allow text that starts with '@' to ignore commands like
    // "\twocolumn[\csname @twocolumnfalse\endcsname]"
    // FIXME: Check, if this is doesn't ignore regular words.
    if (string.startsWith("@")) {
      return para;
    }

    // TODO: Move the normalization to an extra method.
    string = string.replaceAll("~", " ");
    string = string.replaceAll("``", "\"");
    string = string.replaceAll("''", "\"");
    string = string.replaceAll("`", "'");
    string = string.replaceAll("&", ""); // Remove the separators in tabular
    // Replace all variant of a dash by the simple one.
    string = string.replaceAll("--", "-");
    // Replace all variant of a dash by the simple one.
    string = string.replaceAll("---", "-");

    if (string.isEmpty()) {
      // Replace all whitespaces and newlines by a single whitespace.
      para.registerWhitespace();
    } else {
      para.writeString(string);
      para.registerTeXLineNumber(textElement.getBeginLineNumber());
      para.registerTeXLineNumber(textElement.getEndLineNumber());
    }

    return para;
  }

  /**
   * Process the given command.
   */
  protected TeXParagraph processCommand(Command cmd, Iterator<Element> itr,
      String feature, TeXParagraph para, List<TeXParagraph> paras) {
                
    // Check, if the command is a cross reference. TODO
    if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite")) {
      processCrossReferenceCommand(cmd, itr, para, paras);
    }

    TeXElementReference ref = getTeXElementReference(cmd);
    
    if (ref == null) {
      // Do nothing if there is no element reference for the command.
      return para;
    }

    // Some arguments of some commands won't be defined within a group but as
    // a consecutive string, e.g. "\vksip 5pt". 
    // So check, if the command has the expected number of groups. If not, 
    // consider the appropriated number of consecutive string as the command's
    // arguments.
    if (ref.definesNumberOfGroups()) {
      int expectedNumGroups = ref.getNumberOfGroups();
      int actualNumGroups = cmd.getGroups().size();

      // Because the context may be a new object after resolving, adjust
      // the current position in the context.
      // context.reposition(cmd);

      if (expectedNumGroups > actualNumGroups) {
        // The actual number of groups is smaller than the expected number.
        // Add the appropriate number of following elements as groups to the
        // command.
        for (int i = 0; i < expectedNumGroups - actualNumGroups; i++) {
          Element nextElement = itr.peekNonWhitespace();
          if (nextElement instanceof Group) {
            itr.nextNonWhitespace();
            Group nextGroup = (Group) nextElement;
            // context.elements.set(context.curIndex - 1, null);
            // Simply add the group to the command.
            cmd.addGroup(nextGroup);
          } else if (nextElement instanceof Text) {
            itr.nextNonWhitespace();
            Text textElement = (Text) nextElement;
            // context.elements.set(context.curIndex - 1, null);
            String text = textElement.toString();
            if (!text.trim().isEmpty()) {
              // Create new Group and add it to the command.
              cmd.addGroup(new Group(textElement));
            }
          }
        }
      }
    }

    // Write placeholder, if the command introduces a placeholder.
    if (ref.introducesPlaceholder()) {
      para.writeString(ref.getPlaceholder());
      String end = guessEndCommand(cmd);
      itr.skipTo(end);
      return para;
    }

    // Check, if we have to parse the options part of the command.
    if (ref.definesOptionsToParse()) {
      List<Option> options = cmd.getOptions();
      for (Option option : options) {
        para = processElements(option.elements, feature, para, paras);
      }
    }

    // Check which groups of the command we have to parse.
    if (ref.definesGroupsToParse()) {
      List<Integer> groupsToParse = ref.getGroupsToParse();
      for (int id : groupsToParse) {
        Group group = cmd.hasGroups(id + 1) ? cmd.getGroup(id + 1) : null;

        if (group != null) {
          para = processElements(group.elements, feature, para, paras);
        }
      }
    }
    
    // TODO: Kommandos wie "\section{xxx}" sollen als eigene Paragraphen 
    // erkannt werden, also das naechste Element in einen neuen Paragrpahen. 
    // Das wird normalerweise ueber prev.endsParagraph() geregelt, aber dadurch
    // dass die Kindelemente in einem eigenen Kontext behandelt werden, ist
    // für das naechste Element prev == null.
    if (ref.endsParagraph()) {
      if (!para.getText().trim().isEmpty()) {
        paras.add(para);
      }
      
      para = new TeXParagraph(feature);
    }
    
    // Process all child commands within this context, e.g. 
    // '\begin{abstract} ... \end{abstract}'
    return processElements(getChildElements(cmd, itr), feature, para, paras);
  }

  /**
   * Processes a cross reference command (\cite, \label, \ref).
   */
  protected void processCrossReferenceCommand(Command cmd,
      Iterator<Element> itr, TeXParagraph para, List<TeXParagraph> paras) {
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
          para.writeString("[");
          para.writeString(cmd.getName());
          para.writeString("=");
          para.writeString(keys[i].trim());
          para.writeString("]");
          if (i < keys.length - 1) {
            para.writeString(" ");
          }
        }
      }
    }
    return;
  }

  /**
   * Collects all elements of the environment, that is introduced by the given
   * command.
   */
  protected List<Element> getChildElements(Command cmd, Iterator<Element> i) {
    List<Element> elements = new ArrayList<>();

    TeXElementReference startCmdRef = getTeXElementReference(cmd);
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
            TeXElementReference cmdRef = getTeXElementReference((Command) el);
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
    TeXElementReference cmdRef = getTeXElementReference(command);
    return cmdRef != null ? cmdRef.getEndCommand() : null;
  }

  // ===========================================================================
  
  protected TeXElementReference getTeXElementReference(Element element) {
    return this.texElementRefs.getElementReference(element);
  }
  
}