package at.ac.tuwien.dbai.pdfwrap.gui;

import iiuf.awt.BorderLayout;
import iiuf.swing.CheckBoxList;
import iiuf.util.FilePreferences;
import iiuf.util.Preferences;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.BrowserPanel;
import iiuf.xmillum.DocumentChangeEvent;
import iiuf.xmillum.DocumentChangeListener;
import iiuf.xmillum.IllumSource;
import iiuf.xmillum.StatusListener;
import iiuf.xmillum.Window;
import iiuf.xmillum.WindowCreator;
import iiuf.xmillum.XMIllumFrame;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.ac.tuwien.dbai.pdfwrap.GraphMatcher;
import at.ac.tuwien.dbai.pdfwrap.ProcessFile;
import at.ac.tuwien.dbai.pdfwrap.analysis.PageProcessor;
import at.ac.tuwien.dbai.pdfwrap.gui.graphbrowser.DocGBPanel;
import at.ac.tuwien.dbai.pdfwrap.gui.graphbrowser.DocWrapperUI;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyGraph;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocEdge;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocGraphEltSet;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocNode;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocumentGraph;
import at.ac.tuwien.dbai.pdfwrap.model.graph.WrappingInstance;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;
import at.ac.tuwien.dbai.pdfwrap.utils.ListUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.SegmentUtils;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

//import com.hp.hpl.jena.ontology.OntModel;
import com.touchgraph.graphlayout.Node;

/**
 * GUI
 * 
 * Based upon XMIllumFrame.java, which was developed by
 * Department of Informatics at the University of Fribourg, Switzerland (DIUF)
 * and published under the LGPL(?) licence
 * 
 * @author DIUF, Fribourg, CH
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser GUI 0.9
 * 
 */
public class GUI
{
	protected final static String newline = "\n";
	
//	public final static boolean DISPLAY_INSTRUCTIONS = false;
	
	public final static int DEFAULT_SEGMENTATION_MODE = PageProcessor.PP_MERGED_LINES;
	
//	public final static float dividerRatio = 0.9f;
	public final static int dividerLocation = 20;
	public final static boolean horizToolbar = false;
	public final static boolean showWrapperPanel = true;
	public final static boolean standardLookAndFeel = false;
	
	// 30.11.06
	// resolution stuff, used mainly for GUI, so I put the constants here
	public final static float PDF_POINT_RESOLUTION = 72;
	public final static float XML_RESOLUTION = 300;
	public final static float STR_RESOLUTION = 96;

	public final static boolean gsBefore = false; // run gs before processing
	public static String GHOSTSCRIPT_EXECUTABLE = "gs";
	public static String COPY_EXECUTABLE = "cp";
	public static String STR_IMAGE_PREFIX = "";
	// /usr/bin/ necessary for wega
	//public static String GHOSTSCRIPT_EXECUTABLE = "/usr/bin/gs";
	// laptop
	//public static String GHOSTSCRIPT_EXECUTABLE =
	//	"c:\\gs\\gs8.14\\bin\\gswin32c.exe";
	
	// laptop
	public static String BROWSER_EXECUTABLE = "firefox";
		//"c:\\progra~1\\mozill~1\\firefox.exe";
	
	protected int pageNo = 1;
	protected int iterNo = 0;
	
	protected final static String MSG_CONNECTED = "The current graph is not connected." + newline +
		"Please ensure that there are paths between all activated nodes in the " +
		"graph before saving or executing the wrapper.";
	
	protected final static String MSG_RELOAD = "The document must be reloaded in order for " +
		"the changes to take effect.";

	protected final static String MSG_EMPTY = "Wrapper graph is empty or " +
		"no instance has been selected";
	
	protected final static String MSG_NODOC = "No document has been loaded.";
	
	protected final static String MSG_SAVED = "The current wrapper has now been stored." +
		newline + "In order to continue to define further wrappers, you can either use" +
		newline + "the current wrapper as a basis or clear the wrapper and start afresh." +
		newline + "To clear the wrapper, right-click on the interactive graph and select" +
		newline + "\"Clear all edits\".  Please note that selecting a new area on the page " +
		newline + "view does not clear any conditions that have already been set on the nodes.";
	
	/** Zoom preference */
	protected static final String XMIFRAME_ZOOM = "xmillumframe.zoom";

	/** Layers preference */
	protected static final String XMIFRAME_LAYERS = "xmillumframe.layers";

	/** Browser context */
	protected BrowserContext context = new BrowserContext();

	/** List of layers */
	protected CheckBoxList layersList;

	/** List of status messages */
	protected MessageListModel statusList;

	protected BrowserPanel previewPanel;

	protected URL sourceDocumentBaseURL, stylesheetURL;

	// final static String sourceDocumentBaseURL =

	protected Element sourceDocument;

	protected InputSource isource;

	protected DocumentBuilder db;

	protected GenericSegment instanceBBox;

	protected Page currentPage, currentDisplay;

	protected List<DocNode> exampleInstance;

//	TODO: Remove!
	protected GraphMatcher gm;
	
	protected DocumentGraph pageDG = null;

	protected final JFrame mainWindow;
	
	//final JLabel rootLabel, s1Label, s2Label, s3Label,
//	final 
	protected JLabel rootStatus, s1Status, s2Status, s3Status;
//	final 
	protected JButton rootSave, s1Save, s2Save, s3Save;
	
	//// changed for CeBIT
	////boolean displayGraph = false;
	protected boolean displayGraph = false;
//	boolean displayGraph = true;
	
	//// added for CeBIT
	protected DocumentGraph rootWrapper = null,
		s1Wrapper = null,
		s2Wrapper = null,
		s3Wrapper = null;
	//// end of addition
	
	protected File inFile;

	protected Page thisPage;

	protected JSplitPane leftSplit, rightSplit, scrollSplit;

	protected JSpinner pageSelect;
	protected JSpinner iterSelect;

	protected DocGBPanel wrapperGraphPanel; //, documentGraphPanel

	protected JTabbedPane graphPanel;
	
	protected JList scrollPane;

	protected int segmentationMode;
	protected boolean processSpaces = false;
	protected boolean rulingLines = false;
	
	// boolean wrapperGraphAdded;

	// if done here, presumably the chosen directory will be kept
	protected final JFileChooser fcIn = new JFileChooser();
	protected final JFileChooser fcOut = new JFileChooser();

