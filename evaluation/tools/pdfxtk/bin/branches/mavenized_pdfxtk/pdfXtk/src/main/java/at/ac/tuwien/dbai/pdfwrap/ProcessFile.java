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
package at.ac.tuwien.dbai.pdfwrap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import at.ac.tuwien.dbai.pdfwrap.analysis.PageProcessor;
import at.ac.tuwien.dbai.pdfwrap.exceptions.DocumentProcessingException;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.IXHTMLSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.Page;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyGraph;
import at.ac.tuwien.dbai.pdfwrap.pdfread.PDFObjectExtractor;
import at.ac.tuwien.dbai.pdfwrap.pdfread.PDFPage;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;


/**
 * This is the main program that parses the pdf document and transforms it.
 * Based upon PDFBox code example from Ben Litchfield
 *
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 * @author Ben Litchfield (ben@csh.rit.edu)
 */
public class ProcessFile
{
	// TODO: move somewhere sensible!  this is a global var, at least for GUI
	// moved to GUI 30.11.06
	//public static float XML_RESOLUTION = 150;
	
    private static final Logger LOG = Logger.getLogger( ProcessFile.class );

    /**
     * This is the default encoding of the text to be output.
     */
    public static final String DEFAULT_ENCODING =
        //null;
        //"ISO-8859-1";
        //"ISO-8859-6"; //arabic
        //"US-ASCII";
        "UTF-8";
        //"UTF-16";
        //"UTF-16BE";
        //"UTF-16LE";

    //private static Document resultDocument;
    
    /**
     * The stream to write the output to.
     */
    //protected static Writer output;

    // 27.12.08 changed to public due to GraphMatcher.java
    public static final String PASSWORD = "-password";
    public static final String ENCODING = "-encoding";
    public static final String CONSOLE = "-console";
    public static final String START_PAGE = "-startPage";
    public static final String END_PAGE = "-endPage";
//    public static final String TABLE = "-table";
//    public static final String AUTOTABLE = "-autotable";
    public static final String XMILLUM = "-xmillum";
    public static final String NOBORDERS = "-noborders";
    public static final String PROCESS_SPACES = "-spaces";
    public static final String NORULINGLINES = "-norulinglines";

    /*
    public static String STR_INFILE = "";
    public static String STR_OUTPUT_PATH = ".";
    public static int STR_CURR_PAGE_NO = -1;
    public static final String STR_IMAGE_PREFIX = "-imgPrefix";
    */
    
    /*
     * possible conversions:
     * pdf -> xml, pdf -> xhtml,
     * gecko -> xml, gecko -> xhtml
     */
    
    public static List<Page> processPDF(byte[] theFile, PageProcessor pp, 
    	int startPage, int endPage, String encoding, String password,
    	List<AdjacencyGraph<GenericSegment>> adjGraphList, boolean GUI)
        throws DocumentProcessingException
    {


        boolean toConsole = false;
        if (password == null)
            password = "";
        if (encoding == null || encoding == "")
            encoding = DEFAULT_ENCODING;
        
        if (startPage == 0)
            startPage = 1;
        if (endPage == 0)
            endPage = Integer.MAX_VALUE;
        
        ByteArrayInputStream inStream = new ByteArrayInputStream(theFile);
        PDDocument document = null;
        
        try {
        
        	PDFObjectExtractor extractor = new PDFObjectExtractor();
//          PDDocument document = null;
            document = PDDocument.load( inStream );
    //      document.print();
            if( document.isEncrypted() )
            {
                try
                {
                    document.decrypt( password );
                }
                catch( InvalidPasswordException e )
                {
                    if(!(password == null || password == ""))//they supplied the wrong password
                    {
                        throw new DocumentProcessingException
                            ("Error: The supplied password is incorrect.");
                    }
                    else
                    {
                        //they didn't suppply a password and the default of "" was wrong.
                        throw new DocumentProcessingException
                            ( "Error: The document is encrypted." );
                    }
                } catch (CryptographyException e) {
                    throw new DocumentProcessingException(e);
                }
            }
    
            extractor.setStartPage( startPage );
            extractor.setEndPage( endPage );
            // stripper.writeText( document, output );
            
            List<PDFPage> thePages = extractor.findObjects(document);
            List<Page> theResult = new ArrayList<Page>();
            
            startPage = extractor.getStartPage();
            endPage = extractor.getEndPage();
            
	        // now the DU part
	        
	        Iterator<PDFPage> pageIter = thePages.iterator();
	        int currentPage = -1;
	        while(pageIter.hasNext())
	        {
	        	currentPage ++;
	            PDFPage thePage = pageIter.next();

            	Page resultPage = pp.processPage(thePage);
                theResult.add(resultPage);
                if (adjGraphList != null)
                	adjGraphList.add(pp.getAdjGraph());
	            
	        }
	        
	        // 17.11.10 document-wide processing for headers, footers, etc.
	        if (!GUI)
	        	theResult = pp.processDocPages(theResult, null);
	        
            // move to finally block somewhere?
            if( document != null )
            {
                document.close();
            }
	        return theResult;
        }
        catch (IOException e)
        {
        	e.printStackTrace();
            throw new DocumentProcessingException(e);
        }
        
    }
    
