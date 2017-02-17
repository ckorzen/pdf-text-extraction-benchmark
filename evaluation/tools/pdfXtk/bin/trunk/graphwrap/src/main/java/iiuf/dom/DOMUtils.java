package iiuf.dom;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
   (c) 2000, IIUF<p>

   DOM Utilities
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class DOMUtils {

  /** Get all descendant elements.
   
      @param element Element whose descendants we want to search for elements
      @return List of elements */

  public static NodeList getDescendantElements(Element element) {
    ElementList list = new ElementList();
    getDescendants(list, element);
    return list;
  }

  private static void getDescendants(ElementList list, Element current) {
    NodeList nl = getChildElements(current);
    for (int i = 0; i < nl.getLength(); i++) {
      Element e = (Element) nl.item(i);
      list.add(e);
      getDescendants(list, e);
    }
  }

  /** Get all descendant elements with a certain tag name.
   
      @param element Element whose descendants we want to search for elements
      @param tagname Desired tag name
      @return List of elements */

  public static NodeList getDescendantElements(Element element, String tagname) {
    ElementList list = new ElementList();
    getDescendants(list, element, tagname);
    return list;
  }

  private static void getDescendants(ElementList list, Element current, String tagname) {
    NodeList nl = getChildElements(current);
    for (int i = 0; i < nl.getLength(); i++) {
      Element e = (Element) nl.item(i);
      if (e.getTagName().equals(tagname)) {
	list.add(e);
      }
      getDescendants(list, e, tagname);
    }
  }

  /** Get all descendant elements with one of the tagnames.
   
      @param element Element whose descendants we want to search for elements
      @param tagnames list of desired tag names
      @return List of elements */

  public static NodeList getDescendantElements(Element element, LinkedList tagnames) {
    ElementList list = new ElementList();
    getDescendants(list, element, tagnames);
    return list;
  }

  private static void getDescendants(ElementList list, Element current, LinkedList tagnames) {
    NodeList nl = getChildElements(current);
    for (int i = 0; i < nl.getLength(); i++) {
      Element e = (Element) nl.item(i);
      if (tagnames.contains(e.getTagName())) {
	list.add(e);
      }
      getDescendants(list, e, tagnames);
    }
  }

  /** Remove all descendant elements with a certain tag name.
   
      @param element Element whose descendants we want to search for elements
      @param tagname Desired tag name
  */

  public static void removeDescendantElements(Element element, String tagname) {
    NodeList nl = getChildElements(element);
    for (int i = 0; i < nl.getLength(); i++) {
      Element e = (Element) nl.item(i);
      if (e.getTagName().equals(tagname)) {
	element.removeChild(e);
      }
      else {
	removeDescendantElements(e, tagname);
      }
    }
  }

  /** True if element has children elements
      @param element element that we wanted to know if it has children 
      @return true if element has children elements
  */

  public static boolean hasChildElements(Element element) {
    NodeList childs = element.getChildNodes();
    
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	return true;
      }
    }
    return false;
  }

  /** Get all element children of an element.
   
      @param element Element whose children we want to search for elements
      @return List of elements */

  public static NodeList getChildElements(Element element) {
    NodeList childs = element.getChildNodes();
    ElementList list = new ElementList();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
        list.add(node);
      }
    }

    return list;
  }

  public final static int TYPE_ATTRIBUTE              = 0x0001;
  public final static int TYPE_CDATA                  = 0x0002;
  public final static int TYPE_COMMENT                = 0x0004;
  public final static int TYPE_DOCUMENT_FRAGMENT      = 0x0008;
  public final static int TYPE_DOCUMENT               = 0x0010;
  public final static int TYPE_DOCUMENT_TYPE          = 0x0020;
  public final static int TYPE_ELEMENT                = 0x0040;
  public final static int TYPE_ENTITY                 = 0x0080;
  public final static int TYPE_NOTATION               = 0x0100;
  public final static int TYPE_PROCESSING_INSTRUCTION = 0x0200;
  public final static int TYPE_TEXT                   = 0x0400;

  private static HashMap nodeTypes = new HashMap();

  static {
    nodeTypes.put(new Integer(Node.ATTRIBUTE_NODE),              new Integer(TYPE_ATTRIBUTE));
    nodeTypes.put(new Integer(Node.CDATA_SECTION_NODE),          new Integer(TYPE_CDATA));
    nodeTypes.put(new Integer(Node.COMMENT_NODE),                new Integer(TYPE_COMMENT));
    nodeTypes.put(new Integer(Node.DOCUMENT_FRAGMENT_NODE),      new Integer(TYPE_DOCUMENT_FRAGMENT));
    nodeTypes.put(new Integer(Node.DOCUMENT_NODE),               new Integer(TYPE_DOCUMENT));
    nodeTypes.put(new Integer(Node.DOCUMENT_TYPE_NODE),          new Integer(TYPE_DOCUMENT_TYPE));
    nodeTypes.put(new Integer(Node.ELEMENT_NODE),                new Integer(TYPE_ELEMENT));
    nodeTypes.put(new Integer(Node.ENTITY_NODE),                 new Integer(TYPE_ENTITY));
    nodeTypes.put(new Integer(Node.NOTATION_NODE),               new Integer(TYPE_NOTATION));
    nodeTypes.put(new Integer(Node.PROCESSING_INSTRUCTION_NODE), new Integer(TYPE_PROCESSING_INSTRUCTION));
    nodeTypes.put(new Integer(Node.TEXT_NODE),                   new Integer(TYPE_TEXT));
  }

  /** Get all childs of an element matching one of the given types.

      @param element Element whose children we want to search.
      @param types   Types of children we want to get.
      @return List of nodes found. */

  public static NodeList getChilds(Element element, int types) {
    NodeList childs = element.getChildNodes();
    ElementList list = new ElementList();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      Integer t = (Integer) nodeTypes.get(new Integer(node.getNodeType()));
      if (t != null && ((types & t.intValue()) != 0)) {
        list.add(node);
      }
    }

    return list;
  }

  /** Get all children of an element having the specified tag name. 
   
      @param element Element whose children we want to search
      @param tagname Desired tag name
      @return List of elements having these tag name */

  public static NodeList getChildsByTagName(Element element, String tagname) {
    NodeList childs = element.getChildNodes();
    ElementList list = new ElementList();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	if (tagname.equals(node.getNodeName())) {
	  list.add(node);
	}
      }
    }

    return list;
  }

  /** Get the first element child having the specified tag name.
   
      @param element Element whose children we want to search
      @param tagname Desired tag name
      @return The first element having this tag name */

  public static Element getFirstElement(Element element, String tagname) {
    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	if (tagname.equals(node.getNodeName())) {
	  return (Element) node;
	}
      }
    }
    return null;
  }

  /** Get the first element child having the specified tag name.
   
      @param element Element whose children we want to search
      @param namespace Desired name space URI
      @param tagname Desired tag name
      @return The first element having this tag name */

  public static Element getFirstElement(Element element, String namespace, String tagname) {
    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	if (namespace.equals(node.getNamespaceURI()) &&
	    tagname.equals(node.getNodeName())) {
	  return (Element) node;
	}
      }
    }
    return null;
  }

  /** Get the first element descendant having the specified attribute
      name and value.
   
      @param element Element whose children we want to search
      @param attribute Attribute name
      @param value Attribute value
      @return The first element having this attribute value */

  public static Element getElementWithAttribute(Element element, String attribute, String value) {
    String attr = element.getAttribute(attribute);
    if (attr != null && (attr.compareTo(value)==0)) {
      return element;
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	Element e = getElementWithAttribute((Element) node, attribute, value);
	if (e != null) return e;
      }
    }
    return null;
  }

  /** Get the elements descendant having the specified attribute
      name and value.
   
      @param element Element whose children we want to search
      @param attribute Attribute name
      @param value Attribute value
      @return list of the elements having this attribute value */

  public static LinkedList getElementsWithAttribute(Element element, String attribute, String value) {
    LinkedList elements = new LinkedList();
    if (element.hasAttribute(attribute)) {
      String attr = element.getAttribute(attribute);
      if (attr.equals(value)) {
	elements.add(element);
      }
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	elements = getElementsWithAttribute((Element) node, attribute, value, elements);
      }
    }
    return elements;
  }

  private static LinkedList getElementsWithAttribute(Element element, 
						    String attribute, 
						    String value,
						    LinkedList elements) {
    if (element.hasAttribute(attribute)) {
      String attr = element.getAttribute(attribute);
      if (attr.equals(value)) {
	elements.add(element);
      }
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	elements = getElementsWithAttribute((Element) node, attribute, value, elements);
      }
    }
    return elements;        
  }


  /** Get the elements descendant having the specified attribute
      name.
   
      @param element Element whose children we want to search
      @param attribute Attribute name
      @return list of the elements having this attribute value */

  public static LinkedList getElementsWithAttribute(Element element, String attribute) {
    LinkedList elements = new LinkedList();
    if (element.hasAttribute(attribute)) {
//       String attr = element.getAttribute(attribute);
//       System.out.println("element : "+element+"; attribute : "+attr);
//       System.out.println("attribute != null");
      elements.add(element);
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	elements = getElementsWithAttribute((Element) node, attribute, elements);
      }
    }
    return elements;
  }

  private static LinkedList getElementsWithAttribute(Element element, 
						    String attribute, 
						    LinkedList elements) {
//     String attr = element.getAttribute(attribute);
//     System.out.println("private : element : "+element+"; attribute : "+attr);
    if (element.hasAttribute(attribute)) {
      elements.add(element);
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	elements = getElementsWithAttribute((Element) node, attribute, elements);
      }
    }
    return elements;        
  }


  /** Get the first element descendant having the specified attribute
      name and value.
   
      @param element Element whose children we want to search
      @param namespaceURI Attribute namespace URI
      @param attribute Attribute name
      @param value Attribute value
      @return The first element having this attribute value */

  public static Element getElementWithAttributeNS(Element element, String namespaceURI, String attribute, String value) {
    String attr = element.getAttributeNS(namespaceURI, attribute);
    if (attr != null && attr.equals(value)) {
      return element;
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	Element e = getElementWithAttributeNS((Element) node, namespaceURI, attribute, value);
	if (e != null) return e;
      }
    }
    return null;
  }

  /** Remove all attributes with a certain namespace from this element.
   
      @param element Element whose attributes we want to remove.
      @param namespaceURI Namespace URI of the attributes to remove. */

  public static void removeAttributes(Element e, String namespaceURI) {
    NamedNodeMap attributes = e.getAttributes();
    ArrayList list = new ArrayList();
    for (int i = 0; i < attributes.getLength(); i++) {
      Attr a = (Attr) attributes.item(i);
      if (namespaceURI.equals(a.getNamespaceURI())) {
	list.add(a);
      }
    }

    Iterator i = list.iterator();
    while (i.hasNext()) {
      e.removeAttributeNode((Attr) i.next());
    }
  }

  public static void removeAttributesFromDescendants(Element e, String namespaceURI) {
    removeAttributes(e, namespaceURI);

    NodeList childs = getChildElements(e);
    for (int i = 0; i < childs.getLength(); i++) {
      removeAttributesFromDescendants((Element) childs.item(i), namespaceURI);
    }    
  }

  /** Get the value of the first text child of an element.
   
      @param element Element whose text element we want
      @return String value or null. */

  public static String getTextValue(Element element) {
    NodeList childs = element.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.TEXT_NODE == node.getNodeType()) {
	return node.getNodeValue();
      }
    }
    return null;
  }

  /** Replace the first text element of the given element with a new
      text element containing the given text or just create a new text
      element if there is none to replace.
   
      @param element Element whose text element we want 
      @param text Text to set */

  public static void setTextValue(Element element, String text) {
    Node newTextNode = element.getOwnerDocument().createTextNode(text);

    NodeList childs = element.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.TEXT_NODE == node.getNodeType()) {
	element.replaceChild(newTextNode, node);
	return;
      }
    }
    element.appendChild(newTextNode);
  }

  /** Return subtree as a string, formatted as in XML
      documents.

      @param e Element's subtree.
      @return String representation. */

  public static String toString(Element e) {
    return toString(e, "");
  }

  /** Return subtree as a string, formatted as in XML
      documents.

      NOTE: Does only handle elements. Support for text nodes
      still needs to be done.

      @param e Element's subtree.
      @param indentation Indentation of first element.
      @return String representation. */

  public static String toString(Element e, String indentation) {
    String result = indentation + "<";
    result += e.getTagName();
    NamedNodeMap attributes = e.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      result += " " +
	((Attr) attributes.item(i)).getName() + "=\"" +
	((Attr) attributes.item(i)).getValue() + "\"";
    }
    NodeList childs = getChildElements(e);
    if (childs.getLength() == 0) {
      result += "/>\n";
    } else {
      result += ">\n";
//       Node n = e.getFirstChild();
//       while (n != null) {
// 	switch (n.getNodeType()) {
// 	case Node.ELEMENT_NODE:
// 	  result += toString((Element) n, indentation + "  ");
// 	  break;
// 	case Node.TEXT_NODE:
// 	  result += indentation + "  '" + n.getNodeValue() + "'\n";
// 	  break;
//         }
// 	n = n.getNextSibling();
//       }
      for (int i = 0; i < childs.getLength(); i++) {
	result += toString((Element) childs.item(i), indentation+"  ");
      }
      result += indentation + "</" + e.getTagName() + ">\n";
    }
    return result;
  }

  /** Return subtree as a string, formatted as in XML
      documents.

      @param e Element's subtree.
      @return String representation. */

  public static String toStringWithoutChild(Element e) {
    return toStringWithoutChild(e, "");
  }

  /** Return subtree as a string, formatted as in XML
      documents.

      NOTE: Does only handle elements. Support for text nodes
      still needs to be done.

      @param e Element's subtree.
      @param indentation Indentation of first element.
      @return String representation. */

  public static String toStringWithoutChild(Element e, String indentation) {
    String result = indentation + "<";
    result += e.getTagName();
    NamedNodeMap attributes = e.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      result += " " +
	((Attr) attributes.item(i)).getName() + "=\"" +
	((Attr) attributes.item(i)).getValue() + "\"";
    }
    result += "/>\n";

    return result;
  }

}
