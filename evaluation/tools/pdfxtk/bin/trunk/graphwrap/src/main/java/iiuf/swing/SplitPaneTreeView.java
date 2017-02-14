package iiuf.swing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.JViewport;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import iiuf.awt.Awt;
import iiuf.util.ProgressListener;
import iiuf.util.ProgressWatcher;
import iiuf.util.EventListenerList;

/**
   Spilt pane tree view implementation, looks like the NeXTSTEP browser.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class SplitPaneTreeView 
  extends
  JPanel
  implements
  ProgressListener,
  TreeView
{
  final static ImageIcon ICON            = Resource.RIGHTARROW;
  final static Border    NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

  public static final int KEEP_PANE_COUNT = 0;
  public static final int KEEP_PANE_WIDTH = 1;
  
  private static final int MIN_PANE_WIDTH = 30;
  Object[]                 currentPath    = new Object[0];
  int                      lastPathComponentPane;
  JPanel                   browser        = new JPanel();
  ButtonTreePathView       pathView       = new ButtonTreePathView(null);
  int                      minPaneWidth   = 100;
  int                      resizePolicy   = KEEP_PANE_COUNT;
  int                      paneCount;
  ContextMenuManager       ctxmgr;
  Controler                controler;
  Object                   viewRoot;
  EventListenerList        listeners = new EventListenerList();

  public SplitPaneTreeView() {
    this(null, -1);
  }

  public SplitPaneTreeView(int paneCount) {
    this(null, paneCount);
  }

  public SplitPaneTreeView(TreeModel tree, int paneCount) {
    this(tree, null, paneCount);
  }
  
  public SplitPaneTreeView(TreeModel tree, TreePath rootPath, int paneCount) {
    setLayout(new BorderLayout());
    JScrollPane pathSP = new JScrollPane(pathView);
    pathSP.setHorizontalScrollBarPolicy(pathSP.HORIZONTAL_SCROLLBAR_ALWAYS);
    pathSP.setVerticalScrollBarPolicy(pathSP.VERTICAL_SCROLLBAR_NEVER); 
    add(pathSP, BorderLayout.NORTH);
    if(tree != null) setModel(tree, rootPath);
    setPaneCount(paneCount);
    add(browser, BorderLayout.CENTER);
    refresh();
  }

  //------ public methods
  
  public SplitPaneTreeView setResizePolicy(int policy) {
    resizePolicy = policy;
    return this;
  }
  
  public void setMinPaneWidth(int width) {
    setResizePolicy(KEEP_PANE_WIDTH);
    minPaneWidth = width < MIN_PANE_WIDTH ? MIN_PANE_WIDTH : width;
    setPaneCount(getWidth() / minPaneWidth);
  }
  
  public void setPaneCount(int count) {
    int oldPaneCount = paneCount;
    if(count == paneCount) return;
    paneCount = count;
    if(paneCount < 2) paneCount = 2;
    oldPaneCount -= paneCount;
    if(oldPaneCount > 0) {
      // reduce number of panes
      // first we remove panes on the right hand side of the lastPathComponentPane
      // then we remove at the left end.
      for(; oldPaneCount > 0; oldPaneCount--) {
	if(lastPathComponentPane < paneCount)
	  removePane(paneCount + oldPaneCount - 1); // remove on right hand side
	else {
	  removePane(0); // remove on left hand side
	  lastPathComponentPane--;
	}
      }
    } else {
      oldPaneCount = -oldPaneCount;
      // increase number of panes
      // first we insert on the lft hand side
      // then on the right hand side
      int maxPathLength = distance(currentPath, getViewRoot(), currentPath[currentPath.length - 1]);
      for(; oldPaneCount > 0; oldPaneCount--) {
	if(lastPathComponentPane < maxPathLength) {
	  browser.add(getScrollPane(new NodeList(currentPath[currentPath.length - lastPathComponentPane - 2])), 0);
	  lastPathComponentPane++;
	} else
	  browser.add(getScrollPane(new NodeList(null)));
     }
    }
    browser.setLayout(new GridLayout(1, paneCount));
  }
  
  //------ various overrides

  public void setSize(int w, int h) {
    super.setSize(w, h);
    handleResize(w, h);
  }
  
  public void setBounds(int x, int y, int w, int h) {
    super.setBounds(x, y, w, h);
    handleResize(w, h);
  }

  //------ private stuff
  
  private TreePath currentPathTo(Object node) {
    TreePath result = new TreePath(currentPath[0]);
    if(node == currentPath[0]) return result;
    for(int i = 1; i < currentPath.length; i++) {
      result = result.pathByAddingChild(currentPath[i]);
      if(node == currentPath[i])
	return result;
    }
    throw new IllegalArgumentException("node " + node + " not in " + new TreePath(currentPath));
  }
  
  private void removePane(int idx) {
    nodeListAt(idx).dispose();
    browser.remove(idx);
  }
  
  private NodeList nodeListAt(int idx) {
    return (NodeList)((JScrollPane)browser.getComponent(idx)).getViewport().getView();
  }
  
  private JScrollPane getScrollPane(NodeList list) {
    JScrollPane result = new JScrollPane(list);
    result.setHorizontalScrollBarPolicy(result.HORIZONTAL_SCROLLBAR_NEVER);
    result.setVerticalScrollBarPolicy(result.VERTICAL_SCROLLBAR_ALWAYS); 
    result.getViewport().getView().setBackground(new Color(0xCCCCCC));
    return result;
  }
  
  private void handleResize(int w, int h) {
    if(resizePolicy == KEEP_PANE_WIDTH)
      setPaneCount(w / minPaneWidth);
  }
  
  private void setView(TreePath path, int lastPathComponentPane_) {
    currentPath           = path.getPath();
    lastPathComponentPane = lastPathComponentPane_;
    // make sure index is visible
    if(lastPathComponentPane >= paneCount)
      lastPathComponentPane = paneCount - 1;
    // makes user index does not underrun
    if(lastPathComponentPane < 0)
      lastPathComponentPane = 0;
    // make sure we have enough path to display
    if(lastPathComponentPane >= distance(currentPath, getViewRoot(), currentPath[currentPath.length - 1]))
      lastPathComponentPane = distance(currentPath, getViewRoot(), currentPath[currentPath.length - 1]);
    refresh();
  }
  
  private void refresh() {
    int start = currentPath.length - lastPathComponentPane - 1;
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).setNode(start + i < currentPath.length ? currentPath[start + i] : null);
  }
  
  private int distance(Object[] path, Object fromNode, Object toNode) {
    int start = 0;
    try { while(path[start] != fromNode) start++; } 
    catch(ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("fromNode " + fromNode + " not in path:" + new TreePath(path));
    }
    int end = start;
    try{while(path[end] != toNode) end++;}
    catch(ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("fromNode " + fromNode + " not in path:" + new TreePath(path));
    }    
    return end - start;
  }
  
  private TreePath getCommonAncestor(Object[] p1, Object[] p2) {
    TreePath result = new TreePath(p1[0]);
    if(p1[0] != p2[0]) throw new IllegalArgumentException("root not common:" + new TreePath(p1) + ", " + new TreePath(p2));
    for(int i = 1; ; i++) {
      if(i >= p1.length || i >= p2.length)
	return result;
      if(p1[i] != p2[i])
	return result;
      else
	result = result.pathByAddingChild(p1[i]);
    }
  }
  
  private int getPaneIndexForNode(Object node) {
    for(int i = 0; i < paneCount; i++)
      if(nodeListAt(i).node == node)
	return i;
    return -1;
  }
  
  private void setPath(TreePath path) {
    Object leaf = null;
    if(path.getLastPathComponent() != getViewRoot() && controler.tree.isLeaf(path.getLastPathComponent())) {
      leaf = path.getLastPathComponent();
      path = path.getParentPath();
    }
    TreePath cap = getCommonAncestor(currentPath, path.getPath());
    int idx = getPaneIndexForNode(cap.getLastPathComponent());
    if(idx >= 0)
      setView(path, idx + distance(path.getPath(), cap.getLastPathComponent(), path.getLastPathComponent()));
    else
      setView(path, path.getPathCount());
    if(leaf != null)
      nodeListAt(getPaneIndexForNode(path.getLastPathComponent())).makeVisible(leaf);
    pathView.setPath(path);
    refresh();
  }

  private void select(TreePath path) {
    Object[]  p = path.getPath();
    
    for(int j = 0; j < paneCount; j++) {
      NodeList nl = nodeListAt(j);
      for(int i = 0; i < p.length - 1; i++) {
	if(p[i] == nl.node)
	  nl.select(p[i + 1]);
      }
    }
  }

  //------ TreeView implementation
  
  public Component getComponent() {
    return browser;
  }
  
  public void setContextMenuManager(ContextMenuManager manager) {
    ctxmgr = manager;
  }
  
  public Object locationToObject(Component component, Point location) {
    if(!(component instanceof JList)) return null;
    int idx = ((JList)component).locationToIndex(location);
    return idx == -1 ? null : ((JList)component).getModel().getElementAt(idx);
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
  
  public Object getViewRoot() {
    return viewRoot;
  }

  public void clearSelection() {
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).clearSelection();
  }
  
  public TreeModel getModel() {
    return controler == null ? null : controler.tree;
  }
  
  public TreePath getMostVisiblePath() {
    TreePath result = new TreePath(currentPath);
    Object   node   = result.getLastPathComponent();
    if(controler.tree.isLeaf(node) || controler.tree.getChildCount(node) == 0)
      return result;
    else
      return result.pathByAddingChild(controler.tree.getChild(node, 0));
  }
  
  public TreePath getSelectionPath() {
    TreePath[] tp = getSelectionPaths();
    return tp.length > 0 ? tp[0] : null;
  }

  public TreePath[] getSelectionPaths() {
    HashSet selection = new HashSet();
    
    for(int i = 0; i < paneCount; i++) {
      NodeList l = nodeListAt(i);
      TreePath p = l.getPath();
      if(p == null) break;
      
      int[] idxs = l.getSelectedIndices();
      for(int j = 0; j < idxs.length; j++)
	if(idxs[j] < l.getModel().getSize())
	  selection.add(p.pathByAddingChild(l.getModel().getElementAt(idxs[j])));
    }
    
    TreePath[] tmp = (TreePath[])selection.toArray(new TreePath[selection.size()]);
    for(int i = 0; i < tmp.length; i++)
    if(selection.contains(tmp[i].getParentPath()))
    selection.remove(tmp[i].getParentPath());

    return (TreePath[])selection.toArray(new TreePath[selection.size()]);
  }
                       
  public boolean isVisible(TreePath path) {
    for(int j = 0; j < paneCount; j++)
      if(path.getLastPathComponent().equals(nodeListAt(j).node))
	return true;
    return false;
  }
  
  public void makeVisible(TreePath path) {
    if(path == null) return;
    if(isVisible(path)) return;
    setPath(path);
  }
  
  public void setModel(TreeModel model) {
    setModel(model, null);
  }
  
  public void setModel(TreeModel model, TreePath viewRootPath) {
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).setNode(null);

    if(viewRootPath == null)
      viewRootPath = new TreePath(model.getRoot());
    if(controler != null)
      controler.dispose();
    controler = new Controler(model);
    viewRoot  = viewRootPath.getLastPathComponent();
    
    setView(viewRootPath, 0);

    pathView.setModel(model);
    pathView.setPath(viewRootPath, true);
  }
  
  public void setSelectionPath(TreePath path) {
    if(path == null) return;
    setSelectionPaths(new TreePath[] {path});
  }
  
  public void setSelectionPaths(TreePath[] path) {
    if(path == null || path.length == 0) return;
    clearSelection();
    TreePath maxPath = path[0];
    for(int i = 0; i < path.length; i++)
      if(path[i].getPathCount() > maxPath.getPathCount())
	maxPath = path[i];
    makeVisible(maxPath);
    for(int i = 0; i < path.length; i++)
      select(path[i]);
  }
  
  public void operationStart(String desc) {}
  
  public void operationProgress(int amount, int of) {
    if(amount == 0) Awt.setCursor(this, Cursor.WAIT_CURSOR);
  }
  
  public void operationStop() {
    Awt.setCursor(this, Cursor.DEFAULT_CURSOR);
  }

  DragGestureListener   srcListener;
  int                   srcActions;
  public void enableDrag(int sourceActions, DragGestureListener sourceListener) {
    disableDrag();
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).enableDrag(sourceActions, sourceListener);
    srcListener  = sourceListener;
    srcActions   = sourceActions;
  }

  DropTargetListener    trgtListener;
  int                   trgtActions;
  public void enableDrop(int targetActions, DropTargetListener targetListener) {
    disableDrop();
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).enableDrop(targetActions, targetListener);
    trgtListener = targetListener;
    trgtActions  = targetActions;
  }
  
  public void disableDrag() {
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).disableDrag();    
    srcListener  = null;
    System.gc();
  }

  public void disableDrop() {
    for(int i = 0; i < paneCount; i++)
      nodeListAt(i).disableDrop();    
    trgtListener = null;
    System.gc();
  }
  
  //------ helper classes
  
  class Controler 
    implements
    ChangeListener
  {
    ListTreeModel tree;
    
    Controler(TreeModel model) {
      tree = new ListTreeModel(model);
      pathView.addChangeListener(this);
    }

    void dispose() {
      pathView.removeChangeListener(this);
    }
    
    public void stateChanged(ChangeEvent e) {
      if(e.getSource() == pathView)
	setPath(pathView.getPath());
    }
  }
  
  ListSelectionListener lsl = Swing.asyncWrapper(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
	NodeList l = (NodeList)e.getSource();
	if(!e.getValueIsAdjusting()) {
	  int[] idxs = l.getSelectedIndices();
	  if(idxs.length == 1) {
	    ProgressWatcher.watch(SplitPaneTreeView.this);
	    if(!controler.tree.isLeaf(l.getModel().getElementAt(idxs[0]))) {
	      SplitPaneTreeView.this.makeVisible(currentPathTo(l.node).pathByAddingChild(l.getModel().getElementAt(idxs[0])));
	    }
	  }
	}
      }
    });
  
  MouseListener ml = new MouseAdapter() {
    public void mouseReleased(MouseEvent e) {
      NodeList l = (NodeList)e.getSource();
      int idx = l.locationToIndex(e.getPoint());
      if(idx == -1) idx = 0;
      else          idx = 1;
      for(int i = getPaneIndexForNode(l.node) + idx; i < SplitPaneTreeView.this.paneCount; i++)
        nodeListAt(i).clearSelection();    
    }
  };
  
  class NodeList
    extends
    JList 
  {     
    int       minSelected = Integer.MAX_VALUE;
    Rectangle r = new Rectangle(0, 0, 1, 1);
    Object    node;
    
    NodeList(Object node) {
      setNode(node);
      setCellRenderer(new CellRenderer(this));
      
      if(srcListener != null)
	enableDrag(srcActions, srcListener);
      if(trgtListener != null)
	enableDrop(trgtActions, trgtListener);

      addListSelectionListener(lsl);
      addMouseListener(ml);
    }
    
    void dispose() {
      disableDrag();
      disableDrop();
      removeListSelectionListener(lsl);
      removeMouseListener(ml);
    }
    
    void setNode(Object node_) {
      if(node_ == node) return;
      node = node_;
      ProgressWatcher.watch(SplitPaneTreeView.this);
      this.setModel(node == null ? new DefaultListModel() : controler.tree.getListModel(node, true));
    }
    
    void makeVisible(Object child) {
      r.setLocation(indexToLocation(controler.tree.getIndexOfChild(node, child)));
      scrollRectToVisible(r);
    }
    
    void select(Object node) {
      int count = this.getModel().getSize();
      for(int i = 0; i < count; i++)
	if(this.getModel().getElementAt(i).equals(node)) {
	  getSelectionModel().addSelectionInterval(i, i);
	  if(i < minSelected) {
	    ensureIndexIsVisible(i);
	    minSelected = i;
	  }
	}
    }
    
    public void clearSelection() {
      minSelected = Integer.MAX_VALUE;
      super.clearSelection();
    }
    
    public String toString() {
      return NodeList.this.getModel().toString();
    }
    
    public TreePath getPath() {
      if(node == null) return null;
      TreePath result = new TreePath(currentPath[0]);
      if(currentPath[0] == node) return result;
      for(int i = 1; ; i++) {
	result = result.pathByAddingChild(currentPath[i]);
	if(currentPath[i] == node)
	  return result;
      }
    }
    
    DragGestureRecognizer sourceRecognizer;
    void enableDrag(int sourceActions, DragGestureListener sourceListener) {
      sourceRecognizer = DragSource.getDefaultDragSource().
	createDefaultDragGestureRecognizer(this, sourceActions, sourceListener);
    }

    DropTarget dropTarget;
    void enableDrop(int targetActions, DropTargetListener  targetListener) {
      dropTarget = new DropTarget(this, targetActions, targetListener);
    }
    
    public void disableDrag() {
      if(sourceRecognizer != null)
	sourceRecognizer.removeDragGestureListener(srcListener);
      sourceRecognizer = null;
    }

    public void disableDrop() {
      if(dropTarget != null)
	dropTarget.removeDropTargetListener(trgtListener);
      dropTarget       = null;
    }
  }
  
  class CellRenderer 
    extends 
    DefaultListCellRenderer 
  {
    boolean  isLeaf;
    NodeList list;
    
    public CellRenderer(NodeList list_) {
      super();
      list = list_;
      setOpaque(true);
      setBorder(NO_FOCUS_BORDER);
    }
    
    public Component getListCellRendererComponent(JList   list,
						  Object  value,
						  int     index,
						  boolean isSelected,
						  boolean cellHasFocus)
    {
      NodeList nl = (NodeList)list;
      isLeaf = nl.node == null ? true : controler.tree.isLeaf(controler.tree.getChild(nl.node, index));
      
      return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if(!isLeaf)
	ICON.paintIcon(this, g, 
		       ((JViewport)list.getParent()).getExtentSize().width - ICON.getIconWidth(), 
		       (getHeight() - ICON.getIconHeight()) / 2);
    }
  }
}

/*
  $Log: SplitPaneTreeView.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.10  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.9  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.8  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.7  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2001/01/03 08:30:39  schubige
  graph stuff beta

  Revision 1.5  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.4  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.3  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.2  2000/08/17 16:34:22  schubige
  Fixed SplitPaneTreeView icon bug

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added
  
*/
