package edu.isi.bmkeg.lapdf.xml.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="rectangle")
public class LapdftextXMLRectangle implements Serializable {
	static final long serialVersionUID = 8047039304729208683L;

	private int id;
	private int w;
	private int h;
	private int x;
	private int y;
	private int i;

	public LapdftextXMLRectangle() {}

	public LapdftextXMLRectangle(int id, int w, int h, int x, int y) {
		this.setH(h);
		this.setW(w);
		this.setX(x);
		this.setY(y);
	}
	
	@XmlAttribute
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@XmlAttribute
	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	@XmlAttribute
	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	@XmlAttribute
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	@XmlAttribute
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@XmlAttribute
	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}	
	


}
