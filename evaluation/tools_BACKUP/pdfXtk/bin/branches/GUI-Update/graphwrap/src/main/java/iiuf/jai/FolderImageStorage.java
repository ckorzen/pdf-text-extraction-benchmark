/** ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: FolderImageStorage.java,v $
 * SUPPORT:	$Author: hassan $
 * CREATION:	$Date: 2006/05/17 10:22:24 $
 * @VERSION:	$Revision: 1.1 $
 * OVERVIEW:    storage and access of images in folder
 * ------------------------------------------------------------------------ */
/**
    (c) 2000, IIUF

    storage and access of images in folder

    @author $author$
    @version $revision$
*/
/* ------------------------------------------------------------------------ */
package iiuf.jai;

import javax.media.jai.RenderedOp;
import javax.media.jai.JAI;

import java.awt.image.renderable.ParameterBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.media.jai.codec.ByteArraySeekableStream;
/* ------------------------------------------------------------------------ */
public class FolderImageStorage 
  implements ImageStorage
{
  /*------------------------------------------------------------------------*/
  private final static boolean DEBUG = true;
  private String folder;
  /*------------------------------------------------------------------------*/
  /** construct an ImageStorage based on a folder
      @param folder name of the folder used to put and get images 
  */
  public FolderImageStorage(String folder) 
  {
    this.folder = folder;
  }
  /*------------------------------------------------------------------------*/
  /** @param img image to be stored
      @param id  unique id given to the image
  */
  public void storeImage(RenderedOp img,
			 String id) {
    ParameterBlock pb;
    
    pb = new ParameterBlock();
    pb.addSource(img);
    pb.add(folder+"sign_"+id+".tif");    
    pb.add("tiff");
    pb.add(null);    
    JAI.create("filestore",pb);
  }
  /*------------------------------------------------------------------------*/
  /** @param id unique id given to the image 
   */
  public RenderedOp getImage(String id) 
    throws ImageStorageException
  {
    try { 
      File f = new File(folder+"sign_"+id+".tif");
      FileInputStream is = new FileInputStream(f);
      byte[] b = new byte[(new Long(f.length())).intValue()];
      is.read(b);
      ByteArraySeekableStream stream = new ByteArraySeekableStream(b);
      RenderedOp img = JAI.create("stream",stream);
      is.close();
      return img;    
   }
    catch(FileNotFoundException e1) {
      throw new ImageStorageException
	("File "+folder+"sign_"+id+".tif not found");
    }
    catch(IOException e2) {
      throw new ImageStorageException
	("Class FolderImageStorage : IOException throwed : msg : "+e2.getMessage());
    }
  }
  /*------------------------------------------------------------------------*/
}
/* ------------------------------------------------------------------------ */
