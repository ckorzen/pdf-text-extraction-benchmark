package iiuf.dom;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;

/**
   (c) 2000, IIUF<p>

   Simple implementation of the NodeList interface using an ArrayList.
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class ElementList
  extends ArrayList
  implements NodeList
{
  public Node item(int index) {
    return (Node) get(index);
  }
  
  public int getLength() {
    return size();
  }
}
