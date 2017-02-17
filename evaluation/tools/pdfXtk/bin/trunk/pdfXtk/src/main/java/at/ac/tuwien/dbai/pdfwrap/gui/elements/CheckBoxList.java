package at.ac.tuwien.dbai.pdfwrap.gui.elements;

import at.ac.tuwien.dbai.pdfwrap.gui.layer.Style;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * A add-on for the JList class in order to get check boxes running 
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class CheckBoxList extends JList<JCheckBox> {
	
	//The name that is later displayed in the GUI as a heading
	private final String componentName = "Layers";

	/**
	 * The constructor Method for creating a CheckBoxList
	 * 
	 * @param styleMap A map mapping the name of the layer to the corresponding Style information
	 * @param attributePanel A panel for displaying the attributes of the selected segments
	 */
	public CheckBoxList(final HashMap<String,Style> styleMap, final SelectionPanel attributePanel) {
		
		//Set the CellRenderer to be able to display check boxes
		this.setCellRenderer(new ListCellRenderer<JCheckBox>() {

			@Override
			public Component getListCellRendererComponent(
					JList<? extends JCheckBox> list, JCheckBox value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
					return value;
				}
		});
		
		//Organize the Model for the CheckBoxList - here create one check box element for every layer
		DefaultListModel<JCheckBox> checkBoxModel = new DefaultListModel<JCheckBox>();
		
		//Sort the layer names alphabetically
		ArrayList<String> sortedLayerNames = new ArrayList<String>(styleMap.keySet());
		
		Collections.sort(sortedLayerNames);
		
		for (String name : sortedLayerNames) {
			
			JCheckBox checkBox = new JCheckBox(name);
			checkBox.setSelected(true);
			
			checkBoxModel.addElement(checkBox);
		}
		
		this.setModel(checkBoxModel);
		
		//Adds the possibility to interact with the check boxes of the list
	    this.addMouseListener(new MouseAdapter() {
	    	
	    	@Override
	        public void mousePressed(MouseEvent e) {
	    		
	          int index = locationToIndex(e.getPoint());
	          
	          if (index != -1) {
	        	  
	            JCheckBox checkbox = (JCheckBox) getModel().getElementAt(
	                index);
	            
	            boolean setSelect = !checkbox.isSelected();
	            
	            checkbox.setSelected(setSelect);
	            
	            //Set whether the segments belonging to this layer should be painted or not
	            styleMap.get(checkbox.getText()).setPrintable(setSelect);
	            
	            //Update the attribute panel accordingly to the visibility of the segments
	            attributePanel.updateSegmentVisibility();
	            
	            getRootPane().repaint();
	          }
	        }	    	
	      });
	    
	    this.setOpaque(false);
	    this.setBorder(BorderFactory.createTitledBorder(componentName));
	}
}
