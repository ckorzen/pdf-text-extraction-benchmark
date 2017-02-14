
package iiuf.jai;

import java.awt.Rectangle; 
import java.awt.RenderingHints; 

import java.awt.image.ColorModel; 
import java.awt.image.DataBuffer; 
import java.awt.image.IndexColorModel; 
import java.awt.image.MultiPixelPackedSampleModel; 
import java.awt.image.RenderedImage; 
import java.awt.image.SampleModel; 
import java.awt.image.renderable.ParameterBlock; 
import java.awt.image.renderable.RenderedImageFactory; 

import javax.media.jai.ImageLayout; 
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl; 
import javax.media.jai.OperationRegistry; 
import javax.media.jai.registry.RIFRegistry;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Binarize descriptor
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class BinarizeDescriptor
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */

  private static final String[][] resources = {
    {"GlobalName", "Binarize"}, 
    {"LocalName", "Binarize"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "Grayscale to binary"}, 
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"},
    {"arg0Desc", "binarization threshold"},
  };

  /** Parameter names */

  private static final String[] paramNames = {
    "threshold"
  }; 

  /** Class types for the parameters */

  private static final Class[] paramClasses = {
    Integer.class
  };

  /** Default values of the parameters */

  private static final Object[] paramDefaults = {
    new Integer(128)
  }; 

  /** Valid parameter values */

  private static final Object[] validParamValues = {
    null
  };

  /** Source names */

  private static final String[] sourceNames = {
    "source0"
  };

  /** Source classes */

  private static final Class[][] sourceClasses = {
    { RenderedImage.class }
  };

  /** Supported modes */

  private static final String[] supportedModes = {
    "rendered"
  };

  /** Constructs an BinarizeDescriptor object */

  public BinarizeDescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Color map */

  private static byte[] bwColors = {
    (byte) 0xff,                // 0xffffff -> white
    (byte) 0x00                 // 0x000000 -> black
  };

  /** Creates an BinarizeOpImage with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderingHints)
  {
    RenderedImage img = paramBlock.getRenderedSource(0);

    ImageLayout il = new ImageLayout(img);
    ColorModel cm = new IndexColorModel(1, 2, bwColors, bwColors, bwColors);
    SampleModel sm = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE,
						     img.getWidth(),
						     img.getHeight(),
						     1);

    il.setColorModel(cm);
    il.setSampleModel(sm);

    return new BinarizeOpImage(paramBlock.getRenderedSource(0),
			       renderingHints,
			       il,
			       (Integer)paramBlock.getObjectParameter(0));
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    BinarizeDescriptor desc = new BinarizeDescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "binarize", "iiuf.jai", desc);
  }
}


