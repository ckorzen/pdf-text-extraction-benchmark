/* (C) 2001-2002, DIUF, http://www.unifr.ch/diuf
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

import iiuf.awt.BorderLayout;
import iiuf.swing.CheckBoxList;
import iiuf.swing.JWindowToolBarUI;
import iiuf.util.FilePreferences;
import iiuf.util.Preferences;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException; 

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XMIllumFrame
 *
 * JFrame implementation of XMIllum
 *
 * @author $Author: hassan $
 * @version $Revision: 1.1 $
 */
public class XMIllumFrame {

  /** Zoom preference */
  static final String XMIFRAME_ZOOM   = "xmillumframe.zoom";

  /** Layers preference */
  static final String XMIFRAME_LAYERS = "xmillumframe.layers";

  /** Browser context */
  BrowserContext context = new BrowserContext();

  /** List of layers */
  CheckBoxList layersList;

  /** List of status messages */
  MessageListModel statusList;

  BrowserPanel mainPanel;

  /**
   * Creates a new XMIllumFrame.
   *
   * @param sourceDocumentBaseURL Source document base URL.
   * @param stylesheetURL Stylesheet URL.
   */
  public XMIllumFrame(final URL sourceDocumentBaseURL, URL stylesheetURL) throws IOException, SAXException, ParserConfigurationException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource isource = new InputSource(sourceDocumentBaseURL.openStream());
    final Element sourceDocument = db.parse(isource).getDocumentElement();
    
    context.addStatusListener(new StatusListener() {
	public void setStatus(String message) {
	  if (statusList != null) {
	    statusList.addMessage(message);
	  }
	}
      });