    public static org.w3c.dom.Document processResultPageToXMLDocument
        (Page resultPage, boolean toXHTML, boolean borders)
        throws DocumentProcessingException
    {
        List<Page> theResult = new ArrayList<Page>();
        theResult.add(resultPage);
        return processResultToXMLDocument(theResult, toXHTML, borders);
    }
        
    public static org.w3c.dom.Document processResultToXMLDocument
        (List<Page> theResult, boolean toXHTML, boolean borders)
        throws DocumentProcessingException
    {
        org.w3c.dom.Document resultDocument;
        
        // only used in the case of XHTML
        Element newBodyElement = null;
        Element docElement = null;
        
        // set up the XML file
        try
        {
            if (toXHTML)
            {
                resultDocument = setUpXML("html");
                docElement = resultDocument.getDocumentElement();
                if (borders)
                {
                	// add borders stuff here
                	Element newHeadElement = resultDocument.createElement("head");
                	Element newStyleElement = resultDocument.createElement("style");
                	newStyleElement.setAttribute("type", "text/css");
                	Text newTextElement = resultDocument.createTextNode
            			("table {border-collapse: collapse;}");
                	Text newTextElement2 = resultDocument.createTextNode
                		("td, th {border: 1px solid grey; padding: 2px 4px;}");
                	newStyleElement.appendChild(newTextElement);
                	newStyleElement.appendChild(newTextElement2);
                	newHeadElement.appendChild(newStyleElement);
                	docElement.appendChild(newHeadElement);
                }
                newBodyElement = resultDocument.createElement("body");
            }
            else
            {
                resultDocument = setUpXML("PDFResult");
                docElement = resultDocument.getDocumentElement();
            }
        }
        catch (ParserConfigurationException e)
        {
            throw new DocumentProcessingException(e);
        }
        
        // add the new page element
        //docElement = resultDocument.getDocumentElement();
        
        int pageNo = 0;
        Iterator resultIter = theResult.iterator();
        while(resultIter.hasNext())
        {
        	GenericSegment gs = (GenericSegment)resultIter.next();
        	if (gs instanceof Page)
        	{
	            Page resultPage = (Page)gs;
	            pageNo ++;
	            if (toXHTML)
	            {
	                resultPage.setPageNo(pageNo);
	                resultPage.addAsXHTML(resultDocument, newBodyElement);
	            }
	            else
	            {
	                Element newPageElement = resultDocument.createElement("page");
	                newPageElement.setAttribute("page_number", Integer.toString(pageNo));
	                //we want to use the MediaBox!
	                //resultPage.findBoundingBox();
	                // System.out.println("Result page: " + resultPage);
	                
	                resultPage.addAsXmillum(resultDocument, newPageElement, 
		                	resultPage, Utils.XML_RESOLUTION);
	                
	                docElement.appendChild(newPageElement);
	            }
        	}
        	else if (gs instanceof IXHTMLSegment)//(gs.getClass() == Cluster.class || gs.getClass() == strRasterSegment.class)
        	{
        		IXHTMLSegment c = (IXHTMLSegment)gs;
        		if (toXHTML)
	            {
	                c.addAsXHTML(resultDocument, newBodyElement);
	            }
        		// for XMIllum output, the top-level segment is always a Page
        	}
            // run NG on page
            // output page (cluster-wise) to ontology
        }
    
        if (toXHTML)
            docElement.appendChild(newBodyElement);
        
        return resultDocument;
    }


    /**
     *
     * @param theFile as byte array
     * @param pp bring in the pageProcessor implementation
     * @param toXHTML whether to return xhtml document or XMIllum visualization format
     * @param borders adds border to table cell in output format - works only when toXHTML true
     * @param startPage The first page to start extraction(1 based)
     * @param endPage The last page to extract(inclusive)
     * @param encoding (ISO-8859-1,UTF-16BE,UTF-16LE,...)
     * @param password Password to decrypt document
     *
     * @return new instance of dom document representing the processing results
     * @throws DocumentProcessingException
     */
    public static org.w3c.dom.Document processPDFToXMLDocument(byte[] theFile,
    	PageProcessor pp, boolean toXHTML, boolean borders,
    	int startPage, int endPage, String encoding, String password)
    	throws DocumentProcessingException
    {
    	List<Page> theResult = processPDF(theFile, pp, startPage, endPage, 
    		encoding, password, null, false);
    	
    	return processResultToXMLDocument(theResult, toXHTML, borders);
    }
    
