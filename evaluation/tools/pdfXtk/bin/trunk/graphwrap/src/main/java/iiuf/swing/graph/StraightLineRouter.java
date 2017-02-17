package iiuf.swing.graph;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.Polygon;

import iiuf.awt.Awt;

/**
   Straight line edge router, connects directly two points doesn't check any crossing etc..
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class StraightLineRouter 
  implements
  GraphRouter
{
  private static final double PI2 = Math.PI / 2;
  
  public void init() {}
  
  public void setupEdges(GraphPanel panel, GraphEdge[] edges, Component[] nodes) {
    for(int i = 0; i < edges.length; i++) {
      GraphEdge edge = edges[i];
      int[] x = {(int)(edge.startport.x * edge.startcmp.getWidth())  + edge.startcmp.getX(), 
		 (int)(edge.endport.x   * edge.endcmp.getWidth())    + edge.endcmp.getX()};
      int[] y = {(int)(edge.startport.y * edge.startcmp.getHeight()) + edge.startcmp.getY(),
		 (int)(edge.endport.y   * edge.endcmp.getHeight())   + edge.endcmp.getY()};
      
      edge.polyline = new Polygon(x, y, 2);
      
      double angle = Awt.getAngle(x[0], y[0], x[1], y[1]) + PI2;
      for(int j = 0; j < edge.markers.length; j++)
	GraphEdgeUtils.setupMTrans(edge, j, angle, x[0], y[0], x[1], y[1]);
    } 
  }
}
/*
  $Log: StraightLineRouter.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.3  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.2  2001/02/19 15:10:38  schubige
  Fixed graph edge port location bug

  Revision 1.1  2001/02/17 09:54:22  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.6  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/12/29 08:03:55  schubige
  SourceWatch beta debug iter 1

  Revision 1.4  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.3  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.1  2000/07/28 12:07:58  schubige
  Graph stuff update

  
*/
