
package iiuf.jai;

import java.awt.color.ColorSpace;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   LUV ColorSpace for use with JAI.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class LUV_ColorSpace
  extends ColorSpace
{

  /** Constructs a ColorSpace object */

  public LUV_ColorSpace() {
    super(TYPE_Luv, 3);

    cspace_XYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
  }

  /** Reference white of the light source (Yn) */

  private static final float Yn = 1.0F;

  /** un' corresponding to Yn */

  private static final float unp = 0.2009F;

  /** vn' corresponding to Yn */

  private static final float vnp = 0.4610F;

  /** ColorSpace for doing RGB to XYZ conversion and vice versa */

  private ColorSpace cspace_XYZ;

  /** Converts a color from the XYZ space to L*u*v*

      @param colorvalue Color value in XYZ color space
      @return color in L*u*v* color space */

  public float[] fromCIEXYZ(float[] colorvalue) {
    float[] result = new float[3];
    float up, vp;		// u', v'

    if (colorvalue[1] / Yn > 0.008856) {
      result[0] = (float) (116 * Math.exp(Math.log(colorvalue[1] / Yn) / 3) - 16);
    } else {
      result[0] = 903.3F * colorvalue[1] / Yn;
    }

    up = 4*colorvalue[0] / (colorvalue[0] + 15*colorvalue[1] + 3*colorvalue[2]);
    vp = 9*colorvalue[1] / (colorvalue[0] + 15*colorvalue[1] + 3*colorvalue[2]);

    result[1] = 13 * result[0] * (up - unp);
    result[2] = 13 * result[0] * (vp - vnp);

    return result;
  }

  /** Converts a color from the RGB space to L*u*v*

      @param colorvalue Color value in RGB color space
      @return color in L*u*v* color space */

  public float[] fromRGB(float[] colorvalue) {
    return fromCIEXYZ(cspace_XYZ.fromRGB(colorvalue));
  }

  /** Converts a color from the L*u*v* space to XYZ

      @param colorvalue Color value in L*u*v* color space
      @return color in XYZ color space */

  public float[] toCIEXYZ(float[] colorvalue) {
    float[] result = new float[3];
    float up, vp;		// u', v'

    up = (colorvalue[1] / (13 * colorvalue[0])) + unp;
    vp = (colorvalue[2] / (13 * colorvalue[0])) + vnp;

    if ((colorvalue[0] + 16) / 116 <= 0) {
      result[1] = (colorvalue[0]*Yn) / 903.3F;
    } else {
      result[1] = (float) (Math.exp(3 * Math.log((colorvalue[0] + 16) / 116))) * Yn;
    }

    result[2] = result[1] * (12 -3*up - 20*vp) / (4*vp);
    result[0] = up * (15*result[1] + 3*result[2]) / (4 - up);

    return result;
  }

  /** Converts a color from the L*u*v* space to RGB

      @param colorvalue Color value in L*u*v* color space
      @return color in RGB color space */

  public float[] toRGB(float[] colorvalue) {
    return cspace_XYZ.toRGB(toCIEXYZ(colorvalue));
  }
}
