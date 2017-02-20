package edu.isi.bmkeg.lapdf.model;

import java.util.Collection;
import java.util.List;

import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public interface WordBlock extends Block, SpatialEntity {

	public String getWord();

	public String getFont();

	void setFont(String font);

	public String getFontStyle();

	void setFontStyle(String fontStyle); 

	public int getSpaceWidth();

	public List<WordBlock> readNearbyWords(int left, int right, int up, int down);

	public WordBlock readClosestBlock( int x, int y );
	
	public void setLocalDistances(int dx, int dy);

	public int[] getLocalDistances();

	public void setOrderAddedToChunk(int i);

	public int getOrderAddedToChunk();
	
	public WordBlock centroidOf(Collection<WordBlock> wbs);
		
	public double distanceFrom(WordBlock w);

	public void writeFlushArray(List<WordBlock> wordsToCheck); 

	public WordBlock[] getFlushArray();
	
}
