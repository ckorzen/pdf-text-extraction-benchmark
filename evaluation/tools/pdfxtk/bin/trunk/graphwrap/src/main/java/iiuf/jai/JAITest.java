
package iiuf.jai;

import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JFrame;

/**
   (c) 1999, IIUF<p>

   Test program for the IIUF JAI extensions
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class JAITest {
  public static void main(String[] argv) {
    ParameterBlock pb;

    // Register custom operators
    BlackOrDescriptor.register();
    SkeletonDescriptor.register();
    CCDescriptor.register();
    RLSADescriptor.register();
    BinarizeDescriptor.register();

    if (argv.length != 1) {
      System.err.println("Usage: iiuf.jai.JAITest <image>");
      System.exit(0);
    }

    // Load file
    PlanarImage orig = JAI.create("fileload", argv[0]);
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

    // Apply RLSA
    pb = new ParameterBlock();
    pb.addSource(binimg);
    pb.add(new Integer(RLSAOpImage.DIRECTION_H));
    pb.add(new Integer(6));
    PlanarImage rlsah = JAI.create("rlsa", pb);

    pb = new ParameterBlock();
    pb.addSource(binimg);
    pb.add(new Integer(RLSAOpImage.DIRECTION_V));
    pb.add(new Integer(4));
    PlanarImage rlsav = JAI.create("rlsa", pb);

    pb = new ParameterBlock();
    pb.addSource(rlsah);
    pb.addSource(rlsav);
    PlanarImage rlsa = JAI.create("blackor", pb);

    // Find 4-connected components
    pb = new ParameterBlock();
    pb.addSource(rlsa);
    PlanarImage ccimage = JAI.create("cc", pb);
    ArrayList cclist = (ArrayList) ccimage.getProperty("cc4");

    JFrame window = new JFrame();
    
    window.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

    DisplayImagePanel imagePanel = new DisplayImagePanel(orig);
    imagePanel.addLayer(new RectDisplayImageLayer(cclist, imagePanel));

    window.getContentPane().add(imagePanel, BorderLayout.CENTER);
    window.pack();
    window.setSize(320, 250);
    window.setLocation(50, 50);
    window.show();
  }
}
