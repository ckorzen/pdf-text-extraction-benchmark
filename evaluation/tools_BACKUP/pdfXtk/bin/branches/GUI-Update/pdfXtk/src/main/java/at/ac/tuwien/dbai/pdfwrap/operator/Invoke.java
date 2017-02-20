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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of content stream operator for PDFObjectExtractor.
 * 
 * Adapted from PDFBox code
 * @author Ben Litchfield, ben@benlitchfield.com
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class Invoke extends OperatorProcessor {
    private static final Log log = LogFactory.getLog(  Invoke.class );


    /**
     * process : re : append rectangle to path.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error invoking the sub object.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        PDFObjectExtractor drawer = (PDFObjectExtractor)context;
        PDPage page = drawer.getPage();
        Dimension pageSize = drawer.getPageSize();
        Graphics2D graphics = drawer.getGraphics();
        COSName objectName = (COSName)arguments.get( 0 );
        Map xobjects = drawer.getResources().getXObjects();
        PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );


        if( xobject instanceof PDXObjectImage )
        {
/////        	System.out.println("JUHU PDXObjectImage");
            PDXObjectImage image = (PDXObjectImage)xobject;
            try
            {
//            	System.out.println("JUHU PDXObjectImage with subtype: " + image);
//            	System.out.println("with colour space: " + image.getColorSpace());
//            	System.out.println("and bitspercomponent: " + image.getBitsPerComponent());
//            	System.out.println("and with suffix: " + image.getSuffix());
            	// this stuff moved below so that simpleDrawImage is called, even if
            	// image creation/drawing fails
            	
            	// 7.04.09 added try/catch to allow for mis-reading of images
            	// in Der Presse
            	//BufferedImage awtImage = image.getRGBImage();
            	
            	//BufferedImage awtImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
                //try
                //{
            	/*
            	BufferedImage
                	awtImage = image.getRGBImage();
                //}
                //catch (Exception e)
                //{
                //	e.printStackTrace();
                //}
                
                if (awtImage == null) {
                    return;//TODO PKOCH
                }
                */
                Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
                
                //int width = awtImage.getWidth();
                int width = image.getWidth();
                //int height = awtImage.getHeight();
                int height = image.getHeight();
                
                ////drawer.simpleDrawImage(width, height);
                
                Matrix scalingParams = ctm.extractScaling();
                Matrix scalingMatrix = Matrix.getScaleInstance(1f/width,1f/height);
                scalingParams = scalingParams.multiply( scalingMatrix );
                
                Matrix translationParams = ctm.extractTranslating();
                Matrix translationMatrix = null;
                translationParams.setValue(2,1, -translationParams.getValue( 2,1 ));
                translationMatrix = Matrix.getTranslatingInstance(0, (float)pageSize.getHeight()-height*scalingParams.getYScale() );
                translationParams = translationParams.multiply( translationMatrix );

                AffineTransform at = new AffineTransform( 
                        scalingParams.getValue( 0,0), 0,
                        0, scalingParams.getValue( 1, 1),
                        translationParams.getValue(2,0), translationParams.getValue( 2,1 )
                    );

                // moved below graphics.drawImage( awtImage, at, null );
                //drawer.simpleDrawImage()...
                
/////                System.out.println("adding image...");
                
                // 14.04.09
                // this was a problem with google019.pdf
                // (Günter Geiger)
                double scaleX = at.getScaleX();
                double scaleY = at.getScaleY();
                
                if (scaleX == 0.0) scaleX = 1.0;
                if (scaleY == 0.0) scaleY = 1.0;
                
                /*
                drawer.simpleDrawImage((float)at.getTranslateX(), 
                	(float)(at.getTranslateX()+(width*at.getScaleX())),
                	(float)at.getTranslateY(), (float)(at.getTranslateY()+(height*at.getScaleY())));
                */
                
                drawer.simpleDrawImage((float)at.getTranslateX(), 
                    	(float)(at.getTranslateX()+(width*scaleX)),
                    	(float)at.getTranslateY(), (float)(at.getTranslateY()+(height*scaleY)), image );
                
                BufferedImage awtImage = image.getRGBImage();
                graphics.drawImage( awtImage, at, null );
            }
            catch( Exception e ) {
                log.error( "failed to invoke image: " + operator, e  );
            }

        }
        else if( xobject instanceof PDXObjectForm) {
            PDXObjectForm form = (PDXObjectForm)xobject;
            COSStream invoke = (COSStream)form.getCOSObject();
            PDResources pdResources = form.getResources();
            if(pdResources == null)
            {
                pdResources = page.findResources();
            }

            getContext().processSubStream( page, pdResources, invoke );
        }
        else {
            throw new RuntimeException( "unsupported type: " + xobject );

        }
    }
}