	public JPanel setUpLeftPanel()
	{
		JPanel leftPanel = new JPanel();
		if (horizToolbar)
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
		else
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));	
		
		// layers pane
		layersList = new CheckBoxList();
		layersList.setVisibleRowCount(3);
		JScrollPane layersPane = new JScrollPane(layersList);
		layersPane.setBorder(BorderFactory.createTitledBorder("Layers"));
		
		leftPanel.add(setUpOperationButtons());//toolBar);//, BorderLayout.PAGE_START);
		leftPanel.add(setUpSegmentationPanel());//segmentationPanel);
		if (horizToolbar)
		{
			leftPanel.add(setUpPageZoom());//pageZoom);
			leftPanel.add(layersPane);//, BorderLayout.CENTER);
			if (showWrapperPanel)
				leftPanel.add(setUpWrapperPanel());
		}
		else
		{
			if (showWrapperPanel)
				leftPanel.add(setUpWrapperPanel());
			leftPanel.add(layersPane);
			leftPanel.add(setUpPageZoom());//pageZoom);
		}
		
		return leftPanel;
	}
	
	public JPanel setUpOperationButtons()
	{
		// open document button :-)
		JButton butOpenDoc = new JButton("Open Document");
		butOpenDoc.setVerticalTextPosition(AbstractButton.CENTER);
		butOpenDoc.setHorizontalTextPosition(AbstractButton.LEADING);
		butOpenDoc.setAlignmentX(Component.CENTER_ALIGNMENT);
		// aka LEFT, for left-to-right locales
		butOpenDoc.setMnemonic(KeyEvent.VK_O);
		butOpenDoc.setPreferredSize(new Dimension(100, 25));
		butOpenDoc.setMaximumSize(new Dimension(Short.MAX_VALUE,
				40));
		// butOpenDoc.setBorder(new Border(5));
		butOpenDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOpenDocDialog(true);
			}
		});
		
		// view graph button :-) uncommented again 12.11.06
		JToggleButton butViewGraph = new JToggleButton("View Graph");
		butViewGraph.setSelected(false);
		butViewGraph.setVerticalTextPosition(AbstractButton.CENTER);
		butViewGraph.setHorizontalTextPosition(AbstractButton.LEADING);
		butViewGraph.setAlignmentX(Component.CENTER_ALIGNMENT);
		// aka LEFT, for left-to-right locales
		butViewGraph.setMnemonic(KeyEvent.VK_G);
		butViewGraph.setPreferredSize(new Dimension(100, 25));
		butViewGraph.setMaximumSize(new Dimension(Short.MAX_VALUE,
				40));
		
		// now we set up the documentGraphPanel to avoid
		// messing about with creating it when needed...
		wrapperGraphPanel = new DocGBPanel(this);
		wrapperGraphPanel.tgUIManager.addUI(new DocWrapperUI(
				wrapperGraphPanel), "wrapper");
		wrapperGraphPanel.tgUIManager.activate("wrapper");
		wrapperGraphPanel.setDoubleBuffered(false);
		
		// if we want multiple tabs with e.g. view
		//graphPanel = new JTabbedPane();
		//graphPanel.addTab("Wrapper", null, wrapperGraphPanel,
		//"Shows the graph view of the wrapper");
		
		butViewGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JToggleButton)e.getSource()).isSelected())
				{
					displayGraph = true;
					
					wrapperGraphPanel.updateStatusBarControls();
					
					rightSplit.setRightComponent(wrapperGraphPanel);
					wrapperGraphPanel.repaint();
					rightSplit.setDividerLocation(0.5);
				}
				else
				{
//					setBottomPane(scrollPane);
					
					displayGraph = false;
					rightSplit.setRightComponent(null);
					wrapperGraphPanel.repaint();
				}
			}
		});

		// find instances button
		JButton butFindInstances = new JButton("Test Wrapper");
				
		butFindInstances.setVerticalTextPosition(AbstractButton.CENTER);
		butFindInstances.setHorizontalTextPosition(AbstractButton.LEADING);
		butFindInstances.setAlignmentX(Component.CENTER_ALIGNMENT);
		// aka LEFT, for left-to-right locales
		butFindInstances.setMnemonic(KeyEvent.VK_T);
		butFindInstances.setPreferredSize(new Dimension(100, 25));
		butFindInstances.setMaximumSize(new Dimension(Short.MAX_VALUE,
				40));
		// the following makes the button un-clickable
		// b1.setActionCommand("disable");
		butFindInstances.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findInstances();
			}
		});

		// print result button :-)
		JButton butPrintResult = new JButton("Print Result");
		butPrintResult.setVerticalTextPosition(AbstractButton.CENTER);
		butPrintResult.setHorizontalTextPosition(AbstractButton.LEADING);
		butPrintResult.setAlignmentX(Component.CENTER_ALIGNMENT);
		// aka LEFT, for left-to-right locales
		butPrintResult.setMnemonic(KeyEvent.VK_S);
		butPrintResult.setPreferredSize(new Dimension(100, 25));
		butPrintResult.setMaximumSize(new Dimension(Short.MAX_VALUE,
				40));
		// butOpenDoc.setBorder(new Border(5));
		butPrintResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentDisplay != null) {
					System.out.println("*** Currently displayed result: ***");
					//System.out.println(currentDisplay.getItems());
					System.out.println(currentDisplay.toExtendedString());

				} else {
					System.out.println("No result has been generated yet.");
				}
			}
		});

		// save wrapper button :-)
		JButton butSaveWrapper = new JButton("Save Wrapper");
		butSaveWrapper.setVerticalTextPosition(AbstractButton.CENTER);
		butSaveWrapper.setHorizontalTextPosition(AbstractButton.LEADING);
		butSaveWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
		// aka LEFT, for left-to-right locales
		butSaveWrapper.setMnemonic(KeyEvent.VK_S);
		butSaveWrapper.setPreferredSize(new Dimension(100, 25));
		butSaveWrapper.setMaximumSize(new Dimension(Short.MAX_VALUE,
				40));
		// butOpenDoc.setBorder(new Border(5));
		butSaveWrapper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveWrapper();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		// toolbar
		JPanel toolBar = new JPanel();
		toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.Y_AXIS));

		toolBar.add(butOpenDoc);
		toolBar.add(butViewGraph);
		toolBar.add(butFindInstances);
		toolBar.add(butSaveWrapper);
		toolBar.add(butPrintResult);
		
		return toolBar;
	}
	
	public JPanel setUpSegmentationPanel()
	{
		// segmentation options radio buttons

		JRadioButton fragmentButton = new JRadioButton("Text frags");
//		fragmentButton.setFont(fragmentButton.getFont().deriveFont(0));
		fragmentButton.setMnemonic(KeyEvent.VK_F);
		fragmentButton.setSelected(true);
		ActionListener fragmentAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_FRAGMENT;
			}
		};
		fragmentButton.addActionListener(fragmentAction);
		
		
		JRadioButton charButton = new JRadioButton("Indiv. chars");
//		charButton.setFont(charButton.getFont().deriveFont(0));
		charButton.setMnemonic(KeyEvent.VK_I);
		charButton.setSelected(true);
		ActionListener charAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_CHAR;
			}
		};
		charButton.addActionListener(charAction);

		JRadioButton lineButton = new JRadioButton("Initial lines");
//		lineButton.setFont(lineButton.getFont().deriveFont(0));
		lineButton.setMnemonic(KeyEvent.VK_L);
		lineButton.setSelected(true);
		ActionListener lineAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_LINE;
			}
		};
		lineButton.addActionListener(lineAction);

		JRadioButton blockButton = new JRadioButton("Blocks");
//		blockButton.setFont(blockButton.getFont().deriveFont(0));
		blockButton.setMnemonic(KeyEvent.VK_B);
		ActionListener blockAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_BLOCK;
			}
		};
		blockButton.addActionListener(blockAction);
		
		JRadioButton atomicLinesButton = new JRadioButton("Merged lines");
//		atomicLinesButton.setFont(atomicLinesButton.getFont().deriveFont(0));
		atomicLinesButton.setMnemonic(KeyEvent.VK_M);
		ActionListener atomicLinesAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_MERGED_LINES;
			}
		};
		atomicLinesButton.addActionListener(atomicLinesAction);
		
		/*
		JRadioButton structButton = new JRadioButton("Structures");
//		structButton.setFont(structButton.getFont().deriveFont(0));
		structButton.setMnemonic(KeyEvent.VK_U);
		ActionListener structAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_STRUCT;
			}
		};
		structButton.addActionListener(structAction);
		
		JRadioButton columnButton = new JRadioButton("Columns");
//		columnButton.setFont(columnButton.getFont().deriveFont(0));
		columnButton.setMnemonic(KeyEvent.VK_C);
		ActionListener columnAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_COLUMN;
			}
		};
		columnButton.addActionListener(columnAction);
		
		JRadioButton tableButton = new JRadioButton("Tables");
//		tableButton.setFont(tableButton.getFont().deriveFont(0));
		tableButton.setMnemonic(KeyEvent.VK_T);
		ActionListener tableAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentationMode = PageProcessor.PP_TABLE;
			}
		};
		tableButton.addActionListener(tableAction);
		 */
		
		JCheckBox spaceButton = new JCheckBox("Remove spaces");
	    spaceButton.setMnemonic(KeyEvent.VK_S); 
	    spaceButton.setSelected(false);
	    ActionListener spaceAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (processSpaces)
					processSpaces = false;
				else processSpaces = true;
			}
		};
		spaceButton.addActionListener(spaceAction);
		
		JCheckBox rulingLinesButton = new JCheckBox("Ruling lines");
	    rulingLinesButton.setMnemonic(KeyEvent.VK_N); 
	    rulingLinesButton.setSelected(false);
	    ActionListener rulingLinesAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rulingLines)
					rulingLines = false;
				else rulingLines = true;
			}
		};
		rulingLinesButton.addActionListener(rulingLinesAction);
		
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(fragmentButton);
		group.add(charButton);
		group.add(lineButton);
		group.add(blockButton);
		group.add(atomicLinesButton);
//		group.add(structButton);
//		group.add(columnButton);
//		group.add(tableButton);

		JPanel segmentationPanel = new JPanel();
		segmentationPanel.setLayout(new BoxLayout(segmentationPanel,
				BoxLayout.X_AXIS));
		segmentationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel seg1 = new JPanel();
		seg1.setLayout(new BoxLayout(seg1,
				BoxLayout.Y_AXIS));
		JPanel seg2 = new JPanel();
		seg2.setLayout(new BoxLayout(seg2,
				BoxLayout.Y_AXIS));
		// the four options, which were available for CeBIT
		seg1.add(charButton);
		seg1.add(fragmentButton);
		seg1.add(lineButton);
		seg1.add(atomicLinesButton);
		seg2.add(blockButton);
//		seg2.add(structButton);

		segmentationPanel.add(seg1);
		segmentationPanel.add(seg2);
		
//		if (!horizToolbar)
//		{
			seg1.add(spaceButton);
			seg2.add(rulingLinesButton);
//		}
//		else
//		{
			// these are added to pageZoom instead
//		}
		
		segmentationPanel.setMinimumSize(new Dimension(100, 25));
		segmentationPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
				400));

		// this line is necessary, as assignment here doesn't
		// set off the action listener
		segmentationMode = DEFAULT_SEGMENTATION_MODE;
		atomicLinesButton.setSelected(true);
		
		segmentationPanel.setBorder(BorderFactory
				.createTitledBorder("Segmentation Mode"));
		
		return segmentationPanel;
	}
	
	public JPanel setUpPageZoom()
	{
		SpinnerModel psm = new SpinnerNumberModel(1, 1, 1000, 1);
		SpinnerModel ism = new SpinnerNumberModel(0, 0, 10000, 1);
		pageSelect = new JSpinner(psm);
		iterSelect = new JSpinner(ism);
		
		JComboBox zoom = new JComboBox(new ZoomEntry[] {
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 8.0d, "800%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 4.0d, "400%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 2.0d, "200%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 1.5d, "150%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 1.25d, "125%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 1.0d, "100%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.67d, "67%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.5d, "50%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.33d, "33%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.25d, "25%"),
				new ZoomEntry(BrowserPanel.SCALE_IMMEDIATE, 0.125d, "12.5%"),
				new ZoomEntry(BrowserPanel.SCALE_SMART,
						BrowserPanel.SMARTSCALE_FIT_WIDTH, "Fit Width"),
				new ZoomEntry(BrowserPanel.SCALE_SMART,
						BrowserPanel.SMARTSCALE_FIT_WINDOW, "Fit in Window") });
		zoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int zoom = ((JComboBox) e.getSource()).getSelectedIndex();
				Preferences.set(XMIFRAME_ZOOM, new Integer(zoom));
				ZoomEntry ze = (ZoomEntry) ((JComboBox) e.getSource())
						.getItemAt(zoom);
				if (previewPanel != null)
				{
					System.out.println("ze.getType(): " + ze.getType());
					System.out.println("ze.getFactor(): " + ze.getFactor());
					previewPanel.setScale(ze.getType(), ze.getFactor());
				}
			}
		});

		try {
			zoom.setSelectedIndex(((Integer) Preferences.get(XMIFRAME_ZOOM,
					new Integer(11))).intValue());
		} catch (IllegalArgumentException e) {
			zoom.setSelectedIndex(11);
		}

