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
package at.ac.tuwien.dbai.pdfwrap.utils;

//import CandidateTable;
//import TableColumn;
//import TableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.dbai.pdfwrap.comparators.XComparator;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;

/**
 * Static utility methods for lists
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class ListUtils
{
	// adpated from http://briankuhn.com/?p=47
	public static void removeDuplicates(List<?> l) {
	    Set set = new LinkedHashSet();
	    set.addAll(l);
	    l.clear();
	    l.addAll(set);
	}
	
	/*
	public static void test()
	{
		CompositeSegment ts = new CompositeSegment();
		ts.getItems().add(new GenericSegment());
		
		List<GenericSegment> genericSegments = new ArrayList<GenericSegment>();
		List<TextSegment> textSegments = new ArrayList<TextSegment>();
		
		getItemsByClass(genericSegments, TextSegment.class, textSegments);
	}
	*/
	
	public static void selectItemsByClass(List<? extends Object> inputList,
		Class c, List resultList)
	{
		for(Object gs : inputList)
		{
			if (gs.getClass() == c)
			{
				resultList.add(gs);
			}
		}
	}
	
	public static List<CharSegment> selectCharacters(List<GenericSegment> l)
	{
		List<CharSegment> retVal = new ArrayList<CharSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == CharSegment.class)
				retVal.add((CharSegment)thisSegment);
		}
		return retVal;
	}
	
	public static List<TextFragment> selectTextFragments(List<GenericSegment> l)
	{
		List<TextFragment> retVal = new ArrayList<TextFragment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == TextFragment.class)
				retVal.add((TextFragment)thisSegment);
		}
		return retVal;
	}
	
	public static List<TextSegment> selectTextSegments(List<GenericSegment> l)
	{
		List<TextSegment> retVal = new ArrayList<TextSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment instanceof TextSegment)
				retVal.add((TextSegment)thisSegment);
		}
		return retVal;
	}
	
	public static List<TextBlock> selectTextBlocks(List<GenericSegment> l)
	{
		List<TextBlock> retVal = new ArrayList<TextBlock>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == TextBlock.class)
				retVal.add((TextBlock)thisSegment);
		}
		return retVal;
	}
	
	public static List<TextLine> selectTextLines(List<GenericSegment> l)
	{
		List<TextLine> retVal = new ArrayList<TextLine>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == TextLine.class)
				retVal.add((TextLine)thisSegment);
		}
		return retVal;
	}

	public static List<LineSegment> selectLineSegments(List<GenericSegment> l)
	{
		List<LineSegment> retVal = new ArrayList<LineSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == LineSegment.class)
				retVal.add((LineSegment)thisSegment);
		}
		return retVal;
	}
	
	public static List<LineSegment> selectHorizLineSegments(List<GenericSegment> l)
	{
		List<LineSegment> retVal = new ArrayList<LineSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == LineSegment.class &&
				((LineSegment)thisSegment).getDirection() == LineSegment.DIR_HORIZ)
				retVal.add((LineSegment)thisSegment);
		}
		return retVal;
	}
	
	public static List<LineSegment> selectVertLineSegments(List<GenericSegment> l)
	{
		List<LineSegment> retVal = new ArrayList<LineSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == LineSegment.class &&
				((LineSegment)thisSegment).getDirection() == LineSegment.DIR_VERT)
				retVal.add((LineSegment)thisSegment);
		}
		return retVal;
	}
	
	public static List<RectSegment> selectRectSegments(List<GenericSegment> l)
	{
		List<RectSegment> retVal = new ArrayList<RectSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == RectSegment.class)
				retVal.add((RectSegment)thisSegment);
		}
		return retVal;
	}

	public static List<ImageSegment> selectImageSegments(List<GenericSegment> l)
	{
		List<ImageSegment> retVal = new ArrayList<ImageSegment>();
		Iterator<GenericSegment> iter = l.iterator();
		while (iter.hasNext())
		{
			GenericSegment thisSegment = iter.next();
			if (thisSegment.getClass() == ImageSegment.class)
				retVal.add((ImageSegment)thisSegment);
		}
		return retVal;
	}

	public static GenericSegment findLeftMostSegment(List<? extends GenericSegment> l)
	{
		List<GenericSegment> sortedList = new ArrayList<GenericSegment>();
		for (GenericSegment gs : l)
			sortedList.add(gs);
		Collections.sort(l, new XComparator());
		return l.get(0);
	}
	
	public static GenericSegment findRightMostSegment(List<? extends GenericSegment> l)
	{
		List<GenericSegment> sortedList = new ArrayList<GenericSegment>();
		for (GenericSegment gs : l)
			sortedList.add(gs);
		Collections.sort(l, new XComparator());
		return l.get(l.size() - 1);
	}
	
	public static boolean containsItems(List smallerList, List largerList)
	{
		List tempList = new ArrayList();
		for (Object o : largerList)
			tempList.add(o);
		
		for (Object check : smallerList)
		{
			if (tempList.contains(check))
				tempList.remove(check); // necessary to cope with duplicate objects
			else
				return false;
		}
		return true;
	}
	
	public static boolean sameItems(List<?> list1, List<?> list2)
	{
		return (list1.size() == list2.size() && 
			containsItems(list1, list2));
	}
    
    /*
     * note: returns elements that are _fully covered_ by the
     * bBox; partial intersections are NOT returned.
     * Caution: the method within GenericSegment (which is
     * build upon horizIntersect/vertIntersect) returns
     * all intersections, whether full or partial.
     */
    public static List<GenericSegment> findElementsFullyWithinBBox
    	(List<? extends GenericSegment> l, GenericSegment bBox)
    {
    	List<GenericSegment> retVal = new ArrayList<GenericSegment>();
    	for(GenericSegment s : l)
    	{
    		if (s.getX1() >= bBox.getX1() &&
    			s.getX2() <= bBox.getX2() &&
    			s.getY1() >= bBox.getY1() &&
    			s.getY2() <= bBox.getY2())
    			retVal.add(s);
    	}
    	return retVal;
    }
    
    
    
    public static List<GenericSegment> findElementsIntersectingBBox
    	(List<? extends GenericSegment> l, GenericSegment bBox)
    {
    	List<GenericSegment> retVal = new ArrayList<GenericSegment>();
    	for(GenericSegment s : l)
    	{
    		if (SegmentUtils.intersects(bBox, s))
    			retVal.add(s);
    	}
    	return retVal;
    }
    
    public static List<GenericSegment> findElementsWithCentresWithinBBox
    	(List<? extends GenericSegment> l, GenericSegment bBox)
    {
    	List<GenericSegment> retVal = new ArrayList<GenericSegment>();
    	for(GenericSegment s : l)
    	{
    		if (s.getXmid() >= bBox.getX1() &&
    			s.getXmid() <= bBox.getX2() &&
    			s.getYmid() >= bBox.getY1() &&
    			s.getYmid() <= bBox.getY2())
    			retVal.add(s);
    	}
    	return retVal;
    }
    
    public static List<GenericSegment> findElementsWithCentresWithinBBoxOrViceVersa
    	(List<? extends GenericSegment> l, GenericSegment bBox)
    {
    	List<GenericSegment> retVal = new ArrayList<GenericSegment>();
    	for(GenericSegment s : l)
    	{
    		if ((s.getXmid() >= bBox.getX1() &&
    			s.getXmid() <= bBox.getX2() &&
    			s.getYmid() >= bBox.getY1() &&
    			s.getYmid() <= bBox.getY2()) ||
    			(bBox.getXmid() >= s.getX1() &&
    			bBox.getXmid() <= s.getX2() &&
    			bBox.getYmid() >= s.getY1() &&
    			bBox.getYmid() <= s.getY2()))
    			retVal.add(s);
    	}
    	return retVal;
    }
    
    public static List<GenericSegment> findElementsFullyWithinBBox
    	(List<? extends GenericSegment> l, float[] bBox)
    {
    	return findElementsFullyWithinBBox(l, new GenericSegment(bBox));
    }
    
    /* GENERIC PROBLEMS -- METHOD MOVED TO ADJACENCYGRAPH
    public static List<AdjacencyEdge<GenericSegment>> 
    	getVertEdges(List<AdjacencyEdge<? extends GenericSegment>> l)
    {
    	List<AdjacencyEdge<GenericSegment>> retVal = 
    		new ArrayList<AdjacencyEdge<GenericSegment>>();
    		
    	for (AdjacencyEdge<? extends GenericSegment> ae : l)
    		if (ae.isVertical())
    			retVal.add((AdjacencyEdge<GenericSegment>) l);
    	
    	return retVal;
    }
    
    public static List<AdjacencyEdge<GenericSegment>> 
	getHorizEdges(List<AdjacencyEdge<? extends GenericSegment>> l)
	{
		List<AdjacencyEdge<GenericSegment>> retVal = 
			new ArrayList<AdjacencyEdge<GenericSegment>>();
			
		for (AdjacencyEdge<? extends GenericSegment> ae : l)
			if (ae.isHorizontal())
				retVal.add((AdjacencyEdge<GenericSegment>) l);
		
		return retVal;
	}
    */
    
    public static void printList(List<?> l)
    {
//    	System.out.println("List: " + l + " items: " + l.size());
//    	System.out.println("List with items: " + l.size());
    	for (Object o : l)
    		System.out.println(o);
    }
    
    /*
    public static void printEdgeList(List<? extends AdjacencyEdge> l)
    {
    	System.out.println("Edge list: " + l + " items: " + l.size());
    	for (AdjacencyEdge<? extends GenericSegment> e : l)
    		System.out.println(e);
    }
    */
    
    // TODO: move printItems to CS.extendedString
    public static void printListWithSubItems(List<? extends GenericSegment> l)
    {
    	System.out.println("Segment list: " + l + " items: " + l.size());
    	for (GenericSegment gs : l)
    	{
    		System.out.println(gs);
    		if (gs instanceof CompositeSegment<?>)
    		{
    			CompositeSegment<?> cs = (CompositeSegment<?>)gs;
//    			for (GenericSegment item : cs.getItems())
//    				System.out.println("    " + item);
    			cs.printSubItems(1);
    		}
    	}
    }
    
    public static void printListWithOneLevelSubItems(List<? extends GenericSegment> l)
    {
    	System.out.println("Segment list: " + l + " items: " + l.size());
    	for (GenericSegment gs : l)
    	{
    		System.out.println(gs);
    		if (gs instanceof CompositeSegment<?>)
    		{
    			CompositeSegment<?> cs = (CompositeSegment<?>)gs;
    			for (GenericSegment item : cs.getItems())
    				System.out.println("    " + item);
//    			cs.printSubItems(0);
    		}
    	}
    }
    
    public static List<GenericSegment> shallowCopy(List<? extends GenericSegment> l)
    {
    	ArrayList<GenericSegment> retVal = new ArrayList<GenericSegment>();
    	for (GenericSegment gs : l)
    		retVal.add(gs);
    	return retVal;
    }
    
    public static List<GenericSegment> deepCopy(List<? extends GenericSegment> l)
    {
    	ArrayList<GenericSegment> retVal = new ArrayList<GenericSegment>();
    	for (GenericSegment gs : l)
    		retVal.add((GenericSegment)(gs.clone()));
    	return retVal;
    }
    
    // method here to enable apache.ListUtils to be called in an elegant way!
	public static List<GenericSegment> intersection
		(List<? extends GenericSegment> l1, List<? extends GenericSegment> l2)
	{
		List<GenericSegment> retVal = new ArrayList<GenericSegment>();
		List intersect = 
			org.apache.commons.collections.ListUtils.intersection(l1, l2);
		for (Object o : intersect)
		{
			GenericSegment gs = (GenericSegment)o;
			retVal.add(gs);
		}
		return retVal;
	}
}
