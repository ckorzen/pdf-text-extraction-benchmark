
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

   RLSAOpDescriptor describes the RLSA operator for the Java Advanced Imaging API.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class RLSADescriptor
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */

  private static final String[][] resources = {
    {"GlobalName", "RLSA"}, 
    {"LocalName", "RLSA"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "Run Length Smoothing Algorithm"}, 
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"},
    {"arg0Desc", "direction"},
    {"arg1Desc", "threshold"}
  };

  /** Parameter names */

  private static final String[] paramNames = { 
    "direction", 
    "threshold"
  }; 

  /** Class types for the parameters */

  private static final Class[] paramClasses = { 
    java.lang.Integer.class, 
    java.lang.Integer.class
  };

  /** Default values of the parameters */

  private static final Object[] paramDefaults = { 
    new Integer(RLSAOpImage.DIRECTION_H), 
    new Integer(8)
  }; 

  /** Valid parameter values */

  private static final Object[] validParamValues = { 
    null, null
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

  /** Constructs an RLSADescriptor object */

  public RLSADescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Creates an RLSAOpImage with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderHints)
  {
    return new RLSAOpImage(paramBlock.getRenderedSource(0),
			   null,
			   new ImageLayout(paramBlock.getRenderedSource(0)),
			   (Integer) paramBlock.getObjectParameter(0),
			   (Integer) paramBlock.getObjectParameter(1));
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    RLSADescriptor desc = new RLSADescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "rlsa", "iiuf.jai", desc);
  }
}


