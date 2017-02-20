package at.ac.tuwien.dbai.pdfwrap.gui.layer;

import at.ac.tuwien.dbai.pdfwrap.model.document.AttributeTuple;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Combines a segment ({@link GenericSegment}) of the PDF analysis output with a {@link Style} object
 * that contains information about how to visualize the segment.
 * 
 * @author Timo Schleicher
 *
 */
public class StyledSegment implements Comparable<StyledSegment> {
	
	// TODO: deal with XYTextComparator and sorting issues 2013-11-29
	
	//Local coordinates considering the current scaling factors
	private float x, y;
	private float width, height;
	
	private GenericSegment segment;
	private Style style;
	
	private boolean isSelected;
	
	/**
	 * The constructor of a styled Segment.
	 * 
	 * @param segment The segment that will be enriched by styling information
	 * @param style The information about styling
	 */
	public StyledSegment(GenericSegment segment, Style style) {
		
		this.segment = segment;
		this.style = style;
		
		//Default value indicating that the segment is not selected in the beginning
		this.isSelected = false;
	}
	
	/**
	 * Calculates and updates the local coordinates with respect to the current
	 * screen scaling factor.
	 * 
	 * @param screenfactor The current screen scaling factor
	 * @param pageHeight The height of the original PDF page
	 */
	public void updateLocalCoordinates(float screenfactor, float pageHeight) {
		
		x = segment.getX1()*screenfactor;
		width = segment.getWidth()*screenfactor;
		
		y = (pageHeight - segment.getY2())*screenfactor;
		height = segment.getHeight()*screenfactor;
	}
	
	/**
	 * Paint this segment by considering all the given style information.
	 * 
	 * @param g2 The Graphics object on which the painting is done
	 */
	public void paintSegments(Graphics2D g2) {
		
		//Check whether the segment is printed or not
		if (!style.isPrintable()) {
			return;
		}
		
		//Check for selection and set color accordingly
		if (isSelected) {
			
			g2.setColor(new Color(255, 200, 50, 63));
			g2.setStroke(new BasicStroke(2.0f));
			
		} else {
			
			g2.setColor(style.getColor());
			g2.setStroke(new BasicStroke(style.getStrokeWidth()));
		}
		
		//Check for the correct shape and draw the corresponding lines
		//New shapes should be handled here as well as in the enumeration -> Shapes
		
		switch (style.getShape()) {
		
		//Rectangle shape
		case rectangle:
			
			if (style.isfilled()) {
				
				g2.fill(new Rectangle2D.Float(x, y, width, height));
				
			} else {
				
				g2.draw(new Rectangle2D.Float(x, y, width, height));
			}
			
			break;
			
		//Line shape
		case line:
			
			g2.draw(new Line2D.Float(x, y, x+width, y+height));
			
			break;
			
		//Add further shapes here!
		default:
			
			break;
		}
	}
	
	/**
	 * Checks whether the point x,y lies within the segment or not.
	 * 
	 * @param iX The x coordinate of the point
	 * @param iY The y coordinate of the point
	 * @return Returns true if the point lies within the segment, false otherwise
	 */
	public boolean intersects(int iX, int iY) {
		
		//If segment is not printed at all the user can't select it
		if (!style.isPrintable()) {
			
			return false;
		}
		
		//Set an offset value for lines in order to be able to select them properly
		int offSet = style.getShape() == Shapes.line ? 1 : 0;
		
		if ((iX >= x-offSet && iX <= x+width+offSet) && (iY >= y-offSet && iY <= y+height+offSet)) {
			
			return true;		
		}
		
		return false;
	}
	
	/**
	 * Checks whether the rectangular intersects the shape of the segment.
	 * 
	 * @param rec The rectangular you want to check
	 * @return Returns true if the rectangular intersects the shape of the segment, false otherwise
	 */
	public boolean intersects(Rectangle2D rec) {
		
		//If segment is not printed at all the user can't select it
		if (!style.isPrintable()) {
			
			return false;
		}
		
		//Make sure to check whether its a lined shape or a rectangular one
		return (style.getShape() == Shapes.line) ? rec.intersectsLine(x, y, x+width, y+height) : rec.intersects(x, y, width, height);
	}
	
	/**
	 * Specifies the selection of the segment.
	 * 
	 * @param selected sets the selection flag for this segment
	 */
	public void setSelected(boolean selected) {
		
		isSelected = selected;
	}
	
	/**
	 * Checks whether this styled segment is visible or not.
	 * 
	 * @return true if this segment is visible, false otherwise
	 */
	public boolean isVisible() {
		
		return style.isPrintable();
	}
	
	@Override
	public String toString() {
		
		//Need this new toString method for easy displaying different segments if more than one is selected
		String ret = segment.tagName() + " - ";
		
		String text = null;
		
		String position = ",";
		
		for (AttributeTuple att : segment.getAttributes()) {
			
			if (att.getAttributeName().equals("text")) {
				
				text = att.getAttributeValue();
				break;
				
			} else if (att.getAttributeName().equals("x1")) {
				
				position = "(" + att.getAttributeValue() + position;
				
			} else if (att.getAttributeName().equals("y1")) {
				
				position += att.getAttributeValue() + ")";
			}
		}
		
		ret+= (text == null) ? position : text.split(" ")[0];	
		
		return ret;
	}
	
	/**
	 * Getter method for the underlying segment.
	 * 
	 * @return the underlying segment e.g. kind of a {@link GenericSegment}
	 */
	public GenericSegment getSegment() {
		
		return segment;
	}
	
	/**
	 * Checks whether the class of this segment and the input segment is equal or not.
	 * 
	 * @param seg the segment you want to compare the class with
	 * @return true if the class of this segment and the input segment equals, false otherwise
	 */
	public boolean isClassEqual(StyledSegment seg) {
		
		return seg.getSegment().getClass().getCanonicalName().equals(segment.getClass().getCanonicalName());
	}
	
	/**
	 * Getter method for the local y coordinate.
	 * 
	 * @return the local y coordinate regarding the current screen settings like window size etc.
	 */
	public float getLocalY() {
		
		return y;
	}
	
	/**
	 * Getter method for the local x coordinate.
	 * 
	 * @return the local x coordinate regarding the current screen settings like window size etc.
	 */
	public float getX() {
		
		return x;
	}

	@Override
	public int compareTo(StyledSegment seg) {
		
		//Use the natural appearance of a textual document to sort a segment
		//First sort from top to bottom (like one "line" after the other)
		if (y < seg.getLocalY()) {
			
			return -1;
			
		} else if (y > seg.getLocalY()) {
			
			return 1;
			
		}
		
		//Within a "line" sort from left to right
		if (x < seg.getX()) {
			
			return -1;
			
		} else if (x > seg.getX()) {
			
			return 1;
		}
		
		return 0;
	}
}
