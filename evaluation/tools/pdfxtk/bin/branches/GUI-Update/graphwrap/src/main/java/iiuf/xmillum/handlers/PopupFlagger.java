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
import iiuf.xmillum.Displayable;
import iiuf.xmillum.FlagAccess;
import iiuf.xmillum.FlagListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JPopupMenu;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * PopupFlagger
 *
 * Shows a popup menu that can be used to set a flag on an element.
 *
 * <p>Initialization parameters:
 * <ul>
 *   <li>flag: Name of the flag to set
 *   <li>allow-clear: "yes", "true" or "1" to Allow clearing the flag
 *       (i.e. setting the "null" flag value)
 * </ul>
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class PopupFlagger extends ActionHandler {

  FlagAccess access;
  JPopupMenu menu;

  public void init(BrowserContext context, Element param) {
    String    flagName = null;
    boolean   allowClear = false;

    NodeList prefs = DOMUtils.getChildsByTagName(param, "param");
    for (int i = 0; i < prefs.getLength(); i++) {
      Element p = (Element) prefs.item(i);
      String key   = p.getAttribute("name");
      String value = p.getAttribute("value");
      if (key.equals("flag")) {
	flagName = value;
      } else if (key.equals("allow-clear") && ("1".equals(value) || "true".equals(value) || "yes".equals(value))) {
	allowClear = true;
      } else {
	context.log("Unknown initialization parameter `"+key+"' in "+PopupFlagger.class.getName());
      }
    }

    if (flagName == null) {
      context.log("No flag name set in "+PopupFlagger.class.getName());
      return;
    }
    access = context.flagger.addFlagListener(flagName, new FlagListener() {
	public void setFlag(Element e, String v) {
	}
      });

    menu = new JPopupMenu();
    if (allowClear) {
      menu.add("<clear>").addActionListener(listener);
    }
    Set values = context.flagger.getFlagValues(flagName);
    Iterator i = values.iterator();
    while (i.hasNext()) {
      String v = (String) i.next();
      menu.add(v).addActionListener(listener);
    }
  }

  Element element;

  ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if ("<clear>".equals(e.getActionCommand())) {
	  access.setFlag(element, null);
	} else {
	  access.setFlag(element, e.getActionCommand());
	}
      }
    };

  public void handle(ActionHandlerParam param) {
	if (menu == null) {
      param.getContext().log("Popup menu not initialized in "+PopupFlagger.class.getName());
      return;
    }
    element = param.getElement();
    param.getContext().browserPanel.showPopup(menu);
  }
}
