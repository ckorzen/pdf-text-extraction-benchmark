package iiuf.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import iiuf.db.Proxy;
import iiuf.db.Field;
import iiuf.db.Container;
import iiuf.db.Text;
import iiuf.db.NoDatabaseException;
import iiuf.db.ConnectionException;
import iiuf.util.Trans;

/**
   Database preferences.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class DBPreferences
  implements
  PreferencesStore
{
  private final static String F_KEY    = "key";
  private final static String F_NAME   = "name";
  private final static String F_USER   = "user";
  private final static String F_VALUE  = "value";
  private final static int    REQ_SIZE = 16;
  
  private Hashtable prefs = new Hashtable();
  private String    user;
  private String    name;
  private Proxy     prefsdb;

  public DBPreferences(String name_, String user_, Proxy prefsdb_) {
    name    = name_;
    user    = user_;
    prefsdb = prefsdb_;
    load();
  }
  
  public void set(String key, Object value) {
    prefs.put(key, value);
  }

  public void remove(String key) {
    prefs.remove(key);
  }
  
  public Object get(String key) {
    return prefs.get(key);
  }
  
  public void getMulti(String prefix, Vector result) {
    String[] keys = (String[])prefs.keySet().toArray(new String[prefs.size()]);
    for(int i = 0; i < keys.length; i++)
      if(keys[i].startsWith(prefix))
	result.add(prefs.get(keys[i]));
  }

  public void store() {
    try {
      Enumeration e = prefs.keys();
      while(e.hasMoreElements()) {
	String key = (String)e.nextElement();
	store(key, new String(Trans.uuEncode
			      (Trans.zip
			       (Trans.object2byte
				(prefs.get(key))), 0777, "P")));
      }
    } catch(Exception e) {Proxy.deepShit(e);}
  }
  
  private void store(String key, String value) 
    throws ConnectionException, NoDatabaseException, IOException {
    if(value.length() > 800) {
      System.err.println("ignored:" + key + 
			 ", length(" + value.length() + 
			 ") > 800 (fm pro limitation)");
      return;
    }
    Proxy  req = prefsdb.newInstance();
    req.getField(F_KEY).compare(key, Field.EQUALS);
    req.getField(F_NAME).compare(name, Field.EQUALS);
    req.getField(F_USER).compare(user, Field.EQUALS);
    if(req.find(0, 1, Proxy.AND).length > 0) {
      req.getField(F_VALUE).set(0, value);
      req.update();
    }
    else {
      req = prefsdb.newInstance();
      req.getField(F_KEY). set(0,key);
      req.getField(F_NAME).set(0,name);
      req.getField(F_USER).set(0,user);
      req.getField(F_VALUE).set(0, value);
      req.insert();
    }
  }
  
  public void load() {
    try {
      int     index    = 0;
      Proxy[] response = null;
      Proxy   req      = prefsdb.newInstance();
      req.getField(F_NAME).compare(name, Field.EQUALS);
      req.getField(F_USER).compare(user, Field.EQUALS);
      do {
	response = req.find(index, REQ_SIZE, Proxy.AND);
	for(int i = 0; i < response.length; i++) {
	  String key   = ((Text)response[i].getField(F_KEY)).value(0);
	  Object value = 
	    Trans.byte2object
	    (Trans.unzip
	     (Trans.uuDecode(((Text)response[i].getField(F_VALUE)).value(0).getBytes())));
	  prefs.put(key, value);
	}
	index += response.length;
      } while(response.length == REQ_SIZE); 
    } catch(Exception e) {Proxy.deepShit(e);}
  }
}
/*
  $Log: DBPreferences.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2001/01/04 09:58:50  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.3  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  1999/09/14 11:51:16  schubige
  Added preferences classes
  
*/
