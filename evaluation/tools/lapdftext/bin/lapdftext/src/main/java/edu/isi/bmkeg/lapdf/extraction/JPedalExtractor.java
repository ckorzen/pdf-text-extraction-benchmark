package edu.isi.bmkeg.lapdf.extraction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
import org.jpedal.PdfDecoder;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Strip;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.isi.bmkeg.lapdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.EmptyPDFException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.EncryptionException;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.factory.AbstractModelFactory;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.utils.FrequencyCounter;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;

public class JPedalExtractor implements Extractor {

	private static Logger logger = Logger.getLogger(JPedalExtractor.class);
	
	Set<WordBlock> wordListPerPage = null;
	PdfDecoder PDFDecoder = null;
	int currentPage = 1;
	int pageCount;
	private static Document xmlDocument;
	private static DocumentBuilder docBuilder;

	private static int pageHeight;
	private static int pageWidth;
	private IntegerFrequencyCounter avgHeightFrequencyCounter;
	private Map<Integer,IntegerFrequencyCounter> spaceFrequencyCounterMap;
	
	private AbstractModelFactory modelFactory;
	
	private File currentFile;

	public JPedalExtractor(AbstractModelFactory modelFactory)
			throws Exception {

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		docBuilder = dbfac.newDocumentBuilder();
		
		this.modelFactory = modelFactory;
		this.PDFDecoder = new PdfDecoder(false);
		
		this.avgHeightFrequencyCounter = new IntegerFrequencyCounter(1);
		this.spaceFrequencyCounterMap = new HashMap<Integer, IntegerFrequencyCounter>();

		PDFDecoder.setExtractionMode(PdfDecoder.TEXT); 
		PDFDecoder.init(true);
		PdfGroupingAlgorithms.useUnrotatedCoords = true;
		
		// if you do not require XML content, 
		// pure text extraction is much faster.
		PDFDecoder.useXMLExtraction();

	}

	public void init(File file) throws Exception {
		
		if (PDFDecoder.isOpen()) {
			PDFDecoder.flushObjectValues(true);
			PDFDecoder.closePdfFile();
		}
		
		this.currentFile = file;

		PDFDecoder.openPdfFile(file.getPath());
		currentPage = 1;
		pageCount = PDFDecoder.getPageCount();
		if (!PDFDecoder.isExtractionAllowed()) {
			throw new AccessException(file.getPath());
		} else if (PDFDecoder.isEncrypted()) {
			throw new EncryptionException(file.getPath());
		}

	}

	private int[] generatePageBoundaries(PdfPageData currentPageData) {
		
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

	}

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

		String font = null;
		String currentWord;
		String style = null;

		PDFDecoder.decodePage(currentPage);
		
		PdfGroupingAlgorithms currentGrouping = PDFDecoder.getGroupingObject();

		PdfPageData currentPageData = PDFDecoder.getPdfPageData();
		int[] dimensions;

		// pageHeight.add(currentPageData.getCropBoxHeight(page));
		// pageWidth.add(currentPageData.getCropBoxWidth(page));

		dimensions = generatePageBoundaries(currentPageData);
		pageWidth = Math.abs(dimensions[2] - dimensions[0]);
		pageHeight = Math.abs(dimensions[1] - dimensions[3]);
		
		//currentGrouping.extractTextAsWordlist(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7)
		List words = currentGrouping.extractTextAsWordlist(
				dimensions[0], dimensions[1], dimensions[2], dimensions[3], 
				currentPage,
				true, 
				"" 
				// Used to be "&:=()!;\\/\"\"\'\'" 
				// NOTE that this strips out these text elements 
				// 		from the document as part of the parse!
				);
		
		// If there are no words on the page (a common situation 
		// with splashy handouts), make sure we do not encounter 
		// a null pointer exception here. 
		Iterator<String> wordIterator = (new ArrayList<String>()).iterator();		
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
			
			currentWord = wordIterator.next();

			font = getFontData(currentWord, "face");
			if( font != null )
				style = getFontData(currentWord, "style");
			
			currentWord = Strip.convertToText(currentWord, true);
			
			int wx1 = (int) Math.floor(Float.parseFloat((wordIterator.next() + "")));
			int wy1 = (int) Math.floor(Float.parseFloat((wordIterator.next() + "")));
			int wx2 = (int) Math.floor(Float.parseFloat((wordIterator.next() + "")));
			int wy2 = (int) Math.floor(Float.parseFloat((wordIterator.next() + "")));

			wy1 = dimensions[1] - wy1;
			wy2 = dimensions[1] - wy2;
			
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
					1, font, style, currentWord, i);
			
			if( font == null || style == null ) {
				logger.debug("Minor font error for word on pg." + this.currentPage + 
						" in '" + this.currentFile.getName() + "', info:" + 
						wordBlock.toString() + "\n");
			}
			
			wordListPerPage.add(wordBlock);
			i++;

		}
		
		currentPage++;
		PDFDecoder.flushObjectValues(false);

		return true;
	
	}

	@Override
	public boolean hasNext() {

		boolean haveNext = false;

		while (currentPage <= pageCount && !haveNext) {

			try {

				haveNext = decodeFile();

			} catch (EmptyPDFException e) {

				break;

			} catch (Exception e) {

				e.printStackTrace();
				break;

			}

		}

		if (currentPage == pageCount + 1) {

			PDFDecoder.flushObjectValues(true);
			PDFDecoder.closePdfFile();
			haveNext = false;

		}

		return haveNext;

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
