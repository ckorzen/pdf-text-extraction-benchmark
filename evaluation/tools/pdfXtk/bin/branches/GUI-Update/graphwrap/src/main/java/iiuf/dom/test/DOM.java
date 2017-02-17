package iiuf.dom.test;

import iiuf.util.StopWatch;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
    (c) 2000, IIUF

    Test class for our DOM wrapper.

    @author $author$
    @version $revision$
*/

public class DOM {
  public static void main(String[] arg) 
    throws Exception
  {
    if (arg.length == 3) {
      iiuf.dom.DOM dom = iiuf.dom.DOM.getInstance(arg[0]);

      if (arg[1].equals("-r")) {
	InputSource is = new InputSource(new FileInputStream(arg[2]));
	is.setSystemId(arg[2]);
	StopWatch sw = new StopWatch();
	sw.start();
	Document doc = dom.parseDocument(is, false);
	sw.stop();
	dom.writeDocument(doc, new OutputStreamWriter(System.out));
	System.out.println("Parsing took "+sw);
	return;
      } else if (arg[1].equals("-c")) {
	Document doc = dom.createDocument();
	Element root, e;
	
	// Append the root element
	doc.appendChild(root = doc.createElement("root"));
	
	e = root;

	int depth = Integer.parseInt(arg[2]);
	for (int i = 0; i < depth; i++) {
	  // Append one child with text content and an attribute
	  e.appendChild(e = doc.createElement("child"));
	  e.appendChild(doc.createTextNode("content"));
	  e.setAttribute("depth", Integer.toString(i));
	}

	dom.writeDocument(doc, new OutputStreamWriter(System.out));
	return;
      }
    }

    System.err.println("Usage: iiuf.dom.test.DOM [DOM Class] [-r xml-file] [-c depth]");
    System.err.println("       -r   read XML file and write DOM structure to stdout");
    System.err.println("       -c   create DOM structure of certain depth and write it to stdout");
    System.exit(1);
  }
}
