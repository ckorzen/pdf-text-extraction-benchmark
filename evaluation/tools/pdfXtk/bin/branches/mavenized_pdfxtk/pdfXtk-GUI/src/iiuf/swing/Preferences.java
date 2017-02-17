package iiuf.swing;

import java.io.Serializable;
import java.io.File;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

// import iiuf.log.Log;
import iiuf.awt.BorderLayout;
import iiuf.awt.Awt;
import iiuf.util.Util;
import iiuf.util.PrefWatcher;
import iiuf.util.PrefNamer;
import iiuf.util.PrefReanimator;

/**
   Swing related preferences watchers and namers.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Preferences {
  private static final int SWING_DELAY = 1000; // delay for async swing updates, should find a better solution once

  static class EmptyState implements Serializable {}
  
  static class JToolBarState implements Serializable {
    Point             location;
    boolean           floating;
    int               orientation;
    Object            constraints;
    transient boolean inited;

    JToolBarState(JToolBar cmp)   {
      inited = true;
      grab(cmp);
    }
    
    void grab(JToolBar cmp) {
      floating    = false;
      orientation = cmp.getOrientation();
      Component p = cmp.getParent();
      if(p instanceof Container) {
	Container ct = (Container)p;
	if(ct.getLayout() instanceof BorderLayout) {
	  constraints = getConstraints(cmp);
	  floating = 
	    !(BorderLayout.NORTH.equals(constraints) ||
	      BorderLayout.EAST.equals(constraints)  ||
	      BorderLayout.SOUTH.equals(constraints) ||
	      BorderLayout.WEST.equals(constraints));
	} 
      }
      if(floating)
	location = Awt.getWindow(cmp).getLocation();
    }
    
    void apply(JToolBar cmp) {
      if(inited) return;
      inited = true;
      cmp.setOrientation(orientation);
      if(floating) {
	if(cmp.getUI() instanceof BasicToolBarUI) {
	  Point l = (Point)location.clone();
	  ((BasicToolBarUI)cmp.getUI()).setFloatingLocation(l.x, l.y);	  
	  ((BasicToolBarUI)cmp.getUI()).setFloating(true, l);
	}
	if(cmp.getUI() instanceof JWindowToolBarUI) {
	  Point l = (Point)location.clone();
	  ((JWindowToolBarUI)cmp.getUI()).setFloatingLocation(l.x, l.y);	  
	  ((JWindowToolBarUI)cmp.getUI()).setFloating(true, l);
	}
      }
      else {
	Container p = (Container)cmp.getParent();
	p.remove(cmp);
	p.add(cmp, constraints);
      }
    }

    public String toString() {
      return 
	"location:"      + location +
	", floating:"    + floating +
	", orientation:" + orientation +
	", constraints:" + constraints +
	", inited:"      + inited;

    }

    private String getConstraints(JToolBar cmp) {
      Container    ct = (Container)cmp.getParent();
      BorderLayout bl = (BorderLayout)ct.getLayout();
      if(bl.getChild(ct, BorderLayout.NORTH) == cmp)
	return BorderLayout.NORTH;
      else if(bl.getChild(ct, BorderLayout.SOUTH) == cmp)
	return BorderLayout.SOUTH;
      else if(bl.getChild(ct, BorderLayout.EAST) == cmp)
	return BorderLayout.EAST;
      else if(bl.getChild(ct, BorderLayout.WEST) == cmp)
	return BorderLayout.WEST;
      return
	null;
    }
  }

  static class JFileChooserState implements Serializable {
    Point  location;
    File[] selection;
    
    JFileChooserState(JFileChooser cmp)   {
      grab(cmp);
    }
    
    void grab(JFileChooser jfc)  {
      location = jfc.getLocation();
      if(jfc.isMultiSelectionEnabled())
	selection = jfc.getSelectedFiles();
      else
	selection = new File[] {jfc.getSelectedFile()};
    }
    
    void apply(JFileChooser jfc) {
      jfc.setLocation(location);
      if(selection.length == 1) {
	if(selection[0] != null)
	  jfc.setSelectedFile(selection[0]);
      } else
	jfc.setSelectedFiles(selection);
    }
  }
  
  static class JSplitPaneState implements Serializable {
    int location;
    JSplitPaneState(JSplitPane cmp)     {grab(cmp);}
    void          grab(JSplitPane cmp)  {location = cmp.getDividerLocation();}
    void          apply(JSplitPane cmp) {cmp.setDividerLocation(location);}
    public String toString()            {return "location:" + location;}
  }
  
  static class JTextComponentState implements Serializable {
    int     dot;
    int     mark;
    int     len;
    boolean focus;
    transient boolean applied;
    transient long    firstGrab;
    JTextComponentState(JTextComponent cmp) {grab(cmp);}
    void grab(JTextComponent cmp)  { 
      if(firstGrab == 0)
	firstGrab = System.currentTimeMillis();
      if(applied || System.currentTimeMillis() - firstGrab > (5 * SWING_DELAY)) {
	dot  = cmp.getCaret().getDot(); 
	mark = cmp.getCaret().getMark();
	len  = cmp.getDocument().getLength();
      }
      focus = cmp.hasFocus();
    }
    void apply(JTextComponent cmp) {
      if(!applied && cmp.getDocument().getLength() >= len) {
	if(dot == mark) {
	  cmp.setCaretPosition(dot);
	} else {
	  cmp.setSelectionStart(mark);
	  cmp.setSelectionEnd(dot);
	  cmp.getCaret().setSelectionVisible(true);
	}
	if(focus) cmp.requestFocus();
	applied = true;
      }
    }
  }

  static class JTabbedPaneState implements Serializable{
    String            selected;
    transient boolean applied;
    transient long    firstGrab;
    JTabbedPaneState(JTabbedPane cmp) {grab(cmp);}
    void grab(JTabbedPane cmp) {
      if(firstGrab == 0)
	firstGrab = System.currentTimeMillis();
      if(applied || System.currentTimeMillis() - firstGrab > (5 * SWING_DELAY)) {
	try {
	  selected = iiuf.util.Preferences.getPath(cmp.getSelectedComponent());
	} catch(IllegalArgumentException e) {}
      }
    }
    void apply(JTabbedPane cmp) {
      if(!applied) {
	try {
	  Object c = iiuf.util.Preferences.getObjectForPath(selected);
	  if(c == null || !(c instanceof Component)) return;
	  cmp.setSelectedComponent((Component)c);
	  applied = true;
	} catch(IllegalArgumentException e) {}
      }
    }
    public String toString() {return "selected:" + selected;}
  }
  
  static class JViewportState implements Serializable {
    Point             position;
    transient boolean applied;
    
    JViewportState(JViewport cmp)      {grab(cmp);}
    void          grab(JViewport cmp)  {position = cmp.getViewPosition();}
    boolean       apply(JViewport cmp) {
      if(!applied && cmp.getView().getBounds().contains(position)) {
	cmp.setViewPosition(position);
	applied = true;
      }
      return applied;
    }
    public String toString() {return "position:" + position;}    
  }
  
  static class JTreeExpander
    implements
    ActionListener,
    TreeModelListener 
  {
    JTreeState state;
    JTree      tree;

    JTreeExpander(JTreeState state_, JTree tree_) {
      state = state_;
      tree  = tree_;
    }
    
    public void actionPerformed(ActionEvent e) {
      state.expandAll(tree);
    }

    public void treeNodesChanged(TreeModelEvent e) {}
    public void treeNodesRemoved(TreeModelEvent e) {}
    public void treeStructureChanged(TreeModelEvent e) {
      scheduleExpand();
    }
    public void treeNodesInserted(TreeModelEvent e) {
      scheduleExpand();
    }

    private void scheduleExpand() {
      Timer t = new Timer(SWING_DELAY, this); // ughh - find a besser solution
      t.setRepeats(false);
      t.start();
    }
  }
  
  static TreePath str2path(String[] path, int[] idxs, TreeModel model) {
    Object    node  = model.getRoot();
    TreePath  path_ = new TreePath(node);
  pathloop:
    for(int i = 0; i < path.length; i++) {
	Object n = node;
	int    c = model.getChildCount(node);
	
	if(c == 0) return null;
	
	// first we try with child @ idx.
	if(idxs[i] < 0)  idxs[i] = 0;
	if(idxs[i] >= c) idxs[i] = c - 1;
	
	node = model.getChild(n, idxs[i]);
	if(node.toString().equals(path[i])) {
	  path_ = path_.pathByAddingChild(node);
	  continue pathloop;
	}
	// failed -> loop over all childs
	// we start the search in a neighborhood of idxs[i]
	int stop = (c / 2) + 1;
	for(int j = 0; j < stop; j++) {
	  node = model.getChild(n, (idxs[i] + j) % c);
	  if(node.toString().equals(path[i])) {
	    path_ = path_.pathByAddingChild(node);
	    continue pathloop;
	  }
	  node = model.getChild(n, Math.abs(idxs[i] - j) % c);
	  if(node.toString().equals(path[i])) {
	    path_ = path_.pathByAddingChild(node);
	    continue pathloop;
	  }
	} 
	return null;
    }
    return path_;
  }
  
  static void path2str(Object[] path, String[] strs, int idxs[], TreeModel model) {
    for(int i = 0; i < strs.length; i++) {
      strs[i] = path[i + 1].toString();
      idxs[i] = model.getIndexOfChild(path[i], path[i + 1]);
    }
  }
  
  static class ButtonTreePathViewState implements Serializable {
    String[] path = new String[0];
    int[]    idxs = new int[0];

    ButtonTreePathViewState(ButtonTreePathView btpv) {
      grab(btpv);
    }
    
    void apply(ButtonTreePathView btpv) {
      btpv.setPath(str2path(path, idxs, btpv.getModel()));
    }
    
    void grab(ButtonTreePathView btpv) {
      Object[] pcs = btpv.getPath().getPath();
      path         = new String[pcs.length - 1];
      idxs         = new int[pcs.length - 1];
      path2str(pcs, path, idxs, btpv.getModel());
    }
  }

  static class JTreeState implements Serializable {
    String[][]           expandedPaths     = new String[0][];
    int[][]              expandedPathIdxs  = new int[0][];
    String[][]           selectionPaths    = new String[0][];
    int[][]              selectionPathIdxs = new int[0][];
    transient boolean[]  expanded = new boolean[0];

    synchronized void apply(JTree tree) {
      expanded = new boolean[expandedPaths.length];

      expandAll(tree);
      
      TreeModel model = tree.getModel();
      if(model != null)
	model.addTreeModelListener(new JTreeExpander(this, tree));
    }
    
    public String toString() {
      String result = "{";
      for(int i = 0; i < expandedPaths.length; i++) {
	result += "[";
	for(int j = 0; j < expandedPaths[i].length; j++)
	  result += expandedPaths[i][j] + " ";
	result += "]";
      }
      result += "}";
      
      return result;
    }
    
    void expandAll(JTree tree) {
      for(int i = 0; i < expandedPaths.length; i++)
	if(!expanded[i]) {
	  TreePath path = str2path(expandedPaths[i], expandedPathIdxs[i], tree.getModel());
	  if(path != null) {
	    expanded[i] = true;
	    tree.expandPath(path);
	  }
	}
    }
    
    void selectAll(JTree tree) {
      for(int i = 0; i < selectionPaths.length; i++) {
	TreePath path = str2path(selectionPaths[i], selectionPathIdxs[i], tree.getModel());
	if(path != null)
	  tree.addSelectionPath(path);
      }
    }
    
    boolean equals(String[] path1, int idxs1[], String[] path2, int[] idxs2) {
      if(path1.length != path2.length) return false;
      for(int i = 0; i < path1.length; i++)
	if(!path1[i].equals(path2[i]) || idxs1[i] != idxs2[i])
	  return false;
      return true;
    }
    
    boolean has(String[] path, int[] idxs) {
      for(int i = 0; i < expandedPaths.length; i++)
	if(equals(expandedPaths[i], expandedPathIdxs[i], path, idxs))
	  return true;
      return false;
    }
    
    synchronized void add(TreePath path_, JTree tree) {
      Object[] pcs  = path_.getPath();
      String[] path = new String[pcs.length - 1];
      int[]    idxs = new int[pcs.length - 1];
      path2str(pcs, path, idxs, tree.getModel());
      if(!has(path, idxs)) {
	String[][] tmp = expandedPaths;
	expandedPaths = new String[tmp.length + 1][];
	System.arraycopy(tmp, 0, expandedPaths, 0, tmp.length);
	expandedPaths[tmp.length] = path;
	
	boolean[] btmp = expanded;
	expanded = new boolean[btmp.length + 1];
	System.arraycopy(btmp, 0, expanded, 0, btmp.length);
	
	int[][] itmp = expandedPathIdxs;
	expandedPathIdxs = new int[itmp.length + 1][];
	System.arraycopy(itmp, 0, expandedPathIdxs, 0, itmp.length);
	expandedPathIdxs[itmp.length] = idxs;
      }
    }
    
    synchronized void remove(TreePath path_, JTree tree) {
      Object[] pcs  = path_.getPath();
      String[] path = new String[pcs.length - 1];
      int[]    idxs = new int[pcs.length - 1];
      path2str(pcs, path, idxs, tree.getModel());
      if(has(path, idxs)) {
	String[][] tmp = expandedPaths;
	expandedPaths = new String[tmp.length - 1][];
	int[][] itmp = expandedPathIdxs;
	expandedPathIdxs = new int[itmp.length - 1][];
	boolean[] btmp = expanded;
	expanded      = new boolean[btmp.length - 1];
	int j = 0;
	for(int i = 0; i < tmp.length; i++) {
	  if(!equals(path, idxs, tmp[i], itmp[i])) {
	    expandedPaths[j]    = tmp[i];
	    expandedPathIdxs[j] = itmp[i];
	    expanded[j]         = btmp[i];
	    j++;
	  }
	}
      }
    }
    
    void selection(TreePath[] paths, JTree tree) {
      if(paths == null) {
	selectionPaths    = new String[0][];
	selectionPathIdxs = new int[0][];
      } else {
	selectionPaths    = new String[paths.length][];
	selectionPathIdxs = new int[paths.length][];
	for(int i = 0; i < paths.length; i++) {
	  Object[] pcs         = paths[i].getPath();
	  selectionPaths[i]    = new String[pcs.length - 1];
	  selectionPathIdxs[i] = new int[pcs.length - 1];
	  path2str(pcs, selectionPaths[i], selectionPathIdxs[i], tree.getModel());
	}
      }
    }
  }
  
  static class JDesktopPaneState implements Serializable {
    int[]              positions;
    String[]           paths;
    transient int      applyCnt;
   
    JDesktopPaneState(JDesktopPane jdp) {grab(jdp);}
    
    void grab(JDesktopPane jdp) {
      if(applyCnt != 0) return;
      JInternalFrame[] frames = jdp.getAllFrames();
      positions = new int[frames.length];
      paths     = new String[frames.length];
  
      for(int i = 0; i < frames.length; i++) {
	iiuf.util.Preferences.watch(frames[i]);
	positions[i] = jdp.getPosition(frames[i]);
	paths[i]     = iiuf.util.Preferences.getPath(frames[i]);
      }
    }
    
    void apply(JDesktopPane jdp) {
      applyCnt++;
      JInternalFrame[] frames = jdp.getAllFrames();
      for(int i = 0; i < frames.length; i++)
	iiuf.util.Preferences.watch(frames[i]);
      
      JInternalFrame lastFrame = null;
      for(int j = 0; j < positions.length; j++) {
	int maxpos = Integer.MIN_VALUE;
	int maxidx = j;
	for(int i = 0; i < positions.length; i++) {
	  if(positions[i] > maxpos)
	    maxidx = i;
	}
	JInternalFrame frame = (JInternalFrame)iiuf.util.Preferences.getObjectForPath(paths[maxidx]);
	if(frame == null) {
	  Object[] prefs = iiuf.util.Preferences.getMulti(paths[maxidx]);
	  for(int i = 0; i < prefs.length; i++)
	    if(prefs[i] instanceof PrefReanimator) {
	      frame = (JInternalFrame)((PrefReanimator)prefs[i]).reanimate(paths[maxidx], prefs);
	      if(frame == null) continue;
	      jdp.add(frame);
	      iiuf.util.Preferences.watch(frame, paths[maxidx]);
	      frame.setVisible(true);
	      lastFrame = frame;
	      break;
	    }
	} else {
	  frame.toFront();
	  lastFrame = frame;
	}
	positions[maxidx] = Integer.MIN_VALUE;
      }
      if(lastFrame != null) 
	try{lastFrame.setSelected(true);} catch(Exception e) {}
      applyCnt--;
      positions = new int[0];
      grab(jdp);
    }
  }
      
  static class TableColumnState 
    implements 
    Serializable,
    PropertyChangeListener
  {
    int[] widths;
    int[] order;
    
    TableColumnState(TableColumnModel tcm) {grab(tcm);}
    
    void grab(TableColumnModel tcm) {
      order  = new int[tcm.getColumnCount()];
      
      int maxidx = -1;
      for(int i = 0; i < order.length; i++) {
	int idx = tcm.getColumn(i).getModelIndex();
	order[i] = idx;
	if(idx > maxidx)
	  maxidx = idx;
      }
      
      widths = new int[maxidx + 1];
      
      for(int i = 0; i < order.length; i++)
	widths[order[i]] = tcm.getColumn(i).getWidth();
      
      installListeners(tcm);
    }
    
    void apply(TableColumnModel tcm) {
      int count = Math.min(tcm.getColumnCount(), widths.length);
      for(int i = 0; i < count; i++) {
	TableColumn col = tcm.getColumn(i);
	col.setPreferredWidth(widths[col.getModelIndex()]);
	col.setWidth(widths[col.getModelIndex()]);
      }
      
      int last = Math.min(tcm.getColumnCount(), order.length) - 1;
      int idx  = 0;
      for(int i = last; i >= 0; i--) {
	for(int j = 0; j <= i; j++) {
	  if(tcm.getColumn(j).getModelIndex() == order[idx]) {
	    tcm.moveColumn(j, last);
	    break;
	  }
	}
	idx++;
      }
      
      installListeners(tcm);
    }
    
    void installListeners(TableColumnModel tcm) {
      for(int i = 0; i < tcm.getColumnCount(); i++) {
	TableColumn col = tcm.getColumn(i);
	col.removePropertyChangeListener(this);
	col.addPropertyChangeListener(this);
      }
    }

    public void propertyChange(PropertyChangeEvent e) {
      TableColumn tc = (TableColumn)e.getSource();
      widths[tc.getModelIndex()] = tc.getWidth();
    }
  }
  
  static class WindowState 
    implements
    Serializable 
  {
    boolean   icon;
    boolean   showing;
    long      activationTime;
    Rectangle bounds;
    
    WindowState(Window w) {grab(true, w);}
    
    void grab(boolean setATime, Window w) {
      bounds  = w.getBounds();
      showing = w.isShowing();
      if(w instanceof Frame)
	icon  = ((Frame)w).getState() == Frame.ICONIFIED;
      if(setATime)
	activationTime = System.currentTimeMillis();
    }
    
    void apply(Window w) {
      w.setSize(bounds.width, bounds.height);
      w.setLocation(bounds.x, bounds.y);
      if(w instanceof Frame)
	((Frame)w).setState(icon ? Frame.ICONIFIED : Frame.NORMAL);
      w.setVisible(showing);
    }
    
    public String toString() {
      return "icon:" + icon + ",showing:" + showing + ",activationTime:" + activationTime;
    }
  }

  static class JInternalFrameState
    implements
    Serializable
  {
    boolean   icon;
    boolean   showing;
    boolean   maximized;
    Rectangle iconizedBounds;
    Rectangle bounds;

    JInternalFrameState(JInternalFrame frame) {
      grab(false, frame);
    }
    
    void grab(boolean grabPosition, JInternalFrame frame) {
      icon      = frame.isIcon();
      showing   = frame.isVisible();
      maximized = frame.isMaximum();
      if(!icon && !maximized) bounds = frame.getBounds();
    }
    
    void grab(Component cmp) {
      iconizedBounds = cmp.getBounds();
    }
    
    void apply(JInternalFrame frame) {
      if(bounds != null && !frame.isMaximum() && !frame.isIcon()) 
	frame.setBounds(bounds);
      try{frame.setMaximum(maximized);} catch(Exception e) {}
      try{frame.setIcon(icon);}         catch(Exception e) {}
      if(iconizedBounds != null && icon) 
	frame.getDesktopIcon().setLocation(iconizedBounds.getLocation());
      frame.setVisible(showing);
    }
  }
  
  static class JTreeSelecter
    implements
    TreeExpansionListener,
    Runnable
  {    
    JTreeState state;
    JTree      tree;
    
    JTreeSelecter(JTreeState state_) {
      state = state_;
    }

    public void treeCollapsed(TreeExpansionEvent event) {
      tree = (JTree)event.getSource();
      
      state.remove(event.getPath(), tree);
    }
    public void treeExpanded(TreeExpansionEvent event) {
      tree = (JTree)event.getSource();
      
      if(tree.getParent().getParent() instanceof JScrollPane)
	SwingUtilities.invokeLater(this);
      
      state.add(event.getPath(), tree);
    }
    
    public void run() {
      state.selectAll(tree);
    }
  }
  
  private static boolean inited;
  public synchronized static void init() {
    if(inited) return;
    inited = true;
    
    iiuf.util.Preferences.register(Object.class, new PrefNamer() {
	public String getName(Object o) {return o.getClass().getName();}
      });
        
    iiuf.util.Preferences.register(Frame.class, new PrefNamer() {
	public String getName(Object o) {return ((Frame)o).getTitle();}
      });
    
    iiuf.util.Preferences.register(JInternalFrame.class, new PrefNamer() {
	public String getName(Object o) {return ((JInternalFrame)o).getTitle();}
      });
    
    iiuf.util.Preferences.register(TableColumn.class, new PrefNamer() {
	public String getName(Object o) {return ((TableColumn)o).getHeaderValue().toString();}
      });
    
    iiuf.util.Preferences.register(Component.class, new PrefNamer() {
	public String getName(Object o) {
	  String result = ((Component)o).getName();
	  if(result == null || result.equals("")) return o.getClass().getName();
	  else return result;
	}
      });
    
    //------ Window

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  Window window = (Window)o;

	  final WindowState result = preferences == null ? new WindowState(window) : (WindowState)preferences;
	  if(preferences != null) result.apply(window);  
	  
	  window.addWindowListener(new WindowAdapter() {
	      WindowState state = result;

	      public void windowOpened(WindowEvent e)      {state.grab(true,  e.getWindow());}
	      public void windowActivated(WindowEvent e)   {state.grab(true,  e.getWindow());}
	      public void windowDeactivated(WindowEvent e) {state.grab(false, e.getWindow());}
	      
	      public void windowIconified(WindowEvent e)   {state.grab(false, e.getWindow());}
	      public void windowDeiconified(WindowEvent e) {state.grab(false, e.getWindow());}
	    });
	  
	  return result;
	}
	
	public Class watchedClass() {return Window.class;}
      }); 

    //------ JTabbedPane
    
    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JTabbedPane jtp = (JTabbedPane)o;

	  // ensure that tabs are watched
	  
	  for(int i = 0; i < jtp.getTabCount(); i++)
	    iiuf.util.Preferences.watch(jtp.getComponentAt(i));
	  
	  final JTabbedPaneState result = preferences == null ? new JTabbedPaneState(jtp) : (JTabbedPaneState)preferences;
	  if(preferences != null) result.apply(jtp);
	  
	  jtp.addChangeListener(new ChangeListener() {
	      JTabbedPaneState state = result;
	      
	      public void stateChanged(ChangeEvent e) {
		state.grab((JTabbedPane)e.getSource());
	      }
	    });

	  jtp.addContainerListener(new ContainerListener() {
	      JTabbedPaneState state = result;
	      
	      public void componentAdded(ContainerEvent e) {
		iiuf.util.Preferences.watch(e.getChild());
		state.apply((JTabbedPane)e.getContainer());
	      }
	      public void componentRemoved(ContainerEvent e) {}
	    });
	  
	  return result;
	}
	
	
	public Class watchedClass() {return JTabbedPane.class;}
      }); 

    //------ JSplitPane

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JSplitPane jsp = (JSplitPane)o;
	  
	  final JSplitPaneState result = preferences == null ? new JSplitPaneState(jsp) : (JSplitPaneState)preferences;
	  if(preferences != null) result.apply(jsp);

	  jsp.addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY, 
					new PropertyChangeListener() {
					    JSplitPaneState state = result;
					    
					    public void propertyChange(PropertyChangeEvent e) {
					      state.grab((JSplitPane)e.getSource());
					    }
					  });
	  
	  return result;
	}
	
	public Class watchedClass() {return JSplitPane.class;}
      }); 

    //------ JTextComponent
    
    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JTextComponent jtc = (JTextComponent)o;
	  
	  final JTextComponentState result = preferences == null ? new JTextComponentState(jtc) : (JTextComponentState)preferences;
	  if(preferences != null) result.apply(jtc);
	  
	  jtc.addFocusListener(new FocusListener() {
	      JTextComponentState state = result;
	      
	      public void focusGained(FocusEvent e) {
		state.grab((JTextComponent)e.getSource());
	      }
	      public void focusLost(FocusEvent e) {
		state.grab((JTextComponent)e.getSource());
	      }
	    });
	  
	  jtc.addCaretListener(new CaretListener() {
	      JTextComponentState state = result;

	      public void caretUpdate(CaretEvent e) {
		state.grab((JTextComponent)e.getSource());
	      }
	    });
	  
	  return result;
	}

	public Class watchedClass() {return JTextComponent.class;}
      }); 

    //------ JViewport

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JViewport jvp = (JViewport)o;
	  boolean   applied;
	  
	  final JViewportState result = preferences == null ? new JViewportState(jvp) : (JViewportState)preferences;
	  if(preferences != null) result.apply(jvp);
	  
	  jvp.addChangeListener(new ChangeListener() {
	      JViewportState state = result;
	      
	      public void stateChanged(ChangeEvent e) {
		if(state.apply((JViewport)e.getSource()))
		  state.grab((JViewport)e.getSource());
	      }
	    });
	  
	  return result;
	}
	
	public Class watchedClass() {return JViewport.class;}
      }); 

    //------ TableColumnModel

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  final TableColumnModel tm = (TableColumnModel)o;
	  
	  final TableColumnState result = preferences == null ? new TableColumnState(tm) : (TableColumnState)preferences;
	  if(preferences != null) result.apply(tm);
	  
	  tm.addColumnModelListener(new TableColumnModelListener() {
	      TableColumnState state = result;
	      
	      public void columnAdded(TableColumnModelEvent e)   {state.grab((TableColumnModel)e.getSource());}
	      public void columnMarginChanged(ChangeEvent e)     {state.grab((TableColumnModel)e.getSource());}
	      public void columnMoved(TableColumnModelEvent e)   {state.grab((TableColumnModel)e.getSource());}
	      public void columnRemoved(TableColumnModelEvent e) {state.grab((TableColumnModel)e.getSource());}
	      public void columnSelectionChanged(ListSelectionEvent e) {}
	    });
	  
	  
	  return result;
	}
	
	public Class watchedClass() {return TableColumnModel.class;}
      });
    
    //------ JInternalFrame

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  final JInternalFrame jif = (JInternalFrame)o;
	  
	  final JInternalFrameState result = preferences == null ? new JInternalFrameState(jif) : (JInternalFrameState)preferences;
	  if(preferences != null) result.apply(jif);
	  
	  jif.addComponentListener(new ComponentAdapter() { 
	      JInternalFrameState         state = result;
	      
	      public void componentMoved(ComponentEvent e)   {
		state.grab(false, (JInternalFrame)e.getComponent());
	      }
	      public void componentResized(ComponentEvent e) {
		state.grab(false, (JInternalFrame)e.getComponent());
	      }
	    });
	  
	  jif.addInternalFrameListener(new InternalFrameListener() {
	      JInternalFrameState         state = result;
	      JInternalFrame.JDesktopIcon icon;
	      boolean                     setIconBounds = state.iconizedBounds != null && !state.icon;
	      boolean                     setBounds     = state.icon;
	      
	      int dummy = installIconListener(jif);
	      
	      public void internalFrameClosed(InternalFrameEvent e) {}
	      
	      public void internalFrameActivated(InternalFrameEvent e) {
		state.grab(true, (JInternalFrame)e.getSource());
	      }
	      public void internalFrameDeiconified(InternalFrameEvent e) {
		JInternalFrame frame = (JInternalFrame)e.getSource();
		if(setBounds) {
		  try{frame.setMaximum(state.maximized);} catch(Exception ex) {}
		  if(!state.maximized && state.bounds != null)
		    frame.setBounds(state.bounds);
		  setBounds = false;
		}
		state.grab(true, frame);
	      }
	      public void internalFrameOpened(InternalFrameEvent e) {
		state.grab(true, (JInternalFrame)e.getSource());
	      }
	      public void internalFrameClosing(InternalFrameEvent e) {
		state.grab(false, (JInternalFrame)e.getSource());
	      }
	      public void internalFrameDeactivated(InternalFrameEvent e) {
		state.grab(false, (JInternalFrame)e.getSource());
	      }
	      public synchronized void internalFrameIconified(InternalFrameEvent e) {
		JInternalFrame frame = (JInternalFrame)e.getSource();
		state.grab(false, frame);
		installIconListener(frame);
		state.grab(frame.getDesktopIcon());
	      }
	      
	      int installIconListener(JInternalFrame frame) {
		if(!state.icon) return 0;
		if(icon != frame.getDesktopIcon()) {
		  icon = frame.getDesktopIcon();
		  icon.addComponentListener(new ComponentAdapter() {
		      public void componentMoved(ComponentEvent e)   {state.grab(e.getComponent());}
		      public void componentResized(ComponentEvent e) {state.grab(e.getComponent());}
		    });
		  if(setIconBounds) {
		    icon.setLocation(state.iconizedBounds.getLocation());
		    setIconBounds = false;
		  }
		}
		return 0;
	      }
	    });	  
	  return result;
	}

	public Class watchedClass() {return JInternalFrame.class;}
      });
    
    //------ JDesktopPane

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JDesktopPane jdp = (JDesktopPane)o;
	  
	  final JDesktopPaneState result = preferences == null ? new JDesktopPaneState(jdp) : (JDesktopPaneState)preferences;
	  if(preferences != null) result.apply(jdp);
	  
	  jdp.addContainerListener(new ContainerListener() {
	      JDesktopPaneState state = result;
	      
	      public void componentAdded(ContainerEvent e)   {state.grab((JDesktopPane)e.getContainer());}
	      public void componentRemoved(ContainerEvent e) {state.grab((JDesktopPane)e.getContainer());}
	    });
	  
	  return result;
	}
	
	public Class watchedClass() {return JDesktopPane.class;}
      });
    
    //------ ButtonTreePathView

    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  ButtonTreePathView btpv = (ButtonTreePathView)o;
	  
	  final ButtonTreePathViewState result = 
	    preferences == null ? new ButtonTreePathViewState(btpv) :
	    (ButtonTreePathViewState)preferences;
	  
	  if(preferences != null) result.apply(btpv);
	  
	  btpv.addChangeListener(new ChangeListener() {
	      ButtonTreePathViewState state = result;
	      
	      public void stateChanged(ChangeEvent e) {
		state.grab((ButtonTreePathView)e.getSource());
	      }
	    });
	  
	  return result;
	}
	
	public Class watchedClass() {return ButtonTreePathView.class;}
      });
    
    // ----- JFileChooser
    
    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JFileChooser jfc = (JFileChooser)o;
	  
	  final JFileChooserState result = preferences == null ? new JFileChooserState(jfc) : (JFileChooserState)preferences;
	  if(preferences != null) result.apply(jfc);
	  
	  PropertyChangeListener listener = new PropertyChangeListener() {
	      JFileChooserState state = result;
	      
	      public void propertyChange(PropertyChangeEvent e) {
		state.grab((JFileChooser)e.getSource());
	      }
	    };
	  
	  
	  jfc.addPropertyChangeListener(jfc.SELECTED_FILE_CHANGED_PROPERTY, listener);
	  jfc.addPropertyChangeListener(jfc.SELECTED_FILES_CHANGED_PROPERTY, listener);
	  
	  jfc.addAncestorListener(new AncestorListener() {
	      JFileChooserState state = result;
	      
	      public void ancestorAdded(AncestorEvent e) {
		state.grab((JFileChooser)e.getSource());
	      }

	      public void ancestorMoved(AncestorEvent e) {
		state.grab((JFileChooser)e.getSource());
	      }
	      
	      public void ancestorRemoved(AncestorEvent e) {
		state.grab((JFileChooser)e.getSource());
	      }
	    });

	  return result;
	}
	
	public Class watchedClass() {return JFileChooser.class;}
      });

    // ----- JToolBar
    
    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JToolBar jtb = (JToolBar)o;
	  
	  final JToolBarState result = preferences == null ? new JToolBarState(jtb) : (JToolBarState)preferences;
	  if(preferences != null) result.apply(jtb);
	  
	  if(jtb.getParent() instanceof Container) {
	    ((Container)jtb.getParent()).addContainerListener(new ContainerListener() {
		JToolBarState state = result;
		
		public void componentAdded(ContainerEvent e) {
		  state.grab((JToolBar)e.getChild());
		}
		public void componentRemoved(ContainerEvent e) {}
	      });
	  }
	  
	  jtb.addAncestorListener(new AncestorListener() {
	      JToolBarState state = result;
	      
	      public void ancestorAdded(AncestorEvent e) {}

	      public void ancestorMoved(AncestorEvent e) {
		state.grab((JToolBar)e.getSource());
	      }
	      
	      public void ancestorRemoved(AncestorEvent e) {
		state.grab((JToolBar)e.getSource());
	      }
	    });

	  return result;
	}
	
	public Class watchedClass() {return JToolBar.class;}
      });

    //------ JTree
    
    iiuf.util.Preferences.register(new PrefWatcher() {
	public Serializable watch(Object o, Serializable preferences) {
	  JTree jt = (JTree)o;
	  
	  final JTreeState result = preferences == null ? new JTreeState() : (JTreeState)preferences;
	  if(preferences != null) result.apply(jt);
	  
	  jt.addTreeExpansionListener(new JTreeSelecter(result));
	  
	  jt.addPropertyChangeListener(jt.TREE_MODEL_PROPERTY, new PropertyChangeListener() {
	      JTreeState state = result;
	      
	      public void propertyChange(PropertyChangeEvent e) {
		state.apply((JTree)e.getSource());
	      }
	    });
	  
	  jt.addTreeSelectionListener(new TreeSelectionListener() {
	      JTreeState state = result;
	      public void valueChanged(TreeSelectionEvent e) {
		state.selection(((JTree)e.getSource()).getSelectionPaths(), (JTree)e.getSource());
	      }
	    });
	  return result;
	}
	
	public Class watchedClass() {return JTree.class;}
      });

    //----- Container
    
    iiuf.util.Preferences.register(new PrefWatcher() {
	
	public Serializable watch(Object o, Serializable preferences) {
	  chkWatch((Container)o, (Container)o); 
	  return new EmptyState();
	}
	
	public Class watchedClass() {return Container.class;}
	
	void chkWatch(Container root, Component cmp) {
	  if(cmp instanceof JSplitPane         ||
	     cmp instanceof JDesktopPane       ||
	     cmp instanceof JTree              ||
	     cmp instanceof JViewport          ||
	     cmp instanceof JTabbedPane        ||
	     cmp instanceof JTextComponent     ||
	     cmp instanceof ButtonTreePathView ||
	     cmp instanceof JFileChooser       ||
	     cmp instanceof JToolBar           ||
	     cmp instanceof JInternalFrame)
	    addWatch(root, cmp);
	  if(cmp instanceof JTable) {
	    JTable table = (JTable)cmp;
	    addWatch(root, table);
	    iiuf.util.Preferences.watch(table, table.getColumnModel());
	    if(table.getTableHeader() != null)
	      table.getTableHeader().repaint();
	    return;
	  }
	  if(cmp instanceof Container) {
	    Component[] cmps = ((Container)cmp).getComponents();
	    for(int i = 0; i < cmps.length; i++)
	      chkWatch(root, cmps[i]);
	  }
	}
	
	void addWatch(Container root, Component cmp) {
	  if(cmp == root) return;
	  if(iiuf.util.Preferences.isWatched(cmp)) return;
	  addWatch(root, cmp.getParent());
	  iiuf.util.Preferences.watch(cmp);
	}
      });
  }
}

/*
  $Log: Preferences.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.20  2001/05/11 11:30:26  schubige
  fns demo final

  Revision 1.19  2001/04/12 11:40:49  schubige
  fixed toolbar preferences bug

  Revision 1.18  2001/04/12 11:34:46  schubige
  fixed toolbar preferences bug

  Revision 1.17  2001/04/12 11:17:31  schubige
  fixed toolbar preferences bug

  Revision 1.16  2001/04/11 14:17:03  schubige
  adapted tinja stuff for semantic checks

  Revision 1.15  2001/03/30 17:33:25  schubige
  modified beat soundlet

  Revision 1.14  2001/03/01 10:42:48  schubige
  interim checkin for soundium

  Revision 1.13  2001/02/13 14:49:06  schubige
  started work on gui - engine connection

  Revision 1.12  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.11  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.10  2001/01/03 08:30:39  schubige
  graph stuff beta

  Revision 1.9  2000/12/20 10:49:51  schubige
  TJGUI update

  Revision 1.8  2000/12/20 10:28:44  schubige
  TJGUI update

  Revision 1.7  2000/11/29 09:45:42  schubige
  tinja IDE beta 3 - added focus hack, fixed preferences deadlock

  Revision 1.6  2000/11/27 16:10:45  schubige
  tinja IDE beta 2

  Revision 1.5  2000/11/24 17:50:44  schubige
  Tinja IDE beta 1

  Revision 1.4  2000/11/23 15:25:13  schubige
  intermediate TJGUI checkin

  Revision 1.3  2000/11/23 15:15:06  schubige
  intermediate TJGUI checkin

  Revision 1.2  2000/11/20 17:36:57  schubige
  tinja project ide

  Revision 1.1  2000/11/09 07:53:08  schubige
  early checkin for DCJava

  Revision 1.2  2000/10/19 08:03:45  schubige
  Intermediate graph component related checkin
  
*/
