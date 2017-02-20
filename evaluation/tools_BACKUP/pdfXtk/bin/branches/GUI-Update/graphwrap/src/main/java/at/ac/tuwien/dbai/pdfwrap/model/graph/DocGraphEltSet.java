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

import com.touchgraph.graphlayout.TGException;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class extends the TouchGraph GraphEltSet class
 * to display a document graph
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class DocGraphEltSet extends com.touchgraph.graphlayout.graphelements.GraphEltSet 
//	implements Cloneable 
{
	public static Color NODE_COLOR = Color.red.darker();
	public List<DocNode> lastEnabledNodes;
	
	protected DocNode startNode;
	protected DocumentGraph dg;
	
	/**
	 * Constructor.
	 * 
	 * initializes a blank document graph
	 * 
	 */
	public DocGraphEltSet()
	{
		super();
	}

	public DocGraphEltSet(DocumentGraph dg)
	{
		super();
		
		this.dg = dg;
		
		for (DocNode n : dg.getNodes())
			try {
				addNode(n);
			} catch (TGException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		for (DocEdge e : dg.getEdges())
			addEdge(e);
	}
	
	/*
	public Object clone () 
    //throws CloneNotSupportedException
	{
		try
		{
			return super.clone();
		}
		catch(CloneNotSupportedException cnse)
		{
			cnse.printStackTrace();
		}
		return null;
	}
	*/
	
	// following methods unnecessary, as no nodes
	// are ADDED to the displayed graph...
	// ... concurrent mod. exception ...
	/*
	public void addNode(DocNode n)
	{
		try 
		{
			super.addNode(n);
		} 
		catch (TGException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dg.getNodes().add(n);
	}
	
	public void addEdge(DocEdge e)
	{
		super.addEdge(e);
		dg.getEdges().add(e);
	}
	*/
	
	public DocNode getStartNode()
	{
		return startNode;
	}

	public void setStartNode(DocNode n)
	{
		// TODO: hash map or findNode method better?
		this.startNode = n;
	}
	
	public void clearWrapperEdits()
	{
		// clear all nodes & edges
		for(Object o : nodes)
		{
			DocNode n = (DocNode)o;
			n.clearWrapperEdits();
		}
		for(Object o : edges)
		{
			DocEdge e = (DocEdge)o;
			e.clearWrapperEdits();
		}
		// sets the enabled/disabled nodes to the last enabled set
		enableDisableNodes();
		enableDisableEdges();
	}
	
	// ??? TODO: not sure what the use for this is now...
	public void clearNodeHighlights()
	{
		for(Object o : nodes)
		{
			DocNode n = (DocNode)o;
			n.setExampleInstance(false);
			n.setFoundInstance(false);
		}
	}
	
	public void highlightExampleInstance(List<DocNode> l)
	{
		for(DocNode n : l)
		{
			n.setExampleInstance(true);
		}
	}
	
	public void highlightFoundInstance(List<DocNode> l)
	{
		for(DocNode n : l)
		{
			n.setFoundInstance(true);
		}
	}
	
	public DocNode getFirstEnabledNode()
	{
		for(Object o : nodes)
		{
			DocNode n = (DocNode)o;
			if (!n.isRemoveFromInstance())
				return n;
		}
		// if no enabled node found, return first disabled node
		return (DocNode)nodes.get(0);
	}
	
	public void enableDisableNodes(List<DocNode> instanceNodes)
	{
		for(Object o : nodes)
		{
			DocNode n = (DocNode)o;
			Iterator<DocNode> iter = instanceNodes.iterator();
			boolean found = false;
			while(iter.hasNext() && !found)
			{
				DocNode gs = iter.next();
				if (n == gs)//Integer.toString(gs.hashCode()).equals(n.getID()))
					found = true;
			}
			if (found)
				n.setRemoveFromInstance(false);
			else
				n.setRemoveFromInstance(true);
		}
		lastEnabledNodes = instanceNodes;
	}
	
	public void enableDisableNodes()
	{
		enableDisableNodes(lastEnabledNodes);
	}
	
	public void enableDisableEdges()
	{
		for(Object o : edges)
		{
			DocEdge e = (DocEdge)o;
			// if getFrom and getTo are both enabled, 
			// enable this edge; else disable
			
			// TODO: still possible to create a
			// non-totally-connected graph :(
			
			if (!e.getFrom().isRemoveFromInstance() && 
				!e.getTo().isRemoveFromInstance())
				e.setRemoveFromInstance(false);
			else
				e.setRemoveFromInstance(true);
		}
	}
	/*
	public void setStartNode(DocNode startNode)
	{
		this.startNode = startNode;
		//System.out.println("starting node set to: " + startNode);
		//System.out.println("hash code of document graph: " + hashCode());
	}
	*/
}
