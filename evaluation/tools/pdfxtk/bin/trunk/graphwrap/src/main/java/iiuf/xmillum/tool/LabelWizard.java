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
import iiuf.xmillum.ActionHandler;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.FlagAccess;
import iiuf.xmillum.FlagManager;
import iiuf.xmillum.FlagListener;
import iiuf.xmillum.Tool;
import iiuf.xmillum.Window;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTextArea;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * LabelWizard
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class LabelWizard implements Tool {
  BrowserContext  context;
  Element         info;
  Window          window;
  List            tasks = new ArrayList();
  int             current;
  JTextArea       textArea;
  JButton         back;
  JButton         next;
  String          handler;

  class Task {
    String     description;

    FlagAccess flag;
    String     flagName;
    String     flagValue;

    public Task(String d) {
      description = d;
    }

    public void setFlag(String n, String v) {
      flagName  = n;
      flagValue = v;
      flag = context.flagger.addFlagListener(n, new FlagListener() {
	  public void setFlag(Element e, String v) { }
	});
    }

    public Set getElements() {
      return flag.getElements(flagValue);
    }
  }

  public void activateTool(BrowserContext c, Element e) {
    context = c;
    info    = e;

    if (e.hasAttribute("handler")) {
      handler = e.getAttribute("handler");
    }

    setupTasks(e);

    JPanel buttons = new JPanel();
    buttons.setLayout(new BorderLayout());

    back = new JButton("<< Back");
    back.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  current--;
	  showCurrent();
	}
      });
    buttons.add(back, BorderLayout.WEST);

    next = new JButton("Next >>");
    next.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  current++;
	  showCurrent();
	}
      });
    buttons.add(next, BorderLayout.EAST);

    textArea = new JTextArea();
    textArea.setEditable(false);

    current = 0;
    showCurrent();

    window = context.getWindowCreator().createWindow("Label Wizard");
    window.getContentPane().add(textArea, BorderLayout.CENTER);
    window.getContentPane().add(buttons, BorderLayout.SOUTH);
    window.open();
  }

  public void deactivateTool() {
    if (handler != null) {
      for (int i = 0; i < tasks.size(); i++) {
	Task t = (Task) tasks.get(i);
	context.actionFactory.handleFlagged(handler, t.flagValue, t.getElements());
      }
    }
    window.close();
  }

  public void showCurrent() {
    if (current == 0) {
      back.setEnabled(false);
    } else {
      back.setEnabled(true);
    }

    if (current == tasks.size()-1) {
      next.setEnabled(false);
    } else {
      next.setEnabled(true);
    }

    Task t = (Task) tasks.get(current);
    textArea.setText(t.description);

    context.setSelectionFlag(t.flagName, t.flagValue);
  }

  public void setupTasks(Element e) {
    NodeList steps = DOMUtils.getChildsByTagName(e, "step");
    for (int i = 0; i < steps.getLength(); i++) {
      Element step = (Element) steps.item(i);
      Element prompt = DOMUtils.getFirstElement(step, "prompt");

      Task t = new Task(DOMUtils.getTextValue(prompt));
      t.setFlag(step.getAttribute("flag"), step.getAttribute("value"));

      tasks.add(t);
    }
  }
}
