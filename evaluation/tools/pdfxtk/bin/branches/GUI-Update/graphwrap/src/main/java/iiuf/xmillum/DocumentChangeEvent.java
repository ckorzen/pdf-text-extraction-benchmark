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

import java.util.EventObject;

/**
 * DocumentChangeEvent
 *
 * This event gets sent when the input document(s) change.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class DocumentChangeEvent extends EventObject {
  public final static int DOCUMENT_CHANGED = 0;
  public final static int SCALE_CHANGED    = 1;
  public final static int LAYER_TOGGLED    = 2;
  public final static int REFRESH          = 3;

  int type;
  String layer;
  boolean active;

  public DocumentChangeEvent(Object source, int t) {
    super(source);
    type = t;
  }

  public DocumentChangeEvent(Object source, int t, String l) {
    this(source, t);
    layer = l;
  }

  public DocumentChangeEvent(Object source, int t, String l, boolean a) {
    this(source, t, l);
    active = a;
  }

  public int getType() {
    return type;
  }

  public String getLayer() {
    return layer;
  }

  public boolean isActive() {
    return active;
  }
}
