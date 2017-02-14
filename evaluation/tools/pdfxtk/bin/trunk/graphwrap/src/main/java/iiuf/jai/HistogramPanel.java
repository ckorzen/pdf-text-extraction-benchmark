
package iiuf.jai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
   (c) 1999, IIUF<p>

   Display histogram in a panel object.

   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class HistogramPanel
  extends JPanel 
{
  /** Histogram to display */

  protected Histogram histogram;

  /** Number of bins */

  protected int maxBins;

  /** Size of the histogram panel */

  protected Dimension size;

  /** Colors of the bands */
  
  protected Color[] bandColor = { Color.red, 
				  Color.green, 
				  Color.blue, 
				  Color.black };

  /** Constructs the histogram panel object.

      @param histogram Histogram to display */

  public HistogramPanel(Histogram h) {
    this.histogram = h;
    initClass();

    addMouseMotionListener(new MouseMotionAdapter() {
	public void mouseMoved(MouseEvent e) {	    
	  int x = e.getX();
	  int bin = x*maxBins / size.width;
	  int[][] bins = histogram.getBins();
	  int numBands = bins.length;

	  String txt = Integer.toString(bins[0][bin]);
	  for (int i = 1; i < numBands; i++) {
	    txt = txt+","+bins[i][bin];
	  }

	  setToolTipText("Bin: "+bin+" ["+txt+"]");
	}
      });
  }

  /** Initializes the internal variables. */
  
  private void initClass() {
    int[] numBins = histogram.getNumBins();
    int numBands = numBins.length;
    maxBins = 0;
    for (int i = 0; i < numBands; i++) {
      maxBins = Math.max(maxBins, numBins[i]);
    }

    setMinimumSize(new Dimension(maxBins * numBands, 100));
    setPreferredSize(new Dimension(maxBins * numBands, 100));
  }

  /** Set the color of a band. */

  public void setBandColor(int b, Color c) {
    bandColor[b] = c;
  }

  /** Paints the histogram component.

      @param g graphics context */

  protected void paintComponent(Graphics g) {
    size = getSize(null);

    g.clearRect(0, 0, size.width, size.height);
    
    int[][] bins = histogram.getBins();
    int numBands = bins.length;

    int highValue = 0;
    for (int band = 0; band < numBands; band++) {
      for (int bin = 0; bin < bins[band].length; bin++) {
	highValue = Math.max(highValue, bins[band][bin]);
      }
    }

    for (int band = 0; band < numBands; band++) {
      g.setColor(bandColor[band]);
      for (int bin = 0; bin < bins[band].length; bin++) {
	int xpos = ((bin*numBands)+band) * size.width / (maxBins*numBands);
	int xpos2 = ((bin*numBands)+(band+1)) * size.width / (maxBins*numBands);
	int ysize = size.height * bins[band][bin] / highValue;

	g.fillRect(xpos, size.height-ysize, xpos2-xpos, ysize);
      }
    }
  }

  /** Main program

      @param argv argument (filename of image) */
  
  public static void main(String[] argv) {

    // Check arguments
    if (argv.length != 1) {
      System.err.println("Usage: java iiuf.jai.HistogramPanel filename");
      System.exit(1);
    }

    // Load image
    PlanarImage image = JAI.create("fileload", argv[0]);

    // Get the number of bands
    int bands = image.getSampleModel().getNumBands();

    // Create histogram object
    int[] numBins = new int[bands];
    double[] lowValue = new double[bands];
    double[] highValue = new double[bands];

    for (int band = 0; band < bands; band++) {
      numBins[band] = 256;
      lowValue[band] = 0.0;
      highValue[band] = 255.0;
    }

    Histogram histogram = new Histogram(numBins, lowValue, highValue);

    // Calculate histogram of image
    PlanarImage histImage = JAI.create("histogram", image, histogram);

    histogram = (Histogram) histImage.getProperty("histogram");

    JFrame window = new JFrame();
    
    window.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
	  e.getWindow().dispose();
	}
      });

    window.getContentPane().add(new HistogramPanel(histogram),
				BorderLayout.CENTER);
    window.pack();
    window.show();
  }

}
