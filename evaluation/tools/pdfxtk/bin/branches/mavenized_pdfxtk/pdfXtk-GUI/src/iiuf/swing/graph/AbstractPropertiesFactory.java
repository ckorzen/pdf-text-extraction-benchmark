package iiuf.swing.graph;

import java.awt.Component;

import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.graph.GraphNode;

/**
   Properties factory for PROPERTIES attribute of graph nodes in a GraphPanel.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class AbstractPropertiesFactory 
  implements
  AttributeFactory
{
  public Object newAttribute(Attributable attributable, Object[] args) {
    return newPropertiesWindow((GraphNode)attributable, args);
  }
  
  abstract protected Component newPropertiesWindow(GraphNode node, Object[] args);
}

/*
  $Log: AbstractPropertiesFactory.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.2  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
