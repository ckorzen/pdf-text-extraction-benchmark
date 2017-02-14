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

import iiuf.util.EventListenerList;

import java.awt.Graphics;
import java.util.EventListener;

/**
 * Image
 *
 * Abstract class representing images to be used in xmillum.
 *
 * @author $Author: hassan $
 * @version $Revision: 1.1 $
 */
public abstract class Image {

  /** List of ImageListeners listening on this image. */
  EventListenerList listeners = new EventListenerList();

  /**
   * Add an ImageListener to this image.
   *
   * @param l ImageListener to add.
   */
  public void addImageListener(ImageListener l) {
    listeners.add(ImageListener.class, l);
  }

  /**
   * Remove an ImageListener from this image.
   *
   * @param l ImageListener to remove.
   */
  public void removeImageListener(ImageListener l) {
    listeners.remove(ImageListener.class, l);
  }

  /**
   * Calls the ImageListeners that the image has changed.
   */
  protected void fireImageChanged() {
    EventListener[] l = listeners.getListeners(ImageListener.class);
    for (int i = 0; i < l.length; i++) {
      ((ImageListener) l[i]).imageChanged(this);
    }
  }

  /**
   * Returns the width of the image.
   *
   * @return Width in pixels
   */
  public abstract int getWidth();

  /**
   * Returns the height of the image.
   *
   * @return Height in pixels
   */
  public abstract int getHeight();

  public abstract void paintImage(double scale, Graphics g, int x, int y);
}
