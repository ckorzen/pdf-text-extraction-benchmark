
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
   (c) 1999, IIUF<p>

   Power operator a^b.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class PowerOpImage
  extends UntiledOpImage
{
  /** Constructs a PowwerOpImage object */

  public PowerOpImage(RenderedImage source,
		      Map hints,
		      ImageLayout layout,
		      Double exponent)
  {
    super(source, hints, layout);

    this.exponent = exponent.doubleValue();
  }

  /** Exponent value */

  protected double exponent;

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
    RasterAccessor dstAccessor = 
      new RasterAccessor(dest, destRect, formatTags[1],
			 getColorModel());

    switch (srcAccessor.getDataType()) {
    case DataBuffer.TYPE_BYTE:
      byteLoop(srcAccessor,dstAccessor);
      break;
    default:
      throw new RuntimeException(getClass().getName() + 
				 " does not implement computeRect" + 
				 " for short/int/float/double data");
    }

    if (dstAccessor.isDataCopy()) {
      dstAccessor.clampDataArrays();
      dstAccessor.copyDataToRaster();
    }
  }

  public void byteLoop(RasterAccessor src, RasterAccessor dst) {
    byte srcDataArrays[][] = src.getByteDataArrays(); 
    int srcBandOffsets[] = src.getBandOffsets();
    int srcPixelStride = src.getPixelStride();
    int srcScanlineStride = src.getScanlineStride();
    byte dstDataArrays[][] = dst.getByteDataArrays(); 
    int dstBandOffsets[] = dst.getBandOffsets();
    int dstPixelStride = dst.getPixelStride();
    int dstScanlineStride = dst.getScanlineStride();

    // 1 band only
    byte srcData[] = srcDataArrays[0];
    int srcScanlineOffset = srcBandOffsets[0];
    byte dstData[] = dstDataArrays[0];
    int dstScanlineOffset = dstBandOffsets[0];

    for (int y = 0; y < dst.getHeight(); y++) {
      int srcPixelOffset = srcScanlineOffset;
      int dstPixelOffset = dstScanlineOffset;
      for (int x = 0; x < dst.getWidth(); x++) {
	double pixel = (double) (srcData[srcPixelOffset] & 0xff);
	double cpixel = Math.pow(pixel/255.0D, this.exponent);

	dstData[dstPixelOffset] = (byte) (cpixel * 255.0D);

	srcPixelOffset += srcPixelStride;
	dstPixelOffset += dstPixelStride;
      }
      srcScanlineOffset += srcScanlineStride;
      dstScanlineOffset += dstScanlineStride;
    }
  }

}
