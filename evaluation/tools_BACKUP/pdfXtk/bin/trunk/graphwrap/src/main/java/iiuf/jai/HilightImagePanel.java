
package iiuf.jai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JFrame;

/**
   (c) 2000, IIUF<p>

   Panel for hilighting parts of an image in a certain color.
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

class HilightImagePanel
  extends DisplayImagePanel
{
  /** Constructs a HilightImagePanel.

      @param image The image in which we want to hilight certain parts.
      @param mask A greyscale image that contains the parts of the image that
      are hilighted
      @param color The hilight color.
  */

  public HilightImagePanel(RenderedImage image, RenderedImage mask, Color color) {

    // Create a constant image using the supplied color

    ParameterBlock pb = new ParameterBlock();
    pb.add(new Float(image.getWidth()));
    pb.add(new Float(image.getHeight()));
    pb.add(new Byte[] { new Byte((byte) color.getRed()), 
			new Byte((byte) color.getGreen()), 
			new Byte((byte) color.getBlue()) });
    PlanarImage coloredImage = JAI.create("constant", pb);

    // Convert the input image to the same image layout
    // (required for the composite-operator)

    ImageLayout layout = new ImageLayout(coloredImage);

    pb = new ParameterBlock();
    pb.addSource(image);
    pb.add(DataBuffer.TYPE_BYTE);
    PlanarImage imageRGB = JAI.create("format", pb, 
				      new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));

    // Compose the two images according to the mask

    pb = new ParameterBlock();
    pb.addSource(imageRGB);
    pb.addSource(coloredImage);
    pb.add(mask);
    init(JAI.create("composite", pb));
  }

  /** Test program */

  public static void main(String[] arg) {
    PlanarImage image = JAI.create("fileload", arg[0]);
    PlanarImage mask  = JAI.create("fileload", arg[1]);

    JFrame window = new JFrame();

    window.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
	  System.exit(0);
	}
      });
    window.getContentPane().add(new HilightImagePanel(image, mask, Color.blue),
				BorderLayout.CENTER);
    window.pack();
    window.setSize(300, 300);
    window.setLocation(40, 40); 
    window.show();
  }
}
