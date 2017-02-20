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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.ImageIcon;

import iiuf.dom.DOMUtils;
import iiuf.util.EventListenerList;
import iiuf.util.Strings;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.FlagAccess;
import iiuf.xmillum.FlagListener;
import iiuf.xmillum.FlagManager;
import iiuf.xmillum.Tool;
import iiuf.xmillum.Window;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * XMLTree
 *
 * xmillum xml tree.
 *
 * <p>Usage:
 *
 * &lt;xmi:tool class="iiuf.xmillum.tool.XMLTree"
 *           showattributes="..."
 *           start="..."
 *           filter="..."/&gt;<p>
 *
 * <ul>
 * <li>start: (required) specifies what sub-tree is to be visualized
 * <li>showattributes: (optional) set it to 1 to show attributes in the tree
 * <li>filter: (optional) specifies the attributes that are not shown in the tree
 * </ul>
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class XMLTree implements Tool {
  protected BrowserContext context;
  Element               element;
  Element               startElement;
  Window                window;
  boolean               showAttributes = false;
  Set                   filtered = new HashSet();
  FlagAccess            flag;
  DOMTreeSelectionModel selectionModel;
  Icon                  elementIcon   = new ImageIcon(XMLTree.class.getResource("element.gif"));
  Icon                  attributeIcon = new ImageIcon(XMLTree.class.getResource("attribute.gif"));
  Icon                  textIcon      = new ImageIcon(XMLTree.class.getResource("text.gif"));

  public void activateTool(BrowserContext c, Element e) {
    context = c;
    element = e;

    String attr = element.getAttribute("showattributes");
    if (attr != null) {
      if (attr.equals("1")) {
	showAttributes = true;
      }
    }

    filtered.add("tmp:refvalue");

    String filter = element.getAttribute("filter");
    if (filter != null) {
      String[] f = Strings.split(filter, ',');
      for (int i = 0; i < f.length; i++) {
	filtered.add(f[i]);
      }
    }

    String start = element.getAttribute("start");
    if (start != null) {
      startElement = context.getSourceElementByReference(start);
    }

    JTree tree = new JTree(new DOMTreeModel(startElement));
    tree.setCellRenderer(new DOMTreeCellRenderer());
    selectionModel = new DOMTreeSelectionModel();
    tree.setSelectionModel(selectionModel);

    flag = context.flagger.addFlagListener(FlagManager.SELECTION, listener);

    window = context.getWindowCreator().createWindow("XML Tree");
    window.getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
    window.open();
  }

  public void deactivateTool() {
    window.close();
  }

  class DOMTreeCellRenderer implements TreeCellRenderer {
    Color textForeground;
    Color textBackground;
    Color selectionForeground;
    Color selectionBackground;
    Color selectionBorderColor;

    public DOMTreeCellRenderer() {
      textForeground = UIManager.getColor("Tree.textForeground");
      textBackground = UIManager.getColor("Tree.textBackground");
      selectionForeground = UIManager.getColor("Tree.selectionForeground");
      selectionBackground = UIManager.getColor("Tree.selectionBackground");
      selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
    }

    class Cell extends JLabel {
      boolean selected;
      boolean hasFocus;
      public Cell(String label, Icon icon, boolean sel, boolean focus) {
	super(label, icon, SwingConstants.LEFT);
	selected = sel;
	hasFocus = focus;
      }
      public void paint(Graphics g) {
	Color background;

	if (selected) {
	  background = selectionBackground;
	} else {
	  background = textBackground;
	  if (background == null) {
	    background = getBackground();
	  }
	}

	int imageOffset = getLabelStart();

	if (background != null) {
	  g.setColor(background);
	  if (getComponentOrientation().isLeftToRight()) {
	    g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight());
	  } else {
	    g.fillRect(0, 0, getWidth() - 1 - imageOffset, getHeight());
	  }
	}
	
	if (hasFocus) {
	  Color border = selectionBorderColor;
	  
	  if (border != null) {
	    g.setColor(border);
	    if(getComponentOrientation().isLeftToRight()) {
	      g.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight() - 1);
	    } else {
	      g.drawRect(0, 0, getWidth() - 1 - imageOffset, getHeight() - 1);
	    }
	  }
	}

	if (selected) {
	  setForeground(selectionForeground);
	} else {
	  setForeground(textForeground);
	}

	super.paint(g);
      }
      int getLabelStart() {
	Icon image = getIcon();
	if (image != null && getText() != null) {
	  return image.getIconWidth() + Math.max(0, getIconTextGap() - 1);
	}
	return 0;
      }
    }

    final int MAX_TEXT_LENGTH = 48;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      Icon icon;
      String label;
      if (value instanceof Element) {
	Element e = (Element) value;
	label = "<"+e.getTagName()+">";
	icon = elementIcon;
      } else if (value instanceof Attr) {
	Attr a = (Attr) value;
	label = a.getName()+"="+a.getValue();
	icon = attributeIcon;
      } else if (value instanceof Text) {
	Text t = (Text) value;
	label = t.getData();
	icon = textIcon;
      } else {
	label = "?";
	icon = null;
      }
      if (label.length() > MAX_TEXT_LENGTH) {
	label = label.substring(0, MAX_TEXT_LENGTH-1-3)+"...";
      }
      return new Cell(label, icon, selected, hasFocus);
    }
  }

  TreePath getPath(Element e) {
    if (e == null) {
      return null;
    } else if (e == startElement) {
      return new TreePath(e);
    } else {
      return getPath((Element) e.getParentNode()).pathByAddingChild(e);
    }
  }

  FlagListener listener = new FlagListener() {
      public void setFlag(Element e, String value) {
	String id = e.getAttribute("ref");
	if (id != null) {
	  Element o = context.getSourceElementByReference(id);
	  if (value != null) {
	    selectionModel.addToSelection(getPath(o));
	  } else {
	    selectionModel.removeFromSelection(getPath(o));
	  }
	}
      }
    };

  class DOMTreeSelectionModel extends DefaultTreeSelectionModel {
    NodeList getReferenced(TreePath p) {
      if (p == null) return null;
      Object o = p.getLastPathComponent();
      if (o instanceof Element) {
	String r = ((Element) o).getAttribute("tmp:refvalue");
	if (r != null) {
	  return context.getInternalElementsWhichReference(r);
	}
      }
      return null;
    }
    public void addToSelection(TreePath path) {
      super.addSelectionPath(path);
    }
    public void removeFromSelection(TreePath path) {
      super.removeSelectionPath(path);
    }

    public void addSelectionPath(TreePath path) {
      NodeList nl = getReferenced(path);
      if (nl != null) {
	for (int i = 0; i < nl.getLength(); i++) {
	  XMLTree.this.flag.setFlag((Element) nl.item(i), FlagManager.SELECTED);
	}
      }
      super.addSelectionPath(path);
    }
    public void addSelectionPaths(TreePath[] paths) {
      for (int j = 0; j < paths.length; j++) {
	NodeList nl = getReferenced(paths[j]);
	if (nl != null) {
	  for (int i = 0; i < nl.getLength(); i++) {
	    XMLTree.this.flag.setFlag((Element) nl.item(i), FlagManager.SELECTED);
	  }
	}
      }
      super.addSelectionPaths(paths);
    }
    public void removeSelectionPath(TreePath path) {
      NodeList nl = getReferenced(path);
      if (nl != null) {
	for (int i = 0; i < nl.getLength(); i++) {
	  XMLTree.this.flag.setFlag((Element) nl.item(i), null);
	}
      }
      super.removeSelectionPath(path);
    }
    public void removeSelectionPaths(TreePath[] paths) {
      for (int j = 0; j < paths.length; j++) {
	NodeList nl = getReferenced(paths[j]);
	if (nl != null) {
	  for (int i = 0; i < nl.getLength(); i++) {
	    XMLTree.this.flag.setFlag((Element) nl.item(i), null);
	  }
	}
      }
      super.removeSelectionPaths(paths);
    }
    public void setSelectionPath(TreePath path) {
      XMLTree.this.flag.clearFlags();
      NodeList nl = getReferenced(path);
      if (nl != null) {
	for (int i = 0; i < nl.getLength(); i++) {
	  XMLTree.this.flag.setFlag((Element) nl.item(i), FlagManager.SELECTED);
	}
      }
      super.setSelectionPath(path);
    }
    public void setSelectionPaths(TreePath[] paths) {
      XMLTree.this.flag.clearFlags();
      for (int j = 0; j < paths.length; j++) {
	NodeList nl = getReferenced(paths[j]);
	if (nl != null) {
	  for (int i = 0; i < nl.getLength(); i++) {
	    XMLTree.this.flag.setFlag((Element) nl.item(i), FlagManager.SELECTED);
	  }
	}
      }
      super.setSelectionPaths(paths);
    }
  }

  class DOMTreeModel implements TreeModel {
    Element root;
    EventListenerList listeners = new EventListenerList();
    public DOMTreeModel(Element e) {
      root = e;
    }
    public void addTreeModelListener(TreeModelListener l) {
      listeners.add(TreeModelListener.class, l, true);
    }
    public void removeTreeModelListener(TreeModelListener l) {
      listeners.remove(TreeModelListener.class, l);
    }
    public Object getChild(Object parent, int index) {
      Element e = (Element) parent;
      if (showAttributes) {
	NamedNodeMap nm = e.getAttributes();
	for (int i = 0; i < nm.getLength(); i++) {
	  Attr a = (Attr) nm.item(i);
	  if (!filtered.contains(a.getName())) {
	    if (index == 0) {
	      return a;
	    }
	    index--;
	  }
	}
      }

      NodeList nl = DOMUtils.getChilds((Element) parent, DOMUtils.TYPE_ELEMENT | DOMUtils.TYPE_TEXT);
      return nl.item(index);
    }
    public int getChildCount(Object parent) {
      Element e = (Element) parent;
      int n = 0;
      if (showAttributes) {
	NamedNodeMap nm = e.getAttributes();
	for (int i = 0; i < nm.getLength(); i++) {
	  Attr a = (Attr) nm.item(i);
	  if (!filtered.contains(a.getName())) n++;
	}
      }

      NodeList nl = DOMUtils.getChilds((Element) parent, DOMUtils.TYPE_ELEMENT | DOMUtils.TYPE_TEXT);
      return n+nl.getLength();
    }
    public int getIndexOfChild(Object parent, Object child) {
      int index = 0;
      if (showAttributes) {
	NamedNodeMap nm = ((Element) parent).getAttributes();
	for (int i = 0; i < nm.getLength(); i++) {
	  Attr a = (Attr) nm.item(i);
	  if (!filtered.contains(a.getName())) {
	    if (child == a) {
	      return index;
	    }
	    index++;
	  }
	}
      }

      NodeList nl = DOMUtils.getChilds((Element) parent, DOMUtils.TYPE_ELEMENT | DOMUtils.TYPE_TEXT);
      for (int i = 0; i < nl.getLength(); i++, index++) {
	if (child == nl.item(i)) {
	  return index;
	}
      }
      return -1;
    }
    public Object getRoot() {
      return root;
    }
    public boolean isLeaf(Object node) {
      return (node instanceof Attr) || (node instanceof Text);
    }
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
  }
}
