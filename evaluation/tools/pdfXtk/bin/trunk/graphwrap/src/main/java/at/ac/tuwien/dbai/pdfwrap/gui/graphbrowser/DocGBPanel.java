package at.ac.tuwien.dbai.pdfwrap.gui.graphbrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.util.Collection;
import java.util.Stack;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.ac.tuwien.dbai.pdfwrap.gui.GUI;
import at.ac.tuwien.dbai.pdfwrap.model.graph.*;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;

import com.touchgraph.graphlayout.TGAbstractLens;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGLensSet;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.TGPoint2D;
import com.touchgraph.graphlayout.interaction.HVScroll;
import com.touchgraph.graphlayout.interaction.HyperScroll;
import com.touchgraph.graphlayout.interaction.TGUIManager;
import com.touchgraph.graphlayout.interaction.TGUserInterface;
import com.touchgraph.graphlayout.interaction.ZoomScroll;

/* The code in this module is based on the file TGWikiBrowser.java
 * from the TouchGraph WikiBrowser.  These module is published under 
 * the TouchGraph Apache-style licence, which is printed below.
 * 
 * --------------------------------------------------------------------
 * 
 * TouchGraph LLC. Apache-Style Software License
 *
 *
 * Copyright (c) 2001-2002 Alexander Shapiro. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by 
 *        TouchGraph LLC (http://www.touchgraph.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "TouchGraph" or "TouchGraph LLC" must not be used to endorse 
 *    or promote products derived from this software without prior written 
 *    permission.  For written permission, please contact 
 *    alex@touchgraph.com
 *
 * 5. Products derived from this software may not be called "TouchGraph",
 *    nor may "TouchGraph" appear in their name, without prior written
 *    permission of alex@touchgraph.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL TOUCHGRAPH OR ITS CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 */

/**  
 *  Sets up the main frame, and all the components contained within.
 *  Based upon TGWikiBrowser.java from the TouchGraph WikiBrowser by
 *  TouchGraph LLC/Alexander Shapiro
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser GUI 0.9                               
 */
public class DocGBPanel extends JPanel{

	final int controlHeight = 24;
	
	public static RenderingHints hints;
	static {
	    hints = new RenderingHints(null);
	    // fractional metrics distorts text spacing; not for this application!
	    hints.put(RenderingHints.KEY_FRACTIONALMETRICS,   RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    //hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	    hints.put(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_SPEED);
	    hints.put(RenderingHints.KEY_ANTIALIASING ,       RenderingHints.VALUE_ANTIALIAS_ON);
	    //hints.put(RenderingHints.KEY_ANTIALIASING ,       RenderingHints.VALUE_ANTIALIAS_OFF);
	    hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
	    hints.put(RenderingHints.KEY_COLOR_RENDERING,     RenderingHints.VALUE_COLOR_RENDER_SPEED);
	    hints.put(RenderingHints.KEY_DITHERING,           RenderingHints.VALUE_DITHER_DISABLE);
	}
	
	TGPanel tgPanel;
	GUI gui;
	TGLensSet tgLensSet;
	public TGUIManager tgUIManager; // added after moved to new directory to make visible       
	
	public HVScroll hvScroll;
	public ZoomScroll zoomScroll;
	public HyperScroll hyperScroll;
	
	public JPopupMenu GBPopup;	
	private JTextField tfSearch;
	private Label statusBarText;
	private JComboBox maxExpandCombo;
	private JComboBox localityRadiusCombo;
	
	public String textPaneURL = null;
	
	// the version in TGPanel is immutable
	DocGraphEltSet completeEltSet;
	private Stack browseHistory = new Stack();
	
	final JPanel topPanel = new JPanel();
	final JPanel statusBar = new JPanel();
	final JPanel nodeOptionPanel = new JPanel();
	final JPanel edgeOptionPanel = new JPanel();
	
	public static String INITIAL_NODE=null;
	public static int INITIAL_RADIUS=-1;
	public static boolean INITIAL_SHOW_BACKLINKS=true;		 

	DocNavigateUI navigateUI;  
	TGUserInterface editUI;
	
	private Vector searchItems = new Vector();
	
	JTextField bar;
	
	JCheckBox isEnabledButton, extractButton;
	JSpinner minLengthSpinner, maxLengthSpinner;
	JSpinner minEdgeLengthSpinner, maxEdgeLengthSpinner;
	JPanel matchLengthPanel;
	
	JRadioButton mcNoneButton, mcExactStringButton, mcSubstringButton, mcRegexpButton;
	JRadioButton lBlockButton, lColButton, lGreaterButton;
	JRadioButton mlBlockButton, mlColButton, mlGreaterButton, mlAnyButton;
	JRadioButton roLeftRightButton, roRightLeftButton, roNoneButton;
	JRadioButton siLeftRightButton, siRightLeftButton, siNoneButton;
	JRadioButton mmNoneButton, mmFirstButton, mmLastButton;
	JTextField mcMatchContentString, mcFieldName;
	JPanel nodeOptionPanel1, nodeOptionPanel2;
	ButtonGroup group, lGroup, mlGroup, roGroup, siGroup, mmGroup;
	
	JCheckBox isEdgeEnabledButton, matchNButton;
	JCheckBox aTopLeftButton, aCentreButton, aBottomRightButton;
	JCheckBox maTopLeftButton, maCentreButton, maBottomRightButton;
	JCheckBox crossesRulingLineButton, mCrossesRulingLineButton;
	JCheckBox mReadingOrderButton, mSuperiorInferiorButton;
	JCheckBox mFontButton, mFontSizeButton, boldButton, mBoldButton, 
		italicButton, mItalicButton;
	
	JPanel edgeOptionPanel1, edgeOptionPanel2;
	JLabel nodeLabel, edgeLabel, lLabel, mlLabel, aLabel, maLabel, 
		roLabel, siLabel, minlLabel, maxlLabel, mmLabel, fontLabel, fontSizeLabel;
	
	DocNode tempNode;
	
	public DocGBPanel(GUI gui) {
//	public DocGBPanel() {
		
		this.gui = gui;
		
		completeEltSet = new DocGraphEltSet();
		tgPanel = new TGPanel(this);
		tgPanel.setGraphEltSet(completeEltSet);
		//tgPanel.getGraphics().
		
		/*
		Graphics2D g2d = (Graphics2D)tgPanel.getGraphics();
		g2d is null!!
		g2d.setRenderingHints(hints);
		*/
		
		tgLensSet = new TGLensSet();                
		hvScroll = new HVScroll(tgPanel, tgLensSet);
		zoomScroll = new ZoomScroll(tgPanel);
		hyperScroll = new HyperScroll(tgPanel);
		
		buildPanel();
		buildLens();
		tgPanel.setLensSet(tgLensSet);
		
		addUIs();
		setVisible(true);
		
		DocNode.setNodeBackDefaultColor(Color.decode("#A03000"));                      
		zoomScroll.setZoomValue(4);
		
		if(INITIAL_RADIUS>=0 && INITIAL_RADIUS<=6) 
			localityRadiusCombo.setSelectedIndex(INITIAL_RADIUS);
		
		DocNode initialNode;
		
		if (INITIAL_NODE==null) 
			initialNode = (DocNode) completeEltSet.getFirstNode();
		else 
			initialNode = (DocNode) completeEltSet.findNodeLabelContaining(INITIAL_NODE);        
		
		if(initialNode==null) initialNode = (DocNode) completeEltSet.getFirstNode();
		
		tgPanel.setSelect(initialNode);        
		setLocale(initialNode);
		tgPanel.fastFinishAnimation();
		tgPanel.resetDamper();
	}
	
    public void expandNode(DocNode node)
	{
        tgPanel.expandNode(node);
    }

	public void setSelectedNode(DocNode node)
	{
		tgPanel.setSelect(node);
	}
	
	/*
	 * TODO: why the hell do we need these
	 * when we can simply get the completeGraphEltSet directly?
	public void clearNodeHighlights()
	{
		completeEltSet.clearNodeHighlights();
	}
	
	public void highlightExampleInstance(SegmentList sl)
	{
		completeEltSet.highlightExampleInstance(sl);
	}
	
	public void highlightFoundInstance(SegmentList sl)
	{
		completeEltSet.highlightFoundInstance(sl);
	}
	*/
	
	/* replace?
	public DocNode addTVAeNode(String crid, String title) {
		DocNode n;		
		
		n = new DocNode(crid, title);
		
		try {
			completeEltSet.addNode(n);
		}
		catch(TGException tge) { tge.printStackTrace(); }

		return n;
	}
	*/
	
	public void addDocEdge(DocEdge e)
	{
		completeEltSet.addEdge(e);
	}
		
	public TGPanel getTGPanel() {
		return tgPanel;
	}
		
	public URL getDocumentBase() {
		return null;
	}
	
	class HorizontalStretchLens extends TGAbstractLens {
		protected void applyLens(TGPoint2D p) { p.x=p.x*1.5; }
		protected void undoLens(TGPoint2D p) { p.x=p.x/1.5; }
	}

	private void buildLens() {
		tgLensSet.addLens(hvScroll.getLens());
		tgLensSet.addLens(zoomScroll.getLens());
		tgLensSet.addLens(hyperScroll.getLens());
		tgLensSet.addLens(new HorizontalStretchLens());
		tgLensSet.addLens(tgPanel.getAdjustOriginLens());       
	}

	private void buildPanel() {        
		final JScrollBar horizontalSB = hvScroll.getHorizontalSB();     
		final JScrollBar verticalSB = hvScroll.getVerticalSB(); 
		final JScrollBar zoomSB = zoomScroll.getZoomSB();
		final JScrollBar hyperSB = hyperScroll.getHyperSB();
		//final JScrollBar localitySB = localityScroll.getLocalitySB();
		
		setLayout(new BorderLayout());  
		ToolTipManager.sharedInstance().setInitialDelay(0);
		
		JPanel scrollPanel = new JPanel();
		scrollPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		/*
		final JPanel topPanel = new JPanel();
		final JPanel statusBar = new JPanel();
		*/
		
		topPanel.setLayout(new GridBagLayout());
		c.fill=GridBagConstraints.HORIZONTAL;
		
		//statusBar.setLayout(new BorderLayout());
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.Y_AXIS));
		statusBarText = new Label();
		statusBar.add(statusBarText);
		//matchOptionPanel.setLayout(new BoxLayout(statusBar,
		//		BoxLayout.Y_AXIS));
		nodeOptionPanel.setLayout(new BoxLayout(nodeOptionPanel,
			BoxLayout.Y_AXIS));
		
