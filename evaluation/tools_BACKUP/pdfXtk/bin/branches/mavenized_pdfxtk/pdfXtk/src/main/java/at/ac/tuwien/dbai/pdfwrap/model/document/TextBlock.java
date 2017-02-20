/**
 * pdfXtk - PDF Extraction Toolkit
 * Copyright (c) by the authors/contributors.  All rights reserved.
 * This project includes code from PDFBox and TouchGraph.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the names pdfXtk or PDF Extraction Toolkit; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://pdfxtk.sourceforge.net
 *
 */
package at.ac.tuwien.dbai.pdfwrap.model.document;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Default granular object for segmentation output
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class TextBlock extends CompositeSegment<TextLine> 
	implements IXHTMLSegment
{
	// Generic TextBlock stuff
//	protected List<TextLine> items;
	
    protected int textAlignment;
    protected float lineSpacing;
    
    public float strXPosNewline = -1.0f;
    
    public final static int NO_CLASSIFICATIONS = 3;
    
    public final static int ALIGN_LCR = 31;
    public final static int ALIGN_LC = 32;
    public final static int ALIGN_CR = 33;
    public final static int ALIGN_L = 34;
    public final static int ALIGN_C = 35;
    public final static int ALIGN_R = 36;
    public final static int ALIGN_NONE = 37;
    public final static int ALIGN_UNSET = 0;
    
    
    // Paragraph stuff
    protected int classification;
    public final static int PARAGRAPH = 0;
    
    // this is only for backwards compatibility
    // with older stuff -- HEADING should be
    // updated to be a more specific OTHER_TEXT...
    public final static int HEADING = 1;
    public final static int BODY = 0;
    
    // these classifications ONLY for str. mode
    // as line-finding is carried out again
    public final static int BODY_TEXT = 40;
    public final static int HEADING_1 = 41;
    public final static int HEADING_2 = 42;
    public final static int HEADING_3 = 43;
    public final static int ORDERED_LIST_ITEM = 51;
    public final static int UNORDERED_LIST_ITEM = 52;
    
    //public final static int MISC = 2;
    
    // changed 29.10.06, as differing
    // between heading and misc will require
    // generic knowledge (i.e. avg. font size of page)
    
    public final static int OTHER_TEXT = 9;
    // this is to include headings, captions and other
    // misc stuff
    // headings can later be detected (easy!)
    
    public final static int CELL = 2;
    
    
	public TextBlock()
    {
        super();
    }
    
    public TextBlock(List<TextLine> items)
    {
    	super();
//      super(theItems);
    	this.items = items;
    }
    
    public TextBlock(
        float x1,
        float x2,
        float y1,
        float y2,
        String text,
        String fontName,
        float fontSize
        )
    {
		super(x1, x2, y1, y2, text, fontName, fontSize);
    }
    
    public TextBlock(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }

    public TextBlock(
        float x1,
        float x2,
        float y1,
        float y2,
        String text,
        String fontName,
        float fontSize,
		List<TextLine> items
        )
    {
//		super(x1, x2, y1, y2, text, fontName, fontSize, items);
    	super(x1, x2, y1, y2, text, fontName, fontSize);
    	this.items = items;
    }
    
    public TextBlock(
        float x1,
        float x2,
        float y1,
        float y2,
        List<TextLine> items
        )
    {
//		super(x1, x2, y1, y2, items);
    	super(x1, x2, y1, y2);
    	this.items = items;
    }
    
    // IXMillumSegment
	public void setElementAttributes(Document resultDocument, 
    	Element newSegmentElement, GenericSegment pageDim, float resolution)
    {
        super.setElementAttributes(resultDocument, newSegmentElement, pageDim, resolution);
        
        // TODO: HACK -- the below lines refer to the this.getText() method, as the
        // text currently is not stored.  But this is due to change when the
        // line-finding is integrated.
        newSegmentElement.setAttribute
            ("font-size", Float.toString(this.getFontSize()));
//	        newSegmentElement.setAttribute
//	            ("text-ratio", Float.toString(this.getTextRatio()));
//	        newSegmentElement.setAttribute
//	        	("info", getInfoString());
        
        String type = "unknown";
        switch(classification)
        {
        	case PARAGRAPH:
        		type = "paragraph"; break;
        	case HEADING:
        		type = "heading"; break;
        	case OTHER_TEXT:
        		type = "other-text"; break;
        	case CELL:
        		type = "cell"; break;
        	default:
        		type = "error";
        }
        
        newSegmentElement.setAttribute("type", type);
        
        // System.out.println("creating text node: " + this.getText());
        // done in super!
        //newSegmentElement.appendChild
            //(resultDocument.createTextNode(this.getText()));
    }

	public void addAsXHTML(Document resultDocument, Element parent)//, GenericSegment pageDim)
    {
        Element newParagraphElement, newTextElement, tempElement = null;
        
        if (classification == HEADING || classification == HEADING_3)
            newParagraphElement = resultDocument.createElement("h3");
        else if (classification == HEADING_2)
            newParagraphElement = resultDocument.createElement("h2");
        else if (classification == HEADING_1)
            newParagraphElement = resultDocument.createElement("h1");
        else if (classification == UNORDERED_LIST_ITEM)
        {
//        	tempElement = resultDocument.createElement("ul");
//        	newParagraphElement = resultDocument.createElement("li");
        	// 22.01.2011 <ul>s are now separate objects
        	newParagraphElement = resultDocument.createElement("li");
        }
        else
            newParagraphElement = resultDocument.createElement("p");

        // HEADING_1 to HEADING_3 in str mode
        if (classification >= 40 && classification < 60)
        {
        	boolean bold = false;
        	boolean italic = false;
        	boolean underlined = false;
        	int superSubscript = 0;
        	String textToAdd = "";
        	float prevX2 = -1.0f;
        	
        	Iterator iter1 = items.iterator();
        	while(iter1.hasNext())
        	{
        		TextLine tl1 = (TextLine)iter1.next();
//        		System.out.println("tl1: " + tl1);
        		if (tl1.getX1() < prevX2)
				{
					// new line: do we insert a carriage return?
					if (prevX2 < strXPosNewline)
						textToAdd = textToAdd + ("\n");
				}
				prevX2 = tl1.getX2();
				
        		Iterator iter2 = tl1.getItems().iterator();
        		while(iter2.hasNext())
        		{
        			TextLine tl2 = (TextLine)iter2.next();
//        			System.out.println("tl2: " + tl2);
        			TextFragment prevFrag = null;
        			Iterator iter3 = tl2.getItems().iterator();
        			while(iter3.hasNext())
        			{
        				TextFragment tf = (TextFragment)iter3.next();
//        				System.out.println("tf: " + tf);
//        				System.out.println("tf is superSubscript: " + tf.isStrIsUnderlined());
        				
        				// if neither matches the whitespace character
        				// and horiz gap > 0.25(afs)
        				if (prevFrag != null)
        				{
        					float horizGap = tf.getX1() - prevFrag.getX2();
        					float afs = (tf.getFontSize() + prevFrag.getFontSize()) / 2.0f;
        					if (!(tf.getText().trim().matches("[\\s]") || prevFrag.getText().trim().matches("[\\s]")) &&
        						horizGap > afs * 0.15f)
        					{
        						textToAdd = textToAdd + " ";
        					}
        				}	
        				
        				if (tf.isBold() == bold && tf.isItalic() == italic && 
        					tf.isUnderlined() == underlined && tf.getSuperSubscript() == superSubscript)
        				{
        					// same style as previous character
//        					textToAdd.concat(tf.getText()); // doesn't work?!?
        					textToAdd = textToAdd + (tf.getText());
        				}
        				else
        				{
        					// add text
//        					if (textToAdd.length() > 0)
        					if (textToAdd.trim().length() > 0)
        					{
        						if (superSubscript == 1)
        						{
	        						if (underlined)
	        						{
	//        							System.out.println("underlined with textToAdd: " + textToAdd);
	//	        		        		System.out.println("textToAdd.length: " + textToAdd.trim().length());
	        							if (bold && italic)
	    	        		        	{
	        								Element newTextElement4 = resultDocument.createElement("sup");
	        								newParagraphElement.appendChild(newTextElement4);
	        								Element newTextElement3 = resultDocument.createElement("u");
	    	        		        		newTextElement4.appendChild(newTextElement3);
	    	        		            	Element newTextElement2 = resultDocument.createElement("b");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (bold)
	    	        		        	{
	    	        		        		Element newTextElement3 = resultDocument.createElement("sup");
	    	        		        		newParagraphElement.appendChild(newTextElement3);
	    	        		        		Element newTextElement2 = resultDocument.createElement("u");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("b");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (italic)
	    	        		        	{
	    	        		        		Element newTextElement3 = resultDocument.createElement("sup");
	    	        		        		newParagraphElement.appendChild(newTextElement3);
	    	        		        		Element newTextElement2 = resultDocument.createElement("u");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else
	    	        		        	{
	    	        		        		Element newTextElement2 = resultDocument.createElement("sup");
	    	        		        		newParagraphElement.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("u");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	        						}
	        						else
	        						{
	        							if (bold && italic)
	    	        		        	{
	        								Element newTextElement3 = resultDocument.createElement("sup");
	        								newParagraphElement.appendChild(newTextElement3);
	    	        		            	Element newTextElement2 = resultDocument.createElement("b");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (bold)
	    	        		        	{
	    	        		        		Element newTextElement2 = resultDocument.createElement("sup");
	    	        		        		newParagraphElement.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("b");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (italic)
	    	        		        	{
	    	        		        		Element newTextElement2 = resultDocument.createElement("sup");
	    	        		        		newParagraphElement.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else
	    	        		        	{
	    	        		        		newTextElement = resultDocument.createElement("sup");
	    	        		        		newParagraphElement.appendChild(newTextElement);
	    	        		        	}
	        						}
        						}
        						else if (superSubscript == -1)
        						{
//        							System.out.println("outputting subscript");
        							if (underlined)
	        						{
	//        							System.out.println("underlined with textToAdd: " + textToAdd);
	//	        		        		System.out.println("textToAdd.length: " + textToAdd.trim().length());
	        							if (bold && italic)
	    	        		        	{
	        								Element newTextElement4 = resultDocument.createElement("sub");
	        								newParagraphElement.appendChild(newTextElement4);
	        								Element newTextElement3 = resultDocument.createElement("u");
	    	        		        		newTextElement4.appendChild(newTextElement3);
	    	        		            	Element newTextElement2 = resultDocument.createElement("b");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (bold)
	    	        		        	{
	    	        		        		Element newTextElement3 = resultDocument.createElement("sub");
	    	        		        		newParagraphElement.appendChild(newTextElement3);
	    	        		        		Element newTextElement2 = resultDocument.createElement("u");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("b");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (italic)
	    	        		        	{
	    	        		        		Element newTextElement3 = resultDocument.createElement("sub");
	    	        		        		newParagraphElement.appendChild(newTextElement3);
	    	        		        		Element newTextElement2 = resultDocument.createElement("u");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else
	    	        		        	{
	    	        		        		Element newTextElement2 = resultDocument.createElement("sub");
	    	        		        		newParagraphElement.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("u");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	        						}
	        						else
	        						{
	        							if (bold && italic)
	    	        		        	{
	        								Element newTextElement3 = resultDocument.createElement("sub");
	        								newParagraphElement.appendChild(newTextElement3);
	    	        		            	Element newTextElement2 = resultDocument.createElement("b");
	    	        		        		newTextElement3.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (bold)
	    	        		        	{
	    	        		        		Element newTextElement2 = resultDocument.createElement("sub");
	    	        		        		newParagraphElement.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("b");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else if (italic)
	    	        		        	{
	    	        		        		Element newTextElement2 = resultDocument.createElement("sub");
	    	        		        		newParagraphElement.appendChild(newTextElement2);
	    	        		        		newTextElement = resultDocument.createElement("i");
	    	        		        		newTextElement2.appendChild(newTextElement);
	    	        		        	}
	    	        		        	else
	    	        		        	{
	    	        		        		newTextElement = resultDocument.createElement("sub");
	    	        		        		newParagraphElement.appendChild(newTextElement);
	    	        		        	}
	        						}
        						}
        						else // normal text (required in order to initialize newTextElement)
        						{
        							if (underlined)
            						{
//            							System.out.println("underlined with textToAdd: " + textToAdd);
//    	        		        		System.out.println("textToAdd.length: " + textToAdd.trim().length());
            							if (bold && italic)
        	        		        	{
            								Element newTextElement3 = resultDocument.createElement("u");
        	        		        		newParagraphElement.appendChild(newTextElement3);
        	        		            	Element newTextElement2 = resultDocument.createElement("b");
        	        		        		newTextElement3.appendChild(newTextElement2);
        	        		        		newTextElement = resultDocument.createElement("i");
        	        		        		newTextElement2.appendChild(newTextElement);
        	        		        	}
        	        		        	else if (bold)
        	        		        	{
        	        		        		Element newTextElement2 = resultDocument.createElement("u");
        	        		        		newParagraphElement.appendChild(newTextElement2);
        	        		        		newTextElement = resultDocument.createElement("b");
        	        		        		newTextElement2.appendChild(newTextElement);
        	        		        	}
        	        		        	else if (italic)
        	        		        	{
        	        		        		Element newTextElement2 = resultDocument.createElement("u");
        	        		        		newParagraphElement.appendChild(newTextElement2);
        	        		        		newTextElement = resultDocument.createElement("i");
        	        		        		newTextElement2.appendChild(newTextElement);
        	        		        	}
        	        		        	else
        	        		        	{
        	        		        		newTextElement = resultDocument.createElement("u");
        	        		        		newParagraphElement.appendChild(newTextElement);
        	        		        	}
            						}
            						else
            						{
            							if (bold && italic)
        	        		        	{
        	        		            	Element newTextElement2 = resultDocument.createElement("b");
        	        		        		newParagraphElement.appendChild(newTextElement2);
        	        		        		newTextElement = resultDocument.createElement("i");
        	        		        		newTextElement2.appendChild(newTextElement);
        	        		        	}
        	        		        	else if (bold)
        	        		        	{
//        	        		        		System.out.println("bold with textToAdd: " + textToAdd);
//        	        		        		System.out.println("textToAdd.length: " + textToAdd.trim().length());
        	        		        		newTextElement = resultDocument.createElement("b");
        	        		        		newParagraphElement.appendChild(newTextElement);
        	        		        	}
        	        		        	else if (italic)
        	        		        	{
        	        		        		newTextElement = resultDocument.createElement("i");
        	        		        		newParagraphElement.appendChild(newTextElement);
        	        		        	}
        	        		        	else
        	        		        	{
        	        		            	newTextElement = newParagraphElement;
        	        		        	}
            						}
        						}
	        		            
	        		            // the following lines would just add the string
	        		            // without <br/>s
	        		            //newColumnElement.appendChild
	        		    		//(resultDocument.createTextNode(theText));
	        		            String textSection = new String();
	        		            
	        		            for (int n = 0; n < textToAdd.length(); n ++)
	        		            {
	        		            	String thisChar = textToAdd.substring(n, n + 1);
	        		            	if (thisChar.equals("\n"))
	        		            	{
	        		            		newTextElement.appendChild
	        		            			(resultDocument.createTextNode(textSection));
	        		                    newTextElement.appendChild
	        		                    	(resultDocument.createElement("br"));
	        		                    textSection = "";
	        		            	}
	        		            	else
	        		            	{
	        		            		textSection = textSection.concat(thisChar);
	        		            	}
	        		            }
	        		            
	        		            if (textSection.length() > 0)
	        		            	
	        		            	newTextElement.appendChild
	        		            	(resultDocument.createTextNode(textSection));
        					
        					}
        					
        					// update bold and italic
        		            bold = tf.isBold();
        		            italic = tf.isItalic();
        		            underlined = tf.isUnderlined();
        		            textToAdd = "";
        		            textToAdd = textToAdd + (tf.getText());
        		            superSubscript = tf.getSuperSubscript();
        				}
        				prevFrag = tf;
        			}
        		}
        	}
        	
        	// if remaining text
        	if(textToAdd.trim().length() > 0)
        	{
        		if (bold && italic)
	        	{
	            	Element newTextElement2 = resultDocument.createElement("b");
	        		newParagraphElement.appendChild(newTextElement2);
	        		newTextElement = resultDocument.createElement("i");
	        		newTextElement2.appendChild(newTextElement);
	        	}
	        	else if (bold)
	        	{
	        		newTextElement = resultDocument.createElement("b");
	        		newParagraphElement.appendChild(newTextElement);
	        	}
	        	else if (italic)
	        	{
	        		newTextElement = resultDocument.createElement("i");
	        		newParagraphElement.appendChild(newTextElement);
	        	}
	        	else
	        	{
	            	newTextElement = newParagraphElement;
	        	}
	            
	            // the following lines would just add the string
	            // without <br/>s
	            //newColumnElement.appendChild
	    		//(resultDocument.createTextNode(theText));
	            String textSection = new String();
	            
	            for (int n = 0; n < textToAdd.length(); n ++)
	            {
	            	String thisChar = textToAdd.substring(n, n + 1);
	            	if (thisChar.equals("\n"))
	            	{
	            		newTextElement.appendChild
	            			(resultDocument.createTextNode(textSection));
	                    newTextElement.appendChild
	                    	(resultDocument.createElement("br"));
	                    textSection = "";
	            	}
	            	else
	            	{
	            		textSection = textSection.concat(thisChar);
	            	}
	            }
	            
	            if (textSection.length() > 0)
	            	
	            	newTextElement.appendChild
	            	(resultDocument.createTextNode(textSection));

        	}
        	
        }
        else // normal mode
        {
        	if (isBold() && isItalic())
        	{
            	Element newTextElement2 = resultDocument.createElement("b");
        		newParagraphElement.appendChild(newTextElement2);
        		newTextElement = resultDocument.createElement("i");
        		newTextElement2.appendChild(newTextElement);
        	}
        	else if (isBold())
        	{
        		newTextElement = resultDocument.createElement("b");
        		newParagraphElement.appendChild(newTextElement);
        	}
        	else if (isItalic())
        	{
        		newTextElement = resultDocument.createElement("i");
        		newParagraphElement.appendChild(newTextElement);
        	}
        	else
        	{
            	newTextElement = newParagraphElement;
        	}
            
            String theText = this.getText();
            // the following lines would just add the string
            // without <br/>s
            //newColumnElement.appendChild
    		//(resultDocument.createTextNode(theText));
            String textSection = new String();
            
            for (int n = 0; n < theText.length(); n ++)
            {
            	String thisChar = theText.substring(n, n + 1);
            	if (thisChar.equals("\n"))
            	{
            		newTextElement.appendChild
            			(resultDocument.createTextNode(textSection));
                    newTextElement.appendChild
                    	(resultDocument.createElement("br"));
                    textSection = "";
            	}
            	else
            	{
            		textSection = textSection.concat(thisChar);
            	}
            }
            
            if (textSection.length() > 0)
            	
            	newTextElement.appendChild
            	(resultDocument.createTextNode(textSection));
        }

        /*
        newParagraphElement.appendChild
            (resultDocument.createTextNode(this.getText()));
        */
        
        // 22.01.2011 <ul>s are now separate objects
        /*
        if (classification == UNORDERED_LIST_ITEM)
        {
        	tempElement.appendChild(newParagraphElement);
        	parent.appendChild(tempElement);
        }
        else
        */
        	parent.appendChild(newParagraphElement);
    }
	
	public int getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	public float getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	public float getStrXPosNewline() {
		return strXPosNewline;
	}

	public void setStrXPosNewline(float strXPosNewline) {
		this.strXPosNewline = strXPosNewline;
	}

}
