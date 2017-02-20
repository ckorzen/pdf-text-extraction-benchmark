package iiuf.util;

import java.util.Vector;

/**
   Generic binary tree implementation.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class BinaryTree 
extends
Tree
{
  public BinaryTree() {
    super(null);
  }
  
  protected BinaryTree(BinaryTreeNode NIL) {
    super(NIL);
  }
  
  public void add(TreeNode z_) {
    BinaryTreeNode z = (BinaryTreeNode)z_;
    z.left  = (BinaryTreeNode)NIL;
    z.right = (BinaryTreeNode)NIL;
    z.p     = (BinaryTreeNode)NIL;
    BinaryTreeNode y = (BinaryTreeNode)NIL;
    BinaryTreeNode x = (BinaryTreeNode)root;
    while(x != NIL) {
      y = x;
      if(z.key < x.key)
	x = x.left;
      else
	x = x.right;
    }
    z.p = y;
    if(y == NIL)
      root = z;
    else 
      if(((BinaryTreeNode)z).key < y.key)
	((BinaryTreeNode)y).left  = (BinaryTreeNode)z;
      else
	((BinaryTreeNode)y).right = (BinaryTreeNode)z;
    changed();
  }

  public void remove(TreeNode z_) {
    BinaryTreeNode z = (BinaryTreeNode)z_;
    BinaryTreeNode y;
    BinaryTreeNode x;
    if(z.left == NIL || z.right == NIL)
      y = z;
    else
      y = successor((BinaryTreeNode)z);
    if(y.left != NIL)
      x = y.left;
    else
      x = y.right;
    if(x != NIL) 
      x.p = y.p;
    if(y.p == NIL)
      root = x;
    else
      if(((BinaryTreeNode)y.p).left == y)
	((BinaryTreeNode)y.p).left  = x;
      else
	((BinaryTreeNode)y.p).right = x;
    if(y != z) {
      y.p     = z.p;
      y.left  = z.left;
      y.right = z.right;
      if(root == z) 
	root = y;
      else {
	if(((BinaryTreeNode)y.p).left == z)
	  ((BinaryTreeNode)y.p).left  = y;
	else
	  ((BinaryTreeNode)y.p).right = y;
      }
      if(y.left  != NIL) y.left.p  = y;
      if(y.right != NIL) y.right.p = y;
    }
    z.p    = NIL;
    z.left = (BinaryTreeNode)NIL;
    z.left = (BinaryTreeNode)NIL;
    changed();
  }
  
  protected BinaryTreeNode successor(BinaryTreeNode x) {
    if(x.right != NIL)
      return minimum(x.right);
    BinaryTreeNode y = (BinaryTreeNode)x.p;
    while(y != NIL && x == y.right) {
      x = y;
      y = (BinaryTreeNode)y.p;
    }
    return y;
  }
 /**
    Walks the tree in-order.
     
     @param root    Starting node.
     @param handler The node handler.
     @param misc    Additional information passed to the handler.
 */
  public void inorderWalk(TreeNode x, TreeWalk handler, Object misc) {
    if(x != NIL) {
      inorderWalk(((BinaryTreeNode)x).left, handler, misc);
      handler.node(x, misc);
      inorderWalk(((BinaryTreeNode)x).right, handler, misc);
    }
  }
  protected BinaryTreeNode minimum(BinaryTreeNode x) {
    while(x.left != NIL)
      x = x.left;
    return x;
  }

  protected BinaryTreeNode maximum(BinaryTreeNode x) {
    while(x.right != NIL)
      x = x.right;
    return x;
  }
  
  public BinaryTreeNode get(BinaryTreeNode x, long k) {
    while(x != NIL &&  k != x.key)
      if(k < x.key)
	x =  x.left;
      else
	x = x.right;
    return x;
  }

  public BinaryTreeNode minimum() {
    return minimum((BinaryTreeNode)root);
  }

  public BinaryTreeNode maximum() {
    return maximum((BinaryTreeNode)root);
  }

  public BinaryTreeNode get(long key) {
    return get((BinaryTreeNode)root, key);
  }
  
  protected String verify() {
    String result = super.verify();
    // check sentinel
    if(NIL != null) {
      if(((BinaryTreeNode)NIL).left != null) {
	System.out.println("Left of NIL has changed");
	error();
      }
      if(((BinaryTreeNode)NIL).right != null) {
	System.out.println("Right of NIL has changed");
	error();
      }
    }
    // Binary search tree property
    //
    // Let x be node in a binary search tree. If y is a node in the left
    // subtree of x, then y.key <= x.key. If y is a node in the right subtree
    // of x, then x.key <= y.key.
    preorderWalk(root, 
		 new TreeWalk() {
		     public void node(TreeNode node_, Object misc) {
		       BinaryTreeNode node = (BinaryTreeNode)node_;
		       if(node.left != NIL &&
			  !(node.key >= maximum(node.left).key)) {
			 System.out.println("Binary search tree property - violated (left)");
			 System.out.println(node);
			 error();
		       }
		       if(node.right != NIL && 
			  !(node.key <= minimum(node.right).key)) {
			 System.out.println("Binary search tree property - violated (right)");
			 System.out.println(node);
			 error();
		       }
		     }
		   }, null);
    return result + "(BT,OK)";
  }
  
  protected static void test(int TEST_INIT_ELEMS, int TEST_RUNS, BinaryTree tree) {
    Vector entries = new Vector();

    try {
      System.out.println("Initializing...(" + TEST_INIT_ELEMS + ")");
      for(int i = 0; i < TEST_INIT_ELEMS; i++) {
	BinaryTreeNode node = tree.newTestNode(Util.intRandom(1000));
	entries.addElement(node);
	tree.verify();
	tree.add(node);
	tree.verify();
      }
      System.out.println("Testing...(" + TEST_RUNS + ")");
      for(int i = 0; i < TEST_RUNS; i++) {
	int idx = Util.intRandom(entries.size());
	if((idx & 1) == 0) {
	  BinaryTreeNode node = tree.newTestNode(Util.intRandom(1000));
	  entries.addElement(node);
	  tree.verify();
	  tree.add(node);
	  tree.verify();
	}
	else {
	  TreeNode node = tree.get(((BinaryTreeNode)entries.elementAt(idx)).key);
	  if(node == tree.nil()) {
	    System.out.println("Get failed.");
	    tree.error();
	  }
	  else {
	    entries.removeElement(node);
	    tree.verify();
	    tree.remove(node);
	    tree.verify();
	  }
	}
      }
      int count = entries.size();
      System.out.println("Cleanup...(" + count + ")");
      for(int i = 0; i < count; i++) {
	TreeNode node = (BinaryTreeNode)entries.elementAt(i);
	tree.verify();
	tree.remove(node);
	tree.verify();
      }
      System.out.println("Final tree:" + tree);
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println(tree);
    }
  }
  
  protected BinaryTreeNode newTestNode(long key) {
    return new BinaryTreeNode(key, null);
  }

  public static void main(String[] argv) {
    test(Integer.parseInt(argv[0]), Integer.parseInt(argv[1]), new BinaryTree());
  }
}
/*
  $Log: BinaryTree.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes
  
*/
