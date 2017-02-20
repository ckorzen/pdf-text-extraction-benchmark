package iiuf.util;

import java.util.ArrayList;
import java.util.Observer;
import java.util.Observable;
import java.util.BitSet;
import java.util.HashMap;

/**
   Attributable base class.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DefaultAttributable 
  extends
  Observable
{
  private Object[]  attributes = new Object[0];
  private ArrayList observers  = new ArrayList();
  private BitSet    changeMap  = new BitSet();
  
  class ObserverEntry {
    Observer observer;
    Object   tag;
    BitSet   ids = new BitSet();
    
    ObserverEntry(Observer observer_, Object tag_, int[] ids_) {
      observer = observer_;
      tag      = tag_;
      for(int i = 0; i < ids_.length; i++)
	ids.set(ids_[i]);
    }
  }
  
  public final Object get(int id) {
    try{
      return attributes[id];
    } catch(ArrayIndexOutOfBoundsException e) {
      ensure(id);
      return attributes[id];
    }
  }
  
  public final void set(int id, Object value) {
    try {
      attributes[id] = value;
    } catch(ArrayIndexOutOfBoundsException e) {
      ensure(id);
      attributes[id] = value;
      changeMap.set(id);
    }
  }
  
  public final boolean has(int id) {
    return id < attributes.length && attributes[id] != null;
  }
  
  public synchronized void commit() {
    Object[] o = observers.toArray();
    for(int i = 0; i < o.length; i++) {
      BitSet tmp = (BitSet)changeMap.clone();
      ObserverEntry oe = (ObserverEntry)o[i];
      tmp.and(oe.ids);
      if(tmp.length() != 0)
	oe.observer.update(this, oe.tag);
    }
  }    
  
  public synchronized void addObserver(int[] ids, Observer observer, Object tag) {
    observers.add(new ObserverEntry(observer, tag, ids));
  }
  
  public synchronized void removeObserver(Observer observer, Object tag) {
    Object[] o = observers.toArray();
    for(int i = 0; i < o.length; i++) {
      ObserverEntry oe = (ObserverEntry)o[i];
      if(oe.observer == observer && oe.tag.equals(tag)) {
	observers.remove(o);
	break;
      }
    }
  }
  
  public Object[] getAttributes() {
    return attributes;
  }
  
  private synchronized void ensure(int id) {
    if(id >= attributes.length) {
      Object[] tmp = attributes;
      attributes = new Object[id + 1]; 
      System.arraycopy(tmp, 0, attributes, 0, tmp.length);
    } 
  }
  
  public String toString() {
    String result = getClass().getName() + "[";
    for(int i = 0; i < attributes.length; i++)
      if(attributes[i] != null)
	result += "[" + i + "]=" + attributes[i];
    result += "]";
    return result;
  }
}

/*
  $Log: DefaultAttributable.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.4  2001/03/21 19:34:06  schubige
  started with dom stuff

  Revision 1.3  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/20 09:46:39  schubige
  TJGUI update

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph

  Revision 1.3  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:48:10  schubige
  Added graph stuff
  
*/
