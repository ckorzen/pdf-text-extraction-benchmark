package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.iif.collection.ConstantLookupList;

/**
 * A group in a tex file, that is a bunch of elements wrapped in "{...}".
 *
 * @author Claudius Korzen
 */
public class Group extends Element implements Iterable<Element> {
  /** The serial id. */
  protected static final long serialVersionUID = -6615630147305656329L;

  /**
   * The elements of this group.
   */
  public ConstantLookupList<Element> elements;
  /**
   * The elements without leading and trailing whitespaces.
   */
  public ConstantLookupList<Element> trimmedElements;
  
  public List<Element> whitespaceQueue;
  
  /**
   * The current element on iteration.
   */
  public int curIndex = 0;
     
  /**
   * Creates a new group.
   */
  public Group() {
    super(null);
    this.elements = new ConstantLookupList<>();
    this.trimmedElements = new ConstantLookupList<>();
    this.whitespaceQueue = new ArrayList<>();
  }
  
  /**
   * Creates a new group containing the given elements.
   */
  public Group(Element... elements) {
    this();
    for (Element element : elements) {
      addElement(element);
    }
  }

  /**
   * Adds the given element to this group.
   */
  public void addElement(Element element) {
    if (element != null) {      
      this.elements.add(element);
      
      this.beginLine = Math.min(this.beginLine, element.beginLine);
      this.endLine = Math.max(this.endLine, element.endLine);
      
      // Get rid of leading and trialing whitespaces.
      if (element instanceof Whitespace
          || element instanceof NewLine
          || element instanceof NewParagraph) {
        if (this.trimmedElements.isEmpty()) {
          return;
        } else {
          whitespaceQueue.add(element);
          return;
        }
      }
      // The element is non-whitespace, so the elements in whitespace queue 
      // can't be trailing. Append them to elements. 
      trimmedElements.addAll(whitespaceQueue);
      whitespaceQueue.clear();
      trimmedElements.add(element);
    }
  }
    
  /**
   * Returns true, if this group has still more elements.
   */
  public boolean hasNext() {
    return elements.size() > 0 && (curIndex < elements.size());
  }
  
  /**
   * Sets the internal pointer to the given element.
   */
  public void reposition(Element element) {
    int index = elements.indexOf(element);
    if (index >= 0) {
      curIndex = index  + 1;
    }
  }
  
  /**
   * Returns the next element.
   */
  public Element next() {
    return elements.get(curIndex++);
  }
  
  /**
   * Returns the next non whitespace element.
   */
  public Element nextNonWhitespace() {
    while (hasNext()) {
      Element element = next();
      if (!(element instanceof Whitespace) && !(element instanceof NewLine)) {
        return element;
      }
    }
    return null;
  }
  
  /**
   * Peeks the next element.
   */
  public Element peek() {
    return elements.get(curIndex);
  }
  
  /**
   * Returns the next element of the given types.
   */
  public <T extends Element> T next(Class<T> clazz) {
    for ( ; curIndex < elements.size(); curIndex++) {
      if (clazz.isInstance(elements.get(curIndex))) {
        return clazz.cast(elements.get(curIndex));
      }
    }
    return null;
  }
  
  /**
   * Returns the first element of the given type.
   */
  public <T extends Element> T first(Class<T> clazz) {
    for (int i = 0; i < elements.size(); i++) {
      if (clazz.isInstance(elements.get(i))) {
        return clazz.cast(elements.get(i));
      }
    }
    return null;
  }
  
  /**
   * Returns all elements of the given type in this group.
   */
  public <T> List<T> get(Class<T> clazz, boolean recursive) {
    List<T> list = new ArrayList<>();
    
    if (recursive) {
      getRecursive(clazz, this, list);
    } else {
      for (int i = 0; i < elements.size(); i++) {
        if (clazz.isInstance(elements.get(i))) {
          list.add(clazz.cast(elements.get(i)));
        }
      }
    }
    return list;
  }
  
  /**
   * Returns all elements of the given type in this group.
   */
  protected <T> void getRecursive(Class<T> clazz, Group group, List<T> res) {
    for (int i = 0; i < group.elements.size(); i++) {
      Element element = group.elements.get(i);
      
      if (clazz.isInstance(element)) {
        res.add(clazz.cast(element));
      }
      
      if (element instanceof Group) {
        getRecursive(clazz, (Group) element, res);
      }
      
      if (element instanceof Command) {
        Command command = (Command) element;
        for (Group commandGroup : command.getGroups()) {
          getRecursive(clazz, commandGroup, res);  
        }
      }
    }
  }
  
  /**
   * Replaces the first element by the second element. 
   */
  // TODO: Try to get rid of this method.
  public void replace(Element element, List<Element> elements) { 
    replaceRecursive(this, element, elements);
  }
  
  /**
   * Replaces the first element by the second element. 
   */
  protected <T extends Element> void replaceRecursive(Group group,
      Element element1, List<Element> elements) {    
    group.elements.replace(element1, elements);
    
    for (int i = 0; i < group.elements.size(); i++) {
      Element element = group.elements.get(i);
            
      if (element instanceof Group) {
        replaceRecursive((Group) element, element1, elements);
      }
      
      if (element instanceof Command) {
        Command command = (Command) element;
        for (Group commandGroup : command.getGroups()) {
          replaceRecursive(commandGroup, element1, elements);  
        }
      }
    }
  }
    
  // ___________________________________________________________________________
  
  /**
   * Returns the elements of this group.
   */
  public List<Element> getElements() {
    return this.elements;
  }
  
  /**
   * Returns the elements of this group without leading and trailing 
   * whitespaces.
   */
  public List<Element> getTrimmedElements() {
    return this.trimmedElements;
  }
  
  /**
   * Returns the first element in this group.
   */
  public Element getFirstElement() {
    if (this.elements != null && !this.elements.isEmpty()) {
      return this.elements.get(0);  
    }
    return null;
  }
  
  /**
   * Returns the last element in this group.
   */
  public Element getLastElement() {
    if (this.elements != null && !this.elements.isEmpty()) {
      return this.elements.get(this.elements.size() - 1);  
    }
    return null;
  }
  
  /**
   * Returns the text of this group.
   */
  public String getText() {
    StringBuilder sb = new StringBuilder();
    if (elements != null) {
      for (Element element : elements) {
        if (element != null) {
          sb.append(element.toString());
        }
      }
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return toString(true, true);
  }

  public String toString(boolean withSuperScripts, boolean withSubscripts) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append(getText());
    sb.append("}");
    return sb.toString();
  }
  
  public Group clone() {
    try {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(baos)) {
        oos.writeObject(this);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais)) {
          Group group = (Group) ois.readObject();          
          return group;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public Iterator<Element> iterator() {
    return iterator(null, null);
  }
  
  /**
   * Returns an iterator object to iterate the elements of this group.
   */
  public Iterator<Element> iterator(String start, String end) {
    return new Iterator<>(elements, start, end);
  }
}
