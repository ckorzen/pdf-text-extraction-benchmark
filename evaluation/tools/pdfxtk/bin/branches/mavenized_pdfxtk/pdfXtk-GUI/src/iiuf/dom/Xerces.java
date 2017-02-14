package iiuf.dom;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

/**
    (c) 2000, IIUF

    Wrapper class for the Apache Group's XML parser.

    @author $author$
    @version $revision$
*/

public class Xerces
  extends DOM
  implements EntityResolver
{
  public Document parseDocument(InputSource input, boolean validate)
    throws SAXException, IOException
  {
    DOMParser parser = new DOMParser();

    parser.setFeature("http://xml.org/sax/features/validation", validate);
    parser.setEntityResolver(this);
    parser.parse(input);

    return parser.getDocument();
  }

  public Document createDocument() {
    return new DocumentImpl();
  }

  public void writeDocument(Document document, Writer writer)
    throws IOException
  {
    XMLSerializer s = new XMLSerializer(writer, new OutputFormat());
    s.serialize(document);
  }

  public InputSource resolveEntity (String publicId, String systemId)
    throws IOException
  {
    if (!systemId.startsWith("http://")) {
      String newSystemId = "/"+systemId.substring((systemId.indexOf('h')));
      return new InputSource(new FileInputStream(newSystemId));
    }

    return null;
  }
}
