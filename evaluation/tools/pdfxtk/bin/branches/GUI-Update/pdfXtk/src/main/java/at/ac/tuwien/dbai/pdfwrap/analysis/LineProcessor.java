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
package at.ac.tuwien.dbai.pdfwrap.analysis;

import at.ac.tuwien.dbai.pdfwrap.comparators.XYTextComparator;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Methods to find lines of text from fragments on a page
 * and within text blocks/candidate clusters
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class LineProcessor
{
	
	// (take LineFragment -- find line)
	// take TextFragment OR any other TextSegment(e.g. Char) -- create LineFragments and Lines
	
	// public static ...
	public static List<TextLine> findLinesFromLineFragments(List<LineFragment> textBlocks, 
		float maxX, boolean postNG, boolean ignoreFontsize)
	{
		List<TextLine> retVal = new ArrayList<TextLine>();
		
		List<CompositeSegment<? extends TextSegment>> foundLines = 
			findLines(textBlocks, maxX, postNG, ignoreFontsize);
		
		for (CompositeSegment<? extends TextSegment> cs : foundLines)
		{
			TextLine tl = new TextLine();
			tl.getItems().addAll((List<? extends LineFragment>) cs.getItems());
			tl.setCalculatedFields(cs);
			
			retVal.add(tl);
		}
		
		return retVal;
	}
	
	public static List<TextLine> findLinesFromTextFragments(List<TextFragment> textBlocks, 
		float maxX, boolean postNG, boolean ignoreFontsize)
	{
		List<TextLine> retVal = new ArrayList<TextLine>();
		
		List<CompositeSegment<? extends TextSegment>> foundLines = 
			findLines(textBlocks, maxX, postNG, ignoreFontsize);
		
		for (CompositeSegment<? extends TextSegment> cs : foundLines)
		{
			LineFragment lf = new LineFragment();
			lf.getItems().addAll((List<? extends TextFragment>) cs.getItems());
			lf.setCalculatedFields(cs);
			
			TextLine tl = new TextLine();
			tl.getItems().add(lf);
			tl.setCalculatedFields(lf);
			
			retVal.add(tl);
		}
		
		return retVal;
	}
	
	public static List<TextLine> findLinesFromCharacters(List<CharSegment> textBlocks, 
		float maxX, boolean postNG, boolean ignoreFontsize)
	{
		List<TextLine> retVal = new ArrayList<TextLine>();
		
		List<CompositeSegment<? extends TextSegment>> foundLines = 
			findLines(textBlocks, maxX, postNG, ignoreFontsize);
		
		for (CompositeSegment<? extends TextSegment> cs : foundLines)
		{
			TextFragment tf = new TextFragment();
			tf.getItems().addAll((List<? extends CharSegment>) cs.getItems());
			tf.setCalculatedFields(cs);
			
			LineFragment lf = new LineFragment();
			lf.getItems().add(tf);
			lf.setCalculatedFields(tf);
			
			TextLine tl = new TextLine();
			tl.getItems().add(lf);
			tl.setCalculatedFields(lf);
			
			retVal.add(tl);
		}
		
		return retVal;
	}
	
	public static List<TextLine> findLinesFromTextLines(List<TextLine> textBlocks, 
		float maxX, boolean postNG, boolean ignoreFontsize)
	{
		List<TextLine> retVal = new ArrayList<TextLine>();
		
		List<CompositeSegment<? extends TextSegment>> foundLines = 
			findLines(textBlocks, maxX, postNG, ignoreFontsize);
		
		for (CompositeSegment<? extends TextSegment> cs : foundLines)
		{
			TextLine tl = new TextLine();
			for (TextSegment ts : cs.getItems())
			{
				TextLine tl2 = (TextLine)ts;
				tl.getItems().addAll(tl2.getItems());
			}
			tl.setCalculatedFields(cs);
			
			retVal.add(tl);
		}
		
		return retVal;
	}
	
	// 2011-01-26: changed to public -- called directly by CandidateCluster.findLines()
    public static List<CompositeSegment<? extends TextSegment>> findLines(
    	List<? extends TextSegment> textBlocks, float maxX, boolean postNG, boolean ignoreFontsize) //throws Exception
    {
    	// TODO: support super/subscript natively -- or allow misc segments ...
    	
        // pre: textBlocks in collection must be sorted in y-then-x order
        Collections.sort(textBlocks, new XYTextComparator());
        
        // pre: all items in textBlocks must be TextPosition objects
        // TODO: create a specific exception here
        
        List<CompositeSegment<? extends TextSegment>> retVal = 
        	new ArrayList<CompositeSegment<? extends TextSegment>>();
        
        TextSegment lastBlock = null;
        List<TextSegment> newItems = new ArrayList<TextSegment>();
        
        // variables for controlling new line objects to be added
        // these can be generated later -- not for the preNG cluster
        //String newString = "";
        
        boolean merge = false;
        
        Iterator iter = textBlocks.iterator();
        while (iter.hasNext())
        {
            TextSegment thisBlock = null;
            
            // if empty text block, try again :)
            // (required so that empty text blocks do not interfere with processing)
            while (iter.hasNext() && (thisBlock == null || thisBlock.isEmpty()))
            {
                thisBlock = (TextSegment)iter.next();
            }
            
            if (lastBlock != null)
            {
            	
                // should return null if no lastBlock...?
                if (sameLine(lastBlock, thisBlock, maxX, postNG, ignoreFontsize)) // we "merge"
                {
                    // TODO: delete!
                    // System.out.println("merging " + newString + " withspace " + thisBlock.getCharacter());
                    if (merge)
                    {
                        newItems.add(thisBlock);
                    }
                    else
                    {
                        newItems = new ArrayList<TextSegment>();
                        newItems.add(thisBlock);
                        merge = true;
                    }
                }
                else // we don't merge
                {
                    // TODO: add all sub-objects, and fix font! (not null)
                	CompositeSegment<TextSegment> newLine = new CompositeSegment<TextSegment>();
                    newLine.setItems(newItems);
                    newLine.setCalculatedFields();
                    retVal.add(newLine);
                    
                    // nothing to merge with =>
                    // simply assign all new variables
                    newItems = new ArrayList<TextSegment>();
                    newItems.add(thisBlock);
                    // TODO: replace with a proper average (mode?)
                    // newFontSize = fontSize;
                    //first = false;
                    merge = true;
                }                           
                
            }
            else
            {
	            // nothing to merge with =>
	            // simply assign all new variables
	            newItems = new ArrayList<TextSegment>();
	            newItems.add(thisBlock);
	            // TODO: replace with a proper average (mode?)
	            // newFontSize = fontSize;
	            //first = false;
	            merge = true;
            }
            lastBlock = thisBlock;
            //first = false;
        }
        
        // add last block if appropriate
        
        if (newItems.size() > 0)
        {
            CompositeSegment<TextSegment> newLine = new CompositeSegment<TextSegment>();
            newLine.setItems(newItems);
            newLine.setCalculatedFields();
            retVal.add(newLine);
        }
        
        // 2011-05-28: lines; fontsize should be the maximum of all blocks!
        for (CompositeSegment<? extends TextSegment> l : retVal)
        {
        	float largestFontSize = 0.0f;
        	boolean changeMade = false;
        	for (GenericSegment gs : l.getItems())
        	{
        		if (gs instanceof TextSegment)
        		{
        			float thisFontSize = ((TextSegment)gs).getFontSize();
        			if (thisFontSize > largestFontSize)
        			{
        				largestFontSize = thisFontSize;
        				changeMade = true;
        			}
        		}
        	}
        	if (changeMade) l.setFontSize(largestFontSize);
        }
        
        return retVal;
    }

    // TODO: rewrite to make clearer -- it all works with TextSegments now!
    private static boolean sameLine(TextSegment lastBlock, TextSegment thisBlock, float maxX, boolean postNG, boolean ignoreFontsize)
    {
    	// added 12.06.07
    	if(thisBlock.getX1() < lastBlock.getXmid()) return false;
    	
    	if (postNG) return (SegmentUtils.vertIntersect(lastBlock, thisBlock.getYmid()) ||
    		SegmentUtils.vertIntersect(thisBlock, lastBlock.getYmid()));
    		// problem with atomic line finding on tm_03dec08_p04z.pdf
    		// changed 4.05.09
    		//GenericSegment.vertIntersect(lastBlock, thisBlock);
    	
        float fontSize;
        boolean sameFontSize;
        boolean xGuard;
        
        if (lastBlock instanceof TextSegment && thisBlock instanceof TextSegment)
        {
            fontSize = (lastBlock.getFontSize() + 
                thisBlock.getFontSize()) / 2.0f;
            sameFontSize = Utils.within(lastBlock.getFontSize(), 
                    thisBlock.getFontSize(), 
                    fontSize * 0.15f);
            //System.out.println("fontSize: " + fontSize + " maxX: " + maxX + " product: " + fontSize * maxX);
            xGuard = Utils.within(lastBlock.getX2(), thisBlock.getX1(), fontSize * maxX);
        }
        else
        {
            // completely nonsensical to line-find on GenericSegments(!)
            fontSize = -1.0f;
            sameFontSize = false;
            xGuard = false;
        }
        
        if (ignoreFontsize) sameFontSize = true;
        
        // for PDF-TREX comparison was 0.1f; later changed to 0.25f
        return (Utils.within(lastBlock.getY1(), thisBlock.getY1(), fontSize * Utils.sameLineTolerance)
                //&& !crosses(lastBlock, thisBlock, pageDivs)
                && sameFontSize
                && xGuard);
    }
}
