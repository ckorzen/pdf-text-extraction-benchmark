package edu.isi.bmkeg.lapdf.model;

import java.util.List;

import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

/**
 * A contiguous block of text that sits within a PageBlock object and contains
 * a list of WordBlock objects.
 * 
 * @author burns
 */
public interface ChunkBlock extends Block, SpatialEntity {

	public static final String TYPE_TITLE = "title";
	public static final String TYPE_AUTHORS = "authors";
	public static final String TYPE_BODY = "body";
	public static final String TYPE_HEADING = "heading";
	public static final String TYPE_SUBTITLE = "subtitle";

	public static final String TYPE_ABSTRACT = "abstract";
	public static final String TYPE_ABSTRACT_HEADING = "abstract.heading";
	public static final String TYPE_ABSTRACT_BODY = "abstract.body";

	public static final String TYPE_FIGURE_LEGEND = "figureLegend";
	public static final String TYPE_TABLE = "table";

	public static final String TYPE_UNCLASSIFIED = "unclassified";
	public static final String TYPE_PAGE = "page";

	public static final String TYPE_METHODS = "methods";
	public static final String TYPE_METHODS_HEADING = "methods.heading";
	public static final String TYPE_METHODS_BODY = "methods.body";
	public static final String TYPE_METHODS_SUBTITLE = "methods.subtitle";

	public static final String TYPE_RESULTS = "results";
	public static final String TYPE_RESULTS_HEADING = "results.heading";
	public static final String TYPE_RESULTS_BODY = "results.body";
	public static final String TYPE_RESULTS_SUBTITLE = "results.subtitle";

	public static final String TYPE_REFERENCES = "references";
	public static final String TYPE_REFERENCES_HEADING = "references.heading";
	public static final String TYPE_REFERENCES_BODY = "references.body";

	public static final String TYPE_DISCUSSION = "discussion";
	public static final String TYPE_DISCUSSION_HEADING = "discussion.heading";
	public static final String TYPE_DISCUSSION_BODY = "discussion.body";
	public static final String TYPE_DISCUSSION_SUBTITLE = "discussion.subtitle";

	public static final String TYPE_CONCLUSIONS = "conclusions";
	public static final String TYPE_CONCLUSIONS_HEADING = "conclusions.heading";
	public static final String TYPE_CONCLUSIONS_BODY = "conclusions.body";
	public static final String TYPE_CONCLUSIONS_SUBTITLE = "conclusions.subtitle";

	public static final String TYPE_ACKNOWLEDGEMENTS = "acknowledgements";
	public static final String TYPE_ACKNOWLEDGEMENTS_HEADING = "acknowledgements.heading";
	public static final String TYPE_ACKNOWLEDGEMENTS_BODY = "acknowledgements.body";

	public static final String TYPE_AFFLIATION = "affliation";
	public static final String TYPE_HEADER = "header";
	public static final String TYPE_FOOTER = "footer";
	public static final String TYPE_KEYWORDS = "keywords";

	public static final String TYPE_INTRODUCTION = "introduction";
	public static final String TYPE_INTRODUCTION_HEADING = "introduction.heading";
	public static final String TYPE_INTRODUCTION_BODY = "introduction.body";
	public static final String TYPE_INTRODUCTION_SUBTITLE = "introduction.subtitle";

	public static final String TYPE_SUPPORTING_INFORMATION = "supportingInformation";
	public static final String TYPE_SUPPORTING_INFORMATION_HEADING = "supportingInformation.heading";
	public static final String TYPE_SUPPORTING_INFORMATION_BODY = "supportingInformation.body";
	public static final String TYPE_SUPPORTING_INFORMATION_SUBTITLE = "supportingInformation.subtitle";

	public static final String TYPE_CITATION = "citation";
	public static final String META_TYPE_HEADING = ".heading";
	public static final String META_TYPE_SUBTITLE = ".subtitle";

	public int getMostPopularWordHeight();

	public int getMostPopularWordSpaceWidth();

	public String getMostPopularWordFont();

	public String getMostPopularWordStyle();

	public void setMostPopularWordHeight(int height);

	public void setMostPopularWordSpaceWidth(int spaceWidth);

	public void setMostPopularWordStyle(String style);

	public void setMostPopularWordFont(String font);

	public int readNumberOfLine();

	public String readChunkText();

	public Boolean isHeaderOrFooter();

	public void setHeaderOrFooter(boolean headerOrFooter);

	public ChunkBlock readLastChunkBlock();

	boolean isMatchingRegularExpression(String regex);

	boolean isUnderOneLineFlushNeighboursOfType(String type);

	boolean hasNeighboursOfType(String type, int nsew);

	List<WordBlock> getRotatedWords();

	void setRotatedWords(List<WordBlock> rotatedWords);

	public double readDensity();

	public void setType(String type);

	public String getType();

}
