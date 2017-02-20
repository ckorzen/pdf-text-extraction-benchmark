package iiuf.awt;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
   Horizontal line.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class HLine
  
extends
Canvas {
 
  /** @serial */
  private Color     color;
  /** @serial */
  private Dimension min;
  /** @serial */
  private Dimension max;
  
  public HLine(Color color_) { 
    min = new Dimension(2, 1);
    max = new Dimension(Integer.MAX_VALUE, 1);
    color = color_;
    getSize();
  }
  
  public HLine() {
    min = new Dimension(2, 2);
    max = new Dimension(Integer.MAX_VALUE, 2);
    getSize();
  }
  
  public Dimension getMinimumSize() {
    return new Dimension(min.width, min.height);
  }

  public Dimension getMaximumSize() {
    return new Dimension(max.width, max.height);   
  }
  
  public Dimension getPreferredSize() {
    Dimension size = getSize();
    return new Dimension(size.width  < min.width  ? min.width  : size.width, 
			 size.height < min.height ? min.height : size.height);
  }
    
  public Dimension getSize() { 
    Dimension result = super.getSize();
    if(result.width  < min.width)  result.width  = min.width;
    if(result.height < min.height) result.height = min.height;
    if(result.height > max.height) result.height = max.height;
    setSize(result);
    return result;
  }

  public void paint(Graphics g) {
    int width = getSize().width;
    int y     = getSize().height / 2 - 1;
    
    if (g == null) return;

    if(color == null) {
      if (getBackground() == null) return;
      g.setColor(getBackground().darker());
      g.drawLine(0, y + 0, width, y + 0);		
      g.setColor(getBackground().brighter());
      g.drawLine(0, y + 1, width, y + 1);
    } else {
      g.setColor(color);
      g.drawLine(0, y + 1, width, y + 1);	
    }
  }
}
/*
  $Log: HLine.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.3  1999/11/26 10:00:36  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.5  1999/09/28 13:33:06  juillera
  Add some tests in HLine.java to prevent null-ptr-exception

  Revision 1.4  1999/09/24 09:45:21  juillera
  Fixed bugs 5, 10, 11, 12 and 14 of the Bug database.
handling bug 6.

  Revision 1.3  1999/09/17 14:40:53  juillera
  Updated for MCW

  Revision 1.2  1999/09/14 11:59:39  schubige
  Added @serial and transient for javadoc

  Revision 1.1  1999/09/10 12:17:35  juillera
  First Checked In.

  Revision 1.1  1999/09/09 14:32:13  schubige
  Added Line, DateChooser and Dialog

  Revision 1.3  1999/09/03 15:50:09  schubige
  Changed to new header & log conventions.
  
*/
