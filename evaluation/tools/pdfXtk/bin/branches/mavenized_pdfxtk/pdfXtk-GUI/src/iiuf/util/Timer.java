package iiuf.util;

import java.util.Random;
import java.io.Serializable;

/**
  The timer class. This class implements timer tasks that can be run at
   a specified time. This class stores tasks in an efficient tree 
   representation which keeps the insert and remove costs almost constant
   even for a large (> 10000) number of tasks.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Timer 
  implements
  Runnable,
  Serializable
{
  /** @serial The initial delay. */
  public long    milliseconds;
  /** @serial The absolute time when this timer will be triggerd. */
  public long    absolute = Long.MAX_VALUE;
  /** The link to the next timer. */
  transient protected Timer   next;
  /** Set to <code>true</code> as soon as the <code>run</code> method 
    starts executing. reset by <code>reschedule</code> or 
    <code>schedule</code>.
    */
  transient protected boolean execute;
  /** @serial If <code>true</code>, the timer's <code>run</code>
      method will be run
      in a separate thread. */
  protected boolean thread;
  /** The task to run by this timer, or <code>null</code> if no task. */
  transient private Runnable timer_task;
  /** If <code>true</code>, the timer's <code>run</code> will not be called. 
      This variable is set to false by <code>TimerTree.insert()</code> and set by <code>TimerTree.remove()</code>. */
  transient boolean dontRun;
  
  protected Timer() {this(false);}
  
  protected Timer(boolean thread_) {
    thread = thread_;
  }
  
  /**
     Creates a new timer task.
     
    @param timer_task_ The timer task to run.
    @param thread      Run the task as a thread if true, run by timer
    thread if false.
    */
  public Timer(Runnable timer_task_, boolean thread) {
    this(thread);
    timer_task = timer_task_;
  }
  
  /**
    Schedules this objects <code>run</code> method to be executed in
    <code>milliseconds_</code> from now.
    
    @param milliseconds_ Milliseconds from now until the the <code>run</code>
    method is executed.
    
    @return This timer.
    */
  public Timer schedule(long milliseconds_) {
    execute      = false;
    TimerTree.remove(this);
    milliseconds = milliseconds_;
    absolute     = System.currentTimeMillis() + milliseconds;
    TimerTree.insert(this);
    return this;
  }
  
  /**
    Cancels this timer. Cancel only ensures that the <code>run</code> method is
    not executed after <code>cancel</code> returns. But the <code>run</code>
    method may be excuted during the execution of <code>cancel</code>.
    
    @return This timer.
  */
  public Timer cancel() {
    TimerTree.remove(this);
    return this;
  }
  
  /**
    Re-schedules this objects <code>run</code> method to be executed in
    <code>milliseconds_</code> after the <code>schedule</code> call from the
    last execution of the <code>run</code> method. Use this call for fixed
    frequency tasks instead of using <code>schedule</code> in order 
    to avoid drift.
    
    @return This timer.
  */
  public Timer reschedule() {
    TimerTree.remove(this);
    absolute += milliseconds;
    execute = false;
    TimerTree.insert(this);
    return this;
  }
  
  public String toString() {
    return 
      "(ms:"   + milliseconds + 
      ":abs:"  + absolute + 
      ":exe:"  + execute + 
      (next == null ? "" : ":next:" + next) +
      ")";
  }
  
  public void run() {
    if(timer_task != null)
      timer_task.run();
  }
  
  //
  // T E S T  S T U F F
  //
  
  private static Random random = new Random();
  private static int rnd(int range) {
    int result = random.nextInt();
    result = result < 0 ? -result : result;
    return result % range;
  }
  
  private static void countdown(int sec) {
    for(int i = sec; sec >= 0; sec--) {
      System.out.print("\b\b\b\b\b\b\b\b\b" + sec + "    ");
      System.out.flush();
      try{Thread.sleep(1000);}
      catch(Exception e) {}
    }
  }
  
  /** Test program. */
  public static void main(String[] argv) {
    int test = Integer.parseInt(argv[0]);
    switch(test) {
    case 0: 
      System.out.println("Single task, in 5 seconds.");
      new TestTimer("Hello!", false).schedule(5000);
      countdown(5);
      break;
    case 1:
      System.out.println("Single task, every 5 seconds.");
      new TestTimer("Hello!", true).schedule(5000);
      break;
    case 2:
      System.out.println("Two task, in 5 seconds.");
      TestTimer t0 = new TestTimer("Hello 0!", false);
      TestTimer t1 = new TestTimer("Hello 1!", false);
      
      t0.schedule(5000);
      t1.milliseconds = t0.milliseconds;
      t1.absolute     = t0.absolute;
      
      TimerTree.insert(t1);
      System.out.println(TimerTree.root);
      
      countdown(5);
      break;
    case 3: {
      System.out.println("Random timers");
      Timer[] t = new Timer[10000];
      for(;;) {
	int     idx        = rnd(t.length);
	int     time       = rnd(5000) + 100;
	boolean reschedule = rnd(2) == 1;
	if(t[idx] != null)
	  t[idx].cancel();
	t[idx] = new TestTimer("[" + idx + "]Hello(" + time + ", " + 
			       reschedule + ")", reschedule);
	System.out.println("Scheduling new task...");
	t[idx].schedule(time);
	try{
	  Thread.sleep(250);
	  if(System.in.available() > 0) {
	    System.out.println(TimerTree.root);
	    System.exit(0);
	  }
	}
	catch(Exception e) {}
      }
    }
    case 4: {
      System.out.println("Random timers fill");
      Timer[] t = new Timer[Integer.parseInt(argv[1])];
      for(int i = 0; i < t.length; i++) {
	int time = rnd(5000);
	t[i]     = new TestTimer("[" + i + "]Hello(" + time + ")", false);
	t[i].schedule(time);
      }
      countdown(15);
      System.out.println();
      long now = System.currentTimeMillis();
      for(int i = 0; i < t.length; i++)
	System.out.println("[" + i + "]" + t[i] + (now - t[i].absolute));
      System.out.println(TimerTree.root);
      System.exit(0);
      break;
    }
    }
  }
}

