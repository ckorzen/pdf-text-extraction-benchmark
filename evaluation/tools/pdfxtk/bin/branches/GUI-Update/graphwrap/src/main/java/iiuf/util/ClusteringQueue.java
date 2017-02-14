package iiuf.util;

import java.util.LinkedList;

/**
   Implementation of a generalized async executin clustering queue class.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public abstract class ClusteringQueue 
  extends
  Queue
{ 
  protected long handles;
  
  public ClusteringQueue(String name) {
    super(name);
  }
  
  public ClusteringQueue(String name, int priority) {
    super(name);
    setPriority(priority);
  }
  
  private Object[] singleton = new Object[1];
  /**
     The read-from-queue loop.
  */
  public void run() {
    while(go) {
      Object[] elems = null;
      synchronized(this) {
	if(queue.isEmpty()) {
	  try{wait();} catch(InterruptedException e) {Util.printStackTrace(e);}
	}
	if(queue.size() == 1) {
	  singleton[0] = queue.removeFirst();
	  elems = singleton;
	}
	else {
	  elems = queue.toArray();
	  queue.clear();
	}
	if(queue.isEmpty() && flushing)
	  notify();
      }
      go = handle(elems);
      handles++;
    }
  }
  
  final protected boolean handle(Object element) {
    throw new RuntimeException("Handle not implemented.");
  }
  
  /**
     This method is called when some elements from the queue were read.
     Reimplement this method.
     The current thread is the thread started by this queue and is the owner
     of this queue's monitor during the execution of this method.
     
     @param elements The elements read.
     @return The read-from-queue loop exits and the thread terminates if this 
     method returns false. The read-from-queue loop continues and the thread
     stays alive if this method returns true.
  */
  protected abstract boolean handle(Object[] elements);
  
  public String toString() {
    return "Avg clstr sz:" + ((double)puts / (double)handles) + ":" +
      super.toString();
  }
}
/*
  $Log: ClusteringQueue.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/02/08 19:20:44  schubige
  rpcgen compatibility enhanced

  Revision 1.4  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF
  
  Revision 1.3  2000/11/09 07:48:44  schubige
  early checkin for DCJava
  
  Revision 1.2  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
