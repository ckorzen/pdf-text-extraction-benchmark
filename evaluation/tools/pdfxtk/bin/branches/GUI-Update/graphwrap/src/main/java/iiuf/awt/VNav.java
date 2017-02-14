package iiuf.awt;

import java.awt.Dimension;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.Adjustable;
import java.util.Vector;

import iiuf.util.Util;
import iiuf.util.EventListenerList;

/**
   Vertival navigator implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class VNav 
  extends
  Canvas 
  implements
  MouseMotionListener,
  MouseListener,
  Adjustable,
  Runnable
{

  private static final int MIN_SIZE = 16;
  private Color             color;
  private Color             dark;
  private Color             bright;
  private int               delta;
  private int               startY;
  private boolean           startYvalid;
  private int               high;
  private int               low;
  private int               min = -1000;
  private int               max = 1000;
  private EventListenerList listeners = new EventListenerList();
  private int               fac = 2;
  private int               event_ms;
  private Thread            event_poster;
  private boolean           event_poster_running;
  private boolean           invValue;
 
  public VNav(int event_ms_) {
    event_ms = event_ms_;
    event_poster = new Thread(this);
    event_poster.start();
    addMouseMotionListener(this);
    addMouseListener(this);
  }
  
  public int  getOrientation()        {return Adjustable.VERTICAL;}
  public void setMinimum(int min_)    {min = min_; recalc();}
  public int  getMinimum()            {return min;}
  public void setMaximum(int max_)    {max = max_; recalc();}
  public int  getMaximum()            {return max;}
  public void setUnitIncrement(int u) {}
  public int  getUnitIncrement()      {return -1;}
  public void setBlockIncrement(int b){}
  public int  getBlockIncrement()     {return -1;}
  public void setVisibleAmount(int v) {}
  public int  getVisibleAmount()      {return -1;}
  public void setValue(int v)         {}
  public int  getValue()              {return delta;}
  
  public void addAdjustmentListener(AdjustmentListener listener) {
    listeners.add(AdjustmentListener.class, listener);
  }

  public void addAdjustmentListener(AdjustmentListener listener, boolean weak) {
    listeners.add(AdjustmentListener.class, listener, weak);
  }
  
  public void removeAdjustmentListener(AdjustmentListener listener) {
    listeners.remove(AdjustmentListener.class, listener);
  }
  
  public void mouseDragged(MouseEvent e) {
    if(startYvalid) {
      delta = startY - e.getY();
      delta *= delta > 0 ? fac * max : fac * -min;
      delta /= (getSize().height - MIN_SIZE);
      if(delta > max) delta = max;
      if(delta < min) delta = min;
      repaint();
      synchronized(this) {
	if(!event_poster_running)
	  notify();
      }
    }
  }
  public void mouseMoved(MouseEvent e)    {reset(e.getY()); }
  public void mouseClicked(MouseEvent e)  {}
  public void mouseEntered(MouseEvent e)  {}
  public void mouseExited(MouseEvent e)   {}
  public void mousePressed(MouseEvent e)  {reset(e.getY());}
  public void mouseReleased(MouseEvent e) {reset(e.getY());}
  
  public synchronized void run() {
    for(;;) {
      try{wait();}
      catch(InterruptedException e) {Util.printStackTrace(e);}
      event_poster_running = true;
      while(delta != 0) {
	AdjustmentListener[] l = (AdjustmentListener[])listeners.getListeners(AdjustmentListener.class);
	AdjustmentEvent e = new AdjustmentEvent(this, 
						AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
						AdjustmentEvent.TRACK, 
						invValue ? -delta : delta);
	for(int i = 0; i < l.length; i++)
	  l[i].adjustmentValueChanged(e);
	try{wait(event_ms);}
	catch(InterruptedException ex) {Util.printStackTrace(ex);}
      }
      event_poster_running = false;
    }
  }
  
  public void invertValue() {
    invValue = true;
  }
  
  public void normalValue() {
    invValue = false;
  }

  private void recalc() {
    if(max < 0) max = 0;
    if(min > 0) min = 0;
    fac = (max == 0 || min == 0) ? 1 : 2;
  }

  private void reset(int y) {
    startY      = y;
    startYvalid = startY >= low - MIN_SIZE && startY <= low;
    if(delta != 0) {
      delta = 0;
      repaint();
    }
  }

  public void paint(Graphics g) {
    if(color == null) {
      color  = getBackground();
      bright = color.brighter();
      dark   = color.darker();
    }
    Dimension size = getSize();
    size.width--;
    size.height -= 2;
    g.setColor(bright);
    high = delta > 0 ?  
      ((size.height - MIN_SIZE) * (max - delta)) / (fac * max) :
      (size.height - MIN_SIZE) / fac;
    low = delta < 0 ? 
      (((size.height - MIN_SIZE) * delta) / (fac * min)) + (size.height + MIN_SIZE) / fac :
      (size.height + MIN_SIZE) / fac;
    if(max == 0) {
      high = 0;
      low  -= size.height;
    }
    if(min == 0)
      low = size.height;
  
    size.height++;
    high++;
    g.setColor(bright);
    g.drawLine(1, high, size.width - 1, high);
    g.drawLine(1, high, 1,              low);
    
    g.drawLine(size.width, 0,           size.width, size.height);
    g.drawLine(0,          size.height, size.width, size.height);

    g.setColor(dark);
    g.drawLine(1,              low, size.width - 1, low);
    g.drawLine(size.width - 1, low, size.width - 1, high);
    
    g.drawLine(0, 0, size.width, 0);
    g.drawLine(0, 0, 0,          size.height);
  }
  
  public Dimension getMinimumSize() {
    return new Dimension(MIN_SIZE, MIN_SIZE * 2);
  }
  
  public Dimension getPreferredSize() {
    Dimension size = getSize();
    return new Dimension(MIN_SIZE, size.height < MIN_SIZE * 2 ? MIN_SIZE * 2 : size.height);
  }
  
  public static void main(String[] argv) {
    VNav nav0 = new VNav(200);
    nav0.setMaximum(100);
    nav0.setMinimum(-100);

    VNav nav1 = new VNav(200);
    nav1.setMaximum(0);
    nav1.setMinimum(-100);

    VNav nav2 = new VNav(200);
    nav2.setMaximum(100);
    nav2.setMinimum(0);
    
    AdjustmentListener l = new AdjustmentListener() {
	public void adjustmentValueChanged(AdjustmentEvent e) {
	  System.out.println(e);
	}
      };
    
    nav0.addAdjustmentListener(l);
    nav1.addAdjustmentListener(l);
    nav2.addAdjustmentListener(l);

    new Frame(nav0, true);
    new Frame(nav1, true);
    new Frame(nav2, true);
  }
}

/*
  $Log: VNav.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.9  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.8  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.7  2001/01/04 09:58:49  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.6  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.5  2000/01/13 08:55:34  schubige
  Fixed import in VNav

  Revision 1.4  2000/01/11 09:36:50  schubige
  added voter stuff

  Revision 1.3  1999/11/26 10:00:42  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***
  
*/