    context.addDocumentChangeListener(new DocumentChangeListener() {
	public void documentChanged(DocumentChangeEvent e) {
	  switch (e.getType()) {
	  case DocumentChangeEvent.DOCUMENT_CHANGED:
	    String[] layers = context.getDocument().getLayerNames();
	    JCheckBox[] boxes = new JCheckBox[layers.length];
	    for (int i = 0; i < layers.length; i++) {
	      boxes[i] = new JCheckBox(layers[i]);
	      boxes[i].setSelected(true);
	      boxes[i].addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent e) {
		    JCheckBox cb = (JCheckBox) e.getSource();
		    context.toggleLayer(cb.getText(), cb.isSelected());
		  }
		});
	    }
	    layersList.setListData(boxes);
	    break;
	  }
	}
      });

    context.setWindowCreator(new WindowCreator() {
	public Window createWindow(String title) {
	  return new IWindow(title);
	}
      });

    layersList = new CheckBoxList();
    layersList.setVisibleRowCount(3);
    JScrollPane layersPane = new JScrollPane(layersList);
    layersPane.setBorder(BorderFactory.createTitledBorder("Layers"));

    statusList = new MessageListModel(20);

    JFrame w = new JFrame("XMIllum");
    w.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  context.finish();
	  context.clearLayer();
	  Preferences.store();
	  System.exit(0);
	}
      });

    mainPanel = new BrowserPanel(context);

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setLeftComponent(mainPanel);
    split.setRightComponent(new JScrollPane(new JList(statusList)));

    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

    JComboBox zoom = new JComboBox(new ZoomEntry[] {
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 8.0d,   "800%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 4.0d,   "400%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 2.0d,   "200%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 1.5d,   "150%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 1.25d,  "125%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 1.0d,   "100%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.5d,   "50%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.25d,  "25%"),
      new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.125d, "12.5%"),
      new ZoomEntry(BrowserPanel.SCALE_SMART, BrowserPanel.SMARTSCALE_FIT_WIDTH, "Fit Width"),
      new ZoomEntry(BrowserPanel.SCALE_SMART, BrowserPanel.SMARTSCALE_FIT_WINDOW, "Fit in Window")
    });
    zoom.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  int zoom = ((JComboBox) e.getSource()).getSelectedIndex();
	  Preferences.set(XMIFRAME_ZOOM, new Integer(zoom));
	  ZoomEntry ze = (ZoomEntry) ((JComboBox) e.getSource()).getItemAt(zoom);
	  mainPanel.setScale(ze.getType(), ze.getFactor());
	}
      });

    try {
      zoom.setSelectedIndex(((Integer) Preferences.get(XMIFRAME_ZOOM, new Integer(5))).intValue());
    } catch (IllegalArgumentException e) {
      zoom.setSelectedIndex(5);
    }

    zoom.setBorder(BorderFactory.createTitledBorder("Zoom"));
    // Required for Java 1.4.0 (?)
    zoom.setMaximumSize(zoom.getMinimumSize());

    leftPanel.add(zoom);
    leftPanel.add(layersPane);

    JSplitPane mainsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainsplit.setOneTouchExpandable(true);
    mainsplit.setLeftComponent(leftPanel);
    mainsplit.setRightComponent(split);

    w.getContentPane().setLayout(new BorderLayout());
    w.getContentPane().add(mainsplit, BorderLayout.CENTER);
    w.pack();
    Preferences.watch(w);
    w.setVisible(true);

    context.setSource(new IllumSource() {
	public Element getData() {
	  return sourceDocument;
	}
	public URL getBaseURL() {
	  return sourceDocumentBaseURL;
	}
      });

    context.loadStylesheet(stylesheetURL);
  }

  /**
   * ListModel for the status messages.
   */
  class MessageListModel extends AbstractListModel {

    /** List containing the messages */
    ArrayList l = new ArrayList();

    int bufferSize;

    /**
     * Creates a new message list.
     *
     * @param max Max. number of messages.
     */
    public MessageListModel(int max) {
      bufferSize = max;
    }

    /**
     * Adds a message to the list.
     *
     * @param message Message to be added to the list.
     */
    public void addMessage(String message) {
      l.add(message);
      if (l.size() > bufferSize) {
	l.remove(0);
      }
      fireContentsChanged(this, 0, l.size()-1);
    }

    /**
     * Returns the message at the given position.
     *
     * @param i Position
     * @return Message
     */
    public Object getElementAt(int i) {
      return l.get(l.size()-1-i);
    }

    /**
     * Returns the total number of messages.
     *
     * @return Number of messages
     */
    public int getSize() {
      return l.size();
    }
  }

  class ZoomEntry {
    String message;
    int    type;
    double factor;
    public ZoomEntry(int t, double f, String m) {
      type    = t;
      factor  = f;
      message = m;
    }
    public int getType() {
      return type;
    }
    public double getFactor() {
      return factor;
    }
    public String toString() {
      return message;
    }
  }
 
  /** Windows generated by the the WindowCreator */

  class IWindow extends JFrame implements Window {
    /**
     * Creates an IWindow
     *
     * @param title Window title.
     */
    public IWindow(String title) {
      super(title);
      setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	    setState(java.awt.Frame.ICONIFIED);
	  }
	});
    }

    /**
     * Sets a menu bar in the window.
     *
     * @param menubar The menubar to set.
     */
    public void setMenu(JMenuBar menubar) {
      setJMenuBar(menubar);
    }

    /**
     * Opens the window.
     */
    public void open() {
      pack();
      Preferences.watch(this);
      setState(java.awt.Frame.NORMAL);
      show();
    }

    /**
     * Closes the window.
     */
    public void close() {
      hide();
      dispose();
    }

    /**
     * Gets the content pane.
     *
     * @return Content pane for adding objects.
     */
    public Container getContentPane() {
      return super.getContentPane();
    }
  }

  /**
   * Main entry point.
   *
   * @param arg Command line arguments.  
   */
  public static void main(String arg[]) throws IOException, SAXException, MalformedURLException, ParserConfigurationException {
    if (arg.length != 2) {
      System.err.println("Usage: java iiuf.xmillum.XMIllumFrame <xml-file> <xsl-file>");
      System.exit(1);
    }

    Preferences.addStore(new FilePreferences("xmillumframe"));

    URL baseURL = (new File(arg[0])).toURL();
    URL stylesheetURL = (new File(arg[1])).toURL();

    new XMIllumFrame(baseURL, stylesheetURL);
  }
}
