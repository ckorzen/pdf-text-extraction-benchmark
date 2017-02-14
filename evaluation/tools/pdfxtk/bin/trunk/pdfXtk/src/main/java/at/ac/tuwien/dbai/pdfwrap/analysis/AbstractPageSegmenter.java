/**
 * pdfXtk - PDF Extraction Toolkit
 * Copyright (c) by the authors/contributors.  All rights reserved.
 * This project includes code from PDFBox and TouchGraph.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the names pdfXtk or PDF Extraction Toolkit; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://pdfxtk.sourceforge.net
 *
 */
package at.ac.tuwien.dbai.pdfwrap.analysis;

import at.ac.tuwien.dbai.pdfwrap.comparators.YComparator;
import at.ac.tuwien.dbai.pdfwrap.comparators.EdgeAttributeComparator;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyGraph;
import at.ac.tuwien.dbai.pdfwrap.utils.ListUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Abstract page segmenter framework for implementation
 * of page segmentation algorithms
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser 0.9
 */
public abstract class AbstractPageSegmenter {
    private static final Log log = LogFactory.getLog( AbstractPageSegmenter.class );

	protected int maxIterations = Integer.MAX_VALUE;

	protected List<GenericSegment> allSegments;
	protected List<GenericSegment> unusedSegments;

	protected List<AdjacencyEdge<GenericSegment>> allEdges;
	protected List<AdjacencyEdge<GenericSegment>> priorityEdges; // sorted
																	// version
																	// of edges
	protected AdjacencyGraph<? extends GenericSegment> ag;

	protected HashMap<GenericSegment, CandidateCluster> clustHash;
	protected HashMap<GenericSegment, List<GenericSegment>> vertNeighbourMap;

	// List<GenericSegment> items;

	public abstract Comparator<AdjacencyEdge<? extends GenericSegment>> edgeComparator();

	public abstract int clusterTogether(AdjacencyEdge<GenericSegment> ae,
			CandidateCluster clustFrom, CandidateCluster clustTo);

	public abstract boolean isValidCluster(CandidateCluster c);

	public abstract boolean horizSkip();

	public abstract boolean doSwallow();

	public abstract boolean doOverlap();

	public abstract boolean neighbourMap();

	protected boolean isDebugMode() {
		return false;
	}

	// float maxH = 0.0f;

	protected boolean checkHashes(Collection cols, Collection values) {
		if (cols.size() == values.size()) {
			Iterator itemIter = cols.iterator();
			while (itemIter.hasNext()) {
				GenericSegment item = (GenericSegment) itemIter.next();
				if (!values.contains(item))
					return true;
			}
			return false;
		} else {
			return true;
		}
	}

	protected boolean inSwallowGroup(CandidateCluster c,
			List<GenericSegment> swallowedItems) {
		if (doSwallow())
			return true;

		List<GenericSegment> swallowedSegments = new ArrayList<GenericSegment>();
		for (GenericSegment o : swallowedItems) {
			if (!c.getItems().contains(o))
				swallowedSegments.add((TextSegment) o);
		}

		// now we need to make sure that each new segment
		// can (will) be added to c anyway

		for (GenericSegment gs : swallowedItems) {
			TextSegment s = (TextSegment) gs;
			// we need to see whether unusedEdges contains
			// an edge between any item in c and s

			// EdgeList subList = (EdgeList)unusedEdges.getEdges(s);
			// s should not be a member of c.getItems() (can't see how that
			// could happen...)
			boolean foundMemberOfCGetItems = false;
			// Iterator j = subList.iterator();
			// while(j.hasNext())
			for (AdjacencyEdge<GenericSegment> e : priorityEdges) {
				// AdjacencyEdge e = (AdjacencyEdge)j.next();

				if (e.getNodeFrom() == s || e.getNodeTo() == s) {
					if (c.getItems().contains(e.getNodeFrom()))
						foundMemberOfCGetItems = true;
					if (c.getItems().contains(e.getNodeTo()))
						foundMemberOfCGetItems = true;
				}
			}
			if (foundMemberOfCGetItems == false)
				return false;
		}

		return true;
	}

	public List<TextBlock> clusterLinesIntoTextBlocks(
			AdjacencyGraph<? extends GenericSegment> lineAG) {
		List<TextBlock> retVal = new ArrayList<TextBlock>();
		List<CandidateCluster> l = orderedEdgeCluster(lineAG);

		for (CandidateCluster c : l) {
			TextBlock tb = new TextBlock(c.getX1(), c.getX2(), c.getY1(),
					c.getY2(), c.getText(), c.getFontName(), c.getFontSize());
			tb.setLineSpacing(c.getRelLineSpacing());
			for (TextSegment ts : c.getItems())
				tb.getItems().add((TextLine) ts); // NOTE: not type-safe;
													// crashes here if given
													// e.g. TextFragments as
													// input
			retVal.add(tb);
		}
		return retVal;
	}

