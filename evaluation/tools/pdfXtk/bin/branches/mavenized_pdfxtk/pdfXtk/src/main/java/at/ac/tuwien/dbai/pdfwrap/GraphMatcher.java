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
package at.ac.tuwien.dbai.pdfwrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.ac.tuwien.dbai.pdfwrap.analysis.PageProcessor;
import at.ac.tuwien.dbai.pdfwrap.comparators.XComparator;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.Page;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyGraph;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocEdge;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocNode;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocumentGraph;
import at.ac.tuwien.dbai.pdfwrap.model.graph.WrappingInstance;
import at.ac.tuwien.dbai.pdfwrap.pdfread.PDFObjectExtractor;
import at.ac.tuwien.dbai.pdfwrap.utils.ListUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;

/**
 * performs the graph matching to obtain wrapping instances
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class GraphMatcher
{
	// pre: edgesToReturn is a blank EdgeList
    protected static List<DocNode> getNeighboursFrom(DocNode node, List<DocEdge> docEdges, 
    	List<DocEdge> edgesToReturn)
    {
    	List<DocNode> retVal = new ArrayList<DocNode>();
    	
    	for (DocEdge e : docEdges)
    	{
    		if (e.getFrom() == node)
    		{
    			retVal.add(e.getTo());
    			edgesToReturn.add(e);
    		}
    	}
    	
    	return retVal;
    }
    
    // pre: edgesToReturn is a blank EdgeList
    protected static List<DocNode> getNeighboursTo(DocNode node, List<DocEdge> docEdges, 
    	List<DocEdge> edgesToReturn)
    {
    	List<DocNode> retVal = new ArrayList<DocNode>();
    	
    	for (DocEdge e : docEdges)
    	{
    		if (e.getTo() == node)
    		{
    			retVal.add(e.getFrom());
    			edgesToReturn.add(e);
    		}
    	}
    	
    	return retVal;
    }
    
    protected static boolean corresponds(DocNode insN, DocNode docN,
    	boolean[][] M, List<DocNode> instanceNodes, List<DocNode> documentNodes)
    {
    	// find index of insN
    	int insNIndex = -1;
    	int currIndex = -1;
    	Iterator insNodesIter = instanceNodes.iterator(); //faster than for loop
    	while(insNodesIter.hasNext() && insNIndex == -1)
    	{
    		currIndex ++;
    		Object nextObj = insNodesIter.next();
    		if (nextObj == insN)
    			insNIndex = currIndex;
    	}
    	
    	// find index of docN
    	int docNIndex = -1;
    	currIndex = -1;
    	Iterator docNodesIter = documentNodes.iterator(); //faster than for loop
    	while(docNodesIter.hasNext() && docNIndex == -1)
    	{
    		currIndex ++;
    		Object nextObj = docNodesIter.next();
    		if (nextObj == docN)
    			docNIndex = currIndex;
    	}
    	
    	return M[insNIndex][docNIndex];
    }
    
    protected static DocNode getCorrespondingNode(DocNode insN,
    	boolean[][] M, List<DocNode> instanceNodes, List<DocNode> documentNodes)
    {
    	// find index of insN
    	int insNIndex = -1;
    	int currIndex = -1;
    	Iterator insNodesIter = instanceNodes.iterator(); //faster than for loop
    	while(insNodesIter.hasNext() && insNIndex == -1)
    	{
    		currIndex ++;
    		Object nextObj = insNodesIter.next();
    		if (nextObj == insN)
    			insNIndex = currIndex;
    	}
    	
    	// find x where M[insNIndex][x] == 1
    	int x = -1;
    	for (int n = 0; n < M[insNIndex].length; n ++)
    	{
    		if (M[insNIndex][n])
    		{
    			x = n;
    			n = M[insNIndex].length; // break out of loop
    		}
    	}
    	
    	return documentNodes.get(x);
    }
    
    protected static List<DocNode> getCorrespondingNodes(DocNode insN,
    	boolean[][] M, List<DocNode> instanceNodes, List<DocNode> documentNodes)
    {
		List<DocNode> retVal = new ArrayList<DocNode>();
	
    	// find index of insN
    	int insNIndex = -1;
    	int currIndex = -1;
    	Iterator insNodesIter = instanceNodes.iterator(); //faster than for loop
    	while(insNodesIter.hasNext() && insNIndex == -1)
    	{
    		currIndex ++;
    		Object nextObj = insNodesIter.next();
    		if (nextObj == insN)
    			insNIndex = currIndex;
    	}
    	
    	// find x where M[insNIndex][x] == 1
    	//int x = -1;
    	for (int n = 0; n < M[insNIndex].length; n ++)
    	{
    		if (M[insNIndex][n])
    		{
    			retVal.add(documentNodes.get(n));
    			//x = n;
    			//n = M[insNIndex].length; // break out of loop
    		}
    	}
    	
    	//return (GenericSegment)documentNodes.get(x);
    	return retVal;
    }
    
    // we need the original nodeFrom and nodeTo, as they contain the matching details
    
    protected static boolean existsMatchNPath(DocNode insNodeFrom, DocNode insNodeTo, 
    	DocNode docNodeFrom, DocNode docNodeTo, 
    	DocEdge matchNEdge, List<DocEdge> documentEdges)
    {
    	DocNode currentNode = docNodeFrom;
    	boolean switched = false;
    	boolean loop = true;
    	while(loop)
    	{
    		List<DocEdge> edgeList = new ArrayList<DocEdge>();
    		// 28.02.09 SegmentList neighbours = getNeighboursFrom(currentNode, documentEdges, edgeList);
//    		List<DocNode> neighbours = getNeighboursFromHash(currentNode, null, edgeList);
    		List<DocNode> neighbours = getNeighboursFrom(currentNode, documentEdges, edgeList);
    		boolean foundNeighbour = false;
    		for (int n = 0; n < edgeList.size(); n ++)
    		{
    			DocEdge edge = edgeList.get(n);
    			if (compareEdges(matchNEdge, edge))
    			{
    				DocNode neighbour = neighbours.get(n);
    				
    				// this line added on the night before cebit 2.03.09
    				// THIS IS IMPORTANT: it means that we have to match either node...
    				// this refers to the if statement (previously if(true))
    				
    				boolean nodeOK = true;
    				if (matchNEdge.getMultipleMatch() == DocEdge.MATCH_N_TIL_LAST)
    					//nodeOK = compareNodes(insNodeFrom, neighbour) ||
    					//compareNodes(insNodeTo, neighbour);
    				
    				// 24.08.09 above 2 lines (nodeOK...) commented out and replaced by this if statement
    				{
    					if (!compareNodes(insNodeFrom, neighbour))
    						switched = true;
    					if (switched & !compareNodes(insNodeTo, neighbour))
    						nodeOK = false;
    				}
    				
    				if (nodeOK)
    				//if (compareNodes(insNodeFrom, neighbour) ||
    				//	compareNodes(insNodeTo, neighbour))
    				{
    					
	    				if (matchNEdge.getMultipleMatch() == DocEdge.MATCH_N_TIL_FIRST)
	    				{
	    					if (currentNode != docNodeFrom && compareNodes(insNodeFrom, currentNode))
	    						return false;
	    					if (neighbour != docNodeTo && compareNodes(insNodeTo, currentNode))
	    						return false;
	    				}
	    				
	    				
	    				if (neighbour == docNodeTo)
	    				{
	    					// TODO: decide whether we want to implement matchNFirst and matchNLast here...
	        				// instead of just "return true"...
	        				// 21.02.09
	    					
	    					/*
	    					System.out.println("Found a path...");
	    					System.out.println("nodeFrom: " + docNodeFrom);
	    					System.out.println("nodeTo: " + docNodeTo);
	    					*/
	    					
	    					// a path exists from nodeFrom to nodeTo
	    					if (matchNEdge.getMultipleMatch() == DocEdge.MATCH_N_ANY)
	    					{
	    						return true;
	    					}
	    					
	    					else if (matchNEdge.getMultipleMatch() == DocEdge.MATCH_N_TIL_FIRST)
	    					{
	    						return true;
	    						// check, if an intermediate node, that it doesn't match
	    						// insNodeFrom or insNodeTo .. but we do that above, don't we?
	    					}
	    					
	    					else // if (matchNEdge.getMultipleMatch() == Edge.MATCH_N_TIL_LAST) or MATCH_N_TIL_FIRST
	    					{
	    						/*
	    						System.out.println("checking with: dnodeFrom: " + docNodeFrom);
	    						System.out.println("checking with: dnodeTo: " + docNodeTo);
	    						System.out.println("matchNEdge: " + matchNEdge);
	    						*/
	    						
	    						// start at nodeTo and look forwards
	    						DocNode currentNode2 = docNodeTo;
	    				    	boolean loop2 = true;
	    				    	while(loop2)
	    				    	{
//	    				    		System.out.println("currentNode2: " + currentNode2);
	    				    		List<DocEdge> edgeList2 = new ArrayList<DocEdge>();
	    				    		// 28.02.09 SegmentList neighbours2 = getNeighboursFrom(currentNode2, documentEdges, edgeList2);
//	    				    		List<DocNode> neighbours2 = getNeighboursFromHash(currentNode2, null, edgeList2);
	    				    		List<DocNode> neighbours2 = getNeighboursFrom(currentNode2, documentEdges, edgeList2);
	    				    		boolean foundNeighbour2 = false;
	    				    		for (int n2 = 0; n2 < edgeList2.size(); n2 ++)
	    				    		{
	    				    			DocEdge edge2 = edgeList2.get(n2);
//	    				    			System.out.println("edge2: " + edge2);
	    				    			// 3.02.09 if (compareEdges(matchNEdge, edge2))
	    				    			if (compareEdges(matchNEdge, edge2) && 
	    				    				compareNodes(insNodeTo, neighbours2.get(n2)))
	    				    			{
	    				    				DocNode neighbour2 = neighbours2.get(n2);
	    				    				if (compareNodes(insNodeTo, neighbour2)) return false;
	    				    				
	    				    				// 3.02.09 the following lines will never be run
	    				    				// break out of loop (we only take the first valid edge atm)
	    				    				n2 = edgeList2.size();
	    				    				currentNode2 = neighbour2;
	    				    				foundNeighbour2 = true;
	    				    			}
	    				    		}
	    				    		if (!foundNeighbour2)
	    				    		{
//	    				    			System.out.println("no more neighbours setting loop2 to false");
	    				    		}
	    				    		
	    				    		if (!foundNeighbour2) loop2 = false;
	    				    	}	
	    						// until no more (matching) edges
	    						
	    						// start at nodeFrom and look backwards
	    				    	currentNode2 = docNodeFrom;
	    				    	loop2 = true;
	    				    	while(loop2)
	    				    	{
	    				    		List<DocEdge> edgeList2 = new ArrayList<DocEdge>();
	    				    		// 28.02.09 SegmentList neighbours2 = getNeighboursTo(currentNode2, documentEdges, edgeList2);
//	    				    		List<DocNode> neighbours2 = getNeighboursToHash(currentNode2, null, edgeList2);
	    				    		List<DocNode> neighbours2 = getNeighboursTo(currentNode2, documentEdges, edgeList2);
	    				    		boolean foundNeighbour2 = false;
	    				    		for (int n2 = 0; n2 < edgeList2.size(); n2 ++)
	    				    		{
	    				    			DocEdge edge2 = edgeList2.get(n2);
	    				    			// 3.02.09 if (compareEdges(matchNEdge, edge2))
	    				    			if (compareEdges(matchNEdge, edge2) &&
	    				    				compareNodes(insNodeFrom, neighbours2.get(n2)))
	    				    			{
	    				    				DocNode neighbour2 = neighbours2.get(n2);
	    				    				if (compareNodes(insNodeFrom, neighbour2)) return false;
	    				    				
	    				    				// break out of loop (we only take the first valid edge atm)
	    				    				n2 = edgeList2.size();
	    				    				currentNode2 = neighbour2;
	    				    				foundNeighbour2 = true;
	    				    			}
	    				    		}
	    				    		if (!foundNeighbour2) loop2 = false;
	    				    	}

	    						// until no more (matching) edges
	    						return true; // if nothing found which breaks the conditions
	    					}
	    				}
	    				
	    				
	    				// else look further
	    				// break out of loop (we only take the first valid edge atm)
	    				n = edgeList.size();
	    				currentNode = neighbour;
	    				foundNeighbour = true;
    				}
    			}
    		}
    		if (!foundNeighbour) loop = false;
    	}
    	
    	return false;
    }
    
    protected static boolean refineM(boolean[][] M, List<DocNode> instanceNodes,
		List<DocEdge> instanceEdges, List<DocNode> documentNodes, List<DocEdge> documentEdges)
	{
		//System.out.println("in refineM");
    	boolean loop = true;
    	
    	while(loop)
    	{
	    	boolean changeMade = false;
	    	for (int a = 0; a < M.length; a ++) // i
	     	{
	    		boolean noOne = true;
	     		for (int b = 0; b < M[a].length; b ++) // j
	     		{
	     			if (M[a][b]) // if vai corresponds to vbj in any iso. under M
	     			{
	     				noOne = false;
	     				boolean forAllX = true;
	     				DocNode insNode = instanceNodes.get(a);
	     				DocNode docNode = documentNodes.get(b);
	     				
	     				//System.out.println("insNode: " + insNode);
	     				//System.out.println("docNode: " + docNode);
	     				
	     				List<DocEdge> insNEdges = new ArrayList<DocEdge>();
	     				List<DocEdge> docNEdges = new ArrayList<DocEdge>();
	     				
	     				List<DocNode> insNeighbours = getNeighboursFrom
	     					(insNode, instanceEdges, insNEdges);
	     				
	     				//System.out.println("insNeighbours from " + insNode);
	     				//System.out.println(insNeighbours);
	     				
	     				// 28.02.09 SegmentList docNeighbours = getNeighboursFrom
	     				//	(docNode, documentEdges, docNEdges);
//	     				List<DocNode> docNeighbours = getNeighboursFromHash
//	     					(docNode, null, docNEdges);
	     				
	     				List<DocNode> docNeighbours = getNeighboursFrom
     						(docNode, documentEdges, docNEdges);
	     				
	     				//System.out.println("docNeighbours from " + docNode);
	     				//System.out.println(docNeighbours);
	     				
	     				// check all insNeighbours
	     				for (int n = 0; n < insNeighbours.size(); n ++)
	     				{
	     					DocNode insN = insNeighbours.get(n);
	     					DocEdge insE = insNEdges.get(n);
	     					
	     					//System.out.println("checking with insNeighbour : " + insN);
	     					
	     					boolean existsY = false;
	     					if (insE.getMultipleMatch() == DocEdge.MATCH_ONE)
	     					{
	     						// check that there is a resp. node as per Ullmann
	     						// and that the edge matches
	     						
	     						// go through all docNEdges
	     							// if edge matches thisE
	     							// AND if node corresponds to thisN
	     								//then set existsY to true
	     						
	     						for (int p = 0; p < docNeighbours.size(); p ++)
			     				{
	     							DocNode docN = docNeighbours.get(p);
			     					DocEdge docE = docNEdges.get(p);
			     					
			     					if (compareEdges(insE, docE) &&
			     						corresponds(insN, docN, M, 
			     						instanceNodes, documentNodes))
			     						
			     						existsY = true;
			     				}
	     					}
	     					else // multiple match
	     					{
	     						// matchN, 0plus, etc.
	     						// TBD
	     						
	     						// here we
	     						
	     						// docN and insN mean docNeighbour and insNeighbour...
	     						
	     						// we need to start at the corresponding node of insN
	     						List<DocNode> correspondingNodes = getCorrespondingNodes(insN, M,
	     							instanceNodes, documentNodes);
	     						
	     						for (DocNode docN : correspondingNodes)
	     						{
		     						//System.out.println("corresponding node: " + docN);
		     						
		     						// now we look for a path from docNode to docN
		     						// in the document using only edges which "compare"
		     						// to insE
		     						
		     						// to be done here!!
		     						if (existsMatchNPath(insNode, insN, docNode, docN, insE, 
		     							documentEdges))
		     						{
		     							//System.out.println("exists matchN path");
		     							existsY = true;
		     						}
		     						
		     						// ZEROPLUS: this necessitates a major change
		     						// in the Ullmann enumeration algorithm to allow
		     						// finished isomorphisms to not include all matched
		     						// source nodes...
		     						
		     						// if not zeroplus
		     						// check if 
		     						
		     						// if zeroplus, allow the node to match to itself...
	     						}
	     					}
	     					if (!existsY)
     						{
	     						//System.out.println("implication broken");
     							// implication broken;
     							forAllX = false;
     						}
	     				}
	     				if (!forAllX)
	     				{
	     					M[a][b] = false;
	     					//System.out.println("matrix changed to:");
	     					//printBinaryMatrix(M);
	     					
	     					changeMade = true;
	     				}
	     			}	
	     		}
	     		//if (noOne) System.out.println("noOne ... returning false");
	     		if (noOne) return false;
	     	}
	    	if (!changeMade) loop = false;
    	}
    	//System.out.println("**********************************");
    	return true;
	}

	protected static void printBinaryMatrix(boolean[][] M)
	{
		for (int a = 0; a < M.length; a ++)
    	{
    		for (int b = 0; b < M[a].length; b ++)
    		{
    			if (M[a][b])
    				System.out.print(" 1");
    			else System.out.print(" 0");
    			//System.out.print(M[a][b]);
    		}
    		System.out.println();
    	}
	}
	
	protected static void mirrorBinaryMatrix(boolean[][] M)
	{
		//System.out.println("in mirror with width: " + M.length);
		//System.out.println("in mirror with height: " + M[0].length);
		for (int a = 0; a < M.length; a ++)
    	{
    		for (int b = 0; b < M[a].length; b ++)
    		{
    			if (M[a][b])
    				M[b][a] = true;
    		}
    	}
	}
	
	public static boolean[][] copyBinaryMatrix(boolean[][] M)
	{
		boolean[][] R = new boolean[M.length][M[0].length];
		for (int a = 0; a < M.length; a ++)
    	{
    		for (int b = 0; b < M[a].length; b ++)
    		{
    			R[a][b] = M[a][b];
    		}
    	}
		return R;
	}
	
	protected static boolean compareNodes(DocNode insNode, DocNode docNode)
	{
		if (!insNode.isTextSegment()) return false;
//		TextSegment ts = (TextSegment)insNode;
		if (!docNode.isTextSegment()) return false;
//		TextSegment docTs = (TextSegment)docNode;
		
		boolean typographyMatch = true;
		boolean contentMatch = true;
		boolean minLengthMatch = true;
		boolean maxLengthMatch = true;
		
		if (insNode.isMatchFont())
			if (!insNode.getSegFontName().equals(docNode.getSegFontName()))
				typographyMatch = false;
		if (insNode.isMatchFontSize())
			if (insNode.getSegFontSize() != docNode.getSegFontSize())
				typographyMatch = false;
		if (insNode.isMatchBold())
			if (insNode.isBold() != docNode.isBold())
				typographyMatch = false;
		if (insNode.isMatchItalic())
			if (insNode.isItalic() != docNode.isItalic())
				typographyMatch = false;
		if (insNode.getMatchContent() == 
			DocNode.MATCH_CONTENT_STRING)
			if (!docNode.getSegText().trim().equals
				(insNode.getMatchContentString().trim()))
				contentMatch = false;
		if (insNode.getMatchContent() == 
			DocNode.MATCH_CONTENT_SUBSTRING)
			if (!docNode.getSegText().contains
				(insNode.getMatchContentString()))
				contentMatch = false;
		if (insNode.getMatchContent() == 
			DocNode.MATCH_CONTENT_REGEXP)
			if (!docNode.getSegText().trim().matches
				(insNode.getMatchContentString()))
				contentMatch = false;
		if (insNode.getMatchMinLength() >= 0)
			if (docNode.getSegText().length() <
				insNode.getMatchMinLength())
				minLengthMatch = false;
		if (insNode.getMatchMaxLength() >= 0)
			if (docNode.getSegText().length() >
				insNode.getMatchMaxLength())
				maxLengthMatch = false;
		
		/*
		System.out.println(ts.getSegText() + " compare " + docTs.getSegText() + " nodeCompare returning " + (typographyMatch && contentMatch &&
			minLengthMatch && maxLengthMatch));
		
		System.out.println("ts.font " + ts.getFontName() + " dts.font " + docTs.getFontName() +
			" ts.fontsize " + ts.getSegFontSize() + " dts.fontsize " + docTs.getSegFontSize());
		
		System.out.println("typ: " + typographyMatch + " con: " + contentMatch + " minL: " + minLengthMatch + " maxL: " + maxLengthMatch);
		*/
		
		return (typographyMatch && contentMatch &&
			minLengthMatch && maxLengthMatch);
	}
	
	protected static boolean compareEdgesAndNodes(DocEdge insEdge, DocEdge docEdge)
	{
		if (compareEdges(insEdge, docEdge))
		{
			return (compareNodes(insEdge.getFrom(), docEdge.getFrom()) &&
					compareNodes(insEdge.getTo(), docEdge.getTo()));
		}
		else return false;
	}
	
	protected static boolean compareEdges(DocEdge insEdge, DocEdge docEdge)
	{
		/*
		System.out.println("in compareEdges with: " + insEdge + " and: " + docEdge);
		
		System.out.println("insEdge.getWeight: " + insEdge.getWeight());
		System.out.println("docEdge.getWeight: " + docEdge.getWeight());
		
		System.out.println("insEdge.getMatchMaxLength: " + insEdge.getMatchMaxLength());
		*/
		
		//System.out.println("insEdge class: " + insEdge.getNodeFrom().getClass());
		//System.out.println("docEdge class: " + docEdge.getNodeFrom().getClass());
		
		boolean objects = true;
		//if (!insEdge.getNodeFrom().getClass().equals(docEdge.getNodeFrom().getClass()))
		if (insEdge.getFrom().isTextSegment() != docEdge.getFrom().isTextSegment())
			objects = false;
		//if (!insEdge.getNodeTo().getClass().equals(docEdge.getNodeTo().getClass()))
		if (insEdge.getTo().isTextSegment() != docEdge.getTo().isTextSegment())
			objects = false;
		
		boolean relation = false;
		if (insEdge.getRelation().equals(
			docEdge.getRelation()))
			relation = true;
		
		// TODO: for reverse relations (not yet implemented/required)
		
		boolean length = true;
		if (insEdge.getMatchLength() == DocEdge.LENGTH_BLOCK)
			if (docEdge.getLogicalLength() != 
				DocEdge.LENGTH_BLOCK)
				length = false;
		if (insEdge.getMatchLength() == DocEdge.LENGTH_COLUMN)
			if (docEdge.getLogicalLength() != 
				DocEdge.LENGTH_COLUMN)
				length = false;
		if (insEdge.getMatchLength() == DocEdge.LENGTH_GREATER)
			if (docEdge.getLogicalLength() != 
				DocEdge.LENGTH_GREATER)
				length = false;
		
		if (insEdge.getMatchMinLength() != 0.0f && 
			docEdge.getWeight() < insEdge.getMatchMinLength())
			length = false;
				
		if (insEdge.getMatchMaxLength() != 0.0f && 
			docEdge.getWeight() > insEdge.getMatchMaxLength())
			length = false;
		
		/*
		System.out.println("insEdge.getMatchMaxLength() " + insEdge.getMatchMaxLength());
		System.out.println("insEdge.getMatchMinLength() " + insEdge.getMatchMinLength());
		System.out.println("insEdge.getLength() " + insEdge.getLength());
		*/
		
		// 19.01.09 'match alignment' changed to 'require alignment'
		
		//boolean alignTopLeft = true, alignCentre = true, 
		//	alignBottomRight = true;
		boolean alignment = true;
		//if (insEdge.isMAlignTopLeft() && !docEdge.isAlignTopLeft())
		if (insEdge.isMAlignTopLeft() && (docEdge.isAlignTopLeft() != insEdge.isAlignTopLeft()))
			alignment = false;
		//if (insEdge.isMAlignCentre() && !docEdge.isAlignCentre())
		if (insEdge.isMAlignCentre() && (docEdge.isAlignCentre() != insEdge.isAlignCentre()))
			alignment = false;
		if (insEdge.isMAlignBottomRight() && (docEdge.isAlignBottomRight() != insEdge.isAlignBottomRight()))
		//if (insEdge.isMAlignBottomRight() && !docEdge.isAlignBottomRight())
			alignment = false;
		
		boolean crossesRulingLine = true;
		if (insEdge.isMatchCrossesRulingLine() &&
			(insEdge.isCrossesRulingLine() != 
			docEdge.isCrossesRulingLine()))
			crossesRulingLine = false;
		
		boolean readingOrder = true;
		if (insEdge.isMatchReadingOrder() && 
			(insEdge.getReadingOrder() != 
			docEdge.getReadingOrder()))
			readingOrder = false;
		
		boolean superiorInferior = true;
		if (insEdge.isMatchSuperiorInferior() &&
			(insEdge.getSuperiorInferior() != 
			docEdge.getSuperiorInferior()))
			superiorInferior = false;
		
		/*
		System.out.println("rel " + relation + " len " + length + " align " + alignment +
			" cRL " + crossesRulingLine + " rO " + readingOrder + " sI " + superiorInferior + "obj " + objects);
		*/
		/*
		System.out.println("edgeCompare returning " + (relation && length && alignment && crossesRulingLine
				&& readingOrder && superiorInferior && objects));
		*/
		
		return (relation && length && alignment && crossesRulingLine
			&& readingOrder && superiorInferior && objects);
	}
	
	protected static boolean[][] generateStartMatrix
		(List<DocNode> instanceNodes, List<DocNode> documentNodes)//, EdgeList instanceEdges,
		//EdgeList documentEdges)//, SegmentList matchNNodes)
	{
		boolean[][] startMatrix = 
			new boolean[instanceNodes.size()][documentNodes.size()];
        
		int i1count = -1;
		for (DocNode insN : instanceNodes)
        {
        	i1count ++; // quicker to use iterators than for-next loops
//        	Object insObj = i1.next();  // ith point
        	
        	// we are counting edgesFrom and edgesTo as part of the 'degree'
        	// calculation as the opposite relation conveys a meaning
    	//	EdgeList insEdges = dg.getEdgesFrom(insSeg);
    	//	insEdges.addAll(dg.getEdgesTo(insSeg));
        	
        	int i2count = -1;
        	for (DocNode docN : documentNodes)
        	{
        		i2count ++;
//        		Object docObj = i2.next();  // jth point
        		
        		// if edge was here; we are only working with nodes though
        		if (true)
        		{
        			// if not an edge, then must be some kind of segment
        			//if (insObj.getClass() == docObj.getClass())
        			// 29.12.08 changed (don't know if for good)
        			// due to CTSs etc. being saved as TextSegments only
        			if (insN.isTextSegment() && docN.isTextSegment())
        			{
        				// added 14.11.08 (!matchNNodes.contains(insObj) && condition)
        				//if (!matchNNodes.contains(insObj) &&
        				if (insN.isTextSegment())
        				{
        					// we can assume docTs is also a TS
        					// as it must be the same class as insObj...
        					startMatrix[i1count][i2count] = compareNodes(insN, docN);
        				}
        				else
        				{
        					startMatrix[i1count][i2count] = true;
        				}
        			}
        			else
        				startMatrix[i1count][i2count] = false;
        		}
        	}
        }
        return startMatrix;
	}
	
	// think not!
	// pre: DG hashmap already run (dg.indexEdges)
	public static boolean checkForConnectedness(DocumentGraph dg)
	{
		// check if there are any 'lone nodes'
		// (except if that's the only node in the graph'
		
		int enabledNodes = 0;
		for (DocNode n : dg.getNodes())
			if (!n.isRemoveFromInstance())
				enabledNodes ++;
		
		//System.out.println("enabledNodes size: " + enabledNodes);
		
		if (enabledNodes > 1)
		//if (dg.getVertList().size() > 1)
		{
			for (DocNode n : dg.getNodes())
			{
				if (!n.isRemoveFromInstance())
				{
					//System.out.println("checking for loneness: " + gs);
					//TODO: change to hash map lookup!
					//EdgeList edges = dg.getEdges(gs);
					boolean loneNode = true;
					
					for (DocEdge ae : dg.edgesFromTo(n))
					{
						if (!ae.isRemoveFromInstance() &&
							(ae.getFrom() == n ||
							ae.getTo() == n))
							{
								//System.out.println("londNode false with " + ae);
								loneNode = false;
							}
					}
					if (loneNode) 
					{
						//System.out.println("found lone node: " + gs);
						//System.out.println("with edges: "  + edges);
						return false;
					}
				}
			}
		}
		
		HashMap<DocNode, List<DocNode>> groupHash = 
			new HashMap<DocNode, List<DocNode>>();
		List<List<DocNode>> groups = new ArrayList<List<DocNode>>();
		
		for (DocEdge ae : dg.getEdges())
		{
			if (!ae.isRemoveFromInstance())
			{
				DocNode nodeFrom = ae.getFrom();
				DocNode nodeTo = ae.getTo();
				
				if (groupHash.containsKey(nodeFrom) &&
					groupHash.containsKey(nodeTo))
				{
					List<DocNode> nodeFromGroup =
						groupHash.get(nodeFrom);
					List<DocNode> nodeToGroup =
						groupHash.get(nodeTo);
					
					if (nodeFromGroup == nodeToGroup)
					{
						// groups ok; do nothing
					}
					else // merge the group; put all nodeToGroup's nodes 
						 // into nodeFromGroup
					{
						for (DocNode n : nodeToGroup)
						{
							// should overwrite existing hash table entry!
							groupHash.put(n, nodeFromGroup); 
							nodeFromGroup.add(n);
						}
						//TEST
						//if (nodeToGroup.size() > 0)
						//	System.err.println("checkForConnectedness: group not empty!");
						groups.remove(nodeToGroup);
					}
				}
				else if (groupHash.containsKey(nodeFrom))
				{
					// put nodeTo in nodeFrom's group
					List<DocNode> nodeFromGroup =
						groupHash.get(nodeFrom);
					groupHash.put(nodeTo, nodeFromGroup);
					nodeFromGroup.add(nodeTo);
				}
				else if (groupHash.containsKey(nodeTo))
				{
					// put nodeFrom in nodeTo's group
					List<DocNode> nodeToGroup =
						groupHash.get(nodeTo);
					groupHash.put(nodeFrom, nodeToGroup);
					nodeToGroup.add(nodeFrom);
				}
				else // neither node has a group
				{
					List<DocNode> newGroup = new ArrayList<DocNode>();
					newGroup.add(nodeFrom);
					newGroup.add(nodeTo);
					groupHash.put(nodeFrom, newGroup);
					groupHash.put(nodeTo, newGroup);
					groups.add(newGroup);
				}
			}
		}
		
		/*
		for each edge
		check if nodeFrom has a group
		check if nodeTo has a group
		
		if neither has a group
			create new group and put both nodes in it
		if both have the same group
			put both nodes in it
		if both have different groups
			merge the groups
			update hashes
			add to the merged group
		if one has a group
			put the other into that group
		 */
		
		//System.out.println("Groups.size: " + groups.size());
		
		// if we have one group return true else false
		// actually, if we have no groups, technically we also
		// have a connected graph? (i.e. lone node)
		return (groups.size() <= 1);
	}
	
	public static List<WrappingInstance> findInstances(DocumentGraph dg,
		DocumentGraph wrapperGraph, Document resultDocument,
		List<List<String>> returnFieldNames, List<List<String>> returnExtractedData)
	{
		List<List<DocNode>> result = performExtraction
			(dg, wrapperGraph, resultDocument, returnFieldNames, returnExtractedData);
		
		return toWrappingInstances(result);
	}
	
	public static List<WrappingInstance> toWrappingInstances
		(List<List<DocNode>> result)
	{
		List<WrappingInstance> retVal = new ArrayList<WrappingInstance>();
		
		for (List<DocNode> match : result)
			retVal.add(new WrappingInstance(match)); // finds bounding box too
		
		return retVal;
	}

	/* 28.12.08
		 * clones and prepares (i.e. joins matchN edges) wrapper graph
		 * as document graph isn't altered, no need to clone it
		 */
		public static List<List<DocNode>> performExtraction(DocumentGraph dg,
			DocumentGraph wrapperGraph, Document resultDocument, 
			List<List<String>> returnFieldNames, List<List<String>> returnExtractedData)//Element parentElement)
		{
			//Element resultElement = null;
			//if (parentElement != null && resultDocument != null)
			//{
			//	resultElement = resultDocument.createElement("wrapper-result");
			//	parentElement.appendChild(resultElement);
			//}
	////		Element resultElement = parentElement;
			//System.out.println("wrapperGraph.getEdges: " + wrapperGraph.getEdges());
			//System.out.println("wrapperGraph.getNodes: " + wrapperGraph.getVertList());
			
//			List<WrappingInstance> retVal = new ArrayList<WrappingInstance>();
			List<List<DocNode>> retVal = new ArrayList<List<DocNode>>();
			
//			List<DocEdge> instanceEdgesTemp = wrapperGraph.getEdges();
			List<DocEdge> instanceEdgesTemp = new ArrayList<DocEdge>();
			for (DocEdge e : wrapperGraph.getEdges())
				instanceEdgesTemp.add(e);
			
			// HashMap from dgEdges to newly cloned instanceEdges
	        HashMap<DocEdge, DocEdge> hm = new HashMap<DocEdge, DocEdge>();
	        
	        List<DocEdge> instanceEdges = new ArrayList<DocEdge>();
	        for (DocEdge ae : instanceEdgesTemp)
	        {
				DocEdge cae = (DocEdge)ae.clone();
				instanceEdges.add(cae);
				hm.put(ae, cae);
	        }
	        
	        // 20.11.08 end of addition
	        
//	        ListUtils instanceNodes = (ListUtils)wrapperGraph.getVertList().clone();
	        List<DocNode> instanceNodes = new ArrayList<DocNode>();
	        for (DocNode n : wrapperGraph.getNodes())
	        	instanceNodes.add(n);
	        
	        //TODO!!!
	        /*
	        // use hashing to speed up...
	        DocumentGraph instanceGraph = new DocumentGraph();
	        instanceGraph.
	        */
	        
	        List<DocNode> documentNodes = dg.getNodes();
	        List<DocEdge> documentEdges = dg.getEdges();
	        
	        // remove from the list any which are removed from the match
	        List<DocEdge> edgesToRemove = new ArrayList<DocEdge>();
	        for (DocEdge ae : instanceEdges)
	        	if (ae.isRemoveFromInstance())
	        		edgesToRemove.add(ae);
	        instanceEdges.removeAll(edgesToRemove);
	        
	        List<DocNode> nodesToRemove = new ArrayList<DocNode>();
	        for (DocNode n : instanceNodes)
	        	if (n.isRemoveFromInstance())
	        		nodesToRemove.add(n);
	        instanceNodes.removeAll(nodesToRemove);
			
	        //System.out.println("after removing disabled nodes:");
	        //System.out.println("wrapperGraph.getEdges: " + instanceEdges);
		 	//System.out.println("wrapperGraph.getNodes: " + instanceNodes);
	        
	        // TODO: refactor and move to separate method for readability
	        // now join any neighbouring matchN edges...
//	        List<DocNode> visitedNodes = new ArrayList<DocNode>();
	        nodesToRemove = new ArrayList<DocNode>();
	        //EdgeList visitedEdges = new EdgeList();
	        edgesToRemove = new ArrayList<DocEdge>();
	        //ieIter = instanceEdges.iterator();
	        
//	        for (int n = 0; n < instanceEdges.size(); n ++)
	        for (DocEdge ae : instanceEdges)
	        {
//	        	DocEdge ae = (DocEdge)instanceEdges.get(n);
	        	//if (ae.isMatchN() && !edgesToRemove.contains(ae))
	        	if (ae.getMultipleMatch() != DocEdge.MATCH_ONE && !edgesToRemove.contains(ae))
	        	// either the edge has been visited in order or
	        	// the edge has been marked for removal
	        		//!visitedEdges.contains(ae))  could be used for speedup
	        	{
	        		// first, look in direction of edge
	        		DocNode currentNode = ae.getTo();  
	        		DocEdge currentEdge = ae;
	        	////	System.out.println("currentEdge: " + ae);
	        		boolean loop = true;
	        		while(loop)
	        		{
	        			//System.out.println("loopa with currentNode: " + currentNode);
	        			//visitedEdges.add(currentEdge);
	        			
	        			// will be set false if too many edges/sidewards edges encountered
	        			// if true, look further (if nextEdge also found)
	        			boolean expand = true;
	        			
	        			// check whether currentNode has sidewards edges and find next edge
	        			// to expand to...
	        			boolean sidewardsEdges = false;
	        			DocEdge nextEdge = null;
	        			
	        			for (DocEdge aeCheck : instanceEdges)
	        			{
	        				if (aeCheck != currentEdge && aeCheck != ae)
	        				{
		        				if (aeCheck.getFrom() == currentNode)
		        				{
			        				if //(aeCheck != currentEdge &&  // currentEdge != sidewards edge!
			        					(aeCheck.getRelation().equals(currentEdge.getRelation()))
			        				{
			        					if (nextEdge == null)
			        					{
			        						nextEdge = aeCheck;
			        					}
			        					else
			        					{
			        						// nextEdge already assigned; too many edges; don't expand
			        						expand = false;
			        					}
			        				}
			        				else
			        				{
			        					// sidewards edge
			        					expand = false;
			        				}
		        				}
		        				else if (aeCheck.getTo() == currentNode)
		        				{
		        					if //(aeCheck != currentEdge && 
		        						(DocEdge.isInverse(aeCheck, currentEdge))
			        				{
			        					// such an edge will probably never exist in the graph
		        						if (nextEdge == null)
			        					{
			        						nextEdge = aeCheck;
			        					}
			        					else
			        					{
			        						// nextEdge already assigned; too many edges; don't expand
			        						expand = false;
			        					}
			        				}
		        					else
		        					{
		        						// sidewards edge
			        					expand = false;
		        					}
		        				}
	        				}
	        			}
	        			
	        			// check whether nextEdge is match N!
	        			if (expand && nextEdge != null && nextEdge.getMultipleMatch() != DocEdge.MATCH_ONE) //nextEdge.isMatchN())
	        			{
	        				//System.out.println("in Expand with nextEdge: " + nextEdge);
	        				
	        				// remove currentNode from graph match
	        				nodesToRemove.add(currentNode);
	        				
	        				// remove currentEdge from graph match
	        				edgesToRemove.add(nextEdge);
	        				
	        				//System.out.println("for edge: " + ae);
	        				//System.out.println("setting nodeTo to: " + nextEdge.getNodeTo());
	        				ae.setTo(nextEdge.getTo());
	        				
	        				// set new edge and node as current
	        				currentEdge = nextEdge;
	        				currentNode = nextEdge.getTo();
	        				
	        				// look further in this direction
	        				loop = true;
	        			}
	        			else
	        			{
	        				loop = false;
	        			}
	        		}
	        		
	        		
	        		// and now, look in the opposite direction
	        		currentNode = ae.getFrom();
	        		currentEdge = ae; // should still be the case
	        		//System.out.println("currentEdgeb: " + ae);
	        		loop = true;
	        		while(loop)
	        		{
	        			//System.out.println("loopb with currentNode: " + currentNode);
	        			boolean expand = true;
	        			// check whether currentNode has sidewards edges and find next edge
	        			// to expand to...
//	        			boolean sidewardsEdges = false;
	        			DocEdge nextEdge = null;
	        			for (DocEdge aeCheck : instanceEdges)
	        			{
	        				if (aeCheck != currentEdge && aeCheck != ae)
	        				{
		        				if (aeCheck.getTo() == currentNode)
		        				{
		        					if //(aeCheck != currentEdge && //currentEdge != sidewards edge!
			        					(aeCheck.getRelation().equals(currentEdge.getRelation()))
			        				{
			        					if (nextEdge == null)
			        					{
			        						nextEdge = aeCheck;
			        					}
			        					else
			        					{
			        						// nextEdge already assigned; too many edges; don't expand
			        						expand = false;
			        					}
			        				}
			        				else
			        				{
			        					// sidewards edge
			        					expand = false;
			        				}
		        				}
		        				else if (aeCheck.getFrom() == currentNode)
		        				{
		        					if //(aeCheck != currentEdge && 
//		        						(aeCheck.getRelation().equals
//			        					(currentEdge.getRelation().getInverse()))
		        						(DocEdge.isInverse(aeCheck, currentEdge))
			        				{
			        					// such an edge will probably never exist in the graph
		        						if (nextEdge == null)
			        					{
			        						nextEdge = aeCheck;
			        					}
			        					else
			        					{
			        						// nextEdge already assigned; too many edges; don't expand
			        						expand = false;
			        					}
			        				}
		        					else
		        					{
		        						// sidewards edge
			        					expand = false;
		        					}
		        				}
	        				}
	        			}
	        			
	        			// check whether nextEdge is match N!
	        			if (expand && nextEdge != null && nextEdge.getMultipleMatch() != DocEdge.MATCH_ONE)//nextEdge.isMatchN())
	        			{
	        				//System.out.println("in Expand with nextEdge: " + nextEdge);
	        				// remove currentNode from graph match
	        				nodesToRemove.add(currentNode);
	        				
	        				// remove currentEdge from graph match
	        				edgesToRemove.add(nextEdge);
	        				
	        				//System.out.println("for edge: " + ae);
	        				//System.out.println("setting nodeFrom to: " + nextEdge.getNodeFrom());
	        				ae.setFrom(nextEdge.getFrom());
	        				
	        				// set new edge and node as current
	        				currentEdge = nextEdge;
	        				currentNode = nextEdge.getFrom();
	        				
	        				// look further in this direction
	        				loop = true;
	        			}
	        			else
	        			{
	        				loop = false;
	        			}
	        		}
	        		
	        	}
	        }
	        
	        //System.out.println("find instances one");
	        
	        //System.out.println("removing instanceEdges: " + edgesToRemove);
	        instanceEdges.removeAll(edgesToRemove);
	        //System.out.println("removing instanceNodes: " + nodesToRemove);
	        instanceNodes.removeAll(nodesToRemove);
			
	        // ** FOLLOWING COPIED FROM FINDINSTANCESWITHOUTMATCHN
	        
//	        int noInsNodes = instanceNodes.size(); // p sub alpha
//	        int noDocNodes = documentNodes.size(); // p sub beta

	        // this SegmentList is prob. to remain blank 16.02.09
	        List<DocNode> matchNNodes = new ArrayList<DocNode>();
	        
	        //Matrix M = new Matrix();
	        // note: Matrix class in PDFBox supports only 3 x 3 matrices
	        
	        // set up the start matrix M sub 0
	//        boolean[][] startMatrix = new boolean[noInsNodes][noDocNodes];
	        
	        boolean[][] startMatrix = generateStartMatrix(instanceNodes, documentNodes);
	        	//,	instanceEdges, documentEdges, matchNNodes);
	        
	    	// Ullmann
	    	List<boolean[][]> graphMatchResult = ullmannAlgorithm//(A, B, startMatrix);
	    		(instanceNodes, instanceEdges, documentNodes, documentEdges, startMatrix);
	    	for(boolean[][] M : graphMatchResult)
	    	{
	    		//printBinaryMatrix(M);
	    		
	    		//isomorphisms ++;
//				WrappingInstance match = new WrappingInstance();
//	    		changed 2011-02-18
	    		List<DocNode> match = new ArrayList<DocNode>();
	    		
//				match.setClassification(DocNode.C_WRAPPING_INSTANCE);
				
				List<String> fieldNames = new ArrayList<String>();
				List<String> extractedData = new ArrayList<String>();
				
			///	EdgeList matchNEdges = new EdgeList();
				// System.out.println("isomorphism found!");
				
				for (int a = 0; a < M.length; a ++)
	        	{
	        		for (int b = 0; b < M[a].length; b ++)
	        		{
	        			if (M[a][b])// && isIsomorphism(M, A, B))
	        			{
//		        			if (documentNodes.get(b) instanceof GenericSegment)
//		        			{
		        				match.add(documentNodes.get(b));
//		        				changed 2011-02-18
//	        					match.getItems().add(documentNodes.get(b).toGenericSegment());
		        				
//		        				if (documentNodes.get(b) instanceof TextSegment)
	        					if (documentNodes.get(b).isTextSegment())
		        				{
		        					DocNode insTs = instanceNodes.get(a);
		        					DocNode docTs = documentNodes.get(b);
		        					if (insTs.isExtractContent() && 
		        						//wrapperGraph.getVertList().contains(insTs))
		        						// matchNNode seems to get added to wrapperGraph somewhere
		        						!matchNNodes.contains(insTs))
		        					{
		        						fieldNames.add(insTs.getSegType());
		        						extractedData.add(docTs.getSegText());
		        					}
		        				}
//		        			}
	        			}
	        		}
	        	}
				
//				List<DocEdge> foundMatchNEdges = new ArrayList<DocEdge>();
//				List<DocNode> foundMatchNNodes = new ArrayList<DocNode>();
				
//				match.findBoundingBox();
				retVal.add(match);
				
				// now null can also be passed 23.02.09
				if (returnFieldNames != null)
					returnFieldNames.add(fieldNames);
				if (returnExtractedData != null)
					returnExtractedData.add(extractedData);
				
//				System.out.println("in perform Extraction with match: ");
//				ListUtils.printList(match);
	    	}
	    	
	    	// TODO: find bounding boxes!
	    	
	    	return retVal;
	    }

	public static List<boolean[][]> ullmannAlgorithm
		(List<DocNode> instanceNodes, List<DocEdge> instanceEdges,
		List<DocNode> documentNodes, List<DocEdge> documentEdges, boolean[][] M)
		{
//			System.out.println("in Ullmann");
			//System.out.println("M:");
			//printBinaryMatrix(M);
			
			int noInsNodes = instanceNodes.size();
			int noDocNodes = documentNodes.size();
			ArrayList<boolean[][]> retVal = new ArrayList<boolean[][]>();
			
			boolean[][][]Md = new boolean[noDocNodes][noInsNodes][noDocNodes];
	    	for (int a = 0; a < noDocNodes; a ++)
	    		for (int b = 0; b < noInsNodes; b ++)
	    			for (int c = 0; c < noDocNodes; c ++)
	    				Md[a][b][c] = false;
	    	
	    //	Md.add(startMatrix);
	    //	M = (boolean[][]) Md.get(0);
	    	int d = 0; //int d = 1;
	    	int k = -1; // dummy value is necessary to allow compilation
	    	boolean[] F = new boolean[noDocNodes];
	    	int[] H = new int[noInsNodes];
	    	H[0] = -1; //H[1] = 0;
	    	boolean valueFound;
	    	
	    	int possibleIsomorphisms = 0;
	    	int isomorphisms = 0;
	    	
	    	for (int i = 0; i < F.length; i ++)
	    	{
	    		F[i] = false;
	    	}
	    	
	    	boolean loop = true;
	    	int nextStep = 2;
	    	int iteration = -1;
	    	
	    	if (!refineM(M, instanceNodes, instanceEdges, 
	    		documentNodes, documentEdges))
	    	{
	    		System.out.println("terminating algorithm at first step!");
	    		loop = false;
	    	}
	    	
	    	while(loop)
	    	{
	    		iteration ++;
	    		if (iteration % 10000 == 0)
	    		{
	    			System.out.println("Iteration: " + iteration);
	    		}
	    		//System.out.println("Iteration: " + iteration + " d: " + d + " k: " + k);
	    		//printBinaryMatrix(M);
	    		switch(nextStep)
	    		{
	    			case 2:
	    				//System.out.println("step 2");
	    				// check whether there is a value of j such that M[d][j]==1 and F[j]==0
	    				valueFound = false;
	    				for (int j = 0; j < noDocNodes; j ++)
	    				{
	    					//System.out.println("noInsNodes: " + noInsNodes + " noDocNodes: " + noDocNodes);
	    					//System.out.println("d: " + d + " j: " + j);
	    					if (M[d][j] == true && F[j] == false)
	    						valueFound = true;
	    				}
	    				if (valueFound)
	    				{
	    					////	Md.setElementAt(M, d);
	    					for (int a = 0; a < noInsNodes; a ++)
	    						for (int b = 0; b < noDocNodes; b ++)
	    							Md[d][a][b] = M[a][b];
	    					
	    					//Md.set(d, M); //Md = M;
	    					if (d == 0) //if (d == 1)
	    						k = H[0]; //k = H[1];
	    					else
	    						k = -1; //k = 0;
	    					nextStep = 3;
	    				}
	    				else
	    				{
	    					nextStep = 7;
	    				}
	    				break;
	    			case 3:
	    				//System.out.println("step 3");
	    				k ++;
	    				if (M[d][k] == false || F[k] == true)
	    				{
	    					nextStep = 3;
	    				}
	    				else
	    				{
	    					for (int j = 0; j < noDocNodes; j ++)
	    					{
	    						if (j != k)
	    							M[d][j] = false;
	    					}
	    					nextStep = 4;
	    					
	    					if (!refineM(M, instanceNodes, instanceEdges, 
	    				    	documentNodes, documentEdges))
	    						nextStep = 5;
	    				}
	    				break;
	    			case 4:
	    				//System.out.println("step 4");
	    				if (d < (noInsNodes - 1)) //if (d < noInsNodes)
	    				{
	    					nextStep = 6;
	    				}
	    				else
	    				{
	    					//System.out.println("possible isomorphism");
	    					// print out which node is linked to which??
	    					possibleIsomorphisms ++;
	    					
	    					if (true)
	    					//if (isIsomorphism(M, A, B))
	    					{
	    						isomorphisms++;
	    						retVal.add(copyBinaryMatrix(M));
	    					}
	    					nextStep = 5;
	    				}
	    				break;
	    			case 5:
	    				//System.out.println("step 5");
	    				
						for (int a = 0; a < noInsNodes; a ++)
							for (int b = 0; b < noDocNodes; b ++)
								M[a][b] = Md[d][a][b];
	    				
	    				valueFound = false;
	    				for (int j = k + 1; j < noDocNodes; j ++)
	    				{
	    					if (j > k) // should always be true
	    					{
	    						//System.out.println("j: " + j + " mdj: " + M[d][j] + " fj: " + F[j]);
	    						if (M[d][j] == true && F[j] == false)
	    						{
	    							valueFound = true;
	    						}
	    					}
	    				}
	    				if (!valueFound)
	    				{
	    					nextStep = 7;
	    				}
	    				else
	    				{
	    					
	    					nextStep = 3;
	    				}
	    				break;
	    			case 6:
	    				//System.out.println("step 6");
	    				H[d] = k;
	    				F[k] = true;
	    				d ++;
	    				nextStep = 2;
	    				break;
	    			case 7:
	    				//System.out.println("step 7");
	    				if (d == 0)//(d == 1) 
	    				{
	    					loop = false;
	    				}
	    				else
	    				{
	    					//F[k] = false;
	    					d --;
	    					for (int a = 0; a < noInsNodes; a ++)
	    						for (int b = 0; b < noDocNodes; b ++)
	    							M[a][b] = Md[d][a][b];
	    					k = H[d];
	    					F[k] = false;
	    					nextStep = 5;
	    				}
	    		}
	    	}
	    	System.out.println("Number of iterations: " + iteration);
//	    	System.out.println("possible isomorphisms: " + possibleIsomorphisms);
//	    	System.out.println("verified isomorphisms: " + isomorphisms);
	    	
	    	return retVal;
		}

	public static List<WrappingInstance> wrap(Document resultDocument, Element resultElement, 
    	DocumentGraph pageDg, Element wrapperElement)
    {
		boolean output = true;
		if (wrapperElement.getAttributes().getNamedItem("output") != null)
			output = Boolean.parseBoolean(wrapperElement.getAttributes().
    		getNamedItem("output").getNodeValue());
		boolean areaBased = true;
		if (wrapperElement.getAttributes().getNamedItem("area-based") != null)
    		areaBased = Boolean.parseBoolean(wrapperElement.getAttributes().
    		getNamedItem("area-based").getNodeValue());
		
		//swallow?
		boolean wholePage = false;
		if (wrapperElement.getAttributes().getNamedItem("whole-page") != null)
    		wholePage = Boolean.parseBoolean(wrapperElement.getAttributes().
    		getNamedItem("area-based").getNodeValue());
		
//    	5.04.09 not here...
//    	boolean rulingLines = Boolean.parseBoolean(wrapperElement.getAttributes().
//        		getNamedItem("process-ruling-lines").getNodeValue());
    	
    	//System.out.println("wrap one");
    	
    	//System.out.println("output: " + output);
    	//System.out.println("area-base: " + areaBased);
    	
    	NodeList listOfItems = wrapperElement.getChildNodes();
        
    	//System.out.println("listOfItems.size: " + listOfItems);
    	
    	// this ignores any non-node or edge elements
    	DocumentGraph wrapperDg = new DocumentGraph(listOfItems);
    	
    	// created page items, necessary for swallowing later
    	List<GenericSegment> pageItems = new ArrayList<GenericSegment>();
    	for (DocNode dn : wrapperDg.getNodes())
    		pageItems.add(dn.toGenericSegment());
        
    	//System.out.println("wrapperDG: " + wrapperDg);
    	
//    	wrapperDg.indexEdges(); // required for hashMap
//        pageDg.indexEdges();
        
        //System.out.println("wrap two");
        
        // before calling wrap, determine whether result is to be saved and which method of moving between
        // levels
        //SegmentList retVal = wrap(resultDocument, resultElement, 
        //	pageDg, wrapperDg, output);
        
        List<List<String>> returnFieldNames = new ArrayList<List<String>>();
        List<List<String>> returnExtractedData = new ArrayList<List<String>>();
        
//        System.out.println("wrap three");
        
        List<List<DocNode>> matchList = performExtraction(pageDg, wrapperDg, resultDocument, //resultElement);
            	returnFieldNames, returnExtractedData);
        List<WrappingInstance> result = toWrappingInstances(matchList);
        
//        System.out.println("wrap four");
        
        //Element subResultElement = resultElement;
        
        if (wholePage)
        {
        	List<List<WrappingInstance>> subResults = 
        		new ArrayList<List<WrappingInstance>>();
        	// first, process subwrappers on whole page
        	for (int s = 0; s < listOfItems.getLength(); s ++)
	        {
	        	Node itemNode = listOfItems.item(s);
	            if(itemNode.getNodeType() == Node.ELEMENT_NODE)
	            {
	            	if(itemNode.getNodeName().equals("pdf-wrapper"))
	            	{
	            		List<List<String>> dummyRFN = new ArrayList<List<String>>();
	            		List<List<String>> dummyRED = new ArrayList<List<String>>();
	            		
	            		NodeList listOfSubItems = itemNode.getChildNodes();
	                    
	                	// this ignores any non-node or edge elements
	                	DocumentGraph subWrapperDg = new DocumentGraph(listOfSubItems);
	                    
	                	//System.out.println("subWrapperDG: " + subWrapperDg);
	                	
//	                	subWrapperDg.indexEdges();
	            		
	                	
	            		List<WrappingInstance> subResult = 
	            			findInstances(pageDg, subWrapperDg, null, dummyRFN, dummyRED);
	            		
	            		for (int i = 0; i < subResult.size(); i ++)
	            		{
	            			WrappingInstance subInstance = subResult.get(i);
	            			
	            			//System.out.println("tt subInstance: " + subInstance.toExtendedString());
	            			
	            			if (areaBased)
	            			{
	            				List<GenericSegment> items = 
	            					ListUtils.findElementsIntersectingBBox(pageItems, subInstance);
//	            				System.out.println("subinstance items");
//	            				ListUtils.printList(items);
	            				WrappingInstance wi = new WrappingInstance();
	            				wi.setItems(items);
	            				wi.findBoundingBox();
	            				subResult.set(i, wi);
	            			}
	            		}
	            		//System.out.println("tt subResult:" + subResult);
	            		subResults.add(subResult);
	            	}
	            }
	        }
        	
        	int cellCount = 0;
        	
        	// for each found instance
	        for (int n = 0; n < result.size(); n ++)
	        {
	        	// if output==true, add to result
	        	WrappingInstance thisResult = result.get(n);
				if (areaBased)
				{
					List<GenericSegment> items = 
    					ListUtils.findElementsIntersectingBBox(pageItems, thisResult);
    				thisResult = new WrappingInstance();
    				thisResult.setItems(items);
    				thisResult.findBoundingBox();
				}
	        	
				System.out.println("******************");
				System.out.println("Top-level result: ");// + thisResult.toExtendedString());
				Collections.sort(thisResult.getItems(), new XComparator());
				
	        	Element subResultElement = resultElement;
	        	if (output)
	            {
	        		// go through each subresult and check if matches at least one
	        		// if so, then output
	        		// and output intersections as subresults
	        		boolean intersects = false; // deprecated
	        		
	        		for (int p = 0; p < subResults.size(); p ++) // these are the subwrappers
	    	        {
	        			List<GenericSegment> intersections = new ArrayList<GenericSegment>();
	        			List<WrappingInstance> subResult = subResults.get(p);
	        			
	        			for (int r = 0; r < subResult.size(); r ++) // these are the instances
	        			{
	        				WrappingInstance subInstance = subResult.get(r);
	        				
	        				// thisResult is an instance?
	        				
	        				List<GenericSegment> intersection =
		        				ListUtils.intersection(thisResult.getItems(), subInstance.getItems());
		        			intersections.addAll(intersection);
		        			
		        			if (intersection.size() > 0)
		        				intersects = true;
		        			
		        			/*
		        			if (intersects)
		        			{
			        			System.out.println("Subwrapper: " + p + "  intersecting segments:");
			        			Iterator isIter = intersection.iterator();
			        			while(isIter.hasNext())
			        			{
			        				TextSegment gs = (TextSegment)isIter.next();
			        				System.out.println(gs.getSegText());
			        			}
			        			System.out.println("======================");
		        			}
		        			*/
	        			}
	        			/*
	        			if (intersections.size() > 0)
	        			{
	        				System.out.println("Subwrapper: " + p + "  intersecting segments:");
		        			Iterator isIter = intersections.iterator();
		        			while(isIter.hasNext())
		        			{
		        				TextSegment gs = (TextSegment)isIter.next();
		        				System.out.println(gs.getSegText());
		        				cellCount ++;
		        			}
		        			System.out.println("======================");
	        			}
	        			*/
	    	        }
	            }
	        }	
	        System.out.println("cells on page: " + cellCount);
        }
        else
        {
	        // for each found instance
        	for (int n = 0; n < matchList.size(); n ++)
        	{
	        	// if output==true, add to result
	        	Element subResultElement = resultElement;
	        	if (output)
	            {
	            	subResultElement = resultDocument.createElement("wrapper-result");
	            	resultElement.appendChild(subResultElement);
	        	
	            	List<String> resultFieldNames = returnFieldNames.get(n);
	            	List<String> resultExtractedData = returnExtractedData.get(n);
		        	
		        	for (int p = 0; p < resultExtractedData.size(); p ++)
		        	{
		        		Element newFieldElement = resultDocument.
							createElement(resultFieldNames.get(p));
						subResultElement.appendChild(newFieldElement);
						newFieldElement.appendChild(resultDocument.
							createTextNode(resultExtractedData.get(p)));
		        	}
	            }
	        	
	        	// run for sub-graph
	        	WrappingInstance instance = result.get(n);
	        	List<DocNode> match = matchList.get(n);
				if (areaBased)
				{
					/*
					List<GenericSegment> items = 
    					ListUtils.getElementsIntersectingBBox(pageItems, instance);
    				instance = new WrappingInstance();
    				instance.setItems(items);
    				instance.findBoundingBox();
    				*/
					
					// swallow on node level
    				match.clear();
    				for (DocNode dn : pageDg.getNodes())
    				{
    					GenericSegment testSeg = dn.toGenericSegment();
    					if (SegmentUtils.intersects(testSeg, instance))
    						match.add(dn);
    				}
    				
				}
				
				//System.out.println("wrap five");
				
				DocumentGraph subPageDg = 
					pageDg.subGraph(match);
	        	
				for (int s = 0; s < listOfItems.getLength(); s ++)
		        {
		        	Node itemNode = listOfItems.item(s);
		            if(itemNode.getNodeType() == Node.ELEMENT_NODE)
		            {
		            	if(itemNode.getNodeName().equals("pdf-wrapper"))
		            	{
		            		// if wholepage... TODO (don't call wrap directly like that)
		            		// don't recurse here...
		            		// call next level & compare with all results
		            		if (!wholePage)
		            			wrap(resultDocument, subResultElement, subPageDg, 
		            				(Element)itemNode);
		            	}
		            }
		        }
	        }
        }
       // System.out.println("wrap six");
        return result;
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main(String[] args) throws Exception
    {
        boolean toConsole = false;
        int processType = -1;	// selected according to input wrapper
        int processSpacesCommandLine = 0;
        int rulingLinesCommandLine = 0;
        int currentArgumentIndex = 0;
        String password = "";
        String encoding = ProcessFile.DEFAULT_ENCODING;
        PDFObjectExtractor extractor = new PDFObjectExtractor();
        String inDocFile = null;
        String inWrapperFile = null;
        String outFile = null;
        boolean processSpaces = false;
        boolean rulingLines = true;
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        for( int i=0; i<args.length; i++ )
        {
        	if( args[i].equals( "-test"))
        	{
        		i++;
        		System.err.println("This function is not available in this version.");
        		//System.err.println("method graphMatchTest removed; see GraphMatcher3.java");
        		//graphMatchTest();
        		System.exit(0);
        	}
            if( args[i].equals( ProcessFile.PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( ProcessFile.ENCODING ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                encoding = args[i];
            }
            else if( args[i].equals( ProcessFile.START_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                startPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( ProcessFile.END_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                endPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( ProcessFile.CONSOLE ) )
            {
                toConsole = true;
            }
            else if( args[i].equals( "-rulinglines" ))
            {
            	rulingLinesCommandLine = 1;
            }
            else if( args[i].equals( "-norulinglines" ))
            {
            	rulingLinesCommandLine = -1;
            }
            else if( args[i].equals( "-blocks" ))
            {
                processType = PageProcessor.PP_BLOCK;
            }
            else if( args[i].equals( "-mergedlines" ))
            {
                processType = PageProcessor.PP_MERGED_LINES;
            }
            else if( args[i].equals( "-lines" ))
            {
            	processType = PageProcessor.PP_LINE;
            }
            else if( args[i].equals( "-spaces" ))
            {
            	processSpacesCommandLine = 1;
            }
            else if( args[i].equals( "-nospaces" ))
            {
            	processSpacesCommandLine = -1;
            }
            else
            {
                if( inDocFile == null )
                {
                    inDocFile = args[i];
                }
                else if( inWrapperFile == null )
                {
                    inWrapperFile = args[i];
                }
                else
                {
                    outFile = args[i];
                }
            }
        }

        if( inDocFile == null && inWrapperFile == null)
        {
            usage();
        }

        if( outFile == null && inDocFile.length() >4 )
        {
            outFile = inDocFile.substring( 0, inDocFile.length() -4 ) + ".txt";
        }
        
        long docStart = System.currentTimeMillis();
        
        // load the input files
        File inputDocFile = new File(inDocFile);
        byte[] inputDoc = ProcessFile.getBytesFromFile(inputDocFile);
        
        File inputWrapperFile = new File(inWrapperFile);
        //byte[] inputWrapper = ProcessFile.getBytesFromFile(inputWrapperFile);
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document wrapperDocument = docBuilder.parse(inputWrapperFile);
        
        Document resultDocument = null;
        
        //System.out.println("main one");
        
        // load the wrapper
        // normalize text representation
        wrapperDocument.getDocumentElement().normalize();
        //NodeList listOfWrappers = wrapperDocument.getElementsByTagName("pdf-wrapper");
        Element rootWrapper = (Element)wrapperDocument.getElementsByTagName("pdf-wrapper").item(0);
        
        // TODO: crashes if not present
        String granularity = rootWrapper.getAttributes().getNamedItem("granularity").getNodeValue();
        if (rootWrapper.getAttributes().getNamedItem("process-spaces") != null)
	        processSpaces = Boolean.parseBoolean(rootWrapper.getAttributes().
	        getNamedItem("process-spaces").getNodeValue());
        if (rootWrapper.getAttributes().getNamedItem("process-ruling-lines") != null)
        	rulingLines = Boolean.parseBoolean(rootWrapper.getAttributes(). //default false
        	getNamedItem("process-ruling-lines").getNodeValue());
        
        if (processType == -1)
        {
        	if (granularity.equals("raw-line"))
        		processType = PageProcessor.PP_LINE;
        	else if (granularity.equals("line"))
        		processType = PageProcessor.PP_MERGED_LINES;
        	else if (granularity.equals("block"))
        		processType = PageProcessor.PP_BLOCK;
        }
        // eise if overridden in commandline don't alter value here
        
        if (processSpacesCommandLine == 1)
        	processSpaces = true;
        else if (processSpacesCommandLine == -1)
        	processSpaces = false;
        // else if iSCL == 0 (no command line override) as document
        if (rulingLinesCommandLine == 1)
        	rulingLines = true;
        else if (rulingLinesCommandLine == -1)
        	rulingLines = false;
        // else if rLCL == 0 (no command line override) as document
        
        // wrapping...
        
        //NodeList listOfItems = listOfWrappers.item(0).getChildNodes();
//        NodeList listOfItems = rootWrapper.getChildNodes();
//        DocumentGraph wrapperDg = new DocumentGraph(listOfItems, model);
        
        
        // load the document
        // do the processing
        //endPage = startPage; // FOR NOW only process one page at a time
        
        List<AdjacencyGraph<GenericSegment>> theAdjGraphs = 
        	new ArrayList<AdjacencyGraph<GenericSegment>>();
        
        // set up page processor object
        PageProcessor pp = new PageProcessor();
        pp.setProcessType(processType);
        pp.setRulingLines(rulingLines);
        pp.setProcessSpaces(processSpaces);
        // no iterations should be automatically set to -1
        
        
        List<Page> theResult = ProcessFile.processPDF(inputDoc, pp, 
        	startPage, endPage, encoding, password, theAdjGraphs, false);
        
        // copied from ProcessFile.setUpXML
		try
        {
            DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDocBuilder = myFactory.newDocumentBuilder();
            DOMImplementation myDOMImpl = myDocBuilder.getDOMImplementation();
            //org.w3c.dom.Document 
            resultDocument = 
                myDOMImpl.createDocument("at.ac.tuwien.dbai.pdfwrap", "pdf-result", null);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
            // TODO: System.exit
            System.out.println("error");
            return;
        }
        
        Element resultElement = resultDocument.getDocumentElement();
        
        //System.out.println("main three");
//        GraphMatcher gm = new GraphMatcher(pageDg);
//        gm.setDocument(pageDg);
//        SegmentList result = gm.findInstances(wrapperDg, resultDocument, docElement);
        
        for (int p = 0; p < theResult.size(); p ++)
        {
        	System.out.println("Page: " + (p + 1));
	        long pageStart = System.currentTimeMillis();
        	
        	Page resultPage = theResult.get(p);
			//System.out.println("resultPage: " + resultPage.getItems());
	       // System.out.println("3.1");
	        DocumentGraph pageDg = new DocumentGraph(theAdjGraphs.get(p));
	        //System.out.println("3.2");
	        //System.out.println("pageDG: " + pageDg);
	        
	        Element pageResultElement = resultDocument.createElement("page");
	        pageResultElement.setAttribute("page-number",
	        	Integer.toString(p + 1));
	        	//Integer.toString(resultPage.getPageNo()));
        	resultElement.appendChild(pageResultElement);
        	//System.out.println("3.3");
	        List<WrappingInstance> result = wrap(resultDocument, pageResultElement,
	        	pageDg, wrapperDocument.getDocumentElement());
	        //System.out.println("3.4");
	        //System.out.println("result.size: " + result.size());
	        //System.out.println(result);
	        System.out.println("processing time for page: " + (System.currentTimeMillis() - pageStart));
	        
	        /*
	        for (WrappingInstance thisResult : result)
	        {
	        	System.out.println();
	        	System.out.println("New result:");
//	        	List<GenericSegment> theItems = thisResult.getItems();
//	        	Collections.sort(theItems, Collections.reverseOrder(new YComparator()));
	        	
	        	Collections.sort(thisResult.getItems(), 
	        		Collections.reverseOrder(new YComparator()));

	        	// TEST CODE
	        	for (Object o : thisResult.getItems())
	        	{
	        		if (o instanceof TextSegment)
	        			System.out.println(((TextSegment)o).getText());
	        	}
	        	//
	        }
	        */
	        
	        //System.out.println("result: " + result.toExtendedString());
        }
        System.out.println("processing time for document: " + (System.currentTimeMillis() - docStart));
        //System.out.println("main four");
        // now output the XML Document by serializing it to output
        Writer output = null;
        if( toConsole )
        {
            output = new OutputStreamWriter( System.out );
        }
        else
        {
            if( encoding != null )
            {
                output = new OutputStreamWriter(
                    new FileOutputStream( outFile ), encoding );
            }
            else
            {
                //use default encoding
                output = new OutputStreamWriter(
                    new FileOutputStream( outFile ) );
            }
            //System.out.println("using output file: " + outFile);
        }
        //System.out.println("resultDocument: " + resultDocument);
        ProcessFile.serializeXML(resultDocument, output);
        
        if( output != null )
        {
            output.close();
        }
        
    }
    
    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
//        System.err.println( "Usage: java at.ac.tuwien.dbai.pdfwrap.GraphMatcher [OPTIONS] <PDF file> [Text File]\n" +
    	System.err.println( "Usage: graphwrap [OPTIONS] <PDF file> <Wrapper file> [Output file]\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n" +
//            "  -xhtml                       output XHTML for wrapping (instead of XMillum-XML)\n" +
//            "  -table                       assume that whole page contains tabular data\n" +
//            "  -autotable                   attempt to detect location of tables on page\n" +
//            "  -bmw                         Processing for BMW reports (use with -xhtml)\n" +
//            "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n" +
            "  -spaces | -spaces            Override processing settings in wrapper\n" +
            "  -blocks | -lines | mergedlines\n" +
            "  -rulinglines | -norulinglines\n" +
            "  -console                     Send text to console instead of file\n" +
            "  -startPage <number>          The first page to start extraction (1 based)\n" +
            "  -endPage <number>            The last page to extract (inclusive)\n" +
            "  <PDF file>                   The PDF document to use\n" +
            "  <Wrapper file>               The XML wrapper file to use\n" +
            "  [Output File]                The output XML file name\n"
            );
        System.exit( 1 );
    }// add noborders to this printout
}
