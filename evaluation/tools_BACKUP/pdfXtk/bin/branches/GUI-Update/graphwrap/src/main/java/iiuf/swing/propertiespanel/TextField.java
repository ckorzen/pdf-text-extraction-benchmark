package iiuf.swing.propertiespanel;

import java.awt.GridBagConstraints;
import java.util.Hashtable;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

import iiuf.awt.Awt;

/**
   TextField property implementation.
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class TextField 
  extends
  Property
{
  private String label;
  private String value;
  
  public TextField(boolean required, String key, String label_, String value_) {
    super(required, key);
    label = label_;
    value = value_;
  }
  
  public TextField(String key, String label, String value) {
    this(false, key, label, value);
  }
    
  public void read(PropertiesPanel panel, Hashtable values) {
    if(((JTextField)panel.getCmp(this)).getText().length() > 0)
      values.put(key, ((JTextField)panel.valuecmps.get(key)).getText());
  }
  
  public void write(PropertiesPanel panel, Hashtable values) {
    Object v = values.get(key);
    if(v != null)
      ((JTextField)panel.getCmp(this)).setText(v.toString());
  }
  
  public boolean isValid(PropertiesPanel panel, JComponent cmp) {
    return required ? ((JTextField)cmp).getText().length() > 0 : true;
  }

  public void create(PropertiesPanel panel) {
    JLabel l =new JLabel(label);
    l.setForeground(required ? PropertiesPanel.REQUIRED : PropertiesPanel.NON_REQUIRED);
    panel.container.add(l, Awt.constraints(false));
    JTextField tf = null;
    if(required)
      tf = panel.createCheckingTF(value);
    else
      tf = new JTextField(value);
    tf.setEnabled(enabled);
    tf.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    panel.valuecmps.put(key, tf);
    panel.container.add(tf, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
  }
}

/*
  $Log: TextField.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/10/09 06:49:27  schubige
  Added properties panel
  
*/
