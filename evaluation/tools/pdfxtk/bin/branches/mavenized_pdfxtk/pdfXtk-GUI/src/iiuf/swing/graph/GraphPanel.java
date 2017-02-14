package iiuf.swing.graph;

/*
  TODO
  - addContext menu doesn't work after setEditable(true)
*/

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.awt.Point;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTarget;
import java.lang.reflect.Array;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import iiuf.awt.Awt;
import iiuf.swing.Swing;
import iiuf.swing.ContextMenuEnabled;
import iiuf.swing.ContextMenuManager;
import iiuf.swing.SetSelectionModel;
import iiuf.swing.HexagonalBorder;
import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.Util;
import iiuf.util.EventListenerList;
import iiuf.util.graph.GraphNode;
import iiuf.util.graph.GraphModel;
import iiuf.util.graph.DefaultGraphModel;
import iiuf.util.graph.GraphPort;
import iiuf.util.graph.DefaultGraphNode;
import iiuf.util.graph.DefaultGraphEdge;
import iiuf.util.graph.GraphModelListener;

/**
   Graph visualizer class.<p>

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class GraphPanel
  extends
  JPanel
  implements
  Scrollable,
  ContextMenuEnabled,
  GraphModelListener,
  DropTargetListener
{
  public static final String IS_EDITABLE              = "IS_EDITABLE";
  public static final String SELECTION_BOUNDS_CHANGED = "SELECTION_BOUNDS_CHANGED";
  
  static final int    SELECTION_MARK_SIZE   = 6;
  static final int    SELECTION_MARK_CENTER = SELECTION_MARK_SIZE / 2;
  static final String COMPONENT_TAG         = "__component__";
  static final String GRAPH_NODE_PORT_TAG   = "__graph_node_port__";
  static final String EDGE_TAG              = "__edge__";
  
  public int COMPONENT       = -1;
  public int GRAPH_EDGE      = -1;
  public int NODE_PROPERTIES = -1;
  public int GRAPH_NODE_PORT = -1;

  protected GraphPanelEditor editor;
  
  private GraphRouter                  router;
  private GraphModel                   graph;
  private AbstractNodeComponentFactory nodeFactory;
  private AbstractGraphEdgeFactory     edgeFactory;
  private AbstractPortFactory          portFactory;
  private AbstractPropertiesFactory    propertiesFactory;
  private GraphLayout                  layout;
  private boolean                      edgeAfter;
  private HashMap                      cmpToModelMap  = new HashMap();
  private HashMap                      edgeToModelMap = new HashMap();
  private GraphNode                    showPorts;
  private GraphPort                    preferredPort;
  private SelectionModel               selectionModel = new SelectionModel();
  private Rectangle                    selection      = new Rectangle();
  private Point                        dot            = new Point();
  private Point                        mark           = new Point();
  private int                          tolerance      = 5;
  private Color                        selectionColor; 
  private Rectangle                    tmpRect = new Rectangle();
  private ToolTipManager               toolTipManager;
  private Rectangle                    selectionBoundingBox;
  private Rectangle                    oldSelectionBoundingBox;
  int                                  layoutBlock;
  int                                  settingModel;
  boolean                              edit;
  
  public GraphPanel(AbstractNodeComponentFactory nodeFactory,
		    AbstractPortFactory          portFactory,
		    AbstractGraphEdgeFactory     edgeFactory,
		    NodeLayouter layouter,
		    GraphRouter  router) {
    this();
    setNodeComponentFactory(nodeFactory);
    nodeFactory.gp = this;
    setPortFactory(portFactory);
    setGraphEdgeFactory(edgeFactory);
    setRouter(router);
    setLayouter(layouter);
  }
  
  public GraphPanel() {
    toolTipManager = ToolTipManager.sharedInstance();

    setNodeComponentFactory(new AbstractNodeComponentFactory(GraphPanel.this) {
	protected Component newNodeComponent(GraphNode node, Object[] args) {
	  return new JButton("Node");
	}
      });
    
    setPortFactory(new AbstractPortFactory() {
	protected GraphNodePort newPort(GraphPort port, Object[] args) {
	  return null;
	}
      });
    
    setGraphEdgeFactory(new AbstractGraphEdgeFactory() {
	protected GraphEdge newGraphEdge(iiuf.util.graph.GraphEdge edge,
					 GraphNode fromNode, GraphPort fromPort, 
					 GraphNode toNode,   GraphPort toPort, Object[] args) {
	  return new GraphEdge((Component)fromNode.get(COMPONENT), (GraphNodePort)fromPort.get(GRAPH_NODE_PORT),
			       (Component)toNode.get(COMPONENT),   (GraphNodePort)toPort.get(GRAPH_NODE_PORT));
	}
      });
    
    setPropertiesFactory(new AbstractPropertiesFactory() {
	protected Component newPropertiesWindow(GraphNode node, Object[] args) {
	  return null;
	}
      });
    
    setRouter(new StraightLineRouter());
    
    editor = new GraphPanelEditor(this);
    editor.setAutoscrolls(true);
    editor.setSize(0 ,0);
    add(editor);
  
    layout = new GraphLayout(new DefaultNL());
    setLayout(layout);
    selectionModel.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  repaint();
	}
      });
    setAutoscrolls(true);
    
    editor.addMouseMotionListener(new Scroller());
    addMouseMotionListener(new Scroller());

    setSelectionColor(UIManager.getColor("Tree.selectionBackground"));
  }
  
  static class Scroller extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
      ((JComponent)e.getSource()).scrollRectToVisible(r);
    }
  }
  
  public Dimension getPreferredScrollableViewportSize() {return getPreferredSize(); }
  public boolean   getScrollableTracksViewportHeight() {
    return (getParent() instanceof JViewport) ? (((JViewport)getParent()).getHeight() > getPreferredSize().height) : false;
  }
  public boolean   getScrollableTracksViewportWidth()  {
    return (getParent() instanceof JViewport) ? (((JViewport)getParent()).getWidth() > getPreferredSize().width) : false;
  }
  public int       getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {return 8;}
  public int       getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    int result = orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    if(result > 32) result -= 16;
    return result;
  }
  
  public void setSelectionColor(Color newColor) {
    selectionColor = newColor;
  }
  
  public Color getSelectionColor() {
    return selectionColor;
  }

  public void setEdgeSelectionTolerance(int tolerance_) {
    tolerance = tolerance_;
  }

  public int getEdgeSelectionTolerance() {
    return tolerance;
  }

  public GraphPanelEditor getEditor() {
    return editor;
  }

  public SetSelectionModel getSelectionModel() {
    return selectionModel;
  }
  
  public void setNodeComponentFactory(AbstractNodeComponentFactory nodeFactory_) {
    nodeFactory = nodeFactory_;
  }

  public void setPortFactory(AbstractPortFactory portFactory_) {
    portFactory = portFactory_;
  }
  
  public void setPropertiesFactory(AbstractPropertiesFactory propertiesFactory_) {
    propertiesFactory = propertiesFactory_;
  }

  public void setGraphEdgeFactory(AbstractGraphEdgeFactory edgeFactory_) {
    edgeFactory = edgeFactory_;
  }
  
  private GraphNode showPortsNodeCache;
  private GraphPort showPortsPrefferedPortCache;
  public void setShowPorts(GraphNode node, GraphPort preferredPort_) {
    if(showPortsNodeCache == node && showPortsPrefferedPortCache == preferredPort_)
      return;
    showPortsNodeCache          = node;
    showPortsPrefferedPortCache = preferredPort_;
    if(showPorts != null)
      repaint(((Component)showPorts.get(COMPONENT)).getBounds(tmpRect));
    showPorts     = node;
    preferredPort = preferredPort_;
    if(showPorts != null)
      repaint(((Component)showPorts.get(COMPONENT)).getBounds(tmpRect));
  }
  
  public GraphModel getModel() {
    return graph;
  }
  
  public void setModel(GraphModel graph_) {
    settingModel++;
    setShowPorts(null, null);
    selectionModel.clearSelection();
    if(graph != null) 
      graph.removeGraphModelListener(this);
    graph = graph_;
    graph.addGraphModelListener(this);
    
    COMPONENT       = graph.nodeAttribute(COMPONENT_TAG,       nodeFactory);
    NODE_PROPERTIES = graph.nodeAttribute("properties",        propertiesFactory);
    GRAPH_NODE_PORT = graph.portAttribute(GRAPH_NODE_PORT_TAG, portFactory);
    GRAPH_EDGE      = graph.edgeAttribute(EDGE_TAG,            edgeFactory);
    
    Component[]    cs = getComponents();
    for(int i = 0; i < cs.length; i++)
      if(cs[i] instanceof JComponent)
	toolTipManager.unregisterComponent((JComponent)cs[i]);
    removeAll();
    
    add(editor);
    
    GraphNode[] nodes = graph.nodesArray();
    for(int i = 0; i < nodes.length; i++)
      addNode(nodes[i]);
    
    edgeca = null;
    
    settingModel--;
    validate();
    repaint(0, 0, getWidth(), getHeight());
  }
  
  public Object viewToModel(Point p) {
    return viewToModel(p.x, p.y);
  }
  
  public Object viewToModel(int x, int y) {
    Object result = locationToObject(this, x, y);
    if(result instanceof GraphEdge)
      return viewToModel((GraphEdge)result);
    else
      return viewToModel((Component)result);
  }
  
  public GraphNode viewToModel(Component c) {
    return (GraphNode)cmpToModelMap.get(c);
  }

  public iiuf.util.graph.GraphEdge viewToModel(GraphEdge e) {
    return (iiuf.util.graph.GraphEdge)edgeToModelMap.get(e);
  }
  
  public void nodesAdded(GraphModel model, GraphNode[] nodes) {
    layoutBlock++;
    for(int i = 0; i < nodes.length; i++)
      addNode(nodes[i]);
    layoutBlock--;
    doLayout();
    repaint();
  }
  
  public void nodesRemoved(GraphModel model, GraphNode[] nodes) {
    layoutBlock++;
    for(int i = 0; i < nodes.length; i++)
      removeNode(nodes[i]);
    layoutBlock--;
    setShowPorts(null, null);
    doLayout();
    repaint();
  }
  
  public void edgesAdded(GraphModel model, iiuf.util.graph.GraphEdge[] edges) {
    layoutBlock++;
    edgeca = null;
    for(int i = 0; i < edges.length; i++) {
      GraphNode node = edges[i].getToNode();
      if(node instanceof ConnectingNode &&
	 editor.connectingNode != node) {
	Rectangle bounds = ((Component)edges[i].getFromNode().get(COMPONENT)).getBounds(tmpRect);
	((Component)node.get(COMPONENT)).setLocation(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
	break;
      }
    }
    layoutBlock--;
    doLayout();
    repaint();
  }

  public void edgesRemoved(GraphModel model, iiuf.util.graph.GraphEdge[] edges) {
    layoutBlock++;
    SetSelectionModel sm = getSelectionModel();
    for(int i = 0; i < edges.length; i++)
      sm.remove(edges[i]);
    edgeca = null;
    layoutBlock--;
    setShowPorts(null, null);
    doLayout();
    repaint();
  }
  
  public void setEditable(boolean state) {
    if(edit != state) {
      edit = state;
      if(edit) {
	editor.setSize(getSize());
	editor.start();
	if(ctxmgr != null)
	  ctxmgr.setComponent(editor);
      }
      else {
	selectionModel.clearSelection();
	editor.stop();
	editor.setSize(0, 0);
	if(ctxmgr != null)
	  ctxmgr.setComponent(this);
      }
      repaint();
      firePropertyChange(IS_EDITABLE, !state, state);      
    }
  }
  
  public boolean isEditable() {
    return edit;
  }
  
  public void setLayouter(NodeLayouter layouter) {
    layout.setLayouter(layouter);
    doLayout();
    repaint();
  }
  
  public NodeLayouter getLayouter() {
    return layout.getLayouter();
  }
  
  public void setRouter(GraphRouter router_) {
    router = router_;
    router.init();
    repaint();
  }

  public void setPaintEdgesAfterNodes(boolean state) {
    if(state != edgeAfter) {
      edgeAfter = state;
      repaint();
    }
  }
  
  private void addNode(GraphNode node) {
    Component cmp = (Component)node.get(COMPONENT);
    if(cmp instanceof GraphNodeComponent)
      ((GraphNodeComponent)cmp).addComponent(this, 1);
    else
      add(cmp, 1);
    cmpToModelMap.put(node.get(COMPONENT), node);
    nodeca = null;
  }
  
  private void removeNode(GraphNode node) {
    Component c = (Component)node.get(COMPONENT);
    
    if(c instanceof JComponent)
      toolTipManager.unregisterComponent((JComponent)c);
    if(c instanceof GraphNodeComponent)
      ((GraphNodeComponent)c).dispose();
    remove(c);
    cmpToModelMap.remove(node.get(COMPONENT));
    getSelectionModel().remove(node);
    nodeca = null;
  }
  
  public GraphNode findNodeAt(int x, int y) {
    return (GraphNode)cmpToModelMap.get(locationToComponent(x, y));
  }
  
  public GraphPort findPortAt(int x, int y) {
    GraphNode n = findNodeAt(x, y);
    return n == null ? null : findPortAt(n, x, y);
  }
  
  public GraphPort findPortAt(GraphNode node, int mx, int my) {
    GraphPort[] ports = node.getPorts();

    Component cmp = (Component)node.get(COMPONENT);
    
    int    mind = Integer.MAX_VALUE;
    int    min  = 0;
    int    cw   = cmp.getWidth();
    int    ch   = cmp.getHeight();
    int    cx   = cmp.getX();
    int    cy   = cmp.getY();
    for(int i = 0; i < ports.length; i++) {
      GraphNodePort port = (GraphNodePort)ports[i].get(GRAPH_NODE_PORT);
      if(port == null) return null;
      int x = mx - (int)(port.x * cw + cx);
      int y = my - (int)(port.y * ch + cy);
      int d = x * x + y * y;
      if(d < mind) {
	min  = i;
	mind = d;
      }
    }
    
    return ports[min];
  }
 
  GraphEdge[] edgeca;
  private GraphEdge[] getEdges() {
    if(graph == null) return new GraphEdge[0];
    if(edgeca == null) {
      edgeToModelMap = new HashMap();
      iiuf.util.graph.GraphEdge[] tmp = graph.edgesArray();
      edgeca = new GraphEdge[tmp.length];
      for(int i = 0; i < tmp.length; i++) {
	edgeca[i] = (GraphEdge)tmp[i].get(GRAPH_EDGE);
	edgeToModelMap.put(edgeca[i], tmp[i]);
      }
    }
    return edgeca;
  }

  Component[] nodeca;
  private Component[] getNodes() {
    if(graph == null) return new Component[0];
    if(nodeca == null) {
      GraphNode[] tmp = graph.nodesArray();
      nodeca = new Component[tmp.length];
      for(int i = 0; i < tmp.length; i++)
      nodeca[i] = (Component)tmp[i].get(COMPONENT);
    }
    return nodeca;
  }
  
  public Object locationToObject(Component component, Point location) {
    return locationToObject(component, location.x, location.y);
  }
  
  private Component locationToComponent(int x, int y) {
    synchronized (getTreeLock()) {
      Component[] components  = getComponents();
      
      for(int i = 0; i < components.length; i++) {
	Component comp = components[i];
	if(comp != null && comp != editor)
	  if (comp.contains(x - comp.getX(), y - comp.getY()))
	    return comp;
      }
    }
    return null;
  }
  
  public Object locationToObject(Component component, int x, int y) {
    if(!contains(x, y)) return null;
    
    Component cmp = locationToComponent(x, y);
    if(cmp != null) return cmp;
    
    GraphEdge[] edges = getEdges();
    for(int i = 0; i < edges.length; i++)
      if(edges[i].pointIsNear(x, y, tolerance))
	return edges[i];

    return this;
  }  
  
  public Component getComponent() {
    return this;
  }

 private ContextMenuManager ctxmgr;
  public void setContextMenuManager(ContextMenuManager manager) {
    ctxmgr = manager;
  }
  
  public ContextMenuManager getContextMenuManager() {
    return ctxmgr;
  }
  
  public ContextMenuManager addContextMenu() {
    if(ctxmgr == null)
      ctxmgr = new ContextMenuManager(this);
    return ctxmgr;
  }
  
  public Rectangle getDotMarkRectangle() {
    return selection;
  }
  
  public boolean isDragging(GraphNode node) {
    return editor.dragable == node || node instanceof ConnectingNode;
  }
  
  public void setDot(int x, int y) {
    dot.setLocation(x, y);
    setSelection(x, y);
  }
  
  public void setMark(int x, int y) {
    mark.setLocation(x, y);
    setSelection(x, y);
  }
  
  private void setSelection(int x, int y) {
    tmpRect = (Rectangle)selection.clone();
    int w = selection.width;
    int h = selection.height;
    selection.setBounds(x, y, 0, 0);
    selection.add(dot);
    if(selection.width != w || selection.height != h) {
      tmpRect.add(selection);
      tmpRect.width++;
      tmpRect.height++;
      repaint(tmpRect);
    }
  }
  
  public void repaint(int x, int y, int w, int h) {
    super.repaint(x - SELECTION_MARK_CENTER, y - SELECTION_MARK_CENTER, w + SELECTION_MARK_SIZE, h + SELECTION_MARK_SIZE);
  }
  
  public void paintChildren(Graphics g) {
    if(settingModel != 0) {
      repaint(100);
      return;
    }
    oldSelectionBoundingBox = selectionBoundingBox;
    if(!edgeAfter)
      paintEdges(g);
    super.paintChildren(g);
    if(edgeAfter)
      paintEdges(g);
    g.setColor(selectionColor);
    Graphics2D g2 = (Graphics2D)g;
    if(!selectionModel.isEmpty()) {
      selectionBoundingBox = null;
      Object[]  sel = selectionModel.getSelection();
      for(int i = 0; i < sel.length; i++) {
	if(!(sel[i] instanceof GraphNode)) continue;
	Rectangle bounds  = ((Component)((GraphNode)sel[i]).get(COMPONENT)).getBounds(tmpRect);
	g2.draw(bounds);
	if(selectionBoundingBox == null)
	  selectionBoundingBox = (Rectangle)bounds.clone();
	else
	  selectionBoundingBox.add(bounds);
      }
      if(selectionBoundingBox != null) {
	g2.draw(selectionBoundingBox);
	
	if(getLayouter().allowsNodeLocationChange()) {
	  int x  = selectionBoundingBox.x;
	  int y  = selectionBoundingBox.y;
	  int w  = selectionBoundingBox.width;
	  int wh = w / 2;
	  int h  = selectionBoundingBox.height;
	  int hh = h / 2;
	  
	  drawHandle(g, x,      y);
	  drawHandle(g, x + wh, y);
	  drawHandle(g, x + w,  y); 
	  
	  drawHandle(g, x,      y + hh);
	  drawHandle(g, x + w,  y + hh); 
	  
	  drawHandle(g, x,      y + h);
	  drawHandle(g, x + wh, y + h);
	  drawHandle(g, x + w,  y + h);
	}
      }
    } else
      selectionBoundingBox = null;
    if(selection.width != 0 && selection.height != 0)
      g2.draw(selection);
    if(showPorts != null) {
      GraphPort[]     ports = showPorts.getPorts();
      Component       cmp   = (Component)showPorts.get(COMPONENT);
      g.setColor(Color.red);
      for(int i = 0; i < ports.length; i++) {
	GraphNodePort port = (GraphNodePort)ports[i].get(GRAPH_NODE_PORT);
	if(port != null)
	  port.paint(cmp, g, ports[i] == preferredPort);
      }
    }
    
    
    if((oldSelectionBoundingBox == null && selectionBoundingBox != null) ||
       (oldSelectionBoundingBox != null && !oldSelectionBoundingBox.equals(selectionBoundingBox)))
      firePropertyChange(SELECTION_BOUNDS_CHANGED, oldSelectionBoundingBox, selectionBoundingBox);
  }
  
  private void paintEdges(Graphics g) {
    Graphics2D g2        = (Graphics2D)g;
    Stroke      svStroke = g2.getStroke();
    GraphEdge[] edges    = getEdges();
    router.setupEdges(this, edges, getNodes());
    for(int i = 0; i < edges.length; i++) {
      GraphEdge edge    = edges[i];
      Color     svColor = edge.color;
      edge.color = selectionModel.isSelected(edgeToModelMap.get(edges[i])) ? selectionColor : edges[i].color;      
      edge.paint(g);
      edge.color = svColor;
    }
    ((Graphics2D)g).setStroke(svStroke);
    for(int i = 0; i < edges.length; i++)
      edges[i].paintMarkers(g);
  }
  
  private transient Component targetLastEntered;
  private void trackMouseEnterExit(Component targetOver, MouseEvent e) {
    if(targetLastEntered == targetOver) return;
    
    if(targetLastEntered == null)
      retargetMouseEvent(targetOver, MouseEvent.MOUSE_ENTERED, e.getModifiers(), e);
    
    if(targetLastEntered != null) {
      retargetMouseEvent(targetLastEntered, MouseEvent.MOUSE_EXITED, e.getModifiers(), e);
      retargetMouseEvent(targetOver, MouseEvent.MOUSE_ENTERED, e.getModifiers(), e);
    }
    
    targetLastEntered = targetOver;
  }
  
  static void retargetMouseEvent(Component target, int id, int modifiers, MouseEvent e) {
    if(target == null) return;
    target.dispatchEvent(new MouseEvent(target,
					id, 
					e.getWhen(), 
					modifiers,
					e.getX() - target.getX(), 
					e.getY() - target.getY(), 
					e.getClickCount(), 
					e.isPopupTrigger()));
  }
  
  void handleMouseMoved(MouseEvent e) {
    int         x          = e.getX();
    int         y          = e.getY();
    Component[] c          = getComponents();
    Component   targetOver = null;
    for(int i = 0; i < c.length; i++) {
      if(c[i] == editor) continue;
      if(c[i].getBounds(tmpRect).contains(x, y)) {
      	targetOver = c[i];
        break;
      }
    }
    trackMouseEnterExit(targetOver, e);
    retargetMouseEvent(targetOver, e.getID(), e.getModifiers(), e);
  }
  
  public Rectangle getSelectionBounds() {
    return selectionBoundingBox;
  }

  int pointToSelectionArea(int px, int py) {
    if(selectionBoundingBox == null) return -1;
    int x  = selectionBoundingBox.x;
    int y  = selectionBoundingBox.y;
    int w  = selectionBoundingBox.width;
    int wh = w / 2;
    int h  = selectionBoundingBox.height;
    int hh = h / 2;
    
    if(px >= x - SELECTION_MARK_CENTER && px <= x + SELECTION_MARK_CENTER) {
      if(py >= y - SELECTION_MARK_CENTER && py <= y + SELECTION_MARK_CENTER)
	return SwingConstants.NORTH_WEST;
      if(py >= y - SELECTION_MARK_CENTER + hh && py <= y + SELECTION_MARK_CENTER + hh)
	return SwingConstants.WEST;
      if(py >= y - SELECTION_MARK_CENTER + h && py <= y + SELECTION_MARK_CENTER + h)
	return SwingConstants.SOUTH_WEST;
    }
    
    if(px >= x - SELECTION_MARK_CENTER + wh && px <= x + SELECTION_MARK_CENTER + wh) {
      if(py >= y - SELECTION_MARK_CENTER && py <= y + SELECTION_MARK_CENTER)
	return SwingConstants.NORTH;
      if(py >= y - SELECTION_MARK_CENTER + h && py <= y + SELECTION_MARK_CENTER + h)
	return SwingConstants.SOUTH;
    }
    
    if(px >= x - SELECTION_MARK_CENTER + w && px <= x + SELECTION_MARK_CENTER + w) {
      if(py >= y - SELECTION_MARK_CENTER && py <= y + SELECTION_MARK_CENTER)
	return SwingConstants.NORTH_EAST;
      if(py >= y - SELECTION_MARK_CENTER + hh && py <= y + SELECTION_MARK_CENTER + hh)
	return SwingConstants.EAST;
      if(py >= y - SELECTION_MARK_CENTER + h && py <= y + SELECTION_MARK_CENTER + h)
	return SwingConstants.SOUTH_EAST;
    }
    
    return selectionBoundingBox.contains(px, py) ? SwingConstants.CENTER : -1;
  }
  
  private void drawHandle(Graphics g, int x, int y) {
    Color svColor = g.getColor();
    g.setColor(Color.white);
    g.fillRect(x - SELECTION_MARK_CENTER, 
	       y - SELECTION_MARK_CENTER, 
	       SELECTION_MARK_SIZE, SELECTION_MARK_SIZE);
    g.setColor(svColor);
    g.drawRect(x - SELECTION_MARK_CENTER, 
	       y - SELECTION_MARK_CENTER, 
	       SELECTION_MARK_SIZE, SELECTION_MARK_SIZE);
  }

  //--- empty dnd implementation ---
  
  public void dragEnter(DropTargetDragEvent e)         {}
  public void dragExit(DropTargetEvent e)              {}
  public void dragOver(DropTargetDragEvent e)          {}
  public void dropActionChanged(DropTargetDragEvent e) {}
  public void drop(DropTargetDropEvent e)              {}

  //----------------------- test stuff -------------

  static int nodecnt = 0;
  static int MAXNODES = 3;
  static int MAXEDGES = 1;
  static int NAME;
  static GraphNode[] gnodes;
  
  static String[]       NLNAMES     = {"Tree",
				       "Force Directed",
				       "Distance Ordered",
				       "Free Move"};
  
  static String[]       ROUTERNAMES = {"Straight Line", "Orthogonal"};
  
  static String[]       GNAMES      = {"Random",
				       "Circle",
				       "FS Tree",
				       "Example 1",
				       "Example 2",
				       "Example 3",
				       "Example 4"};
  
  static String[]       GS          = {"",
				       "",
				       "",
				       "joe-food,joe-dog,joe-tea,joe-cat,joe-table,table-plate/50,plate-food/30,food-mouse/100,food-dog/100,mouse-cat/150,table-cup/30,cup-tea/30,dog-cat/80,cup-spoon/50,plate-fork,dog-flea1,dog-flea2,flea1-flea2/20,plate-knife",
				       "zero-one,one-two,two-three,three-four,four-five,five-six,six-seven,seven-zero",
				       "zero-one,zero-two,zero-three,zero-four,zero-five,zero-six,zero-seven,zero-eight,zero-nine,one-ten,two-twenty,three-thirty,four-fourty,five-fifty,six-sixty,seven-seventy,eight-eighty,nine-ninety,ten-twenty/80,twenty-thirty/80,thirty-fourty/80,fourty-fifty/80,fifty-sixty/80,sixty-seventy/80,seventy-eighty/80,eighty-ninety/80,ninety-ten/80,one-two/30,two-three/30,three-four/30,four-five/30,five-six/30,six-seven/30,seven-eight/30,eight-nine/30,nine-one/30",
				       "a1-a2,a2-a3,a3-a4,a4-a5,a5-a6,b1-b2,b2-b3,b3-b4,b4-b5,b5-b6,c1-c2,c2-c3,c3-c4,c4-c5,c5-c6,x-a1,x-b1,x-c1,x-a6,x-b6,x-c6"};
  
  private static ArrayList views = new ArrayList();
  
  private static GraphNode findNode(GraphModel g, String lbl) {
    Object nodes[] = g.nodes().toArray();
    
    for (int i = 0 ; i < nodes.length; i++)
      if(((DefaultGraphNode)nodes[i]).get(NAME).equals(lbl))
	return (DefaultGraphNode)nodes[i];
    
    DefaultGraphNode result = new DefaultGraphNode();
    result.set(NAME, lbl);
    g.add(result);
    return result;
  }
  
  private static GraphNode buildTree(GraphModel g, File f) {
    DefaultGraphNode node = new DefaultGraphNode();
    node.set(NAME, f.getName());
    g.add(node);
    if(f.isDirectory()) {
      File[] fs = f.listFiles();
      for(int i = 0; i < fs.length; i++) {
	GraphNode n = buildTree(g, fs[i]);
	iiuf.util.graph.DefaultGraphEdge e = new iiuf.util.graph.DefaultGraphEdge(node, n);
	e.setWeight(100);
	g.add(e);
      }
    }
    return node;
  }
  
  private static GraphNode startnode;

  private static GraphModel filetree(File root) {
    GraphModel result = new DefaultGraphModel();
    
    NAME = result.nodeAttribute("name", new AttributeFactory() {
	public Object newAttribute(Attributable attributable, Object[] args) {
	  return "";
	}
      });
    
    startnode = buildTree(result, root);
    
    GraphView[] va = getViews();
    
    for(int j = 0; j < va.length; j++)
      for(int i = 0; i < va[j].NLS.length; i++)
	if(va[j].NLS[i] instanceof TreeNL)
	  ((TreeNL)va[j].NLS[i]).setStart(startnode);
    
    return result;
  }

  private static GraphModel parse(String edges) {
    DefaultGraphModel result = new DefaultGraphModel();
    
    NAME = result.nodeAttribute("name", new AttributeFactory() {
	public Object newAttribute(Attributable attributable, Object[] args) {
	  return new String("" + nodecnt++);
	}
      });
    
    for(StringTokenizer t = new StringTokenizer(edges, ",") ; t.hasMoreTokens() ; ) {
      String str = t.nextToken();
      int i = str.indexOf('-');
      if (i > 0) {
	int len = 50;
	int j = str.indexOf('/');
	if (j > 0) {
	  len = Integer.valueOf(str.substring(j+1)).intValue();
	  str = str.substring(0, j);
	}
	
	iiuf.util.graph.DefaultGraphEdge edge = 
	  new iiuf.util.graph.DefaultGraphEdge(findNode(result, str.substring(0,i)), findNode(result, str.substring(i+1)));
	edge.setWeight(len);
	result.add(edge);
      }
    }
    
    return result;
  }

  private static GraphModel circle() {
    gnodes = new DefaultGraphNode[MAXNODES];
    
    DefaultGraphModel g = new DefaultGraphModel();
    
    NAME = g.nodeAttribute("name", new AttributeFactory() {
	public Object newAttribute(Attributable attributable, Object[] args) {
	  return new String("" + nodecnt++);
	}
      });
    
    for(int i = 0; i < MAXNODES; i++) {
      gnodes[i] = new DefaultGraphNode();
      g.add(gnodes[i]);
    }
    
    iiuf.util.graph.DefaultGraphEdge edge;
    
    for(int i = 0; i < MAXNODES - 1; i++) {
      edge = new iiuf.util.graph.DefaultGraphEdge(gnodes[i], gnodes[i + 1]);
      edge.setWeight(50);
      g.add(edge);
    }
    edge = new iiuf.util.graph.DefaultGraphEdge(gnodes[MAXNODES - 1], gnodes[0]);    
    edge.setWeight(50);
    g.add(edge);
    
    return g;
  }
  
  private static GraphModel randomize() {
    gnodes = new DefaultGraphNode[MAXNODES];

    DefaultGraphModel g = new DefaultGraphModel();

    NAME = g.nodeAttribute("name", new AttributeFactory() {
	public Object newAttribute(Attributable attributable, Object[] args) {
	  return new String("" + nodecnt++);
	}
      });
    
    for(int i = 0; i < MAXNODES; i++) {
      gnodes[i] = new DefaultGraphNode();
      g.add(gnodes[i]);
    }
    
    for(int i = 0; i < MAXEDGES; i++) {
      iiuf.util.graph.DefaultGraphEdge edge = 
	new iiuf.util.graph.DefaultGraphEdge(gnodes[Util.intRandom(MAXNODES)], gnodes[Util.intRandom(MAXNODES)]);
      edge.setWeight(50);
      g.add(edge);
    }
   
    return g;
  }
  
  static class GraphView
    extends 
    JFrame
  {
    JTextField  nodestf = new JTextField("" + MAXNODES);
    JTextField  edgestf = new JTextField("" + MAXEDGES);
    
    NodeLayouter[] NLS         = {new TreeNL(),
				  new ForceDirectedNL(),
				  new DistanceOrderedNL(),
				  new DefaultNL()};

    GraphRouter[]  ROUTERS     = {new StraightLineRouter(), new OrthogonalRouter()};
    
    EdgeMarker[][]   CAPTIONS = {{new EquilateralTriangleMarker(10, Math.PI / 6.0, true,  false)},
				 {new EquilateralTriangleMarker(10, Math.PI / 6.0, false, false)},
				 {new EquilateralTriangleMarker(10, Math.PI / 6.0, false, false, Color.blue, Color.green)},
				 {new EquilateralTriangleMarker(10, Math.PI / 6.0, false, false),
				  new EquilateralTriangleMarker(10, Math.PI / 6.0, false, true)},
				 {new EquilateralTriangleMarker(12, Math.PI / 4.0, false, false, Color.black, Color.white)},
				 {new LabelMarker("Hello World")},
				 {new LabelMarker("Hello World", HexagonalBorder.newBlackBorder())}};
    
    double[][]      CAPTIONSPOS = {{1.0},
				   {1.0},
				   {1.0},
				   {1.0, 0.0},
				   {0.5},
				   {0.5},
				   {0.5}};
    
    GraphPanel gp;
    
    GraphView(GraphModel model) {
      super("GraphView");
      
      gp = 
	new GraphPanel(
		       new AbstractNodeComponentFactory(gp) {
			   protected Component newNodeComponent(GraphNode node, Object[] args) {
			     return new JButton((String)node.get(NAME));
			   }
			 },
		       new AbstractPortFactory() {
			   protected GraphNodePort newPort(GraphPort port, Object[] args) {
			     return new GraphNodePort(0.5, 0.5, 0);
			   }
			 },
		       new AbstractGraphEdgeFactory() {
			   protected GraphEdge newGraphEdge(iiuf.util.graph.GraphEdge edge, 
							    GraphNode fromNode, GraphPort fromPort, 
							    GraphNode toNode,   GraphPort toPort,
							    Object[] args) {
			     return new GraphEdge((Component)fromNode.get(gp.COMPONENT), 
						  (GraphNodePort)fromPort.get(gp.GRAPH_NODE_PORT),
						  (Component)toNode.get(gp.COMPONENT),  
						  (GraphNodePort)toPort.get(gp.GRAPH_NODE_PORT));
			   }
			 },
		       NLS[0],
		       ROUTERS[0]);
      
      gp.setModel(model);
      gp.addContextMenu();
      
      JPanel params   = new JPanel();
      JPanel controls = new JPanel();
      JFrame frame = new JFrame("Graph Test");
      frame.setSize(800, 500);
      frame.setLocation(Awt.centerOnScreen(frame.getSize()));
      frame.getContentPane().setLayout(new BorderLayout());
      frame.getContentPane().add(new JScrollPane(gp), BorderLayout.CENTER);
      
      params.setLayout(new GridBagLayout());
      params.add(new JLabel("Nodes:"), Awt.constraints(false));
      nodestf = new JTextField("" + MAXNODES, 5);
      params.add(nodestf, Awt.constraints(false));    
      
      JComboBox nodenl = new JComboBox(NLNAMES);
      nodenl.addItemListener(new ItemListener() {
	  public void itemStateChanged(ItemEvent e) {
	    for(int i = 0; i < NLNAMES.length; i++)
	      if(NLNAMES[i].equals(e.getItem())) {
		if(startnode != null && NLS[i] instanceof TreeNL)
		  ((TreeNL)NLS[i]).setStart(startnode);
		gp.setLayouter(NLS[i]);
		break;
	      }
	  }
	});
      params.add(nodenl, Awt.constraints(true));
      
      params.add(new JLabel("Edges:"), Awt.constraints(false));
      edgestf = new JTextField("" + MAXEDGES, 5);
      params.add(edgestf, Awt.constraints(false));
      
      JComboBox edgert = new JComboBox(ROUTERNAMES);
      edgert.addItemListener(new ItemListener() {
	  public void itemStateChanged(ItemEvent e) {
	    if(e.getStateChange() != e.SELECTED) return;
	    for(int i = 0; i < ROUTERNAMES.length; i++)
	      if(ROUTERNAMES[i].equals(e.getItem())) {
		gp.setRouter(ROUTERS[i]);
		break;
	      }
	  }
	});
      params.add(edgert, Awt.constraints(true));
      
      params.add(new GeometryEditor(gp));
      params.add(new StyleEditor(gp));
      
      EdgeEditor ee = new EdgeEditor(gp);
      for(int i = 0; i < CAPTIONS.length; i++)
	ee.addMarkers(CAPTIONS[i], CAPTIONSPOS[i]);
      params.add(ee);

      controls.add(Swing.newCheckBox("Edit", 
				     new ItemListener() {
					 public void itemStateChanged(ItemEvent e) {
					   gp.setEditable(((JCheckBox)e.getItemSelectable()).isSelected());
					 }
				       }));
      
      controls.add(Swing.newCheckBox("Edge Over", 
				     new ItemListener() {
					 public void itemStateChanged(ItemEvent e) {
					   gp.setPaintEdgesAfterNodes(((JCheckBox)e.getItemSelectable()).isSelected());
					 }
				       }));
      
      
      JComboBox gshape = new JComboBox(GNAMES);
      gshape.addItemListener(new ItemListener() {
	  public void itemStateChanged(ItemEvent e) {
	    if(e.getStateChange() != e.SELECTED) return;
	    if(GNAMES[0].equals(e.getItem())) {
	      MAXNODES = Integer.parseInt(nodestf.getText());
	      MAXEDGES = Integer.parseInt(edgestf.getText());
	      setModel(randomize());
	    } else if(GNAMES[1].equals(e.getItem())) {
	      MAXNODES = Integer.parseInt(nodestf.getText());
	      setModel(circle());
	    } else if(GNAMES[2].equals(e.getItem())) {
	      fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
	      if(fc.showOpenDialog((Component)e.getItemSelectable()) == fc.APPROVE_OPTION)
		setModel(filetree(fc.getSelectedFile()));
	    } else { 
	      for(int i = 2; i < GNAMES.length; i++)
		if(GNAMES[i].equals(e.getItem())) {
		  setModel(parse(GS[i]));
		  break;
		}
	    }
	  }
	});
      controls.add(gshape);    
      
      controls.add(Swing.newButton("New View",
				   new ActionListener() {
				       public void actionPerformed(ActionEvent e) {
					 views.add(new GraphView(gp.getModel()));
				       }
				     }));
      
      controls.add(Swing.newButton("Quit",
				   new ActionListener() {
				       public void actionPerformed(ActionEvent e) {
					 System.exit(0);
				       }
				     }));
      
      
      frame.getContentPane().add(params, BorderLayout.NORTH);    
      frame.getContentPane().add(controls, BorderLayout.SOUTH);    
      frame.setVisible(true);
    }
    
    private void setModel(GraphModel model) {
      GraphView[] va = getViews();
      for(int i = 0; i < va.length; i++)
	va[i].gp.setModel(model);
    }
  }
   
  private static GraphView[] getViews() {
    return (GraphView[])views.toArray(new GraphView[views.size()]);
  }

  static JFileChooser fc = new JFileChooser();
  public static void main(String[] argv) {
    views.add(new GraphView(randomize()));    
  }
}

class GraphLayout 
  implements
  LayoutManager2
{
  private Dimension    dimension = new Dimension();
  private NodeLayouter layouter;
  
  GraphLayout(NodeLayouter layouter) {
    setLayouter(layouter);
  }
  
  void setLayouter(NodeLayouter layouter_) {
    if(layouter != null) layouter.deactivate();
    layouter = layouter_;
    layouter.activate();
  }
  
  NodeLayouter getLayouter() {
    return layouter;
  }
  
  public void addLayoutComponent(Component comp, Object constraints) {
    // System.out.println("addLayoutComponent");
  }
  
  public float getLayoutAlignmentX(Container target) {
    // System.out.println("getLayoutAlignmentX");
    return 0;
  }
  
  public float getLayoutAlignmentY(Container target) {
    // System.out.println("getLayoutAlignmentY");
    return 0;
  }
  
  public void invalidateLayout(Container target) {
    // System.out.println("invalidateLayout");
  }
  
  public Dimension maximumLayoutSize(Container target) {
    return dimension;
  }
  
  public void addLayoutComponent(String name, Component comp) {
    layoutContainer(comp.getParent());
  }
  
  public void layoutContainer(Container parent_) {
    GraphPanel parent = (GraphPanel)parent_;
    if(parent.layoutBlock != 0) return;
    Dimension oldDim  = parent.getSize();
    dimension = layouter.layout(parent, parent.getModel());
    dimension = new Dimension(Math.max(dimension.width, parent.getWidth()), Math.max(dimension.height, parent.getHeight()));      
    if(parent.edit)
      parent.editor.setSize(dimension);
    if(oldDim.width  != dimension.width || oldDim.height != dimension.height) 
      parent.revalidate();
  }
  
  public Dimension minimumLayoutSize(Container parent) {
    return dimension;
  }

  public Dimension preferredLayoutSize(Container parent) {
    return dimension;
  }
  
  public void removeLayoutComponent(Component comp) {
    layoutContainer(comp.getParent());
  }
}

class SelectionModel 
  implements
  SetSelectionModel 
{
  private HashSet           selection  = new HashSet();
  private EventListenerList listeners  = new EventListenerList();
  private ChangeEvent       EVENT;
  
  SelectionModel() {
    EVENT = new ChangeEvent(this);
  }
  
  public void add(Object node) {
    selection.add(node);
    fireChangeEvent();
  }

  public void addAll(Collection nodes) {
    selection.addAll(nodes);
    fireChangeEvent();
  }

  public void remove(Object node) {
    selection.remove(node);
    fireChangeEvent();
  }

  public void removeAll(Collection nodes) {
    selection.removeAll(nodes);
    fireChangeEvent();
  }

  public void clearSelection() {
    selection = new HashSet();
    fireChangeEvent();
  }

  public boolean isEmpty() {
    return selection.isEmpty();
  }
  
  public Object[] getSelection() {
    return selection.toArray();
  }
  
  public Object[] getSelection(Class cls) {
    HashSet  subsel = new HashSet();
    Object[] sel    = selection.toArray();
    for(int i = 0; i < sel.length; i++)
    if(cls.isAssignableFrom(sel[i].getClass()))
    subsel.add(sel[i]);
    
    Object[] result = (Object[])Array.newInstance(cls, subsel.size());
    int j = 0;
    for(Iterator i = subsel.iterator(); i.hasNext(); j++)
    result[j] = i.next();
    return result;
  }
  
  public boolean isSelected(Object o) {
    return selection.contains(o);
  }
  
  private synchronized void fireChangeEvent() {
    ChangeListener[] l = (ChangeListener[])listeners.getListeners(ChangeListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].stateChanged(EVENT);
  }
  
  public synchronized void addChangeListener(ChangeListener listener) {
    listeners.add(ChangeListener.class, listener);
  }

  public synchronized void addChangeListener(ChangeListener listener, boolean weak) {
    listeners.add(ChangeListener.class, listener, weak);
  }
  
  public synchronized void removeChangeListener(ChangeListener listener) {
    listeners.remove(ChangeListener.class, listener);
  }
  
  public int size() {
    return selection.size();
  }
  
  public int size(Class cls) {
    return getSelection(cls).length;
  }
}

/*
  $Log: GraphPanel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.15  2001/04/30 07:33:17  schubige
  added webcom to cvstree

  Revision 1.14  2001/03/21 19:34:06  schubige
  started with dom stuff

  Revision 1.13  2001/03/20 14:28:30  schubige
  enhanced sample soundlet, added format popup

  Revision 1.12  2001/03/19 16:13:26  schubige
  soundium without drag cursor

  Revision 1.11  2001/03/16 18:08:20  schubige
  improved orthogonal router

  Revision 1.10  2001/03/13 13:41:05  schubige
  Fixed some graph panel and soundium bugs

  Revision 1.9  2001/03/12 17:52:00  schubige
  Added version support to sourcewatch and enhanced soundium

  Revision 1.8  2001/03/11 17:59:38  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.7  2001/03/09 21:24:58  schubige
  Added preferences to edge editor

  Revision 1.6  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.5  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.4  2001/03/05 17:55:07  schubige
  Still working on soundium properties panel

  Revision 1.3  2001/02/26 15:57:22  schubige
  Again changes in SoundEngine.x, added some todos to graph panel & co

  Revision 1.2  2001/02/23 17:23:11  schubige
  Added loop source to soundium and fxed some bugs along

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.18  2001/02/16 13:47:38  schubige
  Adapted soundium to new rtsp version

  Revision 1.17  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.16  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.15  2001/02/13 14:49:06  schubige
  started work on gui - engine connection

  Revision 1.14  2001/02/12 17:50:05  schubige
  still working on soundium gui

  Revision 1.13  2001/02/11 16:25:39  schubige
  working on soundium

  Revision 1.12  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.11  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.10  2001/01/04 12:12:36  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.9  2001/01/04 09:58:49  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.8  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.7  2000/12/29 08:03:55  schubige
  SourceWatch beta debug iter 1

  Revision 1.6  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.5  2000/12/20 09:46:39  schubige
  TJGUI update

  Revision 1.4  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.3  2000/11/10 10:46:53  schubige
  iiuf tree cleanup iter 3

  Revision 1.2  2000/10/09 06:47:57  schubige
  Updated logger stuff

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:56:20  schubige
  Added graph view stuff
  
*/
