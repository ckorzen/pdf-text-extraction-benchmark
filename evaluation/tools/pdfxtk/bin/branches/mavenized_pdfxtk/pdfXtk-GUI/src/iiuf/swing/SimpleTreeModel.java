package iiuf.swing;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

import iiuf.util.EventListenerList;

/**
   A simple default tree model implementation.

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class SimpleTreeModel
  implements
  TreeModel
{ 
  protected EventListenerList listeners = new EventListenerList();
  protected TreeModelEvent    event;
  protected Object            root;

  public SimpleTreeModel(Object root_) {
    root  = root_;
    event = new TreeModelEvent(this, new TreePath(getRoot()));
  }
  
  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(TreeModelListener.class, l);
  }

  public void addTreeModelListener(TreeModelListener l, boolean weak) {
    listeners.add(TreeModelListener.class, l, weak);
  }
  
  public void removeTreeModelListener(TreeModelListener l) {    
    listeners.remove(TreeModelListener.class, l);
  }
  
  protected abstract Object[] getChildren(Object o);
  
  public Object getChild(Object parent, int index) {
    return getChildren(parent)[index];
  }
  
  public int getChildCount(Object parent) {
    return getChildren(parent).length;
  }
  
  public int getIndexOfChild(Object parent, Object child) {
    Object[] cs = getChildren(parent);
    for(int i = 0; i < cs.length; i++)
      if(cs[i] == child)
	return i;
    return -1;
  }
  
  public Object getRoot() {
    return root;
  }
  
  public boolean isLeaf(Object node) {
    return getChildren(node).length == 0;
  }
  
}

/*
  $Log: SimpleTreeModel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/02/12 17:51:45  schubige
  still working on soundium gui
  
*/
