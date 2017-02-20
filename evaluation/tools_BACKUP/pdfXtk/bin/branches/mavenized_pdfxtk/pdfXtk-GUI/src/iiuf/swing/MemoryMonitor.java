package iiuf.swing;

import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JProgressBar;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import iiuf.util.Util;
import iiuf.util.StopWatch;

/**
   A memory monitor factory.
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class MemoryMonitor {
  public static final int BAR   = 0;
  public static final int CHART = 1;
  
  private static int              delay       = 2000;
  private static int              maxListSize = 10;
  private static DefaultListModel model       = new DefaultListModel();
  private static Thread           runner;
  private static final double     MEGA   = 1024 * 1024;
  private static DecimalFormat    memfmt = new DecimalFormat("##########.##");
  private static long             lastUse0 = -1;
  private static long             lastUse1 = -1;
  private static long             lowMark;
  private static StopWatch        stopWatch = new StopWatch().start();
  private static Font             FONT      = new Font("SansSerif", Font.PLAIN, 9);

  public static JComponent newMemoryMonitor(int type) {
    memfmt.setMaximumFractionDigits(2); 
    if(runner == null) {
      runner = new Thread("Memory Monitor") {
	  public void run() {
	    for(;;) {
	      long total   = Runtime.getRuntime().totalMemory();
	      long use     = total - Runtime.getRuntime().freeMemory();
	      if(lastUse0 == -1) {
		lastUse0 = use;
		lastUse1 = use;
		lowMark  = use;
	      }
	      else {
		if(lastUse1 < use && lastUse0 > lastUse1)
		  lowMark = lastUse1;
		lastUse0 = lastUse1;
		lastUse1 = use;
	      }
	      model.addElement(new long[] {stopWatch.getMillis(), total, use, lowMark});
	      if(model.getSize() > maxListSize)
		model.remove(0);
	      Util.delay(delay);
	    }
	  }
	};
      runner.start();
    }
    switch(type) {
    case BAR:
      return new MemoryBar(model);
    case CHART:
      ChartPanel cp = new ChartPanel(model);
      cp.setLabels(new Labels());
      cp.setPointsVisible(false);
      cp.setZeroVisible(true);
      cp.setLastPointsVisible(true);
      cp.setXLabelIndex(0);
      cp.setFont(FONT);
      return cp;
    default:
      throw new IllegalArgumentException("Illegal type:" + type);
    }
  }
  
  static class Labels extends ChartPanel.Labels {  
    String getYLabel(double val) {
      return memfmt.format(val / MEGA) + "MB";
    }
    
    String getXLabel(double val) {
      return (long)(val / 1000) + " s";
    }
    
    String getPointLabel(double val) {
      return getYLabel(val);
    }
  }

  static class MemoryBar
    extends
    JProgressBar
    implements
    ListDataListener
  {    
    public MemoryBar(ListModel model) {
      super(0, (int)Runtime.getRuntime().totalMemory());
      setString("Memory in use");
      setStringPainted(true);
      model.addListDataListener(this);
    } 

    public void contentsChanged(ListDataEvent e) {
      update();
    }
  
    public void intervalAdded(ListDataEvent e) {
      update();
    }
    
    public void intervalRemoved(ListDataEvent e) {}
    
    void update() {
      long[] data  = (long[])MemoryMonitor.model.getElementAt(MemoryMonitor.model.getSize() - 1);
      long   total = data[1];
      long   use   = data[2];

      setString("Memory in use: " + (int)(getPercentComplete() * 100) + "% (" + 
		memfmt.format((double)use   / MEGA) + " of " + 
		memfmt.format((double)total / MEGA) + " MB)");
      setMaximum((int)total);
      setValue((int)use);
    }
  }
}

/*
  $Log: MemoryMonitor.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.6  2001/01/17 09:55:46  schubige
  Logger update

  Revision 1.5  2001/01/15 15:08:58  schubige
  some sourcewatch bug fixes

  Revision 1.4  2001/01/14 13:21:13  schubige
  Win NT update

  Revision 1.3  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.2  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/09 07:53:08  schubige
  early checkin for DCJava
  
*/
  
