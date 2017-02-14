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

package iiuf.xmillum.handlers;

import iiuf.dom.DOMUtils;
import iiuf.xmillum.ActionHandler;
import iiuf.xmillum.ActionHandlerParam;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.tool.InfoWindow;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Info
 *
 * xmillum information window action. This handler simply displays a message
 * in an <code>InfoWindow</code> tool. The message is taken from the
 * <b>info</b> Attribute of the current element.
 *
 * <p>Optional initialization parameters:
 * <ul>
 *   <li>name: Name of the InfoWindow, in which the message should
 *       appear.
 * </ul>
 *
 * @see iiuf.xmillum.tool.InfoWindow
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $ 
 */
public class Info extends ActionHandler {
  String name = "default";

  public void init(BrowserContext context, Element param) {
    NodeList prefs = DOMUtils.getChildsByTagName(param, "param");
    for (int i = 0; i < prefs.getLength(); i++) {
      Element p = (Element) prefs.item(i);
      String key   = p.getAttribute("name");
      String value = p.getAttribute("value");
      if (key.equals("name")) {
	name = value;
      }
    }
  }

  public void handle(ActionHandlerParam param) {
    InfoWindow i = InfoWindow.getWindow(name);
    if (i != null) {
      i.setMessage(param.getElement().getAttribute("info"));
    } else {
      param.getContext().setStatus("InfoWindow `"+name+"' not found.");
    }
  }
}
