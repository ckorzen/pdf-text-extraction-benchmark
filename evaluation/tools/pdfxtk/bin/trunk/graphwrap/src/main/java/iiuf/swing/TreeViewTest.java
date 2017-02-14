package iiuf.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.awt.dnd.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.MetalIconFactory;

import iiuf.awt.Awt;
import iiuf.swing.Swing;
import iiuf.swing.SplitPaneTreeView;
import iiuf.swing.ChooserTreeView;
import iiuf.swing.JTreeView;
import iiuf.swing.TreeView;
import iiuf.util.Util;

public class TreeViewTest 
  extends 
  JFrame
  implements
  DragGestureListener,
  DragSourceListener,
  DropTargetListener
{
  static int                    counter;
  static DefaultMutableTreeNode root  = new DefaultMutableTreeNode("root");
  static DefaultMutableTreeNode child = new DefaultMutableTreeNode("child");
  static DefaultTreeModel       model = new DefaultTreeModel(root);
  
  static {
    model.insertNodeInto(child, root, 0);
  }
  
  TreeView view;
  
  TreeViewTest(TreeView view_) {
    view = view_;
    getContentPane().add((Component)view);
    view.enableDrag(DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK, this);
    view.enableDrop(DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK, this);
    pack();
  }
  
  static BufferedImage FILE;
  static BufferedImage FOLDER;
  
  public void dragGestureRecognized(DragGestureEvent e) {
    Object o = view.locationToObject(e.getComponent(), e.getDragOrigin());
    if(o != null) {
      if(e.getDragSource().isDragImageSupported()) {
	if(FILE == null) {
	  FILE   = Swing.iconToImage(MetalIconFactory.getTreeLeafIcon());
	  FOLDER = Swing.iconToImage(MetalIconFactory.getTreeFolderIcon());
	}
	BufferedImage image = model.isLeaf(o) ? FILE : FOLDER;
	e.startDrag(DragSource.DefaultMoveDrop,
		    image,
		    new Point(image.getWidth() / 2, image.getHeight() / 2),
		    new StringSelection(o.toString()),
		    this);
      } else
	e.startDrag(DragSource.DefaultMoveDrop, // cursor
		    new StringSelection(o.toString()), // transferable
		    this); // drag source listener
    }
  }

  public void dragDropEnd(DragSourceDropEvent e) {System.out.println(e);}
  public void dragEnter(DragSourceDragEvent e) {System.out.println(e);}
  public void dragExit(DragSourceEvent e) {System.out.println(e);}
  public void dragOver(DragSourceDragEvent e) {System.out.println(e);}
  public void dropActionChanged(DragSourceDragEvent e) {System.out.println(e);}

  public void drop(DropTargetDropEvent e) {
    System.out.println(e);
    System.out.println(e.getTransferable());
    e.acceptDrop(DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK);
    e.dropComplete(true);
  }
  public void dragEnter(DropTargetDragEvent e) {System.out.println(e);}
  public void dragExit(DropTargetEvent e) {System.out.println(e);}
  public void dragOver(DropTargetDragEvent e) {System.out.println(e);}
  public void dropActionChanged(DropTargetDragEvent e) {System.out.println(e);}

  static DefaultMutableTreeNode getRandomNode() {
    int count = 0;
    for(Enumeration e = root.depthFirstEnumeration(); e.hasMoreElements(); count++)
      e.nextElement();
    count = Util.intRandom(count);
    
    Enumeration e = root.depthFirstEnumeration();
    for(; count > 0; count--)
      e.nextElement();
    return (DefaultMutableTreeNode)e.nextElement();
  }
  
  static TreeViewTest show(JFrame f, int x, int y, int w, int h) {
    f.setBounds(x, y, w, h);
    f.show();
    return f instanceof TreeViewTest ? (TreeViewTest)f : null;
  }
  
  static TreeView[] views = new TreeView[3];
  
  public static void main(String[] argv) {
    int h = 400;
    int w = 1280 / 3;
    int x = 0;
    views[0] = show(new TreeViewTest(new JTreeView(model)), x, 0, w, h).view;
    x += w;
    views[1] = show(new TreeViewTest(new SplitPaneTreeView(model, 3).setResizePolicy(SplitPaneTreeView.KEEP_PANE_WIDTH)), x, 0, w, h).view;
    x += w;
    views[2] = show(new TreeViewTest(new ChooserTreeView(model)), x, 0, w, h).view;
    show(new Buttons(), 0, 700, 200, 300);
  }
}

