package at.ac.tuwien.dbai.pdfwrap.gui.tools;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A class for filtering PDF files within a JFileChooser
 * 
 * @author Timo Schleicher
 *
 */
public class OpenDocFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		
        if (f.isDirectory()) {
        	
            return true;
            
        } else {
        	
            return f.getName().toLowerCase().endsWith(".pdf") || f.getName().toLowerCase().endsWith(".xml");
        }
	}

	@Override
	public String getDescription() {
		
		return "PDF Documents (*.pdf), XML Documents (*.xml)";
	}

}
