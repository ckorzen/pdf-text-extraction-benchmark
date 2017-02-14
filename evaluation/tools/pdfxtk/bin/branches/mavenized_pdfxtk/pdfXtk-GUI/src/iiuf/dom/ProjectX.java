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
import com.sun.xml.tree.*;
import com.sun.xml.parser.*;

/**
    (c) 2000, IIUF

    Wrapper class for Sun's Project X XML parser implementation.

    @author $author$
    @version $revision$
*/

public class ProjectX 
  extends DOM
  implements EntityResolver
{
  public Document parseDocument(InputSource input, boolean validate)
    throws SAXException, IOException
  {
    Parser parser;
    XmlDocumentBuilder builder;

    if (validate) {
      parser = new ValidatingParser(true);
    } else {
      parser = new Parser();
    }

    parser.setEntityResolver(this);

    builder = new XmlDocumentBuilder();
    builder.setParser(parser);
    parser.parse(input);
    return builder.getDocument();
  }

  public Document createDocument() {
    return new XmlDocument();
  }

  public void writeDocument(Document document, Writer writer)
    throws IOException
  {
    XmlDocument doc = (XmlDocument) document;
    doc.write(writer);
  }

  public InputSource resolveEntity (String publicId, String systemId)
    throws IOException
  {
    if (!systemId.startsWith("http://")) {
      return new InputSource(new FileInputStream(systemId));
    }

    return null;
  }
}
