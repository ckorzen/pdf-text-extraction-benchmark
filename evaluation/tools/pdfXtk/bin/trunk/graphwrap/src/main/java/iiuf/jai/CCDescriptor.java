
package iiuf.jai;

import java.awt.Rectangle;
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

   CCDescriptor describes the Connected Components detection operator for
   the Java Advanced Imaging API.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class CCDescriptor 
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */
  
  private static final String[][] resources = {
    {"GlobalName",  "CC"}, 
    {"LocalName",   "CC"}, 
    {"Vendor",      "IIUF"}, 
    {"Description", "Connected Components Detection"}, 
    {"DocURL",      "http://www-iiuf.unifr.ch"}, 
    {"Version",     "1.0"},
    {"arg0Desc",    "sampleRectangle"}
  };

  /** Parameter names */

  private static final String[] paramNames = {
    "sampleRectangle"
  }; 

  /** Class types for the parameters */

  private static final Class[] paramClasses = {
    Rectangle.class
  };

  /** Default values of the parameters */

  private static final Object[] paramDefaults = {
    null
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

  /** Constructs a CCDescriptor object */

  public CCDescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Invokes the operator with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderHints)
  {
    return new CCOpImage(paramBlock.getRenderedSource(0),
			 (Rectangle) paramBlock.getObjectParameter(0));
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    CCDescriptor desc = new CCDescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "cc", "iiuf.jai", desc);
  }
}
