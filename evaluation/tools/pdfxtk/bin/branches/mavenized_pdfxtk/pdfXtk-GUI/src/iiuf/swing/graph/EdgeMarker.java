package iiuf.swing.graph;

import java.awt.Dimension;
import java.awt.Graphics;

/**
   Edge marker interface.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface EdgeMarker {
  public Dimension getPreferredSize();
  public int       getWidth();
  public int       getBaseline();
  public void      paint(Graphics g, int width);
}

/*
  $Log: EdgeMarker.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel
  
*/
