
package iiuf.jai;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.Rectangle;
import java.util.Map;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.TileCache;
import javax.media.jai.UntiledOpImage;

/**
   (c) 1999, IIUF<p>

   RLSAOpImage is the implementation of the Run Length Smoothing Algorithm
   for the Java Advanced Imaging API. The operator only supports one
   particular image format.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class RLSAOpImage
  extends UntiledOpImage
{
  /** Horizontal RLSA */

  public static int DIRECTION_H = 0;

  /** Vertical RLSA */

  public static int DIRECTION_V = 1;

  /** Constructs an RLSAOpImage */

  public RLSAOpImage(RenderedImage source, 
		     Map hints,
		     ImageLayout layout,
		     Integer direction,
		     Integer threshold)
  {
    super(source, hints, layout);

    this.direction = direction.intValue();
    this.threshold = threshold.intValue();
  }

  /** Current direction */

  protected int direction;

  /** Threshold value */

  protected int threshold;

  /** Computes the output image */

  public void computeImage(Raster[] sources,
			   WritableRaster dest,
			   Rectangle destRect) 
  {
    if (direction == DIRECTION_H) {
      byteLoop_h(sources[0], dest);
    } else if (direction == DIRECTION_V) {
      byteLoop_v(sources[0], dest);
    } else {
      throw new RuntimeException(this.getClass().getName() + 
				 " invalid RLSA direction");
    }
  }

  /** Needed for pixel accesses */

  private static int bitAccess[] = { 0x80, 0x40, 0x20, 0x10, 
				     0x08, 0x04, 0x02, 0x01 };

  /** Gets the index of the white color (0 or 1). This may differ because
      of the PhotoMetricInterpretation flag in the TIFF format. */

  private int getWhite() {
    if (getColorModel().getRGB(0) == Color.white.getRGB()) {
      return 0;
    } else {
      return 1;
    }
  }

  /** Does horizontal RLSA */

  private void byteLoop_h(Raster src, WritableRaster dst) {
    int minX = getMinX();
    int maxX = getMaxX();
    int minY = getMinY();
    int maxY = getMaxY();

    DataBufferByte srcdb = (DataBufferByte) src.getDataBuffer();
    DataBufferByte dstdb = (DataBufferByte) dst.getDataBuffer();

    byte srcData[] = srcdb.getData();
    byte dstData[] = dstdb.getData();

    MultiPixelPackedSampleModel srcsm = (MultiPixelPackedSampleModel) src.getSampleModel();
    MultiPixelPackedSampleModel dstsm = (MultiPixelPackedSampleModel) dst.getSampleModel();

    int srcScanlineStride = srcsm.getScanlineStride();
    int dstScanlineStride = dstsm.getScanlineStride();

    int srcScanlineOffset = minY*srcScanlineStride;
    int dstScanlineOffset = 0;

    int[] line = new int[maxX];

    int white = getWhite();

    for (int y = minY; y < maxY; y++) {
      int srcPixelOffset = srcScanlineOffset;
      int dstPixelOffset = dstScanlineOffset;

      int lastx = minX-(threshold+2);

      // RLSA one line

      if (white == 0) {
	for (int x = minX; x < maxX; x++) {
	  int xBit = x % 8;
	  int xOffset = x / 8;
	  
	  if ((srcData[srcPixelOffset+xOffset] & bitAccess[xBit]) != 0) {
	    line[x] = 1;
	    if (x < lastx+threshold+2) {
	      for (int i = lastx; i < x; i++) {
		line[i] = 1;
	      }
	    }
	    lastx = x;
	  } else {
	    line[x] = 0;
	  }
	}
      } else {	
	for (int x = minX; x < maxX; x++) {
	  int xBit = x % 8;
	  int xOffset = x / 8;
	  
	  if ((srcData[srcPixelOffset+xOffset] & bitAccess[xBit]) == 0) {
	    line[x] = 0;
	    if (x < lastx+threshold+2) {
	      for (int i = lastx; i < x; i++) {
		line[i] = 0;
	      }
	    }
	    lastx = x;
	  } else {
	    line[x] = 1;
	  }
	}
      }

      // Copy destination line to image

      for (int srcx = minX, dstx = 0; srcx < maxX; srcx++, dstx++) {
	int xBit = dstx % 8;
	int xOffset = dstx / 8;

	if (line[srcx] == 1) {
	  dstData[dstPixelOffset+xOffset] |= bitAccess[xBit];
	} else {
	  dstData[dstPixelOffset+xOffset] &= ~bitAccess[xBit];
	}
      }

      // Next line

      srcScanlineOffset += srcScanlineStride;
      dstScanlineOffset += dstScanlineStride;
    }
  }

  /** Does vertical RLSA */

  private void byteLoop_v(Raster src, WritableRaster dst) {
    int minX = getMinX();
    int maxX = getMaxX();
    int minY = getMinY();
    int maxY = getMaxY();

    DataBufferByte srcdb = (DataBufferByte) src.getDataBuffer();
    DataBufferByte dstdb = (DataBufferByte) dst.getDataBuffer();

    byte srcData[] = srcdb.getData();
    byte dstData[] = dstdb.getData();

    MultiPixelPackedSampleModel srcsm = (MultiPixelPackedSampleModel) src.getSampleModel();
    MultiPixelPackedSampleModel dstsm = (MultiPixelPackedSampleModel) dst.getSampleModel();

    int srcScanlineStride = srcsm.getScanlineStride();
    int dstScanlineStride = dstsm.getScanlineStride();

    int srcScanlineOffset = minY*srcScanlineStride;
    int dstScanlineOffset = 0;

    int[] column = new int[maxY];

    int white = getWhite();

    for (int x = minX, dstx = 0; x < maxX; x++, dstx++) {
      int srcxOffset = x / 8;
      int srcxBit = x % 8;
      int dstxOffset = dstx / 8;
      int dstxBit = dstx % 8;

      int srcLineOffset = srcScanlineOffset;
      int dstLineOffset = dstScanlineOffset;

      int lasty = minY-(threshold+2);

      if (white == 0) {
	for (int y = minY; y < maxY; y++) {
	  if ((srcData[srcLineOffset+srcxOffset] & bitAccess[srcxBit]) != 0) {
	    column[y] = 1;
	    if (y < lasty+threshold+2) {
	      for (int i = lasty; i<y; i++) {
		column[i] = 1;
	      }
	    }
	    lasty = y;
	  } else {
	    column[y] = 0;
	  }
	  srcLineOffset += srcScanlineStride;
	}
      } else {
	for (int y = minY; y < maxY; y++) {
	  if ((srcData[srcLineOffset+srcxOffset] & bitAccess[srcxBit]) == 0) {
	    column[y] = 0;
	    if (y < lasty+threshold+2) {
	      for (int i = lasty; i<y; i++) {
		column[i] = 0;
	      }
	    }
	    lasty = y;
	  } else {
	    column[y] = 1;
	  }
	  srcLineOffset += srcScanlineStride;
	}
      }

      // Copy resulting column onto image

      for (int srcy = minY, lineOffset = 0; srcy < maxY; srcy++, lineOffset += dstScanlineStride) {
	if (column[srcy] == 1) {
	  dstData[lineOffset+dstxOffset] |= bitAccess[dstxBit];
	} else {
	  dstData[lineOffset+dstxOffset] &= ~bitAccess[dstxBit];
	}
      }
    }
  }
}
