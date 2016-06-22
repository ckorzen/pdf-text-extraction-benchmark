package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * A reference for a single tex element containing some semantic metadata about
 * the tex element.
 * 
 * @author Claudius Korzen
 *
 */
public class TeXElementReference {
  /** 
   * The underlying array containing the various metadata fields. 
   */
  protected String[] fields;
  
  /**
   * Creates a new reference based on the given metadata array.
   */
  public TeXElementReference(String[] fields) {
    this.fields = fields;
  }
  
  /**
   * Returns the command name.
   */
  public String getCommandName() {
    return getString(0);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines an end command.
   */
  public boolean definesEndCommand() {
    return getEndCommand() != null;
  }

  /**
   * Returns the defined end command.
   */
  public String getEndCommand() {
    return getString(1);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines an placeholder.
   */
  public boolean introducesPlaceholder() {
    return getPlaceholder() != null;
  }

  /**
   * Returns the defined placeholder.
   */
  public String getPlaceholder() {
    return getString(2);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines the introduction of a paragraph.
   */
  public boolean startsParagraph() {
    return startsParagraphExclusiveElement() 
        || startsParagraphInclusiveElement();
  }
  
  /**
   * Returns true, if this reference defines the introduction of a paragraph.
   */
  public boolean startsParagraphInclusiveElement() {
    return getIntroduceParagraphType() == 1;
  }

  /**
   * Returns true, if this reference defines the introduction of a paragraph.
   */
  public boolean startsParagraphExclusiveElement() {
    return getIntroduceParagraphType() == 2;
  }
  
  /**
   * Returns the defined outline level.
   */
  public int getIntroduceParagraphType() {
    return getInteger(3);
  }
  
  // ___________________________________________________________________________
  
  /**
   * Returns true, if this reference defines the end of a paragraph.
   */
  public boolean endsParagraph() {
    return getBoolean(4);
  }

  // ___________________________________________________________________________
  
  /**
   * Returns true, if this reference defines an outline level.
   */
  public boolean definesOutlineLevel() {
    return getOutlineLevel() > -1;
  }

  /**
   * Returns the defined outline level.
   */
  public int getOutlineLevel() {
    return getInteger(5);
  }
  
  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines a role.
   */
  public boolean definesRole() {
    return getRole() != null;
  }

  /**
   * Returns the defined role.
   */
  public String getRole() {
    return getString(6);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines the number of groups.
   */
  public boolean definesNumberOfGroups() {
    return getNumberOfGroups() > -1;
  }

  /**
   * Returns the defined number of groups.
   */
  public int getNumberOfGroups() {
    return getInteger(7);
  }

  // ___________________________________________________________________________
  
  /**
   * Returns true, if this reference defines the groups to parse.
   */
  public boolean definesGroupsToParse() {
    return getGroupsToParse() != null && !getGroupsToParse().isEmpty();
  }

  /**
   * Returns the defined groups to parse.
   */
  public List<Integer> getGroupsToParse() {
    return getIntegerList(8, ";");
  }

  /**
   * Returns the index of the given group id in the list of groups to parse.
   */
  protected int getIndexOfGroupIdInGroupsToParse(int groupId) {
    List<Integer> groupsToParse = getIntegerList(8, ";");
    if (groupsToParse != null) {
      for (int i = 0; i < groupsToParse.size(); i++) {
        if (groupsToParse.get(i) == groupId) {
          return i;
        }
      }
    }
    return -1;
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines the parsing of options.
   */
  public boolean definesOptionsToParse() {
    return getInteger(9) > 0;
  }
  
  // ___________________________________________________________________________
  

  @Override
  public String toString() {
    return Arrays.toString(fields);
  }

  // ___________________________________________________________________________

  /**
   * Returns the value of the i-th field in the reference.
   */
  protected String getString(int index) {
    if (index >= 0 && index < fields.length) {
      String value = "".equals(fields[index]) ? null : fields[index];
      if (value != null) {
        return StringEscapeUtils.unescapeCsv(value);
      }
    }
    return null;
  }

  /**
   * Splits the value of the i-th field at the given delimiter and returns the
   * resulting fields as an list of strings.
   */
  protected List<String> getStringList(int index, String delimiter) {
    List<String> result = new ArrayList<>();
    String string = getString(index);
    if (string != null) {
      String[] values = string.split(delimiter, -1);
      for (String value : values) {
        result.add(value);
      }
    }
    return result;
  }

  /**
   * Returns the value of the i-th field as an integer.
   */
  protected int getInteger(int index) {
    String value = getString(index);
    return value != null ? Integer.parseInt(value) : -1;
  }

  /**
   * Splits the value of the i-th field at the given delimiter and returns the
   * resulting fields as an list of integers.
   */
  protected List<Integer> getIntegerList(int index, String delimiter) {
    List<Integer> result = new ArrayList<>();
    String string = getString(index);
    if (string != null) {
      String[] values = string.split(delimiter, -1);
      for (String value : values) {
        result.add(Integer.parseInt(value));
      }
    }
    return result;
  }
  
  /**
   * Returns the value of the i-th field as an booleanb.
   */
  protected boolean getBoolean(int index) {
    int value = getInteger(index);
    return value == 1;
  }
}
