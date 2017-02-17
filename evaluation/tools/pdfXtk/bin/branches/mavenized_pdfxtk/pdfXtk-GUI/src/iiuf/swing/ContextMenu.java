package iiuf.swing;

import javax.swing.JMenuItem;

/**
   Interface for context menus that can be registred with ContextMenuManagers.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface ContextMenu {
  public boolean     check(Object obj);
  public JMenuItem[] getItems();
}

/*
  $Log: ContextMenu.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff
  
*/
