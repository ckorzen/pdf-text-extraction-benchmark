package iiuf.awt;

import java.applet.AppletStub;
import java.applet.AppletContext;
import java.net.URL;
import java.util.Hashtable;
import java.awt.Color;

import iiuf.util.Preferences;
import iiuf.util.AppletPreferences;

/**
   Standalone & preferences aware applet.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public abstract class Applet
  extends 
  java.applet.Applet
{
  public  static boolean   STANDALONE = false;
  /** @serial */
  private static Hashtable params = new Hashtable();
  /** @serial */
  boolean                  active;
  
  public Applet() {
    if(STANDALONE)
      setStub(new AppletStub() {
	  public void appletResize(int width, int height) {}
	  
	  public AppletContext getAppletContext() {
	    return null;
	  }
	  
	  public URL getCodeBase() {
	    return null;
	  }
	  
	  public URL getDocumentBase() {
	    return null;
	  }
	  
	  public String getParameter(String key) {
	    return (String)params.get(key);
	  }
	  
	  public boolean isActive() {
	    return active;
	  }
	});
    Preferences.addStore(new AppletPreferences(this));

    setBackground(new Color(0xC0C0C0));
  }
  
  public static void exit(int code) {
    try{System.exit(code);} 
    catch(Exception e) {}
  }
  
  public static Applet standalone(String[] argv, Class applet, 
				  int width, int height) 
    throws InstantiationException, IllegalAccessException {
    STANDALONE = true;
    params     = new Hashtable();
    for(int i = 0; i < argv.length; i += 2)
      params.put(argv[i + 0], argv[i + 1]);
    
    //  Preferences.addStore(new FilePreferences(applet.getName()));
    
    Applet result = (Applet)applet.newInstance();
    new AppletFrame(applet.getName(), result, width, height);
    return result;
  }
}
/*
  $Log: Applet.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.6  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/01/18 11:15:39  schubige
  First beta release of vote server / votlet

  Revision 1.4  2000/01/11 09:36:50  schubige
  added voter stuff

  Revision 1.3  1999/11/26 10:00:23  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***

  Revision 1.3  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes

  Revision 1.2  1999/09/14 11:59:39  schubige
  Added @serial and transient for javadoc

  Revision 1.1  1999/09/14 11:51:55  schubige
  Added applet frame classes

*/
