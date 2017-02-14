package iiuf.util.graph;

import java.util.EventListener;

/**
   Graph model listener interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface GraphModelListener 
  extends
  EventListener 
{
  public void edgesAdded(GraphModel   model, GraphEdge[] edges);
  public void nodesAdded(GraphModel   model, GraphNode[] nodes);
  public void edgesRemoved(GraphModel model, GraphEdge[] edges);
  public void nodesRemoved(GraphModel model, GraphNode[] nodes); 
}

/*
  $Log: GraphModelListener.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/03 15:23:51  schubige
  graph stuff beta

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
