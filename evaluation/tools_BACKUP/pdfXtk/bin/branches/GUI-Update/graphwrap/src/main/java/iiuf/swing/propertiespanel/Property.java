package iiuf.swing.propertiespanel;

import java.util.Hashtable;
import javax.swing.JComponent;

/**
   Property base class.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public abstract class Property {
  protected boolean required;
  String            key;
  protected boolean enabled = true;

  Property(boolean required_, String key_) {
    required = required_;
    key      = key_;
  }
  
  public boolean isValid(PropertiesPanel panel, JComponent cmp) {   
    return true;
  }

  public void setEnabled(boolean state) {
    enabled = state;
  }

  abstract public void read(PropertiesPanel panel, Hashtable values);
  abstract public void write(PropertiesPanel panel, Hashtable values);
  abstract public void create(PropertiesPanel panel);
}

/*
  $Log: Property.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/10/09 06:49:27  schubige
  Added properties panel
  
*/
