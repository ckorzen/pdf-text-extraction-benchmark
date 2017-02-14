package iiuf.util;

import java.util.HashMap;
import java.util.Enumeration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

import iiuf.swing.ProgressMonitor;

/**
   Progress watcher implementation.
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/

public class ProgressWatcher {

  static class Snapshot {
    String[]   thread;
    String[][] info;
    int[][]    amount;
    int[][]    of;

    Snapshot(int size) {
      thread = new String[size];
      info   = new String[size][];
      amount = new int[size][];
      of     = new int[size][];
    }
    
    public String toString() {
      String result = "";
      for(int i = 0; i < thread.length; i++) {
	if(thread[i] != null) {
	  result += thread[i];
	  for(int j = 0; j < info[i].length; j++)
	    result += "," + info[i][j] + amount[i][j] + "/" + of[i][j];
	  result += "\n";
	} 
      }
      return result;
    }
  }

  static class ProgressState {
    int                  level  = 0;
    ProgressListener[][] pl     = new ProgressListener[0][];
    String[]             info   = new String[0];
    int[]                amount = new int[0];
    int[]                of     = new int[0];
    Thread               thread;
    
    ProgressState(Thread thread_) {
      thread = thread_;
    }
    
    synchronized void install(ProgressListener listener) {
      int l = level + 1;
      ensureLevel(l);
      ProgressListener[] pls = pl[l];
      if(pls == null)
	pl[l] = new ProgressListener[1];
      else {
	pl[l] = new ProgressListener[pls.length + 1];
	System.arraycopy(pls, 0, pl[l], 1, pls.length);
      }
      pl[l][0] = listener;
    }
    
    private void ensureLevel(int level) {
      if(level > pl.length - 1) {
	ProgressListener[][] tmp = pl;
	pl = new ProgressListener[level * 2][];
	System.arraycopy(tmp, 0, pl, 0, tmp.length);
	
	String[] stmp = info;
	info = new String[level * 2];
	System.arraycopy(stmp, 0, info, 0, stmp.length);
	
	int[] atmp = amount;
	amount = new int[level * 2];
	System.arraycopy(atmp, 0, amount, 0, atmp.length);
	
	int[] otmp = of;
	of = new int[level * 2];
	System.arraycopy(otmp, 0, of, 0, otmp.length);
      } 
    }
    
    synchronized ProgressListener[] getListeners() {
      ensureLevel(level + 1);
      return pl[level];
    }
    
    synchronized void setInfo(String info_, int amount_, int of_) {
      if(level < info.length) {
	if(info_ != null) 
	  info[level] = info_;
	
	amount[level] = amount_;
	of[level]     = of_;
      }
    }
    
    synchronized void removeListeners() {
      pl[level--] = null;
    }
    
    synchronized void fillSnapshot(Snapshot snap, int idx) {
      if(level <= 0) return;

      snap.thread[idx] = thread.getName();
      
      snap.info[idx] = new String[level];
      System.arraycopy(info, 1, snap.info[idx], 0, level);
      
      snap.amount[idx] = new int[level];
      System.arraycopy(amount, 1, snap.amount[idx], 0, level);
      
      snap.of[idx] = new int[level];
      System.arraycopy(of, 1, snap.of[idx], 0, level);
    }
  }
  
  private static HashMap   states  = new HashMap();
  private static ArrayList statesa = new ArrayList();
  private static Object    lock    = new Object();

  public static void watch(ProgressListener listener) {
    getState().install(listener);
  }
  
  public static int getLevel() {
    return getState().level;
  }
  
  /**
     Call this method at the beginning of a lengthy operation.
     
     @param description A short description of the operation (one word if possible).
     @param callsProgress Pass true if you will call ProgressWatcher.progress yourself
     during the operation. Pass false if the progress watcher should call ProgressWatcher.progress automatically.
  */
  public static void start(String description, boolean callsProgress) {
    ProgressListener[] pl = getListeners(true, description, 0, 0);
    if(pl == null) {
      if(treeModel != null)
	treeModel.trigger();
      return;
    }
    for(int i = 0; i < pl.length; i++) {
      pl[i].operationStart(description);
      if(!callsProgress)
	pl[i].operationProgress(0, 1);
    }
    if(treeModel != null)
      treeModel.trigger();
  }
  
  /**
     Call this method at the end of a lengthy operation.
     
     @param callsProgress Pass true if you have called ProgressWatcher.progress yourself during the operation. 
     Pass false if the progress watcher called ProgressWatcher.progress automatically. 
     The value must be the same as given at the corresponding ProgressWatcher.start call.
  */
  public static void stop(boolean callsProgress) {
    ProgressListener[] pl = getListeners(false, null, -1, -1);
    if(pl == null) {
      getState().level--;
      if(treeModel != null)
	treeModel.trigger();
      return;
    }
    for(int i = 0; i < pl.length; i++) {   
      if(!callsProgress)
	pl[i].operationProgress(1, 1);
      pl[i].operationStop();
    }
    getState().removeListeners();
    if(treeModel != null)
      treeModel.trigger();
  }
  
  /**
     Call this method during a lengthy operation.
     
     @param amount The amount of work already done. Amount should start with zero and end with 
     the job size (the <code>of</code> argument).
     @param of     The job-size.
  */
  public static void progress(int amount, int of) {
    ProgressListener[] pl = getListeners(false, null, amount, of);
    if(pl == null) {
      if(treeModel != null)
	treeModel.trigger();
      return;
    }
    for(int i = 0; i < pl.length; i++)
      pl[i].operationProgress(amount, of);
    if(treeModel != null)
      treeModel.trigger();
    Thread.yield();
  }
  
  private synchronized static ProgressState getState() {
    ProgressState result = (ProgressState)states.get(Thread.currentThread());
    if(result == null) {
      result = new ProgressState(Thread.currentThread());
      states.put(Thread.currentThread(), result);
      statesa.add(result);
    }
    return result;
  }
  
  private static ProgressListener[] getListeners(boolean inc, String info, int amount, int of) {
    ProgressListener[] result = null;
    ProgressState state = getState();
    if(inc) state.level++;
    result = state.getListeners();
    state.setInfo(info, amount, of);
    return result;
  }
  
  static synchronized Snapshot getSnapshot() {
    ProgressState[] states = (ProgressState[])statesa.toArray(new ProgressState[statesa.size()]);
    Snapshot result = new Snapshot(states.length);
    for(int i = 0; i < states.length; i++)
      states[i].fillSnapshot(result, i);
    return result;
  }
  
  // -------------------------------- tree model
  
  public static class Operation
    extends
    DefaultMutableTreeNode 
  {
    public    String                   description;
    public    int                      amount;
    public    int                      of;
    public    DefaultBoundedRangeModel progress = new DefaultBoundedRangeModel();
    
    public Operation(String description_) {
      description = description_;
    }
    
    public String toString() {
      return description;
    }
    
    public boolean set(String description_, int amount_, int of_) {
      boolean result = false;
      if(description_ != null && !(description_.equals(description))) {
	description = description_;
	setUserObject(null);
	result = true;
      }
      
      if(amount != amount_ || of != of_) {
	amount = amount_;
	of     = of_;
	progress.setRangeProperties(amount, 0, 0, of - 1, false);
	result = true;
      }

      return result;
    }
    
    Operation getChild() {
      return getChildCount() == 0 ? null : (Operation)getFirstChild();
    }
  }
  
  public static class ThreadInfo 
    extends 
    Operation 
  {
    ThreadInfo(String info) {
      super(info);
    }
  }
  
  static class RootNode
    extends
    DefaultMutableTreeNode
  {
    RootNode() {super("ProgressWatcher");}
  }
  
  static class PWTreeModel 
    extends
    DefaultTreeModel
    implements
    Runnable
  {
    RootNode root;
    boolean  triggered;

    PWTreeModel() {
      super(new RootNode());
      root = (RootNode)getRoot();
      try{SwingUtilities.invokeLater(this);}
      catch(Exception e) {Util.printStackTrace(e);}      
    }
    
    public synchronized void trigger() {
      if(!triggered && listenerList.getListenerCount() > 0) {
	triggered = true;
	try{SwingUtilities.invokeLater(this);}
	catch(Exception e) {Util.printStackTrace(e);}      
      }
    }
    
    public synchronized void run() {
      triggered = false;
      update(getSnapshot());
    }
    
    void update(ThreadInfo node, String[] nInfo, int[] nAmount, int[] nOf) {
      Operation oldop = node;
      Operation op    = oldop.getChild();
      
      for(int i = 0; i < nInfo.length; i ++) {
	if(op == null) {
	  op = new Operation(nInfo[i]);
	  oldop.add(op);
	  nodeStructureChanged(oldop);
	}
	
	if(op.set(nInfo[i], nAmount[i], nOf[i]))
	  nodeChanged(op);

	oldop = op;
	op    = oldop.getChild();
      }
      if(op != null) {
	oldop.remove(op);
	nodeStructureChanged(oldop);
      }
    }
        
    ThreadInfo getThreadInfo(String thread) {
      for(int i = 0; i < root.getChildCount(); i++)
	if(root.getChildAt(i).toString().equals(thread))
	  return (ThreadInfo)root.getChildAt(i);
      return null;
    }
    
    void update(String nThread, String[] nInfo, int[] nAmount, int[] nOf) {
      ThreadInfo node = getThreadInfo(nThread);
      if(node == null) {
	node = new ThreadInfo(nThread);
	insertNodeInto(node, root, 0);
      }
      update(node, nInfo, nAmount, nOf);
    }
    
    void update(Snapshot snap) {
      // System.out.println(snap);
      // remove old, unused nodes
      for(int i = 0; i < root.getChildCount();) {
	if(!Strings.contains(snap.thread, root.getChildAt(i).toString())) {
	  removeNodeFromParent((Operation)root.getChildAt(i));
	  continue;
	}
	i++;
      }
      
      if(root.getChildCount() == 0)
	nodeStructureChanged(root);
      
      // update old nodes & add new nodes
      for(int i = 0; i < snap.thread.length; i++)
	if(snap.thread[i] != null)
	  update(snap.thread[i], snap.info[i], snap.amount[i], snap.of[i]);
    }
  }
  
  private static PWTreeModel treeModel;
  
  public static TreeModel getTreeModel() {
    if(treeModel == null)
      treeModel = new PWTreeModel();
    return treeModel;
  }
  
  // -------------------------------- test stuff
  
  private static final int NUM_TESTS  = 7;
  private static final int DELAY      = 1;
  private static final int NUM_THREAD = 5;
  
  static void f() {
    ProgressWatcher.start("f", true);
    for(int i = 0; i < NUM_TESTS; i++) {
      ProgressWatcher.progress(i, NUM_TESTS);
      Util.delay(1000);
    }
    ProgressWatcher.stop(true);
  }
  
  static void frecr(int lvl) {
    if(lvl == 0) return;
    /*
    if(lvl == 1) {
      watch(new ProgressListener() {
	  public void operationStart(String description) {
	    System.out.println("@" + ProgressWatcher.getLevel() + "start:" + description);
	  }
	  public void operationProgress(int amount, int of) {
	    System.out.println("@" + ProgressWatcher.getLevel() + "progress:" + amount + "/" + of);
	  }
	  public void operationStop() {
	  System.out.println("@" + ProgressWatcher.getLevel() + "stop");
	  }  
	});
    }
    */
    ProgressWatcher.start("frecr" + lvl, true);
    for(int i = 0; i < NUM_TESTS; i++) {
      ProgressWatcher.progress(i, NUM_TESTS);
      Util.delay(DELAY);
      frecr(lvl - 1);
    }
    ProgressWatcher.stop(true);
  }
  
  static private void testWatch(int func) {
    /*
    watch(new ProgressListener() {
	public void operationStart(String description) {
	  System.out.println("@" + ProgressWatcher.getLevel() + "start:" + description);
	}
	public void operationProgress(int amount, int of) {
	  System.out.println("@" + ProgressWatcher.getLevel() + "progress:" + amount + "/" + of);
	}
	public void operationStop() {
	  System.out.println("@" + ProgressWatcher.getLevel() + "stop");
	}
	
      });
    */
    switch(func) {
    case 0: System.out.println("non recr"); f();      break;
    case 1: System.out.println("recr");     frecr(2 + Util.intRandom(3)); break;
    }
  }
  
  private static void testWatch2(int f) {
    testWatch(f);
  }
  
  public static void main(String[] argv) {
    JFrame monitor = new JFrame("Progress Monitor");
    
    monitor.getContentPane().add(new ProgressMonitor());
    monitor.setSize(500, 500);
    monitor.setVisible(true);
    monitor.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {System.exit(0);}
      });
    
    for(int j = 0; j < NUM_THREAD; j++) {
      new Thread("Thread " + j) {
	  public void run() {
	    for(int i = 0; i < 2; i++) {
	      System.out.print("Test non nested "); testWatch(i);
	      System.out.print("Test nested ");     testWatch2(i);
	    } 
	  }
	}.start();
    }
  }
}

/*
  $Log: ProgressWatcher.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.7  2001/02/21 10:02:10  schubige
  fixed treeModel null-pointer bug in ProgressWatcher

  Revision 1.6  2001/02/21 08:49:35  schubige
  ProgressWatcher lazy tree model creation

  Revision 1.5  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/11/27 16:10:45  schubige
  tinja IDE beta 2

  Revision 1.3  2000/11/24 17:50:44  schubige
  Tinja IDE beta 1

  Revision 1.2  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.1  2000/08/17 16:22:15  schubige
  Swing cleanup & TreeView added
  
*/
