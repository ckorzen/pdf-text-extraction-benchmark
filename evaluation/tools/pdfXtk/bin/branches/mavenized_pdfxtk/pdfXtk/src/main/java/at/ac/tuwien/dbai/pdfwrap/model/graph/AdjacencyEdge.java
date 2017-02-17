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

import at.ac.tuwien.dbai.pdfwrap.gui.EdgeSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

/*
import edu.uci.ics.jung.graph.impl.SimpleUndirectedSparseVertex;
*/

/**
 * This represents an edge in the neighbourhood graph
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class AdjacencyEdge<T extends GenericSegment> //extends Edge
{
	protected T nodeFrom, nodeTo;
	
    public static final int REL_LEFT = 0;
    public static final int REL_RIGHT = 1;
    public static final int REL_ABOVE = 2;
    public static final int REL_BELOW = 3;
    
    protected float weight;
    
//    public static final int REL_SUPERIOR = 10;
//    public static final int REL_INFERIOR = 11;

    // 20.11.06
    // added the new fields:
    // hasAlignment(int, specified by GenericSegment.H/V_ALIGN...)
    // hasSameFontSize(bool)
    // hasSameFont(bool) -- TBA, once we can read it from PDFBox!
    // isSeparatedByRulingLine(bool)
    // hasConstantLeading -- this one needs to be algorithmically deduced ??
    //   or better to just leave this for an on-the-fly calculation?
    
    protected boolean ruled;
    
    protected int direction;

    /**
     * Constructor.
     *
     * @param todo: add parameters :)
     */
    
    public AdjacencyEdge(T nodeFrom, T nodeTo, int direction)
    {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.direction = direction;
        ruled = false;
    }
    
    public AdjacencyEdge(T nodeFrom, T nodeTo, int direction, float weight)
    {
    	this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.direction = direction;
        this.weight = weight;
        ruled = false;
    }
    
    public AdjacencyEdge<T> duplicate()
    {
    	AdjacencyEdge<T> retVal = new AdjacencyEdge<T>(nodeFrom, nodeTo, direction, weight);
    	retVal.ruled = this.ruled;
    	return retVal;
    }

    /*
    public AttributedEdge toAttributedEdge(OntModel model)
    {
        return new AttributedEdge(this, model);
    }
    */
    
    // not in use yet?
    
    // NOTE: there is currently a dependency between the
    // positions of both nodes and the direction -- a
    // nonsensical combination can be chosen
    
    
    public T getNodeFrom() {
		return nodeFrom;
	}

	public void setNodeFrom(T nodeFrom) {
		this.nodeFrom = nodeFrom;
	}

	public T getNodeTo() {
		return nodeTo;
	}

	public void setNodeTo(T nodeTo) {
		this.nodeTo = nodeTo;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public float lowCoord()
    {
        switch(direction)
        {
            case REL_LEFT:
                return nodeTo.getX2();
            case REL_RIGHT:
                return nodeFrom.getX2();
            case REL_ABOVE:
                return nodeFrom.getY2();
            case REL_BELOW:
                return nodeTo.getY2();
            default:
                // yet another nonsensical comparison!
                return -1.0f;
        }   
    }
    
    public float hiCoord()
    {
        switch(direction)
        {
            case REL_LEFT:
                return nodeFrom.getX1();
            case REL_RIGHT:
                return nodeTo.getX1();
            case REL_ABOVE:
                return nodeTo.getY1();
            case REL_BELOW:
                return nodeFrom.getY1();
            default:
                // yet another nonsensical comparison!
                return -1.0f;
        }
    }
    
    public float midCoord()
    {
    	return (lowCoord() + hiCoord()) / 2.0f;
    }

    public boolean isHorizontal()
    {
        if (direction == REL_LEFT ||
            direction == REL_RIGHT)
            return true;
        else
            return false;
    }
    
    public boolean isVertical()
    {
        if (direction == REL_ABOVE ||
            direction == REL_BELOW)
            return true;
        else
            return false;
    }
    
    // 2011-01-26: TODO: CLEAN UP ALL THIS!
    
    public float physicalLength()
    {
//    	2011-01-26 -- now always uses bounding box measurements
//    	baseline comparisons to be moved elsewhere
//    	float topLineBaseline, bottomLineBaseline;
    	
        switch(direction)
        {
            case REL_LEFT:
                return nodeFrom.getX1() - nodeTo.getX2();
            case REL_RIGHT:
                return nodeTo.getX1() - nodeFrom.getX2();
            case REL_ABOVE:
            	return nodeTo.getY1() - nodeFrom.getY2();
            case REL_BELOW:
                return nodeFrom.getY1() - nodeTo.getY2();
            default:
                return -1.0f;
        }
    }
    
    public float baselineDistance()
    {
//    	2011-01-26 -- now always uses bounding box measurements
//    	baseline comparisons to be moved elsewhere
//    	float topLineBaseline, bottomLineBaseline;
    	
        switch(direction)
        {
            case REL_ABOVE:
            	return nodeTo.getY1() - nodeFrom.getY1();
            case REL_BELOW:
                return nodeFrom.getY1() - nodeTo.getY1();
            default:
                return -1.0f;
        }
    }

    // in case ruling line intersects the block of text itself
    // (asiafrontpage/ihtfrontpage examples)
    public GenericSegment toEnlargedBoundingSegment()
    {
    	float tolerance = 0.0f;
    	if (nodeFrom instanceof TextSegment && nodeTo instanceof TextSegment)
    	{
    		float afs = Utils.avg(((TextSegment)nodeFrom).getFontSize(),
    				((TextSegment)nodeTo).getFontSize());
    		tolerance = afs * 0.25f;
    	}
    	// note: in comparison to the next method, max and min
    	// are swapped for yo and xo calculations.
    	
    	float newX1, newX2, newY1, newY2; //, xo1, xo2, yo1, yo2;
    	//System.out.println("direction: " + direction);
    	switch(direction)
        {
            case REL_LEFT:
                newX1 = nodeTo.getX2(); newX2 = nodeFrom.getX1();

            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                newY1 = Utils.minimum(nodeFrom.getY1(), nodeTo.getY1());
                newY2 = Utils.maximum(nodeFrom.getY2(), nodeTo.getY2());
                //newY1 = (yo1 + yo2) / 2;
            	//newY2 = newY1;
                break;
            case REL_RIGHT:
            	newX2 = nodeTo.getX1(); newX1 = nodeFrom.getX2();
                
            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                newY1 = Utils.minimum(nodeFrom.getY1(), nodeTo.getY1());
                newY2 = Utils.maximum(nodeFrom.getY2(), nodeTo.getY2());
                //newY1 = (yo1 + yo2) / 2;
            	//newY2 = newY1;
                break;
            case REL_ABOVE:
                newY1 = nodeFrom.getY2() - tolerance; 
                newY2 = nodeTo.getY1() + tolerance;

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                newX1 = Utils.minimum(nodeFrom.getX1(), nodeTo.getX1());
                newX2 = Utils.maximum(nodeFrom.getX2(), nodeTo.getX2());
                //newX1 = (xo1 + xo2) / 2;
                //newX2 = newX1;
                break;
            case REL_BELOW:
            	newY2 = nodeFrom.getY1() + tolerance; 
            	newY1 = nodeTo.getY2() - tolerance;

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                newX1 = Utils.minimum(nodeFrom.getX1(), nodeTo.getX1());
                newX2 = Utils.maximum(nodeFrom.getX2(), nodeTo.getX2());
                //newX1 = (xo1 + xo2) / 2;
                //newX2 = newX1;
                break;
            default:
            	//System.out.println("whoops!");
                newX1 = -1; newX2 = -1; newY1 = -1; newY2 = -1;
        }
    	return new GenericSegment(newX1, newX2, newY1, newY2);
    }
    
    public GenericSegment toBoundingSegment()
    {
    	// note: in comparison to the next method, max and min
    	// are swapped for yo and xo calculations.
    	
    	float newX1, newX2, newY1, newY2; //, xo1, xo2, yo1, yo2;
    	//System.out.println("direction: " + direction);
    	switch(direction)
        {
            case REL_LEFT:
                newX1 = nodeTo.getX2(); newX2 = nodeFrom.getX1();

            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                newY1 = Utils.minimum(nodeFrom.getY1(), nodeTo.getY1());
                newY2 = Utils.maximum(nodeFrom.getY2(), nodeTo.getY2());
                //newY1 = (yo1 + yo2) / 2;
            	//newY2 = newY1;
                break;
            case REL_RIGHT:
            	newX2 = nodeTo.getX1(); newX1 = nodeFrom.getX2();
                
            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                newY1 = Utils.minimum(nodeFrom.getY1(), nodeTo.getY1());
                newY2 = Utils.maximum(nodeFrom.getY2(), nodeTo.getY2());
                //newY1 = (yo1 + yo2) / 2;
            	//newY2 = newY1;
                break;
            case REL_ABOVE:
                newY1 = nodeFrom.getY2(); newY2 = nodeTo.getY1();

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                newX1 = Utils.minimum(nodeFrom.getX1(), nodeTo.getX1());
                newX2 = Utils.maximum(nodeFrom.getX2(), nodeTo.getX2());
                //newX1 = (xo1 + xo2) / 2;
                //newX2 = newX1;
                break;
            case REL_BELOW:
            	newY2 = nodeFrom.getY1(); newY1 = nodeTo.getY2();

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                newX1 = Utils.minimum(nodeFrom.getX1(), nodeTo.getX1());
                newX2 = Utils.maximum(nodeFrom.getX2(), nodeTo.getX2());
                //newX1 = (xo1 + xo2) / 2;
                //newX2 = newX1;
                break;
            default:
            	//System.out.println("whoops!");
                newX1 = -1; newX2 = -1; newY1 = -1; newY2 = -1;
        }
    	return new GenericSegment(newX1, newX2, newY1, newY2);
    }
    
    public EdgeSegment toDisplayableSegment()
    {
    	float newX1, newX2, newY1, newY2, xo1, xo2, yo1, yo2;
    	//System.out.println("direction: " + direction);
    	switch(direction)
        {
            case REL_LEFT:
                newX1 = nodeTo.getX2(); newX2 = nodeFrom.getX1();

            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                yo1 = Utils.maximum(nodeFrom.getY1(), nodeTo.getY1());
                yo2 = Utils.minimum(nodeFrom.getY2(), nodeTo.getY2());
                newY1 = (yo1 + yo2) / 2;
            	newY2 = newY1;
                break;
            case REL_RIGHT:
            	newX2 = nodeTo.getX1(); newX1 = nodeFrom.getX2();
                
            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                yo1 = Utils.maximum(nodeFrom.getY1(), nodeTo.getY1());
                yo2 = Utils.minimum(nodeFrom.getY2(), nodeTo.getY2());
                newY1 = (yo1 + yo2) / 2;
            	newY2 = newY1;
                break;
            case REL_ABOVE:
                newY1 = nodeFrom.getY2(); newY2 = nodeTo.getY1();

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                xo1 = Utils.maximum(nodeFrom.getX1(), nodeTo.getX1());
                xo2 = Utils.minimum(nodeFrom.getX2(), nodeTo.getX2());
                newX1 = (xo1 + xo2) / 2;
                newX2 = newX1;
                break;
            case REL_BELOW:
            	newY2 = nodeFrom.getY1(); newY1 = nodeTo.getY2();

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                xo1 = Utils.maximum(nodeFrom.getX1(), nodeTo.getX1());
                xo2 = Utils.minimum(nodeFrom.getX2(), nodeTo.getX2());
                newX1 = (xo1 + xo2) / 2;
                newX2 = newX1;
                break;
            default:
            	//System.out.println("whoops!");
                newX1 = -1; newX2 = -1; newY1 = -1; newY2 = -1;
        }
    	return new EdgeSegment(newX1, newX2, newY1, newY2);
    }
    
    public float avgFontSize()
    {
        // TODO: exception or -1 returned if a horizontal edge?
        if (!(nodeFrom instanceof TextSegment) || 
            !(nodeTo instanceof TextSegment)) return -1.0f;
        
        TextSegment nFrom = (TextSegment)nodeFrom;
        TextSegment nTo = (TextSegment)nodeTo;
        
        return (nFrom.getFontSize() + nTo.getFontSize()) / 2;
    }
    
    public float highFontSize()
    {
        // TODO: exception or -1 returned if a horizontal edge?
        if (!(nodeFrom instanceof TextSegment) || 
            !(nodeTo instanceof TextSegment)) return -1.0f;
        
        TextSegment nFrom = (TextSegment)nodeFrom;
        TextSegment nTo = (TextSegment)nodeTo;
        
        if (nFrom.getFontSize() > nTo.getFontSize())
        	return nFrom.getFontSize();
        else return nTo.getFontSize();	
    }
    
    public float lowFontSize()
    {
        // TODO: exception or -1 returned if a horizontal edge?
        if (!(nodeFrom instanceof TextSegment) || 
            !(nodeTo instanceof TextSegment)) return -1.0f;
        
        TextSegment nFrom = (TextSegment)nodeFrom;
        TextSegment nTo = (TextSegment)nodeTo;
        
        if (nFrom.getFontSize() < nTo.getFontSize())
        	return nFrom.getFontSize();
        else return nTo.getFontSize();	
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

    
    public String toString()
    {
    	String direction_text = "?";
    	if (direction == REL_ABOVE) direction_text = "above";
    	if (direction == REL_BELOW) direction_text = "below";
    	if (direction == REL_LEFT) direction_text = "left";
    	if (direction == REL_RIGHT) direction_text = "right";
        return ("AdjacencyEdge:  Direction: " + direction_text + "\n NodeFrom: " + nodeFrom + "\nNodeTo: " + nodeTo + "direction: " + direction + "\n");
    }
    
	public boolean isRuled()
	{
		return ruled;
	}

	public void setRuled(boolean ruled)
	{
		this.ruled = ruled;
	}

}
