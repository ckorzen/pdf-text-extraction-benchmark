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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * StyleRegistry
 *
 * Registers all drawing styles defined by the document (the
 * &lt;xmi:style&gt; elements).
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class StyleRegistry {

  static Style  defaultStyle = new Style();

  BrowserContext context;
  Map            styles = new HashMap();

  /**
   * Creates a style registry.
   *
   * @param c Current browser context.
   * @param s List of elements containing style definitions.
   */
  public StyleRegistry(BrowserContext c, NodeList s) {
    context = c;
    for (int i = 0; i < s.getLength(); i++) {
      Element e = (Element) s.item(i);
      String name = e.getAttribute("name");
      if (name == null) {
	context.log("Style definition without a `name' attribute found.");
      } else {
	styles.put(name, new Style(context, name, e));
      }
    }
  }

  /**
   * Returns the desired style.
   *
   * @param name Name of the style
   * @return Desired style
   */
  public Style getStyle(String name) {
    Style s = defaultStyle;
    if (name != null && !name.equals("")) {
      s = (Style) styles.get(name);
      if (s == null) {
	context.log("Access to unknown style `"+name+"', using default style instead.");
	s = defaultStyle;
      }
    }
    return s;
  }
}
