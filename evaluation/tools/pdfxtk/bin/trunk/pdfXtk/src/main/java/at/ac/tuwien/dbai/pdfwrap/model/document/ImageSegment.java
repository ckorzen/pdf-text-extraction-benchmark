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

import java.util.UUID;

/**
 * ImageSegment document element; represents a (bitmap) image on the page
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class ImageSegment extends GenericSegment implements IXHTMLSegment {

    protected byte[] imageData;

    protected UUID uuid = UUID.randomUUID();


    public ImageSegment(float x1, float x2, float y1, float y2) {
        this(x1, x2, y1, y2, null);
    }


    /**
     * Constructor.
     *
     * @param x1 The x1 coordinate of the segments bounding box.
     * @param x2 The x2 coordinate of the segments bounding box.
     * @param y1 The y1 coordinate of the segments bounding box.
     * @param y2 The y2 coordinate of the segments bounding box.
     */
    public ImageSegment(
        float x1,
        float x2,
        float y1,
        float y2,
        byte[] imageData
        )
    {
		super(x1, x2, y1, y2);
        this.imageData = imageData;
    }


    public byte[] getImageData() {
        return imageData;
    }

    public UUID getUuid() {
        return uuid;
    }


    @Override
    public void addAsXHTML(Document resultDocument, Element parent) {
        Element newImgElement = resultDocument.createElement("img");
        newImgElement.setAttribute( "src", uuid.toString() + ".png");
        newImgElement.setAttribute( "width", "" + getWidth());
        newImgElement.setAttribute( "height", "" + getHeight());
    }
}
