/* ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: ProjectionProfileDescriptor.java,v $
 * SUPPORT:	$Author: hassan $
 * CREATION:	$Date: 2006/05/17 10:22:22 $
 * VERSION:	$Revision: 1.1 $
 * OVERVIEW:	Descriptor of a project profile operator.
 * ------------------------------------------------------------------------ */
package iiuf.jai;

import java.awt.Rectangle; 
import java.awt.RenderingHints; 
import java.awt.image.RenderedImage; 
import java.awt.image.renderable.ParameterBlock; 
import java.awt.image.renderable.RenderedImageFactory; 

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl; 
import javax.media.jai.OperationRegistry; 
import javax.media.jai.registry.RIFRegistry;
/* ------------------------------------------------------------------------ */
public class ProjectionProfileDescriptor
  extends OperationDescriptorImpl 
  implements RenderedImageFactory 
{
  private static boolean registered = false;
  // Operator description
  private static final String[][] resources = {
    {"GlobalName", "ProjectionProfile"}, 
    {"LocalName", "ProjectionProfile"}, 
    {"Vendor", "IIUF"}, 
    {"Description", "ProjectionProfile"}, 
    {"DocURL", "http://www-iiuf.unifr.ch"}, 
    {"Version", "1.0"},
    {"arg0Desc", "region"}
  };
  /*------------------------------------------------------------------------*/
  // Parameter names
  private static final String[] paramNames = { 
    "region"
  }; 
  /*------------------------------------------------------------------------*/
  // Parameter types
  private static final Class[] paramClasses = { 
    java.awt.Rectangle.class
  };
  /*------------------------------------------------------------------------*/
  // Parameter defaults
  private static final Object[] paramDefaults = { 
    new Rectangle()
  }; 
  /*------------------------------------------------------------------------*/
  // Valid parameter values
  private static final Object[] validParamValues = {
    null
  };
  /*------------------------------------------------------------------------*/
  // Source names
  private static final String[] sourceNames = {
    "source0"
  };
  /*------------------------------------------------------------------------*/
  // Source classes
  private static final Class[][] sourceClasses = {
    { RenderedImage.class }
  };
  /*------------------------------------------------------------------------*/
  // Supported modes
  private static final String[] supportedModes = {
    "rendered"
  };
  /*------------------------------------------------------------------------*/
  // Constructor
  public ProjectionProfileDescriptor() { 
    super(resources, supportedModes, sourceNames, sourceClasses,
	  paramNames, paramClasses, paramDefaults, validParamValues);
  }
  /*------------------------------------------------------------------------*/
  // Invoke the operator with a given ParameterBlock
  public RenderedImage create(ParameterBlock paramBlock, 
			      RenderingHints renderHints)
  {
    if (!validateParameters(paramBlock)) { 
      return null;
    } 
    
    return new ProjectionProfileOpImage
      (paramBlock.getRenderedSource(0),
       (Rectangle) paramBlock.getObjectParameter(0));
  }
  /*------------------------------------------------------------------------*/
  // Validate parameters in the ParameterBlock
  public boolean validateParameters(ParameterBlock paramBlock) { 
    Object arg = paramBlock.getObjectParameter(0); 
    if (arg == null) { 
      return false; 
    } 
    if (!(arg instanceof Rectangle)) { 
      return false; 
    } 
    return true; 
  } 
  /*------------------------------------------------------------------------*/
  // Registers this operator with the JAI environment 
  public static void register() {
    if (!registered) {
      OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
      ProjectionProfileDescriptor desc = new ProjectionProfileDescriptor();
      or.registerDescriptor(desc);
      RIFRegistry.register(or, "projectionprofile", "iiuf.jai", desc);
      registered = true;
    }
  }
  /*------------------------------------------------------------------------*/
}
/* ------------------------------------------------------------------------ */


