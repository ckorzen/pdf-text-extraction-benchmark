package iiuf.swing.propertiespanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.Hashtable;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.border.TitledBorder;

import iiuf.awt.Awt;

/**
   List property implementation.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class List
  extends
  Property
{
  private String           label;
  private ListAccess       access;
  private int              visibleRowCount;
  private DefaultListModel model = new DefaultListModel();
  
  public List(boolean required,  String key, String label_, int visibleRowCount_, ListAccess access_) {
    super(required, key);
    label           = label_;
    access          = access_;
    visibleRowCount = visibleRowCount_;
  }
  public List(String key, String label, int visibleRowCount, ListAccess access) {
    this(false, key, label, visibleRowCount, access);
  } 
  
  public void read(PropertiesPanel panel, Hashtable values) {
    String value = access.encode(getElements(panel));
    if(value == null || value.equals("")) return;
    values.put(key, value);
  }
  
  public void write(PropertiesPanel panel, Hashtable values) {
    model.clear();
    Object[] os = access.decode((String)values.get(key));
    for(int i = 0; i < os.length; i++)
      model.addElement(os[i]);
  }
  
  private JList getList(PropertiesPanel panel) {
    return (JList)panel.getCmp(this);
  }
  
  private Object[] getElements(PropertiesPanel panel) {
    JList    l      = getList(panel);
    Object[] result = new Object[l.getModel().getSize()];
    for(int i = 0; i < result.length; i++)
    result[i] = l.getModel().getElementAt(i);
    return result;
  }
  
  public void create(PropertiesPanel panel) {
    JPanel      buttons = new JPanel();
    JPanel      p       = new JPanel();
    JList       l       = new JList(model);

    l.setVisibleRowCount(visibleRowCount);
    JScrollPane sp = new JScrollPane(l);
    
    buttons.setLayout(new BorderLayout());
    buttons.add(BorderLayout.NORTH, access.newAddButton(l));
    buttons.add(BorderLayout.SOUTH, access.newRemoveButton(l));
    p.setLayout(new BorderLayout());
    p.add(BorderLayout.CENTER, sp);
    p.add(BorderLayout.EAST,   buttons);
    p.setBorder(new TitledBorder(label));
    panel.valuecmps.put(key, l);
    panel.container.add(p, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
  }
}

/*
  $Log: List.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/12/28 09:30:37  schubige
  SourceWatch beta
  
*/
