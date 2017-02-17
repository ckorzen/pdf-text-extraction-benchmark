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

import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * ActionHandler
 *
 * This class defines a handler that operates on one specific element.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public abstract class ActionHandler {
  /**
   * Initializes the handler. The handler is free to extract further
   * parameters from the supplied element.
   *
   * @param e Element that describes the handler. It can contain any
   *          kind of further parameters.
   */
  public abstract void init(BrowserContext context, Element e);

  /**
   * Performs an action with an element.
   *
   * @param context Current context of the application
   * @param element Element on which to act
   */
  public abstract void handle(ActionHandlerParam param);

  /**
   * Gets called when this handler is specified as parameter to a
   * flag.
   *
   * @param context Current context of the application
   * @param flagValue Flag value
   * @param elements Element on which to act
   */
  public void handleFlagged(BrowserContext context, String flagValue, Set elements) {
    Iterator i = elements.iterator();
    while (i.hasNext()) {
      ActionHandlerParam p = new ActionHandlerParam(context, (Element) i.next(), flagValue);
      handle(p);
    }
  }
}
