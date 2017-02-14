package at.ac.tuwien.dbai.pdfwrap.gui.layer;

/**
 * This enumeration contains each shape that can be displayed by the GUI.
 * If its necessary to use other shapes, add them here and specify their painting behavior
 * in the method {@link StyledSegment#paintSegments(java.awt.Graphics2D, float, float)}.
 * 
 * @author Timo Schleicher
 *
 */
public enum Shapes {
	
	//Add new shapes here
	rectangle,line;
}
