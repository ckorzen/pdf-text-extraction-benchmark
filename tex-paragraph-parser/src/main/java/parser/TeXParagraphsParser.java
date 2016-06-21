package parser;

import static model.TeXParagraphParserConstants.DEFAULT_PARAGRAPH_ROLE;
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
    this.texElementRefs = new TeXElementReferences(TEX_ELEMENT_REFERENCES_PATH);
    this.document = document;
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
    TeXParagraph para = new TeXParagraph();
    
    processGroup(document, DEFAULT_PARAGRAPH_ROLE, para, paragraphs);
        
    return paragraphs;
  }
  
  /**
   * Processes the given group. The given role represents the current
   * role context, 'para' represents the current paragraph and 'paras'
   * represents the list of paragraphs where to put identified paragraphs into.
   */
  protected TeXParagraph processGroup(Group group, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {
    return processElements(group.getElements(), role, para, paras);
  }
  
  /**
   * Processes the given elements. The given role represents the current
   * role context, 'para' represents the current paragraph and 'paras'
   * represents the list of paragraphs where to put identified paragraphs into.
   */
  protected TeXParagraph processElements(List<Element> elements, String role,
      TeXParagraph para, List<TeXParagraph> paras) {
    
    Iterator<Element> itr = new Iterator<>(elements);
    while (itr.hasNext()) {
      Element element = itr.next();
      
      if (element instanceof Group) {
        para = processGroup(((Group) element), role, para, paras);
      } else if (element instanceof Command) {
        para = processCommand(((Command) element), role, itr, para, paras);
      } else if (element instanceof Text) {
        para = processText((Text) element, role, itr, para, paras);
      }
    }
    
    return para;
  }
  
  /**
   * Processes the given text element.
   */
  protected TeXParagraph processText(Text textElement, String role, 
      Iterator<Element> itr, TeXParagraph para, List<TeXParagraph> paras) {
    String text = textElement.toString().trim();
        
    // Don't allow text that starts with '@' to ignore commands like
    // "\twocolumn[\csname @twocolumnfalse\endcsname]"
    // FIXME: Check, if this is doesn't ignore regular words.
    if (text.startsWith("@")) {
      return para;
    }
    
    // Check if the element introduces a new paragraph.
    para = checkForParagraphStart(textElement, role, para, paras);
        
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
    
    if (text.isEmpty()) {
      // Replace all whitespaces and newlines by a single whitespace.
      para.registerWhitespace();
    } else {
      para.registerText(text);
      para.registerTeXElement(textElement);
    }
    
    // Check if the element ends a paragraph.
    para = checkForParagraphEnd(textElement, role, para, paras);
    
    return para;
  }
  
  /**
   * Process the given command.
   */
  protected TeXParagraph processCommand(Command cmd, String role, 
      Iterator<Element> itr, TeXParagraph para, List<TeXParagraph> paras) {
    // Check, if the command is a cross reference. TODO
    if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite")) {
      processCrossReferenceCommand(cmd, itr, para);
    }

    TeXElementReference ref = getTeXElementReference(cmd);
    
    if (ref == null) {
      itr.skipTo(guessEndCommand(cmd));
      // Do nothing if there is no element reference for the command.
      return para;
    }
        
    // Check if the element introduces a new paragraph.
    para = checkForParagraphStart(cmd, role, para, paras);
    
    // Role may have changed.
    role = para.getRole();
    
    // FIXME: Because \maketitle has no text to insert but represents a new
    // paragraph we need some special handling here.
    if ("\\maketitle".equals(cmd.toString())) {
      para.registerTeXElement(cmd);
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
            cmd.addVirtualGroup(nextGroup);
          } else if (nextElement instanceof Text) {
            itr.nextNonWhitespace();
            Text textElement = (Text) nextElement;
            // context.elements.set(context.curIndex - 1, null);
            String text = textElement.toString();
            if (!text.trim().isEmpty()) {
              // Create new Group and add it to the command.
              cmd.addVirtualGroup(new Group(textElement));
            }
          }
        }
      }
    }
    
    // Check, if we have to parse the options part of the command.
    if (ref.definesOptionsToParse()) {
      List<Option> options = cmd.getOptions();
      for (Option option : options) {
        para = processGroup(option, role, para, paras);
      }
    }
    
    // Check which groups of the command we have to parse.
    if (ref.definesGroupsToParse()) {
      List<Integer> groupsToParse = ref.getGroupsToParse();
      for (int id : groupsToParse) {
        Group group = cmd.hasGroups(id + 1) ? cmd.getGroup(id + 1) : null;

        if (group != null) {
          para = processGroup(group, role, para, paras);
        }
      }
    }
    
    List<Element> childElements = getChildElements(cmd, itr);

    // Write placeholder, if the command introduces a placeholder.
    if (ref.introducesPlaceholder()) {
      para.registerText(ref.getPlaceholder());
      para.registerTeXElements(childElements);
    } else {
      para = processElements(childElements, role, para, paras);
    }
    
    Element lastElement = cmd;
    if (childElements != null && !childElements.isEmpty()) {
      lastElement = childElements.get(childElements.size() - 1);
    }
    
    // Check if the element ends a paragraph.
    para = checkForParagraphEnd(lastElement, role, para, paras);
    
    return para;
  }
  
  /**
   * Processes a cross reference command (\cite, \label, \ref).
   */
  protected void processCrossReferenceCommand(Command cmd, 
      Iterator<Element> itr, TeXParagraph para) {
    Group group = cmd.getGroup();
    TeXElementReference ref = getTeXElementReference(cmd);
    
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
          para.registerText("[");
          para.registerText(cmd.getName());
          para.registerText("=");
          para.registerText(keys[i].trim());
          para.registerText("]");
          if (i < keys.length - 1) {
            para.registerText(" ");
          }
        }
        
        if (ref != null && !ref.startsParagraphExclusiveElement()) {
          para.registerTeXElement(cmd);
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

//    TeXElementReference startCmdRef = getTeXElementReference(cmd);
//    int outline = startCmdRef != null ? startCmdRef.getOutlineLevel() : -1;
    String endCommand = guessEndCommand(cmd);
    
    if (endCommand != null /*|| outline > 0*/) {
      while (i.hasNext()) {
        Element element = i.next();

        elements.add(element);

        if (element.toString().equals(endCommand)) {
          break;
        }

//        // Check the outline level of the next element.
//        if (i.hasNext()) {
//          Element el = i.peek();
//          if (el instanceof Command) {
//            TeXElementReference cmdRef = getTeXElementReference((Command) el);
//            if (cmdRef != null && cmdRef.definesOutlineLevel()) {
//              if (cmdRef.getOutlineLevel() == outline) {
//                break;
//              }
//            }
//          }
//        }
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
  
  /**
   * Checks if the given element introduces a new paragraph. If so, this method
   * adds the given 'para' to 'paras' (if it is not empty) and creates a new 
   * paragraph. The role of the newly created paragraph is either the role 
   * defined by the element reference (if any) or the given role otherwise.
   * Returns either the newly created paragraph or the given paragraph if no
   * new paragraph was introduced.
   */
  protected TeXParagraph checkForParagraphStart(Element element, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {
    TeXElementReference ref = getTeXElementReference(element); 
    // Obtain if the current element starts a paragraph.
    boolean startsParagraph = ref != null && ref.startsParagraph();
        
    role = ref != null && ref.definesRole() ? ref.getRole() : role;
    role = role == null ? DEFAULT_PARAGRAPH_ROLE : role;
        
    if (startsParagraph) {                
      if (!para.isEmpty()) {
        paras.add(para);
      }
      para = new TeXParagraph(role);
    }
    
    return para;
  }
  
  /**
   * Checks if the given element ends a paragraph. If so, this method
   * adds the given 'para' to 'paras' (if it is not empty) and creates a new 
   * paragraph. The role of the newly created paragraph is defined by the 
   * default role. Returns either the newly created paragraph or the given 
   * paragraph if no new paragraph was introduced.
   */
  protected TeXParagraph checkForParagraphEnd(Element element, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {
    TeXElementReference ref = getTeXElementReference(element);
    
    // Obtain if the current element starts a paragraph.
    boolean endsParagraph = ref != null && ref.endsParagraph();
            
    if (endsParagraph) {                
      if (!para.isEmpty()) {
        paras.add(para);
      }
      para = new TeXParagraph(DEFAULT_PARAGRAPH_ROLE);
    }
    
    return para;
  }
  
  protected TeXElementReference getTeXElementReference(Element element) {
    return this.texElementRefs.getElementReference(element);
  }
}
