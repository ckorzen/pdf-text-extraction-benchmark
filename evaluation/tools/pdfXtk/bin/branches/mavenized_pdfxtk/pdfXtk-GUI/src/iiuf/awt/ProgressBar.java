package iiuf.awt;

import iiuf.util.Util;

/**
   A progress bar implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class ProgressBar 
extends
java.awt.Canvas {

  static final int              HEIGHT = 16;
  static final java.awt.Color[] colors = {
    null,
    new java.awt.Color(0xA * 0x111111),
    new java.awt.Color(0xC * 0x111111),
    new java.awt.Color(0xE * 0x111111),
    new java.awt.Color(0xF * 0x111111),
    new java.awt.Color(0xE * 0x111111),
    new java.awt.Color(0xC * 0x111111),
    new java.awt.Color(0xA * 0x111111),
  };
  
  /** @serial */
  int steps;
  /** @serial */
  int step;
  
  public ProgressBar(int width, int steps_) {
    steps = steps_;
    setSize(width, HEIGHT);
    reset();
  }
  
  public void reset() {
    set(-1);
  }
  
  public void step() {
    if(step == -1) step = 0;
    set(step + 1);
  }
  
  public void set(int step_) {
    step = step_;
    repaint();
  }
  
  public void paint(java.awt.Graphics g) {
    java.awt.Dimension d = getSize();
    int width  = (d.width * (step + 1)) / steps;
    int height = d.height;

    if(step != -1) {
      int y0 = 0;
      int y1 = 0;
      for(int i = 1; i < colors.length; i++) {
	y0 = y1;
      y1 = (height * i) / colors.length;
      g.setColor(colors[i]);
      g.fillRect(0, y0, width, y1); 
      }
    }
    else {
      g.setColor(java.awt.Color.lightGray);
      g.fillRect(0, 0, d.width - 1, height -1);    
    }
    g.setColor(java.awt.Color.black);
    g.drawRect(0, 0, width - 1, height -1);
    g.drawRect(0, 0, d.width - 1, height -1);
  }
  
  public static void main(String[] argv) {
    int width = Integer.parseInt(argv[0]);
    int steps = Integer.parseInt(argv[1]);
    java.awt.Frame frame = new java.awt.Frame(ProgressBar.class.getName());
    ProgressBar pb = new ProgressBar(width, steps);
    frame.add(pb);
    frame.pack();
    frame.setVisible(true);
    for(int i = 0; i < steps; i++) {
      pb.step();
      Util.delay(500);
    }
    System.exit(0);
  }
}
/*
  $Log: ProgressBar.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/04/25 12:03:35  schubige
  Bibtex db project restart

  Revision 1.3  1999/11/26 10:00:39  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.3  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
