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

import org.w3c.dom.Element;

/**
 * ActionHandlerParam
 *
 * Parameter object for action handlers.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class ActionHandlerParam {

  private BrowserContext context;
  private Element        element;
  private Displayable    displayable;
  private String         option;

  /**
   * Creates a new ActionHandlerParam object.
   *
   * @param c Current browser context.
   * @param d Displayable in question.
   * @param o Option string.
   */
  public ActionHandlerParam(BrowserContext c, Displayable d, String o) { 
    context = c;
    displayable = d;
    option = o;
    element = d.element;
  }

  /**
   * Creates a new ActionHandlerParam Object.
   *
   * @param c Current browser context.
   * @param e Element in question.
   * @param o Option string.
   */
  public ActionHandlerParam(BrowserContext c, Element e, String o) {
    context = c;
    element = e;
    option = o;
  }

  /**
   * Returns the browser context.
   *
   * @return Current browser context.
   */
  public BrowserContext getContext() {
    return context;
  }

  /**
   * Returns the element in question.
   *
   * @return The element upon which an action is invoked.
   */
  public Element getElement() {
    return element;
  }

  /**
   * Returns the displayable in question.
   *
   * @return The displayable-element upon which an action is invoked.
   */
  public Displayable getDisplayable() {
    return displayable;
  }

  /**
   * Returns the option value.
   *
   * @return Option.
   */
  public String getOption() {
    return option;
  }
}
