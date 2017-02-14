package iiuf.dom;

import com.sun.xml.tree.ElementEx;
import com.sun.xml.tree.ElementFactory;
import com.sun.xml.tree.ElementNode;

import java.util.HashSet;
import java.util.Hashtable;

/**
   (c) 1999, IIUF<p>

   Element factory that loads a class for each element from a specified
   package.
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class DOMElementFactory
  implements ElementFactory
{
  /** Set to true if you want debugging output. */

  private final static boolean DEBUG = false;

  /** Package where the elements can be found. */

  protected String elementPackage;

  /** Class of the Default element. */

  protected Class defaultClass;

  /** Element name to class cache. */

  protected Hashtable elementClasses;

  /** Set containing the element names for which the Default class is used. */

  protected HashSet defaultElements;

  /** Creates a new DOMElementFactory class.

      @param packageName Name of the package containing the elements which
      are subclasses of ElementNode
      @param defaultName Name of the default element (fully qualified),
      subclass of iiuf.dom.DefaultElement */

  public DOMElementFactory(String packageName, String defaultElementName) 
    throws ClassNotFoundException
  {
    init(packageName, defaultElementName);
  }

  /** Creates a new DOMElementFactory class.

      @param packageName Name of the package containing the elements which
      are subclasses of ElementNode */

  public DOMElementFactory(String packageName) 
  {
    try {
      init(packageName, "iiuf.dom.DefaultElement");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("iiuf.dom.DefaultElement could not be loaded.");
    }
  }

  /** Initializes the class' internal fields.

      @param packageName name of the package where element classes are loaded
      from
      @param defaultElementName fully-qualified name of the default element */

  public void init(String packageName, String defaultElementName)
    throws ClassNotFoundException
  {    
    elementPackage = packageName;
    defaultClass = Class.forName(defaultElementName);

    if (!DefaultElement.class.isAssignableFrom(defaultClass)) {
      throw new IllegalArgumentException
	(defaultElementName + " is no subclass of iiuf.dom.DefaultElement");
    }

    elementClasses = new Hashtable();
    defaultElements = new HashSet();
  }

  /** Creates a new element by trying to load a custom class for it
      from the specified package. The class name is exactly the element's
      tag name. If no such class can be loaded, an instance of the default
      class is returned.
      <p>
      Example: If the package is <tt>iiuf.domelements</tt> and the tag
      name of the element to create is <tt>document</tt>, this object tries
      to load the class <tt>iiuf.domelements.document</tt>.
      
      @param packageName Name of the package containing the elements
      @param defaultName Name of the default element (fully qualified) */

  public ElementEx createElementEx(String tag) {
    String className = elementPackage + "." + tag;
    Class elementClass = (Class) elementClasses.get(className);

    boolean isDefault = false;

    if (elementClass == null) {
      if (defaultElements.contains(className)) {
	isDefault = true;
	elementClass = defaultClass;
      } else {
	try {
	  elementClass = Class.forName(className);
	  elementClasses.put(className, elementClass);
	} catch (ClassNotFoundException e) {
	  defaultElements.add(className);
	  isDefault = true;
	  elementClass = defaultClass;
	}
      }
    }

    ElementEx element = null;

    try {
      element = (ElementEx) elementClass.newInstance();
    } catch (InstantiationException e) {
      System.err.println(e);
      System.exit(1);
    } catch (IllegalAccessException e) {
      System.err.println(e);
      System.exit(1);
    }

    if (isDefault) {
      ((DefaultElement) element).setTag(tag);
    }

    return element;
  }

  public ElementEx createElementEx(String uri, String tag) {
    return createElementEx(tag);
  }
}
