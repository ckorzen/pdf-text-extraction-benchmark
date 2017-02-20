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

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Implementation of content stream operator for PDFObjectExtractor.
 * 
 * Adapted from PDFBox code
 * @author Ben Litchfield, ben@benlitchfield.com
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class MoveTo extends OperatorProcessor
{


    /**
     * process : m : Begin new subpath.
     * @param operator The operator that is being executed.
     * @param arguments List
     */
    public void process(PDFOperator operator, List arguments) 
    {
        try{
            PDFObjectExtractor drawer = (PDFObjectExtractor)context;
            
            COSNumber x = (COSNumber)arguments.get( 0 );
            COSNumber y = (COSNumber)arguments.get( 1 );
            
            drawer.getLineSubPaths().add( drawer.getLinePath() );
            GeneralPath newPath = new GeneralPath();
            Point2D Ppos = drawer.TransformedPoint(x.doubleValue(), y.doubleValue());
            
            //newPath.moveTo( x.floatValue(), (float)drawer.fixY( x.doubleValue(), y.doubleValue()) );
            //logger().info("Ready to move to " + Ppos.getX() + ", " + Ppos.getY());
            
            newPath.moveTo((float)Ppos.getX(), (float)Ppos.getY());
            
            drawer.setLinePath( newPath );
            //drawer.setNewPath(true);
            drawer.newPath();
            
            ////drawer.simpleMoveTo((float)Ppos.getX(), (float)Ppos.getY());
            drawer.simpleMoveTo(x.floatValue(), y.floatValue());
        }catch (Exception E){
            //logger().warning( E.toString() + "/n at/n" + FullStackTrace(E));
        	E.printStackTrace();
        }
        
////    extractor.setCurrentX(x.floatValue());
////    extractor.setCurrentY(y.floatValue());
    }
}
