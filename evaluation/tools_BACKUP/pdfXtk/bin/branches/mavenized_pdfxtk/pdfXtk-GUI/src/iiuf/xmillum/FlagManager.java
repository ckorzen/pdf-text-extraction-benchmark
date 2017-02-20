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
import iiuf.util.EventListenerList;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * FlagManager
 *
 * This class manages all the data related to flag sets.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class FlagManager {

  public static final String SELECTION = "selection";
  public static final String SELECTED  = "selected";

  /** Contains AllFlagListeners, which get every flag change */

  EventListenerList allListeners = new EventListenerList();

  /**
   * For every flagged element, contains a Set with the names of the
   * flags the element is flagged with.
   */

  Map flaggedElements = new HashMap();

  void removeFlagged(Element element, String name) {
    Set flags = (Set) flaggedElements.get(element);
    if (flags != null) {
      flags.remove(name);
      if (flags.isEmpty()) flaggedElements.remove(element);
    }
  }

  void setFlagged(Element element, String name) {
    Set flags = (Set) flaggedElements.get(element);
    if (flags == null) {
      flags = new HashSet();
      flaggedElements.put(element, flags);
    }
    flags.add(name);
  }

  /**
   * Represents a flag type.  */

  class FlagType {
    // Flag name
    String name;

    // Possible flag values
    Map values  = new HashMap();

    // Objects flagged (key: object, value: flag value)
    Map objects = new HashMap();

    // Listeners listening for this flag
    EventListenerList listeners = new EventListenerList();

    // Handler
    String handler;

    public FlagType(String n, Map v, String h) {
      name      = n;
      values    = v;
      handler   = h;
    }

    public FlagType(String n, FlagType t, Map v, String h) {
      this(n, v, h);
      objects   = t.objects;
      listeners = t.listeners;
    }

    public Style getStyle(Element e) {
      String f = getFlag(e);
      if (f == null) {
	return null;
      } else {
	return (Style) values.get(f);
      }
    }

    public synchronized Set getElements(String value) {
      Set result = new HashSet();
      Iterator i = objects.keySet().iterator();
      while (i.hasNext()) {
	Element e = (Element) i.next();
	if (value.equals(objects.get(e))) {
	  result.add(e);
	}
      }
      return result;
    }

    public synchronized void setFlag(Element e, String value, FlagListener source) {
      if (value == null) {
	removeFlagged(e, name);
      } else {
	setFlagged(e, name);
      }

      objects.remove(e);
      objects.put(e, value);

      EventListener[] ll = listeners.getListeners(FlagListener.class);
      for (int i = 0; i < ll.length; i++) {
	FlagListener l = (FlagListener) ll[i];
	if (l != source) l.setFlag(e, value);
      }
      ll = allListeners.getListeners(AllFlagListener.class);
      for (int i = 0; i < ll.length; i++) {
	AllFlagListener l = (AllFlagListener) ll[i];
	if (l != source) l.setFlag(e, name, value);
      }
    }

    /**
     * Clears all flags of this type and inform the listeners
     *
     * @param source The listener that is invoking this method (to
     * avoid informing himself and create an endless loop)
     */

    public synchronized void clearFlags(FlagListener source) {
      EventListener[] ll = listeners.getListeners(FlagListener.class);
      for (int i = 0; i < ll.length; i++) {
	FlagListener l = (FlagListener) ll[i];
	if (l != source) {
	  Iterator o = objects.keySet().iterator();
	  while (o.hasNext()) {
	    l.setFlag((Element) o.next(), null);
	  }
	}
      }
      ll = allListeners.getListeners(AllFlagListener.class);
      for (int i = 0; i < ll.length; i++) {
	AllFlagListener l = (AllFlagListener) ll[i];
	if (l != source) {
	  Iterator o = objects.keySet().iterator();
	  while (o.hasNext()) {
	    l.setFlag((Element) o.next(), name, null);
	  }
	}
      }

      Iterator o = objects.keySet().iterator();
      while (o.hasNext()) {
	removeFlagged((Element) o.next(), name);
      }

      objects.clear();
    }

    /**
     * Get the flag name of an object.
     *
     * @param e Object to question
     * @return Flag name or null if this object is not flagged
     */
    public synchronized String getFlag(Element e) {
      return (String) objects.get(e);
    }

    /**
     * Adds a listener for this flag type.
     *
     * @param l FlagListener to add
     */
    public synchronized void addListener(FlagListener l) {
      listeners.add(FlagListener.class, l, true);
    }

    /**
     * Removes a listener from this flag type.
     *
     * @param l FlagListener to remove
     */
    public synchronized void removeListener(FlagListener l) {
      listeners.remove(FlagListener.class, l);
    }
  }

  /**
   * Instances of this class are returned to objects which add FlagListener
   * so they can manipulate the flag state themselves.
   */
  class FlagAccessImpl extends FlagAccess {
    FlagType type;
    FlagListener listener;

    public FlagAccessImpl(FlagListener l, FlagType t) {
      listener = l;
      type     = t;
    }

    public void clearFlags() {
      type.clearFlags(listener);
    }

    public void setFlag(Element e, String value) {
      type.setFlag(e, value, listener);
    }

    public void toggleFlag(Element e, String value1, String value2) {
      String f = type.getFlag(e);
      if (value1 != null) {
	type.setFlag(e, value1.equals(f) ? value2 : value1, listener);
      } else {
	type.setFlag(e, value2.equals(f) ? value1 : value2, listener);
      }
    }

    public Set getElements(String value) {
      return type.getElements(value);
    }
  }

  // Possible flag types (key: name, value: flag type)
  Map flagTypes = new HashMap();
  BrowserContext context;

  public FlagManager(BrowserContext c) {
    context = c;

    // Add the "selection" flags
    Map values = new HashMap();
    values.put(SELECTED, new Style(true));
    addFlagType(SELECTION, values);
  }

  public void setFlags(NodeList f) {
    // Add other flags
    for (int i = 0; i < f.getLength(); i++) {
      Element e = (Element) f.item(i);
      String flagName = e.getAttribute("name");
      if (flagName == null) {
	context.log("Flag definition without a `name' attribute found.");
      } else {
	String handler = e.getAttribute("handler");

	Map values = new HashMap();
	
	NodeList vl = DOMUtils.getChildsByTagName(e, "value");
	for (int j = 0; j < vl.getLength(); j++) {
	  Element v = (Element) vl.item(j);

	  String vname = v.getAttribute("name");
	  String vstyle = v.getAttribute("style");

	  if (vname == null) {
	    context.log("Flag definition: value without a `name' attribute found.");
	  } else if (vstyle == null) {
	    context.log("Flag definition: value without a `style' attribute found.");
	  } else {
	    values.put(vname, context.styleRegistry.getStyle(vstyle));
	  }
	}
	addFlagType(flagName, values, handler);
      }
    }
  }

  public void addFlagType(String type, Map values) {
    addFlagType(type, values, null);
  }

  public void addFlagType(String type, Map values, String handler) {
    FlagType t = (FlagType) flagTypes.get(type);
    if (t != null) {
      t = new FlagType(type, t, values, handler);
    } else {
      t = new FlagType(type, values, handler);
    }
    flagTypes.put(type, t);
  }

  public Set getFlagTypes() {
    return flagTypes.keySet();
  }

  public Set getFlagValues(String type) {
    FlagType t = (FlagType) flagTypes.get(type);
    if (t != null) {
      return t.values.keySet();
    } else {
      return null;
    }
  }

  public Style[] getStyles(Element e) {
    Set types = (Set) flaggedElements.get(e);
    if (types == null) {
      return new Style[0];
    } else {
      Style[] styles  = new Style[types.size()];
      int ii = 0;
      Iterator i = types.iterator();
      while (i.hasNext()) {
	FlagType t = (FlagType) flagTypes.get(i.next());
	styles[ii++] = t.getStyle(e);
      }
      return styles;
    }
  }

  public void setFlag(String type, String value, Element e) {
    FlagType t = (FlagType) flagTypes.get(type);
    if (t == null) {
      context.log("Error in FlagManager.flagObject(): flag type `"+type+"' not found.");
      return;
    }
    t.setFlag(e, value, null);
  }

  public String getFlag(String type, Element e) {
    FlagType t = (FlagType) flagTypes.get(type);
    if (t == null) {
      context.log("Error in FlagManager.getFlag(): flag type `"+type+"' not found.");
      return null;
    }
    return t.getFlag(e);
  }

  public void runHandlers() {
    Iterator i = flagTypes.keySet().iterator();
    while (i.hasNext()) {
      FlagType t = (FlagType) flagTypes.get(i.next());
      if (t.handler != null && !"".equals(t.handler)) {
	Iterator i2 = t.values.keySet().iterator();
	while (i2.hasNext()) {
	  String value = (String) i2.next();
	  context.actionFactory.handleFlagged(t.handler, value, t.getElements(value));
	}
      }
    }
  }

  /**
   * Adds a flag listener for a specified flag type to the system.
   *
   * @param type Flag name the listener should listen to
   * @param listener Flag listener ready to listen
   * @return An object allowed to access the flagging.
   */

  public FlagAccess addFlagListener(String type, FlagListener listener) {
    FlagType t = (FlagType) flagTypes.get(type);
    if (t == null) {
      context.log("Error in FlagManager.addFlagListener(): flag type `"+type+"' not found.");
      return null;
    }
    t.addListener(listener);
    return new FlagAccessImpl(listener, t);
  }

  /**
   * Removes a flag listener for a specified flag type.
   *
   * @param type Flag name the listener should be removed from
   * @param listener Listener to remove
   */

  public void removeFlagListener(String type, FlagListener listener) {
    FlagType t = (FlagType) flagTypes.get(type);
    if (t == null) {
      context.log("Error in FlagManager.removeFlagListener(): flag type `"+type+"' not found.");
      return;
    }
    t.removeListener(listener);
  }

  /**
   * Adds a flag listener for all flag changes
   *
   * @param listener Flag listener ready to listen
   * @return An object allowed to access the flagging.
   */

  public void addAllFlagListener(AllFlagListener listener) {
    allListeners.add(AllFlagListener.class, listener, true);
  }

  /**
   * Removes a flag listener for all flag changes
   *
   * @param listener Listener to remove
   */

  public void removeAllFlagListener(String type, AllFlagListener listener) {
    allListeners.remove(AllFlagListener.class, listener);
  }
}
