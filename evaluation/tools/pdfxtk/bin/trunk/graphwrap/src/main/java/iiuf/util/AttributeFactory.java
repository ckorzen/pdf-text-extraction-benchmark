package iiuf.util;

/**
   Attribute factory interface.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface AttributeFactory {
  public Object newAttribute(Attributable attributable, Object[] args);
}

/*
  $Log: AttributeFactory.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/07/14 13:48:10  schubige
  Added graph stuff
  
*/
