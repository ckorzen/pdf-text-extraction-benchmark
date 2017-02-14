
package iiuf.jai;

import java.awt.RenderingHints; 
import java.awt.image.RenderedImage; 
import java.awt.image.renderable.ParameterBlock; 
import java.awt.image.renderable.RenderedImageFactory; 

import javax.media.jai.ImageLayout; 
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl; 
import javax.media.jai.OperationRegistry; 
import javax.media.jai.registry.RIFRegistry;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Descriptor for the power function a^b.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class PowerDescriptor
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */

  private static final String[][] resources = {
    {"GlobalName", "power"}, 
    {"LocalName", "power"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "power"}, 
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"},
    {"arg0Desc", "power"}
  };

  /** Parameter names */

  private static final String[] paramNames = { 
    "power"
  }; 

  /** Class types for the parameters */

  private static final Class[] paramClasses = { 
    java.lang.Double.class
  };

  /** Default values of the parameters */

  private static final Object[] paramDefaults = { 
    new Double(1.0D)
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

  /** Constructs an PowerDescriptor object */

  public PowerDescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Creates an PowerOpImage with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderingHints)
  {
    return new PowerOpImage(paramBlock.getRenderedSource(0),
			    renderingHints,
			    new ImageLayout(paramBlock.getRenderedSource(0)),
			    (Double) paramBlock.getObjectParameter(0));
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    PowerDescriptor desc = new PowerDescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "power", "iiuf.jai", desc);
  }
}


