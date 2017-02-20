package edu.isi.bmkeg.lapdf.model.factory;

import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;

public interface AbstractModelFactory {

	public ChunkBlock createChunkBlock(
			int topX, 
			int topY, 
			int bottomX,
			int bottomY,
			int order);

	public WordBlock createWordBlock(int topX, 
			int topY, 
			int bottomX,
			int bottomY, 
			int spaceWidth, 
			String font, 
			String style, 
			String word,
			int order);

	public PageBlock createPageBlock(int pageNumber, 
			int pageHeight, 
			int pageWidth, 
			LapdfDocument document);

}
