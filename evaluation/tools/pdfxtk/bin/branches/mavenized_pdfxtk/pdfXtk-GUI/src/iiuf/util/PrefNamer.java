package iiuf.util;

/**
   Preferences naming generator interface.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface PrefNamer {
  public abstract String getName(Object o);
}

/*
  $Log: PrefNamer.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.1  2000/10/17 15:35:59  schubige
  Added watcher preferences
  
*/
