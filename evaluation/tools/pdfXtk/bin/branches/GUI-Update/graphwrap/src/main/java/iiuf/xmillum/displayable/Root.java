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

package iiuf.xmillum.displayable;

import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Displayable;
import iiuf.xmillum.DisplayableClass;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JScrollBar;

import org.w3c.dom.Element;

/**
 * Root
 *
 * Represents the root displayable, the (invisible) object that is shown
 * behind all other objects.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class Root
  extends DisplayableClass
{
  protected BrowserContext context;

  public void initialize(BrowserContext c, Element e) {
    context = c;
  }

  public Displayable getDisplayable(Element element) {
    Displayable d = new RootDisplayable(element);
    d.childs = getChilds(element, context);
    return d;
  }

  private class RootDisplayable extends Displayable {
    public RootDisplayable(Element e) {
      super(e);
    }

    public void paintObject(Graphics2D g, double scale) {
      // Nothing to do
    }

    public Rectangle getBounds(double scale) {
      Rectangle r = super.getBounds(scale);
      if (r == null) {
	return new Rectangle();
      } else {
	return r;
      }
    }

    public boolean mousePressed(MouseEvent event, double scale) {
      boolean m = super.mousePressed(event, scale);
      if (!m && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	return true;
      }
      return m;
    }

    public boolean mouseReleased(MouseEvent event, double scale) {
      boolean m = super.mouseReleased(event, scale);
      if (!m && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	event.getComponent().setCursor(Cursor.getDefaultCursor());
	return true;
      }
      return m;
    }

    public boolean mouseDragged(MouseEvent event, double scale) {
      boolean m = super.mouseDragged(event, scale);

      if (!m && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	Point distance = context.getDragDistance();

	JScrollBar hb = context.browserPanel.getHorizontalScrollBar();
	JScrollBar vb = context.browserPanel.getVerticalScrollBar();

	hb.setValue(hb.getValue()-distance.x);
	vb.setValue(vb.getValue()-distance.y);

	// Compensate for the distance we have moved
	event.translatePoint(-distance.x, -distance.y);

	context.setMousePosition(event);
	return true;
      }
      return m;
    }
  }
}
