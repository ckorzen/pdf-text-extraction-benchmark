
package iiuf.jai.test;

import iiuf.jai.*;

import java.awt.BorderLayout;
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

   Test program for projection profiles
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class ProjectionProfiles {
  public static void main(String[] arg) {
    // Register custom operators
    BlackOrDescriptor.register();
    SkeletonDescriptor.register();
    CCDescriptor.register();
    RLSADescriptor.register();
    BinarizeDescriptor.register();
    ProjectionProfileDescriptor.register();

    if (arg.length != 1) {
      System.err.println("Usage: iiuf.jai.test.ProjectionProfiles <image>");
      System.exit(0);
    }

    new ProjectionProfiles(arg[0]);
  }

  public ProjectionProfiles(String filename) {
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

    // Apply RLSA
    pb = new ParameterBlock();
    pb.addSource(binimg);
    pb.add(new Integer(RLSAOpImage.DIRECTION_H));
    pb.add(new Integer(20));
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

    ArrayList rectList = new ArrayList();
    Iterator cciter = cclist.iterator();
    while (cciter.hasNext()) {
      Rectangle r = (Rectangle) cciter.next();
      PlanarImage profile = JAI.create("projectionprofile", binimg, r);
      ProjectionProfile vp = (ProjectionProfile) profile.getProperty("vertical");

      rectList.add(new MyDisplayRect(r, vp));
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
    public int ascent;
    public int baseline;
    public ProjectionProfile profile;

    public MyDisplayRect(Rectangle r, ProjectionProfile profile) {
      super(r);
      this.profile = profile;
      ascent = profile.findAscent();
      baseline = profile.findBaseline();
    }
    
    public void paintObject(Graphics2D g) {
      g.drawLine(x, y+ascent, x+width, y+ascent);
      g.drawLine(x, y+baseline, x+width, y+baseline);

      g.setXORMode(java.awt.Color.white);
      profile.paint(g, ProjectionProfile.VERTICAL, x, y);
      g.setPaintMode();
    }
  }
}

