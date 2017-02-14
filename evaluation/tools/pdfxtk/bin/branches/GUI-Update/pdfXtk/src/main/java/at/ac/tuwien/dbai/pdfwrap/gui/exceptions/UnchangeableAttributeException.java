package at.ac.tuwien.dbai.pdfwrap.gui.exceptions;

/**
 * This exception should be used if the user tries to modify a segment attribute
 * for which no modification has been defined
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class UnchangeableAttributeException extends Exception {

	/**
	 * Creates a {@link UnchangeableAttributeException}
	 */
	public UnchangeableAttributeException() {
		
		super();
	}
	
	/**
	 * Creates a {@link UnchangeableAttributeException} and adds the specified String
	 * 
	 * @param s The attribute name
	 */
	public UnchangeableAttributeException(String s) {
		
		super("Modifing of the attribute \"" + s + "\" has not been defined yet.");
	}
}
