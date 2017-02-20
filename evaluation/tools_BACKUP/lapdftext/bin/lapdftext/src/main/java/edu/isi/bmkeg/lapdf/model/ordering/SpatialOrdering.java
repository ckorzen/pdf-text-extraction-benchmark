package edu.isi.bmkeg.lapdf.model.ordering;

import java.util.Comparator;

import edu.isi.bmkeg.lapdf.features.ChunkFeatures;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class SpatialOrdering implements Comparator<SpatialEntity> {

	public static final String ORIGINAL_MODE = "original";
	public static final String HORIZONTAL_MODE = "horizontal";
	public static final String VERTICAL_MODE = "vertical";
	public static final String MIXED_MODE = "mixed";
	public static final String MIXED_MODE_ABSOLUTE="mixedAbs";
	public static final String COLUMN_AWARE_MIXED_MODE = "camd";
	public static final String PAGE_COLUMN_AWARE_MIXED_MODE = "pcamd";
	public String mode;

	public SpatialOrdering(String mode) {
		this.mode = mode;
	}

	@Override
	public int compare(SpatialEntity o1, SpatialEntity o2) {

		int code = 0;
		
		if (ORIGINAL_MODE.equalsIgnoreCase(mode)) {
			
			code = originalOrdering(o1, o2);
		
		} 
		else if (HORIZONTAL_MODE.equalsIgnoreCase(mode)) {
		
			code = horizontalOrdering(o1, o2);
		
		} 
		else if (VERTICAL_MODE.equalsIgnoreCase(mode)) {
		
			code = verticalOrdering(o1, o2);
		
		} 
		else if (MIXED_MODE.equalsIgnoreCase(mode)) {
		
			code = mixedOrdering(o1, o2);
		
		}
		else if (MIXED_MODE_ABSOLUTE.equalsIgnoreCase(mode)) {
		
			code = mixedOrderingAbsolute(o1, o2);
		
		} 
		else if (COLUMN_AWARE_MIXED_MODE.equalsIgnoreCase(mode)) {
		
			code = camdOrdering(o1, o2);
		
		} 
		else if (PAGE_COLUMN_AWARE_MIXED_MODE.equalsIgnoreCase(mode)) {
		
			code = pcamdOrdering(o1, o2);
		
		} 
		else {
		
			code = 0;
		
		}
		
		return code;

	}
	
	private int originalOrdering(SpatialEntity o1, SpatialEntity o2) {
		
		return o1.getOrder() - o2.getOrder();
	
	}
	
	/** 
	 * To preserve the transitive relation needed for this process. 
	 * We order all blocks simply as midline, left column, right column.
	 * Trying to be fancy here breaks this condition, since a left column element
	 * could be after a midline element but before a right column element that 
	 * could be itself be before the same midline element. 
	 * 
	 * This would break transitivity.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int camdOrdering(SpatialEntity o1, SpatialEntity o2) {

		String o1Alignment = ((Block) o1).readLeftRightMidLine();
		String o2Alignment = ((Block) o2).readLeftRightMidLine();
		
		if (o1Alignment.equals(o2Alignment)) {

			return mixedOrdering(o1, o2);

		} else if (Block.MIDLINE.equalsIgnoreCase(o1Alignment)) {
			
			return -1;

		} else if (Block.MIDLINE.equalsIgnoreCase(o2Alignment)) {

			return 1;

		} else if (Block.LEFT.equalsIgnoreCase(o1Alignment)) {

			return -1;
		
		} else if (Block.RIGHT.equalsIgnoreCase(o1Alignment)) {
		
			return 1;
		
		}

		return 0;
	
	}

	private int pcamdOrdering(SpatialEntity o1, SpatialEntity o2) {

		int pageNumbero1 = ((PageBlock) ((Block) o1).getContainer())
				.getPageNumber();
		int pageNumbero2 = ((PageBlock) ((Block) o2).getContainer())
				.getPageNumber();
		return (pageNumbero1 == pageNumbero2) ? camdOrdering(o1, o2)
				: pageNumbero1 - pageNumbero2;

	}

	private int horizontalOrdering(SpatialEntity o1, SpatialEntity o2) {

		Block b1 = (Block) o1;
		Block b2 = (Block) o2;
		
		if( b1.getPage() != null && b2.getPage() != null ) {
			int p1 = b1.getPage().getPageNumber();
			int p2 = b2.getPage().getPageNumber();
			if( p1 != p2 )
				return p1 - p2;			
		}
		
		return o1.getX1() - o2.getX1();
	
	}

	private int verticalOrdering(SpatialEntity o1, SpatialEntity o2) {
	
		Block b1 = (Block) o1;
		Block b2 = (Block) o2;

		
		int y1Diff = o1.getY1() - o2.getY1();
		
		/*
		
		// Superscripts and subscripts
		if ( Math.abs(y1Diff) < page.getMostPopularWordHeightPage()/2 ) {

			int y2Diff = o1.getY2() - o2.getY2();
			
			if( Math.abs( y2Diff ) < page.getMostPopularWordHeightPage() * 1.5 ) {
				return 0;
			} else {
				return y2Diff;
			}
			
		}*/
			
		return y1Diff;

	}

	
	private int verticalOrderingAbsolute(SpatialEntity o1, SpatialEntity o2) {
		
		int y1Diff = o1.getY1() - o2.getY1();
		if (y1Diff==0)
			return o1.getY2()
					- o2.getY2();
		return y1Diff;
	}
	
	/**
	 * Note: in order to implement the fix for the superscript subscript induced bug 
	 * (fix implemented by Abhishek November 10th)
	 * replace the return mixedOrderingAbsolute with other commented code. 
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int mixedOrdering(SpatialEntity o1, SpatialEntity o2) {
		//return mixedOrderingAbsolute(o1, o2);
		int y = verticalOrdering(o1, o2);
		if (y == 0) {
			return horizontalOrdering(o1, o2);
		} else {
			return y;
		}
	}
	
	private int mixedOrderingAbsolute(SpatialEntity o1, SpatialEntity o2) {
		int y = verticalOrderingAbsolute(o1, o2);
		if (y == 0) {
			return horizontalOrdering(o1, o2);

		} else {
			return y;
		}
	}

	// GULLY TODO: THIS IS VERY IMPORTANT
	private boolean executeHeaderFooterCheck(ChunkBlock o1, ChunkBlock o2) {

		if (o1.isHeaderOrFooter() != null && o1.isHeaderOrFooter())
			return true;

		if (o2.isHeaderOrFooter() != null && o2.isHeaderOrFooter())
			return true;

		if ((o1.isHeaderOrFooter() != null && !o1.isHeaderOrFooter())
				&& (o2.isHeaderOrFooter() != null && !o2.isHeaderOrFooter()))
			return false;

		boolean containsFirstLine1 = false;
		boolean containsFirstLine2 = false;
		boolean numberOfLineOne1 = false;
		boolean numberofLineOne2 = false;
		boolean containsLastLine1 = false;
		boolean containsLastLine2 = false;

		PageBlock p1 = (PageBlock) o1.getContainer();
		PageBlock p2 = (PageBlock) o1.getContainer();
		numberOfLineOne1 = o1.readNumberOfLine() == 1;
		numberofLineOne2 = o2.readNumberOfLine() == 1;

		if (!numberOfLineOne1 && !numberofLineOne2) {
			o1.setHeaderOrFooter(false);
			o2.setHeaderOrFooter(false);
			return false;
		}

		if (numberOfLineOne1) {
			containsFirstLine1 = Math.abs(o1.getY1() - p1.getMargin()[1]) < p1
					.getDocument().readMostPopularWordHeight();
			if (containsFirstLine1) {
				o1.setHeaderOrFooter(true);
				return true;
			}
			containsLastLine1 = Math.abs(o1.getY2() - p1.getMargin()[3]) < p1
					.getDocument().readMostPopularWordHeight();
			if (containsLastLine1) {
				o1.setHeaderOrFooter(true);
				return true;
			}
			o1.setHeaderOrFooter(false);
		}

		if (numberofLineOne2) {
			containsFirstLine2 = Math.abs(o2.getY1() - p2.getMargin()[1]) < p2
					.getDocument().readMostPopularWordHeight();
			if (containsFirstLine2) {
				o2.setHeaderOrFooter(true);
				return true;
			}
			containsLastLine2 = Math.abs(o2.getY2() - p2.getMargin()[3]) < p2
					.getDocument().readMostPopularWordHeight();
			if (containsLastLine2) {
				o2.setHeaderOrFooter(true);
				return true;
			}
			o2.setHeaderOrFooter(false);
		}

		return false;

	}

}
