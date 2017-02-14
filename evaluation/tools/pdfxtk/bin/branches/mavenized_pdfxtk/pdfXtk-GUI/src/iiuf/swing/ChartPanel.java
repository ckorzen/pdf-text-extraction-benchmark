package iiuf.swing;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FontMetrics;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import iiuf.util.Util;

/**
   Simple charting panel.

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ChartPanel
  extends
  JPanel
  implements
  ListDataListener
{
  private ListModel model;
  private Dimension prefSize = new Dimension(100, 100);
  private Color[]   colors;
  private Labels    labels = new Labels();
  
  public ChartPanel(ListModel model_) {
    model = model_;
    setColors(new Color[] {
      Color.black,
      Color.green, 
      Color.blue,
      Color.yellow, 
      Color.gray, 
      Color.lightGray, 
      Color.magenta, 
      Color.orange, 
      Color.pink, 
      Color.red, 
      Color.white, 
      Color.cyan, 
      Color.darkGray,
    });
    model.addListDataListener(this);
  }
  
  public void contentsChanged(ListDataEvent e) {
    update();
  }
  
  public void intervalAdded(ListDataEvent e) {
    update();
  }
  
  public void intervalRemoved(ListDataEvent e) {
    update();
  }

  public Dimension getMinimalSize() {
    return getPreferredSize();
  }

  public Dimension getPreferredSize() {
    return prefSize;
  }
  
  public void setLabels(Labels l) {
    labels = l;
  }
  
  private boolean showPoints;
  public void setPointsVisible(boolean show) {
    showPoints = show;
    repaint();
  }
  
  private boolean showZero;
  public void setZeroVisible(boolean show) {
    showZero = show;
    repaint();
  }

  private boolean showLastPoints;
  public void setLastPointsVisible(boolean show) {
    showLastPoints = show;
    repaint();
  }
  
  private int xLabelIndex = -1;
  public void setXLabelIndex(int idx) {
    xLabelIndex = idx;
    repaint();
  }

  private int        size   = 0;
  private double     maxVal = Double.MIN_VALUE;
  private double     minVal = Double.MAX_VALUE;
  private int        valCnt = 0;
  private double[][] data   = new double[0][0];
    
  private double[] normalize(Object o) {
    if(o instanceof Number)
    return new double[] {((Number)o).doubleValue()};
    else if(o instanceof Boolean)
    return new double[] {((Boolean)o).booleanValue() ? 1.0 : 0.0};
    else if(o instanceof boolean[]) {
      boolean[] n      = (boolean[])o;
      double[]  result = new double[n.length];
      for(int i = 0; i < result.length; i++)
	result[i] = n[i] ? 1.0 : 0;
      return result;
    }   
    else if(o instanceof byte[]) {
      byte[]    n      = (byte[])o;
      double[]  result = new double[n.length];
      for(int i = 0; i < result.length; i++)
      result[i] = n[i];
      return result;
    }   
    else if(o instanceof short[]) {
      short[]   n      = (short[])o;
      double[]  result = new double[n.length];
      for(int i = 0; i < result.length; i++)
      result[i] = n[i];
      return result;
    }   
    else if(o instanceof int[]) {
      int[]     n      = (int[])o;
      double[]  result = new double[n.length];
      for(int i = 0; i < result.length; i++)
      result[i] = n[i];
      return result;
    }   
    else if(o instanceof long[]) {
      long[]    n      = (long[])o;
      double[]  result = new double[n.length];
      for(int i = 0; i < result.length; i++)
      result[i] = n[i];
      return result;
    }   
    else if(o instanceof float[]) {
      float[]   n      = (float[])o;
      double[]  result = new double[n.length];
      for(int i = 0; i < result.length; i++)
      result[i] = n[i];
      return result;
    }   
    else if(o instanceof double[])
    return (double[])o;
    else
    return new double[0];
  }

  private synchronized void update() {
    size = model.getSize(); 
    data = new double[size][];
    for(int i = 0; i < size; i++) {
      data[i] = normalize(model.getElementAt(i));
      maxVal  = Math.max(maxVal, Util.max(data[i]));
      minVal  = Math.min(minVal, Util.min(data[i]));
      valCnt  = Math.max(valCnt, data[i].length);
    }
    if(valCnt > colors.length) {
      Color[] tmp = colors;
      colors = new Color[valCnt];
      System.arraycopy(tmp, 0, colors, 0, tmp.length);
      for(int i = tmp.length; i < colors.length; i++)
	colors[i] = new Color(Util.intRandom(0xCFFFFF) + 0x400000);
    }
    repaint();
  }
  
  private static final int YOFF       = 15;
  private static final int LABEL_MARK = 5;
  
  public synchronized void paint(Graphics g) {
    if(size < 2) return;
    int    width  = getWidth();
    double height = getHeight();

    FontMetrics fm = g.getFontMetrics();
    
    int XOFF = LABEL_MARK + 2 + Math.max(fm.stringWidth(labels.getYLabel(maxVal)),
					 fm.stringWidth(labels.getYLabel(minVal)));
    
    if(showZero)
      minVal = Math.min(0, minVal);
  
    g.setColor(getBackground());
    g.fillRect(getX(), getY(), width, (int)height);

    g.translate(0, YOFF);
    width  -= XOFF;
    height -= YOFF + LABEL_MARK + fm.getHeight();
    
    double dHeight = maxVal - minVal;
    
    int[] v0 = new int[valCnt];
    int[] v1 = new int[valCnt];
    for(int j = 0; j < data[0].length; j++)
      v0[j] = (int)(((height * (maxVal - data[0][j])) / dHeight));
    
    Color fore = getForeground();
    int x  = 0;
    int ox = x;
    int mid = (int)((maxVal * height) / dHeight);
    int ly  = mid + LABEL_MARK + fm.getAscent();
    for(int i = 1; i < size; i++) { 
      x = (i * width) / (size - 1);
      for(int j = 0; j < data[i].length; j++) {
	if(j == xLabelIndex) continue;
	v1[j] = (int)(((height * (maxVal - data[i][j])) / dHeight));
	g.setColor(colors[j]);
	g.drawLine(ox, v0[j], x, v1[j]);
	if(showPoints || (i == size - 1 && showLastPoints))
	  g.drawString(labels.getPointLabel(data[i][j]), x, v1[j]);
      }
      g.setColor(fore);
      g.drawLine(x, mid - LABEL_MARK, x, mid + LABEL_MARK);
      String l = labels.getXLabel(xLabelIndex == -1 ? (double)i : data[i][xLabelIndex]);
      g.drawString(l, x - fm.stringWidth(l), ly);
      v0 = (int[])v1.clone();
      ox = x;
    }
    g.drawLine(0, mid, width + LABEL_MARK, mid);
    g.drawLine(width, 0, width, (int)height);
    g.drawLine(width, mid, width + LABEL_MARK, mid);
    g.drawLine(width, (int)height, width + LABEL_MARK, (int)height);
    g.drawString(labels.getYLabel(maxVal), width + LABEL_MARK, 0);
    g.drawString(labels.getYLabel(0),      width + LABEL_MARK, mid);
    g.drawString(labels.getYLabel(minVal), width + LABEL_MARK, (int)height);
  }
  
  public void setColors(Color[] colors_) {
    colors = colors_;
  }

  static public class Labels {
    String getYLabel(double val) {
      return val + "";
    }
    
    String getXLabel(double val) {
      return getYLabel(val);
    }
    
    String getPointLabel(double val) {
      return getYLabel(val);
    }
  }

  public static void main(String[] argv) {
    DefaultListModel model = new DefaultListModel();
    
    ChartPanel cp = new ChartPanel(model);
    JFrame f = new JFrame();
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(BorderLayout.CENTER, cp);
    f.setSize(400, 400);
    f.show();
    
    for(;;) {
      model.addElement(new int[] {Util.intRandom(100) - 50, Util.intRandom(100) - 50, Util.intRandom(100) - 50});
      Util.delay(1000);
    } 
  }
}

/*
  $Log: ChartPanel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/01/15 15:08:58  schubige
  some sourcewatch bug fixes

  Revision 1.1  2001/01/14 13:27:33  schubige
  Wind NT update
  
*/
