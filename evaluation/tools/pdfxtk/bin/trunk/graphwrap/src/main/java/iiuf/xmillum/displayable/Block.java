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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

/**
 * Block
 *
 * Represents a rectangular block shown by xmillum.
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
public class Block extends DisplayableClass {
 
  static Map parameters = new HashMap();

  /**
   * Set up the parameter handling functions.
   */
  static {
    parameters.put("style", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Block b = (Block) o;
	  b.style = c.styleRegistry.getStyle(v);
	}
      });
    parameters.put("click1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
	  b.click1Handler = v;
	  b.click1HandlerOpt = opt;
	}
      });
    parameters.put("click2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
	  b.click2Handler = v;
	  b.click2HandlerOpt = opt;
	}
      });
    parameters.put("click3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
	  b.click3Handler = v;
	  b.click3HandlerOpt = opt;
	}
      });
    parameters.put("press1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
	  b.press1Handler = v;
	  b.press1HandlerOpt = opt;
	}
      });
    parameters.put("press2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
	  b.press2Handler = v;
	  b.press2HandlerOpt = opt;
	}
      });
    parameters.put("press3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
	  b.press3Handler = v;
	  b.press3HandlerOpt = opt;
	}
      });
    parameters.put("over", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  Block b = (Block) o;
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
   * Returns a Block displayable.
   */
  public Displayable getDisplayable(Element element) {
    // Create the new block
    DisplayableBlock d = new DisplayableBlock(element);
    try {
      d.bounds = new Rectangle(Integer.parseInt(element.getAttribute("x")),
			       Integer.parseInt(element.getAttribute("y")),
			       Integer.parseInt(element.getAttribute("w")),
			       Integer.parseInt(element.getAttribute("h")));
    } catch (NumberFormatException e) {
    }

    // Recursively build tree
    d.childs = getChilds(element, context);
    return d;
  }

  /**
   * Represents a displayable block.
   */
  private class DisplayableBlock extends Displayable {
    public DisplayableBlock(Element e) {
      super(e);
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
      Rectangle b = getBounds(scale);

      if (style != null) {
	style.setStyle(g);
      }
      Style[] styles = context.flagger.getStyles(element);
      for (int i = 0; i < styles.length; i++) {
	styles[i].setStyle(g);
      }

      if ((style != null) && style.isFilled()) {
	g.fill(b);
      } else {
	g.draw(b);
      }
    }

    /**
     * Mouse moved - calls the "over" handler
     *
     * @param context Current browser context
     * @param event Mouse event
     */
    public boolean mouseMovedAction(MouseEvent event) {     
      if (overHandler != null) {
	context.actionFactory.handleAction(overHandler, overHandlerOpt, this, context);
	return true;
      }
      return false;
    }

    /**
     * Mouse clicked - calls the "clickX" handler
     *
     * @param context Current browser context
     * @param event Mouse event
     */    
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

    /**
     * Mouse pressed - calls the "pressX" handler
     *
     * @param context Current browser context
     * @param event Mouse event
     */
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
  }
}
