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
package at.ac.tuwien.dbai.pdfwrap.comparators;

import java.util.Comparator;

import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

/**
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class EdgeAttributeComparator implements Comparator<AdjacencyEdge<? extends GenericSegment>>
{
	public int compare(AdjacencyEdge<? extends GenericSegment> ae1,
		AdjacencyEdge<? extends GenericSegment> ae2)
	{
		// > means 'before'
		
		// shorter edge (line spacing) > longer edge
		// smaller width difference > larger width difference (forget about alignment for now)
		// smaller font > larger font
		// same font size > differing font sizes
		// edges that contain non-text segments
		
		if (ae1.isHorizontal() && ae2.isHorizontal())
		{
			int lengthRetVal = (int)((ae1.physicalLength() / ae1.avgFontSize()
					- (ae2.physicalLength() / ae2.avgFontSize())) * 10f);
				// if e.g. 0.9 and 1.1 get rounded, still within 10%...
				if (Utils.within(ae1.physicalLength() / ae1.avgFontSize(),
					ae2.physicalLength() / ae2.avgFontSize(), 0.1f))
					lengthRetVal = 0;
				// i.e. error tolerance of 0.1 due to int rounding
				
			//return 0;
			return lengthRetVal;
		}
		else if (ae1.isVertical() && ae2.isHorizontal())
		{
			// first element comes before second
			return -1;
		}
		else if (ae1.isHorizontal() && ae2.isVertical())
		{
			// second element comes before first
			return 1;
		}
		else // this is where it gets exciting :)
		{
			GenericSegment f1 = ae1.getNodeFrom();
			GenericSegment t1 = ae1.getNodeTo();
			GenericSegment f2 = ae2.getNodeFrom();
			GenericSegment t2 = ae2.getNodeTo();
			
			boolean text1 = false, text2 = false;
			if (f1 instanceof TextSegment && t1 instanceof TextSegment)
				text1 = true;
			if (f2 instanceof TextSegment && t2 instanceof TextSegment)
				text2 = true;
			
			if (text1 && text2)
			{	
				TextSegment ft1 = (TextSegment)f1;
				TextSegment tt1 = (TextSegment)t1;
				TextSegment ft2 = (TextSegment)f2;
				TextSegment tt2 = (TextSegment)t2;
				
				boolean sfs1 = false, sfs2 = false;
				if (Utils.sameFontSize(ft1, tt1)) sfs1 = true;
				if (Utils.sameFontSize(ft2, tt2)) sfs2 = true;
				
				if (sfs1 && sfs2)
				{
					// smaller font/larger font
					if (Utils.sameFontSize(ft1, ft2))
					{
						// line spacing
						if (ae1.isVertical() && ae2.isVertical())
						{
							int lengthRetVal = (int)((ae1.physicalLength() / ae1.avgFontSize()
								- (ae2.physicalLength() / ae2.avgFontSize())) * 10f);
							// if e.g. 0.9 and 1.1 get rounded, still within 10%...
							if (Utils.within(ae1.physicalLength() / ae1.avgFontSize(),
								ae2.physicalLength() / ae2.avgFontSize(), 0.1f))
								lengthRetVal = 0;
							// i.e. error tolerance of 0.1 due to int rounding
							//if (retVal == 0)
							//{
							int widthRetVal;
	//							 width difference
							float width1, width2;
							if (ft1.getWidth() > tt1.getWidth())
								width1 = ft1.getWidth() / tt1.getWidth();
							else
								width1 = tt1.getWidth() / ft1.getWidth();
							if (ft2.getWidth() > tt2.getWidth())
								width2 = ft2.getWidth() / tt2.getWidth();
							else
								width2 = tt2.getWidth() / ft2.getWidth();
							
							if (Utils.within(width1, width2, 0.1f))
							{
								//return 0; // no further tasks...
								widthRetVal = 0;
							}
							else if (width1 < width2) widthRetVal = -1;
							else widthRetVal = 1;
							
							// changed 6.06.07
							// was the other way round; i.e. length had priority over width
							// 7.07.07: changed back
							// 29.07.08: changed again :)
							if (lengthRetVal == 0)
								return widthRetVal;
							else return lengthRetVal;
							
							//}
							//else return retVal;
						}
						else return 0; // edges not vertical; academic
					}
					else if (ft1.getFontSize() < ft2.getFontSize()) return -1;
					else return 1;
				}
				else if (sfs1) return -1;
				else if (sfs2) return 1;
				else return 0;
			}
			else if (text1)
			{
				// first element comes before second
				return -1;
			}
			else if (text2)
			{
				// second element comes before first
				return 1;
			}
			else
			{
				// neither is a text segment; equal
				return 0;
			}
		}
	}

	public boolean equals(Object obj)
	{
		return obj.equals(this);
	}
}