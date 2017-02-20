/* ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: ProjectionProfileOpImage.java,v $
 * SUPPORT:	$Author: hassan $
 * CREATION:	$Date: 2006/05/17 10:22:24 $
 * VERSION:	$Revision: 1.1 $
 * OVERVIEW:	Implementation of a project profile detection operator.
 * ------------------------------------------------------------------------ */

package iiuf.jai;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.Rectangle;

import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import javax.media.jai.ROIShape;
import javax.media.jai.StatisticsOpImage;

/* ------------------------------------------------------------------------ */
public class ProjectionProfileOpImage
  extends StatisticsOpImage {
  
  protected RenderedImage image;
  protected RasterAccessor raster;
  protected Rectangle region;

  // Set to true if you want debugging output 
  private DirectRasterAccessor srcDRA;
  /* ---------------------------------------------------------------------- */  
  public ProjectionProfileOpImage(RenderedImage source,
			       Rectangle region) {
    super(source, new ROIShape(new Rectangle(source.getMinX(), source.getMinY(), 
					     source.getWidth(), source.getHeight())),
	  source.getMinX(), source.getMinY(),
	  1, 1);
    
    this.region = region;
    image = source;
    
    RasterFormatTag[] formatTags = getFormatTags();
    
    srcDRA = new DirectRasterAccessor(getData(), getColorModel());
  }
  /* ---------------------------------------------------------------------- */
  private static final String[] statisticsNames = {
    "vertical",
    "horizontal"
  };
  /*------------------------------------------------------------------------*/
  public String[] getStatisticsNames() {
    return statisticsNames;
  }
  /*------------------------------------------------------------------------*/
  private int[] horizProjectionProfile() {
    int x,y;
    int array[] = new int[region.width];
    int black = srcDRA.black;
    
    for (x=region.x;x<(region.x+region.width);x++) {
      array[x-region.x]=0;
      for (y=region.y;y<region.y+region.height;y++) {
	if (srcDRA.getPixel(x, y) == black) {
	  array[x-region.x]++;
	}
      }
    }
    return (array);
  }
  /*------------------------------------------------------------------------*/
  private int[] vertProjectionProfile() {
    int x,y;
    int array[] = new int[region.height];
    int black = srcDRA.black;

    for (y=region.y;y<region.y+region.height;y++) {
      array[y-region.y]=0;
      for (x=region.x;x<region.x+region.width;x++) {
	if (srcDRA.getPixel(x, y) == black) {
	  array[y-region.y]++;
	}
      }
    }
    return (array);
  }
  /*------------------------------------------------------------------------*/
  public Object createStatistics(String name) {
    if (name.equals("vertical")) {
      return new ProjectionProfile(vertProjectionProfile());
    } else if (name.equals("horizontal")) {
      return new ProjectionProfile(horizProjectionProfile());
    } else {
      throw new RuntimeException(this.getClass().getName() + 
				 " statistics " + name + 
				 " not supported");	
    }
  }
  /*------------------------------------------------------------------------*/
  public void accumulateStatistics(String name, Raster source, Object stats) {
  }
  /*------------------------------------------------------------------------*/
}
/* ------------------------------------------------------------------------ */
