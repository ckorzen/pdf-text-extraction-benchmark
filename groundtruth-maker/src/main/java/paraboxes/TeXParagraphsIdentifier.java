package paraboxes;

import static de.freiburg.iif.affirm.Affirm.affirm;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import model.Command;
import model.Document;
import model.Element;
import model.Group;
import model.NewLine;
import model.Option;
import model.TexParagraph;
import model.Whitespace;
import model.TeXFile;
import parse.TeXParser;

/**
 * Identifies text paragraphs in tex files.
 *
 * @author Claudius Korzen
 */
public class TeXParagraphsIdentifier {
  /**
   * The tex file to process.
   */
  protected final TeXFile texFile;

  /**
   * Creates a new paragraphs identifier for the given tex file.
   */
  public TeXParagraphsIdentifier(TeXFile texFile) {
    affirm(texFile != null, "No tex file given.");

    this.texFile = texFile;
  }

  /**
   * Identifies paragraphs in the tex file, i.e. the start line, start column,
   * end line and end column of a paragraph. The order of paragraphs in the
   * output corresponds to the order of paragraphs in tex file.
   */
  public void identify() throws Exception {
    List<TexParagraph> paragraphs = processDocument(parse());

    this.texFile.setTeXParagraphs(paragraphs);
  }

  // ___________________________________________________________________________

  /**
   * Parses the given tex file.
   */
  protected Document parse() throws Exception {
    InputStream is = Files.newInputStream(this.texFile.getPath());
    Document document = new TeXParser(is).parse();
    is.close();
    return document;
  }

  // ___________________________________________________________________________

  /**
   * Identifies paragraphs in the given parsed tex file.
   */
  protected List<TexParagraph> processDocument(Document document) {
    affirm(document != null, "No document given.");

    // List<TexParagraph> paragraphs = new ArrayList<>();
    // processElements(document.elements, paragraphs);
    // return paragraphs;
    return processElements(document.elements);
  }

  /**
   * Identifies paragraphs in the given list of elements.
   */
  protected List<TexParagraph> processElements(List<Element> elements) {
    affirm(elements != null, "No elements given.");

    List<TexParagraph> paragraphs = new ArrayList<>();
    TexParagraph paragraph = new TexParagraph();

    Element prevElement = null;
    for (Element element : elements) {
      if (ignoreElement(element)) {
        continue;
      }

      if (element instanceof Group) {
        List<Element> groupElements = ((Group) element).elements;
        List<TexParagraph> groupParagraphs = processElements(groupElements);
        if (groupParagraphs.size() == 0) {
          continue;
        } else if (groupParagraphs.size() == 1) {
          paragraph.extend(groupParagraphs.get(0));
        } else {
          if (paragraph.getNumTexElements() > 0) {
            paragraphs.add(paragraph);
          }
          paragraphs.addAll(groupParagraphs);
          paragraph = new TexParagraph();
        }
      } else if (element instanceof Option) {
        List<Element> optionElements = ((Option) element).elements;
        List<TexParagraph> optionParagraphs = processElements(optionElements);
        if (optionParagraphs.size() == 0) {
          continue;
        } else if (optionParagraphs.size() == 1) {
          paragraph.extend(optionParagraphs.get(0));
        } else {
          if (paragraph.getNumTexElements() > 0) {
            paragraphs.add(paragraph);
          }
          paragraphs.addAll(optionParagraphs);
          paragraph = new TexParagraph();
        }
      } else {
        ParagraphSplitType type = computeParaSplitType(prevElement, element);

        if (type != null) {
          // 'type' is not null, i.e. there is a paragraph split.
          // Add the previous paragraph to result, if it is not empty and
          // create a new one.
          if (paragraph.getNumTexElements() > 0) {
            paragraphs.add(paragraph);
          }
          paragraph = new TexParagraph();
        }

        // Add the element to the paragraph, if the type allows it.
        if (type != ParagraphSplitType.EXCLUSIVE_NEXT_ELEMENT) {
          paragraph.addTexElement(element);

          // Also process all options and groups of a command.
          if (element instanceof Command) {
            Command command = (Command) element;
            if (command.hasOptions()) {
              for (Option option : command.getOptions()) {
                List<TexParagraph> optionParas =
                    processElements(option.elements);
                if (optionParas.size() == 0) {
                  continue;
                } else if (optionParas.size() == 1) {
                  paragraph.extend(optionParas.get(0));
                } else {
                  if (paragraph.getNumTexElements() > 0) {
                    paragraphs.add(paragraph);
                  }
                  paragraphs.addAll(optionParas);
                  paragraph = new TexParagraph();
                }
              }
            }
            if (command.hasGroups()) {
              for (Group group : command.getGroups()) {
                List<TexParagraph> groupParas = processElements(group.elements);
                if (groupParas.size() == 0) {
                  continue;
                } else if (groupParas.size() == 1) {
                  paragraph.extend(groupParas.get(0));
                } else {
                  if (paragraph.getNumTexElements() > 0) {
                    paragraphs.add(paragraph);
                  }
                  paragraphs.addAll(groupParas);
                  paragraph = new TexParagraph();
                }
              }
            }
          }
        }
      }
      prevElement = element;
    }

    if (paragraph.getNumTexElements() > 0) {
      paragraphs.add(paragraph);
    }

    return paragraphs;
  }

