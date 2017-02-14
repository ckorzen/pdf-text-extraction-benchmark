package edu.isi.bmkeg.lapdf.model.RTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.UnicodeFormatter;

public class RTWordBlock extends RTSpatialEntity implements WordBlock {

	private static Logger logger = Logger.getLogger(RTWordBlock.class);
	
	private String font;
	private String fontStyle;
	private String word;
	
	private int spaceWidth;
	
	private Block container;
	private RTPageBlock page;

	private int[] localDistances; 
	private int orderAddedToChunk;
	private WordBlock[] flushArray; 

	public RTWordBlock(int x1, int y1, int x2, int y2, int spaceWidth,
			String font, String fontStyle, String word, int order) {
		
		super(x1, y1, x2, y2, order);
		this.font = font;
		this.fontStyle = fontStyle;
		
		if( word != null ) {
			try {
				this.word = UnicodeFormatter.fixEncoding(word);						
			} catch(Exception e) {
				logger.debug("Error in parsing " + word);
				this.word = "";
			}
		} else {
			this.word = "";
		}

		this.spaceWidth = spaceWidth;

	}

	@Override
	public int getId() {
		return super.getId();
	}
	
	@Override
	public void setFont(String font) {
		this.font = font;
	}

	@Override
	public String getFont() {
		return font;
	}

	@Override
	public String getFontStyle() {
		return fontStyle;
	}

