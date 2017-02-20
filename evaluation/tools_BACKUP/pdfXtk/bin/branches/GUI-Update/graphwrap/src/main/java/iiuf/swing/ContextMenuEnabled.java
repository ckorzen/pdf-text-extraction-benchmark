package iiuf.swing;

import java.awt.Point;
import java.awt.Component;

/**
   Interface for context menu enabled components.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface ContextMenuEnabled {
  /**
     Converts a location to an object or null.
     
     @param component The component over which the context menu popup (MousePressed) event occured.
     @param location the location of the context menu (MousePressed.getPoint()).
     
     @return The object at this location or null.
   */
  Object locationToObject(Component component, Point location);

  /**
     Get the context menu enabled component, usually the component that implments this interface.

     @return The context menu enabled component.
   */
  Component getComponent();

  /**
     Informs this component that the manager changed.
     
     @param manager The new conetext menu manger for this component.
  */
  void setContextMenuManager(ContextMenuManager manager);
}

/*
  $Log: ContextMenuEnabled.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff
  
*/
