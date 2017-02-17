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
package at.ac.tuwien.dbai.pdfwrap.operator;

import at.ac.tuwien.dbai.pdfwrap.pdfread.PDFObjectExtractor;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDInlinedImage;
import org.apache.pdfbox.util.ImageParameters;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Implementation of content stream operator for PDFObjectExtractor.
 * 
 * Adapted from PDFBox code
 * @author Ben Litchfield, ben@benlitchfield.com
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class BeginInlineImage extends OperatorProcessor
{


    /**
     * process : BI : begin inline image.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error displaying the inline image.
     */
    public void process(PDFOperator operator, List arguments)  throws IOException
    {
        PDFObjectExtractor drawer = (PDFObjectExtractor)context;
        Graphics2D graphics = drawer.getGraphics();
        //begin inline image object
        ImageParameters params = operator.getImageParameters();
        PDInlinedImage image = new PDInlinedImage();
        image.setImageParameters( params );
        image.setImageData( operator.getImageData() );

        // 7.04.09 added try/catch to allow for mis-reading of images
    	// in Kurier
     // BufferedImage awtImage = image.createImage();
        BufferedImage awtImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        try
        {
        	awtImage = image.createImage();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        
        Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
        
        int width = awtImage.getWidth();
        int height = awtImage.getHeight();

        
        AffineTransform at = new AffineTransform(
            ctm.getValue(0,0)/width,
            ctm.getValue(0,1),
            ctm.getValue(1,0),
            ctm.getValue(1,1)/height,
            ctm.getValue(2,0),
            ctm.getValue(2,1)
        );
        //at.setToRotation((double)page.getRotation());

        
        // The transformation should be done 
        // 1 - Translation
        // 2 - Rotation
        // 3 - Scale or Skew
        //AffineTransform at = new AffineTransform();

        // Translation
        //at = new AffineTransform();
        //at.setToTranslation((double)ctm.getValue(0,0),
        //                    (double)ctm.getValue(0,1));

        // Rotation
        //AffineTransform toAdd = new AffineTransform();
        //toAdd.setToRotation(1.5705);
        //toAdd.setToRotation(ctm.getValue(2,0)*(Math.PI/180));
        //at.concatenate(toAdd);

        // Scale / Skew?
        //toAdd.setToScale(width, height); 
        //at.concatenate(toAdd);
        //at.setToScale( width, height );
        graphics.drawImage( awtImage, at, null );
        //graphics.drawImage( awtImage,0,0, width,height,null);
        ////drawer.simpleDrawImage(width, height);
    }
}
