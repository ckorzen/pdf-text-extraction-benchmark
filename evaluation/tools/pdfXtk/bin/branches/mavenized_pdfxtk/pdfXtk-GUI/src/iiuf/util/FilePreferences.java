package iiuf.util;

import java.util.Hashtable;
import java.util.Vector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import iiuf.util.Util;
import iiuf.log.Log;
import iiuf.util.Strings;

/**
   Implementation of file based prefernces store.

   Preferences are stored in the user home directory undser the name given to the creator.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class FilePreferences
  implements
  PreferencesStore
{
  private static final String[] NODOT = 
  {"Mac",
   "OS/2",
   "Windows",
  };
  
  private Hashtable appPrefs = new Hashtable();
  private Hashtable uiPrefs  = new Hashtable();
  private String    name;
  private String    appPrefFile;
  private String    uiPrefFile;
  
  public FilePreferences(String name_) {
    name = name_;
    
    boolean nodot = false;
    for(int i = 0; i < NODOT.length; i++)
      if(((String)Preferences.get("os.name")).startsWith(NODOT[i])) {
	nodot = true;
	break;
      }
    
    String base = 
      (String)Preferences.get("user.home") + 
      (String)Preferences.get("file.separator") + 
      (nodot ? "" : ".");
    
    appPrefFile = base +name + ".cfg";
    uiPrefFile  = base + name + ".ui";      
    load();    
  }
  
  public void set(String key, Object value) {
    if(key.charAt(0) == '/')
      uiPrefs.put(key, value);
    else
      appPrefs.put(key, value);
  }
  
  public void remove(String key) {
    uiPrefs.remove(key);
    appPrefs.remove(key);
  }

  public Object get(String key) {
    if(key.charAt(0) == '/')
      return uiPrefs.get(key);
    else
      return appPrefs.get(key);
  }
  
  public void getMulti(String prefix, Vector result) {
    if(prefix.charAt(0) == '/') {
      String[] keys = (String[])uiPrefs.keySet().toArray(new String[uiPrefs.size()]);
      for(int i = 0; i < keys.length; i++)
	if(keys[i].startsWith(prefix))
	  result.add(uiPrefs.get(keys[i]));
    } else {
      String[] keys = (String[])appPrefs.keySet().toArray(new String[appPrefs.size()]);
      for(int i = 0; i < keys.length; i++)
	if(keys[i].startsWith(prefix))
	  result.add(appPrefs.get(keys[i]));
    }
  }

  public void store() {
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(appPrefFile)));
      out.writeObject(appPrefs);
      out.close();
    } catch(Exception e) {Util.printStackTrace(e);}
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(uiPrefFile)));
      out.writeObject(uiPrefs);
      out.close();
    } catch(Exception e) {Util.printStackTrace(e);}
  }
  
  public void load() {
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(appPrefFile)));
      appPrefs = (Hashtable)in.readObject();
      in.close();
    } 
    catch(FileNotFoundException e) {}
    catch(Exception e) {Util.printStackTrace(e);}

    if(appPrefs == null)
      appPrefs = new Hashtable();
    
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(uiPrefFile)));
      uiPrefs = (Hashtable)in.readObject();
      in.close();
    }
    catch(FileNotFoundException e) {}
    catch(Exception e) {Util.printStackTrace(e);}

    if(uiPrefs == null)
      uiPrefs = new Hashtable();
  }
}
/*
  $Log: FilePreferences.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.7  2001/01/04 09:58:50  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.6  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.5  2000/11/27 16:10:45  schubige
  tinja IDE beta 2

  Revision 1.4  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.3  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.2  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.1  1999/09/14 11:51:16  schubige
  Added preferences classes
  
*/
