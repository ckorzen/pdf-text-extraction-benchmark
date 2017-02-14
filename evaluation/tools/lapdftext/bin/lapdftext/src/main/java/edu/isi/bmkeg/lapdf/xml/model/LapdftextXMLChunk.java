package edu.isi.bmkeg.lapdf.xml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="chunk")
public class LapdftextXMLChunk extends LapdftextXMLRectangle implements Serializable {
	static final long serialVersionUID = 8047039304729208683L;

	private String font;
	private int fontSize;
	private String type;
	private List<LapdftextXMLWord> words = new ArrayList<LapdftextXMLWord>();

	@XmlAttribute
	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	@XmlAttribute
	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElementWrapper( name="words" )
    @XmlElement( name="wd" )
	public List<LapdftextXMLWord> getWords() {
		return words;
	}

	public void setWords(List<LapdftextXMLWord> words) {
		this.words = words;
	}
	
	public String toString() {
		String s = "";
		for( LapdftextXMLWord word : this.words ) {
			if( s.length() > 0 )
				s += " ";
			s += word.getT();
		}
		return s;
	}

}
