package iiuf.dom;

import com.sun.xml.tree.ElementEx;
import com.sun.xml.tree.ElementNode;

import java.util.Hashtable;

/**
   (c) 1999, IIUF<p>

   DefaultElement which allows its tag name to be set.
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class DefaultElement
  extends ElementNode
{
  /** Sets the tag name of the element.

      @param tag the new tag name */

  public void setTag(String tag) {
    super.setTag(tag);
  }
}
