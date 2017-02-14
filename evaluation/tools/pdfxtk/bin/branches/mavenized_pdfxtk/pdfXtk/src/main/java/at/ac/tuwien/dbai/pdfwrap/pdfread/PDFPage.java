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
package at.ac.tuwien.dbai.pdfwrap.pdfread;

import java.util.Iterator;
import java.util.List;

import at.ac.tuwien.dbai.pdfwrap.model.document.CompositeSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;


/**
 * Element to represent the intermediary page, after
 * the objects have been extracted via PDFObjectExtractor
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class PDFPage extends CompositeSegment<GenericSegment>
{
	int rotation = 0;
	int lastOpIndex = -1;
	
    public PDFPage()
    {
        super();
    }
    
    public PDFPage(List<GenericSegment> items)
    {
        super(items);
    }
    
    public PDFPage(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }
    
    public PDFPage(
        float x1,
        float x2,
        float y1,
        float y2,
		List<GenericSegment> items
        )
    {
		super(x1, x2, y1, y2, items);
    }
    
    public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getLastOpIndex() {
		return lastOpIndex;
	}

	public void setLastOpIndex(int lastOpIndex) {
		this.lastOpIndex = lastOpIndex;
	}

	public void reverseYCoordinatesPDF()
	// pre: Y co-ordinates represent those of page bounding box
	{
/////		System.out.println("page is now: " + this);
		Iterator nIter = items.iterator();
		while (nIter.hasNext())
		{
			GenericSegment thisSegment = (GenericSegment) nIter.next();
			//System.out.println("this segment: " + thisSegment);
			
			if (thisSegment instanceof TextSegment)
			{
				float currentY1 = thisSegment.getY1();
				float height = thisSegment.getHeight();
				float reversedY1 = super.getHeight() - currentY1;
				//float reversedY1 = super.getSegHeight() - currentY1 - super.getY1();
				thisSegment.setY1(reversedY1);
				thisSegment.setY2(reversedY1 + height);
			}
			else
			{
				float currentY1 = thisSegment.getY1();
				float currentY2 = thisSegment.getY2();
				thisSegment.setY1(super.getHeight() - currentY2);
				//thisSegment.setY1(super.getSegHeight() - currentY2 - super.getY1());
				thisSegment.setY2(super.getHeight() - currentY1);
				//thisSegment.setY2(super.getSegHeight() - currentY1 - super.getY1());
			}
		}
	}
	
	public void reverseYCoordinatesPNG()
	// pre: Y co-ordinates represent those of page bounding box
	{
/////		System.out.println("page is now: " + this);
		Iterator nIter = items.iterator();
		while (nIter.hasNext())
		{
			GenericSegment thisSegment = (GenericSegment) nIter.next();
			//System.out.println("this segment: " + thisSegment);
			
			float currentY1 = thisSegment.getY1();
			float currentY2 = thisSegment.getY2();
			thisSegment.setY1(super.getHeight() - currentY2);
			//thisSegment.setY1(super.getSegHeight() - currentY2 - super.getY1());
			thisSegment.setY2(super.getHeight() - currentY1);
			//thisSegment.setY2(super.getSegHeight() - currentY1 - super.getY1());
		}
	}
	
	/**
	 * for use when pageRotation == 270 || pageRotation == -90
	 */
	public void reverseXCoordinates()
	// pre: X co-ordinates represent those of page bounding box
	{
		Iterator nIter = items.iterator();
		while (nIter.hasNext())
		{
			// System.out.println("y: " + currentY1 + "total height: " + height
			// + "new y: " + )
			GenericSegment thisSegment = (GenericSegment) nIter.next();
			//System.out.println("this segment: " + thisSegment);
			float currentX1 = thisSegment.getX1();
			float width = thisSegment.getWidth();
			float reversedX1 = super.getWidth() - currentX1;
			thisSegment.setX1(reversedX1);
			thisSegment.setX2(reversedX1 + width);
		}
	}
	
	/**
	 * translates all co-ordinates so that the page's (x1, y1) is at (0, 0)
	 */
	public void normalizeCoordinates()
	{
		Iterator nIter = items.iterator();
		while (nIter.hasNext())
		{
			GenericSegment thisSegment = (GenericSegment) nIter.next();
			thisSegment.setX1(thisSegment.getX1() - super.getX1());
			thisSegment.setX2(thisSegment.getX2() - super.getX1());
			thisSegment.setY1(thisSegment.getY1() - super.getY1());
			thisSegment.setY2(thisSegment.getY2() - super.getY1());
		}
		// and the page itself...
		super.setX2(super.getX2() - super.getX1());
		super.setY2(super.getY2() - super.getY1());
		super.setX1(0); super.setY1(0);
	}
}
