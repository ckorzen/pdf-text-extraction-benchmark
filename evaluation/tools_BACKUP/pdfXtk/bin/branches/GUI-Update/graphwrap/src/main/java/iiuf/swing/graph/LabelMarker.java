package iiuf.swing.graph;

import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.border.Border;

/**
   JLabel based edge marker.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class LabelMarker 
  extends 
  JLabel
  implements
  EdgeMarker
{
  public LabelMarker() {}

  public LabelMarker(Icon image) {
    super(image);
  }

  public LabelMarker(Icon image, int horizontalAlignment) {
    super(image, horizontalAlignment);
  }
  
  public LabelMarker(String text) {
    super(text);
  }
  
  public LabelMarker(String text, Icon icon, int horizontalAlignment) {
    super(text, icon, horizontalAlignment);
  }

  public LabelMarker(String text, int horizontalAlignment) {
    super(text, horizontalAlignment);
  }

  public LabelMarker(String text, Border border) {
    super(text);
    setBorder(border);
  }
  
  public LabelMarker(String text, Icon icon, int horizontalAlignment, Border border) {
    super(text, icon, horizontalAlignment);
    setBorder(border);
  }

  public LabelMarker(String text, int horizontalAlignment, Border border) {
    super(text, horizontalAlignment);
    setBorder(border);
  }
  
  public int getBaseline() {
    int h = getPreferredSize().height;
    return getBorder() == null ? h : (h - 1) / 2;
  }
  
  Insets insets = new Insets(0, 0, 0, 0);
  public void paint(Graphics g, int w) {
    setSize(w, getPreferredSize().height);
    if(getBorder() != null) {
      insets = getInsets(insets);
      g.setColor(getBackground());  
      g.fillRect(insets.left - 1, insets.top, getWidth() + 1 - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
    }
    super.paint(g);
  }
}

/*
  $Log: LabelMarker.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel
  
*/
