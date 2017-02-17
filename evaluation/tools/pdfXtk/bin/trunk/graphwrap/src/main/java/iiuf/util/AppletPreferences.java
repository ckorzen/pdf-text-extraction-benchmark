package iiuf.util;

import java.util.Vector;
import iiuf.awt.Applet;

/**
   Applet based preferences.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class AppletPreferences
implements
  PreferencesStore
{
  Applet applet;
  
  public AppletPreferences(Applet applet_) {
    applet = applet_;
  }
  
  public Object get(String key) {
    return applet.getParameter(key);
  }
  
  public void getMulti(String prefix, Vector result) {
    String[][] info = applet.getParameterInfo();
    for(int i = 0; i < info.length; i++)
      if(info[i][0].startsWith(prefix))
	result.add(get(info[i][0]));
  }

  public void remove(String key) {}
  public void set(String key, Object value) {}
  public void store() {}
  public void load() {}
}
/*
  $Log: AppletPreferences.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.6  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/11/10 09:53:07  schubige
  iiuf tree cleanup iter 3

  Revision 1.4  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.3  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.2  1999/11/26 10:00:24  schubige
  updated for new awt package

  Revision 1.1  1999/09/14 11:51:16  schubige
  Added preferences classes
  
*/
