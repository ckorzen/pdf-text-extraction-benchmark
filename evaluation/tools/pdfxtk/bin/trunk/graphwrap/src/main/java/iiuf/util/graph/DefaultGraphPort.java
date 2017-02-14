package iiuf.util.graph;

import java.util.Iterator;

import iiuf.util.EventListenerList;
import iiuf.util.DefaultAttributable;

/**
   Default graph port implementation.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DefaultGraphPort 
  extends
  DefaultAttributable
  implements
  GraphPort
{
  protected int       maxCapacity;
  protected int       capacity;
  protected GraphNode node;
  private   int       index;
  private   EventListenerList listeners = new EventListenerList();
  
  public DefaultGraphPort(int capacity_) {
    capacity    = capacity_;
    maxCapacity = capacity;
    
    addGraphPortListener(new GraphPortListener() {
	public void connected(GraphPort port, GraphEdge edge) {
	  if(capacity != INFINITE) {
	    capacity--;
	    if(capacity < 0) throw new IllegalArgumentException("capacity < 0");
	  }
	}
	
	public void disconnected(GraphPort port, GraphEdge edge) {
	  if(capacity != INFINITE) {
	    capacity++;
	    if(capacity > maxCapacity) throw new IllegalArgumentException("capacity > maxCapacity");
	  }
	}	
      });
  }
  
  public void addGraphPortListener(GraphPortListener l) {
    listeners.add(GraphPortListener.class, l);
  }

  public void addGraphPortListener(GraphPortListener l, boolean weak) {
    listeners.add(GraphPortListener.class, l, weak);
  }

  public void removeGraphPortListener(GraphPortListener l) {
    listeners.remove(GraphPortListener.class, l);
  }
  
  public void fireConnected(GraphPort port, GraphEdge edge) {
    GraphPortListener[] l = (GraphPortListener[])listeners.getListeners(GraphPortListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].connected(port, edge);
  }
  
  public void fireDisconnected(GraphPort port, GraphEdge edge) {
    GraphPortListener[] l = (GraphPortListener[])listeners.getListeners(GraphPortListener.class);
    for(int i = 0; i < l.length; i++)
      l[i].disconnected(port, edge);
  }
  
  public int getIndex() {
    return index;
  }

  public GraphNode getNode() {
    return node;
  }

  public void setNode(GraphNode node_, int index_) {
    index = index_;
    node  = node_;
  }

  public boolean compatible(GraphPort port) {
    return true;
  }
  
  public boolean isFull() {
    return capacity == 0;
  }
  
  public GraphEdge createEdge(GraphPort toPort) {
    return new DefaultGraphEdge(this, toPort);
  }
  
  private int getCount(GraphEdge[] edges) {
    int result = 0;
    for(int i = 0; i < edges.length; i++)
      if(edges[i].isFrom(this) || edges[i].isTo(this))
	result++;
    return result;
  }
  
  private GraphEdge[] getEdges(GraphEdge[] edges) {
    GraphEdge[] result = new GraphEdge[getCount(edges)];
    int j = 0;
    for(int i = 0; i < edges.length; i++)
    if(edges[i].isFrom(this) || edges[i].isTo(this))
    result[j++] = edges[i];
    return result;
  }
  
  public int getEdgeCount() {
    return getCount(node.getEdges());
  }
  
  public GraphEdge[] getEdges() {
    return getEdges(node.getEdges());
  }
  
  public int getInCount() {
    return getCount(node.getIn());
  }
  
  public GraphEdge[] getIn() {
    return getEdges(node.getIn());
  }

  public int getOutCount() {
    return getCount(node.getOut());
  }

  public GraphEdge[] getOut() {
    return getEdges(node.getOut());    
  }  
}

/*
  $Log: DefaultGraphPort.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.11  2001/04/11 19:02:08  schubige
  fixed connection bug and made JSliderSoundlet domable

  Revision 1.10  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.9  2001/03/08 09:32:49  schubige
  intermim checkin

  Revision 1.8  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.7  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.6  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.5  2001/01/04 09:58:50  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.4  2001/01/03 15:23:51  schubige
  graph stuff beta

  Revision 1.3  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.2  2000/12/20 09:46:40  schubige
  TJGUI update

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
