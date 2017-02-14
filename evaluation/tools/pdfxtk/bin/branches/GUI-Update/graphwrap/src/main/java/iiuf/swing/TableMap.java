package iiuf.swing;

/**
   Table model mapper, taken from the Swing Tutorial at

   http://web2.java.sun.com/docs/books/tutorial/uiswing/index.html

   and extended.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

/** 
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

import javax.swing.table.*; 
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent; 

public abstract class TableMap
  extends AbstractTableModel 
  implements TableModelListener
{
  protected TableModel model;

  public TableMap(TableModel m) {
    model = m;
    model.addTableModelListener(this); 
  }
  
  // The mapping function

  public abstract int mapRowToData(int row);
  
  // By default, implement TableModel by forwarding all messages 
  // to the model.
  
  public Object getValueAt(int aRow, int aColumn) {
    return model.getValueAt(mapRowToData(aRow), aColumn); 
  }
  
  public void setValueAt(Object aValue, int aRow, int aColumn) {
    model.setValueAt(aValue, mapRowToData(aRow), aColumn); 
  }

  public int getRowCount() {
    return model.getRowCount();
  }

  public int getColumnCount() {
    return model.getColumnCount();
  }
        
  public String getColumnName(int aColumn) {
    return model.getColumnName(aColumn); 
  }

  public Class getColumnClass(int aColumn) {
    return model.getColumnClass(aColumn); 
  }
        
  public boolean isCellEditable(int row, int column) {
    return model.isCellEditable(mapRowToData(row), column); 
  }

  // TableModelListener

  public void tableChanged(TableModelEvent e) {
    fireTableChanged(e);
  }
}
