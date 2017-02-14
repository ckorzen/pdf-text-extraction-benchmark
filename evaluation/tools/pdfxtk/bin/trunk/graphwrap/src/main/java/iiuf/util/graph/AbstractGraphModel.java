package iiuf.util.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.LinkedList;

import iiuf.util.DefaultAttributable;
import iiuf.util.EventListenerList;
import iiuf.util.AttributeFactory;
import iiuf.util.graph.GraphException;

/**
   Graph base class.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class AbstractGraphModel
  extends
  DefaultAttributable
  implements
  GraphModel
{
  protected final static int ADD = 0;
  protected final static int RMV = 1;
  
  private HashSet            nodes      = new HashSet();
  private HashSet            edges      = new HashSet();
  private GraphNode[]        nodesa;
  private GraphEdge[]        edgesa;
  private String[]           attributes = new String[0];
  private AttributeFactory[] factories  = new AttributeFactory[0];
  private BitSet             edgeAttr   = new BitSet();
  private BitSet             nodeAttr   = new BitSet();
  private BitSet             graphAttr  = new BitSet();
  private BitSet             portAttr   = new BitSet();
  private EventListenerList  listeners  = new EventListenerList();
  
  public void addGraphModelListener(GraphModelListener listener) {
    listeners.add(GraphModelListener.class, listener);
  }

  public void addGraphModelListener(GraphModelListener listener, boolean weak) {
    listeners.add(GraphModelListener.class, listener, weak);
  }

  public void removeGraphModelListener(GraphModelListener listener) {
    listeners.remove(GraphModelListener.class, listener);
  }
  
  public void add(GraphEdge edge) {
    add(edge, null);
  }
  
  public void add(GraphEdge edge, Object[] args) {
    add(new GraphEdge[] {edge}, args);
  }

  public void add(GraphEdge[] edges) {
    add(edges, null);
  }
  
  private synchronized void add(GraphEdge[] edges_, Object[] args) {
    for(int j = 0; j < edges_.length; j++) {
      GraphEdge edge = edges_[j];
      for(int i = 0; i < attributes.length; i++)
	if(isEdgeAttribute(i) && !edge.has(i))
	  edge.set(i, factories[i].newAttribute(edge, args));
      edges.add(edge);
    }
    edgesa = null;
    
    GraphModelListener[] l = (GraphModelListener[])listeners.getListeners(GraphModelListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].edgesAdded(this, edges_);
  }

  public void remove(GraphEdge edge) {
    remove(new GraphEdge[] {edge});
  }
  
  public synchronized void remove(GraphEdge[] edges_) {
    for(int j = 0; j < edges_.length; j++)
      edges.remove(edges_[j]);
    edgesa = null;
    GraphModelListener[] l = (GraphModelListener[])listeners.getListeners(GraphModelListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].edgesRemoved(this, edges_);
    for(int j = 0; j < edges_.length; j++)
      edges_[j].remove();
  }

  public void add(GraphNode node) {
    add(node, null);
  }
  
  public void add(GraphNode node, Object[] args) {
    add(new GraphNode[] {node}, args);
  }
  
  public void add(GraphNode[] nodes) {
    add(nodes, null);
  }
  
  private synchronized void add(GraphNode[] nodes_, Object[] args) {
    for(int j = 0; j < nodes_.length; j++) {
      GraphNode node = nodes_[j];
      if(node.getEdges().length != 0) throw new IllegalArgumentException("node must have no edges:" + node);
      for(int i = 0; i < attributes.length; i++) {	
	if(isNodeAttribute(i) && !node.has(i))
	  node.set(i, factories[i].newAttribute(node, args));
	GraphPort[] ports = node.getPorts();
	for(int k = 0; k < ports.length; k++)
	  if(isPortAttribute(i) && !ports[k].has(i))
	    ports[k].set(i, factories[i].newAttribute(ports[k], null));
      }
      nodes.add(node);
    }
    nodesa = null;
    GraphModelListener[] l = (GraphModelListener[])listeners.getListeners(GraphModelListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].nodesAdded(this, nodes_);
  }
  
  public void remove(GraphNode node) {
    remove(new GraphNode[] {node});
  }
  
  public synchronized void remove(GraphNode[] nodes_) {
    HashSet removeEdges = new HashSet();
    for(int j = 0; j < nodes_.length; j++) {
      GraphEdge[] es = nodes_[j].getEdges();
      for(int i = 0; i < es.length; i++)
	removeEdges.add(es[i]);
      nodes.remove(nodes_[j]);
    }
    nodesa = null;
    remove((GraphEdge[])removeEdges.toArray(new GraphEdge[removeEdges.size()]));
    GraphModelListener[] l = (GraphModelListener[])listeners.getListeners(GraphModelListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].nodesRemoved(this, nodes_);
  }
  
  public Collection edges() {
    return edges;
  }

  public Collection nodes() {
    return nodes;
  }
  
  public synchronized GraphEdge[] edgesArray() {
    if(edgesa == null)
    edgesa = (GraphEdge[])edges.toArray(new GraphEdge[edges.size()]);
    return edgesa;
  }
  
  public synchronized GraphNode[] nodesArray() {
    if(nodesa == null)
    nodesa = (GraphNode[])nodes.toArray(new GraphNode[nodes.size()]);
    return nodesa;
  }
  
  private int attribute(String id, AttributeFactory factory) {
    int result   = attributes.length;
    String[] tmp = attributes;
    attributes = new String[result + 1];
    System.arraycopy(tmp, 0, attributes, 0, result);
    attributes[result] = id;
    AttributeFactory[] tmp1 = factories;
    factories = new AttributeFactory[result + 1];
    System.arraycopy(tmp1, 0, factories, 0, result);
    factories[result] = factory;
    
    return result;
  }  
  
  public synchronized int portAttribute(String id, AttributeFactory factory) {
    int result = attribute(id, factory);
    
    portAttr.set(result);
    GraphNode[] o = nodesArray();
    for(int i = 0; i < o.length; i++) {
      GraphPort[] ports = o[i].getPorts();
      for(int j = 0; j < ports.length; j++)
	ports[j].set(result, factory.newAttribute(ports[j], null));
    }
    
    return result;
  }
  
  public synchronized int nodeAttribute(String id, AttributeFactory factory) {
    int result = attribute(id, factory);
    
    nodeAttr.set(result);
    GraphNode[] o = nodesArray();
    for(int i = 0; i < o.length; i++)
      o[i].set(result, factory.newAttribute(o[i], null));
    
    return result;
  }
  
  public synchronized int edgeAttribute(String id, AttributeFactory factory) {
    int result = attribute(id, factory);

    edgeAttr.set(result);
    GraphEdge[] o = edgesArray();
    for(int i = 0; i < o.length; i++)
      o[i].set(result, factory.newAttribute(o[i], null));
    
    return result;
  }
  
  public synchronized int graphAttribute(String id, AttributeFactory factory) {
    int result = attribute(id, factory);

    set(result, factory.newAttribute(this, null));
    graphAttr.set(result);

    return result;
  }
  
  public boolean isEdgeAttribute(int id) {
    return edgeAttr.get(id);
  }

  public boolean isNodeAttribute(int id) {
    return nodeAttr.get(id);
  }

  public boolean isGraphAttribute(int id) {
    return graphAttr.get(id);
  }
  
  public boolean isPortAttribute(int id) {
    return portAttr.get(id);
  }
  
  public String getId(int id) {
    return attributes[id];
  }
  
  public synchronized int[] getIds(String id) {
    int[] tmp   = new int[attributes.length];
    int   count = 0;
    for(int i = 0; i < attributes.length; i++)
    if(attributes[i].equals(id))
    tmp[count++] = i;
    int[] result = new int[count];
    System.arraycopy(tmp, 0, result, 0, count);
    return result;
  }
  
  protected abstract void check(int op, Object change) throws GraphException;
}

/*
  $Log: AbstractGraphModel.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.9  2001/04/30 07:33:17  schubige
  added webcom to cvstree

  Revision 1.8  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.7  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2001/01/03 15:23:51  schubige
  graph stuff beta

  Revision 1.5  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.4  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.3  2000/11/10 10:07:03  schubige
  iiuf tree cleanup iter 3

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/11/10 07:30:48  schubige
  iiuf tree cleanup iter 1

  Revision 1.5  2000/10/20 07:38:04  robadey
  *** empty log message ***

  Revision 1.4  2000/10/19 08:03:45  schubige
  Intermediate graph component related checkin

  Revision 1.3  2000/10/19 07:42:44  schubige
  Changed breadthFirst to walk only connected graphs

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:48:10  schubige
  Added graph stuff
  
*/
