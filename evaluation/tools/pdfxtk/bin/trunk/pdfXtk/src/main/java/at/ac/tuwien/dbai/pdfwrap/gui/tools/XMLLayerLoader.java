package at.ac.tuwien.dbai.pdfwrap.gui.tools;

import at.ac.tuwien.dbai.pdfwrap.gui.exceptions.UnknownShapeException;
import at.ac.tuwien.dbai.pdfwrap.gui.layer.Shapes;
import at.ac.tuwien.dbai.pdfwrap.gui.layer.Style;
import at.ac.tuwien.dbai.pdfwrap.gui.layer.StyledSegment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class for loading the xml file which contains the layer styling information
 * Style objects are later needed to create {@link StyledSegment}.
 * 
 * @author Timo Schleicher
 *
 */
public class XMLLayerLoader {

	
	/**
	 * Parses the xml file and creates style objects for each layer specified.
	 * 
	 * @return A Map mapping each layer name to the corresponding style object.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static HashMap<String,Style> readXML( String xmlFilePath ) throws ParserConfigurationException, SAXException, IOException, IllegalArgumentException {
		
		HashMap<String,Style> ret = new HashMap<String,Style>();
			
		//Initialize the parsing process
		File xmlFile = new File(xmlFilePath);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		
		//Get all layer elements
		NodeList nList = doc.getElementsByTagName("layer");

		for (int i = 0; i < nList.getLength(); i++) {
			
			Node nNode = nList.item(i);
			
			//Parse each layer element and check for validity
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element eElement = (Element) nNode;
				
				String name = eElement.getAttribute("name");
				
				if (ret.containsKey(name)) {
					throw new IllegalArgumentException("The layer with the name \"" + name + "\" was specified twice in the xml description.");
				}
				String source = eElement.getAttribute("source");
				
				//Read the shape as a value of the Shapes enumeration
				Shapes shape = null;
				
				try {
					
					shape = Shapes.valueOf(eElement.getElementsByTagName("shape").item(0).getTextContent());
					
				} catch (IllegalArgumentException e) {
					
					throw new UnknownShapeException(eElement.getElementsByTagName("shape").item(0).getTextContent());
					
				}

				
				boolean filled = Boolean.parseBoolean(eElement.getElementsByTagName("filled").item(0).getTextContent());
				int strokeWidth = Integer.parseInt(eElement.getElementsByTagName("stroke-width").item(0).getTextContent());
				
				if (strokeWidth < 0) {
					
					throw new IllegalArgumentException("Stroke width of the layer \"" + name + "\" should not be negative: " + strokeWidth);
				}
				
				Node colorNode = eElement.getElementsByTagName("color").item(0);
				
				//Parse the color values (RGBa)
				int r = 0, g = 0, b = 0, a = 0;
				
				if (colorNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element colorElement = (Element) colorNode;
					
					r = Integer.parseInt(colorElement.getElementsByTagName("r").item(0).getTextContent());
					g = Integer.parseInt(colorElement.getElementsByTagName("g").item(0).getTextContent());
					b = Integer.parseInt(colorElement.getElementsByTagName("b").item(0).getTextContent());
					a = Integer.parseInt(colorElement.getElementsByTagName("a").item(0).getTextContent());
					
					if ((r | g | b | a) > 255 | (r | g | b | a) < 0) {
						
						throw new IllegalArgumentException("The RGBa values of the layer \"" + name + "\" should be between 0 and 255.");
					}
				}
							
				ret.put(name, new Style(filled, r, g, b, a, strokeWidth, shape, source));		
			}
		}
		
		return ret;
	}
}
