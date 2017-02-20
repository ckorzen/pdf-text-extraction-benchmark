package iiuf.awt;

import java.util.EventListener;

import iiuf.db.Proxy;

/**
   Database find listener.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
  */
public interface FindListener 
  extends
  EventListener
{
  public void find(Proxy proxy, int op);
}

/*
  $Log: FindListener.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.3  1999/11/26 10:00:32  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***
  
*/
