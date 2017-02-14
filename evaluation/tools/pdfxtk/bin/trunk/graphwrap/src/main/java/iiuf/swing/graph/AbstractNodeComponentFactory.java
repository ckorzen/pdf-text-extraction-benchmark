package iiuf.swing.graph;

import java.awt.Component;

import iiuf.awt.Awt;
import iiuf.util.AttributeFactory;
import iiuf.util.Attributable;
import iiuf.util.graph.GraphNode;

/**
   Component factory for COMPONENT attribute of graph nodes in a GraphPanel.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class AbstractNodeComponentFactory 
  implements
  AttributeFactory
{
  GraphPanel gp;
  
  public AbstractNodeComponentFactory(GraphPanel graphPanel) {
    gp = graphPanel;
  } 
  
  public Object newAttribute(Attributable attributable, Object[] args) {
    GraphNode node = (GraphNode)attributable;
    if(node instanceof ConnectingNode) return Awt.newComponent();
    Object result = newNodeComponent((GraphNode)attributable, args);
    return result == null ? Awt.newComponent() : result;
  }

  abstract protected Component newNodeComponent(GraphNode node, Object[] args);
}

/*
  $Log: AbstractNodeComponentFactory.java,v $
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
