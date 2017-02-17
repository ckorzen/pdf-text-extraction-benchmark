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
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of content stream operator for PDFObjectExtractor.
 * 
 * Adapted from PDFBox code
 * @author Huault : huault@free.fr
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */

public class ShowTextGlyph extends OperatorProcessor 
{
    /**
     * TJ Show text, allowing individual glyph positioning.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error processing this operator.
     */
    public void process(PDFOperator operator, List arguments) throws IOException 
    {
    	((PDFObjectExtractor)context).setNewTextFragment(true);
        COSArray array = (COSArray)arguments.get( 0 );
        float adjustment=0;
        for( int i=0; i<array.size(); i++ )
        {
            COSBase next = array.get( i );
            if( next instanceof COSNumber )
            {
                adjustment = ((COSNumber)next).floatValue();

                Matrix adjMatrix = new Matrix();
                adjustment=(-adjustment/1000)*context.getGraphicsState().getTextState().getFontSize() *
                    (context.getGraphicsState().getTextState().getHorizontalScalingPercent()/100);
                adjMatrix.setValue( 2, 0, adjustment );
                context.setTextMatrix( adjMatrix.multiply( context.getTextMatrix() ) );
            }
            else if( next instanceof COSString )
            {
//            	TEST 27.08.09
//            	((PDFObjectExtractor)context).setNewTextFragment(true);
            	
            	((PDFObjectExtractor)context).showString( ((COSString)next).getBytes() , i);
                //((PDFObjectExtractor)context).setNewTextFragment(false);
                // automatically set in addCharacter method (simplest way possible)
            }
            else
            {
                throw new IOException( "Unknown type in array for TJ operation:" + next );
            }
        }
    }

}