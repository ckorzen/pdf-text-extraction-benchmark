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
package at.ac.tuwien.dbai.pdfwrap.utils;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;

/**
 * static utility methods used throughout the project
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public class Utils
{
	public static boolean IS_OCR = false;
	
	public final static String EMPTY_STRING = "";
	
//	in GUI now
//	public final static boolean DISPLAY_NAG_SCREEN = false;
//	public final static boolean DISPLAY_INSTRUCTIONS = false;
	public final static boolean DISPLAY_TIMINGS = true;
	
	public final static float PDF_POINT_RESOLUTION = 72;
	public final static float XML_RESOLUTION = 300;
	
//	todo: replace with logical neighbourhood!
	public final static float neighbourLOSMin = 0.4f;
	public final static float neighbourOverlapTolerance = 0.2f;
	
	// float dividerRatio = 0.93f;
//	in GUI now
//	public final static float dividerRatio = 0.9f;
//	public final static boolean horizToolbar = false;
//	public final static boolean showWrapperPanel = false;
//	public final static boolean standardLookAndFeel = false;
	
	public final static float sameLineTolerance = 0.3f;
	
	// from DocGBPanel.java
	public static RenderingHints hints;
	static {
	    hints = new RenderingHints(null);
	    // fractional metrics distorts text spacing; not for this application!
	    hints.put(RenderingHints.KEY_FRACTIONALMETRICS,   RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    //hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	    hints.put(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_SPEED);
	    hints.put(RenderingHints.KEY_ANTIALIASING ,       RenderingHints.VALUE_ANTIALIAS_ON);
	    //hints.put(RenderingHints.KEY_ANTIALIASING ,       RenderingHints.VALUE_ANTIALIAS_OFF);
	    hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
	    hints.put(RenderingHints.KEY_COLOR_RENDERING,     RenderingHints.VALUE_COLOR_RENDER_SPEED);
	    hints.put(RenderingHints.KEY_DITHERING,           RenderingHints.VALUE_DITHER_DISABLE);
	}
	
	// for robert demo
	//public final static boolean standardLookAndFeel = true;
	//public final static boolean horizToolbar = true;
	//public final static boolean showWrapperPanel = true;
	
	public static String getRootDir() throws IOException
	{
//		We want to return the root directory of the program!
//		return "/home/tam/eclipse-workspace-str/PDFAnalyser-1.1-os";
		File currentDir = new File(".");
//		System.out.println(currentDir.getCanonicalPath());
    	return currentDir.getCanonicalPath();
	}
	
	public static float gaussian(float x, float mean, float sd)
	{
		float mu = mean;
		float sigma = sd;

		float k1 = (float) ((float) 1 / (sigma * (Math.sqrt(2 * Math.PI))));
		float k2 = -1 / (2 * (sigma * sigma));

		return (float) (k1 * Math.exp((x - mu) * (x - mu) * k2));
	}

	public static float normgaussian(float x, float mean, float sd)
	{
		return gaussian(x, mean, sd) / gaussian(mean, mean, sd);
	}

	// finds the midpoint of the two given numbers
	public static float avg(float num1, float num2)
	{
		return (num1 + num2) / 2.0f;
	}
	
	// 23.08.10: The following methods from http://snippets.dzone.com/posts/show/2936
	
	public static BufferedImage rotate90CW(BufferedImage bi) // -90deg
	{
		int width = bi.getWidth();
		int height = bi.getHeight();
		
		// changed 14.12.10
		BufferedImage biFlip = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);//bi.getType());
		
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				biFlip.setRGB(j, width-1-i, bi.getRGB(i, j));
//				biFlip.setRGB(height-1-j, width-1-i, bi.getRGB(i, j)); -- this is a flip!
		
		return biFlip;
	}

	
	public static BufferedImage rotate90ACW(BufferedImage bi) // 90deg
	{
		int width = bi.getWidth();
		int height = bi.getHeight();
		
		BufferedImage biFlip = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);//bi.getType());
		
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				biFlip.setRGB(height-1-j, i, bi.getRGB(i, j));
//				biFlip.setRGB(j, i, bi.getRGB(i, j));
		
		return biFlip;
	}

	
	// finds the number with the highest frequency (occurrence)
	// pre: only FLOATS in the list! (i.e. Float objects)
	public static float findMode(List floats, float tolerance)
	{
		Iterator numIter = floats.iterator();
		ArrayList values = new ArrayList();
		ArrayList frequencies = new ArrayList();

		while (numIter.hasNext())
		{
			Float thisObj = (Float) numIter.next();
			float thisVal = thisObj.floatValue();

			// now, find which index it has in the existing array
			int arrayIndex = -1;
			for (int n = 0; n < values.size(); n++)
			{
				// get value at n
				float valAtN = ((Float) values.get(n)).floatValue();

				if (within(thisVal, valAtN, tolerance))
				{
					// break out of loop
					arrayIndex = n;
					n = values.size();
				}
			}

			// if already in array, increment
			if (arrayIndex != -1)
			{
				int frequency = ((Integer) (frequencies.get(arrayIndex)))
					.intValue();
				frequency++;
				frequencies.set(arrayIndex, new Integer(frequency));
			} else
			// add to both lists
			{
				values.add(thisObj);
				frequencies.add(new Integer(1));
			}
		}

		// now we have a list of (approx) values and frequencies,
		// we just need to return the most 'popular' one
		int highestIndex = 0;
		int highestValue = -1;
		for (int n = 0; n < frequencies.size(); n++)
		{
			if (((Integer) frequencies.get(n)).intValue() > highestValue)
			{
				highestValue = ((Integer) frequencies.get(n)).intValue();
				highestIndex = n;
			}
		}

		return ((Float) values.get(highestIndex)).floatValue();
	}

	public static int findModalFreq(List floats, float tolerance)
	{
		Iterator numIter = floats.iterator();
		ArrayList values = new ArrayList();
		ArrayList frequencies = new ArrayList();

		while (numIter.hasNext())
		{
			Float thisObj = (Float) numIter.next();
			float thisVal = thisObj.floatValue();

			// now, find which index it has in the existing array
			int arrayIndex = -1;
			for (int n = 0; n < values.size(); n++)
			{
				// get value at n
				float valAtN = ((Float) values.get(n)).floatValue();

				if (within(thisVal, valAtN, tolerance))
				{
					// break out of loop
					arrayIndex = n;
					n = values.size();
				}
			}

			// if already in array, increment
			if (arrayIndex != -1)
			{
				int frequency = ((Integer) (frequencies.get(arrayIndex)))
					.intValue();
				frequency++;
				frequencies.set(arrayIndex, new Integer(frequency));
			} else
			// add to both lists
			{
				values.add(thisObj);
				frequencies.add(new Integer(1));
			}
		}

		// now we have a list of (approx) values and frequencies,
		// we just need to return the most 'popular' one
		int highestIndex = 0;
		int highestValue = -1;
		for (int n = 0; n < frequencies.size(); n++)
		{
			if (((Integer) frequencies.get(n)).intValue() > highestValue)
			{
				highestValue = ((Integer) frequencies.get(n)).intValue();
				highestIndex = n;
			}
		}

		return highestValue;
	}
	
	// is used at all?
	private int findModalFontSize(Collection textBlocks) throws Exception
	{
		// pre: all items in textBlocks must be TextPosition objects
		// TODO: create a specific exception here
		
		// will count font sizes 0..96pt inclusive, rounding down to nearest integer
		int[] count = new int[96];
		
		Iterator textIter = textBlocks.iterator();
		
		while (textIter.hasNext())
		{
			TextSegment thisBlock = null;
			try
			{
				// if empty text block, try again :)
				// (required so that empty text blocks do not interfere with processing)
				while (textIter.hasNext() && (thisBlock == null || thisBlock.isEmpty()))
				{
					thisBlock = (TextSegment)textIter.next();
				}
			}
			catch (java.lang.ClassCastException e)
			{
				throw new Exception("Objects in the collection must be of type TextSegment.");
			}			
			
			if (thisBlock != null && thisBlock.getFontSize() > 0 
				&& thisBlock.getFontSize() <= 96) 
				count[new Double(thisBlock.getFontSize()).intValue()] ++;
		}
		
		// loop through and find the highest
		// if more than one mode, return the lowest
		
		int highest = 0;
		
		for (int n = 0; n < count.length; n ++)
		{
			if (count[n] > count[highest])
				highest = n;
		}
		
		return highest;
    }

	public static int findIndexOfHighestValuedObject(List l)
	{
		int retVal = -1;
		Object highestObject = null;
		
		for (int n = 0; n < l.size(); n ++)
		{
			if (highestObject == null)
			{
				highestObject = l.get(n);
				retVal = 0;
			}
			else
			{
				if (((Comparable)l.get(n)).compareTo(highestObject) > 0)
				{
					highestObject = l.get(n);
					retVal = n;
				}
			}
		}
		if (retVal == -1) System.err.println("returning -1 with " + l.size() + " items");
		return retVal;
	}
	
	public static boolean sameFontSize(TextSegment seg1, TextSegment seg2)
	{
		// TODO: maybe make it 10% of the smallest of the two?
		//return within(seg1.getFontSize(), seg2.getFontSize(), seg1.getFontSize() * 0.25f);
		float afs = (seg1.getFontSize() + seg2.getFontSize()) / 2.0f;
		if (!IS_OCR)
			return within(seg1.getFontSize(), seg2.getFontSize(), afs * 0.1f);
		else
			return within(seg1.getFontSize(), seg2.getFontSize(), afs * 0.50f);
	}
	
	// TODO: MOVE to another (utility) method!
	public static boolean within(float first, float second, float variance)
	{
		return second > first - variance && second < first + variance;
	}
	
	public static boolean between(float number, float boundary1, float boundary2)
	{
		return ((number >= boundary1 && number <= boundary2) ||
				(number <= boundary1 && number >= boundary2));
	}
	
	public static float minimum(float first, float second)
    {
    	if (first < second)
    		return first;
    	else
    		return second;
    }
    
    public static float maximum(float first, float second)
    {
    	if (first < second)
    		return second;
    	else
    		return first;
    }
    
    public static float calculateThreshold(TextSegment seg1, TextSegment seg2, float multiple)
    {
    	return(minimum(seg1.getFontSize(), seg2.getFontSize()) * multiple);
    }
    
    public static String stripClassName(String fullName)
    {
    	String retVal = new String();
    	for (int n = fullName.length() - 1; n >= 0; n --)
    	{
    		String thisChar = fullName.substring(n, n + 1);
    		if (!thisChar.equals("."))
    		{
    			retVal = thisChar.concat(retVal);
    		}
    		else
    		{
    			n = -1;
    		}
    	}
    	return retVal;
    }
    
    public static String replaceBackslashes(String inputString)
    {
    	String retVal = new String();
    	for (int n = 0; n < inputString.length(); n ++)
    	{
    		String thisChar = inputString.substring(n, n + 1);
    		if (thisChar.equals("\\"))
    		{
    			retVal = retVal.concat("/");
    		}
    		else
    		{
    			retVal = retVal.concat(thisChar);
    		}
    	}
    	return retVal;
    }
    
    // adapted from http://www.javadb.com/check-if-string-contains-another-string
    public static boolean containsSubstring
    	(String test, String substring) 
    {
        int index1 = test.indexOf(substring);
        return (index1 != -1);
    }

    // the following taken from: http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
    /**
     * This method ensures that the output String has only valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see the
     * standard. This method will return an empty String if the input is null or empty.
     *
     * @author Donoiu Cristian, GPL
     * @param  The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String removeInvalidXMLCharacters(String s) {
        StringBuilder out = new StringBuilder();                // Used to hold the output.
    	int codePoint;                                          // Used to reference the current character.
    	//String ss = "\ud801\udc00";                           // This is actualy one unicode character, represented by two code units!!!.
    	//System.out.println(ss.codePointCount(0, ss.length()));// See: 1
		int i=0;
    	while(i<s.length()) {
    		//System.out.println("i=" + i);
    		codePoint = s.codePointAt(i);                       // This is the unicode code of the character.
			if ((codePoint == 0x9) ||          				    // Consider testing larger ranges first to improve speed. 
					(codePoint == 0xA) ||
					(codePoint == 0xD) ||
					((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
					((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
					((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
				out.append(Character.toChars(codePoint));
			}				
			i+= Character.charCount(codePoint);                 // Increment with the number of code units(java chars) needed to represent a Unicode char.  
    	}
    	return out.toString();
    } 
    
    // http://java.itags.org/java-tech/36144/
    // 12.11.10 DOES NOT WORK!
    public static BufferedImage convertToBinary(BufferedImage sourceImg){

    	double[][] matrix = {{ 0.3D, 0.59D, 0.11D, 0D }};

    	ParameterBlock pb = new ParameterBlock();

    	pb.addSource(sourceImg);

    	pb.add(matrix);

    	PlanarImage src = JAI.create("BandCombine", pb, null);

    	// Generate a histogram.

    	Histogram histogram = (Histogram)JAI.create("histogram", src).getProperty("histogram");

    	// Get a threshold equal to the median.

    	double[] threshold = histogram.getPTileThreshold(0.5);

    	// Binarize the image.

    	PlanarImage dst = JAI.create("binarize", src, new Double(threshold[0]));

    	return dst.getAsBufferedImage(); 
    }
    
    public static int oppositeDirection(int direction)
	{
		if (direction == AdjacencyEdge.REL_ABOVE)
			return AdjacencyEdge.REL_BELOW;
		else if (direction == AdjacencyEdge.REL_BELOW)
			return AdjacencyEdge.REL_ABOVE;
		else if (direction == AdjacencyEdge.REL_LEFT)
			return AdjacencyEdge.REL_RIGHT;
		else if (direction == AdjacencyEdge.REL_RIGHT)
			return AdjacencyEdge.REL_LEFT;
		else return -1;
	}
    
    public static void executeCommand(String s, String stdout, String stderr) throws IOException
    {
//    	System.out.println("Executing: " + s);

		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(s);

		// System.out.println("one...");

		// this code from diffpdf...
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(p
				.getErrorStream());

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(p
				.getInputStream());

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// System.out.println("two...");

		// any error???
		try {
			int exitVal = p.waitFor();
			// System.out.println("fooo...");
			// int exitVal2 = proc.exitValue();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		// System.out.println("three...");

		// 15.03.07 these two lines uncommented
		// as there was a process still running!
		// processor utilisation in the GUI is now back down to
		// normal
		stdout = errorGobbler.getData();
		stderr = outputGobbler.getData();
//		System.out.println("stdOut: " + stdout);
//		System.err.println("stdErr: " + stderr);
    }

    // 22.10.10 -- copied from above method, only with sa argument...
	public static void executeCommand(String[] sa, String stdout, String stderr) throws IOException
	{
//		System.out.println("Executing: " + sa);
	
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(sa);
	
		// System.out.println("one...");
	
		// this code from diffpdf...
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(p
				.getErrorStream());
	
		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(p
				.getInputStream());
	
		// kick them off
		errorGobbler.start();
		outputGobbler.start();
	
		// System.out.println("two...");
	
		// any error???
		try {
			int exitVal = p.waitFor();
			// System.out.println("fooo...");
			// int exitVal2 = proc.exitValue();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	
		// System.out.println("three...");
	
		// 15.03.07 these two lines uncommented
		// as there was a process still running!
		// processor utilisation in the GUI is now back down to
		// normal
		stdout = errorGobbler.getData();
		stderr = outputGobbler.getData();
		System.out.println(stdout);
		System.err.println(stderr);
	}

}

class StreamGobbler extends Thread
//credit to http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
//for this code
{
	InputStream is;
	String type;
	String data;
	boolean noMoreData = false;
	boolean finished = false;

	StreamGobbler(InputStream is) {
		this.is = is;
		// this.type = type;
		this.data = "";
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			// while (true)
			while (!finished) {
				while ((line = br.readLine()) != null) {
					noMoreData = false;
					data = data + "\n" + line;
				}
				noMoreData = true;
			}
			is.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// System.err.println("reached end!");
		// return;
	}

	public String getData() throws IOException {
		// pre: process that we are gobbling has now finished
		while (noMoreData == false) {
			System.out.print("");
			// System.out.println("still data to be gobbled! " + this);
			// wait for gobbler to finish gobbling anything that
			// might be left in the buffer
		}
		finished = true;
		return data;
	}
}
