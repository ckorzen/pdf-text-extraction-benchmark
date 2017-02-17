package at.ac.tuwien.dbai.pdfwrap.model.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.touchgraph.graphlayout.TGPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;


/**
 * Node in the document graph
 * NOTE 2011-03-09: the class DocumentGraph must be used to find edges
 * from or to this node ... the TouchGraph methods
 * to iterate through edges currently return a blank list!
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDFAnalyser 0.9
 */
public class DocNode extends com.touchgraph.graphlayout.Node implements Cloneable
{
	// fields for graph wrapping
	public final static int MATCH_CONTENT_OFF = 0;
	public final static int MATCH_CONTENT_STRING = 1;
	public final static int MATCH_CONTENT_SUBSTRING = 2;
	public final static int MATCH_CONTENT_REGEXP = 3;
	
	// ID field already exists in touchgraph node ...
//	protected int segID = this.hashCode();
	
	boolean matchFont = false;
    boolean matchFontSize = false;
    boolean matchBold = false;
    boolean matchItalic = false;
    int matchContent = 0;
    String matchContentString = "";
    
    int matchMaxLength = -1;
    int matchMinLength = -1;
	
    boolean removeFromInstance = false;
    
    // 23.01.2011 necessary?
//    GenericSegment docSegment;
    
    // fields from Generic/TextSegment, which are copied:
    protected String segType;
    protected float segX1, segX2, segY1, segY2;
    protected String segText;
    protected String segFontName;
    protected float segFontSize;
    
    protected boolean extractContent;
    
    // TouchGraph-related fields
	public static Color NODE_COLOR = Color.red.darker();
	public static Color EXAMPLE_AND_FOUND_INSTANCE_COLOUR = new Color(255, 127, 0);
	public static Color EXAMPLE_INSTANCE_COLOUR = new Color(255, 127, 0);
	public static Color FOUND_INSTANCE_COLOUR = new Color(255, 127, 0);

    public static Color MOUSE_OVER_HYPERLINK_COLOR = Color.cyan;
    public static Color HIGHLIGHTED_COLOR = new Color(255, 127, 0);
    public static int DEFAULT_TYPE = TYPE_ELLIPSE;
    
    public static final Font SMALL_TAG_FONT = new Font("Tahoma",Font.PLAIN,8);
	public static final Font TEXT_FONT = new Font("Tahoma",Font.PLAIN,10);
    
    String url;
    
    protected boolean foundInstance = false;
    protected boolean exampleInstance = false;
    
	public DocNode(GenericSegment gs)
	{
		super();
		this.setFields(gs);
	}
    
	/**
	 * also sets text fields for text segments
	 * @param gs
	 */
	public void setFields(GenericSegment gs)
	{
		segX1 = gs.getX1();
		segX2 = gs.getX2();
		segY1 = gs.getY1();
		segY2 = gs.getY2();
		segType = gs.tagName();
		
		if (gs instanceof TextSegment)
		{
			TextSegment ts = (TextSegment)gs;
			segText = ts.getText();
			segFontName = ts.getFontName();
			segFontSize = ts.getFontSize();
		}
//		tagName = gs.getTagName();
		// is a non-calculated field in superclass
		lbl = nodeText(); // must be carried out afer tagName has been set!
		// node label font (another non-calculated field used by paint)
		font = TEXT_FONT;
		typ = TYPE_ROUNDRECT;
		
		removeFromInstance = false;
		
//		mouseOverHyperlink = false;
//      highlighted = false;
		setID(new Integer(gs.hashCode()).toString());
	}
	
	protected void setCommonFields()
	{
//		TODO
	}
	
    public DocNode(Element nodeElement)
	{
		super();
		this.setFields(nodeElement);
	}
	
