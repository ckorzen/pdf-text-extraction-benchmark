/* (C) 2000-2002, DIUF, http://www.unifr.ch/diuf
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package iiuf.xmillum;

import iiuf.util.EventListenerList;
import iiuf.util.Queue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.EventListener;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * BrowserContext
 *
 * Browser context information.  Based on the XMIllum code
 *
 * @author Tamir Hassan, hassan@dbai.tuwien.ac.at
 * @author DIUF, Fribourg, CH
 * @version GraphWrap Beta 1
 */
public class BrowserContext {

  static {
    try {
      InputStream is = BrowserContext.class.getResourceAsStream("xmillum.colors");
      if (is == null) {
	System.err.println("Unable to load xmillum.colors, using predefined colors only.");
      } else {
	System.getProperties().load(is);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Queue runQueue;

  public static final int MSG_LOAD_STYLESHEET = 1;
  public static final int MSG_RESCALE = 2;
  public static final int MSG_TOGGLE_LAYER = 3;
  public static final int MSG_RETRANSFORM = 4;
  public static final int MSG_REFRESH = 6;
  public static final int MSG_SET_SOURCE = 10;
  
  class IllumMessage {    
    public int message;
    public Object arg;
    public Object arg2;

    public IllumMessage(int message) {
      this.message = message;
    }

    public IllumMessage(int message, Object arg) {
      this.message = message;
      this.arg = arg;
    }

    public IllumMessage(int message, Object arg, Object arg2) {
      this.message = message;
      this.arg = arg;
      this.arg2 = arg2;
    }
  }

  public BrowserContext() {
    try {
      Class.forName("javax.media.jai.JAI", false, Thread.currentThread().getContextClassLoader());
      Class i = Class.forName("iiuf.xmillum.JAIImageFactory");
      setImageFactory((ImageFactory) i.newInstance());
      setStatus("Detected JAI.");
    } catch (Throwable t) {
      setImageFactory(new JavaImageFactory());
    }

    runQueue = new Queue("commandqueue") {
	public boolean handle(Object o) {
	  IllumMessage msg = (IllumMessage) o;
	  try {
	    switch (msg.message) {
	    case MSG_SET_SOURCE:
	      setStatus("Loading source data...");
	      document.loadSourceDocument((IllumSource) msg.arg);
	      setStatus("Done.");
	      tryTransform();
	      break;
	    case MSG_LOAD_STYLESHEET:
	      setStatus("Loading stylesheet...");
	      document.loadStylesheet((URL) msg.arg);
	      setStatus("Done.");
	      tryTransform();
	      break;
	    case MSG_TOGGLE_LAYER:
	      if (document.getInternalDocument() != null && displayableFactory != null) {
		setStatus("Displaying...");
		fireDocumentChangeEvent(new DocumentChangeEvent(BrowserContext.this, DocumentChangeEvent.LAYER_TOGGLED, (String) msg.arg, ((Boolean) msg.arg2).booleanValue()));
		setStatus("Done.");
	      }
	      break;
	    case MSG_RETRANSFORM:
	      setStatus("Transforming...");
	      document.transform();
	      fireDocumentChangeEvent(DocumentChangeEvent.DOCUMENT_CHANGED);
	      setStatus("Done.");
	      break;
	    case MSG_REFRESH:
	      fireDocumentChangeEvent(DocumentChangeEvent.REFRESH);
	      break;
	    default:
	      System.out.println("Unknown message "+msg.message);
	    }
	  } catch (IllumException e) {
	    setStatus(e.toString());
	    e.printStackTrace();
	  }
	  return true;
	}

	public void tryTransform() throws IllumException {
	  if (document.hasSource() && document.hasStylesheet()) {
	    actionFactory = null;
	    displayableFactory = null;
	    setStatus("Transforming...");
	    document.transform();
	    styleRegistry = new StyleRegistry(BrowserContext.this, document.getStyles());
	    flagger.setFlags(document.getFlags());
	    setSelectionFlag(FlagManager.SELECTION, FlagManager.SELECTED);
	    actionFactory = new ActionHandlerFactory(BrowserContext.this, document.getHandlers());
	    displayableFactory = new DisplayableFactory(BrowserContext.this, document.getDisplayables());
	    fireDocumentChangeEvent(DocumentChangeEvent.DOCUMENT_CHANGED);
	    setStatus("Done.");
	  }
	}
      };
  }

  /** List of status listeners */

  EventListenerList statusListeners = new EventListenerList();

  /** Adds a status listener which receives status mesages.

      @param listener StatusListener to add */

  public void addStatusListener(StatusListener listener) {
    statusListeners.add(StatusListener.class, listener);
  }

  /** Removes a status listener.

      @param listener StatusListener to remove */

  public void removeStatusListener(StatusListener listener) {
    statusListeners.remove(StatusListener.class, listener);
  }

  /** Notifies all StatusListeners with a status message

      @param message Status message. */

  public void setStatus(String message) {
    EventListener[] l = statusListeners.getListeners(StatusListener.class);
    if (l.length == 0) {
      log(message);
    } else {
      for (int i = 0; i < l.length; i++) {
	((StatusListener) l[i]).setStatus(message);
      }
    }
  }

  /**
   * Log message.
   *
   * @param String message
   */

  public void log(String message) {
    System.err.println(message);
  }

  public void log(String message, Throwable t) {
    System.err.print(message+" ("+t.getMessage()+")");
  }

  /** List of change listeners */

  EventListenerList documentChangeListeners = new EventListenerList();

  /** Adds a change listener which receives change mesages.

      @param listener DocumentChangeListener to add */

  public void addDocumentChangeListener(DocumentChangeListener listener) {
    documentChangeListeners.add(DocumentChangeListener.class, listener);
  }

  /** Removes a change listener.

      @param listener DocumentChangeListener to remove */

  public void removeDocumentChangeListener(DocumentChangeListener listener) {
    documentChangeListeners.remove(DocumentChangeListener.class, listener);
  }

  /** Notifies all DocumentChangeListeners. */

  private void fireDocumentChangeEvent(int type) {
    DocumentChangeEvent e = new DocumentChangeEvent(this, type);
    EventListener[] l = documentChangeListeners.getListeners(DocumentChangeListener.class);
    for (int i = 0; i < l.length; i++) {
      ((DocumentChangeListener) l[i]).documentChanged(e);
    }
  }

  private void fireDocumentChangeEvent(DocumentChangeEvent e) {
    EventListener[] l = documentChangeListeners.getListeners(DocumentChangeListener.class);
    for (int i = 0; i < l.length; i++) {
      ((DocumentChangeListener) l[i]).documentChanged(e);
    }
  }

  /** Set a source document.

      @param source IllumSource. */

  public void setSource(IllumSource source) {
    this.source = source;
    runQueue.put(new IllumMessage(MSG_SET_SOURCE, source));
  }

  /** Load stylesheet.

      @param file Stylesheet. */

  public void loadStylesheet(URL url) {
    runQueue.put(new IllumMessage(MSG_LOAD_STYLESHEET, url));
  }

  /** Switch a layer on and off.

      @param layer The layer to switch on and off.
      @param active On/off. */

  public void toggleLayer(String layer, boolean active) {
    runQueue.put(new IllumMessage(MSG_TOGGLE_LAYER, layer, new Boolean(active)));
  }

  public void finish() {
    flagger.runHandlers();
  }

  /** Clear the current layer. Closes all tool windows and empties the
      BrowserPanel. */

  public void clearLayer() {
    toggleLayer(null, false);
  }

  /** Refresh display */

  public void refresh() {
    runQueue.put(new IllumMessage(MSG_REFRESH));
  }

  public void retransform() {
    runQueue.put(new IllumMessage(MSG_RETRANSFORM));
  }

  public IllumDocument getDocument() {
    return document;
  }

  public double getScale() {
    return browserPanel.getScale();
  }

  // The currently active document
  private IllumDocument document = new IllumDocument(this);

  // Source document
  public IllumSource source;

  // Flag manager
  public FlagManager      flagger = new FlagManager(this);

  // ActionHandler factory
  public ActionHandlerFactory actionFactory;

  // Style registry
  public StyleRegistry styleRegistry;

  // Displayable factory
  public DisplayableFactory displayableFactory;

  public BrowserPanel browserPanel;

  // Image factory
  private ImageFactory imageFactory;

  public ImageFactory getImageFactory() {
    return imageFactory;
  }

  public void setImageFactory(ImageFactory i) {
    imageFactory = i;
    imageFactory.setBrowserContext(this);
  }

  // Mouse event coordinates

  private Point mousePosition = new Point();

  /**
   * Sets the mouse position.
   *
   * @param e mouse event
   */
  public void setMousePosition(MouseEvent e) {
    mousePosition = (Point) e.getPoint().clone();
  }

  /**
   * Returns the mouse position.
   *
   * @return mouse position
   */
  public Point getMousePosition() {
    return new Point(mousePosition);
  }

  public ElementTagger elementTagger;

  public void setWindowCreator(WindowCreator w) {
    windowCreator = w;
  }

  public WindowCreator getWindowCreator() {
    return windowCreator;
  }

  private WindowCreator windowCreator;

  /**
   * Returns the element in the source document which is referenced by
   * the given reference.
   *
   * @param reference Reference to search
   * @return Element that is referenced
   */
  public Element getSourceElementByReference(String reference) {
    if (reference == null || reference.equals("")) {
      return null;
    } else {
      return document.getSourceElementWithReference(reference);
    }
  }

  /**
   * Returns a NodeList with the elements in the internal document
   * which reference the element having the given reference in the
   * source.
   *
   * @param reference Reference to find
   * @return List of elements that reference the requested reference
   */
  public NodeList getInternalElementsWhichReference(String reference) {
    return document.getInternalElementsWhichReference(reference);
  }

  /**
   * Sets the drag distance.
   *
   * @param d Distance
   */
  public void setDragDistance(Point d) {
//     dragDistance = new Point((int) (d.x/getScale()), (int) (d.y/getScale()));
    dragDistance = new Point(d);
  }

  /** Drag distance */
  Point dragDistance;

  /**
   * Returns the drag distance.
   *
   * @return Drag distance
   */
  public Point getDragDistance() {
    return dragDistance;
  }

  /** Flag access */
  private FlagAccess flagAccess;

  /** Flag value */
  private String selectionValue = FlagManager.SELECTED;

  /**
   * Toggles the selection state of an element.
   *
   * @param e The element to select
   */
  public void toggleSelection(Element e) {
    flagAccess.toggleFlag(e, selectionValue, null);
  }

  /** 
   * Sets the flag that is set when toggleSelection is called.
   *
   * @param type Flag type
   * @param value Flag value
   */
  public void setSelectionFlag(String type, String value) {
    flagAccess = flagger.addFlagListener(type, new FlagListener() {
	public void setFlag(Element e, String v) {
	}
      });
    selectionValue = value;
  }
}
