package iiuf.awt;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import iiuf.util.Preferences;

/**
   Standalone apple environment.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class AppletFrame
  extends 
  Frame 
  implements
  WindowListener,
  ActionListener {

  private transient Applet   applet;
  
  public AppletFrame(String title, 
		     Applet applet_, 
		     int width, int height) {
    super("applet", title);
    addWindowListener(this);
    applet = applet_;
    
    MenuBar  menubar = new MenuBar();
    Menu     file    = new Menu("File", true);
    MenuItem quit    = new MenuItem("Quit", new MenuShortcut(KeyEvent.VK_Q));

    setBackground(Color.lightGray);

    quit.addActionListener(this);
    
    menubar.add(file);
    file.add(quit);
    setMenuBar(menubar);
    
    add("Center", applet);

    setSize(width, height);

    applet.init();

    setVisible(true);
    
    validate();

    applet.active = true;
    applet.start();

    validate();
  }
  
  public void actionPerformed(ActionEvent e) {
    exit();
  }
  
  private void exit() {
    applet.stop();
    applet.active = false;
    setVisible(false);
    Preferences.store();
    Applet.exit(0);
  }

  public void windowOpened     (WindowEvent e) {}
  public void windowClosing    (WindowEvent e) {exit();}
  public void windowClosed     (WindowEvent e) {}
  public void windowIconified  (WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowActivated  (WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {}
}
/*
  $Log: AppletFrame.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.3  1999/11/26 10:00:26  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***

  Revision 1.2  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes

  Revision 1.1  1999/09/14 11:51:55  schubige
  Added applet frame classes

*/
