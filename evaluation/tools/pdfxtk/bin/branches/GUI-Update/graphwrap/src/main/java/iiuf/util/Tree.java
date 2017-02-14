package iiuf.util;

import java.util.Vector;

/**
   Implementation of tree base class.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class Tree {
  
  protected TreeNode root;
  protected TreeNode NIL;
  private   Object[] t_array;
  
  public Tree(TreeNode NIL_) {
    NIL  = NIL_;
    root = NIL;
  }

  public String toString() {
    return root == NIL ? "<node/>" : root.toString(0, NIL);
  }

  public TreeNode nil() {
    return NIL;
  }

  /**
     Adds a node to the tree.
     @param node The node to add.
  */
  abstract public void add(TreeNode node);
  /**
     Removes a node from the tree.
     @param node The node to add.
  */
  abstract public void remove(TreeNode node);
  /**
     Walks the tree preorder.
     
     @param root    Starting node.
     @param handler The node handler.
     @param misc    Additional information passed to the handler.
  */
  public void preorderWalk(TreeNode x, TreeWalk handler, Object misc) {
    if(x != NIL) {
      handler.node(x, misc);
      TreeNode[] childs = x.getChilds();
      for(int i = 0; i < childs.length; i++)
	preorderWalk(childs[i], handler, misc);
    }
  }
  /**
     Walks the tree postorder.
     
     @param root    Starting node.
     @param handler The node handler.
     @param misc    Additional information passed to the handler.
  */
  public void postorderWalk(TreeNode x, TreeWalk handler, Object misc) {
    if(x != NIL) {
      TreeNode[] childs = x.getChilds();
      for(int i = 0; i < childs.length; i++)
	postorderWalk(childs[i], handler, misc);
    }
    handler.node(x, misc);
  }

  public void changed() {
    t_array = null;
  }

  public Object[] toArray() {
    if(t_array == null) {
      Vector   result_v = new Vector();
      preorderWalk(root, new TreeWalk() {
	  public void node(TreeNode node, Object misc) {
	    ((Vector)misc).addElement(node.getValue());
	  }
	}, result_v);
      t_array  = new Object[result_v.size()];
      for(int i = 0; i < t_array.length; i++)
      t_array[i] = result_v.elementAt(i);
    }
    return t_array;
  }

  protected void error() {
    System.out.println(this);
    System.out.println("Error - exit(1)");
    System.exit(1);
  }
  
  protected String verify() {
    // check root
    if(root != NIL && root.p != NIL) {
      System.out.println("Root not NIL");
      error();
    }
    // check parent pointers
    preorderWalk(root, 
		 new TreeWalk() {
		     public void node(TreeNode node, Object misc) {
		       TreeNode[] childs = node.getChilds();
		       for(int i = 0; i < childs.length; i++)
			 if(childs[i] != NIL &&
			    childs[i].p != node) {
			   System.out.println("Wrong parent pointer:" + childs[i] + " parent:" + childs[i].p);
			   System.out.println(node);
			   error();
			 }
		     }
		   }, null);
    return "(T,OK)";
  }
}
/*
  $Log: Tree.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:13  schubige
  Added red black and binary tree classes
  
*/
