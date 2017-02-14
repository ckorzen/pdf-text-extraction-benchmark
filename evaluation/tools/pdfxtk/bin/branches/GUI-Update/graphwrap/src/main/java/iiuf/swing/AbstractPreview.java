package iiuf.swing;

import java.io.File;
import javax.swing.JPanel;

/**
   The preview base class.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
   @see iiuf.swing.PreviewFileChooser
*/
public abstract class AbstractPreview 
  extends
  JPanel
{  
  protected abstract boolean accepts(File f);
  protected abstract void    preview(File f);
}

/*
  $Log: AbstractPreview.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/05/08 10:10:53  schubige
  added preview file chooser
  
*/
