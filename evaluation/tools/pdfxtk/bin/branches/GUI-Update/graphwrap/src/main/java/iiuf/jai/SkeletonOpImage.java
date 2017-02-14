
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

   Skeleton operator
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class SkeletonOpImage
  extends UntiledOpImage
{
  /** Constructs a SkeletonOpImage object */

  public SkeletonOpImage(RenderedImage source,
			 Map configuration,
			 ImageLayout layout,
			 Boolean invert)
  {
    super(source, configuration, layout);

    this.invert = invert.booleanValue();
  }

  /** Invert or not */

  protected boolean invert;

  /** Compute the output image */

  public void computeImage(Raster[] sources,
			   WritableRaster dest,
			   Rectangle destRect) 
  {
    DirectRasterAccessor srcDRA = new DirectRasterAccessor(sources[0], getColorModel());
    DirectRasterAccessor dstDRA = new DirectRasterAccessor(dest, getColorModel());

    for (int y = 0; y < getHeight(); y++) {
      for (int x = 0; x < getWidth(); x++) {
	if (invert) {
	  dstDRA.setPixel(x, y, 1-srcDRA.getPixel(x, y));
	} else {
	  dstDRA.setPixel(x, y, srcDRA.getPixel(x, y));
	}
      }
    }
  }
}
