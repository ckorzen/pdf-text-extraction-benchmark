package iiuf.swing.propertiespanel;

import java.awt.GridBagConstraints;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

import iiuf.awt.Awt;
import iiuf.swing.JNumberField;

/**
   NumberField property implementation.<p>
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class NumberField 
  extends
  Property
{
  private String  label;
  private double  value;
  private double  min;
  private double  max;
  private boolean integer;
  
  public NumberField(boolean required, String key, String label_, double value_, double min_, double max_, boolean integer_) {
    super(required, key);
    label = label_;
    value = value_;
    min   = min_;
    max   = max_;
  }
  
  public NumberField(String key, String label, double value, double min, double max, boolean integer) {
    this(false, key, label, value, min, max, integer);
  }
    
  public void read(PropertiesPanel panel, Hashtable values) {
    if(((JNumberField)panel.getCmp(this)).getText().length() > 0)
      values.put(key, ((JNumberField)panel.valuecmps.get(key)).getText());
  }
  
  public void write(PropertiesPanel panel, Hashtable values) {
    Object v = values.get(key);
    if(v != null)
      ((JNumberField)panel.getCmp(this)).setText(v.toString());
  }
  
  public boolean isValid(PropertiesPanel panel, JComponent cmp) {
    return required ? ((JNumberField)cmp).getText().length() > 0 : true;
  }
  
  public void create(PropertiesPanel panel) {
    JLabel l =new JLabel(label);
    l.setForeground(required ? PropertiesPanel.REQUIRED : PropertiesPanel.NON_REQUIRED);
    panel.container.add(l, Awt.constraints(false));
    JNumberField tf = null;
    if(required)
      tf = panel.createCheckingNF(value, min, max, integer);
    else
      if(integer)
	tf = new JNumberField((int)value, (int)min, (int)max);
      else
	tf = new JNumberField(value, min, max);
    tf.setEnabled(enabled);
    tf.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    panel.valuecmps.put(key, tf);
    panel.container.add(tf, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
  }
}

/*
  $Log: NumberField.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/03/12 17:53:42  schubige
  Added version support to sourcewatch and enhanced soundium

  Revision 1.3  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/10/09 06:49:27  schubige
  Added properties panel
  
*/
