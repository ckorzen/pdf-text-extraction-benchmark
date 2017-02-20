/* (C) 2002, DIUF, http://www.unifr.ch/diuf
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

import iiuf.xmillum.ActionHandler;
import iiuf.xmillum.ActionHandlerFactory;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Displayable;
import iiuf.xmillum.DisplayableAppearance;
import iiuf.xmillum.DisplayableClass;
import iiuf.xmillum.FlagManager;
import iiuf.xmillum.Parameter;
import iiuf.xmillum.ParameterException;
import iiuf.xmillum.Style;

import iiuf.dom.DOMUtils;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Polygon
 *
 * Represents a polygon shown by xmillum.
 *
 * <p>ActionHandlers:
 * <ul>
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
public class Polygon extends DisplayableClass {
 
  static Map parameters = new HashMap();

  /**
   * Set up the parameter handling functions.
   */
  static {
    parameters.put("style", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.style = c.styleRegistry.getStyle(v);
	}
      });
    parameters.put("click1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.click1Handler = v;
	  b.click1HandlerOpt = opt;
	}
      });
    parameters.put("click2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.click2Handler = v;
	  b.click2HandlerOpt = opt;
	}
      });
    parameters.put("click3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.click3Handler = v;
	  b.click3HandlerOpt = opt;
	}
      });
    parameters.put("press1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.press1Handler = v;
	  b.press1HandlerOpt = opt;
	}
      });
    parameters.put("press2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.press2Handler = v;
	  b.press2HandlerOpt = opt;
	}
      });
    parameters.put("press3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.press3Handler = v;
	  b.press3HandlerOpt = opt;
	}
      });
    parameters.put("over", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Polygon b = (Polygon) o;
	  b.overHandler = v;
	  b.overHandlerOpt = opt;
	}
      });
  }

  /** Style */
  Style  style;

  /** Mouse button 1 clicked */
  String click1Handler = null;
  String click1HandlerOpt = null;

  /** Mouse button 2 clicked */
  String click2Handler = null;
  String click2HandlerOpt = null;

  /** Mouse button 3 clicked */
  String click3Handler = null;
  String click3HandlerOpt = null;

  /** Mouse button 1 pressed */
  String press1Handler = null;
  String press1HandlerOpt = null;

  /** Mouse button 2 pressed */
  String press2Handler = null;
  String press2HandlerOpt = null;

  /** Mouse button 3 pressed */
  String press3Handler = null;
  String press3HandlerOpt = null;

  /** Mouse button over the object */
  String overHandler   = null;
  String overHandlerOpt = null;

  /** Holds the current browser context */
  BrowserContext context;

  /**
   * Initializes this class of Displayable.
   */
  public void initialize(BrowserContext c, Element e) {
    context = c;
    Parameter.setParameters(c, e, this, parameters);
  }

  /**
   * Returns a Polygon displayable.
   */
  public Displayable getDisplayable(Element element) {
    Point minPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Point maxPoint = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

    // Create the new polygon
    DisplayablePolygon d = new DisplayablePolygon(element);
    ArrayList points = new ArrayList();
    
    NodeList pointElements = DOMUtils.getChildsByTagName(element, "point");
    for (int i = 0; i < pointElements.getLength(); i++) {
      Element p    = (Element) pointElements.item(i);
      try {
	Point point = new Point(Integer.parseInt(p.getAttribute("x")),
				Integer.parseInt(p.getAttribute("y")));
	points.add(point);

	minPoint.x = Math.min(minPoint.x, point.x);
	minPoint.y = Math.min(minPoint.y, point.y);
	maxPoint.x = Math.max(maxPoint.x, point.x);
	maxPoint.y = Math.max(maxPoint.y, point.y);
      } catch (NumberFormatException e) {
      }
    }
    d.points = points;
    d.bounds = new Rectangle(minPoint.x, minPoint.y, maxPoint.x-minPoint.x, maxPoint.y-minPoint.y);

    return d;
  }

  /**
   * Represents a displayable polygon.
   */
  private class DisplayablePolygon extends Displayable {
    ArrayList points;
    int[] xpoints;
    int[] ypoints;

    public DisplayablePolygon(Element e) {
      super(e);
    }

    int[][] scaledPoints;
    double  scale = 0.0;

    int[][] getPoints(double s) {
      if (scaledPoints == null || scale != s) {
	scaledPoints = new int[2][points.size()];
	scale = s;

	xpoints = new int[points.size()];
	ypoints = new int[points.size()];
	for (int i = 0; i < points.size(); i++) {
	  scaledPoints[0][i] = (int) ((double) ((Point) points.get(i)).x * scale);
	  scaledPoints[1][i] = (int) ((double) ((Point) points.get(i)).y * scale);
	}
      }
      return scaledPoints;
    }
    
    public Rectangle getBounds(double scale) {
      return new Rectangle((int) (scale * bounds.x),
			   (int) (scale * bounds.y),
			   (int) Math.max(1, Math.ceil(scale * bounds.width)),
			   (int) Math.max(1, Math.ceil(scale * bounds.height)));
    }

    public void paintObject(Graphics2D g, double scale) {
      if (style != null) {
	style.setStyle(g);
      }
      Style[] styles = context.flagger.getStyles(element);
      for (int i = 0; i < styles.length; i++) {
	styles[i].setStyle(g);
      }

      int[][] p = getPoints(scale);

      if ((style != null) && style.isFilled()) {
	g.fillPolygon(p[0], p[1], p[0].length);
      } else {
	g.drawPolygon(p[0], p[1], p[0].length);
      }
    }

    public boolean mouseMovedAction(MouseEvent event) {     
      if (overHandler != null) {
	context.actionFactory.handleAction(overHandler, overHandlerOpt, this, context);
	return true;
      }
      return false;
    }
    
    public boolean mouseClickedAction(MouseEvent event) {
      if ((click1Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON1_MASK))) {
	context.actionFactory.handleAction(click1Handler, click1HandlerOpt, this, context);
	return true;
      }
      if ((click2Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	context.actionFactory.handleAction(click2Handler, click2HandlerOpt, this, context);
	return true;
      }
      if ((click3Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON3_MASK))) {
	context.actionFactory.handleAction(click3Handler, click3HandlerOpt, this, context);
	return true;
      }
      return super.mouseClickedAction(event);
    }

    public boolean mousePressedAction(MouseEvent event) {
      if ((press1Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON1_MASK))) {
	context.actionFactory.handleAction(press1Handler, press1HandlerOpt, this, context);
	return true;
      }
      if ((press2Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON2_MASK))) {
	context.actionFactory.handleAction(press2Handler, press2HandlerOpt, this, context);
	return true;
      }
      if ((press3Handler != null) && (0 != (event.getModifiers() & MouseEvent.BUTTON3_MASK))) {
	context.actionFactory.handleAction(press3Handler, press3HandlerOpt, this, context);
	return true;
      }
      return false;
    }

    public boolean mouseDraggedAction(MouseEvent event) {
      return false;
    }
  }
}
