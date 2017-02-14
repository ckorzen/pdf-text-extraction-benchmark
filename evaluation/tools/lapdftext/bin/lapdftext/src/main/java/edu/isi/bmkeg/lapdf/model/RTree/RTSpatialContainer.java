package edu.isi.bmkeg.lapdf.model.RTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialContainer;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;

public abstract class RTSpatialContainer implements SpatialContainer {

	private static final long serialVersionUID = 1L;
	
	private int mostPopularHorizontalSpaceBetweenWords = -100;
	private int mostPopularWordWidth = -100;
	private int mostPopularVerticalSpaceBetweenWords = -100;
	private int mostPopularWordHeightPerPage = -100;
	private int[] margin = null;
	
	private List<WordBlock> list = null;
	
	protected RTree tree;

	private int maxNode = 1500;
	private int minNode = 1; 
	
	protected RTSpatialContainer() {
		
		Properties prp = new Properties();
		prp.setProperty("MaxNodeEntries", "" + maxNode);
		prp.setProperty("MinNodeEntries", "" + minNode);

		tree = new RTree();
		tree.init(prp);

	}

	@Override
	public abstract void add(SpatialEntity entity, int id);

	@Override
	public abstract SpatialEntity getEntity(int id);
	
	@Override
	public abstract boolean delete(SpatialEntity entity, int id);
	
	@Override
	public int addAll(List<SpatialEntity> list, int startId) {
		for (SpatialEntity entity : list)
			this.add(entity, startId++);

		return startId;
	}

	public List<SpatialEntity> intersects(SpatialEntity entity, String ordering) {

		return this.intersectsByType(entity, ordering, null);
	
	}

	@Override
	public int[] getMargin() {
		if (margin == null) {
			margin = new int[4];
			Rectangle marginRect = tree.getBounds();

			margin[0] = (int) marginRect.minX;
			margin[1] = (int) marginRect.minY;
			margin[2] = (int) marginRect.maxX;
			margin[3] = (int) marginRect.maxY;

			return margin;
		}

		return margin;
	}

	@Override
	public int getMedian() {
		
		if( margin == null )
			this.getMargin();
		
		return margin[0] + (margin[2] - margin[0]) / 2;
	
	}

	@Override
	public List<SpatialEntity> contains(SpatialEntity entity, String ordering) {
		return this.containsByType(entity, ordering, null);
	}

	@Override
	public SpatialEntity nearest(int x, int y, int maxDistance) {

		RTSimpleProcedure procedure = new RTSimpleProcedure(this);

		Point p = new Point(x, y);
		
		this.tree.nearest(p, procedure, maxDistance);

		return procedure.getFoundEntity();
		
	}
	
	@Override
	public List<SpatialEntity> intersectsByType(SpatialEntity entity,
			String ordering, Class classType) {

		RTProcedure procedure = new RTProcedure(this, ordering,
				(RTSpatialEntity) entity, classType, false);

		tree.intersects((RTSpatialEntity) entity, procedure);

		return procedure.getIntersectionList();
	}

	@Override
	public List<SpatialEntity> containsByType(SpatialEntity entity,
			String ordering, Class classType) {

		RTProcedure procedure = new RTProcedure(this, ordering,
				(RTSpatialEntity) entity, classType, true);

		tree.contains((RTSpatialEntity) entity, procedure);
		if (procedure.getIntersectionList().size() == 0) {
			List<SpatialEntity> intersectList = this.intersectsByType(entity,
					ordering, classType);
			List<SpatialEntity> returnList = new ArrayList<SpatialEntity>();
			for (SpatialEntity loopEntity : intersectList) {
				if (entity.getX1() <= loopEntity.getX1()
						&& entity.getX2() >= loopEntity.getX2()
						&& entity.getY1() <= loopEntity.getY1()
						&& entity.getY2() >= loopEntity.getY2())
					returnList.add(loopEntity);
				return returnList;
			}

		}
		return procedure.getIntersectionList();
	}

	@Override
	public int getMostPopularHorizontalSpaceBetweenWordsPage() {
		
		if (mostPopularHorizontalSpaceBetweenWords != -100) {
			return mostPopularHorizontalSpaceBetweenWords;
		}
		
		IntegerFrequencyCounter avgHorizontalSpaceBetweenWordFrequencyCounter = 
				new IntegerFrequencyCounter(1);
		if (list == null)
			list = this.getAllWordBlocks(SpatialOrdering.MIXED_MODE);
		int lastX2 = list.get(0).getX2();
		int space;
		for (WordBlock block : list) {
			space = block.getX1() - lastX2;
			if (space > 0) {
				avgHorizontalSpaceBetweenWordFrequencyCounter.add(space);
			}
			lastX2 = block.getX2();
		}

		int mostPopular = avgHorizontalSpaceBetweenWordFrequencyCounter
				.getMostPopular();
		double mostPopularCount = avgHorizontalSpaceBetweenWordFrequencyCounter
				.getCount(mostPopular);
		int secondMostPopular = avgHorizontalSpaceBetweenWordFrequencyCounter
				.getNextMostPopular();
		double secondMostPopularCount = avgHorizontalSpaceBetweenWordFrequencyCounter
				.getCount(secondMostPopular);
		double ratio = secondMostPopularCount / mostPopularCount;
		if (secondMostPopular > mostPopular && ratio > 0.8) {
			mostPopularHorizontalSpaceBetweenWords = secondMostPopular;
		} else {
			mostPopularHorizontalSpaceBetweenWords = mostPopular;
		}

		//
		// TODO - CHECK IF WE REALLY NEED THIS...
		//
		//propagateCalculation();
		// System.out.println("Returning mostPopularHorizontalSpaceBetweenWords"+mostPopularHorizontalSpaceBetweenWords);
		return mostPopularHorizontalSpaceBetweenWords;
	}

