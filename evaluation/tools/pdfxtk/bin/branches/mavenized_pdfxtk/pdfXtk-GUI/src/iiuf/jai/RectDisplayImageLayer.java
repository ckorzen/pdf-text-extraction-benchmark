
package iiuf.jai;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

/**
   (c) 1999, IIUF<p>

   Layer for displaying rectangles.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class RectDisplayImageLayer
  extends DisplayImageLayer
{
  protected Color foreground = Color.blue;

  private int[] ystart, ystop;

  private HashMap yrect;

  /** Constructs a RectDisplayImageLayer object.

      @param rects ArrayList containing the rectangles to display */

  public RectDisplayImageLayer(ArrayList rects, DisplayImagePanel panel) {
    super(panel);
    initIndexes(rects);
  }

  /** Initializes the indexes for faster accessing of the visible rectangles.

      @param rects ArrayList containing the rectangles to display */

  public void setRectangles(ArrayList rects) {
    initIndexes(rects);
  }

  /** Initializes the indexes for faster accessing of the visible rectangles.

      @param rects ArrayList containing the rectangles to display */

  protected void initIndexes(ArrayList rects) {
    yrect = new HashMap();

    int xmax = 0;
    int ymax = 0;

    ListIterator li = rects.listIterator();
    while (li.hasNext()) {
      Rectangle r = (Rectangle) li.next();
      HashMap xrect = (HashMap) yrect.get(new Integer(r.y));
      if (xrect == null) {
	xrect = new HashMap();
	yrect.put(new Integer(r.y), xrect);
      }
      xrect.put(new Integer(r.x), r);

      ymax = Math.max(r.y, ymax);
      ymax = Math.max(r.y + r.height, ymax);
    }

    ystart = new int[ymax];
    ystop = new int[ymax];

    for (int y = 0; y < ymax; y++) {
      ystart[y] = y;
      ystop[y] = y;
    }

    for (int y = 0; y < ymax; y++) {
      HashMap xrect = (HashMap) yrect.get(new Integer(y));
      if (xrect != null) {
	Iterator values = xrect.values().iterator();
	while (values.hasNext()) {
	  Rectangle r = (Rectangle) values.next();
	  int rstart = r.y;
	  int rstop = r.y + r.height;
	  for (int yy = rstart; yy < rstop; yy++) {
	    if (ystart[yy] > rstart) ystart[yy] = rstart;
	    if (ystop[yy] < rstop) ystop[yy] = rstop;
	  }
	}
      }
    }
  }

  /** Sets the color of the rectangles.

      @param color Rectangles color. */

  public void setColor(Color color) {
    foreground = color;
  }

  /** Paints one of the rectangles.

      @param g The current graphics context
      @param r The rectangle to paint */

  protected void paintObject(Graphics2D g, Rectangle r) {
    g.drawRect(r.x, r.y, r.width, r.height);
  }
  
  /** Paints this layer.

      @param g The current graphics context
      @param view The currently visible portion of this layer */

  public void paintLayer(Graphics2D g, Rectangle view) {
    int rstart, rstop;

    // Get start and stop coordinates of possible rectangles in this view
    if (view.y < ystart.length) {
      rstart = ystart[view.y];
    } else {
      rstart = ystart.length;
    }

    if (view.y + view.height < ystop.length) {
      rstop = ystop[view.y+view.height];
    } else {
      rstop = ystop.length;
    }

    // Set the foreground color
    if (foreground != null) {
      g.setColor(foreground);
    }

    // Check if the objects at these positions are visible and
    // draw them if necessary.

    for (int y = rstart; y < rstop; y++) {
      HashMap xrect = (HashMap) yrect.get(new Integer(y));
      if (xrect != null) {
	Iterator values = xrect.values().iterator();
	while (values.hasNext()) {
	  Rectangle r = (Rectangle) values.next();
	  if (view.contains(r) || view.intersects(r)) {
	    if (r instanceof DisplayRect) {
	      ((DisplayRect) r).paintObject(g);
	    } else {
	      paintObject(g, r);
	    }
	  }
	}
      }
    }
  }
}