		isEnabledButton = new JCheckBox("Is enabled for wrapping");
		ActionListener isEnabledAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				if (currDocNode.isRemoveFromInstance())
					currDocNode.setRemoveFromInstance(false);
					//completeEltSet.setRemoveFromInstance
					//	(currDocNode, false);
				else
					currDocNode.setRemoveFromInstance(true);
					//completeEltSet.setRemoveFromInstance
					//	(currDocNode, true);
				completeEltSet.enableDisableEdges();
				tgPanel.repaint();
			}
		};
		isEnabledButton.addActionListener(isEnabledAction);
		
		extractButton = new JCheckBox("Extract this node");
		ActionListener extractAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				if (currDocNode.isExtractContent())
					currDocNode.setExtractContent(false);
					//completeEltSet.setRemoveFromInstance
					//	(currDocNode, false);
				else
					currDocNode.setExtractContent(true);
					//completeEltSet.setRemoveFromInstance
					//	(currDocNode, true);
				completeEltSet.enableDisableEdges();
				tgPanel.repaint();
			}
		};
		extractButton.addActionListener(extractAction);
		
		mcFieldName = new JTextField();
		ActionListener mcFieldNameAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				currDocNode.setSegType(
					mcFieldName.getText());
			}
		};
		// TODO: Focuslistener
		mcFieldName.addActionListener(mcFieldNameAction);
		mcFieldName.setMaximumSize
			(new Dimension(128, controlHeight));
		
		SpinnerModel lsm = new SpinnerNumberModel(-1, -1, 1000, 1);
		SpinnerModel lsm2 = new SpinnerNumberModel(-1, -1, 1000, 1);
		minLengthSpinner = new JSpinner(lsm);
		ChangeListener minLengthAction = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				currDocNode.setMatchMinLength(
					((Integer)((JSpinner)e.getSource()).getValue()));
				tgPanel.repaint();
			}
		};
		minLengthSpinner.addChangeListener(minLengthAction);
		minLengthSpinner.setMaximumSize
			(new Dimension(64, controlHeight));
		
		maxLengthSpinner = new JSpinner(lsm2);
		ChangeListener maxLengthAction = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				currDocNode.setMatchMaxLength(
					((Integer)((JSpinner)e.getSource()).getValue()));
				tgPanel.repaint();
			}
		};
		maxLengthSpinner.addChangeListener(maxLengthAction);
		maxLengthSpinner.setMaximumSize
		(new Dimension(64, controlHeight));
		
		matchLengthPanel = new JPanel();
		matchLengthPanel.setLayout(new BoxLayout
			(matchLengthPanel, BoxLayout.X_AXIS));
		
		nodeLabel = new JLabel();
		matchLengthPanel.add(isEnabledButton);
		//matchLengthPanel.add(extractButton);
		matchLengthPanel.add(nodeLabel);
		
		mcNoneButton = new JRadioButton("None");
		ActionListener mcNoneAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				currDocNode.setMatchContent(
					DocNode.MATCH_CONTENT_OFF);
				tgPanel.repaint();
			}
		};
		mcNoneButton.addActionListener(mcNoneAction);
		
		mcExactStringButton = new JRadioButton("Exact string");
		ActionListener mcExactStringAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode) tgPanel.getSelect();
				currDocNode.setMatchContent(
					DocNode.MATCH_CONTENT_STRING);
				tgPanel.repaint();
			}
		};
		mcExactStringButton.addActionListener(mcExactStringAction);
		
		mcSubstringButton = new JRadioButton("Substring");
		ActionListener mcSubstringAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				currDocNode.setMatchContent(
					DocNode.MATCH_CONTENT_SUBSTRING);
				tgPanel.repaint();
			}
		};
		mcSubstringButton.addActionListener(mcSubstringAction);
		
		mcRegexpButton = new JRadioButton("Regexp");
		ActionListener mcRegexpAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				currDocNode.setMatchContent(
					DocNode.MATCH_CONTENT_REGEXP);
				tgPanel.repaint();
			}
		};
		mcRegexpButton.addActionListener(mcRegexpAction);
		
		group = new ButtonGroup();
		group.add(mcNoneButton);
		group.add(mcExactStringButton);
		group.add(mcSubstringButton);
		group.add(mcRegexpButton);
		
		mcMatchContentString = new JTextField();
		ActionListener mcStringAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				currDocNode.setMatchContentString(
					mcMatchContentString.getText());
				tgPanel.repaint();
			}
		};
		FocusListener mcStringFocus = new FocusListener() {
			public void focusLost(FocusEvent e) {
				//DocNode currDocNode = (DocNode)tgPanel.getSelect();
				// otherwise (next chosen) edge (or node) is selected
				tempNode.setMatchContentString(
					mcMatchContentString.getText());
				tgPanel.repaint();
			}
			public void focusGained(FocusEvent arg0) {
				tempNode = (DocNode)tgPanel.getSelect();
			}
		};
		mcMatchContentString.addActionListener(mcStringAction);
		//mcMatchContentString.addFocusListener(mcStringFocus);
		mcMatchContentString.setMaximumSize
		(new Dimension(128, controlHeight));
		
		nodeOptionPanel1 = new JPanel();
		nodeOptionPanel1.setLayout(new BoxLayout
			(nodeOptionPanel1, BoxLayout.X_AXIS));
		
		//matchContentPanel.setLayout(new FlowLayout());
		
		nodeOptionPanel1.add(new JLabel("     Min length:"));
		nodeOptionPanel1.add(minLengthSpinner);
		nodeOptionPanel1.add(new JLabel("     Max length:"));
		nodeOptionPanel1.add(maxLengthSpinner);
		
		//matchContentPanel.setBorder(BorderFactory
		//	.createTitledBorder("Match content: "));
		nodeOptionPanel1.add(new JLabel("     Match content:"));
		nodeOptionPanel1.add(mcNoneButton);
		nodeOptionPanel1.add(mcExactStringButton);
		nodeOptionPanel1.add(mcSubstringButton);
		nodeOptionPanel1.add(mcRegexpButton);
		nodeOptionPanel1.add(mcMatchContentString);
		
		nodeOptionPanel1.add(extractButton);
		nodeOptionPanel1.add(new JLabel("     Field name:"));
		nodeOptionPanel1.add(mcFieldName);
		
		//matchContentPanel.setSize(Integer.MAX_VALUE, 12);
		
		//nodeOptionPanel.add(isEnabledButton);
		
		nodeOptionPanel2 = new JPanel();
		nodeOptionPanel2.setLayout(new BoxLayout
			(nodeOptionPanel2, BoxLayout.X_AXIS));
		
		fontLabel = new JLabel("Current font: ");
		
		mFontButton = new JCheckBox("Match font    ");
		ActionListener mFontAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				if (currDocNode.isMatchFont())
					currDocNode.setMatchFont(false);
				else
					currDocNode.setMatchFont(true);
				completeEltSet.enableDisableEdges();
				tgPanel.repaint();
			}
		};
		mFontButton.addActionListener(mFontAction);
		
		fontSizeLabel = new JLabel("Font size: ");
		
		mFontSizeButton = new JCheckBox("Match font size    ");
		ActionListener mFontSizeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				if (currDocNode.isMatchFontSize())
					currDocNode.setMatchFontSize(false);
				else
					currDocNode.setMatchFontSize(true);
				completeEltSet.enableDisableEdges();
				tgPanel.repaint();
			}
		};
		mFontSizeButton.addActionListener(mFontSizeAction);
		
		boldButton = new JCheckBox("Bold");
		boldButton.setEnabled(false);
		
		mBoldButton = new JCheckBox("Match bold    ");
		ActionListener mBoldAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				if (currDocNode.isMatchBold())
					currDocNode.setMatchBold(false);
				else
					currDocNode.setMatchBold(true);
				completeEltSet.enableDisableEdges();
				tgPanel.repaint();
			}
		};
		mBoldButton.addActionListener(mBoldAction);
		
		italicButton = new JCheckBox("Italic");
		italicButton.setEnabled(false);
		
		mItalicButton = new JCheckBox("Match italic");
		ActionListener mItalicAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocNode currDocNode = (DocNode)tgPanel.getSelect();
				if (currDocNode.isMatchItalic())
					currDocNode.setMatchItalic(false);
				else
					currDocNode.setMatchItalic(true);
				completeEltSet.enableDisableEdges();
				tgPanel.repaint();
			}
		};
		mItalicButton.addActionListener(mItalicAction);
		
		nodeOptionPanel2.add(fontLabel);
		nodeOptionPanel2.add(mFontButton);
		nodeOptionPanel2.add(fontSizeLabel);
		nodeOptionPanel2.add(mFontSizeButton);
		nodeOptionPanel2.add(boldButton);
		nodeOptionPanel2.add(mBoldButton);
		nodeOptionPanel2.add(italicButton);
		nodeOptionPanel2.add(mItalicButton);
		
		nodeOptionPanel.add(matchLengthPanel);
		nodeOptionPanel.add(nodeOptionPanel1);
		nodeOptionPanel.add(nodeOptionPanel2);
		
		/*
		JCheckBox isEnabledButton = new JCheckBox("Is enabled for wrapping");
		//monospaceButton.setMnemonic(KeyEvent.VK_M);
		isEnabledButton.setSelected(false);
		
		matchOptionPanel.add(isEnabledButton);
		*/
		
		
		
		//edgeOptionPanel = new JPanel();
		
		isEdgeEnabledButton = new JCheckBox("Is enabled for wrapping     ");
		ActionListener isEdgeEnabledAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isRemoveFromInstance())
					currDocEdge.setRemoveFromInstance(false);
				else
					currDocEdge.setRemoveFromInstance(true);
				tgPanel.repaint();
			}
		};
		isEdgeEnabledButton.addActionListener(isEdgeEnabledAction);
		
		
		mmLabel = new JLabel("Multiple match: ");
		mmLabel.setAlignmentX(RIGHT_ALIGNMENT);
		mmNoneButton = new JRadioButton("None");
		ActionListener mmNoneAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMultipleMatch(DocEdge.MATCH_ONE);
				tgPanel.repaint();
			}
		};
		mmNoneButton.addActionListener(mmNoneAction);
		
		mmFirstButton = new JRadioButton("First");
		ActionListener mmFirstAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMultipleMatch(DocEdge.MATCH_N_TIL_FIRST);
				tgPanel.repaint();
			}
		};
		mmFirstButton.addActionListener(mmFirstAction);
		
		mmLastButton = new JRadioButton("Last    ");
		ActionListener mmLastAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMultipleMatch(DocEdge.MATCH_N_TIL_LAST);
				tgPanel.repaint();
			}
		};
		mmLastButton.addActionListener(mmLastAction);
		
		mmGroup = new ButtonGroup();
		mmGroup.add(mmNoneButton);
		mmGroup.add(mmFirstButton);
		mmGroup.add(mmLastButton);
		
		/*
		matchNButton = new JCheckBox("match N occurrences     ");
		ActionListener matchNAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AttributedEdge currDocEdge = (AttributedEdge)tgPanel.getSelect();
				if (currDocEdge.isMatchN())
					currDocEdge.setMatchN(false);
				else
					currDocEdge.setMatchN(true);
				tgPanel.repaint();
			}
		};
		matchNButton.addActionListener(matchNAction);
		*/
		
		edgeLabel = new JLabel();

		edgeOptionPanel1 = new JPanel();
		edgeOptionPanel1.setLayout(new BoxLayout
				(edgeOptionPanel1, BoxLayout.X_AXIS));
		edgeOptionPanel1.add(isEdgeEnabledButton);
		
		edgeOptionPanel1.add(mmLabel);
		edgeOptionPanel1.add(mmNoneButton);
		edgeOptionPanel1.add(mmFirstButton);
		edgeOptionPanel1.add(mmLastButton);
		
		//edgeEnabledPanel.add(matchNButton);
		edgeOptionPanel1.add(edgeLabel);
		
		minlLabel = new JLabel("Min length: ");
		SpinnerModel lsm3 = new SpinnerNumberModel(0, 0, 1000, 1);
		SpinnerModel lsm4 = new SpinnerNumberModel(0, 0, 1000, 1);
		minEdgeLengthSpinner = new JSpinner(lsm3);
		ChangeListener minEdgeLengthAction = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				Integer val = (Integer)((JSpinner)e.getSource()).getValue();
				currDocEdge.setMatchMinLength(
					val.floatValue());
				tgPanel.repaint();
			}
		};
		minEdgeLengthSpinner.addChangeListener(minEdgeLengthAction);
		minEdgeLengthSpinner.setMaximumSize
			(new Dimension(64, controlHeight));
		
		maxlLabel = new JLabel("Max length: ");
		maxEdgeLengthSpinner = new JSpinner(lsm4);
		ChangeListener maxEdgeLengthAction = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				Integer val = (Integer)((JSpinner)e.getSource()).getValue();
				currDocEdge.setMatchMaxLength(
					val.floatValue());
				tgPanel.repaint();
			}
		};
		maxEdgeLengthSpinner.addChangeListener(maxEdgeLengthAction);
		maxEdgeLengthSpinner.setMaximumSize
		(new Dimension(64, controlHeight));
		
		
		
		lLabel = new JLabel("Length: ");
		lLabel.setAlignmentX(RIGHT_ALIGNMENT);
		lBlockButton = new JRadioButton("Block");
		lBlockButton.setEnabled(false);
		lColButton = new JRadioButton("Column");
		lColButton.setEnabled(false);
		lGreaterButton = new JRadioButton("Greater");
		lGreaterButton.setEnabled(false);
		
		lGroup = new ButtonGroup();
		lGroup.add(lBlockButton);
		lGroup.add(lColButton);
		lGroup.add(lGreaterButton);
		
		mlLabel = new JLabel("Match length: ");
		mlLabel.setAlignmentX(RIGHT_ALIGNMENT);
		mlBlockButton = new JRadioButton("Block");
		ActionListener mlBlockAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMatchLength(DocEdge.LENGTH_BLOCK);
				tgPanel.repaint();
			}
		};
		mlBlockButton.addActionListener(mlBlockAction);
		
		mlColButton = new JRadioButton("Column");
		ActionListener mlColAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMatchLength(DocEdge.LENGTH_COLUMN);
				tgPanel.repaint();
			}
		};
		mlColButton.addActionListener(mlColAction);
		
		mlGreaterButton = new JRadioButton("Greater");
		ActionListener mlGreaterAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMatchLength(DocEdge.LENGTH_GREATER);
				tgPanel.repaint();
			}
		};
		mlGreaterButton.addActionListener(mlGreaterAction);
		
		mlAnyButton = new JRadioButton("Any");
		ActionListener mlAnyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				currDocEdge.setMatchLength(DocEdge.LENGTH_ANY);
				tgPanel.repaint();
			}
		};
		mlAnyButton.addActionListener(mlAnyAction);
		
		mlGroup = new ButtonGroup();
		mlGroup.add(mlBlockButton);
		mlGroup.add(mlColButton);
		mlGroup.add(mlGreaterButton);
		mlGroup.add(mlAnyButton);
		
		aLabel = new JLabel("Alignment: ");
		aLabel.setAlignmentX(RIGHT_ALIGNMENT);
		aTopLeftButton = new JCheckBox("Left");
		aTopLeftButton.setEnabled(false);
		aCentreButton = new JCheckBox("Centre");
		aCentreButton.setEnabled(false);
		aBottomRightButton = new JCheckBox("Right");
		aBottomRightButton.setEnabled(false);
		
		maLabel = new JLabel("Match alignment: ");
		maLabel.setAlignmentX(RIGHT_ALIGNMENT);
		maTopLeftButton = new JCheckBox("Left");
		ActionListener maTopLeftAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isMAlignTopLeft())
					currDocEdge.setMAlignTopLeft(false);
				else
					currDocEdge.setMAlignTopLeft(true);
				tgPanel.repaint();
			}
		};
		maTopLeftButton.addActionListener(maTopLeftAction);
		
		maCentreButton = new JCheckBox("Centre");
		ActionListener maCentreAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isMAlignCentre())
					currDocEdge.setMAlignCentre(false);
				else
					currDocEdge.setMAlignCentre(true);
				tgPanel.repaint();
			}
		};
		maCentreButton.addActionListener(maCentreAction);
		
		maBottomRightButton = new JCheckBox("Right");
		ActionListener maBottomRightAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isMAlignBottomRight())
					currDocEdge.setMAlignBottomRight(false);
				else
					currDocEdge.setMAlignBottomRight(true);
				tgPanel.repaint();
			}
		};
		maBottomRightButton.addActionListener(maBottomRightAction);
		
		crossesRulingLineButton = new JCheckBox("Crosses ruling line");
		crossesRulingLineButton.setEnabled(false);
		
		mCrossesRulingLineButton = new JCheckBox("Match ruling line");
		ActionListener mCrossesRulingLineAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isMatchCrossesRulingLine())
					currDocEdge.setMatchCrossesRulingLine(false);
				else
					currDocEdge.setMatchCrossesRulingLine(true);
				tgPanel.repaint();
			}
		};
		mCrossesRulingLineButton.addActionListener(mCrossesRulingLineAction);
		
		
		roLabel = new JLabel("Reading order: ");
		roLabel.setAlignmentX(RIGHT_ALIGNMENT);
		roLeftRightButton = new JRadioButton("Left to right");
		roLeftRightButton.setEnabled(false);
		roRightLeftButton = new JRadioButton("Right to left");
		roRightLeftButton.setEnabled(false);
		roNoneButton = new JRadioButton("None");
		roNoneButton.setEnabled(false);
		
		roGroup = new ButtonGroup();
		roGroup.add(roLeftRightButton);
		roGroup.add(roRightLeftButton);
		roGroup.add(roNoneButton);
		
		mReadingOrderButton = new JCheckBox("Match reading order");
		ActionListener mReadingOrderAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isMatchReadingOrder())
					currDocEdge.setMatchReadingOrder(false);
				else
					currDocEdge.setMatchReadingOrder(true);
				tgPanel.repaint();
			}
		};
		mReadingOrderButton.addActionListener(mReadingOrderAction);
		
		
		siLabel = new JLabel("Superor-inferior relationship: ");
		siLabel.setAlignmentX(RIGHT_ALIGNMENT);
		siLeftRightButton = new JRadioButton("Left to right");
		siLeftRightButton.setEnabled(false);
		siRightLeftButton = new JRadioButton("Right to left");
		siRightLeftButton.setEnabled(false);
		siNoneButton = new JRadioButton("None");
		siNoneButton.setEnabled(false);

		siGroup = new ButtonGroup();
		siGroup.add(roLeftRightButton);
		siGroup.add(roRightLeftButton);
		siGroup.add(roNoneButton);
		
		mSuperiorInferiorButton = new JCheckBox("Match superior-inferior");
		ActionListener mSuperiorInferiorAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocEdge currDocEdge = (DocEdge)tgPanel.getSelect();
				if (currDocEdge.isMatchSuperiorInferior())
					currDocEdge.setMatchSuperiorInferior(false);
				else
					currDocEdge.setMatchSuperiorInferior(true);
				tgPanel.repaint();
			}
		};
		mSuperiorInferiorButton.addActionListener(mReadingOrderAction);
		
		
		edgeOptionPanel2 = new JPanel();
		edgeOptionPanel2.setLayout(new GridBagLayout());
		//edgeMatchNPanel.add(matchNButton);
		
		//edgeOptionPanel = new JPanel();
		edgeOptionPanel.setLayout(new BoxLayout
			(edgeOptionPanel, BoxLayout.Y_AXIS));
		edgeOptionPanel.add(edgeOptionPanel1);
		edgeOptionPanel.add(edgeOptionPanel2);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0; c.gridy = 0;
		edgeOptionPanel2.add(minlLabel, c);
		c.gridx = 1; c.gridy = 0; 
		edgeOptionPanel2.add(minEdgeLengthSpinner, c);
		c.gridx = 0; c.gridy = 1; 
		edgeOptionPanel2.add(maxlLabel, c);
		c.gridx = 1; c.gridy = 1; 
		edgeOptionPanel2.add(maxEdgeLengthSpinner, c);
		
		c.gridx = 2; c.gridy = 0; c.insets = new Insets(0,24,0,0);
		edgeOptionPanel2.add(lLabel, c);
		c.gridx = 3; c.gridy = 0; c.insets = new Insets(0,0,0,0);
		edgeOptionPanel2.add(lBlockButton, c);
		c.gridx = 4; c.gridy = 0;
		edgeOptionPanel2.add(lColButton, c);
		c.gridx = 5; c.gridy = 0;
		edgeOptionPanel2.add(lGreaterButton, c);
		
		c.gridx = 2; c.gridy = 1; c.insets = new Insets(0,24,0,0);
		edgeOptionPanel2.add(mlLabel, c);
		c.gridx = 3; c.gridy = 1; c.insets = new Insets(0,0,0,0);
		edgeOptionPanel2.add(mlBlockButton, c);
		c.gridx = 4; c.gridy = 1;
		edgeOptionPanel2.add(mlColButton, c);
		c.gridx = 5; c.gridy = 1;
		edgeOptionPanel2.add(mlGreaterButton, c);
		c.gridx = 6; c.gridy = 1;
		edgeOptionPanel2.add(mlAnyButton, c);
		
		c.gridx = 8; c.gridy = 0; c.insets = new Insets(0,24,0,0);
		edgeOptionPanel2.add(aLabel, c);
		c.gridx = 9; c.gridy = 0; c.insets = new Insets(0,0,0,0);
		edgeOptionPanel2.add(aTopLeftButton, c);
		c.gridx = 10; c.gridy = 0;
		edgeOptionPanel2.add(aCentreButton, c);
		c.gridx = 11; c.gridy = 0;
		edgeOptionPanel2.add(aBottomRightButton, c);
		
		c.gridx = 8; c.gridy = 1; c.insets = new Insets(0,24,0,0);
		edgeOptionPanel2.add(maLabel, c);
		c.gridx = 9; c.gridy = 1; c.insets = new Insets(0,0,0,0);
		edgeOptionPanel2.add(maTopLeftButton, c);
		c.gridx = 10; c.gridy = 1;
		edgeOptionPanel2.add(maCentreButton, c);
		c.gridx = 11; c.gridy = 1;
		edgeOptionPanel2.add(maBottomRightButton, c);
		
		c.gridx = 13; c.gridy = 0; c.insets = new Insets(0,24,0,0);
		edgeOptionPanel2.add(crossesRulingLineButton, c);
		c.gridx = 13; c.gridy = 1;
		edgeOptionPanel2.add(mCrossesRulingLineButton, c);
		
		/* reading order & sup-inf relations
		c.gridx = 13; c.gridy = 0; c.insets = new Insets(0,24,0,0);
		edgeMatchNPanel.add(roLabel, c);
		c.gridx = 14; c.gridy = 0; c.insets = new Insets(0,0,0,0);
		edgeMatchNPanel.add(roLeftRightButton, c);
		c.gridx = 15; c.gridy = 0;
		edgeMatchNPanel.add(roRightLeftButton, c);
		c.gridx = 16; c.gridy = 0;
		edgeMatchNPanel.add(roNoneButton, c);
		c.gridx = 14; c.gridy = 1; c.gridwidth = 3;
		edgeMatchNPanel.add(mReadingOrderButton, c);
		
		c.gridx = 18; c.gridy = 0; c.gridwidth = 1; c.insets = new Insets(0,24,0,0);
		edgeMatchNPanel.add(siLabel, c);
		c.gridx = 19; c.gridy = 0; c.insets = new Insets(0,0,0,0);
		edgeMatchNPanel.add(siLeftRightButton, c);
		c.gridx = 20; c.gridy = 0;
		edgeMatchNPanel.add(siRightLeftButton, c);
		c.gridx = 21; c.gridy = 0;
		edgeMatchNPanel.add(siNoneButton, c);
		c.gridx = 19; c.gridy = 1; c.gridwidth = 3;
		edgeMatchNPanel.add(mSuperiorInferiorButton, c);
		c.gridwidth = 1;
		*/
		
		//c.gridx=1;c.weightx=0;c.insets = new Insets(0,0,0,0);
		//statusBar.add(edgeOptionPanel);
	//	statusBar.repaint();
	//	statusBar.remove(edgeOptionPanel);
