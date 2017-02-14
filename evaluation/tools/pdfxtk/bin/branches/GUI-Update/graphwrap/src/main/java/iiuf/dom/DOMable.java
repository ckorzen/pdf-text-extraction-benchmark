package iiuf.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
   (c) 2000, IIUF<p>
   
   Interface for object that can be serialized to a and from a DOM.
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public interface DOMable {
  /**
     Adds this object as a child of the parent.
     
     @param document The document where this object will be stored.
     @param parent   The parent element of this object.
  */
  public void toDOM(Document document, Element parent);
  
  /**
     Initializes this object from <code>element</code>.
     
     @param element The element containing the object state.
  */
  public void fromDOM(Element element);
}

/*
  $Log: DOMable.java,v $
  Revision 1.1  2002/07/11 12:03:48  ohitz
  Initial checkin

  Revision 1.1  2000/07/14 13:52:24  schubige
  Added DOMable
  
*/
