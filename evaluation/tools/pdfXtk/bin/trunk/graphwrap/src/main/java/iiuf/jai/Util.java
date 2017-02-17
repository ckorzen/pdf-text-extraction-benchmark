
package iiuf.jai;

import java.awt.color.ColorSpace;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.MultiPixelPackedSampleModel;

import javax.media.jai.PlanarImage;

/**
   (c) 1999, IIUF<p>

   Utilities for JAI
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class Util {

  /** Turns a DataBuffer.TYPE_* integer into a string

      @param datatype the DataBuffer.TYPE_* integer
      @return human readable form */

  public static String getDataType(int datatype) {
    switch (datatype) {
    case DataBuffer.TYPE_BYTE:
      return "TYPE_BYTE";
    case DataBuffer.TYPE_DOUBLE:
      return "TYPE_DOUBLE";
    case DataBuffer.TYPE_FLOAT:
      return "TYPE_FLOAT";
    case DataBuffer.TYPE_INT:
      return "TYPE_INT";
    case DataBuffer.TYPE_SHORT:
      return "TYPE_SHORT";
    case DataBuffer.TYPE_UNDEFINED:
      return "TYPE_UNDEFINED";
    case DataBuffer.TYPE_USHORT:
      return "TYPE_USHORT";
    default:
      return "unknown";
    }
  }

  /** Displays information about an image.

      @param image image to display information about */

  public static void printImageInfo(PlanarImage image) {
    String result;

    SampleModel sm = image.getSampleModel();
    ColorModel cm = image.getColorModel();
    ColorSpace cs = cm.getColorSpace();

    result  = "Width:           " + image.getWidth() + "\n";
    result += "Height:          " + image.getHeight() + "\n";
    result += "SampleModel:     " + sm.getClass().getName() + "\n";
    result += "DataType:        " + getDataType(sm.getDataType()) + "\n";
    result += "Bands:           " + sm.getNumBands() + "\n";
    result += "SampleSizes:     ";
    for (int i = 0; i < sm.getNumBands(); i++) {
      result += sm.getSampleSize(i) + " ";
    }
    result += "\n";
    result += "ColorModel:      " + cm.getClass().getName() + "\n";
    result += "ColorComponents: " + cm.getNumComponents() + "\n";
    result += "HasAlpha:        " + ((cm.hasAlpha()) ? "yes" : "no") + "\n";
    result += "ColorSpace:      " + cs.getClass().getName() + "\n";
    result += "TileSize:        " + image.getTileWidth() + "/" + image.getTileHeight() + "\n";

    System.out.print(result);
  }

  /** Checks if an image is binary or not.

      @param image image to check
      @return true if it is a binary image */

  public static boolean isBinary(PlanarImage image) {
    SampleModel sm = image.getSampleModel();
    ColorModel cm = image.getColorModel();

    return (sm instanceof MultiPixelPackedSampleModel) &&
      (sm.getDataType() == DataBuffer.TYPE_BYTE) &&
      (sm.getNumBands() == 1) &&
      (sm.getSampleSize(1) == 1);
  }
}
