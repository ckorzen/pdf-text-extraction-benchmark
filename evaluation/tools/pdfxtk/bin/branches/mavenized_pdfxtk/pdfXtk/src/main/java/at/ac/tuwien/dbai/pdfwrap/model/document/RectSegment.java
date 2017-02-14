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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * Rectangular segment document element; represents
 * a rectangle drawn using ruling lines on the page
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class RectSegment extends GenericSegment
{
	/**
     * Constructor.
     *
     * @param x1 The x1 coordinate of the segment.
     * @param x2 The x2 coordinate of the segment.
     * @param y1 The y1 coordinate of the segment.
     * @param y2 The y2 coordinate of the segment.
     */
	
	protected Color fillColor;
	protected Color lineColor;
	protected boolean isFilled;
	
    public RectSegment(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
		this.isFilled = false;
    }
    
    public List<LineSegment> toLines()
    {
    	ArrayList<LineSegment> retVal = new ArrayList<LineSegment>();
    	
    	// if this rectangle is just a THICK LINE
    	if (this.getWidth() < 3.0f || this.getHeight() < 3.0f)
    	{
    		// the constructor of Line automatically
    		// determines its direction.
            retVal.add(new LineSegment(this.getX1(), this.getX2(), 
                    this.getY1(), this.getY2()));
            return retVal;
    	}
    	else
    	{
    		// add top line
    		retVal.add(new LineSegment(x1, x2, y2, y2));
    		// add bottom line
    		retVal.add(new LineSegment(x1, x2, y1, y1));
    		// add left line
    		retVal.add(new LineSegment(x1, x1, y1, y2));
    		// add right line
    		retVal.add(new LineSegment(x2, x2, y1, y2));
    		return retVal;
    	}
    }

	public Color getFillColor()
	{
		return fillColor;
	}

	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	public boolean isFilled()
	{
		return isFilled;
	}

	public void setFilled(boolean isFilled)
	{
		this.isFilled = isFilled;
	}

	public Color getLineColor()
	{
		return lineColor;
	}

	public void setLineColor(Color lineColor)
	{
		this.lineColor = lineColor;
	}
	
	public String tagName()
	{
		if (isFilled)
			return "filled-rect";
		else return super.tagName();
	}
}
