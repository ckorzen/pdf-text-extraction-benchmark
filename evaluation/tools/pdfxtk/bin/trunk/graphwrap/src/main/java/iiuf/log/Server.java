package iiuf.log;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.LinkedList;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import iiuf.util.ClusteringQueue;
import iiuf.util.Preferences;
import iiuf.util.FilePreferences;
import iiuf.util.EventListenerList;

/**
   Log server implementation.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Server 
  extends
  Thread 
{
  private LinkedList        backlog    = new LinkedList();
  private EventListenerList listeners  = new EventListenerList();
  private ServerSocket      socket;
  private boolean           inited;
  
  private ClusteringQueue   msgQueue = new ClusteringQueue("server msg queue", Thread.MAX_PRIORITY) {
      public boolean handle(Object[] o) {
	LogMessageListener[] l = (LogMessageListener[])listeners.getListeners(LogMessageListener.class);
	for(int j = 0; j < o.length; j++)
	  for(int i = 0; i < l.length; i++)
	    l[i].handle((LogMessage)o[j]);
	return true;
      }
    };
  
  private Class[] c = {Stdout.class, List.class, Overview.class};

  public Server(int port) throws IOException {
    super("Log Server");
    socket = new ServerSocket(port);
    start();
  }
  
  class Connection 
    extends
    Thread
  {
    ObjectInputStream in;
    Socket            socket;
    
    Connection(Socket socket_) {
      super(socket_.getInetAddress().getHostName());
      socket = socket_;
      start();
    }
    
    public void run() {
      process(new LogMessage(Thread.currentThread(), Const.LOG_INFO, "Got connection:" + socket));
      try {
	in = new ObjectInputStream(socket.getInputStream());
	LogMessageListener[] l = (LogMessageListener[])listeners.getListeners(LogMessageListener.class);
	for(int i = 0; i < l.length; i++)
	  l[i].newConnection(socket.getInetAddress());
	for(;;) {
	  try {
	    process((LogMessage)in.readObject());
	  } catch(EOFException eof) {
	    socket.close();
	    return;
	  }
	}
      }
      catch(Exception e) {
	process(new LogMessage(Thread.currentThread(), Const.LOG_ERR, "Connection error (read)", e));
      }      
    }
  }
  
  public void run() {
    process(new LogMessage(Thread.currentThread(), Const.LOG_INFO, "Server running."));
    for(;;) {
      try {new Connection(socket.accept());}
      catch(Exception e) {
	process(new LogMessage(Thread.currentThread(), Const.LOG_ERR, "Connection error (accept)", e));
      }
    }
  }
  
  public synchronized void process(LogMessage m) {
    if(m.time == 0) m.time = System.currentTimeMillis();
    if(!inited) {
      backlog.add(m);
      return;
    } else
      msgQueue.put(m);
  }
  
  public void addLogMessageListener(LogMessageListener listener) {
    listeners.add(LogMessageListener.class, listener);
  }

  public void addLogMessageListener(LogMessageListener listener, boolean weak) {
    listeners.add(LogMessageListener.class, listener, weak);
  }
  
  public void removeLogMessageListener(LogMessageListener listener) {
    listeners.remove(LogMessageListener.class, listener);
  }
  
  private void inited() {
    inited = listeners.getListenerCount(LogMessageListener.class) > 0;
    if(inited && !backlog.isEmpty()) {
      for(int i = 0; i < backlog.size(); i++)
	process((LogMessage)backlog.get(i));
      backlog = null;
    }
  }

  private static void exit() {
    Preferences.store();
    System.exit(0);
  }
  
  public static void main(String[] argv) {
    if(argv.length < 1) {
      System.out.println("usage: " + Server.class.getName() + " [port] module [modules ...]");
      System.exit(1);
    }
    
    Preferences.addStore(new FilePreferences("iiuf_logger"));
    
    int modulecnt = 0;
    int module    = 0;
    int port      = Client.PORT;
    try {port     = Integer.parseInt(argv[0]); module = 1;} catch(Exception e) {}
    
    Server server = null;
    try {
      server = new Server(port);
      
      for(int i = module; i < argv.length; i++) {
	Class cls = null;
	try {
	  cls = Class.forName("iiuf.log." + argv[i]);
	} catch(Exception e) {
	  try {cls = Class.forName(argv[i]);}
	  catch(Exception ex) {}
	}
	if(cls == null) {
	  System.err.println("Module " + argv[i] + " not found."); 
	}
	else {
	  try {
	    LogMessageListener ml = (LogMessageListener)cls.newInstance();
	    Preferences.watch(ml);
	    if(ml instanceof Frame) 
	      ((Frame)ml).addWindowListener(new WindowAdapter() {
		  public void windowClosing(WindowEvent e) {exit();}
		});
	    server.addLogMessageListener(ml);
	    modulecnt++;
	  } catch(Exception e) {
	    e.printStackTrace(Log.err);
	  }
	}
      }
    }
    catch(Exception e) {e.printStackTrace(Log.err);}
    if(modulecnt == 0 || server == null) exit();
    else server.inited();
  }
}

/*
  $Log: Server.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.11  2001/04/11 12:04:44  schubige
  adapted tinja stuff for semantic checks

  Revision 1.10  2001/01/17 09:55:46  schubige
  Logger update

  Revision 1.9  2001/01/12 08:26:20  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.8  2001/01/03 15:23:50  schubige
  graph stuff beta

  Revision 1.7  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.6  2000/10/17 15:35:59  schubige
  Added watcher preferences

  Revision 1.5  2000/10/10 16:32:12  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.4  2000/10/10 14:16:24  hitz
  Time hack.

  Revision 1.3  2000/10/09 07:29:15  schubige
  Features, features, features

  Revision 1.2  2000/10/09 06:47:56  schubige
  Updated logger stuff

  Revision 1.1  2000/10/05 14:59:30  schubige
  Added loging stuff
  
*/
