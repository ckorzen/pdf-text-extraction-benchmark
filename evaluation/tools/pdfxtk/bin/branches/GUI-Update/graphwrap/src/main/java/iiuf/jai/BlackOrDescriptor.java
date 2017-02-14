
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

   BlackOrDescriptor describes the BlackOr operator for the Java Advanced
   Imaging API.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class BlackOrDescriptor 
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{

  /** General documentation and parameter list */
  
  private static final String[][] resources = {
    {"GlobalName", "BlackOr"}, 
    {"LocalName", "BlackOr"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "Bytewise OR of Black pixels"},
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"}
  };

  /** Parameter names */

  private static final String[] paramNames = { }; 

  /** Class types for the parameters */

  private static final Class[] paramClasses = { };

  /** Default values of the parameters */

  private static final Object[] paramDefaults = { }; 

  /** Valid parameter values */

  private static final Object[] validParamValues = { };

  /** Source names */

  private static final String[] sourceNames = {
    "source0", "source1"
  };

  /** Source classes */

  private static final Class[][] sourceClasses = {
    { RenderedImage.class, RenderedImage.class }
  };

  /** Supported modes */

  private static final String[] supportedModes = {
    "rendered"
  };

  /** Constructs a BlackOrDescriptor object */

  public BlackOrDescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }

  /** Creates a BlackOrOpImage with a given ParameterBlock */

  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderingHints)
  {
    return new BlackOrOpImage(paramBlock.getRenderedSource(0),
			      paramBlock.getRenderedSource(1),
			      new ImageLayout(paramBlock.getRenderedSource(0)),
			      renderingHints,
			      true);
  }

  /** Registers this operator with the JAI environment */

  public static void register() {
    OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
    BlackOrDescriptor desc = new BlackOrDescriptor();
    or.registerDescriptor(desc);
    RIFRegistry.register(or, "blackor", "iiuf.jai", desc);
  }
}
