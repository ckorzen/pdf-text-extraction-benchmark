package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The available references for tex elements.
 *
 * @author Claudius Korzen
 */
public class TeXElementReferences {
  /**
   * The field separator in element references file.
   */
  static final String ELEMENT_REFERENCES_SEPARATOR = ",";

  /**
   * The element references.
   */
  protected Map<String, List<TeXElementReference>> references;

  /**
   * Creates a new element references object based on the given path to the 
   * file where the metadata are stored.
   */
  public TeXElementReferences(String path) throws IOException {
    this.references = readReferences(path);
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns the element references.
   */
  public Map<String, List<TeXElementReference>> getElementReferences() {
    return this.references;
  }

  /**
   * Returns true, if the given element has a reference.
   */
  public boolean hasElementReference(Element element) {
    return getElementReference(element) != null;
  }
  
  /**
   * Returns true, if the given element with given contextRole has a reference.
   */
  public boolean hasElementReference(Element element, String contextRole) {
    return getElementReference(element, contextRole) != null;
  }

  /**
   * Returns true, if the given element has a reference.
   */
  public TeXElementReference getElementReference(Element element) {
    return getElementReference(element, null);
  }
  
  /**
   * Returns the element reference for the given element or null if there is no
   * such reference.
   */
  public TeXElementReference getElementReference(Element element, 
      String contextRole) {
    if (element == null) {
      return null;
    }
    
    String elementString = element.getElementReferenceIdentifier();
    if (elementString == null) {
      return null;
    }
    
    // The references file mostly contains *prefixes* to describe a command,
    // for example: "\title" for a command "\title{foo bar}".
    // So, to identify the correct reference for the given element we have to
    // check for prefixes.
    // TODO: Avoid the iteration through the whole map and disambiguate.
    String matchingKey = null;
    for (String key : this.references.keySet()) {
      if (key != null && elementString.startsWith(key)) {
        // Find the *longest* common prefix.
        if (matchingKey == null || key.length() > matchingKey.length()) {
          matchingKey = key;
        }
      }
    }
     
    List<TeXElementReference> candidates = null;
    
    if (matchingKey != null) {
      if (element instanceof Command) {
        Command cmd = (Command) element;
        
        // If the element is a command, the key of the matching reference *must*
        // contain the whole command name, i.e. it is not allowed to match the
        // command \title{foo bar}" to a reference "\t".
        if (matchingKey.contains(cmd.getName())) {
          candidates = this.references.get(matchingKey);
        }
      } else {
        candidates = this.references.get(matchingKey);
      }
    }
    
    TeXElementReference refWithoutContextRole = null;
    
    if (candidates != null) {      
      // Iterate through the various reference variants for the element and 
      // check if there is a reference with same context role.
      for (TeXElementReference ref : candidates) {
        if (ref.getContextRole() == null) {
          refWithoutContextRole = ref;
        } else {
          if (ref.getContextRole().equals(contextRole)) {
            return ref;
          }
        }
      }
    }
        
    return refWithoutContextRole;
  }

  // ---------------------------------------------------------------------------

  /**
   * Reads the commands to consider.
   */
  protected Map<String, List<TeXElementReference>> readReferences(String path)
    throws IOException {
    Map<String, List<TeXElementReference>> references = new HashMap<>();
    InputStream is = getClass().getResourceAsStream(path);
    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
    BufferedReader reader = new BufferedReader(isr);

    String line;

    while ((line = reader.readLine()) != null) {
      // Ignore comment lines.
      if (line.startsWith("#")) {
        continue;
      }

      // Ignore empty lines.
      if (line.trim().isEmpty()) {
        continue;
      }

      String[] fields = line.split(ELEMENT_REFERENCES_SEPARATOR, -1);
      TeXElementReference ref = new TeXElementReference(fields);
      String name = ref.getCommandName();
      if (!references.containsKey(name)) {
        references.put(name, new ArrayList<TeXElementReference>());
      }
      
      references.get(name).add(ref);
    }

    reader.close();

    return references;
  }
}