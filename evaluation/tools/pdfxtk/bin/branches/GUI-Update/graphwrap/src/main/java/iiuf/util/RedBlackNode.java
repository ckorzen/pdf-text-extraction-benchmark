package iiuf.util;

/**
   Red black tree node implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class RedBlackNode 
  extends
  BinaryTreeNode 
{
  boolean color;
  
  public RedBlackNode(long key, Object value) {
    super(key, value);
  }
    
  public String toString() {
    return "color=" + (color == RedBlackTree.RED ? "RED " : "BLACK ") + super.toString();
  }
}

/*
  $Log: RedBlackNode.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes
  
*/
