package iiuf.swing;

import java.awt.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;

/**
   (c) 2001, DIUF<p>

   A JList that allows to select and deselect JCheckBoxes.
 
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class CheckBoxList extends JList {

  public CheckBoxList() {
    super();
    init();
  }

  public CheckBoxList(ListModel dataModel) {
    super(dataModel);
    init();
  }

  public CheckBoxList(Object[] listData) {
    super(listData);
    init();
  }

  public CheckBoxList(Vector listData) {
    super(listData);
    init();
  }

  ListCellRenderer renderer;

  void init() {
    addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	  JList list = (JList) e.getSource();
	  int index = list.locationToIndex(e.getPoint());

	  if (index < 0) {
	    return;
	  }

	  Object obj = list.getModel().getElementAt(index);
	  if (obj instanceof JCheckBox) {
	    JCheckBox cb = (JCheckBox) obj;
	    cb.setSelected(!cb.isSelected());
	    list.setSelectedIndex(index);
	    list.repaint();	// in case it was already selected
	  } else {
	    list.setSelectedIndex(index);
	  }
	}
      });

    renderer = getCellRenderer();
    setCellRenderer(new ListCellRenderer() {
	public Component getListCellRendererComponent(JList list, Object value,
						      int index, boolean isSelected,
						      boolean cellHasFocus) {
	  if (value instanceof JCheckBox) {
	    JCheckBox cb = (JCheckBox) value;
	    cb.setFocusPainted(true);
	    cb.setBackground(list.getBackground());
	    cb.setForeground(list.getForeground());
	    if (cellHasFocus) {
	      cb.setBorderPainted(true);
	      cb.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
	    } else {
	      cb.setBorderPainted(false);
	    }
	    return cb;
	  }

	  return renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}
      });
  }
}
