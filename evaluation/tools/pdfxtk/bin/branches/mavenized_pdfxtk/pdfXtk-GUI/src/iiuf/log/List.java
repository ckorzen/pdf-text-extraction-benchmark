package iiuf.log;

import iiuf.swing.TableSorter;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import iiuf.swing.Swing;
import iiuf.swing.JWindowToolBarUI;
import iiuf.awt.BorderLayout;

/**
   Graphical log list.<p>

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class List
  extends JFrame
  implements LogMessageListener
{
  private static final Font MONOSPACE = new Font("Monospaced", Font.PLAIN, 12);

  protected LogTableModel model    = new LogTableModel();
  protected JTextArea     info     = new JTextArea("", 15, 80);
  protected JToolBar      toolbar  = new JToolBar(JToolBar.VERTICAL);
  protected JTable        table;

  public List() {
    super("Log history by thread");

    toolbar.setUI(new JWindowToolBarUI());
    // Toolbar
    for (int i = 0; i < Const.LOG_ICONS.length; i++) {
      toolbar.add(new PriorityButton(i));
    }
    toolbar.addSeparator();

    JButton button;
    toolbar.add(button = new JButton(new ImageIcon(List.class.getResource("clear.gif"))));
    button.setMargin(new Insets(0,0,0,0));
    button.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  clear();
	}
      });

    final TableSorter sorter = new TableSorter(model);
    table = new JTable(sorter);
    table.getTableHeader().addMouseListener(sorter.createHeaderMouseListener());

    getContentPane().setLayout(new BorderLayout());
    
    info.setFont(MONOSPACE);
    
    getContentPane().add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
					new JScrollPane(table), 
					new JScrollPane(info)), BorderLayout.CENTER);
    getContentPane().add(toolbar, BorderLayout.WEST);
    
    table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	  if (!e.getValueIsAdjusting()) {
	    int selected = table.getSelectedRow();
	    if (selected != -1) {
	      LogMessage m = (LogMessage)model.messages.get(sorter.mapRowToData(selected));
	      info.setText(Const.LOG_STRINGS[m.priority] + " from " + m.thread + " @ " + model.format.format(new Date(m.time)) + "\n" +
			   m.message + "\n" + 
			   m.getException());
	    }
	  }
	}
      });
    
    Swing.setWidth(table.getColumnModel().getColumn(0), 20);
    
    for (int i = 0; i < priority.length; i++) {
      priority[i] = true;
    }

    pack();
    setSize(500, 500);
    setVisible(true);
  }
    
  protected boolean[] priority = new boolean[Const.LOG_STRINGS.length];

  protected synchronized void clear() {
    model.clear();
  }
  
  protected void enablePriority(int p) {
    priority[p] = true;
  }

  protected void disablePriority(int p) {
    priority[p] = false;
  }

  // LogMessageListener

  public synchronized void newConnection(InetAddress host) {
    if(System.getProperty("log.clearonconnect") != null) {
      clear();
    }
  }
  
  public synchronized void handle(LogMessage m) {
    if (priority[m.priority])
      model.add(m);
  }

  class PriorityButton
    extends JToggleButton
  {
    private int priority;
    public PriorityButton(int p) {
      super(Const.LOG_ICONS[p], true);
      priority = p;
      setMargin(new Insets(0,0,0,0));
      setToolTipText("Filter "+Const.LOG_STRINGS[priority]);
      addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	    if (((JToggleButton) e.getSource()).isSelected()) {
	      enablePriority(priority);
	    } else {
	      disablePriority(priority);
	    }
	  }
	});
    }
  }
}

/*
  $Log: List.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.9  2001/03/01 10:42:48  schubige
  interim checkin for soundium

  Revision 1.8  2001/01/17 09:55:45  schubige
  Logger update

  Revision 1.7  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.5  2000/10/11 13:31:10  hitz
  Adapted to new TableSorter, fixed bug with selected row.

  Revision 1.4  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.3  2000/10/10 14:19:34  hitz
  Added a couple of new features.

  
*/
