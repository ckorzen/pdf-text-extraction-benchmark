package iiuf.util.graph;

import java.util.HashMap;
import java.util.LinkedList;

import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;

/**
   Various graph utilities.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Utilities {
  
  public static void breadthFirst(GraphModel graph, DefaultGraphNode start, Object userobject, GraphWalk walker) {
    breadthFirst(graph, start, userobject, false, walker);
  }
  
  private static void updateNode(GraphModel graph, DefaultGraphNode node, int color, double distance, DefaultGraphNode parent, 
			  GraphWalk walker, Object userobject, int component) {
    node.color    = node.GRAY;
    node.distance = distance;
    node.parent   = parent;
    walker.node(graph, node, userobject, component);
  }

  public static void breadthFirst(GraphModel graph, DefaultGraphNode start, Object userobject, boolean all, 
				  GraphWalk walker) {
    synchronized(graph) {
      LinkedList queue = new LinkedList();
      
      Object[] nodes = graph.nodes().toArray();
      for(int i = 0; i < nodes.length; i++) {
	DefaultGraphNode gn = (DefaultGraphNode)nodes[i];
	gn.color    = gn.WHITE;
	gn.distance = gn.INF;
	gn.parent   = null;
      }
      for(int component = 0;; component++) {
	updateNode(graph, start, DefaultGraphNode.GRAY, 0, null, walker, userobject, component);
	queue.add(start);
	while(!queue.isEmpty()) {
	  DefaultGraphNode u   = (DefaultGraphNode)queue.getFirst();
	  GraphEdge[] adj = u.getEdges();
	  for(int i = 0; i < adj.length; i++) {
	    DefaultGraphNode v = (DefaultGraphNode)adj[i].getAdjacent(u);
	    if(v.color == v.WHITE) {
	      updateNode(graph, v, v.GRAY, u.distance + 1, u, walker, userobject, component);
	      queue.add(v);
	    }
	  }
	  
	  queue.removeFirst();
	  u.color = u.BLACK;
	  
	  start = null;
	  if(all) {
	    for(int i = 0; i < nodes.length; i++)
	      if(((DefaultGraphNode)nodes[i]).color == DefaultGraphNode.WHITE) {
		start = (DefaultGraphNode)nodes[i];
		break;
	      }
	  }
	  if(start == null) return;
	}
      }
    }
  }
  
  public static class ShortestPath {
    GraphNode[]   nodes;
    GraphEdge[][] edges;
    HashMap       nodeIndex = new HashMap();
    int[][]       P;

    ShortestPath(GraphNode[] n_) {
      nodes  = n_;
      int        nnodes = nodes.length;
      double[][] D0     = new double[nnodes][nnodes];
      double[][] D1     = new double[nnodes][nnodes];
      int[][]    P0     = new int[nnodes][nnodes];
      int[][]    P1     = new int[nnodes][nnodes];
      
      edges  = new GraphEdge[nnodes][nnodes];
      for(int i = 0; i < nnodes; i++)
	nodeIndex.put(nodes[i], new Integer(i));
      
      // init weight matrix

      for(int i = 0; i < nnodes; i++) {
      nodeloop:
	for(int j = 0; j < nnodes; j++) {
	  if(i == j) D1[i][j] = 0;
	  else {
	    GraphEdge[] outs = nodes[i].getOut();
	    for(int k = 0; k < outs.length; k++)
	      if(outs[k].getToNode() == nodes[j]) {
		edges[i][j] = outs[k];
		D1[i][j]    = outs[k].getWeight();
		continue nodeloop;
	      }
	    D1[i][j] = Double.POSITIVE_INFINITY;
	  }
	}
      }
      // floyd-wharshall
      // init
      for(int i = 0; i < nnodes; i++)
	for(int j = 0; j < nnodes; j++)
	  P1[i][j] = i != j && D1[i][j] != Double.POSITIVE_INFINITY ? i : -1;
      
      for(int k = 1; k <= nnodes; k++) { // as both i and j run from 0 upwards, but k starts from 1                         
	int kdec = k - 1;  // there is a need to adjust k, i.e. create and use kdec.
	for(int i = 0;i < nnodes; i++)
	  for(int j = 0; j < nnodes; j++) {
	    if (D1[i][j] > D1[i][kdec] + D1[kdec][j]) {
	      D0[i][j] = D1[i][kdec] + D1[kdec][j];
	      P0[i][j] = P1[kdec][j];
	    } else {
	      D0[i][j] = D1[i][j];
	      P0[i][j] = P1[i][j];
	    }
	  }
	P1 = P0;
	D1 = D0;
      }
      
      P = P0;

      /*
      System.out.println("D");
      for(int i = 0; i < nnodes; i++) {
	for(int j = 0; j < nnodes; j++)
	  System.out.print((D0[i][j] == Double.POSITIVE_INFINITY ? "INF" : D0[i][j] + "")+ "\t");
	System.out.println();
      }      
      
      System.out.println("P");
      for(int i = 0; i < nnodes; i++) {
	for(int j = 0; j < nnodes; j++)
	  System.out.print((P[i][j] == Double.POSITIVE_INFINITY ? "INF" : P[i][j] + "")+ "\t");
	System.out.println();
      }
      */
    }
    
    private LinkedList shortestPath(LinkedList l, GraphNode from, GraphNode to) {
      if(from == to) {
	l.add(to);
	return l;
      }
      int i = ((Integer)nodeIndex.get(from)).intValue();
      int j = ((Integer)nodeIndex.get(to)).intValue();
      if(P[i][j] == -1) return l;
      else {
	shortestPath(l, from, nodes[P[i][j]]);
	l.add(to);
      }
      return l;
    }
    
    public GraphEdge[] shortestPath(GraphNode from, GraphNode to) {
      LinkedList nodes = shortestPath(new LinkedList(), from, to);
      if(nodes.size() < 2) return null;
      GraphEdge[] result = new GraphEdge[nodes.size() - 1];
      
      for(int k = 1; k < nodes.size(); k++) {
	int i = ((Integer)nodeIndex.get(nodes.get(k - 1))).intValue();
	int j = ((Integer)nodeIndex.get(nodes.get(k))).intValue();
	result[k - 1] = edges[i][j];
      }
      
      return result;
    }
  }
  
  public static ShortestPath shortestPath(GraphModel m) {    
    return shortestPath(m.nodesArray());
  }
  
  public static ShortestPath shortestPath(GraphNode[] n) {
    return new ShortestPath(n);
  }

  public static void main(String[] argv) {
    GraphModel         m = new DefaultGraphModel();    
    DefaultGraphNode[] n = new DefaultGraphNode[6];
    for(int i = 1; i < 6; i++){
      n[i] = new DefaultGraphNode();
      n[i].color = i;
      m.add(n[i]);
    }
    
    m.add(new DefaultGraphEdge(n[1], n[2], 3));
    m.add(new DefaultGraphEdge(n[1], n[3], 8));
    m.add(new DefaultGraphEdge(n[1], n[5], -4));
    m.add(new DefaultGraphEdge(n[2], n[4], 1));
    m.add(new DefaultGraphEdge(n[2], n[5], 7));
    m.add(new DefaultGraphEdge(n[3], n[2], 4));
    m.add(new DefaultGraphEdge(n[4], n[1], 2));
    m.add(new DefaultGraphEdge(n[4], n[3], -5));    
    m.add(new DefaultGraphEdge(n[5], n[4], 6));    
    
    DefaultGraphNode[] tmp = n;
    n = new DefaultGraphNode[tmp.length - 1];

    System.arraycopy(tmp, 1, n, 0, n.length);

    ShortestPath sp = shortestPath(n);
    
    for(int i = 0; i < n.length; i++)
      for(int j = 0; j < n.length; j++) {
	GraphEdge[] e = sp.shortestPath(n[i], n[j]);
	if(e != null) {
	  System.out.print(n[i].color + "->" + n[j].color + ":");
	  for(int k = 0; k < e.length; k++)
	    System.out.print(" " + ((DefaultGraphNode)e[k].getFromNode()).color + "->" + 
			     ((DefaultGraphNode)e[k].getToNode()).color);
	  System.out.println();
	}
      }
  }
}
/*
  $Log: Utilities.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.4  2001/07/30 15:27:04  schubige
  adapted for sample based timing

  Revision 1.3  2001/05/11 11:30:26  schubige
  fns demo final

  Revision 1.2  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/10 10:11:54  schubige
  iiuf tree cleanup iter 3
*/
