package iiuf.swing;

import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalIconFactory;

import iiuf.awt.Awt;
import iiuf.log.Log;
import iiuf.util.ProgressListener;
import iiuf.util.ProgressWatcher;
import iiuf.util.EventListenerList;

/**
   Chooser tree view implementation, looks like a file chooser.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ChooserTreeView
  extends
  JPanel
  implements
  TreeView,
  ProgressListener,
  ChangeListener
{
  private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    
  ListTreeModel      tree;
  ButtonTreePathView pathView = new ButtonTreePathView(tree);
  JList              content  = new JList();
  Object             node;
  JScrollPane        csp;
  ContextMenuManager ctxmgr;
  EventListenerList  listeners = new EventListenerList();
  boolean            ignoreRepaints;

  public ChooserTreeView() {
    this(null);
  }

  public ChooserTreeView(TreeModel model) {
    this(model, null);
  }
  
  public ChooserTreeView(TreeModel model, TreePath rootPath) {
    setLayout(new BorderLayout());
    JScrollPane pathSP = new JScrollPane(pathView);
    pathView.addChangeListener(this);
    pathSP.setHorizontalScrollBarPolicy(pathSP.HORIZONTAL_SCROLLBAR_ALWAYS);
    pathSP.setVerticalScrollBarPolicy(pathSP.VERTICAL_SCROLLBAR_NEVER); 
    add(pathSP, BorderLayout.NORTH);
    csp = new JScrollPane(content);
    csp.setHorizontalScrollBarPolicy(csp.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    csp.setVerticalScrollBarPolicy(csp.VERTICAL_SCROLLBAR_NEVER); 
    add(csp, BorderLayout.CENTER);
    if(model != null) setModel(model, rootPath);

    content.setCellRenderer(new CellRenderer());    
    content.addMouseListener(Awt.asyncWrapper(mouseAdapter));
    content.setUI(new MultiColumnListUI());
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
  
  public void stateChanged(ChangeEvent e) {
    if(lastNode() == node) return;
    synchronized(this) {
      if(setupContent) return;
      setupContent = true;
    }
    node = lastNode();
    ListModel m = tree.getListModel(node, true);
    content.setModel(m == null ? new DefaultListModel() : m);
    synchronized(this) {
      setupContent = false;
    }
  }
  
  public void setModel(TreeModel model) {
    setModel(model, null);
  }
  
  public void setModel(TreeModel model, TreePath rootPath) {
    pathView.setModel(model);
    
    if(rootPath == null)
      rootPath = new TreePath(model.getRoot());
    
    tree = new ListTreeModel(model);
    
    pathView.setPath(rootPath, true);
  }
  
  public TreeModel getModel() {
    return tree;
  }

  public Object getViewRoot() {
    return pathView.getViewRoot();
  }

  public void makeVisible(TreePath treepath) {
    if(treepath == null) return;
    if(tree.isLeaf(treepath.getLastPathComponent()))
      pathView.setPath(treepath.getParentPath());
    else
      pathView.setPath(treepath);
  }
  
  public TreePath getMostVisiblePath() {
    return pathView.getPath();
  }
  
  public boolean isVisible(TreePath path) {
    Object node = path.getLastPathComponent();
    if(tree.isLeaf(node)) {
      ListModel model = content.getModel();
      for(int i = 0; i < model.getSize(); i++)
	if(model.getElementAt(i).equals(node))
	  return true;
      return false;
    } else
      return pathView.getPath().equals(path);
  }
  
  public TreePath getSelectionPath() {
    TreePath[] tp = getSelectionPaths();
    return tp.length > 0 ? tp[0] : null;
  }
  
  public TreePath[] getSelectionPaths() {
    int[] idxs = content.getSelectedIndices();
    TreePath[] result = new TreePath[idxs.length];
    for(int i = 0; i < idxs.length; i++)
      result[i] = pathView.getPath().pathByAddingChild(content.getModel().getElementAt(idxs[i]));
    return result;
  }
  
  public void setSelectionPath(TreePath path) {
    if(path == null) return;
    setSelectionPaths(new TreePath[] {path});
  }
  
  public void setSelectionPaths(TreePath[] path) {
    if(path == null || path.length == 0) return;
    TreePath maxPath = path[0];
    for(int i = 0; i < path.length; i++)
      if(path[i].getPathCount() > maxPath.getPathCount())
	maxPath = path[i];
    
    maxPath = maxPath.getParentPath();
    makeVisible(maxPath);
    
    minSelected = Integer.MAX_VALUE;
    for(int i = 0; i < path.length; i++)
      if(path[i].getParentPath().equals(maxPath))
	select(path[i].getLastPathComponent());
  }

  private int minSelected = Integer.MAX_VALUE;

  private void select(Object node) {
    int count = content.getModel().getSize();
    for(int i = 0; i < count; i++)
      if(content.getModel().getElementAt(i).equals(node)) {
	content.getSelectionModel().addSelectionInterval(i, i);
	if(i < minSelected) {
	  content.ensureIndexIsVisible(i);
	  minSelected = i;
	}
      }
  }

  public void clearSelection() {
    content.clearSelection();
  }
  
  private Object lastNode() {
    return pathView.getPath().getLastPathComponent();
  }
  
  private MouseAdapter mouseAdapter = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
	if(e.getClickCount() == 2 && ((e.getModifiers() & e.BUTTON1_MASK) == e.BUTTON1_MASK)) {
	  ProgressWatcher.watch(ChooserTreeView.this);
	  int idx = content.locationToIndex(e.getPoint());
	  if(idx < 0) return;
	  Object n = content.getModel().getElementAt(idx);
	  if(!tree.isLeaf(n))
	    pathView.setPath(pathView.getPath().pathByAddingChild(n));
	}
      }
    };
    
  public Object locationToObject(Component component, Point location) {
    int idx = content.locationToIndex(location);
    return idx < 0 ? null : content.getModel().getElementAt(idx);
  }
  
  public Component getComponent() {
    return content;
  }
  
  public void setContextMenuManager(ContextMenuManager manager) {
    ctxmgr = manager;
  }
  
  boolean setupContent;
  private void setupContent() {
  }

  public void operationStart(String desc) {}
  
  public void operationProgress(int amount, int of) {
    if(amount == 0) Awt.setCursor(this, Cursor.WAIT_CURSOR);
  }
  
  public void operationStop() {
    Awt.setCursor(this, Cursor.DEFAULT_CURSOR);
  }
  
  DragGestureRecognizer sourceRecognizer;
  DragGestureListener   srcListener;
  public void enableDrag(int sourceActions, DragGestureListener sourceListener) {
    disableDrag();
    sourceRecognizer = DragSource.getDefaultDragSource().
      createDefaultDragGestureRecognizer(content, sourceActions, sourceListener);
    srcListener  = sourceListener;
  }

  DropTarget            dropTarget;
  DropTargetListener    trgtListener;
  public void enableDrop(int targetActions, DropTargetListener  targetListener) {
    disableDrop();
    dropTarget = new DropTarget(content, targetActions, targetListener);
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

  class CellRenderer 
    extends 
    DefaultListCellRenderer 
  {    
    public Component getListCellRendererComponent(JList   list,
						  Object  value,
						  int     index,
						  boolean isSelected,
						  boolean cellHasFocus)
    {
      DefaultListCellRenderer result = 
	(DefaultListCellRenderer)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      
      result.setIcon(tree.isLeaf(value) ? 
		     MetalIconFactory.getTreeLeafIcon() :
		     MetalIconFactory.getTreeFolderIcon());
      
      return result;
    }
  }

}

/*
  $Log: ChooserTreeView.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.8  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.7  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.6  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2001/01/03 08:30:39  schubige
  graph stuff beta

  Revision 1.4  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.3  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.2  2000/10/03 08:39:38  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added
  
*/
