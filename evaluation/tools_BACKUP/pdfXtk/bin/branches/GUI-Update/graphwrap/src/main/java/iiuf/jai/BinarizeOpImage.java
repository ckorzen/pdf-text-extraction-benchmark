
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
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import javax.media.jai.TileCache;
import javax.media.jai.UntiledOpImage;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Binarize operator
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class BinarizeOpImage
  extends UntiledOpImage
{
  /** Binarization threshold */

  private int threshold;

  /** Constructs a BinarizeOpImage object */

  public BinarizeOpImage(RenderedImage source,
			 Map hints,
			 ImageLayout layout,
			 Integer threshold)
  {
    super(source, hints, layout);

    this.threshold = threshold.intValue();
  }

  /** Compute the output image */

  public void computeImage(Raster[] sources,
			   WritableRaster dest,
			   Rectangle destRect) 
  {
    RasterFormatTag[] formatTags = getFormatTags();

    Rectangle srcRect = mapDestRect(destRect, 0);

    RasterAccessor srcAccessor = 
      new RasterAccessor(sources[0], srcRect, formatTags[0],
			 getSourceImage(0).getColorModel());

    DirectRasterAccessor dstAccessor = new DirectRasterAccessor(dest, getColorModel());

    switch (srcAccessor.getDataType()) {
    case DataBuffer.TYPE_BYTE:
      byteLoop(srcAccessor,dstAccessor);
      break;
    case DataBuffer.TYPE_INT:
      intLoop(srcAccessor,dstAccessor);
      break;
    default:
      throw new RuntimeException(getClass().getName() + 
				 " does not implement computeRect" + 
				 " for short/float/double data");
    }
  }

  public void byteLoop(RasterAccessor src, DirectRasterAccessor dst) {
    byte srcDataArrays[][] = src.getByteDataArrays(); 
    int srcBandOffsets[] = src.getBandOffsets();
    int srcPixelStride = src.getPixelStride();
    int srcScanlineStride = src.getScanlineStride();

    // 1 band only
    byte srcData[] = srcDataArrays[0];
    int srcScanlineOffset = srcBandOffsets[0];

    for (int y = 0; y < getHeight(); y++) {
      int srcPixelOffset = srcScanlineOffset;
      for (int x = 0; x < getWidth(); x++) {
	int pixel = srcData[srcPixelOffset] & 0xff;
	if (pixel <= threshold) {
	  dst.setPixel(x, y, 1);
	} else {
	  dst.setPixel(x, y, 0);
	}
	srcPixelOffset += srcPixelStride;
      }
      srcScanlineOffset += srcScanlineStride;
    }
  }

  public void intLoop(RasterAccessor src, DirectRasterAccessor dst) {
    int srcDataArrays[][] = src.getIntDataArrays(); 
    int srcBandOffsets[] = src.getBandOffsets();
    int srcPixelStride = src.getPixelStride();
    int srcScanlineStride = src.getScanlineStride();

    // 1 band only
    int srcData[] = srcDataArrays[0];
    int srcScanlineOffset = srcBandOffsets[0];

    for (int y = 0; y < getHeight(); y++) {
      int srcPixelOffset = srcScanlineOffset;
      for (int x = 0; x < getWidth(); x++) {
	int pixel = srcData[srcPixelOffset];
	if (pixel <= threshold) {
	  dst.setPixel(x, y, 1);
	} else {
	  dst.setPixel(x, y, 0);
	}
	srcPixelOffset += srcPixelStride;
      }
      srcScanlineOffset += srcScanlineStride;
    }
  }
}
