/*
 * @(#)BasicToolBarUI.java	1.59 00/02/02
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package iiuf.swing;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.*;
import java.awt.IllegalComponentStateException;

import java.beans.*;

import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicToolBarUI;

/**
   JWindow based ToolBarUI. based on sun's BasicToolBarUI<p>
   
   A Basic L&F implementation of ToolBarUI.  This implementation 
   is a "combined" view/controller.
   <p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @author Georges Saab
   @author Jeff Shapiro
   @version $Name:  $ $Revision: 1.1 $
*/

public class JWindowToolBarUI 
  extends
  BasicToolBarUI
  implements 
  SwingConstants 
{
  protected JToolBar toolBar;
  private boolean floating;
  private int floatingX;
  private int floatingY;
  private JWindow floatingFrame;
  protected DragWindow dragWindow;
  private Container dockingSource;
  protected int focusedCompIndex = -1;
  
  protected Color dockingColor = null;
  protected Color floatingColor = null;
  protected Color dockingBorderColor = null;
  protected Color floatingBorderColor = null;
  
  protected MouseInputListener dockingListener;
  protected PropertyChangeListener propertyListener;
  
  protected ContainerListener toolBarContListener;
  protected FocusListener toolBarFocusListener;
  
  /**
   * As of Java 2 platform v1.3 this previously undocumented field is no
   * longer used.
   * Key bindings are now defined by the LookAndFeel, please refer to
   * the key bindings specification for further details.
   *
   * @deprecated As of Java 2 platform v1.3.
   */
  protected KeyStroke upKey;
  /**
   * As of Java 2 platform v1.3 this previously undocumented field is no
   * longer used.
   * Key bindings are now defined by the LookAndFeel, please refer to
   * the key bindings specification for further details.
   *
   * @deprecated As of Java 2 platform v1.3.
   */
  protected KeyStroke downKey;
  /**
   * As of Java 2 platform v1.3 this previously undocumented field is no
   * longer used.
   * Key bindings are now defined by the LookAndFeel, please refer to
   * the key bindings specification for further details.
   *
   * @deprecated As of Java 2 platform v1.3.
   */
  protected KeyStroke leftKey;
  /**
   * As of Java 2 platform v1.3 this previously undocumented field is no
   * longer used.
   * Key bindings are now defined by the LookAndFeel, please refer to
   * the key bindings specification for further details.
   *
   * @deprecated As of Java 2 platform v1.3.
   */
  protected KeyStroke rightKey;
  
  
  private static String FOCUSED_COMP_INDEX = "JToolBar.focusedCompIndex";
  
  public static ComponentUI createUI( JComponent c )
  {
    return new JWindowToolBarUI();
  }
  
  public void installUI( JComponent c )
  {
    toolBar = (JToolBar) c;
    
    // Set defaults
    installDefaults();
    installComponents();
    installListeners();
    installKeyboardActions();

        // Initialize instance vars
    floating = false;
    floatingX = floatingY = 0;
    floatingFrame = null;

    setOrientation( toolBar.getOrientation() );
    c.setOpaque(true);

    if ( c.getClientProperty( FOCUSED_COMP_INDEX ) != null )
      {
	focusedCompIndex = ( (Integer) ( c.getClientProperty( FOCUSED_COMP_INDEX ) ) ).intValue();
      }
  }
    
  public void uninstallUI( JComponent c )
  {

    // Clear defaults
    uninstallDefaults();
    uninstallComponents();
    uninstallListeners();
    uninstallKeyboardActions();

    // Clear instance vars
    if (isFloating() == true)
      setFloating(false, null);

    floatingFrame = null;
    dragWindow = null;
    dockingSource = null;

    c.putClientProperty( FOCUSED_COMP_INDEX, new Integer( focusedCompIndex ) );
  }

  protected void installDefaults( )
  {
    LookAndFeel.installBorder(toolBar,"ToolBar.border");	
    LookAndFeel.installColorsAndFont(toolBar,
				     "ToolBar.background",
				     "ToolBar.foreground",
				     "ToolBar.font");
    // Toolbar specific defaults
    if ( dockingColor == null || dockingColor instanceof UIResource )
      dockingColor = UIManager.getColor("ToolBar.dockingBackground");
    if ( floatingColor == null || floatingColor instanceof UIResource )
      floatingColor = UIManager.getColor("ToolBar.floatingBackground");
    if ( dockingBorderColor == null || 
	 dockingBorderColor instanceof UIResource )
      dockingBorderColor = UIManager.getColor("ToolBar.dockingForeground");
    if ( floatingBorderColor == null || 
	 floatingBorderColor instanceof UIResource )
      floatingBorderColor = UIManager.getColor("ToolBar.floatingForeground");
  }

  protected void uninstallDefaults( )
  {
    LookAndFeel.uninstallBorder(toolBar);
    dockingColor = null;
    floatingColor = null;
    dockingBorderColor = null;
    floatingBorderColor = null;
  }

  protected void installComponents( )
  {
  }

  protected void uninstallComponents( )
  {
  }

  protected void installListeners( )
  {
    dockingListener = createDockingListener( );

    if ( dockingListener != null )
      {
	toolBar.addMouseMotionListener( dockingListener );
	toolBar.addMouseListener( dockingListener );
      }

    propertyListener = createPropertyListener();  // added in setFloating

    toolBarContListener = createToolBarContListener();

    if ( toolBarContListener != null )
      {
	toolBar.addContainerListener( toolBarContListener );
      }

    toolBarFocusListener = createToolBarFocusListener();

    if ( toolBarFocusListener != null )
      {
	// Put focus listener on all components in toolbar
	Component[] components = toolBar.getComponents();

	for ( int i = 0; i < components.length; ++i )
	  {
	    components[ i ].addFocusListener( toolBarFocusListener );
	  }
      }
  }

  protected void uninstallListeners( )
  {
    if ( dockingListener != null )
      {
	toolBar.removeMouseMotionListener(dockingListener);
	toolBar.removeMouseListener(dockingListener);

	dockingListener = null;
      }

    if ( propertyListener != null )
      {
	propertyListener = null;  // removed in setFloating
      }

    if ( toolBarContListener != null )
      {
	toolBar.removeContainerListener( toolBarContListener );
	toolBarContListener = null;
      }

    if ( toolBarFocusListener != null )
      {
	// Remove focus listener from all components in toolbar
	Component[] components = toolBar.getComponents();

	for ( int i = 0; i < components.length; ++i )
	  {
	    components[ i ].removeFocusListener( toolBarFocusListener );
	  }

	toolBarFocusListener = null;
      }
  }

  protected void installKeyboardActions( )
  {
    InputMap km = getInputMap(JComponent.
			      WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    SwingUtilities.replaceUIInputMap(toolBar, JComponent.
				     WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				     km);
    ActionMap am = getActionMap();

    if (am != null) {
      SwingUtilities.replaceUIActionMap(toolBar, am);
    }
  }

  InputMap getInputMap(int condition) {
    if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
      return (InputMap)UIManager.get("ToolBar.ancestorInputMap");
    }
    return null;
  }

  ActionMap getActionMap() {
    ActionMap map = (ActionMap)UIManager.get("ToolBar.actionMap");

    if (map == null) {
      map = createActionMap();
      if (map != null) {
	UIManager.put("ToolBar.actionMap", map);
      }
    }
    return map;
  }

  ActionMap createActionMap() {
    ActionMap map = new ActionMapUIResource();
    map.put("navigateRight", new RightAction());
    map.put("navigateLeft", new LeftAction());
    map.put("navigateUp", new UpAction());
    map.put("navigateDown", new DownAction());
    return map;
  }

  protected void uninstallKeyboardActions( )
  {
    SwingUtilities.replaceUIActionMap(toolBar, null);
    SwingUtilities.replaceUIInputMap(toolBar, JComponent.
				     WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				     null);
  }

  protected void navigateFocusedComp( int direction )
  {
    int nComp = toolBar.getComponentCount();
    int j;

    switch ( direction )
      {
      case EAST:
      case SOUTH:

	if ( focusedCompIndex < 0 || focusedCompIndex >= nComp ) break;

	j = focusedCompIndex + 1;

	while ( j != focusedCompIndex )
	  {
	    if ( j >= nComp ) j = 0;
	    Component comp = toolBar.getComponentAtIndex( j++ );

	    if ( comp != null && comp.isFocusTraversable() )
	      {
		comp.requestFocus();
		break;
	      }
	  }

	break;

      case WEST:
      case NORTH:

	if ( focusedCompIndex < 0 || focusedCompIndex >= nComp ) break;

	j = focusedCompIndex - 1;

	while ( j != focusedCompIndex )
	  {
	    if ( j < 0 ) j = nComp - 1;
	    Component comp = toolBar.getComponentAtIndex( j-- );

	    if ( comp != null && comp.isFocusTraversable() )
	      {
		comp.requestFocus();
		break;
	      }
	  }

	break;

      default:
	break;
      }
  }
  
  protected JWindow _createFloatingFrame(JToolBar toolbar) {
    JWindow frame = new JWindow() {
	public void paint(Graphics g) {
	  super.paint(g);
	  g.setColor(Color.black);
	  g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);	    
	}
      };
    WindowListener wl = createFrameListener();
    frame.addWindowListener(wl);
    return frame;
  }

  protected DragWindow _createDragWindow(JToolBar toolbar) {
    Window frame = null;
    if(toolBar != null) {
      Container p;
      for(p = toolBar.getParent() ; p != null && !(p instanceof Frame) ;
	  p = p.getParent());
      if(p != null && p instanceof Frame)
	frame = (Frame) p;
    }
    if(floatingFrame == null) {
      floatingFrame = _createFloatingFrame(toolBar);
    }
    frame = floatingFrame;

    DragWindow dragWindow = new DragWindow(frame);
    return dragWindow;
  }

  public Dimension getMinimumSize(JComponent c) {
    return getPreferredSize(c);
  }

  public Dimension getPreferredSize(JComponent c) {
    return null;
  }

  public Dimension getMaximumSize(JComponent c) {
    return getPreferredSize(c);
  }

  public void setFloatingLocation(int x, int y) {
    floatingX = x;
    floatingY = y;
  }
    
  public boolean isFloating() {
    return floating;
  }

  public void setFloating(boolean b, Point p) {
    if (toolBar.isFloatable() == true) {
      if (dragWindow != null)
	dragWindow.setVisible(false);
      this.floating = b;
      if (b == true)
	{
	  if (dockingSource == null)
	    {
	      dockingSource = toolBar.getParent();
	      dockingSource.remove(toolBar);
	    }
	  if ( propertyListener != null )
	    UIManager.addPropertyChangeListener( propertyListener );
	  if (floatingFrame == null)
	    floatingFrame = _createFloatingFrame(toolBar);
	  floatingFrame.getContentPane().add(toolBar,BorderLayout.CENTER);
	  floatingFrame.pack();
	  floatingFrame.setLocation(floatingX, floatingY);
	  floatingFrame.show();
	} else {
	  if (floatingFrame == null)
	    floatingFrame = _createFloatingFrame(toolBar);
	  floatingFrame.setVisible(false);
	  floatingFrame.getContentPane().remove(toolBar);
	  String constraint  = getDockingConstraint(dockingSource, p);
	  int    orientation = mapConstraintToOrientation(constraint);
	  setOrientation(orientation);
	  if (dockingSource== null)
	    dockingSource = toolBar.getParent();
	  if ( propertyListener != null )
	    UIManager.removePropertyChangeListener( propertyListener );
	  dockingSource.add(constraint, toolBar);
	}
      dockingSource.invalidate();
      Container dockingSourceParent = dockingSource.getParent();
      if (dockingSourceParent != null) 
	dockingSourceParent.validate();
      dockingSource.repaint();
    }
  }

  private int mapConstraintToOrientation(String constraint)
  {
    int orientation = toolBar.getOrientation();

    if ( constraint != null )
      {
	if ( constraint.equals(BorderLayout.EAST) || constraint.equals(BorderLayout.WEST) )
	  orientation = JToolBar.VERTICAL;
	else if ( constraint.equals(BorderLayout.NORTH) || constraint.equals(BorderLayout.SOUTH) )
	  orientation = JToolBar.HORIZONTAL;
      }

    return orientation;
  }

  public void setOrientation(int orientation)
  { 
    if(orientation == toolBar.getOrientation()) return;
    
    toolBar.setOrientation(orientation);
    
    if(dragWindow != null)
      dragWindow.setOrientation(orientation);
    
    if(floatingFrame != null) {
      if(floatingFrame.isShowing()) {
	floatingFrame.pack();
      }
    }    
  }
    
  /**
     * Gets the color displayed when over a docking area
     */
  public Color getDockingColor() {
    return dockingColor;
  }
    
  /**
     * Sets the color displayed when over a docking area
     */
  public void setDockingColor(Color c) {
    this.dockingColor = c;
  }

  /**
     * Gets the color displayed when over a floating area
     */
  public Color getFloatingColor() {
    return floatingColor;
  }

  /**
     * Sets the color displayed when over a floating area
     */
  public void setFloatingColor(Color c) {
    this.floatingColor = c;
  }
  
  public boolean canDock(Component c, Point p) {
    // System.out.println("Can Dock: " + p);
    return c.contains(p);
  }
  
  private String getDockingConstraint(Component c, Point p) {
    String result = BorderLayout.NORTH;
    boolean trBL = p.y * c.getWidth() < p.x * c.getHeight();
    boolean tlBR = p.y  < c.getHeight() + (p.x * -(c.getHeight()) / c.getWidth());
    if(trBL && tlBR)
      result = BorderLayout.NORTH;
    if(trBL && !tlBR)
      result = BorderLayout.EAST;
    if(!trBL && tlBR)
      result = BorderLayout.WEST;
    if(!trBL && !tlBR)
      result = BorderLayout.SOUTH;
    // System.out.println(result);
    return result;
  }
  
  protected void dragTo(Point position, Point offset_, Point origin) {
    if (toolBar.isFloatable() == true)
      {
	try
	  {
	    if(dragWindow == null) {
	      dragWindow = _createDragWindow(toolBar);
	    }
	    Point offset = dragWindow.getOffset();
	    if(offset == null) {
	      Dimension size = toolBar.getPreferredSize();
	      offset = offset_;
	      if(dragWindow.getWidth() > 0 && offset.x > dragWindow.getWidth())
		offset.x = dragWindow.getWidth() / 2;
	      if(dragWindow.getHeight() > 0 &&offset.y > dragWindow.getHeight())
		offset.y = dragWindow.getHeight() / 2;
	      dragWindow.setOffset(offset);
	    }
	    
	    Point global = new Point(origin.x + position.x,
				     origin.y + position.y);
	    Point dragPoint = new Point(global.x - offset.x, 
					global.y - offset.y);
	    if (dockingSource == null)
	      dockingSource = toolBar.getParent();
	    
	    Point dockingPosition = dockingSource.getLocationOnScreen();
	    Point comparisonPoint = new Point(global.x-dockingPosition.x,
					      global.y-dockingPosition.y);
	    if (canDock(dockingSource, comparisonPoint)) {
	      dragWindow.setBackground(getDockingColor());	
	      String constraint  = getDockingConstraint(dockingSource, comparisonPoint);
	      int    orientation = mapConstraintToOrientation(constraint);
	      dragWindow.setOrientation(orientation);
	      dragWindow.setBorderColor(dockingBorderColor);
	    } else {
	      dragWindow.setBackground(getFloatingColor());
	      dragWindow.setBorderColor(floatingBorderColor);
	    }

	    dragWindow.setLocation(dragPoint.x, dragPoint.y);
	    if (dragWindow.isVisible() == false) {
	      Dimension size = toolBar.getPreferredSize();
	      dragWindow.orientation = toolBar.getOrientation();
	      dragWindow.setSize(size.width, size.height);
	      dragWindow.show();
	    }
	  }
	catch ( IllegalComponentStateException e )
	  {
	  }
      }
  }

  protected void floatAt(Point position, Point origin)
  {
    if(toolBar.isFloatable() == true)
      {
	try
	  {
	    Point offset = dragWindow.getOffset();
	    if (offset == null) {
	      offset = position;
	      dragWindow.setOffset(offset);
	    }
	    Point global = new Point(origin.x+ position.x,
				     origin.y+position.y);
	    setFloatingLocation(global.x-offset.x, 
				global.y-offset.y);
	    if (dockingSource != null) { 
	      Point dockingPosition = dockingSource.getLocationOnScreen();
	      Point comparisonPoint = new Point(global.x-dockingPosition.x,
						global.y-dockingPosition.y);
	      if (canDock(dockingSource, comparisonPoint)) {
		setFloating(false, comparisonPoint);
	      } else {
		setFloating(true, null);
	      }
	    } else {
	      setFloating(true, null);
	    }
	    dragWindow.setOffset(null);
	  }
	catch ( IllegalComponentStateException e )
	  {
	  }
      }
  }

  protected ContainerListener createToolBarContListener( )
  {
    return new ToolBarContListener( );
  }

  protected FocusListener createToolBarFocusListener( )
  {
    return new ToolBarFocusListener( );
  }

  protected PropertyChangeListener createPropertyListener()
  {
    return new PropertyListener();
  }

  protected MouseInputListener createDockingListener( ) {
    return new DockingListener(toolBar);
  }
    
  protected WindowListener createFrameListener() {
    return new FrameListener();
  }

  // The private inner classes below should be changed to protected the
  // next time API changes are allowed.
  
  private static abstract class KeyAction extends AbstractAction {
    public boolean isEnabled() { 
      return true;
    }
  }

  private static class RightAction extends KeyAction {
    public void actionPerformed(ActionEvent e) {
      JToolBar toolBar = (JToolBar)e.getSource();
      JWindowToolBarUI ui = (JWindowToolBarUI)toolBar.getUI();
      ui.navigateFocusedComp(EAST);
    }
  }
    
  private static class LeftAction extends KeyAction {
    public void actionPerformed(ActionEvent e) {
      JToolBar toolBar = (JToolBar)e.getSource();
      JWindowToolBarUI ui = (JWindowToolBarUI)toolBar.getUI();
      ui.navigateFocusedComp(WEST);
    }
  }

  private static class UpAction extends KeyAction {
    public void actionPerformed(ActionEvent e) {
      JToolBar toolBar = (JToolBar)e.getSource();
      JWindowToolBarUI ui = (JWindowToolBarUI)toolBar.getUI();
      ui.navigateFocusedComp(NORTH);
    }
  }

  private static class DownAction extends KeyAction {
    public void actionPerformed(ActionEvent e) {
      JToolBar toolBar = (JToolBar)e.getSource();
      JWindowToolBarUI ui = (JWindowToolBarUI)toolBar.getUI();
      ui.navigateFocusedComp(SOUTH);
    }
  }

  protected class FrameListener extends WindowAdapter {
    public void windowClosing(WindowEvent w) {	    
      setFloating(false, null);
    }

  } 

  protected class ToolBarContListener implements ContainerListener
  {
    public void componentAdded( ContainerEvent e )
    {
      Component c = e.getChild();

      if ( toolBarFocusListener != null )
	{
	  c.addFocusListener( toolBarFocusListener );
	}
    }

    public void componentRemoved( ContainerEvent e )
    {
      Component c = e.getChild();

      if ( toolBarFocusListener != null )
	{
	  c.removeFocusListener( toolBarFocusListener );
	}
    }

  } // end class ToolBarContListener

  protected class ToolBarFocusListener implements FocusListener
  {
    public void focusGained( FocusEvent e )
    {
      Component c = e.getComponent();

      focusedCompIndex = toolBar.getComponentIndex( c );
    }

    public void focusLost( FocusEvent e )
    {
    }

  } // end class ToolBarFocusListener

  protected class PropertyListener implements PropertyChangeListener
  {
    public void propertyChange( PropertyChangeEvent e )
    {
      if ( e.getPropertyName().equals("lookAndFeel") )
	{
	  toolBar.updateUI();
	}
    }
  }

  /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of JWindowToolBarUI.
     */
  public class DockingListener implements MouseInputListener {
    protected JToolBar toolBar;
    protected boolean isDragging = false;
    protected Point origin = null;
    protected Point offset = null;

    public DockingListener(JToolBar t) {
      this.toolBar = t;
    } 
    
    public void mouseClicked(MouseEvent e) {
      if(e.getClickCount() == 2) {
	if(toolBar.getOrientation() == VERTICAL)
	  setOrientation(HORIZONTAL);
	else if(toolBar.getOrientation() == HORIZONTAL)
	  setOrientation(VERTICAL);
      }
    }

    public void mousePressed(MouseEvent e) { 
      if (!toolBar.isEnabled()) {
	return;
      }
      isDragging = false;
      offset = e.getPoint();
    }
    public void mouseReleased(MouseEvent e) {
      if (!toolBar.isEnabled()) {
	return;
      }
      if (isDragging == true) {
	Point position = e.getPoint();
	if (origin == null)
	  origin = e.getComponent().getLocationOnScreen();
	floatAt(position, origin);
      }
      origin = null;
      isDragging = false;
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    public void mouseDragged(MouseEvent e) {
      if (!toolBar.isEnabled()) {
	return;
      }
      isDragging = true;
      Point position = e.getPoint();
      if (origin == null)
	origin = e.getComponent().getLocationOnScreen();
      dragTo(position, offset, origin);
    }
    public void mouseMoved(MouseEvent e) {
    }
  }

  protected class DragWindow extends Window
  {
    Color borderColor = Color.gray;
    int   orientation = toolBar.getOrientation();
    Point offset; // offset of the mouse cursor inside the DragWindow
    
    Dimension oh;
    Dimension ov;

    DragWindow(Window f) {
      super(f);
    }
    
    public void setOrientation(int o) {
      if (o == this.orientation)
	return;	    
      this.orientation = o;
      Dimension size;
      int ot = toolBar.getOrientation();
      if(o == VERTICAL) {
	if(ov == null) {
	  toolBar.setOrientation(VERTICAL);
	  ov = toolBar.getPreferredSize();
	}
	size = ov;
      } else {
	if(oh == null) {
	  toolBar.setOrientation(HORIZONTAL);
	  oh = toolBar.getPreferredSize();
	}
	size = oh;
      }
      toolBar.setOrientation(ot);
      setSize(size);
      if(isShowing()) {
	if (offset!=null) {
	  if( toolBar.getComponentOrientation().isLeftToRight() ) {
	    setOffset(new Point(Math.min(offset.y, size.width - 5), Math.min(offset.x, size.height - 5)));
	  } else if( o == JToolBar.HORIZONTAL ) {
	    setOffset(new Point(size.height-offset.y, Math.min(offset.x, size.height - 5)));
	  } else {
	    setOffset(new Point(Math.min(offset.y, size.width - 5), size.width-offset.x));
	  }
	}
	repaint();
      }
    }

    public Point getOffset() {
      return offset;
    }

    public void setOffset(Point p) {
      this.offset = p;
    }
	
    public void setBorderColor(Color c) {
      if (this.borderColor == c)
	return;
      this.borderColor = c;
      repaint();
    }

    public Color getBorderColor() {
      return this.borderColor;
    }

    public void paint(Graphics g) {
      Color temp = g.getColor();
      g.setColor(getBackground());	    
      Dimension size = getSize();
      g.fillRect(0,0,size.width, size.height);	    
      g.setColor(getBorderColor());
      g.drawRect(0,0,size.width-1, size.height-1);	    
      g.setColor(temp);
      super.paint(g);
    }
    
    public Insets getInsets() {
      return new Insets(1,1,1,1);
    }
  }
}

/*
  $Log: JWindowToolBarUI.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/03/05 17:55:07  schubige
  Still working on soundium properties panel

  Revision 1.3  2001/03/05 07:46:43  schubige
  Working on soundium properties panel, some sourcewatch extensions.

  Revision 1.2  2001/03/02 17:51:16  schubige
  Enhanced sourcewatch and worked on soundium properties panel

  Revision 1.1  2001/03/01 13:29:22  schubige
  interim checkin for soundium
  
*/
