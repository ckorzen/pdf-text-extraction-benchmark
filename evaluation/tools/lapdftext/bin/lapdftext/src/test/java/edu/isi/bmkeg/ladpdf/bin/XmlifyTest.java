package edu.isi.bmkeg.ladpdf.bin;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.lapdf.bin.BlockifyClassify;
import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.xml.model.LapdftextXMLDocument;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;
import edu.isi.bmkeg.utils.xml.XmlBindingTools;

public class XmlifyTest extends TestCase
{

	BmkegProperties prop;
	String login, password, dbUrl;

	File pdf, outputDir, ruleFile;
	File f1, f2, f3;
	
	@Before
	protected void setUp() throws Exception
	{ 
				
		URL u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8/pbio.1000441.pdf");
		pdf = new File( u.getPath() );
		outputDir = new File( pdf.getParentFile().getPath() + "/temp/output" );
		outputDir.mkdirs();
				
		u = this.getClass().getClassLoader().getResource("rules/plosbiology/epoch_7Jun_8.csv");
		ruleFile = new File( u.getPath() );

	}

	@After
	protected void tearDown() throws Exception	{
		Converters.cleanContentsFiles(outputDir, "pdf");
		if( outputDir.exists() ) {
			Converters.recursivelyDeleteFiles(outputDir.getParentFile());
		}
	}

	@Test
	public void testBuildXml() throws Exception
	{		

		LapdfEngine engine = new LapdfEngine();
		LapdfDocument lapdf = engine.blockifyFile(pdf);
		LapdftextXMLDocument xmlDoc = lapdf.convertToLapdftextXmlFormat();
			
		File outXml = new File( outputDir.getPath() + "/temp.xml");
		XmlBindingTools.saveAsXml(xmlDoc, outXml);		
		
	}
	
}
