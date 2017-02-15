package model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A document which conists of tex syntax, like tex- or aux-documents.
 *
 * @author Claudius Korzen
 */
public class Document extends Group {
  /** The serial id. */
  protected static final long serialVersionUID = -3894340026310766394L;
  
  /**
   * The associated file.
   */
  protected Path file;
    
  /**
   * The cross references (cites and labels).
   */
  protected Map<String, Group> crossReferences;
  
  /**
   * The cross references (cites and labels).
   */
  protected Map<String, Group> macroDefinitions;
  
  /**
   * The default constructor.
   */
  public Document() {
    this.crossReferences = new HashMap<>();
    this.macroDefinitions = new HashMap<>();
  }
  
  
  /**
   * Returns the associated file.
   */
  public Path getFile() {
    return this.file;
  }
  
  /**
   * Sets the associated file.
   */
  public void setFile(Path file) {
    this.file = file;  
  }
  
  /**
   * Returns the cross references.
   */
  public Map<String, Group> getCrossReferences() {
    return this.crossReferences;
  }
  
  /**
   * Adds a cross references to this document.
   */
  public void addCrossReference(String name, Group crossReference) {
    this.crossReferences.put(name, crossReference);  
  }
  
  /**
   * Returns the macro definitions.
   */
  public Map<String, Group> getMacroDefinitions() {
    return this.macroDefinitions;
  }
  
  /**
   * Adds a macro definitions to this document.
   */
  public void addMacroDefinition(String name, Group macroDefintion) {
    this.macroDefinitions.put(name, macroDefintion);  
  }
}
