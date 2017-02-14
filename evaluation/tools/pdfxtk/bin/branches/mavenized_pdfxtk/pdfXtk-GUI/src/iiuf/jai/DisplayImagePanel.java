
package iiuf.jai;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.media.jai.PlanarImage;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Panel for displaying JAI images.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class DisplayImagePanel extends JScrollPane {

  protected DisplayImage image;

  private int lastMouseX;
  private int lastMouseY;

  public DisplayImagePanel() {
    addMouseMotionListener(new MouseMotionAdapter() {
	public void mouseDragged(MouseEvent e) {
	  if (0 != (e.getModifiers() & InputEvent.BUTTON2_MASK)) {
	    JScrollBar hb = getHorizontalScrollBar();
	    JScrollBar vb = getVerticalScrollBar();
	    
	    int x = e.getX();
	    int y = e.getY();
	    
	    hb.setValue(hb.getValue()+x - lastMouseX);
	    vb.setValue(vb.getValue()+y - lastMouseY);

	    lastMouseX = x;
	    lastMouseY = y;
	  }
	}
      });
    
    addMouseListener(new MouseAdapter() {
	public void mousePressed(MouseEvent e) {
	  if (0 != (e.getModifiers() & InputEvent.BUTTON2_MASK)) {
	    lastMouseX = e.getX();
	    lastMouseY = e.getY();
	  }
	}
      });
  }

  public DisplayImagePanel(PlanarImage image) {
    this();
    init(image);
  }

  protected void init(PlanarImage image) {
    setImage(image);
  }

  public void setImage(PlanarImage img) {
    image = new DisplayImage(img, true, true);
    JLabel imageLabel = new JLabel(image) {
	protected void paintChildren(Graphics graphics) {
	  super.paintChildren(graphics);

	  if (!(graphics instanceof Graphics2D)) {
	    throw new RuntimeException("DisplayImagePanel requires Graphics2D.");
	  }
	  Graphics2D g2d = (Graphics2D) graphics;
	  Rectangle clipBounds = g2d.getClipBounds();
	  
	  if (layers != null) {
	    ListIterator layerIter = layers.listIterator();
	    while (layerIter.hasNext()) {
	      DisplayImageLayer layer = (DisplayImageLayer) layerIter.next();
	      layer.paintLayer(g2d, clipBounds);
	    }
	  }
	}
      };

    imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
    imageLabel.setVerticalAlignment(SwingConstants.TOP);

    setViewportView(imageLabel);
  }

  public Rectangle getViewRect() {
    return getViewport().getViewRect();
  }

  public PlanarImage getImage() {
    return image.getImage();
  }

  protected ArrayList layers = null;
  
  public void addLayer(DisplayImageLayer layer) {
    if (layers == null) {
      layers = new ArrayList();
    }
    layers.add(layer);
  }
}
