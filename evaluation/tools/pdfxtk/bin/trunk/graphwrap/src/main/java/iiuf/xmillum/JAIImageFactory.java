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

import iiuf.jai.Util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;
import javax.media.jai.TileComputationListener;
import javax.media.jai.TileRequest;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/** 
 * Work around a bug in the native "subsamplebinarytogray" implementation of JAI-1.1.1
 */

import java.util.List;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;
import java.awt.image.renderable.RenderedImageFactory;

/** End of workaround */

/**
 * JAIImageFactory
 *
 * An ImageFactory working with JAI images.
 *
 * @author $Author: hassan $
 * @version $Revision: 1.1 $
 */
public class JAIImageFactory extends ImageFactory {

  static {
    /** 
     * Workaround a bug in the native "subsamplebinarytogray" implementation of JAI-1.1.1
     */

    // Get the registry of the default JAI instance.
    OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
    
    // Get all RIFs associated with "subsamplebinarytogray" (there
    // should be two as installed by default).
    List imageFactories = RIFRegistry.getOrderedList(registry,
						     "subsamplebinarytogray",
						     "com.sun.media.jai");
    if(imageFactories.size() >= 2) {
      // Save references to the native and Java RIFs.
      RenderedImageFactory nativeFactory = (RenderedImageFactory)imageFactories.get(0);
      RenderedImageFactory javaFactory =   (RenderedImageFactory)imageFactories.get(1);
      
      // Unset the preferences between the two.
      RIFRegistry.unsetPreference(registry,
				  "subsamplebinarytogray",
				  "com.sun.media.jai",
				  nativeFactory,
				  javaFactory);
      
      // Unregister the native implementation of "subsamplebinarytogray".
      RIFRegistry.unregister(registry,
			     "subsamplebinarytogray",
			     "com.sun.media.jai",
			     nativeFactory);
    }

    /** End of workaround */

    // Setup a BIG cache for the JAI images
    TileCache cache = JAI.createTileCache(64000000);
    cache.setMemoryThreshold(0.75F);
    JAI.getDefaultInstance().setTileCache(cache);
  }

  public JAIImageFactory() {
  }

  public Image getImage(URL imageURL) throws IOException {
    PlanarImage image = loadImage(imageURL);
    if (image != null) {
      return new JAIImage(image);
    } else {
      return null;
    }
  }

  PlanarImage loadImage(URL imageURL) throws IOException {
    SeekableStream stream = new MemoryCacheSeekableStream(imageURL.openStream());
    return JAI.create("stream", stream);
  }

  public class JAIImage extends Image {
    protected PlanarImage original;

    public JAIImage() {
    }

    public JAIImage(PlanarImage i) {
      original = i;
    }

    public int getWidth() {
      return original.getWidth();
    }

    public int getHeight() {
      return original.getHeight();
    }

    PlanarImage scaled;
    double scale;

    public void resetImage() {
      scaled = null;
    }

    public void paintImage(double s, Graphics g, int x, int y) {
      if (scaled == null || s != scale) {
	scale = s;
	if (scale != 1.0) {
	  ParameterBlock pb = new ParameterBlock();
	  pb.addSource(original);
	  pb.add(new Float(scale));
	  pb.add(new Float(scale));

	  if (Util.isBinary(original) && scale < 1.0 && original.getSampleModel() instanceof MultiPixelPackedSampleModel) {
	    ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 8 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	    SampleModel sm = cm.createCompatibleSampleModel(256, 256);
	    ImageLayout layout = new ImageLayout(0, 0, 256, 256, sm, cm);
	    layout.setTileWidth(256);
	    layout.setTileHeight(256);
	    RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
	    scaled = JAI.create("subsamplebinarytogray", pb,  hints);
	  } else {
	    ImageLayout layout = new ImageLayout();
	    layout.setTileWidth(256);
	    layout.setTileHeight(256);
	    RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
	    scaled = JAI.create("scale", pb,  hints);
	  }
	} else {
	  scaled = original;
	}

	setupPainting();
      }

      Graphics2D g2d = (Graphics2D) g;

      // Get the clipping rectangle and translate it into image coordinates. 
      Rectangle clipBounds = g2d.getClipBounds();
      
      int transX = x;
      int transY = y;
    
      // Determine the extent of the clipping region in tile coordinates.
      int txmin, txmax, tymin, tymax;
      int ti, tj;
    
      // Constrain drawing to the active image area
      Rectangle imageRect = new Rectangle(minX + transX, minY + transY, width, height);

      if (clipBounds != null) {
	txmin = Math.min(Math.max(XtoTileX(clipBounds.x), minTileX), maxTileX);
	txmax = Math.min(Math.max(XtoTileX(clipBounds.x + clipBounds.width - 1), minTileX), maxTileX);
	tymin = Math.min(Math.max(YtoTileY(clipBounds.y), minTileY), maxTileY);
	tymax = Math.min(Math.max(YtoTileY(clipBounds.y + clipBounds.height - 1), minTileY), maxTileY);
	g2d.clip(imageRect);
      } else {
	txmin = minTileX; txmax = maxTileX;
	tymin = minTileY; tymax = maxTileY;
	g2d.setClip(imageRect);
      }
    
      if (backgroundColor != null) {
	g2d.setColor(backgroundColor);
      }

      // Loop over tiles within the clipping region
      for (tj = tymin; tj <= tymax; tj++) {
	for (ti = txmin; ti <= txmax; ti++) {
	  int tx = TileXtoX(ti);
	  int ty = TileYtoY(tj);
	  
	  Raster tile = null;
	  
	  Hashtable xtable = (Hashtable) ytable.get(new Integer(tj));
	  if (xtable != null) {
	    tile = (Raster) xtable.get(new Integer(ti));
	  }

	  if (backgroundColor != null) {
	    g2d.fillRect(tx+transX, ty+transY, tileWidth, tileHeight);
	  }

	  if (tile != null) {
	    WritableRaster wr = tile.createWritableRaster(sampleModel, tile.getDataBuffer(), new Point(0, 0));
	    BufferedImage bi = new BufferedImage(colorModel, wr, false, null);
	    AffineTransform transform = AffineTransform.getTranslateInstance(tx + transX, ty + transY);
	    g2d.drawImage(bi, transform, null);
	  }
	}
      }
    }

