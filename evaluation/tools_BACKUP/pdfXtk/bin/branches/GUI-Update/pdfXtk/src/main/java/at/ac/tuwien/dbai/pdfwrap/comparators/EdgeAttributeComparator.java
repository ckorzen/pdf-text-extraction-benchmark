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

import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Comparator;

/**
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class EdgeAttributeComparator implements Comparator<AdjacencyEdge<? extends GenericSegment>>
{


    /**
     * TODO: check correctness of:
     * > means 'before'
     * shorter edge (line spacing) > longer edge
     * smaller width difference > larger width difference (forget about alignment for now)
     * smaller font > larger font
     * same font size > differing font sizes
     * edges that contain non-text segments
     *
     * @param ae1
     * @param ae2
     *
     * @return
     */
	public int compare(AdjacencyEdge<? extends GenericSegment> ae1, AdjacencyEdge<? extends GenericSegment> ae2 ) {

		// both edges horizontal
		if (ae1.isHorizontal() && ae2.isHorizontal())
		{
//			System.out.println("A");
			float ae1RelLength = ae1.physicalLength() / ae1.avgFontSize();
			float ae2RelLength = ae2.physicalLength() / ae2.avgFontSize();
			
//			if (Utils.within(ae1RelLength, ae2RelLength, 0.01f)) {
			if (ae1RelLength == ae2RelLength) {
				return 0;
			}
			else {
				// longer edges come after shorter edges
				if (ae1RelLength > ae2RelLength) return 1;
				else if (ae1RelLength == ae2RelLength) return 0;
				else return -1;
			}
		}
		else if (ae1.isVertical() && ae2.isHorizontal())
		{
//			System.out.println("B");
			// first element comes before second
			return -1;
		}
		else if (ae1.isHorizontal() && ae2.isVertical())
		{
//			System.out.println("C");
			// second element comes before first
			return 1;
		}
		     // both edges vertical
		else // this is where it gets exciting :)
		{
//			System.out.println("D");
			GenericSegment f1 = ae1.getNodeFrom();
			GenericSegment t1 = ae1.getNodeTo();
			GenericSegment f2 = ae2.getNodeFrom();
			GenericSegment t2 = ae2.getNodeTo();
			
			boolean text1 = false, text2 = false;
			if (f1 instanceof TextSegment && t1 instanceof TextSegment)
				text1 = true;
			if (f2 instanceof TextSegment && t2 instanceof TextSegment)
				text2 = true;
			
			if (text1 && text2) // both text segments
			{	
//				System.out.println("E");
				TextSegment ft1 = (TextSegment)f1;
				TextSegment tt1 = (TextSegment)t1;
				TextSegment ft2 = (TextSegment)f2;
				TextSegment tt2 = (TextSegment)t2;
				
				boolean sfs1 = false, sfs2 = false, sfs3 = false, sfs4 = false;
				if (Utils.sameFontSize(ft1, tt1)) sfs1 = true;
				if (Utils.sameFontSize(ft2, tt2)) sfs2 = true;
//				if (Utils.sameFontSize(ft1, ft2)) sfs3 = true;
//				if (Utils.sameFontSize(tt1, tt2)) sfs4 = true;
				
				if (sfs1 && sfs2) // && sfs3 && sfs4) // both have same font size
				{
//					System.out.println("G");
					
					// compare widths if linespacing is (almost) equal
					boolean compareWidths = false;
					float ae1RelLength = ae1.physicalLength() / ae1.avgFontSize();
					float ae2RelLength = ae2.physicalLength() / ae2.avgFontSize();
					
//					if (Utils.within(ae1RelLength, ae2RelLength, 0.01f)) 
					if (ae1RelLength == ae2RelLength)
						compareWidths = true;
					
					if (compareWidths) // compares the width _ratios_
					{
//						System.out.println("I");
						float widthRatio1, widthRatio2;
						
						// avoid potential division by zero error
						if (ft1.getWidth() == 0 || tt1.getWidth() == 0 ||
							ft2.getWidth() == 0 || tt2.getWidth() == 0) return 0;
						
						if (ft1.getWidth() > tt1.getWidth())
							widthRatio1 = ft1.getWidth() / tt1.getWidth();
						else
							widthRatio1 = tt1.getWidth() / ft1.getWidth();
						
						
						if (ft2.getWidth() > tt2.getWidth())
							widthRatio2 = ft2.getWidth() / tt2.getWidth();
						else
							widthRatio2 = tt2.getWidth() / ft2.getWidth();
						
//						if (Utils.within(widthRatio1, widthRatio2, 0.1f))
						if (widthRatio1 == widthRatio2)
							return 0;
						else if (widthRatio1 < widthRatio2) return -1;
						else return 1;
					}
					else
					{
						// compare heights
//						if (Utils.within(ae1RelLength, ae2RelLength, 0.01f)) {
						if (ae1RelLength == ae2RelLength) {
							return 0;
						}
						else {
							if (ae1RelLength > ae2RelLength) return 1; // smaller line spacing first
							else if (ae1RelLength == ae2RelLength) return 0;
							else return -1;
						}
					}
					
				}

				else if (sfs1 && !sfs2) return -1; // return edge with sfs before
				else if (sfs2 && !sfs1) return 1;  // edge without sfs
				else return 0; // if sfs is equal
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

	public boolean equals( Object obj ) {
        return EqualsBuilder.reflectionEquals( this, obj );
	}
}