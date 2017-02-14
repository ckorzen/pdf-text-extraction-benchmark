
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

   Skeleton descriptor
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class SkeletonDescriptor
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */

  private static final String[][] resources = {
    {"GlobalName", "Skeleton"}, 
    {"LocalName", "Skeleton"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "Skeleton"}, 
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"},
    {"arg0Desc", "invert"}
  };

  /** Parameter names */

  private static final String[] paramNames = { 
    "invert"
  };

  /** Parameter classes */

  private static final Class[] paramClasses = { 
    java.lang.Boolean.class
  };

  /** Parameter defaults */

  private static final Object[] paramDefaults = { 
    Boolean.TRUE
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

  /** Constructs an RLSADescriptor object */

  public SkeletonDescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Creates an RLSAOpImage with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderingHints)
  {
    return new SkeletonOpImage(paramBlock.getRenderedSource(0),
			       renderingHints,
			       new ImageLayout(paramBlock.getRenderedSource(0)),
			       (Boolean) paramBlock.getObjectParameter(0));
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    SkeletonDescriptor desc = new SkeletonDescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "skeleton", "iiuf.jai", desc);
  }
}


