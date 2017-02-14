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

import iiuf.dom.DOM;
import iiuf.xmillum.tool.XMLTree;
import iiuf.util.FilePreferences;
import iiuf.util.Preferences;
import iiuf.util.Queue;
import iiuf.util.Util;
import iiuf.awt.BorderLayout;
import iiuf.swing.JWindowToolBarUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.jai.PlanarImage;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XMIllumDesktop
 *
 * JDesktop implementation of xmillum.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class XMIllumDesktop implements StatusListener, DocumentChangeListener, IllumSource, WindowCreator {

  static final String XSLT_DIRECTORY = "xslt_directory";
  static final String XSLT_LASTUSED  = "xslt_lastused";
  static final String XML_DIRECTORY  = "xml_directory";

  JDesktopPane   desktop        = setupDesktop();
  BrowserContext context        = new BrowserContext();
  JLabel         status         = new JLabel();

  JComboBox      stylesheetList;
  JComboBox      layersList;

  public XMIllumDesktop() {
    context.addStatusListener(this);
    context.addDocumentChangeListener(this);
    context.setWindowCreator(this);

    setStatus("Welcome to XMIllum!");
        
    JPanel main = new JPanel();
    main.setLayout(new BorderLayout());
    main.add(new BrowserPanel(context), BorderLayout.CENTER);
    main.add(status, BorderLayout.SOUTH);

    Window w = createWindow("XMIllum");
    w.setMenu(setupMenubar());
    w.getContentPane().setLayout(new BorderLayout());
    w.getContentPane().add(setupToolbar(), BorderLayout.NORTH);
    w.getContentPane().add(main, BorderLayout.CENTER);
    w.open();
  }

  /** Sets up the desktop */

  public JDesktopPane setupDesktop() {
    JDesktopPane desktop = new JDesktopPane();
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    size.width = size.width - 80;
    size.height = size.height - 40;
    
    JFrame window = new JFrame("XMIllum");
    window.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  Preferences.store();
	  System.exit(0);
	}
      });
    window.setContentPane(desktop);
    window.pack();
    window.setSize(size);
    window.setLocation(0, 0);
    window.show();

    Preferences.watch(window);

    return desktop;
  }

  /** Sets up a toolbar */

  public JToolBar setupToolbar() {
    JToolBar toolbar = new JToolBar();

    toolbar.setUI(new JWindowToolBarUI());  

    toolbar.add(new JLabel("Stylesheet:"));
    
    stylesheetList = new JComboBox((String[]) Preferences.get(XSLT_LASTUSED, new String[0]));
    stylesheetList.setEditable(false);
    stylesheetList.setLightWeightPopupEnabled(true);
    stylesheetList.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  File file = new File((String) stylesheetList.getSelectedItem());
	  try {
	    context.loadStylesheet(file.toURL());
	  } catch (MalformedURLException ex) {
	    ex.printStackTrace();
	  }
	}
      });
    stylesheetList.setRenderer(new DefaultListCellRenderer() {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	  String filename = "";
	  try {
	    filename = (new File((String) value)).getName();
	  } catch (NullPointerException e) {
	  }
	  return super.getListCellRendererComponent(list, filename, index, isSelected, cellHasFocus);
	}
      });

    toolbar.add(stylesheetList);
   
    JButton stylesheetButton = new JButton("Load new");
    stylesheetButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  JFileChooser fc = new JFileChooser();
	  fc.setCurrentDirectory(new File((String) Preferences.get(XSLT_DIRECTORY, "")));
	  fc.addChoosableFileFilter(new FileFilter() {
	      public boolean accept(File f) {
		return f.isDirectory() || f.getName().endsWith(".xsl");
	      }
	      public String getDescription() {
		return ".xsl Stylesheets";
	      }
	    });
	  
	  if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)) {
	    Preferences.set(XSLT_DIRECTORY, fc.getCurrentDirectory().getAbsolutePath());
	    File file = fc.getSelectedFile();
	    
	    stylesheetList.insertItemAt(file.getAbsolutePath(), 0);
	    stylesheetList.setSelectedIndex(0);
	    
	    // Save combobox contents to preferences
	    ComboBoxModel model = stylesheetList.getModel();
	    int size = model.getSize();
	    String[] lastUsed = new String[(model.getSize() <= 10) ? model.getSize() : 10];
	    for (int i = 0; i < lastUsed.length; i++) {
	      lastUsed[i] = (String) model.getElementAt(i);
	    }
	    Preferences.set(XSLT_LASTUSED, lastUsed);
	  }
	}
      });

    toolbar.add(stylesheetButton);
    toolbar.addSeparator();
    toolbar.add(new JLabel("Layer:"));

    layersList = new JComboBox();
    layersList.setEditable(false);
    layersList.setLightWeightPopupEnabled(true);
    layersList.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