class TestTimer
extends
Timer {
  
  static long min = Long.MAX_VALUE;
  static long max = Long.MIN_VALUE;
  static long sum;
  static long count;
  transient String  message;
  transient boolean reschedule;
  
  TestTimer(String message_, boolean reschedule_) {
    message    = message_;
    reschedule = reschedule_;
  }
  
  public void run() {
    long error = System.currentTimeMillis() - absolute;
    if(error < min) min = error;
    if(error > max) max = error;
    sum  += error;
    count++;
    System.out.println(message + "[" + 
		       min   + "," + 
		       error + "," + 
		       max   + "," + 
		       (sum / count) + "]");
    if(reschedule)
      reschedule();
  }
}

final class TimerTree {
  static final Object  LOCK         = TimerTree.class;
  static final boolean DEBUG        = false;
  static final int     CHILDS       = 256;
  static final int     ILLEGAL      = 1000;
  static long          start        = System.currentTimeMillis();
  static TimerTree     root;
  static TimerThread   timer_thread = new TimerThread();
  static int           baseshift;
  int         shift;
  int         min;
  int         max;
  TimerTree[] childs;
  Timer[]     timers;
  
  static Timer min() {
    synchronized(LOCK) {
      return root._min();
    }
  }
  
  static void insert(Timer timer) {
    if(DEBUG) System.out.println("insert(" + timer + ")");
    timer.dontRun = false;
    long ltime = timer.absolute - start;
    long stime = ltime >> baseshift;
    synchronized(LOCK) {
      while(stime > 0) {
	baseshift += 8;
	stime = ltime >> baseshift;
	if(root != null)
	  root = new TimerTree(baseshift, root);
      }
      if(root == null) root = new TimerTree(baseshift);
      root.insert(ltime, timer);
    }
    timer_thread.check();
    if(DEBUG) System.out.println(root);
  }
  
  static void remove(Timer timer) {
    if(DEBUG) System.out.println("remove(" + timer + ")");
    synchronized(LOCK) {
      if(root != null) {
	root.remove(timer.absolute - start, timer);
	timer.next = null;
      }
    }
    timer.dontRun = true;
    if(DEBUG) System.out.println(root);
  }
  
  private Timer _min() {
    if(min == ILLEGAL) return null;
    return shift > 0 ? childs[min]._min() : timers[min];
  }
  
  private TimerTree(int shift_) {
    shift = shift_ - 8;
    min   = ILLEGAL;
    max   = -1;
    if(shift > 0)
      childs = new TimerTree[CHILDS];
    else
      timers = new Timer[CHILDS];
  }
  
  private TimerTree(int shift_, TimerTree old) {
    shift = shift_ - 8;
    childs    = new TimerTree[CHILDS];
    if(old.min != ILLEGAL) {
      childs[0] = old;
      min = max = 0;
    }
    else {
      min   = ILLEGAL;
      max   = -1; 
    }
  }
  
