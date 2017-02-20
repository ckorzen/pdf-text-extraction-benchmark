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

import iiuf.dom.DOMUtils;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Tool;
import iiuf.xmillum.Window;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextArea;
import org.w3c.dom.Element;

/**
 * TextUpdate
 *
 * xmillum TextUpdate tool - used to update PCDATA
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class TextUpdate implements Tool {
  BrowserContext context;
  Window         window;
  JTextArea      textarea = new JTextArea();
  JButton        savebutton = new JButton(new AbstractAction("Save") {
      public void actionPerformed(ActionEvent e) {
	savebutton.setEnabled(false);
	textarea.setEnabled(false);
	DOMUtils.setTextValue(activeElement, textarea.getText());
	context.retransform();
      }
    });
  Element        activeElement;
  String         name = "default";

  public void activateTool(BrowserContext c, Element e) {
    context = c;

    if (e.hasAttribute("name")) {
      name = e.getAttribute("name");
    }

    textarea.setBorder(BorderFactory.createTitledBorder("Text Content"));
    textarea.setPreferredSize(new Dimension(200, 100));
    textarea.setLineWrap(true);
    textarea.setWrapStyleWord(true);
    textarea.setEnabled(false);

    savebutton.setEnabled(false);

    window = context.getWindowCreator().createWindow("TextUpdate: "+name);
    window.getContentPane().add(textarea, BorderLayout.CENTER);
    window.getContentPane().add(savebutton, BorderLayout.SOUTH);
    window.open();

    register(name, this);
  }

  public void deactivateTool() {
    window.close();
    unregister(name);
  }

  public void updateText(Element e) {
    activeElement = e;
    String text = DOMUtils.getTextValue(e);
    if (text != null) {
      textarea.setText(text);
      textarea.setEnabled(true);
      savebutton.setEnabled(true);
    } else {
      textarea.setText("");
      textarea.setEnabled(false);
      savebutton.setEnabled(false);
    }
  }

  private static HashMap registered = new HashMap();
  private static void register(String name, TextUpdate i) {
    synchronized(registered) {
      registered.put(name, i);
    }
  }
  private static void unregister(String name) {
    synchronized(registered) {
      registered.remove(name);
    }
  }
  public static TextUpdate getWindow(String name) {
    synchronized(registered) {
      return (TextUpdate) registered.get(name);
    }
  }
}
