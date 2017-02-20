package iiuf.util.graph;

import iiuf.util.DefaultAttributable;

/**
   Default graph edge implementation.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DefaultGraphEdge
  extends
  DefaultAttributable
  implements
  GraphEdge
{
  private double weight = 1.0;
  
  private GraphPort[] ports = new GraphPort[2];
  
  public DefaultGraphEdge(GraphNode fromNode, GraphNode toNode, double weight) {
    this(fromNode, toNode);
    setWeight(weight);
  }

  public DefaultGraphEdge(GraphPort fromPort, GraphPort toPort, double weight) {
    this(fromPort, toPort);
    setWeight(weight);
  }

  public DefaultGraphEdge(GraphNode fromNode, GraphNode toNode) {
    this(fromNode.getDefaultPort(), toNode.getDefaultPort());
  }
  
  public DefaultGraphEdge(GraphPort fromPort, GraphPort toPort) {
    if(fromPort == null)
      throw new IllegalArgumentException("fromPort must be != null");
    if(toPort == null)
      throw new IllegalArgumentException("toPort must be != null");
    ports[FROM] = fromPort;
    ports[TO]   = toPort;
    ((DefaultGraphNode)fromPort.getNode()).out.add(this);
    ((DefaultGraphNode)toPort.getNode()).in.add(this);
    ((DefaultGraphPort)fromPort).fireConnected(toPort, this);
    ((DefaultGraphPort)toPort).fireConnected(fromPort, this);
  }
  
  public void fromTo(GraphNode fromNode, GraphNode toNode) {
    fromTo(fromNode.getDefaultPort(), toNode.getDefaultPort());
  }
  
  public void fromTo(GraphPort fromPort, GraphPort toPort) {
    if(fromPort == null)
      throw new IllegalArgumentException("fromPort must be != null");
    if(toPort == null)
      throw new IllegalArgumentException("toPort must be != null");
    remove();
    ports[FROM] = fromPort;
    ports[TO]   = toPort;
    ((DefaultGraphNode)fromPort.getNode()).out.add(this);
    ((DefaultGraphNode)toPort.getNode()).in.add(this);
    ((DefaultGraphPort)fromPort).fireConnected(toPort, this);
    ((DefaultGraphPort)toPort).fireConnected(fromPort, this);    
  }
  
  public void swapFromTo() {
    ((DefaultGraphNode)ports[FROM].getNode()).out.remove(this);    
    ((DefaultGraphNode)ports[TO].getNode()).in.remove(this);
    
    GraphPort tmp = ports[FROM];
    ports[FROM]   = ports[TO];
    ports[TO]     = tmp;
    
    ((DefaultGraphNode)ports[FROM].getNode()).out.add(this);
    ((DefaultGraphNode)ports[TO].getNode()).in.add(this);    
  }
  
  public void setFrom(GraphNode fromNode) {
    setFrom(fromNode.getPorts()[GraphNode.DEFAULT_PORT]);
  }
  
  public void setTo(GraphNode toNode) {
    setTo(toNode.getPorts()[GraphNode.DEFAULT_PORT]);
  }

  public GraphNode getFromNode() {
    return ports[FROM].getNode();
  }

  public GraphNode getToNode() {
    return ports[TO].getNode();
  }

  public GraphPort getFromPort() {
    return ports[FROM];
  }

  public GraphPort getToPort() {
    return ports[TO];
  }
  
  public void setFrom(GraphPort fromPort) {
    fromTo(fromPort, ports[TO]);
  }
  
  public void setTo(GraphPort toPort) {
    fromTo(ports[FROM], toPort);    
  }
  
  
  public GraphNode[] getNodes() {
    return new GraphNode[] {ports[0].getNode(), ports[1].getNode()};
  }

  public GraphPort[] getPorts() {
    return ports;
  }
  
  
  public GraphNode getAdjacent(GraphNode node) {
    if(node == ports[FROM].getNode()) return ports[TO].getNode();
    if(node == ports[TO].getNode())   return ports[FROM].getNode();
    throw new IllegalArgumentException("Don't know \"" + node + "\" from:\"" + 
				       ports[FROM].getNode() + "\", to \"" + 
				       ports[TO].getNode() + "\"");
  }
  
  public GraphPort getAdjacent(GraphPort port) {
    if(port == ports[FROM]) return ports[TO];
    if(port == ports[TO])   return ports[FROM];
    throw new IllegalArgumentException("Don't know \"" + port + "\" from:\"" + ports[FROM] + "\", to \"" + ports[TO ]+ "\"");
  }

  public boolean isFrom(GraphNode node) {
    return node == ports[FROM].getNode();
  }

  public boolean isTo(GraphNode node) {
    return node == ports[TO].getNode();
  }

  public boolean isFrom(GraphPort port) {
    return port == ports[FROM];
  }

  public boolean isTo(GraphPort port) {
    return port == ports[TO];
  }
  
  public void setWeight(double weight_) {
    weight = weight_;
  }

  public double getWeight() {
    return weight;
  }
  
  public void remove() {
    if(ports[FROM] != null) {
      ((DefaultGraphNode)ports[FROM].getNode()).out.remove(this);
      ((DefaultGraphPort)ports[FROM]).fireDisconnected(ports[FROM], this);
      ports[FROM] = null;
    }
    
    if(ports[TO] != null) {
      ((DefaultGraphNode)ports[TO].getNode()).in.remove(this);
      ((DefaultGraphPort)ports[TO]).fireDisconnected(ports[TO], this);
      ports[TO] = null;
    }
  }
  
  public String toString() {
    return "Edge from " + ports[FROM] + " to " + ports[TO] + " " + super.toString();
  }
}

/*
  $Log: DefaultGraphEdge.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.16  2001/07/30 15:27:04  schubige
  adapted for sample based timing

  Revision 1.15  2001/04/11 19:02:08  schubige
  fixed connection bug and made JSliderSoundlet domable

  Revision 1.14  2001/03/26 15:35:33  schubige
  fixed format bug

  Revision 1.13  2001/03/21 19:34:06  schubige
  started with dom stuff

  Revision 1.12  2001/03/15 16:05:13  schubige
  cleanup and various fixes

  Revision 1.11  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.10  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.9  2001/02/23 17:23:11  schubige
  Added loop source to soundium and fxed some bugs along

  Revision 1.8  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.7  2001/02/13 14:49:06  schubige
  started work on gui - engine connection

  Revision 1.6  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.4  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.3  2000/11/10 09:53:07  schubige
  iiuf tree cleanup iter 3

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/11/10 07:33:42  schubige
  iiuf tree cleanup iter 1

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:48:11  schubige
  Added graph stuff
  
*/
