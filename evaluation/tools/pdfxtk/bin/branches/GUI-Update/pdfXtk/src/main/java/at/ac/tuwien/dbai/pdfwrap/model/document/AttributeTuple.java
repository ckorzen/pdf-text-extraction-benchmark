package at.ac.tuwien.dbai.pdfwrap.model.document;

/**
 * A class for saving attribute tuples in form of (AttributeName, AttributeValue)
 * 
 * @author Timo Schleicher
 *
 */
public class AttributeTuple {

    private final String attributeName;
    private final String attributeValue;
    
    /**
     * Constructor method for a attribute tuple
     * 
     * @param x The attribute name
     * @param y The attribute value as String
     */
    public AttributeTuple(String x, String y) {
    	
        this.attributeName = x; 
        this.attributeValue = y; 
    }
    
    /**
     * Constructor method for a attribute tuple
     * 
     * @param x The attribute name
     * @param y The attribute value as float
     */
    public AttributeTuple(String x, float y) {
    	
    	this.attributeName = x;
    	this.attributeValue = Float.toString(y);
    }
    
    /**
     * Constructor method for a attribute tuple
     * 
     * @param x The attribute name
     * @param y The attribute value as int
     */
    public AttributeTuple(String x, int y) {
    	
    	this.attributeName = x;
    	this.attributeValue = Integer.toString(y);
    }
    
    /**
     * Constructor method for a attribute tuple
     * 
     * @param x The attribute name
     * @param y The attribute value as boolean
     */
    public AttributeTuple(String x, boolean y) {
    	
    	this.attributeName = x;
    	this.attributeValue = Boolean.toString(y);
    }
    
    /**
     * Getter method for the attribute name
     * 
     * @return Returns the attribute name
     */
    public String getAttributeName() {
    	
    	return this.attributeName;
    }
    
    /**
     * Getter method for the attribute value
     * 
     * @return Returns the attribute value
     */
    public String getAttributeValue() {
    	
    	return this.attributeValue;
    }

    @Override
    public String toString() {
        return "(" + attributeName + "," + attributeValue + ")";
    }

	@Override
    public boolean equals(Object other) {
    	
        if (other == null) {
            return false;
        }
        
        if (other == this) {
            return true;
        }
        
        if (!(other instanceof AttributeTuple)){
            return false;
        }
        
        AttributeTuple other_ = (AttributeTuple) other;
        return other_.attributeName.equals(this.attributeName) && other_.attributeValue.equals(this.attributeValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
        result = prime * result + ((attributeValue == null) ? 0 : attributeValue.hashCode());
        return result;
    }
}
