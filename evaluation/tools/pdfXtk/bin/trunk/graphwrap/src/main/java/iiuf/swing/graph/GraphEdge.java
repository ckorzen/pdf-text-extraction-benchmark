package iiuf.swing.graph;

import java.awt.Component;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import iiuf.awt.Awt;

/**
   Graph edge encapsulation.<p>

   (c) 2000, 2001, IIUF, DIUF<p>
      
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class GraphEdge {
  private static final EdgeMarker[] MDFLT = new EdgeMarker[0];
  private static final double[]     PDFLT = new double[0];
  private static final Stroke       SDFLT = new BasicStroke();
  
  public EdgeMarker[]      markers   = MDFLT;
  public double[]          markerpos = PDFLT;
  public int[]             markerwidth;
  public Stroke            stroke    = SDFLT;
  public Color             color     = Color.black;
  public Component         startcmp;
  public GraphNodePort     startport;
  public Component         endcmp;
  public GraphNodePort     endport;
  public Polygon           polyline;
  public AffineTransform[] mtrans;

  public GraphEdge(Component     startcmp,
		   GraphNodePort startport,
		   Component     endcmp,
		   GraphNodePort endport) {
    setStart(startcmp, startport);
    setEnd(endcmp,     endport);
  }
  
  public GraphEdge(Component     startcmp,
		   GraphNodePort startport,
		   Component     endcmp,
		   GraphNodePort endport,
		   EdgeMarker[]  markers, double[] markerpos) {
    this(startcmp, startport, endcmp, endport);
    setMarkers(markers, markerpos);    
  }
  
  public GraphEdge(Component     startcmp,
		   GraphNodePort startport,
		   Component     endcmp,
		   GraphNodePort endport,
		   EdgeMarker[] markers, double[] markerpos, Stroke stroke, Color color) {
    this(startcmp, startport, endcmp, endport);
    setMarkers(markers, markerpos);
    setStroke(stroke);
    setColor(color);
  }
  
  public void setStart(Component cmp, GraphNodePort port) {
    startcmp  = cmp;
    startport = port;
  }

  public void setEnd(Component cmp, GraphNodePort port) {
    endcmp  = cmp;
    endport = port;
  }
  
  public void setAdjacent(Component cmpRef, Component cmpToSet, GraphNodePort portToSet) {
    if(cmpRef == startcmp)
      setEnd(cmpToSet, portToSet);
    else
      setStart(cmpToSet, portToSet);
  }
  
  public void setStroke(Stroke stroke_) {
    stroke = stroke_;
  }
  
  public Stroke getStroke() {
    return stroke;
  }
  
  public void setMarkers(EdgeMarker[] markers_, double[] markerpos_) {
    markers     = markers_;
    markerpos   = markerpos_;
    markerwidth = new int[markerpos.length];
    mtrans      = new AffineTransform[markerpos.length];
  }
  
  public void setColor(Color color_) {
    color = color_;
  }

  public Color getColor() {
    return color;
  }
  
  private Rectangle polyRect;
  private Rectangle boundsCache;
  public Rectangle getBounds() {
    Rectangle pbounds = polyline.getBounds();
    if(polyRect != pbounds) {
      polyRect    = pbounds;
      boundsCache = (Rectangle)pbounds.clone();
      boundsCache.width++;
      boundsCache.height++;
    }
    return boundsCache;
  }
  
  public Rectangle getBounds(Rectangle result) {
    Rectangle tmp = polyline.getBounds();
    result.x      = tmp.x;
    result.y      = tmp.y;
    result.width  = tmp.width  + 1;
    result.height = tmp.height + 1;
    return result;
  }
  
  public void paint(Graphics g) {
    if(polyline == null) return;
    g.setColor(color);
    ((Graphics2D)g).setStroke(stroke);
    g.drawPolyline(polyline.xpoints, polyline.ypoints, polyline.npoints);
  }
  
  public void paintMarkers(Graphics g) {
    if(polyline != null && markers.length > 0) {
      Graphics2D      g2      = (Graphics2D)g;
      AffineTransform svTrans = g2.getTransform();
      for(int i = 0; i < markers.length; i++) {
	g2.transform(mtrans[i]);
	markers[i].paint(g, markerwidth[i]);
	g2.setTransform(svTrans);
      } 
    }
  }
  
  public boolean pointIsNear(int x, int y, int tolerance) {
    if(polyline == null) return false;
    for(int j = 0; j < polyline.npoints - 1; j++)
      if(Awt.near(x, y, polyline.xpoints[j], polyline.ypoints[j], polyline.xpoints[j + 1], polyline.ypoints[j + 1], tolerance))
	return true;
    return false;
  }
  
  public boolean containedIn(Rectangle r) {
    return r.contains(getBounds());
  }
  
  private int siam(int p0x, int p0y, int p1x, int p1y, int p2x, int p2y) {
    int dx1 = p1x - p0x;
    int dy1 = p1y - p0y;
    int dx2 = p2x - p0x;
    int dy2 = p2y - p0y;
    if(dx1 * dy2 > dy1 * dx2) return 1;
    if(dx1 * dy2 < dy1 * dx2) return -1;
    if((dx1 * dx2 < 0) || (dy1 * dy2 <0)) return -1;
    if((dx1 * dx1 + dy1 * dy1) < (dx2 * dx2 + dy2 * dy2)) return 1;
    return 0;
  }
  
  private boolean intersects(int s1x0, int s1y0, int s1x1, int s1y1, int s2x0, int s2y0, int s2x1, int s2y1) {
    return 
      ((siam(s1x0, s1y0, s1x1, s1y1, s2x0, s2y0) * siam(s1x0, s1y0, s1x1, s1y1, s2x1, s2y1)) <= 0) &&
      ((siam(s2x0, s2y0, s2x1, s2y1, s1x0, s1y0) * siam(s2x0, s2y0, s2x1, s2y1, s1x1, s1y1)) <= 0);
  }
  
  public boolean intersectsWith(Rectangle r) {
    if(r.intersects(getBounds())) {
      if(r.contains(polyline.xpoints[0], polyline.ypoints[0]))
	return true;      
      for(int j = 0; j < polyline.npoints - 1; j++) {
	if(r.contains(polyline.xpoints[j + 1], polyline.ypoints[j + 1]))
	  return true;
	if(intersects(r.x, r.y, r.x + r.width, r.y,
		      polyline.xpoints[j], polyline.ypoints[j], polyline.xpoints[j + 1], polyline.ypoints[j + 1]))      
	  return true;
	if(intersects(r.x, r.y + r.height, r.x + r.width, r.y + r.height,
		      polyline.xpoints[j], polyline.ypoints[j], polyline.xpoints[j + 1], polyline.ypoints[j + 1]))      
	  return true;
	if(intersects(r.x, r.y, r.x, r.y + r.height,
		      polyline.xpoints[j], polyline.ypoints[j], polyline.xpoints[j + 1], polyline.ypoints[j + 1]))      
	  return true;
	if(intersects(r.x + r.width, r.y, r.x + r.width, r.y + r.height,
		      polyline.xpoints[j], polyline.ypoints[j], polyline.xpoints[j + 1], polyline.ypoints[j + 1]))      
	  return true;
      }
    }
    return false;
  }
  
  public String toString() {
    return "(" + startport.x + "," + startport.y + "),(" + endport.x + "," + endport.y + ")";
  }
}

/*
  $Log: GraphEdge.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.5  2001/03/11 17:59:38  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.4  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.3  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.2  2001/02/19 15:10:38  schubige
  Fixed graph edge port location bug

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.7  2001/02/16 13:47:38  schubige
  Adapted soundium to new rtsp version

  Revision 1.6  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.5  2001/02/13 14:49:06  schubige
  started work on gui - engine connection

  Revision 1.4  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/12/29 08:03:55  schubige
  SourceWatch beta debug iter 1

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:56:20  schubige
  Added graph view stuff
  
*/
