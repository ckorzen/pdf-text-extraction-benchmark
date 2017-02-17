package at.ac.tuwien.dbai.pdfwrap.gui.elements;

import at.ac.tuwien.dbai.pdfwrap.gui.layer.StyledSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.Page;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for the panel which later displays the PDF image and other contents.
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class PDFPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {

	//The PDF converted into a BufferedImage and already scaled to fit window size
	private BufferedImage img;
	
	//A list containing all elements of the PDF analysis process
	private List<StyledSegment> segList;
	
	//Factors for scaling and visualizing the output of the analysis process on the window
	private float pageResizeFactor;
	
	//Factor for scaling the image to the window
	private float imageResizeFactor;
	
	//Current value of the zoom
	private float zoomValue;
	
	//Height of the image depending on the current factors
	private int imgH;
	
	//Determines whether the background image should be printed or not
	private boolean printImg;
	
	//The origin of the screen coordinate system
	private int originX, originY;
	
	//The previous coordinates for dragging of the whole screen content
	private int previousY, previousX;
	
	//The selection box rectangle
	private Rectangle2D selectionBox;
	
	//The attribute panel of the GUI that displays all currently selected segments
	private SelectionPanel attributePanel;
	
	//Factor for constant zooming
	private final float zoomFactor = 0.065f;
	
	//The underlying page
	private Page page;
	
	//The JLabel for displaying the current mouse position
	private JLabel mouseCoordinates;
	
	/**
	 * The constructor for the PDFPanel object.
	 * 
	 * @param dim The preferred size of this Panel
	 * @param img The PDF as a BufferedImage
	 * @param segList A list with all the elements of the PDF analysis process
	 * @param attributePanel A panel for displaying the attributes of the selected segments
	 * @param page The underlying page
	 * @param mouseCoordinates the JLabel for displaying the current mouse position
	 */
	public PDFPanel(Dimension dim, BufferedImage img, List<StyledSegment> segList,
			        SelectionPanel attributePanel, Page page, JLabel mouseCoordinates) {
		
		this.setSize(dim);
		this.setOpaque(false);
		
		if (img == null) {
			
			this.img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
			
		} else {
			
			this.img = img;
			
		}
		
		if (segList == null) {
			
			this.segList = new ArrayList<StyledSegment>();	
			
		} else {
			
			this.segList = segList;		
			
		}
		
		this.attributePanel = attributePanel;
		
		this.page = page;
		
		this.mouseCoordinates = mouseCoordinates;
		
		this.printImg = true;
		
		//Set default zoom factor
		this.zoomValue = 1.0f;
		
		updateResizeScaleFactor();
		
		//Adding all listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		//Translate coordinate system in order to perform a mouse drag
		g2.translate(originX, originY);
		
		//Draw the image PDF
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);	

		if (printImg) {
			
			g2.drawImage(img, 0, 0, (int)((float)this.getWidth()*zoomValue), imgH, null);		
		}
		
		for (StyledSegment styl : segList){
			
			styl.paintSegments(g2);
		}
		
		if (selectionBox != null) {
			
			g2.setColor(Color.BLACK);
			g2.draw(selectionBox);
			g2.setColor(new Color(0,0,255,63));
			g2.fill(selectionBox);
		}
		
		super.paint(g);
	}
	
	/**
	 * Getter method for the underlying page
	 * 
	 * @return The underlying page object.
	 */
	public Page getPage() {
		
		return page;
	}
	
	/**
	 * Determines whether the background image is visible or not.
	 * 
	 * @param isPrintable true if the background image should be printed, false otherwise
	 */
	public void setImgVisible(boolean isPrintable) {
		
		this.printImg = isPrintable;
	}
	
	/**
	 * Method for updating the window scaling factors
	 */
	public void updateResizeScaleFactor() {
		
		//Calculate the scaling factors
		imageResizeFactor = (float)this.getWidth()/(float)img.getWidth();
		pageResizeFactor = ((float)this.getWidth()*zoomValue/page.getWidth());
		imgH = (int)((float)img.getHeight()*imageResizeFactor*zoomValue);
		
		//Scale every segment according to the previously calculated page factor
		for (StyledSegment seg : segList) {
			
			seg.updateLocalCoordinates(pageResizeFactor, page.getHeight());
		}
		
		setPreferredSize(new Dimension(1, imgH));
	}
	
	/**
	 * Update the location of a {@link StyledSegment} after it has been modified by the user
	 * 
	 * @param seg the segment that has been modified
	 */
	public void updateResizeScaleFactor(StyledSegment seg) {
		
		seg.updateLocalCoordinates(pageResizeFactor, page.getHeight());
		repaint();
	}
	
	/**
	 * Fits everything according to either the current window width or the window height
	 * 
	 * @param fitWidth true if you want to fit the PDF to the window width, false otherwise (fit to window height)
	 */
	public void fitWindow(boolean fitWidth) {
		
		originX = 0;
		originY = 0;
		
		if (fitWidth) {
			
			zoomValue = 1.0f;
			
		} else {
			
			zoomValue = (float)getHeight()/((float)img.getHeight()*imageResizeFactor);
		}
		
		updateResizeScaleFactor();
		
		repaint();	
	}

	//Methods of the MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if (SwingUtilities.isRightMouseButton(e)) {
			
			JPopupMenu menu = new JPopupMenu();
			JMenuItem merge = new JMenuItem("Merge Segments");
			
			//Merging of two or more segments is done here
			merge.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					//Get all currently selected segments
					ArrayList<StyledSegment> selectedSegs = attributePanel.getSelectedSegments();
					
					//Check whether at least two segments are selected
					if (selectedSegs.size() < 2) {
						
						JOptionPane.showMessageDialog(PDFPanel.this, "Please select at least two segments.");
						return;
					}
					
					//Check whether the class of the selected objects match
					for (StyledSegment seg : selectedSegs) {
						
						if (!seg.isClassEqual(selectedSegs.get(0))) {
							
							JOptionPane.showMessageDialog(PDFPanel.this, "Please only select items of the same type.");
							return;
						}
					}
					
					//Remove all selected segment from the current page
					for (StyledSegment seg : selectedSegs) {
						
						page.getItems().remove(seg.getSegment());
					}
					
					//Choose the first selected segment to merge it with all the remaining ones
					StyledSegment growingSeg = selectedSegs.get(0);
					
					//Remove all selected segments from the display list
					segList.removeAll(selectedSegs);
					
					//Merge each segment into the first one - means that the first one grows
					for (int i = 1; i < selectedSegs.size(); i++) {
						
						growingSeg.getSegment().mergeSegment(selectedSegs.get(i).getSegment());
					}
					
					//Position the merged segment on the screen by considering the current window size
					growingSeg.updateLocalCoordinates(pageResizeFactor, page.getHeight());
					
					//Add the new segment to the current page
					page.getItems().add(growingSeg.getSegment());			
					
					//Add the new segment to the display list
					segList.add(growingSeg);
					
					//Update the attribute panel for getting the information about the new created segment instead of the old ones
					attributePanel.updateSegmentVisibility();
					
					repaint();
				}
			});
			
			menu.add(merge);
			
			menu.show(e.getComponent(), e.getX(), e.getY());
			
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { 
		
		mouseCoordinates.setText("0/0");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		previousX = e.getX();
    	previousY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		//Go into selection section if left mouse button was released
		if(SwingUtilities.isLeftMouseButton(e)) {

			ArrayList<StyledSegment> selected = new ArrayList<StyledSegment>();
			
			//Check which segments got selected
			for (StyledSegment seg : segList) {
				
				//Check whether the selection was caused by means of the selection box or by simple clicking
				boolean intersect = (selectionBox == null) ? seg.intersects((e.getX()-originX), (e.getY()-originY)) :
									seg.intersects(selectionBox);
					
				if (intersect) {
					
					seg.setSelected(true);
					selected.add(seg);
				
				} else {
				
					seg.setSelected(false);		
				}
			}
			
			//Check whether the control key is pressed and if so add or remove segments
			if (e.getModifiers() == KeyEvent.VK_ALT) {
				
				for (StyledSegment s : attributePanel.getSelectedSegments()) {
					
					if (selectionBox == null && selected.contains(s)) {

						//Remove segment if it was previously selected
						s.setSelected(false);
						selected.remove(s);
						
					} else if (!selected.contains(s)) {
						
						//Add segment if its a new previously not selected segment
						s.setSelected(true);
						selected.add(s);
					}
				}
			}
			
			//Display all selected segments within the attribute panel
			attributePanel.setSelectedElements(selected);
			
			//Reset Selection Box
			selectionBox = null;
			
			repaint();
		}
	}

	//Methods of the MouseMotionListener
	@Override
	public void mouseDragged(MouseEvent e) {

		//Move the coordinate system with respect to the dragging direction
		if (SwingUtilities.isMiddleMouseButton(e)) {
			

			originX -= (previousX - e.getX());
			originY -= (previousY - e.getY());
	    	
	    	previousX = e.getX();
	    	previousY = e.getY();
	    	
	    //Set the selection box for segment selection
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			
			selectionBox = new Rectangle2D.Float();			
			selectionBox.setFrameFromDiagonal(e.getX()-originX, e.getY()-originY, previousX-originX, previousY-originY);	
		}
		
    	repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
			
		Point pos = e.getPoint();
		
		int x = (int) ((pos.getX()-originX) / pageResizeFactor);
		int y = (int) (page.getHeight() - (pos.getY()-originY) / pageResizeFactor);
		
		mouseCoordinates.setText(x + " / " + y);
		
	}
	
	@Override
	public JToolTip createToolTip() {
		
		//Customizing tool tip for better visibility
		JToolTip tip = super.createToolTip();
		
		tip.setForeground(Color.BLACK);
		tip.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		return tip;
	}

	//Methods of the ComponentListener
	@Override
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void componentMoved(ComponentEvent e) { }

	@Override
	public void componentResized(ComponentEvent e) {
		
		updateResizeScaleFactor();
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) { }

	//Methods of the MouseWheelListener
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		int rotation = e.getWheelRotation();
		
		//Check mouse position before zoom
		float beforeX = (float)e.getX()*zoomValue;
		float beforeY = (float)e.getY()*zoomValue;
		
		if (rotation < 0) {

			//zoom in
			zoomValue *= Math.abs(rotation)*(1.0f+zoomFactor);
			
		} else {
			
			//zoom out
			zoomValue *= rotation*(1.0f-zoomFactor);
		}
		
		//Check mouse position after zoom
		float afterx = (float)e.getX()*zoomValue;
		float aftery = (float)e.getY()*zoomValue;
		
		//Mouse sensitive shift
		originX -= (afterx - beforeX);
		originY -= (aftery - beforeY);
		
		updateResizeScaleFactor();
		
		repaint();
	}
}
