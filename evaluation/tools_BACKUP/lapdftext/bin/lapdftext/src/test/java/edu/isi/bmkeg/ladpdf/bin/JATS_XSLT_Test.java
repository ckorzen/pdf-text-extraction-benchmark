package edu.isi.bmkeg.ladpdf.bin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.isi.bmkeg.utils.springContext.BmkegProperties;

public class JATS_XSLT_Test extends TestCase
{

	BmkegProperties prop;
	String login, password, dbUrl;

	File xmlFile, htmlFile, txtFile;
	File f1, f2, f3;
	
	protected void setUp() throws Exception
	{ 	
		URL u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8/pbio.1000441.xml");
		xmlFile = new File( u.getPath() );
		htmlFile = new File( xmlFile.getParentFile().getPath() + "/pbio.1000441.html" );
		txtFile = new File( xmlFile.getParentFile().getPath() + "/pbio.1000441.txt" );
	}

	protected void tearDown() throws Exception	{
	}

	@Test
	public void test1() throws Exception
	{		

		FileReader inputReader = new FileReader(xmlFile);
		StringWriter stringWriter = new StringWriter();
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Resource xslResource = new ClassPathResource(
				"jatsPreviewStyleSheets/xslt/main/jats-html-textOnly.xsl"
				);
		StreamSource xslt = new StreamSource(xslResource.getInputStream());
		Transformer transformer = tf.newTransformer(xslt);

		StreamSource source = new StreamSource(inputReader);
		StreamResult result = new StreamResult(stringWriter);
		transformer.transform(source, result);

		String html = stringWriter.toString();  
		
		Document doc = Jsoup.parse(html);

		//
		// pulling the text from the html, we preserve some of the formatting as simple html 
		// tags with no additional ID values. We strip the 
		//
		doc.select("div").unwrap();
		
		// Ditch <span> and <img> elements completely, 
		// TODO: may need to check this, what text might occur within spans?
		doc.select("span").remove();
		doc.select("img").remove();
		
		Elements bodyEls = doc.select("body");
		for( Element bodyEl : bodyEls ) {
			for( Element el : bodyEl.getAllElements() ) {
				List<String> keys = new ArrayList<String>();
				for( Attribute at : el.attributes() ) {
					keys.add(at.getKey());
				}
				for( String key : keys ) {
					el.removeAttr(key);
				}
			}
			List<Node> comments = new ArrayList<Node>();
			for(Node n: bodyEl.childNodes()){
	            if(n instanceof Comment){
	                comments.add(n);
	            }
	        }
			for(Node n : comments){
                n.remove();
	        }
		}
		
		FileWriter outputWriter = new FileWriter(htmlFile);
		BufferedWriter bw = new BufferedWriter(outputWriter);
				
		bw.write(doc.toString());
		
		bw.close();

		String text = doc.select("body").text();

		
		int i = 0;
		i++;
		
		assert(true);
	
	}
	
}
