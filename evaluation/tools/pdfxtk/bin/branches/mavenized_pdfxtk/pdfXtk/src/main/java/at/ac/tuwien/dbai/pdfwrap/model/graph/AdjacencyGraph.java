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
package at.ac.tuwien.dbai.pdfwrap.model.graph;

// todo: linear segments method only for first build on one level
// (i.e. with getElementsAbove, etc)

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.dbai.pdfwrap.comparators.EdgeLengthComparator;
import at.ac.tuwien.dbai.pdfwrap.comparators.XComparator;
import at.ac.tuwien.dbai.pdfwrap.comparators.YComparator;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

/**
 * AdjacencyGraph -- the neighbourhood graph
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class AdjacencyGraph<T extends GenericSegment>// extends DocumentGraph 
{   
//	protected List<T> nodes;
	protected List<AdjacencyEdge<T>> edges;
	
	/*
	protected List<T> neighboursLeft;
	protected List<T> neighboursRight;
	protected List<T> neighboursAbove;
	protected List<T> neighboursBelow;
	*/
	
	protected List<T> horiz;
	protected List<T> vert;

	// 2011-11-17 does not appear to be in use!
//	protected HashMap<T, List<AdjacencyEdge<T>>> edgesFrom;
//	protected HashMap<T, List<AdjacencyEdge<T>>> edgesTo;
	
	public final static int NEIGHBOUR_INTERSECTING_X_Y_MID = 0;
	public final static int NEIGHBOUR_X_Y_INTERSECT = 1;
	
	protected int neighbourRules = 0;
	
	/**
     * Constructor.
     *
     * @param to fill in! todo!
     */
    public AdjacencyGraph()
    // initialize a blank neighbourhood graph
    {
//    	nodes = new ArrayList<T>();
    	edges = new ArrayList<AdjacencyEdge<T>>();
    	
    	horiz = new ArrayList<T>();
    	vert = new ArrayList<T>();
    	
//    	2011-01-27 TODO: implement HashMap
//    	edgesFrom = new HashMap<T, List<AdjacencyEdge<T>>>();
//    	edgesTo = new HashMap<T, List<AdjacencyEdge<T>>>();
    }
    
    /*
    public List<T> getNodes() {
		return nodes;
	}

	public void setNodes(List<T> nodes) {
		this.nodes = nodes;
	}
	*/
    
	public void addList(List<? extends T> nodes)
	{
		horiz.addAll(nodes);
		vert.addAll(nodes);
	}
	
	public List<AdjacencyEdge<T>> getEdges() {
		return edges;
	}

	public void setEdges(List<AdjacencyEdge<T>> edges) {
		this.edges = edges;
	}
	
	public List<AdjacencyEdge<T>> horizEdges() {
		List<AdjacencyEdge<T>> retVal = new ArrayList<AdjacencyEdge<T>>();
		for (AdjacencyEdge<T> ae : edges)
			if (ae.isHorizontal()) retVal.add(ae);
		return retVal;
	}
	
	public List<AdjacencyEdge<T>> vertEdges() {
		List<AdjacencyEdge<T>> retVal = new ArrayList<AdjacencyEdge<T>>();
		for (AdjacencyEdge<T> ae : edges)
			if (ae.isVertical()) retVal.add(ae);
		return retVal;
	}

	public List<T> getHorizSegmentList() {
		return horiz;
	}

	public List<T> getVertSegmentList() {
		return vert;
	}

	public int getNeighbourRules() {
		return neighbourRules;
	}

	public void setNeighbourRules(int neighbourRules) {
		this.neighbourRules = neighbourRules;
	}

	protected List<T> findNeighboursBelow(T thisSegment, boolean findFirst)
    {
        List<T> retVal = new ArrayList<T>();
        float threshold = 2.0f; // 2pt threshold
        
        // the next line returns -1 if the given node is not in the list
        // perhaps throw an exception in this case?
        // TODO: a quicker (binary-chop) searching method instead of using indexOf?
        // or a hash map?
        int index = vert.indexOf(thisSegment);
        
        // granularity stuff... we don't need it
        // extent stuff... we don't need it
        
        // look for neighbours above, return the first or null!
        for (int n = index + 1; n < vert.size(); n ++)
        {
            T o = vert.get(n);
            // 
            boolean isNeighbour = SegmentUtils.horizIntersect(o, thisSegment) 
        		&& (SegmentUtils.horizIntersect(thisSegment, o.getXmid()) 
            	|| SegmentUtils.horizIntersect(o, thisSegment.getXmid()));
            
            if (neighbourRules == NEIGHBOUR_X_Y_INTERSECT)
            	isNeighbour = SegmentUtils.horizIntersect(o, thisSegment);
            	
            if (isNeighbour)
            {
                // if there's already a neighbour...
            	if (retVal.size() > 0)
            	{
            		if (o.getY1() <= (retVal.get(0).getY1() + threshold))
            			retVal.add(o);
            		else
            			return retVal;
            	}
            	else
            	{
            		retVal.add(o);
            		if (findFirst) return retVal;
            	}
            }
        }
        return retVal;
    }
    
    protected List<T> findNeighboursAbove(T thisSegment, boolean findFirst)
    {
        List<T> retVal = new ArrayList<T>();
        float threshold = 2.0f; // 2pt threshold
        
        // the next line returns -1 if the given node is not in the list
        // perhaps throw an exception in this case?
        // TODO: a quicker (binary-chop) searching method instead of using indexOf?
        // or a hash map?
        int index = vert.indexOf(thisSegment);
        
        // granularity stuff... we don't need it
        // extent stuff... we don't need it
        
        // look for neighbours below, return the first or null!
        for (int n = index - 1; n >= 0; n --)
        {
            T o = vert.get(n);
            // 
            boolean isNeighbour = SegmentUtils.horizIntersect(o, thisSegment) 
	    		&& (SegmentUtils.horizIntersect(thisSegment, o.getXmid()) 
	        	|| SegmentUtils.horizIntersect(o, thisSegment.getXmid()));
	        
	        if (neighbourRules == NEIGHBOUR_X_Y_INTERSECT)
	        	isNeighbour = SegmentUtils.horizIntersect(o, thisSegment);
	        	
	        if (isNeighbour)
	        {
                // if there's already a neighbour...
            	if (retVal.size() > 0)
            	{
            		if (o.getY2() >=
            			(retVal.get(0).getY2() - threshold))
            			retVal.add(o);
            		else
            			return retVal;
            	}
            	else
                {
            		retVal.add(o);
            		if (findFirst) return retVal;
                }
            }
        }
        return retVal;
    }
    
    protected List<T> findNeighboursLeft(T thisSegment, boolean findFirst)
    {
        List<T> retVal = new ArrayList<T>();
        float threshold = 1.0f; // 1pt threshold
        
        // the next line returns -1 if the given node is not in the list
        // perhaps throw an exception in this case?
        // TODO: a quicker (binary-chop) searching method instead of using indexOf?
        // or a hash map?
        int index = horiz.indexOf(thisSegment);
        
        // granularity stuff... we don't need it
        // extent stuff... we don't need it
        
        // look for neighbours below, return the first or null!
        for (int n = index - 1; n >= 0; n --)
        {
            T o = horiz.get(n);
            // 
//          if (SegmentUtils.vertIntersect(o, thisSegment))
            boolean isNeighbour =
            	SegmentUtils.vertMinIntersect(o, thisSegment, Utils.neighbourLOSMin) &&
                !SegmentUtils.horizMinIntersect(o, thisSegment, Utils.neighbourOverlapTolerance);
            	
            if (neighbourRules == NEIGHBOUR_X_Y_INTERSECT)
	        	isNeighbour = SegmentUtils.vertIntersect(o, thisSegment);
            
            if (isNeighbour)
            {
                // if there's already a neighbour...
            	if (retVal.size() > 0)
            	{
            		if (o.getX2() >=
            			(retVal.get(0).getX2() - threshold))
            			retVal.add(o);
        			else
        				return retVal;
            	}
            	else
            	{
            		retVal.add(o);
            		if (findFirst) return retVal;
            	}
            }
        }
        return retVal;
    }
    
    protected List<T> findNeighboursRight(T thisSegment, boolean findFirst)
    {
        List<T> retVal = new ArrayList<T>();
        float threshold = 1.0f; // 1pt threshold
        
        // the next line returns -1 if the given node is not in the list
        // perhaps throw an exception in this case?
        // TODO: a quicker (binary-chop) searching method instead of using indexOf?
        // or a hash map?
        int index = horiz.indexOf(thisSegment);
        
        // granularity stuff... we don't need it
        // extent stuff... we don't need it
        
        // look for neighbours below, return the first or null!
        for (int n = index + 1; n < horiz.size(); n ++)
        {
            T o = horiz.get(n);
            // 
//          if (SegmentUtils.vertIntersect(o, thisSegment))
            boolean isNeighbour =
            	SegmentUtils.vertMinIntersect(o, thisSegment, Utils.neighbourLOSMin) &&
                !SegmentUtils.horizMinIntersect(o, thisSegment, Utils.neighbourOverlapTolerance);
            	
            if (neighbourRules == NEIGHBOUR_X_Y_INTERSECT)
	        	isNeighbour = SegmentUtils.vertIntersect(o, thisSegment);
            
            if (isNeighbour)
            {
                if (retVal.size() > 0)
                {
                	if (o.getX1() <=
                		(retVal.get(0).getX1() + threshold))
                		retVal.add(o);
                	else
                		return retVal;
                }
                else
                {
                	retVal.add(o);
                	if (findFirst) return retVal;
                }
            }
        }
        return retVal;
    }
    
    private List<T> findNeighboursAbove(T thisSegment)
    {
    	return findNeighboursAbove(thisSegment, false);
    }
    
    private List<T> findNeighboursBelow(T thisSegment)
    {
    	return findNeighboursBelow(thisSegment, false);
    }
    private List<T> findNeighboursLeft(T thisSegment)
    {
    	return findNeighboursLeft(thisSegment, false);
    }
    
    private List<T> findNeighboursRight(T thisSegment)
    {
    	return findNeighboursRight(thisSegment, false);
    }
    
    /*
     * just returns the first neighbour
     * probably all that we need!
     */
    
    private T findNeighbourAbove(T thisSegment)
    {
    	List<T> l = findNeighboursAbove(thisSegment, true);
    	if (l.size() > 0) return l.get(0);
    	else return null;
    }
    
    private T findNeighbourBelow(T thisSegment)
    {
    	List<T> l = findNeighboursBelow(thisSegment, true);
    	if (l.size() > 0) return l.get(0);
    	else return null;
    }
    private T findNeighbourLeft(T thisSegment)
    {
    	List<T> l = findNeighboursLeft(thisSegment, true);
    	if (l.size() > 0) return l.get(0);
    	else return null;
    }
    
    private T findNeighbourRight(T thisSegment)
    {
    	List<T> l = findNeighboursRight(thisSegment, true);
    	if (l.size() > 0) return l.get(0);
    	else return null;
    }
    
    
    /*
     * commented out 11.07.06 for our "one neighbour only" approach
     * ... we need to see if any other code is dependent on these methods... 
     *
     * put back 13.07.06 as xxxOld, as Giannicola's table-growing
     * algorithm needs them...
     * 
     * TODO: when refactoring, sort this mess out!
     * 
     */
    
    
    // TODO: replace these crazy methods with methods that simply
    // add edges?
    
    public void generateEdgesSingle(T thisBlock)
    {
    	List<T> neighboursLeft = new ArrayList<T>();
    	T neighbourLeft = findNeighbourLeft(thisBlock);
    	if (neighbourLeft != null)
    		neighboursLeft.add(neighbourLeft);
    	List<T> neighboursRight = new ArrayList<T>();
    	T neighbourRight = findNeighbourRight(thisBlock);
    	if (neighbourRight != null)
    		neighboursRight.add(neighbourRight);
    	List<T> neighboursAbove = new ArrayList<T>();
    	T neighbourAbove = findNeighbourAbove(thisBlock);
    	if (neighbourAbove != null)
    		neighboursAbove.add(neighbourAbove);
    	List<T> neighboursBelow = new ArrayList<T>();
    	T neighbourBelow = findNeighbourBelow(thisBlock);
    	if (neighbourBelow != null)
    		neighboursBelow.add(neighbourBelow);
    
        // and create edges for each neighbouring direction
        for (T theNode : neighboursLeft)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_LEFT));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_RIGHT));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_RIGHT, level));   
        }
        for (T theNode : neighboursRight)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_RIGHT));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_LEFT));
            //edges.add(new Edge(lookupNode, (Node)nIter.next(), Edge.DIR_RIGHT, level));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_LEFT, level));
        }
        for (T theNode : neighboursAbove)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_ABOVE));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_BELOW));
            //edges.add(new Edge(lookupNode, (Node)nIter.next(), Edge.DIR_ABOVE, level));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_BELOW, level));
        }
        for (T theNode : neighboursBelow)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_BELOW));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_ABOVE));
            //edges.add(new Edge(lookupNode, (Node)nIter.next(), Edge.DIR_BELOW, level));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_ABOVE, level));
        }
    }
    
    public void generateEdgesMultiple(T thisBlock)
    {
    	List<T> neighboursLeft = findNeighboursLeft(thisBlock);
    	List<T> neighboursRight = findNeighboursRight(thisBlock);
    	List<T> neighboursAbove = findNeighboursAbove(thisBlock);
    	List<T> neighboursBelow = findNeighboursBelow(thisBlock);
    
        // and create edges for each neighbouring direction
        for (T theNode : neighboursLeft)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_LEFT));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_RIGHT));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_RIGHT, level));   
        }
        for (T theNode : neighboursRight)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_RIGHT));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_LEFT));
            //edges.add(new Edge(lookupNode, (Node)nIter.next(), Edge.DIR_RIGHT, level));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_LEFT, level));
        }
        for (T theNode : neighboursAbove)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_ABOVE));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_BELOW));
            //edges.add(new Edge(lookupNode, (Node)nIter.next(), Edge.DIR_ABOVE, level));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_BELOW, level));
        }
        for (T theNode : neighboursBelow)
        {
            edges.add(new AdjacencyEdge<T>(thisBlock, theNode, AdjacencyEdge.REL_BELOW));
            edges.add(new AdjacencyEdge<T>(theNode, thisBlock, AdjacencyEdge.REL_ABOVE));
            //edges.add(new Edge(lookupNode, (Node)nIter.next(), Edge.DIR_BELOW, level));
            //theNode.getEdges().add(new Edge(theNode, lookupNode, Edge.DIR_ABOVE, level));
        }
    }
    
    public void generateEdgesSingle()
    {
    	edges.clear();
    	
    	Collections.sort(horiz, new XComparator());
    	Collections.sort(vert, new YComparator());
    	
    	// TODO: clear hash maps too
    	
        for (T thisBlock: vert)
        {
        	generateEdgesSingle(thisBlock);
            	
            // we need a separate iterator for removing duplicate edges...
            // TODO: this method doesn't work yet!  perhaps due to infinite loop?
            // test later over time...
        }
        //System.out.println("*** finished generating edges. ***");
        removeDuplicateEdges();
        //System.out.println("removed duplicate edges");
    }
    
    public void generateEdgesMultiple()
    {
    	edges.clear();
    	
    	Collections.sort(horiz, new XComparator());
    	Collections.sort(vert, new YComparator());
    	
    	// TODO: clear hash maps too
    	
    	for (T thisBlock: vert)
        {
        	generateEdgesMultiple(thisBlock);
        }
        removeDuplicateEdges();
    }
    
    public void removeTransitiveEdges()
    {
    	// TODO: sort edges longest first
    	Collections.sort(edges, Collections.reverseOrder(new EdgeLengthComparator()));
    	
//    	for (AdjacencyEdge<T> e : edges)
    	for (int i = 0; i < edges.size(); i ++)
    	{
    		AdjacencyEdge<T> e = edges.get(i);
    		
    		List<T> fromList = new ArrayList<T>();
    		fromList.add(e.getNodeFrom());
    		if (isTransitivePath(fromList, e.getNodeTo(), e.getDirection()))
    		{
    			// no updating index necessary; i points to next edge
    			// after removal; edges.size() decreases by 1
    			edges.remove(e);
    		}
    	}
    }
    
    protected boolean isTransitivePath(List<T> fromList, T nodeTo, int direction)
    {
    	for (T fromNode : fromList)
    	{
    		if (fromNode == nodeTo) 
    		{	
    			return true;
    		}
    		else
    		{
    			List<T> newFromList = new ArrayList<T>();
    			for (AdjacencyEdge<T> e : edges)
    				if (e.getNodeFrom() == fromNode && e.getDirection() == direction)
    					newFromList.add(e.getNodeTo());
    			if (isTransitivePath(newFromList, nodeTo, direction))
    				return true;
    		}
    	}
    	return false;
    }
    
    public void generateComplementaryEdges()
    {
    	List<AdjacencyEdge<T>> edgesToAdd = 
    		new ArrayList<AdjacencyEdge<T>>();
    	
    	for (AdjacencyEdge<T> e : edges)
    	{
    		int newDirection = -1;
    		if (e.getDirection() == AdjacencyEdge.REL_ABOVE)
    			newDirection = AdjacencyEdge.REL_BELOW;
    		else if (e.getDirection() == AdjacencyEdge.REL_BELOW)
    			newDirection = AdjacencyEdge.REL_ABOVE;
    		else if (e.getDirection() == AdjacencyEdge.REL_LEFT)
    			newDirection = AdjacencyEdge.REL_RIGHT;
    		else if (e.getDirection() == AdjacencyEdge.REL_RIGHT)
    			newDirection = AdjacencyEdge.REL_LEFT;
    		
    		edgesToAdd.add(new AdjacencyEdge<T>
    			(e.nodeTo, e.nodeFrom, newDirection));
    	}
    	edges.addAll(edgesToAdd);
    	
    	removeDuplicateEdges();
    }
    
    // TODO: to speed this up (and other operations...)
    // implement a hash map lookup for edges in the DocumentGraph method
    // or, rather EdgeList?
    /**
     * Removes extra instances of an edge between two
     * given nodes (even if they are distinct objects)
     * 
     * TODO: move to ListUtils?
     */
    protected void removeDuplicateEdges()
    {
    	List<AdjacencyEdge<T>> edgesToRemove = 
    		new ArrayList<AdjacencyEdge<T>>();
    	
    	for (AdjacencyEdge<T> e1 : edges)
    	{
			for (AdjacencyEdge<T> e2 : edges)
			{
				if (e1.getDirection() == e2.getDirection() &&
					e1.getNodeFrom() == e2.getNodeFrom() &&
					e1.getNodeTo() == e2.getNodeTo() &&
					e1 != e2 && !edgesToRemove.contains(e1))
				{
					edgesToRemove.add(e2);
				}
			}
    	}
    	edges.removeAll(edgesToRemove);
    }
    
    public void removeEdgesAboveLeft()
    {
    	List<AdjacencyEdge<? extends GenericSegment>> edgesToRemove =
			new ArrayList<AdjacencyEdge<? extends GenericSegment>>();
		
		for (AdjacencyEdge<? extends GenericSegment> ag : edges)
			if (ag.getDirection() == AdjacencyEdge.REL_LEFT ||
				ag.getDirection() == AdjacencyEdge.REL_ABOVE)
				edgesToRemove.add(ag);
		
		edges.removeAll(edgesToRemove);
    }
    
    /*
     here, we only seem look at nodes pointing FROM
     the given segment in the relevant direction --
     the other edges are duplicated anyway ...
     
     this way, we should end up with each neighbouring
     item once ...
     */
    
    public List<T> returnNeighboursLeft(T thisSeg)
    {
        List<T> retVal = new ArrayList<T>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_LEFT &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e.getNodeTo());
        }
        return retVal;
    }
    
    public List<T> returnNeighboursRight(GenericSegment thisSeg)
    {
    	List<T> retVal = new ArrayList<T>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_RIGHT &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e.getNodeTo());
        }
        return retVal;
    }
    
    public List<T> returnNeighboursAbove(GenericSegment thisSeg)
    {
    	List<T> retVal = new ArrayList<T>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_ABOVE &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e.getNodeTo());
        }
        return retVal;
    }
    
    public List<T> returnNeighboursBelow(GenericSegment thisSeg)
    {
    	List<T> retVal = new ArrayList<T>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_BELOW &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e.getNodeTo());
        }
        return retVal;
    }
    
    public List<AdjacencyEdge<T>> returnEdgesLeft(T thisSeg)
    {
        List<AdjacencyEdge<T>> retVal = new ArrayList<AdjacencyEdge<T>>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_LEFT &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e);
        }
        return retVal;
    }
    
    public List<AdjacencyEdge<T>> returnEdgesRight(T thisSeg)
    {
        List<AdjacencyEdge<T>> retVal = new ArrayList<AdjacencyEdge<T>>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_RIGHT &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e);
        }
        return retVal;
    }
    
    public List<AdjacencyEdge<T>> returnEdgesAbove(T thisSeg)
    {
        List<AdjacencyEdge<T>> retVal = new ArrayList<AdjacencyEdge<T>>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_ABOVE &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e);
        }
        return retVal;
    }
    
    public List<AdjacencyEdge<T>> returnEdgesBelow(T thisSeg)
    {
        List<AdjacencyEdge<T>> retVal = new ArrayList<AdjacencyEdge<T>>();
        for (AdjacencyEdge<T> e : edges)
        {
        	if (e.getDirection() == AdjacencyEdge.REL_BELOW &&
        		e.getNodeFrom() == thisSeg)
        		retVal.add(e);
        }
        return retVal;
    }
    
    public AdjacencyGraph<T> generateSubgraph(List<T> segments)
    {
    	AdjacencyGraph<T> retVal = new AdjacencyGraph<T>();
    	for (AdjacencyEdge<T> ae : edges)
    		if (segments.contains(ae.getNodeFrom()) && 
    			segments.contains(ae.getNodeTo()))
    			retVal.edges.add(ae);
    	for (T seg : horiz)
    		if (segments.contains(seg)) retVal.horiz.add(seg);
    	for (T seg : vert)
    		if (segments.contains(seg)) retVal.vert.add(seg);
    	
    	return retVal;
    }
    
    public String toString()
    {
        StringBuffer retVal = new StringBuffer("");
        for (T seg : vert)
        {
            retVal.append(seg.toString() + "\n");
            List<T> neighboursLeft = returnNeighboursLeft(seg);
            List<T> neighboursRight = returnNeighboursRight(seg);
            List<T> neighboursAbove = returnNeighboursAbove(seg);
            List<T> neighboursBelow = returnNeighboursBelow(seg);
            retVal.append("     Neighbours left: " + neighboursLeft.size() +
                " right: " + neighboursRight.size() +
                " above: " + neighboursAbove.size() +
                " below: " + neighboursBelow.size() + "\n");
        }
        return retVal.toString();
    }
}
