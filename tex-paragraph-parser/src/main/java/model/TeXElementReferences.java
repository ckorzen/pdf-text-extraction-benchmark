package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
  protected Map<String, TeXElementReference> references;

  /**
   * Creates a new element references object based on the given path to the 
   * file where the metadata are stored.
   */
  public TeXElementReferences(String path) throws IOException {
    this(Paths.get(path));
  }

  /**
   * Creates a new element references object based on the given path to the 
   * file where the metadata are stored.
   */
  public TeXElementReferences(Path path) throws IOException {
    this.references = readReferences(path);
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns the element references.
   */
  public Map<String, TeXElementReference> getElementReferences() {
    return this.references;
  }

  /**
   * Returns true, if the given element has a reference.
   */
  public boolean hasElementReference(Element element) {
    return getElementReference(element) != null;
  }

  /**
   * Returns the element reference for the given element or null if there is no
   * such reference.
   */
  public TeXElementReference getElementReference(Element element) {
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
     
    if (matchingKey != null) {
      if (element instanceof Command) {
        Command cmd = (Command) element;
        
        // If the element is a command, the key of the matching reference *must*
        // contain the whole command name, i.e. it is not allowed to match the
        // command \title{foo bar}" to a reference "\t".
        if (matchingKey.contains(cmd.getName())) {
          return this.references.get(matchingKey);
        }
      } else {
        return this.references.get(matchingKey);
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------

  /**
   * Reads the commands to consider.
   */
  protected Map<String, TeXElementReference> readReferences(Path path)
    throws IOException {
    Map<String, TeXElementReference> references = new HashMap<>();
    BufferedReader reader = Files.newBufferedReader(path);

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
      references.put(ref.getCommandName(), ref);
    }

    reader.close();

    return references;
  }
}