    Hashtable ytable;  
    SampleModel sampleModel;
    ColorModel colorModel;
    int minX, minY;
    int width, height;
    int minTileX;
    int maxTileX;
    int minTileY;
    int maxTileY;
    int tileWidth;
    int tileHeight;
    int tileGridXOffset;
    int tileGridYOffset;
    Color backgroundColor = null;

    void setupPainting() {
      ytable = new Hashtable();

      scaled.addTileComputationListener(new TileComputationListener() {
	  public void tileComputationFailure(Object eventSource, TileRequest[] requests, PlanarImage image, int tileX, int tileY, Throwable situation) {
	    situation.printStackTrace();
	  }
	  public void tileCancelled(Object eventSource, TileRequest[] requests, PlanarImage image, int tileX, int tileY) {
	  }
	  public void tileComputed(Object eventSource, TileRequest[] requests, PlanarImage image, int tileX, int tileY, Raster tile) {
	    storeTile(tileX, tileY, tile);
	    fireImageChanged();
	  }
	});

      minTileX = scaled.getMinTileX();
      maxTileX = scaled.getMinTileX() + scaled.getNumXTiles() - 1;
      minTileY = scaled.getMinTileY();
      maxTileY = scaled.getMinTileY() + scaled.getNumYTiles() - 1;
    
      sampleModel = scaled.getSampleModel();
      colorModel = scaled.getColorModel();
      if (colorModel == null) {
	colorModel = PlanarImage.createColorModel(scaled.getSampleModel());
      }

      if (colorModel == null) {
	throw new IllegalArgumentException("JAIImage is unable to display supplied PlanarImage.");
      }
    
      if (colorModel.getTransparency() != Transparency.OPAQUE) {
	Object col = scaled.getProperty("background_color");
	if (col != null) {
	  backgroundColor = (Color)col;
	} else {
	  backgroundColor = Color.white;
	}
      }
    
      minX = scaled.getMinX();
      minY = scaled.getMinY();
      width = scaled.getWidth();
      height = scaled.getHeight();

      tileWidth = scaled.getTileWidth();
      tileHeight = scaled.getTileHeight();
      tileGridXOffset = scaled.getTileGridXOffset();
      tileGridYOffset = scaled.getTileGridYOffset();

      // Queueing tiles for computation
      Point[] tiles = new Point[scaled.getNumXTiles() * scaled.getNumYTiles()];
      int i=0;
      for (int y = minTileY; y <= maxTileY; y++) {
	for (int x = minTileX; x <= maxTileX; x++) {
	  tiles[i++] = new Point(x, y);
	}
      }
      scaled.queueTiles(tiles);
    }

    int XtoTileX(int x) {
      return (int) Math.floor((double) (x - tileGridXOffset)/tileWidth);
    }

    int YtoTileY(int y) {
      return (int) Math.floor((double) (y - tileGridYOffset)/tileHeight);
    }

    int TileXtoX(int tx) {
      return tx*tileWidth + tileGridXOffset;
    }

    int TileYtoY(int ty) {
      return ty*tileHeight + tileGridYOffset;
    }

    void storeTile(int tileX, int tileY, Raster tile) {
      Hashtable xtable = (Hashtable) ytable.get(new Integer(tileY));
      if (xtable == null) {
	xtable = new Hashtable();
	ytable.put(new Integer(tileY), xtable);
      }
      xtable.put(new Integer(tileX), tile);
    }
  }
}
