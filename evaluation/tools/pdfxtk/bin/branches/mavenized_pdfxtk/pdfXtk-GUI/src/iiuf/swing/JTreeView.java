package iiuf.swing;

/**
   JTree based tree view implementation.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

import java.util.Enumeration;
import java.util.ArrayList;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Component;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JTree;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import iiuf.util.ProgressListener;
import iiuf.util.ProgressWatcher;
import iiuf.util.EventListenerList;
import iiuf.log.Log;
import iiuf.awt.Awt;

public class JTreeView
  extends
  JScrollPane
  implements
  TreeView,
  ProgressListener,
  TreeModelListener
{  
  private final static Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor            saveCursor;
  private TreeModel         model;
  private JTree             trcmp     = new JTree();
  private EventListenerList listeners = new EventListenerList();
   
  public JTreeView() {
    this(null);
  }
  
  public JTreeView(TreeModel tree) {
    this(tree, tree == null ? null : new TreePath(tree.getRoot()));
  }
  
  private TreeWillExpandListener treeWillExpandListener = new TreeWillExpandListener() {
      public void treeWillCollapse(TreeExpansionEvent e) {}
      public void treeWillExpand(TreeExpansionEvent e) {
	synchronized(JTreeView.this) {
	  if(saveCursor == null) {
	    saveCursor = trcmp.getCursor();
	    ProgressWatcher.watch(JTreeView.this);
	  }
	}
	// force loading of tree
	for(Enumeration en = ((TreeNode)e.getPath().getLastPathComponent()).children();
	    en.hasMoreElements();
	    en.nextElement());
      }
    };
  
  public JTreeView(TreeModel model, TreePath rootPath) {
    setModel(model, rootPath);
    
    expanded = false;    
    trcmp.addTreeWillExpandListener(Swing.asyncWrapper(treeWillExpandListener));
				    
    trcmp.setRootVisible(false);
    trcmp.setShowsRootHandles(true);
    trcmp.putClientProperty("JTree.lineStyle", "Angled");
    setViewportView(trcmp);    
  }
  
  public void addRootNodeChangeListener(ChangeListener l) {
    listeners.add(ChangeListener.class, l);
  }
  
  public void addRootNodeChangeListener(ChangeListener l, boolean weak) {
    listeners.add(ChangeListener.class, l, weak);
  }
  
  public void removeRootNodeChangeListener(ChangeListener l) {
    listeners.remove(ChangeListener.class, l);
  }
  
  public void treeNodesChanged(TreeModelEvent e) {
    System.out.println(e);
    Object[] childs = e.getChildren();
    for(int i = 0; i < childs.length; i++)
      if(childs[i] == trcmp.getModel().getRoot()) {
	fireRootNodeChanged();
	break;
      }
  }

  public void treeStructureChanged(TreeModelEvent e) {
    System.out.println(e);
    if(e.getTreePath().getPathCount() == 1 &&
       e.getTreePath().getPathComponent(0) != trcmp.getModel().getRoot())
	fireRootNodeChanged();
  }
  
  public void treeNodesInserted(TreeModelEvent e) {}
  public void treeNodesRemoved(TreeModelEvent e) {}
  
  private void fireRootNodeChanged() {
    ChangeListener[] listeners = (ChangeListener[])listenerList.getListeners(ChangeListener.class);
    ChangeEvent ev = listeners.length == 0 ? 
      null : 
      new ChangeEvent(this);
    for(int i = 0; i < listeners.length; i++)
      listeners[i].stateChanged(ev);
  }
  
  private TreePath shortestCommonPath(TreePath p1, TreePath p2) {
    TreePath result = new TreePath(p1.getPathComponent(0));
    for(int i = 0; i < Math.min(p1.getPathCount(), p2.getPathCount()); i++)
      if(!p1.getPathComponent(i).equals(p2.getPathComponent(i)))
	break;
      else
	result = result.pathByAddingChild(p1.getPathComponent(i));
    return result;
  }
  
  boolean expanded;
  private void expandRoot() {
    if(expanded) return;
    trcmp.expandPath(new TreePath(trcmp.getModel().getRoot()));
    expanded = true;
  }
  
  public Object locationToObject(Component component, Point location) { 
    TreePath path = trcmp.getPathForLocation(location.x, location.y);
    return path == null ? null : path.getLastPathComponent();
  }
  
  public Component getComponent() {
    return trcmp;
  }
  
  public void setContextMenuManager(ContextMenuManager manager) {}
  
  public TreePath getSelectionPath() {
    if(trcmp.getModel() instanceof SubTreeModel)
      return ((SubTreeModel)trcmp.getModel()).absolute(trcmp.getSelectionPath());
    else
      return trcmp.getSelectionPath();
  }
  
  public TreePath[] getSelectionPaths() {
    TreePath[] result = trcmp.getSelectionPaths();
    if(result == null) return new TreePath[0];
    
    if(trcmp.getModel() instanceof SubTreeModel)
      for(int i = 0; i < result.length; i++)
	result[i] = ((SubTreeModel)trcmp.getModel()).absolute(result[i]); 
    
    return result;
  }
  
  private TreePath relative(TreePath path) {
    if(trcmp.getModel() instanceof SubTreeModel)
      return ((SubTreeModel)trcmp.getModel()).relative(path);
    else
      return path;
  }

  public void makeVisible(TreePath path) {
    if(path == null) return;
    path = relative(path);
    if(path == null) return;
    trcmp.expandPath(path);
    trcmp.scrollPathToVisible(path);
  }

  public void setModel(TreeModel model) {
    setModel(model, null);
  }
  
  public void setModel(TreeModel model_, TreePath rootPath) {
    trcmp.getModel().removeTreeModelListener(this);
    model = model_;
    if(rootPath == null)
      trcmp.setModel(model);
    else
      trcmp.setModel(new SubTreeModel(model, (TreeNode)rootPath.getLastPathComponent()));
    trcmp.getModel().addTreeModelListener(this);
  }

  public void setSelectionPath(TreePath path) {
    path = relative(path);
    if(path == null) return;
    makeVisible(path);
    trcmp.setSelectionPath(path);
  }
  
  public void setSelectionPaths(TreePath[] path) {
    for(int i = 0; i < path.length; i++)
      path[i] = relative(path[i]);
    if(path == null || path.length == 0) return;
    makeVisible(path[0]);
    trcmp.setSelectionPaths(path);
  }
  
  public void clearSelection() {
    trcmp.clearSelection();
  }

  public TreeModel getModel() {
    return model;
  }
  
  public Object getViewRoot() {
    return trcmp.getModel().getRoot();
  }

  public boolean isVisible(TreePath path) {
    path = relative(path);
    return trcmp.isVisible(path);
  }
  
  public TreePath getMostVisiblePath() {
    TreePath result = new TreePath(trcmp.getModel().getRoot());
    Enumeration e = trcmp.getExpandedDescendants(result);
    if(e == null) return result;
    while(e.hasMoreElements()) {
      TreePath p = (TreePath)e.nextElement();
      if(p.getPathCount() >= result.getPathCount())
	result = p;
    }
    if(trcmp.getModel() instanceof SubTreeModel)
      return ((SubTreeModel)trcmp.getModel()).absolute(result);
    else
      return result;
  }

  public void operationStart(String desc) {}
  
  public void operationProgress(int amount, int of) {
    if(amount == 0) trcmp.setCursor(WAIT_CURSOR);
  }
  
  public void operationStop() {
    synchronized(JTreeView.this) {
      trcmp.setCursor(saveCursor);
      saveCursor = null;
    }
  }
  
  DragGestureRecognizer sourceRecognizer;
  DragGestureListener   srcListener;
  public void enableDrag(int sourceActions, DragGestureListener sourceListener) {
    disableDrag();
    sourceRecognizer = DragSource.getDefaultDragSource().
      createDefaultDragGestureRecognizer(trcmp, sourceActions, sourceListener);
    srcListener  = sourceListener;
    System.out.println(sourceRecognizer);
  }

  DropTarget            dropTarget;
  DropTargetListener    trgtListener;
  public void enableDrop(int targetActions, DropTargetListener  targetListener) {
    disableDrop();
    dropTarget = new DropTarget(trcmp, targetActions, targetListener);
    trgtListener = targetListener;
  }
  
  public void disableDrag() {
    if(sourceRecognizer != null)
      sourceRecognizer.removeDragGestureListener(srcListener);
    sourceRecognizer = null;
    srcListener      = null;
    System.gc();
  }

  public void disableDrop() {
    if(dropTarget != null)
      dropTarget.removeDropTargetListener(trgtListener);
    dropTarget       = null;
    trgtListener     = null;
    System.gc();
  }

}

/*
  $Log: JTreeView.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.9  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.8  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.7  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2001/01/03 08:30:39  schubige
  graph stuff beta

  Revision 1.5  2000/12/01 14:41:36  schubige
  SourceWatch beta 1

  Revision 1.4  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.3  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added
  
*/
