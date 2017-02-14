package iiuf.util;

import java.util.LinkedList;

/**
   Implementation of a generalized async execution queue class.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public abstract class Queue 
extends
Thread
{
  protected LinkedList queue = new LinkedList();
  protected boolean    go    = true;
  protected long puts;

  /**
    Constructs a new queue.
    
    @param name The name given to the thread reading from the queue.
    */
  public Queue(String name) {
    setName(name);
    start();
  }

  /**
    Constructs a new queue.
    
    @param name The name given to the thread reading from the queue.
    @param priority The priority of the read-from queue thread.
    */
  public Queue(String name, int priority) {
    setName(name);
    start();
    setPriority(priority);
  }
  
  /**
    Adds an element to the queue.
    
    @param element The element to add to the queue.
    */
  final public synchronized void put(Object element) {
    queue.add(element);
    notify();
    puts++;
  }
  
  /**
     The read-from-queue loop.
  */
  public void run() {
    while(go) {
      Object elem;
      synchronized(this) {
	if(queue.isEmpty()) {
	  try{wait();} catch(InterruptedException e) {Util.printStackTrace(e);}
	}
	if(queue.isEmpty()) continue;
	elem = queue.removeFirst();
	if(queue.isEmpty() && flushing)
	  notify();
      }
      go = handle(elem);
    }
  }
  
  protected boolean flushing;
  /**
     Waits until queue is empty.
  */
  public synchronized void flush() {
    flushing = true;
    while(!queue.isEmpty()) {
      try{wait();} catch(InterruptedException e) {Util.printStackTrace(e);}
    }
    flushing = false;
  }
  
  /**
     Checks if this queue is empty.
     
     @return True if the queue is empty, false otherwise.
  */
  public synchronized boolean isEmpty() {
    return queue.isEmpty();
  }
  
  /**
    This method is called when an element from the queue was read.
    The current thread is the thread started by this queue and is the owner
    of this queue's monitor during the execution of this method.
    
    @param element The element read.
    @return The read-from-queue loop exits and the thread terminates if this 
    method returns false. the read-from-queue loop continues and the thread
    stays alive if this method returns true.
    */
  protected abstract boolean handle(Object element);
  
  public String toString() {
    String result = "";
    Object[] q = queue.toArray();
    for(int i = 0; i < q.length; i++)
      result += "[" + i + "]" + q[i];
    return result;
  }
}
/*
  $Log: Queue.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/02/08 19:20:44  schubige
  rpcgen compatibility enhanced

  Revision 1.4  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.2  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