	public void setFields(Element nodeElement)
	{
		NodeList nl; Element el; NodeList textNL; String val;
		
		//-------
        nl = nodeElement.getElementsByTagName("segment-id");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
//        segID = Integer.parseInt(val);
        setID(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("x1");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        segX1 = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("x2");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        segX2 = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("y1");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        segY1 = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("y2");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        segY2 = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("remove-from-instance");
        if (nl.getLength() > 0)
        {
	        el = (Element)nl.item(0);
	        textNL = el.getChildNodes();
	        val = ((Node)textNL.item(0)).getNodeValue().trim();
	        removeFromInstance = Boolean.parseBoolean(val);
        }
        else removeFromInstance = false;
        
        //-------
        nl = nodeElement.getElementsByTagName("segment-type");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        segType = val;
        
        if (nodeElement.getChildNodes().getLength() >= 39)
        {
        	// TODO: add text fields!
        	//-------
            nl = nodeElement.getElementsByTagName("extract-content");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            extractContent = Boolean.parseBoolean(val);
            
    		//-------
            nl = nodeElement.getElementsByTagName("text");
            //if (nl.getLength() > 0)
            //{
    	        el = (Element)nl.item(0);
    	        textNL = el.getChildNodes();
    	        if (textNL.getLength() > 0)
    	        {
    	        	val = ((Node)textNL.item(0)).getNodeValue().trim();
    	        	segText = val;
    	        }
            //}
            
            //-------
            nl = nodeElement.getElementsByTagName("font-name");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            segFontName = val;
            
            //-------
            nl = nodeElement.getElementsByTagName("font-size");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            segFontSize = Float.parseFloat(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-font");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchFont = Boolean.parseBoolean(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-font-size");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchFontSize = Boolean.parseBoolean(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-bold");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchBold = Boolean.parseBoolean(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-italic");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchItalic = Boolean.parseBoolean(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-content");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchContent = Integer.parseInt(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-content-string");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            if (textNL.getLength() > 0)
            {
            	val = ((Node)textNL.item(0)).getNodeValue().trim();
            	matchContentString = val;
            }
            
            //-------
            nl = nodeElement.getElementsByTagName("match-max-length");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchMaxLength = Integer.parseInt(val);
            
            //-------
            nl = nodeElement.getElementsByTagName("match-min-length");
            el = (Element)nl.item(0);
            textNL = el.getChildNodes();
            val = ((Node)textNL.item(0)).getNodeValue().trim();
            matchMinLength = Integer.parseInt(val);
            
        	segType = "text-segment";
        }
        else
        {	
        	segType = "generic-segment";
        }
        
     // is a non-calculated field in superclass
		lbl = nodeText();
		// node label font (another non-calculated field used by paint)
		font = TEXT_FONT;
		typ = TYPE_ROUNDRECT;
		
//		mouseOverHyperlink = false;
//      highlighted = false;
        
		
		
		setNodeTextFont(TEXT_FONT);
		//setNodeType(TYPE_RECTANGLE);
		setNodeType(TYPE_ROUNDRECT);
		setBackColor(NODE_COLOR);
	}
	
	public String toString()
	{
		return ("DocNode: text: " + segText + " x1: " + segX1 + " x2: " + segX2 + " y1: " + segY1
				+ " y2: " + segY2 + " segType: " + segType);
	}
	
	public void addAsXMLNode(Document resultDocument, Element parent)//, GenericSegment pageDim)
    {
        Element newNodeElement;
        newNodeElement = resultDocument.createElement("node");
        
        setXMLNodeAttributes(resultDocument, newNodeElement);
        
        parent.appendChild(newNodeElement);
    }
	
	public void setXMLNodeAttributes(Document resultDocument, Element nodeElement)
	{
		Element newAttribElement;
		
		newAttribElement = resultDocument.createElement("segment-id");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(getID()));
    	
    	newAttribElement = resultDocument.createElement("x1");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Float.toString(segX1)));
    	
    	newAttribElement = resultDocument.createElement("x2");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Float.toString(segX2)));
    	
    	newAttribElement = resultDocument.createElement("y1");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Float.toString(segY1)));
    	
    	newAttribElement = resultDocument.createElement("y2");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Float.toString(segY2)));
    	
		newAttribElement = resultDocument.createElement("remove-from-instance");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Boolean.toString(removeFromInstance)));
    	
    	newAttribElement = resultDocument.createElement("extract-content");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(extractContent)));
	 	
	 	newAttribElement = resultDocument.createElement("segment-type");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(segType));
	 	
		newAttribElement = resultDocument.createElement("text");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Utils.removeInvalidXMLCharacters(segText)));
    	
		newAttribElement = resultDocument.createElement("font-name");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(segFontName));
	 	
	 	newAttribElement = resultDocument.createElement("font-size");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Float.toString(segFontSize)));
	 	
	 	newAttribElement = resultDocument.createElement("match-font");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Boolean.toString(matchFont)));
    	
		newAttribElement = resultDocument.createElement("match-font-size");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(matchFontSize)));
	 	
	 	newAttribElement = resultDocument.createElement("match-bold");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(matchBold)));
	 	
	 	newAttribElement = resultDocument.createElement("match-italic");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(matchItalic)));
	 	
	 	newAttribElement = resultDocument.createElement("match-content");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(matchContent)));
	 	
	 	newAttribElement = resultDocument.createElement("match-content-string");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(matchContentString));
    	
		newAttribElement = resultDocument.createElement("match-max-length");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(matchMaxLength)));
	 	
	 	newAttribElement = resultDocument.createElement("match-min-length");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(matchMinLength)));
	}

	/*
	public String getLabel() {
        return getNodeText();
    }
	*/
	
	public boolean isTextSegment()
	{
		if (segType == "image-segment" || segType == "line-segment" || segType == "rect-segment")
			return false;
		else return true;
	}
	
	public String nodeText()
	{
		if (segType.equals("text-block") || 
			segType.equals("text-segment") ||
			segType.equals("text-line"))
		{
			// limit length to 40 characters
			
			if (segText.length() > 40)
				return segText.substring(0, 36) + "...";
			else return segText;
		}
		else
		{
			return "[" + segType + "]";
		}
	}
	
	public void clearWrapperEdits()
	{
		// these are probably unnecessary... legacy variables
//		mouseOverHyperlink = false;
//	    highlighted = false;
//	    exampleInstance = false;
//	    foundInstance = false;
	    
	    removeFromInstance = false;
	    extractContent = false;
//		tagName = "result";
		
		matchFont = false;
		matchFontSize = false;
		matchContent = 0;
	    matchContentString = "";
	    matchMaxLength = -1;
	    matchMinLength = -1;
	}
	
	public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }
	
	// copied from TextSegment
	public boolean isBold()
    {
    	if (segFontName == null) return false;
    	
    	if (Utils.containsSubstring(segFontName, "Bold") ||
    		Utils.containsSubstring(segFontName, "bold") ||
    		Utils.containsSubstring(segFontName, "Black") ||
    		Utils.containsSubstring(segFontName, "black") ||
    		Utils.containsSubstring(segFontName, "Heavy") ||
    		Utils.containsSubstring(segFontName, "heavy"))
    		return true;
    	else return false;
    }
    
    public boolean isItalic()
    {
    	if (segFontName == null) return false;
    	
    	if (Utils.containsSubstring(segFontName, "Italic") ||
    		Utils.containsSubstring(segFontName, "italic") ||
    		Utils.containsSubstring(segFontName, "Cursive") ||
    		Utils.containsSubstring(segFontName, "cursive") ||
    		Utils.containsSubstring(segFontName, "Kursiv") ||
    		Utils.containsSubstring(segFontName, "kursiv"))
    		return true;
    	else return false;
    }
    
    public String toSBText()
	{
    	return "ID: " + this.getID();
	}
    
    public GenericSegment toGenericSegment()
    {
    	if (isTextSegment())
    		return new TextSegment(segX1, segX2, segY1, segY2, segText, segFontName, segFontSize);
    	else
    		return new GenericSegment(segX1, segX2, segY1, segY2);
    }
    
    /*
	public int getSegID() {
		return segID;
	}

	public void setSegID(int segID) {
		this.segID = segID;
	}
	*/
    
	public boolean isMatchFont() {
		return matchFont;
	}

	public void setMatchFont(boolean matchFont) {
		this.matchFont = matchFont;
	}

	public boolean isMatchFontSize() {
		return matchFontSize;
	}

	public void setMatchFontSize(boolean matchFontSize) {
		this.matchFontSize = matchFontSize;
	}

	public boolean isMatchBold() {
		return matchBold;
	}

	public void setMatchBold(boolean matchBold) {
		this.matchBold = matchBold;
	}

	public boolean isMatchItalic() {
		return matchItalic;
	}

	public void setMatchItalic(boolean matchItalic) {
		this.matchItalic = matchItalic;
	}

	public int getMatchContent() {
		return matchContent;
	}

	public void setMatchContent(int matchContent) {
		this.matchContent = matchContent;
	}

	public String getMatchContentString() {
		return matchContentString;
	}

	public void setMatchContentString(String matchContentString) {
		this.matchContentString = matchContentString;
	}

	public int getMatchMaxLength() {
		return matchMaxLength;
	}

	public void setMatchMaxLength(int matchMaxLength) {
		this.matchMaxLength = matchMaxLength;
	}

	public int getMatchMinLength() {
		return matchMinLength;
	}

	public void setMatchMinLength(int matchMinLength) {
		this.matchMinLength = matchMinLength;
	}

	public boolean isExtractContent() {
		return extractContent;
	}

	public void setExtractContent(boolean extractContent) {
		this.extractContent = extractContent;
	}

	public boolean isRemoveFromInstance() {
		return removeFromInstance;
	}

	public void setRemoveFromInstance(boolean removeFromInstance) {
		this.removeFromInstance = removeFromInstance;
	}

	public boolean isFoundInstance() {
		return foundInstance;
	}

	public void setFoundInstance(boolean foundInstance) {
		this.foundInstance = foundInstance;
	}

	public boolean isExampleInstance() {
		return exampleInstance;
	}

	public void setExampleInstance(boolean exampleInstance) {
		this.exampleInstance = exampleInstance;
	}

	public String getSegType() {
		return segType;
	}

	public void setSegType(String segType) {
		this.segType = segType;
	}

	public float getSegX1() {
		return segX1;
	}

	public void setSegX1(float segX1) {
		this.segX1 = segX1;
	}

	public float getSegX2() {
		return segX2;
	}

	public void setSegX2(float segX2) {
		this.segX2 = segX2;
	}
	
	public float segXmid()
	{
		return Utils.avg(segX1, segX2);
	}

	public float getSegY1() {
		return segY1;
	}

	public void setSegY1(float segY1) {
		this.segY1 = segY1;
	}

	public float getSegY2() {
		return segY2;
	}

	public void setSegY2(float segY2) {
		this.segY2 = segY2;
	}
	
	public float segYmid()
	{
		return Utils.avg(segY1, segY2);
	}

	public String getSegText() {
		return segText;
	}

	public void setSegText(String segText) {
		this.segText = segText;
	}

	public String getSegFontName() {
		return segFontName;
	}

	public void setSegFontName(String segFontName) {
		this.segFontName = segFontName;
	}

	public float getSegFontSize() {
		return segFontSize;
	}

	public void setSegFontSize(float segFontSize) {
		this.segFontSize = segFontSize;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public int getWidth() {
    	lbl = getLabel();
        if(fontMetrics!=null && lbl!=null) {
            if(typ!=TYPE_ELLIPSE) 
                return fontMetrics.stringWidth(lbl) + 8;
            else
                return fontMetrics.stringWidth(lbl) + 28;
        }
        else
            return 8;
    }
    
    public int getHeight() {
        if (fontMetrics!=null) 
            return fontMetrics.getHeight()+2;
        else 
            return 8;                   
    }
    
    Color myBrighter(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        
        if(b>r+64&&b>g+64) {r+=32;g+=32;} 
         
        r=Math.min(r+144, 255);
        g=Math.min(g+144, 255);
        b=Math.min(b+144, 255);
        
        return new Color(r,g,b);   
    }

    public void paint(Graphics g, TGPanel tgPanel) {
    	Graphics2D g2d = (Graphics2D)g;
    	// TODO: move elsewhere
		g2d.setRenderingHints(Utils.hints);
		
        if (!intersects(tgPanel.getSize()) ) return;
        paintNodeBody(g, tgPanel);
        
        int ix = (int)drawx;
        int iy = (int)drawy;
        int h = getHeight();
        int w = getWidth();
                
        if ( visibleEdgeCount()<edgeCount() ) {
            int tagX = ix+(w-6)/2-2+w%2;
            int tagY = iy-h/2-3;
            String hiddenEdgeStr = String.valueOf(edgeCount()-visibleEdgeCount());            
            g.setColor(Color.red);
            g.fillRect(tagX, tagY, 3+5*hiddenEdgeStr.length(), 8);
            g.setColor(Color.white);
            g.setFont(SMALL_TAG_FONT);
            g.drawString(hiddenEdgeStr, tagX+2, tagY+7);
        }
    }

    public Color getPaintUnselectedBackColor() {
            if (fixed) return BACK_FIXED_COLOR;
            if (markedForRemoval) return backColor.darker().darker();
            if (justMadeLocal) return myBrighter(backColor);
            return backColor;            
    }
        
    public Color getPaintTextColor(TGPanel tgPanel) {
          return textColor;
    }    
    
    public Color getPaintBackColor(TGPanel tgPanel)
    {
    	Color col;

    	if (extractContent)
    	{
			col = Color.YELLOW;
    	}
		else
    	{
			boolean isMatchContent = 
				(matchContent != MATCH_CONTENT_OFF ||
				matchMinLength != -1 || matchMaxLength != -1);
			
		    col = getPaintUnselectedBackColor();
		    boolean matchTypography =
		    	isMatchFont() || isMatchFontSize() || isMatchBold() || isMatchItalic();
		    if (matchTypography && !isMatchContent)
		    		col = Color.MAGENTA.darker();
		    else if (matchTypography && isMatchContent)
		    		col = Color.PINK.darker();
		    else if (!matchTypography && isMatchContent)
		    		col = Color.cyan.darker();
        }
    	
    	if ( this == tgPanel.getSelect() ) {
    		col = col.darker();
    	}
    	if (isRemoveFromInstance())
    		col = myBrighter(col);//.brighter();
    	
    	return col;
    }
    
    public Color getPaintBorderColor(TGPanel tgPanel) {
        if ( this == tgPanel.getSelect() ) {
            if (fixed) return BACK_FIXED_COLOR;
            if (markedForRemoval) return new Color(100,60,40);
            if (justMadeLocal) return new Color(255,220,200);
            if (isRemoveFromInstance())	return myBrighter(backColor);
            return backColor;            
        } else {
            return super.getPaintBorderColor(tgPanel); // white
        }
    }
}
