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


import at.ac.tuwien.dbai.pdfwrap.comparators.EdgeAttributeComparator;
import at.ac.tuwien.dbai.pdfwrap.model.document.CompositeSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

import java.util.Comparator;
import java.util.List;

/**
 * Text block segmentation rules
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class TextBlockPageSegmenter extends AbstractPageSegmenter
{
    public static float MAX_CLUST_LINE_SPACING = 1.75f; // 5524.pdf i-cite
    public static float MIN_CLUST_LINE_SPACING = 0.25f; // Baghdad problem! 30.07.08
    public static float MAX_COL_LINE_THRESHOLD = 3.5f;
//    final static float LINE_SPACING_TOLERANCE = 0.25f;
    public static float LINE_SPACING_TOLERANCE = 0.05f; // changed 30.10.10
    // NOTE! This linespacing tolerance does not apply to OCR; 
    // 9.01.11 also does not apply to str conversions;
    // PageProcessor changes this value if a page image is used
	/*
	    protected static boolean clusterTogether(AdjacencyEdge<GenericSegment> ae, 
	    		CandidateCluster clustFrom, CandidateCluster clustTo, 
	    		List<AdjacencyEdge<GenericSegment>> allEdges, HashMap vertNeighbourMap, 
	    		List<? extends GenericSegment> items, int processPhase)
	    {
	    	if (processPhase == 2)
	    		return clusterTogether2(ae, clustFrom, clustTo, allEdges, items);
	    	else
	    		return clusterTogether1(ae, clustFrom, clustTo, allEdges, vertNeighbourMap);
	    }
	    */
	    
	// TODO: only vertical edges actually need to be passed here...?
	public int clusterTogether(AdjacencyEdge<GenericSegment> ae,
		CandidateCluster clustFrom, CandidateCluster clustTo) 
	{
		TextSegment segFrom = (TextSegment)ae.getNodeFrom();
		TextSegment segTo = (TextSegment)ae.getNodeTo();
		
//		30.10.10 -- Cluster.lineSpacing is not a multiple, but rather the absolute linespacing
//		float lineSpacing = ae.getEdgeLength();
		
		boolean boolRetVal;
		// caution: do not confuse segFrom with clustFrom :)
		
		// don't cluster the same segment together(!)
		// (should not happen anyway...)
		if (segFrom == segTo) boolRetVal = false;
		else
		{
			if (ae.isHorizontal())
				boolRetVal = clusterTogetherHoriz(ae, clustFrom, clustTo);
			else
				boolRetVal = clusterTogetherVert(ae, clustFrom, clustTo);
		}
		
		if (boolRetVal == true) return 1; //true
			else return -1; //false
		// 0 -- third state
	}
	
	protected boolean clusterTogetherHoriz(AdjacencyEdge<GenericSegment> ae, 
		CandidateCluster clustFrom, CandidateCluster clustTo)
	{
		TextSegment segFrom = (TextSegment)ae.getNodeFrom();
		TextSegment segTo = (TextSegment)ae.getNodeTo();
		
		if (clustFrom == null)
		{
			clustFrom = new CandidateCluster();
			clustFrom.getItems().add(segFrom);
			clustFrom.findLinesWidth();
			clustFrom.findBoundingBox(); // precondition for findNVN
		}
		
		if (clustTo == null)
		{
			clustTo = new CandidateCluster();
			clustTo.getItems().add(segFrom);
			clustTo.findLinesWidth();
			clustFrom.findBoundingBox(); // precondition for findNVN
		}
		
		// don't cluster the same cluster together(!)
		if (clustFrom == clustTo) return false;
		
		long t = System.currentTimeMillis();
		
		// changed on 30.04.09 to use segments rather than clusters
		List<GenericSegment> neighboursFrom = 
			AbstractPageSegmenter.findNearestVerticalNeighbours(segFrom, allEdges, vertNeighbourMap);
		List<GenericSegment> neighboursTo = 
			AbstractPageSegmenter.findNearestVerticalNeighbours(segTo, allEdges, vertNeighbourMap);
		
		TextSegment closestNeighbourFrom = null;
		if (neighboursFrom.get(0) != null && neighboursFrom.get(1) != null)
		{
			float distanceAbove = 
				((TextSegment)neighboursFrom.get(0)).getY1() - segFrom.getY2();
			float distanceBelow = 
				segFrom.getY1() - ((TextSegment)neighboursFrom.get(1)).getY2();
			
			if (distanceAbove < distanceBelow)
				closestNeighbourFrom = (TextSegment)neighboursFrom.get(0);
			else
				closestNeighbourFrom = (TextSegment)neighboursFrom.get(1);
		}
		else if (neighboursFrom.get(0) != null)
		{
			closestNeighbourFrom = (TextSegment)neighboursFrom.get(0);
		}
		else if (neighboursFrom.get(1) != null)
		{
			closestNeighbourFrom = (TextSegment)neighboursFrom.get(1);
		}
		
		TextSegment closestNeighbourTo = null;
		if (neighboursTo.get(0) != null && neighboursTo.get(1) != null)
		{
			float distanceAbove = 
				((TextSegment)neighboursTo.get(0)).getY1() - segTo.getY2();
			float distanceBelow = 
				segTo.getY1() - ((TextSegment)neighboursTo.get(1)).getY2();
			
			if (distanceAbove < distanceBelow)
				closestNeighbourTo = (TextSegment)neighboursTo.get(0);
			else
				closestNeighbourTo = (TextSegment)neighboursTo.get(1);
		}
		else if (neighboursTo.get(0) != null)
		{
			closestNeighbourTo = (TextSegment)neighboursTo.get(0);
		}
		else if (neighboursTo.get(1) != null)
		{
			closestNeighbourTo = (TextSegment)neighboursTo.get(1);
		}
		
		TextSegment closestNeighbour = null;
		float neighbourDistance = -1;
		if (closestNeighbourFrom != null && closestNeighbourTo != null)
		{
			float distanceFrom;
			if (closestNeighbourFrom.getYmid() < segFrom.getYmid())
				distanceFrom = segFrom.getY1() - closestNeighbourFrom.getY2();
			else
				distanceFrom = closestNeighbourFrom.getY1() - segFrom.getY2();
			
			float distanceTo;
			if (closestNeighbourTo.getYmid() < segTo.getYmid())
				distanceTo = segTo.getY1() - closestNeighbourTo.getY2();
			else
				distanceTo = closestNeighbourTo.getY1() - segTo.getY2();
			
			if (distanceFrom < distanceTo)
			{
				closestNeighbour = closestNeighbourFrom;
				neighbourDistance = distanceFrom;
			}
			else
			{
				closestNeighbour = closestNeighbourTo;
				neighbourDistance = distanceTo;
			}
		}
		else if (closestNeighbourFrom != null)
		{
			closestNeighbour = closestNeighbourFrom;
			float distanceFrom;
			if (closestNeighbourFrom.getYmid() < segFrom.getYmid())
				distanceFrom = segFrom.getY1() - closestNeighbourFrom.getY2();
			else
				distanceFrom = closestNeighbourFrom.getY1() - segFrom.getY2();
			neighbourDistance = distanceFrom;
		}
		else if (closestNeighbourTo != null)
		{
			closestNeighbour = closestNeighbourTo;
			float distanceTo;
			if (closestNeighbourTo.getYmid() < segTo.getYmid())
				distanceTo = segTo.getY1() - closestNeighbourTo.getY2();
			else
				distanceTo = closestNeighbourTo.getY1() - segTo.getY2();
			neighbourDistance = distanceTo;
		}

		// TODO: neighbourDistance is not used at all!
		
		float max_horiz_edge_width = 0.75f;
		
		if (!(clustFrom.getFoundLines().size() <= 2 
			|| clustTo.getFoundLines().size() <= 2))
			max_horiz_edge_width = 0.85f;
		
		if (!(clustFrom.getFoundLines().size() <= 1 
			|| clustTo.getFoundLines().size() <= 1))
			max_horiz_edge_width = 1.0f;
		
		// if baseline of both segs doesn't match, reduce to 0.3
		// addition of 30.04.09
		boolean sameBaseline = 
			Utils.within(segFrom.getY1(), segTo.getY1(), 
			Utils.calculateThreshold(segFrom, segTo, 0.20f));
		
		if (!sameBaseline)
			max_horiz_edge_width = 0.3f;
		
		//float d = neighbourDistance / ae.getFontSize();
		
		// 29.04.09: we recalculate (at least for horiz. edges)
		// the lineSpacing (i.e. relative edge length)
		// using the smallest of both fontsize values...
		
		float smallestFontSize = 
			((TextSegment)ae.getNodeFrom()).getFontSize();
		if (((TextSegment)ae.getNodeFrom()).getFontSize() >
			((TextSegment)ae.getNodeTo()).getFontSize())
			smallestFontSize = ((TextSegment)ae.getNodeTo()).getFontSize();

		float horizGap = ae.physicalLength() / smallestFontSize;
		
		if (horizGap > max_horiz_edge_width) return false;
		
		return true;
	}
	
	protected boolean clusterTogetherVert(AdjacencyEdge<GenericSegment> ae, 
		CandidateCluster clustFrom, CandidateCluster clustTo)
	{
		TextSegment segFrom = (TextSegment)ae.getNodeFrom();
		TextSegment segTo = (TextSegment)ae.getNodeTo();
		
		float lineSpacing;
		
		if (ae.getDirection() == AdjacencyEdge.REL_ABOVE)
			lineSpacing = ae.getNodeTo().getY1() - ae.getNodeFrom().getY1();
		else // REL_BELOW
			lineSpacing = ae.getNodeFrom().getY1() - ae.getNodeTo().getY1();
		
		lineSpacing = lineSpacing/ae.avgFontSize();
		
//		System.out.println("eins");
		if (!(Utils.sameFontSize(segFrom, segTo)))
			return false;
//		System.out.println("lineSpacing: " + lineSpacing);
//		System.out.println("zwei");
		if (!(lineSpacing <= MAX_CLUST_LINE_SPACING && lineSpacing >= MIN_CLUST_LINE_SPACING))
			return false;
//		System.out.println("drei");
//		2011-10-28: the first three if clauses are not executed any more,
//		as the algorithm now takes singleton clusters as input
		if (clustFrom == null && clustTo == null)
		{
//			System.out.println("drei punkt eins");
			return true;
		}
		else if (clustFrom == null)
		{
//			System.out.println("drei punkt zwei");
			// check if line spacing matches that of cluster, or has not yet been
			// assigned
			if (clustTo.getRelLineSpacing() == 0.0f || 
				Utils.within(lineSpacing, clustTo.getRelLineSpacing(), LINE_SPACING_TOLERANCE))
				return true;
		}
		else if (clustTo == null)
		{
//			System.out.println("drei punkt drei");
			// check if line spacing matches that of cluster, or has not yet been
			// assigned
			if (clustFrom.getRelLineSpacing() == 0.0f || 
				Utils.within(lineSpacing, clustFrom.getRelLineSpacing(), LINE_SPACING_TOLERANCE))
				return true;
		}
		else
		{
//			System.out.println("drei punkt vier");
			// don't cluster the same segments together!
			if (clustFrom == clustTo) return false;
			// check that the line spacings are the same and ?within the threshold?
			boolean sameLineSpacing = 
				(Utils.within(clustFrom.getRelLineSpacing(), clustTo.getRelLineSpacing(), 
				LINE_SPACING_TOLERANCE));
			if (clustFrom.getRelLineSpacing() == 0.0f || clustTo.getRelLineSpacing() == 0.0f)
				sameLineSpacing = true;
			
//			System.out.println("drei punkt fünf");
//			System.out.println("ls: " + clustFrom.getLineSpacing() + " clustFrom: " + clustFrom);
//			System.out.println("ls: " + clustTo.getLineSpacing() + " clustTo: " + clustFrom);
//			System.out.println("LINE_SPACING_TOLERANCE = " + LINE_SPACING_TOLERANCE);
			// highly unlikely that it will succeed with sameLineSpacing but 
			// fail here but just in case...
			// also return true if either linespacing unassigned
			
			boolean clustFromValidLineSpacing =
				clustFrom.getRelLineSpacing() == 0.0f || 
				Utils.within(lineSpacing, clustFrom.getRelLineSpacing(), LINE_SPACING_TOLERANCE);
			boolean clustToValidLineSpacing =
				clustTo.getRelLineSpacing() == 0.0f ||
				Utils.within(lineSpacing, clustTo.getRelLineSpacing(), LINE_SPACING_TOLERANCE);
			return
				(sameLineSpacing && clustFromValidLineSpacing && clustToValidLineSpacing);
		}
//		System.out.println("vier");
		return false;
	}
	
	public boolean isValidCluster(CandidateCluster c)
	{
		// prerequisite for calling this method is that the lines have been found ...
		// and that the average linespacing has been found
		//c.findLinesWidth();
		c.setCalculatedFields();
		// now, we check that the linespacing is constant by comparing the
		// spacing of each consecutive line with the average linespacing
		boolean clashingLines = false;
		CompositeSegment<? extends GenericSegment> prevLine = null;
		
		for (CompositeSegment<? extends GenericSegment> l : c.getFoundLines())
    	{
    		if (prevLine != null)
    		{
    			float lineSpacing = (prevLine.getY1() - l.getY1()) / c.getFontSize();
    			if (SegmentUtils.vertIntersect(prevLine, l.getYmid())) clashingLines = true;
//    			System.out.println("lineSpacing: " + lineSpacing);
    			if (!Utils.within(lineSpacing, c.getRelLineSpacing(), LINE_SPACING_TOLERANCE))
    				return false;
    			// fontsize check too
    		}
    		prevLine = l;
    	}

//		System.out.println("returning: " + !checkForChasms(c));
		return !AbstractPageSegmenter.checkForChasms(c);
	}

	public Comparator<AdjacencyEdge<? extends GenericSegment>> edgeComparator() {
		return new EdgeAttributeComparator();
	}
	
	public boolean horizSkip()
	{
		return true;
	}
	
	public boolean doSwallow()
	{
		return true;
	}
	
	// no effect if doSwallow is true
	public boolean doOverlap()
	{
		return true;
	}
	
	public boolean neighbourMap()
	{
		return true;
	}

}
