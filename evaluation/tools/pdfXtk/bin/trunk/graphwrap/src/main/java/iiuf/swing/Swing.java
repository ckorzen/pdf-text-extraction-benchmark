package iiuf.swing;

import java.awt.Container;
import java.awt.Component;
import java.awt.MenuComponent;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToolTip;
import javax.swing.Action;
import javax.swing.table.TableColumn;
import javax.swing.tree.ExpandVetoException;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

import iiuf.awt.Awt;
import iiuf.util.AsyncAccelerator;
import iiuf.util.AsyncInvocation;

/**
   Swing utilities, intended be similar to iiuf.awt.Awt.<p>
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
   @see iiuf.awt.Awt
*/
public class Swing {
  
  public static JComponent newJComponent() {
    return new JComponent() {};
  }

  private static final Component emptyCmp = Awt.newComponent();
  public static BufferedImage iconToImage(Icon icon) {
    BufferedImage result = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    icon.paintIcon(emptyCmp, result.getGraphics(), 0, 0);
    return result;
  }
  
  public static JCheckBox newCheckBox(String label, ItemListener listener) {
    JCheckBox result = new JCheckBox(label) {
	public JToolTip createToolTip() {
	  MultiLineToolTip result = new MultiLineToolTip();
	  result.setComponent(this);
	  return result;
	}
      };
    result.addItemListener(listener);
    return result;
  }

  public static JButton newButton(String label, ActionListener listener) {
    return newButton(label, label, listener);
  }
  
  public static JButton newButton(Icon icon, ActionListener listener) {
    return newButton(null, null, icon, listener);
  }

  public static JButton newButton(String label, Icon icon, ActionListener listener) {
    return newButton(label, label, icon, listener);
  }
  
  public static JButton newButton(String label, String command, ActionListener listener) {
    return newButton(label, command, null, listener);
  }
  
  public static JButton newButton(String label, String command, Icon icon, ActionListener listener) {
    JButton result = new JButton(label, icon) {
	public JToolTip createToolTip() {
	  MultiLineToolTip result = new MultiLineToolTip();
	  result.setComponent(this);
	  return result;
	}
      };
    result.addActionListener(listener);
    if(command != null)
      result.setActionCommand(command);
    return result;
  }
  
  public static JMenuItem newMenuItem(String label, ActionListener listener) {
    return newMenuItem(label, listener, null);
  }
  
  public static JRadioButtonMenuItem newRadioButtonMenuItem(String label, ButtonGroup bg, ActionListener listener) {
    return newRadioButtonMenuItem(label, bg, listener, null);
  }
  
  public static JCheckBoxMenuItem newCheckBoxMenuItem(String label, ActionListener listener) {
    return newCheckBoxMenuItem(label, listener, null, false);
  }

  public static JCheckBoxMenuItem newCheckBoxMenuItem(String label, ActionListener listener, boolean state) {
    return newCheckBoxMenuItem(label, listener, null, state);
  }
  
  public static JMenuItem newMenuItem(String label, ActionListener listener, KeyStroke ks) {
    JMenuItem result = new JMenuItem(label) {
	public JToolTip createToolTip() {
	  MultiLineToolTip result = new MultiLineToolTip();
	  result.setComponent(this);
	  return result;
	}
      };
    result.addActionListener(listener);
    if(ks != null) result.setAccelerator(ks);
    return result;
  }
  
  public static JMenuItem newMenuItem(Action action, KeyStroke ks) {
    JMenuItem result = new JMenuItem(action) {
	public JToolTip createToolTip() {
	  MultiLineToolTip result = new MultiLineToolTip();
	  result.setComponent(this);
	  return result;
	}
      };
    if(ks != null) result.setAccelerator(ks);
    return result;
  }
  
  public static JRadioButtonMenuItem newRadioButtonMenuItem(String label, ButtonGroup bg, ActionListener listener, KeyStroke ks) {
    JRadioButtonMenuItem result = new JRadioButtonMenuItem(label) {
	public JToolTip createToolTip() {
	  MultiLineToolTip result = new MultiLineToolTip();
	  result.setComponent(this);
	  return result;
	}
      };
    bg.add(result);
    if(bg.getSelection() == null)
      bg.setSelected(result.getModel(), true);
    result.addActionListener(listener);
    if(ks != null) result.setAccelerator(ks);
    return result;
  }
  
