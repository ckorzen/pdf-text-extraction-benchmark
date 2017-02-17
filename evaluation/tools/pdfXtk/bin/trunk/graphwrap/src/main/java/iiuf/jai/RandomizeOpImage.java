
package iiuf.jai;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.Rectangle;

import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.media.jai.ImageLayout;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import javax.media.jai.TileCache;
import javax.media.jai.PointOpImage;
import javax.media.jai.PlanarImage;

/**
   (c) 1999, IIUF<p>

   Randomize operator
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class RandomizeOpImage
  extends PointOpImage
{

  /** Density */

  protected double density;

  /** Constructs a RandomizeOpImage object */

  public RandomizeOpImage(ImageLayout layout,
			  Map configuration,
			  Double density)
  {
    super((Vector) null, layout, configuration, false);

    this.density = density.doubleValue();
  }

  /** True if the seed is already set */

  private boolean seedSet = false;

  /** Seed */

  private long seed;
  
  /** Compute an output tile */

  public void computeRect(PlanarImage[] sources,
			  WritableRaster dest,
			  Rectangle destRect)
  {
    RasterFormatTag[] formatTags = getFormatTags();

    RasterAccessor dstAccessor = new RasterAccessor(dest, destRect, 
						    formatTags[0], 
						    getColorModel());

    switch (dstAccessor.getDataType()) {
    case DataBuffer.TYPE_BYTE:
      byteLoop(dstAccessor);
      break;
    default:
      String className = this.getClass().getName();
      throw new RuntimeException(className + 
				 " does not implement computeRect" + 
				 " for short/int/float/double data");
    }

    if (dstAccessor.isDataCopy()) {
      dstAccessor.clampDataArrays();
      dstAccessor.copyDataToRaster();
    }
  }

  public void byteLoop(RasterAccessor dst) {
    Random rand = new Random();
    if (!seedSet) {
      seed = rand.nextLong();
    }

    rand.setSeed(seed);
    seedSet = true;

    int dwidth = dst.getWidth();
    int dheight = dst.getHeight();
    int dnumBands = dst.getNumBands();
    
    byte dstDataArrays[][] = dst.getByteDataArrays();
    int dstBandOffsets[] = dst.getBandOffsets();
    int dstPixelStride = dst.getPixelStride();
    int dstScanlineStride = dst.getScanlineStride();

    byte[] bytes = new byte[1];

    for (int k = 0; k < dnumBands; k++)  {
      byte dstData[] = dstDataArrays[k];
      int dstScanlineOffset = dstBandOffsets[k];
      for (int j = 0; j < dheight; j++)  {
	int dstPixelOffset = dstScanlineOffset;
	for (int i = 0; i < dwidth; i++)  {
	  if (rand.nextDouble() < density) {
	    rand.nextBytes(bytes);
	  } else {
	    bytes[0] = 0;
	  }

	  dstData[dstPixelOffset] = bytes[0];
	  dstPixelOffset += dstPixelStride;
	}
	dstScanlineOffset += dstScanlineStride;
      }
    }
  }
}
