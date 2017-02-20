package at.ac.tuwien.dbai.pdfwrap.gui.tools;

import at.ac.tuwien.dbai.pdfwrap.model.document.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for (de-)serializing the PDF analysis output.
 * 
 * @author Timo Schleicher
 *
 */
public class PDF_XMLSerializer {

	/**
	 * Serializes the result of a PDF analysis to XML.
	 * 
	 * @param savePath The path where you want to save the XML
	 * @param pdfOutput The output of the PDF analysis process
	 * @param curFile The current file either the PDF itself or a XML file
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void serialize(String savePath, List<Page> pdfOutput, File curFile) throws ParserConfigurationException, TransformerException, SAXException, IOException {
		
		//Take care of the file extension - maybe we need to add a XML extension
		savePath = (savePath.toLowerCase().endsWith(".xml")) ? savePath : savePath.concat(".xml");
		
		//Determine the path of the PDF file. I
		//f it is already a PDF just take its location otherwise get the path from the XML file.
		String pdfPath = (curFile.getPath().toLowerCase().endsWith(".pdf")) ? curFile.getPath() :
						  PDF_XMLSerializer.getPDFPath(curFile);		
		
		//Check whether the file was not deleted or moved during the process
		if (!new File(pdfPath).exists()) {
			
			throw new FileNotFoundException("Can not find the following file:\n" + pdfPath);
		}
		
		//Initialization stuff
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		//Get the root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("PDFDocument");
		
		rootElement.setAttribute("path", pdfPath);
		doc.appendChild(rootElement);
				
		for (Page page : pdfOutput) {
			
			Element pageE = doc.createElement("page");
			
			//Serialize all page attributes
			for (AttributeTuple attr : page.getAttributes()) {
				
				pageE.setAttribute(attr.getAttributeName(), attr.getAttributeValue());
			}
			
			rootElement.appendChild(pageE);
			
			//Serialize all segment attributes
			for (GenericSegment seg : page.getItems()) {
				
				Element segment = doc.createElement("segment");
				
				for (AttributeTuple attr : seg.getAttributes()) {
					
					segment.setAttribute(attr.getAttributeName(), attr.getAttributeValue());
				}
				
				pageE.appendChild(segment);
			}
		}
		
		//Write the content into XML file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(savePath));
		
		//Styling of the XML file with indenting lines
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		
		transformer.transform(source, result);
	}
	
	/**
	 * Load a previous analysis result from a XML file.
	 * 
	 * @param xml The XML file you want to load
	 * @return A list of Page objects
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<Page> deserializeAnalysis(File xml) throws ParserConfigurationException, SAXException, IOException {

		List<Page> pages = new ArrayList<Page>();
		
		//Initialization stuff
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		Document doc = dBuilder.parse(xml);
		doc.getDocumentElement().normalize();
		
		//Get all the page elements
		NodeList xmlPages = doc.getElementsByTagName("page");
		
		for (int i = 0; i < xmlPages.getLength(); i++) {
			
			Element xmlPage = (Element) xmlPages.item(i);
				
			//Get the page attributes
			float x1 = Float.valueOf(xmlPage.getAttribute("x1"));
			float x2 = Float.valueOf(xmlPage.getAttribute("x2"));
			float y1 = Float.valueOf(xmlPage.getAttribute("y1"));
			float y2 = Float.valueOf(xmlPage.getAttribute("y2"));
			
			int pageNumber = Integer.valueOf(xmlPage.getAttribute("pageNo"));
			
			//Get all the segments of a page element
			NodeList xmlSegments = xmlPage.getElementsByTagName("segment");
			
			ArrayList<GenericSegment> segList = new ArrayList<GenericSegment>();
			
			for (int k = 0; k < xmlSegments.getLength(); k++) {
				
				Element xmlSegment = (Element) xmlSegments.item(k);		
			
				try {
				
					GenericSegment seg = attrToSegment(xmlSegment);
					segList.add(seg);
					
				} catch (IOException e) {
						
					e.printStackTrace();
				}

			}			
			
			Page page = new Page(x1, x2, y1, y2, segList);
			page.setPageNo(pageNumber);
			
			pages.add(page);
		}
		
		return pages;
	}
	
	/**
	 * Method for loading a segment from a XML node. Make sure to correctly
	 * outline each possible segment within this method in order to get a proper loading of the segments.
	 * 
	 * @param node The node of the XML structure
	 * @return A GenericSegment loaded from the XML node
	 * @throws IOException
	 */
	private static GenericSegment attrToSegment(Element node) throws IOException {
		
		//Get the type of the segment as well as the coordinates
		String type = node.getAttribute("type");
		
		float x1 = Float.valueOf(node.getAttribute("x1"));
		float x2 = Float.valueOf(node.getAttribute("x2"));
		float y1 = Float.valueOf(node.getAttribute("y1"));
		float y2 = Float.valueOf(node.getAttribute("y2"));
		
		GenericSegment seg = null;
		
		//Find the corresponding segment -> add new segment types here in order to properly load them after saving
		switch (type) {
		
		case "line-segment":
			
			seg = new LineSegment(x1, x2, y1, y2);
			break;

		case "rect-segment":
					
			seg = new RectSegment(x1, x2, y1, y2);
			break;
			
		case "filled-rect":
			
			seg = new RectSegment(x1, x2, y1, y2);
			((RectSegment)seg).setFilled(true);
			break;
	
		case "text-block":
			
			String text = node.getAttribute("text");
			String fontName = node.getAttribute("font");
			float fontSize = Float.valueOf(node.getAttribute("fontsize"));
			
			seg = new TextBlock(x1, x2, y1, y2, text, fontName, fontSize);
			break;
			
		case "image-segment":
			
			seg = new ImageSegment(x1, x2, y1, y2);
			break;
			
		default:
			throw new IOException("Unknown segment. Please specify: " + node.getAttribute("type"));
		}
		
		return seg;
	}
	
	/**
	 * Get the path of the PDF file if working with a XML file.
	 * 
	 * @param xml The XML file
	 * @return The path of the PDF document that corresponds to the content of the XML file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String getPDFPath(File xml) throws ParserConfigurationException, SAXException, IOException {
		
		//Initialization stuff
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		Document doc = dBuilder.parse(xml);
		doc.getDocumentElement().normalize();
		
		//Get the root element
		NodeList nList = doc.getElementsByTagName("PDFDocument");
		
		Node nNode = nList.item(0);
			
		Element eElement = (Element) nNode;
		
		return eElement.getAttribute("path");
	}
}
