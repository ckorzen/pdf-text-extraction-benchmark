package iiuf.util;

import java.util.Observer;

/**
   Attributable interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface Attributable {
  public Object   get(int id);
  public void     set(int id, Object value);
  public boolean  has(int id);
  public void     commit();
  public void     addObserver(int[] ids, Observer observer, Object tag);
  public void     removeObserver(Observer observer, Object tag);
  public Object[] getAttributes();
}

/*
  $Log: Attributable.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.7  2001/03/21 19:34:06  schubige
  started with dom stuff

  Revision 1.6  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.4  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.3  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:48:10  schubige
  Added graph stuff
*/
