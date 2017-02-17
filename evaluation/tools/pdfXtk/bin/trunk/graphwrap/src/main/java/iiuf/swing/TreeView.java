package iiuf.swing;

import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTargetListener;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.ChangeListener;

/**
   Tree view interface.

   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public interface TreeView 
  extends
  ContextMenuEnabled
{
  public void       setModel(TreeModel model, TreePath viewRoot);
  public void       setModel(TreeModel model);
  public TreeModel  getModel();
  public boolean    isVisible(TreePath path);
  public void       makeVisible(TreePath path);
  public TreePath   getSelectionPath();
  public TreePath[] getSelectionPaths();
  public void       setSelectionPath(TreePath path);
  public void       setSelectionPaths(TreePath[] path);
  public void       clearSelection();
  public TreePath   getMostVisiblePath();
  public Object     getViewRoot();
  public void       enableDrag(int sourceActions, DragGestureListener sourceListener); 
  public void       enableDrop(int targetActions, DropTargetListener targetListener);
  public void       disableDrag();
  public void       disableDrop();
  /**
     Adds a change listener to this tree view.
     
     The change listener is called when the root node of the tree model viewed by this tree view changed.
     
     @param l The change listener to add.
  */
  public void       addRootNodeChangeListener(ChangeListener l);
  public void       addRootNodeChangeListener(ChangeListener l, boolean weak);
  public void       removeRootNodeChangeListener(ChangeListener l);
}

/*
  $Log: TreeView.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.8  2001/01/14 13:21:14  schubige
  Win NT update

  Revision 1.7  2001/01/12 09:11:15  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.6  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.5  2001/01/04 17:25:16  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added
*/
