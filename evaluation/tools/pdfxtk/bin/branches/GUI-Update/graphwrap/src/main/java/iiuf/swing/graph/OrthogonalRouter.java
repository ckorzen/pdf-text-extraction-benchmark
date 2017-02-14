package iiuf.swing.graph;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Color;

import iiuf.awt.Awt;

/**
   Orthogonal edge router, connects two points using orthogonal bends only.<p>

   (c) 2000, 2001, IIUF, DIUF<p>
      
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public final class OrthogonalRouter 
  implements
  GraphRouter
{
  private static final double PI2     = Math.PI / 2;
  private static final int    MAXLOOP = 10;
  private static final int    DINC    = 2;
  
  private void hline(int x, int y) {
    int lastx = xpoints[npoints - 1];
    int lasty = ypoints[npoints - 1];

    if(lastx < x) lr(lastx, lasty, x, y);
    else          rl(lastx, lasty, x, y);
  }

  private void lr(int lastx, int lasty, int x, int y) {
    hcnt += hdir;
    
    if(y < 0 || y >= height) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }
    int[] idxs = getYCmpsCa[y];
    if(idxs == null) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }

    int is   = -1;
    int minx = Integer.MAX_VALUE;
    int miny = Integer.MAX_VALUE;
    int maxy = Integer.MIN_VALUE;
    int stop = idxs.length;
    for(int j = 0; j < stop; j++) {
      int i = idxs[j];
      
      if(x     <= x1ca[i]) continue;
      if(lastx >= x2ca[i]) continue;
      if(exclude[i])       continue;
      
      if(x1ca[i] < minx) {
	is   = i;
	minx = x1ca[i];
      }

      miny = y1ca[i] < miny ? y1ca[i] : miny;
      maxy = y2ca[i] > maxy ? y2ca[i] : maxy;
    }
    
    if(minx != Integer.MAX_VALUE) {
      wbrac[is]++;
      int dx  = minx - lastx;
      int off = wbrac[is] * DINC;
      int nx  = lastx + (dx > off ? dx - off : dx);
      xpoints[npoints]   = nx;
      ypoints[npoints++] = y;
      if(hcnt < 0) {
	sbrac[is]++;
	vline(nx, maxy + (sbrac[is] * DINC));
      }
      else {
	nbrac[is]++;
	vline(nx, miny - (nbrac[is] * DINC));
      }
      hline(x, ypoints[npoints - 1]);
    }
    else {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
    }
  }

  private void rl(int lastx, int lasty, int x, int y) {
    hcnt -= hdir;
    int is   = -1;
    int maxx = Integer.MIN_VALUE;
    int miny = Integer.MAX_VALUE;
    int maxy = Integer.MIN_VALUE;
    
    if(y < 0 || y >= height) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }
    int[] idxs = getYCmpsCa[y];
    if(idxs == null) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }

    int stop = idxs.length;
    for(int j = 0; j < stop; j++) {
      int i = idxs[j];

      if(lastx <= x1ca[i]) continue;
      if(x     >= x2ca[i]) continue;
      if(exclude[i])       continue;
      
      if(x2ca[i] > maxx) {
	is = i;
	maxx = x2ca[i];
      }
      
      miny = y1ca[i] < miny ? y1ca[i] : miny;
      maxy = y2ca[i] > maxy ? y2ca[i] : maxy;   
    }
    
    if(maxx != Integer.MIN_VALUE) {
      ebrac[is]++;
      int dx  = lastx - maxx;
      int off = ebrac[is] * DINC;
      int nx  = lastx - (dx > off ? dx - off : dx);
      xpoints[npoints]   = nx;
      ypoints[npoints++] = y;
      if(hcnt < 0) {
	sbrac[is]++;
	vline(nx, maxy + (sbrac[is] * DINC));
      }
      else {
	nbrac[is]++;
	vline(nx, miny - (nbrac[is] * DINC));
      }
      hline(x, ypoints[npoints - 1]);
    }
    else {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
    }
  }
  
  private void vline(int x, int y) {
    int lastx = xpoints[npoints - 1];
    int lasty = ypoints[npoints - 1];

    if(lasty < y) tb(lastx, lasty, x, y);
    else          bt(lastx, lasty, x, y);
  }
  
  private void tb(int lastx, int lasty, int x, int y) {
    vcnt += vdir;
    
    if(x < 0 || x >= width) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }
    int[] idxs = getXCmpsCa[x];
    if(idxs == null) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }

    int is   = -1;
    int minx = Integer.MAX_VALUE;
    int maxx = Integer.MIN_VALUE;
    int miny = Integer.MAX_VALUE;
    int stop = idxs.length;
    for(int j = 0; j < stop; j++) {
      int i = idxs[j];

      if(y     <= y1ca[i]) continue;
      if(lasty >= y2ca[i]) continue;
      if(exclude[i])       continue;
      
      if(y1ca[i] < miny) {
	is = i;
	miny = y1ca[i];
      }
      
      minx = x1ca[i] < minx ? x1ca[i] : minx;
      maxx = x2ca[i] > maxx ? x2ca[i] : maxx;
    }
    
    if(miny != Integer.MAX_VALUE) {
      nbrac[is]++;
      int dy  = miny - lasty;
      int off = nbrac[is] * DINC;
      int ny  = lasty + (dy > off ? dy - off : dy);
      xpoints[npoints]   = x;
      ypoints[npoints++] = ny;
      
      if(vcnt > 0) {
	wbrac[is]++;
	hline(minx - (wbrac[is] * DINC), ny);
      }
      else {
	ebrac[is]++;
	hline(maxx + (ebrac[is] * DINC), ny);
      }
      vline(xpoints[npoints - 1], y);
    }
    else {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
    }
  }
  
  private void bt(int lastx, int lasty, int x, int y) {
    vcnt -= vdir;
    
    if(x < 0 || x >= width) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }
    int[] idxs = getXCmpsCa[x];
    if(idxs == null) {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
      return;
    }

    int is   = -1;
    int minx = Integer.MAX_VALUE;
    int maxx = Integer.MIN_VALUE;
    int maxy = Integer.MIN_VALUE;
    int stop = idxs.length;
    for(int j = 0; j < stop; j++) {
      int i = idxs[j];
      
      if(lasty <= y1ca[i]) continue;
      if(y     >= y2ca[i]) continue;
      if(exclude[i])       continue;
      
      if(y2ca[i] > maxy) {
	is = i;
	maxy = y2ca[i];
      }
      
      minx = x1ca[i] < minx ? x1ca[i] : minx;
      maxx = x2ca[i] > maxx ? x2ca[i] : maxx;
    }
    
    if(maxy != Integer.MIN_VALUE) {
      sbrac[is]++;
      int dy  = lasty - maxy;
      int off = sbrac[is] * DINC;
      int ny  = lasty - (dy > off ? dy - off : dy);
      xpoints[npoints]   = x;
      ypoints[npoints++] = ny;

      if(vcnt > 0) {
	wbrac[is]++;
	hline(minx - (wbrac[is] * DINC), ny);
      }
      else {
	ebrac[is]++;
	hline(maxx + (ebrac[is] * DINC), ny);
      }
      vline(xpoints[npoints - 1], y);
    }
    else {
      xpoints[npoints]   = x;
      ypoints[npoints++] = y;
    }
  }
  
  private void hconnect(int x, int y) {
    int q = 0;
    while(xpoints[npoints - 1] != x ||
	  ypoints[npoints - 1] != y) {
      hline(x, ypoints[npoints - 1]);
      vline(xpoints[npoints - 1], y);
      if(q++ > MAXLOOP) {
	xpoints[npoints]   = x;
	ypoints[npoints++] = y;
	break;
      }
    }
  }
  
  private void vconnect(int x, int y) {
    int q = 0;
    while(xpoints[npoints - 1] != x ||
	  ypoints[npoints - 1] != y) {
      vline(xpoints[npoints - 1], y);
      hline(x, ypoints[npoints - 1]);
      if(q++ > MAXLOOP) {
	xpoints[npoints]   = x;
	ypoints[npoints++] = y;
	break;
      }
    }
  }
  
  private int       vdir;
  private int       hdir;
  private int       hcnt;
  private int       vcnt;
  private int       endx;
  private int       endy;
  private int       startx;
  private int       starty;
  private int[]     x1ca;
  private int[]     y1ca;
  private int[]     x2ca;
  private int[]     y2ca;
  private int[]     rtca;
  private boolean[] exclude;
  private int[]     nbrac;
  private int[]     ebrac;
  private int[]     sbrac;
  private int[]     wbrac;
  private int       nnodes = -1;
  private boolean   changed;
  private boolean   reinit;
  private int[][]   getYCmpsCa;
  private int[]     getYCmpsCnt;
  private int[][]   getXCmpsCa;
  private int[]     getXCmpsCnt;
  private int       width;
  private int       oldwidth = -1;
  private int       height;
  private int       oldheight = -1;
  private int[]     xpoints = new int[1024];
  private int[]     ypoints = new int[1024];
  private int[]     cummlen = new int[1024];
  private int       npoints;
  private Rectangle tmpRect = new Rectangle();
  
  public void init() {
    nnodes = -1;
  }

  private void stateUpdate(Component[] nodes, GraphEdge[] edges) {
    changed = false;
    reinit  = false;
    
    if(nnodes != nodes.length) {
      nnodes = nodes.length;
      x1ca   = new int[nnodes];
      y1ca   = new int[nnodes];
      x2ca   = new int[nnodes];
      y2ca   = new int[nnodes];
      rtca   = new int[nnodes];
    }
    
    nbrac   = new int[nnodes];
    ebrac   = new int[nnodes];
    sbrac   = new int[nnodes];
    wbrac   = new int[nnodes];

    width  = Integer.MIN_VALUE;
    height = Integer.MIN_VALUE;

    int chx1 = 0;
    int chy1 = 0;
    int chx2 = 0;
    int chy2 = 0;
    int chi  = -1;
    
    for(int i = 0; i < nnodes; i++) {
      Component node = nodes[i];
      tmpRect = node.getBounds(tmpRect);
      int x1 = tmpRect.x;
      int y1 = tmpRect.y;
      int x2 = x1 + tmpRect.width;
      int y2 = y1 + tmpRect.height;
      int r  = node instanceof GraphNodeComponent ? ((GraphNodeComponent)node).getRotation() : 0;
      
      if(x1 != x1ca[i] || y1 != y1ca[i] || x2 != x2ca[i] || y2 != y2ca[i] || r != rtca[i]) {
	if(!changed) {
	  chx1 = x1ca[i];
	  chy1 = y1ca[i];
	  chx2 = x2ca[i];
	  chy2 = y2ca[i];
	  chi  = i;
	  changed = true;
	} else reinit = true;
      }
	
      x1ca[i] = x1;
      y1ca[i] = y1;
      x2ca[i] = x2;
      y2ca[i] = y2;
      rtca[i] = r;

      width  = x2 > width  ? x2 : width;
      height = y2 > height ? y2 : height;
    }
    
    width++;
    height++;

    if(oldwidth != width) {
      reinit   = true;
      oldwidth = width;
    }

    if(oldheight != height) {
      reinit    = true;
      oldheight = height;
    }
    
    if(changed) {
      exclude    = new boolean[nnodes];
      
      for(int j = 0; j < nnodes; j++)
	for(int k = j + 1; k < nnodes; k++) {
	  boolean inter =
	    x2ca[j] >= x1ca[k] - 3 &&
	    x2ca[k] >= x1ca[j] - 3 &&
	    y2ca[j] >= y1ca[k] - 3 &&
	    y2ca[k] >= y1ca[j] - 3;
	  exclude[k] |= inter;
	  exclude[j] |= inter;
	}
      
      if(reinit) {
	getYCmpsCnt = new int[height];
	getXCmpsCnt = new int[width];
	
	// calc array sizes
	for(int i = 0; i < nnodes; i++) {
	  int stop = y2ca[i];
	  for(int k = y1ca[i] < 1 ? 1 : y1ca[i] + 1; k < stop; k++)
	    getYCmpsCnt[k]++;
	  stop = x2ca[i];
	  for(int k = x1ca[i] < 1 ? 1 : x1ca[i] + 1; k < stop; k++)
	    getXCmpsCnt[k]++;
	}
	
	// alloc arrays
	getYCmpsCa = new int[height][];
	for(int i = 0; i < height; i++)
	  if(getYCmpsCnt[i] > 0) {
	    getYCmpsCa[i]  = new int[getYCmpsCnt[i]];
	    getYCmpsCnt[i] = 0;
	  }
	
	getXCmpsCa = new int[width][];
	for(int i = 0; i < width; i++)
	  if(getXCmpsCnt[i] > 0) {
	    getXCmpsCa[i]  = new int[getXCmpsCnt[i]];
	    getXCmpsCnt[i] = 0;
	  }
	
	// setup array
	for(int i = 0; i < nnodes; i++) {
	  int stop = y2ca[i];
	  for(int k = y1ca[i] < 1 ? 1 : y1ca[i] + 1; k < stop; k++) {
	    getYCmpsCa[k][getYCmpsCnt[k]] = i;
	    getYCmpsCnt[k]++;
	  }
	  stop = x2ca[i];
	  for(int k = x1ca[i] < 1 ? 1 : x1ca[i] + 1; k < stop; k++) {
	    getXCmpsCa[k][getXCmpsCnt[k]] = i;
	    getXCmpsCnt[k]++;
	  }
	}
      } else {
	// remove changed component
	
	int stop = chy2;
	for(int k = chy1 < 1 ? 1 : chy1 + 1; k < stop; k++) {
	  getYCmpsCnt[k]--;
	  
	  if(getYCmpsCnt[k] == 0) {
	    getYCmpsCa[k] = null;
	    continue;
	  }
	  
	  int[] a      = getYCmpsCa[k];
	  int[] result = new int[getYCmpsCnt[k]];
	  int   ri     = 0;
	  for(int i = 0; i < a.length; i++)
	    if(a[i] != chi) result[ri++] = a[i];
	  getYCmpsCa[k] = result;
	}

	stop = chx2;
	for(int k = chx1 < 1 ? 1 : chx1 + 1; k < stop; k++) {
	  getXCmpsCnt[k]--;

	  if(getXCmpsCnt[k] == 0) {
	    getXCmpsCa[k] = null;
	    continue;
	  }
	  
	  int[] a      = getXCmpsCa[k];
	  int[] result = new int[getXCmpsCnt[k]];
	  int   ri     = 0;
	  for(int i = 0; i < a.length; i++)
	    if(a[i] != chi) result[ri++] = a[i];
	  getXCmpsCa[k] = result;
	}
	
	// add changed component
	stop = y2ca[chi];
	for(int k = y1ca[chi] < 1 ? 1 : y1ca[chi] + 1; k < stop; k++) {
	  getYCmpsCnt[k]++;
	  int[] tmp = getYCmpsCa[k];
	  getYCmpsCa[k] = new int[getYCmpsCnt[k]];
	  if(getYCmpsCnt[k] > 1) System.arraycopy(tmp, 0,  getYCmpsCa[k], 1, tmp.length);
	  getYCmpsCa[k][0] = chi;
	}
	stop = x2ca[chi];
	for(int k = x1ca[chi] < 1 ? 1 : x1ca[chi] + 1; k < stop; k++) {
	  getXCmpsCnt[k]++;
	  int[] tmp = getXCmpsCa[k];
	  getXCmpsCa[k] = new int[getXCmpsCnt[k]];
	  if(getXCmpsCnt[k] > 1) System.arraycopy(tmp, 0,  getXCmpsCa[k], 1, tmp.length);
	  getXCmpsCa[k][0] = chi;
	}
      }
    }
  }
  
  public synchronized void setupEdges(GraphPanel panel, GraphEdge[] edges, Component[] nodes) {
    if(edges.length == 0) return;
    
    stateUpdate(nodes, edges);
    
    if(changed) {
      for(int i = 0; i < edges.length; i++) {
	GraphEdge edge = edges[i];
	hcnt = vcnt = 0;
	
	tmpRect = edge.endcmp.getBounds(tmpRect);
 	int ecx1 = tmpRect.x;
	int ecy1 = tmpRect.y;
	int ecx2 = tmpRect.width;
	int ecy2 = tmpRect.height;
	tmpRect = edge.startcmp.getBounds(tmpRect);
	int scx1 = tmpRect.x;
	int scy1 = tmpRect.y;
	int scx2 = tmpRect.width;
	int scy2 = tmpRect.height;
	
	endx   = (int)(edge.endport.x   * ecx2) + ecx1;
	endy   = (int)(edge.endport.y   * ecy2) + ecy1;
	startx = (int)(edge.startport.x * scx2) + scx1;
	starty = (int)(edge.startport.y * scy2) + scy1;
	
	hdir = endx > startx ? -1 : 1;
	vdir = endy > starty ? -1 : 1;
	
	Polygon hpoly = null;
	Polygon vpoly = null;
	npoints = 0;
	xpoints[npoints]   = startx;
	ypoints[npoints++] = starty;
	      
	int     eci     = -1;
	int     sci     = -1;
	boolean endsv     = false;
	boolean startsv   = false;
	boolean endC      = edge.endport.x   == 0.5 && edge.endport.y   == 0.5;
	boolean startC    = edge.startport.x == 0.5 && edge.startport.y == 0.5;
	if(endC || startC) {
	  for(int j = 0; j < nodes.length; j++)
	    if(nodes[j] == edge.endcmp) {	   
	      eci = j;
	      break;
	    }
	  for(int j = 0; j < nodes.length; j++)
	    if(nodes[j] == edge.startcmp) {
	      sci = j;
	      break;
	    }
	  
	  endsv   = exclude[eci];
	  startsv = exclude[sci];
	  
	  exclude[eci] = true;
	  exclude[sci] = true;

	  hconnect(endx, endy);
	  hpoly = new Polygon(xpoints, ypoints, npoints);
	  npoints = 1;
	  
	  vconnect(endx, endy);
	  vpoly = new Polygon(xpoints, ypoints, npoints);
	} else {
	  ecx2 += ecx1;
	  ecy2 += ecy1;
	  scx2 += scx1;
	  scy2 += scy1;
	  
	  int minc = 0;
	  int min  = startx - scx1;
	  int tmp  = scx2   - startx;
	  if(tmp < min) {min = tmp; minc = 1;}
	  tmp      = starty - scy1;
	  if(tmp < min) {min = tmp; minc = 2;}
	  tmp      = scy2 - starty;
	  if(tmp < min) {min = tmp; minc = 3;}
	  
	  switch(minc) {
	  case 0: xpoints[npoints] = scx1 - DINC * edge.startport.index;   ypoints[npoints++] = starty; break;
	  case 1: xpoints[npoints] = scx2 + DINC * edge.startport.index;   ypoints[npoints++] = starty; break;
	  case 2: xpoints[npoints] = startx; ypoints[npoints++] = scy1 - DINC * edge.startport.index;   break;
	  case 3: xpoints[npoints] = startx; ypoints[npoints++] = scy2 + DINC * edge.startport.index;   break;
	  }
	  
	  minc = 0;
	  min  = endx - ecx1;
	  tmp  = ecx2 - endx;
	  if(tmp < min) {min = tmp; minc = 1;}
	  tmp      = endy - ecy1;
	  if(tmp < min) {min = tmp; minc = 2;}
	  tmp      = ecy2 - endy;
	  if(tmp < min) {min = tmp; minc = 3;}
	  
	  switch(minc) {
	  case 0: 
	    ecx1 -= DINC * edge.endport.index;
	    hconnect(ecx1, endy); 
	    hpoly = new Polygon(xpoints, ypoints, npoints);
	    npoints = 2;
	    vconnect(ecx1, endy);
	    vpoly = new Polygon(xpoints, ypoints, npoints);
	    break;
	  case 1:
	    ecx2 += DINC * edge.endport.index;
	    hconnect(ecx2, endy);
	    hpoly = new Polygon(xpoints, ypoints, npoints);
	    npoints = 2;
	    vconnect(ecx2, endy);
	    vpoly = new Polygon(xpoints, ypoints, npoints);
	    break;
	  case 2:
	    ecy1 -= DINC * edge.endport.index;
	    hconnect(endx, ecy1); 
	    hpoly = new Polygon(xpoints, ypoints, npoints);
	    npoints = 2;
	    vconnect(endx, ecy1);
	    vpoly = new Polygon(xpoints, ypoints, npoints);
	    break;
	  case 3:
	    ecy2 += DINC * edge.endport.index;
	    hconnect(endx, ecy2);
	    hpoly = new Polygon(xpoints, ypoints, npoints);
	    npoints = 2;
	    vconnect(endx, ecy2);
	    vpoly = new Polygon(xpoints, ypoints, npoints);
	    break;
	  }
	}
		
	edge.polyline = hpoly.npoints < vpoly.npoints ? hpoly : vpoly;
	
	if(endC || startC) {
	  exclude[eci] = endsv;
	  exclude[sci] = startsv;
	} else	  
	  edge.polyline.addPoint(endx, endy);
      }
    }

    for(int i = 0; i < edges.length; i++) {
      GraphEdge edge = edges[i];
      // setup markers
      if(edge.markers.length > 0) {
	int   ns  = edge.polyline.npoints;
	int[] xs  = edge.polyline.xpoints;
	int[] ys  = edge.polyline.ypoints;
	int   len = 0;
	for(int j = 1; j < ns; j++) {
	  int l = 
	    xs[j] - xs[j - 1] +
	    ys[j] - ys[j - 1];
	  if(l < 0) l = -l;
	  len += l;
	  cummlen[j - 1] = len;
	}
	if(ns < 3) {
	  for(int j = 0; j < edge.markers.length; j++)
	    GraphEdgeUtils.setupMTrans(edge, j, 0, 0, 0, 0, 0);
	} else {
	  ns--;
	  for(int j = 0; j < edge.markers.length; j++) {
	    int mp = (int)(edge.markerpos[j] * len);
	    int k = 0;
	    while(cummlen[k] < mp && k < ns) k++;
	    GraphEdgeUtils.setupMTrans(edge, j, Awt.getAngle(xs[k], ys[k], xs[k + 1], ys[k + 1]) + PI2, 
				       xs[k], ys[k], xs[k + 1], ys[k + 1]);
	  }  
	}
      }      
    }    
  }
}

/*
  $Log: OrthogonalRouter.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.7  2001/03/16 18:08:20  schubige
  improved orthogonal router

  Revision 1.6  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.5  2001/03/09 21:24:58  schubige
  Added preferences to edge editor

  Revision 1.4  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.3  2001/02/22 15:59:23  schubige
  Worked on FileReader soundlet

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

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.1  2000/07/28 12:07:58  schubige
  Graph stuff update
*/
