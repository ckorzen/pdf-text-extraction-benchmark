package iiuf.swing.graph;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Component;

import iiuf.util.graph.GraphModel;
import iiuf.util.graph.GraphNode;

/**
   Default (no-op) node layouter.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DefaultNL
  implements
  NodeLayouter
{
  public boolean allowsNodeLocationChange() {
    return true;
  }
  
  public void activate() {}
  
  public void deactivate() {}
  
  public Dimension layout(GraphPanel panel, GraphModel graph) {
    if(graph == null) return new Dimension(1, 1);
    GraphNode[] nodes  = graph.nodesArray();
    Rectangle   r      = null;
    Rectangle   bounds = new Rectangle();
    for(int i = 0; i < nodes.length; i++) {
      Component c = (Component)nodes[i].get(panel.COMPONENT);
      bounds = c.getBounds(bounds);
      if(bounds.width == 0 && bounds.height == 0)
	c.setSize(c.getPreferredSize());
      if(r == null)
	r = (Rectangle)bounds.clone();
      r.add(bounds);
    }
    return r == null ? panel.getSize() : r.getSize();
  }
}

/*
  $Log: DefaultNL.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/02/26 15:57:22  schubige
  Again changes in SoundEngine.x, added some todos to graph panel & co

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.4  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.3  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
