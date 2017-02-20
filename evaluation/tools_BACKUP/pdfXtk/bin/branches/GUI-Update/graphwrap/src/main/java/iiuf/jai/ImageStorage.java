/** ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: ImageStorage.java,v $
 * SUPPORT:	$Author: hassan $
 * CREATION:	$Date: 2006/05/17 10:22:21 $
 * @VERSION:	$Revision: 1.1 $
 * OVERVIEW:    storage and access of images
 * ------------------------------------------------------------------------ */
/**
    (c) 2000, IIUF

    storage and access of images

    @author $author$
    @version $revision$
*/
/* ------------------------------------------------------------------------ */
package iiuf.jai;

import javax.media.jai.RenderedOp;
/* ------------------------------------------------------------------------ */
public interface ImageStorage 
{
  public void storeImage(RenderedOp img,
			 String id) throws ImageStorageException;
  public RenderedOp getImage(String id) throws ImageStorageException;
}
/* ------------------------------------------------------------------------ */
