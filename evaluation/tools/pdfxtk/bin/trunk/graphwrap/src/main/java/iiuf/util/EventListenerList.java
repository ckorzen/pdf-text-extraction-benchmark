package iiuf.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.EventListener;

/**
   Event listener list implementation. This implementation is compatible with 
   <code>javax.swing.EventListenerList</code> but uses weak references to
   the listeners to avoid memory leaks.

   Event listener list are prone to memory leaks because clients often forget
   or are unable to remove them properly.
   
   Classes using <code>EventListenerList</code> should implement beside the standard
   <code>addXXXListener(EventListener l)</code> and <code>removeXXXListener(EventListener l)</code> methods an 
   <code>addXXXListener(EventListener l, boolean weak)</code> method that allows
   addition of listeners using strong references if required by the client.
   
   (c) 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
   @see javax.swing.event.EventListenerList
   @see java.lang.ref.WeakReference
*/
public class EventListenerList {
  /* A null array to be shared by all empty listener lists*/
  private final static Object[] NULL_ARRAY = new Object[0];
  /* The list of ListenerType - Listener pairs */
  protected transient Object[] listenerList = NULL_ARRAY;
  
  private synchronized Object[] compact() {
    Object[] result = new Object[listenerList.length];
    int count = 0;
    for(int i = 0; i < listenerList.length; i += 2) {
      result[i]     = listenerList[i];
      result[i + 1] = listenerList[i + 1] instanceof WeakReference ? 
	((WeakReference)listenerList[i + 1]).get() :
	listenerList[i + 1];
      if(result[i + 1] != null)
	count += 2;
    }
    if(count == listenerList.length) return result;
    Object[] tmp     = new Object[count];
    Object[] result2 = new Object[count];
    count = 0;
    for(int i = 0; i < result.length; i += 2) {
      if(result[i + 1] != null) {
	tmp[count]     = listenerList[i];
	result2[count] = result[i];
	count++;
	tmp[count]     = listenerList[i + 1];
	result2[count] = result[i + 1];
	count++;
      }
    }
    listenerList = tmp;
    return result2;
  }

  /**
   * Passes back the event listener list as an array
   * of ListenerType-listener pairs. 
   * This method is guaranteed to pass back a non-null
   * array, so that no null-checking is required in 
   * fire methods.  A zero-length array of Object should
   * be returned if there are currently no listeners.
   */
  public Object[] getListenerList() {
    return compact();
  }
  
  /**
   * Return an array of all the listeners of the given type.
   * 
   * @returns all of the listeners of the specified type. 
   */
  public EventListener[] getListeners(Class t) {
    Object[] lList = compact(); 
    int n = 0;
    for(int i = 0; i < lList.length; i += 2)
    if(lList[i] == t)
    n++;
    EventListener[] result = (EventListener[])Array.newInstance(t, n); 
    int j = 0; 
    for(int i = lList.length - 2; i>=0; i-=2) {
      if(lList[i] == t)
	result[j++] = (EventListener)lList[i + 1];
    }
    return result;   
  }
  
  /**
   * Returns the total number of listeners for this listener list.
   */
  public int getListenerCount() {
    return compact().length / 2;
  }
  
  /**
   * Returns the total number of listeners of the supplied type 
   * for this listener list.
   */
  public int getListenerCount(Class t) {
    int count = 0;
    Object[] lList = compact();
    for(int i = 0; i < lList.length; i += 2) {
      if(t == (Class)lList[i])
	count++;
    }
    return count;
  }
  
  private static long lastGc;
  /**
   * Adds the listener as a listener of the specified type.
   * The listener is add as a strong reference by default.
   * @param t the type of the listener to be added
   * @param l the listener to be added
   */
  public void add(Class t, EventListener l) {
    add(t, l, false);
  }
  /**
   * Adds the listener as a listener of the specified type.
   * @param t the type of the listener to be added
   * @param l the listener to be added
   * @param weak if true, add listener as weak reference, if false, add as strong reference.
   */
  public synchronized void add(Class t, EventListener l, boolean weak) {
    if(l==null)
      throw new IllegalArgumentException("event listener must not be null");
    if(!t.isInstance(l)) {
      throw new IllegalArgumentException("Listener " + l +
					 " is not of type " + t);
    }
    // run gc to get rid of old listeners.
    
    long now = System.currentTimeMillis();
    if(now > lastGc + 1000) {
      System.gc();
      lastGc = now;
    }
    compact();
    if(listenerList == NULL_ARRAY) {
      // if this is the first listener added, 
      // initialize the lists
      listenerList = new Object[] { t, weak ? (Object)new WeakReference(l) : l};
    } else {
      // Otherwise copy the array and add the new listener
      int i = listenerList.length;
      Object[] tmp = new Object[i+2];
      System.arraycopy(listenerList, 0, tmp, 0, i);
      
      tmp[i] = t;
      tmp[i+1] = weak ? (Object)new WeakReference(l) : l;
      
      listenerList = tmp;
    }
  }
  
  /**
   * Removes the listener as a listener of the specified type.
   * @param t the type of the listener to be removed
   * @param l the listener to be removed
   */
  public synchronized void remove(Class t, EventListener l) {
    if(l==null)
      throw new IllegalArgumentException("event listener must not be null");
    if(!t.isInstance(l)) {
      throw new IllegalArgumentException("Listener " + l +
					 " is not of type " + t);
    }
    // Is l on the list?
    int index = -1;
    for(int i = listenerList.length - 2; i >= 0; i -= 2) {
      if(listenerList[i + 1] instanceof WeakReference) {
	if((listenerList[i] == t) && (l.equals(((WeakReference)listenerList[i + 1]).get()))) {
	  ((WeakReference)listenerList[i + 1]).clear();
	  listenerList[i + 1] = null;
	  break;
	}
      } else {
	if((listenerList[i] == t) && (l.equals(listenerList[i + 1]))) {
	  listenerList[i + 1] = null;
	  break;
	}
      }
    }
  }
  
  /**
   * Returns a string representation of the EventListenerList.
   */
  public String toString() {
    Object[] lList = compact();
    String s = "EventListenerList: ";
    s += lList.length/2 + " listeners: ";
    for(int i = 0 ; i <= lList.length - 2 ; i += 2) {
      s += " type " + ((Class)lList[i]).getName();
      s += " listener " + lList[i+1];
    }
    return s;
  }
}

/*
  $Log: EventListenerList.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.7  2001/02/13 14:49:06  schubige
  started work on gui - engine connection

  Revision 1.6  2001/01/12 09:11:15  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.5  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.4  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2001/01/04 12:12:36  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.2  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.1  2001/01/03 15:24:25  schubige
  graph stuff beta
  
*/
