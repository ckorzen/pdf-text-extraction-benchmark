package iiuf.util.graph;

import iiuf.util.Attributable;

/**
   Graph node port interface.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
     */
public interface GraphPort
  extends
  Attributable
{
  public static final int INFINITE = -1;
  
  public void        addGraphPortListener(GraphPortListener l);
  public void        removeGraphPortListener(GraphPortListener l);
  
  public int         getIndex();
  public void        setNode(GraphNode node, int index);
  public GraphNode   getNode();
  public boolean     compatible(GraphPort port);
  public boolean     isFull();
  public GraphEdge   createEdge(GraphPort toPort);
  public int         getOutCount();
  public GraphEdge[] getOut();
  public int         getInCount();
  public GraphEdge[] getIn();
  public int         getEdgeCount();
  public GraphEdge[] getEdges();
}

/*
  $Log: GraphPort.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.6  2001/03/08 09:32:49  schubige
  intermim checkin

  Revision 1.5  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.4  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/12/28 09:29:11  schubige
  SourceWatch beta

  Revision 1.2  2000/12/20 09:46:40  schubige
  TJGUI update

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
