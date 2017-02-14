package iiuf.swing.graph;

import java.awt.geom.AffineTransform;

import iiuf.awt.Awt;

/**
   Utils for graph edges.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
     */
public class GraphEdgeUtils {
  private final static double          PI2   = Math.PI / 2.0;
  
  static void setupMTrans(GraphEdge edge, int mtransIdx, double angle, int x0, int y0, int x1, int y1) {
    int    dx  = x1 - x0;
    int    dy  = y1 - y0;
    edge.mtrans[mtransIdx] = AffineTransform.getTranslateInstance(x0, y0);
    if(dx == 0 && dy == 0) return; 
    double len = Math.sqrt(dx * dx + dy * dy);
    double w   = edge.markers[mtransIdx].getPreferredSize().width;    
    edge.mtrans[mtransIdx].rotate(angle);
    if(w > len) {
      edge.markerwidth[mtransIdx] = (int)len;
      edge.mtrans[mtransIdx].translate(0, -(double)edge.markers[mtransIdx].getBaseline());
    } else {
      edge.markerwidth[mtransIdx] = (int)w;
      edge.mtrans[mtransIdx].translate((Math.sqrt(dx * dx + dy * dy) - w) * edge.markerpos[mtransIdx], 
			       -(double)edge.markers[mtransIdx].getBaseline());
    }
  }
}

/*
  $Log: GraphEdgeUtils.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/03/13 13:41:05  schubige
  Fixed some graph panel and soundium bugs

  Revision 1.2  2001/03/11 17:59:38  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel
  
*/
