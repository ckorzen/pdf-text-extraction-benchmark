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
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Implementation of content stream operator for PDFObjectExtractor.
 * 
 * Adapted from PDFBox code
 * @author Ben Litchfield, ben@benlitchfield.com
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class AppendRectangleToPath extends OperatorProcessor
{


    /**
     * process : re : append rectangle to path.
     * @param operator The operator that is being executed.
     * @param arguments List
     */
    public void process(PDFOperator operator, List arguments) 
    {
        PDFObjectExtractor drawer = (PDFObjectExtractor)context;
        
        COSNumber x = (COSNumber)arguments.get( 0 );
        COSNumber y = (COSNumber)arguments.get( 1 );
        COSNumber w = (COSNumber)arguments.get( 2 );
        COSNumber h = (COSNumber)arguments.get( 3 );
       
        double finalX = x.floatValue();
        double finalY = y.floatValue();
        double finalW = w.floatValue();
        double finalH = h.floatValue();
                
        Point2D Ppos = drawer.TransformedPoint(finalX, finalY);
        Point2D Psize = drawer.ScaledPoint(finalW, finalH);
        
        finalY = Ppos.getY() - Psize.getY();
        
        
        if(finalY < 0)
        {
        	finalY = 0;
        }
	      
        
        //logger().info("Rectangle coords: " + Ppos.getX() + "," +  finalY + "," +  Psize.getX() + "," +  Psize.getY() );
        Rectangle2D rect = new Rectangle2D.Double(Ppos.getX(), finalY, Psize.getX(), Psize.getY());
        
        drawer.getLinePath().reset();
        
        //System.out.println( "Bounds before=" + drawer.getLinePath().getBounds() );
        drawer.getLinePath().append( rect, false );
        //graphics.drawRect((int)x.doubleValue(), (int)(pageSize.getHeight() - y.doubleValue()),
        //                  (int)w.doubleValue(),(int)h.doubleValue() );
        //System.out.println( "<re x=\"" + x.getValue() + "\" y=\"" + y.getValue() + "\" width=\"" +
        //                                 width.getValue() + "\" height=\"" + height.getValue() + "\" >" );
        
     	//drawer.simpleAddRect(x.floatValue(), y.floatValue(), w.floatValue(), h.floatValue());
        drawer.newPath();
        drawer.simpleAddRect((float)Ppos.getX(), (float)finalY, 
        	(float)Psize.getX(), (float)Psize.getY());
    }
}
