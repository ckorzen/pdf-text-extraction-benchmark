/* ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: Comparison.java,v $
 * SUPPORT:	$Author: ohitz $
 * CREATION:	$Date: 2002/07/11 12:03:47 $
 * VERSION:	$Revision: 1.1 $
 * OVERVIEW:    makes deep comparison of DOM elements
 * ------------------------------------------------------------------------ */
/**
   (c) 2000, IIUF

   makes deep comparison of DOM elements

   @author $author$
   @version $revision$
*/

package iiuf.dom;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
/* ------------------------------------------------------------------------ */
public class Comparison
{
  /*------------------------------------------------------------------------*/
  private final static boolean DEBUG = false;
  /*------------------------------------------------------------------------*/
  public Comparison() {
  }
  /*------------------------------------------------------------------------*/
  /** deep equal : compares e1 and e2 childs */
  public static boolean equal(Element e1,
			      Element e2) {
    return sameElements(e1,e2);
  }
  /*------------------------------------------------------------------------*/
  private static boolean sameElements(Element e1,
				      Element e2) {
    Attr         e1Attr, e2Attr;
    Element      e1Child,e2Child;
    String       attrName;
    int          i;
    NamedNodeMap e1Attrs, e2Attrs; 
    NodeList     e1Childs, e2Childs;
    
    if (!e1.getTagName().equals(e2.getTagName())) return false;
    
    e1Attrs = e1.getAttributes();
    e2Attrs = e2.getAttributes();
    if (e1Attrs.getLength()!=e2Attrs.getLength()) return false;
    
    for (i=0;i<e1Attrs.getLength();i++) {
      e1Attr = (Attr)e1Attrs.item(i);
      attrName = e1Attr.getName();
      if ((e2Attr=(Attr)e2Attrs.getNamedItem(attrName))==null) return false;
      if (!e1Attr.getValue().equals(e2Attr.getValue())) return false;
    }

    e1Childs = DOMUtils.getChildElements(e1);
    e2Childs = DOMUtils.getChildElements(e2);
    if (e1Childs.getLength()!=e2Childs.getLength()) return false;

    for (i=0;i<e1Childs.getLength();i++) {
      e1Child = (Element)e1Childs.item(i);
      e2Child = (Element)e2Childs.item(i);
      if (sameElements(e1Child,e2Child)==false) return false;
    }
    return true;
  }
  /*------------------------------------------------------------------------*/
}
/* ------------------------------------------------------------------------ */

