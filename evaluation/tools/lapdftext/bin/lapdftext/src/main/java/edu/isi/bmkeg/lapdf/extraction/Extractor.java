package edu.isi.bmkeg.lapdf.extraction;

import java.util.Iterator;
import java.util.List;

import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.utils.FrequencyCounter;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;

public interface Extractor extends Iterator<List<WordBlock>>{

	public int getCurrentPageBoxHeight();
	
	public int getCurrentPageBoxWidth();
	
	public IntegerFrequencyCounter getAvgHeightFrequencyCounter();

	public FrequencyCounter getFontFrequencyCounter();

	public IntegerFrequencyCounter getSpaceFrequencyCounter(int height);

}
