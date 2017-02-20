
package iiuf.jai;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.EventListener;

import javax.swing.Icon;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileComputationListener;
import javax.media.jai.TileRequest;

import iiuf.util.EventListenerList;
import iiuf.util.Util;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Display a JAI image.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class DisplayImage implements Icon {
  protected EventListenerList listeners = new EventListenerList();

  protected Hashtable ytable = new Hashtable();

  protected PlanarImage image;
  protected SampleModel sampleModel;
  protected ColorModel colorModel;
  
  protected int minX, minY;
  protected int width, height;
  
  protected int minTileX;
  protected int maxTileX;
  protected int minTileY;
  protected int maxTileY;
  protected int tileWidth;
  protected int tileHeight;
  protected int tileGridXOffset;
  protected int tileGridYOffset;

  private Color backgroundColor = null;

  protected boolean cacheTiles;
  protected boolean asyncPaint;

  protected Component paintComponent;

  /** Constructs a DisplayImage object.

      @param image The image to visualize.
      @param cacheTiles Caches tiles inside this object (may be very memory consuming).
      @param asyncPaint Only paints the parts of the image that are already computed
      (only in conjunction with cacheTiles). */

  public DisplayImage(PlanarImage image, boolean cacheTiles, boolean asyncPaint) {
    this.image      = image;
    this.cacheTiles = cacheTiles;
    this.asyncPaint = asyncPaint;

    if (cacheTiles) {
      image.addTileComputationListener(new TileComputationListener() {
	  public void tileComputationFailure(Object eventSource, TileRequest[] requests, PlanarImage image, int tileX, int tileY, Throwable situation) {
	    Util.printStackTrace(situation);
	  }
	  public void tileComputed(Object eventSource, TileRequest[] requests, PlanarImage image, int tileX, int tileY, Raster tile) {
	    storeTile(tileX, tileY, tile);
	    fireImageChanged();
	  }
	  public void tileCancelled(Object eventSource, TileRequest[] requests, PlanarImage image, int tileX, int tileY) {
	  }
	});
    }

    minTileX = image.getMinTileX();
    maxTileX = image.getMinTileX() + image.getNumXTiles() - 1;
    minTileY = image.getMinTileY();
    maxTileY = image.getMinTileY() + image.getNumYTiles() - 1;

    sampleModel = image.getSampleModel();
    colorModel = image.getColorModel();
    if (colorModel == null) {
      colorModel = PlanarImage.createColorModel(image.getSampleModel());
    }

    if (colorModel == null) {
      throw new IllegalArgumentException("JAIImage is unable to display supplied PlanarImage.");
    }
    
    if (colorModel.getTransparency() != Transparency.OPAQUE) {
      Object col = image.getProperty("background_color");
      if (col != null && col instanceof Color) {
	backgroundColor = (Color)col;
      } else {
	backgroundColor = Color.white;
      }
    }
    
    minX = image.getMinX();
    minY = image.getMinY();
    width = image.getWidth();
    height = image.getHeight();

    tileWidth = image.getTileWidth();
    tileHeight = image.getTileHeight();
    tileGridXOffset = image.getTileGridXOffset();
    tileGridYOffset = image.getTileGridYOffset();

    queueTiles();
  }

  /** Queues all the tiles for computation. Otherwise the tiles will only 
      be computed as soon as the image is to be painted. */

  public void queueTiles() {
    // Queue all tiles for computation
    Point[] tiles = new Point[image.getNumXTiles()*image.getNumYTiles()];
    int i=0;
    for (int y = minTileY; y <= maxTileY; y++) {
      for (int x = minTileX; x <= maxTileX; x++) {
	tiles[i++] = new Point(x, y);
      }
    }
    image.queueTiles(tiles);
  }

  /** Returns the PlanarImage that is painted with this object.

      @return PlanarImage painted with this object. */

  public PlanarImage getImage() {
    return image;
  }

  /** Returns the width of the image.

      @return Width of the image. */

  public int getIconWidth() {
    return image.getWidth();
  }

  /** Returns the height of the image.

      @return Height of the image. */

  public int getIconHeight() {
    return image.getHeight();
  }

  /** Paints the image.

      @param component Component, into which the image is painted.
      @param graphics Current graphics context.
      @param x X position.
      @param y Y position. */

  public void paintIcon(Component component, Graphics graphics, int x, int y) {
    if (!(graphics instanceof Graphics2D)) {
      throw new RuntimeException("DisplayImage requires Graphics2D.");
    }
    Graphics2D g2d = (Graphics2D) graphics;

    paintComponent = component;

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

	if (cacheTiles) {
	  Hashtable xtable = (Hashtable) ytable.get(new Integer(tj));
	  if (xtable != null) {
	    tile = (Raster) xtable.get(new Integer(ti));
	  }
	  if (tile == null && !asyncPaint) {
	    tile = image.getTile(ti, tj);
	    storeTile(ti, tj, tile);
	  }
	} else {
	  tile = image.getTile(ti, tj);
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

  /** Calls repaint in every component that has asked the image to be painted. */
  
  protected void fireImageChanged() {
    if (paintComponent != null) paintComponent.repaint();
  }

  /** Caches the given tile in our private tile cache. 

      @param tileX X position of the tile.
      @param tileY Y position of the tile.
      @param tile Tile to cache. */
  
  protected void storeTile(int tileX, int tileY, Raster tile) {
    Hashtable xtable = (Hashtable) ytable.get(new Integer(tileY));
    if (xtable == null) {
      xtable = new Hashtable();
      ytable.put(new Integer(tileY), xtable);
    }
    xtable.put(new Integer(tileX), tile);
  }

  /** Some utility functions */
  
  private final int XtoTileX(int x) {
    return (int) Math.floor((double) (x - tileGridXOffset)/tileWidth);
  }
  
  private final int YtoTileY(int y) {
    return (int) Math.floor((double) (y - tileGridYOffset)/tileHeight);
  }
  
  private final int TileXtoX(int tx) {
    return tx*tileWidth + tileGridXOffset;
  }
  
  private final int TileYtoY(int ty) {
    return ty*tileHeight + tileGridYOffset;
  }
}
