package iiuf.awt;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.TextComponent;
import java.awt.Frame;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.io.InputStream;
import java.lang.reflect.Method;
import javax.swing.ImageIcon;

import iiuf.util.Util;
import iiuf.util.AsyncInvocation;
import iiuf.util.AsyncAccelerator;
import iiuf.log.Log;

/**
   Awt utilities.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Awt {
  public  static Cursor       STOP_CURSOR = makeCursor("stop", new Point(8, 8));
  private static Component    emptyCmp    = newComponent();
  private static final double PI2         = Math.PI * 2;
  
  public static double getAngle(int ox, int oy, int x, int y) {
    double dx = x - ox;
    double dy = y - oy;
    double r  = Math.sqrt(dx * dx + dy * dy);
    double result = dy < 0 ? Math.PI + Math.asin(dx / r) : -Math.asin(dx / r);
    result %= PI2;
    return result < 0 ? PI2 + result : result;
  }
  
  public static Point place(Rectangle rectToPlace, int x, int y, Rectangle outerLimit) {
    rectToPlace.setLocation(x - rectToPlace.width / 2, y - rectToPlace.height / 2);
    return fitSmallerIntoBigger(rectToPlace.x, rectToPlace.y, rectToPlace.width, rectToPlace.height, outerLimit);
  }
  
  public static Point centerOnScreen(Dimension dim) {
    Toolkit   tk = Toolkit.getDefaultToolkit();
    Dimension sd = tk.getScreenSize();
    return new Point((sd.width - dim.width) / 2, (sd.height - dim.height) / 2);
  }

  public static Point fitSmallerIntoBigger(int x, int y, int width, int height, Rectangle bigger) {
    return new Point(x < bigger.x ? 
		     bigger.x : 
		     x + width  > bigger.x + bigger.width  ? bigger.x + bigger.width  -
		     width  : x,
		     y < bigger.y ? 
		     bigger.y : 
		     y + height > bigger.y + bigger.height ? bigger.y + bigger.height -
		     height : y);
  }
  
  public final static boolean near(int x, int y, int x0, int y0, int x1, int y1, int tolerance) {
    int t2    = tolerance * tolerance;
    int x1_x0 = x1 - x0;
    int y1_y0 = y1 - y0;
    int x_x0  = x  - x0;
    int y_y0  = y  - y0;
    int denom = x1_x0 * x1_x0 + y1_y0 * y1_y0;
    if(denom == 0)
      return x_x0 * x_x0 + y_y0 * y_y0 <= t2;
    else {
      int nom = x_x0 * x1_x0 + y_y0 * y1_y0;
      if(nom < 0 || nom > denom) 
	return false;
      else {
	double u = (double)nom / (double)denom;
	double dx = x - (x0 + u * x1_x0);
	double dy = y - (y0 + u * y1_y0);
	return dx * dx + dy * dy <= t2;
      }
    }
  }
  
  private static Cursor makeCursor(String name, Point hotSpot) {
    return makeCursor(Awt.class.getResource("rsrc/" + name + "_crsr.gif"), hotSpot, name);
  }
  
  public  static Cursor makeCursor(URL cursorImage, Point hotSpot, String name) {
    Toolkit   tk      = Toolkit.getDefaultToolkit();
    int       ccolors = tk.getMaximumCursorColors();
    ImageIcon ic      = null;
    // try to get optimized version
    try {
      String url = cursorImage.toExternalForm();
      int    idx = url.lastIndexOf('.');
      int    bpp = -1;
      for(bpp = 31; bpp >= 0; bpp--)
	if((ccolors & (1 << bpp)) != 0)
	  break;
      if(idx == -1)
	url += bpp + "bpp";
      else
	url = url.substring(0, idx) + bpp + "bpp" + url.substring(idx);
      URL curl = new URL(url);
      InputStream in = curl.openStream();
      in.close();
      ic = new ImageIcon(new URL(url));
    } catch(Exception e) {
      ic = new ImageIcon(cursorImage);
    }
    Image     crsr = ic.getImage();
    Dimension d    = tk.getBestCursorSize(ic.getIconWidth(), ic.getIconHeight());
    if(d.width != ic.getIconWidth() || d.height != ic.getIconHeight()) {
      Image tmp = crsr;
      crsr = new BufferedImage(d.width, d.height, BufferedImage.TYPE_4BYTE_ABGR);
      ic.paintIcon(emptyCmp, crsr.getGraphics(), 0, 0);
    } 
    return tk.createCustomCursor(crsr, hotSpot, name);
  }

  public static void setCursor(Container container, int cursor) {
    Component[] cmps = container.getComponents();
    container.setCursor(Cursor.getPredefinedCursor(cursor));
    for(int i = 0; i < cmps.length; i++) {
      if(cmps[i] instanceof Container)
	setCursor((Container)cmps[i], cursor);
      else if (cmps[i] instanceof TextComponent && 
	       cursor == Cursor.DEFAULT_CURSOR)
	cmps[i].setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      else
	cmps[i].setCursor(Cursor.getPredefinedCursor(cursor));
    }
  }

  public static Window getWindow(Component component) {
    if(component == null) return null;
    if(component instanceof Window) return (Window)component;
    else return getWindow(component.getParent());
  }
  
  public static Frame getFrame(Component component) {
    if(component == null) return null;
    if(component instanceof Frame) return (Frame)component;
    else return getFrame(component.getParent());
  }
  
  public static void setFrameCursor(Component component, int cursor) {
    setCursor(getFrame(component), cursor);
  }
  
  public static GridBagConstraints constraints(boolean last) {
  	return constraints(last, 0, 0, GridBagConstraints.NONE);
  }

  public static GridBagConstraints constraints(boolean last, int fill) {
  	return constraints(last, 0, 0, fill);
  }
  
  public static GridBagConstraints constraints(boolean last, int fill, float weightx, float weighty) {
    return constraints(last, 0, 0, fill, weightx, weighty);
  }
  
  public static GridBagConstraints constraints(boolean last, int insetx, int insety) {
    return constraints(last, insetx, insety, GridBagConstraints.NONE);
  }
  
  public static GridBagConstraints constraints(boolean last, int insetx, int insety, int fill) {
    float weightx = 0;
    float weighty = 0;
    if(fill == GridBagConstraints.HORIZONTAL) weightx = 1;
    if(fill == GridBagConstraints.VERTICAL)   weighty = 1;
    if(fill == GridBagConstraints.BOTH)
      weightx =weighty = 1;
    return constraints(last, insetx, insety, fill, weightx, weighty);
  }
  
  public static GridBagConstraints constraints(boolean last, 
					       int insetx, int insety, 
					       int fill, 
					       float weightx, float weighty) {
    GridBagConstraints result = new GridBagConstraints();
    result.weightx = weightx;
    result.weighty = weighty;
    result.anchor = GridBagConstraints.WEST;
    result.fill   = fill;
    result.insets.left   = result.insets.right = insetx;
    result.insets.bottom = result.insets.top   = insety;
    if(last) result.gridwidth = GridBagConstraints.REMAINDER;
    return result;
  }
  
  public static Button newButton(String label, ActionListener listener) {
    return newButton(label, label, listener);
  }

  public static Button newButton(String label, String command, ActionListener listener) {
    Button result = new Button(label);
    result.addActionListener(listener);
    result.setActionCommand(command);
    return result;
  }
  
  public static int getIndex(Container container, Component component) {
    Component[] cmps = container.getComponents();
    for(int i = 0; i < cmps.length; i++)
      if(component == cmps[i]) {
	return i;
      }
    return -1;
  }

  static class EmptyComponent extends Component {}

  public static Component newComponent() {
    return new EmptyComponent() {};
  }

  // ----------------------- async stuff
  
  private static AsyncInvocation async = new AsyncInvocation("AWT-AsyncInvocation");
  
  public static AsyncInvocation.Result invokeAsync(Object object, String method, Class argType, Object arg) {
    return async.invoke(object, method, argType, arg);
  }
  
  private static boolean check(Method m, String method, Class argType) {
    return 
      m.getName().equals(method) && 
      argType == m.getParameterTypes()[0] &&
      m.getParameterTypes().length == 1;
  }

  public static void addAsyncAccelerator(AsyncAccelerator accel) {
    async.addAsyncAccelerator(accel);
  }
  
  static {
    addAsyncAccelerator(new AsyncAccelerator() {
	public void handle(AsyncInvocation.Invocation i, AsyncInvocation.Result result) {
	  if(i.object instanceof MouseListener) {
	    MouseListener ml = (MouseListener)i.object;
	    if(     check(i.method, "mouseClicked",  MouseEvent.class))	ml.mouseClicked( (MouseEvent)i.args[0]);
	    else if(check(i.method, "mouseEntered",  MouseEvent.class))	ml.mouseEntered( (MouseEvent)i.args[0]);
	    else if(check(i.method, "mouseExited",   MouseEvent.class))	ml.mouseExited(  (MouseEvent)i.args[0]);
	    else if(check(i.method, "mousePressed",  MouseEvent.class))	ml.mousePressed( (MouseEvent)i.args[0]);
	    else if(check(i.method, "mouseReleased", MouseEvent.class)) ml.mouseReleased((MouseEvent)i.args[0]);
	    else throw new IllegalArgumentException("Unknown MouseListener method:" + i.method);
	    result.set(null);
	  } 
	}
      });
    
    addAsyncAccelerator(new AsyncAccelerator() {
	public void handle(AsyncInvocation.Invocation i, AsyncInvocation.Result result) {
	  if(i.object instanceof ActionListener) {
	    ActionListener al = (ActionListener)i.object;
	    if(check(i.method, "actionPerformed",  ActionEvent.class)) al.actionPerformed( (ActionEvent)i.args[0]);
	    else throw new IllegalArgumentException("Unknown MouseListener method:" + i.method);
	    result.set(null);
	  } 
	}
      });
  }
  
  private static MouseListener tmpMouseListener;
  public static synchronized MouseListener asyncWrapper(MouseListener listener_) {
    tmpMouseListener = listener_;
    
    return new MouseListener() {
	MouseListener listener = tmpMouseListener;
	
	public void mouseClicked(MouseEvent e)  {invokeAsync(listener, "mouseClicked",  MouseEvent.class, e);}
	public void mouseEntered(MouseEvent e)  {invokeAsync(listener, "mouseEntered",  MouseEvent.class, e);}
	public void mouseExited(MouseEvent e)   {invokeAsync(listener, "mouseExited",   MouseEvent.class, e);}
	public void mousePressed(MouseEvent e)  {invokeAsync(listener, "mousePressed",  MouseEvent.class, e);}
	public void mouseReleased(MouseEvent e) {invokeAsync(listener, "mouseReleased", MouseEvent.class, e);}
      };
  }

  private static ActionListener tmpActionListener;
  public static synchronized ActionListener asyncWrapper(ActionListener listener_) {
    tmpActionListener = listener_;
    
    return new ActionListener() {
	ActionListener listener = tmpActionListener;
	
	public void actionPerformed(ActionEvent e)  {invokeAsync(listener, "actionPerformed",  ActionEvent.class, e);}
      };
  }
}
/*
  $Log: Awt.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.16  2001/03/09 15:30:50  schubige
  Added markers to graph panel

  Revision 1.15  2001/03/02 09:06:43  schubige
  interim checkin for soundium

  Revision 1.14  2001/03/01 10:42:48  schubige
  interim checkin for soundium

  Revision 1.13  2001/02/23 11:03:15  schubige
  try to recover table_source.png

  Revision 1.12  2001/02/12 17:50:05  schubige
  still working on soundium gui

  Revision 1.11  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.10  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.9  2001/01/03 08:30:39  schubige
  graph stuff beta

  Revision 1.8  2000/12/28 09:29:09  schubige
  SourceWatch beta

  Revision 1.7  2000/12/18 12:39:08  schubige
  Added ports to iiuf.util.graph

  Revision 1.6  2000/11/20 17:36:56  schubige
  tinja project ide

  Revision 1.5  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.4  2000/07/14 13:40:54  schubige
  Various updates/fixes

  Revision 1.3  1999/12/02 16:07:32  schubige
  updated block, general cleanup

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***

  Revision 1.6  1999/09/10 06:54:18  schubige
  Dialogs & requesters are now placed at 1/2 x and 1/3 y of the screen.

  Revision 1.5  1999/09/09 14:31:20  schubige
  Added DateChooser, Dilog and Line, updated Awt.

  Revision 1.4  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.

  Revision 1.3  1999/09/03 14:06:14  schubige
  MacCVS testing
  
  Revision 1.2  1999/09/02 14:15:28  schubige
  added @serial tag or transient to make javadoc happy
*/
