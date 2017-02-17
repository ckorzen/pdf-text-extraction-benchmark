package iiuf.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
   Nested exceptions.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class NestedException

  extends
  Exception {
  
  /** @serial */
  public Exception exception;

  public NestedException() {
    this((Exception)null);
  }
  
  public NestedException(Exception exception_, String msg) {
    super(msg);
    exception = exception_;
  }
  
  public NestedException(Exception exception_) {
    super(exception_ == null ? "<null>" : exception_.getMessage());
    exception = exception_;
  }
  
  public NestedException(String msg) {
    super(msg);
  }
  
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintWriter out) {
    if(exception != null)
      exception.printStackTrace(out);
    super.printStackTrace(out);
  }

  public void printStackTrace(PrintStream out) {
    if(exception != null)
      exception.printStackTrace(out);
    super.printStackTrace(out);
  }
}
/*
  $Log: NestedException.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.8  2001/05/11 11:30:26  schubige
  fns demo final

  Revision 1.7  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2000/05/26 09:45:44  schubige
  Added iiuf.io.fs and iiuf.os

  Revision 1.5  2000/01/11 09:36:50  schubige
  added voter stuff

  Revision 1.4  1999/11/26 08:44:25  schubige
  cleanup, move to awt package

  Revision 1.3  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/