  private TimerTree(int shift_, long time, Timer timer) {
    shift = shift_ - 8;
    min = max = (int)((time >> shift) & 0x0FF);
    if(shift > 0) {
      childs      = new TimerTree[CHILDS];
      childs[min] = new TimerTree(shift, time, timer);
    }
    else {
      timers      = new Timer[CHILDS];
      timers[min] = timer;
    }
  }
  
  private void insert(long time, Timer timer) {
    int  idx   = (int)(( time >> shift) & 0x0FF);
    if(idx < min) min = idx;
    if(idx > max) max = idx;
    if(shift > 0) {
      if(childs[idx] == null)
	childs[idx] = new TimerTree(shift, time, timer);
      else
	childs[idx].insert(time, timer);
    }
    else {
      timer.next  = timers[idx];
      timers[idx] = timer;
    }
  }
  
  private boolean remove(long time, Timer timer) {
    int idx = (int)((time >> shift) & 0x0FF);
    if(shift > 0) {
      if(childs[idx] != null &&
	 childs[idx].remove(time, timer)) {
	childs[idx] = null;
	for(int i = min; i <= max; i++)
	  if(childs[i] != null) {
	    min = i;
	    return false;
	  }
	min = ILLEGAL;
	max = -1;
	return true;
      }
      return false;
    }
    else {
      Timer last = null;
      for(Timer t = timers[idx]; t != null; t = t.next) {
	if(t == timer)
	  if(last == null)
	    timers[idx] = t.next;
	  else
	    last.next = t.next;
	last = t;
      }
      for(int i = min; i <= max; i++)
	if(timers[i] != null) {
	  min = i;
	  return false;
	}
      min = ILLEGAL;
      max = -1;
      return true;
    }
  }
  
  private String prefix() {
    String result = "";
    for(int i = 0; i < (64 - shift) / 8; i++)
      result += "  ";
    return result;
  }
  
  public String toString() {
    String result = prefix() + "shift:" + shift +
      ":min:" + min + ":max:" + max + "\n";
    if(childs != null)
      for(int i = 0; i < childs.length; i++)
	if(childs[i] != null)
	  result += prefix() + "childs[" + i + "]\n" + childs[i];
    if(timers != null)
      for(int i = 0; i < timers.length; i++)
	if(timers[i] != null)
	  result += prefix() + "timers[" + i + "]:" + timers[i] + "\n";
    return result;
  }
}

final class TimerThread
extends
Thread 
{    
  private boolean notified;
  
  TimerThread() {
    setName("TimerThread");
    setPriority(Thread.MAX_PRIORITY);
    start();
  }
  
  synchronized void check() {
    notified = true;
    notify();
  }
  
  public void run() {
    synchronized(this) {
      while(TimerTree.root == null) {
	try{wait();}
	catch(InterruptedException e) {
	  Util.printStackTrace(e);
	}
      }
    }
    for(;;) {
      long  now = System.currentTimeMillis();
      Timer t   = TimerTree.min();
      if(t != null) {
	if(now >= t.absolute) {
	  if(t.dontRun) {
	    TimerTree.remove(t);
	    continue;
	  }
	  t.execute = true;
	  TimerTree.remove(t);
	  if(t.thread) 
	    new Thread(t).start();
	  else
	    t.run();
	  continue;
	}
	else {
	  long delta = t.absolute - now;
	  if(delta > 10) {
	    synchronized(this) {
	      if(notified) {
		notified = false;
		continue;
	      }
	      try{wait(delta);}
	      catch(InterruptedException e) {
		Util.printStackTrace(e);
	      }
	    }
	  }
	}
      } else {
	synchronized(this) {
	  if(notified) {
	    notified = false;
	    continue;
	  }
	  try{wait();}
	  catch(InterruptedException e) {
	    Util.printStackTrace(e);
	  }
	}
      }
    }
  }
}
/*
  $Log: Timer.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.8  2001/04/30 07:33:17  schubige
  added webcom to cvstree

  Revision 1.7  2001/04/11 14:17:03  schubige
  adapted tinja stuff for semantic checks

  Revision 1.6  2001/02/23 17:23:11  schubige
  Added loop source to soundium and fxed some bugs along

  Revision 1.5  2001/02/02 18:04:35  schubige
  rpc client working for udp & tcp

  Revision 1.4  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.2  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
