package iiuf.jai;

import java.awt.Rectangle;
import java.awt.Graphics2D;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Base class for rectangles to be represented using RectDisplayImageLayer.

   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class DisplayRect
  extends Rectangle
{
  public DisplayRect(Rectangle r) {
    super(r);
  }

  public void paintObject(Graphics2D g) {
    g.drawRect(x, y, width, height);
  }
}
