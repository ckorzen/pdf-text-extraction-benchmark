package preprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * The reference of a single command.
 *
 * @author Claudius Korzen
 *
 */
public class CommandReference {
  /** The reference fields. */
  protected String[] fields;

  /**
   * The default constructor.
   */
  public CommandReference(String[] fields) {
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
   * Returns true, if this reference introduces an environment.
   */
  public boolean introducesEnvironment() {
    return definesEndCommand() || definesOutlineLevel();
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
   * Returns true, if this reference defines an outline level.
   */
  public boolean definesOutlineLevel() {
    return getOutlineLevel() > -1;
  }

  /**
   * Returns the defined outline level.
   */
  public int getOutlineLevel() {
    return getInteger(3);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines a context.
   */
  public boolean definesContextName() {
    return getContextName() != null;
  }

  /**
   * Returns the defined context name.
   */
  public String getContextName() {
    return getString(4);
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
    return getInteger(5);
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if this reference defines the parsing of options.
   */
  public boolean definesOptionsToParse() {
    return getInteger(8) > 0;
  }
  
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
    return getIntegerList(6, ";");
  }

  /**
   * Returns the index of the given group id in the list of groups to parse.
   */
  protected int getIndexOfGroupIdInGroupsToParse(int groupId) {
    List<Integer> groupsToParse = getIntegerList(6, ";");
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
   * Returns true, if this reference defines fieldnames of the groups to parse.
   */
  public boolean definesGroupFieldNames() {
    return getGroupFieldNames() != null && !getGroupFieldNames().isEmpty();
  }

  /**
   * Returns the defined groups to parse.
   */
  public List<String> getGroupFieldNames() {
    return getStringList(7, ";");
  }

  /**
   * Returns the defined fieldname of the i-th groups to parse.
   */
  public String getGroupFieldName(int groupId) {
    List<String> groupFieldNames = getGroupFieldNames();
    if (groupFieldNames != null) {
      int index = getIndexOfGroupIdInGroupsToParse(groupId);
      if (index > -1 && index < groupFieldNames.size()) {
        return groupFieldNames.get(index);
      }
    }
    return null;
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
}

