/* (C) 2000-2002, DIUF, http://www.unifr.ch/diuf
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package iiuf.xmillum;

import iiuf.dom.DOMUtils;
import iiuf.xmillum.displayable.Root;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;

/**
 * BrowserPanel
 * 
 * XMIllum browser panel.  Based on XMIllum code.
 * 
 * @author Tamir Hassan, hassan@dbai.tuwien.ac.at
 * @author DIUF, Fribourg, CH
 * @version GraphWrap Beta 1
 */
public class BrowserPanel extends JScrollPane implements MouseListener,
	MouseMotionListener, AllFlagListener, DocumentChangeListener
{
	// JPanel content;
	browserContentPanel content;

	BrowserContext context;

	GenericSegment selectionBBox;

	MouseEvent downPosition = null;

	boolean automaticRescale = true;

	public static final int SCALE_IMMEDIATE = 0;

	public static final int SCALE_SMART = 1;

	public static final double SMARTSCALE_FIT_WIDTH = 0.0d;

	public static final double SMARTSCALE_FIT_WINDOW = 1.0d;

	static RenderingHints hints;

	/**
	 * Set rendering hints to speed up the rendering.
	 */
	static
	{
		hints = new RenderingHints(null);
		hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
			RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_SPEED);
		hints.put(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
			RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		hints.put(RenderingHints.KEY_COLOR_RENDERING,
			RenderingHints.VALUE_COLOR_RENDER_SPEED);
		hints.put(RenderingHints.KEY_DITHERING,
			RenderingHints.VALUE_DITHER_DISABLE);
	}

	/**
	 * Constructs a new browser panel given a context.
	 * 
	 * @param c
	 *            The given browser context
	 */
	public BrowserPanel(BrowserContext c)
	{
		context = c;
		context.addDocumentChangeListener(this);
		context.browserPanel = this;

		/*
		 * content = new JPanel() { public GenericSegment selectionBBox = null;
		 * public void paint(Graphics g) { Graphics2D g2d = (Graphics2D) g;
		 * g2d.setRenderingHints(hints); // Clear background. Can be removed to
		 * speed up the displaying. Rectangle r = g2d.getClipBounds();
		 * g2d.clearRect(r.x, r.y, r.width, r.height);
		 * 
		 * if (selectionBBox != null) g2d.drawRect((int)selectionBBox.getX1(),
		 * (int)selectionBBox.getY1(), (int)selectionBBox.getWidth(),
		 * (int)selectionBBox.getHeight()); if (!disabled) { for (int i = 0; i <
		 * layers.length; i++) { if (layers[i].isActive()) {
		 * layers[i].getDisplayable().paint(g2d, getScale()); } } } } };
		 */
		// content = new browserContentPanel();
		// using true doesn't really do much (we've implemented our own
		// buffering anyway) other than slow everything else down!
		content = new browserContentPanel(false);
		setViewportView(content);

		addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				if (automaticRescale && scaleType == SCALE_SMART)
				{
					rescale();
				}
			}
		});

		content.setLayout(null);
		content.addMouseListener(this);
		content.addMouseMotionListener(this);

		requestFocus();

		context.flagger.addAllFlagListener(this);
	}

	int scaleType = SCALE_IMMEDIATE;

	double scaleValue = 1.0;

	public void setScale(double scale)
	{
		setScale(SCALE_IMMEDIATE, scale);
	}

	public void setScale(int type, double scale)
	{
		scaleType = type;
		scaleValue = scale;
		rescale();
	}

	/**
	 * Enables or disables the automatic rescale.
	 * 
	 * @param autoRescale
	 *            Automatic rescale true/false.
	 */
	public void setAutomaticRescale(boolean autoRescale)
	{
		automaticRescale = autoRescale;
	}

	/**
	 * Overridden method.
	 * 
	 * @return Always true
	 */
	public boolean isFocusTraversable()
	{
		return true;
	}

	/**
	 * Overridden method.
	 * 
	 * @return Always true
	 */
	public boolean requestDefaultFocus()
	{
		return true;
	}

	boolean disabled;

	List tools;

	void closeTools()
	{
		if (tools != null)
		{
			Iterator i = tools.iterator();
			while (i.hasNext())
			{
				Tool t = (Tool) i.next();
				try
				{
					t.deactivateTool();
				} catch (Error e)
				{
					e.printStackTrace();
				}
			}
			tools = null;
		}
	}

	void openTools()
	{
		NodeList toolElements = context.getDocument().getTools();
		if (toolElements != null)
		{
			tools = new LinkedList();
			for (int i = 0; i < toolElements.getLength(); i++)
			{
				Element element = (Element) toolElements.item(i);
				Tool t = ToolFactory.getTool(element, context.getDocument()
					.getClassLoader());
				if (t != null)
				{
					try
					{
						t.activateTool(context, element);
					} catch (Error e)
					{
						e.printStackTrace();
					}
					tools.add(t);
				}
			}
		}
	}

	/**
	 * Centers the view on a given Displayable.
	 * 
	 * @param d
	 *            Displayable upon which to center the view.
	 */
	public void centerOnElement(Displayable d)
	{
		Rectangle view = getViewport().getViewRect();
		Rectangle r = d.getBounds(getScale());

		if (!view.contains(r))
		{
			Dimension size = getViewport().getPreferredSize();
			int x = view.x;
			int y = view.y;

			if (r.width < view.width)
			{
				// Element fits on screen
				if (r.x < view.x || r.x + r.width >= view.x + view.width)
				{
					x = r.x + (r.width - view.width) / 2;
					x = Math.min(Math.max(x, 0), size.width - view.width);
				}
			} else
			{
				// Element doesn't fit on screen
				if (r.x + r.width < view.x || r.x >= view.x + view.width)
				{
					x = r.x + (r.width - view.width) / 2;
					x = Math.min(Math.max(x, 0), size.width - view.width);
				}
			}

			if (r.height < view.height)
			{
				// Element fits on screen
				if (r.y < view.y || r.y + r.height >= view.y + view.height)
				{
					y = r.y + (r.height - view.height) / 2;
					y = Math.min(Math.max(y, 0), size.height - view.height);
				}
			} else
			{
				// Element doesn't fit on screen
				if (r.y + r.height < view.y || r.y >= view.y + view.height)
				{
					y = r.y + (r.height - view.height) / 2;
					y = Math.min(Math.max(y, 0), size.height - view.height);
				}
			}

			getViewport().setViewPosition(new Point(x, y));
		}
	}

	/**
	 * Show a popup menu.
	 * 
	 * @param menu
	 *            Menu to pop up
	 */
	public void showPopup(JPopupMenu menu)
	{
		Point p = context.getMousePosition();
		menu.show(content, p.x, p.y);
	}

	/**
	 * Repaints a rectangular area on the browser panel.
	 * 
	 * @param area
	 *            Rectangular area to repaint.
	 */
	public void repaintArea(Rectangle area)
	{
		getViewport().getView()
			.repaint(area.x, area.y, area.width, area.height);
	}

	// ////////////////scrollable methods
	// taken from
	// http://groups.google.com/group/comp.lang.java.gui/browse_thread/thread/aeccd24205adc949/523f3973093e263f?lnk=st&q=java+mouse+wheel+speed&rnum=1&hl=en#523f3973093e263f
	// however, they don't help
	// even with Scrollable interface implemented...
	// TODO: look into this further...

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
		int orientation, int direction)
	{
		return 30; // I don't know when this is called.

	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
		int orientation, int direction)
	{
		return 30; // this is called. It means 30 pixel, i guess.

	}

	// MouseListener...
	// layer stuff commented out 26.04.06 as it was causing performance problems
	// with the graph...

	/**
	 * Called whenever the mouse enters the region. We request the focus so the
	 * user can move using the keyboard.
	 * 
	 * @param event
	 *            Mouse event
	 */
	public void mouseEntered(MouseEvent event)
	{
		requestFocus();
	}

	/**
	 * Not used.
	 * 
	 * @param event
	 *            Mouse event
	 */
	public void mouseExited(MouseEvent event)
	{
	}

	/**
	 * Mouse clicked. Pass the event to the root displayable of every active
	 * layer.
	 * 
	 * @param event
	 *            Mouse event
	 */
	public void mouseClicked(MouseEvent event)
	{
		requestFocus();
		
		// uncommented 12.11.06
		context.setMousePosition(event);

		// this chunk uncommented 12.11.06
		for (int i = layers.length - 1; i >= 0; i--)
		{
			if (layers[i].isActive())
			{
				// if (layers[i].getDisplayable().mouseClicked(event, getScale())) return;
				if (layers[i].getDisplayable().mouseClicked(event, getScale()))
				{
					//System.out.println();
					return;
				}

			}
		}
		
	}

	/**
	 * Mouse pressed. Pass the event to the root displayable of every active
	 * layers.
	 * 
	 * @param event
	 *            Mouse event
	 */
	public void mousePressed(MouseEvent event)
	{
		requestFocus();
		if (downPosition == null)
			downPosition = event;

		// context.setMousePosition(event);
		// we will try a different techinque instead :)
		
		for (int i = layers.length - 1; i >= 0; i--)
		{
			if (layers[i].isActive())
			{
				// if (layers[i].getDisplayable().mousePressed(event,
				// getScale())) return;
				if (layers[i].getDisplayable().mousePressed(event, getScale()))
				{
					//System.out.println("mouse pressed on object...");
					//System.out.println("i = " + i);
					//System.out.println("mouse button: " + event.getButton());
					//System.out.println("source: " + event.getSource());
					//System.out.println("iD: " + event.getID());
					//System.out.println(event.)
				}
			}
		}
		
	}

	public void mouseReleased(MouseEvent event)
	{
		requestFocus();
		
		Point p = context.getMousePosition();
		Point distance = new Point(event.getX() - p.x, event.getY() - p.y);
		
		/*
		System.out.println("distance.x " + distance.x);
		System.out.println("distance.y " + distance.y);
		*/
		
		if (distance.x > 8 || distance.x < -8 ||
			distance.y < -8 || distance.y > 8)
		{
			if (downPosition != null)
			{
				/*
				 * context.setStatus("dragged from: " + (downPosition.getX() /
				 * getScale()) + ", " + (downPosition.getY() / getScale()) + " to: " +
				 * (event.getX() / getScale()) + ", " + (event.getY() /
				 * getScale()));
				 */
	
				context.setMousePosition(downPosition);
				
				context.setDragDistance(distance);
				// 11.12.08 don't select when scrolling with middle mouse button
				if (event.getButton() == 1)
					context.setStatus("Selection made");
			}
		}
		else
		{
			// just a press
			// todo... which button?
			context.setMousePosition(event);
			// 11.12.08 don't select when scrolling with middle mouse button
			if (event.getButton() == 1)
				context.setStatus("Node selected");
		}
		downPosition = null;

		
		for (int i = layers.length - 1; i >= 0; i--)
		{
			if (layers[i].isActive())
			{
				if (layers[i].getDisplayable().mouseReleased(event, getScale()))
					return;
			}
		}
		
	}

	// MouseMotionListener

	public void mouseMoved(MouseEvent event)
	{
		context.setMousePosition(event);

		// System.out.println("mouse moved");

		// layer updating for mouseMotionListener disabled...
		/*
		 * for (int i = layers.length-1; i >= 0; i--) { if
		 * (layers[i].isActive()) { if
		 * (layers[i].getDisplayable().mouseMoved(event, getScale())) return; } }
		 */
	}

	public void mouseDragged(MouseEvent event)
	{
		if (event.getModifiers() != 16) // second button = 8; first button = 16
		{
			// the following is exactly as it was in the original method
			Point p = context.getMousePosition();
		    Point distance = new Point(event.getX()-p.x, event.getY()-p.y);
		    
		    context.setMousePosition(event);
		    context.setDragDistance(distance);
	
		    for (int i = layers.length-1; i >= 0; i--) {
		      if (layers[i].isActive()) {
			if (layers[i].getDisplayable().mouseDragged(event, getScale())) return;
		      }
		    }
		
		}
		//if (event.getModifiers() == 16) // first button
		else
		{	
		
			/*
			 * context.setStatus("mouse dragged:" + " (" + event.getX() + "," +
			 * event.getY() + ")" + " detected on "* +
			 * event.getComponent().getClass().getName() + "\n");
			 * 
			 * context.setStatus("mousePosition: " + context.getMousePosition().x + ", " +
			 * context.getMousePosition().y);
			 */
	
			Point p = context.getMousePosition();
			// Point distance = new Point(event.getX()-p.x, event.getY()-p.y);
			// Point p2 = downPosition.getPoint();
			Point p2 = event.getPoint();
			float x1 = p.x;
			float y1 = p.y;
			// float x2 = x1 + distance.x; float y2 = x1 + distance.y;
			float x2 = p2.x;
			float y2 = p2.y;
	
			GenericSegment oldSelectionBBox = selectionBBox;
	
			selectionBBox = new GenericSegment(x1, x2, y1, y2);
			selectionBBox.correctNegativeDimensions();
			content.setSelectionBBox(selectionBBox);
			// System.out.println("in mouse dragged " + selectionBBox);
	
			content.setDrawOnlySelectionBox(true);
	
			if (oldSelectionBBox != null)
			{
				// work out co-ordinates to repaint
				CompositeSegment containerBox = new CompositeSegment();
				containerBox.getItems().add(oldSelectionBBox);
				containerBox.getItems().add(selectionBBox);
				containerBox.findBoundingBox();
				containerBox.enlargeCoordinates(1.0f);
	
				content.repaint(containerBox.getBoundingRectangle());
			} else
			{
				content.repaint(selectionBBox.getBoundingRectangle());
			}

		
		}
		// content.setDrawOnlySelectionBox(false);

		/*
		 * context.setStatus("distance is now: x " + (event.getX()-p.x) + " y " +
		 * (event.getY()-p.y));
		 * 
		 * context.setMousePosition(event); context.setDragDistance(distance);
		 */

		// layer updating for mouseDragged disabled...
		/*
		 * for (int i = layers.length-1; i >= 0; i--) { if
		 * (layers[i].isActive()) { if
		 * (layers[i].getDisplayable().mouseDragged(event, getScale())) return;
		 *  } }
		 */
	}

	// AllFlagListener

	public void setFlag(Element e, String name, String value)
	{
		for (int i = 0; i < layers.length; i++)
		{
			Displayable d = layers[i].getDisplayable()
				.getDisplayableForElement(e);
			if (d != null)
			{
				if (value != null)
				{
					centerOnElement(d);
				}
				repaint();
			}
		}
	}

	public double getScale()
	{
		if (scaleType == SCALE_IMMEDIATE)
		{
			return scaleValue;
		} else
		{
			Rectangle size = new Rectangle();
			for (int i = 0; i < layers.length; i++)
			{
				size = size.union(layers[i].getDisplayable().getBounds(1.0d));
			}

			Rectangle vp = getViewportBorderBounds();

			if (size.getWidth() == 0 || size.getHeight() == 0
				|| vp.getWidth() <= 0 || vp.getHeight() <= 0)
			{
				return 1.0d;
			}

			double wf = (double) getViewportBorderBounds().getWidth()
				/ size.getWidth();
			double hf = (double) getViewportBorderBounds().getHeight()
				/ size.getHeight();
			if (scaleValue == SMARTSCALE_FIT_WIDTH)
			{
				return wf;
			} else
			{
				return Math.min(wf, hf);
			}
		}
	}

	void rescale()
	{
		double scale = getScale();

		disabled = true;

		Rectangle size = new Rectangle();
		for (int i = 0; i < layers.length; i++)
		{
			size = size.union(layers[i].getDisplayable().getBounds(scale));
		}

		content.setPreferredSize(size.getSize());
		setViewportView(content);

		disabled = false;
		// repaint();
		context.refresh();
	}

	// DocumentChangeListener interface

	public void documentChanged(DocumentChangeEvent e)
	{
		switch (e.getType())
		{
		case DocumentChangeEvent.SCALE_CHANGED:
			rescale();
			break;
		case DocumentChangeEvent.LAYER_TOGGLED:
		{
			disabled = true;
			String layer = e.getLayer();

			for (int i = 0; i < layers.length; i++)
			{
				if (layers[i].getName().equals(layer))
				{
					layers[i].setActive(e.isActive());
				}
			}
			disabled = false;
			repaint();
		}
			break;
		case DocumentChangeEvent.REFRESH:
			repaint();
			break;
		case DocumentChangeEvent.DOCUMENT_CHANGED:
		{
			Point pos = getViewport().getViewPosition();

			closeTools();

			String[] l = context.getDocument().getLayerNames();
			LayerEntry[] ls = new LayerEntry[l.length];

			for (int i = 0; i < l.length; i++)
			{
				Element browserSection = context.getDocument().getLayer(l[i]);
				DisplayableClass root = new Root();
				root.initialize(context, browserSection);
				ls[i] = new LayerEntry(l[i], root
					.getDisplayable(browserSection));
			}
			layers = ls;
			openTools();

			rescale();
		}
			break;
		}
	}

	/*
	
	public void highlightRegion(GenericSegment bBox)
	{
		for (int i = 0; i < layers.length; i ++)
		{
			if (layers[i].isActive())
			{
				Displayable d = layers[i].getDisplayable();
				Displayable[] c = d.childs;
				for (int j = 0; j < c.length; j ++)
				{
					if (c[j] instanceof TextArea2)
					{
						//foo
					}
				}
			}
		}
	}
	
	*/
	
	LayerEntry[] layers = new LayerEntry[0];

	private class LayerEntry
	{
		String name;

		boolean active = true;

		Displayable display;

		public LayerEntry(String n, Displayable d)
		{
			name = n;
			display = d;
		}

		public String getName()
		{
			return name;
		}

		public boolean isActive()
		{
			return active;
		}

		public void setActive(boolean a)
		{
			active = a;
		}

		public Displayable getDisplayable()
		{
			return display;
		}
	}

	private class browserContentPanel extends JPanel
	{
		private BufferedImage theBuf;

		private GenericSegment selectionBBox = null;

		private boolean drawOnlySelectionBox;

		public browserContentPanel(boolean doubleBuffered)
		{
			super(doubleBuffered);
			BufferedImage theBuf;
			if (this.getWidth() > 0 && this.getHeight() > 0)
			{
				theBuf = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			} else
			{
				theBuf = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			}
			/*
			 * Graphics2D g = theBuf.createGraphics(); this.paint(g);
			 * g.dispose();
			 */
			drawOnlySelectionBox = false;
		}

		public void setSize(Dimension d)
		{
			super.setSize(d);

			if (this.getWidth() > 0 && this.getHeight() > 0)
			{
				theBuf = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_RGB);
				theBuf.createGraphics();
			} else
			{
				theBuf = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
				theBuf.createGraphics();
			}
		}

		public void resize(Dimension d)
		{
			super.resize(d);

			if (this.getWidth() > 0 && this.getHeight() > 0)
			{
				theBuf = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_RGB);
				theBuf.createGraphics();
			} else
			{
				theBuf = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
				theBuf.createGraphics();
			}
		}

		public void paint(Graphics g)
		{

			/*
			 * Calendar c = new GregorianCalendar(); System.out.println("in
			 * paint method " + c.getTime());
			 */

			Graphics2D g2d = (Graphics2D) g;

			// System.out.println("drawOnlySelectionBox: " +
			// drawOnlySelectionBox);

			if (!drawOnlySelectionBox)
			{

				// System.out.println("drawing everything...");
				g2d.setRenderingHints(hints);

				Graphics2D g2dbuf = (Graphics2D) theBuf.getGraphics();
				g2dbuf.setRenderingHints(hints);

				g2dbuf.setClip(g2d.getClipBounds());

				// Clear background. Can be removed to speed up the displaying.
				/*
				 * Rectangle r = g2dbuf.getClipBounds(); g2dbuf.clearRect(r.x,
				 * r.y, r.width, r.height);
				 */
				Rectangle r = g2dbuf.getClipBounds();
				g2dbuf.setColor(Color.WHITE);
				g2dbuf.fillRect(r.x, r.y, r.width, r.height);
				if (!disabled)
				{
					for (int i = 0; i < layers.length; i++)
					{
						if (layers[i].isActive())
						{
							layers[i].getDisplayable()
								.paint(g2dbuf, getScale());
						}
					}
				}

			}

			g2d.drawImage(theBuf, null, 0, 0);

			paintSelectionBox(g2d);

			// drawOnlySelectionBox = false;

		}

		public void paintSelectionBox(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			// System.out.println("selectionBBox: in bcp " + selectionBBox);
			if (selectionBBox != null)
			{
				// Rectangle rect2 = new Rectangle(100,100,200,200);
				// System.out.println("fofo");
				g2d.setPaint(Color.gray);
				g2d.setStroke(new BasicStroke());
				Rectangle theRect = new Rectangle((int) selectionBBox.getX1(),
					(int) selectionBBox.getY1(),
					(int) selectionBBox.getWidth(), (int) selectionBBox
						.getHeight());

				// System.out.println(selectionBBox);

				// g2d.fill(theRect);
				g2d.draw(theRect);
				// g2d.fill(rect2);
				// g2d.draw(rect2);

				// repaint();
				drawOnlySelectionBox = false;
				// theBuf = this.

			}
		}

		public GenericSegment getSelectionBBox()
		{
			return selectionBBox;
		}

		public void setSelectionBBox(GenericSegment selectionBBox)
		{
			this.selectionBBox = selectionBBox;
		}

		public void setDrawOnlySelectionBox(boolean drawOnlySelectionBox)
		{
			this.drawOnlySelectionBox = drawOnlySelectionBox;
		}

		public boolean isDrawOnlySelectionBox()
		{
			return drawOnlySelectionBox;
		}
	}
}
