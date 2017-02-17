package iiuf.util;

import java.awt.Rectangle;

/**
   Interface for filtering rectangles
   
   (c) 2001, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public interface RectMapFilter {
  public boolean filter(Rectangle r);
}
