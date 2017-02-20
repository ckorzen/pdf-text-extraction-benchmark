package iiuf.log;

import java.net.InetAddress;
import java.util.EventListener;

/**
   Log message listener interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface LogMessageListener 
  extends
  EventListener
{
  public void newConnection(InetAddress client);
  public void handle(LogMessage m);
}

/*
  $Log: LogMessageListener.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.1  2001/01/17 09:55:45  schubige
  Logger update

  Revision 1.3  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.2  2000/10/09 07:29:15  schubige
  Features, features, features

  Revision 1.1  2000/10/05 14:59:30  schubige
  Added loging stuff
  
*/
