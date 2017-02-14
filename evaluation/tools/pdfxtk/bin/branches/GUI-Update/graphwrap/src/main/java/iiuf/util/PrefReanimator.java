package iiuf.util;

import java.io.Serializable;

/**
   Interface for preferences states that are able to reanimate (create) instances.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface PrefReanimator 
  extends
  Serializable
{
  public Object reanimate(String path, Object prefs[]);
}

/*
  $Log: PrefReanimator.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/09 07:53:09  schubige
  early checkin for DCJava
  
*/
