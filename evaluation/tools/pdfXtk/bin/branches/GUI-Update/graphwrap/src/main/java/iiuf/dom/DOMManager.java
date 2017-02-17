package iiuf.dom;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.lang.reflect.Array;

import iiuf.dom.DOMUtils;
import iiuf.util.Util;
import iiuf.util.Strings;
import iiuf.util.NotImplementedException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
   Manager for DOMable stuff.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class DOMManager {
  public static String     ARRAY_PREFIX = "_";
  public static String     NULL         = "null";
  public static String     SUPER        = "super";
  public static String     CLASS        = "class";
  public static String     LENGTH       = "length";
  public static String     KEYS         = "keys";
  public static String     VALUES       = "values";
  public static String     VERSION      = "vers.";
  public static Class      CLS_OBJECT   = Object.class;
  public static Class      CLS_STRING   = String.class;
  public static Class      CLS_MAP      = Map.class;
  
  private static Element[] ELEMENT_TMPL = new Element[0];
  
  private static HashMap handlers = new HashMap();
  
  public static Object fromDOM(DOMContext context, Element element, Object object) {
    if(element == null)
      throw new IllegalArgumentException("Element must not be null.");
    for(;;) {
      String clsName = element.getAttribute(CLASS);
      if(clsName == null || clsName.equals(""))
	break;
      Class cls = null;
      try {cls = Class.forName(clsName);} catch(ClassNotFoundException e) {
	System.out.println("Element:" + DOMUtils.toString(element));
	System.out.println("Offending class: <" + clsName + ">");
	Util.printStackTrace(e);
	return object;
      }	
      if(CLS_STRING == cls)
	return DOMUtils.getTextValue(element);	
      else if(CLS_MAP.isAssignableFrom(cls)) {
	try {
	  Map      result = (Map)cls.newInstance();
	  Object[] keys   = (Object[])get(context, element, KEYS);
	  Object[] values = (Object[])get(context, element, VALUES);
	  for(int i = 0; i < keys.length; i++)
	    result.put(keys[i], values[i]);
	  return result;
	} 
	catch(InstantiationException e) {Util.printStackTrace(e);}	  
	catch(IllegalAccessException e) {Util.printStackTrace(e);}	  
      } else if(cls.isArray()) { 
	int    len    = getInt(element, LENGTH);
	Class  cmpcls = cls.getComponentType();
	Object result = Array.newInstance(cmpcls, len);
	if(cmpcls == Boolean.class)
	  for(int i = 0; i < len; i++)
	    Array.setBoolean(result, i, ((Boolean)get(context, element, ARRAY_PREFIX + i)).booleanValue());
	else if(cmpcls == Byte.class)
	  for(int i = 0; i < len; i++)
	    Array.setByte(result, i, ((Byte)get(context, element, ARRAY_PREFIX + i)).byteValue());
	else if(cmpcls == Character.class)
	  for(int i = 0; i < len; i++)
	    Array.setChar(result, i, ((Character)get(context, element, ARRAY_PREFIX + i)).charValue());
	else if(cmpcls == Short.class)
	  for(int i = 0; i < len; i++)
	    Array.setShort(result, i, ((Short)get(context, element, ARRAY_PREFIX + i)).shortValue());
	else if(cmpcls == Integer.class)
	  for(int i = 0; i < len; i++)
	    Array.setInt(result, i, ((Integer)get(context, element, ARRAY_PREFIX + i)).intValue());
	else if(cmpcls == Long.class)
	  for(int i = 0; i < len; i++)
	    Array.setLong(result, i, ((Long)get(context, element, ARRAY_PREFIX + i)).longValue());
	else if(cmpcls == Float.class)
	  for(int i = 0; i < len; i++)
	    Array.setFloat(result, i, ((Float)get(context, element, ARRAY_PREFIX + i)).floatValue());
	else if(cmpcls == Double.class)
	  for(int i = 0; i < len; i++)
	    Array.setDouble(result, i, ((Double)get(context, element, ARRAY_PREFIX + i)).doubleValue());
	else
	  for(int i = 0; i < len; i++)
	    Array.set(result, i, get(context, element, ARRAY_PREFIX + i));
	return result;
      }
      else {
	DOMHandler handler = findHandler(cls).handler;
	if(handler == null)
	  throw new IllegalArgumentException("No handler for:" + cls.getName() + ":" + object + ":" + element);
	object  = handler.fromDOM(context, element, object);
	put(element, VERSION, handler.getVersion());
	if(context.fBreak) {
	  context.fBreak = false;
	  break;
	}
      }
      element = DOMUtils.getFirstElement(element, SUPER);
      if(element == null)
	break;
    }
    return object;
  }
  
  public static int getVersion(Element element) {
    try {return getInt(element, VERSION);}
    catch(Exception e) {return -1;}
  }
  
  public static int getInt(Element e, String name) {
    try {
      return Integer.parseInt(e.getAttribute(name).substring(1));
    } catch(NumberFormatException ex) {      
      throw new IllegalArgumentException("Can't decode int:" + name + ":" + e.getAttribute(name));	  	
    }    
  }
  
  public static boolean getBoolean(Element e, String name) {
    return (true + "").equals(e.getAttribute(name));
  }

  public static double getDouble(Element e, String name) {
    try {
      return Double.parseDouble(e.getAttribute(name).substring(1));
    } catch(NumberFormatException ex) {      
      throw new IllegalArgumentException("Can't decode double:" + name + ":" + e.getAttribute(name));	  	
    }    
  }
  
  public static Object get(DOMContext context, Element e, String name) {
    return get(context, e, name, null);
  }
  
  public static Object get(DOMContext context, Element e, String name, Object object) {
    String num = e.getAttribute(name);
    if(num != null && num.length() > 1) {
      try {
	switch(num.charAt(0)) {
	case 'B': return new Byte(num.substring(1));
	case 'C': return new Character(num.charAt(1));
	case 'D': return new Double(num.substring(1));
	case 'F': return new Float(num.substring(1));
	case 'I': return new Integer(num.substring(1));
	case 'J': return new Long(num.substring(1));
	case 'S': return new Short(num.substring(1));
	case 'Z': return new Boolean(num.substring(1));
	default: throw new IllegalArgumentException("Can't decode number:" + name + ":" + object);	  
	}
      } catch(NumberFormatException ex) {
	throw new IllegalArgumentException("Can't decode number:" + name + ":" + object);	  	
      }
    } else {
      Element element = DOMUtils.getFirstElement(e, name);
      if(element != null)
	object = fromDOM(context, element, object);
      return object;
    }
  }
  
  public static Element[] find(Element e, Class cls) {
    return (Element[])DOMUtils.getElementsWithAttribute(e, CLASS, cls.getName()).toArray(ELEMENT_TMPL);
  }
  
  public static Element findSuper(Element e, Class cls) {
    Element result = DOMUtils.getFirstElement(e, SUPER);
    if(result == null)
      return null;
    else if(cls.getName().equals(result.getAttribute(CLASS)))
      return result;
    else
      return findSuper(result, cls);
  }
  
  public static Node toDOM(DOMContext context, String name, Object object) {
    Document document = context.getDocument();
    if(object == null)
      return document.createElement(name);
    else {
      HandlerWrapper[] handlers = getHandlers(object.getClass());
      if(handlers.length == 0)
	throw new IllegalArgumentException("Not DOMable (" + object.getClass().getName() + "):" + object);
      try {
	Element result  = handlers[0].handler.toDOM(context,
						    document.createElement(name), 
						    object);
	result.setAttribute(CLASS, handlers[0].cls.getName());
	put(result, VERSION, handlers[0].handler.getVersion());
	Element current = result;
	for(int i = 1; i < handlers.length; i++) {
	  Element tmp = handlers[i].handler.toDOM(context,
						  document.createElement(SUPER),
						  object);
	  tmp.setAttribute(CLASS, handlers[i].cls.getName());
	  put(tmp, VERSION, handlers[i].handler.getVersion());
	  current.appendChild(tmp);
	  current = tmp;
	}
	return result;
      } catch(Exception e) {
	System.out.println(name + ":" + object.getClass().getName() + ":" + object);
	Util.printStackTrace(e);
	return null;
      }
    }
  }
  
  public static void put(Element element, String name, int value) {
    element.setAttribute(name, cPrefix(Integer.class) + value);
  }

  public static void put(Element element, String name, double value) {
    element.setAttribute(name, cPrefix(Double.class) + value);
  }

  public static void put(Element element, String name, boolean value) {
    element.setAttribute(name, value + "");
  }
  
  private static String cPrefix(Class cls) {
    if(cls == Byte.class)           return "B";
    else if(cls == Character.class) return "C";
    else if(cls == Double.class)    return "D";
    else if(cls == Float.class)     return "F";
    else if(cls == Integer.class)   return "I";
    else if(cls == Long.class)      return "J";
    else if(cls == Short.class)     return "S";
    else if(cls == Boolean.class)   return "Z";
    else return "";
  }
  
  public static void put(DOMContext context, Element element, String name, Object value) {
    Document document = context.getDocument();
    if(value instanceof Number)
      element.setAttribute(name, cPrefix(value.getClass()) + value.toString());
    else if(value instanceof String) {
      Element val = document.createElement(name);
      val.appendChild(document.createTextNode(value.toString()));
      val.setAttribute(CLASS, value.getClass().getName());
      element.appendChild(val);
    } else if(value instanceof Map) {
      Map map = (Map)value;
      Object[] keys   = new Object[map.size()];
      Object[] values = new Object[keys.length];
      Element  val    = document.createElement(name);
      int j = 0;
      for(Iterator i = map.keySet().iterator(); i.hasNext();) {
	keys[j]   = i.next();	    
	values[j] = map.get(keys[j]);
	j++;
      }
      put(context, val, KEYS,   keys);
      put(context, val, VALUES, values);
      val.setAttribute(CLASS, value.getClass().getName());
      element.appendChild(val);      
    } else if(value != null && value.getClass().isArray()) {
      Element val = document.createElement(name);
      val.setAttribute(CLASS,  value.getClass().getName());
      int len = Array.getLength(value);
      put(val, LENGTH, len);
      for(int i = 0; i < len; i++)
	put(context, val, ARRAY_PREFIX + i, Array.get(value, i));
      element.appendChild(val);      
    } else {
      Node val = DOMManager.toDOM(context, name, value);
      element.appendChild(val);
    }
  }
  
  public static int countHandlers(Class cls) {
    int result = 0;
    while(cls != CLS_OBJECT) {
      if(getHandler(cls) != null)
	result++;
      cls = cls.getSuperclass();
    }
    return result;
  }
  
  private static HandlerWrapper[] getHandlers(Class cls) {
    HandlerWrapper[] result = new HandlerWrapper[countHandlers(cls)];
    int i = 0;
    while(cls != CLS_OBJECT) {
      if(getHandler(cls) != null) {
	result[i] = new HandlerWrapper(getHandler(cls), cls);
	i++;
      }
      cls = cls.getSuperclass();
    }
    return result;
  }
  
  private static HandlerWrapper findHandler(Class cls) {
    while(cls != CLS_OBJECT) {
      if(getHandler(cls) != null)
	return new HandlerWrapper(getHandler(cls), cls);
      cls = cls.getSuperclass();
    }
    return null;
  }
  
  public static DOMHandler getHandler(Class cls) {
    return (DOMHandler)handlers.get(cls);
  }
  
  public static void register(Class cls, DOMHandler handler) {
    if(cls.isInterface())
      throw new IllegalArgumentException(cls.getName() + " is an interface, only classes can be registred.");
    handlers.put(cls, handler);
  }
  
  static class HandlerWrapper {
    DOMHandler handler; 
    Class      cls;

    HandlerWrapper(DOMHandler handler_, Class cls_) {
      handler = handler_;
      cls     = cls_;
    }
    
    public String toString() {
      return cls.getName();
    }
  }
}
/*
  $Log: DOMManager.java,v $
  Revision 1.1  2002/07/11 12:03:48  ohitz
  Initial checkin

  Revision 1.8  2001/04/11 19:02:07  schubige
  fixed connection bug and made JSliderSoundlet domable

  Revision 1.7  2001/03/30 17:33:28  schubige
  modified beat soundlet

  Revision 1.6  2001/03/28 21:31:18  schubige
  dom save and load works now (very early version)

  Revision 1.5  2001/03/28 18:44:33  schubige
  working on dom again

  Revision 1.4  2001/03/26 15:35:37  schubige
  fixed format bug

  Revision 1.3  2001/03/22 16:08:24  schubige
  more work on dom stuff

  Revision 1.2  2001/03/21 22:18:14  schubige
  working on dom stuff

  Revision 1.1  2001/03/21 19:37:45  schubige
  started with dom stuff
  
*/
