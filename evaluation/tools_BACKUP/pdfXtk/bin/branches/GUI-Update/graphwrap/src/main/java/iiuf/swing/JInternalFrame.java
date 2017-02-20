package iiuf.swing;

import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;

import iiuf.util.Util;
import iiuf.util.PreferencesHandler;

/**
   Preferences aware JInternalFrame.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
     */
public class JInternalFrame
  extends
  javax.swing.JInternalFrame
{
  private transient Icon                  normalIcon;
  private transient Icon                  iconifiedIcon;
  private transient boolean               inited;
  private transient Rectangle             dBounds;
  private transient Rectangle             dIBounds;
    
  /** 
   * Creates a non-resizable, non-closable, non-maximizable,
   * non-iconifiable JInternalFrame with no title.
   */
  public JInternalFrame() {
    this("", false, false, false, false);
  }
  
  /** 
   * Creates a non-resizable, non-closable, non-maximizable,
   * non-iconifiable JInternalFrame with the specified title.
   *
   * @param title  the String to display in the title bar.
   */
  public JInternalFrame(String title) {
    this(title, false, false, false, false);
  }
  
  /** 
   * Creates a non-closable, non-maximizable, non-iconifiable 
   * JInternalFrame with the specified title and with resizability 
   * specified.
   *
   * @param title      the String to display in the title bar.
   * @param resizable  if true, the frame can be resized
   */
  public JInternalFrame(String title, boolean resizable) {
    this(title, resizable, false, false, false);
  }
  
  /** 
   * Creates a non-maximizable, non-iconifiable JInternalFrame with the
   * specified title and with resizability and closability specified.
   *
   * @param title      the String to display in the title bar.
   * @param resizable  if true, the frame can be resized
   * @param closable   if true, the frame can be closed
   */
  public JInternalFrame(String title, boolean resizable, boolean closable) {
    this(title, resizable, closable, false, false);
  }
  
  /** 
   * Creates a non-iconifiable JInternalFrame with the specified title 
   * and with resizability, closability, and maximizability specified.
   *
   * @param title       the String to display in the title bar.
   * @param resizable   if true, the frame can be resized
   * @param closable    if true, the frame can be closed
   * @param maximizable if true, the frame can be maximized
   */
  public JInternalFrame(String title, boolean resizable, boolean closable,
			boolean maximizable) {
    this(title, resizable, closable, maximizable, false);
  }
  
  /** 
   * Creates a JInternalFrame with the specified title and 
   * with resizability, closability, maximizability, and iconifiability
   * specified.
   *
   * @param title       the String to display in the title bar.
   * @param resizable   if true, the frame can be resized
   * @param closable    if true, the frame can be closed
   * @param maximizable if true, the frame can be maximized
   * @param iconifiable if true, the frame can be iconified
   */
  public JInternalFrame(String title, boolean resizable, boolean closable, 
			boolean maximizable, boolean iconifiable) {
    super(title, resizable, closable, maximizable, iconifiable);
    iconifiedIcon = normalIcon = getFrameIcon();
    setVisible(false);
  }
  
  public void setFrameIcon(Icon icon) {
    setNormalFrameIcon(icon);
    setIconifiedFrameIcon(icon);
  }
  
  public void setNormalFrameIcon(Icon icon) {
    normalIcon = icon;
    if(!isIcon()) super.setFrameIcon(icon);
  }
  
  public void setIconifiedFrameIcon(Icon icon) {
    iconifiedIcon = icon;
    if(isIcon()) super.setFrameIcon(icon);
  }
  
  public void setIcon(boolean state) 
    throws java.beans.PropertyVetoException {
    if(state) super.setFrameIcon(iconifiedIcon);
    else      super.setFrameIcon(normalIcon);
    super.setIcon(state);
  }
}

/*
  $Log: JInternalFrame.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.3  2000/10/17 15:35:59  schubige
  Added watcher preferences

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added
  
*/
