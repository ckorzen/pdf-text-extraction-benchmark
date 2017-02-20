package iiuf.awt;

import java.awt.Component;
import java.awt.Container;

/**
   Our version of border layout that allows child access.
   Use this if you want preferences support for JToolBars.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
     */
public class BorderLayout
  extends
  java.awt.BorderLayout
{
  Component north;
  Component south;
  Component east;
  Component west;
  Component center;
  Component firstLine;
  Component firstItem;
  Component lastItem;
  Component lastLine;

  public void addLayoutComponent(String name, Component comp) {
    super.addLayoutComponent(name, comp);
    synchronized (comp.getTreeLock()) {
      /* Special case:  treat null the same as "Center". */
      if (name == null) {
	name = CENTER;
      }
      
      /* Assign the component to one of the known regions of the layout.
       */
      if (CENTER.equals(name)) 
	center = comp;
      else if (NORTH.equals(name)) 
	north = comp;
      else if (SOUTH.equals(name)) 
	south = comp;
      else if (EAST.equals(name)) 
	east = comp;
      else if (WEST.equals(name)) 
	west = comp;
      else if (BEFORE_FIRST_LINE.equals(name)) 
	firstLine = comp;
      else if (AFTER_LAST_LINE.equals(name)) 
	lastLine = comp;
      else if (BEFORE_LINE_BEGINS.equals(name)) 
	firstItem = comp;
      else if (AFTER_LINE_ENDS.equals(name)) 
	lastItem = comp;      
    }
  }
  
  /**
   * Removes the specified component from this border layout. This
   * method is called when a container calls its <code>remove</code> or
   * <code>removeAll</code> methods. Most applications do not call this
   * method directly.
   * @param   comp   the component to be removed.
   * @see     java.awt.Container#remove(java.awt.Component)
   * @see     java.awt.Container#removeAll()
   */
  public void removeLayoutComponent(Component comp) {
    super.removeLayoutComponent(comp);
    synchronized (comp.getTreeLock()) {
      if (comp == center) {
	center = null;
      } else if (comp == north) {
	north = null;
      } else if (comp == south) {
	south = null;
      } else if (comp == east) {
	east = null;
      } else if (comp == west) {
	west = null;
      }
      if (comp == firstLine) {
	firstLine = null;
      } else if (comp == lastLine) {
	lastLine = null;
      } else if (comp == firstItem) {
	firstItem = null;
      } else if (comp == lastItem) {
	lastItem = null;
      }
    }
  }
  
  /**
   * Get the component that corresponds to the given constraint location
   *
   * @param   Container The container with this layout.
   * @param   key       The desired absolute position,
   *                    either NORTH, SOUTH, EAST, or WEST.
   */
  public Component getChild(Container c, String key) {
    Component result = null;
    boolean   ltr    = c.getComponentOrientation().isLeftToRight();
    
    if (key == NORTH) {
      result = (firstLine != null) ? firstLine : north;
    }
    else if (key == SOUTH) {
      result = (lastLine != null) ? lastLine : south;
    }
    else if (key == WEST) {
      result = ltr ? firstItem : lastItem;
      if (result == null) {
	result = west;
      }
    }
    else if (key == EAST) {
      result = ltr ? lastItem : firstItem;
      if (result == null) {
	  result = east;
      }
    }
    else if (key == CENTER) {
      result = center;
    }
    if (result != null && !result.isVisible()) {
      result = null;
    }
    return result;
  }
}

/*
  $Log: BorderLayout.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.1  2001/03/01 13:29:22  schubige
  interim checkin for soundium
  
*/
