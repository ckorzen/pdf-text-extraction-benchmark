package iiuf.swing.propertiespanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.util.Hashtable;

import iiuf.awt.Awt;

/**
   ComboBox implementation.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public class ComboBox 
  extends
  Property
{
  private String   label;
  private Object   initialValue;
  private Object[] values;
  private String   unit;

  public ComboBox(boolean required, String key, String label_, String unit_, Object initialValue_, Object[] values_) {
    super(required, key);
    label        = label_;
    initialValue = initialValue_;
    values       = values_;
    unit         = unit_;
  }
  
  public ComboBox(String key, String label, String unit, Object initialValue, Object[] values) {
    this(false, key, label, unit, initialValue, values);
  }
  
  public ComboBox(boolean required, String key, String label, Object initialValue, Object[] values) {
    this(false, key, label, null, initialValue, values);    
  }
  
  public ComboBox(String key, String label, Object initialValue, Object[] values) {
    this(false, key, label, null, initialValue, values);
  }
  
  public void read(PropertiesPanel panel, Hashtable values) {
    values.put(key, ((JComboBox)panel.getCmp(this)).getSelectedItem());
  }
  
  public void write(PropertiesPanel panel, Hashtable values) {
    Object v = values.get(key);
    if(v != null)
      ((JComboBox)panel.getCmp(this)).setSelectedItem(v);
  }
  
  public void create(PropertiesPanel panel) {
    JLabel l =new JLabel(label);
    l.setForeground(required ? PropertiesPanel.REQUIRED : PropertiesPanel.NON_REQUIRED);
    panel.container.add(l, Awt.constraints(false));
    JComboBox  cb   = new JComboBox();
    cb.setEnabled(enabled);
    for(int i = 0; i < values.length; i++)
      cb.addItem(values[i]);
    cb.setSelectedItem(initialValue);
    panel.valuecmps.put(key, cb);
    panel.container.add(cb, Awt.constraints(unit == null));
    if(unit != null)
      panel.container.add(new JLabel(unit), Awt.constraints(true));
  }  
}

/*
  $Log: ComboBox.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.6  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.5  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.3  2000/12/20 09:46:39  schubige
  TJGUI update

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/10/09 06:49:26  schubige
  Added properties panel
  
*/
