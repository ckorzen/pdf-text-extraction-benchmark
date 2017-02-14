package edu.isi.bmkeg.lapdf.extraction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.isi.bmkeg.lapdf.extraction.exceptions.EmptyPDFException;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.factory.AbstractModelFactory;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLChunk;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLWord;
import edu.isi.bmkeg.utils.FrequencyCounter;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;
import edu.isi.bmkeg.utils.xml.XmlBindingTools;

public class PDFBoxExtractor implements Extractor {

	private static Logger logger = Logger.getLogger(PDFBoxExtractor.class);
	
	Set<WordBlock> wordListPerPage = null;
	LAPDFTextStripper lapdfTextStripper = null;
	int currentPage = 1;
	int pageCount = -1;
	private static Document xmlDocument;
	private static DocumentBuilder docBuilder;

	private static int pageHeight;
	private static int pageWidth;
	private IntegerFrequencyCounter avgHeightFrequencyCounter;
	private Map<Integer,IntegerFrequencyCounter> spaceFrequencyCounterMap;
	
	private AbstractModelFactory modelFactory;
	
	private File currentFile;

	public PDFBoxExtractor(AbstractModelFactory modelFactory)
			throws Exception {

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		docBuilder = dbfac.newDocumentBuilder();
		
		this.modelFactory = modelFactory;
		this.lapdfTextStripper = new LAPDFTextStripper("UTF-8");
		
		this.lapdfTextStripper.setForceParsing( true );
		this.lapdfTextStripper.setSortByPosition( true );
		this.lapdfTextStripper.setShouldSeparateByBeads( true );
			
		this.avgHeightFrequencyCounter = new IntegerFrequencyCounter(1);
		this.spaceFrequencyCounterMap = new HashMap<Integer, IntegerFrequencyCounter>();

	}

	public void init(File file) throws Exception {
		
		this.currentFile = file;

	}
	
	public int getPageCount() {

		if( this.pageCount != -1 ) {
			return this.pageCount;
		}

		int count = this.lapdfTextStripper.document.
				getDocumentCatalog().getAllPages().size();
		
		this.pageCount = count;

		return count;
	
	}

/*	private int[] generatePageBoundaries(PdfPageData currentPageData) {
		
		// 0:TLX, 1:TLY, 2:BRX, 3:BRY
		int[] dimensions = new int[4];

		// Using just cropbox
		if( currentPageData.getCropBoxHeight(currentPage) 
				!= currentPageData.getMediaBoxHeight(currentPage) ) {
			
			dimensions[0] = currentPageData.getCropBoxX(currentPage);
			dimensions[2] = currentPageData.getCropBoxWidth(currentPage)
					+ dimensions[0];

			dimensions[3] = currentPageData.getCropBoxY(currentPage);
			dimensions[1] = currentPageData.getCropBoxHeight(currentPage)
					+ dimensions[3];
			
		} else {
			
			dimensions[0] = currentPageData.getMediaBoxX(currentPage);
			dimensions[2] = currentPageData.getMediaBoxWidth(currentPage)
					+ dimensions[0];

			dimensions[3] = currentPageData.getMediaBoxY(currentPage);
			dimensions[1] = currentPageData.getMediaBoxHeight(currentPage)
					+ dimensions[3];
		
		}
		
		return dimensions;

	}*/

	private String getFontData(String xml, String item)
			throws UnsupportedEncodingException, IOException {
		
		xml = "<root>" + xml + "</root>";

		try {

			xmlDocument = docBuilder.parse(new ByteArrayInputStream(xml
					.getBytes("UTF-8")));
			Element font = (Element) xmlDocument.getElementsByTagName("font").item(
					0);
			return font.getAttribute(item);
			
		} catch (Exception e) {

			return null;

		}

	}

