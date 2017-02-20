package at.ac.tuwien.dbai.pdfwrap.analysis;

import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.TextSegment;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.utils.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import com.tamirhassan.pdfanalyser.utils.PAUtils;

public class SegmentationResult implements Cloneable
{
	protected List<CandidateCluster> segments;
	protected HashMap<GenericSegment, CandidateCluster> clustHash;
	
	protected List<AdjacencyEdge<GenericSegment>> joinedEdges; // include redundant edges here
	protected List<AdjacencyEdge<GenericSegment>> remainingEdges;
	
	public SegmentationResult()
	{
		// clone segment list
		segments = new ArrayList<CandidateCluster>();
		clustHash = new HashMap<GenericSegment, CandidateCluster>();
		joinedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		remainingEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
	}
	
	public SegmentationResult(List<GenericSegment> initialSegments)
		{
			// clone segment list
			segments = new ArrayList<CandidateCluster>();
			clustHash = new HashMap<GenericSegment, CandidateCluster>();
			
			for (GenericSegment gs : initialSegments)
			{
				CandidateCluster cc = AbstractPageSegmenter.
					makeCluster(AbstractPageSegmenter.createList(gs));
				segments.add(cc);
				clustHash.put(gs, cc);
			}
			
			joinedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
			remainingEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		}
	
	public SegmentationResult(List<CandidateCluster> cloneSegments,
		HashMap<GenericSegment, CandidateCluster> cloneClustHash)
	{
		// clone segment list
		segments = new ArrayList<CandidateCluster>();
		for (CandidateCluster gs : cloneSegments)
			segments.add(gs);
		
		clustHash = (HashMap<GenericSegment, CandidateCluster>)
			cloneClustHash.clone();
		
		joinedEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
		remainingEdges = new ArrayList<AdjacencyEdge<GenericSegment>>();
	}
	
	public SegmentationResult clone()
	{
		SegmentationResult sr = new SegmentationResult();
		for (CandidateCluster gs : this.segments)
			sr.segments.add(gs);
		sr.clustHash = (HashMap<GenericSegment, CandidateCluster>)
			this.clustHash.clone();
		for (AdjacencyEdge<GenericSegment> ae : this.joinedEdges)
			sr.joinedEdges.add(ae);
		for (AdjacencyEdge<GenericSegment> ae : this.remainingEdges)
			sr.remainingEdges.add(ae);
		
		return sr;
	}
	
	public SegmentationResult cloneSubSegmentation(CandidateCluster targetSeg)
	{
		SegmentationResult sr = new SegmentationResult();
		sr.segments.add(targetSeg);
		for (GenericSegment gs : clustHash.keySet())
			if (targetSeg.getItems().contains(gs))
				sr.clustHash.put(gs, targetSeg);
		for (AdjacencyEdge<GenericSegment> ae : this.joinedEdges)
			if (targetSeg.getItems().contains(ae.getNodeFrom()) ||
				targetSeg.getItems().contains(ae.getNodeTo()))
				sr.joinedEdges.add(ae);
		// TODO: necessary at all?
		for (AdjacencyEdge<GenericSegment> ae : this.remainingEdges)
			sr.remainingEdges.add(ae);
		
		return sr;
	}
	
	public SegmentationResult cloneSubSegmentation(List<CandidateCluster> targetSegs)
	{
		SegmentationResult sr = new SegmentationResult();
		sr.segments.addAll(targetSegs);
		List<GenericSegment> targetSegItems = new ArrayList<GenericSegment>();
		for (CandidateCluster targetSeg : targetSegs)
			targetSegItems.addAll(targetSeg.getItems());
		for (GenericSegment gs : clustHash.keySet())
			if (targetSegItems.contains(gs))
				sr.clustHash.put(gs, clustHash.get(gs));
		for (AdjacencyEdge<GenericSegment> ae : this.joinedEdges)
			if (targetSegItems.contains(ae.getNodeFrom()) ||
				targetSegItems.contains(ae.getNodeTo()))
				sr.joinedEdges.add(ae);
		// TODO: really necessary to add edges pointing outside?
		// TODO: following necessary at all?
		for (AdjacencyEdge<GenericSegment> ae : this.remainingEdges)
			sr.remainingEdges.add(ae);
		
		return sr;
	}

