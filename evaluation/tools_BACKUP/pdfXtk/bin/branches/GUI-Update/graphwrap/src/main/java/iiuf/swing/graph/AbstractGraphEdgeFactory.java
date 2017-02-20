package iiuf.swing.graph;

import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.graph.GraphNode; 
import iiuf.util.graph.GraphPort; 

/**
   Graph edge factory.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class AbstractGraphEdgeFactory 
  implements 
  AttributeFactory
{
  public Object newAttribute(Attributable attributable, Object[] args) {
    iiuf.util.graph.GraphEdge edge  = (iiuf.util.graph.GraphEdge)attributable;
    return newGraphEdge(edge, edge.getFromNode(), edge.getFromPort(), edge.getToNode(), edge.getToPort(), args);
  }
  
  protected abstract GraphEdge newGraphEdge(iiuf.util.graph.GraphEdge edge, 
					    GraphNode fromNode, GraphPort fromPort, 
					    GraphNode toNode,   GraphPort toPort,
					    Object[] args);
}

/*
  $Log: AbstractGraphEdgeFactory.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.3  2001/01/04 16:28:37  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.1  2000/12/18 12:44:34  schubige
  Added ports to iiuf.util.graph
  
*/
