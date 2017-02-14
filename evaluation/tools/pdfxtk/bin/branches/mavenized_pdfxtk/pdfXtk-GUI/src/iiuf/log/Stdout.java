package iiuf.log;

import java.net.InetAddress;

/**
   Standard output logger implementation.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Stdout
  implements
  LogMessageListener 
{
  public void newConnection(InetAddress host) {
  }

  public void handle(LogMessage m) {
    System.out.println(m);
  }
}

/*
  $Log: Stdout.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.4  2001/01/17 09:55:46  schubige
  Logger update

  Revision 1.3  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.1  2000/10/09 06:47:56  schubige
  Updated logger stuff
  
*/
