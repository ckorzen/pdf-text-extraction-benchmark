package iiuf.swing.graph;

import java.io.File;
import java.util.HashMap;
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DnDConstants;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;

import iiuf.awt.Awt;
import iiuf.swing.Swing;
import iiuf.swing.SetSelectionModel;
import iiuf.swing.ContextMenuEnabled;
import iiuf.swing.ContextMenuManager;
import iiuf.swing.ContextMenu;
import iiuf.swing.Resource;
import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.Util;
import iiuf.util.Unicode;
import iiuf.util.graph.GraphNode;
import iiuf.util.graph.GraphPort;
import iiuf.util.graph.GraphModel;
import iiuf.util.graph.DefaultGraphNode;
import iiuf.util.graph.DefaultGraphEdge;
import iiuf.util.graph.GraphModelListener;

/**
   Graph editor implementation.<p>

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class GraphPanelEditor 
  extends
  JComponent 
  implements
  MouseListener,
  MouseMotionListener,
  ContextMenuEnabled,
  DropTargetListener,
  SwingConstants
{
  private final static int  MIN_RESIZE = 16;
  
  private GraphPanel        gp;
  private int               dx;
  private int               dy;
  private int               ox;
  private int               oy;
  private Rectangle[]       resizeSnap;
  private Component[]       resizeSnapCmp;
  private Rectangle         resizeSnapBounds;
  private Rectangle         tmpRect       = new Rectangle();
  private int               dragArea;
  private Cursor            defaultCursor;
  private Cursor            dragCursor    = new Cursor(Cursor.MOVE_CURSOR);
  private Cursor            NECursor      = new Cursor(Cursor.NE_RESIZE_CURSOR);
  private Cursor            ECursor       = new Cursor(Cursor.E_RESIZE_CURSOR);
  private Cursor            SECursor      = new Cursor(Cursor.SE_RESIZE_CURSOR);
  private Cursor            NCursor       = new Cursor(Cursor.N_RESIZE_CURSOR);
  private Cursor            SCursor       = new Cursor(Cursor.S_RESIZE_CURSOR);
  private Cursor            NWCursor      = new Cursor(Cursor.NW_RESIZE_CURSOR);
  private Cursor            WCursor       = new Cursor(Cursor.W_RESIZE_CURSOR);
  private Cursor            SWCursor      = new Cursor(Cursor.SW_RESIZE_CURSOR);
  private Cursor            connectCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
  private Cursor            stopCursor    = Awt.STOP_CURSOR;
  private MouseEvent        lastMouseEvent;
  GraphNode                 dragable;
  iiuf.util.graph.GraphEdge connectingEdge;
  ConnectingNode            connectingNode = new ConnectingNode();
  GraphPort                 fromPort;
  JMenu                     alignMenu      = new JMenu("Align");
  JMenu                     distributeMenu = new JMenu("Distribute");
  JMenu                     rotateMenu     = new JMenu("Rotate");
  JMenu                     arrangeMenu    = new JMenu("Arrange");
  Component                 invisible      = Awt.newComponent();
  
  private static final int MOVING     = 0;
  private static final int CONNECTING = 1;
  private static final int DRAGGING   = 2;
  private static final int SELECTING  = 3;

  private int              state = MOVING;
  
  private Action[] editActions = {
    new AbstractAction("Delete") {
	boolean init = init();
	public void actionPerformed(ActionEvent e) {delete();}
	
	boolean init() {
	  putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
	  return true;
	}
      },
    new AbstractAction("Select All") {
	boolean init = init();
	public void actionPerformed(ActionEvent e) {selectAll();}

	boolean init() {
	  putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl A"));
	  return true;
	}
      },
  };
  
  private Action[] propertiesActions = {
    new AbstractAction("Properties") {
	public void actionPerformed(ActionEvent e) {properties(gp.viewToModel((Component)ctxmgr.getLastObject()));}
      },    
  };
  
  private JMenuItem[] ctxMenuEditItems       = {Swing.newMenuItem(editActions[0], 
								  (KeyStroke)editActions[0].getValue(Action.ACCELERATOR_KEY))};
  private JMenuItem[] ctxMenuPropertiesItems = {new JMenuItem(propertiesActions[0])};
  private JMenuItem[] ctxMenuAlignItems      = {alignMenu, distributeMenu};
  private JMenuItem[] ctxMenuRotateItems     = {rotateMenu};
  private JMenuItem[] ctxMenuArrangeItems    = {arrangeMenu};
  
  GraphPanelEditor(GraphPanel gp_) {
    addMouseListener(this);
    addMouseMotionListener(this);
    gp = gp_;
    
    for(int i = 0; i < alignActions.length; i++)
      alignMenu.add(new JMenuItem(alignActions[i]));
    
    for(int i = 0; i < distributeActions.length; i++)
      distributeMenu.add(new JMenuItem(distributeActions[i]));
    
    for(int i = 0; i < rotateActions.length; i++)
      rotateMenu.add(new JMenuItem(rotateActions[i]));
    
    for(int i = 0; i < arrangeActions.length; i++)
      arrangeMenu.add(new JMenuItem(arrangeActions[i]));
    
    addActions(editActions);
    addActions(propertiesActions);
    addActions(alignActions);
    addActions(distributeActions);
    addActions(rotateActions);
    addActions(arrangeActions);
    
    addKeyListener(new KeyAdapter() {
	public void keyPressed(KeyEvent e) {
	  if(e.getKeyCode() == e.VK_SHIFT && lastMouseEvent != null) {
	    gp.retargetMouseEvent((Component)lastMouseEvent.getSource(), 
				  lastMouseEvent.getID(), 
				  e.getModifiers(),
				  lastMouseEvent);
	  }
	}
	public void keyReleased(KeyEvent e) {
	  if(e.getKeyCode() == e.VK_SHIFT && lastMouseEvent != null) {
	    gp.retargetMouseEvent((Component)lastMouseEvent.getSource(), 
				  lastMouseEvent.getID(), 
				  e.getModifiers() & ~e.SHIFT_MASK,
				  lastMouseEvent);
	  }
	}
      });
    
    InputMap imap = getInputMap();
    imap.put(KeyStroke.getKeyStroke("BACK_SPACE"), editActions[0].getValue(Action.NAME));
    for(int i = 0; i < editActions.length; i++)
      imap.put((KeyStroke)editActions[i].getValue(Action.ACCELERATOR_KEY), editActions[i].getValue(Action.NAME));
  }
  
  public void start() {
    defaultCursor = getCursor();
  }
  
  public void stop() {
    gp.setDot(dx, dy);
    gp.setMark(dx, dy);
    gp.setShowPorts(null, null);
    setCursor(defaultCursor);
  }
  
  public Component getComponent() {return this;}
  public Object locationToObject(Component component, Point location) {
    Object result = gp.locationToObject(gp, location);
    return result;
  }

  private ContextMenuManager ctxmgr;
  public void setContextMenuManager(ContextMenuManager manager) {
    gp.setContextMenuManager(manager);
    if(manager != ctxmgr) {            
      manager.addContextMenu(new ContextMenu() {
	  public boolean check(Object o) {
	    return  gp.getSelectionModel().size(GraphNode.class) > 0;
	  }
	  
	  public JMenuItem[] getItems() {
	    return ctxMenuArrangeItems;
	  }
	}, true);
      
      manager.addContextMenu(new ContextMenu() {
	  public boolean check(Object o) {
	    return  gp.getLayouter().allowsNodeLocationChange() && gp.getSelectionModel().size(GraphNode.class) > 1;
	  }
	  
	  public JMenuItem[] getItems() {
	    return ctxMenuAlignItems;
	  }
	}, true);
      
      manager.addContextMenu(new ContextMenu() {
	  public boolean check(Object o) {
	    return 
	      gp.getLayouter().allowsNodeLocationChange() && 
	      gp.getSelectionModel().size(GraphNode.class) <= 1 && 
	      o instanceof GraphNodeComponent &&
	      GraphNodeComponent.ANGLE_90 % ((GraphNodeComponent)o).getMinimalRotation() == 0;
	  }
	   
	  public JMenuItem[] getItems() {
	    return ctxMenuRotateItems;
	  }
	}, true);
      
      manager.addContextMenu(new ContextMenu() {
	  public boolean check(Object o) {
	    return o instanceof Component && 
	      gp.viewToModel((Component)o) != null && 
	      gp.viewToModel((Component)o).get(gp.NODE_PROPERTIES) != null;
	  }
	  
	  public JMenuItem[] getItems() {
	    return ctxMenuPropertiesItems;
	  }
	}, true);

      manager.addContextMenu(new ContextMenu() {
	  public boolean check(Object o) {
	    return !gp.getSelectionModel().isEmpty();
	  }
	  
	  public JMenuItem[] getItems() {
	    return ctxMenuEditItems;
	  }
	}, true);

      ctxmgr = manager;
    }
  }

  private Cursor lastCursor;
  public void setCursor(Cursor crsr) {
    if(crsr != lastCursor)
      super.setCursor(crsr);
    lastCursor = crsr;
  }

  private void setResizeCursor() {
    switch(dragArea) {
    case NORTH_EAST: setCursor(NECursor);   break;
    case EAST:       setCursor(ECursor);    break;
    case SOUTH_EAST: setCursor(SECursor);   break;
    case NORTH:      setCursor(NCursor);    break;
    case SOUTH:      setCursor(SCursor);    break;
    case NORTH_WEST: setCursor(NWCursor);   break;
    case WEST:       setCursor(WCursor);    break;
    case SOUTH_WEST: setCursor(SWCursor);   break;
    }
  }

  public void mouseClicked(MouseEvent e)  {
    lastMouseEvent = null;
    dx = e.getX();
    dy = e.getY();
    switch(state) {
    case MOVING: {
      int modif = e.getModifiers();
      if((modif & e.BUTTON1_MASK) != 0) {
	if(!e.isShiftDown())
	  gp.getSelectionModel().clearSelection();		  
	Object o = gp.viewToModel(e.getPoint());
	if(o instanceof GraphNode ||
	   o instanceof iiuf.util.graph.GraphEdge)
	  if(gp.getSelectionModel().isSelected(o))
	    gp.getSelectionModel().remove(o);
	  else
	    gp.getSelectionModel().add(o);
      }
      gp.setShowPorts(null, null);
      break;
    }
    case CONNECTING: {
      endConnect();
      state = MOVING;
      break;
    }  
    case DRAGGING:
      setCursor(defaultCursor);
      state = MOVING;
      break;
    case SELECTING:
      select(dx, dy);
      if(!e.isShiftDown())
	gp.getSelectionModel().clearSelection();
      state = MOVING;
      break;
    }
  }
  
  public void mouseEntered(MouseEvent e)  {
    lastMouseEvent = null;
    requestFocus();
  }
  
  public void mouseExited(MouseEvent e)   {
    lastMouseEvent = null;
    gp.setShowPorts(null, null);
  }
  
  public void mousePressed(MouseEvent e)  {
    lastMouseEvent = null;
    dx = e.getX();
    dy = e.getY();
    switch(state) {
    case MOVING:
      break;
    case CONNECTING:
      endConnect();
      state = MOVING;
      break;
    case DRAGGING:  
      setCursor(defaultCursor);
      state = MOVING;
      break;
    case SELECTING:
      select(dx, dy);
      state = MOVING;
      break;
    }
  }

  public void mouseReleased(MouseEvent e) {
    lastMouseEvent = null;
    dx = e.getX();
    dy = e.getY();
    switch(state) {
    case MOVING:
      break;
    case CONNECTING:
      endConnect();
      state = MOVING;
      break;
    case DRAGGING:
      setCursor(defaultCursor);
      state = MOVING;
      break;
    case SELECTING:
      select(dx, dy);
      state = MOVING;
      break;
    }
  }
  
  public void mouseMoved(MouseEvent e)    {
    lastMouseEvent = e;
    requestFocus();
    dx = e.getX();
    dy = e.getY();
    Object o = gp.viewToModel(e.getPoint());
    switch(state) {
    case MOVING:
      if(gp.getSelectionModel().isEmpty() && o instanceof GraphNode && !(o instanceof ConnectingNode))
	gp.setShowPorts((GraphNode)o, gp.findPortAt((GraphNode)o, dx, dy));
      else
	gp.setShowPorts(null, null);
      setCursor(defaultCursor);
      break;
    case CONNECTING:
      endConnect();
      state = MOVING;
      break;
    case DRAGGING:
      setCursor(defaultCursor);
      state = MOVING;
      break;
    case SELECTING:
      select(dx, dy);
      state = MOVING;
      break;
    }
    gp.handleMouseMoved(e);
  }	

  public void mouseDragged(MouseEvent e)  {
    lastMouseEvent = e;
    int        x = e.getX();
    int        y = e.getY();
    switch(state) {
    case MOVING: 
      int modif = e.getModifiers();
      if((modif & e.BUTTON1_MASK) != 0) {
	dragArea   = gp.pointToSelectionArea(dx, dy);
	setResizeCursor();
	switch(dragArea) {
	case NORTH_EAST:
	case EAST:    
	case SOUTH_EAST: 
	case NORTH:      
	case SOUTH:     
	case NORTH_WEST:
	case WEST:     
	case SOUTH_WEST:
	  resizeSnap();
	  state = DRAGGING;
	  break;
	default:
	  Object o = gp.viewToModel(dx, dy);
	  if(gp.getSelectionModel().isEmpty() && o instanceof GraphNode) {
	    GraphNode node = (GraphNode)o;
	    fromPort = gp.findPortAt(node, dx, dy);
	    if(fromPort == null) return;
	    if(fromPort.isFull()) {
	      fromPort = null;
	      setCursor(stopCursor);
	      return;
	    }
	    gp.getModel().add(connectingNode);
	    connectingEdge = fromPort.createEdge(connectingNode.getDefaultPort());
	    gp.getModel().add(connectingEdge);
	    ((Component)connectingNode.get(gp.COMPONENT)).setLocation(e.getPoint());
	    setCursor(connectCursor);
	    state = CONNECTING;
	    return;
	  } else {
	    dragable = gp.findNodeAt(dx, dy);
	    if(dragable == null || !gp.getSelectionModel().isSelected(dragable)) {
	      gp.setDot(dx, dy);
	      gp.setMark(x, y);
	      if(!e.isShiftDown())
		gp.getSelectionModel().clearSelection();
	      state = SELECTING;
	      return;
	    }
	    Component dragableCmp = (Component)dragable.get(gp.COMPONENT);
	    if(gp.getLayouter().allowsNodeLocationChange())
	      setCursor(dragCursor);
	    ox = dragableCmp.getX() - dx;
	    oy = dragableCmp.getY() - dy;
	  }
	  state = DRAGGING;
	}
      }
      break;
    case CONNECTING: {
      ((Component)connectingNode.get(gp.COMPONENT)).setLocation(0, 0);
      Object o = gp.viewToModel(e.getPoint());
      if(o instanceof GraphNode) {
	GraphNode node = (GraphNode)o;
	
	GraphPort port = gp.findPortAt(node, x, y);
	setCursor(port.compatible(fromPort) && !port.isFull() ? connectCursor : stopCursor);
	
	((Component)connectingNode.get(gp.COMPONENT)).
	  setLocation(((GraphNodePort)port.get(gp.GRAPH_NODE_PORT)).
		      getLocation(((Component)node.get(gp.COMPONENT))));
	
	gp.setShowPorts(node, port);
      } else {
	((Component)connectingNode.get(gp.COMPONENT)).setLocation(e.getPoint());
	gp.setShowPorts(null, null);
      	setCursor(connectCursor);
      }
      gp.repaint();      
      break;
    }
    case DRAGGING: {
      if(gp.getLayouter().allowsNodeLocationChange()) {
	if(dragArea != CENTER && dragArea != -1) {
	  Rectangle dst   = (Rectangle)gp.getSelectionBounds().clone();
	  int       mdx   = x - dx;
	  int       mdy   = y - dy;
	  int       xlim  = resizeSnapBounds.x;
	  int       ylim  = resizeSnapBounds.y;
	  
	  switch(dragArea) {
	  case NORTH_EAST:
	    dst.width  = resizeSnapBounds.width  + mdx;
	    dst.height = resizeSnapBounds.height - mdy;
	    dst.y      = resizeSnapBounds.y      + mdy;
	    ylim       = resizeSnapBounds.y      + resizeSnapBounds.height - MIN_RESIZE;
	    break;
	  case EAST:  
	    dst.width  = resizeSnapBounds.width  + mdx;
	    break;	      
	  case SOUTH_EAST:
	    dst.width  = resizeSnapBounds.width  + mdx;
	    dst.height = resizeSnapBounds.height + mdy;
	    break;
	  case NORTH:
	    dst.height = resizeSnapBounds.height - mdy;
	    dst.y      = resizeSnapBounds.y      + mdy;
	    ylim       = resizeSnapBounds.y      + resizeSnapBounds.height - MIN_RESIZE;
	    break;
	  case SOUTH:
	    dst.height = resizeSnapBounds.height + mdy;
	    break;
	  case NORTH_WEST:
	    dst.width  = resizeSnapBounds.width  - mdx;
	    dst.x      = resizeSnapBounds.x      + mdx;
	    dst.height = resizeSnapBounds.height - mdy;
	    dst.y      = resizeSnapBounds.y      + mdy;
	    xlim       = resizeSnapBounds.x      + resizeSnapBounds.width  - MIN_RESIZE;
	    ylim       = resizeSnapBounds.y      + resizeSnapBounds.height - MIN_RESIZE;
	    break;
	  case WEST:
	    dst.width  = resizeSnapBounds.width  - mdx;
	    dst.x      = resizeSnapBounds.x      + mdx;
	    xlim       = resizeSnapBounds.x      + resizeSnapBounds.width - MIN_RESIZE;
	    break;
	  case SOUTH_WEST:
	    dst.width  = resizeSnapBounds.width  - mdx;
	    dst.x      = resizeSnapBounds.x      + mdx;
	    dst.height = resizeSnapBounds.height + mdy;
	    xlim       = resizeSnapBounds.x      + resizeSnapBounds.width - MIN_RESIZE;
	    break;
	  }
	  
	  if(dst.width <  MIN_RESIZE) {
	    dst.width = MIN_RESIZE;
	    dst.x     = xlim;
	  }
	  
	  if(dst.height < MIN_RESIZE) {
	    dst.height = MIN_RESIZE;
	    dst.y      = ylim;
	  }

	  if(e.isShiftDown()) 
	    switch(dragArea) {
	    case SOUTH:
	    case NORTH:
	      dst.width = (resizeSnapBounds.width * dst.height) / resizeSnapBounds.height;
	      dst.width &= 0xFFFFFFFE;
	      dst.x     = resizeSnapBounds.x + (resizeSnapBounds.width - dst.width) / 2;
	      break;
	    case EAST:  
	    case WEST:
	    case SOUTH_EAST:
	    case SOUTH_WEST:
	    case NORTH_WEST:
	    case NORTH_EAST:
	      dst.height = (resizeSnapBounds.height * dst.width) / resizeSnapBounds.width;
	      if(dragArea == EAST || dragArea == WEST) {
		dst.height &= 0xFFFFFFFE;
		dst.y      = resizeSnapBounds.y + (resizeSnapBounds.height - dst.height) / 2;	      
	      }
	      if(dragArea == NORTH_WEST || dragArea == NORTH_EAST)
		dst.y      = resizeSnapBounds.y + resizeSnapBounds.height - dst.height;	      		
	      break;
	    }
	  
	  resizeSelection(dst);
	} else if(dragable != null){
	  Component dragableCmp = (Component)dragable.get(gp.COMPONENT);
	  if(!gp.getSelectionModel().isEmpty() && gp.getSelectionModel().isSelected(dragable)) {
	    Object[] sel   = gp.getSelectionModel().getSelection();
	    int      dragX = dragableCmp.getX();
	    int      dragY = dragableCmp.getY();
	    
	    Rectangle r = null;
	    for(int i = 0; i < sel.length; i++) {
	      if(sel[i] instanceof GraphNode) {
		tmpRect = ((Component)((GraphNode)sel[i]).get(gp.COMPONENT)).getBounds(tmpRect);
		if(r == null)
		  r = (Rectangle)tmpRect.clone();
		r.add(tmpRect);
	      }
	    }
	    
	    Point loc = Awt.fitSmallerIntoBigger(x + ox + r.x - dragX, y + oy + r.y - dragY, r.width, r.height, getBounds(tmpRect));
	    for(int i = 0; i < sel.length; i++) {
	      if(!(sel[i] instanceof GraphNode)) continue;
	      Component c = (Component)((GraphNode)sel[i]).get(gp.COMPONENT);
	      c.setLocation(c.getX() - r.x + loc.x, c.getY() - r.y + loc.y);
	    }
	  }
	}
	gp.repaint();
      }
      break;
    }
    case SELECTING: 
      gp.setMark(x, y);
      break;
    }
  }
  
  public void resizeSelection(int x, int y, int w, int h) {
    resizeSnap();
    if(w < MIN_RESIZE)
      w = MIN_RESIZE;
    if(h < MIN_RESIZE)
      h = MIN_RESIZE;
    resizeSelection(new Rectangle(x, y, w, h));
    gp.repaint();
  }
  
  private void resizeSnap() {
    resizeSnapBounds = gp.getSelectionBounds();
    Object[] sel = gp.getSelectionModel().getSelection();
    int count = 0;
    for(int i = 0; i < sel.length; i++)
      if(sel[i] instanceof GraphNode)
	count++;
    resizeSnap    = new Rectangle[count];
    resizeSnapCmp = new Component[count];
    count = 0;
    for(int i = 0; i < sel.length; i++)
      if(sel[i] instanceof GraphNode) {
	resizeSnapCmp[count] = (Component)((GraphNode)sel[i]).get(gp.COMPONENT);
	resizeSnap[count]    = resizeSnapCmp[count].getBounds();
	count++;
      }
  }

  private void resizeSelection(Rectangle dst) {
    for(int i = 0; i < resizeSnapCmp.length; i++) {
      tmpRect = scaleRect(resizeSnapBounds, dst, resizeSnap[i], tmpRect);
      resizeSnapCmp[i].setBounds(tmpRect);
      int     cx    = tmpRect.x;
      int     cy    = tmpRect.y;
      int     cw    = resizeSnapCmp[i].getWidth();
      int     ch    = resizeSnapCmp[i].getHeight();
      int     dx2   = dst.x + dst.width;
      int     dy2   = dst.y + dst.height;
      boolean reloc = false;
      if(cw != tmpRect.width) {
	if(cx != dst.x && tmpRect.x + tmpRect.width == dx2) {
	  cx = dx2 - cw;
	  if(cx < resizeSnapBounds.x)
	    cx = resizeSnapBounds.x;
	  reloc = true;
	}
	if(cw > dst.width && 
	   (dragArea == WEST || dragArea == NORTH_WEST || dragArea == NORTH || dragArea == SOUTH_WEST)) {
	  cx = resizeSnapBounds.x + resizeSnapBounds.width - cw;
	  reloc = true;
	}
	if(resizeSnapCmp.length == 1) {
	  cx = resizeSnapBounds.x;
	  reloc = true;
	}
      }
      if(ch != tmpRect.height) {
	if(cy != dst.y && tmpRect.y + tmpRect.height == dy2) {
	  cy = dy2 - ch;
	  if(cy < resizeSnapBounds.y)
	    cy = resizeSnapBounds.y;
	  reloc = true;
	}
	if(ch > dst.height && 
	   (dragArea == WEST || dragArea == NORTH_WEST || dragArea == NORTH || dragArea == NORTH_EAST)) {
	  cy = resizeSnapBounds.y + resizeSnapBounds.height - ch;
	  reloc = true;
	}
	if(resizeSnapCmp.length == 1) {
	  cy = resizeSnapBounds.y;
	  reloc = true;
	}
      }
      if(reloc) resizeSnapCmp[i].setLocation(cx, cy);
    }	  
  }
 
  private Rectangle scaleRect(Rectangle src, Rectangle dst, Rectangle r, Rectangle result) {
    result.x      = dst.x + (((r.x           - src.x) * dst.width)  / src.width);
    result.y      = dst.y + (((r.y           - src.y) * dst.height) / src.height);
    result.width  = dst.x + (((r.x + r.width - src.x) * dst.width)  / src.width);
    result.height = dst.y + (((r.y + r.height- src.y) * dst.height) / src.height);
    
    int tmp;    
    if(result.width < result.x) {
      tmp          = result.x;
      result.x     = result.width;
      result.width = tmp;
    }
    
    if(result.height < result.y) {
      tmp           = result.x;
      result.x      = result.height;
      result.height = tmp;
    }
    
    result.width  -= result.x;
    result.height -= result.y;
    return result;
  }

  void select(int x, int y) {
    Rectangle selection = gp.getDotMarkRectangle();
    GraphNode[] nodes = gp.getModel().nodesArray();
    for(int i = 0; i < nodes.length; i++) {
      Component c = (Component)nodes[i].get(gp.COMPONENT);
      if(selection.intersects(c.getBounds(tmpRect)))
	gp.getSelectionModel().add(nodes[i]);
    }
    iiuf.util.graph.GraphEdge[] edges = gp.getModel().edgesArray();
    for(int i = 0; i < edges.length; i++) {
      GraphEdge e = (GraphEdge)edges[i].get(gp.GRAPH_EDGE);
      if(e.intersectsWith(selection))
	gp.getSelectionModel().add(edges[i]);      
    }
    gp.setDot(x, y);
    gp.setMark(x, y);
  }
  
  void endConnect() {
    ((Component)connectingNode.get(gp.COMPONENT)).setLocation(0, 0);
    Object o = gp.viewToModel(dx, dy);
    if(o instanceof GraphNode) {
      GraphNode node = (GraphNode)o;
      GraphPort port = gp.findPortAt(node, dx, dy);
      if(port != null &&
	 !port.isFull() &&
	 port.compatible(fromPort)) {
	
	if(connectingEdge.isTo(fromPort))
	  connectingEdge.setFrom(port);	  
	else
	  connectingEdge.setTo(port);
	
	GraphNode fromNode = fromPort.getNode();
	fromPort = null;
	
	int[] cmp_ids  = gp.getModel().getIds(gp.COMPONENT_TAG);
	int[] gnp_ids  = gp.getModel().getIds(gp.GRAPH_NODE_PORT_TAG);
	int[] edge_ids = gp.getModel().getIds(gp.EDGE_TAG);
	
	for(int i = 0; i < cmp_ids.length; i++)
	  if(node.get(cmp_ids[i]) instanceof Component &&
	     port.get(gnp_ids[i]) instanceof GraphNodePort &&
	     connectingEdge.get(edge_ids[i]) instanceof GraphEdge)
	    ((GraphEdge)connectingEdge.get(edge_ids[i])).setAdjacent((Component)fromNode.get(cmp_ids[i]),
								     (Component)node.get(cmp_ids[i]),
								     (GraphNodePort)port.get(gnp_ids[i]));
      }
    }
    
    gp.getModel().remove(connectingNode);
    setCursor(defaultCursor);
  }

  private void properties(GraphNode node) {
    Component props = (Component)node.get(gp.NODE_PROPERTIES);
    if(props != null) {
      Component c = (Component)node.get(gp.COMPONENT);
      Point loc = Awt.place(props.getBounds(), c.getX() + c.getWidth() / 2, c.getY() + c.getHeight(), getBounds());
      loc.x += getLocationOnScreen().x;
      loc.y += getLocationOnScreen().y;
      props.setLocation(loc);
      props.setVisible(true);
      gp.repaint();
    }
  }
  
  private void delete() {
    gp.getModel().remove((iiuf.util.graph.GraphEdge[])gp.getSelectionModel().getSelection(iiuf.util.graph.GraphEdge.class));
    gp.getModel().remove((GraphNode[])gp.getSelectionModel().getSelection(GraphNode.class));
    gp.getSelectionModel().clearSelection();
    gp.setShowPorts(null, null);
  }
  
  public void selectAll() {
    gp.getSelectionModel().addAll(gp.getModel().nodes());
    gp.getSelectionModel().addAll(gp.getModel().edges());
  }

  public void dragEnter(DropTargetDragEvent e)         {gp.dragEnter(e);}
  public void dragExit(DropTargetEvent e)              {gp.dragExit(e);}
  public void dragOver(DropTargetDragEvent e)          {gp.dragOver(e);}
  public void dropActionChanged(DropTargetDragEvent e) {gp.dropActionChanged(e);}
  public void drop(DropTargetDropEvent e)              {gp.drop(e);}

  private static final int LEFT       = 0; 
  private static final int HORIZONTAL = 1;
  private static final int RIGHT      = 2;
  private static final int TOP        = 3;
  private static final int VERTICAL   = 4;
  private static final int BOTTOM     = 5;
  
  private void align(int direction) {
    Object[]    sel = gp.getSelectionModel().getSelection(GraphNode.class);
    Component[] cs = new Component[sel.length];
    for(int i = 0; i < cs.length; i++)
      cs[i] = (Component)((GraphNode)sel[i]).get(gp.COMPONENT);
    int min; 
    int max;
    int c;
    switch(direction) {
    case LEFT:
      min = Integer.MAX_VALUE;
      for(int i = 0; i < cs.length; i++)
	if(cs[i].getX() < min)
	  min = cs[i].getX();
      for(int i = 0; i < cs.length; i++)
	cs[i].setLocation(min, cs[i].getY());
      break;
    case HORIZONTAL:
      c = cs[0].getX() + cs[0].getWidth() / 2;
      for(int i = 0; i < cs.length; i++)
	cs[i].setLocation(c - cs[i].getWidth() / 2, cs[i].getY());      
      break;
    case RIGHT:
      max = Integer.MIN_VALUE;
      for(int i = 0; i < cs.length; i++)
	if(cs[i].getX() + cs[i].getWidth() > max)
	  max = cs[i].getX() + cs[i].getWidth();
      for(int i = 0; i < cs.length; i++)
	cs[i].setLocation(max - cs[i].getWidth(), cs[i].getY());
      break;
    case TOP:
      min = Integer.MAX_VALUE;
      for(int i = 0; i < cs.length; i++)
	if(cs[i].getY() < min)
	  min = cs[i].getY();
      for(int i = 0; i < cs.length; i++)
	cs[i].setLocation(cs[i].getX(), min);
      break;
    case VERTICAL:
      c = cs[0].getY() + cs[0].getHeight() / 2;
      for(int i = 0; i < cs.length; i++)
	cs[i].setLocation(cs[i].getX(), c - cs[i].getHeight() / 2);      
      break;
    case BOTTOM:
       max = Integer.MIN_VALUE;
       for(int i = 0; i < cs.length; i++)
	 if(cs[i].getY() + cs[i].getHeight() > max)
	   max = cs[i].getY() + cs[i].getHeight();
       for(int i = 0; i < cs.length; i++)
	 cs[i].setLocation(cs[i].getX(), max - cs[i].getHeight());
       break;
    }
    gp.repaint();
  }
  
  static class CmpCard {int card(Component c) {return 0;}}
  
  static class CmpCardL extends CmpCard {int card(Component c) {return c.getX();}} 
  static private final CmpCard CMP_CARD_L = new CmpCardL();

  static class CmpCardH extends CmpCard {int card(Component c) {return c.getX() + c.getWidth() / 2;}} 
  static private final CmpCard CMP_CARD_H = new CmpCardH();

  static class CmpCardR extends CmpCard {int card(Component c) {return c.getX() + c.getWidth();}} 
  static private final CmpCard CMP_CARD_R = new CmpCardR();

  static class CmpCardT extends CmpCard {int card(Component c) {return c.getY();}} 
  static private final CmpCard CMP_CARD_T = new CmpCardT();

  static class CmpCardV extends CmpCard {int card(Component c) {return c.getY() + c.getHeight() / 2;}} 
  static private final CmpCard CMP_CARD_V = new CmpCardV();
  
  static class CmpCardB extends CmpCard {int card(Component c) {return c.getY() + c.getHeight();}} 
  static private final CmpCard CMP_CARD_B = new CmpCardB();
  
  private int partition(Component[] a, int p, int r, CmpCard cc) {
    int x = cc.card(a[p]);
    int i = p - 1;
    int j = r + 1;
    for(;;) {
      do {j--;} while(cc.card(a[j]) > x);
      do {i++;} while(cc.card(a[i]) < x);
      if(i < j) {
	Component tmp = a[i];
	a[i] = a[j];
	a[j] = tmp;
      } else
	return j;
    }
  }
  
  private void sort(Component[] a, int p, int r,  CmpCard cc) {
    if(p < r) {
      int q = partition(a, p, r, cc);
      sort(a, p,     q, cc);
      sort(a, q + 1, r, cc);
    }
  }

  private void distribute(int direction, boolean space) {
    Object[]    sel = gp.getSelectionModel().getSelection(GraphNode.class);
    Component[] cs = new Component[sel.length];
    for(int i = 0; i < cs.length; i++)
      cs[i] = (Component)((GraphNode)sel[i]).get(gp.COMPONENT);

    int min;
    int size;
    if(space)
      switch(direction) {
      case HORIZONTAL:
	sort(cs, 0, cs.length - 1, CMP_CARD_L);
	
	size = 0;
	for(int i = 0; i < cs.length; i++)
	  size += cs[i].getWidth();
	
	min  = CMP_CARD_L.card(cs[0]);
	size = CMP_CARD_R.card(cs[cs.length - 1]) - min - size;
	
	min += cs[0].getWidth();
	for(int i = 1; i < cs.length - 1; i++) {
	  cs[i].setLocation(((i * size) / (cs.length - 1)) + min, cs[i].getY());
	  min += cs[i].getWidth();
	}
	break;
      case VERTICAL:
	sort(cs, 0, cs.length - 1, CMP_CARD_T);
	
	size = 0;
	for(int i = 0; i < cs.length; i++)
	  size += cs[i].getHeight();
	
	min  = CMP_CARD_T.card(cs[0]);
	size = CMP_CARD_B.card(cs[cs.length - 1]) - min - size;
	
	min += cs[0].getHeight();
	for(int i = 1; i < cs.length - 1; i++) {
	  cs[i].setLocation(cs[i].getX(), ((i * size) / (cs.length - 1)) + min);
	  min += cs[i].getHeight();
	}
	break;
      }
    else
      switch(direction) {
      case LEFT:
	sort(cs, 0, cs.length - 1, CMP_CARD_L);
	
	min  = CMP_CARD_L.card(cs[0]);
	size = CMP_CARD_L.card(cs[cs.length - 1]) - min;
	
	if(size > 0)
	  for(int i = 1; i < cs.length - 1; i++)
	    cs[i].setLocation(((i * size) / (cs.length - 1)) + min, cs[i].getY());
	break;
      case HORIZONTAL:   
	sort(cs, 0, cs.length - 1, CMP_CARD_H);
	
	min  = CMP_CARD_H.card(cs[0]);
	size = CMP_CARD_H.card(cs[cs.length - 1]) - min;
	
	if(size > 0)
	  for(int i = 1; i < cs.length - 1; i++)
	    cs[i].setLocation(((i * size) / (cs.length - 1)) + min - cs[i].getWidth() / 2, cs[i].getY());
	break;
      case RIGHT:
	sort(cs, 0, cs.length - 1, CMP_CARD_R);
	
	min  = CMP_CARD_R.card(cs[0]);
	size = CMP_CARD_R.card(cs[cs.length - 1]) - min;
	
	if(size > 0)
	  for(int i = 1; i < cs.length - 1; i++)
	    cs[i].setLocation(((i * size) / (cs.length - 1)) + min - cs[i].getWidth(), cs[i].getY()); 
	break;
      case TOP:
	sort(cs, 0, cs.length - 1, CMP_CARD_T);
	
	min  = CMP_CARD_T.card(cs[0]);
	size = CMP_CARD_T.card(cs[cs.length - 1]) - min;
	
	if(size > 0)
	  for(int i = 1; i < cs.length - 1; i++)
	    cs[i].setLocation(cs[i].getX(), ((i * size) / (cs.length - 1) + min));
	break;
      case VERTICAL:   
	sort(cs, 0, cs.length - 1, CMP_CARD_V);
	
	min  = CMP_CARD_V.card(cs[0]);
	size = CMP_CARD_V.card(cs[cs.length - 1]) - min;
	
	if(size > 0)
	  for(int i = 1; i < cs.length - 1; i++)
	    cs[i].setLocation(cs[i].getX(), ((i * size) / (cs.length - 1)) + min - cs[i].getHeight() / 2); 
	break;
      case BOTTOM:
	sort(cs, 0, cs.length - 1, CMP_CARD_B);
	
	min  = CMP_CARD_B.card(cs[0]);
	size = CMP_CARD_B.card(cs[cs.length - 1]) - min;
	
	if(size > 0)
	  for(int i = 1; i < cs.length - 1; i++)
	    cs[i].setLocation(cs[i].getX(), ((i * size) / (cs.length - 1)) + min - cs[i].getHeight()); 
	break;
      }
    gp.repaint();
  }
  
  void rotate(int angle){
    Object o = ctxmgr.getLastObject();
    if(o instanceof GraphNodeComponent) 
      ((GraphNodeComponent)o).setRotation(((GraphNodeComponent)o).getRotation() + angle);
  }
  
  private static final int TO_FRONT = 0;
  private static final int FORWARD  = 1;
  private static final int BACKWARD = 2;
  private static final int TO_BACK  = 3;
  
  private void arrange(int key) {
    SetSelectionModel sm   = gp.getSelectionModel();
    Component[]       cs   = new Component[sm.size(GraphNode.class)];
    int[]             idxs = new int[cs.length];
    Component[]       cmps = gp.getComponents();
    int j = 0;
    for(int i = 0; i < cmps.length; i++)
      if(sm.isSelected(gp.viewToModel(cmps[i]))) {
	idxs[j] = i;
	cs[j++] = cmps[i];
      }
    
    switch(key) {
    case TO_FRONT:
      for(int i = j - 1; i >= 0; i--) {
	gp.remove(cs[i]);
	gp.add(cs[i], 1);
      }
      break;
    case FORWARD:
      for(int i = 0; i < j; i++) {
	gp.remove(cs[i]);
	int idx = idxs[i] - 1;
	if(idx < 1) idx = 1;
	gp.add(cs[i], idx);
      }
      break;      
    case BACKWARD:
      for(int i = 0; i < j; i++) {
	gp.remove(cs[i]);
	int idx = idxs[i] + 1;
	if(idx > cmps.length) idx = cmps.length;
	gp.add(cs[i], idx);
      }
      break;      
    case TO_BACK:
      for(int i = 0; i < j; i++) {
	gp.remove(cs[i]);
	gp.add(cs[i]);
      }
      break;
    }
    gp.repaint();
  }
  
  private void addActions(Action[] actions) {
    ActionMap amap = getActionMap();
    
    for(int i = 0; i < actions.length; i++)
      amap.put(actions[i].getValue(Action.NAME), actions[i]);
  }
  
  public Action[] getEditActions() {
    return editActions;
  }

  public Action[] getAlignActions() {
    return alignActions;
  }
  
  public Action[] getDistributeActions() {
    return distributeActions;
  }
  
  public Action[] getRotateActions() {
    return rotateActions;
  }

  public Action[] getArrangeActions() {
    return arrangeActions;
  }
 
  private Action[] alignActions = {
    new AbstractAction("Left", Resource.ALIGN_LEFT) {
	public void actionPerformed(ActionEvent e) {align(LEFT);}
      },
    new AbstractAction("Horizontal", Resource.ALIGN_HCENTER) {
	public void actionPerformed(ActionEvent e) {align(HORIZONTAL);}
      },
    new AbstractAction("Right", Resource.ALIGN_RIGHT) {
	public void actionPerformed(ActionEvent e) {align(RIGHT);}
      },
    new AbstractAction("Top", Resource.ALIGN_TOP) {
	public void actionPerformed(ActionEvent e) {align(TOP);}
      },
    new AbstractAction("Vertical", Resource.ALIGN_VCENTER) {
	public void actionPerformed(ActionEvent e) {align(VERTICAL);}
      },
    new AbstractAction("Bottom", Resource.ALIGN_BOTTOM) {
	public void actionPerformed(ActionEvent e) {align(BOTTOM);}
      },
  };
  
  private Action[] arrangeActions = {
    new AbstractAction("Bring To Front", Resource.TO_FRONT) {
	public void actionPerformed(ActionEvent e) {arrange(TO_FRONT);}
      },
    new AbstractAction("Bring Forward", Resource.FORWARD) {
	public void actionPerformed(ActionEvent e) {arrange(FORWARD);}
      },
    new AbstractAction("Send Backward", Resource.BACKWARD) {
	public void actionPerformed(ActionEvent e) {arrange(BACKWARD);}
      },
    new AbstractAction("Send To Back", Resource.TO_BACK) {
	public void actionPerformed(ActionEvent e) {arrange(TO_BACK);}
      },
  };
  
  private Action[] distributeActions = {
    new AbstractAction("Left", Resource.DISTRIBUTE_LEFT) {
	public void actionPerformed(ActionEvent e) {distribute(LEFT, false);}
      },
    new AbstractAction("Horizontal", Resource.DISTRIBUTE_HCENTER) {
	public void actionPerformed(ActionEvent e) {distribute(HORIZONTAL, false);}
      },
    new AbstractAction("Space Horizontal", Resource.DISTRIBUTE_SPACE_H) {
	public void actionPerformed(ActionEvent e) {distribute(HORIZONTAL, true);}
      },
    new AbstractAction("Right", Resource.DISTRIBUTE_RIGHT) {
	public void actionPerformed(ActionEvent e) {distribute(RIGHT, false);}
      },
    new AbstractAction("Top", Resource.DISTRIBUTE_TOP) {
	public void actionPerformed(ActionEvent e) {distribute(TOP, false);}
      },
    new AbstractAction("Vertical", Resource.DISTRIBUTE_VCENTER) {
	public void actionPerformed(ActionEvent e) {distribute(VERTICAL, false);}
      },
    new AbstractAction("Space Vertical", Resource.DISTRIBUTE_SPACE_V) {
	public void actionPerformed(ActionEvent e) {distribute(VERTICAL, true);}
      },
    new AbstractAction("Bottom", Resource.DISTRIBUTE_BOTTOM) {
	public void actionPerformed(ActionEvent e) {distribute(BOTTOM, false);}
      },
  };

  private Action[] rotateActions = {
    new AbstractAction("90" + Unicode.deg, Resource.ROT_90) {
	public void actionPerformed(ActionEvent e) {rotate(GraphNodeComponent.ANGLE_90);}
      },
    new AbstractAction("180" + Unicode.deg, Resource.ROT_180) {
	public void actionPerformed(ActionEvent e) {rotate(GraphNodeComponent.ANGLE_180);}
      },
    new AbstractAction("270" + Unicode.deg, Resource.ROT_270) {
	public void actionPerformed(ActionEvent e) {rotate(GraphNodeComponent.ANGLE_270);}
      },
  };
}
/*
  $Log: GraphPanelEditor.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.6  2001/03/20 21:31:26  schubige
  improved sample soundlet, added arrange actions to graph panel editor

  Revision 1.5  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.4  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.3  2001/03/05 17:55:07  schubige
  Still working on soundium properties panel

  Revision 1.2  2001/02/23 11:03:15  schubige
  try to recover table_source.png

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.12  2001/02/16 14:29:05  schubige
  Changed sound engine spec.

  Revision 1.11  2001/02/16 13:47:38  schubige
  Adapted soundium to new rtsp version

  Revision 1.10  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.9  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.8  2001/02/12 17:50:05  schubige
  still working on soundium gui

  Revision 1.7  2001/02/11 16:25:39  schubige
  working on soundium

  Revision 1.6  2001/02/09 17:34:16  schubige
  working on soundium

  Revision 1.5  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/12/29 08:03:55  schubige
  SourceWatch beta debug iter 1

  Revision 1.3  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.2  2000/12/20 09:46:39  schubige
  TJGUI update

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph

*/
