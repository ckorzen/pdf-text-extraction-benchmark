package edu.isi.bmkeg.lapdf.model.RTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class RTPageBlock extends RTSpatialContainer implements PageBlock {
	
	private static final long serialVersionUID = 1L;
	
	private int pageNumber;
	private int boxHeight;
	private int boxWidth;
	
	private Map<Integer, WordBlock> indexToWordBlockMap;
	private Map<Integer, ChunkBlock> indexToChunkBlockMap;

	private LapdfDocument document;

	public RTPageBlock(int pageNumber,
			int pageBoxWidth, int pageBoxHeight,
			LapdfDocument document) {
		
		super();

		this.indexToWordBlockMap = new HashMap<Integer, WordBlock>();
		this.indexToChunkBlockMap = new HashMap<Integer, ChunkBlock>();
				
		this.pageNumber = pageNumber;
		this.boxHeight = pageBoxHeight;
		this.boxWidth = pageBoxWidth;
		this.document = document;

	}
	
	public int getHeight() {
		return this.getX2()-this.getX1();
	}

	public int getWidth() {
		return this.getY2()-this.getY1();
	}

	public int getX1() {
		return (int) this.getMargin()[0];
	}

	public int getX2() {
		return (int) this.getMargin()[2];
	}

	public int getY1() {
		return (int) this.getMargin()[1];
	}

	public int getY2() {
		return (int) this.getMargin()[3];
	}

	public int getPageNumber() {
		return pageNumber;
	}
	
	@Override
	public String readLeftRightMidLine() {
		return null;
	}

	@Override
	public boolean isFlush(String condition, int value) {		
		return false;
	}

	@Override
	public Block getContainer() {
		return null;
	}

	@Override
	public void setContainer(Block block) {
	}

	@Override
	public int getPageBoxHeight() {		
		return boxHeight;
	}

	@Override
	public int getPageBoxWidth() {
		return boxWidth;
	}

	@Override
	public LapdfDocument getDocument() {
		return document;
	}

	@Override
	public PageBlock getPage() {
		return this;
	}

	@Override
	public void setPage(PageBlock page) { 
		// Do nothing
	}
	
	@Override
	public int initialize(List<WordBlock> list, int startId) {

		for(WordBlock block:list){
			block.setPage(this);
			this.add(block, startId++);
		}
		
		return startId;
	
	}

	@Override
	public void add(SpatialEntity entity, int id) {

		RTSpatialEntity rtSpatialEntity = (RTSpatialEntity) entity;
		rtSpatialEntity.setId(id);
		if (rtSpatialEntity instanceof ChunkBlock) {
			this.indexToChunkBlockMap.put(id, (ChunkBlock) rtSpatialEntity);
		} else {
			this.indexToWordBlockMap.put(id, (WordBlock) rtSpatialEntity);
		}
		tree.add(rtSpatialEntity, id);
						
	}	
	
	@Override
	public SpatialEntity getEntity(int id) {
		if (indexToWordBlockMap.containsKey(id))
			return indexToWordBlockMap.get(id);

		return indexToChunkBlockMap.get(id);
	}

	@Override
	public boolean delete(SpatialEntity entity, int id) {

		RTSpatialEntity rtSpatialEntity = (RTSpatialEntity) entity;

		if (indexToChunkBlockMap.containsKey(id))
			indexToChunkBlockMap.remove(id);
		else
			indexToWordBlockMap.remove(id);
		
		boolean treeDel = tree.delete(rtSpatialEntity, id);
		
		return treeDel;

	}
	
	@Override
	public List<ChunkBlock> getAllChunkBlocks(String ordering) {

		List<ChunkBlock> list = new ArrayList<ChunkBlock>(
				indexToChunkBlockMap.values());
		if (ordering != null) {
			Collections.sort(list, new SpatialOrdering(ordering));
		}

		return list;
	}
	
	@Override
	public List<WordBlock> getAllWordBlocks(String ordering) {
		
		List<WordBlock> list = new ArrayList<WordBlock>(
				indexToWordBlockMap.values());
		
		if (ordering != null) {			
			Collections.sort(list, new SpatialOrdering(ordering));
		}

		return list;
	}

}
