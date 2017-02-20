package iiuf.swing;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.ArrayList;
import javax.swing.JToolTip;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

import iiuf.util.Strings;

/**
   A multi-line tool tip implementation.
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class MultiLineToolTip 
  extends 
  JToolTip 
{
  static class MultiLineToolTipUI
    extends
    MetalToolTipUI 
  {
    private String[] lines;
    private int      maxWidth;
    private int      maxAllowedWidth;
    
    MultiLineToolTipUI(int maxWidth) {
      maxAllowedWidth = maxWidth;
    }

    public void paint(Graphics g, JComponent c) {
      FontMetrics metrics = g.getFontMetrics();
      Dimension size = c.getSize();
      g.setColor(c.getBackground());
      g.fillRect(0, 0, size.width, size.height);
      g.setColor(c.getForeground());
      int y = metrics.getAscent() + 1;
      int h = metrics.getHeight();
      if(lines != null)
	for (int i = 0; i < lines.length; i++) {
	  g.drawString(lines[i], 3, y);
	  y += h;
	}
    }
    
    public Dimension getPreferredSize(JComponent c) {
      FontMetrics metrics = c.getFontMetrics(c.getFont());
      String tipText = ((JToolTip)c).getTipText();
      if(tipText == null) tipText = "";
      
      ArrayList linesa = new ArrayList();
      String[]  strs   = Strings.split(tipText, '\n');
      for(int i = 0; i < strs.length; i++) {     
	Segment s = new Segment(strs[i].toCharArray(), 0, strs[i].length());
	
	for(int brk = 0; ;) {
	  if(Utilities.getTabbedTextWidth(s, metrics, 0, null, 0) < maxAllowedWidth) {
	    linesa.add(new String(s.array, s.offset, s.array.length - s.offset));
	    break;
	  }
	  brk = Utilities.getBreakLocation(s, metrics, 0, maxAllowedWidth, null, 0);
	  linesa.add(new String(s.array, s.offset, brk));
	  s.offset += brk;
	  s.count  -= brk;
	}
      }
      
      lines = (String[])linesa.toArray(new String[linesa.size()]);
      maxWidth = 0;
      for(int i = 0; i < lines.length; i++)
	maxWidth = Math.max(maxWidth, SwingUtilities.computeStringWidth(metrics, lines[i]));
      return new Dimension(maxWidth + 6, metrics.getHeight() * lines.length + 2);
    }
  }
  
  public MultiLineToolTip() {  
    setUI(new MultiLineToolTipUI(400));
  }

  public MultiLineToolTip(int maxWidth) {  
    setUI(new MultiLineToolTipUI(maxWidth));
  }
}

/*
  $Log: MultiLineToolTip.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/02/19 15:10:38  schubige
  Fixed graph edge port location bug

  Revision 1.2  2001/02/11 16:25:39  schubige
  working on soundium

  Revision 1.1  2001/02/09 17:34:48  schubige
  working on soundium
  
*/
