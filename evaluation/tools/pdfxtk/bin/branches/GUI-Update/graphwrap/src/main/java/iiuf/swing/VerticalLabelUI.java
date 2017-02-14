package iiuf.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.text.View;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicHTML;

/**
   A JLabel UI that does vertical instead of horizontal rendering.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class VerticalLabelUI
  extends 
  BasicLabelUI
{
  static {
    labelUI = new VerticalLabelUI(false);
  }
 
  protected boolean clockwise;
  
  public VerticalLabelUI(boolean clockwise_) {
    clockwise = clockwise_;
  }
  
  public Dimension getPreferredSize(JComponent c) {
    Dimension dim = super.getPreferredSize(c);
    return new Dimension(dim.height, dim.width);
  }   
  
  private static Rectangle paintIconR      = new Rectangle();
  private static Rectangle paintTextR      = new Rectangle();
  private static Rectangle paintViewR      = new Rectangle();
  private static Insets    paintViewInsets = new Insets(0, 0, 0, 0);
  
  public void paint(Graphics g, JComponent c) {
    JLabel label = (JLabel)c;
    String text  = label.getText();
    Icon   icon  = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
    
    if(icon == null && text == null) return;
  
    FontMetrics  fm = g.getFontMetrics();
    paintViewInsets = c.getInsets(paintViewInsets);
    
    paintViewR.x      = paintViewInsets.top;
    paintViewR.y      = paintViewInsets.left;
    paintViewR.width  = c.getHeight() - (paintViewInsets.top  + paintViewInsets.bottom);
    paintViewR.height = c.getWidth()  - (paintViewInsets.left + paintViewInsets.right);
    
    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
    
    String clippedText = 
      layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);
    
    Graphics2D      g2 = (Graphics2D)g;
    AffineTransform tr = g2.getTransform();
    if(clockwise) {
      g2.rotate(Math.PI / 2); 
      g2.translate(0, - c.getWidth());
    } else {
      g2.rotate(-Math.PI / 2); 
      g2.translate(-c.getHeight(), 0);
    }
    
    if (icon != null) {
      icon.paintIcon(c, g2, paintIconR.x, paintIconR.y);
    }
    
    if (text != null) {
      View v = (View) c.getClientProperty(BasicHTML.propertyKey);
      if (v != null) {
	v.paint(g2, paintTextR);
      } else {
	int textX = paintTextR.x;
	int textY = paintTextR.y + fm.getAscent();
	
	if (label.isEnabled())
	  paintEnabledText(label, g2, clippedText, textX, textY);
	else
	  paintDisabledText(label, g2, clippedText, textX, textY);
      }
    }
  }
}

/*
  $Log: VerticalLabelUI.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/02/23 17:25:07  schubige
  Added loop source to soundium and fxed some bugs along
  
*/
