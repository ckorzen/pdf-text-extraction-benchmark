package iiuf.awt;

import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import iiuf.util.PreferencesHandler;

/**
   Preferences aware dialog.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
   @see java.awt.Dialog
*/
abstract public class Dialog
  extends
  java.awt.Dialog
{
  public final static int DIALOG_CANCEL = 0;
  
  private static    final String        P_LOCATION = "location";
  private transient PreferencesHandler  prefs;
  
  public Dialog(String pref_id, Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    prefs = new PreferencesHandler(getClass(), pref_id);
    addWindowListener(new WindowListener() {
	public void windowOpened     (WindowEvent e) {capture();}
	public void windowClosing    (WindowEvent e) {capture(); close(DIALOG_CANCEL);}
	public void windowClosed     (WindowEvent e) {capture();}
	public void windowIconified  (WindowEvent e) {capture();}
	public void windowDeiconified(WindowEvent e) {capture();}
	public void windowActivated  (WindowEvent e) {capture();}
	public void windowDeactivated(WindowEvent e) {capture();}
      });
  }
  
  public Dialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    addWindowListener(new WindowListener() {
	public void windowOpened     (WindowEvent e) {}
	public void windowClosing    (WindowEvent e) {close(DIALOG_CANCEL);}
	public void windowClosed     (WindowEvent e) {}
	public void windowIconified  (WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated  (WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
      });
  }

  private Point defaultLocation() {
    Dimension screen = getToolkit().getScreenSize();
    Dimension size   = getSize();
    
    return new Point((screen.width  - size.width) / 2,
		     (screen.height - size.height) / 3);
    
  }
  
  public void setVisible(boolean visible) {
    if(visible) {
      setResizable(true);
      Point location = defaultLocation();
      if(prefs != null)
	location = (Point)prefs.get(P_LOCATION, location);
      setLocation(location);
      setResizable(false);
    }
    super.setVisible(visible);
  }
  
  public void close(int exit_code) {
    if(done(exit_code))
      setVisible(false);
  }
  
  public void pack() {
    setResizable(true);    
    super.pack();
    setResizable(false);
  }

  public abstract boolean done(int exit_code);

  private void capture() {
    if(prefs != null)
      prefs.set(P_LOCATION, getLocation());
  }
}
/*
  $Log: Dialog.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.3  1999/11/26 10:00:28  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***

  Revision 1.5  1999/09/14 11:59:39  schubige
  Added @serial and transient for javadoc

  Revision 1.4  1999/09/14 11:48:13  schubige
  Updated some preferences realted classes

  Revision 1.3  1999/09/10 06:54:19  schubige
  Dialogs & requesters are now placed at 1/2 x and 1/3 y of the screen.

  Revision 1.2  1999/09/09 14:57:54  juillera
  Updated for new iiuf.util.Dialog

  Revision 1.1  1999/09/09 14:32:12  schubige
  Added Line, DateChooser and Dialog
  
*/
