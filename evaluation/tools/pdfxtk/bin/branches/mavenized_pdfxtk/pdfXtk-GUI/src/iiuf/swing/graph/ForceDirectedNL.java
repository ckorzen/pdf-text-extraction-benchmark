package iiuf.swing.graph;

import java.awt.Dimension;
import java.awt.Component;

import iiuf.util.Util;
import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.graph.GraphNode;
import iiuf.util.graph.GraphModel;
import iiuf.util.graph.GraphEdge;

import iiuf.util.Util;

/**
   Force directred node layout node layouter.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ForceDirectedNL
  extends
  Thread
  implements
  NodeLayouter
{
  private int         NODE_INFO;
  private GraphModel  gca;
  private Dimension   size   = new Dimension(1500, 1500);
  private GraphNode[] gnodes;
  private GraphEdge[] gedges;
  private Component[] cmps;
  private int         COMPONENT;
  private boolean     running;
  private boolean     inited;
  
  public ForceDirectedNL() {
    setPriority(MIN_PRIORITY);
    start();
  }
  
  public boolean allowsNodeLocationChange() {
    return true;
  }

  public Dimension layout(GraphPanel panel, GraphModel graph) {
    boolean reinit;
    synchronized(this) {
      reinit = gca != graph;
      
      if(reinit) {      
	NODE_INFO = graph.nodeAttribute("node_info", new AttributeFactory() {
	    public Object newAttribute(Attributable attributable, Object[] args) {
	      return new NodeInfo();
	    }
	  });
	gca = graph;
      }
      
      COMPONENT  = panel.COMPONENT;
      gnodes     = (GraphNode[])graph.nodes().toArray(new GraphNode[graph.nodes().size()]);
      gedges     = (GraphEdge[])graph.edges().toArray(new GraphEdge[graph.edges().size()]);
      cmps       = new Component[gnodes.length];
    }
    
    int fixedc = 0;
    for(int i = 0; i < cmps.length; i++) {
      cmps[i] = (Component)gnodes[i].get(COMPONENT);
      NodeInfo info = (NodeInfo)gnodes[i].get(NODE_INFO);
      if(info.fixed) fixedc++;
      
      if(reinit) {
	Dimension d = cmps[i].getPreferredSize();      
	cmps[i].setBounds(Util.intRandom(size.width), Util.intRandom(size.height), d.width, d.height);
      }
      info.x  = cmps[i].getX();
      info.y  = cmps[i].getY();
    }
    
    inited = true;
    activate();
    
    return size;
  }
  
  public void setFixed(GraphNode node, boolean state) {
    ((NodeInfo)node.get(NODE_INFO)).fixed = state;
  } 
  
  public synchronized void activate() {
    running = true;
    notify();
  }

  public synchronized void deactivate() {
    running = false;
    inited  = false;
  }

  class NodeInfo {
    double  dx;
    double  dy;
    double  x;
    double  y;
    boolean fixed;
  }
  
  public void run() {
    for(;;) {
      synchronized(this) {
	try{wait();}
	catch(InterruptedException e) {Util.printStackTrace(e);}
	if(!inited) continue;
      }
      while(running) {
	Util.delay(100);
	GraphPanel gp = null;
	synchronized(this) {
	  relax();
	  
	  if(cmps[0] == null) continue;
	  gp = (GraphPanel)cmps[0].getParent();
	  if(gp == null) continue;
	}
	
	for(int i = 0; i < gnodes.length; i++) { 
	  NodeInfo  info = (NodeInfo)gnodes[i].get(NODE_INFO);
	  if(gp.getSelectionModel().isSelected(gnodes[i]) || gp.isDragging(gnodes[i])) {
	    info.x = cmps[i].getX();
	    info.y = cmps[i].getY();
	  } else
	    cmps[i].setLocation((int)info.x, (int)info.y);
	}
	gp.repaint();
      }
    }
  }

  private void relax() {
    int nnodes = gnodes.length;    
        
    for (int i = 0 ; i < gedges.length; i++) {
      GraphEdge e = gedges[i];
      
      GraphNode[] nodes = e.getNodes();
      
      Component to   = (Component)nodes[e.TO].get(COMPONENT);
      Component from = (Component)nodes[e.FROM].get(COMPONENT);
      
      double vx  = to.getX() - from.getX();
      double vy  = to.getY() - from.getY();
      double len = Math.sqrt(vx * vx + vy * vy);
      
      len = (len == 0) ? .0001 : len;
      double f = (e.getWeight() - len) / (len * 3);
      double dx = f * vx;
      double dy = f * vy;
      
      NodeInfo toi   = (NodeInfo)nodes[e.TO].get(NODE_INFO);
      toi.dx += dx;
      toi.dy += dy;
      NodeInfo fromi = (NodeInfo)nodes[e.FROM].get(NODE_INFO);
      fromi.dx += -dx;
      fromi.dy += -dy;
    }
    
    for (int i = 0 ; i < nnodes ; i++) {
      GraphNode n1  = gnodes[i];
      NodeInfo  n1i = (NodeInfo)n1.get(NODE_INFO);
      double    dx  = 0;
      double    dy  = 0;
      
      for (int j = 0 ; j < nnodes ; j++) {
	if (i == j) {
	  continue;
	}
	GraphNode n2  = gnodes[j];
	NodeInfo  n2i = (NodeInfo)n2.get(NODE_INFO);
	double    vx  = n1i.x - n2i.x;
	double    vy  = n1i.y - n2i.y;
	double    len = vx * vx + vy * vy;
	if (len == 0) {
	  dx += Math.random();
	  dy += Math.random();
	} else if (len < 100*100) {
	  dx += vx / len;
	  dy += vy / len;
	}
      }
      double dlen = dx * dx + dy * dy;
      if (dlen > 0) {
	dlen = Math.sqrt(dlen) / 2;
	n1i.dx += dx / dlen;
	n1i.dy += dy / dlen;
      }
    }
    
    for (int i = 0 ; i < nnodes ; i++) {
      GraphNode n  = gnodes[i];
      NodeInfo  ni = (NodeInfo)n.get(NODE_INFO);
      if (!ni.fixed) {
	ni.x += Math.max(-5, Math.min(5, ni.dx));
	ni.y += Math.max(-5, Math.min(5, ni.dy));
      }
      if (ni.x < 0) {
	ni.x = 0;
      } else if (ni.x > size.width) {
	ni.x = size.width;
      }
      if (ni.y < 0) {
	ni.y = 0;
      } else if (ni.y > size.height) {
	ni.y = size.height;
      }
      ni.dx /= 2;
      ni.dy /= 2;
    }
  }
}

/*
  $Log: ForceDirectedNL.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.5  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.3  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.2  2000/11/10 10:46:53  schubige
  iiuf tree cleanup iter 3

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.1  2000/07/28 12:07:58  schubige
  Graph stuff update
  
*/

