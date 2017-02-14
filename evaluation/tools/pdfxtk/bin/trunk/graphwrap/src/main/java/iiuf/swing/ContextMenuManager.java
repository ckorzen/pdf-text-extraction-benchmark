package iiuf.swing;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
   Context menu manager implementation.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ContextMenuManager
  implements
  MouseListener
{
  private ContextMenuEnabled cme;
  private ArrayList          menusa    = new ArrayList();
  private ContextMenu[]      menus     = new ContextMenu[0];
  private HashMap            menuCache = new HashMap();
  private Object             lastObject;

  /**
     Create a new context menu manager.
  */
  public ContextMenuManager() {
    this(null);
  }

  /**
     Create a new context menu manager for the component <code>cmp</code>.

     @param cmp The component managed by this manager.
   */
  public ContextMenuManager(ContextMenuEnabled cmp) {
    if(cmp != null)
      setComponent(cmp);
  }
  
  private void setupML(Component cmp, boolean add) {
    if(cmp instanceof Container) { 
      Component[] cmps = ((Container)cmp).getComponents();
      for(int i = 0; i < cmps.length; i++)
	if(cmps[i] instanceof Component) 
	  setupML(cmps[i], add);
      
    } else if(cmp instanceof JScrollPane) {
      if(((JScrollPane)cmp).getViewport().getView() instanceof Component)
	setupML(((JScrollPane)cmp).getViewport().getView(), add);
      return;
    }
    if(add)
      cmp.addMouseListener(this);
    else
      cmp.removeMouseListener(this);
  }
  
  /**
     Set the managed component to <code>cmp</code>.
     
     @param cmp The component managed by this manager.
  */  
  public void setComponent(ContextMenuEnabled cmp) {
    removeComponent();
    cme = cmp;
    setupML(cme.getComponent(), true);
    cme.setContextMenuManager(this);
  }
  
  /**
     Get the managed component.
     
     @returns The component managed by this manager.
  */  
  public ContextMenuEnabled getComponent() {
    return cme;
  }
  
  /**
     Remove the managed component.
  */  
  public void removeComponent() {
    if(cme == null) return;
    Object[] popups = menuCache.values().toArray();
    for(int i = 0; i < popups.length; i++)
      if(cme.getComponent() instanceof Container)
	((Container)cme.getComponent()).remove((JPopupMenu)popups[i]);
    menuCache = new HashMap();
    setupML(cme.getComponent(), false);
    cme = null;
  }
  
  /**
     Register a context menu with this manager.
     The menu will be added to the tail of the registred menus.

     @param menu The menu to register.
   */
  public void addContextMenu(ContextMenu menu) {
    addContextMenu(menu, false);
  }
  
  /**
     Register a context menu with this manager.

     @param menu    The menu to register.
     @param asFirst Add this menu as first menu in popup.
   */
  public void addContextMenu(ContextMenu menu, boolean asFirst) {
    if(asFirst)
      menusa.add(0, menu);
    else
      menusa.add(menu);
    menus = (ContextMenu[])menusa.toArray(new ContextMenu[menusa.size()]);
  }

  /**
     Deregister a context menu with this manager.
     
     @param menu The menu to register.
   */
  public void removeContextMenu(ContextMenu menu) {
    menusa.remove(menu);
    menus = (ContextMenu[])menusa.toArray(new ContextMenu[menusa.size()]);
  }
  
  public void mouseClicked(MouseEvent e)  {}
  public void mouseEntered(MouseEvent e)  {}
  public void mouseExited(MouseEvent e)   {}
  public void mouseReleased(MouseEvent e) {handlePopUp(e);}
  public void mousePressed(MouseEvent e)  {handlePopUp(e);}
  
  private void handlePopUp(MouseEvent e) {
    if(!e.isPopupTrigger()) return;
    lastObject = cme.locationToObject(e.getComponent(), e.getPoint());
    if(lastObject == null) return;
    boolean[] menubm = new boolean[menus.length];
    int lastitem = -1;
    for(int i = 0; i < menus.length; i++) {
      menubm[i] = menus[i].check(lastObject);
      if(menubm[i]) lastitem = i;
    }
    JPopupMenu popup = (JPopupMenu)menuCache.get(menubm);
    if(popup == null) {
      popup = new JPopupMenu();
      for(int i = 0; i < menubm.length; i++)
	if(menubm[i]) {
	  JMenuItem[] mi = menus[i].getItems();
	  for(int j = 0; j < mi.length; j++)
	    popup.add(mi[j]);
	  if(i != lastitem) popup.addSeparator();
	}
      menuCache.put(menubm, popup);
      if(cme instanceof Container)
	((Container)cme.getComponent()).add(popup);
    }
    lastLocation = e.getPoint();
    if(popup.getComponentCount() > 0)
      popup.show(e.getComponent(), lastLocation.x, lastLocation.y);
  }

  private Point lastLocation = new Point();
  /**
     Get last menu popup location.
     
     @return Last location of popup menu or (0,0) if no menu did popup yet.
  */
  public Point getLastMenuLocation() {
    return lastLocation;
  }
  
  /**
     Get object under last menu location.
     
     @return Last object under the menu location or null.
  */
  public Object getLastObject() {
    return lastObject;
  }  
}

/*
  $Log: ContextMenuManager.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.7  2001/03/11 17:59:38  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.6  2001/02/11 16:25:39  schubige
  working on soundium

  Revision 1.5  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.4  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.2  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.1  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff
  
*/
