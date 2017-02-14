package iiuf.swing;

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.JTree;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.ComponentUI;

import iiuf.log.Log;
import iiuf.util.Util;
import iiuf.util.ProgressWatcher;

/**
   A progress watcher monitor.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class ProgressMonitor 
  extends
  AutoExpandingJTree
{  
  static class CellRenderer 
    implements
    TreeCellRenderer
  {
    TreeCellRenderer old;
    
    CellRenderer(TreeCellRenderer current) {
      old = current;
    }
    
    public Component getTreeCellRendererComponent(JTree   tree,
						  Object  value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf,
						  int     row,
						  boolean hasFocus) {
      if(value instanceof ProgressWatcher.Operation &&
	 !(value instanceof ProgressWatcher.ThreadInfo)) {
	ProgressWatcher.Operation op = (ProgressWatcher.Operation)value;
	if(op.getUserObject() == null) {
	  JProgressBar pb = new JProgressBar(op.progress);
	  pb.setString(op.description);
	  pb.setStringPainted(true);
	  op.setUserObject(pb);
	}
	return (Component)op.getUserObject();
      }
      else {
	JLabel l = (JLabel)old.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	l.setIcon(Resource.THREAD);
	return l;
      }
    }
  }
  
  private static final BufferedImage EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  
  private TreeNode root;
  public ProgressMonitor() {
    super(ProgressWatcher.getTreeModel());
    setRootVisible(false);
    setShowsRootHandles(false);
    setSelectionModel(null);
    root = (TreeNode)treeModel.getRoot();
    setCellRenderer(new CellRenderer(getCellRenderer()));
    putClientProperty("JTree.lineStyle", "Angled");
    ComponentUI treeUI = getUI();
    EMPTY_IMAGE.setRGB(0, 0, getBackground().getRGB());
    if(treeUI instanceof BasicTreeUI) { 
      ((BasicTreeUI)treeUI).setExpandedIcon(new ImageIcon(EMPTY_IMAGE)); 
      ((BasicTreeUI)treeUI).setCollapsedIcon(new ImageIcon(EMPTY_IMAGE)); 
    } 
  }
}

/*
  $Log: ProgressMonitor.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.3  2001/01/15 15:08:59  schubige
  some sourcewatch bug fixes

  Revision 1.2  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/09 07:53:08  schubige
  early checkin for DCJava
  
*/
