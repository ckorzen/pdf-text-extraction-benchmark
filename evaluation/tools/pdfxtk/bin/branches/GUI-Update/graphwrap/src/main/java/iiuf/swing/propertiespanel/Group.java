package iiuf.swing.propertiespanel;

import java.util.Hashtable;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import iiuf.awt.Awt;

/**
   Property group implementation.<p>

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public class Group
  extends
  Property
{
  private String     label;
  private Property[] content;
  
  public Group(String label_, Property[] content_) {
    super(false, label_);
    label   = label_;
    content = content_;
  }
  
  public void read(PropertiesPanel panel, Hashtable values) {
    for(int i = 0; i < content.length; i++)
      content[i].read(panel, values);
  }
  
  public void write(PropertiesPanel panel, Hashtable values) {
    for(int i = 0; i < content.length; i++)
      content[i].write(panel, values);
  }
  
  public boolean isValid(PropertiesPanel panel, JComponent value) {
    boolean result = true;
    for(int i = 0; i < content.length; i++)
      result &= content[i].isValid(panel, panel.getCmp(content[i]));
    return result;
  }
  
  public void create(PropertiesPanel panel) {
    panel.groups.push(panel.container);
    panel.container = new JPanel();
    panel.container.setLayout(new GridBagLayout());
    panel.container.setBorder(new TitledBorder(label));
    for(int i = 0; i < content.length; i++)
      content[i].create(panel);
    ((JPanel)panel.groups.peek()).add(panel.container, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
      panel.container = (JPanel)panel.groups.pop();
  } 
}

/*
  $Log: Group.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/10/09 06:49:26  schubige
  Added properties panel
  
*/
