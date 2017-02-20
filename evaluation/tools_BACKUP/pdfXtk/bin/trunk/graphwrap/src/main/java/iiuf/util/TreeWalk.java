package iiuf.util;

/**
   Tree walker interface.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface TreeWalk {
  public void node(TreeNode node, Object misc);
}
/*
  $Log: TreeWalk.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:13  schubige
  Added red black and binary tree classes

*/
