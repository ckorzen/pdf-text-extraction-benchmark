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

package iiuf.xmillum.handlers;

import iiuf.dom.DOMUtils;
import iiuf.xmillum.ActionHandler;
import iiuf.xmillum.ActionHandlerParam;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Displayable;
import iiuf.xmillum.DisplayableAppearance;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Split
 *
 * ActionHandler that allow to split rectangular blocks.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class Split extends ActionHandler {

  final int DIR_VERTICAL   = 0;
  final int DIR_HORIZONTAL = 1;

  int splitDirection = DIR_VERTICAL;

  public void init(BrowserContext c, Element e) {
  }

  public void handle(ActionHandlerParam param) {
    String option = param.getOption();
    if (option != null) {
      if (option.equals("turn")) {
	if (splitDirection == DIR_HORIZONTAL) {
	  splitDirection = DIR_VERTICAL;
	} else {
	  splitDirection = DIR_HORIZONTAL;
	}
      } else if (option.equals("show")) {
	showSplit(param.getContext(), param.getDisplayable());
      } else if (option.equals("split")) {
	split(param.getContext(), param.getDisplayable());
      }
    }
  }

  Displayable lastObject;

  void showSplit(BrowserContext context, final Displayable d) {
    final Point position = context.getMousePosition();

    if (lastObject != null && lastObject != d) {
      context.browserPanel.repaintArea(lastObject.getBounds(context.getScale()));
    }

    d.setAppearance(new DisplayableAppearance() {
	public boolean paint(Graphics2D g, double scale) {
	  Rectangle bounds = d.getBounds(scale);

	  g.setPaintMode();
	  g.setColor(java.awt.Color.red);
	  if (splitDirection == DIR_VERTICAL) {
	    g.drawLine(bounds.x, position.y, bounds.x+bounds.width, position.y);
	  } else {
	    g.drawLine(position.x, bounds.y, position.x, bounds.y+bounds.height-1);
	  }
	  return true;
	}
      });
    context.browserPanel.repaintArea(d.getBounds(context.getScale()));
    lastObject = d;
  }

  void split(BrowserContext context, Displayable d) {
    Element e = d.element;

    String reference = e.getAttribute("ref");
    Element original = context.getSourceElementByReference(reference);

    if (original == null) return;

    Element splitted = (Element) original.cloneNode(false);

    int x, y, w, h;

    try {
      x = Integer.parseInt(e.getAttribute("x"));
      y = Integer.parseInt(e.getAttribute("y"));
      w = Integer.parseInt(e.getAttribute("w"));
      h = Integer.parseInt(e.getAttribute("h"));
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
      return;
    }

    // Calculate position of mouse given the current scale
    Point position = context.getMousePosition();
    position.x = (int) ((double) position.x / context.getScale());
    position.y = (int) ((double) position.y / context.getScale());

    if (splitDirection == DIR_VERTICAL) {
      original.setAttribute("height", ""+(position.y-y-1));

      splitted.setAttribute("y", ""+position.y);
      splitted.setAttribute("height", ""+(y+h-position.y));
    } else {
      original.setAttribute("width", ""+(position.x-x-1));

      splitted.setAttribute("x", ""+position.x);
      splitted.setAttribute("width", ""+(x+w-position.x));
    }

    original.getParentNode().appendChild(splitted);

    context.retransform();
  }
}
