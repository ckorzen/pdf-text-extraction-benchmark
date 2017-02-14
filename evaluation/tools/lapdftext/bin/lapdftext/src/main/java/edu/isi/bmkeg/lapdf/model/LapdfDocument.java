package edu.isi.bmkeg.lapdf.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.isi.bmkeg.lapdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.lapdf.model.RTree.RTSpatialEntity;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLChunk;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLDocument;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLFontStyle;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLPage;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLRectangle;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLWord;
import edu.isi.bmkeg.utils.FrequencyCounter;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;

public class LapdfDocument implements Serializable {

	private File pdfFile;
	
	private ArrayList<PageBlock> pageList;
	
	private IntegerFrequencyCounter avgHeightFrequencyCounter;
	private FrequencyCounter fontFrequencyCounter;
	
	private int mostPopularWordHeight = -100;
	private String mostPopularFontStyle = "";
	private String nextMostPopularFontStyle = "";
	private String mostPopularFontStyleOnLastPage = "";
	
	// This the rectangle that holds the text of the main 'panel' 
	// across the whole document (excluding footers and headers)
	private SpatialEntity bodyTextFrame;

	private boolean jPedalDecodeFailed;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public LapdfDocument() {
		this.setAvgHeightFrequencyCounter(new IntegerFrequencyCounter(1));
		this.setFontFrequencyCounter(new FrequencyCounter());
	}
	
