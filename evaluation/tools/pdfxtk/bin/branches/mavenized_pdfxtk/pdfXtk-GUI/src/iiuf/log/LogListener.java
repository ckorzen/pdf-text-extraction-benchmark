package iiuf.log;

import java.util.EventListener;

/**
   Log listener interface.

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface LogListener
  extends
  EventListener 
{
  public void log(LogMessage m);
  public int  getPriorityMask();
}

/*
  $Log: LogListener.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.1  2001/01/17 09:56:31  schubige
  Logger update
  
*/
