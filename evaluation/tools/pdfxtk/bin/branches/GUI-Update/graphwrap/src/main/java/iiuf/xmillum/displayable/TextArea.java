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

import iiuf.dom.DOMUtils;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Displayable;
import iiuf.xmillum.DisplayableClass;
import iiuf.xmillum.FlagManager;
import iiuf.xmillum.Parameter;
import iiuf.xmillum.Style;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.text.StringCharacterIterator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * TextArea
 *
 * xmillum text area. Shows text in the xmillum main window.
 *
 * <p>Optional initialization parameters:
 * <ul>
 *   <li>color: color of the text
 *       (default: black)
 *   <li>family: family name (<code>helvetica</code>, <code>times</code> etc.)
 *       (default: <code>helvetica</code>)
 *   <li>size: in points
 *       (default: 12)
 *   <li>slope: <code>upright</code>, <code>slanted</code>
 *       (default: <code>upright</code>)
 *   <li>weight: <code>regular</code>, <code>medium</code>, <code>bold</code>,
 *       <code>extrabold</code>
 *       (default: <code>regular</code>)
 *   <li>resolution: resolution in dpi of the coordinates (used to
 *       calculate size of the fonts correctly)
 *       (default: 300)
 *   <li>direction: <code>l2r</code>, <code>r2l</code>
 *       (default: <code>l2r</code>)
 *   <li>background: lighten or darken the background using the given color
 *        (default: white)
 * </ul>
 *
 * <p>ActionHandlers:
 *  <ul>
 *   <li>over: triggered when the mouse is over this object
 *   <li>click1: mouse button 1 clicked
 *   <li>click2: mouse button 2 clicked
 *   <li>click3: mouse button 3 clicked
 *   <li>press1: mouse button 1 press & hold
 *   <li>press2: mouse button 2 press & hold
 *   <li>press3: mouse button 3 press & hold
 * </ul>
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class TextArea extends DisplayableClass {
  static Map parameters = new HashMap();

  static {
    parameters.put("style", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.style = c.styleRegistry.getStyle(v);
	}
      });
    parameters.put("click1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.click1Handler = v;
	}
      });
    parameters.put("click2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.click2Handler = v;
	}
      });
    parameters.put("click3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.click3Handler = v;
	}
      });
    parameters.put("press1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.press1Handler = v;
	}
      });
    parameters.put("press2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.press2Handler = v;
	}
      });
    parameters.put("press3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {	
	  TextArea t = (TextArea) o;
	  t.press3Handler = v;
	}
      });
    parameters.put("over", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea t = (TextArea) o;
	  t.overHandler = v;
	}
      });
  }

  Style   style;
  String  click1Handler = null;
  String  click2Handler = null;
  String  click3Handler = null;
  String  press1Handler = null;
  String  press2Handler = null;
  String  press3Handler = null;
  String  overHandler   = null;

  BrowserContext context;

  public void initialize(BrowserContext c, Element e) {
    context = c;

    Parameter.setParameters(c, e, this, parameters);
  }

  public Displayable getDisplayable(Element element) {
    // Create the new block
    DisplayableBlock d = new DisplayableBlock(element);
    try {
      d.bounds  = new Rectangle(Integer.parseInt(element.getAttribute("x")),
				Integer.parseInt(element.getAttribute("y")),
				Integer.parseInt(element.getAttribute("w")),
				Integer.parseInt(element.getAttribute("h")));
    } catch (NumberFormatException e) {
    }
    return d;
  }

  private class DisplayableBlock extends Displayable {
    boolean visible = false;

    public DisplayableBlock(Element e) {
      super(e);
    }

    double       textScale;
    TextLayout[] textLines;
    Point[]      textPositions;

    TextLayout[] getTextLines(double scale) {
      if (textLines == null || textScale != scale) {
	rescale(scale);
      }
      return textLines;
    }
    
    Point[] getTextPositions(double scale) {
      if (textPositions == null || textScale != scale) {
	rescale(scale);
      }
      return textPositions;
    }

    void rescale(double scale) {
      Rectangle bounds = getBounds(scale);

      HashMap settings = new HashMap();
      settings.put(TextAttribute.FONT, new Font(style.getFontAttributes(scale)));
      
      AttributedCharacterIterator par = (new AttributedString(element.getAttribute("text"), settings)).getIterator();
      LineBreakMeasurer lbm = new LineBreakMeasurer(par, new FontRenderContext(null, false, false));
      
      ArrayList drawList = new ArrayList();
      
      int parEnd   = par.getEndIndex();
      
      int positionX;
      int positionY = bounds.y;
      lbm.setPosition(par.getBeginIndex());
      while (lbm.getPosition() < parEnd) {
	TextLayout layout = lbm.nextLayout(bounds.width);
	positionX = bounds.x;
	if (!layout.isLeftToRight()) {
	  positionX += bounds.width - (int) layout.getAdvance();
	}
	positionY += layout.getAscent();
	if (positionY > bounds.y+bounds.height) break;
	drawList.add(new Point(positionX, positionY));
	drawList.add(layout);
	positionY += layout.getDescent() + layout.getLeading();
      }
      
      textPositions = new Point[drawList.size()/2];
      textLines     = new TextLayout[drawList.size()/2];
      textScale     = scale;

      for (int i = 0; i < textPositions.length; i++) {
	textPositions[i] = (Point)      drawList.get(i*2);
	textLines[i]     = (TextLayout) drawList.get(i*2+1);
      }
    }
    
    public Rectangle getBounds(double scale) {
      if (bounds != null) {
	return new Rectangle((int) (scale * bounds.x),
			     (int) (scale * bounds.y),
			     Math.max(1, (int) Math.ceil(scale * bounds.width)),
			     Math.max(1, (int) Math.ceil(scale * bounds.height)));
      } else {
	return new Rectangle();
      }
    }
    
    public void paintObject(Graphics2D g, double scale) {
      Rectangle bounds = getBounds(scale);

      if (style != null) style.setStyle(g);
      Style[] styles = context.flagger.getStyles(element);
      for (int i = 0; i < styles.length; i++) {
	styles[i].setStyle(g);
      }

      if (style.isFilled()) {
	Color c = g.getColor();
	g.setColor(style.getBackground());
	g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
	g.setColor(c);
      }

      Point[] tp = getTextPositions(scale);
      TextLayout[] tl = getTextLines(scale);
      for (int i = 0; i < tp.length; i++) {
	tl[i].draw(g, tp[i].x, tp[i].y);
      }
    }

    public boolean mouseMovedAction(MouseEvent event) {
      if (overHandler != null) {
	context.actionFactory.handleAction(overHandler, null, this, context);
	return true;
      }
      return false;
    }

    public boolean mouseClickedAction(MouseEvent event) {
      if ((click1Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON1_MASK))) {
	context.actionFactory.handleAction(click1Handler, null, this, context);
	return true;
      }
      if ((click2Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	context.actionFactory.handleAction(click2Handler, null, this, context);
	return true;
      }
      if ((click3Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON3_MASK))) {
	context.actionFactory.handleAction(click3Handler, null, this, context);
	return true;
      }
      return super.mouseClickedAction(event);
    }

    public boolean mousePressedAction(MouseEvent event) {
      if ((press1Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON1_MASK))) {
	context.actionFactory.handleAction(press1Handler, null, this, context);
	return true;
      }
      if ((press2Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	context.actionFactory.handleAction(press2Handler, null, this, context);
	return true;
      }
      if ((press3Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON3_MASK))) {
	context.actionFactory.handleAction(press3Handler, null, this, context);
	return true;
      }
      return false;
    }
  }
}
