package iiuf.util;

import java.util.EventListener;

/**
   Progress listener interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public interface ProgressListener 
  extends
  EventListener 
{
  public void operationStart(String description);  
  public void operationStop();
  public void operationProgress(int amount, int of);
}

/*
  $Log: ProgressListener.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/03 15:23:51  schubige
  graph stuff beta

  Revision 1.2  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added
  
*/
