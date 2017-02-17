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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

import com.touchgraph.graphlayout.TGPanel;

/*
import edu.uci.ics.jung.graph.impl.SimpleUndirectedSparseVertex;
*/

/**
 * This represents an edge in the document graph
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class DocEdge extends com.touchgraph.graphlayout.Edge implements Cloneable
{
	// more generic fields
	public static int MATCH_ONE = 0;
	public static int MATCH_N_TIL_FIRST = 1;
	public static int MATCH_N_TIL_LAST = 2;
	public static int MATCH_N_ANY = 3;
	
	//protected boolean matchN = false;
	protected int multipleMatch = MATCH_ONE;
	protected boolean removeFromInstance = false;
	//protected boolean selected = false;
	
	public static Color DEFAULT_COLOR = Color.decode("#0000B0");
    public static Color MOUSE_OVER_COLOR = Color.pink;
	
//	in TG Edge class
    protected DocNode from;
  	protected DocNode to;
    
    protected float weight; //???
    protected int repetitions = 1;
    
    // fields specific to DocEdge
	public static int LENGTH_ANY = 10;
	public static int LENGTH_BLOCK = 11;
	public static int LENGTH_COLUMN = 12;
	public static int LENGTH_GREATER = 13;
	
	public static int REL_NONE = 0;
	public static int REL_LEFT_RIGHT = 1; // or top to bottom 
	public static int REL_RIGHT_LEFT = 2; // or bottom to top
	
	public static float ALIGN_TOLERANCE = 3.0f;
	
	protected float matchMinLength = 0.0f;
	protected float matchMaxLength = 0.0f;
	
	protected int logicalLength = LENGTH_GREATER;
	protected int matchLength = LENGTH_ANY;
	
//	replaced by calculated fields
//	protected boolean alignTopLeft = false;
//	protected boolean alignCentre = false;
//	protected boolean alignBottomRight = false;
	protected boolean mAlignTopLeft = false;
	protected boolean mAlignCentre = false;
	protected boolean mAlignBottomRight = false;
	
	protected boolean crossesRulingLine = false;
	protected boolean matchCrossesRulingLine = false;
	
	protected int readingOrder = REL_NONE;
	protected boolean matchReadingOrder = false;
	
	protected int superiorInferior = REL_NONE;
	protected boolean matchSuperiorInferior = false;
	
    protected String relation;
    
    // the relations (no more dependency on Pellet...)
    static final String ont = "http://www.dbai.tuwien.ac.at/staff/hassan/pdfwrap.owl#pdfwrap:";
    public static final String ADJ_LEFT = ont + "adjLeft";
    public static final String ADJ_RIGHT = ont + "adjRight";
    public static final String ADJ_ABOVE = ont + "adjAbove";
    public static final String ADJ_BELOW = ont + "adjBelow";
    

    /**
     * Constructor.
     *
     * @param todo: add parameters :)
     */

    public DocEdge(DocNode nodeFrom, DocNode nodeTo)
    {
    	super(nodeFrom, nodeTo, DEFAULT_LENGTH);
        this.weight = 1.0f;
        this.length = (int)Math.pow(weight, 0.85) + 10;
    }
    
    public DocEdge(DocNode nodeFrom, DocNode nodeTo, float weight)
    {
    	super(nodeFrom, nodeTo, DEFAULT_LENGTH);
        this.weight = weight;
        this.length = (int)Math.pow(weight, 0.85) + 10;
    }
    
    public DocEdge(DocNode nodeFrom, DocNode nodeTo, int repetitions)
    {
    	super(nodeFrom, nodeTo, DEFAULT_LENGTH);
        this.weight = 1.0f;
        this.repetitions = repetitions;
        this.length = (int)Math.pow(weight, 0.85) + 10;
    }
    
    /**
     * 
     * Constructor: As well as the AdjacencyEdge (with docmodel segments)
     * the correct corresponding created DocNodes must be passed
     * 
     * @param ae
     * @param nodeFrom
     * @param nodeTo
     * @param model
     */
    
    public DocEdge(AdjacencyEdge ae, DocNode nodeFrom, DocNode nodeTo)//, OntModel model)
    {
    	super(nodeFrom, nodeTo, DEFAULT_LENGTH);
        this.from = nodeFrom;
        this.to = nodeTo;
        this.weight = 1.0f; // currently we ignore the AG weights (they are not used/set...)
        this.length = (int)Math.pow(weight, 0.85) + 10;
        
        switch (ae.getDirection())
        {
        
            case AdjacencyEdge.REL_LEFT:
                relation = ADJ_LEFT;
                //weight = ae.getEdgeLength();
                weight = (int)Math.pow(ae.physicalLength(), 0.85) + 10;
                break;
        
            case AdjacencyEdge.REL_RIGHT:
                relation = ADJ_RIGHT;
                //weight = ae.getEdgeLength();
                weight = (int)Math.pow(ae.physicalLength(), 0.85) + 10;
                break;
        
            case AdjacencyEdge.REL_ABOVE:
                relation = ADJ_ABOVE;
                //weight = ae.getEdgeLength();
                weight = (int)Math.pow(ae.physicalLength(), 0.85) + 10;
                break;
        
            case AdjacencyEdge.REL_BELOW:
                relation = ADJ_BELOW;
                //weight = ae.getEdgeLength();
                weight = (int)Math.pow(ae.physicalLength(), 0.85) + 10;
                break;
        }
    }
    
    public DocEdge(Element nodeElement, List<DocNode> nodes)
    {
    	super(null, null);
    	NodeList nl; Element el; NodeList textNL; String val;
		
		//-------
        nl = nodeElement.getElementsByTagName("node-from");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        int nodeFromHC = Integer.parseInt(val);
        
		//-------
        nl = nodeElement.getElementsByTagName("node-to");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        int nodeToHC = Integer.parseInt(val);
        
        for (DocNode n : nodes)
        {
        	if (Integer.parseInt(n.getID()) == nodeFromHC)
        		from = n;
        	if (Integer.parseInt(n.getID()) == nodeToHC)
        		to = n;
        }
        
        //-------
        nl = nodeElement.getElementsByTagName("relation");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        relation = val;
//        relation = model.getOntProperty(relationString);
        
        //weight = ae.getEdgeLength();
        //weight = (int)Math.pow(getEdgeLength(), 0.85) + 10;
        
        //-------
        nl = nodeElement.getElementsByTagName("weight");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        weight = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("remove-from-instance");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        removeFromInstance = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("multiple-match");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        multipleMatch = Integer.parseInt(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("match-min-length");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        matchMinLength = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("match-max-length");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        matchMaxLength = Float.parseFloat(val);
        
        //-------
        nl = nodeElement.getElementsByTagName("logical-length");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        logicalLength = Integer.parseInt(val);

        //-------
        nl = nodeElement.getElementsByTagName("match-length");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        matchLength = Integer.parseInt(val);

        /*
        //-------
        nl = nodeElement.getElementsByTagName("align-top-left");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        alignTopLeft = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("align-centre");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        alignCentre = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("align-bottom-right");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        alignBottomRight = Boolean.parseBoolean(val);
		*/
        
        //-------
        nl = nodeElement.getElementsByTagName("match-align-top-left");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        mAlignTopLeft = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("match-align-centre");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        mAlignCentre = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("match-align-bottom-right");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        mAlignBottomRight = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("crosses-ruling-line");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        crossesRulingLine = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("match-crosses-ruling-line");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        matchCrossesRulingLine= Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("reading-order");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        readingOrder = Integer.parseInt(val);

        //-------
        nl = nodeElement.getElementsByTagName("match-reading-order");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        matchReadingOrder = Boolean.parseBoolean(val);

        //-------
        nl = nodeElement.getElementsByTagName("superior-inferior");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        superiorInferior = Integer.parseInt(val);

        //-------
        nl = nodeElement.getElementsByTagName("match-superior-inferior");
        el = (Element)nl.item(0);
        textNL = el.getChildNodes();
        val = ((Node)textNL.item(0)).getNodeValue().trim();
        matchSuperiorInferior = Boolean.parseBoolean(val);
    }
    
    public DocNode getFrom() {
		return from;
	}

	public void setFrom(DocNode from) {
		this.from = from;
	}

	public DocNode getTo() {
		return to;
	}

	public void setTo(DocNode to) {
		this.to = to;
	}

	public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    public String toString()
    {
        return ("AttributedEdge: " + relation + "\n NodeFrom: " + from + "\nNodeTo: " + to + "\n");
    }    
    
    public String getStringLabel()
    {
//    	return ("Direction: " + relation.getLocalName() + "     Length: " + weight);
    	return ("Direction: " + relation + "     Length: " + weight);
    }
    
    /*
    public String toString()
    {
        return ("Edge: \n  NodeFrom: " + nodeFrom + "\n  NodeTo: " + nodeTo + "\n");
    }
    */
    
    public String toSBText()
	{
    	return "ID: " + this.getID();
	}
	
	public int getLength()
	{
		if (removeFromInstance)
			return (int)Math.pow(weight, 0.55);
		else
			return (int)Math.pow(weight, 0.85) + 10;
	}
	
	// 2.11.08:
	// these attribute setting methods only to be called by
	// graphEltSet, as they do not set the segment properties!
	/*
	public boolean isMatchN()
	{
		return matchN;
	}

	public void setMatchN(boolean matchN)
	{
		this.matchN = matchN;
	}
	*/
	
	public static boolean isInverse(DocEdge e1, DocEdge e2)
	{
		if (e1.getRelation().equals(ADJ_LEFT))
			if (e2.getRelation().equals(ADJ_RIGHT)) return true;
		else if (e1.getRelation().equals(ADJ_RIGHT))
			if (e2.getRelation().equals(ADJ_LEFT)) return true;
		else if (e1.getRelation().equals(ADJ_ABOVE))
			if (e2.getRelation().equals(ADJ_BELOW)) return true;
		else if (e1.getRelation().equals(ADJ_BELOW))
			if (e2.getRelation().equals(ADJ_ABOVE)) return true;
			
		return false;
		
//		2011-06-26 ???
//		return (e2.getRelation() == Utils.oppositeDirection(e1.getRelation()));
		
	}
	
	public void addAsXMLEdge(Document resultDocument, Element parent)//, GenericSegment pageDim)
    {
        Element newNodeElement;
        newNodeElement = resultDocument.createElement("edge");
        
        setXMLEdgeAttributes(resultDocument, newNodeElement);
        
        parent.appendChild(newNodeElement);
    }
	
	public void setXMLEdgeAttributes(Document resultDocument, Element nodeElement)
	{
		Element newAttribElement;
		/*
		newAttribElement = resultDocument.createElement("hash-code");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Integer.toString(hashCode())));
    	*/
		
		newAttribElement = resultDocument.createElement("node-from");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
//    		(resultDocument.createTextNode(Integer.toString(from.getSegID())));
    		(resultDocument.createTextNode(from.getID()));
		
    	newAttribElement = resultDocument.createElement("node-to");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
//    		(resultDocument.createTextNode(Integer.toString(to.getSegID())));
    		(resultDocument.createTextNode(to.getID()));
    	
    	newAttribElement = resultDocument.createElement("weight");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Float.toString(weight)));
    	
    	newAttribElement = resultDocument.createElement("relation");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(relation.toString()));
    	
		newAttribElement = resultDocument.createElement("remove-from-instance");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Boolean.toString(removeFromInstance)));
    	
    	newAttribElement = resultDocument.createElement("multiple-match");
        nodeElement.appendChild(newAttribElement);
    	newAttribElement.appendChild
    		(resultDocument.createTextNode(Integer.toString(multipleMatch)));
    	
    	newAttribElement = resultDocument.createElement("match-min-length");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Float.toString(matchMinLength)));
	 	
	 	newAttribElement = resultDocument.createElement("match-max-length");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Float.toString(matchMaxLength)));
	 	
		newAttribElement = resultDocument.createElement("logical-length");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(logicalLength)));
	 	
	 	newAttribElement = resultDocument.createElement("match-length");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(matchLength)));
	 	
	 	/*
	 	newAttribElement = resultDocument.createElement("align-top-left");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(alignTopLeft)));
	 	
	 	newAttribElement = resultDocument.createElement("align-centre");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(alignCentre)));
	 	
	 	newAttribElement = resultDocument.createElement("align-bottom-right");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(alignBottomRight)));
	 	*/
	 	
	 	newAttribElement = resultDocument.createElement("match-align-top-left");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(mAlignTopLeft)));
	 	
	 	newAttribElement = resultDocument.createElement("match-align-centre");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(mAlignCentre)));
	 	
	 	newAttribElement = resultDocument.createElement("match-align-bottom-right");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(mAlignBottomRight)));
	 	
	 	newAttribElement = resultDocument.createElement("crosses-ruling-line");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(crossesRulingLine)));
	 	
	 	newAttribElement = resultDocument.createElement("match-crosses-ruling-line");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(matchCrossesRulingLine)));
	 	
	 	newAttribElement = resultDocument.createElement("reading-order");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(readingOrder)));
	 	
	 	newAttribElement = resultDocument.createElement("match-reading-order");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(matchReadingOrder)));
	 	
	 	newAttribElement = resultDocument.createElement("superior-inferior");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Integer.toString(superiorInferior)));
	 	
	 	newAttribElement = resultDocument.createElement("match-superior-inferior");
	    nodeElement.appendChild(newAttribElement);
	 	newAttribElement.appendChild
     		(resultDocument.createTextNode(Boolean.toString(matchSuperiorInferior)));
	}
	
	public void clearWrapperEdits()
	{
		matchMinLength = 0.0f;
		matchMaxLength = 0.0f;
		matchLength = LENGTH_ANY;

		mAlignTopLeft = false;
		mAlignCentre = false;
		mAlignBottomRight = false;
		
		matchCrossesRulingLine = false;
		matchReadingOrder = false;
		matchSuperiorInferior = false;
		
		//matchN = false;
		multipleMatch = MATCH_ONE;
		removeFromInstance = false;
	}
	
	/*
	public String toString()
	{
	    return ("Edge: \n  NodeFrom: " + nodeFrom + "\n  NodeTo: " + nodeTo + "\n");
	}
	*/
	
	public Color getColor()
	{
		// default grey -- should never be displayed
		Color c = new Color(127, 127, 127);//col;
		
		if (relation.equals(ADJ_RIGHT))
			c = new Color(159, 159, 255);
		else if (relation.equals(ADJ_BELOW))
			c = new Color(159, 255, 159);
		
		return c;
		//if (isMatchN()) done in paint method
		//	return c.darker().darker();
		//else return c;
	}

	// this method adapted from WikiEdge.java
	public void paint(Graphics g, TGPanel tgPanel) {
		
		// these two lines and hints uncommented 31.10.08
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHints(Utils.hints);
		
	    Color c = getColor();
	    
	    // do this, as we don't want the mouseOverColor to be altered
	    //if (tgPanel.getMouseOverN()==from || tgPanel.getMouseOverE()==this) 
	    //    c = MOUSE_OVER_COLOR; 
	    //else
	    //    c = col;        
	    
	  //  if (this == tgPanel.getSelect())
	    //if (isSelected())
	    //	c = c.darker(); // TODO: replace with outline drawing
	    
	    //if (isMatchN())
	    if (getMultipleMatch() != MATCH_ONE)
	    	c = c.darker().darker();
	
	    if (isRemoveFromInstance())
	    	c = c.brighter();
	    
	    if (tgPanel.getMouseOverN()==from || tgPanel.getMouseOverE()==this) 
	        c = MOUSE_OVER_COLOR; 
	    
		int x1=(int) from.drawx;
		int y1=(int) from.drawy;
		int x2=(int) to.drawx;
		int y2=(int) to.drawy;
		
		int arrowWidth = 3;
		int outlineWidth = 2;
		int backgroundWidth = arrowWidth + outlineWidth;
		if (intersects(tgPanel.getSize())) {
	    
			if (this == tgPanel.getSelect()) // if TG-Modified not in classpath above TGWikiBrowser, will not compile here!
			{
				int x3=x1;
	            int y3=y1;
	            
				double dist=Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	            if (dist>10) {
	                double adjustDistRatio = (dist-10)/dist;
	                x3=(int) (x1+(x2-x1)*adjustDistRatio);
	                y3=(int) (y1+(y2-y1)*adjustDistRatio);
	            }
	
	            x3=(int) ((x3*4+x1)/5.0);
	            y3=(int) ((y3*4+y1)/5.0);
	            
				g.setColor(c.darker());
				if (x2 > x1 && y2 > y1 || x1 > x2 && y1 > y2)
	            {
	            	//int[] a1 = {x1-5, x2-1, x2+1, x1+5};
	            	//int[] a2 = {y1+5, y2+1, y2-1, y1-5};
	            	////int[] a1 = {x1-6, x3-2, x2, x3+2, x1+6};
	            	////int[] a2 = {y1+6, y3+2, y2, y3-2, y1-6};
	            	int[] a1 = {x1-backgroundWidth, x2-1, x2+1, x1+backgroundWidth};
	            	int[] a2 = {y1+backgroundWidth, y2+1, y2-1, y1-backgroundWidth};
	            	g.fillPolygon(a1, a2, 4);
	            }
	            else
	            {
	            	//int[] a1 = {x1-6, x3-2, x2, x3+2, x1+6};
	            	//int[] a2 = {y1-6, y3-2, y2, y3+2, y1+6};
	            	int[] a1 = {x1-backgroundWidth, x2-1, x2+1, x1+backgroundWidth};
	            	int[] a2 = {y1-backgroundWidth, y2-1, y2+1, y1+backgroundWidth};
	            	g.fillPolygon(a1, a2, 4);
	            }
				//g.setColor(c);
			}
			
			g.setColor(c);
			
			int x3=x1; //x2;//x1;
	        int y3=y1; //y2;//y1;
	
	        
	        double dist=Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	        if (dist>10) {
	            double adjustDistRatio = (dist-10)/dist;
	            x3=(int) (x1+(x2-x1)*adjustDistRatio);
	            y3=(int) (y1+(y2-y1)*adjustDistRatio);
	        }
	
	        x3=(int) ((x3*4+x1)/5.0);
	        y3=(int) ((y3*4+y1)/5.0);
	        
	        // doesn't quite meet.... :(
	        // g.drawLine(x1, y1, x2, y2);
	        
	        if (x2 > x1 && y2 > y1 || x1 > x2 && y1 > y2)
	        {
	        	//int[] a1 = {x1-4, x3-0, x3+0, x1+4};
	        	//int[] a2 = {y1+4, y3+0, y3-0, y1-4};
	        	////int[] a1 = {x1-4, x3, x2, x3, x1+4};
	        	////int[] a2 = {y1+4, y3, y2, y3, y1-4};
	        	int[] a1 = {x1-arrowWidth, x2, x1+arrowWidth};
	        	int[] a2 = {y1+arrowWidth, y2, y1-arrowWidth};
	        	g.fillPolygon(a1, a2, 3);
	        }
	        else
	        {
	        	//int[] a1 = {x1-4, x3-0, x3+0, x1+4};
	        	//int[] a2 = {y1-4, y3-0, y3+0, y1+4};
	        	////int[] a1 = {x1-4, x3, x2, x3, x1+4};
	        	////int[] a2 = {y1-4, y3, x2, y3, y1+4};
	        	int[] a1 = {x1-arrowWidth, x2, x1+arrowWidth};
	        	int[] a2 = {y1-arrowWidth, y2, y1+arrowWidth};
	        	g.fillPolygon(a1, a2, 3);
	        }
	        
			//paintArrow(g, x1, y1, x2, y2, c);
	        
			/*
	        // x3 is 75% along the width of the arrow
	        // makes the arrows more pointed :)
	        int x3=x1;
	        int y3=y1;
	
	        double dist=Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	        if (dist>2) {
	            double adjustDistRatio = (dist-2)/dist;
	            x3=(int) (x1+(x2-x1)*adjustDistRatio);
	            y3=(int) (y1+(y2-y1)*adjustDistRatio);
	        }
	
	        x3=(int) ((x3*4+x1)/5.0);
	        y3=(int) ((y3*4+y1)/5.0);
	        
	        if (this == tgPanel.getSelect())
	        {
	        	// g.setLineWidth or whatever...
	            g.setColor(c.darker().darker());
	
	            g.drawLine(x3, y3, x2, y2);
	            if (x2 > x1 && y2 > y1 || x1 > x2 && y1 > y2)
	            {
	            	g.drawLine(x3-1, y3+1, x2-1, y2+1);
	            	g.drawLine(x3+1, y3-1, x2+1, y2-1);
	            	
	            	g.drawLine(x1-4, y1+4, x3, y3);
	            	g.drawLine(x1+4, y1-4, x3, y3);
	            	
	            	g.drawLine(x1-5, y1+5, x3-1, y3+1);
	            	g.drawLine(x1+5, y1-5, x3+1, y3-1);
	            }
	            else
	            {
	            	g.drawLine(x3-1, y3-1, x2-1, y2-1);
	            	g.drawLine(x3+1, y3+1, x2+1, y2+1);
	            	
	            	g.drawLine(x1-4, y1-4, x3, y3);
	            	g.drawLine(x1+4, y1+4, x3, y3);
	            	
	            	g.drawLine(x1-5, y1-5, x3-1, y3-1);
	            	g.drawLine(x1+5, y1+5, x3+1, y3+1);
	            }
	        }
	        */
		}
	}

	public static void paintLine(Graphics g, int x1, int y1, int x2, int y2, Color c)
	{
		
	}

	public boolean isAlignTopLeft() {
		if (relation.equals(ADJ_BELOW))
		{
			return (Utils.within(from.getSegX1(), to.getSegX1(), ALIGN_TOLERANCE));
		}
		else if (relation.equals(ADJ_RIGHT))
		{
			return (Utils.within(from.getSegY1(), to.getSegY1(), ALIGN_TOLERANCE));
		}
		//return alignTopLeft;
		return false;
	}
	
	public boolean isAlignCentre() {
		if (relation.equals(ADJ_BELOW))
		{
			return (Utils.within(from.segXmid(), to.segXmid(), ALIGN_TOLERANCE));
		}
		else if (relation.equals(ADJ_RIGHT))
		{
			return (Utils.within(from.segYmid(), to.segYmid(), ALIGN_TOLERANCE));
		}
		//return alignCentre;
		return false;
	}
	
	public boolean isAlignBottomRight() {
		if (relation.equals(ADJ_BELOW))
		{
			return (Utils.within(from.getSegX2(), to.getSegX2(), ALIGN_TOLERANCE));
		}
		else if (relation.equals(ADJ_RIGHT))
		{
			return (Utils.within(from.getSegY2(), to.getSegY2(), ALIGN_TOLERANCE));
		}
		//return alignBottomRight;
		return false;
	}

	/**
	 * @return Returns the weight.
	 */
	public float getWeight() {
	    return weight;
	}

	/**
	 * @param weight The weight to set.
	 */
	public void setWeight(float weight) {
	    this.weight = weight;
	}

	public int getRepetitions() {
		return repetitions;
	}

	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

	public int getMultipleMatch() {
		return multipleMatch;
	}

	public void setMultipleMatch(int multipleMatch) {
		this.multipleMatch = multipleMatch;
	}

	public boolean isRemoveFromInstance() {
		return removeFromInstance;
	}

	public void setRemoveFromInstance(boolean removeFromInstance) {
		this.removeFromInstance = removeFromInstance;
	}

	public float getMatchMinLength() {
		return matchMinLength;
	}

	public void setMatchMinLength(float matchMinLength) {
		this.matchMinLength = matchMinLength;
	}

	public float getMatchMaxLength() {
		return matchMaxLength;
	}

	public void setMatchMaxLength(float matchMaxLength) {
		this.matchMaxLength = matchMaxLength;
	}

	public int getLogicalLength() {
		return logicalLength;
	}

	public void setLogicalLength(int logicalLength) {
		this.logicalLength = logicalLength;
	}

	public int getMatchLength() {
		return matchLength;
	}

	public void setMatchLength(int matchLength) {
		this.matchLength = matchLength;
	}

	public boolean isMAlignTopLeft() {
		return mAlignTopLeft;
	}

	public void setMAlignTopLeft(boolean mAlignTopLeft) {
		this.mAlignTopLeft = mAlignTopLeft;
	}

	public boolean isMAlignCentre() {
		return mAlignCentre;
	}

	public void setMAlignCentre(boolean mAlignCentre) {
		this.mAlignCentre = mAlignCentre;
	}

	public boolean isMAlignBottomRight() {
		return mAlignBottomRight;
	}

	public void setMAlignBottomRight(boolean mAlignBottomRight) {
		this.mAlignBottomRight = mAlignBottomRight;
	}

	public boolean isCrossesRulingLine() {
		return crossesRulingLine;
	}

	public void setCrossesRulingLine(boolean crossesRulingLine) {
		this.crossesRulingLine = crossesRulingLine;
	}

	public boolean isMatchCrossesRulingLine() {
		return matchCrossesRulingLine;
	}

	public void setMatchCrossesRulingLine(boolean matchCrossesRulingLine) {
		this.matchCrossesRulingLine = matchCrossesRulingLine;
	}

	public int getReadingOrder() {
		return readingOrder;
	}

	public void setReadingOrder(int readingOrder) {
		this.readingOrder = readingOrder;
	}

	public boolean isMatchReadingOrder() {
		return matchReadingOrder;
	}

	public void setMatchReadingOrder(boolean matchReadingOrder) {
		this.matchReadingOrder = matchReadingOrder;
	}

	public int getSuperiorInferior() {
		return superiorInferior;
	}

	public void setSuperiorInferior(int superiorInferior) {
		this.superiorInferior = superiorInferior;
	}

	public boolean isMatchSuperiorInferior() {
		return matchSuperiorInferior;
	}

	public void setMatchSuperiorInferior(boolean matchSuperiorInferior) {
		this.matchSuperiorInferior = matchSuperiorInferior;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}
}