	protected void addSegmentUpdateHash(CandidateCluster c)
	{
		// c instanceof Cluster
		
//			System.out.println("in updateHashes with c: " + c.toExtendedString());
    	
//			System.out.println("in updateHashes with pnf: " + performNeighbourFinding);
		
    	long t = System.currentTimeMillis();
    	
		// go through all swallowedItems
		// if not part of c.items & if not newSegment
			// if unused, add to c
			// if used, look them up in colHash & merge col with c
		
//			System.out.println("newSegment: " + newSegment);
//			System.out.println("adding to cluster c: " + c.toExtendedString());
		
    	//Cluster c = new Cluster();
    	
		for (TextSegment ts : c.getItems())
		{
			/*
			 * 2011-11-20 unnecessary and causes a crash if no swallowing!
			 */
			// 2012-07-27 reinstated!
			
			if (clustHash.get(ts) != null) // item belongs to another cluster
			{
				CandidateCluster clust = (CandidateCluster)clustHash.get(ts);
				//c.getItems().addAll(clust.getItems());
				for (GenericSegment gs : clust.getItems())
				{
//					System.out.println("removing");
					TextSegment item = (TextSegment)gs;
					clustHash.remove(item);
					// all of the items within the cluster should already be
					// in the list of swallowedItems...
					// clustHash.put(item, c);
					segments.remove(clust);   
				}
			}
			
			clustHash.put(ts, c);
			
		}
		segments.add(c);
	}
	
	public List<CandidateCluster> neighboursAbove(CandidateCluster cc)
	{
		List<CandidateCluster> retVal = new ArrayList<CandidateCluster>();
		for (AdjacencyEdge<GenericSegment> ae: remainingEdges)
		{
			if (ae.getDirection() == AdjacencyEdge.REL_ABOVE &&	
				cc.getItems().contains(ae.getNodeFrom()))
				retVal.add(clustHash.get(ae.getNodeTo()));
			else if (ae.getDirection() == AdjacencyEdge.REL_BELOW && 
				cc.getItems().contains(ae.getNodeTo()))
				retVal.add(clustHash.get(ae.getNodeFrom()));
		}
		ListUtils.removeDuplicates(retVal);
		return retVal;
	}
	
	public List<CandidateCluster> neighboursBelow(CandidateCluster cc)
	{
		List<CandidateCluster> retVal = new ArrayList<CandidateCluster>();
		for (AdjacencyEdge<GenericSegment> ae: remainingEdges)
		{
			if (ae.getDirection() == AdjacencyEdge.REL_BELOW &&	
				cc.getItems().contains(ae.getNodeFrom()))
				retVal.add(clustHash.get(ae.getNodeTo()));
			else if (ae.getDirection() == AdjacencyEdge.REL_ABOVE && 
				cc.getItems().contains(ae.getNodeTo()))
				retVal.add(clustHash.get(ae.getNodeFrom()));
		}
		ListUtils.removeDuplicates(retVal);
		return retVal;
	}
	
	public List<CandidateCluster> neighboursLeft(CandidateCluster cc)
	{
		List<CandidateCluster> retVal = new ArrayList<CandidateCluster>();
		for (AdjacencyEdge<GenericSegment> ae: remainingEdges)
		{
			if (ae.getDirection() == AdjacencyEdge.REL_LEFT &&	
				cc.getItems().contains(ae.getNodeFrom()))
				retVal.add(clustHash.get(ae.getNodeTo()));
			else if (ae.getDirection() == AdjacencyEdge.REL_RIGHT && 
				cc.getItems().contains(ae.getNodeTo()))
				retVal.add(clustHash.get(ae.getNodeFrom()));
		}
		ListUtils.removeDuplicates(retVal);
		return retVal;
	}
	
	public List<CandidateCluster> neighboursRight(CandidateCluster cc)
	{
		List<CandidateCluster> retVal = new ArrayList<CandidateCluster>();
		for (AdjacencyEdge<GenericSegment> ae: remainingEdges)
		{
			if (ae.getDirection() == AdjacencyEdge.REL_RIGHT &&	
				cc.getItems().contains(ae.getNodeFrom()))
				retVal.add(clustHash.get(ae.getNodeTo()));
			else if (ae.getDirection() == AdjacencyEdge.REL_LEFT && 
				cc.getItems().contains(ae.getNodeTo()))
				retVal.add(clustHash.get(ae.getNodeFrom()));
		}
		ListUtils.removeDuplicates(retVal);
		return retVal;
	}
	
	public List<CandidateCluster> getSegments() {
		return segments;
	}

	public void setSegments(List<CandidateCluster> segments) {
		this.segments = segments;
	}

	public HashMap<GenericSegment, CandidateCluster> getClustHash() {
		return clustHash;
	}

	public void setClustHash(HashMap<GenericSegment, CandidateCluster> clustHash) {
		this.clustHash = clustHash;
	}

	public void addJoinedEdges
		(List<AdjacencyEdge<GenericSegment>> edges) 
	{
		for (AdjacencyEdge<GenericSegment> ae : edges)
			if (!joinedEdges.contains(ae))
				joinedEdges.add(ae);
	}
	
	public List<AdjacencyEdge<GenericSegment>> getJoinedEdges() {
		return joinedEdges;
	}

	public void setJoinedEdges(List<AdjacencyEdge<GenericSegment>> joinedEdges) {
		this.joinedEdges = joinedEdges;
	}

	public List<AdjacencyEdge<GenericSegment>> getRemainingEdges() {
		return remainingEdges;
	}

	public void setRemainingEdges(List<AdjacencyEdge<GenericSegment>> remainingEdges) {
		this.remainingEdges = remainingEdges;
	}
}