package iiuf.util;

/**
   Preferences handler.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class PreferencesHandler
{ 
  private String hkey;
  
  public PreferencesHandler() {
    hkey = "";
  }

  public PreferencesHandler(Class cls, String id) {
    hkey = cls.getName() + "." + id + ".";
  }

  // set methods

  public void set(String key, boolean value) {
    set(key, new Boolean(value));
  }

  public void set(String key, char value) {
    set(key, new Character(value));
  }

  public void set(String key, short value) {
    set(key, new Short(value));
  }

  public void set(String key, int value) {
    set(key, new Integer(value));
  }

  public void set(String key, long value) {
    set(key, new Long(value));
  }

  public void set(String key, float value) {
    set(key, new Float(value));
  }

  public void set(String key, double value) {
    set(key, new Double(value));
  }

  public void set(String key, Object value) {
    Preferences.set(hkey + key, value);
  }
  
  // get methods

  public boolean getBoolean(String key, boolean dflt) {
    return ((Boolean)get(key, new Boolean(dflt))).booleanValue();
  }

  public char getChar(String key, char dflt) {
    return ((Character)get(key, new Character(dflt))).charValue();
  }

  public short getShort(String key, short dflt) {
    return ((Short)get(key, new Short(dflt))).shortValue();
  }

  public int getInt(String key, int dflt) {
    return ((Integer)get(key, new Integer(dflt))).intValue();
  }

  public long getLong(String key, long dflt) {
    return ((Long)get(key, new Long(dflt))).longValue();
  }

  public float getFloat(String key, float dflt) {
    return ((Float)get(key, new Float(dflt))).floatValue();
  }

  public double getDouble(String key, double dflt) {
    return ((Double)get(key, new Double(dflt))).doubleValue();
  }

  public Object get(String key, Object deflt) {
    return Preferences.get(hkey + key, deflt);
  }

  public Object get(String key) {
    return Preferences.get(hkey + key);
  }
}
/*
  $Log: PreferencesHandler.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/09/14 11:51:16  schubige
  Added preferences classes
  
*/
