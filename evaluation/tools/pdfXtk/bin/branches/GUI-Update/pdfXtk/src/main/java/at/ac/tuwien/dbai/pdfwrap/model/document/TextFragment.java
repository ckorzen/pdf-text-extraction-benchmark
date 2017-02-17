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

import at.ac.tuwien.dbai.pdfwrap.utils.ListUtils;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.TextPosition;

import java.util.ArrayList;
import java.util.List;



/**
 * Text fragment element; represents an atomic fragment corresponding
 * to one COS instruction
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class TextFragment extends CompositeSegment<CharSegment>
{
	// TODO: what about rotated text/text with negative width/height?

	// should these variables be included even in the base class?
	/*
	float xScale;
	float yScale;
	float wordSpacing;
	float widthOfSpace;
	*/
	boolean overprint = false;

//	2011-01-24 unnecessary
//	protected String tagName = "text-fragment";
	
    /**
     * Constructor.
     *
     * @param x1 The x1 coordinate of the segment.
     * @param x2 The x2 coordinate of the segment.
     * @param y1 The y1 coordinate of the segment.
     * @param y2 The y2 coordinate of the segment.
     * @param text The textual contents of the segment.
     * @param font The (main) font of the segment.
     * @param fontSize The (main) font size in the segment.
     */
    public TextFragment(
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
    
    public TextFragment(
            float x1,
            float x2,
            float y1,
            float y2,
            String text,
            PDFont font,
            float fontSize
            )
        {
    		super(x1, x2, y1, y2, text, findFontName(font), fontSize);
        }
    
    public TextFragment(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }
    
    public TextFragment()
    {
		super();
    }
    
    public TextFragment(CharSegment c)
    {
		super(c.getX1(), c.getX2(), c.getY1(), c.getY2());
		items.add(c);
		text = c.getText();
		fontName = c.getFontName();
		fontSize = c.getFontSize();
    }

	// not in current use (I think) -- WRONG
    // now by default sets level to zero (primitive)
    public TextFragment(TextPosition tPos)
    {
		super(tPos.getX(),
			  tPos.getX() + (tPos.getWidth()),
			  tPos.getY(),
			  tPos.getY() + (tPos.getFontSize() * tPos.getYScale()),
			  tPos.getCharacter(),
			  findFontName(tPos.getFont()),
			  tPos.getFontSize() * tPos.getYScale());


    	// todo: trim the name of the font
    	String fontName = tPos.getFont().getBaseFont();
    }

	/**
     * This will create a TextFragment object from a TextPosition object.
     * As of PDFBox 0.7.2, this is the method currently in use, which
	 * converts co-ordinates back to the original system.
	 *
     * @param tPos - the TextPosition object; pageDim - page dimensions in order to
	 * convert co-ordinates
	 * @return The new TextFragment object
     */
    public TextFragment(TextPosition tPos, GenericSegment pageDim)
    {
		super(tPos.getX(),
			  tPos.getX() + tPos.getWidth(),
			  pageDim.getY2() - tPos.getY(),
			  pageDim.getY2() - tPos.getY() + (tPos.getFontSize() * tPos.getYScale()),
			  tPos.getCharacter(),
			  tPos.getFont().getBaseFont(),
			  tPos.getFontSize() * tPos.getYScale());

		// uncomment to print the contents of all text fragments to the screen
		// System.out.println("Created text fragment: x1: " + tPos.getX() + " x2: " + (tPos.getX() + tPos.getWidth()) + " y1: " + tPos.getY() + " y2: " + (tPos.getY() + (tPos.getFontSize() * tPos.getYScale())) + " Text: " + text + " Font size: " + tPos.getFontSize() + " X Scale: " + tPos.getYScale() + " Y Scale: " + tPos.getYScale());

    	// todo: trim the name of the font
    	String fontName = tPos.getFont().getBaseFont();
		/*
		this.xScale = tPos.getXScale();
		this.yScale = tPos.getYScale();
		this.widthOfSpace = tPos.getWidthOfSpace();
		this.wordSpacing = tPos.getWordSpacing();
		*/
    }

    protected static String findFontName(PDFont font)
    {
    	if (font.getBaseFont().matches("^[A-Z]{6}\\+.+"))
    		return font.getBaseFont().substring(7);
    	else return font.getBaseFont();
    }
    
    public List<OpTuple> sourceOps()
    {
    	List<OpTuple> retVal = new ArrayList<OpTuple>();
    	
    	for (CharSegment cs : items)
    		retVal.add(cs.getSourceOp());
    	
    	ListUtils.removeDuplicates(retVal);
    	return retVal;
    }
    
	public boolean isOverprint() {
		return overprint;
	}

	public void setOverprint(boolean overprint) {
		this.overprint = overprint;
	}
    
    /*
     * with the tagName stuff this should not be necessary
     * 
    public void addAsXML(Document resultDocument, Element parent, GenericSegment pageDim,
    	float resolution)
    {
        //TODO: find a better name for this element?
        Element newSegmentElement = resultDocument.createElement("text-fragment");
        super.setElementAttributes(resultDocument, newSegmentElement, pageDim, resolution);
        parent.appendChild(newSegmentElement);
    }
    */
	
	// 2011-11-02 overrides method from CompositeSegment
	// to ignore whitespace characters (with sometimes fancy coordinates)
	public void findBoundingBox()
	{
		boolean first = true;

		int noItems = 0;
		double fontSizeTotal = 0.0;

		for (CharSegment thisSegment : items)
		{
			if (!(thisSegment instanceof IBlankSegment) &&
				!(thisSegment.getText().equals(" ")))
			{
				if (thisSegment instanceof TextSegment)
				{
					noItems++;
					fontSizeTotal += ((TextSegment) thisSegment).getFontSize();
				}
	
				if (first)
				{
					x1 = thisSegment.getX1();
					x2 = thisSegment.getX2();
					y1 = thisSegment.getY1();
					y2 = thisSegment.getY2();
					first = false;
				} else
				{
					growBoundingBox(thisSegment);
				}
			}
		}
		if (noItems >= 0)
		{
			fontSize = (float) (fontSizeTotal / noItems);
		} else
		{
			fontSize = -1.0f;
		}
	}
}
