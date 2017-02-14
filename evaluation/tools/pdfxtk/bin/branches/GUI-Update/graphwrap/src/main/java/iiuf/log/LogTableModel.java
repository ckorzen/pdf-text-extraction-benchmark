package iiuf.log;

import java.util.Date;
import java.util.LinkedList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
   A table model for log entries.

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class LogTableModel
  extends 
  AbstractTableModel
{
  protected LinkedList messages = new LinkedList();
  protected DateFormat format   = new SimpleDateFormat("HH:mm:ss");

  Class[] columnClasses = {
    ImageIcon.class, String.class, String.class, String.class
  };
  
  String[] columnNames = {
    "", "Thread", "Time", "Message"
  };
  
  public void add(LogMessage m) {
    messages.add(m);
    fireTableDataChanged();
  }

  public void clear() {
    messages.clear();
    fireTableDataChanged();
  }

  public Class getColumnClass(int i) {
    return columnClasses[i];
  }
  
  public int getColumnCount() {
    return columnClasses.length;
  }
  
  public String getColumnName(int i) {
    return columnNames[i];
  }
  
  public int getRowCount() {
    return messages.size();
  }
  
  public Object getValueAt(int row, int column) {
    LogMessage m = (LogMessage) messages.get(row);
    switch (column) {
    case 0:
      return Const.LOG_ICONS[m.priority];
    case 1:
      return m.thread;
    case 2:
      return format.format(new Date(m.time));
    case 3:
      return Const.STDOUT.equals(m.message) || Const.STDERR.equals(m.message) ? m.exception : m.message;
    }
    return "?";
  }
}
/*
  $Log: LogTableModel.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.1  2001/01/17 09:56:31  schubige
  Logger update
  
*/
