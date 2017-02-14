/* (C) 2001-2002, DIUF, http://www.unifr.ch/diuf
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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Style
 *
 * Represents a drawing style.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class Style {

  static Map parameters = new HashMap();

  boolean   isFilled    = false;
  boolean   isXOR       = false;
  boolean   isHilight   = false;

  Color     foreground;
  Color     background;
  Composite composite;

  String    fontFamily    = "helvetica";
  Float     fontSlope     = TextAttribute.POSTURE_REGULAR;
  Float     fontWeight    = TextAttribute.WEIGHT_REGULAR;
  float     fontSize      = 8.0f;
  Boolean   textDirection = TextAttribute.RUN_DIRECTION_LTR;
  Stroke    stroke        = new BasicStroke(1.0f);

  int       resolution    = 300;

  static {
    parameters.put("foreground", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  s.foreground = Color.getColor(v);
	  if (s.foreground == null) {
	    c.log("Color "+v+" not found, using red instead");
	    s.foreground = Color.red;
	  }
	}
      });
    parameters.put("background", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  s.background = Color.getColor(v);
	  if (s.background == null) {
	    c.log("Color "+v+" not found, using red instead");
	    s.background = Color.red;
	  }
	}
      });
    parameters.put("xor", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  if (trueFalse(v)) s.isXOR = true;
	}
      });
    parameters.put("fill", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  if (trueFalse(v)) s.isFilled = true;
	}
      });
    parameters.put("hilight", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  if (trueFalse(v)) s.isHilight = true;
	}
      });
    parameters.put("transparency", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  float transparency = 0.0f;
	  if (v != null) {
	    try {
	      transparency = Float.parseFloat(v);
	    } catch (NumberFormatException n) {
	      throw new ParameterException("Not a valid transparency value "+v);
	    }
	    if (transparency < 0.0f || transparency > 1.0f) {
	      throw new ParameterException("Not a valid transparency value "+transparency);
	    }
	  }
	  s.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
	}
      });
    parameters.put("fontfamily", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  s.fontFamily = v;
	}
      });
    parameters.put("fontweight", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  if ("regular".equals(v)) {
	    s.fontWeight = TextAttribute.WEIGHT_REGULAR;
	  } else if ("medium".equals(v)) {
	    s.fontWeight = TextAttribute.WEIGHT_MEDIUM;
	  } else if ("bold".equals(v)) {
	    s.fontWeight = TextAttribute.WEIGHT_BOLD;
	  } else if ("extrabold".equals(v)) {
	    s.fontWeight = TextAttribute.WEIGHT_EXTRABOLD;
	  } else {
	    throw new ParameterException("Unknown fontweight value "+v);
	  }
	}
      });
    parameters.put("fontslope", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  if ("upright".equals(v)) {
	    s.fontSlope = TextAttribute.POSTURE_REGULAR;
	  } else if ("slanted".equals(v)) {
	    s.fontSlope = TextAttribute.POSTURE_OBLIQUE;
	  } else {
	    throw new ParameterException("Unknown fontslope value "+v);
	  }
	}
      });
    parameters.put("fontsize", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  try {
	    s.fontSize = Float.parseFloat(v);
	  } catch (NumberFormatException n) {
	    throw new ParameterException("Not a valid fontsize value "+v);
	  }
	}
      });
    parameters.put("textdirection", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  if ("l2r".equals(v)) {
	    s.textDirection = TextAttribute.RUN_DIRECTION_LTR;
	  } else if ("r2l".equals(v)) {
	    s.textDirection = TextAttribute.RUN_DIRECTION_RTL;
	  } else {
	    throw new ParameterException("Not a valid textdirection value "+v);
	  }
	}
      });
    parameters.put("resolution", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  try {
	    s.resolution = Integer.parseInt(v);
	  } catch (NumberFormatException n) {
	    throw new ParameterException("Not a valid resolution value "+v);
	  }
	}
      });
    parameters.put("stroke-width", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) throws ParameterException {
	  Style s = (Style) o;
	  try {
	    s.stroke = new BasicStroke(Float.parseFloat(v));
	  } catch (NumberFormatException n) {
	    throw new ParameterException("Not a valid stroke-width value "+v);
	  }
	}
      });
  }

  /**
   * Creates a default style.
   */

  public Style() {
  }

  /**
   * Creates a style with the specified XOR value
   */

  public Style(boolean hl) {
    isHilight = hl;
  }

  /**
   * Creates a new style.
   *
   * @param context Current browser context
   * @param name Name of the style
   * @param e Element containing the style description
   */

  public Style(BrowserContext context, String name, Element e) {
    Parameter.setParameters(context, e, this, parameters);
  }

  /**
   * Set style parameters on a graphics object.
   *
   * @param g Graphics object to update
   */

  public void setStyle(Graphics2D g) {
    g.setStroke(getStroke());
    if (isHilight()) {
      Color c = g.getColor();
      g.setColor(getHilightColor(c));
      c = g.getBackground();
      g.setBackground(getHilightColor(c));
    } else {
      Color c = getForeground();
      if (c != null) g.setColor(c);
      c = getBackground();
      if (c != null) g.setBackground(c);
      if (getComposite() != null) {
	g.setComposite(getComposite());
      } else if (isXOR()) {
	g.setXORMode(Color.white);
      } else {
	g.setPaintMode();
      }
    }
  }

  /**
   * Returns whether to hilight or not
   *
   * @return true for hilight, false otherwise
   */

  public boolean isHilight() {
    return isHilight;
  }

  /**
   * Returns whether to fill areas or not.
   *
   * @return true for fill, false otherwise
   */

  public boolean isFilled() {
    return isFilled;
  }

  /**
   * Returns whether to draw in XOR mode or not.
   *
   * @return true for XOR mode, false otherwise
   */

  public boolean isXOR() {
    return isXOR;
  }

  /**
   * Returns the foreground color.
   *
   * @return foreground color or null
   */

  public Color getForeground() {
    return foreground;
  }

  /**
   * Returns the background color.
   *
   * @return background color or null
   */

  public Color getBackground() {
    return background;
  }

  /**
   * Returns the composite.
   *
   * @return composite or null if the composite is not changed
   */

  public Composite getComposite() {
    return composite;
  }

  /**
   * Returns the stroke.
   *
   * @return composite or null if the composite is not changed
   */

  public Stroke getStroke() {
    return stroke;
  }

  /**
   * Returns the font attributes for use in new Font().
   * 
   * @param scale The current zoom value (0 < scale)
   * @return font attributes
   */

  public Map getFontAttributes(double scale) {
    HashMap fontAttributes = new HashMap();
    fontAttributes.put(TextAttribute.FAMILY,        fontFamily);
    fontAttributes.put(TextAttribute.WEIGHT,        fontWeight);
    fontAttributes.put(TextAttribute.POSTURE,       fontSlope);

    float rs = (float) resolution / (float) Toolkit.getDefaultToolkit().getScreenResolution();
    float size = rs * (float) scale * fontSize;
    fontAttributes.put(TextAttribute.SIZE,          new Float(size));

    fontAttributes.put(TextAttribute.RUN_DIRECTION, textDirection);
    return fontAttributes;
  }

  /** Cache for already calculated colors */

  static Map hilightColors = new HashMap();

  /**
   * Calculates hilight colors.
   *
   * @param rgbColor Base color
   * @return "hilighted" version of rgbColor
   */

  Color getHilightColor(Color rgbColor) {
    Color h = (Color) hilightColors.get(rgbColor);
    if (h == null) {
      float[] rgb = rgbColor.getRGBColorComponents(null);
      // Some implementations return values between 0 and 1, others between 0 and 255...
//       if (rgb[0] <= 1.0 && rgb[1] <= 1.0 && rgb[2] < 1.0) {
// 	rgb[0] = rgb[0]*256;
// 	rgb[1] = rgb[1]*256;
// 	rgb[2] = rgb[2]*256;
//       }
      float[] hsb = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], null);
      h = Color.getHSBColor(hsb[0]+0.5F, hsb[1], Math.max(1.0F, hsb[2]*2));
      hilightColors.put(rgbColor, h);
    }
    return h;
  }
}
