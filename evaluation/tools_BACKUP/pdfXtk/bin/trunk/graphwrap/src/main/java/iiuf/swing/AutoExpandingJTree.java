package iiuf.swing;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

/**
   An JTree specialization that automatically expands when tree nodes are added or the tree structure changes.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class AutoExpandingJTree 
  extends
  JTree
{
  public AutoExpandingJTree()                   {super();         init();}
  public AutoExpandingJTree(Hashtable value)    {super(value);    init();}
  public AutoExpandingJTree(Object[] value)     {super(value);    init();} 
  public AutoExpandingJTree(TreeModel newModel) {super(newModel); init();} 
  public AutoExpandingJTree(TreeNode root)      {super(root);     init();} 
  public AutoExpandingJTree(Vector value)       {super(value);    init();}
  public AutoExpandingJTree(TreeNode root, boolean asksAllowsChildren) {
    super(root, asksAllowsChildren);
    init();
  } 
    
  private void init() {
    treeModel.addTreeModelListener(new TreeModelListener() {
	public void treeNodesChanged(TreeModelEvent e)     {}
	public void treeNodesRemoved(TreeModelEvent e)     {}
	
	public void treeNodesInserted(TreeModelEvent e)    {expand(e.getTreePath());}
	public void treeStructureChanged(TreeModelEvent e) {expand(e.getTreePath());}
      });
  }
  
  private void expand(TreePath path) {
    if(!(path.getLastPathComponent() instanceof TreeNode)) {
      if(isVisible(path)) return;
      makeVisible(path);
      return;
    }
    TreeNode node = (TreeNode)path.getLastPathComponent();
    if(node.getChildCount() == 0) {
      if(isVisible(path)) return;
      makeVisible(path);
    } else {
      for(Enumeration e = node.children(); e.hasMoreElements();)
	expand(path.pathByAddingChild(e.nextElement()));
    } 
  }
}

/*
  $Log: AutoExpandingJTree.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/09 07:53:08  schubige
  early checkin for DCJava
  
*/
