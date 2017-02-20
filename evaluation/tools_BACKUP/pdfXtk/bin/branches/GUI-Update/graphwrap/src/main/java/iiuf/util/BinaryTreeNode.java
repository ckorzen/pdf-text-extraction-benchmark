package iiuf.util;

/**
   Implementation of a binary tree node.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class BinaryTreeNode 
  extends
  TreeNode 
{
  protected long           key;
  protected BinaryTreeNode left;
  protected BinaryTreeNode right;
  
  /**
     Creates a new tree node with the given key and value.
     @param key_ The key of this node.
     @param value The value of this node.
  */
  public BinaryTreeNode(long key_, Object value) {
    super(value);
    key   = key_;
  }
  
  public TreeNode[] getChilds() {
    return new TreeNode[] {right, left};
  }
  
  public String toString() {
    return "key=\"" + key + "\" value=\"" + value + "\"";
  }
}

/*
  $Log: BinaryTreeNode.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes
  
*/
