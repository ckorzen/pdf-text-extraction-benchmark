package iiuf.awt;

import java.awt.*;
import java.util.*;

/**
   This example is from the book _Java in a Nutshell_ by David Flanagan.
   Written by David Flanagan.  Copyright (c) 1996 O'Reilly & Associates.
   You may study, use, modify, and distribute this example for any purpose.
   This example is provided WITHOUT WARRANTY either expressed or implied.

   Copyright (c) 1996 O'Reilly & Associates.
   
   @author David Flanagan, $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class MultiLineLabel extends Canvas {
  public static final int LEFT = 0; // Alignment constants
  public static final int CENTER = 1;
  public static final int RIGHT = 2;
  /** @serial The lines of text to display. */
  protected String[] lines;
  /** @serial The number of lines. */
  protected int num_lines;        
  /** @serial Left and right margins. */
  protected int margin_width;      
  /** @serial Top and bottom margins. */
  protected int margin_height;    
  /** @serial Total height of the font. */
  protected int line_height;       
  /** @serial Font height above baseline. */
  protected int line_ascent;       
  /** @serial How wide each line is. */
  protected int[] line_widths;    
  /** @serial The width of the widest line. */
  protected int max_width;          
  /** @serial The alignment of the text. */
  protected int alignment = LEFT;  
  
  // This method breaks a specified label up into an array of lines.
  // It uses the StringTokenizer utility class.
  protected void newLabel(String label) {
    StringTokenizer t = new StringTokenizer(label, "\n");
    num_lines = t.countTokens();
    lines = new String[num_lines];
    line_widths = new int[num_lines];
    for(int i = 0; i < num_lines; i++) lines[i] = t.nextToken();
  }
  
  // This method figures out how the font is, and how wide each
  // line of the label is, and how wide the widest line is.
  protected void measure() {
    FontMetrics fm = this.getFontMetrics(this.getFont());
    // If we don't have font metrics yet, just return.
    if (fm == null) return;
    
    line_height = fm.getHeight();
    line_ascent = fm.getAscent();
    max_width = 0;
    for(int i = 0; i < num_lines; i++) {
      line_widths[i] = fm.stringWidth(lines[i]);
      if (line_widths[i] > max_width) max_width = line_widths[i];
    }
  }
  
  // Here are four versions of the cosntrutor.
  // Break the label up into separate lines, and save the other info.
  public MultiLineLabel(String label, int margin_width, int margin_height,
			int alignment) {
    newLabel(label);
    this.margin_width = margin_width;
    this.margin_height = margin_height;
    this.alignment = alignment;
  }
  public MultiLineLabel(String label, int margin_width, int margin_height) {
    this(label, margin_width, margin_height, LEFT);
  }
  public MultiLineLabel(String label, int alignment) {
    this(label, 10, 10, alignment);
  }
  public MultiLineLabel(String label) {
    this(label, 10, 10, LEFT);
  }
  
  // Methods to set the various attributes of the component
  public void setLabel(String label) {
    newLabel(label);
    measure();
    repaint();
  }
  public void setFont(Font f) {
    super.setFont(f);
    measure();
    repaint();
  }
  public void setForeground(Color c) { 
    super.setForeground(c); 
    repaint(); 
  }
  public void setAlignment(int a) { alignment = a; repaint(); }
  public void setMarginWidth(int mw) { margin_width = mw; repaint(); }
  public void setMarginHeight(int mh) { margin_height = mh; repaint(); }
  public int getAlignment() { return alignment; }
  public int getMarginWidth() { return margin_width; }
  public int getMarginHeight() { return margin_height; }
  
  // This method is invoked after our Canvas is first created
  // but before it can actually be displayed.  After we've
  // invoked our superclass's addNotify() method, we have font
  // metrics and can successfully call measure() to figure out
  // how big the label is.
  public void addNotify() { super.addNotify(); measure(); }
  
  // This method is called by a layout manager when it wants to
  // know how big we'd like to be.  
  public Dimension getPreferredSize() {
    return new Dimension(max_width + 2* margin_width, 
			 num_lines * line_height + 2*margin_height);
  }
  
  // This method is called when the layout manager wants to know
  // the bare minimum amount of space we need to get by.
  public Dimension getMinimumSize() {
    return new Dimension(max_width, num_lines * line_height);
  }
  
  // This method draws the label (applets use the same method).
  // Note that it handles the margins and the alignment, but that
  // it doesn't have to worry about the color or font--the superclass
  // takes care of setting those in the Graphics object we're passed.
  public void paint(Graphics g) {
    int x, y;
    Dimension d = this.getSize();
    y = line_ascent + (d.height - num_lines * line_height)/2;
    for(int i = 0; i < num_lines; i++, y += line_height) {
      switch(alignment) {
      case LEFT:
	x = margin_width; break;
      case CENTER:
      default:
	x = (d.width - line_widths[i])/2; break;
      case RIGHT:
	x = d.width - margin_width - line_widths[i]; break;
      }
      g.drawString(lines[i], x, y);
    }
  }

  public void setText(String text) {
    setLabel(text);
  }
}
/*
  $Log: MultiLineLabel.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/11/20 17:36:56  schubige
  tinja project ide

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.3  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
