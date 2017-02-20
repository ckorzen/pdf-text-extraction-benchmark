package iiuf.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import iiuf.util.Util;

/**
   Unicode translator base class.

   Usually sublcasses store an instance of itslef in a public static variable called "trans".
   <code>public static UnicodeTranslator trans = new UTXxxxx();</code>

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class UnicodeTranslator {
  
  protected UnicodeTranslator(String[] unicode2table) {
    if(unicode2table == null)
      throw new IllegalArgumentException("unicode2table is null");

    Field[] fields = Unicode.class.getFields();
    
    for(int i = 0; i < fields.length; i++) {
      if(fields[i].getModifiers() != (Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC))
	continue;
      try{
	Object o = fields[i].get(Unicode.class);
	if(o instanceof String) {
	  boolean found = false;
	  for(int j = 0; j < unicode2table.length; j += 2)
	    found |= o.equals(unicode2table[j]);
	  if(!found) throw new IllegalArgumentException("Unicode char '" + o + "' (" + fields[i].getName() + 
							") not mapped by class " + getClass().getName());
	}
      } catch(IllegalAccessException e) {Util.printStackTrace(e);}
    }
  }
  
  /**
     Translate from a native representation to unicode.
     
     @param nstr The native representation.
     @return The unicode translation.
  */
  public abstract String getUnicode(String nstr);
  
  /**
     Translate from unicode to the native representation.
     
     @param unicode The unicode representation.
     @return The native translation.
  */
  public abstract String getNative(String unicode);
  
  /**
     Translates from this native representaiton to another native representation.
     
     This implementation uses unicode as an intermediate format. 
     (<code>return translator.getNative(getUnicode(nstr));</code>) 
     Subclasses may use more sophisticated (lossless) transformations.
     
     @param nstr The native represenation of the string.
     @param translator The translator class to use.
     @return The string in the translator's native representaiton.
  */
  public String trans(String nstr, UnicodeTranslator translator) {
    return translator.getNative(getUnicode(nstr));
  }
  
  /**
     Utility for building an inverse translation table.
     
     @param table The source table.
     @return The inverse (swapped) table.
  */
  protected static String[] invTable(String[] table) {
    String[] result = new String[table.length];
    for(int i = 0; i < table.length; i += 2) {
      result[i]     = table[i + 1];
      result[i + 1] = table[i];
    }
    return result;
  }
  
  public static void main(String[] argv) {
    Field[] fields = Unicode.class.getFields();
    
    for(int i = 0; i < fields.length; i++) {
      if(fields[i].getModifiers() != (Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC))
	continue;
      try{
	Object o = fields[i].get(Unicode.class);
	if(o instanceof String)
	  System.out.println(o + ":" + Integer.toHexString(((String)o).charAt(0)) + ":" + fields[i].getName());
      } catch(IllegalAccessException e) {Util.printStackTrace(e);}
    }
  }
}

/*
  $Log: UnicodeTranslator.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.1  2000/04/25 12:11:15  schubige
  pre bibtex restart commit
  
*/
