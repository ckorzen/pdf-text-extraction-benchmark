/* GraphWrap/PDF Analyser Beta 1
 * 
 * The code in this module is based on the file WikiNavigateUI.java
 * from the TouchGraph WikiBrowser application.  These module is published 
 * under the TouchGraph Apache-style licence, which is printed below.
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
 * 1. Redistributions of soursce code must retain the above copyright
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
  *  Generic user interface for moving around the graph.  Based on
  *  WikiNavigateUI.java from TouchGraph WikiBrowser by TouchGraph LLC/
  *  Alexander Shapiro 
  *  
  *  @author Tamir Hassan, hassan@dbai.tuwien.ac.at
  *  @version GraphWrap Beta 1
  */

package at.ac.tuwien.dbai.pdfwrap.gui.graphbrowser;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/*
import uk.co.tvae.framework.UI.tools.TVAeImageCache;
import uk.co.tvae.plugins.gpie.components.menu.TVAeMenuItem;
import uk.co.tvae.plugins.gpie.components.menu.TVAeMenuSeparator;
import uk.co.tvae.plugins.tvanytime.visualization.graphelements.TVAeAllGenreNode;
import uk.co.tvae.plugins.tvanytime.visualization.graphelements.TVAeGenreNode;
import uk.co.tvae.plugins.tvanytime.visualization.graphelements.TVAeGroupNode;
import uk.co.tvae.plugins.tvanytime.visualization.graphelements.TVAePanel;
import uk.co.tvae.plugins.tvanytime.visualization.graphelements.TVAeProgGroupNode;
import uk.co.tvae.plugins.tvanytime.visualization.graphelements.TVAeProgNode;
import uk.co.tvae.plugins.tvanytime.visualization.interaction.TVAePopupListener;
import bbc.rd.tvanytime.util.TVAnytimeGenreToolbox;
*/

import at.ac.tuwien.dbai.pdfwrap.model.graph.DocEdge;
import at.ac.tuwien.dbai.pdfwrap.model.graph.DocNode;
import at.ac.tuwien.dbai.pdfwrap.model.document.*;

import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.interaction.DragNodeUI;
import com.touchgraph.graphlayout.interaction.TGAbstractClickUI;
import com.touchgraph.graphlayout.interaction.TGAbstractDragUI;
import com.touchgraph.graphlayout.interaction.TGUserInterface;

/**
 * DocNavigateUI
 * Some code based on TouchGraph
 * 
 * @author Tamir Hassan, pdfanalyser@tamirhassan.com
 * @version PDF Analyser GUI 0.9
 */
public class DocNavigateUI extends TGUserInterface {

    TGPanel tgPanel;
    DocGBPanel docGBPanel;
    DocNavigateMouseListener ml;
    DocNavigateMouseMotionListener mml;

    TGAbstractDragUI hvDragUI;

    TGAbstractClickUI hvScrollToCenterUI;
    DragNodeUI dragNodeUI;
    
    // determines whether the "Enter Edit Mode" menu item is enabled
    private boolean editMode;
    
    JPopupMenu nodePopup;   
    JPopupMenu edgePopup;
    JPopupMenu backPopup;
    DocNode popupNode;
    DocEdge popupEdge;

    //DocPopupListener popupListenerObject;

    public DocNavigateUI(DocGBPanel docGBPanel) {
        // rename both these variables!
    	this.docGBPanel = docGBPanel;
        tgPanel = docGBPanel.getTGPanel();

        //localityScroll=tgWikiBrowser.localityScroll;
        
        editMode = false;

        hvDragUI = docGBPanel.hvScroll.getHVDragUI();

        hvScrollToCenterUI = docGBPanel.hvScroll.getHVScrollToCenterUI();
        
        dragNodeUI = new DragNodeUI(tgPanel);                   

        ml = new DocNavigateMouseListener();
        mml = new DocNavigateMouseMotionListener();
    }

