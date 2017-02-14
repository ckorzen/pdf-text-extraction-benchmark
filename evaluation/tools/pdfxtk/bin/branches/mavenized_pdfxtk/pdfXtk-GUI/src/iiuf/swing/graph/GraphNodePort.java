package iiuf.swing.graph;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.ImageIcon;

import iiuf.swing.Resource;
import iiuf.util.graph.DefaultGraphEdge;
import iiuf.util.graph.DefaultGraphNode;

/**
   Visual hints & implementation for rendering ports on a graph node.<p>
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class GraphNodePort {
  public double x;
  public double y;
  public int    index;

  private static final ImageIcon SELECTED   = Resource.RED_BALL_SMALL;
  private static final ImageIcon DESELECTED = Resource.YELLOW_BALL_SMALL;
  
  public GraphNodePort(double x_, double y_, int index_) {
    x     = x_;
    y     = y_;
    index = index_;
  }
  
  public Point getLocation(Component c) {
    return new Point((int)(x * c.getWidth())  + c.getX(), (int)(y * c.getHeight()) + c.getY());
  }
  
  public void paint(Component c, Graphics g, boolean selected) {
    int ix = (int)(x * c.getWidth())  + c.getX();
    int iy = (int)(y * c.getHeight()) + c.getY();
    (selected ? SELECTED : DESELECTED).paintIcon(c, g, ix - 2, iy - 2);
  }
  
  public boolean accept(GraphNodePort fromPort, iiuf.util.graph.GraphEdge edge) {
    return true;
  }
  
  public DefaultGraphEdge createGraphEdge(DefaultGraphNode fromNode, DefaultGraphNode toNode) {
    return new DefaultGraphEdge(fromNode, toNode);
  }
}

/*
  $Log: GraphNodePort.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/03/16 18:08:20  schubige
  improved orthogonal router

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.5  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.4  2001/02/11 16:25:39  schubige
  working on soundium

  Revision 1.3  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.1  2000/12/18 12:44:35  schubige
  Added ports to iiuf.util.graph
  
*/
