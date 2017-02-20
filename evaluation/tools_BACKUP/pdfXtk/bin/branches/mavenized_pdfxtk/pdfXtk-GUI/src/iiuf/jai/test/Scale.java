
package iiuf.jai.test;

import iiuf.jai.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Iterator;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JFrame;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Test program for the scale operator.
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class Scale {
  public static void main(String[] arg) {
    // Register custom operators
    BlackOrDescriptor.register();
    CCDescriptor.register();

    if (arg.length != 1) {
      System.err.println("Usage: iiuf.jai.test.Scale <image>");
      System.exit(0);
    }

    new Scale(arg[0]);
  }

  public Scale(String filename) {
    ParameterBlock pb;

    // Load file
    PlanarImage orig = JAI.create("fileload", filename);
    PlanarImage binimg;

    if (Util.isBinary(orig)) {
      binimg = orig;
    } else {
      // Binarize
      pb = new ParameterBlock();
      pb.addSource(orig);
      pb.add(new Integer(160));
      binimg = JAI.create("binarize", pb);
    }

    // Scale image

    pb = new ParameterBlock();
    pb.addSource(binimg);
    pb.add(new Float(0.1F));
    pb.add(new Float(0.1F));
    binimg = JAI.create("scale", pb);

    // Find 4-connected components
    pb = new ParameterBlock();
    pb.addSource(binimg);
    PlanarImage ccimage = JAI.create("cc", pb);
    ArrayList cclist = (ArrayList) ccimage.getProperty("cc4");

    ArrayList rectList = new ArrayList();
    Iterator cciter = cclist.iterator();
    while (cciter.hasNext()) {
      Rectangle r = (Rectangle) cciter.next();

      //
      //
      //

      rectList.add(new MyDisplayRect(r));
    }

    JFrame window = new JFrame();
    
    window.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

    DisplayImagePanel imagePanel = new DisplayImagePanel(binimg);
    imagePanel.addLayer(new RectDisplayImageLayer(rectList, imagePanel));

    window.getContentPane().add(imagePanel, BorderLayout.CENTER);
    window.pack();
    window.setSize(320, 250);
    window.setLocation(50, 50);
    window.show();
  }

  private class MyDisplayRect
    extends DisplayRect
  {
    public MyDisplayRect(Rectangle r) {
      super(r);
    }
    
    public void paintObject(Graphics2D g) {
      if ((height <= 10) && (width <= 10))  {
	g.setColor(Color.red);
	g.drawRect(x, y, width, height);
	return;
      }

      g.setColor(Color.blue);
      g.drawRect(x, y, width, height);
    }
  }
}
