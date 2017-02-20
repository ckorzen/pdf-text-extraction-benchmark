package iiuf.util;

import java.io.InputStream;
import java.io.PrintStream;

/**
   Copies data from an input stream to a print stream.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class CopyThread 
  extends
  Thread 
{  
  protected InputStream src;
  protected PrintStream dst;
  
  public CopyThread(InputStream src_, PrintStream dst_) {
    src = src_;
    dst = dst_;
    start();
  }
  
  public void run() {
    for(;;) {
      try{
	int c = src.read();
	if(c == -1)
	  return;
	dst.write(c);
      } catch(Exception e) {
	Util.printStackTrace(e);
	break;
      }
    }
  }
}

/*
  $Log: CopyThread.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.1  2001/05/30 11:25:18  schubige
  Added TJTest
  
*/
