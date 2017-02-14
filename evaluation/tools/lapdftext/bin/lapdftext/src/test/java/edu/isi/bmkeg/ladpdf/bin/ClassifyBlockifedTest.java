package edu.isi.bmkeg.ladpdf.bin;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

import edu.isi.bmkeg.lapdf.bin.ClassifyBlockifed;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;

public class ClassifyBlockifedTest extends TestCase
{

	BmkegProperties prop;
	String login, password, dbUrl;

	File inputDir, outputDir, ruleFile;
	File f1, f2, f3;
	
	protected void setUp() throws Exception
	{ 
				
		URL u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8_lapdf_xml");
		inputDir = new File( u.getPath() );
		outputDir = new File( inputDir.getParentFile().getPath() + "/temp/output" );
				
		u = this.getClass().getClassLoader().getResource("rules/plosbiology/epoch_7Jun_8.csv");
		ruleFile = new File( u.getPath() );

	}

	protected void tearDown() throws Exception	{
		Converters.cleanContentsFiles(inputDir, "pdf");
		if( outputDir.exists() ) {
			Converters.recursivelyDeleteFiles(outputDir.getParentFile());
		}
	}

	@Test
	public void testInputOutputRuleFiles() throws Exception
	{		
		String[] args = {
				inputDir.getPath(), outputDir.getPath(), ruleFile.getPath()
			};
		ClassifyBlockifed.main(args);
	}
	
}
