package parser;

import static model.TeXParagraphParserSettings.DEFAULT_PARAGRAPH_ROLE;
import static model.TeXParagraphParserSettings.TEX_ELEMENT_REFERENCES_PATH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import de.freiburg.iif.text.StringUtils;
import model.Characters;
import model.Command;
import model.Document;
import model.Element;
import model.Group;
import model.Iterator;
import model.NewLine;
import model.NewParagraph;
import model.Option;
import model.PdfElement;
import model.TeXElementReference;
import model.TeXElementReferences;
import model.TeXParagraph;
import model.Text;
import model.Whitespace;

/**
 * Class to parse Document objects for text paragraphs.
 * 
 * @author Claudius Korzen
 */
public class TeXParagraphsParser_BACKUP {
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
  public TeXParagraphsParser_BACKUP(Document document) throws IOException {
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
   * Processes the given (standalone) option. The given role represents the
   * current role context, 'para' represents the current paragraph and 'paras'
   * represents the list of paragraphs where to put identified paragraphs into.
   * 
   * A standalone option means an option that is not associated with any other
   * command, e.g. "foo bar [1]". Standalone options should be considered as
   * "normal" text.
   * 
   */
  protected TeXParagraph processOption(Option option, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {
    para.registerText("[");
    para = processElements(option.getElements(), role, para, paras);
    para.registerText("]");
    return para;
  }

  /**
   * Processes the given group. The given role represents the current role
   * context, 'para' represents the current paragraph and 'paras' represents the
   * list of paragraphs where to put identified paragraphs into.
   */
  protected TeXParagraph processGroup(Group group, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {
    return processElements(group.getElements(), role, para, paras);
  }

  /**
   * Processes the given elements. The given role represents the current role
   * context, 'para' represents the current paragraph and 'paras' represents the
   * list of paragraphs where to put identified paragraphs into.
   */
  protected TeXParagraph processElements(List<Element> elements, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {

    Iterator<Element> itr = new Iterator<>(elements);
    while (itr.hasNext()) {
      Element element = itr.next();

      if (element instanceof Option) {
        // There may be (valid) text like "foo bar [2]", see cond-mat0001200.
        // So, if the element is a standalone option, handle it as text.
        para = processOption(((Option) element), role, para, paras);
        // TODO
      } else if (element instanceof Group) {
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
      Iterator<Element> itr, TeXParagraph para, 
      List<TeXParagraph> paras) {
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
    if (StringUtils.equals(cmd.getName(), "\\onlinecite")) {
      cmd.setName("\\cite");
    }

    if (StringUtils.equals(cmd.getName(), "\\ref", "\\cite")) {
      processCrossReferenceCommand(cmd, itr, para);
    }

    TeXElementReference ref = getTeXElementReference(cmd, role);

    if (ref == null) {
      itr.skipTo(guessEndCommand(cmd, role));
      // Do nothing if there is no element reference for the command.
      return para;
    }

    // Check if the element introduces a new paragraph.
    para = checkForParagraphStart(cmd, role, para, paras);

    // System.out.println("2 " + cmd + " " + para.hashCode() + " " +
    // para.getOutlineLevel());

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

      if (groupsToParse.size() == 1 && groupsToParse.get(0) == -1) {
        // groupsToParse may contain "-1", that means is not definitely clear
        // how many groups the command contains. So process all groups.
        for (Group group : cmd.getGroups()) {
          para = processGroup(group, role, para, paras);
        }
      } else {
        // Process the group that are defined by groupsToParse.
        for (int id : groupsToParse) {
          Group group = cmd.hasGroups(id + 1) ? cmd.getGroup(id + 1) : null;

          if (group != null) {
            para = processGroup(group, role, para, paras);
          }
        }
      }
    }
    
    System.out.println(cmd + " " + role);
    
    List<Element> childElements = getChildElements(cmd, itr, role);
    
    String roleForChildElements = ref.getRoleForChildElements();
    if (roleForChildElements == null) {
      roleForChildElements = role;
    }

    // Write placeholder, if the command introduces a placeholder.
    if (ref.introducesPlaceholder()) {
      String text = ref.getPlaceholder();
      // TODO: Handle "simple" formulas. Don't resolve separated formulas.
      if ("[formula]".equals(text) && !ref.startsParagraph()) {
        String formulaText = getTextOfSimpleFormula(childElements);
        if (formulaText != null) {
          text = formulaText;
        }
      }

      para.registerText(text);
      para.registerTeXElements(childElements);
    } else {
      para = processElements(childElements, roleForChildElements, para, paras);
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
  protected String getTextOfSimpleFormula(List<Element> elements) {
    StringBuilder sb = new StringBuilder();
    Iterator<Element> itr = new Iterator<>(elements);

    // Only iterate until numElements - 1, because last element is
    // "end math-mode" command. Ignore it.
    while (itr.hasNext()) {
      Element element = itr.next();

      // // TODO: Decide: With or without sub/superscripts?
      // if (isSubscriptCommand(element) || isSuperscriptCommand(element)) {
      // return null;
      // }

      // If there is a run of "_x^y" we want to proceed with superscript first.
      if (isSubscriptCommand(element)) {
        Element next = itr.peekNonWhitespace();
        if (isSuperscriptCommand(next)) {
          next = itr.nextNonWhitespace();

          if (!getTextOfFormulaElement(next, itr, sb)) {
            return null;
          }
          if (!getTextOfFormulaElement(element, itr, sb)) {
            return null;
          }

          continue;
        }
      }

      if (!getTextOfFormulaElement(element, itr, sb)) {
        return null;
      }
    }

    return sb.length() > 0 ? sb.toString() : null;
  }

  protected boolean isSubscriptCommand(Element element) {
    if (!(element instanceof Command)) {
      return false;
    }

    Command cmd = (Command) element;

    return "_".equals(cmd.getName());
  }

  protected boolean isSuperscriptCommand(Element element) {
    if (!(element instanceof Command)) {
      return false;
    }

    Command cmd = (Command) element;

    return "^".equals(cmd.getName());
  }

  /**
   * // * Returns true if the formula could be parsed, false otherwise.
   */
  protected boolean getTextOfFormulaElement(Element element,
      Iterator<Element> itr, StringBuilder sb) {
    List<String> strings = getTextOfFormulaElement(element, itr);

    // TODO: Find better way to determine if given element is sub/super-script.
    boolean isSuperScript = element.toString().startsWith("^");
    boolean isSubScript = element.toString().startsWith("_");

    if (strings == null) {
      return false;
    }

    for (int i = 0; i < strings.size(); i++) {
      String string = strings.get(i);

      if (string == null) {
        return false;
      }

      // Surround specific math words (e.g. "sin") with whitespaces, if it is
      // not sub- or superscript.
      boolean isMathOperator = Characters.MATH_OPERATORS.contains(string);
      boolean hasPredecessor = i > 0 || itr.previous() != null;
      boolean hasSuccessor = i < strings.size() - 1 || itr.hasNext();

      if (isMathOperator && hasPredecessor && !isSuperScript && !isSubScript) {
        sb.append(" ");
      }

      char[] chars = string.toCharArray();
      for (int j = 0; j < chars.length; j++) {
        char character = chars[j];

        boolean charHasPredecessor = hasPredecessor || j > 0;
        boolean charHasSuccessor = hasSuccessor || j < chars.length - 1;
        boolean isMathSymbol =
            Characters.MATH_OPERATORS.contains(String.valueOf(character));
        if (isMathSymbol && charHasPredecessor && !isSuperScript
            && !isSubScript) {
          sb.append(" ");
        }

        sb.append(character);

        if (isMathSymbol && charHasSuccessor && !isSuperScript
            && !isSubScript) {
          sb.append(" ");
        }
      }

      // Surround specific math words (e.g. "sin") with whitespaces.
      if (isMathOperator && hasSuccessor && !isSuperScript && !isSubScript) {
        sb.append(" ");
      }
    }

    return true;
  }

  /**
   * Processes a cross reference command (\cite, \label, \ref).
   */
  protected List<String> getTextOfFormulaElement(Element element,
      Iterator<Element> itr) {
    ArrayList<String> result = new ArrayList<>();

    if (element == null) {
      return null;
    }

    if (element instanceof NewLine) {
      // result.add(" ");
    } else if (element instanceof NewParagraph) {
      // result.add(" ");
    } else if (element instanceof Whitespace) {
      // Nothing to do.
    } else if (element instanceof Option) {
      // TODO: This is a workaround for formulas with brackets ("[", "]")
      // Elements like "[...]" are identified as options but aren't options,
      // because we are in math mode. Identify this in TeX-Parser.
      Option option = (Option) element;
      String text = getTextOfSimpleFormula(option.getElements());
      if (text == null) {
        return null;
      }
      result.add("[" + text + "]");
    } else if (element instanceof Group) {
      Group group = (Group) element;
      String text = getTextOfSimpleFormula(group.getElements());
      if (text == null) {
        return null;
      }
      result.add(text);
    } else if (element instanceof Command) {
      // Check if the element introduces a placeholder, for example:
      // \epsilon -> ɛ

      Command cmd = (Command) element;

      if ("^".equals(cmd.getName())) {
        // TODO: superscripts
        return getTextOfFormulaElement(cmd.getGroup(), itr);
      }

      if ("_".equals(cmd.getName())) {
        // TODO: subscripts
        return getTextOfFormulaElement(cmd.getGroup(), itr);
      }

      // Math space commands.
      if (StringUtils.equals(cmd.toString(),
          "\\quad", "\\qquad", "\\", "\\!", "\\;", "\\:", "\\,")) {
        result.add(" ");
      }

      TeXElementReference ref = getTeXElementReference(cmd, null);

      if (ref == null) {
        return null;
      }

      // Some arguments of some commands won't be defined within a group but as
      // a consecutive string, e.g. "\vksip 5pt".
      // So check, if the command has the expected number of groups. If not,
      // consider the appropriated number of consecutive string as the command's
      // arguments. Example: $\acute e$
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

        // Update the tex element reference.
        ref = getTeXElementReference(cmd, null);
      }

      // Last element could be "$" that resolves to "[formula]"
      if ("[formula]".equals(ref.getPlaceholder())) {
        return result;
      }

      if (ref.getPlaceholder() != null) {
        result.add(ref.getPlaceholder());
      }

      if (ref.definesNumberOfGroups()) {
        int expectedNumGroups = ref.getNumberOfGroups();
        int actualNumGroups = cmd.getGroups().size();

        // In rare cases, the expected number of groups could be smaller than
        // the actual number of groups. For example, in formulas, a text
        // like "\langle{\bf n}" would be considered as a command with one
        // group by the parser. In fact, this is a command and a discrete group.
        // In this case, handle all "superfluous groups" individually.
        if (expectedNumGroups < actualNumGroups) {
          for (int i = expectedNumGroups; i < actualNumGroups; i++) {
            Group group = cmd.getGroup(i + 1); // 1-based.
            String text = getTextOfSimpleFormula(group.getElements());
            if (text == null) {
              return null;
            }
            result.add(text);
          }
        }
      }

    } else if (element instanceof Text) {
      String text = ((Text) element).toString(false, false);
      if (text != null) {
        result.add(text);
      }
    }

    return result;
  }

  /**
   * Processes a cross reference command (\cite, \label, \ref).
   */
  protected void processCrossReferenceCommand(Command cmd,
      Iterator<Element> itr, TeXParagraph para) {
    Group group = cmd.getGroup();
    TeXElementReference ref = getTeXElementReference(cmd, para.getRole());

    // The group may be separated by whitespace.
    // If next element is a group, take this group as argument.
    if (group == null) {
      Element next = itr.peekNonWhitespace();
      if (next instanceof Group) {
        group = (Group) itr.nextNonWhitespace();
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
          // Don't allow whitespace in keys (because they would be split in
          // evaluation, and wouldn't be identified as cross reference command.
          // E.g. "[\ref=foo bar]" would be split into "[\ref=foo" and "bar]".
          // TODO: Also replace whitespaces in related \label command.
          para.registerText(keys[i].trim().replaceAll(" ", "_"));
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
  protected List<Element> getChildElements(Command cmd, 
      Iterator<Element> i, String role) {
    List<Element> elements = new ArrayList<>();

    TeXElementReference startCmdRef = getTeXElementReference(cmd, null);
    int outline = startCmdRef != null && startCmdRef.definesOutlineLevel() 
        ? startCmdRef.getOutlineLevel() : Integer.MAX_VALUE;

    String endCommand = guessEndCommand(cmd, role);
    
    if (endCommand != null || outline < Integer.MAX_VALUE) {
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
            TeXElementReference cmdRef =
                getTeXElementReference((Command) el, null);
            if (cmdRef != null && cmdRef.definesOutlineLevel()) {
              if (cmdRef.getOutlineLevel() == outline) {
                break;
              }
            }
          }
        }
      }
    }
    
    int outlineChilds = getOutlineLevel(elements);
           
    if (outlineChilds < outline) {
      
      while (i.hasNext()) {
        int currentIndex = i.getCurrentIndex();
        
        Element element = i.next();
        
        if (element instanceof Command) {
          Command command = (Command) element;

          int elementOutlineLevel = getOutlineLevel(command);
          if (elementOutlineLevel <= outlineChilds) {
            break;
          }
          
          List<Element> childElements = getChildElements(command, i, role);
          int childElementsOutlineLevel = getOutlineLevel(childElements);
          
          if (childElementsOutlineLevel <= outlineChilds) {
            i.setCurrentIndex(currentIndex);
            break;
          } else {
            elements.add(element);
            elements.addAll(childElements);
          }
        } else {
          elements.add(element);
        }
      }
    }

    //
    // TeXElementReference childElementRef = getTeXElementReference(element,
    // null);
    // int elementOutline = childElementRef != null &&
    // childElementRef.definesOutlineLevel() ? childElementRef.getOutlineLevel()
    // : Integer.MAX_VALUE;
    // if (elementOutline <= childOutline) {
    // break;
    // }
    //
    // if (element instanceof Group) {
    // Group group = (Group) element;
    // for (Element groupElement : group.getElements()) {
    // childElementRef = getTeXElementReference(groupElement, null);
    // elementOutline = childElementRef != null &&
    // childElementRef.definesOutlineLevel() ? childElementRef.getOutlineLevel()
    // : Integer.MAX_VALUE;
    // if (elementOutline <= childOutline) {
    // break outerloop;
    // }
    // }
    // }
    //
    // elements.add(element);
    // }
    // }

    return elements;
  }

  protected int getOutlineLevel(List<Element> childElements) {
    int outlineLevel = Integer.MAX_VALUE;
    
    for (Element element : childElements) {
      int elementOutlineLevel = getOutlineLevel(element);
      
      if (elementOutlineLevel < outlineLevel) {
        outlineLevel = elementOutlineLevel;
      }

      if (element instanceof Group) {
        Group group = (Group) element;
        for (Element groupElement : group.getElements()) {
          elementOutlineLevel = getOutlineLevel(groupElement);
                    
          if (elementOutlineLevel < outlineLevel) {
            outlineLevel = elementOutlineLevel;
          }
        }
      }
    }

    return outlineLevel;
  }

  protected int getOutlineLevel(Element element) {
    TeXElementReference ref = getTeXElementReference(element, null);
    boolean definesOutlineLevel = ref != null && ref.definesOutlineLevel();
    return definesOutlineLevel ? ref.getOutlineLevel() : Integer.MAX_VALUE;
  }
  
  protected String getRole(Element element) {
    TeXElementReference ref = getTeXElementReference(element, null);
    boolean definesRole = ref != null && ref.definesRole();
    return definesRole ? ref.getRole() : null;
  }

  /**
   * Guesses the end command of unknown command (= command without a reference).
   */
  protected String guessEndCommand(Command command, String role) {
    if (command == null) {
      return null;
    }

    // If the command is "\begin{foobar}", it must end with "\end{foobar}".
    if (command.nameEquals("\\begin")) {
      String value = command.getValue();
      return "\\end{" + value + "}";
    }

    // Check, if the references defines a end command for the command.
    TeXElementReference cmdRef = getTeXElementReference(command, role);
    return cmdRef != null ? cmdRef.getEndCommand() : null;
  }

  // ===========================================================================

  /**
   * Checks if the given element introduces a new paragraph. If so, this method
   * adds the given 'para' to 'paras' (if it is not empty) and creates a new
   * paragraph. The role of the newly created paragraph is either the role
   * defined by the element reference (if any) or the given role otherwise.
   * Returns either the newly created paragraph or the given paragraph if no new
   * paragraph was introduced.
   */
  protected TeXParagraph checkForParagraphStart(Element element, String role, 
      TeXParagraph para, List<TeXParagraph> paras) {
    TeXElementReference ref = getTeXElementReference(element, role);
    // Obtain if the current element starts a paragraph.
    boolean startsParagraph = ref != null && ref.startsParagraph();
    
    // Role may be predefined, for example if the element is a child element
    // of aanother element.
    // For example, in \begin{thebibliography} \item ... \end{thebibliography}
    // the role for \item is predined by "reference" to resolve ambiguities
    // with "\item" elements within itemizes.
    if (role == null || role == DEFAULT_PARAGRAPH_ROLE) {
      role = ref != null && ref.definesRole() ? ref.getRole() : role;
      role = role == null ? DEFAULT_PARAGRAPH_ROLE : role; 
    }

    if (startsParagraph) {
      if (!para.isEmpty()) {
        paras.add(para);
        // Introduce a new paragraph.
        para = new TeXParagraph(role);
      }
      para.setOutlineLevel(ref.getOutlineLevel());
    }

    return para;
  }

  TeXParagraph prevNonEmptyParagraph = null;

  /**
   * Checks if the given element ends a paragraph. If so, this method adds the
   * given 'para' to 'paras' (if it is not empty) and creates a new paragraph.
   * The role of the newly created paragraph is defined by the default role.
   * Returns either the newly created paragraph or the given paragraph if no new
   * paragraph was introduced.
   */
  protected TeXParagraph checkForParagraphEnd(Element element, String role,
      TeXParagraph para, List<TeXParagraph> paras) {
    TeXElementReference ref = getTeXElementReference(element, role);

    // Obtain if the current element starts a paragraph.
    boolean endsParagraph = ref != null && ref.endsParagraph();

    if (endsParagraph) {
      if (!para.isEmpty()) {
        paras.add(para);

        para = new TeXParagraph(DEFAULT_PARAGRAPH_ROLE);
      }
    }

    return para;
  }
  
  protected TeXElementReference getTeXElementReference(Element element,
      String role) {
    return this.texElementRefs.getElementReference(element, role);
  }
}