	public LapdfDocument(File pdfFile) {
		this.setPdfFile(pdfFile);
		this.setAvgHeightFrequencyCounter(new IntegerFrequencyCounter(1));
		this.setFontFrequencyCounter(new FrequencyCounter());
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public boolean hasjPedalDecodeFailed() {
		return jPedalDecodeFailed;
	}

	public void setjPedalDecodeFailed(boolean jPedalDecodeFailed) {
		this.jPedalDecodeFailed = jPedalDecodeFailed;
	}

	public int getTotalNumberOfPages() {
		return this.pageList.size();
	}

	public File getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(File pdfFile) {
		this.pdfFile = pdfFile;
	}

	public IntegerFrequencyCounter getAvgHeightFrequencyCounter() {
		return avgHeightFrequencyCounter;
	}

	public void setAvgHeightFrequencyCounter(IntegerFrequencyCounter avgHeightFrequencyCounter) {
		this.avgHeightFrequencyCounter = avgHeightFrequencyCounter;
	}
	
	public FrequencyCounter getFontFrequencyCounter() {
		return fontFrequencyCounter;
	}

	public void setFontFrequencyCounter(FrequencyCounter fontFrequencyCounter) {
		this.fontFrequencyCounter = fontFrequencyCounter;
	}

	public SpatialEntity getBodyTextFrame() {
		return bodyTextFrame;
	}

	public void setBodyTextFrame(SpatialEntity bodyTextFrame) {
		this.bodyTextFrame = bodyTextFrame;
	}

	public String getMostPopularFontStyle() {
		return mostPopularFontStyle;
	}

	public void setMostPopularFontStyle(String mostPopularFontStyle) {
		this.mostPopularFontStyle = mostPopularFontStyle;
	}

	public String getNextMostPopularFontStyle() {
		return nextMostPopularFontStyle;
	}

	public void setNextMostPopularFontStyle(String nextMostPopzularFontStyle) {
		this.nextMostPopularFontStyle = nextMostPopularFontStyle;
	}

	public String getMostPopularFontStyleOnLastPage() {
		return mostPopularFontStyleOnLastPage;
	}

	public void setMostPopularFontStyleOnLastPage(
			String mostPopularFontStyleOnLastPage) {
		this.mostPopularFontStyleOnLastPage = mostPopularFontStyleOnLastPage;
	}

	public int getMostPopularWordHeight() {
		return mostPopularWordHeight;
	}

	public void setMostPopularWordHeight(int mostPopularWordHeight) {
		this.mostPopularWordHeight = mostPopularWordHeight;
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void addPages(List<PageBlock> pageList) {
		this.pageList = new ArrayList<PageBlock>(pageList);
	}

	public PageBlock getPage(int pageNumber) {

		return pageList.get(pageNumber - 1);
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public ChunkBlock getLastChunkBlock(ChunkBlock chunk)
			throws InvalidPopularSpaceValueException {
		
		int pageNumber = ((PageBlock) chunk.getContainer()).getPageNumber();
		PageBlock page = this.getPage(pageNumber);
		
		if (page.getMostPopularVerticalSpaceBetweenWordsPage() < 0
				&& page.getMostPopularWordHeightPage() > page
						.getMostPopularWordWidthPage() * 2) {
			// page.getMostPopularWordHeightPage()>page.getMostPopularWordWidthPage()*2
			
			System.err.println(
					"Possible page with vertical text flow at page number +"
					+ pageNumber);

			// throw new
			// InvalidPopularSpaceValueException("Possible page with vertical text flow at page number +"+pageNumber);
		}

		if (chunk.readLastChunkBlock() != null) {
			// System.out.println("Same page");
			return chunk.readLastChunkBlock();
		} else {
			pageNumber = ((PageBlock) chunk.getContainer()).getPageNumber() - 1;

			if (pageNumber == 0) {
				return null;
			}

			page = this.getPage(pageNumber);
			List<ChunkBlock> sortedChunkBlockList = page
					.getAllChunkBlocks(SpatialOrdering.MIXED_MODE);
			// System.out.println("Page:"+ pageNumber);
			return sortedChunkBlockList.get(sortedChunkBlockList.size() - 1);
		}

	}

	public int readMostPopularWordHeight() {
		
		if( this.mostPopularWordHeight != -100 )
			return this.mostPopularWordHeight;
		
		int mp = this.avgHeightFrequencyCounter.getMostPopular();
		double mpCount = this.avgHeightFrequencyCounter.getCount(mp);
		int nmp = this.avgHeightFrequencyCounter.getNextMostPopular();
		double nmpCount = this.avgHeightFrequencyCounter.getCount(nmp);
		double ratio = nmpCount / mpCount;

		// Sneaky check for long reference sections
		if (nmp > mp && ratio > 0.8) {
			mostPopularWordHeight = nmp;
		} else {
			mostPopularWordHeight = mp;
		}

		return mostPopularWordHeight;
	}	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void calculateBodyTextFrame() {

		String mp = (String) this.fontFrequencyCounter.getMostPopular();
		String[] mpArray = mp.split(";");
		
		int x_min = 10000;
		int y_min = 10000;
		int x_max = -100;
		int y_max = -100;
		
		Iterator<PageBlock> pgIt = this.pageList.iterator();
		while( pgIt.hasNext() ) {
			PageBlock pg = pgIt.next();
			
			Iterator<WordBlock> wdIt = pg.getAllWordBlocks(SpatialOrdering.MIXED_MODE).iterator();
			while( wdIt.hasNext() ) {
				WordBlock wd = wdIt.next();

				if( wd.getFont() == null || wd.getFontStyle() == null) 
					continue;
				
				if( wd.getFont().equals(mpArray[0]) && 
						wd.getFontStyle().equals(mpArray[1]) ) {
				
					if( wd.getX1() < x_min )
						 x_min = wd.getX1();
					if( wd.getX2() > x_max )
						 x_max = wd.getX2();
					if( wd.getY1() < y_min )
						 y_min = wd.getY1();
					if( wd.getY2() > y_max )
						 y_max = wd.getY2();
				
				}
			
			}
		
		}
		
		this.setBodyTextFrame(new RTSpatialEntity(
				(float) x_min, (float) y_min, (float) x_max, (float) y_max, 0
				));
		
	}

	public void calculateMostPopularFontStyles() {

		String lastPage = this.readMostPopularFontStyleOnLastPage();

		String mp = (String) this.fontFrequencyCounter.getMostPopular();
		int mpCount = this.fontFrequencyCounter.getCount(mp);
		String nmp = (String) this.fontFrequencyCounter.getNextMostPopular();
		int nmpCount = this.fontFrequencyCounter.getCount(nmp);
		String nnmp = (String) this.fontFrequencyCounter.getThirdMostPopular();

		//
		// If there is a particularly long reference section, then we use the second 
		// most popular font style. Need to check if the last page is not just the 
		// same font as the rest of the document.
		//
		if( mp.equals( lastPage ) && mpCount < nmpCount * 7) {
			
			this.setMostPopularFontStyle(nmp);
			this.setNextMostPopularFontStyle(nnmp);
			
		} else if( nmp.equals( lastPage ) ) {

			this.setMostPopularFontStyle(mp);
			this.setNextMostPopularFontStyle(nnmp);
			
		} else {

			this.setMostPopularFontStyle(mp);
			this.setNextMostPopularFontStyle(nmp);

		}
		
	}

	public String readMostPopularFontStyleOnLastPage() {

		if( this.getMostPopularFontStyleOnLastPage() != null &&
				this.getMostPopularFontStyleOnLastPage().length() > 0 ) {

			return this.getMostPopularFontStyleOnLastPage();
		
		}
		
		this.setMostPopularFontStyle((String) this.fontFrequencyCounter.getMostPopular() );
	
		FrequencyCounter freq = new FrequencyCounter ();
		
		Iterator<PageBlock> pgIt = this.pageList.iterator();
		while( pgIt.hasNext() ) {
			PageBlock pg = pgIt.next();
			
			if( pg.getPageNumber() < this.pageList.size() ) {
				continue;
			}

			
			Iterator<WordBlock> wdIt = pg.getAllWordBlocks(SpatialOrdering.MIXED_MODE).iterator();
			while( wdIt.hasNext() ) {
				WordBlock wd = wdIt.next();

				if( wd.getFont() == null || wd.getFontStyle() == null) 
					continue;
				
				freq.add( wd.getFont() + ";" + wd.getFontStyle() );
								
			}
		
		}
		
		this.setMostPopularFontStyleOnLastPage((String) freq.getMostPopular() );
		
		return this.getMostPopularFontStyleOnLastPage();
				
	}	
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Output functions
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Reads the chunks whose classifications are listed in the 'sections' Set 
	 * 
	 * @param sections
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public String readClassifiedText(Set<String> sections) 
			throws IOException,FileNotFoundException {

		StringBuilder sb = new StringBuilder();
		
		int n = this.getTotalNumberOfPages();
		for (int i = 1; i <= n; i++)	{
			PageBlock page = this.getPage(i);
			
			List<ChunkBlock> chunksPerPage = page.getAllChunkBlocks(
					SpatialOrdering.MIXED_MODE
					);
			
			for(ChunkBlock chunkBlock:chunksPerPage){
				if( sections.contains( chunkBlock.getType() ) ) {
					sb.append(chunkBlock.readChunkText() + "\n");
				} 
			}
		}
		
		return sb.toString();

	}
	
	
	/**
	 * Reads the chunks whose classifications start with 'stem'.
	 * 
	 * @param sections
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public List<ChunkBlock> readClassifiedChunkBlocks(Set<String> sections) 
			throws IOException,FileNotFoundException {

		List<ChunkBlock> blocks = new ArrayList<ChunkBlock>(); 
		
		int n = this.getTotalNumberOfPages();
		for (int i = 1; i <= n; i++)	{
			PageBlock page = this.getPage(i);
			
			List<ChunkBlock> chunksPerPage = page.getAllChunkBlocks(
					SpatialOrdering.MIXED_MODE
					);
			
			for(ChunkBlock chunkBlock:chunksPerPage){
				if( sections.contains( chunkBlock.getType() ) ) {
					blocks.add( chunkBlock );
				} 
			}
		}
		
		return blocks;

	}
	
	public List<ChunkBlock> readStemmedChunkBlocks(String stem) 
			throws IOException,FileNotFoundException {

		List<ChunkBlock> blocks = new ArrayList<ChunkBlock>(); 
		
		int n = this.getTotalNumberOfPages();
		for (int i = 1; i <= n; i++)	{
			PageBlock page = this.getPage(i);
			
			List<ChunkBlock> chunksPerPage = page.getAllChunkBlocks(
					SpatialOrdering.MIXED_MODE
					);
			
			for(ChunkBlock chunkBlock:chunksPerPage){
				if( chunkBlock.getType().startsWith(stem) ) {
					blocks.add( chunkBlock );
				} 
			}
		}
		
		return blocks;

	}
	
	public List<ChunkBlock> readAllChunkBlocks() 
			throws IOException,FileNotFoundException {

		List<ChunkBlock> blocks = new ArrayList<ChunkBlock>(); 
		
		int n = this.getTotalNumberOfPages();
		for (int i = 1; i <= n; i++)	{
			PageBlock page = this.getPage(i);
			
			blocks.addAll( page.getAllChunkBlocks(
					SpatialOrdering.ORIGINAL_MODE
					));
			
		}
		
		return blocks;

	}
	
	/**
	 * Convert the LapdfDocument to XML Objects
	 * @param doc
	 * @param out
	 */
	public LapdftextXMLDocument convertToLapdftextXmlFormat() throws Exception {
		
		LapdftextXMLDocument xmlDoc = new LapdftextXMLDocument();
		
		Map<String, Integer> fontStyles = new HashMap<String, Integer>();
		int fsCount = 0;
		
		int nPages = this.getTotalNumberOfPages();
		int id = 0;
		for( int i=0; i<nPages; i++ ) {
			PageBlock page = this.getPage(i+1);
			
			LapdftextXMLPage xmlPage = new LapdftextXMLPage();
			xmlDoc.getPages().add(xmlPage);
			
			xmlPage.setId( id++ );
			xmlPage.setH( page.getPageBoxHeight() );
			xmlPage.setW( page.getPageBoxWidth() );

			// int width = parent.getMargin()[2] - parent.getMargin()[0];
			// int height = parent.getMargin()[3] - parent.getMargin()[1];
			int[] m = page.getMargin();
			LapdftextXMLRectangle r = new LapdftextXMLRectangle(id++, m[2]-m[0], m[3]-m[1], m[0], m[1]);
			xmlPage.setMargin( r );
			
			xmlPage.setMostPopWordHeight( 
					page.getMostPopularWordHeightPage() 
					);
			xmlPage.setPageNumber( i+1 );
			
			Iterator<ChunkBlock> cIt = page.getAllChunkBlocks(
					SpatialOrdering.COLUMN_AWARE_MIXED_MODE
					).iterator();
			
			while( cIt.hasNext() ) {
				ChunkBlock chunk = cIt.next();
				
				LapdftextXMLChunk xmlChunk = new LapdftextXMLChunk();
				xmlPage.getChunks().add(xmlChunk);
				
				if( chunk.getType() != null )
					xmlChunk.setType( chunk.getType() );
				
				xmlChunk.setFont( chunk.getMostPopularWordFont() );
				xmlChunk.setFont( chunk.getMostPopularWordFont() );
				xmlChunk.setFontSize( chunk.getMostPopularWordHeight() );
			
				xmlChunk.setId( id++ );
				xmlChunk.setW( chunk.getX2() - chunk.getX1() );
				xmlChunk.setH( chunk.getY2() - chunk.getY1() );
				xmlChunk.setX( chunk.getX1() );
				xmlChunk.setY( chunk.getY1() );
				xmlChunk.setI( chunk.getOrder() );
				
				List<SpatialEntity> wbList = page.containsByType(chunk,
						SpatialOrdering.ORIGINAL_MODE, 
						WordBlock.class);
				if( wbList != null ) {					
					Iterator<SpatialEntity> wbIt = wbList.iterator();
					while( wbIt.hasNext() ) {
						WordBlock word = (WordBlock) wbIt.next();
	
						LapdftextXMLWord xmlWord = new LapdftextXMLWord();
						xmlChunk.getWords().add( xmlWord ); 
						
						if( word.getWord() != null ) {
							xmlWord.setT(word.getWord());							
						} else {
							continue;
						}
						
						xmlWord.setId( id++ );
						xmlWord.setW( word.getX2() - word.getX1() );
						xmlWord.setH( word.getY2() - word.getY1() );
						xmlWord.setX( word.getX1() );
						xmlWord.setY( word.getY1() );
						xmlWord.setI( word.getOrder() );
						
						if( !fontStyles.containsKey( word.getFont() ) ) {
							fontStyles.put(word.getFont(), fsCount++);
						} 
						xmlWord.setfId(fontStyles.get( word.getFont() ) );
											
						if( !fontStyles.containsKey( word.getFontStyle() ) ) {
							fontStyles.put(word.getFontStyle(), fsCount++);
						} 
						xmlWord.setsId(fontStyles.get( word.getFontStyle() ) );
						
					}
					
				}
				
			}
			
		}
		
		for( String fsStr : fontStyles.keySet() ) {
			LapdftextXMLFontStyle fsXml = new LapdftextXMLFontStyle();
			fsXml.setFontStyle(fsStr);
			fsXml.setId(fontStyles.get(fsStr));
			xmlDoc.getFontStyles().add(fsXml);
		}
		
		return xmlDoc;
	
	}	
	
}
