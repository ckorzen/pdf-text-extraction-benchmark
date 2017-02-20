package iiuf.util;

/**
   Throw this if something is not implemented.
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
     */
public class NotImplementedException
  extends
  RuntimeException
{
  public NotImplementedException() {}
  public NotImplementedException(String msg) {super(msg);}
}

/*
  $Log: NotImplementedException.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/05/11 14:40:30  schubige
  made ontologies modifiable

  Revision 1.2  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/20 17:39:08  schubige
  tinja project ide
  
*/