	private void propagateCalculation() {
		
		if (mostPopularHorizontalSpaceBetweenWords == -100) {
			getMostPopularHorizontalSpaceBetweenWordsPage();
		}
		
		if (mostPopularWordWidth == -100) {
			getMostPopularWordWidthPage();
		}
		
		if (mostPopularVerticalSpaceBetweenWords == -100) {
			getMostPopularVerticalSpaceBetweenWordsPage();
		}
		
		if (mostPopularWordHeightPerPage == -100) {
			getMostPopularWordHeightPage();
		}
		
		list = null;
	
	}

	@Override
	public int getMostPopularVerticalSpaceBetweenWordsPage() {
		
		if (mostPopularVerticalSpaceBetweenWords != -100) {
			return mostPopularVerticalSpaceBetweenWords;
		}
		
		IntegerFrequencyCounter verticalSpaceBetweenWordFrequencyCounter = new IntegerFrequencyCounter(
				1);
		if (list == null)
			list = this.getAllWordBlocks(SpatialOrdering.MIXED_MODE);
		int lastX2 = list.get(0).getX2();
		int firstY2 = list.get(0).getY2();
		int space;
		for (WordBlock block : list) {
			space = block.getX1() - lastX2;
			if (space < 0) {
				verticalSpaceBetweenWordFrequencyCounter.add(block.getY1()
						- firstY2);
				firstY2 = block.getY2();

			}
			lastX2 = block.getX2();
		}

		int mostPopular = verticalSpaceBetweenWordFrequencyCounter
				.getMostPopular();
		double mostPopularCount = verticalSpaceBetweenWordFrequencyCounter
				.getCount(mostPopular);
		int secondMostPopular = verticalSpaceBetweenWordFrequencyCounter
				.getNextMostPopular();
		double secondMostPopularCount = verticalSpaceBetweenWordFrequencyCounter
				.getCount(secondMostPopular);
		double ratio = secondMostPopularCount / mostPopularCount;
		if (secondMostPopular > mostPopular && ratio > 0.8) {
			mostPopularVerticalSpaceBetweenWords = secondMostPopular;
		} else {
			mostPopularVerticalSpaceBetweenWords = mostPopular;
		}

		propagateCalculation();
		
		// System.out.println("Returning mostPopularVerticalSpaceBetweenWords"+mostPopularVerticalSpaceBetweenWords);
		return mostPopularVerticalSpaceBetweenWords;
	}

	@Override
	public int getMostPopularWordWidthPage() {
		if (mostPopularWordWidth != -100) {
			return mostPopularWordWidth;
		}
		IntegerFrequencyCounter avgWordWidthFrequencyCounter = new IntegerFrequencyCounter(
				1);
		if (list == null)
			list = this.getAllWordBlocks(null);

		for (WordBlock block : list)
			avgWordWidthFrequencyCounter.add(block.getWidth());

		int mostPopular = avgWordWidthFrequencyCounter.getMostPopular();
		double mostPopularCount = avgWordWidthFrequencyCounter
				.getCount(mostPopular);
		int secondMostPopular = avgWordWidthFrequencyCounter
				.getNextMostPopular();
		double secondMostPopularCount = avgWordWidthFrequencyCounter
				.getCount(secondMostPopular);
		double ratio = secondMostPopularCount / mostPopularCount;
		if (secondMostPopular > mostPopular && ratio > 0.8) {
			mostPopularWordWidth = secondMostPopular;
		} else {
			mostPopularWordWidth = mostPopular;
		}
		return mostPopularWordWidth;
	}

	@Override
	public int getMostPopularWordHeightPage() {
		if (mostPopularWordHeightPerPage != -100) {
			return mostPopularWordHeightPerPage;
		}
		IntegerFrequencyCounter avgWordWidthFrequencyCounter = new IntegerFrequencyCounter(
				1);
		if (list == null)
			list = this.getAllWordBlocks(null);

		for (WordBlock block : list)
			avgWordWidthFrequencyCounter.add(block.getHeight());

		int mostPopular = avgWordWidthFrequencyCounter.getMostPopular();
		double mostPopularCount = avgWordWidthFrequencyCounter
				.getCount(mostPopular);
		int secondMostPopular = avgWordWidthFrequencyCounter
				.getNextMostPopular();
		double secondMostPopularCount = avgWordWidthFrequencyCounter
				.getCount(secondMostPopular);
		double ratio = secondMostPopularCount / mostPopularCount;
		if (secondMostPopular > mostPopular && ratio > 0.8) {
			mostPopularWordHeightPerPage = secondMostPopular;
		} else {
			mostPopularWordHeightPerPage = mostPopular;
		}

		return mostPopularWordHeightPerPage;

	}

}
