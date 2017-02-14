package edu.isi.bmkeg.lapdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import junit.framework.TestCase;
import edu.isi.bmkeg.lapdf.bin.BlockifyClassify;
import edu.isi.bmkeg.lapdf.bin.DebugLapdfFeatures;
import edu.isi.bmkeg.lapdf.bin.ImagifySections;

public abstract class LAPDFTextEpochCheck_BaseTest extends TestCase
{
	
	private File ruleFile;
	private File pdfFile;
	

	public void runBlockifyClassifyImagify(String pdfFilePath, 
			String ruleFilePath) throws Exception {	

		URL ruleFileUrl = this.getClass().getClassLoader().getResource(ruleFilePath);
		if( ruleFileUrl == null )
			throw new FileNotFoundException(ruleFilePath);
		
		this.ruleFile = new File(ruleFileUrl.getFile());

		URL pdfFileUrl = this.getClass().getClassLoader().getResource(pdfFilePath);
		if( pdfFileUrl == null )
			throw new FileNotFoundException(pdfFilePath);
		
		this.pdfFile = new File(pdfFileUrl.getFile());		
		
		String stem = pdfFile.getName();
		stem = stem.substring(0, stem.lastIndexOf(".pdf"));
		File outputDir = new File( pdfFile.getParent() + "/" + stem + "/" );
		
		String[] args = {
				pdfFile.getPath(), outputDir.getPath(), ruleFile.getPath()
			};
		DebugLapdfFeatures.main(args);
		
	}
		
}
