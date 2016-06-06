package model;
import static de.freiburg.iif.collection.CollectionUtils.toList;

import java.util.List;

/**
 * Some global constants for TeXParagraphParser
 * 
 * @author Claudius Korzen
 *
 */
public class TeXParagraphParserConstants {
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
  public static final String TEX_ELEMENT_REFERENCES_PATH = "/element-references.csv";
}
