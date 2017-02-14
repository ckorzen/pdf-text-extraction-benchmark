package edu.isi.bmkeg.lapdf.xml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="page")
public class LapdftextXMLPage extends LapdftextXMLRectangle implements Serializable {
	static final long serialVersionUID = 8047039304729208683L;

	private LapdftextXMLRectangle margin;
	private int mostPopWordHeight;
	private int pageNumber;
	private List<LapdftextXMLChunk> chunks = new ArrayList<LapdftextXMLChunk>();

	@XmlElement(name="margin")
	public LapdftextXMLRectangle getMargin() {
		return margin;
	}

	public void setMargin(LapdftextXMLRectangle margin) {
		this.margin = margin;
	}

	@XmlAttribute
	public int getMostPopWordHeight() {
		return mostPopWordHeight;
	}

	public void setMostPopWordHeight(int mostPopWordHeight) {
		this.mostPopWordHeight = mostPopWordHeight;
	}

	@XmlAttribute
	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	@XmlElementWrapper( name="chunks" )
    @XmlElement( name="chunk" )
	public List<LapdftextXMLChunk> getChunks() {
		return chunks;
	}

	public void setChunks(List<LapdftextXMLChunk> chunks) {
		this.chunks = chunks;
	}
		
}
