package iiuf.swing.propertiespanel;

import java.util.Hashtable;
import java.util.Stack;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import iiuf.awt.Awt;
import iiuf.swing.Swing;
import iiuf.swing.JNumberField;

/**
   Generic properties panel.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class PropertiesPanel 
  extends
  JPanel
{
  static final Color NON_REQUIRED = new JLabel().getForeground();
  static final Color REQUIRED     = NON_REQUIRED.darker();
  
  private Property[] desc;
  Hashtable          valuecmps = new Hashtable();
  Stack              groups    = new Stack();
  JPanel             container;
  JComponent         reqToggle;
  
  public PropertiesPanel(Property[] description, Hashtable values) {
    this(description);
    setProperties(values);
  }
  
  public PropertiesPanel(Property[] description) {
    setLayout(new GridBagLayout());
    desc      = description;
    container = this;
    for(int i = 0; i < desc.length; i++)
      desc[i].create(this);
    add(Awt.newComponent(), Awt.constraints(true, GridBagConstraints.BOTH));
  }
  
  JComponent getCmp(Property property) {
    return (JComponent)valuecmps.get(property.key);
  }
  
  public void setRequiredToggle(JComponent toggle) {
    reqToggle = toggle;
    checkRequired();
  }
  
  synchronized void checkRequired() {
    if(reqToggle == null) return;
    boolean checkFlag = true;
    for(int i = 0; i < desc.length; i++)
      checkFlag &= desc[i].isValid(this, getCmp(desc[i]));
    reqToggle.setEnabled(checkFlag);
  }
  
  JTextField createCheckingTF(String value) {
    return new CheckingTF(value);
  }
  
  JNumberField createCheckingNF(double value, double min, double max, boolean integer) {
    return integer ? new CheckingNF(value, (int)min, (int)max) : new CheckingNF(value, min, max);
  }
  
  class CheckingTF
    extends
    JTextField
  {
    CheckingTF(String content) {
      super(content);
    }
    
    protected Document createDefaultModel() {
      return new PlainDocument() {
	  public void insertString(int offs, String str, AttributeSet a) 
	    throws BadLocationException {
	    
	    if(str == null) return;
	    
	    super.insertString(offs, str, a);
	    
	    if(PropertiesPanel.this != null) PropertiesPanel.this.checkRequired();
	  }
	  
	  public void remove(int offs, int len) 
	    throws BadLocationException {
	    
	    super.remove(offs, len);
	    
	    if(PropertiesPanel.this != null) PropertiesPanel.this.checkRequired();
	  }
	};
    }    
  }

  class CheckingNF
    extends
    JNumberField
  {
    CheckingNF(double content, double min, double max) {
      super(content, min, max);
    }
    
    CheckingNF(int content, int min, int max) {
      super(content, min, max);
    }
    
    protected Document createDefaultModel() {
      return new JNumberField.NumberDocument() {
	  public void insertString(int offs, String str, AttributeSet a) 
	    throws BadLocationException {
	    
	    if(str == null) return;
	    
	    super.insertString(offs, str, a);
	    
	    if(PropertiesPanel.this != null) PropertiesPanel.this.checkRequired();
	  }
	  
	  public void remove(int offs, int len) 
	    throws BadLocationException {
	    
	    super.remove(offs, len);
	    
	    if(PropertiesPanel.this != null) PropertiesPanel.this.checkRequired();
	  }
	};
    }    
  }
  
  public Hashtable getProperties() {
    Hashtable result = new Hashtable();
    for(int i = 0; i < desc.length; i++)
      desc[i].read(this, result);
    return result;
  }
  
  public Object getProperty(String key) {
    return getProperties().get(key);
  }
  
  public void setProperties(Hashtable properties) {
    for(int i = 0; i < desc.length; i++)
      desc[i].write(this, properties);
    checkRequired();
  }
    
  static PropertiesPanel pp = new PropertiesPanel(new Property[] 
    {
      new TextField("text_field", "TEXT_FIELD:", 
		    "some text"),
      new ComboBox( "combo_box",  "COMBO_BOX:", "earth",
		    new String[] {"mercury","venus","earth","mars","jupituer","saturn","uranus","netptune","pluto"}),
      new StringCheckbox("string_checkbox", "STRING_CEHCKBOX",
			 true, "false", "true"),
      new Group("Required",
		new Property[] {
		  new TextField(true, "r_text_field", "REQ_TEXT_FIELD:", "some required text"),
		  new ComboBox(true, "r_combo_box",  "REQ_COMBO_BOX:",  "earth",
			       new String[] {"mercury","venus","earth","mars","jupituer",
					     "saturn","uranus","netptune","pluto"}),
		  new StringCheckbox(true, "r_string_checkbox", "REQ_STRING_CEHCKBOX",
				     true, "false", "true")
		}),
    });
  
  public static void main(String[] argv) {
    JFrame          frame = new JFrame();
    
    JButton b = Swing.newButton("OK", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  System.out.println(pp.getProperties());
	}
      });
    
    pp.setRequiredToggle(b);
    
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(pp, BorderLayout.CENTER);
    frame.getContentPane().add(b,  BorderLayout.SOUTH);
    frame.pack();
    frame.setVisible(true);
  }
}
 
/*
  $Log: PropertiesPanel.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/03/12 17:52:00  schubige
  Added version support to sourcewatch and enhanced soundium

  Revision 1.3  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.1  2000/10/09 06:49:27  schubige
  Added properties panel

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and context menu stuff

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added
  
*/
