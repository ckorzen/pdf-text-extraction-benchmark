package iiuf.swing.graph;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Graphics;

/**
   Equilateral triangular edge marker.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
     */
public class EquilateralTriangleMarker 
  implements
  EdgeMarker
{
  private Polygon   p;
  private Dimension d;
  private boolean   opaque;
  private Color     foreground;
  private Color     background;
  private boolean   open;
  private int       baseline;
  
  public EquilateralTriangleMarker(double length, double headAngle, boolean open_, boolean rightToLeft,
				   Color foreground, Color background) {
    p = new Polygon();
    int x    = (int)(Math.cos(headAngle) * length);
    baseline = (int)(Math.sin(headAngle) * length);
    int y    = baseline * 2;
    if(rightToLeft) {
      p.addPoint(x, 0);
      p.addPoint(0, baseline);
      p.addPoint(x, y);      
    } else {
      p.addPoint(0, 0);
      p.addPoint(x, baseline);
      p.addPoint(0, y);
    }
    d = p.getBounds().getSize();
    setOpaque(true);
    setForeground(foreground);
    setBackground(background);
    open = open_;
  }
  
  public EquilateralTriangleMarker(double length, double headAngle, boolean open, boolean rightToLeft) {
    this(length, headAngle, open, rightToLeft, null, null);
  }
  
  public void setOpaque(boolean state) {
    opaque = state;
  }

  public boolean isOpaque() {
    return opaque;
  }

  public Dimension getPreferredSize() {
    return d;
  }
    
  public int getWidth() {
    return d.width;
  }
  
  public int getBaseline() {
    return baseline;
  }
  
  public void setForeground(Color c) {
    foreground = c;
  }

  public Color getForegorund() {
    return foreground;
  }
  
  public void setBackground(Color c) {
    background = c;
  }

  public Color getBackgorund() {
    return background;
  }

  public void paint(Graphics g, int width) {
    if(background != null)
      g.setColor(background);
    if(opaque && !open)
      g.fillPolygon(p);    
    if(foreground != null)
      g.setColor(foreground);
    if(open)
      g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
    else
      g.drawPolygon(p);
  }
}

/*
  $Log: EquilateralTriangleMarker.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel
  
*/