//		zoom.setBorder(BorderFactory.createTitledBorder("Zoom"));
		// Required for Java 1.4.0 (?)
		zoom.setMaximumSize(zoom.getMinimumSize());
		
		
		// reload document button :-)
		JButton butReloadDoc = new JButton("Reload Document");
		butReloadDoc.setVerticalTextPosition(AbstractButton.CENTER);
		butReloadDoc.setHorizontalTextPosition(AbstractButton.LEADING);
		butReloadDoc.setAlignmentX(Component.CENTER_ALIGNMENT);
		// aka LEFT, for left-to-right locales
		butReloadDoc.setMnemonic(KeyEvent.VK_R);
		butReloadDoc.setPreferredSize(new Dimension(100, 25));
		butReloadDoc.setMaximumSize(new Dimension(Short.MAX_VALUE,
				40));
		// butOpenDoc.setBorder(new Border(5));
		butReloadDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOpenDocDialog(false);
			}
		});
		
		
// PAGEZOOM
		
		JPanel pageZoom = new JPanel();
		pageZoom.setLayout(new BoxLayout(pageZoom, BoxLayout.Y_AXIS));
		pageZoom.setAlignmentX(Component.CENTER_ALIGNMENT);
		pageZoom.add(butReloadDoc);
		pageSelect.setMaximumSize(new Dimension(105, 25));
		
		JPanel pageControl = new JPanel();
		pageControl.setLayout(new BoxLayout(pageControl, BoxLayout.X_AXIS));
		pageControl.setAlignmentX(Component.CENTER_ALIGNMENT);
		pageControl.add(new JLabel("         Page: "));
		pageControl.add(pageSelect);
		pageZoom.add(pageControl);
		
		iterSelect.setMaximumSize(new Dimension(105, 25));
		JPanel iterControl = new JPanel();
		iterControl.setLayout(new BoxLayout(iterControl, BoxLayout.X_AXIS));
		iterControl.add(new JLabel(" Iterations: "));
		iterControl.add(iterSelect);
		pageZoom.add(iterControl);
		
//		if (horizToolbar)
//		{
//			pageZoom.add(spaceButton);
//			pageZoom.add(rulingLinesButton);
//		}
//		else
//		{
			// already added to seg1 & seg2 (SegmentationMode)
//		}
		
		JPanel zoomControl = new JPanel();
		zoomControl.setLayout(new BoxLayout(zoomControl, BoxLayout.X_AXIS));
		zoomControl.add(new JLabel("  Zoom: "));
		zoomControl.add(zoom);
		pageZoom.add(zoomControl);
		
		pageZoom.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		return pageZoom;
		
	}
	
	public JPanel setUpWrapperPanel()
	{
		JPanel wrapperPanel = new JPanel();
		if (horizToolbar)
			wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
		else
			wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
		
		Dimension prefSize = new Dimension(60, 20);
		
		JLabel rootLabel = new JLabel("Root");
		rootLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//		rootLabel.setPreferredSize(prefSize);
		JLabel s1Label = new JLabel("Sub1");
		s1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
//		s1Label.setPreferredSize(prefSize);
		JLabel s2Label = new JLabel("Sub2");
		s2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
//		s2Label.setPreferredSize(prefSize);
		JLabel s3Label = new JLabel("Sub3");
		s3Label.setAlignmentX(Component.CENTER_ALIGNMENT);
//		s3Label.setPreferredSize(prefSize);
		
		rootStatus = new JLabel(" Empty ");
		rootStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
//		rootStatus.setPreferredSize(prefSize);
		s1Status = new JLabel(" Empty ");
		s1Status.setAlignmentX(Component.CENTER_ALIGNMENT);
//		s1Status.setPreferredSize(prefSize);
		s2Status = new JLabel(" Empty ");
		s2Status.setAlignmentX(Component.CENTER_ALIGNMENT);
//		s2Status.setPreferredSize(prefSize);
		s3Status = new JLabel(" Empty ");
		s3Status.setAlignmentX(Component.CENTER_ALIGNMENT);
//		s3Status.setPreferredSize(prefSize);
		
		rootSave = new JButton("Save");
		rootSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		rootSave.setSize(prefSize);
		rootSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// make sure any text box contents are saved
		        wrapperGraphPanel.updateStatusBarControls();
		        if (currentPage == null) {
					showErrorMsgBox(MSG_NODOC);
				} else if (exampleInstance == null) {
					showErrorMsgBox(MSG_EMPTY);
				} else if (!GraphMatcher.checkForConnectedness(pageDG)) {
					showErrorMsgBox(MSG_CONNECTED);
				} else {
			        // need to clone the wrapper!
					rootWrapper = pageDG.deepCopy();
					rootStatus.setText(" Defined ");
					rootSave.setText("Replace");
					showInfoMsgBox(MSG_SAVED);
		        }
			}
		});
		
		s1Save = new JButton("Save");
		s1Save.setAlignmentX(Component.CENTER_ALIGNMENT);
		s1Save.setSize(prefSize);
		s1Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// make sure any text box contents are saved
		        wrapperGraphPanel.updateStatusBarControls();
		        if (currentPage == null) {
					showErrorMsgBox(MSG_NODOC);
				} else if (exampleInstance == null) {
					showErrorMsgBox(MSG_EMPTY);
				} else if (!GraphMatcher.checkForConnectedness(pageDG)) {
					showErrorMsgBox(MSG_CONNECTED);
				} else {
					// need to clone the wrapper!
					s1Wrapper = pageDG.deepCopy();
					s1Status.setText(" Defined ");
					s1Save.setText("Replace");
					showInfoMsgBox(MSG_SAVED);
				}
			}
		});
		s2Save = new JButton("Save");
		s2Save.setAlignmentX(Component.CENTER_ALIGNMENT);
		s2Save.setSize(prefSize);
		s2Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// make sure any text box contents are saved
		        wrapperGraphPanel.updateStatusBarControls();
		        if (currentPage == null) {
					showErrorMsgBox(MSG_NODOC);
				} else if (exampleInstance == null) {
					showErrorMsgBox(MSG_EMPTY);
				} else if (!GraphMatcher.checkForConnectedness(pageDG)) {
					showErrorMsgBox(MSG_CONNECTED);
				} else {
					// need to clone the wrapper!
					s2Wrapper = pageDG.deepCopy();
					s2Status.setText(" Defined ");
					s2Save.setText("Replace");
					showInfoMsgBox(MSG_SAVED);
				}
			}
		});
		s3Save = new JButton("Save");
		s3Save.setAlignmentX(Component.CENTER_ALIGNMENT);
		s3Save.setSize(prefSize);
		s3Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// make sure any text box contents are saved
		        wrapperGraphPanel.updateStatusBarControls();
		        if (currentPage == null) {
					showErrorMsgBox(MSG_NODOC);
				} else if (exampleInstance == null) {
					showErrorMsgBox(MSG_EMPTY);
				} else if (!GraphMatcher.checkForConnectedness(pageDG)) {
					showErrorMsgBox(MSG_CONNECTED);
				} else {
					// need to clone the wrapper!
					s3Wrapper = pageDG.deepCopy();
					s3Status.setText(" Defined ");
					s3Save.setText("Replace");
					showInfoMsgBox(MSG_SAVED);
				}
			}
		});
		
		JButton rootClear = new JButton("Clear");
		rootClear.setAlignmentX(Component.CENTER_ALIGNMENT);
		rootClear.setSize(prefSize);
		rootClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// need to clone the wrapper!
				rootWrapper = null;
				rootStatus.setText(" Empty ");
				rootSave.setText("Save");
			}
		});
		JButton s1Clear = new JButton("Clear");
		s1Clear.setAlignmentX(Component.CENTER_ALIGNMENT);
		s1Clear.setSize(prefSize);
		s1Clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// need to clone the wrapper!
				s1Wrapper = null;
				s1Status.setText(" Empty ");
				s1Save.setText("Save");
			}
		});
		JButton s2Clear = new JButton("Clear");
		s2Clear.setAlignmentX(Component.CENTER_ALIGNMENT);
		s2Clear.setSize(prefSize);
		s2Clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// need to clone the wrapper!
				s2Wrapper = null;
				s2Status.setText(" Empty ");
				s2Save.setText("Save");
			}
		});
		JButton s3Clear = new JButton("Clear");
		s3Clear.setAlignmentX(Component.CENTER_ALIGNMENT);
		s3Clear.setSize(prefSize);
		s3Clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// need to clone the wrapper!
				s3Wrapper = null;
				s3Status.setText(" Empty ");
				s3Save.setText("Save");
			}
		});
		
		JButton showResult = new JButton("Show result");
		showResult.setAlignmentX(Component.CENTER_ALIGNMENT);
		showResult.setSize(prefSize);
		showResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResult();
			}
		});
		
		JButton showXML = new JButton("Show XML");
		showXML.setAlignmentX(Component.CENTER_ALIGNMENT);
		showXML.setSize(prefSize);
		showXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showXML();
			}
		});
		
		int panelOrientation = BoxLayout.X_AXIS;
		if (horizToolbar) panelOrientation = BoxLayout.Y_AXIS;
		
		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BoxLayout(rootPanel, panelOrientation));
		JPanel rootSubPanel = new JPanel();
		rootSubPanel.setLayout(new BoxLayout(rootSubPanel, BoxLayout.Y_AXIS));
		rootSubPanel.add(rootLabel);
		rootSubPanel.add(rootStatus);
		rootPanel.add(rootSubPanel);
		rootPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		rootPanel.add(rootSave);
		rootPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		rootPanel.add(rootClear);
		rootPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		
		JPanel s1Panel = new JPanel();
		s1Panel.setLayout(new BoxLayout(s1Panel, panelOrientation));
		JPanel s1SubPanel = new JPanel();
		s1SubPanel.setLayout(new BoxLayout(s1SubPanel, BoxLayout.Y_AXIS));
		s1SubPanel.add(s1Label);
		s1SubPanel.add(s1Status);
		s1Panel.add(s1SubPanel);
		s1Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		s1Panel.add(s1Save);
		s1Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		s1Panel.add(s1Clear);
		s1Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		
		JPanel s2Panel = new JPanel();
		s2Panel.setLayout(new BoxLayout(s2Panel, panelOrientation));
		JPanel s2SubPanel = new JPanel();
		s2SubPanel.setLayout(new BoxLayout(s2SubPanel, BoxLayout.Y_AXIS));
		s2SubPanel.add(s2Label);
		s2SubPanel.add(s2Status);
		s2Panel.add(s2SubPanel);
		s2Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		s2Panel.add(s2Save);
		s2Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		s2Panel.add(s2Clear);
		s2Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		
		JPanel s3Panel = new JPanel();
		s3Panel.setLayout(new BoxLayout(s3Panel, panelOrientation));
		JPanel s3SubPanel = new JPanel();
		s3SubPanel.setLayout(new BoxLayout(s3SubPanel, BoxLayout.Y_AXIS));
		s3SubPanel.add(s3Label);
		s3SubPanel.add(s3Status);
		s3Panel.add(s3SubPanel);
		s3Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		s3Panel.add(s3Save);
		s3Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		s3Panel.add(s3Clear);
		s3Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		
		JPanel wrapPanel = new JPanel();
		wrapPanel.setLayout(new BoxLayout(wrapPanel, panelOrientation));
		wrapPanel.add(showResult);
		wrapPanel.add(showXML);
		
		wrapperPanel.add(rootPanel);
		if (!horizToolbar) wrapperPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		wrapperPanel.add(s1Panel);
		if (!horizToolbar) wrapperPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		wrapperPanel.add(s2Panel);
		if (!horizToolbar) wrapperPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		wrapperPanel.add(s3Panel);
		if (!horizToolbar) wrapperPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		wrapperPanel.add(wrapPanel);
		
		wrapperPanel.setBorder(BorderFactory
			.createTitledBorder("Wrapper"));
		
		return wrapperPanel;
	}
	
