package model;

import java.util.ArrayList;
import java.util.List;

import parse.Token;

/**
 * A command that represnets a macro definition.
 *
 * @author Claudius Korzen
 *
 */
public class MacroDefinition extends Command {
  /** The serial id. */
  protected static final long serialVersionUID = 1814478884419802065L;

  /**
   * The macro command (the command to substitute).
   */
  protected Group key;
  
  /**
   * The substitution.
   */
  protected List<Group> values;
  
  /**
   * Creates a new macro definition.
   */
  public MacroDefinition(String name, Token token) {
    super(name, token);
    this.values = new ArrayList<>();
  }
  
  /**
   * Sets macro command.
   */
  public void setKey(Group group) {
    this.key = group;
  }
  
  /**
   * Returns the defined command.
   */
  public Group getKey() {
    return this.key;
  }
  
  /**
   * Adds a substitution.
   */
  public void addValue(Group group) {
    this.values.add(group);
  }
  
  /**
   * Returns the group to plug in on calling the defined command.
   */
  public List<Group> getValues() {
    return this.values;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName());
    sb.append(getKey().getText());
    if (!getValues().isEmpty()) {
      for (Group v : getValues()) {
        sb.append(v);
      }
    }
    return sb.toString();
  }
  
}
