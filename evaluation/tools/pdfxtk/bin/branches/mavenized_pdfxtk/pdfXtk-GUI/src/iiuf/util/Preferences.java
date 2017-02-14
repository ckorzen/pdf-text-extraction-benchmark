package iiuf.util;

import java.io.Serializable;
import java.util.Vector;
import java.util.Hashtable;
import java.awt.Component;

import iiuf.log.Log;

/**
   Preferences implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Preferences
{
  static private Vector stores = new Vector();
  
  static {
    addStore(new SysPreferences());
  }
  
  public static void addStore(PreferencesStore store) {
    stores.insertElementAt(store, 0);
  }
  
  public static void set(String key, Object value) {
    for(int i = 0; i < stores.size(); i++)
      ((PreferencesStore)stores.elementAt(i)).set(key, value);
  }

  public static void remove(String key) {
    for(int i = 0; i < stores.size(); i++)
      ((PreferencesStore)stores.elementAt(i)).remove(key);
  }
  
  public static Object get(String key) {
    Object result = null;
    
    for(int i = 0; i < stores.size(); i++) {
      result = ((PreferencesStore)stores.elementAt(i)).get(key);
      if(result != null) break;
    }
    
    return result;
  }
  
  public static Object[] getMulti(String prefix) {
    Vector result = new Vector();
    for(int i = 0; i < stores.size(); i++)
    ((PreferencesStore)stores.elementAt(i)).getMulti(prefix, result);
    return result.toArray();
  }
  
  public static Object get(String key, Object deflt) {
    Object result = get(key);
    if(result == null) set(key, deflt);
    result = get(key);
    return result == null ? deflt : result;
  }
  
  public static void store() {
    for(int i = 0; i < stores.size(); i++)
      ((PreferencesStore)stores.elementAt(i)).store();
  }
  
  public static boolean isWatched(Object object) {
    return paths.containsKey(object);
  }
  
  private static Hashtable paths = new Hashtable();
  
  public synchronized static String watch(Object object, String path) {
    iiuf.swing.Preferences.init();
    if(isWatched(object)) return (String)paths.get(object);
    if(pathinv.containsKey(path)) throw new IllegalArgumentException("Duplicate path:" + path);
    paths.put(object, path);
    pathinv.put(path, object);
    
    Vector ws = getWatchers(object);
    
    for(int i = 0; i < ws.size(); i++) {
      String pkey = path + "/" + ws.elementAt(i).getClass().getName();
      set(pkey, ((PrefWatcher)ws.elementAt(i)).watch(object, (Serializable)get(pkey)));
    }

    return path;
  }

  public synchronized static String watch(Object parent, Object object, String key) {
    if(isWatched(object)) return (String)paths.get(object);
    String path = unique(getPath(parent) + "/" + key);
    return watch(object, path);
  }
  
  public static String watch(Object parent, Object object) {    
    return watch(parent, object, getName(getPath(parent), object));
  }
  
  public static String watch(Component cmp) {
    return watch(cmp.getParent(), cmp);
  }
  
  public static String watch(Object object) {
    return watch(null, object);
  }
  
  public synchronized static String getPath(Object o) {
    if(o == null) return "";
    String result = (String)paths.get(o);
    if(result == null) throw new IllegalArgumentException("Unknown object:" + o);
    return result;
  }

  public synchronized static Object getObjectForPath(String path) {
    if(path == null) return null;
    return pathinv.get(path);
  }

  private static Vector watchers = new Vector();
  public static void register(PrefWatcher watcher) {
    watchers.add(watcher);
  }
  
  private static Vector getWatchers(Object o) {
    Vector result = new Vector();
    
    for(int i = 0; i < watchers.size(); i++)
      if(((PrefWatcher)watchers.elementAt(i)).watchedClass().isAssignableFrom(o.getClass()))
	result.add(watchers.elementAt(i));
    
    return result;
  }
  
  private static Hashtable namers = new Hashtable();
  public static void register(Class cls, PrefNamer namer) {
    namers.put(cls, namer);
  }
  
  private static String getName(String parent, Object object) {
    iiuf.swing.Preferences.init();
    String result = null;
    Class  ccls   = object.getClass();
    do {
      PrefNamer pn = (PrefNamer)namers.get(ccls);
      if(pn != null)
	result = pn.getName(object);
      ccls = ccls.getSuperclass();
    } while(result == null);
    
    return unique(parent, result);
  }
  
  static Hashtable pathinv = new Hashtable();
  private static String unique(String parent, String name) {
    String result;    
    if(!pathinv.containsKey(parent + "/" + name)) 
      result = name;
    else {
      int i = 0; 
      while(pathinv.containsKey(parent + "/" + name + i)) i++;
      result = name + i;
    }
        
    return result;
  }
  
  private static String unique(String path) {
    String result;    
    if(!pathinv.containsKey(path))
      result = path;
    else {
      int i = 0; 
      while(pathinv.containsKey(path + i)) i++;
      result = path + i;
    }
    
    return result;
  }
}

/*
  $Log: Preferences.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.14  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.13  2001/03/09 21:24:59  schubige
  Added preferences to edge editor

  Revision 1.12  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.11  2001/01/04 09:58:50  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.10  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.9  2000/11/24 17:50:44  schubige
  Tinja IDE beta 1

  Revision 1.8  2000/11/20 17:36:57  schubige
  tinja project ide

  Revision 1.7  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.6  2000/10/19 08:03:46  schubige
  Intermediate graph component related checkin

  Revision 1.5  2000/10/17 15:35:59  schubige
  Added watcher preferences

  Revision 1.4  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.3  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.2  1999/11/26 10:00:27  schubige
  updated for new awt package

  Revision 1.1  1999/09/14 11:51:16  schubige
  Added preferences classes
  
*/
