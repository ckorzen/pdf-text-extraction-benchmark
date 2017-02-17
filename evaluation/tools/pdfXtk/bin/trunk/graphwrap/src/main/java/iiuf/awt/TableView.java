package iiuf.awt;

import java.net.URL;
import java.awt.Frame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Button;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.Scrollbar;
import java.util.Vector;
import java.util.Hashtable;

import iiuf.db.Proxy;
import iiuf.util.Util;
import iiuf.util.CacheArray;
import iiuf.util.CacheArrayBackEnd;
import iiuf.util.EventListenerList;

/**
   Database table view implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class TableView 
  extends 
  Canvas
  implements
  AdjustmentListener,
  ItemSelectable,
  FindListener
{
  
  private final static int MIN_COL_SIZE = 10;
  private final static int DEF_COL_SIZE = 50;  
  private Proxy             req;
  private int               relop;
  private int               txt_height = -1;
  private int               start;
  private CacheArray        cache;
  private String[]          columns;
  private int[]             column_starts;
  private Vector            columns_v;
  private EventListenerList listeners = new EventListenerList();
  private int               dragX;
  private int               dragCol;
  private int               oldX;
  private Hashtable         labels = new Hashtable();
  private int               evt_id;
  private boolean           absNav;
  private int               limit = Integer.MAX_VALUE;

  public TableView(Proxy req, int relop) {
    this(req, relop, null);
  }

  public TableView(Proxy req, int relop, String[] columns_) {
    cache = new CacheArray(128, new CacheArrayBackEnd() {
	public int  chunkSize() {return 1;}
	public int  maxChunks() {return 25;}
	public void read(long start, Object[] data, int idx, int count) {
	  try {
	    Proxy[] resp = TableView.this.req.find((int)start, count, TableView.this.relop);
	    for(int i = 0; i < resp.length; i++)
	      data[idx++] = resp[i];
	  } catch(Exception e) {
	    e.printStackTrace();
	  }
	}
	public void write(long start, Object[] data, int idx, int count) {}
      });
    setRequest(req, relop);
    
    addMouseListener(new MouseListener() {
	public void mouseClicked(MouseEvent e) {
	  int id = getItemIdx(e.getY());
	  if(id < 0) return;
	  ItemEvent ev = new ItemEvent(TableView.this, 0, cache.elementAt(id), ItemEvent.SELECTED);
	  ItemListener[] l = (ItemListener[])listeners.getListeners(ItemListener.class);
	  for(int i = 0; i < l.length; i++)
	    l[i].itemStateChanged(ev);
	}
  	public void mouseEntered(MouseEvent e)  {}
  	public void mouseExited(MouseEvent e)   {}
  	public void mousePressed(MouseEvent e)  {}
  	public void mouseReleased(MouseEvent e) {}
      });
    
    addMouseMotionListener(new MouseMotionListener() {
	Cursor crsr      = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	
	public void mouseDragged(MouseEvent e) {
	  if(dragCol > 0) {
	    int     x           =  e.getX();
	    int     clipX       = column_starts[dragCol];
	    Cursor ncrsr = Cursor.getPredefinedCursor(x - TableView.this.oldX < 0 ? 
						      Cursor.W_RESIZE_CURSOR : 
						      Cursor.E_RESIZE_CURSOR);
	    TableView.this.oldX = x;
	    for(int i = dragCol; i < column_starts.length; i++) {
	      column_starts[i] += x - dragX;
	      if(column_starts[i] - column_starts[i - 1] < MIN_COL_SIZE)
		column_starts[i] = column_starts[i - 1] + MIN_COL_SIZE;
	    }
	    if(ncrsr != crsr) {
	      crsr = ncrsr;
	      setCursor(crsr);
	    }
	    dragX = column_starts[dragCol];
	    Dimension size = getSize();
	    if(dragX < clipX) clipX = dragX;
	    repaint(clipX, 0, size.width - x, size.height);
	  }
	}
	
	public void mouseMoved(MouseEvent e) {
	  Cursor  ncrsr = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	  int     x              = e.getX();
	  boolean dir            = x - TableView.this.oldX < 0;
	  TableView.this.oldX    = x;
	  TableView.this.dragCol = -1;
	  for(int i = 0; i < column_starts.length; i++)
	    if(x > (column_starts[i] - 5) &&
	       x < (column_starts[i] + 5)) {
	      ncrsr = Cursor.getPredefinedCursor(dir ? Cursor.W_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR);
	      TableView.this.dragX   = x;
	      TableView.this.dragCol = i;
	      break;
	    }
	  if(ncrsr != crsr) {
	    crsr = ncrsr;
	    setCursor(crsr);
	  }
	}
      });
    
    if(columns_ != null)
      for(int i = 0; i < columns_.length; i++)
	addColumn(columns_[i]);
  }
  
  public void setAbsNav() {
    absNav = true;
  }
  
  public void setRelNav() {
    absNav = false;
  }

  public void adjustmentValueChanged(AdjustmentEvent e) {
    if(absNav) setStart(e.getValue());
    else       addStart(e.getValue());
  }

  public Dimension getMinimumSize() {
    return new Dimension(columns_v.size() * MIN_COL_SIZE, 100);
  }
  
  public Dimension getPreferredSize() {
    if(column_starts.length > 0)
      return new Dimension(column_starts[column_starts.length - 1] + DEF_COL_SIZE, 200);
    else
      return new Dimension(0, 0);
  }

  public void addItemListener(ItemListener listener) {
    listeners.add(ItemListener.class, listener);
  }

  public void addItemListener(ItemListener listener, boolean weak) {
    listeners.add(ItemListener.class, listener, weak);
  }

  public void removeItemListener(ItemListener listener) {
    listeners.remove(ItemListener.class, listener);
  }

  public Object[] getSelectedObjects() {
    return null;
  }
  
  public synchronized void addColumn(String column) {
    addColumn(column, column);
  }

  public synchronized void addColumn(String column, String label) {
    columns_v.addElement(column);
    labels.put(column, label);
    updateColumns();
  }
  
  public synchronized void removeColumn(String column) {
    columns_v.removeElement(column);
    labels.remove(column);
    updateColumns();
  }
  
  private void updateColumns() {
    columns          = new String[columns_v.size()];
    column_starts    = new int[columns.length];
    for(int i = 0; i < columns.length; i++) {
      columns[i]       = (String)columns_v.elementAt(i);
      if(column_starts[i] < MIN_COL_SIZE)
	column_starts[i] = i == 0 ? 0 : column_starts[i - 1] + DEF_COL_SIZE;
    }
  }
  
  public void find(Proxy proxy, int relop_) {
    req   = proxy;
    relop = relop_;
    cache.flush();
    cache.clear();
    limit = Integer.MAX_VALUE;
    setStart(0, true);
  }

  public synchronized void setRequest(Proxy req_, int relop_) {
    req   = req_;
    relop = relop_;
    cache.flush();
    cache.clear();
    columns_v = new Vector();
    limit = Integer.MAX_VALUE;
    updateColumns();
    setStart(0, true);
  }

  public void update(Graphics g) {
    paint(g);
  }
  
  private int getItemIdx(int y) {
    return y < txt_height ? -1 : start -1 + y / txt_height;
  }
  
  public synchronized void paint(Graphics g) {
    txt_height      = g.getFontMetrics().getHeight();
    int asc         = g.getFontMetrics().getAscent();
    Rectangle clip  = g.getClipBounds();
    Object[]  range = cache.elementsAt(start, (clip.y + clip.height) / txt_height);
    
    // draw rectangles
    g.setColor(Color.white);
    g.fillRect(clip.x, 0, clip.width, txt_height);
    for(int row = 1; row <= range.length; row++) {
      g.setColor(((start + row ) & 1) == 1 ? Color.lightGray : Color.white);
      g.fillRect(clip.x, txt_height * row, clip.width, txt_height);
    }
    // draw h-line
    g.setColor(Color.black);
    g.drawLine(clip.x, txt_height, clip.x + clip.width, txt_height);
    // draw columns
    asc += txt_height;
    for(int col = 0; col < columns.length; col++) {
      int x = column_starts[col];
      g.setClip(clip.x, clip.y, clip.width, clip.height);
      g.clipRect(x, 0, 
		 ((col + 1 < columns.length) ? column_starts[col + 1] : x + clip.width) - x, 
		 clip.height);
      // draw v-line
      g.drawLine(x, 0, x, clip.y + clip.height);
      // draw title
      g.drawString(columns[col], x + 2, asc - txt_height);
      // draw column
      for(int row = 0; row < range.length; row++)
	if(range[row] != null) {
	  try {
	    g.drawString(((Proxy)range[row]).getField(columns[col]).stringValue(0), 
			 x + 2, 
			 txt_height * row + asc);
	  } catch(Exception e) {
	    e.printStackTrace();
	  }
	}
	else {
	  if(limit == Integer.MAX_VALUE)
	    limit = start + row - 1;
	  break;
	}
    }
  }
  
  public synchronized final void addStart(int amount) {
    setStart(start + amount);
  }
  
  public final void setStart(int start) {
    setStart(start, false);
  }

  private synchronized final void setStart(int start_, boolean repaint) {
    if(start_ < 0)     start_ = 0; 
    if(start_ > limit) start_ = limit;
    if(start_ != start || repaint) {
      start = start_;
      repaint();
    }
  }

  public synchronized final int getStart() {
    return start;
  }
  
  static TableView tv;
  
  public static void main(String[] argv) {
    try {
      Proxy p = (Proxy)Class.forName(argv[1]).getConstructor(new Class[] {iiuf.db.Connection.class}).newInstance(new Object[] {new iiuf.db.fmpro.Connection(new URL(argv[0]))});
      
      tv = new TableView(p, Proxy.AND);
      
      for(int i = 2; i < argv.length; i++)
	tv.addColumn(argv[i]);

      tv.addItemListener(new ItemListener() {
	  public void itemStateChanged(ItemEvent e) {
	    System.out.println(e.getItem());
	  }
	});

      ActionListener bh = new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	    if(e.getActionCommand().equals("UP"))
	      tv.addStart(+1);
	    else if(e.getActionCommand().equals("DOWN"))
	      tv.addStart(-1);
	  }
	};
      
      Frame f = new Frame();
      
      Scrollbar sb = new Scrollbar(Scrollbar.VERTICAL, 0, 10, 0, 437);
      sb.addAdjustmentListener(new AdjustmentListener() {
	  public void adjustmentValueChanged(AdjustmentEvent e) {
	    tv.setStart(e.getValue());
	  }
	});
      f.setLayout(new GridBagLayout());
      f.add(tv, Awt.constraints(false, GridBagConstraints.BOTH));
      f.add(sb, Awt.constraints(true, GridBagConstraints.VERTICAL));
      f.pack();
      f.setVisible(true);
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

/*
  $Log: TableView.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.7  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.6  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.4  2000/01/11 09:36:50  schubige
  added voter stuff

  Revision 1.3  1999/11/26 10:00:41  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***
  
*/
