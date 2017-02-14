package iiuf.swing;

import java.util.Collection;
import javax.swing.event.ChangeListener;

/**
   A selection model that supports sets of items.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface SetSelectionModel {
  public void     add(Object o);
  public void     addAll(Collection os);
  public void     remove(Object o);
  public void     removeAll(Collection os);
  public void     clearSelection();
  public boolean  isEmpty();
  public Object[] getSelection();
  public Object[] getSelection(Class cls);
  public boolean  isSelected(Object o);
  public void     addChangeListener(ChangeListener listener);
  public void     removeChangeListener(ChangeListener listener);
  public int      size();
  public int      size(Class cls);
}

/*
  $Log: SetSelectionModel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.3  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/29 08:03:55  schubige
  SourceWatch beta debug iter 1

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
