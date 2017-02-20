package iiuf.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
   Handler for DOMable classes.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface DOMHandler {
  public Object  fromDOM(DOMContext context, Element element, Object object);
  public Element toDOM(DOMContext context, Element element, Object object);
  public int     getVersion();
}
/*
  $Log: DOMHandler.java,v $
  Revision 1.1  2002/07/11 12:03:48  ohitz
  Initial checkin

  Revision 1.6  2001/03/28 21:31:18  schubige
  dom save and load works now (very early version)

  Revision 1.5  2001/03/28 18:44:33  schubige
  working on dom again

  Revision 1.4  2001/03/26 15:35:36  schubige
  fixed format bug

  Revision 1.3  2001/03/22 16:08:23  schubige
  more work on dom stuff

  Revision 1.2  2001/03/21 22:18:14  schubige
  working on dom stuff

  Revision 1.1  2001/03/21 19:37:45  schubige
  started with dom stuff
  
*/
