package iiuf.swing;

import java.io.File;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.ImageIcon;

import iiuf.util.Util;

/**
   Preview plugin for images.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
   @see iiuf.swing.PreviewFileChooser
*/
public class ImagePreview 
  extends
  AbstractPreview 
{
  protected ImageIcon preview;
  protected ImageIcon image;
  
  public boolean accepts(File f) {
    return load(f);
  }
  
  private boolean load(File f) {
    try {
      image = null;
      image = new ImageIcon(f.toURL());
      return image.getIconWidth() > 1 && image.getIconHeight() > 1;
    } catch(MalformedURLException e) {
      Util.printStackTrace(e);
      return false;
    }
  }
  
  public void preview(File file) {    
    int w = getWidth();
    int h = getHeight();
    if(image == null)
      load(file);
    if(image != null && w > 0 && h > 0) {
      boolean chgHeight = ((double)w / (double)h) < 
	((double)image.getIconWidth() / (double)image.getIconHeight());
      
      if(false) {
	BufferedImage imBuffer = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
	
	Graphics graphics = imBuffer.getGraphics();
	
	new ImageIcon(image.getImage().getScaledInstance(chgHeight ? w : -1, 
							 chgHeight ? -1 : h,
							 Image.SCALE_FAST)).paintIcon(this, graphics, 0, 0);
	
	preview = new ImageIcon(imBuffer);
      } else
	preview = new ImageIcon(image.getImage().getScaledInstance(chgHeight ? w : -1, 
								   chgHeight ? -1 : h,
								   Image.SCALE_FAST));
      repaint();
    } else
      preview = null;
  }
  
  public void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    if(preview != null) {      
      int x = getWidth()  / 2 - preview.getIconWidth()/2;
      int y = getHeight() / 2 - preview.getIconHeight()/2;
      
      if (y < 0) y = 0;	
      if (x < 0) x = 0;
      
      preview.paintIcon(this, g, x, y);
    }
  }

  public String toString() {
    return "Image";
  }
}

/*
  $Log: ImagePreview.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/07/30 15:27:04  schubige
  adapted for sample based timing

  Revision 1.1  2001/05/08 10:10:53  schubige
  added preview file chooser
  
*/