//	final static Logger log = Logger.getLogger(GUI.class);

	/**
	 * Creates a new XMIllumFrame.
	 * 
	 * @param sourceDocumentBaseURL
	 *            Source document base URL.
	 * @param stylesheetURL
	 *            Stylesheet URL.
	 */
	public GUI() throws IOException, SAXException, ParserConfigurationException {
		
		// set up the JFileChoosers
		ExampleFileFilter inFilter = new ExampleFileFilter();
	    inFilter.addExtension("pdf");
	    inFilter.setDescription("Portable Document Format");
	    fcIn.addChoosableFileFilter(inFilter);
//	    fcIn.setFileFilter(inFilter);
	    ExampleFileFilter inFilter2 = new ExampleFileFilter();
	    inFilter2.addExtension("png");
	    inFilter2.addExtension("tif");
	    inFilter2.addExtension("tiff");
	    inFilter2.addExtension("jpg");
	    inFilter2.addExtension("jpeg");
	    inFilter2.setDescription("Scanned Image");
	    fcIn.addChoosableFileFilter(inFilter2);
	    fcIn.setFileFilter(inFilter);

	    ExampleFileFilter outFilter = new ExampleFileFilter();
	    outFilter.addExtension("xml");
	    outFilter.setDescription("Extensible Markup Language");
	    fcOut.setFileFilter(outFilter);
		
		// TODO: change to a proper main method;
		// accept source etc. as command line parameters
		// also the ability to switch pages... hmm...

		sourceDocumentBaseURL = new File("output.xml").toURL();
		stylesheetURL = new File("xml/text.xsl").toURL();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		db = dbf.newDocumentBuilder();

		/*
		 * isource = new InputSource(sourceDocumentBaseURL.openStream()); //
		 * final Element sourceDocument =
		 * db.parse(isource).getDocumentElement();
		 */

		addListeners();

		// crossplatform look and feel (takes up less space on Mac)
		if(standardLookAndFeel)
		{
			try {
		        UIManager.setLookAndFeel(
		            UIManager.getCrossPlatformLookAndFeelClassName());
		    } catch (Exception e) { }
		}
		
		// float scale = 0.85f;
		float scale = 0.95f;
		// from http://coding.derkeiler.com/Archive/Java/comp.lang.java.gui/2005-05/msg00219.html
		UIDefaults defaults = UIManager.getDefaults(); 
		Enumeration keys = defaults.keys(); 
		while(keys.hasMoreElements()) { 
		Object key = keys.nextElement(); 
		Object value = defaults.get(key); 
		if(value != null && value instanceof Font) { 
		UIManager.put(key, null); 
		Font font = UIManager.getFont(key).deriveFont(0); 
		if(font != null) { 
		float size = font.getSize2D(); 
		UIManager.put(key, new FontUIResource(font.deriveFont(size * 
		scale))); 
		} } }
		
		// doesn't seem to help...
		System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
      
//		JFrame.setDefaultLookAndFeelDecorated(false);

		statusList = new MessageListModel(20);

		//JFrame w = new JFrame("PDF Analyser");
		mainWindow = new JFrame("PDF Analyser");
		mainWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				context.finish();
				context.clearLayer();
				Preferences.store();
				System.exit(0);
			}
		});

		// set up blank panel
		JPanel blankPanel = new JPanel(false);
		JLabel filler = new JLabel("No document has been loaded.");
		filler.setHorizontalAlignment(JLabel.CENTER);
		blankPanel.setLayout(new GridLayout(1, 1));
		blankPanel.add(filler);

		JPanel leftPanel = setUpLeftPanel();

		scrollSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		if (horizToolbar)
			leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		else
			leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		leftSplit.setOneTouchExpandable(true);

		leftSplit.setLeftComponent(leftPanel);
		leftSplit.setRightComponent(scrollSplit);
		scrollSplit.setLeftComponent(rightSplit);
		scrollPane = new JList(statusList);
		//scrollSplit.setRightComponent(new JScrollPane(new JList(statusList)));
//		scrollSplit.setRightComponent(scrollPane);
		rightSplit.setLeftComponent(blankPanel);
		// rightSplit.setRightComponent(blankPanel);

		rightSplit.setDividerLocation(0.5);
