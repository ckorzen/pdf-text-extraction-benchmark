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
package at.ac.tuwien.dbai.pdfwrap.model.document;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;


/**
 * Page object
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class Page extends CompositeSegment<GenericSegment> implements IXHTMLSegment
{   
    int pageNo = 0;
    int rotation = 0;
    int lastOpIndex = -1;
//  removed 2011-01-24
//  DocumentGraph docGraph;
    
    public Page()
    {
        super();
    }
    
    public Page(List<GenericSegment> items)
    {
        super(items);
    }
    
    public Page(
        float x1,
        float x2,
        float y1,
        float y2
        )
    {
		super(x1, x2, y1, y2);
    }
    
    public Page(
        float x1,
        float x2,
        float y1,
        float y2,
		List<GenericSegment> items
        )
    {
		super(x1, x2, y1, y2, items);
    }
    
    public void addAsXHTML(Document resultDocument, Element parent)
    {
        Element newPageElement = resultDocument.createElement("h2");
        
        newPageElement.appendChild
            (resultDocument.createTextNode("Page " + pageNo));
        
        parent.appendChild(newPageElement);
        
        for(GenericSegment thisItem : items)
        {
            // eventually, this line should take care of everything here...
            // thisItem.addAsXHTML(resultDocument, parent);
            
            // but for now, enable only for those elements that have
            // it defined...

            if (thisItem instanceof IXHTMLSegment)// && 
//            	thisItem.getClass() != TableColumn.class &&
//            	thisItem.getClass() != TableRow.class)
            {
            	//System.out.println("adding class: " + thisItem.getClass());
            	((IXHTMLSegment)thisItem).addAsXHTML(resultDocument, parent);
            }
        }
    }
    
    public void addAsXmillum(Document resultDocument, Element parent, 
    	GenericSegment pageDim, float resolution)
    {
    	//System.out.println("adding as XML with pageDim: " + pageDim);
        Iterator itemIter = items.iterator();
        while(itemIter.hasNext())
        {
            GenericSegment thisItem = (GenericSegment)itemIter.next();
        	thisItem.addAsXmillum(resultDocument, parent, pageDim, resolution);
        }
    }
    
    @Override
    public List<AttributeTuple> getAttributes() {

    	List<AttributeTuple> attList = super.getAttributes();
    	
    	attList.add(new AttributeTuple("pageNo", pageNo));
    	
    	return attList;
    }
    
    /**
     * @return Returns the pageNo.
     */
    public int getPageNo() {
        return pageNo;
    }
    

    /**
     * @param pageNo The pageNo to set.
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getLastOpIndex() {
		return lastOpIndex;
	}

	public void setLastOpIndex(int lastOpIndex) {
		this.lastOpIndex = lastOpIndex;
	}
}
