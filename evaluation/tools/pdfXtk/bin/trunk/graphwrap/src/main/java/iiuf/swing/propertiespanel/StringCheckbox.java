package iiuf.swing.propertiespanel;

import java.util.Hashtable;
import javax.swing.JCheckBox;

import iiuf.awt.Awt;

/**
   String checkbox implementation.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public class StringCheckbox
  extends
  Property
{
  private String   label;
  private boolean  initialState;
  private String   trueValue;
  private String   falseValue;
  
  public StringCheckbox(boolean required, String key, String label_, boolean initialState_, 
			String falseValue_, String trueValue_) {
    super(required, key);
    label        = label_;
    initialState = initialState_;
    trueValue    = trueValue_;
    falseValue   = falseValue_;
  }
  
  public StringCheckbox(String key, String label, boolean initialState, String falseValue, String trueValue) {
    this(false, key, label, initialState, falseValue, trueValue);
  }
  
  public void read(PropertiesPanel panel, Hashtable values) {
    values.put(key, ((JCheckBox)panel.getCmp(this)).isSelected() ? trueValue : falseValue);
  }
  
  public void write(PropertiesPanel panel, Hashtable values) {
    Object v = values.get(key);
    if(trueValue.equals(v)) ((JCheckBox)panel.getCmp(this)).setSelected(true);
    else                    ((JCheckBox)panel.getCmp(this)).setSelected(false);
  }
  
  public void create(PropertiesPanel panel) {
    panel.container.add(Awt.newComponent(), Awt.constraints(false));
    JCheckBox cb = new JCheckBox(label, initialState);
    cb.setEnabled(enabled);
    cb.setForeground(required ? PropertiesPanel.REQUIRED : PropertiesPanel.NON_REQUIRED);
    panel.valuecmps.put(key, cb);
    panel.container.add(cb, Awt.constraints(true));
  }
}
/*
  $Log: StringCheckbox.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.5  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.4  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/10/09 06:49:27  schubige
  Added properties panel
  
*/
