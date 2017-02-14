package iiuf.swing;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Component;
import javax.swing.border.Border;
import javax.swing.border.AbstractBorder;

/**
   A class which implements a hexagonal line border of arbitrary thickness
   and of a single color.
   <p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class HexagonalBorder 
  extends 
  AbstractBorder 
{
  private static Border blackLine;
  private static Border grayLine;
  
  protected Color   lineColor;
  protected int[]   x1s = new int[3];
  protected int[]   x2s = new int[3];
  protected int[]   ys  = new int[3];
  protected boolean opaque = true;
  
  /** 
      Convenience method for getting the Color.black HexagonalBorder.
  */
  public static Border newBlackBorder() {
    if (blackLine == null) {
      blackLine = new HexagonalBorder(Color.black);
    }
    return blackLine;
  }
  
  /**
     Convenience method for getting the Color.gray HexagonalBorder.
   */
  public static Border newGrayBorder() {
    if (grayLine == null) {
      grayLine = new HexagonalBorder(Color.gray);
    }
    return grayLine;
  }
  
  /** 
   * Creates a hexagonal line border with the specified color.
   * @param color the color for the border.
   */
  public HexagonalBorder(Color color) {
    lineColor = color;
  }
  
  /**
   * Paints the border for the specified component with the 
   * specified position and size.
   * @param c the component for which this border is being painted
   * @param g the paint graphics
   * @param x the x position of the painted border
   * @param y the y position of the painted border
   * @param width the width of the painted border
   * @param height the height of the painted border
   */
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Color oldColor = g.getColor();
    int   h2       = (height - 1) / 2;
    
    x1s[0] = width - h2 -2;
    x1s[1] = width      -2;
    x1s[2] = width - h2 -2;
    
    x2s[0] = h2 + 1;
    x2s[1] = 1;
    x2s[2] = h2 + 1;
    
    ys[0] = 0;
    ys[1] = h2;
    ys[2] = h2 * 2;
    
    if(opaque) {
      g.setColor(c.getBackground());
      g.fillPolygon(x1s, ys, 3);
      g.fillPolygon(x2s, ys, 3);
    }
    
    x1s[0]++; x1s[1]++; x1s[2]++;

    x2s[0]--; x2s[1]--; x2s[2]--;

    g.setColor(lineColor);
    g.drawLine(x1s[0] , ys[0], x1s[1], ys[1]);
    g.drawLine(x1s[1] , ys[1], x1s[2], ys[2]);
    g.drawLine(x1s[2] , ys[2], x2s[0], ys[2]);
    g.drawLine(x2s[0] , ys[2], x2s[1], ys[1]);
    g.drawLine(x2s[1] , ys[1], x2s[2], ys[0]);
    g.drawLine(x2s[2] , ys[0], x1s[0], ys[0]);
    g.setColor(oldColor);
  }
  
  /**
   * Returns the insets of the border.
   * @param c the component for which this border insets value applies
   */
  public Insets getBorderInsets(Component c)       {
    return new Insets(1, 1 + c.getHeight() / 2, 1, 1 + c.getHeight() / 2);
  }
  
  /** 
   * Reinitialize the insets parameter with this Border's current Insets. 
   * @param c the component for which this border insets value applies
   * @param insets the object to be reinitialized
   */
  public Insets getBorderInsets(Component c, Insets insets) {
    insets.left = insets.right  = 1 + c.getHeight() / 2;
    insets.top  = insets.bottom = 1;
    return insets;
  }
  
  /**
   * Returns the color of the border.
   */
  public Color getLineColor() {
    return lineColor;
  }
    
  public boolean isBorderOpaque() {
    return opaque;
  }
  
  public void setOpaque(boolean state) {
    opaque = state;
  }
}

/*
  $Log: HexagonalBorder.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel
  
*/