	@Override
	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}

	@Override
	public String getWord() {

		return this.word;
	}

	public void setWord(String word) {
		if( word==null)
			word = "";
		this.word = word;
	}

	@Override
	public int getSpaceWidth() {

		return this.spaceWidth;
	}

	public void setSpaceWidth(int spaceWidth) {
		this.spaceWidth = spaceWidth;
	}

	@Override
	public Block getContainer() {
		return container;
	}

	@Override
	public void setContainer(Block block) {
		this.container = block;

	}

	@Override
	public RTPageBlock getPage() {
		return page;
	}

	@Override
	public void setPage(PageBlock page) {
		this.page = (RTPageBlock) page;
	}
	
	@Override
	public String readLeftRightMidLine() {
		PageBlock parent = (PageBlock) this.getContainer().getContainer();
		int median = parent.getMedian();
		int X1 = this.getX1();
		int width = this.getWidth();
		int averageWordHeightForTheDocument = parent.getDocument().readMostPopularWordHeight();

		// Conditions for left
		if (X1 < median
				&& (X1 + width) < (median + averageWordHeightForTheDocument))
			return LEFT;
		// conditions for right
		if (X1 > median)
			return RIGHT;
		// conditions for medline
		int left = median - X1;
		int right = X1 + width - median;
		/*
		 * Doubtful code if(right <= 0) return LEFT;
		 */
		double leftIsToRight = (double) left / (double) right;
		double rightIsToLeft = (double) right / (double) left;
		if (leftIsToRight < 0.05)
			return RIGHT;
		else if (rightIsToLeft < 0.05)
			return LEFT;
		else
			return MIDLINE;

	}

	public boolean isFlush(String condition, int value) {
		PageBlock parent = (PageBlock) this.getContainer();
		int median = parent.getMedian();
		String leftRightMidline = this.readLeftRightMidLine();

		int x1 = this.getX1();
		int x2 = this.getX2();
		int marginX1 = parent.getMargin()[0];
		int marginX2 = parent.getMargin()[3];

		if (condition.equals(MIDLINE)) {
			if (leftRightMidline.equals(MIDLINE))
				return false;
			else if (leftRightMidline.equals(LEFT)
					&& Math.abs(x2 - median) < value)
				return true;
			else if (leftRightMidline.equals(RIGHT)
					&& Math.abs(x1 - median) < value)
				return true;
		} else if (condition.equals(LEFT)) {
			if (leftRightMidline.equals(MIDLINE)
					&& Math.abs(x1 - marginX1) < value)
				return true;
			else if (leftRightMidline.equals(LEFT)
					&& Math.abs(x1 - marginX1) < value)
				return true;
			else if (leftRightMidline.equals(RIGHT))
				return false;
		} else if (condition.equals(RIGHT)) {
			if (leftRightMidline.equals(MIDLINE)
					&& Math.abs(x2 - marginX2) < value)
				return true;
			else if (leftRightMidline.equals(LEFT))
				return false;
			else if (leftRightMidline.equals(RIGHT)
					&& Math.abs(x2 - marginX2) < value)
				return true;
		}
		return false;
	}
	
	@Override
	public List<WordBlock> readNearbyWords(int left, int right, int up, int down) {

		// expand the current word.
		int topX = this.getX1() - left;
		int topY = this.getY1() - up;
		int bottomX = this.getX2() + right;
		int bottomY = this.getY2() + down;

		return readWordsInBounds(topX, topY, bottomX, bottomY);
	
	}
	
	private List<WordBlock> readWordsInBounds(int x1, int y1, int x2, int y2) {
		
		PageBlock page = this.getPage();
		
		SpatialEntity expandedWord = new RTWordBlock(
				x1, y1, x2, y2, 
				0, null, null, null, -1);
		
		// find all overlapping words
		TreeSet<SpatialEntity> spatiallyDefinedNeighbors = new TreeSet<SpatialEntity>(
				new SpatialOrdering(SpatialOrdering.MIXED_MODE)
				);
		spatiallyDefinedNeighbors.addAll(page.intersects(expandedWord, null));

		List<WordBlock> nearbyWords = new ArrayList<WordBlock>();
		for( SpatialEntity se : spatiallyDefinedNeighbors ) {
			if( se instanceof WordBlock )
				nearbyWords.add( (WordBlock) se ); 
		}
		
		return nearbyWords;
		
		
	}
	
	@Override
	public WordBlock readClosestBlock(int x, int y) {
	
		int maxD = this.getPage().getMostPopularHorizontalSpaceBetweenWordsPage() +
				this.getPage().getMostPopularVerticalSpaceBetweenWordsPage();
		
		WordBlock wb = (WordBlock) page.nearest(x, y, maxD);

		return wb;
	}	

	@Override
	public void setLocalDistances(int dx, int dy) {
		
		int dWest = 1000, dEast = 1000, dNorth = 1000, dSouth = 1000;
		
		List<WordBlock> proximalWords = this.readNearbyWords(dx, dx, dy, dy);
		
		for( WordBlock w : proximalWords ) {
			
			int centroidX = Math.round( (w.getX1() + w.getX2()) / 2 );
			int centroidY = Math.round( (w.getY1() + w.getY2()) / 2 );
			
			// Does the word line up and is it close enough in each of the 
			if( 	this.getX1() > centroidX && 
					(this.getX1() - w.getX2()) < dx && 
					(this.getX1() - w.getX2()) < dWest )
				dWest = this.getX1() - w.getX2();
			
			if( this.getX2() < centroidX && 
					(w.getX1() - this.getX2()) < dx && 
					(w.getX1() - this.getX2()) < dEast )
				dEast = this.getX2() - w.getX1();

			if( this.getY1() > centroidY && 
					(this.getY1() - w.getY2()) < dy && 
					(this.getY1() - w.getY2()) < dSouth )
				dSouth = this.getY1() - w.getY2();

			if( this.getY2() < centroidY && 
					(w.getY1() - this.getY2()) < dy && 
					(w.getY1() - this.getY2()) < dNorth)
				dNorth = this.getY1() - w.getY2();
			
		}
			
		this.localDistances = new int[]{dNorth, dSouth, dEast, dWest};
		
	}
	
	@Override
	public int[] getLocalDistances() {
		return localDistances;
	}

	@Override
	public int getOrderAddedToChunk() {
		return orderAddedToChunk;
	}

	@Override
	public void setOrderAddedToChunk(int orderAddedToChunk) {
		this.orderAddedToChunk = orderAddedToChunk;
	}

	@Override
	public WordBlock centroidOf(Collection<WordBlock> wbs) {
		
		int x=0, y=0;
		for( WordBlock wb : wbs ) {
			x += wb.getX1() + wb.getX2();
			y += wb.getY1() + wb.getY2();
		}
		
		x = x / ( wbs.size() * 2 ) ;
		y = y / ( wbs.size() * 2 ) ;
		
		WordBlock centroid = readClosestBlock(x, y);
		
		return centroid;
	}

	public float deltaFunction(Collection<WordBlock> wbs) {
		
		int minX=10000, maxX=-10000, minY=10000, maxY=-10000;
		int wordCoverage1 = 0;
		for( WordBlock wb : wbs ) {
			if( wb.getX1() < minX ) minX = wb.getX1();
			if( wb.getX2() > maxX ) maxX = wb.getX2();
			if( wb.getY1() < minY ) minY = wb.getY1();
			if( wb.getY2() > maxX ) maxX = wb.getY2();
			
			// word coverage, note that this is probably 
			// an over-estimation since words overlap
			wordCoverage1 += wb.getHeight() * wb.getWidth();
		}
		
		int area1 = (maxX - minX) * (maxY - minY);
		
		if( this.getX1() < minX ) minX = this.getX1();
		if( this.getX2() > maxX ) maxX = this.getX2();
		if( this.getY1() < minY ) minY = this.getY1();
		if( this.getY2() > maxX ) maxX = this.getY2();
	
		int area2 = (maxX - minX) * (maxY - minY);
		int wordCoverage2 = wordCoverage1 + (this.getHeight() * this.getWidth());
	
		float delta = (area2 * wordCoverage2) / (area1 * wordCoverage1);  
		
		return delta;
		
	}

	
	/**
	 * Manhattan distance from edge of 'w' to edge of 'this'
	 */
	@Override
	public double distanceFrom(WordBlock w) {

		if( w == null ){
			return 10000;
		}
		
		int dx = 0;
		if( this.getX2() < w.getX1() ) {
			dx = w.getX1() - this.getX2();
		} else if( this.getX1() > w.getX2() ) {
			dx = this.getX1() - w.getX2();		
		}

		int dy = 0;
		if( this.getY2() < w.getY1() ) {
			dy = w.getY1() - this.getY2();
		} else if( this.getY1() > w.getY2() ) {
			dy = this.getY1() - w.getY2();		
		}

		return (double) dx + dy;
		
	}

	@Override
	public void writeFlushArray(List<WordBlock> wordsToCheck) {

		if( this.flushArray == null ) {
			
			WordBlock lu = null, ru = null, ld = null, rd = null;
			
			List<WordBlock> wList = readWordsInBounds(
					this.getX1(), this.getY1() - this.getHeight(), 
					this.getX2(), this.getY1() - 1);
			for( WordBlock w : wList ) {
				if( w.getX1() == this.getX1() && w != this) 
					lu = w;
				if( w.getX2() == this.getX2() && w != this) 
					ru = w;
			}

			wList = readWordsInBounds(
					this.getX1(), this.getY2() + 1, 
					this.getX2(), this.getY2() + this.getHeight() );
			for( WordBlock w : wList ) {
				if( w.getX1() == this.getX1() && w != this) 
					ld = w;
				if( w.getX2() == this.getX2() && w != this) 
					rd = w;
			}

			
			this.flushArray = new WordBlock[]{lu, ru, ld, rd};
			
		}
	}
	 
	@Override
	public WordBlock[] getFlushArray() {

		return this.flushArray;
		
	}

}
