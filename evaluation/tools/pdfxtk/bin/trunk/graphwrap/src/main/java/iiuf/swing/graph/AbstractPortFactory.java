package iiuf.swing.graph;

import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.graph.GraphPort;

/**
   Port list factory for GRAPH_NODE_PORT attribute of graph ports in a GraphPanel.<p>

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class AbstractPortFactory 
  implements
  AttributeFactory
{
  public Object newAttribute(Attributable attributable, Object[] args) {
    Object result = newPort((GraphPort)attributable, args);
    return result == null ? new GraphNodePort(0.5, 0.5, ((GraphPort)attributable).getIndex()) : result;
  }
  
  abstract protected GraphNodePort newPort(GraphPort port, Object[] args);
}

/*
  $Log: AbstractPortFactory.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/03/16 18:08:20  schubige
  improved orthogonal router

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.3  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
