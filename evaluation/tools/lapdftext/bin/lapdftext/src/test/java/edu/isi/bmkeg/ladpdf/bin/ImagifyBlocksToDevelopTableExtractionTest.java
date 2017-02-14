package edu.isi.bmkeg.ladpdf.bin;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

import edu.isi.bmkeg.lapdf.bin.Blockify;
import edu.isi.bmkeg.lapdf.bin.ImagifyBlocks;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;

public class ImagifyBlocksToDevelopTableExtractionTest extends TestCase
{

	BmkegProperties prop;
	String login, password, dbUrl;

	File input;
	File f1, f2, f3;
	
	protected void setUp() throws Exception
	{ 

		URL u = this.getClass().getClassLoader().getResource("table/GhaniEtAl2014.pdf");	
		input = new File( u.getPath() );
				
	}

	protected void tearDown() throws Exception	{
		Converters.cleanContentsFiles(input, "pdf");
	}

	@Test
	public void testInputFileOnly() throws Exception
	{		
		String[] args = {
				input.getPath(), 
			};
		ImagifyBlocks.main(args);
		
	}
	
}
