package iiuf.swing.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.util.Dictionary;
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
   Editor panel for component font, alignment, foreground color and background color.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class StyleEditor 
  extends
  JPanel
  implements
  ChangeListener
{  
  static final String GP_NULL = "Graph panel not set. Call setGraphPanel() first.";

  private static final int P_SIZE     = 0x001;
  private static final int P_FONT     = 0x002;
  private static final int P_HALIGN   = 0x004;
  private static final int P_VALIGN   = 0x008;
  private static final int P_BCOLOR   = 0x010;
  private static final int P_FCOLOR   = 0x020;
  private static final int P_BOLD     = 0x040;
  private static final int P_ITALIC   = 0x080;
  private static final int P_FONT_ALL = 0x100;
  
  private static final String PREFS = "cmp_style_editor.dtyle";
  
  protected GraphPanel gp;
  Style       userStyle;
  Style       selStyle;
  JComboBox   font;
  JComboBox   size  = new JComboBox(new String[] {"8", "9", "10", "11", "12", "14", "16", "18", "20", 
						  "22", "24", "26", "28", "36", "48", "72"});

  ColorIcon   color    = new ColorIcon();
  JLabel      colorc   = new JLabel(color);
  JCheckBox   bold     = new JCheckBox(Resource.BOLD);
  JCheckBox   italic   = new JCheckBox(Resource.ITALIC);
  JCheckBox   anorth   = new JCheckBox(Resource.ALIGN_NORTH);
  JCheckBox   aeast    = new JCheckBox(Resource.ALIGN_EAST);
  JCheckBox   asouth   = new JCheckBox(Resource.ALIGN_SOUTH);
  JCheckBox   awest    = new JCheckBox(Resource.ALIGN_WEST);
  JCheckBox   acenterh = new JCheckBox(Resource.ALIGN_CENTERH);
  JCheckBox   acenterv = new JCheckBox(Resource.ALIGN_CENTERV);
  int         selectFontCnt;
  JPanel      alignp   = new JPanel();

  public StyleEditor(GraphPanel graphPanel) {
    setGraphPanel(graphPanel);

    userStyle = (Style)Preferences.get(PREFS, new Style());
        
    colorc.setBorder(new EmptyBorder(5, 5, 5, 5));

    size.setEditor(new FontSizeEditor());
    size.setEditable(true);
    size.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setSizes();
	}
      });
    
    bold.setSelectedIcon(Resource.BOLD_SEL);
    bold.setHorizontalAlignment(SwingConstants.RIGHT);
    bold.setBorder(new EmptyBorder(0, 4, 0, 0));
    bold.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setBold(((JCheckBox)e.getSource()).isSelected());
	}
      });
    
    italic.setSelectedIcon(Resource.ITALIC_SEL);
    italic.setHorizontalAlignment(SwingConstants.LEFT);
    italic.setBorder(new EmptyBorder(0, 0, 0, 4));
    italic.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setItalic(((JCheckBox)e.getSource()).isSelected());
	}
      });
      
    ArrayList fontsa = new ArrayList();
    fontsa.add("?");
    String[]  fonts  = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for(int i = 0; i < fonts.length; i++)
      if(fonts[i].length() > 2)
	fontsa.add(fonts[i]);
    font = new JComboBox(fontsa.toArray());
    font.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setFonts();
	}
      });
    
    ButtonGroup valign = new ButtonGroup();
    anorth.setSelectedIcon(Resource.ALIGN_NORTH_SEL);
    anorth.setBorder(new EmptyBorder(0, 4, 0, 0));
    anorth.setHorizontalAlignment(SwingConstants.RIGHT);
    anorth.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) 
	{setVAligns(SwingConstants.TOP);}});
    valign.add(anorth);
    acenterv.setSelectedIcon(Resource.ALIGN_CENTERV_SEL);
    acenterv.setBorder(null);
    acenterv.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) 
	{setVAligns(SwingConstants.CENTER);}});
    valign.add(acenterv);
    asouth.setSelectedIcon(Resource.ALIGN_SOUTH_SEL);
    asouth.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) 
	{setVAligns(SwingConstants.BOTTOM);}});
    asouth.setHorizontalAlignment(SwingConstants.LEFT);
    asouth.setBorder(new EmptyBorder(0, 0, 0, 4));
    valign.add(asouth);
    
    ButtonGroup halign = new ButtonGroup();
    awest.setSelectedIcon(Resource.ALIGN_WEST_SEL);
    awest.setBorder(new EmptyBorder(0, 4, 0, 0));
    awest.setHorizontalAlignment(SwingConstants.RIGHT);
    awest.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) 
	{setHAligns(SwingConstants.LEFT);}});
    halign.add(awest);
    acenterh.setSelectedIcon(Resource.ALIGN_CENTERH_SEL);
    acenterh.setBorder(null);
    acenterh.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) 
	{setHAligns(SwingConstants.CENTER);}});
    halign.add(acenterh);
    aeast.setSelectedIcon(Resource.ALIGN_EAST_SEL);
    aeast.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) 
	{setHAligns(SwingConstants.RIGHT);}});
    halign.add(aeast);
    
    setLayout(new GridBagLayout());
    alignp.add(anorth);
    alignp.add(acenterv);
    alignp.add(asouth);
    alignp.add(awest);
    alignp.add(acenterh);
    alignp.add(aeast);
    alignp.setBorder(null);
    add(alignp,   Awt.constraints(true));
    add(size,     Awt.constraints(false));
    add(colorc,   Awt.constraints(false));
    add(bold,     Awt.constraints(false));
    add(italic,   Awt.constraints(true));    
    add(font,     Awt.constraints(true, GridBagConstraints.HORIZONTAL));
    
    colorc.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) { 
	  int x = e.getX() - color.getX();
	  int y = e.getY() - color.getY();
	  if(x > 10 && x < 17 && y > 0 && y < 5) {
	    Color c = getCurrStyle().foreground;
	    getCurrStyle().foreground = getCurrStyle().background;
	    getCurrStyle().background = c;
	    setBackgrounds();
	    setForegrounds();
	    e.getComponent().repaint();
	  } else if(x > 0 && x < 10 && y > 0 && y < 10) {
	    Color c = JColorChooser.showDialog(StyleEditor.this, "Foreground color", getCurrStyle().foreground);
	    if(c != null) {
	      getCurrStyle().foreground = c;
	      setForegrounds();
	    }
	  } else if(x > 5 && x < 17 && y > 5 && y < 17) {
	    Color c = JColorChooser.showDialog(StyleEditor.this, "Background color", getCurrStyle().background);
	    if(c != null) {
	      getCurrStyle().background = c;
	      setBackgrounds();
	    }
	  }
	  e.getComponent().repaint();
	}
      });
    
    setupUI(userStyle);
  }
  
  private void setupUI(Style style) {
    size.getEditor().setItem(style.font.getSize() + "");
    bold.setSelected((style.font.getStyle() & Font.BOLD) != 0);
    italic.setSelected((style.font.getStyle() & Font.ITALIC) != 0);
    selectFont(style.font.getFamily());
    switch(style.vAlign) {
    case SwingConstants.TOP:    anorth.setSelected(true);   break;
    case SwingConstants.CENTER: acenterv.setSelected(true); break;
    case SwingConstants.BOTTOM: asouth.setSelected(true);   break;
    }
    switch(style.hAlign) {
    case SwingConstants.LEFT:   awest.setSelected(true);    break;
    case SwingConstants.CENTER: acenterh.setSelected(true); break;
    case SwingConstants.RIGHT:  aeast.setSelected(true);    break;
    }
  }

  public static Style getStyle(Component c) {
    return new Style(c);
  }
  
  public static Component setStyle(Component c, Style style) {
    style.set(c);
    return c;
  }
  
  public void preset(Component c, boolean dontColorize) {
    Color svb = getCurrStyle().background;
    Color svf = getCurrStyle().foreground;
    if(dontColorize) {
      getCurrStyle().background = c.getBackground();
      getCurrStyle().foreground = c.getForeground();      
    }
    setStyle(c, getCurrStyle());
    getCurrStyle().background = svb;
    getCurrStyle().foreground = svf;
  }
  
  void setHAligns(int align) {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)
	setProperty(P_HALIGN, sel[i], align);
      userStyle.vAlign = align;
      Preferences.set(PREFS, userStyle);
    }
  }
  
  void setVAligns(int align) {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)
	setProperty(P_VALIGN, sel[i], align);
      userStyle.hAlign = align;
      Preferences.set(PREFS, userStyle);
    }
  }
  
  void setFonts() {
    if(gp != null && selectFontCnt == 0) {
      Object[] sel = gp.getSelectionModel().getSelection();
      String f = (String)font.getSelectedItem();
      if(f == null || f.equals("?")) return;
      for(int i = 0; i < sel.length; i++)
	setProperty(P_FONT, sel[i], f);
      userStyle.font = new Font(f, userStyle.font.getStyle(), userStyle.font.getSize());
      Preferences.set(PREFS, userStyle);
    }
  }
  
  void setSizes() {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      try {
	float sz = Float.parseFloat((String)size.getEditor().getItem());
	for(int i = 0; i < sel.length; i++)
	  setProperty(P_SIZE, sel[i], sz);
	userStyle.font = userStyle.font.deriveFont(sz);
	Preferences.set(PREFS, userStyle);
      } catch(Exception e) {}
    }
  }
  
  void setBold(boolean state) {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)	
	if(sel[i] instanceof Attributable)
	  setProperty(P_BOLD, sel[i], state);
      if(state)
	userStyle.font = userStyle.font.deriveFont(userStyle.font.getStyle() | Font.BOLD);
      else
	userStyle.font = userStyle.font.deriveFont(userStyle.font.getStyle() & ~Font.BOLD);
      Preferences.set(PREFS, userStyle);
    }
  }
  
  void setItalic(boolean state) {
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)
	if(sel[i] instanceof Attributable)
	  setProperty(P_ITALIC, sel[i], state);
      if(state)
	userStyle.font = userStyle.font.deriveFont(userStyle.font.getStyle() | Font.ITALIC);
      else
	userStyle.font = userStyle.font.deriveFont(userStyle.font.getStyle() & ~Font.ITALIC);
    }
  }
  
  void setBackgrounds() {
    if(getCurrStyle().background == null) return;
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)
	setProperty(P_BCOLOR, sel[i], getCurrStyle().background);
      userStyle.background = getCurrStyle().background;
    }
  }
  
  void setForegrounds() {
    if(getCurrStyle().foreground == null) return;
    if(gp != null) {
      Object[] sel = gp.getSelectionModel().getSelection();
      for(int i = 0; i < sel.length; i++)
	setProperty(P_FCOLOR, sel[i], getCurrStyle().foreground);
      userStyle.foreground = getCurrStyle().foreground;
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

    int cmpCnt = 0;
    for(int i = 0; i < sel.length; i++) 
      if(sel[i].get(gp.COMPONENT) != null)
	cmpCnt++;
        
    Component[] cmps = new Component[cmpCnt];
    cmpCnt = 0;
    for(int i = 0; i < sel.length; i++) {
      Component c = (Component)sel[i].get(gp.COMPONENT);
      if(c != null)
	cmps[cmpCnt++] = c;
    }
        
    set(cmps);
  }

  Component getCmp(Component cmp) {
    if(cmp instanceof JSlider) {
      Dictionary d = ((JSlider)cmp).getLabelTable();
      if(d != null) {
	for(Enumeration e = d.elements(); e.hasMoreElements();) {
	  Object o = e.nextElement();
	  if(o instanceof JLabel) {
	    cmp = (Component)o;
	    break;
	  }
	}
      }
    }
    return cmp;
  }

  void set(Component[] c) {
    String  ff      = null;
    int     sz      = -1;
    int     st      = -1;
    int     ha      = -1;
    int     va      = -1;
    Color   fg      = null;
    Color   bg      = null;
    boolean enAlign = false;
    if(c != null && c.length != 0) {
      Component cmp = getCmp(c[0]);
      fg = cmp.getForeground();
      bg = cmp.getBackground();
      ff = cmp.getFont().getFamily();
      sz = cmp.getFont().getSize();
      st = cmp.getFont().getStyle();
      for(int i = 0; i < c.length; i++) {
	cmp = getCmp(c[i]);
	if(fg != null && !fg.equals(cmp.getForeground()))
	  fg = null;
	if(bg != null && !bg.equals(cmp.getBackground()))
	  bg = null;
	if(ff != null && !ff.equals(cmp.getFont().getFamily()))
	  ff = null;
	if(sz != -1 && cmp.getFont().getSize() != sz)
	  sz = -1;
	if(st != -1 && cmp.getFont().getStyle() != st)
	  st = -1;	
	if(cmp instanceof JLabel) {
	  ha = ((JLabel)cmp).getHorizontalAlignment();
	  va = ((JLabel)cmp).getVerticalAlignment();
	}
	if(cmp instanceof AbstractButton) {
	  ha = ((AbstractButton)cmp).getHorizontalAlignment();
	  va = ((AbstractButton)cmp).getVerticalAlignment();
	}
      }
      
      selStyle = new Style(ff, st, sz, ha, va, bg, fg);
    } else {
      selStyle = null;
    }
    setupUI(getCurrStyle());
    repaint();
  }
  
  void selectFont(String family) {
    selectFontCnt++;
    for(int i = 0; i < font.getItemCount(); i++)
      if(font.getItemAt(i).equals(family)) {
	font.setSelectedIndex(i);
	selectFontCnt--;
	return;
      }
    font.setSelectedIndex(0);
    selectFontCnt--;
  }
    
  static class FontSizeEditor extends BasicComboBoxEditor {
    FontSizeEditor() {
      editor = new JNumberField(5, 1, 1000);
    }
  }
    
  class ColorIcon extends LocatedIcon {
    ColorIcon() {
      super(Resource.TWO_COLOR);
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      super.paintIcon(c, g, x, y);
      if(getCurrStyle().foreground != null) {
	g.setColor(getCurrStyle().foreground);
	g.fillRect(x + 1, y + 1, 9, 9);
      }
      if(getCurrStyle().background != null ) {
	g.setColor(getCurrStyle().background);
	g.fillRect(x + 6, y + 11, 9, 4);
	g.fillRect(x + 11, y + 6, 4, 5);
      }
    }
  }
  
  public Style getCurrStyle() {
    return selStyle == null ? userStyle : selStyle;
  }
  
  private boolean setPrefs;
  private void setProperty(int property, Object o, int i)    
  {setProperty(property, o, i, null, null, false, null);}  
  private void setProperty(int property, Object o, float f)  
  {setProperty(property, o, f, null, null, false, null);}
  private void setProperty(int property, Object o, String s) 
  {setProperty(property, o, 0, s,    null, false, null);}
  private void setProperty(int property, Object o, Color col)
  {setProperty(property, o, 0, null, col,  false, null);}
  private void setProperty(int property, Object o, boolean b)
  {setProperty(property, o, 0, null, null, b,     null);}
  private void setProperty(Component c, Font f)
  {setProperty(P_FONT_ALL, c, 0, null, null, false, f);}
  
  private void setProperty(int property, Object o, double d, String s, Color col, boolean b, Font f) {
    if(o == null) return;
    if(o instanceof Attributable)
      setProperty(property, ((Attributable)o).get(gp.COMPONENT), d, s, col, b, f);
    if(o instanceof Container) {
      Object[] os = ((Container)o).getComponents();
      for(int i = 0; i < os.length; i++)
	setProperty(property, os[i], d, s, col, b, f);
    }
    if(o instanceof JSlider) {
      for(Enumeration e = ((JSlider)o).getLabelTable().elements(); e.hasMoreElements();)
	setProperty(property, e.nextElement(), d, s, col, b, f);
      ((Component)o).repaint();
    }
    else if(o instanceof Component) {
      Component c = (Component)o;
      switch(property) {
      case P_SIZE:
	c.setFont(c.getFont().deriveFont((float)d));
	break;
      case P_FONT:
	c.setFont(new Font(s, c.getFont().getStyle(), c.getFont().getSize()));
	break;
      case P_HALIGN:
	if(!validHorizontalKey((int)d))
	  break;
	if(c instanceof AbstractButton)
	  ((AbstractButton)c).setHorizontalAlignment((int)d);
	else if(c instanceof JLabel)
	  ((JLabel)c).setHorizontalAlignment((int)d);
	break;
      case P_VALIGN:
	if(!validVerticalKey((int)d))
	  break;
	if(c instanceof AbstractButton)
	  ((AbstractButton)c).setVerticalAlignment((int)d);
	else if(c instanceof JLabel)
	  ((JLabel)c).setVerticalAlignment((int)d);
	break;
      case P_BCOLOR:
	c.setBackground(col);
	break;
      case P_FCOLOR:
	c.setForeground(col);
	break;
      case P_BOLD:
	c.setFont(c.getFont().deriveFont(b ? c.getFont().getStyle() | Font.BOLD :
					 c.getFont().getStyle() & ~Font.BOLD));
	break;
      case P_ITALIC:
	c.setFont(c.getFont().deriveFont(b ? c.getFont().getStyle() | Font.ITALIC : 
					 c.getFont().getStyle() & ~Font.ITALIC));
	break;
      case P_FONT_ALL:
	c.setFont(f);
	break;
      }
    }
  }

  public static class Style 
    implements 
    Cloneable,
    java.io.Serializable 
  {
    int   hAlign;
    int   vAlign;
    Color background;
    Color foreground;
    Font  font;
    
    Style() {
      hAlign     = SwingConstants.CENTER;
      vAlign     = SwingConstants.CENTER;
      background = Color.white;
      foreground = Color.black;
      font       = new Font("Dialog", Font.PLAIN, 12);
    }
    
    Style(String font_, int style, int size, int halign, int valign, Color back, Color fore) {
      hAlign     = halign;
      vAlign     = valign;
      background = back;
      foreground = fore;
      try {font = new Font(font_, style, size);} catch(Exception e) {}
    }

    Style(Component c) {
      if(c instanceof JLabel) {
	hAlign = ((JLabel)c).getHorizontalAlignment();
	vAlign = ((JLabel)c).getVerticalAlignment();
      }
      if(c instanceof AbstractButton) {
	hAlign = ((AbstractButton)c).getHorizontalAlignment();
	vAlign = ((AbstractButton)c).getVerticalAlignment();
      }
      background = c.getBackground();
      foreground = c.getForeground();
      font       = c.getFont();
    }  
    
    void set(Component c) {
      if(c instanceof Container) {
	Component[] cmps = ((Container)c).getComponents();
	for(int i = 0; i < cmps.length; i++)
	  set(cmps[i]);
      }
      if(c instanceof JLabel) {
	if(validHorizontalKey(hAlign))
	  ((JLabel)c).setHorizontalAlignment(hAlign);
	if(validVerticalKey(vAlign))
	  ((JLabel)c).setVerticalAlignment(vAlign);
      }
      if(c instanceof AbstractButton) {
	if(validHorizontalKey(hAlign))
	  ((AbstractButton)c).setHorizontalAlignment(hAlign);
	if(validVerticalKey(vAlign))
	  ((AbstractButton)c).setVerticalAlignment(vAlign);
      } 
      if(c instanceof JSlider) {
	for(Enumeration e = ((JSlider)c).getLabelTable().elements(); e.hasMoreElements();)
	  set((Component)e.nextElement());
	c.repaint();
      }
      if(font != null)
	c.setFont(font);
      if(foreground != null)
	c.setForeground(foreground);
      if(background != null)
	c.setBackground(background);
    }
    
    public Object clone() {
      try{return super.clone();}catch(Exception e){return null;}
    }
  }

  static boolean validHorizontalKey(int key) {
    return ((key == SwingConstants.LEFT)    ||
	    (key == SwingConstants.CENTER)  ||
	    (key == SwingConstants.RIGHT)   ||
	    (key == SwingConstants.LEADING) ||
	    (key == SwingConstants.TRAILING));
  }
  
  static boolean validVerticalKey(int key) {
    return ((key == SwingConstants.TOP) || (key == SwingConstants.CENTER) || (key == SwingConstants.BOTTOM));
  }
}

/*
  $Log: StyleEditor.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/03/15 16:05:13  schubige
  cleanup and various fixes

  Revision 1.3  2001/03/12 17:52:00  schubige
  Added version support to sourcewatch and enhanced soundium

  Revision 1.2  2001/03/11 17:59:39  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.1  2001/03/09 15:55:08  schubige
  Added markers to graph panel

  Revision 1.2  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.1  2001/03/07 07:52:07  schubige
  soundium properites panel
  
*/