////	statusBar.add(nodeOptionPanel);
	//	gui.setBottomPane(nodeOptionPanel);
		//	statusBar.repaint();
		
		//topPanel.setBackground(TVAeColourCache.getColour("editor.background"));
		topPanel.setBackground(Color.WHITE);
		
		c.gridx=1;c.weightx=0;c.insets = new Insets(0,0,0,0);
	//	topPanel.add(new Label("Search",Label.RIGHT), c);
		c.gridx=2;c.weightx=1.00; c.insets=new Insets(0,0,0,0);        
		tfSearch = new JTextField();
		tfSearch.setToolTipText("Press enter to find node label containing substring");
		// cboSearch.setMinimumSize(new Dimension(160, 20));
		//tfSearch.setMenuWidth(400);
		//tfSearch.setNumberOfItemsViewable(10);
	// commented out 2.03.09
	// topPanel.add(tfSearch, c);
		
		
		tfSearch.addActionListener(new AbstractAction() {


            	public void actionPerformed(ActionEvent e) {              
                    String searchString = tfSearch.getText();
                    if (!searchString.trim().equals("")) {
                    	DocNode foundNode = (DocNode) 
                        		tgPanel.findNodeLabelContaining(searchString);
                        if (foundNode!=null) {
                            setLocale(foundNode);
                            tgPanel.setSelect(foundNode);
                        }
                    }    
                }
            }
		);
		
		
		maxExpandCombo = new JComboBox(new String[] {"5","10","25","50","125","250","500","1000"});
	//	maxExpandCombo.setMenuWidth(75);
		maxExpandCombo.setSize(80, 40);
		
		localityRadiusCombo = new JComboBox(new String[] {"0","2","4","6","8","10","12"});
	//	localityRadiusCombo.setMenuWidth(75);
		localityRadiusCombo.setSize(80, 40);
		
		maxExpandCombo.setSelectedIndex(3);
		localityRadiusCombo.setSelectedIndex(2);
		          
		maxExpandCombo.setToolTipText("Nodes with more than X children will not automatically expand");            
		localityRadiusCombo.setToolTipText("Shows nodes reachable by following up to X edges from current node");            
		
		ActionListener setLocaleAL = new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
            	//System.out.println("tgPanel.getSelect: " + tgPanel.getSelect());
                if (tgPanel.getSelect() instanceof DocNode)
                	setLocale((DocNode)tgPanel.getSelect());
            }        
        };

		maxExpandCombo.addActionListener(setLocaleAL);
		localityRadiusCombo.addActionListener(setLocaleAL);                               
       
		c.gridx=4;c.weightx=0;        
		topPanel.add(new Label("Expansion limit",Label.RIGHT), c);        
		c.gridx=5;c.weightx=0; 
		topPanel.add(maxExpandCombo,c);                                
		c.gridx=6;c.weightx=0;
		topPanel.add(new Label("Locality radius",Label.RIGHT), c);
		c.gridx=7;c.weightx=0;        
		topPanel.add(localityRadiusCombo,c);                
		c.gridx=8;c.weightx=0;        
		topPanel.add(new Label("Zoom",Label.RIGHT), c);
		c.gridx=9;c.weightx=0.5;
		c.insets=new Insets(0,0,0,5); 
		zoomSB.setToolTipText("Controls magnification of graph space");            
		topPanel.add(zoomSB,c);
		c.gridx=10;c.weightx=0;        
		topPanel.add(new Label("Hyperbolic",Label.RIGHT), c);
		c.gridx=11;c.weightx=0.5;
		c.insets=new Insets(0,0,0,5); 
		hyperSB.setToolTipText("Enlarges the centre of the graph space by applying a hyperbolic-like effect"); 
		topPanel.add(hyperSB,c); 
		c.insets = new Insets(0,0,0,4);

		JButton stopButton = new JButton("Stop");
		stopButton.setToolTipText("Stops graph motion; click twice to stop all motion");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tgPanel.stopMotion();
				tgPanel.fastFinishAnimation();
			}        
		});
		stopButton.setPreferredSize(new Dimension(60,20));
		stopButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
		c.gridx=13;c.weightx=0;        
		topPanel.add(stopButton, c);
		
		c.insets=new Insets(0,0,0,0);                                        
		add(topPanel, BorderLayout.NORTH);
		add(statusBar, BorderLayout.SOUTH);
		
		c.fill = GridBagConstraints.BOTH; 
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 1; c.weightx = 1; c.weighty = 1;
		scrollPanel.add(tgPanel,c);
		
		c.gridx = 1; c.gridy = 1; c.weightx = 0; c.weighty = 0;
		scrollPanel.add(verticalSB,c);
		
		c.gridx = 0; c.gridy = 2;
		scrollPanel.add(horizontalSB,c);
				add(scrollPanel, BorderLayout.CENTER);
		
		GBPopup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Toggle Controls");
		ActionListener toggleControlsAction = new ActionListener() {
			boolean controlsVisible = true;
			public void actionPerformed(ActionEvent e) {
				controlsVisible = !controlsVisible;
				horizontalSB.setVisible(controlsVisible);
				verticalSB.setVisible(controlsVisible);                                     
				topPanel.setVisible(controlsVisible);
			}
		};
		menuItem.addActionListener(toggleControlsAction);
		GBPopup.add(menuItem);
	}
	
	private void addUIs() {
		tgUIManager = new TGUIManager();
		navigateUI = new DocNavigateUI(this);
		tgUIManager.addUI(navigateUI,"Navigate");
		tgUIManager.activate("Navigate");
	}
	
	public void setNavigateUI(DocNavigateUI navigateUI)
	{
		this.navigateUI = navigateUI;
	}
	
	public void setEditUI(TGUserInterface editUI)
	{
		this.editUI = editUI;
	}
	
	public void setLocale(DocNode n) {
		try {
			if(maxExpandCombo!=null && n!=null) {
				int localityRadius = Integer.parseInt((String) localityRadiusCombo.getSelectedItem());
				int maxExpandEdgeCount = Integer.parseInt((String) maxExpandCombo.getSelectedItem());
				boolean unidirectional = false;
				int maxAddEdgeCount = 2147483647;
				tgPanel.setLocale(n,localityRadius,maxAddEdgeCount,maxExpandEdgeCount,unidirectional);
				try {
					Thread.sleep(1000); 
					} catch (InterruptedException ex) { 
					}
				hvScroll.scrollToCenter(n);
				tgPanel.setSelect(n); // this line a later addition
				tgPanel.repaintAfterMove();
			}
		}
		catch (TGException tge) { tge.printStackTrace(); }
	}

    public void setLocale( DocNode node, int localityRadius, int maxAddEdgeCount, int maxExpandEdgeCount,
                           boolean unidirectional ) throws TGException {
        tgPanel.setLocale(node, localityRadius, maxAddEdgeCount, maxExpandEdgeCount, unidirectional);
    }
    
    DocNode findNodeByLabel(String label) {
    	DocNode foundNode = null;
		Collection foundNodes = tgPanel.findNodesByLabel(label);
		if (foundNodes!=null && !foundNodes.isEmpty()) {
			foundNode = (DocNode) foundNodes.iterator().next();
		}
		return foundNode;
	}
    
	public DocNode findNode( String id ) {
		if ( id == null ) return null; // ignore
		return (DocNode)completeEltSet.findNode(id);
	}
	
	public void setStatusBarText(String s)
	{
		statusBarText.setText(s);
	}
	
	public void updateStatusBarControls()
	{
		// tempNode is the previous node if it was a TextSegment, otherwise null
		// to deal with text entry boxes...
		if (tempNode != null)
		{
			tempNode.setMatchContentString(mcMatchContentString.getText());
			tempNode.setSegType(mcFieldName.getText());
		}
		Object currentObj = tgPanel.getSelect();
		if (currentObj != null)
        {
////			statusBar.remove(statusBarText);
////			statusBar.remove(nodeOptionPanel);
////			statusBar.remove(edgeOptionPanel);
////			statusBar.add(statusBarText);
//			2011-01-29 TODO! Types of nodes
//        	if (currentNode instanceof TextSegment)
			if (currentObj instanceof DocNode)
			{
        		////statusBar.add(nodeOptionPanel);
        		gui.setBottomPane(nodeOptionPanel);
        		final DocNode currDocNode = (DocNode)currentObj;
        		
        		// to deal with text entry boxes
        		tempNode = currDocNode;
        		
        		//TODO: no get and set methods?
        		isEnabledButton.setSelected(!currDocNode.isRemoveFromInstance());
        		extractButton.setSelected(currDocNode.isExtractContent());
        		mcFieldName.setText(currDocNode.getSegType());
        		nodeLabel.setText("     " + currDocNode.toString());
        		
        		minLengthSpinner.setValue(currDocNode.getMatchMinLength());
        		maxLengthSpinner.setValue(currDocNode.getMatchMaxLength());

        		mcNoneButton.setSelected(currDocNode.getMatchContent() 
        			== DocNode.MATCH_CONTENT_OFF);
        		mcExactStringButton.setSelected(currDocNode.getMatchContent() 
            			== DocNode.MATCH_CONTENT_STRING);
        		mcSubstringButton.setSelected(currDocNode.getMatchContent() 
            			== DocNode.MATCH_CONTENT_SUBSTRING);
        		mcRegexpButton.setSelected(currDocNode.getMatchContent() 
        			== DocNode.MATCH_CONTENT_REGEXP);
        		mcMatchContentString.setText
        			(currDocNode.getMatchContentString());
        		
        		fontLabel.setText("Current font: " + currDocNode.getSegFontName());// + "    ");
        		mFontButton.setSelected(currDocNode.isMatchFont());
        		fontSizeLabel.setText("Font size: " + currDocNode.getSegFontSize());
        		mFontSizeButton.setSelected(currDocNode.isMatchFontSize());
        		boldButton.setSelected(currDocNode.isBold());
        		mBoldButton.setSelected(currDocNode.isMatchBold());
        		italicButton.setSelected(currDocNode.isItalic());
        		mItalicButton.setSelected(currDocNode.isMatchItalic());
        	}
        	else if (currentObj instanceof DocEdge)
        	{
        		tempNode = null;
        		DocEdge currDocEdge = (DocEdge)currentObj;
        		//statusBar.add(edgeOptionPanel); wurscht
        		isEdgeEnabledButton.setSelected
        			(!currDocEdge.isRemoveFromInstance());
        		//matchNButton.setSelected(currDocEdge.isMatchN());
        		
        		if (currDocEdge.getMultipleMatch() == 
        			DocEdge.MATCH_ONE)
        			mmNoneButton.setSelected(true);
        		else if (currDocEdge.getMultipleMatch() == 
        			DocEdge.MATCH_N_TIL_FIRST)
        			mmFirstButton.setSelected(true);
        		else if (currDocEdge.getMultipleMatch() == 
        			DocEdge.MATCH_N_TIL_LAST)
        			mmLastButton.setSelected(true);
        		
        		if (currDocEdge.getRelation().toString().
        			equals(DocEdge.ADJ_BELOW))
        		{
        			aTopLeftButton.setText("Left");
        			aBottomRightButton.setText("Right");
        			maTopLeftButton.setText("Left");
        			maBottomRightButton.setText("Right");
        			
        			roLeftRightButton.setText("Left to right");
        			roRightLeftButton.setText("Right to left");
        			
        			siLeftRightButton.setText("Left to right");
        			siRightLeftButton.setText("Right to left");
        		}
        		else if (currDocEdge.getRelation().toString().
        			equals(DocEdge.ADJ_RIGHT))
        		{
        			aTopLeftButton.setText("Top");
        			aBottomRightButton.setText("Bottom");
        			maTopLeftButton.setText("Top");
        			maBottomRightButton.setText("Bottom");
        			
        			roLeftRightButton.setText("Top to bottom");
        			roRightLeftButton.setText("Bottom to top");
        			
        			siLeftRightButton.setText("Top to bottom");
        			siRightLeftButton.setText("Bottom to top");
        		}
        			
        		minEdgeLengthSpinner.setValue(new Float(currDocEdge.getMatchMinLength()).intValue());
        		maxEdgeLengthSpinner.setValue(new Float(currDocEdge.getMatchMaxLength()).intValue());
        		
        		if (currDocEdge.getLogicalLength() == 
        			DocEdge.LENGTH_BLOCK)
        			lBlockButton.setSelected(true);
        		else if (currDocEdge.getLogicalLength() ==
        			DocEdge.LENGTH_COLUMN)
        			lColButton.setSelected(true);
        		else if (currDocEdge.getLogicalLength() ==
        			DocEdge.LENGTH_GREATER)
        			lGreaterButton.setSelected(true);
        		
        		if (currDocEdge.getMatchLength() ==
        			DocEdge.LENGTH_ANY)
        			mlAnyButton.setSelected(true);
        		else if (currDocEdge.getMatchLength() == 
        			DocEdge.LENGTH_BLOCK)
        			mlBlockButton.setSelected(true);
        		else if (currDocEdge.getMatchLength() ==
        			DocEdge.LENGTH_COLUMN)
        			mlColButton.setSelected(true);
        		else if (currDocEdge.getMatchLength() ==
        			DocEdge.LENGTH_GREATER)
        			mlGreaterButton.setSelected(true);
        		
        		aTopLeftButton.setSelected(currDocEdge.isAlignTopLeft());
        		aCentreButton.setSelected(currDocEdge.isAlignCentre());
        		aBottomRightButton.setSelected(currDocEdge.isAlignBottomRight());
        		
        		maTopLeftButton.setSelected(currDocEdge.isMAlignTopLeft());
        		maCentreButton.setSelected(currDocEdge.isMAlignCentre());
        		maBottomRightButton.setSelected(currDocEdge.isMAlignBottomRight());
        		
        		crossesRulingLineButton.setSelected(currDocEdge.isCrossesRulingLine());
        		mCrossesRulingLineButton.setSelected(currDocEdge.isMatchCrossesRulingLine());
        		
        		if (currDocEdge.getReadingOrder() == 
        			DocEdge.REL_NONE)
        			roNoneButton.setSelected(true);
        		else if (currDocEdge.getReadingOrder() == 
        			DocEdge.REL_LEFT_RIGHT)
        			roLeftRightButton.setSelected(true);
        		else if (currDocEdge.getReadingOrder() == 
        			DocEdge.REL_RIGHT_LEFT)
        			roRightLeftButton.setSelected(true);
        		
        		mReadingOrderButton.setSelected
        			(currDocEdge.isMatchReadingOrder());
        		
        		if (currDocEdge.getSuperiorInferior() == 
        			DocEdge.REL_NONE)
        			siNoneButton.setSelected(true);
        		else if (currDocEdge.getSuperiorInferior() == 
        			DocEdge.REL_LEFT_RIGHT)
        			siLeftRightButton.setSelected(true);
        		else if (currDocEdge.getSuperiorInferior() == 
        			DocEdge.REL_RIGHT_LEFT)
        			siRightLeftButton.setSelected(true);
        		
        		mSuperiorInferiorButton.setSelected
        			(currDocEdge.isMatchSuperiorInferior());
        		
        		edgeLabel.setText(currDocEdge.getStringLabel());
        		////statusBar.add(edgeOptionPanel);
        		gui.setBottomPane(edgeOptionPanel);
        		// does nothing: edgeOptionPanel.repaint();
        	}
        	////statusBar.repaint();
        }
	}
	
	public int getEdgeCount() {
        return completeEltSet.edgeCount();
    }

    /** Returns the current node count. */
    public int getNodeCount() {
        return completeEltSet.nodeCount();
    }
    
    public DocGraphEltSet getGraphEltSet()
    {
    	return completeEltSet;
    }
    
    public void setGraphEltSet(DocGraphEltSet ges)
    {
    	completeEltSet = ges;
    	tgPanel.setGraphEltSet(ges);
    	// TODO: repaint somekinda way?
    }
    
    public void repaint()
    {
    		if (tgPanel != null)
    			tgPanel.repaint();
    		if (topPanel != null)
    			topPanel.repaint(); 
    }
    
    public void setEditMode(boolean b)
    {
    	navigateUI.setEditMode(b);
    }
    
    public void setLocalityRadius(int radius)
    {
    		localityRadiusCombo.setSelectedIndex(radius);
    }
}
