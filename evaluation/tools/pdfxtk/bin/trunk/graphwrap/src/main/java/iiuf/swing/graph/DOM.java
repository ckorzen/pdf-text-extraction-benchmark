package iiuf.swing.graph;

import iiuf.util.NotImplementedException;
import iiuf.dom.DOMManager;
import iiuf.dom.DOMContext;
import iiuf.dom.DOMHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
   DOM handlers for graph visual objects.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class DOM {
  private static boolean inited;
  
  public static synchronized void init() {
    if(inited) return;
    inited = true;

    DOMManager.register(GraphEdge.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  	 
	  return object;
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  return element;
	}

	public int getVersion() {return 0;}
      });
  }
}

/*
  $Log: DOM.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/03/28 21:31:18  schubige
  dom save and load works now (very early version)

  Revision 1.3  2001/03/28 18:44:30  schubige
  working on dom again

  Revision 1.2  2001/03/26 15:35:32  schubige
  fixed format bug

  Revision 1.1  2001/03/22 16:08:48  schubige
  more work on dom stuff

  Revision 1.2  2001/03/21 22:18:14  schubige
  working on dom stuff

  Revision 1.1  2001/03/21 19:37:42  schubige
  started with dom stuff
  
*/
