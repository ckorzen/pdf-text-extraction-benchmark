package iiuf.util;

import java.util.Vector;

/**
   Preferences store interface.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public interface PreferencesStore
{
  Object get(String key);
  void   getMulti(String prefix, Vector result);
  void   set(String key, Object value);
  void   remove(String key);
  void   load();
  void   store();
}
/*
  $Log: PreferencesStore.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2001/01/04 09:58:50  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.3  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.2  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.1  1999/09/14 11:51:16  schubige
  Added preferences classes
  
*/
