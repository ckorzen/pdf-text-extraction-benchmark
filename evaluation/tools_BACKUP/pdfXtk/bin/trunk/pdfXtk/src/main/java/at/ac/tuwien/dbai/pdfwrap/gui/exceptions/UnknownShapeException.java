package at.ac.tuwien.dbai.pdfwrap.gui.exceptions;

/**
 * This exception should be used if the shape that was specified by the user
 * is not known yet.
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class UnknownShapeException extends IllegalArgumentException {

	/**
	 * Creates a {@link UnknownShapeException}
	 */
	public UnknownShapeException() {
		
		super();
	}
	
	/**
	 * Creates a {@link UnknownShapeException} and adds the specified String
	 * 
	 * @param s The shape of the name that is previously unknown
	 */
	public UnknownShapeException(String s) {
		
		super("The specified shape \"" + s + "\" is not known yet.");
	}
}
