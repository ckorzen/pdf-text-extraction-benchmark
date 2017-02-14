package iiuf.swing.graph;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import iiuf.awt.Awt;
import iiuf.swing.Resource;
import iiuf.swing.LocatedIcon;
import iiuf.swing.JNumberField;
import iiuf.util.graph.GraphNode;

/**
   Graph node geometry editor.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class GeometryEditor
  extends 
  JPanel 
  implements
  ActionListener,
  PropertyChangeListener
{
  static ImageIcon[][] OICONS = new ImageIcon[][] {{Resource.LOC_NW, Resource.LOC_N, Resource.LOC_NE},
						   {Resource.LOC_W,  Resource.LOC_C, Resource.LOC_E},
						   {Resource.LOC_SW, Resource.LOC_S, Resource.LOC_SE}};
  
  protected GraphPanel         gp;
  protected GraphNodeComponent cmp;
  
  JNumberField x       = new JNumberField(5, 0, 10000);
  JNumberField y       = new JNumberField(5, 0, 10000);
  JNumberField w       = new JNumberField(5, 0, 10000);
  JNumberField h       = new JNumberField(5, 0, 10000);
  JComboBox    angle   = new JComboBox(new String[] {"0", "90", "180", "270"});
  LocatedIcon  origin  = new LocatedIcon(Resource.LOC_NW);
  JLabel       xl      = new JLabel("X: ", origin, JLabel.LEADING);
  double       xf      = 0;
  double       yf      = 0;
  Rectangle    r       = new Rectangle();
  
  public GeometryEditor(GraphPanel graphPanel) {
    setGraphPanel(graphPanel);
    setLayout(new GridBagLayout());
    add(xl,                       Awt.constraints(false, GridBagConstraints.HORIZONTAL));
    add(x,                        Awt.constraints(false));
    add(new JLabel("W: "),        Awt.constraints(false));
    add(w,                        Awt.constraints(true));
    add(new JLabel("Y: ", JLabel.TRAILING), Awt.constraints(false, GridBagConstraints.HORIZONTAL));
    add(y,                        Awt.constraints(false));
    add(new JLabel("H: "),        Awt.constraints(false));
    add(h,                        Awt.constraints(true));
    add(new JLabel(Resource.ARC), Awt.constraints(false));
    angle.setEditor(new NumberEditor());
    angle.setEditable(true);
    add(angle, Awt.constraints(false));
    Component[] cmps = getComponents();
    for(int i = 0; i < cmps.length; i += 2)
      cmps[i + 1].setEnabled(false);
    x.addActionListener(this);
    y.addActionListener(this);
    w.addActionListener(this);
    h.addActionListener(this);
    angle.addActionListener(this);
    xl.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	  int x = (e.getX() - xl.getX()) / 5;
	  int y = (e.getY() - xl.getY()) / 5;
	  if(x < 0 || y < 0 || x > 2 || y > 2) return;
	  origin.setIcon(OICONS[y][x]);
	  xf = (double)x / 2.0;
	  yf = (double)y / 2.0;
	  GeometryEditor.this.x.setNumber(r.x + (r.width  * xf));
	  GeometryEditor.this.y.setNumber(r.y + (r.height * yf));
	  xl.repaint();
	}
      });
  }
  
  public void setGraphPanel(GraphPanel graphPanel) {
    if(graphPanel == gp) return;
    if(gp != null)
      gp.removePropertyChangeListener(GraphPanel.SELECTION_BOUNDS_CHANGED, this);
    gp = graphPanel;
    gp.addPropertyChangeListener(GraphPanel.SELECTION_BOUNDS_CHANGED, this);
  }
  
  public void actionPerformed(ActionEvent e) {
    if(gp == null) throw new IllegalArgumentException("Graph panel not set. Call setGraphPanel() first.");
    r.setBounds((int)(x.getNumber() - w.getNumber() * xf),
		(int)(y.getNumber() - h.getNumber() * yf),
		(int)w.getNumber(),
		(int)h.getNumber());
    gp.getEditor().resizeSelection(r.x, r.y, r.width, r.height);
    _set(gp.getSelectionBounds());
    if(cmp                                  != null && 
       cmp.getMinimalRotation()             != cmp.NOT_ROTATABLE && 
       angle.getEditor().getItem()          != null &&
       !angle.getEditor().getItem().toString().equals(""))
      cmp.setRotation((int)Double.parseDouble(angle.getEditor().getItem().toString()) * cmp.ANGLE_TO_DEG);
    else 
      angle.setEnabled(false);
    angle.getEditor().setItem("" + cmp.getRotation() / cmp.ANGLE_TO_DEG);
  }
  
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }
  
  public void propertyChange(PropertyChangeEvent e) {
    GraphNode[] nodes = (GraphNode[])gp.getSelectionModel().getSelection(GraphNode.class);
    Component   cmp   = null;
    int         count = 0;
    for(int i = 0; i < nodes.length; i++) {
      Component tmp = (Component)nodes[0].get(gp.COMPONENT);
      if(tmp != null) {
	cmp = tmp;
	count++;
      }
    }
    if(count == 1) {
      if(cmp instanceof GraphNodeComponent) {
	set((GraphNodeComponent)cmp);
	return;
      } else
	set(cmp.getBounds());
    }
    set(gp.getSelectionBounds());
  }

  void set(GraphNodeComponent c) {
    if(c == null) {
      set((Rectangle)null);
      angle.getEditor().setItem("");
      angle.setEnabled(false);
    } else {
      set(((Component)c).getBounds());
      angle.getEditor().setItem("" + c.getRotation() / c.ANGLE_TO_DEG);
      angle.setEnabled(true);
    }
    cmp = c;
  }
  
  void set(Rectangle r) {
    _set(r);
    angle.setEnabled(false);
  }
  
  private void _set(Rectangle r_) {
    if(r_ == null) {
      r.setBounds(0, 0, 0, 0);
      x.setText("");
      y.setText("");
      w.setText("");
      h.setText("");	
      x.setEnabled(false);
      y.setEnabled(false);
      w.setEnabled(false);
      h.setEnabled(false);
    } else {
      r = (Rectangle)r_.clone();
      x.setNumber(r.x + (r.width  * xf));
      y.setNumber(r.y + (r.height * yf));
      w.setNumber(r.width);
      h.setNumber(r.height);
      x.setEnabled(true);
      y.setEnabled(true);
      w.setEnabled(true);
      h.setEnabled(true);
    }
  }

  static class NumberEditor extends BasicComboBoxEditor {
    NumberEditor() {
      editor = new JNumberField(3, 0, 359);
    }
  }
}

/*
  $Log: GeometryEditor.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.1  2001/03/07 07:52:07  schubige
  soundium properites panel
  
*/
