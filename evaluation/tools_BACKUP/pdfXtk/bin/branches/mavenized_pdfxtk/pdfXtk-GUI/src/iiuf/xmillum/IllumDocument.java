/* (C) 2001-2002, DIUF, http://www.unifr.ch/diuf
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

import iiuf.dom.DOMUtils;
import iiuf.dom.DOMUtilsNS;
import iiuf.dom.ElementList;
import iiuf.util.Queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException; 

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * IllumDocument
 *
 * Manages all data related to a document presented in xmillum.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class IllumDocument {

  public final static String XMI_DOCUMENT    = "document";
  public final static String XMI_LAYER       = "layer";
  public final static String XMI_TOOL        = "tool";
  public final static String XMI_HANDLER     = "handler";
  public final static String XMI_DISPLAYABLE = "object";
  public final static String XMI_FLAG        = "flag";
  public final static String XMI_STYLE       = "style";

  public final static String XMI_CLASSPATH   = "classpath";

  public final static String XMI_NSURI       = "http://www-iiuf.unifr.ch/~hitz/xmillum";

  protected BrowserContext   context;
  protected URL              baseURL;
  protected Element          sourceDocument;
  protected Element          stylesheetDocument;
  protected Document         internalDocument;
  protected Templates        stylesheetCompiled;

  DocumentBuilder documentBuilder;

  /*** 
   * Creates a new IllumDocument.
   *
   * @param context BrowserContext this document is attached to.
   */
  public IllumDocument(BrowserContext context) {
    this.context = context;

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      documentBuilder = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks if a source document is loaded.
   *
   * @return True if a source document is loaded, false otherwise.
   */
  public boolean hasSource() {
    return sourceDocument != null;
  }

  /**
   * Checks if a stylesheet is loaded.
   *
   * @return True if a stylesheet is loaded, false otherwise.
   */
  public boolean hasStylesheet() {
    return stylesheetCompiled != null;
  }

  /** 
   * Sets the source document.
   *
   * @param e Root element of the source document.
   */
  public void setSourceDocument(Element e) throws IllumException {
    context.elementTagger = (e != null) ? new ElementTagger(e, "tmp:refvalue") : null;
    sourceDocument = e;
  }

  /**
   * Strips off the "tmp" attributes from the source document and
   * returns it.
   *
   * @return The source document, without any "tmp" attributes.
   */
  public Element getStrippedOriginalDocument() {
    Element s = getSourceDocument();
    DOMUtils.removeAttributesFromDescendants(s, "tmp");
    try {
      setSourceDocument(null);
    } catch (IllumException e) {
    }
    return s;
  }

  /**
   * Returns a copy of the source document and strips off all "tmp"
   * attributes.
   *
   * @return Copy of the source document.
   */
  public Element getStrippedOriginalDocumentCopy() {
    Element root = (Element) sourceDocument.cloneNode(true);
    DOMUtils.removeAttributesFromDescendants(root, "tmp");
    return root;
  }

  /**
   * Returns the source document
   *
   * @return Source document.
   */
  public Element getSourceDocument() {
    return sourceDocument;
  }

  /**
   * Sets the stylesheet.
   *
   * @param e Root element of the stylesheet.
   */
  public void setStylesheetDocument(Element e) throws IllumException {
    try {
      stylesheetDocument = e;
      stylesheetCompiled = null;
      if (e != null) {
	TransformerFactory tFactory = TransformerFactory.newInstance();
	stylesheetCompiled = tFactory.newTemplates(new DOMSource(e.getOwnerDocument()));
      }
    } catch (TransformerConfigurationException ex) {
      throw new IllumException(ex);
    } catch (TransformerFactoryConfigurationError ex) {
      throw new IllumException(ex);
    }
  }

  /**
   * Returns the stylesheet document.
   *
   * @return Root element of the stylesheet.
   */
  public Element getStylesheetDocument() {
    return stylesheetDocument;
  }

  /** 
   * Returns the internal document.
   *
   * @return Internal document
   */
  public Document getInternalDocument() {
    return internalDocument;
  }

  /**
   * Sets the base URL used for relative accesses.
   *
   * @param base Base URL for relative used for relative accesses.
   */
  private void setBaseURL(URL base) {
    baseURL = base;
  }

  /**
   * Returns the base URL.
   *
   * @return Base URL.
   */
  public URL getBaseURL() {
    return baseURL;
  }

  /**
   * Loads the source document.
   *
   * @param source Source document to load.
   */
  public void loadSourceDocument(IllumSource source) throws IllumException {
    setBaseURL(source.getBaseURL());
    setSourceDocument(source.getData());
  }

  /**
   * Loads the stylesheet document.
   *
   * @param url URL of the stylesheet to load.
   */
  public void loadStylesheet(URL url) throws IllumException {
    try {
      InputSource isource  = new InputSource(url.openStream());
      Element ss = documentBuilder.parse(isource).getDocumentElement();
      setStylesheetDocument(ss);
      ssURL = url;
    } catch (IOException e) {
      throw new IllumException(e);
    } catch (SAXException e) {
      throw new IllumException(e);
    }
  }

  URL ssURL;

  /**
   * Transforms the source document using the stylesheet.
   */
  public void transform() throws IllumException {
    try {
      internalDocument = documentBuilder.newDocument();

      Transformer transformer = stylesheetCompiled.newTransformer();
      if (baseURL != null) {
	transformer.setParameter("xmillum.baseurl", baseURL.toString());
      }
      transformer.transform(new DOMSource(sourceDocument), new DOMResult(internalDocument));
    } catch (TransformerConfigurationException e) {
      internalDocument = null;
      throw new IllumException(e);
    } catch (TransformerException e) {
      internalDocument = null;
      throw new IllumException(e);
    }
  }

  public ClassLoader getClassLoader() {
    try {
      if (internalDocument.getDocumentElement().hasAttribute(XMI_CLASSPATH)) {
	URL u = new URL(ssURL, internalDocument.getDocumentElement().getAttribute(XMI_CLASSPATH));
	return new URLClassLoader(new URL[] { u });
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }      
    return ClassLoader.getSystemClassLoader();
  }

  public NodeList getHandlers() {
    return DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_HANDLER);
  }

  public NodeList getDisplayables() {
    return DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_DISPLAYABLE);
  }

  public NodeList getFlags() {
    return DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_FLAG);
  }

  public NodeList getStyles() {
    return DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_STYLE);
  }

  /**
   * Returns the names of the layers defined by the stylesheet.
   *
   * @return List of layer names
   */
  public String[] getLayerNames() {
    if (internalDocument == null) {
      return new String[0];
    }
    NodeList layers = DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_LAYER);
    String[] l = new String[layers.getLength()];
    for (int i = 0; i < layers.getLength(); i++) {
      l[i] = ((Element) layers.item(i)).getAttribute("name");
    }
    return l;
  }

  /**
   * Returns a layer defined by its name.
   *
   * @param layer Name of the desired layer
   * @return &lt;xmi:layer&gt; element of the desired layer
   */
  public Element getLayer(String layer) {
    NodeList layers = DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_LAYER);
    for (int i = 0; i < layers.getLength(); i++) {
      Element l = (Element) layers.item(i);
      if (l.getAttribute("name").equals(layer)) {
	return l;
      }
    }
    return null;
  }

  /**
   * Returns the tools defined in the internal document.
   *
   * @return List of &lt;xmi:tool&gt; elements
   */
  public NodeList getTools() {
    return DOMUtilsNS.getChildsByTagName(internalDocument.getDocumentElement(), XMI_NSURI, XMI_TOOL);
  }

  /**
   * Returns the element in the source document which is referenced by
   * the given reference.
   *
   * @param reference Reference to find
   * @return Element referenced by the given reference
   */
  public Element getSourceElementWithReference(String reference) {
    return context.elementTagger.getElementWithTag(reference);
  }

  /**
   * Returns a NodeList with the elements in the internal document
   * which reference the element having the given reference in the
   * source.
   *
   * @param reference Reference to find
   * @return Elements that are referencing the given reference
   */
  public NodeList getInternalElementsWhichReference(String reference) {
    String[] layers = getLayerNames();
    ElementList el = new ElementList();

    for (int j = 0; j < layers.length; j++) {
      LinkedList l = DOMUtils.getElementsWithAttribute(getLayer(layers[j]), "ref", reference);
      for (int i = 0; i < l.size(); i++) {
	el.add(l.get(i));
      }
    }
    return el;
  }
}
