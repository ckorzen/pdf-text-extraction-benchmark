/* (C) 2000-2002, DIUF, http://www.unifr.ch/diuf
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package iiuf.xmillum;

import iiuf.dom.DOMUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import javax.swing.JComponent;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Displayable
 *
 * This class defines one specific displayable element.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public abstract class Displayable {

  /** All children */
  public Displayable[] childs = new Displayable[0];

  /** Bounding box */
  public Rectangle bounds;

  /** Element which produced this object */
  public Element element;

  /**
   * Constructs a Displayable.
   *
   * @param e Element bound to this displayable.
   */
  public Displayable(Element e) {
    element = e;
  }

  /**
   * Searches the displayable that represents a given element.
   *
   * @param e Element whose Displayable we are looking for.
   * @return Displayable representing the given element.
   */
  public Displayable getDisplayableForElement(Element e) {
    if (element == e) {
      return this;
    }
    for (int i = 0; i < childs.length; i++) {
      Displayable result = childs[i].getDisplayableForElement(e);
      if (result != null) {
	return result;
      }
    }
    return null;
  }

  /**
   * Paints the element.
   *
   * @param g Current graphics context.
   */
  public final void paint(Graphics2D g, double scale) {
    Rectangle b = getBounds(scale);
    if (b.intersects(g.getClipBounds())) {
      paintObject(g, scale);
      if (appearance != null) {
	if (appearance.paint(g, scale)) {
	  appearance = null;
	}
      }
      paintChildren(g, scale);
    }
  }

  private DisplayableAppearance appearance;

  public void setAppearance(DisplayableAppearance a) {
    appearance = a;
  }

  /**
   * Paints the object. Overridden by subclasses.
   *
   * @param g Current graphics context.
   */
  public abstract void paintObject(Graphics2D g, double scale);

  /**
   * Paints the children of this object.
   *
   * @param g Current graphics context.
   */
  public final void paintChildren(Graphics2D g, double scale) {
    for (int i = 0; i < childs.length; i++) {
      childs[i].paint(g, scale);
    }
  }

  /**
   * Returns the bounds of this object and all contained children.
   *
   * @return Bounding box.
   */
  public Rectangle getBounds(double scale) {
    Rectangle b = null;
    for (int i = 0; i < childs.length; i++) {
      Rectangle r = childs[i].getBounds(scale);
      if (b == null) {
	b = new Rectangle(r);
      } else {
	enlargeBounds(r, b);
      }
    }
    bounds = b;
    return b;
  }  

  /**
   * Enlarge one rectangle by another one.
   *
   * @param r1 Rectangle that enlarges the other one.
   * @param r2 Rectangle to be enlarged.
   */
  private void enlargeBounds(Rectangle r1, Rectangle r2) {
    if (r1.x < r2.x) {
      r2.width += r2.x - r1.x;
      r2.x = r1.x;
    }
    if (r1.y < r2.y) {
      r2.height += r2.y - r1.y;
      r2.y = r1.y;
    }
    if (r1.x+r1.width > r2.x+r2.width) {
      r2.width = r1.x + r1.width - r2.x;
    }
    if (r1.y+r1.height > r2.y+r2.height) {
      r2.height = r1.y + r1.height - r2.y;
    }
  }

  /**
   * Checks if (x,y) is inside the bounds of this object.
   *
   * @param x horizontal coordinate
   * @param y vertical coordinate
   * @return true if (x, y) is within the bounds
   */
  public boolean contains(int x, int y, double scale) {
    Rectangle b = getBounds(scale);
    return b.contains(x, y);
  }

  /** Get highlight color corresponding to another color. */

  private static Hashtable hilightColors = new Hashtable();

  protected Color getHilightColor(Color rgbColor) {
    Color h = (Color) hilightColors.get(rgbColor);
    if (h == null) {
      float[] rgb = rgbColor.getRGBColorComponents(null);
      float[] hsb = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], null);
      h = Color.getHSBColor(hsb[0]+0.5F, hsb[1], Math.max(1.0F, hsb[2]*2));
      hilightColors.put(rgbColor, h);
    }
    return h;
  }

  /** Mouse Clicked */

  public boolean mouseClicked(MouseEvent event, double scale) {
    return mouseClickedRecursive(event, scale);
  }

  protected boolean mouseClickedRecursive(MouseEvent event, double scale) {
    for (int i = 0; i < childs.length; i++) {
      if (childs[i].contains(event.getX(), event.getY(), scale)) {
    	  //System.out.println("mouse clicked recursive in level " + i);
	if (childs[i].mouseClicked(event, scale)) return true;
      }
    }
    return mouseClickedAction(event);
  }

  protected boolean mouseClickedAction(MouseEvent event) {
    return false;
  }

  /** Mouse Pressed */

  public boolean mousePressed(MouseEvent event, double scale) {
    return mousePressedRecursive(event, scale);
  }

  protected boolean mousePressedRecursive(MouseEvent event, double scale) {
    for (int i = 0; i < childs.length; i++) {
      if (childs[i].contains(event.getX(), event.getY(), scale)) {
    	  //System.out.println("mouse pressed recursive in level " + i);
	if (childs[i].mousePressed(event, scale)) return true;
      }
    }
    return mousePressedAction(event);
  }

  protected boolean mousePressedAction(MouseEvent event) {
    return false;
  }

  /** Mouse Released */

  public boolean mouseReleased(MouseEvent event, double scale) {
    return mouseReleasedRecursive(event, scale);
  }

  protected boolean mouseReleasedRecursive(MouseEvent event, double scale) {
    for (int i = 0; i < childs.length; i++) {
      if (childs[i].contains(event.getX(), event.getY(), scale)) {
	if (childs[i].mouseReleased(event, scale)) return true;
      }
    }
    return mouseReleasedAction(event);
  }

  protected boolean mouseReleasedAction(MouseEvent event) {
    return false;
  }

  /** Mouse Dragged */

  public boolean mouseDragged(MouseEvent event, double scale) {
    return mouseDraggedRecursive(event, scale);
  }

  protected boolean mouseDraggedRecursive(MouseEvent event, double scale) {
    for (int i = 0; i < childs.length; i++) {
      if (childs[i].contains(event.getX(), event.getY(), scale)) {
	if (childs[i].mouseDragged(event, scale)) return true;
      }
    }
    return mouseDraggedAction(event);
  }

  protected boolean mouseDraggedAction(MouseEvent event) {
    return false;
  }

  /** Mouse Moved */

  public boolean mouseMoved(MouseEvent event, double scale) {
    return mouseMovedRecursive(event, scale);
  }

  protected boolean mouseMovedRecursive(MouseEvent event, double scale) {
    for (int i = 0; i < childs.length; i++) {
      if (childs[i].contains(event.getX(), event.getY(), scale)) {
	if (childs[i].mouseMoved(event, scale)) return true;
      }
    }
    return mouseMovedAction(event);
  }

  protected boolean mouseMovedAction(MouseEvent event) {
    return false;
  }
}
