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

import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The document graph
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class DocumentGraph // extends com.touchgraph.graphlayout.graphelements.GraphEltSet 
	implements Cloneable // extends UndirectedSparseGraph
{
	
//	protected ListUtils horiz;
//	protected ListUtils vert;
	
	protected List<DocNode> nodes;
	protected List<DocEdge> edges;

	protected HashMap<GenericSegment, DocNode> segNodeHash;
	protected HashMap<DocNode, GenericSegment> nodeSegHash;
	
	// TODO: generic segment/text segment problem...
	// sloppy code... sort out at some time!
	// potential hole...
	// used for graph matching only at this stage...

	/**
	 * Constructor.
	 * 
	 * initializes a blank document graph
	 * 
	 */
	public DocumentGraph()
	// initialize a blank neighbourhood graph
	{
		nodes = new ArrayList<DocNode>();
		edges = new ArrayList<DocEdge>();
	}

	public DocumentGraph(AdjacencyGraph<?> ag)
	{
		nodes = new ArrayList<DocNode>();
		edges = new ArrayList<DocEdge>();
		
		segNodeHash = new HashMap<GenericSegment, DocNode>();
		nodeSegHash = new HashMap<DocNode, GenericSegment>();
		
//		System.out.println("in AG with nodes: " + ag.getVertList().size() + " and edges: " + ag.getEdges().size());
		
		for (Object o : ag.getVertSegmentList())
		{
			GenericSegment gs = (GenericSegment)o;	// MUST be a GenericSegment
			DocNode n = new DocNode(gs);
			nodes.add(n);
//			addNode(n);
			segNodeHash.put(gs, n);
			nodeSegHash.put(n, gs);
		}
		
		for (AdjacencyEdge<?> ae : ag.getEdges())
		{
			GenericSegment segFrom = ae.getNodeFrom();
			GenericSegment segTo = ae.getNodeTo();
			DocNode nodeFrom = segNodeHash.get(segFrom);
			DocNode nodeTo = segNodeHash.get(segTo);
			
			// add AttributedEdge TODO:...
			if (ae.getDirection() == AdjacencyEdge.REL_RIGHT ||
				ae.getDirection() == AdjacencyEdge.REL_BELOW)
			{
				DocEdge atr  = new DocEdge(ae, nodeFrom, nodeTo); 
				edges.add(atr);
//				addEdge(atr);
			}
		}
		
		System.out.println("creating DG with nodes: " + nodes.size() + " edges: " + edges.size());
	}
	
	public DocumentGraph(NodeList listOfItems)
	{
		this();
		
		for (int s = 0; s < listOfItems.getLength(); s ++)
        {
        	Node itemNode = listOfItems.item(s);
            if(itemNode.getNodeType() == Node.ELEMENT_NODE)
            {
            	if(itemNode.getNodeName().equals("node"))
            	{
            		nodes.add(new DocNode((Element)itemNode));
            	}
            }
        }
		
		// edges must be added after all the nodes have been added
		for (int s = 0; s < listOfItems.getLength(); s ++)
        {
        	Node itemNode = listOfItems.item(s);
            if(itemNode.getNodeType() == Node.ELEMENT_NODE)
            {
            	if(itemNode.getNodeName().equals("edge"))
            	{
            		edges.add(new DocEdge((Element)itemNode, nodes));
            		
            		// hash?
            	}
            }
        }
	}
	
	public List<DocNode> getNodes() {
		return nodes;
	}
	
	/*
	public Iterator<DocNode> getNodes()
	{
        if ( nodes.size() == 0 ) return null;
        return nodes.iterator(); 
    }
 */	
	
	/*
	public void addNode( DocNode node ) 
	{
		try {
			super.addNode(node);
		} catch (TGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	public void setNodes(List<DocNode> nodes) {
		this.nodes = nodes;
	}
	
	public List<DocEdge> getEdges() {
		return edges;
	}
	
	/*
	public Iterator<DocEdge> getEdges() {
        if ( edges.size() == 0 ) return null;
        else return edges.iterator(); 
    }
	*/
	
	public List<DocEdge> edgesFrom(DocNode n)
	{
		List<DocEdge> retVal = new ArrayList<DocEdge>();
		for (Object o : edges)
		{
			DocEdge e = (DocEdge)o;
			if (e.getFrom() == n)
				retVal.add(e);
		}
		return retVal;
	}
	
	public List<DocEdge> edgesTo(DocNode n)
	{
		List<DocEdge> retVal = new ArrayList<DocEdge>();
		for (Object o : edges)
		{
			DocEdge e = (DocEdge)o;
			if (e.getTo() == n)
				retVal.add(e);
		}
		return retVal;
	}
	
	public List<DocEdge> edgesFromTo(DocNode n)
	{
		List<DocEdge> retVal = new ArrayList<DocEdge>();
		for (Object o : edges)
		{
			DocEdge e = (DocEdge)o;
			if (e.getFrom() == n || e.getTo() == n)
				retVal.add(e);
		}
		return retVal;
	}
	
	/*
	public void addEdge( DocEdge e )
	{
		super.addEdge(e);
	}
	*/
	
	public void setEdges(List<DocEdge> edges) {
		this.edges = edges;
	}
	
	public DocNode getNodeFromHash(GenericSegment gs)
	{
		return segNodeHash.get(gs);
	}
	
	public GenericSegment getSegmentFromHash(DocNode n)
	{
		return nodeSegHash.get(n);
	}
	
	public DocumentGraph subGraph(List<DocNode> nodes)
	{
		DocumentGraph retVal = new DocumentGraph();
		retVal.nodes.addAll(nodes);
		for (Object o : edges)
		{
			DocEdge e = (DocEdge)o;
			if (nodes.contains(e.getFrom()) || nodes.contains(e.getTo()))
				retVal.edges.add(e);
		}
		return retVal;
	}
	
	public DocumentGraph deepCopy()
	{
		DocumentGraph retVal = new DocumentGraph();
		
		// HashMap from nodes to newly cloned nodes...
		HashMap <DocNode, DocNode> nhm = new HashMap<DocNode, DocNode>();
		for (Object o : nodes)
		{
			DocNode n = (DocNode)o;
			DocNode cln = (DocNode)n.clone();
			retVal.nodes.add(cln);
			nhm.put(n, cln);
		}
		
		// HashMap from dgEdges to newly cloned instanceEdges
        //HashMap hm = new HashMap();
        
//        EdgeList instanceEdges = new EdgeList();
        for (Object o : edges)
        {
        	DocEdge ae = (DocEdge)o;
			DocEdge cae = (DocEdge)ae.clone();
			cae.setFrom(nhm.get(cae.getFrom()));
			cae.setTo(nhm.get(cae.getTo()));
			//instanceEdges.add(cae);
			retVal.edges.add(cae);
			//hm.put(ae, cae);
        }
        
        return retVal;
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
	
	public void addAsXMLGraph(Document resultDocument, Element parent,
		boolean addDisabledItems)
    {
        for(Object o : nodes)
        {
        	DocNode thisItem = (DocNode)o;
        	if (!thisItem.isRemoveFromInstance() || addDisabledItems)
        		thisItem.addAsXMLNode(resultDocument, parent);
        }
        for (Object o: edges)
        {
        	DocEdge thisItem = (DocEdge)o;
            if (!thisItem.isRemoveFromInstance() || addDisabledItems)
            	thisItem.addAsXMLEdge(resultDocument, parent);
        }
    }

	/*
	public String toString()
	{
		StringBuffer vertices = new StringBuffer("");
		StringBuffer edges = new StringBuffer("");
		// output all vertices (think go through vert)
		for (DocNode n : nodes)
		{
			// vertices.append("\"");
			vertices.append("" + n + " " + "\"text=\'" + n.getSegText()
				+ "\' " + "x1=" + n.getSegX1() + " x2="
				+ n.getSegX2() + " y1=" + n.getSegY1() + " y2="
				+ n.getSegY2() + "\"\n");

			EdgeList neighbours = getEdges(n); // thisNode.getNeighbours();

			Iterator eIter = neighbours.iterator();

			while (eIter.hasNext())
			{
				AttributedEdge e2 = (AttributedEdge) eIter.next();
				GenericSegment node2 = e2.getNodeTo();
				// GenericSegment temp2 = node2.getSegment();

				if (node2 instanceof TextSegment)
				{
					TextSegment thisNeighbour = (TextSegment) node2;
					edges.append("" + n + " " + vert.indexOf(node2) + " "
						+ e2.getWeight() + "\n");
				}
			}
		}

		return "*Vertices\n" + vertices.toString() + "\n*Edges\n"
			+ edges.toString();
	}
	*/

}
