package edu.isi.bmkeg.lapdf.model.RTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDirection;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class RTChunkBlock extends RTSpatialEntity implements ChunkBlock {

	private static final long serialVersionUID = 1L;

	private Block container;
	private int mostPopularWordHeight;
	private int mostPopularWordSpaceWidth;
	private String mostPopularWordFont = "";
	private String mostPopularWordStyle = "";
	
	private String alignment = null;
	private String type = ChunkBlock.TYPE_UNCLASSIFIED;
	private Boolean headerOrFooter=null;
	
	private double density = -1;
	
	private List<WordBlock> rotatedWords = new ArrayList<WordBlock>();

	public RTChunkBlock() {
		super();
	}

	public RTChunkBlock(int x1, int y1, int x2, int y2, int order) {
		super(x1, y1, x2, y2, order);
	}
	
	@Override
	public int getId() {
		return super.getId();
	}

	@Override
	public Block getContainer() {
		return container;
	}

	@Override
	public int getMostPopularWordHeight() {

		return mostPopularWordHeight;
	}

	public int getMostPopularWordSpaceWidth() {
		return mostPopularWordSpaceWidth;
	}

	public void setMostPopularWordSpaceWidth(int mostPopularWordSpaceWidth) {
		this.mostPopularWordSpaceWidth = mostPopularWordSpaceWidth;
	}

	public String getMostPopularWordFont() {
		return mostPopularWordFont;
	}

	public void setMostPopularWordFont(String mostPopularWordFont) {
		this.mostPopularWordFont = mostPopularWordFont;
	}

	public void setMostPopularWordHeight(int height) {
		this.mostPopularWordHeight = height;
	}

	@Override
	public String getMostPopularWordStyle() {
		return mostPopularWordStyle;
	}

	@Override
	public void setMostPopularWordStyle(String style) {
		this.mostPopularWordStyle=style;
	}

	@Override
	public Boolean isHeaderOrFooter() {
		return headerOrFooter;
	}

	@Override
	public void setHeaderOrFooter(boolean headerOrFooter) {
		this.headerOrFooter=headerOrFooter;
	}
	
	@Override
	public void setContainer(Block block) {
		this.container = (PageBlock) block;
	}
	
	@Override
	public PageBlock getPage() {
		return (PageBlock) this.container;
	}

	@Override
	public void setPage(PageBlock page) {
		this.container = page;
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
	
	public List<WordBlock> getRotatedWords() {
		return rotatedWords;
	}

	public void setRotatedWords(List<WordBlock> rotatedWords) {
		this.rotatedWords = rotatedWords;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public String readLeftRightMidLine() {
		
		if (this.alignment != null)
			return this.alignment;
		
		PageBlock page = (PageBlock) this.getContainer();
		int median = page.getMedian();
		int X1 = this.getX1();
		int width = this.getWidth();

		if ( X1 + width < median ) {
		
			this.alignment = LEFT;	
		
		} else if (X1 > median) {

			this.alignment = RIGHT;
		
		} else {
		
			/*int left = median - X1;
			int right = X1 + width - median;

			double leftIsToRight = (double) left / (double) right;
			double rightIsToLeft = (double) right / (double) left;
			if (leftIsToRight < 0.05)
				this.alignment = RIGHT;
			else if (rightIsToLeft < 0.05)
				this.alignment = LEFT;
			else*/
				this.alignment = MIDLINE;
		}
			
		return this.alignment;
	
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
	public int readNumberOfLine() {
		PageBlock parent = (PageBlock) this.container;
		List<SpatialEntity> wordBlockList = parent.containsByType(this,
				SpatialOrdering.MIXED_MODE, WordBlock.class);
		if (wordBlockList.size() == 0)
			return 0;
		WordBlock block = (WordBlock) wordBlockList.get(0);
		int numberOfLines = 1;
		int lastY = block.getY1() + block.getHeight() / 2;
		int currentY = lastY;
		for (SpatialEntity entity : wordBlockList) {
			lastY = currentY;
			block = (WordBlock) entity;
			currentY = block.getY1() + block.getHeight() / 2;
			if (currentY > lastY + block.getHeight() / 2)
				numberOfLines++;

		}
		return numberOfLines;
	}

	@Override
	public String readChunkText() {
		
		List<SpatialEntity> wordBlockList = ((PageBlock) container)
				.containsByType(this, SpatialOrdering.MIXED_MODE,
						WordBlock.class);
		
		StringBuilder builder = new StringBuilder();
		for (SpatialEntity entity : wordBlockList) {
			builder.append( ((WordBlock) entity).getWord() );

			if( ((WordBlock) entity).getWord() == null )
				continue;
			
			if( !((WordBlock) entity).getWord().endsWith("-") )
				builder.append(" ");

		}
		
		return builder.toString().trim();

	}
	
	@Override
	public ChunkBlock readLastChunkBlock() {
		
		List<ChunkBlock> sortedChunkBlockList = ((PageBlock) this
				.getContainer())
				.getAllChunkBlocks(SpatialOrdering.MIXED_MODE);

		int index = Collections.binarySearch(sortedChunkBlockList, this,
				new SpatialOrdering(SpatialOrdering.MIXED_MODE));

		return (index <= 0) ? null : sortedChunkBlockList.get(index - 1);
	}
	
	/**
	 * returns true if the chunk block contains text that matches the input regex
	 * @param regex
	 * @return
	 */
	@Override
	public boolean isMatchingRegularExpression(String regex) {
		Pattern pattern = Pattern.compile(regex);

		String text = this.readChunkText();
		Matcher matcher = pattern.matcher(text);
		
		if (matcher.find())
			return true;

		return false;
	}
		
	/**
	 * returns true if chunk block has neighbors of specific type within specified distance
	 * @param type
	 * @param nsew
	 * @return
	 */
	@Override
	public boolean hasNeighboursOfType(String type, int nsew) {
		
		List<ChunkBlock> list = getOverlappingNeighbors(nsew, 
				(PageBlock) this.getContainer(), 
				(ChunkBlock) this);
		
		for (ChunkBlock chunky : list)
			if (chunky.getType().equalsIgnoreCase(type))
				return true;

		return false;
	
	}

	@Override
	public boolean isUnderOneLineFlushNeighboursOfType(String type) {
		
		List<ChunkBlock> list = getOverlappingNeighbors(LapdfDirection.NORTH, 
				(PageBlock) this.getContainer(), 
				(ChunkBlock) this);
		
		double threshold = this.getMostPopularWordHeight() * 2;
		
		for (ChunkBlock chunky : list) {
			
			int delta1 = Math.abs(chunky.getX1() - this.getX1());
			int delta2 = Math.abs(chunky.getX2() - this.getX2());
			
			if( delta1 < threshold
					&& delta2 < threshold 
					&& chunky.readNumberOfLine() == 1 
					&& chunky.getType().equalsIgnoreCase(type)) {
				return true;
			}
		}
		
		return false;
	
	}

	
	public List<ChunkBlock> getOverlappingNeighbors(
			int nsew, 
			PageBlock parent,
			ChunkBlock chunkBlock) {
		
		int topX = chunkBlock.getX1();
		int topY = chunkBlock.getY1();
		int width = chunkBlock.getWidth();
		int height = chunkBlock.getHeight();

		if (nsew == LapdfDirection.NORTH) {
			height = height / 2;
			topY = topY - height;
		} else if (nsew == LapdfDirection.SOUTH) {
			topY = topY + height;
			height = height / 2;
		} else if (nsew == LapdfDirection.EAST) {
			topX = topX + width;
			width = width / 2;
		} else if (nsew == LapdfDirection.WEST) {
			width = width / 2;
			topX = topX - width;
		} else if (nsew == LapdfDirection.NORTH_SOUTH) {
			topY = topY - height / 2;
			height = height * 2;
		} else if (nsew == LapdfDirection.EAST_WEST) {
			topX = topX - width / 2;
			width = width * 2;

		}

		SpatialEntity entity = new RTChunkBlock(topX, topY, topX
				+ width, topY + height, -1);

		List<ChunkBlock> l = new ArrayList<ChunkBlock>();		
		 Iterator<SpatialEntity> it = parent.intersectsByType(
				 entity, null, ChunkBlock.class).iterator();
		 while( it.hasNext() ) {
			 l.add((ChunkBlock)it.next());
		 }

		 return l;
		 
	}

	@Override
	public double readDensity() {
		
		if( this.density < 0.0 ) {
	
			PageBlock page = (PageBlock) this.getContainer();
			List<SpatialEntity> wordBlockList = page.containsByType(this,
					SpatialOrdering.MIXED_MODE, 
					WordBlock.class);
			double chunkSize = this.width() * this.height();
			double wordCoverage = 0.0;
			for (int i = 0; i < wordBlockList.size(); i++) {
				WordBlock wordBlock = (WordBlock) wordBlockList.get(i);
				wordCoverage += wordBlock.getHeight() * wordBlock.getWidth();
			}
				
			this.density = wordCoverage / chunkSize;
			
		}

		return this.density;
		
	}
	
	/* Need to implement these functions 
	 * to handle issues with serialization
	 private void writeObject(java.io.ObjectOutputStream out)
		     throws IOException
		 private void readObject(java.io.ObjectInputStream in)
		     throws IOException, ClassNotFoundException;
		 private void readObjectNoData() 
		     throws ObjectStreamException;*/

}
