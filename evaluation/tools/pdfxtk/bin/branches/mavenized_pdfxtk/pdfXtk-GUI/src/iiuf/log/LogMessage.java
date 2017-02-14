package iiuf.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import iiuf.util.Util;

/**
   Log message encapuslation.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class LogMessage 
  implements
  java.io.Serializable
{
  public long      time;
  public String    thread;
  public int       priority;
  public String    message;
  public String    exception;
  public String    exceptionMsg = "";
  
  public LogMessage(Thread thread, int priority, String message) {
    this(thread, priority, message, "");
  }
  
  public LogMessage(Thread thread, int priority, String message, Throwable exception) {
    this(thread, priority, message, getException(exception));
    exceptionMsg = exception == null ? "" : exception.getMessage();
  }
  
  public LogMessage(Thread thread_, int priority_, String message_, String exception_) {
    thread    = thread_.getName();
    priority  = priority_;
    message   = message_ == null ? "<null>" : message_;
    exception = exception_;
  }

  public String toString() {
    return new Date(time) + " " + thread + ":" + message + ":" + getException(); 
  }
  
  public String getExceptionMessage() {
    return exceptionMsg;
  }

  public String getException() {
    return exception;
  }
  
  private static String getException(Throwable exception) {
    if(exception == null) return "";
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream           out  = new PrintStream(bout);
    Util.printStackTrace(exception, out);
    out.flush();
    String result = bout.toString(); 
    out.close();
    return result;
  }
}

/*
  $Log: LogMessage.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.1  2001/01/17 09:55:45  schubige
  Logger update

  Revision 1.5  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.3  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.2  2000/10/09 06:47:56  schubige
  Updated logger stuff

  Revision 1.1  2000/10/05 14:59:30  schubige
  Added loging stuff
  
*/
