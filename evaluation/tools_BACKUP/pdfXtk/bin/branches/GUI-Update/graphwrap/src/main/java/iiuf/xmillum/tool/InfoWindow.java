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

package iiuf.xmillum.tool;

import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Tool;
import iiuf.xmillum.Window;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import org.w3c.dom.Element;

/**
 * InfoWindow
 *
 * xmillum information window. Visualizes message from the <code>Info</code>
 * ActionHandler.
 *
 * <p>Optional initialization parameters:
 * <ul>
 *   <li>name: Name of the InfoWindow, if no name is given, the name
 *       `default' is taken
 * </ul>
 *
 * @see iiuf.xmillum.handlers.Info
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class InfoWindow implements Tool
{
  BrowserContext context;
  Element        info;
  Window         window;
  JLabel         text = new JLabel();
  String         name = "default";

  public void activateTool(BrowserContext c, Element e) {
    context = c;
    info    = e;

    if (e.hasAttribute("name")) {
      name = e.getAttribute("name");
    }

    text.setBorder(BorderFactory.createTitledBorder("Message"));
    text.setPreferredSize(new Dimension(200, 40));

    window = context.getWindowCreator().createWindow("InfoWindow: "+name);
    window.getContentPane().add(text, BorderLayout.CENTER);
    window.open();

    register(name, this);
  }

  public void deactivateTool() {
    window.close();
    unregister(name);
  }

  public void setMessage(String message) {
    text.setText(message);
  }

  private static HashMap registered = new HashMap();
  private static void register(String name, InfoWindow i) {
    synchronized(registered) {
      registered.put(name, i);
    }
  }
  private static void unregister(String name) {
    synchronized(registered) {
      registered.remove(name);
    }
  }
  public static InfoWindow getWindow(String name) {
    synchronized(registered) {
      return (InfoWindow) registered.get(name);
    }
  }
}
