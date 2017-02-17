package iiuf.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
   Map of Rectangles
   
   (c) 2001, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class RectMap {
  List rectangles = new ArrayList();

  public RectMap() {
  }

  public void add(Rectangle r) {
    rectangles.add(r);
  }

  public RectMap getIntersecting(final Rectangle rect) {
    return filter(new RectMapFilter() {
	public boolean filter(Rectangle r) {
	  return rect.intersects(r);
	}
      });
  }

  public RectMap getContained(final Rectangle rect) {
    return filter(new RectMapFilter() {
	public boolean filter(Rectangle r) {
	  return rect.contains(r);
	}
      });
  }
  
  public RectMap getSurrounding(final Rectangle rect) {   
    return filter(new RectMapFilter() {
	public boolean filter(Rectangle r) {
	  return r.contains(rect);
	}
      });
  }

  public void extract(RectMap rm) {
    rectangles.removeAll(rm.values());
  }

  public Collection values() {
    return rectangles;
  }

  public RectMap filter(RectMapFilter rmf) {
    RectMap rm = new RectMap();
    Iterator i = rectangles.iterator();
    while (i.hasNext()) {
      Rectangle r = (Rectangle) i.next();
      if (rmf.filter(r)) {
	rm.add(r);
      }
    }
    return rm;
  }

  public String toString() {
    return "RectMap["+rectangles.size()+"]";
  }
}
