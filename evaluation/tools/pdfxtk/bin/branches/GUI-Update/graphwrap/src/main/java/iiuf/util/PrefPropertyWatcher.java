package iiuf.util;

import java.io.Serializable;
import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
   Generic property preferences watcher.
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class PrefPropertyWatcher 
  implements
  PrefWatcher 
{
  static class PropertyState 
    implements
    Serializable
  {
    String property;
    Object value;
    
    PropertyState(String property_, JComponent cmp) {
      property = property_;
      grab(cmp.getClientProperty(property));
    }
    
    void grab(Object value_) {
      value = value_;
    }
    
    void apply(JComponent cmp) {
      if(value != null)
	cmp.putClientProperty(property, value);
    }
    
    public String toString() {
      return property + ":" + value;
    }
  }

  private String property;
  private Class  watchedClass;
  
  PrefPropertyWatcher(String property_, Class watchedClass_) {
    property     = property_;
    watchedClass = watchedClass_;
  }
  
  private PropertyState result;
  
  public synchronized Serializable watch(Object o, Serializable preferences) {
    JComponent component = (JComponent)o;
    
    result = preferences == null ? new PropertyState(property, component) : (PropertyState)preferences;
    
    component.addPropertyChangeListener(property, new PropertyChangeListener() {
	PropertyState state = result;
	
	public void propertyChange(PropertyChangeEvent e) {
	  System.out.println(e);
	  state.grab(e.getNewValue());
	}
      });
    
    if(preferences != null)
      result.apply(component);
    
    return result;
  }
  
  public Class watchedClass() {
    return watchedClass;
  }
}

/*
  $Log: PrefPropertyWatcher.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/10/17 15:35:59  schubige
  Added watcher preferences
  
*/
