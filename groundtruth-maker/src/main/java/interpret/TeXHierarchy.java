package interpret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that represents the hierarchy of a tex document.
 *
 * @author Claudius Korzen
 */
public class TeXHierarchy {
  /**
   * The map where all the properties are kept.
   */
  protected final Map<String, Object> map;

  /**
   * Flag that indicates whether we have to insert a whitespace before 
   * inserting the next text.
   */
  protected boolean introduceWhiteSpace;
    
  /**
   * Flag that indicates whether we have to insert a new paragraph before 
   * inserting the next text.
   */
  protected boolean introduceNewParagraph;
  
  
  /**
   * Construct an empty hierarchy.
   */
  public TeXHierarchy() {
    this.map = new LinkedHashMap<>();
  }

  /**
   * Inserts a new context under the given key.
   */
  @SuppressWarnings("unchecked")
  public void insertNewContext(String key, TeXHierarchy hierarchy) {
    if (key == null || hierarchy == null) {
      return;
    }
    
    Object object = this.map.get(key);
    
    // Need to check, if there is already an object given for the key.
    
    if (object == null) {
      // There is no existing value for the given key. Add the context to map.
      this.map.put(key, hierarchy);
    } else if (object instanceof List) {
      // There is already a list for the key. Add the context to this list.
      List<Object> contexts = (List<Object>) object;
      contexts.add(hierarchy);
    } else {
      // There is already any other object for the given key.
      // Wrap this object and the new context in a new list and add this list 
      // to map.
      List<Object> list = new ArrayList<>();
      list.add(object);
      list.add(hierarchy);
      this.map.put(key, list);
    }
  }
  
  /**
   * Writes text to the context given by key.
   */
  @SuppressWarnings("unchecked")
  public void writeText(String key, String text) {   
    if (key == null || text == null) {
      return;
    }
    
    Object object = this.map.get(key);    
    
    if (object == null) {
      // No existing object. Insert the text.
      this.map.put(key, new StringBuilder(text));
    } else if (object instanceof List) {
      // There exists a list for the given key.
      // Distinguish if we have to append the text to the end of the list or 
      // to append to the last element of the list. 
      List<StringBuilder> paragraphs = (List<StringBuilder>) object;
      if (paragraphs.isEmpty() || introduceNewParagraph) {
        // Add the text to the end of the list.
        paragraphs.add(new StringBuilder(text));
      } else {
        // Add the text to the last element of the list.
        StringBuilder lastParagraph = paragraphs.get(paragraphs.size() - 1);
        
        lastParagraph.append(introduceWhiteSpace ? " " : "");
        lastParagraph.append(text);
      }
    } else if (object instanceof StringBuilder) {
      // There exists text for the given key.
      StringBuilder existingText = (StringBuilder) object;
      
      if (introduceNewParagraph) {
        // Create a list from existing text and new text.
        List<StringBuilder> paragraphs = new ArrayList<>();
        paragraphs.add(existingText);
        paragraphs.add(new StringBuilder(text));
        this.map.put(key, paragraphs);
      } else {
        // Append the text to the existing text.
        existingText.append(introduceWhiteSpace ? " " : "");
        existingText.append(text);
      }
    }
    
    introduceWhiteSpace = false;
    introduceNewParagraph = false;
    
    if (object instanceof TeXHierarchy) {
      throw new IllegalStateException("You can't write to a hierarchy object");
    }
  }
  
  /**
   * Inserts a new whitespace.
   */
  public void writeWhiteSpace(String key) {
    // set flag to insert new whitespace.
    this.introduceWhiteSpace = true;
  }
  
  /**
   * Inserts a new line.
   */
  public void writeNewLine(String key) {
    // set flag to insert new line.
    this.introduceWhiteSpace = true;
  }
  
  /**
   * Inserts a new paragraph.
   */
  public void writeNewParagraph(String key) {
    // set flag to insert new paragraph.
    this.introduceNewParagraph = true;
  }
    
  @Override
  public String toString() {
    return this.map.toString();
  }
  
  /**
   * Returns the keys of this hierarchy.
   */
  public Iterable<String> keys() {
    return this.map.keySet();
  }
  
  /**
   * Returns the keys of this hierarchy.
   */
  public Object get(String key) {
    if (key == null) {
      return null;
    }
    return this.map.get(key);
  }
}
