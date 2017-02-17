package iiuf.log;

import java.io.PrintStream;
import iiuf.util.EventListenerList;

/**
   Logger interface.
   
   Set the "log" property to enable client/server logging.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public final class Log {
  public static PrintStream  err;
  public static PrintStream  out;
  
  private static Client            logger;
  private static boolean           USE_LOGGER = init();
  static         EventListenerList listeners = new EventListenerList(); 

  private static boolean init() {
    boolean result = System.getProperty("log") != null;
    if(result) logger = new Client();
    else {
      err = System.err;
      out = System.out;
    }
    return result;
  }
  
  public final static void stackTrace(int priority, Throwable t) {
    String msg = t.getMessage();
    if(msg == null) msg = t.toString();
    println(priority, msg, t);
    if(!USE_LOGGER) t.printStackTrace(out);
  }
  
  public final static void println(int priority, Object o, boolean trace) {
    Throwable t = null;
    if(trace) {
      t = new Throwable();
      t.fillInStackTrace();
    }
    println(priority, o, t);
  }
  
  public final synchronized static void println(int priority, Object o, Throwable t) {
    LogMessage m = new LogMessage(Thread.currentThread(), priority, o == null ? "<null>" : o.toString(), t);
    m.time = System.currentTimeMillis();    
    if(USE_LOGGER)
      logger.log(m);
    else out.println(o);

    LogListener[] l = (LogListener[])listeners.getListeners(LogListener.class);
    int prio = 1 << priority;
    for(int i = 0; i < l.length; i++)
      if((prio & l[i].getPriorityMask()) != 0)
	l[i].log(m);
  }
  
  public static void addLogListener(LogListener l) {
    addLogListener(l, true);
  }
  
  public static void addLogListener(LogListener l, boolean weak) {
    listeners.add(LogListener.class, l, weak);
  }

  public static void removeLogListener(LogListener l) {
    listeners.remove(LogListener.class, l);
  }

  public final static void emergency(Object o) {
    println(Const.LOG_EMERG, o, true);
  }

  public final static void alert(Object o) {
    println(Const.LOG_ALERT, o, true);
  }

  public final static void critical(Object o) {
    println(Const.LOG_CRIT, o, true);
  }

  public final static void error(Object o) {
    println(Const.LOG_ERR, o, true);
  }

  public final static void warning(Object o, boolean trace) {
    println(Const.LOG_WARNING, o, trace);
  }

  public final static void warning(Object o) {
    println(Const.LOG_WARNING, o, false);
  }

  public final static void notice(Object o, boolean trace) {
    println(Const.LOG_NOTICE, o, trace);
  }

  public final static void notice(Object o) {
    println(Const.LOG_NOTICE, o, false);
  }
  
  public final static void info(Object o, boolean trace) {
    println(Const.LOG_INFO, o, trace);
  }
  
  public final static void info(Object o) {
    println(Const.LOG_INFO, o, false);
  }
    
  public final static void debug(Object o) {
    println(Const.LOG_DEBUG, o, true);
  }
}
/*
  $Log: Log.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.8  2001/01/17 09:55:45  schubige
  Logger update

  Revision 1.7  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2000/11/16 16:22:50  schubige
  javap / bytecode checkin

  Revision 1.5  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.4  2000/10/17 15:35:59  schubige
  Added watcher preferences

  Revision 1.3  2000/10/10 15:15:04  hitz
  Mistake, re-created old file.

  Revision 1.1  2000/10/09 06:47:56  schubige
  Updated logger stuff
  
*/
