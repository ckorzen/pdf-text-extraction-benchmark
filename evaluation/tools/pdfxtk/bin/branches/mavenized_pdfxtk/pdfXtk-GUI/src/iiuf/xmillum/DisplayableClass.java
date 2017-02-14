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

import iiuf.dom.DOMUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Vector;

/**
 * DisplayableClass
 *
 * This class defines an abstract class for a factory of Displayables.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public abstract class DisplayableClass {
  /**
   * Creates a new DisplayableClass.
   */
  public DisplayableClass() {
  }  

  /**
   * Gets called whenever a new DisplayableClass is instantiated.
   *
   * @param context The current browser context.
   * @param element Element instantiating the class (&lt;xmi:object&gt;)
   */
  public abstract void initialize(BrowserContext context, Element element);

  /** 
   * Returns a new instance of the Displayable represented by this
   * DisplayableClass.
   *
   * @param element Element instantiating the Displayable object.
   * @return Displayable used for displaying the data object.
   */
  public abstract Displayable getDisplayable(Element element);

  /**
   * Creates the hierarchy of childs for a given element. This is used
   * when working with nested objects.
   *
   * @param element Pointer to the element whose children should be
   * instantiated.
   * @param context Current browser context.
   * @return List of Displayables.
   */
  public Displayable[] getChilds(Element element, BrowserContext context) {
    NodeList nl     = DOMUtils.getChildElements(element);
    Vector   childs = new Vector();
    for (int i = 0; i < nl.getLength(); i++) {
      Displayable child = context.displayableFactory.getDisplayable((Element) nl.item(i));
      if (child != null) {
	childs.add(child);
      }
    }
    return (Displayable[]) childs.toArray(new Displayable[0]);
  }
}
