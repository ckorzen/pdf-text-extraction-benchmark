package iiuf.swing;

import java.awt.Point;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import iiuf.util.EventListenerList;

/**
   Horizontal aligned buttons representing a path.

   (c) 2000, 2001, IIUF, DIUF<p>
      
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ButtonTreePathView 
  extends
  JPanel
  implements
  TreeModelListener
{
  private TreePath          path;
  private EventListenerList listeners = new EventListenerList();
  private int               startDisplay;
  private TreeModel         model;

  public ButtonTreePathView(TreeModel model) {
    this(null, model);
  }
  
  public ButtonTreePathView(TreePath path, TreeModel model) {
    ((FlowLayout)getLayout()).setAlignment(FlowLayout.LEFT);
    if(path != null)
      setPath(path);
    setModel(model);
  }
  
  public void treeNodesChanged(TreeModelEvent e) {
    Component[] cmps = getComponents();
    for(int i = 0; i < cmps.length; i++)
      ((JButton)cmps[i]).setText(path.getPathComponent(i).toString());
  }
  
  public void treeNodesRemoved(TreeModelEvent e) {
    if(e.getTreePath().getPathComponent(0) != path.getPathComponent(0)) return;
    setPath(shortestCommonPath(path, e.getTreePath()));
  }
  
  public void treeStructureChanged(TreeModelEvent e) {
    setPath(shortestCommonPath(path, e.getTreePath()));
  }
  
  public void treeNodesInserted(TreeModelEvent e) {}
  
  private TreePath shortestCommonPath(TreePath p1, TreePath p2) {
    TreePath result = new TreePath(p1.getPathComponent(0));
    for(int i = 1; i < Math.min(p1.getPathCount(), p2.getPathCount()); i++)
      if(p1.getPathComponent(i) != p2.getPathComponent(i))
	break;
      else
	result = result.pathByAddingChild(p1.getPathComponent(i));
    return result;
  }
  
  public void setModel(TreeModel model_) {
    if(model != null)
      model.removeTreeModelListener(this);
    model = model_;
    if(model != null)
      model.addTreeModelListener(this);
  }

  public TreeModel getModel() {
    return model;
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(ChangeListener.class, listener);
  }

  public void addChangeListener(ChangeListener listener, boolean weak) {
    listeners.add(ChangeListener.class, listener, weak);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(ChangeListener.class, listener);
  }
  
  public void setPath(TreePath path) {
    if(path == null) return;
    setPath(path, path.getPathCount() < startDisplay);
  }
  
  public void setPath(TreePath path_, boolean lastIsLimit) {
    if(path_ == null) return;
    if(path_.equals(path)) return;
    path = path_;
    removeAll();
    Object[] p = path.getPath();
    if(lastIsLimit) startDisplay = p.length - 1;
    for(int i = startDisplay; i < p.length; i++) {
      final int iterator = i;
      JButton button = Swing.newButton(p[iterator].toString(), new ActionListener() {
	  int idx = iterator;
	  public void actionPerformed(ActionEvent e) {
	    synchronized(ButtonTreePathView.this) {
	      Object[] pOld = path.getPath();
	      Object[] pNew = new Object[idx + 1];
	      for(int j = 0; j < pNew.length; j++)
		pNew[j] = pOld[j]; 
	      path = new TreePath(pNew);
	      while(getComponentCount() > idx + 1 - startDisplay)
		remove(idx + 1 - startDisplay);
	    }
	    fireChangeEvent(new ChangeEvent(ButtonTreePathView.this));
	    validate();
	    repaint();
	  }
	});
      button.setIcon(Resource.RIGHTARROW);
      button.setHorizontalTextPosition(button.LEADING);
      add(button);
    }
    if(getParent().getParent() instanceof JScrollPane) {
      getParent().getParent().validate();
      JScrollBar sb = ((JScrollPane)getParent().getParent()).getHorizontalScrollBar();
      sb.setValue(sb.getMaximum());
      repaint();
    } else {
      validate();
      repaint();
    }
    fireChangeEvent(new ChangeEvent(this));
  }
  
  protected void fireChangeEvent(ChangeEvent e) {
    ChangeListener[] ls = (ChangeListener[])listeners.getListeners(ChangeListener.class);
    for(int i = 0; i < ls.length; i++)
      ls[i].stateChanged(e);
  }

  public TreePath getPath() {
    return path; 
  }
  
  public int getInvisiblePathLength() {
    return startDisplay;
  }
  
  public TreeNode getViewRoot() {
    return (TreeNode)path.getPathComponent(startDisplay);
  }
}

/*
  $Log: ButtonTreePathView.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.5  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.4  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.3  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.2  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.1  2000/10/03 08:39:38  schubige
  Added tree view and contect menu stuff
  
*/