	public List<TextBlock> clusterFragsIntoTextBlocks(
			AdjacencyGraph<? extends GenericSegment> lineAG) {
		List<TextBlock> retVal = new ArrayList<TextBlock>();
		List<CandidateCluster> l = orderedEdgeCluster(lineAG);

		for (CandidateCluster c : l) {
			TextBlock tb = new TextBlock(c.getX1(), c.getX2(), c.getY1(),
					c.getY2(), c.getText(), c.getFontName(), c.getFontSize());
			tb.setLineSpacing(c.getRelLineSpacing());
			for (TextSegment ts : c.getItems()) {
				TextFragment tf = (TextFragment) ts;
				LineFragment lf = new LineFragment();
				lf.getItems().add(tf);
				lf.setCalculatedFields(tf);
				TextLine tl = new TextLine();
				tl.getItems().add(lf);
				tl.setCalculatedFields(lf);
				tb.getItems().add(tl); // NOTE: not type-safe; crashes here if
										// given e.g. TextFragments as input
			}
			retVal.add(tb);
		}
		return retVal;
	}

	public List<TextBlock> clusterCharsIntoTextBlocks(
			AdjacencyGraph<? extends GenericSegment> lineAG) {
		List<TextBlock> retVal = new ArrayList<TextBlock>();
		List<CandidateCluster> l = orderedEdgeCluster(lineAG);

		for (CandidateCluster c : l) {
			TextBlock tb = new TextBlock(c.getX1(), c.getX2(), c.getY1(),
					c.getY2(), c.getText(), c.getFontName(), c.getFontSize());
			tb.setLineSpacing(c.getRelLineSpacing());
			for (TextSegment ts : c.getItems()) {
				CharSegment cs = (CharSegment) ts;
				TextFragment tf = new TextFragment();
				tf.getItems().add(cs);
				tf.setCalculatedFields(cs);
				LineFragment lf = new LineFragment();
				lf.getItems().add(tf);
				lf.setCalculatedFields(tf);
				TextLine tl = new TextLine();
				tl.getItems().add(lf);
				tl.setCalculatedFields(lf);
				tb.getItems().add(tl); // NOTE: not type-safe; crashes here if
										// given e.g. TextFragments as input
			}
			retVal.add(tb);
		}
		return retVal;
	}

	// NOTE: when starting with a partial segmentation, clustHash should be set
	// otherwise pass a null value
	public void initializeSegmenter(
			AdjacencyGraph<? extends GenericSegment> ag,
			HashMap<GenericSegment, CandidateCluster> clustHash) {
		if (clustHash != null)
			this.clustHash = clustHash;

		this.ag = ag;
		if (maxIterations <= 0)
			maxIterations = Integer.MAX_VALUE;
		else
			System.out.println("running with " + maxIterations + " iterations");

		long startProcess = System.currentTimeMillis();
		long t = System.currentTimeMillis();

		// SegmentList unusedSegments =
		// (SegmentList)pageFromLines.getItems().clone();
		List<CandidateCluster> retVal = new ArrayList<CandidateCluster>();

		unusedSegments = new ArrayList<GenericSegment>();
		allSegments = new ArrayList<GenericSegment>();

		for (GenericSegment s : ag.getVertSegmentList()) {
			allSegments.add(s);
			unusedSegments.add(s);
		}

		priorityEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		allEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		// optimizationEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		for (AdjacencyEdge<?> e : ag.getEdges()) {
			// if (e.isHorizontal())
			if (e.isVertical()) {
				AdjacencyEdge<GenericSegment> aegs = new AdjacencyEdge<GenericSegment>(
						e.getNodeFrom(), e.getNodeTo(), e.getDirection(),
						e.getWeight());

				priorityEdges.add(aegs);
				allEdges.add(aegs);
			}
		}
		
		Collections.sort(priorityEdges, edgeComparator());
		// priorityEdges.removeDuplicateEdges();

		List<AdjacencyEdge<GenericSegment>> edgesToRemove = new ArrayList<AdjacencyEdge<GenericSegment>>();
		int count = 0;
		for (AdjacencyEdge<GenericSegment> ae : priorityEdges) {
			count++;
			if (count > maxIterations)
				edgesToRemove.add(ae);
		}
		priorityEdges.removeAll(edgesToRemove);

//		System.out.println("reduced size to: " + priorityEdges.size());

