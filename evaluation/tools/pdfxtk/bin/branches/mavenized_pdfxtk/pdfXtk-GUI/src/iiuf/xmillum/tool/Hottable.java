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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.AbstractTableModel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import iiuf.dom.DOMUtils;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.FlagListener;
import iiuf.xmillum.FlagAccess;
import iiuf.xmillum.FlagManager;
import iiuf.xmillum.Tool;
import iiuf.xmillum.Window;

/**
 * Hottable
 *
 * Shows xmillum data in a tabular form.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class Hottable implements Tool, FlagListener, ListSelectionListener {
  protected BrowserContext context;
  protected Window window;
  protected JTable table;
  protected NodeList nodes;
  protected String[] columns;
  protected String sortColumn;
  protected FlagAccess flagAccess;

  public void activateTool(BrowserContext context, Element e) {
    this.context = context;

    nodes = DOMUtils.getChildElements(e);
    columns = getColumns(e.getAttribute("columns"));

    String name = e.hasAttribute("name") ? e.getAttribute("name") : "Hotlist";
    window = context.getWindowCreator().createWindow(name);

    table = new JTable(model);
    table.setShowGrid(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener(this);

    window.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
    window.open();

    flagAccess = context.flagger.addFlagListener(FlagManager.SELECTION, this);
  }

  public void deactivateTool() {
    context.flagger.removeFlagListener(FlagManager.SELECTION, this);
    window.close();
  }

  protected String[] getColumns(String c) {
    StringTokenizer tok = new StringTokenizer(c, ",");
    String[] columns = new String[tok.countTokens()];
    for (int i = 0; i < columns.length; i++) {
      columns[i] = tok.nextToken();
    }
    return columns;
  }

  AbstractTableModel model = new AbstractTableModel() {
      public int getRowCount() {
	return nodes.getLength();
      }
      public int getColumnCount() {
	return columns.length;
      }
      public Object getValueAt(int row, int column) {
	Element e = (Element) nodes.item(row);
	return e.getAttribute(columns[column]);
      }
      public String getColumnName(int column) {
	return columns[column];
      }
    };

  // FlagListener

  public void setFlag(Element e, String value) {
    String ref = e.getAttribute("ref");
    for (int i = 0; i < nodes.getLength(); i++) {
      if (ref.equals(((Element) nodes.item(i)).getAttribute("ref"))) {
	if (value != null) {
	  table.getSelectionModel().setSelectionInterval(i, i);
	} else {
	  table.getSelectionModel().removeSelectionInterval(i, i);
	}
      }
    }
  }

  // ListSelectionListener

  protected int selectedIndex = -1;

  public void valueChanged(ListSelectionEvent event) {
    flagAccess.clearFlags();
    if (table.getSelectedRow() != -1) {
      Element e = (Element) nodes.item(table.getSelectedRow());
      NodeList nl = context.getInternalElementsWhichReference(e.getAttribute("ref"));
      if (nl.getLength() > 0) {
	flagAccess.setFlag((Element) nl.item(0), FlagManager.SELECTED);
      }
    }
  }
}
