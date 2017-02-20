package at.ac.tuwien.dbai.pdfwrap.gui.elements;

import at.ac.tuwien.dbai.pdfwrap.ProcessFile;
import at.ac.tuwien.dbai.pdfwrap.analysis.PageProcessor;
import at.ac.tuwien.dbai.pdfwrap.exceptions.DocumentProcessingException;
import at.ac.tuwien.dbai.pdfwrap.gui.layer.Style;
import at.ac.tuwien.dbai.pdfwrap.gui.layer.StyledSegment;
import at.ac.tuwien.dbai.pdfwrap.gui.tools.OpenDocFileFilter;
import at.ac.tuwien.dbai.pdfwrap.gui.tools.PDF_XMLSerializer;
import at.ac.tuwien.dbai.pdfwrap.gui.tools.XMLLayerLoader;
import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import at.ac.tuwien.dbai.pdfwrap.model.document.Page;
import at.ac.tuwien.dbai.pdfwrap.utils.Utils;

import org.apache.pdfbox.pdmodel.PDDocument;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicArrowButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main frame of the pdfXtkGUI
 * 
 * In order to run the PDF analysis process via this GUI, GhostScript must be installed
 * 
 * @author Timo Schleicher
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	private String ghostScriptPath;
	
	private JSplitPane split;
	private PageSpinner pageSpinner;
	private SelectionPanel attributePanel;
	private JComboBox<String> resetViewBox;
	private JLabel mouseCoordinateLabel;
	
	private PDFPanel pdfPanel;
	
	private PageProcessor pageProcessor;
	
	private HashMap<String, Style> styleMap;
	private HashMap<String, Integer> pageProcessorMap;
	
	private File curFile;
	private List<Page> pageList;
	
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, URISyntaxException {
        URL url = MainFrame.class.getClassLoader().getResource("guiConfig.xml");
        String configFile = new File( url.toURI() ).getAbsolutePath();

        if( args.length > 0 ) {
            configFile = args[ 0 ];
        }

        System.out.println( "loading config: " + configFile );
		new MainFrame( configFile );
	}


	
	/**
	 * Constructor method for creating the main frame of the GUI
     * @param configFile
	 */
	public MainFrame( String configFile ) {
		
		//Reading the XML styling sheet
		try {
			
			styleMap = XMLLayerLoader.readXML( configFile );
			
		} catch (Exception e) {
			
			JOptionPane.showMessageDialog(this, e.getMessage()+"\n System is shutting down. Please check XML files for correctness");
			System.exit(1);
		}
		
		//Initialize the possible PageProcessor types and their textual appearance
		pageProcessorMap = new HashMap<String, Integer>();
		
		pageProcessorMap.put("Text Fragments", PageProcessor.PP_FRAGMENT);
		pageProcessorMap.put("Indiv. Chars", PageProcessor.PP_CHAR);
		pageProcessorMap.put("Initial Lines", PageProcessor.PP_LINE);
		pageProcessorMap.put("Blocks", PageProcessor.PP_BLOCK);
		pageProcessorMap.put("Merged Lines", PageProcessor.PP_MERGED_LINES);
		
		//Default PageProcessor settings
		pageProcessor = new PageProcessor();
		pageProcessor.setProcessType(PageProcessor.PP_FRAGMENT);
		pageProcessor.setProcessSpaces(false);
		pageProcessor.setRulingLines(false);
        pageProcessor.setNoIterations(0);
        
		initialize();	
	
		//Setting Tooltip disappear time
		ToolTipManager.sharedInstance().setDismissDelay(7000);
		
		//Set window settings
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);		
		this.pack();
		this.setVisible(true);
	}
	
	/**
	 * This method initializes all of the components for the main frame
	 */
	private void initialize() {
		
		//Left component of the JSplitPane that contains different setting possibilities
		JPanel menu = new JPanel();
		menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		//Container for the GhostScript path
		JPanel ghostContainer = new JPanel() {
			
			@Override
			public Dimension getMaximumSize() {
				
				//Need to override because of height resize bug
				Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                max.width = getPreferredSize().width;
                return max;
			}
		};
		
		ghostContainer.setLayout(new BoxLayout(ghostContainer, BoxLayout.X_AXIS));
		
		JLabel ghostLabel = new JLabel("GhostScript:");
		
		final JTextField ghostPath = new JTextField();
		ghostPath.setPreferredSize(new Dimension(50,0));	
		
		ghostPath.setText(checkGhostScript());
		ghostPath.setToolTipText(ghostPath.getText());
		
		BasicArrowButton gsPathArrow = new BasicArrowButton(BasicArrowButton.EAST);
		
		gsPathArrow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				final JFileChooser fc = new JFileChooser();
				
				int returnVal = fc.showOpenDialog(MainFrame.this);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        	
	        		ghostPath.setText(fc.getSelectedFile().getPath());
	        		ghostPath.setCaretPosition(0);
		        }
			}
		});

		ghostContainer.add(Box.createRigidArea(new Dimension(5, 0)));
		ghostContainer.add(ghostLabel);
		ghostContainer.add(Box.createRigidArea(new Dimension(5, 0)));
		ghostContainer.add(ghostPath);
		ghostContainer.add(Box.createRigidArea(new Dimension(5, 0)));
		ghostContainer.add(gsPathArrow);
		ghostContainer.add(Box.createRigidArea(new Dimension(5, 0)));
		
		menu.add(ghostContainer);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		//Button for opening a new PDF file
		JButton openButton = new JButton("Open File");
		openButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//If no GhostScript path has been typed in
				if (ghostPath.getText().length() == 0) {
					
					JOptionPane.showMessageDialog(MainFrame.this, "Please first specify your GhostScript installation path.");
					
				} else {					

					try {
						
						//Get the path from the JTextField
						ghostScriptPath = ghostPath.getText();
						
						//Additionally write it in a text file
						PrintWriter out = new PrintWriter(new File("gs.txt"));
						out.println(ghostScriptPath);
						
						out.close();
						
					} catch (FileNotFoundException er) {
						er.printStackTrace();
					}
					
					final JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new OpenDocFileFilter());
					
					int returnVal = fc.showOpenDialog(MainFrame.this);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			        	
		        		curFile = fc.getSelectedFile();
		        		
				        new PDFAnalysisThread(curFile).execute();
			        }
				}
			}
		});
		
		openButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		menu.add(openButton);
		
		menu.add(Box.createRigidArea(new Dimension(0, 5)));
		
		//Button for saving
		JButton savePDFButton = new JButton("Save as XML");
		savePDFButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
			
	        	saveAsXML(); 
			}
		});
		
		savePDFButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		menu.add(savePDFButton);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		//Button for reloading the current page
		JButton reloadPDFButton = new JButton("Reload Document");
		reloadPDFButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (curFile != null) {
					
					new PDFAnalysisThread(curFile).execute();
				}
			}
		});
		
		reloadPDFButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		menu.add(reloadPDFButton);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		//Combo box for resetting the view frustum
		resetViewBox = new JComboBox<String>() {
			
			@Override
			public Dimension getMaximumSize() {
				
				//Need to override because of height resize bug
				Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                max.width = getPreferredSize().width;
                return max;
			}
		};
		
		resetViewBox.addItem("Fit Width");
		resetViewBox.addItem("Fit Height");
		resetViewBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if (pdfPanel != null) {
					
					if (resetViewBox.getSelectedItem().equals("Fit Width")) {
						
						pdfPanel.fitWindow(true);
						
					} else if (resetViewBox.getSelectedItem().equals("Fit Height")) {
						
						pdfPanel.fitWindow(false);
					}
				}

			}
		});
		
		resetViewBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		menu.add(resetViewBox);
		
		menu.add(Box.createRigidArea(new Dimension(0, 10)));
		
		//Spinner for page spinning
		pageSpinner = new PageSpinner();
		
		//Left button reaction
		pageSpinner.getPreviousButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (curFile != null) {
		    		
					boolean successful = pageSpinner.change(pageSpinner.getCurrentPage()-1);
					
					if(successful) {
						
			            new PDFAnalysisThread(curFile, pageSpinner.getCurrentPage()).execute();
					}

				}
			}
		});
		
		//Right button reaction
		pageSpinner.getNextButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (curFile != null) {
		    		
					boolean successful = pageSpinner.change(pageSpinner.getCurrentPage()+1);
					
					if(successful) {
						
			            new PDFAnalysisThread(curFile, pageSpinner.getCurrentPage()).execute();
					}

				}
			}
		});
		
		//Middle text field reaction
		pageSpinner.getTextField().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (curFile != null) {
		    		
					int side = 0;
					
					try {
						
						side = Integer.parseInt(pageSpinner.getTextField().getText());
								
					} catch (NumberFormatException e) {
						
						pageSpinner.change(pageSpinner.getCurrentPage());
						return;
					}
					
					
					boolean successful = pageSpinner.change(side);
					
					if(successful) {
						
			            new PDFAnalysisThread(curFile, pageSpinner.getCurrentPage()).execute();
					}

				}
			}
		});
		
		pageSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		menu.add(pageSpinner);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		//Create settings components
		String[] processorTypes = pageProcessorMap.keySet().toArray(new String[0]);
		
		final JComboBox<String> processingTypeComboBox = new JComboBox<String>(processorTypes) {
			
			@Override
			public Dimension getMaximumSize() {
				
				//Need to override because of height resize bug
				Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                max.width = getPreferredSize().width;
                return max;
			}
		};

		//Again set the type of the PageProcessor to make sure having initially set the right processor type
		pageProcessor.setProcessType(pageProcessorMap.get((String) processingTypeComboBox.getSelectedItem()));
		
		processingTypeComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				String selectedType = (String) processingTypeComboBox.getSelectedItem();
				
				pageProcessor.setProcessType(pageProcessorMap.get(selectedType));
			}
		});
		
		//Finally add the two check boxes for further options
		final JCheckBox rulingLinesCheckBox = new JCheckBox("Ruling Lines");
		rulingLinesCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		rulingLinesCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pageProcessor.setRulingLines(rulingLinesCheckBox.isSelected());
			}
		});
		
		final JCheckBox removeSpaceCheckBox = new JCheckBox("Remove Space");
		removeSpaceCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		removeSpaceCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pageProcessor.setProcessSpaces(removeSpaceCheckBox.isSelected());
			}
		});
		
		//Create a JPanel to cover all the settings components
		JPanel processingOptionPanel = new JPanel();
		
		processingOptionPanel.setLayout(new BoxLayout(processingOptionPanel , BoxLayout.Y_AXIS));
		processingOptionPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		processingOptionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		processingOptionPanel.add(processingTypeComboBox);
		
		processingOptionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		processingOptionPanel.add(removeSpaceCheckBox);
		processingOptionPanel.add(rulingLinesCheckBox);
		
		menu.add(processingOptionPanel);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		//Create the panel for displaying attributes of selected segment
		attributePanel = new SelectionPanel();
		
		//Create a list for the different kinds of layers
		CheckBoxList list = new CheckBoxList(styleMap, attributePanel);
		
		menu.add(list);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));
		
		menu.add(attributePanel);
		
		menu.add(Box.createRigidArea(new Dimension(0, 15)));

		//Panel for displaying the mouse coordinates
		JPanel coordinatePanel = new JPanel();
		
		mouseCoordinateLabel = new JLabel("  /  ");
		
		coordinatePanel.add(mouseCoordinateLabel);
		
		menu.add(coordinatePanel);
		
		Dimension fitWindow = getBestWindowSize();
		
		menu.setPreferredSize(new Dimension(160, (int)fitWindow.getHeight()));
		
		//Right component of the JSplitPane that contains the PDF view
		JPanel PDFViewerPanel = new JPanel();
		PDFViewerPanel.setPreferredSize(fitWindow);

		JScrollPane menuScroll = new JScrollPane(menu);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuScroll, PDFViewerPanel);
		split.setEnabled(false);
		
		getContentPane().add(split);
	}
	
	/**
	 * Getter method for the PDF panel.
	 * 
	 * @return the current PDF panel
	 */
	public PDFPanel getPDFPanel() {
		
		return this.pdfPanel;
	}
	
	/**
	 * Saves the current PDF analysis pages to XML.
	 */
	private void saveAsXML() {
		
		if (curFile == null) {
			return;
		}
			
		JFileChooser fc = new JFileChooser();
		
		//Create a filter for only display XML files
		fc.setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File f) {
				
		        if (f.isDirectory()) {
		        	
		            return true;
		            
		        } else {
		        	
		            return f.getName().toLowerCase().endsWith(".xml");
		        }
			}

			@Override
			public String getDescription() {
				
				return "XML Documents (*.xml)";
			}
		});
		
		int returnVal = fc.showSaveDialog(MainFrame.this);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {

        	try {
        		
        		//Actual saving process
				PDF_XMLSerializer.serialize(fc.getSelectedFile().getPath(), pageList, curFile);
				
			} catch(FileNotFoundException e) {
				
				JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
				
			} catch (Exception e) {
				
				JOptionPane.showMessageDialog(MainFrame.this, "An error occurred during the saving process.\n" + e.getMessage());
			}
        }
		
	}
	
	/**
	 * Determine the best frame size according to the current user screen size
	 * 
	 * @return best width and height for the GUI
	 */
	private Dimension getBestWindowSize() {
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		return new Dimension((int)(screen.getWidth()*(1.0/3.0)), (int)(screen.getHeight()*(2.0/3.0)));
	}
	
	/**
	 * Checks if a file exists that contains the GhostScript path.
	 * 
	 * @return the GhostScript path contained in the text file otherwise null
	 */
	private String checkGhostScript() {

			if (new File("gs.txt").exists()) {
				
				try {
					
					InputStream in = new FileInputStream("gs.txt");
					BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(in)));
					
					String temp = br.readLine();
					
					br.close();
					
					return temp;
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return null;		
		}
	
	/**
	 * Inner class using SwingWorker to prevent the GUI from freezing while analyzing a PDF document
	 * 
	 * @author Timo Schleicher
	 *
	 */
	class PDFAnalysisThread extends SwingWorker<Object, Object> {

		private File f;
		
		private boolean loadNew;
		
		private int pageNo;
		
		/**
		 * Constructor method for the PDF analysis thread based on SwingWorker.
		 * Tries to only layout and print a PDF page, but uses previous analysis output.
		 * 
		 * @param f The PDF or XML file
		 * @param pageNo The page number you want to show
		 */
		public PDFAnalysisThread(File f, int pageNo) {
			
			this.pageNo = pageNo;
			
			this.loadNew = false;
			
			this.f = f;		
		}
		
		/**
		 * Constructor method for the PDF analysis thread based on SwingWorker.
		 * Loads the PDF completely new and analyzes it.
		 * 
		 * @param f The PDF or XML file
		 */
		public PDFAnalysisThread(File f) {
			
			this.loadNew = true;
			
			this.pageNo = 1;
			
			this.f = f;		
		}
		
		@Override
		protected Object doInBackground() throws Exception {      
			
			String pdfPath = (f.getPath().toLowerCase().endsWith(".pdf")) ? f.getPath() : PDF_XMLSerializer.getPDFPath(f);
			
			if (!new File(pdfPath).exists()) {
				
				JOptionPane.showMessageDialog(MainFrame.this, "Can not find the following file:\n" + pdfPath);
				return null;
			}
			
			if (loadNew) {
				
				List<Page> thePages = null;
				
				if (f.getPath().toLowerCase().endsWith(".pdf")) {
					
					try {
						
						byte[] inFile = ProcessFile.getBytesFromFile(f);
						
						thePages = ProcessFile.processPDF(inFile, 
							pageProcessor, 1, Integer.MAX_VALUE, null, null, null, true);
						
						for (int i = 0; i < thePages.size(); i++) {
							
							thePages.get(i).setPageNo(i+1);
						}
						
					} catch (DocumentProcessingException e) {
		
						JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
						return null;
					}
					
				} else if (f.getPath().toLowerCase().endsWith(".xml")) {
					
					thePages = PDF_XMLSerializer.deserializeAnalysis(f);
				}
				
				PDDocument pdf = PDDocument.load(pdfPath);
				
	    		pageSpinner.initNewSpinnerValues(pdf.getNumberOfPages());
	    		
				pageList = thePages;
				
				if (pageList == null) {
					
					return null;
				}
			}
			
			List<GenericSegment> seg = pageList.get(pageNo-1).getItems();			
			
			ArrayList<StyledSegment> styl = new ArrayList<StyledSegment>();
			
			//Link the different segments with the corresponding styling object
			for (Map.Entry<String, Style> entry : styleMap.entrySet()) {
				
				String xmlName = entry.getValue().getSource();
				
				for (GenericSegment s : seg) {

					if (s.tagName().equals(xmlName)) {
						
						styl.add(new StyledSegment(s,entry.getValue()));
					}	
				}
			}
			
			//Convert PDF to image by means of ghostscript
			String[] sa = new String[] {ghostScriptPath, "-dBATCH", "-sDEVICE=pngmono", 
			"-r" + Utils.XML_RESOLUTION, "-dTextAlphaBits=4", "-dAlignToPixels=0", "-dSAFER",
			"-dNOPAUSE", "-dDOINTERPOLATE", "-dBATCH", "-dQUIET", "-dNOPAGEPROMPT", "-q",
			"-dNOPAUSE", "-DFirstPage=" + pageNo, "-DLastPage=" + pageNo, "-dUseCropBox",
			"-sOutputFile=" + Utils.getRootDir() + File.separator + "output.png", pdfPath};
	
			Utils.executeCommand(sa, null, null);
			
			BufferedImage img = ImageIO.read(new File("output.png"));
    		
			attributePanel.setSelectedElements(null);
			
			resetViewBox.setSelectedItem("Fit Width");	
			
			pdfPanel = new PDFPanel(split.getRightComponent().getSize(), img, styl, attributePanel, pageList.get(pageNo-1), mouseCoordinateLabel);
			split.setRightComponent(pdfPanel);
			
			return null;
		}
	}
	
}
