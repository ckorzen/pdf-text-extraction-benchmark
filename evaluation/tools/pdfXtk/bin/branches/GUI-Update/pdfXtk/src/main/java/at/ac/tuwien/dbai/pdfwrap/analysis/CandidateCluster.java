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
package at.ac.tuwien.dbai.pdfwrap.analysis;

import at.ac.tuwien.dbai.pdfwrap.model.document.CompositeSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Candidate cluster used in segmentation algorithm
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class CandidateCluster extends CompositeSegment<TextSegment>
{
	protected boolean constantFont = true;
	protected boolean constantFontSize = true;
	protected float absLineSpacing;
	
    protected int textAlignment;
    protected boolean constantLS;
    protected boolean constantFS;
    protected boolean constantGS;
    protected boolean uniqueLines;
    
    protected boolean strContainsSuperSubscript = false;
	
	protected List<CompositeSegment<? extends TextSegment>> foundLines;
	
    public final static int ALIGN_LCR = 31;
    public final static int ALIGN_LC = 32;
    public final static int ALIGN_CR = 33;
    public final static int ALIGN_L = 34;
    public final static int ALIGN_C = 35;
    public final static int ALIGN_R = 36;
    public final static int ALIGN_NONE = 37;
    public final static int ALIGN_UNSET = 0;
    
	/**
	 * Constructor.
	 * 
	 * @param x1
	 *            The x1 coordinate of the segment.
	 * @param x2
	 *            The x2 coordinate of the segment.
	 * @param y1
	 *            The y1 coordinate of the segment.
	 * @param y2
	 *            The y2 coordinate of the segment.
	 * @param text
	 *            The textual contents of the segment.
	 * @param font
	 *            The (main) font of the segment.
	 * @param fontSize
	 *            The (main) font size in the segment.
	 */
	/* 30.11.06: these constructors appear to be useless*/
	// 1.12.06: but they are used by TextBlock...
	public CandidateCluster(float x1, float x2, float y1, float y2,
			String text, String fontName, float fontSize)
	{
		super(x1, x2, y1, y2, text, fontName, fontSize);
		this.items = new ArrayList<TextSegment>();
	}
	

	public CandidateCluster(float x1, float x2, float y1, float y2)
	{
		super(x1, x2, y1, y2);
		this.items = new ArrayList<TextSegment>();
	}

	public CandidateCluster(float x1, float x2, float y1, float y2,
			String text, String fontName, float fontSize, List<TextSegment> items)
	{
		super(x1, x2, y1, y2, text, fontName, fontSize);
		this.items = items;
	}

	public CandidateCluster(float x1, float x2, float y1, float y2,
			List<TextSegment> items)
	{
		super(x1, x2, y1, y2);
		this.items = items;
	}

	public CandidateCluster(List<TextSegment> items)
	{
		super();
		this.items = items;
	}

	public CandidateCluster()
	{
		// most common method if initialization now
		// the fields are filled once all the items have
		// been added...
		super();
		this.items = new ArrayList<TextSegment>();
	}


	public boolean isConstantFont() {
		return constantFont;
	}


	public void setConstantFont(boolean constantFont) {
		this.constantFont = constantFont;
	}


	public boolean isConstantFontSize() {
		return constantFontSize;
	}


	public void setConstantFontSize(boolean constantFontSize) {
		this.constantFontSize = constantFontSize;
	}

	public float getRelLineSpacing() {
		return (absLineSpacing / fontSize);
	}

	public float getAbsLineSpacing() {
		return absLineSpacing;
	}


	public void setAbsLineSpacing(float absLineSpacing) {
		this.absLineSpacing = absLineSpacing;
	}


	public int getTextAlignment() {
		return textAlignment;
	}


	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	public boolean isLeftAligned()
	{
		return textAlignment == ALIGN_L ||
			textAlignment == ALIGN_LC ||
			textAlignment == ALIGN_LCR;
	}
	
	public boolean isCentreAligned()
	{
		return textAlignment == ALIGN_C ||
			textAlignment == ALIGN_LC ||
			textAlignment == ALIGN_CR ||
			textAlignment == ALIGN_LCR;
	}
	
	public boolean isRightAligned()
	{
		return textAlignment == ALIGN_R ||
			textAlignment == ALIGN_CR ||
			textAlignment == ALIGN_LCR;
	}

	public boolean isConstantLS() {
		return constantLS;
	}


	public void setConstantLS(boolean constantLS) {
		this.constantLS = constantLS;
	}


	public boolean isConstantFS() {
		return constantFS;
	}


	public void setConstantFS(boolean constantFS) {
		this.constantFS = constantFS;
	}


	public boolean isConstantGS() {
		return constantGS;
	}


	public void setConstantGS(boolean constantGS) {
		this.constantGS = constantGS;
	}


	public boolean isUniqueLines() {
		return uniqueLines;
	}


	public void setUniqueLines(boolean uniqueLines) {
		this.uniqueLines = uniqueLines;
	}


	public boolean isStrContainsSuperSubscript() {
		return strContainsSuperSubscript;
	}


	public void setStrContainsSuperSubscript(boolean strContainsSuperSubscript) {
		this.strContainsSuperSubscript = strContainsSuperSubscript;
	}


	public List<CompositeSegment<? extends TextSegment>> getFoundLines() {
		return foundLines;
	}


	public void setFoundLines
		(List<CompositeSegment<? extends TextSegment>> foundLines) {
		this.foundLines = foundLines;
	}

	public void findLines()
    {
    	findLines(0.5f);
    }
    
    public void findLinesWidth()
    {
    	findLines(Float.MAX_VALUE);
    }
    
	public void findLines(float horizThreshold)
	{
		foundLines = 
			LineProcessor.findLines(items, horizThreshold, true, false);
		processLines();
	}
	
	public void setCalculatedFields()
    {
    	//TODO: e.g. super.setCalculatedFields();
    	//findLines();
    	findLinesWidth(); // TODO: does this replacement cause a problem?
    	
//    	System.out.println("foundLinesWidth: " + foundLines);
    	
    	processLines();
    	
    	findFontSize(); // TODO: with processLines now redundant :(
    	findBoundingBox(); // NOT WITH NEW METHOD HERE!  
    					   // 17.01.07 done automatically now during findLines
    	findText();
    }
	
    public void processLines()
    {
    	if (foundLines.size() > 1)
    	{
    		// first find averages
    		
    		float avgX1 = 0.0f;
    		float avgXcen = 0.0f;
    		float avgX2 = 0.0f;
    		float afs = 0.0f;
	    	float als = 0.0f;
    		boolean clashingLines = false;
	    	
    		CompositeSegment<? extends TextSegment> prevLine = null;
	    	for (CompositeSegment<? extends TextSegment> l : foundLines)
	    	{
	    		avgX1 += l.getX1();
	    		avgXcen += l.getXmid();
	    		avgX2 += l.getX2();
	    		afs += l.getFontSize();
	    		
	    		if (prevLine != null)
	    		{
	    			float lineSpacing = prevLine.getY1() - l.getY1();
	    			als += lineSpacing;
	    			if (SegmentUtils.vertIntersect(prevLine,l.getYmid())) clashingLines = true;
	    		}
	    		prevLine = l;
	    	}
    		
    		avgX1 /= foundLines.size();
    		avgXcen /= foundLines.size();
    		avgX2 /= foundLines.size();
    		afs /= foundLines.size();
    		fontSize = afs;
    		als /= (foundLines.size() - 1);
    		
//    		System.out.println("setting als to: " + als);
    		
//    		lineSpacing = als;
    		absLineSpacing = als;///afs; // changed 30.10.10
    		
    		// now, see if they are within allowed error
    		boolean constantX1 = true, 
    			constantXcen = true, 
    			constantX2 = true, 
    			constantfs = true, 
    			constantls = true;
    		float tolerance = afs * 0.5f;
    		prevLine = null;
    		for (CompositeSegment<? extends TextSegment> l : foundLines)
	    	{
	    		if (!Utils.within(l.getX1(), avgX1, tolerance)) constantX1 = false;
	    		if (!Utils.within(l.getXmid(), avgXcen, tolerance)) constantXcen = false;
	    		if (!Utils.within(l.getX2(), avgX2, tolerance)) constantX2 = false;
	    		if (!Utils.within(l.getFontSize(), afs, afs * 0.1f)) constantfs = false;
	    		
	    		if (prevLine != null)
	    		{
	    			float lineSpacing = prevLine.getY1() - l.getY1();
	    			if (!Utils.within(lineSpacing, als, afs * 0.2f)) constantls = false;
	    		}
	    		prevLine = l;
	    	}
    		if (constantX1 && constantX2)
    			textAlignment = ALIGN_LCR;
    		else if (constantX1 && constantXcen)
    			textAlignment = ALIGN_LC;
    		else if (constantXcen && constantX2)
    			textAlignment = ALIGN_CR;
    		else if (constantX1)
    			textAlignment = ALIGN_L;
    		else if (constantXcen)
    			textAlignment = ALIGN_C;
    		else if (constantX2)
    			textAlignment = ALIGN_R;
    		else textAlignment = ALIGN_NONE;
    		
    		if (constantls) constantLS = true;
    			else constantLS = false;
    		if (constantfs) constantFS = true;
    			else constantFS = false;
    		if (!clashingLines) uniqueLines = true;
				else uniqueLines = false;
    		
//    		this.fontSize = afs;
    	}
    	else
    	{
    		// if singleton, or if no sub-objects etc.
    		textAlignment = ALIGN_LCR;
    	}
    }
    
 // TODO: in font size comparison, allow the usual error (use Utils.within 10%)
    // although whether that makes sense depends on which algorithm we use to work
    // out the font size :)
    
    // pre: findLinesWidth carried out and elements sorted in ascending order
    public TextSegment getTopElementMatchingFontsizeAfterSorting()
    {
    	TextSegment retVal = null;
    	for (TextSegment s : items)
    	{
    		for (CompositeSegment<? extends TextSegment> l : foundLines)
    		{
    			if (l.getItems().contains(s))
    			{
    				//if (s.getFontSize() == l.getFontSize())
    				if (Utils.within(s.getFontSize(), l.getFontSize(), s.getFontSize() * 0.1f))
    					return s;
    			}
    		}
    	}
    	return retVal;
    }
    
 // pre: findLinesWidth carried out and elements sorted in ascending order
    public TextSegment getBottomElementMatchingFontsizeAfterSorting()
    {
    	TextSegment retVal = null;
    	for (int n = items.size() - 1; n >= 0; n --)
    	{
    		TextSegment s = items.get(n);
    		for (CompositeSegment<? extends TextSegment> l : foundLines)
    		{
    			if (l.getItems().contains(s))
    			{
    				//if (s.getFontSize() == l.getFontSize())
    				if (Utils.within(s.getFontSize(), l.getFontSize(), s.getFontSize() * 0.1f))
    					return s;
    			}
    		}
    	}
    	return retVal;
    }
}
