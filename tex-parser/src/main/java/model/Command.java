package model;

import java.util.ArrayList;
import java.util.List;

import parse.Token;

/**
 * The class representing a latex command like "\foobar{foo}". A command 
 * consists of a command name ("\foobar") and an arbitrary number of groups
 * ("{foo}").
 *
 * @author Claudius Korzen
 */
public class Command extends Element {
  /**
   * The serial id.
   */
  private static final long serialVersionUID = -3707468295489014361L;
  
  /**
   * The name of the command, including the preceding "\" (or "$" in case of a 
   * formula).
   */
  protected String name;
  
  /**
   * The options of this command.
   */
  protected List<Option> options;
  
  /**
   * The groups of this command.
   */
  protected List<Group> groups;
        
  /**
   * The list of options and groups of this command. The order in list 
   * corresponds to the order in tex file.
   */
  protected List<Element> optionsAndGroupsInCorrectOrder;
  
  /**
   * Flag to indicate whether this command is a macro.
   */
  protected boolean isMacro;
  
  // ___________________________________________________________________________
  
  /**
   * Creates a new command with the given name.
   */
  public Command(String name, Token token) {
    super(token);
    this.groups = new ArrayList<>();
    this.options = new ArrayList<>();
    this.optionsAndGroupsInCorrectOrder = new ArrayList<>();
    this.name = name;
  }
  
  // ___________________________________________________________________________
  
  /**
   * Adds the given option to this command.
   */
  public void addOption(Option option) {
    this.options.add(option);
    this.optionsAndGroupsInCorrectOrder.add(option);
    this.beginLine = Math.min(this.beginLine, option.beginLine);
    this.endLine = Math.max(this.endLine, option.endLine);
    this.beginColumn = Math.min(this.beginColumn, option.beginColumn);
    this.endColumn = Math.max(this.endColumn, option.endColumn);
  }
  
  /**
   * Adds the given group to this command.
   */
  public void addGroup(Group group) {
    this.groups.add(group);
    this.optionsAndGroupsInCorrectOrder.add(group);
    this.beginLine = Math.min(this.beginLine, group.beginLine);
    this.endLine = Math.max(this.endLine, group.endLine);
    this.beginColumn = Math.min(this.beginColumn, group.beginColumn);
    this.endColumn = Math.max(this.endColumn, group.endColumn);
  }
 
  /**
   * Returns the options of this command.
   */
  public List<Option> getOptions() {
    return this.options;
  }
  
  /**
   * Returns true, if this command contains at least one option.
   */
  public boolean hasOptions() {
    return hasOptions(1);
  }
  
  /**
   * Returns true, if this command contains at least i options.
   */
  public boolean hasOptions(int i) {
    return this.groups.size() >= i;
  }
  
  /**
   * Returns the groups of this command.
   */
  public List<Group> getGroups() {
    return this.groups;
  }
 
  /**
   * Returns true, if this command contains at least one group.
   */
  public boolean hasGroups() {
    return hasGroups(1);
  }
  
  /**
   * Returns true, if this command contains at least i groups.
   */
  public boolean hasGroups(int i) {
    return this.groups.size() >= i;
  }
  
  /**
   * Returns the first group of this command.
   */
  public Group getGroup() {
    return getGroup(1);
  }
 
  /**
   * Returns the last group of this command.
   */
  public Group getLastGroup() {
    return getGroup(this.groups.size());
  }
  /**
   * Returns the i-th group of this command.
   */
  public Group getGroup(int i) {
    if (i > 0 && i <= this.groups.size()) {
      return this.groups.get(i - 1);  
    }
    return null;
  }
  
  /**
   * Returns the first group as string.
   */
  public String getValue() {
    return hasGroups() ? getGroup().getText() : null;
  }
  
  // ___________________________________________________________________________
  
  /**
   * Returns the name of this command.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the name of this command.
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Returns true, if the name of this command starts with the given string.
   */
  public boolean nameEquals(String command) {
    return name.equals(command) || name.equals("\\" + command);
  }
  
  // ___________________________________________________________________________
  
  /**
   * Returns true if this command is a macro.
   */
  public boolean isMacro() {
    return isMacro;
  }
  
  /**
   * Sets the isMacro flag.
   */
  public void setIsMacro(boolean isMacro) {
    this.isMacro = isMacro;
  }
  
  // ___________________________________________________________________________
  
  /**
   * Returns a string representation of this command containing at most the 
   * first group. For example, for the command "\begin{thebibliography}{99}"
   * this method will return the string "\begin{thebibliography}".
   */
  public String toShortString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName());
    sb.append(hasGroups(1) ? getGroup(1) : "");
    return sb.toString();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName());
    for (Element optionOrGroup : optionsAndGroupsInCorrectOrder) {
      sb.append(optionOrGroup);
    }
    return sb.toString();
  }
  
  /**
   * The \begin{document} command.
   */
  public static final String BEGIN_DOCUMENT = "\\begin{document}";
  
  /**
   * The \end{document} command.
   */
  public static final String END_DOCUMENT = "\\end{document}";
}
