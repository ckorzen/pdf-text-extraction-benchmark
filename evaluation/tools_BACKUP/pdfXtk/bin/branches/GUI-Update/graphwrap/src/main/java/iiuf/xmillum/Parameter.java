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

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parameter
 *
 * Utility class for parameter settings.
 *  
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public abstract class Parameter {
  /**
   * Sets a parameter.
   *
   * @param context Browser context
   * @param o The object on which the parameter needs to be set.
   * @param v The value to be set.
   */
  public void setParam(BrowserContext context, Object o, String v) throws ParameterException {
  }

  /**
   * Sets a parameter, including an optional argument.
   *
   * @param context Browser context
   * @param o The object on which the parameter needs to be set.
   * @param v The value to be set.
   * @param opt Optional argument.
   */
  public void setParam(BrowserContext context, Object o, String v, String opt) throws ParameterException {
    setParam(context, o, v);
  }

  /**
   * Checks true/false answers.
   *
   * @param yn Value
   * @return true, if the yn is "yes", "true" or "1"
   */
  protected boolean trueFalse(String yn) {
    return "on".equals(yn) || "yes".equals(yn) || "true".equals(yn) || "1".equals(yn);
  }

  /**
   * Parses childs of a DOM tree and applies the required parameters.
   *
   * @param context BrowserContext
   * @param e DOM tree
   * @param o Object ob which parameters need to be set.
   * @param parameters Map of parameters
   */
  public static void setParameters(BrowserContext context, Element e, Object o, Map parameters) {
    NodeList prefs = DOMUtils.getChildsByTagName(e, "param");
    for (int i = 0; i < prefs.getLength(); i++) {
      Element p    = (Element) prefs.item(i);
      String  name = p.getAttribute("name");

      if (name == null || "".equals(name)) {
	context.log("Missing the `name' attribute in the style parameters for "+o);
      } else {
	Parameter par = (Parameter) parameters.get(name);
	if (par == null) {
	  context.log("Unable to handle style parameter `"+name+"'");
	} else {
	  try {
	    par.setParam(context, o, p.getAttribute("value"), p.getAttribute("opt"));
	  } catch (ParameterException ex) {
	    context.log(ex.getMessage());
	  }
	}
      }
    }
  }
}
