package model;

import static de.freiburg.iif.collection.CollectionUtils.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some global constants for TeXParagraphParser.
 * 
 * @author Claudius Korzen
 *
 */
public class TeXParagraphParserSettings {
  /**
   * The list of file extensions to consider on scanning input directories.
   */
  public static final List<String> TEX_EXTENSIONS = toList(".tex");

  /**
   * The list of file extensions to consider on scanning intermediate files.
   */
  public static final List<String> TMP_TEX_EXTENSIONS = toList(".tmp.tex");
  
  /** 
   * The path to the tex element references. 
   */
  public static final String TEX_ELEMENT_REFERENCES_PATH = 
      "/element-references.csv";
  
  /**
   * The default role for a paragraph.
   */
  public static final String DEFAULT_PARAGRAPH_ROLE = "text";
  
  /**
   * Returns the available role profiles.
   */
  public static Map<String, List<String>> getRoleProfiles() {
    Map<String, List<String>> profiles = new HashMap<>();    
    
    // Also add "formula", "figure" because they can build an own
    // paragraph. Furthermore, a placeholder is added for these roles such that
    // we are able to ignore them on evaluation.
    profiles.put("body", Arrays.asList("title", "heading", "text", 
        "listing-item", "formula"));
    
    return profiles;
  }
}
