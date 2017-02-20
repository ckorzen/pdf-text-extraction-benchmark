package at.ac.tuwien.dbai.pdfwrap.gui.elements;

import at.ac.tuwien.dbai.pdfwrap.gui.exceptions.UnchangeableAttributeException;
import at.ac.tuwien.dbai.pdfwrap.gui.layer.StyledSegment;
import at.ac.tuwien.dbai.pdfwrap.gui.tools.MultiLineTooltip;
import at.ac.tuwien.dbai.pdfwrap.model.document.AttributeTuple;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

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
			
			@Override
			public boolean isCellEditable(int row, int column) {
				
				if (column == 1) {
					
					return true;
					
				} else {
					
					return false;
				}
			}
		};		
		
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
	private DefaultTableModel getAttributeText(final StyledSegment segment) {
		
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
		
		//Add cell value changed listener
		model.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				
				int row = e.getFirstRow();
		        int column = e.getColumn();
		        TableModel model = (TableModel) e.getSource();
		        String attrName = (String) model.getValueAt(row, 0);
		        String newData = (String) model.getValueAt(row, column);  		       
		        
		        //Search for the attribute that has been modified by the user
		        for (AttributeTuple attr : segment.getSegment().getAttributes()) {
		        	
		        	if (attr.getAttributeName().equals(attrName)) {
		        		
		        		//Get the MainFrame for later noticing it
	        			MainFrame frame = (MainFrame) SwingUtilities.getRoot(SelectionPanel.this);
	        			
	        			//Check if the attribute has changed at all
	        			if (!newData.equals(attr.getAttributeValue())) {
	        				
	        				try {
								
								//Try to transfer the modifications into the segment object
								segment.getSegment().setAttribute(attrName, newData);
								
							} catch (NumberFormatException ex) {
								
								//Exception while parsing new String into the attribute value			
								JOptionPane.showMessageDialog(frame, "Type missmatch! Make sure you use a valid input value for the attribute.");
								model.setValueAt(attr.getAttributeValue(), row, column);
								
								return;
								
							} catch (UnchangeableAttributeException ex) {										
								
								//Exception while modifying an attribute for which modification is not allowed/defined	
								JOptionPane.showMessageDialog(frame, ex.getMessage());
								model.setValueAt(attr.getAttributeValue(), row, column);
								
								return;
							}
	        			}
						

						//Update the view to display the changes
	        			frame.getPDFPanel().updateResizeScaleFactor(segment);
		        		
		        		return;
		        	}
		        }	        
			}
		});
		
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
