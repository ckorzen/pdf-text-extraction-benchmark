package iiuf.swing.graph;

import java.awt.Dimension;
import java.awt.Component;
import java.util.ArrayList;

import iiuf.util.graph.GraphModel;
import iiuf.util.graph.GraphNode;
import iiuf.awt.Awt;

/**
   Node layouter implementation that orderes the node in a tree starting from a start node.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class TreeNL
  implements
  NodeLayouter
{
  public static final int NORTH = 0;
  public static final int EAST  = 1;
  public static final int SOUTH = 2;
  public static final int WEST  = 3;
  
  private static final int OFF  = 10;
  private static final int IOFF = 4;
  private GraphNode        start;
  private int              direction;
  private boolean          bottomUp;
  
  public TreeNL() {
    this(null, EAST);
  }
  
  public TreeNL(GraphNode start, int direction) {
    this(start, direction, false);
  }
  
  public TreeNL(GraphNode start, int direction, boolean bottomUp_) {
    setStart(start);
    setDirection(direction);
    bottomUp = bottomUp_;
  }
  
  public boolean allowsNodeLocationChange() {
    return false;
  }

  public void setDirection(int direction_) {
    direction = direction_;
  }

  public void setStart(GraphNode start_) {
    start = start_;
  }
  
  class TreeNode {
    Component cmp;
    ArrayList children;
    Dimension cmpsz;

    TreeNode() {
      cmp   = iiuf.awt.Awt.newComponent();
      cmpsz = new Dimension();
    }
    
    TreeNode(GraphNode node, int COMPONENT) {
      node.setColor(node.BLACK);
      if(bottomUp) {
	iiuf.util.graph.GraphEdge[] in = node.getIn();
	cmp   = (Component)node.get(COMPONENT);
	cmpsz = cmp.getPreferredSize(); 
	for(int i = 0; i < in.length; i++) {
	  GraphNode from = in[i].getNodes()[in[i].FROM];
	  if(from.getColor() == from.WHITE)
	    add(new TreeNode(from, COMPONENT));
	}
      } else {
	iiuf.util.graph.GraphEdge[] out = node.getOut();
	cmp   = (Component)node.get(COMPONENT);
	cmpsz = cmp.getPreferredSize(); 
	for(int i = 0; i < out.length; i++) {
	  GraphNode to = out[i].getNodes()[out[i].TO];
	  if(to.getColor() == to.WHITE)
	    add(new TreeNode(to, COMPONENT));
	}
      }
    }
    
    void add(TreeNode n) {
      if(children == null)
	children = new ArrayList();
      children.add(n);
    }
    
    Dimension getSize() {
      if(children == null)
	return cmpsz;
      int width  = 0;
      int height = 0;
      TreeNode[] tns = (TreeNode[])children.toArray(new TreeNode[children.size()]);
      for(int i = 0; i < tns.length; i++) {
	tns[i].cmp.setBounds(cmp.getX() + OFF + cmpsz.width, cmp.getY() + height, tns[i].cmpsz.width, tns[i].cmpsz.height);
	Dimension d = tns[i].getSize();
	width  =  d.width > width ? d.width : width;
	height += d.height + (i < tns.length - 1 ? IOFF : 0);
      }
      
      height = height > cmpsz.height ? height : cmpsz.height;

      cmp.setLocation(cmp.getX(), cmp.getY() + (height - cmpsz.height - IOFF) / 2);

      width  += cmpsz.width + OFF;

      if(tns.length > 1)
	height += OFF;
      
      return new Dimension(width, height);
    }
  }
  
  private GraphNode getRoot(GraphNode node) {
    if(node == null) return null;
    if(bottomUp) {
      iiuf.util.graph.GraphEdge[] out = node.getOut();
      if(out.length == 0) return node;
      for(GraphNode n = out[0].getToNode(); n != node; n = out[0].getToNode()) {
	out = n.getOut();
	if(out.length == 0) return n;
      }
    }
    else {
      iiuf.util.graph.GraphEdge[] in = node.getIn();
      if(in.length == 0) return node;
      for(GraphNode n = in[0].getFromNode(); n != node; n = in[0].getFromNode()) {
	in = n.getIn();
	if(in.length == 0) return n;
      }
    }
    return node;
  }

  public Dimension layout(GraphPanel panel, GraphModel graph) {
    if(graph == null) return new Dimension(1, 1);
    synchronized(graph) {
      GraphNode[] nodes = graph.nodesArray();
      
      if(start == null) {
	if(nodes.length == 0) return new Dimension(1,1);
	for(int i = 0; i < nodes.length; i++)
	  if(nodes[i] instanceof ConnectingNode)
	    continue;
	  else {
	    setStart(getRoot(nodes[i]));
	    break;
	  }
      }
      if(start == null) return new Dimension(1, 1);
      
      for(int i = 0; i < nodes.length; i++) {
	GraphNode gn = nodes[i];
	gn.setColor(gn instanceof ConnectingNode ? gn.BLACK : gn.WHITE);
      }
      
      TreeNode  result = new TreeNode();
      GraphNode top    = start;
      do {
	result.add(new TreeNode(top, panel.COMPONENT));
	top = null;
	for(int i = 0; i < nodes.length; i++) {
	  if(nodes[i].getColor() == GraphNode.WHITE) {
	    top = getRoot(nodes[i]);
	    break;
	  }
	}
      } while(top != null);
      
      return result.getSize();
    }
  }
  
  public void activate()   {}
  public void deactivate() {}
}

/*
  $Log: TreeNL.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/05/01 18:08:56  schubige
  webcom demo beta

  Revision 1.2  2001/04/30 07:33:17  schubige
  added webcom to cvstree

  Revision 1.1  2001/02/17 09:54:22  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.6  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.4  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.3  2000/11/10 10:46:53  schubige
  iiuf tree cleanup iter 3

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:56:20  schubige
  Added graph view stuff
  
*/
