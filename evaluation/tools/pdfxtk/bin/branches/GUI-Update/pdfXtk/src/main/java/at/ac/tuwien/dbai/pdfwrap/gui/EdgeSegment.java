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
package at.ac.tuwien.dbai.pdfwrap.gui;

import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;

/**
 * This represents an edge for the XMillum view
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class EdgeSegment extends GenericSegment {
	
        
    public EdgeSegment(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }
    
    public EdgeSegment()
    {
        super();
        
    }
    
    /* 2011-06-26 TODO: doesn't work -- returns all zeros?!?
     * currently using e.toDisplayableSegment instead
    public EdgeSegment(AdjacencyEdge<? extends GenericSegment> e)
    {
    	super();
    	float newX1, newX2, newY1, newY2, xo1, xo2, yo1, yo2;
    	//System.out.println("direction: " + direction);
    	switch(e.getDirection())
        {
            case AdjacencyEdge.REL_LEFT:
                newX1 = e.getNodeTo().getX2(); newX2 = e.getNodeFrom().getX1();

            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                yo1 = Utils.maximum(e.getNodeFrom().getY1(), e.getNodeTo().getY1());
                yo2 = Utils.minimum(e.getNodeFrom().getY2(), e.getNodeTo().getY2());
                newY1 = (yo1 + yo2) / 2;
            	newY2 = newY1;
                break;
            case AdjacencyEdge.REL_RIGHT:
            	newX2 = e.getNodeTo().getX1(); newX1 = e.getNodeFrom().getX2();
                
            	// newY1 = (nodeFrom.getYcen() + nodeTo.getYcen()) / 2;
                // find overlap coordinates yo1, yo2:
                yo1 = Utils.maximum(e.getNodeFrom().getY1(), e.getNodeTo().getY1());
                yo2 = Utils.minimum(e.getNodeFrom().getY2(), e.getNodeTo().getY2());
                newY1 = (yo1 + yo2) / 2;
            	newY2 = newY1;
                break;
            case AdjacencyEdge.REL_ABOVE:
                newY1 = e.getNodeFrom().getY2(); newY2 = e.getNodeTo().getY1();

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                xo1 = Utils.maximum(e.getNodeFrom().getX1(), e.getNodeTo().getX1());
                xo2 = Utils.minimum(e.getNodeFrom().getX2(), e.getNodeTo().getX2());
                newX1 = (xo1 + xo2) / 2;
                newX2 = newX1;
                break;
            case AdjacencyEdge.REL_BELOW:
            	newY2 = e.getNodeFrom().getY1(); newY1 = e.getNodeTo().getY2();

                // newX1 = (nodeFrom.getXcen() + nodeTo.getXcen()) / 2;
                // find overlap coordinates xo1, xo2:
                xo1 = Utils.maximum(e.getNodeFrom().getX1(), e.getNodeTo().getX1());
                xo2 = Utils.minimum(e.getNodeFrom().getX2(), e.getNodeTo().getX2());
                newX1 = (xo1 + xo2) / 2;
                newX2 = newX1;
                break;
            default:
            	//System.out.println("whoops!");
                newX1 = -1; newX2 = -1; newY1 = -1; newY2 = -1;
        }
//    	return new EdgeSegment(newX1, newX2, newY1, newY2);
    }
    */
}
