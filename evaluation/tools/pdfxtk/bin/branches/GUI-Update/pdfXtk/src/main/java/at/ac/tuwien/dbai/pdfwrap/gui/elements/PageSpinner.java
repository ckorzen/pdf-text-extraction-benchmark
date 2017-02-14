package at.ac.tuwien.dbai.pdfwrap.gui.elements;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;

/**
 * PageSpinner class for displaying a page spinner including one previous and one next button as well as
 * a JLabel showing the current and the maximal page number
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class PageSpinner extends JPanel {

	private JTextField displayLeft;
	private JLabel displayRight;
	
	private int currentPageNo;
	private int maxPageNo;
	
	private BasicArrowButton previous;
	private BasicArrowButton next;
	
	/**
	 * Constructor method for a PageSpinner with default value displayed "0 / 0"
	 */
	public PageSpinner() {
		
		//Default values of the spinner
		currentPageNo = 0;
		maxPageNo = 0;
		
		displayLeft = new JTextField(Integer.toString(currentPageNo));
		displayLeft.setBackground(null);
		displayLeft.setBorder(null);
		
		displayRight = new JLabel("/ " + maxPageNo);
		
		previous = new BasicArrowButton(BasicArrowButton.WEST);
		
		next = new BasicArrowButton(BasicArrowButton.EAST);
		
		//Arrange JPanel child components
		add(previous);
		
		add(Box.createRigidArea(new Dimension(7, 0)));
		
		add(displayLeft);
		add(displayRight);
		
		add(Box.createRigidArea(new Dimension(7, 0)));
		
		add(next);
	}
	
	/**
	 * Getter method for the left previous arrow button
	 * 
	 * @return The left previous arrow button
	 */
	public BasicArrowButton getPreviousButton() {
		
		return previous;
	}
	
	/**
	 * Getter method for the right next arrow button
	 * 
	 * @return The right next arrow button
	 */
	public BasicArrowButton getNextButton() {
		
		return next;
	}
	
	/**
	 * Getter method for the JTextField
	 * 
	 * @return the left JTextfield
	 */
	public JTextField getTextField() {
		
		return displayLeft;
	}
	
	/**
	 * Sets the current spinner value to the given value but takes care of the maximum page number
	 * 
	 * @param value the number of the side you want to access
	 * @return true if the increasing operation was successful, false otherwise
	 */
	public boolean change(int value) {
		
		if ((value) > maxPageNo || (value) <= 0) {
			return false;
		}
		
		currentPageNo = value;
		
		displayLeft.setText(Integer.toString(currentPageNo));
		
		return true;
	}
	
	/**
	 * Getter method for the current visible page number
	 * 
	 * @return the number of the currently displayed page
	 */
	public int getCurrentPage() {
		
		return currentPageNo;
	}
	
	/**
	 * Method for initializing a new spinner value starting at page one of max pages
	 * 
	 * @param max the maximum number of pages
	 */
	public void initNewSpinnerValues(int max) {
		
		maxPageNo = max;
		currentPageNo = 1;
		
		displayLeft.setText(Integer.toString(currentPageNo));
		displayRight.setText(" / " + maxPageNo);
	}
	
	@Override
	public Dimension getMaximumSize() {
		
		//Need to override because of height resize bug
		Dimension max = super.getMaximumSize();
        max.height = getPreferredSize().height;
        
        return max;
	}
}
