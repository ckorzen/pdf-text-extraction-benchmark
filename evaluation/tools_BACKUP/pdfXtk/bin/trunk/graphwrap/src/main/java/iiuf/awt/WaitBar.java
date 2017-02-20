package iiuf.awt;

import iiuf.util.Util;

/**
   A wait bar (non-advancing progress bar) implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class WaitBar 
extends
ProgressBar 
  implements
  Runnable {
  
  transient Thread  spinner;
  transient boolean running;  
  
  public WaitBar(int width) {
    super(width, 0);
    spinner = new Thread(this);
  }
  
  public void start() {
    step();
    while(running)
      Util.delay(100);
    spinner.start();
  }
  
  public void stop() {
    running = false;
    reset();
  }
  
  public void step() {
    step++;
    repaint();
  }
  
  public synchronized void run() {
    for(running = true; running;) { 
      try{wait(100);} catch(Exception e) {e.printStackTrace();}
      step();
    }
    running = false;
  }
  
  public void paint(java.awt.Graphics g) {
    java.awt.Dimension d = getSize();
    int width            = d.width;
    int height           = d.height;

    if(step != -1) {
      int y0 = 0;
      int y1 = 0;
      for(int i = 1; i < colors.length; i++) {
	y0 = y1;
	y1 = (height * i) / colors.length;
	g.setColor(colors[i]);
	g.fillRect(0, y0, width, y1); 
      }
      g.setColor(java.awt.Color.black);
      int x0 =  step % height;
      int x1 = (step % height) + height;
      for(int i = -height; i < width; i += height)
	g.drawLine(x0 + i, 0, x1 + i, height);
    } else {
      g.setColor(java.awt.Color.lightGray);
      g.fillRect(0, 0, width - 1, height -1);    
    }
    g.setColor(java.awt.Color.black);
    g.drawRect(0, 0, width - 1, height -1);
  }
  
  public static void main(String[] argv) {
    int width = Integer.parseInt(argv[0]);
    java.awt.Frame frame = new java.awt.Frame(WaitBar.class.getName());
    WaitBar pb = new WaitBar(width);
    frame.add(pb);
    frame.pack();
    frame.setVisible(true);
    pb.start();
  }
}
/*
  $Log: WaitBar.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/11/29 12:04:34  schubige
  some 'deprecated' fixes

  Revision 1.2  1999/11/26 09:14:30  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.3  1999/09/03 15:50:09  schubige
  Changed to new header & log conventions.

  Revision 1.2  1999/09/02 08:04:01  schubige
  Added transient or @serial tags to get rid of javadoc warnings.
  
  Revision 1.1.1.1  1999/09/01 06:49:03  schubige
  Moved to common iiuf treee
  
  Revision 1.1.1.1  1999/08/31 15:38:22  schubige
  Moved to common iiuf cvs tree
*/
