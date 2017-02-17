/* (C) 2001-2002, DIUF, http://www.unifr.ch/diuf
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package iiuf.xmillum;

import java.awt.Rectangle;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import iiuf.dom.DOMUtils;

/**
 * DisplayableFactory
 *
 * A factory class that creates Displayables. The actual creation is
 * delegated to DisplayableClass objects. This class decides to which
 * DisplayableClass object the call is delegated.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class DisplayableFactory {

  protected HashMap        classes = new HashMap();
  protected BrowserContext context;

  /**
   * Creates a new DisplayableFactory object.
   *
   * @param context Current browser context.
   * @param objectList The list of &lt;xmi:object&gt; elements.
   */
  public DisplayableFactory(BrowserContext context, NodeList objectList) {
    this.context = context;

    for (int i = 0; i < objectList.getLength(); i++) {
      Element h = (Element) objectList.item(i);
      String name = h.getAttribute("name");
      String className = h.getAttribute("class");

      try {
	Class clazz = Class.forName(className, true, context.getDocument().getClassLoader());
	Object dclass = clazz.newInstance();

	if (dclass instanceof DisplayableClass) {
	  ((DisplayableClass) dclass).initialize(context, h);
	  classes.put(name, dclass);
	} else {
	  System.err.println("Object "+className+" is not of class DisplayableClass.");
	}
      } catch (ClassNotFoundException e) {
	System.err.println("Unable to load DisplayObject: "+className);
      } catch (InstantiationException e) {
	System.err.println(e);
      } catch (IllegalAccessException e) {
	System.err.println(e);
      }
    }
  }

  /**
   * Creates a displayable for a given element.
   *
   * @param element The element we want to transform into a Displayable.
   * @return The created Displayable object or null if the object could not
   * be created.
   */
  public Displayable getDisplayable(Element element) {
    DisplayableClass c = (DisplayableClass) classes.get(element.getTagName());
    if (c == null) {
      System.err.println("No DisplayableClass found for '"+element.getTagName()+"'.");
      return null;
    }
    return c.getDisplayable(element);
  }
}
