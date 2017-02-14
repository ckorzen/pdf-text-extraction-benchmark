
package iiuf.jai;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
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

   Randomize descriptor
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class RandomizeDescriptor
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */

  private static final String[][] resources = {
    {"GlobalName", "Randomize"}, 
    {"LocalName", "Randomize"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "Get a random image"}, 
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"},
    {"arg0Desc", "width"},
    {"arg1Desc", "height"},
    {"arg2Desc", "density"},
  };

  /** Parameter names */

  private static final String[] paramNames = {
    "width",
    "height",
    "density"
  }; 

  /** Class types for the parameters */

  private static final Class[] paramClasses = {
    Integer.class,
    Integer.class,
    Double.class
  };

  /** Default values of the parameters */

  private static final Object[] paramDefaults = {
    new Integer(256),
    new Integer(256),
    new Double(0.5)
  }; 

  /** Valid parameter values */

  private static final Object[] validParamValues = {
    null, null, null
  };

  /** Source names */

  private static final String[] sourceNames = { };

  /** Source classes */

  private static final Class[][] sourceClasses = { };

  /** Supported modes */

  private static final String[] supportedModes = {
    "rendered"
  };

  /** Constructs an RandomizeDescriptor object */

  public RandomizeDescriptor() {
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Creates an RandomizeOpImage with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderingHints)
  {
    int width = ((Integer) paramBlock.getObjectParameter(0)).intValue();
    int height = ((Integer) paramBlock.getObjectParameter(1)).intValue();

    ImageLayout il = new ImageLayout(0, 0, width, height);

    int[] bits = { 8 };

    ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
					    bits, false, false,
					    Transparency.OPAQUE,
					    DataBuffer.TYPE_BYTE);

    int[] bandoffsets = { 0 };
    
    SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
					      width, height, 1, width,
					      bandoffsets);


    il.setColorModel(cm);
    il.setSampleModel(sm);

    return new RandomizeOpImage(il, renderingHints, (Double) paramBlock.getObjectParameter(2));
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    RandomizeDescriptor desc = new RandomizeDescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "randomize", "iiuf.jai", desc);
  }
}


