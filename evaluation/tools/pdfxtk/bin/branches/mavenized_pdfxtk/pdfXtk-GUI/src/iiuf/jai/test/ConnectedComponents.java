
package iiuf.jai.test;

import iiuf.jai.*;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JFrame;

/**
   (c) 2000, 2001, IIUF, DIUF<p>

   Test program for connected components
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class ConnectedComponents {
  public static void main(String[] arg) {
    // Register custom operators
    BlackOrDescriptor.register();
    CCDescriptor.register();

    if (arg.length != 1) {
      System.err.println("Usage: iiuf.jai.test.ConnectedComponents <image>");
      System.exit(0);
    }

    new ConnectedComponents(arg[0]);
  }

  public ConnectedComponents(String filename) {
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

    // Find 4-connected components
    pb = new ParameterBlock();
    pb.addSource(binimg);
    PlanarImage ccimage = JAI.create("cc", pb);

    ArrayList rectList = new ArrayList();
    Iterator cciter = ((ArrayList) ccimage.getProperty("cc4")).iterator();
    while (cciter.hasNext()) {
      rectList.add(new MyDisplayRect((Rectangle) cciter.next()));
    }

    JFrame window = new JFrame();
    
    window.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

    DisplayImagePanel imagePanel = new DisplayImagePanel(orig);
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
      Composite transparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4F);
      Composite c = g.getComposite();
      g.setComposite(transparent);

      g.setColor(Color.blue);
      g.fillRect(x, y, width, height);
    }
  }
}
