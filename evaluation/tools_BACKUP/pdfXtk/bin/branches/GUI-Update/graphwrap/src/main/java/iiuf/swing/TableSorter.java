package iiuf.swing;

/**
   Table sorter, inspired from the Swing Tutorial.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public class TableSorter
  extends TableMap
{
  int       sortColumn;
  Integer[] indexes;

  public TableSorter(TableModel model) {
    super(model);
    sortByColumn(-1);
  }

  public int mapRowToData(int row) {
    return indexes[row].intValue();
  }

  private void sortByColumn(int column) {
    sortColumn = column;
    int rows = model.getRowCount();

    Integer[] idx = new Integer[rows];
    for (int i = 0; i < rows; i++) {
      idx[i] = new Integer(i);
    }

    if (sortColumn != -1) {
      Class columnclass = model.getColumnClass(sortColumn);
      if (columnclass.isInstance(Comparator.class)) {
	Arrays.sort(idx, new Comparator() {
	    public int compare(Object o1, Object o2) {
	      Comparable v1 = (Comparable) model.getValueAt(((Integer) o1).intValue(), sortColumn);
	      Comparable v2 = (Comparable) model.getValueAt(((Integer) o2).intValue(), sortColumn);
	      return v1.compareTo(v2);
	    }
	  });
      } else {
	Arrays.sort(idx, new Comparator() {
	    public int compare(Object o1, Object o2) {
	      String v1 = model.getValueAt(((Integer) o1).intValue(), sortColumn).toString();
	      String v2 = model.getValueAt(((Integer) o2).intValue(), sortColumn).toString();
	      return v1.compareTo(v2);
	    }
	  });
      }
    }
      
    indexes = idx;
  }

  public void tableChanged(TableModelEvent e) {
    sortByColumn(sortColumn);
    super.tableChanged(e);
  }

  // Create a MouseListener that can be attached to a table's header and
  // that does the sorting of the columns.

  public MouseListener createHeaderMouseListener() {
    return new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	  JTableHeader header = (JTableHeader) e.getSource();
	  if (e.getClickCount() == 1) {
 	    sortByColumn(header.columnAtPoint(e.getPoint()));
	  }
	}
      };
  }
}
