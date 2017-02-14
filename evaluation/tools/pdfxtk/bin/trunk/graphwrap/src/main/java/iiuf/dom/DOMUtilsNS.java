package iiuf.dom;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
   (c) 2000, IIUF<p>

   Namespace aware DOM Utilities
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class DOMUtilsNS {

  /** Get all element children of an element with a given namespace.

      @param element Element whose children we want to search for elements
      @param namespaceURI Namespace of the children
      @return List of elements */

  public static NodeList getChildElements(Element element, String namespaceURI) {
    NodeList childs = element.getChildNodes();
    ElementList list = new ElementList();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType() && namespaceURI.equals(node.getNamespaceURI())) {
        list.add(node);
      }
    }
    return list;
  }

  /** Get all children of an element having the specified namespace and tag name.

      @param element Element whose children we want to search
      @param namespaceURI Namespace 
      @param localName Desired tag name
      @return List of elements having these tag name */

  public static NodeList getChildsByTagName(Element element, String namespaceURI, String localName) {
    ElementList list = new ElementList();
    NodeList childs = element.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	if (namespaceURI.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName())) {
	  list.add(node);
	}
      }
    }
    return list;
  }

  /** Get the first element child having the specified tag name.
   
      @param element Element whose children we want to search
      @param namespaceURI Namespace
      @param localName Desired tag name
      @return The first element having this tag name */

  public static Element getFirstElement(Element element, String namespaceURI, String localName) {
    NodeList childs = element.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
	if (namespaceURI.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName())) {
	  return (Element) node;
	}
      }
    }
    return null;
  }

  /** Get the elements descendant having the specified attribute name
      and value.
   
      @param element Element whose children we want to search
      @param namespaceURI Attribute's namespace
      @param localName Attribute name
      @param value Attribute value
      @return list of the elements having this attribute value */

  public static Collection getElementsWithAttribute(Element element, String namespaceURI, String localName, String value) {
    Collection elements = new ArrayList();
    getElementsWithAttribute(element, namespaceURI, localName, value, elements);
    return elements;
  }

  private static void getElementsWithAttribute(Element element, String namespaceURI, String localName, String value, Collection elements) {
    if (element.hasAttributeNS(namespaceURI, localName)) {
      String attr = element.getAttributeNS(namespaceURI, localName);
      if (attr.equals(value)) elements.add(element);
    }

    NodeList childs = element.getChildNodes();

    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType())
	getElementsWithAttribute((Element) node, namespaceURI, localName, value, elements);
    }
  }
}
