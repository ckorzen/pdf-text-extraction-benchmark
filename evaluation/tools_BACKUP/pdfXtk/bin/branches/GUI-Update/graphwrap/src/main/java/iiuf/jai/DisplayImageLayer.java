
package iiuf.jai;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Base class representing an additionnal layer on a DisplayImagePanel.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public abstract class DisplayImageLayer {
  protected DisplayImagePanel panel;

  public DisplayImageLayer(DisplayImagePanel panel) {
    this.panel = panel;
  }

  public void repaint() {
    panel.repaint();
  }

  public void addMouseListener(MouseListener listener) {
    panel.addMouseListener(listener);
  }

  public void addMouseMotionListener(MouseMotionListener listener) {
    panel.addMouseMotionListener(listener);
  }

  public void addKeyListener(KeyListener listener) {
    panel.addKeyListener(listener);
  }

  public void setCursor(Cursor cursor) {
    panel.setCursor(cursor);
  }

  public Rectangle getViewRect() {
    return panel.getViewRect();
  }

  public abstract void paintLayer(Graphics2D g, Rectangle view);
}
