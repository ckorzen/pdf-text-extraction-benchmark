/** ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: ImageStorageTest.java,v $
 * SUPPORT:	$Author: ohitz $
 * CREATION:	$Date: 2002/07/11 12:24:01 $
 * @VERSION:	$Revision: 1.1 $
 * OVERVIEW:    test class ImageStorage
 * ------------------------------------------------------------------------ */
/**
    (c) 2000, 2001, IIUF, DIUF<p>

    test class ImageStorageTest

    @author $author$
    @version $revision$
*/
/* ------------------------------------------------------------------------ */
package iiuf.jai.test;

import iiuf.jai.FolderImageStorage;
import iiuf.jai.ImageStorage;
import iiuf.jai.ImageStorageException;

import javax.media.jai.RenderedOp;
import javax.media.jai.JAI;
/* ------------------------------------------------------------------------ */
public class ImageStorageTest 
{
  /*------------------------------------------------------------------------*/
  /** construct an ImageStorage based on a folder
      @param folder name of the folder used to put and get images 
  */
  public ImageStorageTest(String imgName, String folderName) 
    throws ImageStorageException {

    ImageStorage imgStore = 
      new FolderImageStorage(folderName);
    RenderedOp img = JAI.create("fileload",imgName);
    imgStore.storeImage(img,"toto.tif+100+100+100+100");
    img = imgStore.getImage("toto.tif+100+100+100+100");
    imgStore.storeImage(img,"tutu.tif+10+10+50+50");
  }
  /*------------------------------------------------------------------------*/
  public static void main(String[] arg)
    throws ImageStorageException {
    if (arg.length!=2) {
      System.out.println("usage : java iiuf.jai.ImageStorage <imgName> <folderName");
      return;
    }
    new ImageStorageTest(arg[0],arg[1]);
  }
  /*------------------------------------------------------------------------*/
}
/* ------------------------------------------------------------------------ */
