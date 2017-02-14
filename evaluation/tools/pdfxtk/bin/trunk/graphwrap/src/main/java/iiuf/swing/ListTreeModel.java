package iiuf.swing;

import javax.swing.ListModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

/**
   A tree model that provides list models for non-leaf nodes.

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ListTreeModel 
  implements
  TreeModel
{
  TreeModel         model;

  ListTreeModel(TreeModel model_) {
    model = model_;
  }

  public void addTreeModelListener(TreeModelListener l) {
    model.addTreeModelListener(l);
  }
  
  public void removeTreeModelListener(TreeModelListener l) {
    model.removeTreeModelListener(l);
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
    return model.getRoot();
  }
  
  public boolean isLeaf(Object node) {
    return model.isLeaf(node);
  }
  
  public void valueForPathChanged(TreePath path, Object newValue) {
    model.valueForPathChanged(path, newValue);
  }
  
  public ListModel getListModel(final Object node_, final boolean prefetch) {
    if(isLeaf(node_)) return null;
    if(node_ instanceof ListModel) {
      ListModel result = (ListModel)node_;

      if(prefetch) {
	int size = result.getSize();
	for(int i = 0; i < size; i++)
	  result.getElementAt(i); 
      }

      return result;
    }
    return new ListModel() {
	Object            node      = node_;
	EventListenerList listeners = new EventListenerList();
	boolean           listening = init();

	TreeModelListener tml = new TreeModelListener() {
	    public void treeNodesChanged(TreeModelEvent e)     {update(e.getPath());}
	    public void treeNodesInserted(TreeModelEvent e)    {update(e.getPath());}
	    public void treeNodesRemoved(TreeModelEvent e)     {update(e.getPath());}
	    public void treeStructureChanged(TreeModelEvent e) {update(e.getPath());}
	    
	    void update(Object[] path) {
	      if(path[path.length - 1] == node ||
		 (path.length > 1 && path[path.length - 2] == node))
		fireChangeEvent();
	    }
	  };
	
	boolean init() {
	  if(!prefetch) return false;
	  int size = getSize();
	  for(int i = 0; i < size; i++)
	    getElementAt(i);
	  return false;
	}
	
	public void addListDataListener(ListDataListener l) {
	  listeners.add(ListDataListener.class, l);
	  if(!listening) {
	    model.addTreeModelListener(tml);
	    listening = true;
	  }
	}
	
	public void removeListDataListener(ListDataListener l) {
	  listeners.remove(ListDataListener.class, l);
	}

	public Object getElementAt(int index) {
	  return getChild(node, index);
	}
	
	public int getSize() {
	  return getChildCount(node);
	}
	
	void fireChangeEvent() {
	  ListDataListener[] l     = (ListDataListener[])listeners.getListeners(ListDataListener.class);
	  if(l.length == 0) {
	    model.removeTreeModelListener(tml);
	    listening = false;
	  } else {
	    ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1);
	    for(int i = 0; i < l.length; i++)
	      l[i].contentsChanged(event);
	  }
	}
      };
  }
}

/*
  $Log: ListTreeModel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.6  2001/05/17 12:42:36  schubige
  updates for tinja

  Revision 1.5  2001/01/31 11:36:07  schubige
  add tons of comments for tinja

  Revision 1.4  2001/01/17 09:55:46  schubige
  Logger update

  Revision 1.3  2001/01/15 15:08:58  schubige
  some sourcewatch bug fixes

  Revision 1.2  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.1  2001/01/12 08:27:17  schubige
  TJGUI update and some TreeView bug fixes
  
*/
