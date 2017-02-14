/* (C) 2000-2002, DIUF, http://www.unifr.ch/diuf
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package at.ac.tuwien.dbai.pdfwrap.gui.displayable;

// the following added for debugging purposes only
import iiuf.util.Preferences;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Displayable;
import iiuf.xmillum.DisplayableClass;
import iiuf.xmillum.ImageListener;
import iiuf.xmillum.Parameter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Element;

/**
 * Image
 *
 * Represents an image shown by xmillum.
 *
 * <p>Optional initialization parameters:
 * <ul>
 *   <li>visible: 0/1 (default: 1)
 * </ul>
 *
 * 
 * @author DIUF, Fribourg, CH 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser GUI 0.9
 */
public class Image2 extends DisplayableClass {
  static Map parameters = new HashMap();

  static String IMAGE_DIRECTORY = "displayable.image.directory";

  static {
    parameters.put("visibility", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v) {
	  Image2 i = (Image2) o;
	  i.visible = trueFalse(v);
	}
      });
  }

  BrowserContext context;
  boolean visible = true;

  public void initialize(BrowserContext c, Element e) {
    context = c;
    Parameter.setParameters(context, e, this, parameters);
  }

  static String[][] filters = new String[][] {
    { ".jpg", "JPEG Images" },
    { ".gif", "GIF Images" },
    { ".png", "PNG Images" },
    { ".pnm", "PNM Images" },
    { ".pbm", "PBM Images" },
    { ".pgm", "PGM Images" },
    { ".tif", "TIFF Images" }
  };

  private class SuffixFilter extends FileFilter {
    String suffix, description;
    public SuffixFilter(String s, String d) {
      suffix = s;
      description = d;
    }
    public boolean accept(File f) {
      return f.isDirectory() || f.getName().endsWith(suffix);
    }
    public String getDescription() {
      return description;
    }
  }

  public Displayable getDisplayable(Element element) {
    String imageName = null;

    if (element.hasAttribute("asksrc")) {
      JFileChooser fc = new JFileChooser();
      fc.setCurrentDirectory(new File((String) Preferences.get(IMAGE_DIRECTORY, "")));

      for (int i = 0; i < filters.length; i++) {
	fc.addChoosableFileFilter(new SuffixFilter(filters[i][0], filters[i][1]));
      }
      
      if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)) {
	Preferences.set(IMAGE_DIRECTORY, fc.getCurrentDirectory().getAbsolutePath());
	imageName = fc.getSelectedFile().getPath();
      }
    } else {
      imageName = element.getAttribute("src");
    }

    if (imageName == null || imageName.equals("")) {
      return null;
    }

    try {
      URL imageURL;

      if (context.getDocument().getBaseURL() != null) {
	imageURL = new URL(context.getDocument().getBaseURL(), imageName);
      } else {
	imageURL = new URL(imageName);
      }

      int x = 0; int y = 0;
      try {
	if (element.hasAttribute("x")) x = Integer.parseInt(element.getAttribute("x"));
	if (element.hasAttribute("y")) y = Integer.parseInt(element.getAttribute("y"));
      } catch (NumberFormatException e) {
      }
      Displayable d = new DisplayableImage(element, imageURL, new Point(x, y));

      // Recursively build tree
      d.childs = getChilds(element, context);
      return d;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  class DisplayableImage extends Displayable implements ImageListener {
    URL imageURL;
    boolean visible;
    iiuf.xmillum.Image image;

    public DisplayableImage(Element element, URL url, Point location) {
      super(element);

      visible = Image2.this.visible;
      if (element.hasAttribute("visible")) {
	if (element.getAttribute("visible").equals("0")) visible = false;
      }

      imageURL   = url;

      try {
	image = context.getImageFactory().getImage(imageURL);
	image.addImageListener(this);
	bounds = new Rectangle(location.x, location.y, image.getWidth(), image.getHeight());
      } catch (IOException e) {
	e.printStackTrace();
      }

      if (image == null) {
	bounds = new Rectangle(location);
      } else {
	bounds = new Rectangle(location.x, location.y, image.getWidth(), image.getHeight());
      }
    }

    public Rectangle getBounds(double scale) {
      return new Rectangle((int) (bounds.x * scale),
			   (int) (bounds.y * scale),
			   (int) (bounds.width * scale), 
			   (int) (bounds.height * scale));
    }

    public void paintObject(Graphics2D g, double scale) {
      g.setBackground(new Color(0.5f, 0.5f, 0.5f));
      Rectangle b = getBounds(scale);
      //System.out.println("Painting image");
      //if (visible && image != null) System.out.println("vis & not null");
      if (visible && image != null) image.paintImage(scale, g, b.x, b.y);
    }

    // ImageListener

    public void imageChanged(iiuf.xmillum.Image i) {
      context.refresh();
    }
  }
}