  public static JCheckBoxMenuItem newCheckBoxMenuItem(String label, ActionListener listener, KeyStroke ks) {
    return newCheckBoxMenuItem(label, listener, ks, false);
  }
  
  public static JCheckBoxMenuItem newCheckBoxMenuItem(String label, ActionListener listener, KeyStroke ks, boolean state) {
    JCheckBoxMenuItem result = new JCheckBoxMenuItem(label, state) {
	public JToolTip createToolTip() {
	  MultiLineToolTip result = new MultiLineToolTip();
	  result.setComponent(this);
	  return result;
	}
      };
    result.addActionListener(listener);
    if(ks != null) result.setAccelerator(ks);
    return result;
  }
  
  /*
   * Creates a new EventDispatchThread to dispatch events from. This
   * method returns when stopModal is invoked.
   */
  public static void startModal(javax.swing.JInternalFrame f) {
    synchronized(f) {
      /* Since all input will be blocked until this dialog is dismissed,
       * make sure its parent containers are visible first (this component
       * is tested below).  This is necessary for JApplets, because
       * because an applet normally isn't made visible until after its
       * start() method returns -- if this method is called from start(),
       * the applet will appear to hang while an invisible modal frame
       * waits for input.
       */
      
      if (f.isVisible() && !f.isShowing()) {
	Container parent = f.getParent();
	while (parent != null) {
	  if (parent.isVisible() == false) {
	    parent.setVisible(true);
	  }
	  parent = parent.getParent();
	}
      }

      try {
	if (SwingUtilities.isEventDispatchThread()) {
	  EventQueue theQueue = f.getToolkit().getSystemEventQueue();
	  while (f.isVisible()) {
	    // This is essentially the body of EventDispatchThread
	    AWTEvent event = theQueue.getNextEvent();
	    Object src = event.getSource();
	    // can't call theQueue.dispatchEvent, so I pasted it's body here
	    /*if (event instanceof ActiveEvent) {
	      ((ActiveEvent) event).dispatch();
	      } else */ if (src instanceof Component) {
		((Component) src).dispatchEvent(event);
	      } else if (src instanceof MenuComponent) {
		((MenuComponent) src).dispatchEvent(event);
	      } else {
		System.err.println("unable to dispatch event: " + event);
	      }
	  }
	} else
	  while (f.isVisible())
	    f.wait();
      } catch(InterruptedException e){}
    }
  }

  /*
   * Stops the event dispatching loop created by a previous call to
   * <code>startModal</code>.
   */
  public static void stopModal(javax.swing.JInternalFrame f) {
    synchronized(f) {
      f.notifyAll();
      f.getDesktopPane().remove(f);
    }
  }
  
  public static void setWidth(TableColumn column, int width) {
    column.setPreferredWidth(width);   
    column.setMinWidth(width);
    column.setMaxWidth(width);
  }

  // -------------------- async stuff
  
  private static boolean check(Method m, String method, Class argType) {
    return 
      m.getName().equals(method) && 
      argType == m.getParameterTypes()[0] &&
      m.getParameterTypes().length == 1;
  }
  
