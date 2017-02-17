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
package at.ac.tuwien.dbai.pdfwrap.pdfread;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.apache.pdfbox.pdmodel.encryption.PDStandardEncryption;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;

import at.ac.tuwien.dbai.pdfwrap.model.document.CharSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.CompositeSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.ImageSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.LineSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.OpTuple;
import at.ac.tuwien.dbai.pdfwrap.model.document.RectSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextFragment;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

/**
 * This extracts the low-level information in the PDF
 *
 * Based on PDF code
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class PDFObjectExtractor extends PDFStreamEngine
{
	// 13.04.09 2332
	// changes: copied showString, addCharacter from PDFObjectExtractorOld.java
	// these methods did not exist before
	// disabled adding of text block items within showCharacter
	// refactoring would be a good idea...
	
	// copied from PDFStreamEngine as not visible to this method
    public static final byte[] SPACE_BYTES = { (byte)32 };
    
	protected Graphics2D graphics;
    protected Dimension pageSize;
    protected PDPage page;

    protected List lineSubPaths = new ArrayList();
    protected GeneralPath linePath = new GeneralPath();
    
    // 100409: necessary?
    protected Color strokingColor = Color.BLACK;
    protected Color nonStrokingColor = Color.BLACK;
    protected Color currentColor = Color.black;
    
    protected List<ImageSegment> imageList = new ArrayList<ImageSegment>();
    protected List<TextFragment> fragmentList = new ArrayList<TextFragment>();
    protected List<LineSegment> lineList = new ArrayList<LineSegment>();
    protected List<RectSegment> rectList = new ArrayList<RectSegment>();
    // 22.01.07
    protected List<CharSegment> charList = new ArrayList<CharSegment>();
    
    // current subpath
    protected List<LineSegment> currentLines = new ArrayList<LineSegment>();
    protected List<RectSegment> currentRects = new ArrayList<RectSegment>();
    // current path
    protected List<LineSegment> linesToAdd = new ArrayList<LineSegment>();
    protected List<RectSegment> rectsToAdd = new ArrayList<RectSegment>();
    // following relate to subpaths!
    // subpaths containing curves are thrown away
    protected boolean pathContainsCurve = false;
    protected float pathBeginX, pathBeginY;
    protected boolean pathBeginSet = false, pathClosed = false;
    
    // new from 23 Apr. 09
    protected GenericSegment clipBounds = null;
    protected Stack clipBoundsStack = new Stack();
    
    protected float currentX = -1;
    protected float currentY = -1;
    protected boolean newPath = false;
    protected CompositeSegment lastStringFragment = null;
    
    // 24.04.09  Tj operator
    protected boolean newTextFragment = false;
    protected boolean mergeAcrossTextInstructions = false;
    protected int opIndex = -1;
    
    protected int currentPageNo = 0;
    protected int startPage = 1;
    protected int endPage = Integer.MAX_VALUE;
    protected PDOutlineItem startBookmark = null;
    protected int startBookmarkPageNumber = -1;
    protected PDOutlineItem endBookmark = null;
    protected int endBookmarkPageNumber = -1;
    protected PDDocument document;
    protected boolean suppressDuplicateOverlappingText = true;
    protected boolean shouldSeparateByBeads = true;
    protected boolean sortByPosition = false;
    
    protected List pageArticles = null;
    
    /**
     * The charactersByArticle is used to extract text by article divisions.  For example
     * a PDF that has two columns like a newspaper, we want to extract the first column and
     * then the second column.  In this example the PDF would have 2 beads(or articles), one for
     * each column.  The size of the charactersByArticle would be 5, because not all text on the 
     * screen will fall into one of the articles.  The five divisions are shown below
     * 
     * Text before first article
     * first article text
     * text between first article and second article
     * second article text
     * text after second article
     * 
     * Most PDFs won't have any beads, so charactersByArticle will contain a single entry.
     */
    protected Vector charactersByArticle = new Vector();
    
    protected Map characterListMapping = new HashMap();
    
    protected String lineSeparator = System.getProperty("line.separator");
    protected String pageSeparator = System.getProperty("line.separator");
    protected String wordSeparator = " ";
    
    /**
     * Default constructor, loads properties from file.
     * 
     * @throws IOException If there is an error loading properties from the file.
     */
    public PDFObjectExtractor() throws IOException
    {
        // super( ResourceLoader.loadProperties( "Resources/PDFObjectExtractor.properties", true ) );
    	// 100904 don't know what this parameter means, but was set to false before...
    	
//    	super( ResourceLoader.loadProperties( Utils.getRootDir() + "/Resources/PDFObjectExtractor.properties", false ) );
    	super( ResourceLoader.loadProperties( "PDFObjectExtractor.properties", false ) );
    }

    /**
     * This will draw the page to the requested context.
     *
     * @param g The graphics context to draw onto.
     * @param p The page to draw.
     * @param pageDimension The size of the page to draw.
     *
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage( Graphics g, PDPage p, Dimension pageDimension ) throws IOException
    {
        graphics = (Graphics2D)g;
        page = p;
        pageSize = pageDimension;
        
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        PDResources resources = page.findResources();
        processStream( page, resources, page.getContents().getStream() );
        List annotations = page.getAnnotations();
        for( int i=0; i<annotations.size(); i++ )
        {
            PDAnnotation annot = (PDAnnotation)annotations.get( i );
            PDRectangle rect = annot.getRectangle();
            String appearanceName = annot.getAppearanceStream();
            PDAppearanceDictionary appearDictionary = annot.getAppearance();
            if( appearDictionary != null )
            {
                if( appearanceName == null )
                {
                    appearanceName = "default";
                }
                Map appearanceMap = appearDictionary.getNormalAppearance();
                PDAppearanceStream appearance = 
                    (PDAppearanceStream)appearanceMap.get( appearanceName );
                if( appearance != null )
                {
                    g.translate( (int)rect.getLowerLeftX(), (int)-rect.getLowerLeftY()  );
                    //g.translate( 20, -20 );
                    processSubStream( page, appearance.getResources(), appearance.getStream() );
                    g.translate( (int)-rect.getLowerLeftX(), (int)+rect.getLowerLeftY()  );
                }
            }
        }
        // Transformations should be done in order
        // 1 - Translate
        // 2 - Rotate
        // 3 - Scale
        // Refer to PDFReference p176 (or 188 in xpdf)
        /*AffineTransform transform = graphics.getTransform();        
        transform.setToTranslation( 0, page.findMediaBox().getHeight()/2 );
        transform.setToRotation((double)p.getRotation());
        transform.setTransform( 1, 0, 0, 1, 0, 0 );        
        transform.setToScale( 1, 1 );
        
        AffineTransform rotation = graphics.getTransform();
        rotation.rotate( (page.findRotation() * Math.PI) / 180d );
        graphics.setTransform( rotation );*/
        
    }

    @Override
    protected void processOperator(PDFOperator arg0, List arg1)
    		throws IOException {
    	opIndex ++;
    	// TODO Auto-generated method stub
    	super.processOperator(arg0, arg1);
    }
    
    /**
     * You should override this method if you want to perform an action when a
     * string is being shown.
     *
     * @param string The string to display.
     *
     * @throws IOException If there is an error showing the string
     */
    public void showString( byte[] string , int argIndex) throws IOException
    {
    	OpTuple sourceOp = new OpTuple(opIndex, argIndex);
    	
    	//super.showString(string);
    	
    	ArrayList charactersToAdd = new ArrayList();
    	
    	float[] individualWidths = new float[2048];
        float spaceWidth = 0;
        float spacing = 0;
        StringBuffer stringResult = new StringBuffer(string.length);
        
        float characterHorizontalDisplacement = 0;
        float characterVerticalDisplacement = 0;
        float spaceDisplacement = 0;
        float fontSize = getGraphicsState().getTextState().getFontSize();
        float horizontalScaling = getGraphicsState().getTextState().getHorizontalScalingPercent()/100f;
        float verticalScaling = horizontalScaling;//not sure if this is right but what else to do???
        float rise = getGraphicsState().getTextState().getRise();
        final float wordSpacing = getGraphicsState().getTextState().getWordSpacing();
        final float characterSpacing = getGraphicsState().getTextState().getCharacterSpacing();
        float wordSpacingDisplacement = 0;
        //We won't know the actual number of characters until
        //we process the byte data(could be two bytes each) but
        //it won't ever be more than string.length*2(there are some cases
        //were a single byte will result in two output characters "fi"
        
        
        PDFont font = getGraphicsState().getTextState().getFont();
        
        //This will typically be 1000 but in the case of a type3 font
        //this might be a different number
        float glyphSpaceToTextSpaceFactor = 1f/font.getFontMatrix().getValue( 0, 0 );
        float averageWidth = font.getAverageFontWidth();

        Matrix initialMatrix = new Matrix();
        initialMatrix.setValue(0,0,1);
        initialMatrix.setValue(0,1,0);
        initialMatrix.setValue(0,2,0);
        initialMatrix.setValue(1,0,0);
        initialMatrix.setValue(1,1,1);
        initialMatrix.setValue(1,2,0);
        initialMatrix.setValue(2,0,0);
        initialMatrix.setValue(2,1,rise);
        initialMatrix.setValue(2,2,1);


        //this
        int codeLength = 1;
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        
        //lets see what the space displacement should be
        spaceDisplacement = (font.getFontWidth( SPACE_BYTES, 0, 1 )/glyphSpaceToTextSpaceFactor);
        if( spaceDisplacement == 0 )
        {
            spaceDisplacement = (averageWidth/glyphSpaceToTextSpaceFactor);
            //The average space width appears to be higher than necessary
            //so lets make it a little bit smaller.
            spaceDisplacement *= .80f;
        }
        int pageRotation = page.findRotation();
        Matrix trm = initialMatrix.multiply( getTextMatrix() ).multiply( ctm );
        float x = trm.getValue(2,0);
        float y = trm.getValue(2,1);
        if( pageRotation == 0 )
        {
            trm.setValue( 2,1, -y + page.findMediaBox().getHeight() );
        }
        else if( pageRotation == 90 || pageRotation == -270 )
        {
        	trm.setValue( 2,0, y );
          trm.setValue( 2,1, x ); //2013-03-12 fixed shifted coordinates in e.g. eu-014.pdf (table datset)
//        	                        TODO: update GUI code to correct positioning of raster graphic
//            trm.setValue( 2,1, x + page.findMediaBox().getHeight() - page.findMediaBox().getWidth() );
        }
        else if( pageRotation == 270 || pageRotation == -90 )
        {
            trm.setValue( 2,0, -y  + page.findMediaBox().getHeight() );
            trm.setValue( 2,1, x );
        }
        float xScale = trm.getXScale();
        float yScale = trm.getYScale(); 
        float xPos = trm.getXPosition();
        float yPos = trm.getYPosition();
        spaceWidth = spaceDisplacement * xScale * fontSize;
        wordSpacingDisplacement = wordSpacing*xScale * fontSize;
        float totalStringWidth = 0;
        
        // addition 15.04.09
        // to go back to older version, simply make x and yTrans 0
        // this handles (more or less) edocsacs.pdf
        float xPosBefore2 = getTextMatrix().getXPosition();
        float yPosBefore2 = getTextMatrix().getYPosition();
        float xTrans = 0, yTrans = 0;
        
        if (pageRotation == 0)
        {
	        xTrans = xPos - xPosBefore2;
	        //System.out.println("xPos: " + xPos + " tmxp: " + getTextMatrix().getXPosition() + " xTrans: " + xTrans);
	        yTrans = yPos - yPosBefore2;
	        //System.out.println("yPos: " + yPos + " tmyp: " + getTextMatrix().getYPosition() + " yTrans: " + yTrans);
        }
        else if (pageRotation == 90 || pageRotation == -270)
        {
        	xTrans = xPos - yPosBefore2;
/////        	System.out.println("xPos: " + xPos + " tmyp: " + getTextMatrix().getYPosition() + " xTrans: " + xTrans);
	        yTrans = yPos - xPosBefore2;
/////	        System.out.println("yPos: " + yPos + " tmxp: " + getTextMatrix().getXPosition() + " yTrans: " + yTrans);
        }
        else if (pageRotation == 270 || pageRotation == -90)
        {
        	yTrans = xPos - yPosBefore2;
/////        	System.out.println("xPos: " + xPos + " tmyp: " + getTextMatrix().getYPosition() + " xTrans: " + xTrans);
	        xTrans = yPos - xPosBefore2;
/////	        System.out.println("yPos: " + yPos + " tmxp: " + getTextMatrix().getXPosition() + " yTrans: " + yTrans);
        }
        //xTrans = 0;
        //yTrans = 0;
        // end addition
        
        for( int i=0; i<string.length; i+=codeLength )
        {
            
            codeLength = 1;

            String c = font.encode( string, i, codeLength );
            if( c == null && i+1<string.length)
            {
                //maybe a multibyte encoding
                codeLength++;
                c = font.encode( string, i, codeLength );
            }

            //todo, handle horizontal displacement
            
//            System.out.println("font: " + font + " font width: " + font.getFontWidth( string, i, codeLength ));
            
            characterHorizontalDisplacement = (font.getFontWidth( string, i, codeLength )/glyphSpaceToTextSpaceFactor); 
            characterVerticalDisplacement = 
                Math.max( 
                    characterVerticalDisplacement, 
                    font.getFontHeight( string, i, codeLength)/glyphSpaceToTextSpaceFactor);

            
            // PDF Spec - 5.5.2 Word Spacing
            //
            // Word spacing works the same was as character spacing, but applies
            // only to the space character, code 32.
            //
            // Note: Word spacing is applied to every occurrence of the single-byte
            // character code 32 in a string.  This can occur when using a simple
            // font or a composite font that defines code 32 as a single-byte code.
            // It does not apply to occurrences of the byte value 32 in multiple-byte
            // codes.
            //
            // RDD - My interpretation of this is that only character code 32's that
            // encode to spaces should have word spacing applied.  Cases have been
            // observed where a font has a space character with a character code
            // other than 32, and where word spacing (Tw) was used.  In these cases,
            // applying word spacing to either the non-32 space or to the character
            // code 32 non-space resulted in errors consistent with this interpretation.
            //
            if( (string[i] == 0x20) && c != null && c.equals( " " ) )
            {
                spacing = wordSpacing + characterSpacing;
            }
            else
            {
                spacing = characterSpacing;
            }
            
            // We want to update the textMatrix using the width, in text space units.
            //
            //The adjustment will always be zero.  The adjustment as shown in the
            //TJ operator will be handled separately.
            float adjustment=0;
            //todo, need to compute the vertical displacement
            float ty = 0;
            float tx = ((characterHorizontalDisplacement-adjustment/glyphSpaceToTextSpaceFactor)*fontSize + spacing)
                       *horizontalScaling;
            // tx2, td2 added by TH sometime
            float tx2 = (characterHorizontalDisplacement-adjustment/glyphSpaceToTextSpaceFactor)*fontSize * horizontalScaling;
            
//            System.out.println("String: " + string + " tx: " + tx + " tx2: " + tx2);
//            System.out.println("characterHorizontalDisplacement: " + characterHorizontalDisplacement);
//            System.out.println("horizontalScaling: " + horizontalScaling);
//            System.out.println("adjustment: " + adjustment);
            
            tx2 = (characterHorizontalDisplacement)*fontSize * horizontalScaling;
            
            Matrix td = new Matrix();
            td.setValue( 2, 0, tx );
            td.setValue( 2, 1, ty );
            
            Matrix td2 = new Matrix();
            td2.setValue( 2, 0, tx2 );
            td2.setValue( 2, 1, ty );
            
            float xPosBefore = getTextMatrix().getXPosition();
            float yPosBefore = getTextMatrix().getYPosition();
            Matrix textMatrix2 = (Matrix)getTextMatrix().clone();
            setTextMatrix(td.multiply( getTextMatrix() ));
            textMatrix2 = td2.multiply( textMatrix2 );

            /*
            // addition 15.04.09
             * moved above as it needs to be the same value for the string...
            float xTrans = xPos - xPosBefore;
            System.out.println("xTrans: " + xTrans);
            float yTrans = yPos - yPosBefore;
            System.out.println("yTrans: " + yTrans);
            // end addition
            */
            
            float width = 0, width2 = 0;
            if( pageRotation == 0 )
            {
                width = (getTextMatrix().getXPosition() - xPosBefore);
                width2 = (textMatrix2.getXPosition() - xPosBefore);
            }
            else if( pageRotation == 90 || pageRotation == -270)
            {
                width = (getTextMatrix().getYPosition() - yPosBefore);
                width2 = (textMatrix2.getYPosition() - yPosBefore);
            }
            else if( pageRotation == 270 || pageRotation == -90 )
            {
                width = (yPosBefore - getTextMatrix().getYPosition());
                width2 = (yPosBefore - textMatrix2.getYPosition());
                
                //width = (textMatrix.getYPosition() - yPosBefore);
                //width2 = (textMatrix2.getYPosition() - yPosBefore);
            }
            //there are several cases where one character code will
            //output multiple characters.  For example "fi" or a
            //glyphname that has no mapping like "visiblespace"
            if( c != null )
            {
                float widthOfEachCharacterForCode = width/c.length();
                for( int j=0; j<c.length(); j++)
                {
                    if( stringResult.length()+j <individualWidths.length )
                    {
                        if( c.equals("-"))
                        {
                            //System.out.println( "stringResult.length()+j=" + (widthOfEachCharacterForCode));
                        }
                        individualWidths[stringResult.length()+j] = widthOfEachCharacterForCode;
                    }
                }
            }
            
            totalStringWidth += width;
            //System.out.println("new instruction: " + c + 
            //	" width: " + width + " xPosBefore: " + xPosBefore + " yPosBefore: " + yPosBefore);
            
            // TODO:
            // with this code (incorrect results), try on matrox
            // with indiv. characters as clusters (NG generation)
            // to see whether the graph generation does actually terminate :)
            /*
            TextFragment thisChar = new TextFragment
            	(xPosBefore, xPosBefore + width, yPosBefore, 
            	yPosBefore + + (fontSize * yScale), c, font, fontSize);
            addCharacter(thisChar);
            */
            
            CharSegment thisChar;
            if (c == null) c = "";
            if (pageRotation == 0)
            {
	            //TextFragment 
            	thisChar = new CharSegment
		        	(xPosBefore + xTrans, xPosBefore + width2 + xTrans, yPos, 
		        	yPos + (fontSize * yScale), c, font, fontSize * yScale, sourceOp);
            	// addCharacter(thisChar, false);
            	//System.out.println("thisChar: " + thisChar);
            	charactersToAdd.add(thisChar);
            }
            else // pageRotation == 90
            {
            	/*
            	System.out.println("c: " + c + " yPosBefore: " + yPosBefore +
            		" (width:) " + width + " width2: " + width2 + " xPos: " + xPos + 
            		" xPosBefore: " + xPosBefore + " yPos: " + yPos + " yPosBefore: " 
            		+ yPosBefore +
            		" fontSize: " + fontSize + " xScale: " + xScale + " yScale: " +
            		yScale);
            	*/
            	
            	// xScale? need to find a doc where they are different ;)
            	thisChar = new CharSegment
		        	(yPosBefore + xTrans, yPosBefore + width2 + xTrans, yPos, 
		        	yPos + (fontSize * xScale), c, font, fontSize * xScale, sourceOp);
            	//System.out.println("thisChar: " + thisChar);
            	if (pageRotation == 270 || pageRotation == 90)
            		//addCharacter(thisChar, true);
            		charactersToAdd.add(thisChar);
            	else
            		//addCharacter(thisChar, false);
            		charactersToAdd.add(thisChar);
            }
            
//            System.out.println("yPos1.5: " + yPos);
            stringResult.append( c );
        }
//        System.out.println("yPos2: " + yPos);
        float totalStringHeight = characterVerticalDisplacement * fontSize * yScale;
        String resultingString = stringResult.toString();
//        System.out.println("yPos2.5: " + yPos);
        if( individualWidths.length != resultingString.length() )
        {
            float[] tmp = new float[resultingString.length()];
            System.arraycopy( individualWidths, 0, tmp, 0, Math.min( individualWidths.length, resultingString.length() ));
            individualWidths = tmp;
            if( resultingString.equals( "- " ))
            {
                //System.out.println( "EQUALS " + individualWidths[0] );
            }
        }
        
//        System.out.println("yPos3: " + yPos);
        float charX1 = xPos;
        float charX2 = xPos + totalStringWidth;
        float charY1 = yPos;
        float charY2 = yPos + (fontSize * yScale);//totalStringHeight;
        String c = stringResult.toString();
        
        //TextFragment thisChar = new TextFragment(charX1, charX2, charY1, charY2, 
        //		c, font, fontSize * yScale);
        //addCharacter(thisChar);
            
   //     System.out.println("showCharacter with xPos: " + xPos + " and yPos " + yPos + " and string " + stringResult.toString());
        
        // addCharacter already called above
        // showCharacter does nothing; neither in super nor in this class
        
/* start commented out 1.1
        showCharacter(
                new TextPosition(
                    xPos,
                    yPos,
                    xScale,
                    yScale,
                    totalStringWidth,
                    individualWidths,
                    totalStringHeight,
                    spaceWidth,
                    stringResult.toString(),
                    font,
                    fontSize,
                    wordSpacingDisplacement ));
        //System.out.println("relief");
end commented out 1.1 */
        
        //no call addCharacter for the list of characters to be processed...
        
        CompositeSegment thisStringFragment = new CompositeSegment();
        thisStringFragment.setText(stringResult.toString());
        //System.out.println("stringResult: " + stringResult);
        thisStringFragment.getItems().addAll(charactersToAdd);
        thisStringFragment.findBoundingBox();
        
        //System.out.println("thisStringFragment: " + thisStringFragment);
        //System.out.println("lastStringFragment: " + lastStringFragment);
        
        //float tolerance = fontSize * 0.05f;
        // changed on 7.04.09 to allow for google19.pdf and other pdfs where yscale is used
        // to determine fontsize...
        float tolerance = fontSize * yScale * 0.05f;
        
        //System.out.println("lastStringFragment: " + lastStringFragment);
        //System.out.println("thisStringFragment: " + thisStringFragment);
        
        //System.out.println("tolerance: " + tolerance);
        
        if (lastStringFragment != null &&
        	thisStringFragment.getText().equals
        		(lastStringFragment.getText()) &&
        	Utils.within(thisStringFragment.getX1(),
        		lastStringFragment.getX1(), tolerance)  &&
    		Utils.within(thisStringFragment.getX2(),
            	lastStringFragment.getX2(), tolerance)  &&
    		Utils.within(thisStringFragment.getY1(),
            	lastStringFragment.getY1(), tolerance)  &&
    		Utils.within(thisStringFragment.getY2(),
            	lastStringFragment.getY2(), tolerance))
        //if (false)
        {
        	//System.out.println("stringResult overprint...");
        	// it's an overprint; code to come here 
        }
        else
	    {
        	// add the text fragment!
	        Iterator<CharSegment> ctaIter = charactersToAdd.iterator();
	        while(ctaIter.hasNext())
	        {
	        	CharSegment thisChar = ctaIter.next();
	        	
	        	if (pageRotation == 270 || pageRotation == -90)
	        	//if (pageRotation == 270 || pageRotation == 90)
	        		addCharacter(thisChar, true);
	        	else
	        		addCharacter(thisChar, false);
	        }
        }
        lastStringFragment = thisStringFragment;
    }
    
    public void addCharacter(CharSegment thisChar, boolean reverseCoords)
    {
    	boolean concatenate = false;
    	if (fragmentList.size() > 0)
    	{
    		TextFragment lastFragment = fragmentList.get(fragmentList.size() - 1);
    		//System.out.println("**thisChar: " + thisChar);
    		//System.out.println("lastFragment: " + lastFragment);
    		
    		// TODO: this lovely overprint code doesn't work on
    		// google19.pdf, where complete sentences are
    		// rendered at once... :(
    		CharSegment lastChar = (CharSegment)lastFragment.getItems().
    			get(lastFragment.getItems().size() - 1);
    		boolean overprint = false;
    		float tolerance = thisChar.getFontSize() * 0.05f;
    		if ((lastChar.getText().equals(thisChar.getText()) &&
    			 lastChar.getFontSize() == thisChar.getFontSize() &&
    			 Utils.within(lastChar.getX1(), thisChar.getX1(), tolerance) &&
    			 Utils.within(lastChar.getX2(), thisChar.getX2(), tolerance) &&
    			 Utils.within(lastChar.getY1(), thisChar.getY1(), tolerance) &&
    			 Utils.within(lastChar.getY2(), thisChar.getY2(), tolerance) &&
     			 lastChar.getFontName() == thisChar.getFontName())
    			 ) overprint = true;
    		if (overprint)
    		{
    			/*
    			System.out.println("Overprint with: ");
    			System.out.println("lastChar: " + lastChar);
    			System.out.println("lastFramgent: " + lastFragment);
    			System.out.println("thisChar: " + thisChar);
    			System.out.println("Setting lastChar to overprint");
    			System.out.println();
    			*/
    			lastChar.setOverprint(true);
    		}
    		else
    		{
	    		boolean sameLine;
	    		/*
	    		if (lastFragment.getY1() == thisChar.getY1())
	    			sameLine = true; else sameLine = false;
	    		*/
	    		// 23.06.08 added error margin
	    		if (Utils.within(lastFragment.getY1(), thisChar.getY1(), tolerance))
	    			sameLine = true; else sameLine = false;
	    		boolean sameWord;
	    		float spacing;

	    		if (reverseCoords)
	    		{
	    			//System.out.println("reverseCoords");
		    		float lfWidth = lastFragment.getWidth();
	    			float tcWidth = thisChar.getWidth();
	    			lastFragment.setX1(0 - lastFragment.getX1());
	    			lastFragment.setX2(lastFragment.getX1() + lfWidth);
	    			thisChar.setX1(0 - thisChar.getX1());
	    			thisChar.setX2(thisChar.getX1() + tcWidth);
	    		}

	    		spacing = thisChar.getX1() - lastFragment.getX2();
	    		//System.out.println("spacing: " + spacing);
	    		if (spacing < thisChar.getWidth() * 0.25 &&
	    			spacing > (0 - thisChar.getWidth() * 1.0))
	    			sameWord = true; else sameWord = false;
	    		if (sameLine && sameWord)
	    			concatenate = true; else concatenate = false;	
    	
	    		if (mergeAcrossTextInstructions)
	    			concatenate = !newTextFragment;
	    		
//	    		TEST 27.08.09
//	    		concatenate = !newTextFragment;
	    			
		    	if (concatenate)
		    	{
		    		// concatenate last fragment of fragmentList
		    		// logically, the size of fragmentList must be at least one
		    		
		    		lastFragment.setX2(thisChar.getX2());
		    		lastFragment.setText(lastFragment.getText().concat(thisChar.getText()));
		    		lastFragment.getItems().add(thisChar);
		    	}
		    	else
		    	{
		    		// !concatenate stuff was here but we
		    		// need to revert the co-ordinates back...
		    	}
	    	
		    	if (reverseCoords)
	    		{
		    		float lfWidth = lastFragment.getWidth();
	    			float tcWidth = thisChar.getWidth();
	    			lastFragment.setX1(0 - lastFragment.getX1());
	    			lastFragment.setX2(lastFragment.getX1() + lfWidth);
	    			thisChar.setX1(0 - thisChar.getX1());
	    			thisChar.setX2(thisChar.getX1() + tcWidth);
	    		}
		    	if (!concatenate)
		    	{
		    		TextFragment newFrag = new TextFragment(thisChar);
//		    		2011-01-25 constructor does all this automatically
//		    		newLine.getItems().add(thisChar);
//		    		newLine.setBoundingBox(thisChar.getBoundingBox());
//		    		newLine.setText(thisChar.getText());
//		    		newLine.setFontName(thisChar.getFontName());
//		    		newLine.setFontSize(thisChar.getFontSize());
		    		fragmentList.add(newFrag);
		    	}
    		}
    	}
    	else
    	{
    		TextFragment newFrag = new TextFragment(thisChar);
//    		2011-01-25 constructor does all this automatically
//    		newFrag.getItems().add(thisChar);
//    		newFrag.setBoundingBox(thisChar.getBoundingBox());
//    		newFrag.setText(thisChar.getText());
//    		newFrag.setFontName(thisChar.getFontName());
//    		newFrag.setFontSize(thisChar.getFontSize());
    		fragmentList.add(newFrag);
    	}
    	// do not add empty space characters (bmw example)
    	if (!thisChar.getText().equals(" "))
    		charList.add(thisChar);
    	
    	setNewTextFragment(false); // only needed when mergeAcrossTextInstructions == true
    }
    
    // pre: characters are in the correct order
	public static void removeLeadingTrailingSpaces
		(List<TextFragment> fragmentList)
	{
		List<TextFragment> fragsToRemove =
			new ArrayList<TextFragment>();
		for (TextFragment tf : fragmentList)
		{
			// remove from beginning
			while(tf.getItems().size() > 0 &&
				tf.getItems().get(0).getText().equals(" "))
			{
				tf.getItems().remove(0);
				tf.findBoundingBox();
				tf.findText();
			}
			
			// remove at end
			int lastIndex = tf.getItems().size() - 1;
			while(tf.getItems().size() > 0 &&
				tf.getItems().get(lastIndex).getText().equals(" "))
			{
				tf.getItems().remove(lastIndex);
				tf.findBoundingBox();
				tf.findText();
				
				// recalculate lastIndex -- important!
				lastIndex = tf.getItems().size() - 1;
			}
			
			if (tf.getItems().size() == 0)
				fragsToRemove.add(tf);
		}
		
		fragmentList.removeAll(fragsToRemove);
	}
	
    
    /**
     * You should override this method if you want to perform an action when a
     * string is being shown.
     *
     * @param text The string to display.
     */
