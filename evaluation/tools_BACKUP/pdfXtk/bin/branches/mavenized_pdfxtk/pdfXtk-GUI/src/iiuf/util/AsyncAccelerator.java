package iiuf.util;

/**
   AsyncInvocation accelerator. 
   Implement this interface for Async invocation that should/can not be called by the refelection
   invocation mechanism such as non-public classes and performance sensitive calls.

   (c) 2000, 2001, IIUF, DIUF<p>
      
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface AsyncAccelerator {
  public void handle(AsyncInvocation.Invocation invocation, AsyncInvocation.Result result);
}

/*
  $Log: AsyncAccelerator.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/09 07:53:09  schubige
  early checkin for DCJava
  
*/
