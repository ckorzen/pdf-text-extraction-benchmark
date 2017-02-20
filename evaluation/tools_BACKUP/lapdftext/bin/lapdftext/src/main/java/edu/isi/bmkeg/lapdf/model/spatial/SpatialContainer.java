package edu.isi.bmkeg.lapdf.model.spatial;

import java.io.Serializable;
import java.util.List;

import edu.isi.bmkeg.lapdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;

public interface SpatialContainer extends Serializable {
	
	public int addAll(List<SpatialEntity> list, int startId);

	public void add(SpatialEntity entity, int id);

	public List<SpatialEntity> intersects(SpatialEntity entity, String ordering);

	public SpatialEntity nearest(int x, int y, int maxDistance);

	public List<ChunkBlock> getAllChunkBlocks(String ordering);

	public List<WordBlock> getAllWordBlocks(String ordering);

	public int getMedian();

	public int[] getMargin();

	public List<SpatialEntity> contains(SpatialEntity entity, String ordering);

	public List<SpatialEntity> containsByType(SpatialEntity entity,
			String ordering, Class classType);

	public boolean delete(SpatialEntity entity, int id);

	public List<SpatialEntity> intersectsByType(SpatialEntity entity,
			String ordering, Class classType);

	public SpatialEntity getEntity(int id);

	public int getMostPopularWordHeightPage();

	public int getMostPopularHorizontalSpaceBetweenWordsPage();

	public int getMostPopularWordWidthPage();

	public int getMostPopularVerticalSpaceBetweenWordsPage();

}
