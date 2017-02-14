package iiuf.util.graph;

import java.util.Collection;
import java.util.ArrayList;

import iiuf.util.DefaultAttributable;

/**
   Default graph node implementaiton.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DefaultGraphNode
  extends
  DefaultAttributable 
  implements
  GraphNode
{
  private static final GraphEdge[] ETMPL = new GraphEdge[0];

  public final static int INF   = Integer.MAX_VALUE;
  
  protected ArrayList   out   = new ArrayList();
  protected ArrayList   in    = new ArrayList();
  protected GraphPort[] ports;
  
  public int       color;
  public double    distance;
  public GraphNode parent;
  
  public DefaultGraphNode() {
    ports = new GraphPort[] {new DefaultGraphPort(GraphPort.INFINITE)};
    initPorts();
  }
  
  public DefaultGraphNode(GraphPort[] ports_) {
    ports = ports_;
    if(ports.length < 1)
      throw new IllegalArgumentException("Missing default port (ports.length must be > 0)");
    
    initPorts();
  }
  
  private void initPorts() {
    for(int i = 0; i < ports.length; i++)
      ports[i].setNode(this, i);
  }
  
  public GraphEdge[] getOut() {
    return (GraphEdge[])out.toArray(ETMPL);
  }

  public GraphEdge[] getIn() {
    return (GraphEdge[])in.toArray(ETMPL);
  }
  
  public GraphEdge[] getEdges() {
    Object[]    outa   = out.toArray();
    Object[]    ina    = in.toArray();
    GraphEdge[] result = new GraphEdge[outa.length + ina.length];
    for(int i = 0; i < outa.length; i++)
    result[i] = (GraphEdge)outa[i];
    for(int i = 0; i < ina.length; i++)
    result[outa.length + i] = (GraphEdge)ina[i];
    return result;
  }

  public int getEdgeCount() {
    return out.size() + in.size();
  }
  
  public int getInCount() {
    return in.size();
  }
  
  public int getOutCount() {
    return out.size();
  }

  public String toString() {
    return "GraphNode[color:" + color + " distance:" + distance + " super:" + super.toString() + "]";
  }

  public GraphPort[] getPorts() {
    return ports;
  }
  
  public GraphPort getPort(int index) {
    return ports[index];
  }

  public GraphPort getDefaultPort() {
    return getPort(DEFAULT_PORT);
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color_) {
    color = color_;
  }
}

/*
  $Log: DefaultGraphNode.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.10  2001/05/11 11:30:26  schubige
  fns demo final

  Revision 1.9  2001/03/22 16:08:19  schubige
  more work on dom stuff

  Revision 1.8  2001/03/21 19:34:06  schubige
  started with dom stuff

  Revision 1.7  2001/03/08 09:32:49  schubige
  intermim checkin

  Revision 1.6  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.5  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/12/20 09:46:39  schubige
  TJGUI update

  Revision 1.3  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/11/10 07:33:22  schubige
  iiuf tree cleanup iter 1

  Revision 1.2  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.1  2000/07/14 13:48:11  schubige
  Added graph stuff
  
*/
