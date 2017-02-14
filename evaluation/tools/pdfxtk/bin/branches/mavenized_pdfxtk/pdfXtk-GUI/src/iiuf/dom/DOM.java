package iiuf.dom;

import java.io.IOException;
import java.io.Writer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
    (c) 2000, IIUF

    Wrapper class for different XML parser implementations.

    @author $author$
    @version $revision$
*/

public abstract class DOM {

  /** Name of the system property which specifies the default DOM class */

  public final static String DEFAULT_DOM_PROPERTY = "iiuf.dom.default";

  /** Parses an XML document and return a DOM tree.

      @param input The XML document to parse.
      @param validate True if the document should be validated,
      false otherwise
      @return DOM structure */

  public abstract Document parseDocument(InputSource input, boolean validate)
    throws SAXException, IOException;

  /** Creates an empty document.

      @return DOM structure */

  public abstract Document createDocument();

  /** Writes a DOM structure.

      @param document DOM structure to write
      @param writer destination */

  public abstract void writeDocument(Document document, Writer writer)
    throws IOException;

  /** Gets the default DOM instance.

      @return DOM object */

  public static DOM getInstance() {
    return getInstance(null);
  }

  /** Gets a DOM instance.

      @param name fully-qualified class name or null for the default. The
      default DOM class is given by a system property (@see DEFAULT_DOM_PROPERTY).
      @return DOM object */
  
  public static DOM getInstance(String name) {
    if (name == null) {
      name = System.getProperty(DEFAULT_DOM_PROPERTY);
      if (name == null) {
	return new Xerces();
      }
    }

    try {
      Class domclass = Class.forName(name);
      DOM dom = (DOM) domclass.newInstance();
      return dom;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (InstantiationException e) {
      e.printStackTrace();
      return null;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }
  }
}
