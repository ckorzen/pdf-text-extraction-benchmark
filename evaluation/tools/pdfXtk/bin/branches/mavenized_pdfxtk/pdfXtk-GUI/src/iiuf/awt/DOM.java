package iiuf.awt;

import java.util.Map;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.font.TransformAttribute;

import iiuf.util.Attributable;
import iiuf.util.NotImplementedException;
import iiuf.dom.DOMContext;
import iiuf.dom.DOMManager;
import iiuf.dom.DOMHandler;
import iiuf.swing.graph.GraphNodeComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.font.TextAttribute;

/**
   DOM handlers for Awt objects.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class DOM {
  public static final String ATTR_X          = "x";
  public static final String ATTR_Y          = "y";
  public static final String ATTR_WIDTH      = "width";
  public static final String ATTR_HEIGHT     = "height";
  public static final String ATTR_FOREGROUND = "foreground";
  public static final String ATTR_BACKGROUND = "background";
  public static final String ATTR_COLOR      = "color";
  public static final String ATTR_FONT       = "font";
  public static final String ATTR_ROTATION   = "rotation";
  public static final String ATTR_TRANSFORM  = "transform";
  public static final String ATTR_FONT_ATTR  = "font_attributes";
  public static final String ATTR_NAME       = "name";

  private static boolean inited;
  
  public static synchronized void init() {
    if(inited) return;
    inited = true;
    
    DOMManager.register(Component.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  	 
	  Component cmp = (Component)object;
	  
	  cmp.setBounds(DOMManager.getInt(element, ATTR_X),
			DOMManager.getInt(element, ATTR_Y),
			DOMManager.getInt(element, ATTR_WIDTH),
			DOMManager.getInt(element, ATTR_HEIGHT));
	  
	  cmp.setForeground((Color)DOMManager.get(context, element, ATTR_FOREGROUND));
	  cmp.setBackground((Color)DOMManager.get(context, element, ATTR_BACKGROUND));
	  cmp.setFont((Font)DOMManager.get(context, element, ATTR_FONT));
	  if(cmp instanceof GraphNodeComponent)
	    ((GraphNodeComponent)cmp).setRotation(DOMManager.getInt(element, ATTR_ROTATION));	    
	  return object;
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {	  
	  Component cmp = (Component)object;
	  
	  DOMManager.put(element,          ATTR_X         , cmp.getX());
	  DOMManager.put(element,          ATTR_Y         , cmp.getY());
	  DOMManager.put(element,          ATTR_WIDTH     , cmp.getWidth());
	  DOMManager.put(element,          ATTR_HEIGHT    , cmp.getHeight());
	  DOMManager.put(context, element, ATTR_FOREGROUND, cmp.getForeground());
	  DOMManager.put(context, element, ATTR_BACKGROUND, cmp.getBackground());
	  DOMManager.put(context, element, ATTR_FONT      , cmp.getFont());
	  if(cmp instanceof GraphNodeComponent)
	    DOMManager.put(element, ATTR_ROTATION, ((GraphNodeComponent)cmp).getRotation());	    
	  
	  return element;
	}

	public int getVersion() {return 0;}
      });
    
    DOMManager.register(Color.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  	 
	  return new Color(DOMManager.getInt(element, ATTR_COLOR));
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  DOMManager.put(element, ATTR_COLOR, ((Color)object).getRGB());
	  return element;
	}

	public int getVersion() {return 0;}
      });
    
    DOMManager.register(Font.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  	 
	  return new Font((Map)DOMManager.get(context, element, ATTR_FONT_ATTR));	  
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {	  
	  DOMManager.put(context, element, ATTR_FONT_ATTR, ((Font)object).getAttributes());	  	  
	  return element;
	}

	public int getVersion() {return 0;}
      });
    
    DOMManager.register(TextAttribute.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  	 
	  String name = (String)DOMManager.get(context, element, ATTR_NAME);
	  for(int i = 0; i < TEXT_ATTRS.length; i++)
	    if(TEXT_ATTRS[i].toString().equals(name))
	      return TEXT_ATTRS[i];
	  return null;
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {	  
	  DOMManager.put(context, element, ATTR_NAME, object.toString());	  
	  return element;
	}

	public int getVersion() {return 0;}
      });
    
    DOMManager.register(TransformAttribute.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {
	  Object o = DOMManager.get(context, element, ATTR_TRANSFORM);
	  return new TransformAttribute((AffineTransform)DOMManager.get(context, element, ATTR_TRANSFORM));
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  DOMManager.put(context, element, ATTR_TRANSFORM, ((TransformAttribute)object).getTransform());
	  return element;
	}

	public int getVersion() {return 0;}
      });    
    
    DOMManager.register(AffineTransform.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {
	  return new AffineTransform((double[])DOMManager.get(context, element, ATTR_TRANSFORM));
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  double[] m = new double[6];
	  ((AffineTransform)object).getMatrix(m);
	  DOMManager.put(context, element, ATTR_TRANSFORM, m);
	  return element;
	}

	public int getVersion() {return 0;}
      });    
  }
  
  private final static TextAttribute[] TEXT_ATTRS = {    
    TextAttribute.BACKGROUND,
    TextAttribute.BIDI_EMBEDDING, 
    TextAttribute.CHAR_REPLACEMENT,
    TextAttribute.FAMILY,
    TextAttribute.FONT,
    TextAttribute.FOREGROUND, 
    TextAttribute.INPUT_METHOD_HIGHLIGHT, 
    TextAttribute.INPUT_METHOD_UNDERLINE,
    TextAttribute.JUSTIFICATION,
    TextAttribute.POSTURE,
    TextAttribute.RUN_DIRECTION,
    TextAttribute.SIZE,
    TextAttribute.STRIKETHROUGH,
    TextAttribute.SUPERSCRIPT,
    TextAttribute.SWAP_COLORS,
    TextAttribute.TRANSFORM,
    TextAttribute.UNDERLINE,
    TextAttribute.WEIGHT,
    TextAttribute.WIDTH
  };
}

/*
  $Log: DOM.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.6  2001/03/28 21:31:18  schubige
  dom save and load works now (very early version)

  Revision 1.5  2001/03/28 18:44:30  schubige
  working on dom again

  Revision 1.4  2001/03/26 15:35:32  schubige
  fixed format bug

  Revision 1.3  2001/03/22 16:08:19  schubige
  more work on dom stuff

  Revision 1.2  2001/03/21 22:18:14  schubige
  working on dom stuff

  Revision 1.1  2001/03/21 19:37:42  schubige
  started with dom stuff
  
*/
