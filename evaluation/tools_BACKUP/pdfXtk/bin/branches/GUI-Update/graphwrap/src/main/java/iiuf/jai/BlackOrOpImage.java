
package iiuf.jai;

import java.awt.Color;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.Rectangle;
import java.util.Map;

import javax.media.jai.ImageLayout;
import javax.media.jai.RasterAccessor;
import javax.media.jai.PointOpImage;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   BlackOrOpImage is an operator which allows to do a bytewise OR of the
   black pixels of two binary images. The operator only supports one
   particular image format.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class BlackOrOpImage
  extends PointOpImage
{
  /** Constructs a BlackOrOpImage object */

  public BlackOrOpImage(RenderedImage source1,
			RenderedImage source2,
			ImageLayout layout,
			Map configuration,
			boolean cobbleSources)
  {
    super(source1, source2, layout, configuration, cobbleSources);
  }

  /** Computes the bytewise OR of the two source images */
  
  public void computeRect(Raster[] sources,
			  WritableRaster dest,
			  Rectangle destRect) {    
    byteLoop(sources[0], sources[1], dest, destRect);
  }

  /** Get the index of the white color (0 or 1) */

  private int getWhite() {
    if (getColorModel().getRGB(0) == Color.white.getRGB()) {
      return 0;
    } else {
      return 1;
    }
  }

  /** Bytewise OR of the black pixels */

  private void byteLoop(Raster src0, Raster src1, 
			WritableRaster dst, Rectangle dstRect) {
    int w = dst.getWidth();
    int h = dst.getHeight();

    DataBufferByte src0db = (DataBufferByte) src0.getDataBuffer();
    DataBufferByte src1db = (DataBufferByte) src1.getDataBuffer();
    DataBufferByte dstdb = (DataBufferByte) dst.getDataBuffer();

    byte src0Data[] = src0db.getData();
    byte src1Data[] = src1db.getData();
    byte dstData[] = dstdb.getData();

    MultiPixelPackedSampleModel src0sm = 
      (MultiPixelPackedSampleModel) src0.getSampleModel();
    MultiPixelPackedSampleModel src1sm = 
      (MultiPixelPackedSampleModel) src1.getSampleModel();
    MultiPixelPackedSampleModel dstsm = 
      (MultiPixelPackedSampleModel) dst.getSampleModel();

    int src0ScanlineStride = src0sm.getScanlineStride();
    int src1ScanlineStride = src1sm.getScanlineStride();
    int dstScanlineStride = dstsm.getScanlineStride();

    int white = getWhite();

    if (white == 0) {
      for (int offset = 0; offset < h*dstScanlineStride; offset++) {
	dstData[offset] = (byte) (src0Data[offset] | src1Data[offset]);
      }
    } else {
      for (int offset = 0; offset < h*dstScanlineStride; offset++) {
	dstData[offset] = (byte) (src0Data[offset] & src1Data[offset]);
      }
    }
  }
}
