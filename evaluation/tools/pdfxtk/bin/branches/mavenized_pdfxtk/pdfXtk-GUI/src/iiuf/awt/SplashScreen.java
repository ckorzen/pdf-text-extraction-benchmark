package iiuf.awt;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.MediaTracker;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JWindow;

/**
   A splash screen with classloader.
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class SplashScreen 
  extends
  JWindow
{
  static SplashScreen screen;
  static String       infoStr;
  static int          infoX;
  static int          infoY;

  boolean     painted;
  ImageCanvas imgCanvas;

  class ImageCanvas extends Canvas {
    Image img;
    
    ImageCanvas(Image img_) throws InterruptedException {
      img = img_;
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(img, 0);
      mt.waitForAll();
      setSize(img.getWidth(this), img.getHeight(this));
    }

    public void update(Graphics g) {
      paint(g);
    }

    public void paint(Graphics g) {
      g.drawImage(img, 0, 0, this);
      g.setColor(Color.black);
      g.drawRect(getX(), getY(), getWidth() - 1, getHeight() - 1);
      g.drawRect(getX() + 1, getY() + 1, getWidth() - 3, getHeight() - 3);
      if(infoStr != null)
	g.drawString(infoStr, infoX, infoY);
      synchronized(SplashScreen.this) {
	painted = true;
	SplashScreen.this.notify();
      }
    }
  }
    
  void showLoadAndRun(Image img, String cls, String[] argv)
    throws Exception {
    Toolkit   tk  = Toolkit.getDefaultToolkit();
    Dimension d   = tk.getScreenSize();
    
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent evt) {
	  dispose(); 
	  System.exit(0);
	}});
    
    imgCanvas = new ImageCanvas(img);
    getContentPane().add(imgCanvas);
    pack();
    setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);
    setVisible(true);
    synchronized(this) {
      if(!painted)
	wait();
    }
    Class.forName(cls).getMethod("main", new Class[] {String[].class}).invoke(null, new Object[] {argv});
    dispose();
  }
 
  public static void setInfo(String info, int x, int y) {
    infoStr = info;
    infoX   = x;
    infoY   = y;
    if(screen != null) screen.imgCanvas.repaint(screen.imgCanvas.getX() + 2, 
						screen.imgCanvas.getY() + 2, 
						screen.imgCanvas.getWidth() - 5, 
						screen.imgCanvas.getHeight() - 5);
  }
  
  protected static void splash(String cls, String img, String[] argv) {
    screen = new SplashScreen();
    try {
      screen.showLoadAndRun(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource(img)), 
			    cls, 
			    argv);
    } catch(Exception e) {
      screen.dispose();
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  
  public static void main(String[] argv) {
    if(argv.length < 2) {
      System.out.println("usage: " + SplashScreen.class.getName() +
			 " <class to load> <image resource> [args...]");
      System.exit(1);
    }
    
    String[] argv2 = new String[argv.length - 2];
    System.arraycopy(argv, 2, argv2, 0, argv2.length);

    splash(argv[0], argv[1], argv2);
  }
}
/*
  $Log: SplashScreen.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.2  2001/02/27 21:43:36  schubige
  Switch to new SoundEngine interface

  Revision 1.1  2001/02/26 16:02:22  schubige
  Added splash screen
  
*/
