package iiuf.swing.propertiespanel;

import javax.swing.JButton;
import javax.swing.JList;

/**
   List property access interface.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface ListAccess {
  public Object[] decode(String value);
  public String   encode(Object[] values);
  public JButton  newAddButton(JList l);
  public JButton  newRemoveButton(JList l);
}

/*
  $Log: ListAccess.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/12/28 09:30:37  schubige
  SourceWatch beta
  
*/
