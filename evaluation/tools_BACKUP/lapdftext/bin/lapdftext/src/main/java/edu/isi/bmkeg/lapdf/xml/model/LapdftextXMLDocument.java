package edu.isi.bmkeg.lapdf.xml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="document")
public class LapdftextXMLDocument implements Serializable {
	static final long serialVersionUID = 8047039304729208683L;

	private Long vpdmfId;
	
	private List<LapdftextXMLPage> pages = new ArrayList<LapdftextXMLPage>();

	private List<LapdftextXMLFontStyle> fontStyles = new ArrayList<LapdftextXMLFontStyle>();

	@XmlAttribute
	public Long getVpdmfId() {
		return vpdmfId;
	}

	public void setVpdmfId(Long vpdmfId) {
		this.vpdmfId = vpdmfId;
	}

	@XmlElementWrapper( name="pages" )
    @XmlElement( name="page" )
	public List<LapdftextXMLPage> getPages() {
		return pages;
	}

	public void setPages(List<LapdftextXMLPage> pages) {
		this.pages = pages;
	}

	@XmlElementWrapper( name="fontStyles" )
    @XmlElement( name="fontStyle"  )
	public List<LapdftextXMLFontStyle> getFontStyles() {
		return fontStyles;
	}

	public void setFontStyles(List<LapdftextXMLFontStyle> fontStyles) {
		this.fontStyles = fontStyles;
	}
	
}
