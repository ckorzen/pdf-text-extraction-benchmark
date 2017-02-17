package iiuf.util;

/**
   RedBalck tree implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class RedBlackTree 
  extends
  BinaryTree
{
  public static boolean RED   = true;
  public static boolean BLACK = false;
  
  public RedBlackTree() {
    this(new RedBlackNode(-1, "NIL"));
  }

  public RedBlackTree(RedBlackNode nil) {
    super(nil);
  }

  public void add(TreeNode x) {
    super.add(x);
    RedBlackNode y;
    ((RedBlackNode)x).color = RED;
    while(x != root && 
	  ((RedBlackNode)x.p).color == RED) {
      if(x.p == ((BinaryTreeNode)x.p.p).left) {
	y = (RedBlackNode)((BinaryTreeNode)x.p.p).right;
	if(y.color == RED) {
	  ((RedBlackNode)x.p).color   = BLACK;
	  y.color                     = BLACK;
	  ((RedBlackNode)x.p.p).color = RED;
	  x = (RedBlackNode)x.p.p;
	} else {
	  if(((BinaryTreeNode)x.p).right == x) {
	    x = (RedBlackNode)x.p;
	    leftRotate((BinaryTreeNode)x);
	  }
	  ((RedBlackNode)x.p).color   = BLACK;
	  ((RedBlackNode)x.p.p).color = RED;
	  rightRotate((BinaryTreeNode)x.p.p);
	}
      } else {
	y = (RedBlackNode)((BinaryTreeNode)x.p.p).left;
	if(y.color == RED) {
	  ((RedBlackNode)x.p).color   = BLACK;
	  y.color                     = BLACK;
	  ((RedBlackNode)x.p.p).color = RED;
	  x = (RedBlackNode)x.p.p;
	} else {
	  if(((BinaryTreeNode)x.p).left == x) {
	    x = (RedBlackNode)x.p;
	    rightRotate((BinaryTreeNode)x);
	  }
	  ((RedBlackNode)x.p).color   = BLACK;
	  ((RedBlackNode)x.p.p).color = RED;
	  leftRotate((BinaryTreeNode)x.p.p);
	}
      }
    }
    ((RedBlackNode)root).color = BLACK;
    changed();
  }
  
  public void remove(TreeNode z_) {
    if(z_ == NIL) return;

    BinaryTreeNode z = (BinaryTreeNode)z_;
    BinaryTreeNode y = z.left == NIL || z.right == NIL ? z      : successor(z);
    BinaryTreeNode x = y.left != NIL                   ? y.left : y.right;

    x.p = y.p;
    if(y.p == NIL)
      root = x;
    else
      if(((BinaryTreeNode)y.p).left == y)
	((BinaryTreeNode)y.p).left  = x;
      else
	((BinaryTreeNode)y.p).right = x;
    boolean delfix = ((RedBlackNode)y).color == BLACK;
    if(y != z) {
      y.p     = z.p;
      y.left  = z.left;
      y.right = z.right;
      ((RedBlackNode)y).color = ((RedBlackNode)z).color;
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
      if(x.p     == z)   x.p       = y;
    }
    z.p    = NIL;
    z.left = (BinaryTreeNode)NIL;
    z.left = (BinaryTreeNode)NIL; 
    if(delfix)
      deleteFixup((RedBlackNode)x);
    changed();
  }
  
  private void deleteFixup(RedBlackNode x) {
    RedBlackNode w;
    while(root    != x && 
	  x.color == BLACK) {
      if(x == (RedBlackNode)((BinaryTreeNode)x.p).left) {
	w = (RedBlackNode)((BinaryTreeNode)x.p).right;
	if(w.color == RED) {
	  w.color                   = BLACK;
	  ((RedBlackNode)x.p).color = RED;
	  leftRotate((BinaryTreeNode)x.p);
	  w                         = (RedBlackNode)((BinaryTreeNode)x.p).right;
	}
	if(((RedBlackNode)w.left).color  == BLACK && 
	   ((RedBlackNode)w.right).color == BLACK) {
	  w.color = RED;
	  x       = (RedBlackNode)x.p;
	}
	else {
	  if(((RedBlackNode)w.right).color == BLACK) {
	    ((RedBlackNode)w.left).color = BLACK;
	    w.color                      = RED;
	    rightRotate(w);
	    w                            = (RedBlackNode)((BinaryTreeNode)x.p).right;
	  }
	  w.color                       = ((RedBlackNode)x.p).color;
	  ((RedBlackNode)x.p).color     = BLACK;
	  ((RedBlackNode)w.right).color = BLACK;
	  leftRotate((BinaryTreeNode)x.p);
	  x = (RedBlackNode)root;
	}
      }
      else {
	w = (RedBlackNode)((BinaryTreeNode)x.p).left;
	if(w.color == RED) {
	  w.color   = BLACK;
	  ((RedBlackNode)x.p).color = RED;
	  rightRotate((BinaryTreeNode)x.p);
	  w = (RedBlackNode)((BinaryTreeNode)x.p).left;
	}
	if(((RedBlackNode)w.right).color == BLACK && 
	   ((RedBlackNode)w.left).color  == BLACK) {
	  w.color = RED;
	  x = (RedBlackNode)x.p;
	}
	else {
	  if(((RedBlackNode)w.left).color == BLACK) {
	    ((RedBlackNode)w.right).color = BLACK;
	    w.color      = RED;
	    leftRotate(w);
	    w            = (RedBlackNode)((BinaryTreeNode)x.p).left;
	  }
	  w.color                      = ((RedBlackNode)x.p).color;
	  ((RedBlackNode)x.p).color    = BLACK;
	  ((RedBlackNode)w.left).color = BLACK;
	  rightRotate((BinaryTreeNode)x.p);
	  x = (RedBlackNode)root;
	}
      }
    }
    x.color = BLACK;
  }
  
  private void leftRotate(BinaryTreeNode x) {
    BinaryTreeNode y = x.right;
    x.right = y.left;
    if(y.left != NIL)
      y.left.p = x;
    if(y != NIL)
      y.p = x.p;
    if(x.p == NIL) 
      root = y;
    else
      if(x == ((BinaryTreeNode)x.p).left)
	((BinaryTreeNode)x.p).left  = y;
      else
	((BinaryTreeNode)x.p).right = y;
    y.left = x;
    if(x != NIL)
      x.p = y;
  }
  
  private void rightRotate(BinaryTreeNode x) {
    BinaryTreeNode y = x.left;
    x.left = y.right;
    if(y.right != NIL)
      y.right.p = x;
    if(y != NIL)
      y.p = x.p;
    if(x.p == NIL) 
      root = y;
    else {
      if(x == ((BinaryTreeNode)x.p).right)
	((BinaryTreeNode)x.p).right  = y;
      else
	((BinaryTreeNode)x.p).left = y;
    }
    y.right = x;
    if(x != NIL) 
      x.p = y;
  }
  
  private int blackHeight(RedBlackNode node, int height) {
    if(node == NIL) return height;
    return blackHeight((RedBlackNode)node.p, node.color == BLACK ? height + 1 : height);
  }
  
  protected String verify() {
    String result = super.verify();
    // check sentinel
    if(((RedBlackNode)NIL).color != BLACK) {
      System.out.println("Color of NIL has changed");
      error();
    }
    // red-black properties
    // 1. every node is either red or black 
    // -> ensured by the boolean color var
    // 2. Every leaf (NIL) is black
    if(((RedBlackNode)NIL).color != BLACK) {
      System.out.println("2. Every leaf (NIL) is black - violated");
      error();
    }
    // 3. If a node is red, then both its child are black
    inorderWalk(root,
		new TreeWalk() {
		    public void node(TreeNode node_, Object misc) {
		      RedBlackNode node = (RedBlackNode)node_;
		      if(node.color == RED) {
			if(((RedBlackNode)node.left).color != BLACK) {
			  System.out.println("3. If a node is red, " +
					     "then both its child are black - violated (left)");
			  error();
			}
			if(((RedBlackNode)node.right).color != BLACK) {
			  System.out.println("3. If a node is red, " +
					     "then both its child are black - violated (right)");
			  error();
			}
		      }
		    }
		  }, null);
    // 4. Every simple path from a node to a descendant leaf contains the same
    //    number of black nodes
    inorderWalk(root,
		new TreeWalk() {
		    int blackHeight = -1;
		    
		    private void updateBH(RedBlackNode node) {
		      int bh = blackHeight(node, 1);
		      if(blackHeight == -1)
			blackHeight = bh;
		      else
			if(bh != blackHeight) {
			  System.out.println("4. Every simple path from a node to a " + 
					     "descendant leaf contains the same number " +
					     "of black nodes - violated");
			  error();
			}
		    }
	    
		    public void node(TreeNode node_, Object misc) {
		      RedBlackNode node = (RedBlackNode)node_;
		      if(node.left  == NIL) updateBH(node);
		      if(node.right == NIL) updateBH(node);		      
		    }
		  }, null);
    return result + "(RBT,OK)";
  }
  
  protected BinaryTreeNode newTestNode(long key) {
    return new RedBlackNode(key, null);
  }

  public static void main(String[] argv) {
    test(Integer.parseInt(argv[0]), Integer.parseInt(argv[1]), new RedBlackTree());
  }
}

/*
  $Log: RedBlackTree.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes
  
*/