  static {
    Awt.addAsyncAccelerator(new AsyncAccelerator() {
	public void handle(AsyncInvocation.Invocation i, AsyncInvocation.Result result) {
	  if(i.object instanceof ListSelectionListener) {
	    ListSelectionListener ll = (ListSelectionListener)i.object;
	    if(check(i.method, "valueChanged", ListSelectionEvent.class)) ll.valueChanged((ListSelectionEvent)i.args[0]);
	    else throw new IllegalArgumentException("Unknown ListSelectionListener method:" + i.method);
	    result.set(null);
	  }
	  
	  else if(i.object instanceof TreeModelListener) {
	    TreeModelListener tl = (TreeModelListener)i.object;
	    if(check(i.method, "treeNodesChanged", TreeModelEvent.class)) 
	      tl.treeNodesChanged((TreeModelEvent)i.args[0]);
	    else if(check(i.method, "treeNodesInserted", TreeModelEvent.class))
	      tl.treeNodesInserted((TreeModelEvent)i.args[0]);
	    else if(check(i.method, "treeNodesRemoved", TreeModelEvent.class))
	      tl.treeNodesRemoved((TreeModelEvent)i.args[0]);
	    else if(check(i.method, "treeStructureChanged", TreeModelEvent.class))
	      tl.treeStructureChanged((TreeModelEvent)i.args[0]);
	    else throw new IllegalArgumentException("Unknown TreeModelListener method:" + i.method);
	    result.set(null);
	  }
	  
	  else if(i.object instanceof TreeWillExpandListener) {
	    TreeWillExpandListener tl = (TreeWillExpandListener)i.object;
	    try {
	      if(check(i.method, "treeWillCollapse", TreeExpansionEvent.class))
		tl.treeWillCollapse((TreeExpansionEvent)i.args[0]);
	      else if(check(i.method, "treeWillExpand", TreeExpansionEvent.class))
		tl.treeWillExpand((TreeExpansionEvent)i.args[0]);
	      else throw new IllegalArgumentException("Unknown TreeWillExpandListener method:" + i.method);
	      result.set(null);
	    } catch(ExpandVetoException ex) {result.setException(ex);}
	  }
	}
      });
  }
  
  private static ListSelectionListener tmpListSelectionListener;
  public static synchronized ListSelectionListener asyncWrapper(ListSelectionListener listener_) {
    tmpListSelectionListener = listener_;
    
    return new ListSelectionListener() {
	ListSelectionListener listener = tmpListSelectionListener;
	
	public void valueChanged(ListSelectionEvent e) {Awt.invokeAsync(listener, "valueChanged", ListSelectionEvent.class, e);}
      };
  }

  private static TreeModelListener tmpTreeModelListener;
  public static synchronized TreeModelListener asyncWrapper(TreeModelListener listener_) {
    tmpTreeModelListener = listener_;
    
    return new TreeModelListener() {
	TreeModelListener listener = tmpTreeModelListener;
	
	public void treeNodesChanged(TreeModelEvent e) {
	  Awt.invokeAsync(listener, "treeNodesChanged", TreeModelEvent.class, e);}
	public void treeNodesInserted(TreeModelEvent e) {
	  Awt.invokeAsync(listener, "treeNodesInserted", TreeModelEvent.class, e);}
	public void treeNodesRemoved(TreeModelEvent e) {
	  Awt.invokeAsync(listener, "treeNodesRemoved", TreeModelEvent.class, e);}
	public void treeStructureChanged(TreeModelEvent e) {
	  Awt.invokeAsync(listener, "treeStructureChanged", TreeModelEvent.class, e);}
      };
  }

  private static TreeWillExpandListener tmpTreeWillExpandListener;
  public static synchronized TreeWillExpandListener asyncWrapper(TreeWillExpandListener listener_) {
    tmpTreeWillExpandListener = listener_;
    
    return new TreeWillExpandListener() {
	TreeWillExpandListener listener = tmpTreeWillExpandListener;
	
	public void treeWillCollapse(TreeExpansionEvent e){
	  Awt.invokeAsync(listener, "treeWillCollapse", TreeExpansionEvent.class, e);}
	public void treeWillExpand(TreeExpansionEvent e) {
	  Awt.invokeAsync(listener, "treeWillExpand", TreeExpansionEvent.class, e);}
      };
  }
  
}

/*
  $Log: Swing.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.13  2001/03/30 17:33:25  schubige
  modified beat soundlet

  Revision 1.12  2001/03/02 17:51:16  schubige
  Enhanced sourcewatch and worked on soundium properties panel

  Revision 1.11  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.10  2001/02/14 17:25:37  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.9  2001/02/12 17:50:05  schubige
  still working on soundium gui

  Revision 1.8  2001/02/09 17:34:16  schubige
  working on soundium

  Revision 1.7  2001/01/14 13:21:14  schubige
  Win NT update

  Revision 1.6  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.4  2000/11/20 17:36:57  schubige
  tinja project ide

  Revision 1.3  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added

  Revision 1.1  2000/07/28 12:07:58  schubige
  Graph stuff update
  
*/
