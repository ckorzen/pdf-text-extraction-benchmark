package iiuf.awt;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import iiuf.util.PreferencesHandler;

/**
   Preferences aware frame.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Frame 
  extends
  java.awt.Frame 
{
  
  private static    final String        P_BOUNDS = "bounds";
  private transient PreferencesHandler  prefs;
  private transient boolean             exit_on_hide;
  private transient boolean             inited;

  /**
     For test purpose only.
  */
  Frame(Component cmp, boolean exit_on_hide_) {
    super(cmp.getClass().getName());
    exit_on_hide = exit_on_hide_;
    setLayout(new GridBagLayout());
    add(cmp, Awt.constraints(true, 10, 10, GridBagConstraints.BOTH));
    pack();
    setVisible(true);
  }

  public Frame() {
    super();
  }

  public Frame(String title) {
    super(title);
  }
  
  public Frame(String pref_id, String title) {
    super(title);
    prefs = new PreferencesHandler(getClass(), pref_id);
  }
  
  private void init(Rectangle bounds) {
    if(inited) return;
    inited = true;

    boolean resizeable = isResizable();
    setResizable(true);
    if(prefs != null)
      super.setBounds((Rectangle)prefs.get(P_BOUNDS, bounds));
    setResizable(resizeable);
    addWindowListener(new WindowListener() {
	public void windowOpened     (WindowEvent e) {capture();}
	public void windowClosing    (WindowEvent e) {capture(); setVisible(false);}
	public void windowClosed     (WindowEvent e) {capture();}
	public void windowIconified  (WindowEvent e) {capture();}
	public void windowDeiconified(WindowEvent e) {capture();}
	public void windowActivated  (WindowEvent e) {capture();}
	public void windowDeactivated(WindowEvent e) {capture();}
      });
  }
  
  public void setSize(int width, int height) {
    super.setSize(width, height);
    capture();
  }
  
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    capture();
  }
  
  public void setLocation(int x, int y) {
    super.setLocation(x, y);
    capture();
  }
  
  public void setVisible(boolean state) {
    if(state)
      init(getBounds());
    super.setVisible(state);
    if(exit_on_hide && !state) System.exit(0);
  }
  
  private void capture() {
    if(prefs != null && isVisible())  prefs.set(P_BOUNDS, getBounds());
  }
}
/*
  $Log: Frame.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/08/17 16:22:13  schubige
  Swing cleanup & TreeView added

  Revision 1.3  1999/11/26 10:00:34  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.2  1999/09/14 11:59:39  schubige
  Added @serial and transient for javadoc

  Revision 1.1  1999/09/14 11:51:55  schubige
  Added applet frame classes
  
*/