  /**
   * Computes the type of paragraph split between the two given elements.
   * Returns null if there is no paragraph split.
   */
  protected ParagraphSplitType computeParaSplitType(Element prevElement,
      Element element) {
    if (prevElement == null || element == null) {
      return null;
    }

    String prevElementStr = prevElement.toString();
    String elementStr = element.toString();

    for (String s : inclusiveParagraphSplitElements) {
      if (elementStr.startsWith(s)) {
        return ParagraphSplitType.INCLUSIVE_NEXT_ELEMENT;
      }
    }

    for (String s : exclusiveParagraphSplitElements) {
      if (elementStr.startsWith(s)) {
        return ParagraphSplitType.EXCLUSIVE_NEXT_ELEMENT;
      }
    }

    // Check, if a paragraph ends within the two elements.
    for (String s : endParagraphElements) {
      if (elementStr.startsWith(s)) {
        return ParagraphSplitType.EXCLUSIVE_NEXT_ELEMENT;
      }

      if (prevElementStr.startsWith(s)) {
        return ParagraphSplitType.INCLUSIVE_NEXT_ELEMENT;
      }
    }

    return null;
  }

  /**
   * The different types of a paragraph split.
   */
  public enum ParagraphSplitType {
    /**
     * Describes a paragraph split, where the current element implies a
     * paragraph split and the element *should* be a member of the paragraph
     * (e.g. '\section{Introduction}').
     */
    INCLUSIVE_NEXT_ELEMENT,
    /**
     * Describes a paragraph split, where the current element implies a
     * paragraph split and the element *shouldn't* be a member of the paragraph
     * (e.g. '\par').
     */
    EXCLUSIVE_NEXT_ELEMENT
  }

  /**
   * Returns true if the given element should be ignored.
   */
  protected boolean ignoreElement(Element element) {
    if (element == null) {
      return true;
    }

    if (element instanceof NewLine || element instanceof Whitespace) {
      return true;
    }

    if (element.toString().startsWith("\\label")) {
      return true;
    }

    return false;
  }

  /**
   * The list of elements that implies a paragraph split and the element is
   * *not* a member of the paragraph.
   */
  protected List<String> exclusiveParagraphSplitElements = createList(
      "\n\n",
      "\\begin{",
      "\\bibitem");

  /**
   * The list of elements that implies a paragraph split and the element *is* a
   * member of the paragraph.
   */
  protected List<String> inclusiveParagraphSplitElements = createList(
      "\\begin{abstract}",
      "\\section",
      "\\subsection",
      "\\subsubsection");

  /**
   * The list of elements that implies the end of a paragraph.
   */
  protected List<String> endParagraphElements = createList(
      "\\end{abstract}",
      "\\end{",
      "\\section",
      "\\subsection",
      "\\subsubsection");

  /**
   * Returns a list containing the given strings.
   */
  public static List<String> createList(String... strings) {
    List<String> list = new ArrayList<String>();

    for (String s : strings) {
      list.add(s);
    }
    return list;
  }
}