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

package iiuf.xmillum;

import iiuf.dom.DOMUtils;

import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * ActionHandlerFactory
 *
 * This class produces ActionHandlers.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class ActionHandlerFactory {

  HashMap handlers;
  BrowserContext context;

  /**
   * Creates a new ActionHandlerFactory object.
   *
   * @param c The current browser context.
   * @param handlerList The list of handlers to create (&lt;xmi:handler&gt;
   * elements)
   */
  public ActionHandlerFactory(BrowserContext c, NodeList handlerList) {
    context = c;
    handlers = new HashMap();

    for (int i = 0; i < handlerList.getLength(); i++) {
      Element h = (Element) handlerList.item(i);
      String name = h.getAttribute("name");
      String className = h.getAttribute("class");

      try {
	Class clazz = Class.forName(className, true, context.getDocument().getClassLoader());
	Object handler = clazz.newInstance();

	if (handler instanceof ActionHandler) {
	  ((ActionHandler) handler).init(context, h);
	  handlers.put(name, handler);
	} else {
	  context.log("Handler "+className+" is not of class ActionHandler.");
	}
      } catch (ClassNotFoundException e) {
	context.log("Unable to load handler `"+className+"'");
      } catch (InstantiationException e) {
	context.log("Unable to load handler `"+className+"'", e);
      } catch (IllegalAccessException e) {
	context.log("Unable to load handler `"+className+"'", e);
      }
    }
  }

  /**
   * Invoke a specific action.
   *
   * @param handlerName Name of the handler to invoke.
   * @param option Parameter to pass to the handler.
   * @param element Element upon which the handler is invoked.
   * @param context Current browser context.
   */
  public void handleAction(String handlerName,
			   String option,
			   Displayable element,
			   BrowserContext context)
  {
    ActionHandler handler = (ActionHandler) handlers.get(handlerName);
    if (handler == null) {
      context.log("Handler '"+handlerName+"' unknown.");
    } else {
      ActionHandlerParam param = new ActionHandlerParam(context, element, option);
      handler.handle(param);
    }
  }

  /**
   * Invoke an action upon a Set of elements. The elements is a set of
   * elements flagged with a specific value.
   *
   * @param handlerName Name of the handler to invoke.
   * @param flagValue Value of the flag which created this Set.
   * @param elements Set of elements upon which the handler is invoked.
   */
  public void handleFlagged(String handlerName, String flagValue, Set elements) {
    ActionHandler handler = (ActionHandler) handlers.get(handlerName);
    if (handler == null) {
      context.log("Handler '"+handlerName+"' unknown.");
    } else {
      handler.handleFlagged(context, flagValue, elements);
    }
  }
}
