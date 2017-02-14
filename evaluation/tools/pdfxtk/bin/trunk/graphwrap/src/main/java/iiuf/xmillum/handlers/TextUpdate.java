/* (C) 2000-2002, DIUF, http://www.unifr.ch/diuf
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * TextUpdate
 *
 * ActionHandler that allows to update text in an xmillum TextArea object.
 *
 * <p>Optional initialization parameters:
 * <ul>
 *   <li>name: Name of the TextUpdate tool which should update the text
 * </ul>
 *
 * @see iiuf.xmillum.tool.TextUpdate
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class TextUpdate extends ActionHandler {
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
    iiuf.xmillum.tool.TextUpdate t = iiuf.xmillum.tool.TextUpdate.getWindow(name);
    if (t != null) {
      t.updateText(param.getContext().elementTagger.getReferencedElement(param.getElement()));
    } else {
      param.getContext().setStatus("TextUpdate `"+name+"' not found.");
    }
  }
}
