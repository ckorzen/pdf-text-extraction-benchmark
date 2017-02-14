package at.ac.tuwien.dbai.pdfwrap.gui.elements;

import at.ac.tuwien.dbai.pdfwrap.gui.layer.StyledSegment;
import at.ac.tuwien.dbai.pdfwrap.gui.tools.MultiLineTooltip;
import at.ac.tuwien.dbai.pdfwrap.model.document.AttributeTuple;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A class for displaying the attributes of a selected segment within a JPanel
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class SelectionPanel extends JPanel {

	//The name that is later displayed in the GUI as a heading
	private final String componentName = "Attributes";
	
	private JComboBox<StyledSegment> selectionBox;
	
	private JTable attributeTable;
	
	private ArrayList<StyledSegment> segments;
	
	/**
	 * The constructor method for creating a SelectionPanel
	 */
	public SelectionPanel() {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		selectionBox = new JComboBox<StyledSegment>() {
			
			@Override
			public Dimension getMaximumSize() {
				
				//Need to override because of height resize bug
				Dimension max = super.getMaximumSize();
		        max.height = getPreferredSize().height;
		        
		        return max;
			}
		};
		
		selectionBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				attributeTable.setModel((getAttributeText((StyledSegment)selectionBox.getSelectedItem())));
			}
		});
		
		add(selectionBox);
		
		add(Box.createRigidArea(new Dimension(0, 13)));
		
		//Create the JTable with a customized tool tip behavior
		attributeTable = new JTable() {
			
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				
		        Component c = super.prepareRenderer(renderer, row, column);
		        
		        if (c instanceof JComponent) {

		            JComponent jc = (JComponent) c;        	            
		            
		            //Setting of the tool tip
		            jc.setToolTipText(MultiLineTooltip.splitToolTip(getValueAt(row, column).toString()));
		        }
		        
		        return c;
		    }

			@Override
			public JToolTip createToolTip() {
				
				//Customizing tool tip for better visibility
				JToolTip tip = super.createToolTip();
				
				tip.setForeground(Color.BLACK);
				tip.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				
				return tip;
			}
		};
		
		attributeTable.setEnabled(false);
		
		//Add the attribute table and its header to the JPanel
		add(attributeTable.getTableHeader());
		add(attributeTable);
		
	    setBorder(BorderFactory.createTitledBorder(componentName));
	    
	    segments = new ArrayList<StyledSegment>();
	}
	
	/**
	 * Set the selected segments in order to be able to choose them from the combo box.
	 * If segments are null then the selection panel will be cleared completely
	 * 
	 * @param segments The segments that were selected
	 */
	public void setSelectedElements(ArrayList<StyledSegment> segments) {
		
		if (segments == null) {
			
			this.segments = new ArrayList<StyledSegment>();
			
		} else {
			
			this.segments = segments;
			Collections.sort(this.segments);
		}
		
		updateSegmentVisibility();
	}
	
	/**
	 * Update the segment list within the combo box by caring about the visibility of each segment
	 */
	public void updateSegmentVisibility() {
		
		selectionBox.removeAllItems();
		
		for (StyledSegment seg : segments) {
			
			if (seg.isVisible()) {
				
				selectionBox.addItem(seg);
			}
		}
	}
	
	/**
	 * Returns for one segment a table model containing all its attributes.
	 * 
	 * @param segment the segment you wanted to display its attributes
	 * @return a table model containing the attributes of the selected segment
	 */
	private DefaultTableModel getAttributeText(StyledSegment segment) {
		
		if (segment == null) {
			
			return new DefaultTableModel();
		}
		
		DefaultTableModel model = new DefaultTableModel();
		
		//Set the table header
		model.setColumnIdentifiers(new String[]{"Attribute","Value"});
		
		//Set the table rows (attributes)
		for (AttributeTuple attr : segment.getSegment().getAttributes()) {
			
			model.addRow(new String[]{attr.getAttributeName(),attr.getAttributeValue()});
		}
		
		return model;
	}
	
	/**
	 * Getter method for getting all the currently selected segments
	 * 
	 * @return Returns a list of the currently selected segments
	 */
	public ArrayList<StyledSegment> getSelectedSegments() {
		
		return segments;
	}
	
}