	private boolean decodeFile() throws Exception {
		
		boolean useNonSeqParser = true;
		PDDocument document = null;
        if (useNonSeqParser) 
        {
            document = PDDocument.loadNonSeq(this.currentFile, null, "");
        }
        else
        {
            document = PDDocument.load(this.currentFile.getPath(), true);
            if( document.isEncrypted() )
            {
                StandardDecryptionMaterial sdm = new StandardDecryptionMaterial( "" );
                document.openProtection( sdm );
            }
        }	
        
        //
        // Hijack PDFBox Textstripper to generate our Word-Block XML 
        //
        this.lapdfTextStripper.setStartPage( currentPage );
      	this.lapdfTextStripper.setEndPage( currentPage );
      	String xml = this.lapdfTextStripper.getWordBlocks(document);
      	      	
		if( currentPage > this.getPageCount() ) 
			return false;
      	
      	xml = "<chunk>\n<words>\n" + xml + "\n</words>\n</chunk>";
      	
      	//
      	// Parse this XML to give us RTWordBlocks
      	//
      	StringReader reader = new StringReader(xml);
      	LapdftextXMLChunk chunkHolder = XmlBindingTools.parseXML(reader, LapdftextXMLChunk.class);
      	List<LapdftextXMLWord> words = chunkHolder.getWords();
      			
		int[] dimensions;		
		pageHeight = Math.round(this.lapdfTextStripper.getCurrentPage().findCropBox().getHeight());
		pageWidth = Math.round(this.lapdfTextStripper.getCurrentPage().findCropBox().getWidth());
		
		// If there are no words on the page (a common situation 
		// with splashy handouts), make sure we do not encounter 
		// a null pointer exception here. 
		Iterator<LapdftextXMLWord> wordIterator = (new ArrayList<LapdftextXMLWord>()).iterator();		
		if( words != null ) {
			wordIterator = words.iterator();
		}
		
		if (wordListPerPage == null)
			wordListPerPage = new TreeSet<WordBlock>(
					new SpatialOrdering(SpatialOrdering.MIXED_MODE)
					);
		else {
			wordListPerPage.clear();
		}

		int lastY = -1;
		int lastX = -1;

		int i = 0;
		while (wordIterator.hasNext()) {
			
			LapdftextXMLWord wd = wordIterator.next();

			String font = wd.getFont();
			
			String t = wd.getT();
			
			int wx1 = wd.getX();
			int wy1 = wd.getY();
			int wx2 = wd.getX() + wd.getW();
			int wy2 = wd.getY() + wd.getH();
			
			int h = wy2-wy1;
			this.avgHeightFrequencyCounter.add(h);
			
			//
			// if these words are on the same line, gather statistics about the 
			// spaces between words
			//
			if( lastY == wy2 ) {
				IntegerFrequencyCounter sfc = null;
				if(spaceFrequencyCounterMap.containsKey(h)){
					sfc = this.spaceFrequencyCounterMap.get(h);
				} else {
					sfc = new IntegerFrequencyCounter(1);
					this.spaceFrequencyCounterMap.put(h,sfc);									
				}
				sfc.add(wx1 - lastX);				
			}
			
			lastX = wx2;
			lastY = wy2;
			
			WordBlock wordBlock = modelFactory.createWordBlock(
					wx1, wy1, wx2, wy2, 
					1, font, "", t, i);
			
			if( font == null ) {
				logger.debug("Minor font error for word on pg." + this.currentPage + 
						" in '" + this.currentFile.getName() + "', info:" + 
						wordBlock.toString() + "\n");
			}
			
			wordListPerPage.add(wordBlock);
			i++;

		}
		
		currentPage++;
		
		return true;
	
	}

	@Override
	public boolean hasNext() {

		boolean haveNext = false;
		
		try {

			haveNext = decodeFile();

		} catch (EmptyPDFException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return haveNext;

	}

	public void close() throws IOException {
		 this.lapdfTextStripper.document.close();
	}
	
	@Override
	public List<WordBlock> next() {
		return new ArrayList<WordBlock>(wordListPerPage);
	}

	@Override
	public void remove() {
	}

	@Override
	public int getCurrentPageBoxHeight() {
		return pageHeight;
	}

	@Override
	public int getCurrentPageBoxWidth() {
		return pageWidth;
	}

	@Override
	public IntegerFrequencyCounter getAvgHeightFrequencyCounter() {
		return this.avgHeightFrequencyCounter;
	}

	@Override
	// TODO Not yet built this.
	public FrequencyCounter getFontFrequencyCounter() {
		return null;
	}

	@Override
	public IntegerFrequencyCounter getSpaceFrequencyCounter(int height) {
		return this.spaceFrequencyCounterMap.get(height);
	}
	
}
