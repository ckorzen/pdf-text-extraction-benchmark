package iiuf.swing;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

import iiuf.log.Log;
import iiuf.util.EventListenerList;
import iiuf.util.Util;

/**
   A tree model implementation that wraps a subtree of a tree model.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class SubTreeModel
  implements
  TreeModel,
  TreeModelListener
{
  private EventListenerList listenerList = new EventListenerList();
  private TreeModel         model;
  private TreeNode          root;
  private TreePath          rootPath;
  private static int        count;

  SubTreeModel(TreeModel model_, TreeNode root) {
    count++;
    model = model_;
    setRoot(root);
    model.removeTreeModelListener(this);
    model.addTreeModelListener(this);
  }

  private void setRoot(TreeNode root_) {
    root     = root_;
    rootPath = getPathToRoot(root);
  }

  public void treeNodesChanged(TreeModelEvent e) {
    if(!Util.contains(e.getTreePath().getPath(), root)) return;
    TreeModelListener[] listeners = (TreeModelListener[])listenerList.getListeners(TreeModelListener.class);
    TreeModelEvent ev = listeners.length == 0 ? 
      null : 
      new TreeModelEvent(this, relative(e.getTreePath()), e.getChildIndices(), e.getChildren());
    for(int i = 0; i < listeners.length; i++)
      listeners[i].treeNodesChanged(ev);
  }
  
  public void treeNodesInserted(TreeModelEvent e) {
    if(!Util.contains(e.getTreePath().getPath(), root)) return;
    TreeModelListener[] listeners = (TreeModelListener[])listenerList.getListeners(TreeModelListener.class);
    TreeModelEvent ev = listeners.length == 0 ? 
      null : 
      new TreeModelEvent(this, relative(e.getTreePath()), e.getChildIndices(), e.getChildren());
    for(int i = 0; i < listeners.length; i++)
      listeners[i].treeNodesInserted(ev);
  }
  
  public void treeNodesRemoved(TreeModelEvent e) {
    if(!Util.contains(e.getTreePath().getPath(), root)) return;
    TreeModelListener[] listeners = (TreeModelListener[])listenerList.getListeners(TreeModelListener.class);
    Object[] childs = e.getChildren();
    for(int i = 0; i < childs.length; i++) {
      if(isAChildOf((TreeNode)childs[i], root)){
	setRoot((TreeNode)e.getTreePath().getLastPathComponent());
	TreeModelEvent ev = listeners.length == 0 ? 
	  null : 
	  new TreeModelEvent(this, new TreePath(root));
	for(int j = 0; j < listeners.length; j++)
	  listeners[j].treeStructureChanged(ev);
	return;
      }
    }
    TreeModelEvent ev = listeners.length == 0 ? 
      null : 
      new TreeModelEvent(this, relative(e.getTreePath()), e.getChildIndices(), e.getChildren());
    for(int i = 0; i < listeners.length; i++)
      listeners[i].treeNodesRemoved(ev);
  }
  
  public void treeStructureChanged(TreeModelEvent e) {
    if(!Util.contains(e.getTreePath().getPath(), root)) return;
    TreeModelListener[] listeners = (TreeModelListener[])listenerList.getListeners(TreeModelListener.class);
    TreeModelEvent      ev        = null;
    if(isAChildOf((TreeNode)e.getTreePath().getLastPathComponent(), root)) {
      setRoot((TreeNode)e.getTreePath().getLastPathComponent());
      ev = new TreeModelEvent(this, new TreePath(root));
    }
    else 
      ev = new TreeModelEvent(this, relative(e.getTreePath()), e.getChildIndices(), e.getChildren());
    for(int i = 0; i < listeners.length; i++)
      listeners[i].treeStructureChanged(ev);
  }
  
  public boolean isAChildOf(TreeNode startNode, TreeNode testNode) {
    if(testNode == null)      return false;
    if(testNode == startNode) return true;
    else return isAChildOf(startNode, testNode.getParent());
  }

  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(TreeModelListener.class, l);
  }

  public void addTreeModelListener(TreeModelListener l, boolean weak) {
    listenerList.add(TreeModelListener.class, l, weak);
  }
  
  public void removeTreeModelListener(TreeModelListener l) {    
    listenerList.remove(TreeModelListener.class, l);
  }
  
  public Object getChild(Object parent, int index) {
    return model.getChild(parent, index);
  }
  
  public int getChildCount(Object parent) {
    return model.getChildCount(parent);
  }
  
  public int getIndexOfChild(Object parent, Object child) {
    return model.getIndexOfChild(parent, child);
  }
  
  public Object getRoot() {
    return root;
  }
  
  public boolean isLeaf(Object node) {
    return model.isLeaf(node);
  }
  
  public void valueForPathChanged(TreePath path, Object newValue) {
    model.valueForPathChanged(absolute(path), newValue);
  }

  private TreePath getPathToRoot(TreeNode node) {
    ArrayList pa = new ArrayList();
    for(; node != model.getRoot(); node = node.getParent())
      pa.add(0, node);
    
    pa.add(0, model.getRoot());
    
    return new TreePath(pa.toArray());
  }
 
  public TreePath absolute(TreePath relPath) {
    if(relPath == null) return null;
    int rpl = rootPath.getPathCount() - 1;
    Object[] result = new Object[rpl + relPath.getPathCount()];
    
    for(int i = 0; i < rpl; i++)
      result[i] = rootPath.getPathComponent(i);
    
    for(int i = 0; i < relPath.getPathCount(); i++)
      result[i + rpl] = relPath.getPathComponent(i);
    
    return new TreePath(result);
  }
  
  private Object[] relativeO(Object[] absPath) {
    if(absPath == null) return null;
    
    int rpl = rootPath.getPathCount() - 1;
    Object[] result = new Object[absPath.length - rpl];
    
    for(int i = 0; i < result.length; i++)
      result[i] = absPath[i + rpl];

    return result;
  }
  
  public TreePath relative(TreePath absPath) {
    if(absPath == null) return null;
    
    return new TreePath(relativeO(absPath.getPath()));
  }

  public String toString() {
    return "SubTreeModel" + count;
  }
}

/*
  $Log: SubTreeModel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.5  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.4  2001/01/04 09:58:49  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.3  2001/01/03 16:54:55  schubige
  various bugs fixed reported by iiuf.dev.java.Verify

  Revision 1.2  2001/01/03 12:40:18  schubige
  graph stuff beta

  Revision 1.1  2001/01/03 08:31:31  schubige
  graph stuff beta
  
*/
