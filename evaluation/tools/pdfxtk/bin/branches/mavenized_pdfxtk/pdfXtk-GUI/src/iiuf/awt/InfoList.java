package iiuf.awt;

import java.util.Vector;

/**
   A java.awt.List specialization that supports item associated infos.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
   @see java.awt.List
*/
public class InfoList
  extends
  java.awt.List 
{
  
  /** @serial */
  Vector info_v = new Vector();

  public InfoList() {
    super();
  }
  public InfoList(int lines) {
    super(lines);
  }
  public InfoList(int lines, boolean multi){ 
    super(lines, multi);
  }
  
  public void add(String item, Object info) {
    info_v.addElement(info);
    super.add(item);
  }
  
  public void add(String item, int index, Object info) {
    if(index == -1)
      info_v.addElement(info);
    else
      info_v.insertElementAt(info, index);
    super.add(item, index);
  }
  
  public Object getSelectedInfo() {
    return info_v.elementAt(getSelectedIndex());
  }
  
  public Object[] getSelectedInfos() {
    int[] indexes = getSelectedIndexes();
    Object[] result = new Object[indexes.length];
    for(int i = 0; i < result.length; i++)
      result[i] = info_v.elementAt(indexes[i]);
    return result;
  }
  
  public void remove(int idx) {
    info_v.removeElementAt(idx);
    super.remove(idx);
  }

  public void remove(String item) {
    String[] items = getItems();
    for(int i = 0; i < items.length; i++)
      if(items[i].equals(item)) {
	remove(i);
	break;
      }
  }
  
  public Object getInfo(int idx) {
    return info_v.elementAt(idx);
  }

  public Object[] getInfos() {
    Object[] result = new Object[info_v.size()];
    for(int i = 0; i < result.length; i++)
    result[i] = getInfo(i);
    return result;
  }

  public boolean contains(String item) {
    String[] items = getItems();
    for(int i = 0; i < items.length; i++)
      if(items[i].equals(item))
	return true;
    return false;
  }

  public void removeAll() {
    info_v = new Vector();
    super.removeAll();
  }
}
/*
  $Log: InfoList.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/11/29 12:04:34  schubige
  some 'deprecated' fixes

  Revision 1.3  2000/11/10 08:49:58  schubige
  iiuf tree cleanup iter 2

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.3  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
