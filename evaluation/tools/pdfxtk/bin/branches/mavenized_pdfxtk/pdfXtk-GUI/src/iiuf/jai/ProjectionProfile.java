
package iiuf.jai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
   (c) 1999, IIUF<p>

   Projection Profile
 
   @author $Author: hassan $
   @version $Revision: 1.1 $
*/

public class ProjectionProfile {

  /** Profile direction. */

  public final static int VERTICAL = 0;
  public final static int HORIZONTAL = 1;

  /** Original projection profile. */

  protected int[] profile;

  /** Construct a projection profile. */

  public ProjectionProfile(int[] profile) {
    this.profile = profile;
  }

  /** Get the profile size. */

  public int size() {
    return profile.length;
  }

  /** Get a copy of the original profile. */

  public int[] getArray() {
    return profile;
  }

  /** Smooth the profile. */

  public void smooth() {
    for (int i = 0; i < size()-1; i++) {
      profile[i] = (profile[i]+profile[i+1])/2;
    }
  }

  /** Calculate the derivative of the profile. */

  public void derive() {
    for (int i = 0; i < size()-1; i++) {
      profile[i] = profile[i+1]-profile[i];
    }
    profile[size()-1] = 0;
  }

  /** Tries to find the ascent between start and end,
      from top to bottom.
      Ascent: max(dy) for positive dy's */

  public int findAscent(int start, int end) {
    int peak_i = 0;
    int peak_diff = 0;

    for (int i = start; i < end-1; i++) {
      int d = profile[i+1] - profile[i];
      if (d > peak_diff) {
	peak_diff = d;
	peak_i = i;
      }
    }

    return peak_i;
  }

  /** Tries to find the ascent in the upper half of the word. */

  public int findAscent() {
    return findAscent(0, profile.length/2);
  }

  /** Tries to find the base line between start and end,
      from bottom to top.
      Baseline: max(dy) for negative dy's */

  public int findBaseline(int start, int end) {
    int peak_i = 0;
    int peak_diff = 0;

    if (start < end) {
      int tmp = start;
      start = end;
      end = tmp;
    }

    for (int i = start; i > end; --i) {
      int d = profile[i-1] - profile[i];
      if (d > peak_diff) {
	peak_diff = d;
	peak_i = i;
      }
    }

    return peak_i;
  }

  public int findBaseline() {
    return findBaseline(profile.length/2, profile.length-1);
  }

  /** Returns the position of the maximum between start and end. */

  public int getMax(int start, int end) {
    int max_i = 0;
    int max_val = 0;

    for (int i = start; i < end-1; i++) {
      if (profile[i] > max_val) {
	max_i = i;
	max_val = profile[i];
      }
    }

    return max_i;
  }

  /** Creates a visual representation of this profile. */

  public JPanel getVerticalPanel() {
    return new ProfilePanel(VERTICAL);
  }

  public JPanel getHorizontalPanel() {
    return new ProfilePanel(HORIZONTAL);
  }

  /** Paints the profile in a certain direction and at a certain position.

      @param g The current graphics context
      @param direction VERTICAL or HORIZONTAL
      @param x Position
      @param y Pposition */

  public void paint(Graphics2D g, int direction, int x, int y) {
    for (int i = 0; i < size(); i++) {
      if (direction == VERTICAL) {
	g.drawLine(x, y+i, x+profile[i], y+i);
      } else {
	g.drawLine(x+i, y, x+i, y+profile[i]);
      }
    }
  }

  /** A class used to display projection profiles. */

  private class ProfilePanel
    extends JPanel 
  {
    protected int direction;

    public ProfilePanel(int direction) {
      this.direction = direction;
      int maxval = profile[getMax(0, ProjectionProfile.this.size())];

      if (direction == ProjectionProfile.VERTICAL) {
	setMinimumSize(new Dimension(maxval, ProjectionProfile.this.size()));
	setPreferredSize(new Dimension(maxval, ProjectionProfile.this.size()));
      } else {
	setMinimumSize(new Dimension(ProjectionProfile.this.size(), maxval));
	setPreferredSize(new Dimension(ProjectionProfile.this.size(), maxval));
      }
    }

    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;

      Dimension size = getSize(null);

      g2.setColor(getBackground());
      g2.fillRect(0, 0, size.width, size.height);
      g2.setColor(Color.black);

      ProjectionProfile.this.paint(g2, direction, 0, 0);
    }
  }
}
