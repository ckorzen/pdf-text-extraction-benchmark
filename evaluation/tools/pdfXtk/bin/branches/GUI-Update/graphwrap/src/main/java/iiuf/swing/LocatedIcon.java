package iiuf.swing;

import java.awt.Point;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
   Icon wrapper that stores last icon location during paint.

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class LocatedIcon 
  implements
  Icon
{
  private Point location = new Point();
  private Icon  icon;
  
  public LocatedIcon(Icon icon) {
    setIcon(icon);
  }

  public int getIconWidth() {
    return icon.getIconWidth();
  }
  
  public int getIconHeight() {
    return icon.getIconHeight();
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    location.x = x;
    location.y = y;
    icon.paintIcon(c, g, x, y);
  }

  public Point getLocation() {
    return location;
  }
    
  public int getX() {
    return location.x;
  }

  public int getY() {
    return location.y;
  }

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(Icon icon_) {
    icon = icon_;
  }
}

/*
  $Log: LocatedIcon.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/03/07 07:52:07  schubige
  soundium properites panel
  
*/
