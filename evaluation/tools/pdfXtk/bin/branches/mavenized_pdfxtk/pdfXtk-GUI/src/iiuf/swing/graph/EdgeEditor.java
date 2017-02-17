package iiuf.swing.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GraphicsEnvironment;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JColorChooser;
import javax.swing.JSlider;
import javax.swing.JComponent;
import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import iiuf.awt.Awt;
import iiuf.util.Attributable;
import iiuf.util.Preferences;
import iiuf.swing.Resource;
import iiuf.swing.LocatedIcon;
import iiuf.swing.JNumberField;
import iiuf.swing.SetSelectionModel;
import iiuf.util.graph.GraphNode;

/**
   Editor panel for edge style.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class EdgeEditor 
  extends
  JPanel
  implements
  ChangeListener
{  
  static final String GP_NULL = "Graph panel not set. Call setGraphPanel() first.";

  static final String PLINE_WIDTH = "edge_editor.line_width";
  static final String PCOLOR      = "edge_editor.line_color";
  static final String PDASH       = "edge_editor.line_dash";
  static final String PCAP        = "edge_editor.line_cap";
  static final String PMARKER     = "edge_editor.line_marker";
  
  protected GraphPanel gp;
  Color                foreground;
  BasicStroke          stroke    = new BasicStroke();
  EdgeMarker[]         markers   = new EdgeMarker[0];
  double[]             markerPos = new double[0];
  
  JComboBox   lineWidth = new JComboBox(new Stroke[] {
    new StrokeWrapper(new BasicStroke(1), LINE_WIDTH), new StrokeWrapper(new BasicStroke(2), LINE_WIDTH),
    new StrokeWrapper(new BasicStroke(3), LINE_WIDTH), new StrokeWrapper(new BasicStroke(4), LINE_WIDTH), 
    new StrokeWrapper(new BasicStroke(6), LINE_WIDTH), new StrokeWrapper(new BasicStroke(8), LINE_WIDTH)
      });
  JComboBox   dash = new JComboBox(new Stroke[] {
    new StrokeWrapper(new BasicStroke(1), DASH),
    new StrokeWrapper(StrokeWrapper.setDash(new BasicStroke(1), new float[]{1, 1}, 0), DASH),
    new StrokeWrapper(StrokeWrapper.setDash(new BasicStroke(1), new float[]{2, 2}, 0), DASH),
    new StrokeWrapper(StrokeWrapper.setDash(new BasicStroke(1), new float[]{4, 4}, 0), DASH),
    new StrokeWrapper(StrokeWrapper.setDash(new BasicStroke(1), new float[]{5, 2, 1, 2}, 0), DASH),
  });
  JComboBox   cap = new JComboBox(new Stroke[] {
    new StrokeWrapper(StrokeWrapper.setEndCap(new BasicStroke(9), BasicStroke.CAP_BUTT),   CAP),
    new StrokeWrapper(StrokeWrapper.setEndCap(new BasicStroke(9), BasicStroke.CAP_ROUND),  CAP),
    new StrokeWrapper(StrokeWrapper.setEndCap(new BasicStroke(9), BasicStroke.CAP_SQUARE), CAP),
  });
  JComboBox   marker = new JComboBox(new MarkerWrapper[] {
    new MarkerWrapper(new EdgeMarker[0], new double[0])});
  
  ColorIcon   color    = new ColorIcon();
  JLabel      colorc   = new JLabel(color);
  int         selectLineWidthCnt;
  int         selectDashCnt;
  int         selectCapCnt;
  int         markerPrefIdx;

  public EdgeEditor(GraphPanel graphPanel) {
    setGraphPanel(graphPanel);
    
    colorc.setBorder(new EmptyBorder(5, 5, 5, 5));
    foreground = (Color)Preferences.get(PCOLOR);

    lineWidth.setEditor(new LineWidthEditor());
    lineWidth.setRenderer(new StrokeRenderer());
    lineWidth.setEditable(true);
    lineWidth.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setLineWidths();
	}
      });
    selectLineWidth(((Float)Preferences.get(PLINE_WIDTH, new Float(1.0))).floatValue());

    dash.setRenderer(new StrokeRenderer());
    dash.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setDashes();
	}
      });
    dash.setSelectedIndex(((Integer)Preferences.get(PDASH, new Integer(0))).intValue());
    setDashes();

    cap.setRenderer(new StrokeRenderer());
    cap.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setCaps();
	}
      });
    cap.setSelectedIndex(((Integer)Preferences.get(PCAP, new Integer(0))).intValue());
    setCaps();

    marker.setRenderer(new MarkerRenderer());
    marker.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setMarkers();
	}
      });
    markerPrefIdx = ((Integer)Preferences.get(PMARKER, new Integer(0))).intValue();
  
    setLayout(new GridBagLayout());
    
    add(lineWidth, Awt.constraints(false));
    add(colorc,    Awt.constraints(false));
    add(dash,      Awt.constraints(true));
    
    GridBagConstraints gc = Awt.constraints(false, GridBagConstraints.HORIZONTAL);
    gc.gridwidth = 2;
    add(marker,    gc);
    add(cap,       Awt.constraints(true));
    
    colorc.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) { 
	  int x = e.getX() - color.getX();
	  int y = e.getY() - color.getY();
	  if(x >= 0 && x < 17 && y >= 0 && y < 17) {
	    Color c = JColorChooser.showDialog(EdgeEditor.this, "Foreground color", foreground);
	    if(c != null) {
	      foreground = c;
	      setForegrounds();
	    }
	    e.getComponent().repaint();
	  }
	}
      });
    set(null);
  }
  
  public void addDash(float[] dashArray, int dashPhase) {
    dash.addItem(new StrokeWrapper(StrokeWrapper.setDash(new BasicStroke(1), dashArray, dashPhase), DASH));
  }
  
  public void addMarkers(EdgeMarker[] markers_, double[] markerPos_) {
    marker.addItem(new MarkerWrapper(markers_, markerPos_));
    if(marker.getItemCount() == markerPrefIdx + 1) {
      marker.setSelectedIndex(markerPrefIdx);
      markers   = markers_;
      markerPos = markerPos_;
    } else
      setMarkers();
  }
  
  void setForegrounds() {
    if(foreground == null) return;
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)
	if(sel[i] instanceof Attributable) {
	  GraphEdge e = (GraphEdge)((Attributable)sel[i]).get(gp.GRAPH_EDGE);
	  if(e != null)
	    e.setColor(foreground);
	}
    }
    Preferences.set(PCOLOR, foreground);
  }
  
  void setLineWidths() {
    if(gp != null && selectLineWidthCnt == 0) {
      Object[] sel = gp.getSelectionModel().getSelection();
      try {
	float lw = Float.parseFloat((String)lineWidth.getEditor().getItem());
	for(int i = 0; i < sel.length; i++)
	  if(sel[i] instanceof Attributable) {
	    GraphEdge e = (GraphEdge)((Attributable)sel[i]).get(gp.GRAPH_EDGE);
	    if(e != null)
	      e.setStroke(StrokeWrapper.setLineWidth(e.getStroke(), lw));
	  }
	stroke = (BasicStroke)StrokeWrapper.setLineWidth(stroke, lw);
	Preferences.set(PLINE_WIDTH, new Float(lw));
      } catch(Exception e) {}
      gp.repaint();
    }
  }
  
  void setDashes() {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      StrokeWrapper s = (StrokeWrapper)dash.getSelectedItem();
      for(int i = 0; i < sel.length; i++)
	if(sel[i] instanceof Attributable) {
	  GraphEdge e = (GraphEdge)((Attributable)sel[i]).get(gp.GRAPH_EDGE);
	  if(e != null)
	    e.setStroke(StrokeWrapper.setDash(e.getStroke(), s.getDashArray(), s.getDashPhase()));
	}
      stroke = (BasicStroke)StrokeWrapper.setDash(stroke, s.getDashArray(), s.getDashPhase());
      Preferences.set(PDASH, new Integer(dash.getSelectedIndex()));
      gp.repaint();
    }
  }
  
  void setCaps() {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      StrokeWrapper s = (StrokeWrapper)cap.getSelectedItem();
      for(int i = 0; i < sel.length; i++)
	if(sel[i] instanceof Attributable) {
	  GraphEdge e = (GraphEdge)((Attributable)sel[i]).get(gp.GRAPH_EDGE);
	  if(e != null)
	    e.setStroke(StrokeWrapper.setEndCap(e.getStroke(), s.getEndCap()));	  
	}
      stroke = (BasicStroke)StrokeWrapper.setEndCap(stroke, s.getEndCap());	  
      Preferences.set(PCAP, new Integer(cap.getSelectedIndex()));
      gp.repaint();
    }
  }
  
  void setMarkers() {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      MarkerWrapper s = (MarkerWrapper)marker.getSelectedItem();
      for(int i = 0; i < sel.length; i++)
	if(sel[i] instanceof Attributable) {
	  GraphEdge e = (GraphEdge)((Attributable)sel[i]).get(gp.GRAPH_EDGE);
	  if(e != null) {
	    e.setMarkers(s.markers, s.markerPos);
	  }
	}
      markers   = s.markers;
      markerPos = s.markerPos;
      Preferences.set(PMARKER, new Integer(marker.getSelectedIndex()));
      gp.repaint();
    }
  }
  
  public void setGraphPanel(GraphPanel graphPanel) {
    if(graphPanel == gp) return;
    if(gp != null)
      gp.getSelectionModel().removeChangeListener(this);
    gp = graphPanel;
    gp.getSelectionModel().addChangeListener(this);
  }
  
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }
  
  public void stateChanged(ChangeEvent e) {
    SetSelectionModel sm   = (SetSelectionModel)e.getSource();
    Attributable[]    sel  = (Attributable[])sm.getSelection(Attributable.class);
    
    int edgCnt = 0;
    for(int i = 0; i < sel.length; i++) 
      if(sel[i].get(gp.GRAPH_EDGE) != null)
	edgCnt++;
    
    iiuf.swing.graph.GraphEdge[] edges = new iiuf.swing.graph.GraphEdge[edgCnt];
    edgCnt = 0;
    for(int i = 0; i < sel.length; i++) {
      iiuf.swing.graph.GraphEdge ed = (iiuf.swing.graph.GraphEdge)sel[i].get(gp.GRAPH_EDGE);
      if(ed != null)
	edges[edgCnt++] = ed;
    }
    
    set(edges);
  }
  
  void set(GraphEdge[] e) {
    float     lw  = -1;
    float[]   da  = null;
    int       cp  = -1;
    if(e != null && e.length != 0) {
      foreground = e[0].getColor();  
      
      for(int i = 0; i < e.length; i++) {
	if(foreground != null && !e[i].getColor().equals(foreground))
	  foreground = null;
	if(e[i].getStroke() instanceof BasicStroke) {
	  BasicStroke b = (BasicStroke)e[i].getStroke();
	  lw = b.getLineWidth();
	  cp = b.getEndCap();
	  da = b.getDashArray();
	}
      }
      
      if(e.length > 0)
	selectDash(da);
      if(lw != -1)
	selectLineWidth(lw);
      if(cp != -1)
	selectCap(cp);
    }
    repaint();
  }
  
  void selectLineWidth(float width) {
    selectLineWidthCnt++;
    for(int i = 0; i < lineWidth.getItemCount(); i++)
      if(((StrokeWrapper)lineWidth.getItemAt(i)).getLineWidth() == width) {
	lineWidth.setSelectedIndex(i);
	selectLineWidthCnt--;
	return;
      }
    selectLineWidthCnt--;
  }
  
  void selectDash(float[] da) {
    selectDashCnt++;
    if(da == null)
      dash.setSelectedIndex(0);
    else {
      for(int i = 0; i < dash.getItemCount(); i++)
	if(da.equals(((StrokeWrapper)dash.getItemAt(i)).getDashArray())) {
	  dash.setSelectedIndex(i);
	  selectDashCnt--;
	  return;
	}
    }
    selectDashCnt--;
  }
  
  void selectCap(int ec) {
    selectCapCnt++;
    for(int i = 0; i < cap.getItemCount(); i++) {
      StrokeWrapper s = (StrokeWrapper)cap.getItemAt(i);
      if(s.getEndCap() == ec) {
	cap.setSelectedIndex(i);
	selectCapCnt--;
	return;
      }
    }
    selectCapCnt--;
  }

  /*
  void selectMarker(int ec) {
    selectMarkerCnt++;
    for(int i = 0; i < cap.getItemCount(); i++) {
      Object o = cap.getItemAt(i);
      if(o instanceof StrokeWrapper && ((StrokeWrapper)o).getEndCap() == ec) {
	cap.setSelectedIndex(i);
	selectCapCnt--;
	return;
      }
    }
    selectMarkerCnt--;
  }
  */
  
  public Color getColor() {
    return foreground;
  }
  
  public Stroke getStroke() {
    return stroke;
  }
  
  public double[] getMarkerPos() {
    return markerPos;
  }
  
  public EdgeMarker[] getMarkers() {
    return markers;
  }

  static class LineWidthEditor extends BasicComboBoxEditor {
    LineWidthEditor() {
      editor = new JNumberField(3, 0.0, 100.0);
    }
  }
  
  class ColorIcon extends LocatedIcon {
    ColorIcon() {
      super(Resource.ONE_COLOR);
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      super.paintIcon(c, g, x, y);
      if(foreground != null) {
	g.setColor(foreground);
	g.fillRect(x + 1, y + 1, 14, 14);
      }
    }
  }
  
  static final int LINE_WIDTH = 0;
  static final int DASH       = 1;
  static final int CAP        = 2;

  static class StrokeWrapper implements Stroke {
    Stroke stroke;
    int    mode;
    
    StrokeWrapper(Stroke stroke_, int mode_) {
      stroke = stroke_;
      mode   = mode_;
    }
    
    public Shape createStrokedShape(Shape p) {
      return stroke.createStrokedShape(p); 
    }
    
    public String toString() {
      if(stroke instanceof BasicStroke && mode == LINE_WIDTH)
	return ((BasicStroke)stroke).getLineWidth() + "";
      return "     ";
    }
    
    float getLineWidth() {
      if(stroke instanceof BasicStroke)
	return ((BasicStroke)stroke).getLineWidth();
      return 0;
    }
    
    static Stroke setLineWidth(Stroke s, float lw) {
      if(s instanceof BasicStroke) {
	BasicStroke b = (BasicStroke)s;
	float[] da  = b.getDashArray();
	if(da != null) {
	  float[] dsa = new float[da.length];
	  for(int i = 0; i < dsa.length; i++)
	    dsa[i] = da[i] * (lw / b.getLineWidth());
	  da = dsa;
	}
	return new BasicStroke(lw,
			       b.getEndCap(),
			       b.getLineJoin(),
			       b.getMiterLimit(),
			       da,
			       b.getDashPhase());
      }
      else return s;
    }
    
    float[] getDashArray() {
      if(stroke instanceof BasicStroke)
	return ((BasicStroke)stroke).getDashArray();
      return new float[0];      
    }

    float getDashPhase() {
      if(stroke instanceof BasicStroke)
	return ((BasicStroke)stroke).getDashPhase();
      return 0;      
    }
    
    static Stroke setDash(Stroke s, float[] da, float phase) {
      if(s instanceof BasicStroke) {
	BasicStroke b = (BasicStroke)s;
	if(da != null) {
	  float[] dsa = new float[da.length];
	  for(int i = 0; i < dsa.length; i++)
	    dsa[i] = da[i] * b.getLineWidth();
	  da = dsa;
	}
	return new BasicStroke(b.getLineWidth(),
			       b.CAP_BUTT,
			       b.getLineJoin(),
			       b.getMiterLimit(),
			       da,
			       phase);
      }
      else return s;
    }

    int getEndCap() {
      if(stroke instanceof BasicStroke)
	return ((BasicStroke)stroke).getEndCap();
      return -1;            
    }

    static Stroke setEndCap(Stroke s, int cap) {
      if(s instanceof BasicStroke) {
	BasicStroke b = (BasicStroke)s;
	return new BasicStroke(b.getLineWidth(),
			       cap,
			       b.getLineJoin(),
			       b.getMiterLimit(),
			       b.getDashArray(),
			       b.getDashPhase());
      }
      else return s;
    }
  }

  static Dimension   SRDIM = new Dimension(40, 18);
  static BasicStroke BASIC = new BasicStroke();
  static class StrokeRenderer
    extends 
    DefaultListCellRenderer 
  {
    StrokeWrapper stroke;
    
    public Component getListCellRendererComponent(JList   list,
						  Object  value,
						  int     index,
						  boolean sel,
						  boolean hasFocus) {
      
      super.getListCellRendererComponent(list, value, index, sel, hasFocus);
      stroke = (StrokeWrapper)value;
      return this;
    }
    
    public Dimension getPreferredSize() {
      return SRDIM;
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      g2.setStroke(stroke.stroke);
      int h = getHeight();
      int w = getWidth();
      switch(stroke.mode) {
      case LINE_WIDTH: 
	g.drawLine(30, h / 2, w, h / 2);    
	break;
      case CAP: 
	g.drawLine(10, h / 2, w - 10, h / 2); 
	g2.setStroke(BASIC);
	g.setColor(Color.white);
	g.drawLine(10, h / 2, w - 10, h / 2); 
	break;
      default:      
	g.drawLine(0,  h / 2, w, h / 2); 
	break;
      }
    }
  }

  static class MarkerWrapper {
    EdgeMarker[] markers;
    double[]     markerPos;
    
    MarkerWrapper(EdgeMarker[] markers_, double[] markerPos_) {
      markers   = markers_;
      markerPos = markerPos_;
    }
    
    public String toString() {
      return " ";
    }
  }

  static Dimension   MRDIM = new Dimension(50, 18);
  static class MarkerRenderer
    extends 
    DefaultListCellRenderer 
  {
    MarkerWrapper marker;
    
    public Component getListCellRendererComponent(JList   list,
						  Object  value,
						  int     index,
						  boolean sel,
						  boolean hasFocus) {
      
      super.getListCellRendererComponent(list, value, index, sel, hasFocus);
      marker = (MarkerWrapper)value;
      return this;
    }
    
    public Dimension getPreferredSize() {
      return MRDIM;
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      int h = getHeight();
      int w = getWidth();
      g.drawLine(10, h / 2, w - 10, h / 2);
      for(int i = 0; i < marker.markers.length; i++) {
	int yoff = (h / 2) - marker.markers[i].getBaseline();
	int mw = Math.min(w - 20, marker.markers[i].getPreferredSize().width);
	int xoff = (int)(10 + marker.markerPos[i] * (w - 20) - (marker.markerPos[i] * mw));
	g.translate(xoff, yoff);
	marker.markers[i].paint(g, mw);
	g.translate(-xoff, -yoff);
      }
    }
  }
}

/*
  $Log: EdgeEditor.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/03/11 17:59:38  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.2  2001/03/09 21:24:58  schubige
  Added preferences to edge editor

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel

  Revision 1.2  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.1  2001/03/07 07:52:07  schubige
  soundium properites panel
  
*/
