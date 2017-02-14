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

package at.ac.tuwien.dbai.pdfwrap.gui.displayable;

//import iiuf.xmillum.displayable.*;

import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Displayable;
import iiuf.xmillum.DisplayableClass;
import iiuf.xmillum.Parameter;
import iiuf.xmillum.Style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * TextArea2
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
 * @version GraphWrap Beta 1
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser GUI 0.9
 */

public class TextArea2 extends DisplayableClass {
  static Map parameters = new HashMap();

  static RenderingHints hints;
	static {
	    hints = new RenderingHints(null);
	    hints.put(RenderingHints.KEY_FRACTIONALMETRICS,   RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    hints.put(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
	    hints.put(RenderingHints.KEY_ANTIALIASING ,       RenderingHints.VALUE_ANTIALIAS_ON);
	    hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	    hints.put(RenderingHints.KEY_COLOR_RENDERING,     RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	    hints.put(RenderingHints.KEY_DITHERING,           RenderingHints.VALUE_DITHER_DISABLE);
	}
  
  static {
    parameters.put("style", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
	  t.style = c.styleRegistry.getStyle(v);
	}
      });
    parameters.put("click1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
	  t.click1Handler = v;
	}
      });
    parameters.put("click2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
	  t.click2Handler = v;
	}
      });
    parameters.put("click3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
	  t.click3Handler = v;
	}
      });
    parameters.put("press1", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
	  t.press1Handler = v;
	}
      });
    parameters.put("press2", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
	  t.press2Handler = v;
	}
      });
    parameters.put("press3", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {	
	  TextArea2 t = (TextArea2) o;
	  t.press3Handler = v;
	}
      });
    parameters.put("over", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  TextArea2 t = (TextArea2) o;
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
  
  // boolean highlighted;
  boolean exampleInstance = false;
  boolean foundInstance = false;

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
      
      // java 5: Float fontSize = Float.parseFloat(element.getAttribute("font-size"));
      float fontSize = Float.parseFloat(element.getAttribute("font-size"));
      
      settings.put(TextAttribute.FONT, new Font(style.getFontAttributes(scale * fontSize)));
      
      AttributedCharacterIterator par = (new AttributedString(element.getAttribute("text"), settings)).getIterator();
      
      //style.fontSize = Float.parseFloat(element.getAttribute("font-size"));
      //style.setParam(context, this, element.getAttribute("font-size"));
     // this.style("fontsize", element.getAttribute("font-size"));
      
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
    	//Graphics2D g2d = (Graphics2D)g;
		g.setRenderingHints(hints);
    	
    	Rectangle bounds = getBounds(scale);

      if (style != null) style.setStyle(g);
      Style[] styles = context.flagger.getStyles(element);
      for (int i = 0; i < styles.length; i++) {
	styles[i].setStyle(g);
      }

      if (style.isFilled()) {
	Color c = g.getColor();
	// java 5: Float newCol = 0.8f;
	float newCol = 0.8f;
    try {
		newCol = Float.parseFloat(element.getAttribute("colour")) * 0.4f;
	} catch (Exception e) {
		e.printStackTrace();
	}
	Color d = new Color(1.0f, 1.0f, newCol);
	if (exampleInstance && !foundInstance)
		d = Color.ORANGE;
	else if (foundInstance && !exampleInstance)
		d = Color.MAGENTA;
	else if (foundInstance && exampleInstance)
		d = Color.PINK;
	
	// g.setColor(style.getBackground());
	g.setColor(d);
	g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
	g.setColor(d);
      }

      Point[] tp = getTextPositions(scale);
      TextLayout[] tl = getTextLines(scale);
      for (int i = 0; i < tp.length; i++) {
	tl[i].draw(g, tp[i].x, tp[i].y);
      }
    }

    /*
     * for use with Cluster
    public boolean mousePressed(MouseEvent event, double scale)
    {
    		System.out.println("mouse pressed; event = " + event);
    		return true;
    }
    
    public boolean mouseClicked(MouseEvent event, double scale)
    {
    		System.out.println("mouse clicked; event = " + event);
    		return true;
    }
    */
    
    public boolean mouseMovedAction(MouseEvent event) {
    	//System.out.println("mouse moved action; event = " + event);
      if (overHandler != null) {
	context.actionFactory.handleAction(overHandler, null, this, context);
	return true;
      }
      return false;
    }

    public boolean mouseClickedAction(MouseEvent event) {
    //	System.out.println("mouse clicked action; event = " + event);
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
    	//System.out.println("mouse pressed action; event = " + event);
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

public boolean isExampleInstance()
{
	return exampleInstance;
}

public void setExampleInstance(boolean exampleInstance)
{
	this.exampleInstance = exampleInstance;
}

public void highlightExampleInstance()
{
	setExampleInstance(true);
}

public boolean isFoundInstance()
{
	return foundInstance;
}

public void setFoundInstance(boolean foundInstance)
{
	this.foundInstance = foundInstance;
}

public void highlightFoundInstance()
{
	setFoundInstance(true);
}

public void clearHighlights()
{
	this.exampleInstance = false;
	this.foundInstance = false;
}
}