//		scrollSplit.setDividerLocation(dividerRatio);
		scrollSplit.setDividerLocation(scrollSplit.getMaximumDividerLocation() - dividerLocation);
		
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(leftSplit, BorderLayout.CENTER);

		// if we already have a source document (e.g. passed
		// from command line) display it
		if (sourceDocument != null)
			displayDocument();

		mainWindow.pack();
		Preferences.watch(mainWindow);
		mainWindow.setVisible(true);

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

	protected void displayDocument() {

		// copied (moved) from openDocumentDialog
		try {
			// (re)-loads an xml file directly
			//System.out.println("one");
			isource = new InputSource(new FileInputStream("output.xml"));
			// final Element
			//System.out.println("two");
			sourceDocument = db.parse(isource).getDocumentElement();
			//System.out.println("three");
			context.setSource(new FileIllumSource(sourceDocument));
			//System.out.println("four");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		// if the panels don't exist, create them
		if (previewPanel == null) {
			previewPanel = new BrowserPanel(context);
			rightSplit.setLeftComponent(previewPanel);
			// previewPanel.setScale(BrowserPanel.SMARTSCALE_FIT_WIDTH);
		}
		
		rightSplit.setDividerLocation(0.5);
//		scrollSplit.setDividerLocation(dividerRatio);
		scrollSplit.setDividerLocation(scrollSplit.getMaximumDividerLocation() - dividerLocation);
	}

	protected void addListeners() {
		context.addStatusListener(new StatusListener() {
			public void setStatus(String message) {
				if (statusList != null) {
					if (message.equals("Node selected"))
						selectCurrentNode();
					else if (message.equals("Selection made"))
						selectExampleInstance();
					else
						statusList.addMessage(message);
				}
			}
		});

		context.addDocumentChangeListener(new DocumentChangeListener() {
			public void documentChanged(DocumentChangeEvent e) {
				switch (e.getType()) {
				case DocumentChangeEvent.DOCUMENT_CHANGED:
					// System.out.println("document changed!!");
					String[] layers = context.getDocument().getLayerNames();
					JCheckBox[] boxes = new JCheckBox[layers.length];
					for (int i = 0; i < layers.length; i++) {
						boxes[i] = new JCheckBox(layers[i]);
						// TODO: check if we want this to be highlighted...
						// TODO: this is how to select default layers!!
						if (layers[i].equals("Clusters")
								|| layers[i].equals("Potential Tables")
								|| layers[i].equals("Tables")
								|| layers[i].equals("Page Image")
								|| layers[i].equals("Found instances")
								|| layers[i].equals("Sub-instances")
								|| layers[i].equals("Table Rows")
								|| layers[i].equals("Table Columns")) {
							boxes[i].setSelected(true);
							// layer is visible by default
						} else {
							boxes[i].setSelected(false);
							context.toggleLayer(layers[i], false);
						}
						boxes[i].addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								JCheckBox cb = (JCheckBox) e.getSource();
								context.toggleLayer(cb.getText(), cb
										.isSelected());
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
	}

	// TODO: re-implement this method? or remove it?
	protected void selectCurrentNode() 
	{
		/*
		// find node
		if (gm == null) {
			statusList.addMessage("No document has been loaded.");
		} else {
			Point dp = context.getMousePosition();
			float xpos = (float) (dp.x / context.getScale());
			float ypos = (float) (dp.y / context.getScale());

			xpos = xpos * (Utils.PDF_POINT_RESOLUTION / Utils.XML_RESOLUTION);
			ypos = ypos * (Utils.PDF_POINT_RESOLUTION / Utils.XML_RESOLUTION);
			ypos = currentPage.getHeight() - ypos;
			
		*/	

				
				// if the graph is open select the node
//				if (documentGraphPanel != null) {
					// TODO: move this method to grapheltset
					// where it makes a ton more sense
					// TODO: you're finding the same node!
//					GenericSegment n = (GenericSegment) documentGraphPanel.getGraphEltSet()
//							.findNode(Integer.toString(selectedSeg.hashCode()));
//					if (n != null)
//						documentGraphPanel.setLocale(n);
//					n = (GenericSegment) wrapperGraphPanel.getGraphEltSet().findNode(
//							Integer.toString(selectedSeg.hashCode()));
//					if (n != null)
//						if (wrapperGraphPanel.getGraphEltSet().contains(n))
//							wrapperGraphPanel.setLocale(n);
					// documentGraphPanel.getsetSelect(n);
//					graphPanel.repaint();
//					documentGraphPanel.repaint();
		
/*					
				}
			} else {
				System.out.println("returned null :)");
			}
			
		}*/
		// status bar text or so saying what it is
		// maybe a different colour
		// find it on graph
	}

	protected void selectExampleInstance() 
	{
		Point dp = context.getMousePosition();
		Point dd = context.getDragDistance();

		instanceBBox = new GenericSegment((float) (dp.x / context.getScale()),
				(float) ((dp.x + dd.x) / context.getScale()),
				(float) (dp.y / context.getScale()),
				(float) ((dp.y + dd.y) / context.getScale()));
		instanceBBox.correctNegativeDimensions(); // necessary here too?

		if (pageDG == null) 
		{
			statusList.addMessage("No document has been loaded.");
		} 
		else 
		{
			instanceBBox
					.scaleCoordinates(Utils.PDF_POINT_RESOLUTION / Utils.XML_RESOLUTION);
			instanceBBox.invertYCoordinates(currentPage);
			instanceBBox.correctNegativeDimensions();

//			exampleInstance = pageDG.getIntersectingNodes(instanceBBox);
			exampleInstance = getNodesWithIntersectingCentres(pageDG, instanceBBox);
			
			if (displayGraph == true)
			{
				// first, if the graph is displayed, we
				// mark the segments appropriately
				/*
				if (documentGraphPanel != null) {
					documentGraphPanel.getGraphEltSet().clearNodeHighlights();
					documentGraphPanel.getGraphEltSet()
							.highlightExampleInstance(
									exampleInstance.getItems());
					documentGraphPanel.repaint();
				}
				 */
				wrapperGraphPanel.setGraphEltSet(new DocGraphEltSet(pageDG));
				
				wrapperGraphPanel.getGraphEltSet().
					enableDisableNodes(exampleInstance);
				wrapperGraphPanel.getGraphEltSet().enableDisableEdges();
				
				wrapperGraphPanel.setLocalityRadius(2);
				
				wrapperGraphPanel.setLocale
					(wrapperGraphPanel.getGraphEltSet().getFirstEnabledNode());
			} 
			else 
			{
				System.out.println("exampleInstance: ");
				ListUtils.printList(exampleInstance);
			}
		}
	}

	public void setBottomPane(JComponent panel)
	{
		scrollSplit.setRightComponent(panel);
//		scrollSplit.setDividerLocation(dividerRatio);
		scrollSplit.setDividerLocation(scrollSplit.getMaximumDividerLocation() - dividerLocation);
	}
	
	/*
	// so it can be set back to the message list when "View Graph" is deselected
	public JList getScrollPane() 
	{
		return scrollPane;
	}
	*/
	
	protected void showSegments(List<GenericSegment> segsToHide, List<GenericSegment> segsToShow) {
		if (currentPage == null) {
			context.setStatus("No document has been loaded.");
		} else if (exampleInstance == null) {
			context.setStatus("No instance has been selected.");
		} else {
			try {
				currentDisplay = (Page) currentPage.clone();
				// TODO: rewrite clone method of Page so that next line is not
				// necessary...
				currentDisplay.setItems(ListUtils.shallowCopy(currentPage.getItems()));
				currentDisplay.getItems().removeAll(segsToHide);
				currentDisplay.getItems().addAll(segsToShow);

				// the following copied from open doc button

				// now output the current display to xml
				statusList.addMessage("Displaying result...");
				org.w3c.dom.Document resultDocument = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.processResultPageToXMLDocument(currentDisplay, false, true);

				byte[] outputFile = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.serializeXML(resultDocument);

				writeToOutputFile(outputFile);

				isource = new InputSource(new FileInputStream("output.xml"));
				sourceDocument = db.parse(isource).getDocumentElement();
				context.setSource(new FileIllumSource(sourceDocument));
			} catch (Exception ex) {
				ex.printStackTrace();
				context.setStatus("Error in processing document.");
			}
		}
	}

	protected void showErrorMsgBox(String text)
	{
		JOptionPane.showMessageDialog(
			    mainWindow,
			    text,
			    "Error!",
			    JOptionPane.WARNING_MESSAGE);
	}
	
	protected void showInfoMsgBox(String text)
	{
		JOptionPane.showMessageDialog(
			    mainWindow,
			    text,
			    "Information",
			    JOptionPane.INFORMATION_MESSAGE);
	}
	
	protected void findInstances() {
		
		// make sure any text box contents are saved
        wrapperGraphPanel.updateStatusBarControls();
        
        /*
        int n = JOptionPane.showConfirmDialog(
			    w,
			    "Would you like green eggs and ham?",
			    "An Inane Question",
			    JOptionPane.YES_NO_OPTION);
			    */
        
		if (currentPage == null) 
		{
			showErrorMsgBox(MSG_NODOC);
		} 
		else if (exampleInstance == null) 
		{
			showErrorMsgBox(MSG_EMPTY);
		} 
		else if (!GraphMatcher.checkForConnectedness(pageDG)) 
		{
			showErrorMsgBox(MSG_CONNECTED);
		} 
		else 
		{
			try 
			{
				long t = System.currentTimeMillis();
				
				// note: conditions are ignored on the document graph; here both graphs are the same
																	  // document graph, wrapper graph
				List<WrappingInstance> theInstances = GraphMatcher.findInstances(pageDG, pageDG, null, null, null);
				
				System.out.println("time for graph matching: " + (System.currentTimeMillis() - t));
				
				// doesn't work, unfortunately
				//SegmentList theInstances = gm.findInstancesError(exampleInstance, 5);
				
				statusList.addMessage("Found " + theInstances.size()
						+ " matching instances");

				currentDisplay = (Page) currentPage.clone();
				currentDisplay.getItems().addAll(theInstances);

				// now output the current display to xml
				statusList.addMessage("Displaying result...");
				org.w3c.dom.Document resultDocument = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.processResultPageToXMLDocument(currentDisplay, false, true);

				byte[] outputFile = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.serializeXML(resultDocument);

				writeToOutputFile(outputFile);

				isource = new InputSource(new FileInputStream("output.xml"));
				sourceDocument = db.parse(isource).getDocumentElement();
				context.setSource(new FileIllumSource(sourceDocument));
			} 
			catch (Exception ex) 
			{
				ex.printStackTrace();
				context.setStatus("Error in processing document.");
			}
		}
	}

	protected void showResult()
	{
		if (rootWrapper == null) 
		{
			showErrorMsgBox("No root wrapper has been defined.");
		}
		else
		{
			try 
			{
				long t = System.currentTimeMillis();
				
				// commented out 2011-02-12 for refactoring purposes (partial)
				// change to 				List<WrappingInstance> theInstances = GraphMatcher.findInstances(pageDG, rootWrapper, null, null, null);

				List<WrappingInstance> theInstances = GraphMatcher.findInstances(pageDG, rootWrapper, null, null, null);
				
				System.out.println("time for graph matching: " + (System.currentTimeMillis() - t));
				
				statusList.addMessage("Found " + theInstances.size()
						+ " matching instances");
	
				List<WrappingInstance> subInstances = new ArrayList<WrappingInstance>();
				for (WrappingInstance instance : theInstances)
				{
					List<DocNode> nextLevelItems = new ArrayList<DocNode>();
					// swallow on node level
    				for (DocNode dn : pageDG.getNodes())
    				{
    					GenericSegment testSeg = dn.toGenericSegment();
    					if (SegmentUtils.intersects(testSeg, instance))
    						nextLevelItems.add(dn);
    				}
					
					//System.out.println("nextLevelItems: " + nextLevelItems.getItems());
					
					DocumentGraph subPageDg = 
						//pageDG.getSubGraph(instance);
						pageDG.subGraph(nextLevelItems);
					
					//System.out.println("subPageDg: " + subPageDg);
					
					//SegmentList subInstances = new SegmentList();
					if (s1Wrapper != null)
						subInstances.addAll(GraphMatcher.findInstances(subPageDg, s1Wrapper, null, null, null));
					if (s2Wrapper != null)
						subInstances.addAll(GraphMatcher.findInstances(subPageDg, s2Wrapper, null, null, null));
					if (s3Wrapper != null)
						subInstances.addAll(GraphMatcher.findInstances(subPageDg, s3Wrapper, null, null, null));
					
					//System.out.println("no subInstances: " + subInstances.size());
					for(WrappingInstance si : subInstances)
						si.setSubInstance(true);
				}
			//s	System.out.println("back");
				currentDisplay = (Page) currentPage.clone();
				currentDisplay.getItems().addAll(theInstances);
				currentDisplay.getItems().addAll(subInstances);
				
				statusList.addMessage("Displaying result...");
				org.w3c.dom.Document resultDocument = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.processResultPageToXMLDocument(currentDisplay, false, true);
	
				byte[] outputFile = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.serializeXML(resultDocument);
	
				writeToOutputFile(outputFile);
	
				isource = new InputSource(new FileInputStream("output.xml"));
				sourceDocument = db.parse(isource).getDocumentElement();
				context.setSource(new FileIllumSource(sourceDocument));
			
			} catch (Exception ex) {
				ex.printStackTrace();
				context.setStatus("Error in processing document.");
			}
		}
	}
	
	protected void showXML()
	{
		if (rootWrapper == null) 
		{
			showErrorMsgBox("No root wrapper has been defined.");
		}
		else
		{
			try 
			{
				// set up result document
				Document resultDocument = null;
				 
				try
		        {
		            DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
		            DocumentBuilder myDocBuilder = myFactory.newDocumentBuilder();
		            DOMImplementation myDOMImpl = myDocBuilder.getDOMImplementation();
		            //org.w3c.dom.Document 
		            resultDocument = 
		                myDOMImpl.createDocument("at.ac.tuwien.dbai.pdfwrap", "pdf-result", null);
		        }
		        catch (ParserConfigurationException e)
		        {
		            e.printStackTrace();
		            // TODO: System.exit
		            System.out.println("error");
		            return;
		        }
		        
		        Element resultElement = resultDocument.getDocumentElement();
		        
		        Element pageResultElement = resultDocument.createElement("page");
		        pageResultElement.setAttribute("page-number",
		        	Integer.toString(((SpinnerNumberModel) pageSelect.getModel())
							.getNumber().intValue()));
	        	resultElement.appendChild(pageResultElement);

	        	//SegmentList result = GraphMatcher.wrap(resultDocument, pageResultElement,
		        //	pageDg, wrapperDocument.getDocumentElement(), model);
				
				long t = System.currentTimeMillis();
				List<List<String>> returnFieldNames = new ArrayList<List<String>>(),
					returnExtractedData = new ArrayList<List<String>>();
				List<WrappingInstance> theInstances = GraphMatcher.findInstances
					(pageDG, rootWrapper, null, returnFieldNames, returnExtractedData);
				
				System.out.println("time for graph matching: " + (System.currentTimeMillis() - t));
				
				statusList.addMessage("Found " + theInstances.size()
						+ " matching instances");
	
				List<WrappingInstance> subInstances = new ArrayList<WrappingInstance>();
				//while(instanceIter.hasNext())
				for (int n = 0; n < theInstances.size(); n ++)
				{
					WrappingInstance instance = theInstances.get(n);
					
					// now add this instance to XML
					Element rootElement = resultDocument.createElement("wrapper-result");
	            	resultElement.appendChild(rootElement);
	        	
		        	List<String> resultFieldNames = returnFieldNames.get(n);
		        	List<String> resultExtractedData = returnExtractedData.get(n);
		        	
		    ///    	System.out.println("no extracted data items: " + resultExtractedData.size());
		        	
		        	for (int p = 0; p < resultExtractedData.size(); p ++)
		        	{
		        		Element newFieldElement = resultDocument.
							createElement(resultFieldNames.get(p));
						resultElement.appendChild(newFieldElement);
						newFieldElement.appendChild(resultDocument.
							createTextNode(resultExtractedData.get(p)));
		        	}
					
					List<DocNode> nextLevelItems = new ArrayList<DocNode>();

					for (DocNode dn : rootWrapper.getNodes())
    				{
    					GenericSegment testSeg = dn.toGenericSegment();
    					if (SegmentUtils.intersects(testSeg, instance))
    						nextLevelItems.add(dn);
    				}
					
					DocumentGraph subPageDg = 
						pageDG.subGraph(nextLevelItems);
					
					List<List<String>> subReturnFieldNames = new ArrayList<List<String>>(),
					subReturnExtractedData = new ArrayList<List<String>>();
					
					if (s1Wrapper != null)
						subInstances.addAll(GraphMatcher.findInstances(subPageDg, 
						s1Wrapper, null, subReturnFieldNames, subReturnExtractedData));
					if (s2Wrapper != null)
						subInstances.addAll(GraphMatcher.findInstances(subPageDg,
						s2Wrapper, null, subReturnFieldNames, subReturnExtractedData));
					if (s3Wrapper != null)
						subInstances.addAll(GraphMatcher.findInstances(subPageDg, 
						s3Wrapper, null, subReturnFieldNames, subReturnExtractedData));
					
					//System.out.println("no subInstances: " + subInstances.size());
					for(WrappingInstance si : subInstances)
						si.setSubInstance(true);
					
					for (int r = 0; r < subReturnExtractedData.size(); r ++)
		        	{
						List<String> subResultExtractedData = 
							subReturnExtractedData.get(r);
						List<String> subResultFieldNames =
							subReturnFieldNames.get(r);
						for (int s = 0; s < subResultExtractedData.size(); s ++)
						{
			        		Element newFieldElement = resultDocument.
								createElement(subResultFieldNames.get(s));
							resultElement.appendChild(newFieldElement);
							newFieldElement.appendChild(resultDocument.
								createTextNode(subResultExtractedData.get(s)));
						}
		        	}
				}
				
				currentDisplay = (Page) currentPage.clone();
				currentDisplay.getItems().addAll(theInstances);
				currentDisplay.getItems().addAll(subInstances);
				
				statusList.addMessage("Displaying result...");
				
				byte[] outputFile = at.ac.tuwien.dbai.pdfwrap.ProcessFile
				.serializeXML(resultDocument);

				writeToWrapperOutputFile(outputFile);
				
				// find out our current directory...
				File currentDir = new File(".");
				
				String s = (BROWSER_EXECUTABLE + " " + currentDir.getCanonicalPath()
						+ File.separator + "wrapper-output.xml");

				Utils.executeCommand(s, null, null);
				
			} catch (Exception ex) {
				ex.printStackTrace();
				context.setStatus("Error in processing document.");
			}
		}
	}

	protected void showOpenDocDialog(boolean newFile) {
		int returnVal;
		
//		boolean newFile = (iterNo == 0 || inFile == null);
		
		iterNo = ((SpinnerNumberModel) iterSelect.getModel())
				.getNumber().intValue();
		// condition in which to show open dialogue:
		// no iteration limit OR (iteration limit and) no file chosen yet
		if (newFile)
			returnVal = fcIn.showOpenDialog(fcIn);
		//if ( segmentationMode != PageProcessor.PP_BLOCK_WATCH)
		//	returnVal = fc.showOpenDialog(fc);
		else
			returnVal = JFileChooser.APPROVE_OPTION;

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (newFile)
				inFile = fcIn.getSelectedFile();
			else
				//inFile = new File("/home/hassan/_PDFs/table_corpus/product_selection.pdf");
				//inFile = new File("/home/hassan/_PDFs/table_corpus/EFLensChart.pdf");
				//inFile = new File("/home/hassan/_PDFs/table_corpus/RY2003ChemicalList.pdf");
				//inFile = new File("/home/hassan/_PDFs/asiafrontpage.pdf");
				// old inFile used
			// This is where a real application would open the file.
			// log.append("Opening: " + file.getName() + "." + newline);
			System.out.println("Opening: " + inFile.getName() + "." + newline);
			try {
				statusList.addMessage("Loading file...");
				// load the pdf file
				byte[] inputFile = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.getBytesFromFile(inFile);

				pageNo = ((SpinnerNumberModel) pageSelect.getModel())
						.getNumber().intValue();

				//moved above
				//iterNo = ((SpinnerNumberModel) iterSelect.getModel())
				//		.getNumber().intValue();

				if (gsBefore) executeGhostscript();
				
				// process the pdf file, using object
				statusList.addMessage("Processing file...");

				List<Page> thePages;
				List<AdjacencyGraph<GenericSegment>> theGraphs =
					new ArrayList<AdjacencyGraph<GenericSegment>>();
				
				/*
				boolean pdf = true;
				if (inFile.getName().endsWith("png") ||
					inFile.getName().endsWith("tif") ||
					inFile.getName().endsWith("tiff")||
					inFile.getName().endsWith("jpg") ||
					inFile.getName().endsWith("jpeg")||
					inFile.getName().endsWith("PNG") ||
					inFile.getName().endsWith("TIF") ||
					inFile.getName().endsWith("TIFF") ||
					inFile.getName().endsWith("JPG") ||
					inFile.getName().endsWith("JPEG")) pdf = false;
				*/
				
				// set up page processor object
		        PageProcessor pp = new PageProcessor();
		        pp.setProcessType(segmentationMode);
		        pp.setRulingLines(rulingLines);
		        pp.setProcessSpaces(processSpaces);
		        pp.setNoIterations(iterNo);
		        
				thePages = at.ac.tuwien.dbai.pdfwrap.ProcessFile.processPDF(inputFile, 
					pp, pageNo, pageNo, null, null, theGraphs, true);
				currentPage = thePages.get(0);
				
				if (segmentationMode != PageProcessor.PP_FRAGMENT &&
					segmentationMode != PageProcessor.PP_CHAR)
				pageDG = new DocumentGraph(theGraphs.get(0));

				// TODO: surround with try/catch
				
				currentDisplay = currentPage;

				/*
				lxDocOntology thisOnt = new lxDocOntology();

				try {
					thisOnt.setUp();
				} catch (Exception e) {
					e.printStackTrace();
				}

				OntModel model = thisOnt.getModel();
				*/
				
//C				2011-01-26 TEMPORARILY COMMENTED OUT
				
//C				currentPage.setDocGraph(((AdjacencyGraph) (currentPage
//C						.getDocGraph())).toAttributedGraph(model));

//C				pageDG.indexEdges();
				
				// // end of addition 15.09.06

				// commented out 13.10.06 for performance reasons

//C				gm = new GraphMatcher(pageDG);
//C				gm.normalizeGraph();
//C				gm.getDocument().indexEdges();
				
				// now output the current display to xml
				statusList.addMessage("Displaying file...");
				org.w3c.dom.Document resultDocument = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.processResultPageToXMLDocument(currentDisplay, false, true);
				byte[] outputFile = at.ac.tuwien.dbai.pdfwrap.ProcessFile
						.serializeXML(resultDocument);

				writeToOutputFile(outputFile);

				// launch ghostscript to create background image
//				File currentDir = new File(".");
//				moved to Utils method 7.10.10

				/*
				 * String s = (GHOSTSCRIPT_EXECUTABLE + " -dBATCH
				 * -sDEVICE=pngmono -r300 -dBATH -dQUITE -dNOPAGEPROMPT -q
				 * -dNOPAUSE \"-sOutputFile=" + currentDir.getCanonicalPath() +
				 * File.separator + "output.png\" \"" + inFile.getPath() +
				 * "\"");
				 */

				// TODO: escape strings -- poss. different treatment for Windows
				// (above works)
				// use png16m for full colour instead of pngmono
				// 30.11.06 -r150 replaced with -r&XML_RESOLUTION
				// 11.06.08 using -dTextAlphaBits=4 seems to have no effect,
				// other than to mess up the rendering of asiafrontpage2.pdf
				// with pngmono (some headline text missing)
				// with pngmono, entire bitmap is bilevel
				// either -sDEVICE=png16m -dTextAlphaBits=4 -dAlignToPixels=0
				// (subpixel hinting) OR
				// -sDEVICE=pngmono -dTextAlphaBits=1
				//  gs -dSAFER -dBATCH -dNOPAUSE -sDEVICE=png16m -dGraphicsAlphaBits=4
				// -dUseCropBox
			    //Sets the page size to the CropBox rather than the MediaBox. Some files have a CropBox that is smaller than the MediaBox and may include white space, registration or cutting marks outside the CropBox. Using this option will set the page size appropriately for a viewer. 
				
//				if (pdf)
				if (!gsBefore) executeGhostscript();
				
				System.out.println("Displaying document...");

				displayDocument();

				mainWindow.setTitle("PDF Analyser - " + inFile.getName());
				
				if (previewPanel != null)
					previewPanel.setScale(BrowserPanel.SCALE_SMART,
							BrowserPanel.SMARTSCALE_FIT_WIDTH);
				/* not visible for some reason here... :(
				try {
					zoom.setSelectedIndex(((Integer) Preferences.get(XMIFRAME_ZOOM,
							new Integer(11))).intValue());
				} catch (IllegalArgumentException e) {
					zoom.setSelectedIndex(11);
				}
				*/
				// 11.08.08: oioioi! what to do here?
				
//				2011-01-26 COMMENTED OUT TEMPORARILY
				/*
				if (segmentationMode == PageProcessor.PP_TABLE) {
					List<GenericSegment> theResult = new ArrayList<GenericSegment>();
					Page outputPage = new Page();
					outputPage.getItems().addAll(
							currentPage.getItems().getTables());
					theResult.add(outputPage);

					try {
						org.w3c.dom.Document resultDocument2 = ProcessFile
								.processResultToXMLDocument(theResult, 1,
										true);

						FileOutputStream fos = new FileOutputStream(
								"/home/tam/table.html");
						ProcessFile.serializeXML(resultDocument2, fos);
						fos.close();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				*/

			} catch (Exception ex) {
				showErrorMsgBox("There was an error displaying the document: \n" 
					+ ex.getMessage());
				statusList.addMessage("Error converting document: "
						+ ex.getMessage());
				ex.printStackTrace();
			}

			/*
			 * context.setSource(new IllumSource() { public Element getData() {
			 * return resultDocument.getDocumentElement(); } public URL
			 * getBaseURL() { return null; } });
			 */

			// TODO: create a separate button for this!
			try {
				sourceDocumentBaseURL = (inFile.toURL());
			} catch (MalformedURLException ex) {
				System.out.println("Is this file bunk?");
				ex.printStackTrace();
			}
		} else {
			// log.append("Open command cancelled by user." + newline);
			System.out.println("Open command cancelled by user." + newline);
		}
	}

	protected void executeGhostscript() throws IOException
	{
//		22.10.10 -- moved to String[] in order to work with spaces in filenames, see:
//		http://stackoverflow.com/questions/697621/spaces-in-java-execute-path-for-os-x
//		String s;
		
		String[] sa;
		
//		if (pdf)
		if (true)
		{
		/*
				s = (Utils.GHOSTSCRIPT_EXECUTABLE
					+ " -dBATCH -sDEVICE=pngmono -r"
					+ Utils.XML_RESOLUTION
					+ " -dTextAlphaBits=4 -dGraphicsAlphaBits=4 -dAlignToPixels=0 -dSAFER -dNOPAUSE -dDOINTERPOLATE -dBATCH -dQUIET -dNOPAGEPROMPT -q -dNOPAUSE -DFirstPage="
					+ pageNo + " -DLastPage=" + pageNo //+ " -geometry 400x400+200+200"
					+ " -dUseCropBox -sOutputFile=" + Utils.getRootDir()
//					+ " -sOutputFile=" + currentDir.getCanonicalPath()
					//+ File.separator + "output.png \"" + inFile.getPath()) + "\"";
					// above line seems to work only in windows?!?
					+ File.separator + "output.png " + inFile.getPath()) + "";		
				*/
		
		sa = new String[] {GHOSTSCRIPT_EXECUTABLE, "-dBATCH", "-sDEVICE=pngmono", 
			"-r" + Utils.XML_RESOLUTION, "-dTextAlphaBits=4", "-dAlignToPixels=0", "-dSAFER",
			"-dNOPAUSE", "-dDOINTERPOLATE", "-dBATCH", "-dQUIET", "-dNOPAGEPROMPT", "-q",
			"-dNOPAUSE", "-DFirstPage=" + pageNo, "-DLastPage=" + pageNo, "-dUseCropBox",
			"-sOutputFile=" + Utils.getRootDir() + File.separator + "output.png",
			inFile.getPath()};
		}
		/*
			else // for OCR only
			{
//				s = (Utils.COPY_EXECUTABLE + " " + inFile.getPath() + " " + 
//					Utils.getRootDir() + File.separator + "output.png ") + "";
				
				sa = new String[] {COPY_EXECUTABLE, inFile.getPath(),
					Utils.getRootDir() + File.separator + "output.png"};
			}
			*/
		if (true){
//			if (newFile) {
			
//				Utils.executeCommand(s, null, null);
//				System.out.println("executing gs");
			Utils.executeCommand(sa, null, null);
		}

		/*
		System.out.println("sa: " + sa.toString());
		
		for (int n = 0; n < sa.length; n ++ )
		{
			System.out.println("sa " + n + ": " + sa[n]);
		}
		*/
			
		// rotation stuff
		if (false) { // seems not to be necessary with current version of gs...
		//if (currentPage.getRotation() != 0) {
			String s2 = "convert -rotate " + currentPage.getRotation()
					+ " output.png output.png";

			Utils.executeCommand(s2, null, null);
			
		}
	}
	
	protected void saveWrapper() throws IOException 
	{
		File outFile;

		int returnVal = fcOut.showSaveDialog(fcOut);//fc.showOpenDialog(fc);
		
		if (returnVal != JFileChooser.APPROVE_OPTION) return;
			
		if(fcOut.getFileFilter().getDescription().equals
			("Extensible Markup Language (.xml)"))
		{
			// add .xml to end of file name
			// IF IT'S NOT ALREADY THERE
		}
		
		outFile = fcOut.getSelectedFile();
			
		org.w3c.dom.Document resultDocument;
		
		// copied from ProcessFile.setUpXML
		try
        {
            DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDocBuilder = myFactory.newDocumentBuilder();
            DOMImplementation myDOMImpl = myDocBuilder.getDOMImplementation();
            //org.w3c.dom.Document 
            resultDocument = 
                myDOMImpl.createDocument("at.ac.tuwien.dbai.pdfwrap", "pdf-wrapper", null);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
            return;
        }
		
        // make sure any text box contents are saved
        wrapperGraphPanel.updateStatusBarControls();
        
        Element docElement = resultDocument.getDocumentElement();
        
        String granularity = "block";
        if (segmentationMode == PageProcessor.PP_MERGED_LINES)
        	granularity = "line";
        if (segmentationMode == PageProcessor.PP_LINE)
        	granularity = "raw-line";
        
        docElement.setAttribute("granularity", granularity);
        docElement.setAttribute("process-spaces", Boolean.toString(processSpaces));
        docElement.setAttribute("process-ruling-lines", Boolean.toString(rulingLines));
        docElement.setAttribute("area-based", "true");
        docElement.setAttribute("output", "true");
        
        pageDG.addAsXMLGraph
        	(resultDocument, docElement, false);
        
		boolean toConsole = false;
		String encoding = "UTF-8";
		
		Writer output = null;
        if( toConsole )
        {
            output = new OutputStreamWriter( System.out );
        }
        else
        {
            if( encoding != null )
            {
                output = new OutputStreamWriter(
                    new FileOutputStream( outFile ), encoding );
            }
            else
            {
                //use default encoding
                output = new OutputStreamWriter(
                    new FileOutputStream( outFile ) );
            }
            //System.out.println("using out put file: " + outFile);
        }
        //System.out.println("resultDocument: " + resultDocument);
        ProcessFile.serializeXML(resultDocument, output);
        
        if( output != null )
        {
            output.close();
        }
	}
	
	/**
	 * ListModel for the status messages.
	 */
	protected class MessageListModel extends AbstractListModel {

		/** List containing the messages */
		ArrayList l = new ArrayList();

		int bufferSize;

		/**
		 * Creates a new message list.
		 * 
		 * @param max
		 *            Max. number of messages.
		 */
		public MessageListModel(int max) {
			bufferSize = max;
		}

		/**
		 * Adds a message to the list.
		 * 
		 * @param message
		 *            Message to be added to the list.
		 */
		public void addMessage(String message) {
			l.add(message);
			if (l.size() > bufferSize) {
				l.remove(0);
			}
			fireContentsChanged(this, 0, l.size() - 1);
		}

		/**
		 * Returns the message at the given position.
		 * 
		 * @param i
		 *            Position
		 * @return Message
		 */
		public Object getElementAt(int i) {
			return l.get(l.size() - 1 - i);
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

	protected class ZoomEntry {
		String message;

		int type;

		double factor;

		public ZoomEntry(int t, double f, String m) {
			type = t;
			factor = f;
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

	protected class IWindow extends JFrame implements Window {
		/**
		 * Creates an IWindow
		 * 
		 * @param title
		 *            Window title.
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
		 * @param menubar
		 *            The menubar to set.
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

	protected void writeToOutputFile(byte[] ba) throws FileNotFoundException,
			IOException {
		String outFileName = "output.xml";
		// File outFile = new File(outFileName);

		FileOutputStream outStream = new FileOutputStream(outFileName);
		outStream.write(ba);
		outStream.close();
	}
	
	protected void writeToWrapperOutputFile(byte[] ba) throws FileNotFoundException,
		IOException {
		String outFileName = "wrapper-output.xml";
		// File outFile = new File(outFileName);
		
		FileOutputStream outStream = new FileOutputStream(outFileName);
		outStream.write(ba);
		outStream.close();
	}

	/**
	 * Main entry point.
	 * 
	 * @param arg
	 *            Command line arguments.
	 */
	public static void main(String arg[]) throws IOException, SAXException,
		MalformedURLException, ParserConfigurationException {
/*		commented out 2011-02-12
		File f = new File("src/at/ac/tuwien/dbai/pdfwrap/log4j.properties");
		URL u = f.toURL();
		PropertyConfigurator.configure(u);
*/
//		System.out.println("\\u002D|\\u2013|\\u2014|\\u2010|\\u2011|\\u2012|\\u2015|\\u2212|\\uFE63|\\uFF0D|\\x96|\\x97|\\xAD|–");

		/*
		 * if (arg.length != 2) { System.err.println("Usage: java
		 * iiuf.xmillum.XMIllumFrame <xml-file> <xsl-file>"); System.exit(1); }
		 */

		// TODO: neaten up dealing with args
		Preferences.addStore(new FilePreferences("xmillumframe"));

		// URL baseURL = (new File(arg[0])).toURL();
		// URL stylesheetURL = (new File(arg[1])).toURL();

		// new GUI(baseURL, stylesheetURL);

		GUI theGUI = new GUI();

		if (arg.length == 1) {
			//theGUI.stylesheetURL = (new File(arg[0])).toURL();
			// modified 11.01.09 -- stylesheetURL unimportant, GS important!
			GHOSTSCRIPT_EXECUTABLE = arg[0];
		}

		/*
		 * if (arg.length == 1) { GUI theGUI = new GUI((new
		 * File(arg[0])).toURL()); } else { GUI theGUI = new GUI(null); }
		 */
	}

	class FileIllumSource implements IllumSource {
		Element docElement;

		public FileIllumSource(Element docElement) {
			this.docElement = docElement;
		}

		public Element getData() {
			return docElement;
		}

		public URL getBaseURL() {
			try {
				// not necessary -- and not portable!
				// return new
				// File("/home/tam/eclipse/PDF_segmentation/output.xml").toURL();
				return new File("output.xml").toURL();
				// return new URL(iiuf.xmillum.IllumDocument.XMI_NSURI);
			} catch (MalformedURLException mue) {
				mue.printStackTrace();
				return null;
			}
		}
	}
	/*
	 * class MarqueeBox extends Graphics2D { }
	 */
	
	public List<DocNode> getNodesWithIntersectingCentres(DocumentGraph dg, GenericSegment bBox)
	{
		ArrayList<DocNode> retVal = new ArrayList<DocNode>();
		for (Object o : dg.getNodes())
		{
			DocNode n = (DocNode)o;
//			if (SegmentUtils.intersects(getSegment(n), bBox))
			if (SegmentUtils.horizIntersect(bBox, n.toGenericSegment().getXmid()) &&
				SegmentUtils.vertIntersect(bBox, n.toGenericSegment().getYmid()))
				retVal.add(n);
		}
		return retVal;
	}
}


/*
 * Copyright (c) 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

/*
 * @(#)ExampleFileFilter.java	1.13 02/06/13
 */

class ExampleFileFilter extends FileFilter {

    private static String TYPE_UNKNOWN = "Type Unknown";
    private static String HIDDEN_FILE = "Hidden File";

    private Hashtable filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public ExampleFileFilter() {
	this.filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExampleFileFilter("jpg");
     *
     * @see #addExtension
     */
    public ExampleFileFilter(String extension) {
	this(extension,null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExampleFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension
     */
    public ExampleFileFilter(String extension, String description) {
	this();
	if(extension!=null) addExtension(extension);
 	if(description!=null) setDescription(description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new ExampleFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed adn
     * will be ignored.
     *
     * @see #addExtension
     */
    public ExampleFileFilter(String[] filters) {
	this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see #addExtension
     */
    public ExampleFileFilter(String[] filters, String description) {
	this();
	for (int i = 0; i < filters.length; i++) {
	    // add filters one by one
	    addExtension(filters[i]);
	}
 	if(description!=null) setDescription(description);
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see FileFilter#accepts
     */
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    String extension = getExtension(f);
	    if(extension != null && filters.get(getExtension(f)) != null) {
		return true;
	    };
	}
	return false;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
     public String getExtension(File f) {
	if(f != null) {
	    String filename = f.getName();
	    int i = filename.lastIndexOf('.');
	    if(i>0 && i<filename.length()-1) {
		return filename.substring(i+1).toLowerCase();
	    };
	}
	return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *   ExampleFileFilter filter = new ExampleFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(String extension) {
	if(filters == null) {
	    filters = new Hashtable(5);
	}
	filters.put(extension.toLowerCase(), this);
	fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     * @see FileFilter#getDescription
     */
    public String getDescription() {
	if(fullDescription == null) {
	    if(description == null || isExtensionListInDescription()) {
 		fullDescription = description==null ? "(" : description + " (";
		// build the description from the extension list
		Enumeration extensions = filters.keys();
		if(extensions != null) {
		    fullDescription += "." + (String) extensions.nextElement();
		    while (extensions.hasMoreElements()) {
			fullDescription += ", ." + (String) extensions.nextElement();
		    }
		}
		fullDescription += ")";
	    } else {
		fullDescription = description;
	    }
	}
	return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     */
    public void setDescription(String description) {
	this.description = description;
	fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see getDescription
     * @see setDescription
     * @see isExtensionListInDescription
     */
    public void setExtensionListInDescription(boolean b) {
	useExtensionsInDescription = b;
	fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see getDescription
     * @see setDescription
     * @see setExtensionListInDescription
     */
    public boolean isExtensionListInDescription() {
	return useExtensionsInDescription;
    }
}
