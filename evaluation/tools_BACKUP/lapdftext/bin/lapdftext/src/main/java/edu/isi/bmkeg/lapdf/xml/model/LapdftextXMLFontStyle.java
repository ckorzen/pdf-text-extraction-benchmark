package edu.isi.bmkeg.lapdf.xml.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="fontStyle")
public class LapdftextXMLFontStyle implements Serializable {

	private static final long serialVersionUID = 2378553987603139794L;

	private String fontStyle;

	private int id;

	@XmlAttribute	
	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}

	@XmlAttribute	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}	
	
}
