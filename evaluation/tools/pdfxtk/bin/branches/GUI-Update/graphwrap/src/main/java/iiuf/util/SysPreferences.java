package iiuf.util;

import java.util.Vector;

/**
   System preferences.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class SysPreferences
implements
  PreferencesStore
{
  
  public Object get(String key) {
    try{
      return System.getProperty(key);
    } catch(Exception e) {
      return null;
    }
  }
  
  public void remove(String key) {
    System.getProperties().remove(key);
  }

  public void set(String key, Object value) {}
  public void load()  {}
  public void store() {}
  
  public void getMulti(String prefix, Vector result) {
    String[] keys = (String[])System.getProperties().keySet().toArray(new String[System.getProperties().size()]);
    for(int i = 0; i < keys.length; i++)
      if(keys[i].startsWith(prefix))
	result.add(System.getProperty(keys[i]));
  }
}
/*
  $Log: SysPreferences.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2001/01/04 09:58:50  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.3  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.2  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.1  1999/09/14 11:51:17  schubige
  Added preferences classes
  
*/
