package iiuf.jai;

import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Iterator;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   ImageViewer
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class ImageViewer {
  public static void main(String[] arg) {
    if (arg.length != 1) {
      System.err.println("Usage: iiuf.jai.ImageViewer <image>");
      System.exit(0);
    }
    new ImageViewer(JAI.create("fileload", arg[0]));
  }

  protected PlanarImage image;

  public ImageViewer(PlanarImage img) {
    image = img;

    JFrame window = new JFrame();
    window.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });
    final DisplayImagePanel imagePanel = new DisplayImagePanel(image);
    window.getContentPane().add(imagePanel, BorderLayout.CENTER);

    JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
    slider.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  JSlider s = (JSlider) e.getSource();
	  if (!s.getValueIsAdjusting()) {
	    float scale = s.getValue() / 100F;

	    ParameterBlock pb = new ParameterBlock();
	    pb.addSource(image);
	    pb.add(new Float(scale));
	    pb.add(new Float(scale));
	    if (Util.isBinary(image)) {
	      ComponentColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 8 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	      SampleModel sm = cm.createCompatibleSampleModel(256, 256);
	      ImageLayout layout = new ImageLayout(0, 0, 256, 256, sm, cm);
	      layout.setTileWidth(256);
	      layout.setTileHeight(256);
	      RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
	      imagePanel.setImage(JAI.create("subsamplebinarytogray", pb, hints));
	    } else {
	      imagePanel.setImage(JAI.create("scale", pb));
	    }
	  }
	}
      });

    window.getContentPane().add(slider, BorderLayout.EAST);

    window.pack();
    window.setSize(320, 250);
    window.setLocation(50, 50);
    window.show();
  }
}
