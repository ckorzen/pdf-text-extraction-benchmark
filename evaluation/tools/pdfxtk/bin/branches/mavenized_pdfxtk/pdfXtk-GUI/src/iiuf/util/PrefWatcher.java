package iiuf.util;

import java.io.Serializable;

/**
   Interface for preference watchers.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface PrefWatcher {
  
  public Serializable watch(Object o, Serializable preferences);
  public Class        watchedClass();
}

/*
  $Log: PrefWatcher.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.2  2000/10/19 08:03:45  schubige
  Intermediate graph component related checkin
  
*/
