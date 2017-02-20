
package iiuf.jai;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;

import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import javax.media.jai.ROIShape;
import javax.media.jai.StatisticsOpImage;

/**
   (c) 1999, IIUF<p>

   This is the implementation of the Connected Components extraction 
   algorithm for the Java Advanced Imaging API. This operator does only
   work with binary images satisfying certain criteria.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class CCOpImage
  extends StatisticsOpImage 
{
  /** Inner class representing a sort of "hierarchical rectangles" where every
      rectangle has a parent. */

  private class HRectangle extends Rectangle {
    public int   parent;
    public Point member;

    public HRectangle(int x, int y, int p) {
      super(x, y, 1, 1);
      member = new Point(x, y);
      parent = p;
    }
  }

  /** Constructs a CCOpImage object */

  public CCOpImage(RenderedImage source, Rectangle sample) {
    super(source, new ROIShape(new Rectangle(source.getMinX(), source.getMinY(), 
					     source.getWidth(), source.getHeight())),
	  source.getMinX(), source.getMinY(),
	  1, 1);

    sampleRectangle = sample;
    image = source;
  }

  /** A sample rectangle which is cloned for returning the results */

  protected Rectangle sampleRectangle;

  /** The candidate image */

  protected RenderedImage image;

  /** An object for accessing the image */

  protected DirectRasterAccessor raster;

  /** Name of the two available operations provided by this operator */

  private static final String[] statisticsNames = {
    "CC4", "CC8", "CC4points", "CC8points"
  };

  /** Returns the names of the two operations */

  public String[] getStatisticsNames() {
    return statisticsNames;
  }

  /** Apply one of the two operations */

  public Object createStatistics(String name) {
    raster = new DirectRasterAccessor(getData(), getColorModel());

    name = name.toLowerCase();

    if (name.equals("cc4")) {
      return makeRectangles(CC4());
    } else if (name.equals("cc4points")) {
      return makePoints(CC8());
    } else if (name.equals("cc8")) {
      return makeRectangles(CC8());
    } else if (name.equals("cc8points")) {
      return makePoints(CC8());
    } else {
      throw new RuntimeException(getClass().getName() + 
				 " statistics " + name + 
				 " not supported");	
    }
  }

  /** Not supported */

  public void accumulateStatistics(String name, Raster source, Object stats) {
  }

  /** 4-neighbors connected components (rectangles) */

  List makeRectangles(List zones) {
    List ccs   = new ArrayList();

    /* If the user did not provide a prototype rectangle to clone, 
       create new rectangles, which is faster than cloning a existing
       rectangle. */

    if (sampleRectangle == null) {
      for (int i = 1; i < zones.size(); i++) {
	HRectangle hr = (HRectangle) zones.get(i);
	if (hr.parent == i) {
	  ccs.add(new Rectangle(hr.x, hr.y, hr.width, hr.height));
	}
      }
    } else {
      for (int i = 1; i < zones.size(); i++) {
	HRectangle hr = (HRectangle) zones.get(i);
	if (hr.parent == i) {
	  Rectangle r = (Rectangle) sampleRectangle.clone();
	  r.setBounds(hr.x, hr.y, hr.width, hr.height);
	  ccs.add(r);
	}
      }
    }
    return ccs;
  }

  List makePoints(List zones) {
    List points = new ArrayList();
    for (int i = 1; i < zones.size(); i++) {
      HRectangle hr = (HRectangle) zones.get(i);
      if (hr.parent == i) {
	points.add(hr.member);
      }
    }
    return points;
  }

  private int[] scanline;
  private int[] lastScanline;
  private List zones;

  /** Creates a new zone containing the pixel at (x, y) */

  private void newZone(int x, int y) {
    scanline[x] = zones.size();
    zones.add(new HRectangle(x, y, zones.size()));
  }

  /** Gets the number of the zone which contains the pixel at (x,currentLine) */

  private int getZone(int x) {
    int idx = scanline[x];
    while (idx != ((HRectangle) zones.get(idx)).parent) {
      idx = ((HRectangle) zones.get(idx)).parent;
    }
    return idx;
  }

  private int getLastZone(int x) {
    int idx = lastScanline[x];
    while (idx != ((HRectangle) zones.get(idx)).parent) {
      idx = ((HRectangle) zones.get(idx)).parent;
    }
    return idx;
  }

  /** Fusion the two zones i and j */

  private void fusionZones(int i, int j) {
    if (i == j) {
      return;
    }

    HRectangle zi = (HRectangle) zones.get(i);
    HRectangle zj = (HRectangle) zones.get(j);

    if (zi.parent > zj.parent) {
      int tmp = j;
      j = i;
      i = tmp;

      HRectangle tmpz = zj;
      zj = zi;
      zi = tmpz;
    }

    zj.parent = zi.parent;

    int newx1 = Math.min(zi.x, zj.x);
    int newy1 = Math.min(zi.y, zj.y);
    int newx2 = Math.max(zi.x+zi.width, zj.x+zj.width);
    int newy2 = Math.max(zi.y+zi.height, zj.y+zj.height);

    zi.x = newx1;
    zi.y = newy1;
    zi.width = newx2-newx1;
    zi.height = newy2-newy1;
  }

  /** 8-neighbors connected components */

  List cc8cache;

  List CC8() { 
    if (cc8cache != null) {
      return cc8cache;
    }

    int width = getWidth();
    int height = getHeight();

    zones = new ArrayList();
    scanline = new int[width];
    lastScanline = new int[width];
    newZone(0, 0);

    int x, y;

    int black = raster.black;

    // 1st column on 1st line
    if (raster.getPixel(0, 0) == black) {
      newZone(0, 0);
    } else {
      scanline[0] = 0;
    }

    // other columns on 1st line
    for (x = 1; x < width; x++) {
      if (raster.getPixel(x, 0) == black) {
	if (getZone(x-1) == 0) {
	  newZone(x, 0);
	} else {
	  scanline[x] = getZone(x-1);
	  ((HRectangle) zones.get(getZone(x))).width++;
	}
      } else {
	scanline[x] = 0;
      }
    }

    for (y = 1; y < height; y++) {
      // save the last scanline
      System.arraycopy(scanline, 0, lastScanline, 0, scanline.length);

      // 1st column
      if (raster.getPixel(0, y) == black) {
	if (getZone(0) == 0) {
	  newZone(0, y);
	} else {
	  scanline[0] = getZone(0);
	  ((HRectangle) zones.get(getZone(0))).height++;
	}
      } else {
	scanline[0] = 0;
      }

      // other columns
      for (x = 1; x < width; x++) {
	if (raster.getPixel(x, y) == black) {
	  int zx1 = getZone(x-1);
	  int zx2 = getZone(x);

	  if (zx1 == 0 && zx2 == 0) {
	    if (lastScanline[x-1] != 0) {
	      scanline[x] = getLastZone(x-1);

	      HRectangle hr = (HRectangle) zones.get(getZone(x));

	      if (x >= hr.x + hr.width) {
		hr.width++;
	      }
	      if (y >= hr.y + hr.height) {
		hr.height++;
	      }
	    } else {
	      newZone(x, y);
	    }
	  } else if (zx1 != 0 && zx2 == 0) {
	    scanline[x] = zx1;

	    HRectangle hr = (HRectangle) zones.get(zx1);

	    if (x >= hr.x + hr.width) {
	      hr.width++;
	    }
	  } else if (zx2 != 0) {
	    scanline[x] = zx2;

	    HRectangle hr = (HRectangle) zones.get(zx2);

	    if (y >= hr.y + hr.height) {
	      hr.height++;
	    }

	    if (zx1 != 0) {
	      fusionZones(zx1, zx2);
	    }
	  }
	} else {
	  scanline[x] = 0;

	  if (scanline[x-1] != 0 && lastScanline[x] != 0) {
	    fusionZones(getZone(x-1), getLastZone(x));
	  }
	}
      }
    }
    cc8cache = zones;
    return zones;
  }
  
  List cc4cache;

  List CC4() {
    if (cc4cache != null) {
      return cc4cache;
    }

    int width = getWidth();
    int height = getHeight();

    zones = new ArrayList();
    scanline = new int[width];
    newZone(0, 0);

    int x, y;

    int black = raster.black;

    // 1st column on 1st line
    if (raster.getPixel(0, 0) == black) {
      newZone(0, 0);
    } else {
      scanline[0] = 0;
    }

    // other columns on 1st line
    for (x = 1; x < width; x++) {
      if (raster.getPixel(x, 0) == black) {
	if (getZone(x-1) == 0) {
	  newZone(x, 0);
	} else {
	  scanline[x] = getZone(x-1);
	  ((HRectangle) zones.get(getZone(x))).width++;
	}
      } else {
	scanline[x] = 0;
      }
    }

    for (y = 1; y < height; y++) {
      // 1st column
      if (raster.getPixel(0, y) == black) {
	if (getZone(0) == 0) {
	  newZone(0, y);
	} else {
	  scanline[0] = getZone(0);
	  ((HRectangle) zones.get(getZone(0))).height++;
	}
      } else {
	scanline[0] = 0;
      }

      // other columns
      for (x = 1; x < width; x++) {
	if (raster.getPixel(x, y) == black) {
	  int zx1 = getZone(x-1);
	  int zx2 = getZone(x);

	  if (zx1 == 0 && zx2 == 0) {
	    newZone(x, y);
	  } else if (zx1 != 0 && zx2 == 0) {
	    scanline[x] = zx1;

	    HRectangle hr = (HRectangle) zones.get(zx1);

	    if (x >= hr.x + hr.width) {
	      hr.width++;
	    }
	  } else if (zx2 != 0) {
	    scanline[x] = zx2;

	    HRectangle hr = (HRectangle) zones.get(zx2);

	    if (y >= hr.y + hr.height) {
	      hr.height++;
	    }

	    if (zx1 != 0) {
	      fusionZones(zx1, zx2);
	    }
	  }
	} else {
	  scanline[x] = 0;
	}
      }
    }
    cc4cache = zones;
    return zones;
  }
}
