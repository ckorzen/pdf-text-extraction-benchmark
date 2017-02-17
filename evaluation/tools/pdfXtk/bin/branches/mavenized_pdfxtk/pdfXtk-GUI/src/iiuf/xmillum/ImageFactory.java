package iiuf.xmillum;

import java.io.IOException;
import java.net.URL;

/**
 * (C) 2002, DIUF
 *
 * ImageFactory base class. Allows JAI as well as standard Java AWT
 * images to be used in XMIllum.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */

public abstract class ImageFactory {

  protected BrowserContext context;

  /**
   * Set the browser context.
   *
   * @param c The browser context.
   */
  public void setBrowserContext(BrowserContext c) {
    context = c;
  }

  /**
   * Loads an image at the specified scale.
   *
   * @param imageURL URL pointing to the image
   * @param scale Scale of the image
   * @return Loaded image
   */
  public abstract Image getImage(URL imageURL) throws IOException;
}