// 	  context.displayLayer((String) layersList.getSelectedItem());
	}
      });

    toolbar.add(layersList);

    return toolbar;
  }

  /** Sets up a menubar */

  public JMenuBar setupMenubar() {
    JMenuBar menubar = new JMenuBar();
    
    JMenu menu;
    JMenuItem menuItem;
    
    menu = menubar.add(new JMenu("File"));
    menu.setMnemonic(KeyEvent.VK_F);

    // File / Open

    menuItem = menu.add(new AbstractAction("Open File...") {
	public void actionPerformed(ActionEvent e) {
	  JFileChooser fc = new JFileChooser();
 	  fc.setCurrentDirectory(new File((String) Preferences.get(XML_DIRECTORY, "")));
	  fc.addChoosableFileFilter(new FileFilter() {
	      public boolean accept(File f) {
		return f.isDirectory() || f.getName().endsWith(".xml");
	      }
	      public String getDescription() {
		return ".xml Files";
	      }
	    });
	  
	  if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(desktop)) {
	    Preferences.set(XML_DIRECTORY, fc.getCurrentDirectory().getAbsolutePath());
	    
	    try {
	      sourceDocument = fc.getSelectedFile().toURL();
	      context.setSource(XMIllumDesktop.this);
	    } catch (MalformedURLException ex) {
	      ex.printStackTrace();
	    }
	  }
	}
      });
    menuItem.setMnemonic(KeyEvent.VK_O);
    
    menuItem = menu.add(new AbstractAction("Open Location...") {
	public void actionPerformed(ActionEvent e) {
	}
      });
    
    // File / Close
    
    menuItem = menu.add(new AbstractAction("Close") {
	public void actionPerformed(ActionEvent e) {
	  sourceDocument = null;
	  context.setSource(XMIllumDesktop.this);
	}
      });
    menuItem.setMnemonic(KeyEvent.VK_C);
    
    menu.addSeparator();

    // File / Save

    menuItem = menu.add(new AbstractAction("Save") {
	public void actionPerformed(ActionEvent e) {
// 	  System.out.println(iiuf.dom.DOMUtils.toString(context.document.getStrippedOriginalDocument().getDocumentElement()));
	}
      });
    menuItem.setMnemonic(KeyEvent.VK_S);

    // File / Save As...

    menuItem = menu.add(new AbstractAction("Save As...") {
	public void actionPerformed(ActionEvent e) {
	  JFileChooser fc = new JFileChooser();
	  fc.setCurrentDirectory(new File((String) Preferences.get(XML_DIRECTORY, "")));
	  fc.addChoosableFileFilter(new FileFilter() {
	      public boolean accept(File f) {
		return f.isDirectory() || f.getName().endsWith(".xml");
	      }
	      public String getDescription() {
		return ".xml Files";
	      }
	    });
	  
	  if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(desktop)) {
	    Preferences.set(XML_DIRECTORY, fc.getCurrentDirectory().getAbsolutePath());
	    // ...Save the file...
	  }
	}
      });
    menuItem.setMnemonic(KeyEvent.VK_A);

    menu.addSeparator();

    // File / Print...

    menuItem = menu.add(new AbstractAction("Print...") {
	public void actionPerformed(ActionEvent e) {
	}
      });
    menuItem.setMnemonic(KeyEvent.VK_P);

    menu.addSeparator();

    // File / Exit

    menuItem = menu.add(new AbstractAction("Exit") {
	public void actionPerformed(ActionEvent e) {
	  Preferences.store();
	  System.exit(0);
	}
      });
    menuItem.setMnemonic(KeyEvent.VK_X);

    // Display

    menu = menubar.add(new JMenu("Display"));

    // Display / 100%

    menuItem = menu.add(new AbstractAction("100%") {
	public void actionPerformed(ActionEvent e) { context.setScale(1.0D); }
      });
    menuItem.setMnemonic(KeyEvent.VK_1);

    // Display / 50%

    menuItem = menu.add(new AbstractAction("50%") {
	public void actionPerformed(ActionEvent e) { context.setScale(0.5D); }
      });
    menuItem.setMnemonic(KeyEvent.VK_2);

    // Display / 25%

    menuItem = menu.add(new AbstractAction("25%") { 
	public void actionPerformed(ActionEvent e) { context.setScale(0.25D); }
      });
    menuItem.setMnemonic(KeyEvent.VK_3);

    // Display / 10%

    menuItem = menu.add(new AbstractAction("10%") {
	public void actionPerformed(ActionEvent e) { context.setScale(0.1D); }
      });
    menuItem.setMnemonic(KeyEvent.VK_4);

    // Windows

    menu = menubar.add(new JMenu("Windows"));

    // Windows / Original XML

    menuItem = menu.add(new AbstractAction("Original XML") {
	public void actionPerformed(ActionEvent e) {
	  XMLTree panel = new XMLTree() {
	      protected Element selectionElementToTree(Element e) {
		if (!e.hasAttribute("ref")) return null;
		return this.context.getSourceElementByReference(e.getAttribute("ref"));
	      }
	      protected Element treeToSelectionElement(Element e) {
		NodeList nl = this.context.getInternalElementsWhichReference(e.getAttributeNS("tmp", "refvalue"));
		if (nl.getLength() > 0) {
		  return (Element) nl.item(0);
		} else {
		  return null;
		}
	      }
	    };
	  panel.activateTool(context, context.getDocument().getSourceDocument());
	}
      });

    // Windows / Transformed XML

    menuItem = menu.add(new AbstractAction("Transformed XML") {
	public void actionPerformed(ActionEvent e) {
	  XMLTree panel = new XMLTree();
// 	  panel.activateTool(context, context.getDocument().getBrowserSection());
	}
      });

    // Windows / Stylesheet

    menuItem = menu.add(new AbstractAction("Stylesheet") {
	public void actionPerformed(ActionEvent e) {
	  XMLTree panel = new XMLTree();
// 	  panel.activateTool(context, context.getDocument().getStylesheetDocument().getDocumentElement());
	}
      });

    return menubar;
  }

  // StatusListener interface

  public void setStatus(String message) {
    status.setText(message);
  }

  // DocumentChangeListener interface

  public void documentChanged(DocumentChangeEvent e) {
    switch (e.getType()) {
    case DocumentChangeEvent.DOCUMENT_CHANGED:
      String[] layers = context.getDocument().getLayerNames();
      layersList.removeAllItems();
      for (int i = 0; i < layers.length; i++) {
	layersList.insertItemAt(layers[i], i);
      }
      if (layers.length > 0) layersList.setSelectedIndex(0);
      break;
    }
  }

  // IllumSource interface

  private URL sourceDocument;

  public Element getData() throws IllumException {
    if (sourceDocument == null) return null;

    try {
      DOM dom = DOM.getInstance();
      InputSource isource  = new InputSource(sourceDocument.openStream());
      return dom.parseDocument(isource, false).getDocumentElement();
    } catch (IOException e) {
      throw new IllumException(e);
    } catch (SAXException e) {
      throw new IllumException(e);
    } 
  }

  public URL getBaseURL() {
    return sourceDocument;
  }

  public PlanarImage getImage(String id, double scale) {
    return null;
  }

  // WindowCreator interface

  public Window createWindow(String title) {
    return new IWindow(title);
  }

  class IWindow extends JInternalFrame implements Window {
    public IWindow(String title) {
      super("title", true, false, true, true);
    }
    public void setMenu(JMenuBar menubar) {
      setJMenuBar(menubar);
    }
    public void open() {
      pack();
      show();
      desktop.add(this);
    }
    public void close() {
      setVisible(false);
      dispose();
    }
    public Container getContentPane() {
      return super.getContentPane();
    }
  }

  /** Main entry point */

  public static void main(String arg[]) {
    System.setProperty(DOM.DEFAULT_DOM_PROPERTY, "iiuf.dom.Xerces");

    TileCache cache = JAI.createTileCache(64000000);
    cache.setMemoryThreshold(0.75F);
    JAI.getDefaultInstance().setTileCache(cache);

    Preferences.addStore(new FilePreferences("xmillum"));

    new XMIllumDesktop();
  }
}
