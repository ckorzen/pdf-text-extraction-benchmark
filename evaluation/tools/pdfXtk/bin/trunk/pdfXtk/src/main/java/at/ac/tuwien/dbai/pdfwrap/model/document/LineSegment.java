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

import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

import java.awt.*;



/**
 * Line segment document element; represents a ruling line on the page
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class LineSegment extends GenericSegment
{
    public static final int DIR_HORIZ = 1;
    public static final int DIR_VERT = 2;
    public static final int DIR_OTHER = 0;
    
    protected Color color;
    boolean isCurve = false;
    
    protected int direction;
    
	/**
     * Constructor.
     *
     * @param x1 The x1 coordinate of the segment.
     * @param x2 The x2 coordinate of the segment.
     * @param y1 The y1 coordinate of the segment.
     * @param y2 The y2 coordinate of the segment.
     */
    public LineSegment(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
        
        // TODO: variance is hard-coded as 1pt -- make it relative to something...
        
        if (Utils.within(y1, y2, 1.0f)) // horizontal
        {
            direction = DIR_HORIZ;
//            l1 = x1;
//            l2 = x2;
//            t = (y1 + y2) / 2;
        }
        else if (Utils.within(x1, x2, 1.0f)) // vertical
        {
            direction = DIR_VERT;
//            l1 = y1;
//            l2 = y2;
//            t = (x1 + x2) / 2;
        }
        else // angled
        {
            // TODO: throw exception here, if an attempt is made to
            // access the co-ordinates?
            direction = DIR_OTHER;
//            l1 = -1;
//            l2 = -1;
//            t = -1;
        }
    }

    public String getNodeText()
    {
    	return "[Line]";
    }
    

    /**
     * @return Returns the direction.
     */
    public int getDirection() {
        return direction;
    }
    

    /**
     * @param direction The direction to set.
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }
    

    /**
     * @return Returns the l1.
     */
    public float getL1() {
    	if (direction == DIR_HORIZ) // horizontal
        {
        	return x1;
        }
        else if (direction == DIR_VERT) // vertical
        {
        	return y1;
        }
        else // angled
        {
        	return -1;
        }
    }
    

    /**
     * @return Returns the l2.
     */
    public float getL2() {
    	if (direction == DIR_HORIZ) // horizontal
        {
        	return x2;
        }
        else if (direction == DIR_VERT) // vertical
        {
        	return y2;
        }
        else // angled
        {
            // TODO: throw exception here, if an attempt is made to
            // access the co-ordinates?
        	return -1;
        }
    }
    

    /**
     * @return Returns the t.
     */
    public float getT() {
    	if (direction == DIR_HORIZ) // horizontal
        {
    		return (y1 + y2) / 2;
        }
        else if (direction == DIR_VERT) // vertical
        {
        	return (x1 + x2) / 2;
        }
        else // angled
        {
            // TODO: throw exception here, if an attempt is made to
            // access the co-ordinates?
        	return -1;
        }
    }
    

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public boolean isCurve() {
		return isCurve;
	}

	public void setCurve(boolean isCurve) {
		this.isCurve = isCurve;
	}
}
