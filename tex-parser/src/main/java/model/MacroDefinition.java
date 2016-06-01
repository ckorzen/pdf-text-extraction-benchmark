package model;

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
   * Creates a new macro definition.
   */
  public MacroDefinition(String name, Token token) {
    super(name, token);
  }
  
  /**
   * Returns the defined command.
   */
  public Command getCommand() {
    if (hasGroups()) {
      return getGroup().first(Command.class);
    }
    return null;
  }
  
  /**
   * Returns the group to plug in on calling the defined command.
   */
  public Group getSubstitution() {
    if (hasGroups(2)) {
      return getGroup(2);
    }
    return null;
  }
  
}