    /*
    public static byte[] processPDFToByteArray(byte[] theFile, 
    	PageProcessor pp, int toXHTML, 
    	int startPage, int endPage, String encoding, String password)
	    throws DocumentProcessingException
	{
    	org.w3c.dom.Document resultDocument;
	    // calls the above and returns a byte[] from the XML Document.
    	List<Page> theResult = processPDF(theFile, pp, startPage, endPage, 
    		encoding, password, null, false);
    	resultDocument = processResultToXMLDocument(theResult, toXHTML, borders);
    	
	    return serializeXML(resultDocument);
	}
	*/

    public static byte[] processPDFToByteArray(byte[] theFile, 
    	PageProcessor pp, boolean toXHTML, boolean borders, 
    	int startPage, int endPage, String encoding, String password)
        throws DocumentProcessingException
    {
        // calls the above and returns a byte[] from the XML Document.
        
        org.w3c.dom.Document resultDocument =
            processPDFToXMLDocument(theFile, pp, toXHTML, borders,
            startPage, endPage, encoding, password);
        
        return serializeXML(resultDocument);
    }
    
    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main(String[] args) throws Exception
    {
        boolean toConsole = false;
//        boolean table = false;
//        boolean autotable = false;
        boolean toXHTML = true;
        boolean borders = true;
        boolean rulingLines = true;
        boolean processSpaces = false;
        int currentArgumentIndex = 0;
        String password = "";
        String encoding = DEFAULT_ENCODING;
        PDFObjectExtractor extractor = new PDFObjectExtractor();
        String inFile = null;
        String outFile = null;
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( ENCODING ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                encoding = args[i];
            }
            else if( args[i].equals( START_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                startPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( END_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                endPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( CONSOLE ) )
            {
                toConsole = true;
            }
            /*
            else if( args[i].equals( AUTOTABLE ))
            {
                autotable = true;
            }
            else if( args[i].equals( TABLE ))
            {
                table = true;
            }
            */
            else if( args[i].equals( NOBORDERS ))
            {
            	borders = false;
            }
            else if( args[i].equals( XMILLUM ) )
            {
                toXHTML = false;
            }
            else if( args[i].equals( NORULINGLINES ))
            {
            	rulingLines = false;
            }
            else if( args[i].equals( PROCESS_SPACES ))
            {
            	processSpaces = false;
            }
            else
            {
                if( inFile == null )
                {
                    inFile = args[i];
                }
                else
                {
                    outFile = args[i];
                }
            }
        }

        if( inFile == null )
        {
            usage();
        }

        if( outFile == null && inFile.length() >4 )
        {
            outFile = inFile.substring( 0, inFile.length() -4 ) + ".txt";
        }
        
        // decide whether we have a pdf or image (TODO: command-line override)
        /*
        boolean pdf = true;
		if (inFile.endsWith("png") ||
			inFile.endsWith("tif") ||
			inFile.endsWith("tiff")||
			inFile.endsWith("jpg") ||
			inFile.endsWith("jpeg")||
			inFile.endsWith("PNG") ||
			inFile.endsWith("TIF") ||
			inFile.endsWith("TIFF") ||
			inFile.endsWith("JPG") ||
			inFile.endsWith("JPEG")) pdf = false;
		*/
        
//		System.err.println("Processing: " + inFile);
		
        // load the input file
        File inputFile = new File(inFile);
        /*
        STR_INFILE = inputFile.getCanonicalPath();
        File tempOutFile = new File(outFile); // tmp for str only
        if (tempOutFile.getParent() != null)
        	STR_OUTPUT_PATH = tempOutFile.getParent();
        */
        byte[] inputDoc = getBytesFromFile(inputFile);
        
        org.w3c.dom.Document resultDocument = null;
        
        // set up page processor object
        PageProcessor pp = new PageProcessor();
        pp.setProcessType(PageProcessor.PP_BLOCK);
        pp.setRulingLines(rulingLines);
        pp.setProcessSpaces(processSpaces);
        // no iterations should be automatically set to -1
        
        // do the processing
    	resultDocument =
    		processPDFToXMLDocument(inputDoc, pp, toXHTML, borders,
    		startPage, endPage, encoding, password);
    	
        // now output the XML Document by serializing it to output
        Writer output = null;
        if( toConsole )
        {
            output = new OutputStreamWriter( System.out );
        }
        else
        {
            if( encoding != null )
            {
                output = new OutputStreamWriter(
                    new FileOutputStream( outFile ), encoding );
            }
            else
            {
                //use default encoding
                output = new OutputStreamWriter(
                    new FileOutputStream( outFile ) );
            }
            //System.out.println("using out put file: " + outFile);
        }
        //System.out.println("resultDocument: " + resultDocument);
        serializeXML(resultDocument, output);
        
        if( output != null )
        {
            output.close();
        }
    }
    
    public static byte[] PDFToXHTML(byte[] theFile, 
    	int startPage, int endPage, String encoding, String password)
        throws DocumentProcessingException
    {
    	PageProcessor pp = new PageProcessor(PageProcessor.PP_BLOCK);
    	
        return processPDFToByteArray(theFile, pp, true, true,
            startPage, endPage, encoding, password);
    }
    
    // try/catch moved to calling method 9.04.06
    protected static org.w3c.dom.Document setUpXML(String nodeName) 
        throws ParserConfigurationException
    {
        //try
        //{
            DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDocBuilder = myFactory.newDocumentBuilder();
            DOMImplementation myDOMImpl = myDocBuilder.getDOMImplementation();
            // resultDocument = myDOMImpl.createDocument("at.ac.tuwien.dbai.pdfwrap", "PDFResult", null);
            org.w3c.dom.Document resultDocument = 
                myDOMImpl.createDocument("at.ac.tuwien.dbai.pdfwrap", nodeName, null);
            return resultDocument;
        //}
        //catch (ParserConfigurationException e)
        //{
         //   e.printStackTrace();
         //   return null;
        //}
        
    }
    
//  Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
    public static byte[] serializeXML(org.w3c.dom.Document resultDocument)
        throws DocumentProcessingException
    {
        // calls the above and returns a byte[] from the XML Document.
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        
        try
        {
        	Writer output = new OutputStreamWriter(outStream, DEFAULT_ENCODING);
            serializeXML(resultDocument, output);
        }
        catch (IOException e)
        {
        	throw new DocumentProcessingException(e);
    	}

        return outStream.toByteArray();
    }
    
    public static void serializeXML(org.w3c.dom.Document resultDocument, OutputStream outStream)
        throws DocumentProcessingException
    {
        try
        {
        	Writer output = new OutputStreamWriter(outStream, DEFAULT_ENCODING);
            serializeXML(resultDocument, output);
        }
        catch (IOException e)
        {
        	throw new DocumentProcessingException(e);
    	}
    }
    
    public static void serializeXML
        (org.w3c.dom.Document resultDocument, Writer output)
        throws IOException
    {
        // The third parameter in the constructor method for
        // _OutputFormat_ controls whether indenting should be
        // used.  Unfortunately, I have found some bugs in the
        // indenting implementation that have corrupted the text
        // so I have switched it off. 
         
        OutputFormat myOutputFormat =
            new OutputFormat(resultDocument,
                             "UTF-8",
                             true);

        // output used to be replaced with System.out
        XMLSerializer s = 
        new XMLSerializer(output, 
                              myOutputFormat);

        try {
        s.serialize(resultDocument);
        // next line added by THA 21.03.05
        output.flush();
        }
        catch (IOException e) {
            System.err.println("Couldn't serialize document: "+
               e.getMessage());
            throw e;
        }        

         // end of addition
    }
    
    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java at.ac.tuwien.dbai.pdfwrap.ProcessFile [OPTIONS] <PDF file> [Text File]\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n" +
            "  -xmillum                     output XMIllum XML (instead of XHTML)\n" +
            "  -norulinglines               do not process ruling lines\n" +
            "  -spaces                      split low-level segments according to spaces\n" +
            "  -console                     Send text to console instead of file\n" +
            "  -startPage <number>          The first page to start extraction(1 based)\n" +
            "  -endPage <number>            The last page to extract(inclusive)\n" +
            "  <PDF file>                   The PDF document to use\n" +
            "  [Text File]                  The file to write the text to\n"
            );
        System.exit( 1 );
    }
}

// the above taken from: 
// http://userpage.fu-berlin.de/~ram/pub/pub_jf47htqHHt/java_sax_parser_en

/** utility class */

final class XML
{ /** create a new XML reader */
  final public static org.xml.sax.XMLReader makeXMLReader()  
  throws Exception 
  { final javax.xml.parsers.SAXParserFactory saxParserFactory   =  
    javax.xml.parsers.SAXParserFactory.newInstance(); 
    final javax.xml.parsers.SAXParser        saxParser = saxParserFactory.newSAXParser(); 
    final org.xml.sax.XMLReader              parser    = saxParser.getXMLReader(); 
    return parser; }}

