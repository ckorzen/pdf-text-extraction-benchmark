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

package iiuf.xmillum.handlers;

import iiuf.xmillum.ActionHandler;
import iiuf.xmillum.ActionHandlerParam;
import iiuf.xmillum.BrowserContext;
import org.w3c.dom.Element;

/**
 * Invalidate
 *
 * ActionHandler that can invalidate objects.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class Invalidate extends ActionHandler {
  public void init(BrowserContext context, Element param) {
  }

  public void handle(ActionHandlerParam param) {
    Element e = param.getContext().elementTagger.getReferencedElement(param.getElement());
    if (e.hasAttribute("state") && e.getAttribute("state").equals("suspect")) {
      e.removeAttribute("state");
    } else {
      e.setAttribute("state", "suspect");
    }
    param.getContext().retransform();
  }
}
