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

import java.util.List;

import at.ac.tuwien.dbai.pdfwrap.model.document.CompositeSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;

/**
 * 
 * This represents a wrapping instance as a list of segments
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 *
 */
public class WrappingInstance extends CompositeSegment<GenericSegment> 
{
	protected boolean subInstance = false;
	
	public WrappingInstance(
        float x1,
        float x2,
        float y1,
        float y2,
        String text,
        String fontName,
        float fontSize,
        int colspan,
        int rowspan
        )
    {
		super(x1, x2, y1, y2,text,fontName,fontSize);
    }

    public WrappingInstance(
        float x1,
        float x2,
        float y1,
        float y2,
        String text,
        String fontName,
        float fontSize
        )
    {
		super(x1, x2, y1, y2,text,fontName,fontSize);
    }
        
    public WrappingInstance(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }
        

/*  2011-01-24 TODO: decide how to create constructors
 *  or factory methods to "convert" e.g. TextBlock to TableCell, etc.  
    public TableCell(
		TextSegment seg,
		int colspan,
		int rowspan)
    {
//    	super(seg);
    	super();
    	this.colspan = colspan;
        this.rowspan = rowspan;
    }
*/
    
    public WrappingInstance()
    {
        super();
    }
    
    public WrappingInstance(List<DocNode> match)
    {
    	super();
    	for (DocNode n : match)
    		items.add(n.toGenericSegment());
    	findBoundingBox();
    }

	public boolean isSubInstance() {
		return subInstance;
	}

	public void setSubInstance(boolean subInstance) {
		this.subInstance = subInstance;
	}
	
	public String tagName()
	{
		if (subInstance)
			return "sub-instance";
		else return super.tagName();
	}
}
