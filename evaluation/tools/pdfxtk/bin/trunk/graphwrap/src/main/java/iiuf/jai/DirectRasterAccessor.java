
package iiuf.jai;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   DirectRasterAccessor allows to operate directly on raster data.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class DirectRasterAccessor {

  /** Constructs a DirectRasterAccessor object */

  public DirectRasterAccessor(Raster raster, ColorModel cm) {
    DataBuffer db = raster.getDataBuffer();

    offsetX = raster.getMinX()-raster.getSampleModelTranslateX();
    offsetY = raster.getMinY()-raster.getSampleModelTranslateY();

    if (!(db instanceof DataBufferByte)) {
      throw new RuntimeException("DataBuffer of Raster not of correct type " +
				 "(expected DataBufferByte, got " +
				 db.getClass().getName() + ")");
    }

    DataBufferByte dbb = (DataBufferByte) db;

    SampleModel sm = raster.getSampleModel();

    if (!(sm instanceof MultiPixelPackedSampleModel)) {
      throw new RuntimeException("SampleModel of Raster not of correct type " +
				 "(expected MultiPixelPackedSampleModel, got " +
				 sm.getClass().getName() + ")");
    }

    MultiPixelPackedSampleModel mppsm = (MultiPixelPackedSampleModel) sm;
    
    data = dbb.getData();
    scanlineStride = mppsm.getScanlineStride();

    if (cm.getRGB(0) == Color.white.getRGB()) {
      white = 0;
      black = 1;
    } else {
      white = 1;
      black = 0;
    }
  }

  /** Min x and y coordinate */

  public int offsetX;
  public int offsetY;

  /** Data byte array */

  public byte data[];

  /** Scanline stride */

  public int scanlineStride;

  /** White color */

  public int white;

  /** Black color */

  public int black;

  /** Predefined table for pixel access */

  public static int bitAccess[] = { 0x80, 0x40, 0x20, 0x10, 
				    0x08, 0x04, 0x02, 0x01 };

  /** Get pixel value */

  public final int getPixel(int x, int y) {
    if ((data[(y+offsetY)*scanlineStride + (x+offsetX)/8] & bitAccess[(x+offsetX)%8]) != 0) {
      return 1;
    } else {
      return 0;
    }
  }

  /** Set pixel value */

  public final void setPixel(int x, int y, int value) {
    if (value == 0) {
      data[(y+offsetY)*scanlineStride + (x+offsetX)/8] &= ~bitAccess[(x+offsetX)%8];
    } else {
      data[(y+offsetY)*scanlineStride + (x+offsetX)/8] |= bitAccess[(x+offsetX)%8];
    }
  }
}
