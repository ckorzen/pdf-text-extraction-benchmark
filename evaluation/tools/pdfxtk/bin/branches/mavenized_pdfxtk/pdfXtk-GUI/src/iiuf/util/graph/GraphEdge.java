package iiuf.util.graph;

import iiuf.util.Attributable;

/**
   Graph edge interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $
*/
public interface GraphEdge 
  extends
  Attributable
{
  public static final int FROM = 0;
  public static final int TO   = 1;
  
  public void        fromTo(GraphNode fromNode, GraphNode toNode);  
  public void        fromTo(GraphPort fromPort, GraphPort toPort);  
  public void        setFrom(GraphNode fromNode);  
  public void        setFrom(GraphPort fromPort);  
  public void        setTo(GraphNode toNode);  
  public void        setTo(GraphPort toPort);  
  public GraphNode[] getNodes();
  public GraphNode   getFromNode();
  public GraphNode   getToNode();
  public GraphPort[] getPorts();
  public GraphPort   getFromPort();
  public GraphPort   getToPort();
  public GraphNode   getAdjacent(GraphNode node);
  public GraphPort   getAdjacent(GraphPort port);
  public boolean     isFrom(GraphNode node);
  public boolean     isFrom(GraphPort port);
  public boolean     isTo(GraphNode node);
  public boolean     isTo(GraphPort port);
  public void        swapFromTo();

  public void        setWeight(double weight);
  public double      getWeight();

  public void        remove();
}

/*
  $Log: GraphEdge.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.4  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/12/28 09:29:11  schubige
  SourceWatch beta

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/11/10 07:30:48  schubige
  iiuf tree cleanup iter 1

*/
