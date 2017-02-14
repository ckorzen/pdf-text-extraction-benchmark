/* (C) 2002, DIUF, http://www.unifr.ch/diuf
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * JavaImageFactory
 *
 * Basic ImageFactory working with images supported by JDK. Does not require
 * any external libraries.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class JavaImageFactory extends ImageFactory {

  public JavaImageFactory() {
  }

  public iiuf.xmillum.Image getImage(URL imageURL) throws IOException {
    ImageIcon icon = new ImageIcon(imageURL);
    if (icon.getImageLoadStatus() == MediaTracker.ERRORED) {
      context.setStatus("Unable to load image "+imageURL);
      return null;
    }
    return new JavaImage(icon.getImage());
  }

  private class JavaImage extends iiuf.xmillum.Image {
    Image original;

    public JavaImage(Image i) {
      original = i;
    }

    public int getWidth() {
      return original.getWidth(null);
    }

    public int getHeight() {
      return original.getHeight(null);
    }

    BufferedImage scaled;
    double scale = 1.0d;

    public void paintImage(double s, Graphics g, int x, int y) {
      if (scaled == null || s != scale) {
	scale = s;

	int w = (int) (original.getWidth(null) * scale);
	int h = (int) (original.getHeight(null) * scale);

	BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	AffineTransform tx = new AffineTransform();
	if (s != 1.0d) {
	  tx.scale(scale, scale);
	}
	Graphics2D ig = scaled.createGraphics();
	ig.drawImage(original, tx, null);
	ig.dispose();
      }

      Graphics2D g2d = (Graphics2D) g;
      g2d.drawImage(scaled, null, x, y);
    }
  }
}
