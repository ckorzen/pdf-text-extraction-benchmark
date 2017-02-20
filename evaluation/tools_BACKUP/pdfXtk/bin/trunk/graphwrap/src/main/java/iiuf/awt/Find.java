package iiuf.awt;

import java.awt.Panel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.Choice;
import java.awt.Component;
import java.awt.TextField;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Vector;

import iiuf.util.EventListenerList;
import iiuf.util.Strings;
import iiuf.util.Unicode;
import iiuf.db.Proxy;
import iiuf.db.Field;

/**
   Database finder.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Find 
  extends
  Panel 
  implements
  Unicode,
  ItemListener
{
  public static String[] ENGLISH = {
    "",            // 0x00 (NOP)
    "begins with", // 0x01 (BEGINS_WITH)
    "contains",    // 0x02 (CONTAINS)
    "ends with",   // 0x03 (ENDS_WITH)
    "=",           // 0x04 (EQUALS)
    ">",           // 0x05 (GREATER_THAN) 
    ">=",          // 0x06 (GREATER_THAN_OR_EQUALS) 
    "<",           // 0x07 (LESS_THAN) 
    "<=",          // 0x08 (LESS_THAN_OR_EQUALS) 
    "!=",          // 0x09 (NOT_EQUALS) 
    "",            // 0x0A ()
    "",            // 0x0B ()
    "",            // 0x0C ()
    "",            // 0x0D ()
    "",            // 0x0E ()
    "",            // 0x0F ()
    "ascending",   // 0x10 (ASCENDING)
    "descending",  // 0x11 (DESCENDING)
    "custom",      // 0x12 (CUSTOM)
  };
  public static String[] GERMAN =  {
    "",                  // 0x00 (NOP)
    "beginnt mit",       // 0x01 (BEGINS_WITH)
    "enth"+auml+"lt",    // 0x02 (CONTAINS)
    "endet mit",         // 0x03 (ENDS_WITH)
    "=",                 // 0x04 (EQUALS)
    ">",                 // 0x05 (GREATER_THAN) 
    ">=",                // 0x06 (GREATER_THAN_OR_EQUALS) 
    "<",                 // 0x07 (LESS_THAN) 
    "<=",                // 0x08 (LESS_THAN_OR_EQUALS) 
    "!=",                // 0x09 (NOT_EQUALS) 
    "",                  // 0x0A ()
    "",                  // 0x0B ()
    "",                  // 0x0C ()
    "",                  // 0x0D ()
    "",                  // 0x0E ()
    "",                  // 0x0F ()
    "aufsteigend",       // 0x10 (ASCENDING)
    "absteigend",        // 0x11 (DESCENDING)
    "benutzerdefiniert", // 0x12 (CUSTOM)
  };
  public static String[] FRENCH =  {
    "",            // 0x00 (NOP)
    "", // 0x01 (BEGINS_WITH)
    "",    // 0x02 (CONTAINS)
    "",   // 0x03 (ENDS_WITH)
    "",           // 0x04 (EQUALS)
    "",           // 0x05 (GREATER_THAN) 
    "",          // 0x06 (GREATER_THAN_OR_EQUALS) 
    "",           // 0x07 (LESS_THAN) 
    "",          // 0x08 (LESS_THAN_OR_EQUALS) 
    "",          // 0x09 (NOT_EQUALS) 
    "",            // 0x0A ()
    "",            // 0x0B ()
    "",            // 0x0C ()
    "",            // 0x0D ()
    "",            // 0x0E ()
    "",            // 0x0F ()
    "",   // 0x10 (ASCENDING)
    "",  // 0x11 (DESCENDING)
    "",      // 0x12 (CUSTOM)
  };

  private Choice            and_or    = new Choice();
  private Font              bf        = new Font("SansSerif", Font.BOLD, 12);
  private InfoList          sortorder = new InfoList(4);
  private Proxy             proxy;
  private EventListenerList listeners = new EventListenerList();
  private int               relop;

  public Find(Proxy proxy, String[] labels, 
	      String   field_str,
	      String   value,
	      String   sort,
	      String   sortorder_str,
	      String[] columns,
	      int      relop) {
    this(proxy, labels, field_str, null, value, sort, sortorder_str, columns, relop);
  }

  public Find(Proxy proxy, String[] labels, 
	      String   field_str,
	      String[] and_or_strs,
	      String   value,
	      String   sort,
	      String   sortorder_str,
	      String[] columns) {
    this(proxy, labels, field_str, and_or_strs, value, sort, sortorder_str, columns, 0);
  }
  
  private Find(Proxy proxy_, String[] labels, 
	       String   field_str,
	       String[] and_or_strs,
	       String   value,
	       String   sort,
	       String   sortorder_str,
	       String[] columns,
	       int      relop_) {
    proxy = proxy_;
    relop = relop_;
    setLayout(new GridBagLayout());
    
    if(and_or_strs != null) {
      and_or.add(and_or_strs[0]);
      and_or.add(and_or_strs[1]);
      and_or.addItemListener(this);
    }
    
    Label tmp = new Label(field_str, Label.RIGHT);
    tmp.setFont(bf);
    add(tmp,    Awt.constraints(false, GridBagConstraints.HORIZONTAL, 0.0f, 0.0f));
    
    if(relop != Proxy.AND && relop != Proxy.OR) {
      add(and_or, Awt.constraints(false));
      relop = Proxy.AND;
    }
    else
      add(new Component() {});

    tmp = new Label(value);
    tmp.setFont(bf);
    add(tmp,    Awt.constraints(false));
    
    tmp = new Label(sort);
    tmp.setFont(bf);
    add(tmp,    Awt.constraints(true));
    
    Field[] fields = proxy.getFields();
    for(int j = 0; j < columns.length; j++)
      for(int i = 0; i < fields.length; i++)
	if(columns[j].equals(fields[i].name))
	  addField(fields[i], labels);
    
    tmp = new Label(sortorder_str);
    tmp.setFont(bf);
    add(tmp, Awt.constraints(true));
    add(sortorder, Awt.constraints(true, GridBagConstraints.BOTH));
    sortorder.addItemListener(this);
  }
  
  public void addFindListener(FindListener listener) {
    listeners.add(FindListener.class, listener);
  }

  public void addFindListener(FindListener listener, boolean weak) {
    listeners.add(FindListener.class, listener, weak);
  }

  public void removeFindListener(FindListener listener) {
    listeners.remove(FindListener.class, listener);
  }
  
  void callFindListeners() {
    FindListener[] l = (FindListener[])listeners.getListeners(FindListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].find(proxy, relop);
  }
  
  void sortAdd(FieldLine field, int index) {
    if(!sortorder.contains(field.field.name))
      sortorder.add(field.field.name, index, field);
  }
  
  void sortRemove(String field) {
    if(sortorder.contains(field))
      sortorder.remove(field);
  }
  
  public void itemStateChanged(ItemEvent e) {
    if(e.getItemSelectable() == sortorder) {
      FieldLine field = (FieldLine)sortorder.getSelectedInfo();
      sortRemove(field.field.name);
      sortAdd(field, 0);
      Object[] infos = sortorder.getInfos();
      for(int i = 0; i < infos.length; i++)
	((FieldLine)infos[i]).set();
    }
    else {
      callFindListeners();
      relop = and_or.getSelectedIndex() == 0 ? Proxy.AND : Proxy.OR;
    }
  }
  
  int sortIdx(Field field) {
    String[] items = sortorder.getItems();
    for(int i = 0; i < items.length; i++)
      if(items[i].equals(field.name))
	return i;
    return -1;
  }
  
  private void addField(Field field, String[] labels) {
    new FieldLine(this, field, this, labels);
  }
    
  static Find      find;
  static TableView tv;

  public static void main(String[] argv) {
    try {
      Proxy p = (Proxy)Class.forName(argv[1]).getConstructor(new Class[] {iiuf.db.Connection.class}).newInstance(new Object[] {new iiuf.db.fmpro.Connection(new URL(argv[0]))});
      
      String[] fields = new String[argv.length - 2];
      for(int i = 0; i < fields.length; i++)
	fields[i] = argv[i + 2];
      
      find = new Find(p, Find.ENGLISH, "Field", new String[] {"AND", "OR"}, "Value", "Sorting", "Sort order:", fields);
      tv   = new TableView(p, Proxy.AND);
      find.addFindListener(tv);
      
      for(int i = 0; i < fields.length; i++)
	tv.addColumn(fields[i]);
      
      Frame f = new Frame();
      Frame f2 = new Frame();
      f.add(find);
      f.pack();
      f.setVisible(true);

      VNav vnav = new VNav(500);
      vnav.setMinimum(-20);
      vnav.setMaximum(20);
      vnav.invertValue();
      f2.setLayout(new GridBagLayout());
      f2.add(tv, Awt.constraints(false,  GridBagConstraints.BOTH));
      f2.add(vnav, Awt.constraints(true, GridBagConstraints.VERTICAL));
      vnav.addAdjustmentListener(tv);
      f2.pack();
      f2.setVisible(true);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}

class FieldLine 
  implements
  ItemListener,
  ActionListener
{
  Choice    op; 
  Choice    sort;
  TextField val   = new TextField(20);
  Field     field;
  Find      find;

  FieldLine(Find find_, Field field_, Container c, String[] labels) {
    find  = find_;
    field = field_;
    op    = newOpChoice(labels);
    sort  = newSortChoice(labels);  
    c.add(new Label(field.name, Label.RIGHT), Awt.constraints(false, GridBagConstraints.HORIZONTAL, 0.0f, 0.0f));
    c.add(op,   Awt.constraints(false));
    c.add(val,  Awt.constraints(false, GridBagConstraints.HORIZONTAL, 1.0f, 0.0f));
    c.add(sort, Awt.constraints(true));
    op.addItemListener(this);
    sort.addItemListener(this);
    val.addActionListener(this);
  }
  
  private Choice newOpChoice(String[] labels) {
    Choice result = new Choice();
    for(int i = Field.NOP; i <= Field.NOT_EQUALS; i++)
      result.add(labels[i]);
    return result;
  }
  
  private Choice newSortChoice(String[] labels) {
    Choice result = new Choice();
    result.add(labels[Field.NOP]);
    for(int i = Field.ASCENDING; i <= Field.CUSTOM; i++)
      result.add(labels[i]);
    return result;
  }
  
  void set() {
    field.compare(val.getText(), op.getSelectedIndex()); 
    int idx = sort.getSelectedIndex();
    if(idx > 0) {
      idx += (Field.ASCENDING -1);
      find.sortAdd(this, -1);
    }
    else {
      find.sortRemove(field.name);
    }
    field.sort(idx, find.sortIdx(field));
    find.callFindListeners();
  }
  
  public void itemStateChanged(ItemEvent e)  {set();}
  public void actionPerformed(ActionEvent e) {set();}
}

/*
  $Log: Find.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.6  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.5  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2001/01/03 12:40:18  schubige
  graph stuff beta

  Revision 1.3  1999/11/26 10:00:30  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***
  
*/