    public void activate() {        
        tgPanel.addMouseListener(ml);
        tgPanel.addMouseMotionListener(mml);
    }
    
    public void deactivate() {
        tgPanel.removeMouseListener(ml);
        tgPanel.removeMouseMotionListener(mml);
    }
    
    class DocNavigateMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
               triggerPopup(e);
            }
            else {
            	DocNode mouseOverN = (DocNode) tgPanel.getMouseOverN();            
                if (e.getModifiers() == MouseEvent.BUTTON1_MASK) { 
                    if (mouseOverN == null) 
                        hvDragUI.activate(e);
                    else 
                        dragNodeUI.activate(e);
                }
            }
        }    

        public void mouseClicked(MouseEvent e) {
        	DocNode mouseOverN = (DocNode) tgPanel.getMouseOverN();
            DocEdge mouseOverE = (DocEdge) tgPanel.getMouseOverE();
            DocNode select = (DocNode) tgPanel.getSelect();
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK)!=0) { 
                if (mouseOverN != null) {                    
                    if (e.getClickCount()==1) {
                       // docGBPanel.setSelect(mouseOverN);
                    	tgPanel.setSelect(mouseOverN);                        
                    }
                    else {                   
                        tgPanel.setSelect(mouseOverN);                    
                        docGBPanel.setLocale(mouseOverN);
                    }
                }
                /*
                else if (mouseOverE != null)
                {
                	tgPanel.setSelect(mouseOverE);
                }
                */
            }   
        }
        
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
               triggerPopup(e);
            }
        }   

        public void triggerPopup(MouseEvent e) {
            popupNode = (DocNode) tgPanel.getMouseOverN();
            popupEdge = (DocEdge) tgPanel.getMouseOverE();
            if (popupNode != null) {
                tgPanel.setMaintainMouseOver(true);
                setUpNodePopup();
                nodePopup.show(e.getComponent(), e.getX(), e.getY());
            }
            
            else {
            	if (backPopup != null)
            		backPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    class DocNavigateMouseMotionListener extends MouseMotionAdapter {
        public void mouseMoved( MouseEvent e ) {
        	DocNode mouseOverN = (DocNode) tgPanel.getMouseOverN();         
            if(mouseOverN!=null) {
                tgPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                docGBPanel.setStatusBarText("[Node under pointer]      " + mouseOverN.toSBText());
            }
            else {
            	tgPanel.setCursor(null);
                DocNode currentNode = (DocNode) docGBPanel.getTGPanel().getSelect();
                String SBText = "Ready";
                if (currentNode != null) SBText = "[Selected node]      " + currentNode.toSBText();
                docGBPanel.setStatusBarText(SBText);
                /*
                tgPanel.setCursor(null);
                // DocNode currentNode = (DocNode) docGBPanel.getTGPanel().getSelect();
                Object currentNode = docGBPanel.getTGPanel().getSelect();
                String SBText = "Ready";
                if (currentNode != null)
                {
                	if (currentNode instanceof DocNode)
                		SBText = "[Selected node]      " + ((DocNode)currentNode).toSBText();
                	else if (currentNode instanceof DocEdge)
                		SBText = "[Selected edge]      " + ((DocEdge)currentNode).toSBText();
                }
                docGBPanel.setStatusBarText(SBText);
                */
            }
        }
        
        public void mouseDragged( MouseEvent e ) {
            DocNode mouseOverN = (DocNode) tgPanel.getMouseOverN();         
            if(mouseOverN!=null) {
                tgPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                docGBPanel.setStatusBarText("[Node under pointer]      " + mouseOverN.toSBText());
            }
            else {
                tgPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }
        }
    }
    
    private void setUpNodePopup() {
        nodePopup = new JPopupMenu();
        JMenuItem menuItem;
        
        menuItem = new JMenuItem("Expand node");
        ActionListener expandAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(popupNode!=null) {
                        tgPanel.expandNode(popupNode);
                    }
                }
            };
        menuItem.addActionListener(expandAction);
        nodePopup.add(menuItem);
        
        menuItem = new JMenuItem("Collapse node");
        ActionListener collapseAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {                    
                    if(popupNode!=null) {
                        tgPanel.collapseNode(popupNode );
                    }
                }
            };
        menuItem.addActionListener(collapseAction);
        nodePopup.add(menuItem);

        menuItem = new JMenuItem("Hide node");
        ActionListener hideAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {                    
                    if(popupNode!=null) {
                        tgPanel.hideNode(popupNode);
                    }
                }
            };
        menuItem.addActionListener(hideAction);
        nodePopup.add(menuItem);
        
        nodePopup.addSeparator();
        //nodePopup.add(new JSeparator());
        
        ActionListener sendAction = null;
        
        // all the node types are listed here but the only types of interest to us
        // are the Group Node and Genre Node.  The others are included here for
        // for completeness, e.g. if TVAeNavigateUI were to be used for additional,
        // new visualizations
        
        /*
        
        if (popupNode instanceof TVAeProgNode)
		{
			String itemText = "Send this programme...";
			menuItem = new TVAeMenuItem(itemText, TVAeImageCache.getImage("icon.send"));
			sendAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {                    
					if(popupNode!=null && popupListenerObject != null) {
						popupListenerObject.popupEvent(popupNode.getID(), 
								TVAePopupListener.POPUP_SEND_PROG);
					}
				}
			};
		}
		else if (popupNode instanceof TVAeGroupNode)
		{
			String itemText = "Send this group...";
			menuItem = new TVAeMenuItem(itemText, TVAeImageCache.getImage("icon.send"));
			sendAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {                    
					if(popupNode!=null && popupListenerObject != null) {
						popupListenerObject.popupEvent(popupNode.getID(), 
								TVAePopupListener.POPUP_SEND_GROUP);
					}
				}
			};
		}
		else if (popupNode instanceof TVAeGenreNode)
		{
			String itemText = "Send this genre...";
			menuItem = new TVAeMenuItem(itemText, TVAeImageCache.getImage("icon.send"));
			sendAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {                    
					if(popupNode!=null && popupListenerObject != null) {
						popupListenerObject.popupEvent(popupNode.getID(), 
								TVAePopupListener.POPUP_SEND_GENRE);
					}
				}
			};
		}
		else if (popupNode instanceof TVAeProgGroupNode)
		{	
			String itemText = "Send all programmes belonging to group...";
			menuItem = new TVAeMenuItem(itemText, TVAeImageCache.getImage("icon.send"));
			sendAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {                    
					if(popupNode!=null && popupListenerObject != null) {
						TVAeProgGroupNode thisGroup = (TVAeProgGroupNode) popupNode;
						for (int n = 0; n < thisGroup.getNumChildren(); n ++)
							popupListenerObject.popupEvent(thisGroup.getChild(n).getID(), 
								TVAePopupListener.POPUP_SEND_PROG);
					}
				}
			};
		}
        
        */
        
		// if the node is not recognized as any of the above
		if (sendAction != null)
		{
			menuItem.addActionListener(sendAction);
			nodePopup.add(menuItem);
			//nodePopup.add(new TVAeMenuSeparator());
			nodePopup.addSeparator();
		}
		
		/*
        menuItem = new TVAeMenuItem("Add Group/Genre Membership", TVAeImageCache.getImage("icon.blank"));
		ActionListener addAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {                    
					if(popupNode!=null) {
						System.out.println("Adding here...");
						popupListenerObject.popupEvent(popupNode.getID(), 
								TVAePopupListener.POPUP_ADD_MEMBER);
						System.out.println("Added...");
						popupNode.setHighlighted(true);
					}
				}
			};
		if(popupNode!=null) {
			menuItem.setEnabled(!(popupNode.getHighlighted()));
		}
		menuItem.addActionListener(addAction);
		nodePopup.add(menuItem);
		
        menuItem = new JMenuItem("Remove Group/Genre Membership")//, TVAeImageCache.getImage("icon.blank"));
		ActionListener removeAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {                    
					if(popupNode!=null) {
						System.out.println("Removing here...");
						popupListenerObject.popupEvent(popupNode.getID(), 
								TVAePopupListener.POPUP_REMOVE_MEMBER);
						System.out.println("Removed...");
						popupNode.setHighlighted(false);
					}
				}
			};
		if(popupNode!=null) {
			menuItem.setEnabled(popupNode.getHighlighted());
		}
		menuItem.addActionListener(removeAction);
		nodePopup.add(menuItem);
        
        */
        
		if (editMode)
		{
			nodePopup.addSeparator();
			menuItem = new JMenuItem("Enter Edit Mode");//, TVAeImageCache.getImage("icon.vis.edit"));
			ActionListener editAction = new ActionListener() {
					public void actionPerformed(ActionEvent e) {                    
						deactivate();
						docGBPanel.tgUIManager.activate("Edit");
					}
				};
			menuItem.addActionListener(editAction);
			nodePopup.add(menuItem);
		}

        nodePopup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                tgPanel.setMaintainMouseOver(false);                
                tgPanel.setMouseOverN(null);
                tgPanel.repaint();  
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        });
        
        //addHelpMenuItem(nodePopup);
    }

    
    private void setUpEdgePopup() {
        edgePopup = new JPopupMenu();
        
        JMenuItem menuItem;
                
        menuItem = new JMenuItem("Hide edge");
        ActionListener hideAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(popupEdge!=null) {
                        tgPanel.hideEdge(popupEdge);
                    }
                }
            };
            
        menuItem.addActionListener(hideAction);
        edgePopup.add(menuItem);        
        
        //addHelpMenuItem(edgePopup);
        
        edgePopup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                tgPanel.setMaintainMouseOver(false);
                tgPanel.setMouseOverE(null);
                tgPanel.repaint();      
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        });
        
        
    }
    
    private void setUpBackPopup() {	
		if (editMode)
		{
	    	backPopup = new JPopupMenu();
	    	JMenuItem menuItem;
			menuItem = new JMenuItem("Enter Edit Mode");
			//menuItem = new JMenuItem("Enter Edit Mode");
			ActionListener editAction = new ActionListener() {
					public void actionPerformed(ActionEvent e) {                    
						deactivate();
						docGBPanel.tgUIManager.activate("Edit");
						// tvaePVPanel.editUI.activate();
					}
				};
			menuItem.addActionListener(editAction);
			backPopup.add(menuItem);
		
			//addHelpMenuItem(backPopup);
			
			backPopup.addPopupMenuListener(new PopupMenuListener() {
	            public void popupMenuCanceled(PopupMenuEvent e) {}
	            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	                tgPanel.setMaintainMouseOver(false);
	                tgPanel.setMouseOverE(null);
	                tgPanel.repaint();      
	            }
	            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
	        });
		}
    }
    
    /*
	public void setPopupListener(TVAePopupListener popupListenerObject)
	{
		this.popupListenerObject = popupListenerObject;
	}
	
    private void addHelpMenuItem(JPopupMenu jpopup)
    {
	    jpopup.add(new TVAeMenuSeparator());
	    TVAeMenuItem menuItem = new TVAeMenuItem("Show context help", TVAeImageCache.getImage("icon.blank"));
	    ActionListener showHelpAction = new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	System.out.println("popupListenerObject.popupEvent");
				popupListenerObject.popupEvent("", 
	    				TVAePopupListener.POPUP_SHOW_HELP);
	        }
		};
	    menuItem.addActionListener(showHelpAction);
	    jpopup.add(menuItem);
    }
	*/
	
	public void setEditMode(boolean b)
	{
		editMode = b;
		setUpBackPopup();
	}
	
	public boolean getEditMode()
	{
		return editMode;
	}
	
}