/* start commented out 1.1
    protected void showCharacter( TextPosition text )
    {
        //should use colorspaces for the font color but for now assume that
        //the font color is black
        try
        {
            if( this.getGraphicsState().getTextState().getRenderingMode() == PDTextState.RENDERING_MODE_FILL_TEXT ) 
            {
                graphics.setColor( this.getGraphicsState().getNonStrokingColorSpace().createColor() );
            } 
            else if( this.getGraphicsState().getTextState().getRenderingMode() == PDTextState.RENDERING_MODE_STROKE_TEXT )
            {
                graphics.setColor( this.getGraphicsState().getStrokingColorSpace().createColor() );
            }
            else
            {
                //need to implement....
            }
            PDFont font = text.getFont();
//            System.out.println("gotten font: " + font.getBaseFont() + " subtype: " + font.getSubType());
            try
            {
            	font.drawString( text.getCharacter(), graphics, text.getFontSize(), text.getXScale(), text.getYScale(),
                    text.getX(), text.getY() );
            }
            catch (Exception e)
            {
            	// e.g. font name not found
            	e.printStackTrace();
            }
            // works for the top part of Daimler!! edocsacs.pdf
            //TextFragment newFragment = new TextFragment(text);
            //TextLine newLine = new TextLine(newFragment);
            //newLine.findBoundingBox();
            //newLine.findText(false);
            //fragmentList.add(newLine);
            //System.out.println("added fragment: " + newLine);
            
        }
        catch( IOException io )
        {
            io.printStackTrace();
        }
    }
end commented out 1.1 */    
  
    /**
     * Get the graphics that we are currently drawing on.
     * 
     * @return The graphics we are drawing on.
     */
    public Graphics2D getGraphics()
    {
        return graphics;
    }
    
    /**
     * Get the page that is currently being drawn.
     * 
     * @return The page that is being drawn.
     */
    public PDPage getPage()
    {
        return page;
    }
    
    /**
     * Get the size of the page that is currently being drawn.
     * 
     * @return The size of the page that is being drawn.
     */
    public Dimension getPageSize()
    {
        return pageSize;
    }
    
    /**
     * Fix the y coordinate based on page rotation.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The updated y coordinate.
     */
    public double fixY( double x, double y )
    {
    	return pageSize.getHeight() - y;
    }
    
    /**
     * Get the current line path to be drawn.
     * 
     * @return The current line path to be drawn.
     */
    public GeneralPath getLinePath()
    {
        return linePath;
    }

    /**
     * Set the line path to draw.
     * 
     * @param newLinePath Set the line path to draw.
     */
    public void setLinePath(GeneralPath newLinePath)
    {
        if (linePath == null || linePath.getCurrentPoint() == null){
            linePath = newLinePath;
        }else{
            linePath.append (newLinePath, false);
        }
    }
    
    /**
     * Get the current list of line paths to be drawn.
     * 
     * @return The current list of line paths to be drawn.
     */
    public List getLineSubPaths()
    {
        return lineSubPaths;
    }

    /**
     * Set the list of line paths to draw.
     * 
     * @param newLineSubPaths Set the list of line paths to draw.
     */
    public void setLineSubPaths(List newLineSubPaths)
    {
        lineSubPaths = newLineSubPaths;
    }
    	
    /**
     *
     * Fill the path
     * 
     * @param windingRule The winding rule this path will use.
     */
    public void fillPath(int windingRule) throws IOException{
    	
    	graphics.setColor( getGraphicsState().getNonStrokingColor().getJavaColor() );
        
        //logger().info("Filling the path with rule: " + windingRule);
        
    	getLinePath().setWindingRule(windingRule);
        
    	graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        List subPaths = getLineSubPaths();
        for( int i=0; i<subPaths.size(); i++ )
        {
            GeneralPath subPath = (GeneralPath)subPaths.get( i );
            if (subPath.getCurrentPoint() != null){ //Sector9's suggestion in bug 1672556
                subPath.closePath();
            }
            /*Rectangle bBox = subPath.getBounds();
            Point2D point1 = TransformedPoint(bBox.x, bBox.y);
            Point2D point2 = TransformedPoint(bBox.x + bBox.width, bBox.y + bBox.height);
            //RectSegment ls = new RectSegment(bBox.x, bBox.x*bBox.width, bBox.y, bBox.y+bBox.height);
            RectSegment ls = new RectSegment((float)point1.getX(), (float)point2.getX(), 
            	(float)point1.getY(), (float)point2.getY());
            System.out.println("fillPath adding line segment: " + ls);
            rectList.add(ls);*/
            
            graphics.fill( subPath );
        }
        
            graphics.fill( getLinePath() );
            getLinePath().reset();;
    }
    
        
    public void setStroke(BasicStroke newStroke){
    	getGraphics().setStroke( newStroke );
    }
    
    public void StrokePath() throws IOException{
    	graphics.setColor( getGraphicsState().getStrokingColor().getJavaColor() ); //per Ben's 11/15 change in StrokePath.java
        List subPaths = getLineSubPaths();
        for( int i=0; i<subPaths.size(); i++ )
        {
            GeneralPath subPath = (GeneralPath)subPaths.get( i );
            graphics.draw( subPath );
            /*
            Rectangle bBox = subPath.getBounds();
            Point2D point1 = TransformedPoint(bBox.x, bBox.y);
            Point2D point2 = TransformedPoint(bBox.x + bBox.width, bBox.y + bBox.height);
            //RectSegment ls = new RectSegment(bBox.x, bBox.x*bBox.width, bBox.y, bBox.y+bBox.height);
            RectSegment ls = new RectSegment((float)point1.getX(), (float)point2.getX(), 
            	(float)point1.getY(), (float)point2.getY());
            System.out.println("strokePath adding line segment: " + ls);
            rectList.add(ls);*/
        }
        subPaths.clear();
        GeneralPath path = getLinePath();
        graphics.draw( path );
        path.reset();
    }
    
    // these colour methods added from old method 100409
    /**
     * Get the non stroking color.
     * 
     * @return The non stroking color.
     */
    public Color getNonStrokingColor()
    {
        return nonStrokingColor;
    }

    /**
     * Set the non stroking color.
     * 
     * @param newNonStrokingColor The non stroking color.
     */
    public void setNonStrokingColor(Color newNonStrokingColor)
    {
        nonStrokingColor = newNonStrokingColor;
        currentColor = nonStrokingColor;
    }

    /**
     * Get the stroking color.
     * 
     * @return The stroking color.
     */
    public Color getStrokingColor()
    {
        return strokingColor;
    }

    /**
     * Set the stroking color.
     * 
     * @param newStrokingColor The stroking color.
     */
    public void setStrokingColor(Color newStrokingColor)
    {
        strokingColor = newStrokingColor;
        currentColor = strokingColor;
    }
    
    
    //If you need to do anything when a color changes, do it here ... or in an override of this function
    public void ColorChanged(Boolean bStroking) throws IOException{
        //logger().info("changing " + (bStroking ? "" : "non") + "stroking color");
/////    	System.out.println("changing " + (bStroking ? "" : "non") + "stroking color");
    }
    
    //This code generalizes the code Jim Lynch wrote for AppendRectangleToPath
    public java.awt.geom.Point2D.Double TransformedPoint (double x, double y){
        
        double scaleX = 0.0;
        double scaleY = 0.0;
        double transX = 0.0;
        double transY = 0.0;
        
        double finalX = x;
        double finalY = y;
        
        //Get the transformation matrix 
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();

    	scaleX = at.getScaleX();
        scaleY = at.getScaleY();
        transX = at.getTranslateX();
        transY = at.getTranslateY();
        
        Point2D Pscale = ScaledPoint (finalX, finalY, scaleX, scaleY);
        finalX = Pscale.getX();
        finalY = Pscale.getY();
        
        finalX += transX;
      	finalY += transY;
	
        finalY = fixY( finalX, finalY );
        finalY -= .6;
        
        return new java.awt.geom.Point2D.Double(finalX, finalY);
    }
    
    //Use ScaledPoint rather than TransformedPoint in situations where most of the translation
    //need not be repeated.
    //Consider, for example, the second coordinate of a rectangle.
    public java.awt.geom.Point2D.Double ScaledPoint (double x, double y, double scaleX, double scaleY){
        
        double finalX = 0.0;
        double finalY = 0.0;
        
        if(scaleX > 0)
    	{
	    	finalX = x * scaleX;
    	}
        if(scaleY > 0)
        {
        	finalY = y * scaleY;
    	}
        
        return new java.awt.geom.Point2D.Double(finalX, finalY);
    }
    
    public java.awt.geom.Point2D.Double ScaledPoint (double x, double y){
        
        double scaleX = 0.0;
        double scaleY = 0.0;
        
        //Get the transformation matrix 
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();
       	scaleX = at.getScaleX();
        scaleY = at.getScaleY();
        return ScaledPoint(x, y, scaleX, scaleY);
    }
    
    // 100409 methods from old class below...
    
    /**
     * Start a new page.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     * 
     * @param page The page we are about to process.
     * 
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startPage( PDPage page ) throws IOException
    {
        //default is to do nothing.
    }
    
    /**
     * End a page.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     * 
     * @param page The page we are about to process.
     * 
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endPage( PDPage page ) throws IOException
    {
        //default is to do nothing
    }
    
    /**
     * This will set the first page to be extracted by this class.
     *
     * @param startPageValue New value of property startPage.
     */
    public void setStartPage(int startPageValue)
    {
        startPage = startPageValue;
    }
    
    /**
     * This will set the last page to be extracted by this class.
     *
     * @param endPageValue New value of property endPage.
     */
    public void setEndPage(int endPageValue)
    {
        endPage = endPageValue;
    }

    /**
     * This is the page that the text extraction will start on.  The pages start
     * at page 1.  For example in a 5 page PDF document, if the start page is 1
     * then all pages will be extracted.  If the start page is 4 then pages 4 and 5
     * will be extracted.  The default value is 1.
     *
     * @return Value of property startPage.
     */
    public int getStartPage()
    {
        return startPage;
    }
    
    /**
     * This will get the last page that will be extracted.  This is inclusive,
     * for example if a 5 page PDF an endPage value of 5 would extract the
     * entire document, an end page of 2 would extract pages 1 and 2.  This defaults
     * to Integer.MAX_VALUE such that all pages of the pdf will be extracted.
     *
     * @return Value of property endPage.
     */
    public int getEndPage()
    {
        return endPage;
    }
    
    
    public BufferedImage pageImage(PDDocument doc, int pageNo) throws IOException
    {
    	BufferedImage retVal;
    	
    	// *** start copied from beginning of getObjects method
    	
    	PDEncryptionDictionary encDictionary = doc.getEncryptionDictionary();

        //only care about standard encryption and if it was decrypted with the
        //user password
        if( encDictionary instanceof PDStandardEncryption && 
            !doc.wasDecryptedWithOwnerPassword() )
        {
            PDStandardEncryption stdEncryption = (PDStandardEncryption)encDictionary;
            if( !stdEncryption.canExtractContent() )
            {
                throw new IOException( "You do not have permission to extract text" );
            }
        }
        currentPageNo = 1;
        document = doc;
        
        //output = outputStream;
        //startDocument(document);
        
        if( document.isEncrypted() )
        {
            // We are expecting non-encrypted documents here, but it is common
            // for users to pass in a document that is encrypted with an empty
            // password (such a document appears to not be encrypted by
            // someone viewing the document, thus the confusion).  We will
            // attempt to decrypt with the empty password to handle this case.
            //
            try
            {
                document.decrypt("");
            }
            catch (CryptographyException e)
            {
                throw new IOException("Error decrypting document, details: " + e.getMessage());
            }
            catch (InvalidPasswordException e)
            {
                throw new IOException("Error: document is encrypted");
            }
        }

        List allPages = document.getDocumentCatalog().getAllPages();
        
        // *** end copied from beginning of getObjects method
        
        PDPage page = (PDPage)allPages.get(pageNo);
    	PDResources resources = page.getResources();
    	Map  images = resources.getImages();
    	if( images != null )
    	{
    		Iterator  imageIter = images.keySet().iterator();
    		// ** TODO: return exception if >1 image on page?
    		while( imageIter.hasNext() )
	    	{
		    	String  key = (String )imageIter.next();
		    	PDXObjectImage image = (PDXObjectImage)images.get( key );
		    	retVal = image.getRGBImage();
		    	return retVal;
	    	}
    	}
        
    	throw new IOException("No images found");
    }
    
    
    /**
     * This will take a PDDocument and return a list of PDFPage objects for each page
     *
     * @param doc The document to get the data from.
     * @param outputStream The location to put the text.
     *
     * @throws IOException If the doc is in an invalid state.
     */
    public List<PDFPage> findObjects(PDDocument doc) throws IOException
    {
        PDEncryptionDictionary encDictionary = doc.getEncryptionDictionary();

        //only care about standard encryption and if it was decrypted with the
        //user password
        if( encDictionary instanceof PDStandardEncryption && 
            !doc.wasDecryptedWithOwnerPassword() )
        {
            PDStandardEncryption stdEncryption = (PDStandardEncryption)encDictionary;
            if( !stdEncryption.canExtractContent() )
            {
                throw new IOException( "You do not have permission to extract text" );
            }
        }
        currentPageNo = 1;
        document = doc;
        
        //output = outputStream;
        //startDocument(document);

        if( document.isEncrypted() )
        {
            // We are expecting non-encrypted documents here, but it is common
            // for users to pass in a document that is encrypted with an empty
            // password (such a document appears to not be encrypted by
            // someone viewing the document, thus the confusion).  We will
            // attempt to decrypt with the empty password to handle this case.
            //
            try
            {
                document.decrypt("");
            }
            catch (CryptographyException e)
            {
                throw new IOException("Error decrypting document, details: " + e.getMessage());
            }
            catch (InvalidPasswordException e)
            {
                throw new IOException("Error: document is encrypted");
            }
        }

        List allPages = document.getDocumentCatalog().getAllPages();
        int numPages = allPages.size();
        
        // take care of negative page numbers
        // and swap startPage and endPage if necessary
        
        if (startPage < 0)
            startPage = numPages + startPage;
        if (endPage < 0)
            endPage = numPages + endPage;
        if (startPage > endPage)
        {
            int tempVar = startPage;
            startPage = endPage;
            endPage = tempVar;
        }
        
        /*
        System.out.println("Number of pages: " + numPages);
        System.out.println("Start page is now: " + startPage);
        System.out.println("End page is now: " + endPage);
        */
        
        // return processPages( document.getDocumentCatalog().getAllPages() );
        return processPages(allPages);
        // writer only method?
        //endDocument(document);
    }
    
    /**
     * This will process all of the pages and the text that is in them.
     *
     * @param pages The pages object in the document.
     *
     * @throws IOException If there is an error parsing the text.
     */
    protected List<PDFPage> processPages( List pages ) throws IOException
    {
        List<PDFPage> retVal = new ArrayList<PDFPage>();
        
        if( startBookmark != null )
        {
            startBookmarkPageNumber = currentPageNumber( startBookmark, pages );
        }
        
        if( endBookmark != null )
        {
            endBookmarkPageNumber = currentPageNumber( endBookmark, pages );
        }
        
        if( startBookmarkPageNumber == -1 && startBookmark != null &&
            endBookmarkPageNumber == -1 && endBookmark != null &&
            startBookmark.getCOSObject() == endBookmark.getCOSObject() )
        {
            //this is a special case where both the start and end bookmark
            //are the same but point to nothing.  In this case
            //we will not extract any text.
            startBookmarkPageNumber = 0;
            endBookmarkPageNumber = 0;
        }

        Iterator pageIter = pages.iterator();
        // this loop uncommented to process only the first page
        while( pageIter.hasNext() )
        {
            PDPage nextPage = (PDPage)pageIter.next();
            
            // added for further development
            page = nextPage;
            
            try 
            {
            	System.out.println("Current page no: " + currentPageNo);
            
	            if (page.getArtBox() != null)
	            	System.out.println("Art Box: " + new GenericSegment(page.getArtBox()));
	            if (page.getBleedBox() != null)
	           		System.out.println("Bleed Box: " + new GenericSegment(page.getBleedBox()));
	            // the following value caused problems for pp120-hassan.pdf
	            if (page.getCropBox() != null)
	            	System.out.println("Crop Box: " + new GenericSegment(page.getCropBox()));
	            if (page.getMediaBox() != null)
	            	System.out.println("Media Box: " + new GenericSegment(page.getMediaBox()));
	            if (page.getTrimBox() != null)
	            	System.out.println("Trim Box: " + new GenericSegment(page.getTrimBox()));
	            
	            // if media box is null (not allowed!) find a substitute
	            
	            if (page.getMediaBox() == null)
	            {
	            	if (page.getBleedBox() != null)
	               		page.setMediaBox(page.getBleedBox());
	            	else if (page.getCropBox() != null)
	            		page.setMediaBox(page.getCropBox());
	            	else if (page.getTrimBox() != null)
	            		page.setMediaBox(page.getTrimBox());
	            	else if (page.getArtBox() != null)
	                	page.setMediaBox(page.getArtBox());
	            }
 
            }
            catch(NullPointerException npe)
            {
            	System.out.println("at least one of the boxes is missing!");
            	npe.printStackTrace();
            }
            int pageRotation = page.findRotation();
            System.out.println("Page rotation: " + pageRotation);
            // end of addition
            PDStream contentStream = nextPage.getContents();
            
            GenericSegment pageDim;
            /* simple version
            if (page.getMediaBox() != null)
            	pageDim = new GenericSegment(page.getMediaBox());
            else 
            	pageDim = new GenericSegment(page.getArtBox());
            */
            
            // 20.02.09: swapped artBox for trimBox...
            if (page.getMediaBox() != null && page.getTrimBox() != null)
            {
            	GenericSegment mediaBox = new GenericSegment(page.getMediaBox());
            	GenericSegment trimBox = new GenericSegment(page.getTrimBox());
            	
            	// fixed "Das Windsor-Syndrom" document
            	pageDim =
            		new GenericSegment(trimBox.getX1(), trimBox.getX2(),
            		trimBox.getY1(), trimBox.getY2());
            	
            	/* state at end of NextWrap
            	pageDim =
            		new GenericSegment(mediaBox.getX1(), mediaBox.getX2(),
            		mediaBox.getY1(), artBox.getY2());
            	*/
            }
            else if (page.getMediaBox() != null)
            {
            	pageDim = new GenericSegment(page.getMediaBox());
            }
            else if (page.getArtBox() != null)
            {
            	// will never reach this stage because
            	// a page always has a MediaBox... (above=...
            	pageDim = new GenericSegment(page.getArtBox());
            }
            else
            {
            	throw new IOException
            		("Cannot find a suitable page bounding box!");
            }
            
            
            System.out.println("Processing page: " + currentPageNo);
//            System.out.println("startPage: " + startPage);
//            System.out.println("endPage: " + endPage);
            
            if( currentPageNo >= startPage && currentPageNo <= endPage &&
                    (startBookmarkPageNumber == -1 || currentPageNo >= startBookmarkPageNumber ) && 
                    (endBookmarkPageNumber == -1 || currentPageNo <= endBookmarkPageNumber ))
            {
                if( contentStream != null )
                {
                    COSStream contents = contentStream.getStream();
                    PDFPage thePage = processPage(nextPage, contents);
                    
                // 20.06.08 this is now done in processPage...
                // because of the complications with rotation...
                //    thePage.setBoundingBox(pageDim.getBoundingBox()); 
                    //thePage.reverseYCoordinates();
                    
                    retVal.add(thePage);
                }
            }
            else
            {
                // skip to next page
                if( contentStream != null )
                {
                    COSStream contents = contentStream.getStream();
                    //System.out.println("processing page " + currentPageNo + "...");
                    //PDFPage throwawayVar = processPage( nextPage, contents );
                    	// unnecessary; all that did was increment currentPageNo!
                    currentPageNo ++;
                }
            }
        }
        return retVal;
    }
    
    protected int currentPageNumber( PDOutlineItem bookmark, List allPages ) throws IOException 
    {
        int pageNumber = -1;
        PDPage page = bookmark.findDestinationPage( document );
        if( page != null )
        {
            pageNumber = allPages.indexOf( page )+1;//use one based indexing
        }
        return pageNumber;
    }
    
    /**
     * This will process the contents of a page.
	 * modified by TH
     *
     * @param page The page to process.
     * @param content The contents of the page.
     *
     * @throws IOException If there is an error processing the page.
     */
    protected PDFPage processPage( PDPage page, COSStream content ) throws IOException
    {
    	// new from 23.04.09
    	clipBounds = new GenericSegment(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
    		Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    	
    	this.page = page;
//      pageSize = page.getArtBox().createDimension();
//    	pageSize = page.getCropBox().createDimension(); // seems to make no difference to SN...
    	
        PDResources resources = page.findResources();
        
        PDFPage thisPage = new PDFPage();
        //GenericSegment pageDim = new GenericSegment(page.getArtBox());
        GenericSegment pageDim; // = new GenericSegment(page.getCropBox()); // seems to make no difference to SN...
        
        if (page.getCropBox() != null)
        {
        	pageDim = new GenericSegment(page.getCropBox());
        	//pageSize = page.getCropBox().createDimension(); // needed for PDFBox methods
        }
        else
        {
        	pageDim = new GenericSegment(page.findMediaBox());
        	//pageSize = page.findMediaBox().createDimension(); // needed for PDFBox methods
        }
        pageSize = page.findMediaBox().createDimension(); // needed for PDFBox graphic drawing methods
        // if statement added for EFLensChart.pdf
        if (page.getRotation() == null) thisPage.setRotation(0);
        else thisPage.setRotation(page.getRotation());
        
        // PDFBox code for rasterizing image
        // the mBox is not used for our purposes
        int scaling = 2;
        PDRectangle mBox = page.findMediaBox();
        //PDRectangle mBox = page.getCropBox(); // also does nothing!
        int width = (int)(mBox.getWidth());//*2);
        int height = (int)(mBox.getHeight());//*2);
        Dimension pageDimension = new Dimension( width, height );
        
        //note we are doing twice as many pixels because
        //the default size is not really good resolution,
        //so create an image that is twice the size
        //and let the client scale it down.
        BufferedImage retval = new BufferedImage
        	( width*scaling, height*scaling, BufferedImage.TYPE_BYTE_INDEXED );
        //Graphics2D graphics = (Graphics2D)retval.getGraphics();
        graphics = (Graphics2D)retval.getGraphics();
        graphics.setColor( Color.WHITE );
        graphics.fillRect(0,0,width*scaling, height*scaling);
        graphics.scale( scaling, scaling );
        
        // end of new stuff...
        
        imageList = new ArrayList<ImageSegment>();
        lineList = new ArrayList<LineSegment>();
        rectList = new ArrayList<RectSegment>();
        fragmentList = new ArrayList<TextFragment>();
        charList = new ArrayList<CharSegment>();
        
        // TODO: method within GenericSegment instead of here?
        thisPage.setX1(pageDim.getX1());
        thisPage.setX2(pageDim.getX2());
        thisPage.setY1(pageDim.getY1());
        thisPage.setY2(pageDim.getY2());
        
        List<GenericSegment> pageItems = thisPage.getItems();
		// end of addition

        long start = System.currentTimeMillis();
        currentPageNo++;
        
        // former loop (page selection)
        // {
            startPage( page );
            pageArticles = page.getThreadBeads();
            int numberOfArticleSections = 1 + pageArticles.size() * 2;
            if( !shouldSeparateByBeads )
            {
                numberOfArticleSections = 1;
            }
            int originalSize = charactersByArticle.size();
            charactersByArticle.setSize( numberOfArticleSections );
            for( int i=0; i<numberOfArticleSections; i++ )
            {
                if( numberOfArticleSections < originalSize )
                {
                    ((List)charactersByArticle.get( i )).clear();
                }
                else
                {
                    charactersByArticle.set( i, new ArrayList() );
                }
            }
            
            characterListMapping.clear();
            long startProcess = System.currentTimeMillis();
            processStream( page, page.findResources(), content );
            long stopProcess = System.currentTimeMillis();
            long startFlush = System.currentTimeMillis();
            
            // TODO: rescue this bit of code someday!
            // add all text elements to page object
			for( int i=0; i<charactersByArticle.size(); i++)
        	{
            	List textList = (List)charactersByArticle.get( i );
            	Iterator textIter = textList.iterator();
				
            	while( textIter.hasNext() )
            	{
					TextPosition thisPos = (TextPosition)textIter.next();
					pageItems.add(new TextFragment(thisPos, pageDim));
					System.out.println("text fragment by article found");
				}
			}

			thisPage.getItems().addAll(fragmentList);
			// line below uncommented 17.03.07
			thisPage.getItems().addAll(charList);
			
			
			// remove anything not within the crop box
			if (page.getCropBox() != null)
			{
				//System.out.println("cropBox: " + new GenericSegment(page.getCropBox()));
				
//				2011-01-25 theItems already set
//				List items = thisPage.getItems();
				
				GenericSegment cropBox = new GenericSegment(page.getCropBox()); 
				
				
				// these lines only for manual input of the cropBox
				float new_Y2 = page.getMediaBox().getHeight() - cropBox.getY1();
				float new_Y1 = page.getMediaBox().getHeight() - cropBox.getY2();
				cropBox.setY1(new_Y1);
				cropBox.setY2(new_Y2);
				
				//System.out.println("current cropBox: " + cropBox);
				
				if (thisPage.getRotation() == 270 || thisPage.getRotation() == -90)
				{
					// TODO: refactor as e.g. GenericSegment.rotate
					// 3x 90 deg rotate
					// well, that's the cheeky way... :)
					cropBox = new GenericSegment(cropBox.getX1(), 
						cropBox.getX1() + cropBox.getHeight(), cropBox.getY1(),
						cropBox.getY1() + cropBox.getWidth());
					cropBox = new GenericSegment(cropBox.getX1(), 
						cropBox.getX1() + cropBox.getHeight(), cropBox.getY1(),
						cropBox.getY1() + cropBox.getWidth());
					cropBox = new GenericSegment(cropBox.getX1(), 
						cropBox.getX1() + cropBox.getHeight(), cropBox.getY1(),
						cropBox.getY1() + cropBox.getWidth());
				}
				else if (thisPage.getRotation() == 90 || thisPage.getRotation() == -270)
				{
					cropBox = new GenericSegment(cropBox.getX1(), 
						cropBox.getX1() + cropBox.getHeight(), cropBox.getY1(),
						cropBox.getY1() + cropBox.getWidth());
					
					//System.out.println("new cropBox: " + cropBox);
				}
				
				for (int n = 0; n < pageItems.size(); n ++)
				{
					GenericSegment gs = (GenericSegment)pageItems.get(n);
					if (!SegmentUtils.intersects(gs, cropBox))
					{
						pageItems.remove(gs);
						n --;
					}
				}
			}
			
			
			if (thisPage.getRotation() == 270 || thisPage.getRotation() == -90)
			{
				// I imagine somewhere in the PDFBox code it tries to be
				// clever and reverse the co-ordinates with the incorrect
				// page dimensions; we need to undo this(!)
				thisPage.reverseYCoordinatesPDF();
				
				// not quite as easy as just swapping X and Y co-ordinates
				// page needs to be rotated around its _top_ axis; this means
				// subtracting the difference between height & width...
				thisPage.setBoundingBox(new float[]
				    {thisPage.getY1(), thisPage.getY2(),
					 thisPage.getY2() - thisPage.getWidth(), thisPage.getY2()});
				// note getWidth is actually now the height :)
				
				thisPage.normalizeCoordinates();
				thisPage.reverseXCoordinates();
			}
			else
			{
				//thisPage.normalizeCoordinates();
				// reverseYCoordinates is common, so do nothing...
			}
			
			pageItems.addAll(rectList);
			pageItems.addAll(lineList);
			pageItems.addAll(imageList);
			
			thisPage.normalizeCoordinates();
			thisPage.reverseYCoordinatesPDF();
			thisPage.setLastOpIndex(opIndex);
			
            //pageItems.addAll(lineList);
            //pageItems.addAll(rectList);
            
            long stopFlush = System.currentTimeMillis();
            endPage( page );
        // end of former loop
        // }
            
        long stop = System.currentTimeMillis();
        
    //    System.out.println("returning thisPage: " + thisPage.toExtendedString());
        
        return thisPage;
    }
    
    public boolean isNewTextFragment() {
		return newTextFragment;
	}

	public void setNewTextFragment(boolean newTextFragment) {
		this.newTextFragment = newTextFragment;
	}

	public void pushClipBounds()
    {
		clipBoundsStack.push(clipBounds.clone());
    	// NOOOOOO!!!
		//clipBounds = new GenericSegment(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
        //	Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }
    
    public void popClipBounds()
    {
    	clipBounds = (GenericSegment)clipBoundsStack.pop();
    }
    
    public void simpleModifyClippingPath()
    {
    	//GenericSegment pathBounds = new GenericSegment();
    	// TODO: getDilatedSegment and enlarge... redundancy?
/////    	System.out.println("path: " + ((CompoundTextSegment)getPathBounds()).toExtendedString());
    	clipBounds.shrinkBoundingBox(currentPathBounds());//.getDilatedSegment(5.0f));
/////    	System.out.println("clipBounds is now: " + clipBounds);
    }
    
    public GenericSegment currentPathBounds()
    {
    	CompositeSegment retVal = new CompositeSegment();
    	retVal.getItems().addAll(linesToAdd);
    	retVal.getItems().addAll(rectsToAdd);
    	retVal.getItems().addAll(currentLines);
    	retVal.getItems().addAll(currentRects);
    	retVal.findBoundingBox();
    	return retVal;
    }
    
    public void newPath()
	{
    	if (true)
    	//if (!pathContainsCurve)
    	{
	    	linesToAdd.addAll(currentLines);
			rectsToAdd.addAll(currentRects);
    	}
		newPath = true;
		pathContainsCurve = false;
		pathBeginSet = false;
		pathClosed = false;
		
		currentLines = new ArrayList<LineSegment>();
		currentRects = new ArrayList<RectSegment>();
	}
    
    public void endPath()
    {
    	newPath = true;
		pathContainsCurve = false;
		pathBeginSet = false;
		pathClosed = false;
		linesToAdd = new ArrayList<LineSegment>();
		rectsToAdd = new ArrayList<RectSegment>();
    }

    public void simpleCurveTo(float x1, float y1, float x2, float y2, float x3, float y3)
    {
    	// points already transformed!
    	pathContainsCurve = true;
    	LineSegment l1 = new LineSegment(currentX, x1, currentY, y1);
    	LineSegment l2 = new LineSegment(x1, x2, y1, y2);
    	LineSegment l3 = new LineSegment(x2, x3, y2, y3);
    	l1.setCurve(true);
    	l2.setCurve(true);
    	l3.setCurve(true);
    	currentLines.add(l1);
    	currentLines.add(l2);
    	currentLines.add(l3);
    	currentX = x3;
    	currentY = y3;
    }
    
    public void simpleClosePath()
    {
    	if (pathBeginSet && !pathClosed)
    	{
	    	simpleLineTo(pathBeginX, pathBeginY);
	    	pathClosed = true;
    	}
    }
    
	public void simpleMoveTo(float x, float y)
    {
    	Point2D Ppos = TransformedPoint(x, y);   
    	currentX = (float)Ppos.getX();
    	currentY = (float)Ppos.getY();
/////    	System.out.println("moving to: " + Ppos.getX() + ", " + Ppos.getY());
    	// newPath() called by calling OperatorProcessor method
    }
    
    public void simpleLineTo(float x, float y)
	{
    	/*
    	Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
    	
    	float xFrom = ctm.getXPosition();
		float yFrom = ctm.getYPosition();
		
		Point2D Pfrom = TransformedPoint(xFrom, yFrom);
		*/
    	float[] comp = getStrokingColor().getRGBColorComponents(null);
/////    	System.out.println("line stroke colour: " + comp[0] + " " + comp[1] + " " + comp[2]);
    	
		Point2D Pto = TransformedPoint(x, y);    	
    	
		//if (newPath)
		//{
	    	LineSegment newLine = new LineSegment(currentX, 
	    		(float)Pto.getX(), currentY, (float)Pto.getY());
	    	/*
	    	LineSegment newLine = new LineSegment(newLineTemp.getX1(),
	    		newLineTemp.getX2(), newLineTemp.getY1() - newLineTemp.getHeight(),
	    		newLineTemp.getY1());
	    	*/
/////	    	System.out.println("adding line: " + newLine);
	    	newLine.correctNegativeDimensions();
	    	newLine.rotate(page);
/////	    	System.out.println("after rotation: " + newLine);
	    	//lineList.add(newLine);
	    	linesToAdd.add(newLine);
	    	// lineList.add(newLine); naughty! 4.08.09
//	    	System.out.println("newLine: " + newLine);
		//}
		
    	currentX = (float)Pto.getX();
    	currentY = (float)Pto.getY();
	}
    
    public void simpleAddRect(float x, float y, float w, float h)
	{
		float[] comp = getStrokingColor().getRGBColorComponents(null);
		
/////		System.out.println("transparency: "+getStrokingColor().getTransparency());
/////		System.out.println("rect stroke colour: " + comp[0] + " " + comp[1] + " " + comp[2]);
	
		//if (!(comp[0] > 0.9 && comp[1] > 0.9 && comp[2] > 0.9))
		//{
			//rectsToAdd.add(newRect);
			// 11.04.09 funny behaviour owing to reverseYCoordinates
		/*
			RectSegment newRect = new RectSegment(x, x+w, y+h, y+2*h);
			if (h < 0)
				newRect = new RectSegment(x, x+w, y, y-h);
		*/
			RectSegment newRect = new RectSegment(x, x+w, y, y+h);
			//newRect.correctNegativeDimensions(); -- this didn't work right here
			//RectSegment newRect = new RectSegment(x, x+w, y+2*h, y+h);
/////			System.out.println("adding rect: " + newRect);
			
			//if (page.getRotation() != null)
			newRect.rotate(page);
/////			System.out.println("after rotation: " + newRect);
			//rectList.add(newRect);
			newRect.correctNegativeDimensions();
			
			rectsToAdd.add(newRect);
			
			// coordinates already transformed in calling OperatorProcessor method...
			currentX = x;
			currentY = y;
		//}
		//else
		//{
		//	System.out.println("ignored rect");
		//}
	}


    /**
     * TODO: extract byte[] from img and add it to ImageSegment
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param img
     */
	public void simpleDrawImage(float x1, float x2, float y1, float y2, PDXObjectImage img ) {
		ImageSegment newImageSegment = new ImageSegment(x1, x2, y1, y2);
			//(ctm.getXPosition(), ctm.getXPosition() + (float)twh.getX(), 
				//ctm.getYPosition(), ctm.getYPosition() + (float)twh.getY());
/////		System.out.println("adding image segment: " + newImageSegment);
		newImageSegment.correctNegativeDimensions();
		newImageSegment.rotate(page);
/////		System.out.println("image before clipping: " + newImageSegment);
/////		System.out.println("clipBounds: " + clipBounds);
		newImageSegment.shrinkBoundingBox(clipBounds);
		if (!newImageSegment.isZeroSize())
		{
/////			System.out.println("adding image segment");
			imageList.add(newImageSegment);
		}
/////		else System.out.println("not adding image segment");
/////		System.out.println("after rotation: " + newImageSegment);
	}
    
    public void simpleStrokePath()
    {
    	newPath(); // adds contents of last sub-path to toAdd lists
    	
    	// from PageDrawer.java (Ben) hack 4.08.09
    	try {
			graphics.setColor( getGraphicsState().getStrokingColor().getJavaColor() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //per Ben's 11/15 change in StrokePath.java
    	float[] comp = graphics.getColor().getRGBColorComponents(null);

    	//if (true);
		if (!(comp[0] > 0.9 && comp[1] > 0.9 && comp[2] > 0.9))
		{
			//lineList.addAll(linesToAdd);
			//rectList.addAll(rectsToAdd);
			Iterator<LineSegment> llIter = linesToAdd.iterator();
			while(llIter.hasNext()) // forgotten while statement added 4.08.09
			{
				LineSegment ls = llIter.next();
				ls.shrinkBoundingBox(clipBounds);
				if (!ls.isZeroSize())
					lineList.add(ls);
			}
			Iterator<RectSegment> rlIter = rectsToAdd.iterator();
			while(rlIter.hasNext()) // forgotten while statement added 4.08.09
			{
				RectSegment rs = rlIter.next();
				rs.setFilled(false); // is false anyway
				rs.shrinkBoundingBox(clipBounds);
				if (!rs.isZeroSize())
					rectList.add(rs);
			}
		}
		
		// empty toAdd lists
		//newPath();
		endPath();
    }
    
    public void simpleFillPath()
    {
    	newPath(); // adds contents of last sub-path to toAdd lists
    	
    	//float[] comp = getStrokingColor().getRGBColorComponents(null);
    	//System.out.println("rect non stroke colour1: " + comp[0] + " " + comp[1] + " " + comp[2]);
    	// from PageDrawer.java (Ben) hack 4.08.09
    	try {
			graphics.setColor( getGraphicsState().getNonStrokingColor().getJavaColor() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//float[] comp = getNonStrokingColor().getRGBColorComponents(null);
		float[] comp = graphics.getColor().getRGBColorComponents(null);
		//System.out.println("rect non stroke colour2: " + comp[0] + " " + comp[1] + " " + comp[2]);
    	//comp = graphics.getColor().getRGBColorComponents(null);
    	//System.out.println("r " + graphics.getColor().getRed() + " g " + graphics.getColor().getGreen() + " b " + graphics.getColor().getBlue());
    	//System.out.println("rect non stroke colour3: " + comp[0] + " " + comp[1] + " " + comp[2]);
    	
    	
		if (!(comp[0] > 0.9 && comp[1] > 0.9 && comp[2] > 0.9))
		{
			//lineList.addAll(linesToAdd);
			
			
			
			//System.out.println("rectsToAdd: " + rectsToAdd);
			
			Iterator<RectSegment> rlIter = rectsToAdd.iterator(); // in practice, should not be more than one rect#
													// in this list
			while (rlIter.hasNext())
			{
				RectSegment rs = rlIter.next();
				rs.setFilled(true);
/////				System.out.println("rect before shrinking: " + rs);
/////				System.out.println("clipBounds: " + clipBounds);
				rs.shrinkBoundingBox(clipBounds);
				if (!rs.isZeroSize())
					rectList.add(rs);
			}
			
			// TODO: rectangle recognition from lines
			// this part added 4.08.09 (Sydney)
			// TODO: accept only if resembles a rectangle
				// find BBox
				// any lines with points o/s bbox (within error margin) lead to rejection
			RectSegment lineRect = null;
			Iterator lIter = linesToAdd.iterator();
			while(lIter.hasNext())
			{
				LineSegment ls = (LineSegment)lIter.next();
				if (lineRect == null)
					lineRect = new RectSegment
						(ls.getX1(), ls.getX2(), ls.getY1(), ls.getY2());
				// 4.08.09 previously lineRect.getX... got rid of a lot of rubbish...
				else
					lineRect.growBoundingBox(ls);
			}
			if (lineRect != null)
				rectList.add(lineRect);
			// end of addition
			
			
			if (lineRect != null)
			{
//				System.out.println("col: r: " + comp[0] + " g: " + comp[1] + " b: " + comp[2]);
//				System.out.println("adding lineRect: " + lineRect);
			}
			
			//rectList.addAll(rectsToAdd);
		}
		
		// empty toAdd lists
		//newPath();
		endPath();
    }
}