class Buttons extends JFrame 
  implements
  DropTargetListener
{
  boolean stress;

  Buttons() {
    getContentPane().setLayout(new FlowLayout());
    getContentPane().add(Swing.newButton("Add 10", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  for(int i = 0; i < 10; i++)
	    TreeViewTest.model.insertNodeInto(new DefaultMutableTreeNode(new Integer(TreeViewTest.counter++)), TreeViewTest.getRandomNode(), 0);
	}
      }));
    getContentPane().add(Swing.newButton("Selection", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  for(int i = 0; i < TreeViewTest.views.length; i++) {
	    TreePath[] sel = TreeViewTest.views[i].getSelectionPaths();
	    for(int j = 0; j < sel.length; j++)
	      System.out.println(i + ":" + sel[j]);
	  } 
	}
      }));
    getContentPane().add(Swing.newButton("Sync Selection", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  for(int i = 0; i < TreeViewTest.views.length; i++)
	    for(int j = 0; j < TreeViewTest.views.length; j++)
	      if(j != i)
		TreeViewTest.views[j].setSelectionPaths(TreeViewTest.views[i].getSelectionPaths());
	}
      }));
    
    getContentPane().add(Swing.newButton("Clear Selection", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  for(int j = 0; j < TreeViewTest.views.length; j++)
	    TreeViewTest.views[j].clearSelection();
	}
      }));
    
    getContentPane().add(Swing.newButton("Change Root", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  
	  TreePath p = null;
	  do {
	    p = new TreePath(TreeViewTest.getRandomNode().getPath());
	  } while(TreeViewTest.model.isLeaf(p.getLastPathComponent()));
	  
	  for(int j = 0; j < TreeViewTest.views.length; j++)
	    TreeViewTest.views[j].setModel(TreeViewTest.model, p);
	}
      }));
    
    getContentPane().add(Swing.newButton("Delete Selection", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  HashSet nodes = new HashSet();
	  for(int i = 0; i < TreeViewTest.views.length; i++) {
	    TreePath[] sel = TreeViewTest.views[i].getSelectionPaths();
	    for(int j = 0; j < sel.length; j++)
	      nodes.add(sel[j].getLastPathComponent());
	  }
	  Object[] nodesa = nodes.toArray();
	  for(int i = 0; i < nodesa.length; i++)
	    TreeViewTest.model.removeNodeFromParent((MutableTreeNode)nodesa[i]);
	}
      }));
    
    getContentPane().add(Swing.newButton("Stress Test", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  stress = !stress;
	}
      }));
    
    JLabel dropTarget = new JLabel("DropTarget");
    dropTarget.setBorder(new BevelBorder(BevelBorder.RAISED));
    getContentPane().add(dropTarget);
    
    new DropTarget(dropTarget, DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK, this);
    pack();
  }
  
  public void drop(DropTargetDropEvent e) {
    System.out.println(e);
    System.out.println(e.getTransferable());
    e.acceptDrop(DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK);
    e.dropComplete(true);
  }
  public void dragEnter(DropTargetDragEvent e) {System.out.println(e);}
  public void dragExit(DropTargetEvent e) {System.out.println(e);}
  public void dragOver(DropTargetDragEvent e) {System.out.println(e);}
  public void dropActionChanged(DropTargetDragEvent e) {System.out.println(e);}
}