		// added 2011-10-28
		// initialize vertNeighbourMap -- used by some clusterTogether methods
		// however, this map is only populated by demand
		vertNeighbourMap = new HashMap<GenericSegment, List<GenericSegment>>();
	}

	/**
	 * greedy, best-first page segmentation algorithm
	 */
	public List<CandidateCluster> orderedEdgeCluster(
			AdjacencyGraph<? extends GenericSegment> lineAG) {
		initializeSegmenter(lineAG, null);

//		ListUtils.printList(priorityEdges);

//		System.out.println("allSegments: " + allSegments);
		SegmentationResult retInt = processEdges(new SegmentationResult(
				allSegments), priorityEdges);
		// , new FirstPassSegmentationRules());

		return retInt.getSegments();
	}

	/*
	 * replaced by the short form!
	 * 
	 * public List<CandidateCluster> orderedEdgeCluster (AdjacencyGraph<?
	 * extends GenericSegment> lineAG)//, { if (maxIterations <= 0)
	 * maxIterations = Integer.MAX_VALUE; else
	 * System.out.println("running with " + maxIterations + " iterations");
	 * 
	 * long startProcess = System.currentTimeMillis(); long t =
	 * System.currentTimeMillis();
	 * 
	 * // SegmentList unusedSegments =
	 * (SegmentList)pageFromLines.getItems().clone(); List<CandidateCluster>
	 * retVal = new ArrayList<CandidateCluster>();
	 * 
	 * unusedSegments = new ArrayList<GenericSegment>(); allSegments = new
	 * ArrayList<GenericSegment>();
	 * 
	 * for (GenericSegment s : lineAG.getVertSegmentList()) {
	 * allSegments.add(s); unusedSegments.add(s); }
	 * 
	 * // now, with vertical edges, seems to give different results without
	 * clone :) // EdgeList priorityEdges = (EdgeList)allEdges.clone(); // don't
	 * need to clone? maybe do? // 2011-01-26: why can't you do this? //
	 * List<AdjacencyEdge<? extends GenericSegment>> priorityEdges =
	 * lineAG.getEdges();//.clone();
	 * 
	 * priorityEdges = new ArrayList<AdjacencyEdge<GenericSegment>>(); allEdges
	 * = new ArrayList<AdjacencyEdge<GenericSegment>>(); for (AdjacencyEdge<?> e
	 * : lineAG.getEdges()) { AdjacencyEdge<GenericSegment> aegs = new
	 * AdjacencyEdge<GenericSegment> (e.getNodeFrom(), e.getNodeTo(),
	 * e.getDirection(), e.getWeight());
	 * 
	 * priorityEdges.add(aegs); allEdges.add(aegs); }
	 * 
	 * Collections.sort(priorityEdges, edgeComparator());
	 * //priorityEdges.removeDuplicateEdges();
	 * 
	 * clustHash = new HashMap<GenericSegment, CandidateCluster>();
	 * vertNeighbourMap = new HashMap<GenericSegment, List<GenericSegment>>();
	 * 
	 * // System.out.println("priorityEdges.size: " + priorityEdges.size()); //
	 * System.out.println("lineAGEdges.size: " + lineAG.getEdges().size());
	 * 
	 * int iteration = 0;
	 * 
	 * t = System.currentTimeMillis();
	 * 
	 * while(priorityEdges.size() > 0 && iteration < maxIterations) { long start
	 * = System.currentTimeMillis();
	 * 
	 * AdjacencyEdge<GenericSegment> ae = priorityEdges.remove(0);
	 * 
	 * if (ae.getNodeFrom() instanceof TextSegment && ae.getNodeTo() instanceof
	 * TextSegment) { TextSegment segFrom = (TextSegment)ae.getNodeFrom();
	 * TextSegment segTo = (TextSegment)ae.getNodeTo(); float lineSpacing =
	 * ae.physicalLength() / ae.getFontSize();
	 * 
	 * iteration ++; if (iteration % 1000 == 0) System.out.println("Iteration: "
	 * + iteration + " of " + allEdges.size());
	 * 
	 * if (isDebugMode()) System.out.println("Examining edge: " + ae +
	 * " with length: " + ae.physicalLength());
	 * 
	 * if (clustHash.get(segFrom) == null && clustHash.get(segTo) == null) { if
	 * (isDebugMode()) System.out.println("one"); if (clusterTogether(ae, null,
	 * null)) { if (isDebugMode()) System.out.println("two");
	 * List<GenericSegment> swallowedSegments = swallow(createList(segFrom),
	 * createList(segTo));
	 * 
	 * if (ae.isVertical() || ae.isHorizontal() && swallowedSegments.size() <=
	 * 2) { if (isDebugMode()) System.out.println("three"); CandidateCluster
	 * newc = makeCluster(swallowedSegments);
	 * 
	 * if (isValidCluster(newc)) { if (isDebugMode())
	 * System.out.println("four"); t = System.currentTimeMillis();
	 * updateHashes(newc, retVal, neighbourMap() && ae.isVertical()); t =
	 * System.currentTimeMillis(); } } } // else do nothing } else if
	 * (clustHash.get(segFrom) == null) { if (isDebugMode())
	 * System.out.println("five"); CandidateCluster c = clustHash.get(segTo); if
	 * (clusterTogether(ae, null, c)) { if (isDebugMode())
	 * System.out.println("six"); List<GenericSegment> swallowedSegments =
	 * swallow(cloneList(c.getItems()), createList(segFrom));
	 * 
	 * if (ae.isVertical() || ae.isHorizontal() && inSwallowGroup(c,
	 * swallowedSegments)) { if (isDebugMode()) System.out.println("seven");
	 * CandidateCluster newc = makeCluster(swallowedSegments); if
	 * (isValidCluster(newc)) { if (isDebugMode()) System.out.println("eight");
	 * updateHashes(newc, retVal, neighbourMap() && ae.isVertical()); t =
	 * System.currentTimeMillis(); retVal.remove(c); } } } } else if
	 * (clustHash.get(segTo) == null) { if (isDebugMode())
	 * System.out.println("nine"); CandidateCluster c = clustHash.get(segFrom);
	 * if (clusterTogether(ae, c, null)) { if (isDebugMode())
	 * System.out.println("ten"); List<GenericSegment> swallowedSegments =
	 * swallow(cloneList(c.getItems()), createList(segTo));
	 * 
	 * // check if the addition doesn't swallow any additional elements if
	 * (ae.isVertical() || ae.isHorizontal() && inSwallowGroup(c,
	 * swallowedSegments)) { if (isDebugMode()) System.out.println("eleven");
	 * CandidateCluster newc = makeCluster(swallowedSegments); if
	 * (isValidCluster(newc)) { if (isDebugMode()) System.out.println("twelve");
	 * updateHashes(newc, retVal, neighbourMap() && ae.isVertical());
	 * retVal.remove(c); } } } } else // both segments already used, merge { if
	 * (isDebugMode()) System.out.println("thirteen"); // only possibility for
	 * horizontal edge, as all other segments added // as singletons by now
	 * 
	 * t = System.currentTimeMillis(); // merge the two clusters if compatible
	 * CandidateCluster c1 = clustHash.get(segFrom); CandidateCluster c2 =
	 * clustHash.get(segTo);
	 * 
	 * boolean skip = false;
	 * 
	 * // commented out after PDF-TREX comparison 17.07.10
	 * 
	 * // correction 21.07.10 // if (ae.isHorizontal()) skip = true; if
	 * (ae.isHorizontal() && horizSkip()) skip = true;
	 * 
	 * if (c1 == c2) skip = true; // in clusterTogether -- redundant!
	 * 
	 * if (!skip) { if (isDebugMode()) System.out.println("thirteenandahalf");
	 * if (isDebugMode()) System.out.println("c1: " + c1); if (isDebugMode())
	 * System.out.println("c2: " + c2); if (clusterTogether(ae, c1, c2)) { if
	 * (isDebugMode()) System.out.println("fourteen"); // check if the addition
	 * doesn't swallow any additional elements List<GenericSegment>
	 * swallowedSegments = swallow(cloneList(c1.getItems()),
	 * cloneList(c2.getItems())); if (ae.isVertical() || ae.isHorizontal() &&
	 * swallowedSegments.size() <= c1.getItems().size() + c2.getItems().size())
	 * { if (isDebugMode()) System.out.println("fifteen"); CandidateCluster newc
	 * = makeCluster(swallowedSegments); if (isValidCluster(newc)) { if
	 * (isDebugMode()) System.out.println("sixteen"); updateHashes(newc, retVal,
	 * neighbourMap() && ae.isVertical()); if (isDebugMode())
	 * System.out.println("16a"); newc.findBoundingBox();
	 * newc.setFontSize(ae.getFontSize()); if (isDebugMode())
	 * System.out.println("16b"); //newc.setLineSpacing(lineSpacing); // removed
	 * 29.10.10 newc.setCalculatedFields(); // 29.10.10 retVal.remove(c2);
	 * retVal.remove(c1); if (isDebugMode()) System.out.println("16c"); if
	 * (ae.isHorizontal()) { // 2011-01-26 TEMPORARILY COMMENTED OUT
	 * 
	 * } } } } } else { // part of same cluster; not merging :) } } } }
	 * 
	 * if (priorityEdges.size() == 0) // don't add singletons if in 'watch' mode
	 * :) { if (isDebugMode()) System.out.println("remaining singletons: " +
	 * priorityEdges.size()); // add remaining singletons for (GenericSegment s
	 * : allSegments) { if (clustHash.get(s) == null) { CandidateCluster c =
	 * makeCluster(createList(s)); //newc.setLineSpacing(lineSpacing); //
	 * removed 29.10.10 c.setCalculatedFields(); clustHash.put(s, c);
	 * retVal.add(c); //only for testing when bailing out of method here... } }
	 * }
	 * 
	 * if (Utils.DISPLAY_TIMINGS)
	 * System.out.println("Total time for clustering: " +
	 * (System.currentTimeMillis() - startProcess));
	 * 
	 * 
	 * return retVal; }
	 */

	// clones too
	public SegmentationResult processEdges(SegmentationResult sr,
			List<AdjacencyEdge<GenericSegment>> selectedEdges)// ,
																// ISegmentationRules
																// rules)
	{
		SegmentationResult cloneSegResult = sr.clone();

		int counter = 0;
		for (AdjacencyEdge<GenericSegment> ae : selectedEdges) {
			counter++;

			if (counter <= maxIterations) {
				if (isDebugMode())
					System.out
							.println("processing edge " + counter + ": " + ae);
				// if (counter % 100 == 0)
				// System.out.println("processing edge " + counter + ": " + ae);

				processEdge(ae, cloneSegResult);// , rules);

				// and find redundant edges...
				// find which items were SWALLOWED -- these should take us
				// directly to the edges
				// ListUtils.removeDuplicates(sr.joinedEdges);
			}
		}

		return cloneSegResult;
	}

	// clones too
	public SegmentationResult processEdgesResort(SegmentationResult sr,
			List<AdjacencyEdge<GenericSegment>> selectedEdges)// ,
																// ISegmentationRules
																// rules)
	{
		SegmentationResult cloneSegResult = sr.clone();

		int counter = 0;

		List<AdjacencyEdge<GenericSegment>> cloneEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		for (AdjacencyEdge<GenericSegment> ae : selectedEdges)
			cloneEdges.add(ae);

		while (cloneEdges.size() > 0) {
			// Collections.sort(selectedEdges, rules.edgeComparator());
			Collections.sort(selectedEdges, this.edgeComparator());
			AdjacencyEdge<GenericSegment> ae = cloneEdges.remove(0);
			counter++;
			if (isDebugMode())
				System.out.println("processing edge sort " + counter + ": "
						+ ae);
			processEdge(ae, cloneSegResult);// , rules);
		}

		return cloneSegResult;
	}

	// TODO
	// (static)
	public void processEdge(AdjacencyEdge<GenericSegment> ae,
			SegmentationResult sr)// , ISegmentationRules rules)
	{
		TextSegment segFrom = (TextSegment) ae.getNodeFrom();
		TextSegment segTo = (TextSegment) ae.getNodeTo();

		if (sr.clustHash.get(segFrom) == null
				&& sr.clustHash.get(segTo) == null) {
			if (isDebugMode())
				System.out.println("one");
			int ctTest = // rules.clusterTogether(ae, null, null, sr);
			clusterTogether(ae, null, null);// , sr);
			if (ctTest == 1) {
				if (isDebugMode())
					System.out.println("two");
				List<AdjacencyEdge<GenericSegment>> swallowedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
				if (!doSwallow())
					swallowedEdges.add(ae);
				List<GenericSegment> swallowedSegments = swallow(
						createList(segFrom), createList(segTo), sr.clustHash,
						swallowedEdges); // clustHash not modified; only lookup

				if (isDebugMode())
					System.out.println("three");
				CandidateCluster newc = makeCluster(swallowedSegments);

				if (isValidCluster(newc)) {
					if (isDebugMode())
						System.out.println("four");
					sr.addSegmentUpdateHash(newc);
					sr.addJoinedEdges(swallowedEdges);
				}
			} else if (ctTest == 0) {
				sr.remainingEdges.add(ae);
			}
			// else do nothing
		} else if (sr.clustHash.get(segFrom) == null) {
			if (isDebugMode())
				System.out.println("five");
			CandidateCluster c = sr.clustHash.get(segTo);
			int ctTest = // rules.clusterTogether(ae, null, c, sr);
			clusterTogether(ae, null, c);// , sr);
			if (ctTest == 1) {
				if (isDebugMode())
					System.out.println("six");
				List<AdjacencyEdge<GenericSegment>> swallowedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
				if (!doSwallow())
					swallowedEdges.add(ae);
				List<GenericSegment> swallowedSegments = swallow(
						cloneList(c.getItems()), createList(segFrom),
						sr.clustHash, swallowedEdges); // clustHash not
														// modified; only lookup

				if (isDebugMode())
					System.out.println("seven");
				CandidateCluster newc = makeCluster(swallowedSegments);

				if (isValidCluster(newc)) {
					if (isDebugMode())
						System.out.println("eight");
					sr.addSegmentUpdateHash(newc);
					sr.addJoinedEdges(swallowedEdges);
					sr.segments.remove(c);
				}
			} else if (ctTest == 0) {
				sr.remainingEdges.add(ae);
			}
		} else if (sr.clustHash.get(segTo) == null) {
			System.out.println("segTo: " + segTo);
			System.out.println("null reached, containskey: "
					+ sr.clustHash.containsKey(segTo));

			if (isDebugMode())
				System.out.println("nine");
			CandidateCluster c = sr.clustHash.get(segFrom);
			int ctTest = // rules.clusterTogether(ae, c, null, sr);
			clusterTogether(ae, c, null);// , sr);
			if (ctTest == 1) {
				if (isDebugMode())
					System.out.println("ten");
				List<AdjacencyEdge<GenericSegment>> swallowedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
				if (!doSwallow())
					swallowedEdges.add(ae);
				List<GenericSegment> swallowedSegments = swallow(
						cloneList(c.getItems()), createList(segTo),
						sr.clustHash, swallowedEdges);

				// check if the addition doesn't swallow any additional elements
				if (isDebugMode())
					System.out.println("eleven");
				CandidateCluster newc = makeCluster(swallowedSegments);
				if (isValidCluster(newc)) {
					if (isDebugMode())
						System.out.println("twelve");
					sr.addSegmentUpdateHash(newc);
					sr.addJoinedEdges(swallowedEdges);
					sr.segments.remove(c);
				}
			} else if (ctTest == 0) {
				sr.remainingEdges.add(ae);
			}
		} else // both segments already used, merge
		{
			if (isDebugMode())
				System.out.println("thirteen");
			// only possibility for horizontal edge, as all other segments added
			// as singletons by now

			// merge the two clusters if compatible
			CandidateCluster c1 = sr.clustHash.get(segFrom);
			CandidateCluster c2 = sr.clustHash.get(segTo);

			boolean skip = false;

			// if (ae.isHorizontal() && horizSkip()) skip = true;

			if (c1 == c2)
				skip = true; // in clusterTogether -- redundant!

			if (!skip) {
				if (isDebugMode())
					System.out.println("thirteenandahalf");
				if (isDebugMode())
					System.out.println("c1: " + c1);
				if (isDebugMode())
					System.out.println("c2: " + c2);
				int ctTest = // rules.clusterTogether(ae, c1, c2, sr);
				clusterTogether(ae, c1, c2);// , sr);
				if (ctTest == 1) {
					if (isDebugMode())
						System.out.println("fourteen");

					List<AdjacencyEdge<GenericSegment>> swallowedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
					if (!doSwallow())
						swallowedEdges.add(ae);
					List<GenericSegment> swallowedSegments = swallow(
							cloneList(c1.getItems()), cloneList(c2.getItems()),
							sr.clustHash, swallowedEdges);

					// check if the addition doesn't swallow any additional
					// elements
					// if (ae.isVertical() || ae.isHorizontal() &&
					// swallowedSegments.size() <= c1.getItems().size() +
					// c2.getItems().size())
					if (true) {
						if (isDebugMode())
							System.out.println("fifteen");
						CandidateCluster newc = makeCluster(swallowedSegments);
						if (isValidCluster(newc)) {
							if (isDebugMode())
								System.out.println("sixteen");
							sr.addSegmentUpdateHash(newc);
							sr.addJoinedEdges(swallowedEdges);
							if (isDebugMode())
								System.out.println("16a");
							newc.findBoundingBox();
							newc.setFontSize(ae.avgFontSize());
							if (isDebugMode())
								System.out.println("16b");
							// newc.setLineSpacing(lineSpacing); // removed
							// 29.10.10
							newc.setCalculatedFields(); // 29.10.10
							sr.segments.remove(c2);
							sr.segments.remove(c1);
							if (isDebugMode())
								System.out.println("16c");

						}
					}
				} else if (ctTest == 0) {
					sr.remainingEdges.add(ae);
				}
			} else {
				// part of same cluster; not merging :)
			}
		}
	}

	protected static List<GenericSegment> findNearestVerticalNeighbours(
			GenericSegment c, List<AdjacencyEdge<GenericSegment>> allEdges,
			HashMap<GenericSegment, List<GenericSegment>> vertNeighbourMap) {
		if (vertNeighbourMap.containsKey(c)) {
			return vertNeighbourMap.get(c);
		} else {
			// find lowest neighbourAbove and highest neighbourBelow
			GenericSegment lowestNeighbourAbove = null;
			GenericSegment highestNeighbourBelow = null;

			// Iterator edgeIter = priorityEdges.iterator();
			Iterator edgeIter = allEdges.iterator();
			while (edgeIter.hasNext()) {
				AdjacencyEdge ae = (AdjacencyEdge) edgeIter.next();
				GenericSegment segFrom = (GenericSegment) ae.getNodeFrom();
				GenericSegment segTo = (GenericSegment) ae.getNodeTo();

				if (ae.isVertical()) {
					// we can assume that the bounding box of the current
					// cluster is correct
					// edge points into or out of cluster
					if (c == segFrom && c != segTo) {
						// segTo is the outside element
						if (segTo.getYmid() > c.getY2()) {
							// segTo is above the cluster
							if (lowestNeighbourAbove == null
									|| segTo.getYmid() < lowestNeighbourAbove
											.getYmid()) {
								lowestNeighbourAbove = segTo;
							}
						} else if (segTo.getYmid() < c.getY1()) {
							// segTo is below the cluster
							if (highestNeighbourBelow == null
									|| segTo.getYmid() > highestNeighbourBelow
											.getYmid()) {
								highestNeighbourBelow = segTo;
							}
						} else {
							// do nothing if within boundary of cluster
							// but not swallowed for some reason
						}
					} else if (c != segFrom && c == segTo) {
						// segFrom is the outside element
						if (segFrom.getYmid() > c.getY2()) {
							// segTo is above the cluster
							if (lowestNeighbourAbove == null
									|| segFrom.getYmid() < lowestNeighbourAbove
											.getYmid()) {
								lowestNeighbourAbove = segFrom;
							}
						} else if (segFrom.getYmid() < c.getY1()) {
							// segTo is below the cluster
							if (highestNeighbourBelow == null
									|| segFrom.getYmid() > highestNeighbourBelow
											.getYmid()) {
								highestNeighbourBelow = segFrom;
							}
						} else {
							// do nothing if within boundary of cluster
							// but not swallowed for some reason
						}
					}
				}
			}

			// System.out.println("in foo section");
			// System.out.println("lowestNeighbourAbove: " +
			// lowestNeighbourAbove);
			// System.out.println("highestNeighbourBelow: " +
			// highestNeighbourBelow);

			List<GenericSegment> retVal = new ArrayList<GenericSegment>();
			retVal.add(lowestNeighbourAbove);
			retVal.add(highestNeighbourBelow);
			vertNeighbourMap.put(c, retVal);
			return retVal;
		}
	}

	protected List<GenericSegment> swallow(List<GenericSegment> l1,
			List<GenericSegment> l2) {
		return swallow(l1, l2, this.clustHash, null);
	}

	protected List<GenericSegment> swallow(List<GenericSegment> l1,
			List<GenericSegment> l2,
			HashMap<GenericSegment, CandidateCluster> clustHash,
			List<AdjacencyEdge<GenericSegment>> redundantEdges) {
		/*
		 * if (!rules.swallow()) { // override swallow method to return just the
		 * items, no swallowing
		 * 
		 * List<GenericSegment> retVal = new ArrayList<GenericSegment>();
		 * retVal.addAll(l1); retVal.addAll(l2); return retVal; }
		 */

		// TODO: RETURN LIST OF SWALLOWED ITEMS
		CompositeSegment<GenericSegment> temp = new CompositeSegment<GenericSegment>();
		temp.getItems().addAll(l1);
		temp.getItems().addAll(l2);
		temp.findBoundingBox();

		List<GenericSegment> swallowedItems = new ArrayList<GenericSegment>();

		// long startTime = System.currentTimeMillis();

		if (!doSwallow()) {
			if (doOverlap()) {
				boolean loop = true;
				while (loop) {
					boolean changeMade = false;
					// swallowedItems =
					// ListUtils.findElementsIntersectingBBox(items, temp);

					List<GenericSegment> itemsToAdd = new ArrayList<GenericSegment>();

					for (GenericSegment gs : temp.getItems()) // loop through
																// items in seg
					{
						for (GenericSegment gs2 : allSegments) // loop through
																// _other_ items
																// on page
						{
							if (!temp.getItems().contains(gs2)) {
								if (gs != gs2
										&& SegmentUtils.intersects(gs2, gs)) {
									itemsToAdd.add(gs2);
									temp.growBoundingBox(gs2);
									changeMade = true;
								}
							}
						}
					}
					temp.getItems().addAll(itemsToAdd);
					if (!changeMade)
						loop = false;
				}
			}
			// System.out.println("finished swallow in: " +
			// (System.currentTimeMillis() - startTime));
			return temp.getItems();
		} else {
			boolean loop = true;
			while (loop) {
				// System.out.println("allsegments.size: " +
				// allSegments.size());
				// System.out.println("temp: " + temp.toExtendedString());
				swallowedItems =
				// items.getElementsWithCentresWithinBBoxOrViceVersa(temp);
				ListUtils.findElementsIntersectingBBox(allSegments, temp);

				List<GenericSegment> newItems = new ArrayList<GenericSegment>();

				for (GenericSegment gs : swallowedItems) {
					if (clustHash.get(gs) != null) // if belongs to another
													// cluster
					{
						CandidateCluster clust = clustHash.get(gs);
						newItems.addAll(clust.getItems());
						// System.out.println("adding cc items: " +
						// clust.toExtendedString());
					}
				}

				swallowedItems.addAll(newItems);
				ListUtils.removeDuplicates(swallowedItems);

				if (temp.getItems().size() == swallowedItems.size()) {
					// no items swallowed
					loop = false;
				} else // elsepart added 18.05.07
				{
					// commented out 6.06.07
					// if (newSegment instanceof Cluster) return true;
				}
				temp.setItems(swallowedItems);
				temp.findBoundingBox();
			}

			if (redundantEdges != null) // if valid method passed
				for (AdjacencyEdge<GenericSegment> ae : allEdges) {
					if (swallowedItems.contains(ae.getNodeFrom())
							&& swallowedItems.contains(ae.getNodeTo()))
						redundantEdges.add(ae);
				}

			return swallowedItems;
		}
	}

	// TODO: This method will crash if the input list contains non-TextSegments
	// 2011-10-27 changed to static!
	protected static CandidateCluster makeCluster(List<GenericSegment> items) {
		CandidateCluster retVal = new CandidateCluster();
		for (GenericSegment gs : items)
			retVal.getItems().add((TextSegment) gs);
		retVal.findFontSize(); // added 13.08.08
		// retVal.findLinesWidth(); // this method uses pnglf and the entire
		// width
		// Collections.sort(retVal.getFoundLines(), new YComparator());

		CandidateCluster tempClust = new CandidateCluster();
		for (GenericSegment gs : items)
			tempClust.getItems().add((TextSegment) gs);
		// tempClust.flattenByOneLevel();
		tempClust.findLines(Float.MAX_VALUE); // 14.08.08 this method ensures
												// that the resulting lines are
												// SORTED
												// but IntegrateLines destroys
												// this... :(
		retVal.setFoundLines(tempClust.getFoundLines());
		// above doesn't work due to NPE, but need to fix it soon TODO

		// TODO: Integrate lines?
		// retVal.integrateLines();

		Collections.sort(retVal.getFoundLines(), new YComparator());
		retVal.findBoundingBox();
		return retVal;
	}

	// pre: foundLines and fontSize are set
	// TODO: what when the line spacing is different? replace fontSize for
	// vertical with lineSpacing?
	// TODO: consider the shape of the gap and generate a score?
	public static boolean checkForChasms(CandidateCluster cts) {
		float minChasmHeight = 3.5f;
		float minChasmWidth = 0.5f;

		List<List<GenericSegment>> lineGaps = findLineGaps(cts, minChasmWidth
				* cts.getFontSize());

		// TODO: sort out this static shit!
		List<GenericSegment> gaps = mergeLineGaps(lineGaps,
				minChasmWidth * cts.getFontSize(),
				minChasmHeight * cts.getFontSize());

		for (GenericSegment gap : gaps) {
			if ((gap.getWidth() > minChasmWidth * cts.getFontSize())
					&& (gap.getHeight() > minChasmHeight * cts.getFontSize()))
				return true;
		}
		return false;
	}

	// pre: foundLines must be set
	// returns a SegmentList of SegmentLists of GenericSegments
	public static List<List<GenericSegment>> findLineGaps(CandidateCluster cts,
			float minWidth) {
		List<List<GenericSegment>> retVal = new ArrayList<List<GenericSegment>>();
		for (CompositeSegment<? extends GenericSegment> l : cts.getFoundLines()) {
			List<GenericSegment> lineGaps = new ArrayList<GenericSegment>();
			for (int n = 1; n < l.getItems().size(); n++) {
				GenericSegment a = l.getItems().get(n - 1);
				GenericSegment b = l.getItems().get(n);

				// assume that a and b intersect; and that b is to the right of
				// a
				if ((b.getX1() - a.getX2()) > minWidth) {
					float newY1 = Utils.maximum(a.getY1(), b.getY1());
					float newY2 = Utils.minimum(a.getY2(), b.getY2());
					GenericSegment gapSeg = new GenericSegment(a.getX2(),
							b.getX1(), newY1, newY2);
					lineGaps.add(gapSeg);
				}
			}
			retVal.add(lineGaps);
		}
		return retVal;
	}

	// TODO: for speedup, find a way to make this work with iterators instead
	// of for/next loops.
	public static List<GenericSegment> mergeLineGaps(
			List<List<GenericSegment>> lineGaps, float minWidth, float minHeight) {
		List<GenericSegment> retVal = new ArrayList<GenericSegment>();
		for (int n = 1; n <= lineGaps.size(); n++) {
			List<GenericSegment> thisLineGaps = lineGaps.get(n - 1);
			List<GenericSegment> nextLineGaps = new ArrayList<GenericSegment>();
			if (n < lineGaps.size())
				nextLineGaps = lineGaps.get(n);

			// compare this with next
			// Iterator i = thisLineGaps.iterator();
			// while(i.hasNext())
			boolean potentialNewGap = false;
			float lastX2 = -1.0f;
			float lastY1 = -1.0f;
			float lastY2 = -1.0f;
			int lastIndex = -1;
			for (int i = 0; i < thisLineGaps.size(); i++) {
				GenericSegment thisGap = (GenericSegment) thisLineGaps.get(i);// i.next();
				boolean intersects = false;
				boolean addedGap = false;
				if (potentialNewGap) {
					if (lastX2 >= thisGap.getX1()) {
						float newX2 = lastX2;
						if (lastX2 <= thisGap.getX2()) {
							potentialNewGap = false;
							newX2 = thisGap.getX2();
						}
						GenericSegment newGap = new GenericSegment(
								thisGap.getX1(), newX2, lastY1, thisGap.getY2());
						nextLineGaps.add(lastIndex + 1, newGap);
						addedGap = true;
						intersects = true;
					}
				}
				if (!addedGap) {
					for (int j = 0; j < nextLineGaps.size(); j++)
					// Iterator j = nextLineGaps.iterator();
					// while(j.hasNext())
					{
						GenericSegment nextGap = nextLineGaps.get(j);// j.next();

						if (SegmentUtils.horizIntersect(thisGap, nextGap)) {
							intersects = true;
							// update next with vertical co-ordinates of this;
							// shrink x if necc.

							nextGap.setY2(thisGap.getY2());
							if (thisGap.getX1() > nextGap.getX1())
								nextGap.setX1(thisGap.getX1());
							if (thisGap.getX2() < nextGap.getX2()) {
								lastX2 = nextGap.getX2();
								lastY1 = nextGap.getY1();
								lastY2 = nextGap.getY2();
								nextGap.setX2(thisGap.getX2());
								potentialNewGap = true;
								lastIndex = j;
							} else {
								potentialNewGap = false;
							}
						}

					}
				}
				// the last row doesn't intersect with anything anyway :)
				if (!intersects)// || n == (lineGaps.size()))
				{
					// add the last one to the result
					// add to result
					retVal.add(thisGap);
				}
			}

			// here add remaining last row of gaps (nextLineGaps)
			// if (n == lineGaps.size() - 1)
			// retVal.addAll(nextLineGaps);
		}
		return retVal;
	}

	protected static List<GenericSegment> createList(GenericSegment gs) {
		List<GenericSegment> retVal = new ArrayList<GenericSegment>();
		retVal.add(gs);
		return (retVal);
	}

	protected static List<GenericSegment> cloneList(
			List<? extends GenericSegment> l) {
		List<GenericSegment> retVal = new ArrayList<GenericSegment>();
		for (GenericSegment gs : l)
			retVal.add(gs);
		return (retVal);
	}

	public AdjacencyGraph<? extends GenericSegment> getAG() {
		return ag;
	}

	public void setAG(AdjacencyGraph<? extends GenericSegment> ag) {
		this.ag = ag;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public HashMap<GenericSegment, CandidateCluster> getClustHash() {
		return clustHash;
	}

	public void setClustHash(HashMap<GenericSegment, CandidateCluster> clustHash) {
		this.clustHash = clustHash;
	}

}