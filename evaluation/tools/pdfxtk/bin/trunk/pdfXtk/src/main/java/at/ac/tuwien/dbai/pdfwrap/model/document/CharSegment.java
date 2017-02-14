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

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.TextPosition;

import java.util.List;


/**
 * Text fragment element; represents a single character
 * This class is identical in functionality to TextFragment!
 * ... however, does not extend it in order to allow typing
 * of lists ...
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */

public class CharSegment extends TextSegment
{
	boolean overprint = false;
	protected OpTuple sourceOp;
	
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
    public CharSegment(
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
    
    public CharSegment(
            float x1,
            float x2,
            float y1,
            float y2,
            String text,
            PDFont font,
            float fontSize,
            OpTuple sourceOp
            )
        {
    		super(x1, x2, y1, y2, text, findFontName(font), fontSize);
    		this.sourceOp = sourceOp;
        }
    
    public CharSegment(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }

	// not in current use (I think) -- WRONG
    // now by default sets level to zero (primitive)
    public CharSegment(TextPosition tPos)
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
    public CharSegment(TextPosition tPos, GenericSegment pageDim)
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
    
    @Override
    public List<AttributeTuple> getAttributes() {

    	List<AttributeTuple> attributeList = super.getAttributes();

    	attributeList.add(new AttributeTuple("opindex", sourceOp.getOpIndex()));
    	attributeList.add(new AttributeTuple("argindex", sourceOp.getArgIndex()));

    	return attributeList;
    }
    
	public boolean isOverprint() {
		return overprint;
	}

	public void setOverprint(boolean overprint) {
		this.overprint = overprint;
	}

	public OpTuple getSourceOp() {
		return sourceOp;
	}

	public void setSourceOp(OpTuple sourceOp) {
		this.sourceOp = sourceOp;
	}
}
