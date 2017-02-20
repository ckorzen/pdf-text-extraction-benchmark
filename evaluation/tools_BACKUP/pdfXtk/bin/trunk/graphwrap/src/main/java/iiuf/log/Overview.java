package iiuf.log;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Date;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.net.InetAddress;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

/**
   Graphical log overview implementation.
   
   Set "log.cleronconnect" property to any value to clear the log each time a new client connection is made.<p>

   (c) 2000, 2001, IIUF, DIUF<p>
      
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Overview 
  extends
  JFrame
  implements
  LogMessageListener
{
  private static final ImageIcon DOC_ICON  = new ImageIcon(Const.class.getResource("doc.gif"));
  private static final Font      MONOSPACE = new Font("Monospaced", Font.PLAIN, 12);
  private static       Font      DEFAULT;
  
  private JTabbedPane threadPanes = new JTabbedPane();
  private HashMap     threads     = new HashMap();
  private ThreadPane  all         = new ThreadPane("All", 0);
  private JTextArea   info        = new JTextArea("", 15, 80);
  
  class ThreadPane
    extends 
    JPanel 
  {
    Priority[] priorities = new Priority[Const.LOG_ALL];
    
    DefaultMutableTreeNode root      = new DefaultMutableTreeNode();
    DefaultTreeModel       treeModel = new DefaultTreeModel(root);
    JTree                  jtree     = new JTree(treeModel);
    TreePath               rootpath  = new TreePath(root);
    
    class TreeRenderer
      extends 
      DefaultTreeCellRenderer 
    {      
      public Component getTreeCellRendererComponent(JTree   tree,
						    Object  value,
						    boolean sel,
						    boolean expanded,
						    boolean leaf,
						    int     row,
						    boolean hasFocus) {
	
	super.getTreeCellRendererComponent(tree, value, sel,
					   expanded, leaf, row,
					   hasFocus);

	if(value instanceof Priority)
	  setIcon(Const.LOG_ICONS[((Priority)value).priority]);
	
	if(value instanceof LogMessageNode && !((LogMessageNode)value).m.exception.equals(""))
	  if(Const.STDOUT.equals(((LogMessageNode)value).m.message) ||
	     Const.STDERR.equals(((LogMessageNode)value).m.message))
	    setIcon(null);
	  else
	    setIcon(DOC_ICON);
	return this;
      }
    }
    
    class Priority    
      extends
      DefaultMutableTreeNode 
    {
      TreeMap map = new TreeMap();
      int     priority;
      
      Priority(int priority_) {
	priority = priority_;
      }
      
      public String toString() {
	return Const.LOG_STRINGS[priority] + " (" + countMsgs() + ")";
      }
      
      int countMsgs() {
	LogMessages[] msgs = (LogMessages[])map.values().toArray(new LogMessages[map.size()]);
	int result = 0;
	for(int i = 0; i < msgs.length; i++)
	  result += msgs[i].getChildCount();
	return result;
      }
      
      LogMessages get(LogMessage m) {
	LogMessages result = (LogMessages)map.get(m.message);
	if(result == null) {
	  result = new LogMessages(m.message);
	  map.put(m.message, result);
	  treeModel.insertNodeInto(result, this, getChildCount());
	}
	return result;
      }
    }
    
    class LogMessageNode
      extends
      DefaultMutableTreeNode 
    {
      LogMessage m;

      LogMessageNode(LogMessage message) {
	m = message;
      }
      
      public String toString() {
	if(Const.STDOUT.equals(m.message) || Const.STDERR.equals(m.message))
	  return m.exception;
	else
	  return new Date(m.time).toString();
      }
    }
    
    class LogMessages 
      extends
      DefaultMutableTreeNode 
    {
      String    message;
      boolean   stdoutExp;
      boolean   stderrExp;
      TreePath  path;
 
      LogMessages(String message_) {
	message = message_;
      }
      
      void add(LogMessage m) {
	if(path == null)
	  path = new TreePath(new Object[] {root, getParent(), this});
	
	LogMessageNode node = new LogMessageNode(m);
	treeModel.insertNodeInto(node, this, getChildCount());
	if(! stdoutExp && Const.STDOUT.equals(m.message)) {
	  jtree.expandPath(path);
	  stdoutExp = true;
	}
	if(! stderrExp && Const.STDERR.equals(m.message)) {
	  jtree.expandPath(path);
	  stderrExp = true;
	}
	treeModel.nodeChanged(this);
	
	if(jtree.isExpanded(path))
	   jtree.scrollPathToVisible(new TreePath(new Object[] {root, getParent(), this, node}));
      }
      
      public String toString() {
	return message + " (" + getChildCount() + ")";
      }
    }

    int    msgCount;
    String name;    
    int    tabIdx;

    ThreadPane(String name_, int tabIdx_) {
      name   = name_;
      tabIdx = tabIdx_;
      setLayout(new BorderLayout());
      setName(name + " (" + msgCount + ")");
      jtree.setRootVisible(false);
      jtree.setShowsRootHandles(true);
      jtree.setCellRenderer(new TreeRenderer());
      jtree.setScrollsOnExpand(true); 
      jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      jtree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
	  public void valueChanged(TreeSelectionEvent e) {
	    if(e.getNewLeadSelectionPath() == null) return;
	    if(e.getNewLeadSelectionPath().getLastPathComponent() instanceof LogMessageNode) {
	      LogMessage m = ((LogMessageNode)e.getNewLeadSelectionPath().getLastPathComponent()).m;
	      info.setText(Const.LOG_STRINGS[m.priority] + " from " + m.thread + " @ " + new Date(m.time).toString() + "\n" +
			   m.message + "\n" + 
			   m.getException());
	    }
	  }
	});
      add(new JScrollPane(jtree), BorderLayout.CENTER);
    }
    
    void add(LogMessage m) {
      Priority priority =  priorities[m.priority];
      if(priority == null) {
	priority = new Priority(m.priority);
	int idx = 0;
	for(int i = 0; i < m.priority; i++)
	  if(priorities[i] != null) idx++;
	priorities[m.priority] = priority;
	treeModel.insertNodeInto(priority, root, idx);
	treeModel.nodeChanged(priority);
      }
      priority.get(m).add(m);
      jtree.expandPath(rootpath);
      msgCount++;
      threadPanes.setTitleAt(tabIdx, name + " (" + msgCount + ")");
    }
  }
  
  public Overview() {
    super("Log overview by thread");
    threadPanes.add(all);
    info.setFont(MONOSPACE);
    getContentPane().add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, threadPanes, new JScrollPane(info)));
    pack();
    setSize(500, 500);
    setVisible(true);
  }

  int tabIdx = 1;

  public synchronized void newConnection(InetAddress host) {
    if(System.getProperty("log.clearonconnect") != null) {
      threads = new HashMap();
      all     = new ThreadPane("All", 0);
      tabIdx = 1;
      threadPanes.removeAll();
      System.gc();
      threadPanes.add(all);
      info.setText("");
    }
  }

  public synchronized void handle(LogMessage m) {
    ThreadPane pane = (ThreadPane)threads.get(m.thread);
    if(pane == null) {
      pane = new ThreadPane(m.thread, tabIdx++);
      threads.put(m.thread, pane);
      threadPanes.add(pane);
    }
    all.add(m);
    pane.add(m);
  }
}

/*
  $Log: Overview.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.9  2001/03/05 17:55:07  schubige
  Still working on soundium properties panel

  Revision 1.8  2001/01/17 09:55:46  schubige
  Logger update

  Revision 1.7  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.6  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.5  2000/10/17 15:35:59  schubige
  Added watcher preferences

  Revision 1.4  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.3  2000/10/09 07:29:15  schubige
  Features, features, features

  Revision 1.2  2000/10/09 06:47:56  schubige
  Updated logger stuff

  Revision 1.1  2000/10/05 14:59:30  schubige
  Added loging stuff
  
*/
