package iiuf.log;

import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import iiuf.util.Timer;

/**
   Log client implementation.

   Set the "log.host" property to set the host where the server is running (default is "localhost").
   Set the "log.port" property to set the tcp port used for logging (default is 7659).
   Set the "log.qsize" property to limit message buffering on the client side (default is 100, use -1 for unlimited buffering).


   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Client 
  extends
  Thread 
{
  public static int  PORT = 0x1DEB;
  
  ObjectOutputStream out;
  InetAddress        server;
  int                port;
  static Client      client;
  LinkedList         queue = new LinkedList();
  int                MAX_QSIZE;

  static class State {
    StringBuffer b = new StringBuffer();
    Thread       thread;
    
    State(Thread t) {
      thread = t;
    }
    
    void append(int c) {
      b.append((char)c);
      if(c == '\n') {
	String line = b.toString();
	if(Client.client == null) {
	  Log.out.print(line);
	}
	else
	  Client.client.log(new LogMessage(thread, Const.LOG_INFO, Const.STDOUT, line));
	b = new StringBuffer();
      }
    }
  }
  
  static class StdOutStream 
    extends 
    OutputStream 
  {
    HashMap threads = new HashMap();
    
    public synchronized void write(int c) {
      Thread curr = Thread.currentThread();
      State s = (State)threads.get(curr);
      if(s == null) {
	s = new State(curr);
	threads.put(curr, s);
      }
      s.append(c);
    }
  }
  
  static class ErrorStream
    extends
    OutputStream
  {
    HashMap threads = new HashMap();
    
    class State 
      implements
      Runnable 
    {
      StringBuffer b = new StringBuffer();
      Thread       thread;
      String       exception     = "";
      boolean      exceptionDump = false;
      Timer        timer = new Timer(this, false);
      
      State(Thread t) {
	thread = t;
      }
      
      void append(int c) {
	b.append((char)c);
	if(c == '\n') {
	  String line = b.toString();
	  if(exceptionDump) {
	    if(line.startsWith("\tat ")) {
	      exception += line;
	      timer.cancel();
	      timer.schedule(500);
	    }
	    else {
	      exception();
	      stderr(line);
	    }
	  }
	  else {
	    int idx = line.indexOf(':');
	    if(idx != -1) {
	      try {
		if(Throwable.class.isAssignableFrom(Class.forName(line.substring(0, idx)))) {
		  exceptionDump = true;
		  exception += line;
		}
		else stderr(line);
	      } catch(Exception e) {
		stderr(line);
	      }
	    }
	    else {
	      try {
		if(Throwable.class.isAssignableFrom(Class.forName(line.trim()))) {
		  exceptionDump = true;
		  exception += line;
		}
		else stderr(line);
	      } catch(Exception e) {
		stderr(line);
	      }
	    }
	  }
	  b = new StringBuffer();
	}
      }
      
      public void run() {
	if(exception.length() > 50)
	  exception();
      }
      
      void exception() {
	if(Client.client == null)
	  Log.out.print(exception);
	else
	  Client.client.log(new LogMessage(thread, Const.LOG_ERR, "Runtime Exception", exception));      
	exception = "";
	exceptionDump = false;
      }
      
      void stderr(String line) {
	if(Client.client == null)
	  Log.out.print(line);
	else
	  Client.client.log(new LogMessage(thread, Const.LOG_WARNING, Const.STDERR, line));
      }
    }
    
    public synchronized void write(int c) {
      Thread curr = Thread.currentThread();
      State s = (State)threads.get(curr);
      if(s == null) {
	s = new State(curr);
	threads.put(curr, s);
      }
      s.append(c);
    }
  }
  
  static {
    Log.err = System.err;
    Log.out = System.out;
    System.setErr(new PrintStream(new ErrorStream()));
    System.setOut(new PrintStream(new StdOutStream()));
  }
  
  public Client() {
    this(getHost(), getPort());
  }
  
  private static InetAddress getHost() {
    try {
      return InetAddress.getByName(System.getProperty("log.host"));
    } catch(Exception e) {
      try {
	return InetAddress.getLocalHost();
      } catch(Exception ex) {
	return null;
      }
    }
  }
  
  private static int getPort() {
    try {
      return Integer.parseInt(System.getProperty("log.port"));
    } catch(Exception e) {
      return PORT;
    }
  }
  
  public Client(InetAddress server_, int port_) {
    server    = server_;
    port      = port_;
    client    = this;
    MAX_QSIZE = 100;
    try{MAX_QSIZE = Integer.parseInt(System.getProperty("log.qsize"));}
    catch(Exception e) {}
    start();
  }

  public void log(int priority, String message, Throwable exception) {
    log(new LogMessage(Thread.currentThread(), priority, message, exception));
  }
  
  synchronized void log(LogMessage m) {
    m.time = System.currentTimeMillis();
    if(MAX_QSIZE > 0 && queue.size() >= MAX_QSIZE) 
      queue.removeFirst();
    queue.add(m);
    notify();
  }
  
  public synchronized void run() {
    for(;;) {
      try{wait(10000);}
      catch(InterruptedException e) {e.printStackTrace(Log.err);}
      if(out == null) connect();
      if(out != null) {
	try {
	  while(!queue.isEmpty()) {
	    out.writeObject(queue.getFirst());
	    queue.removeFirst();
	  }
	  out.flush();
	} catch(Exception e) {
	  out = null;
	}
      }
    }
  }
  
  private synchronized void connect() {
    try {
      out = new ObjectOutputStream(new Socket(server, port).getOutputStream());
    } catch(Exception e) {
      out = null;
    }
  }
  
  public static void main(String[] argv) {
    try {
      if(argv.length > 2) {
	Log.err.println("usage: " + Client.class.getName() + " [server [port]]");
	System.exit(1);
      }

      Client c = argv.length == 0 ? 
	new Client() : 
	new Client(InetAddress.getByName(argv[0]), argv.length == 2 ? Integer.parseInt(argv[1]) : PORT);
      
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      for(;;) {
	String line = in.readLine();
	if(line == null || line.equals("")) {
	  break;
	}
	try{
	  int prio = Integer.parseInt(line.substring(0, 1));
	  if(prio < 0 || prio > 7)
	    throw new IllegalArgumentException("illegal priority value:" + prio);
	  c.log(prio, line.substring(1), null);
	} catch(Exception e) {
	  c.log(Const.LOG_ERR, line, e);
	}
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}

/*
  $Log: Client.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.10  2001/04/11 12:04:44  schubige
  adapted tinja stuff for semantic checks

  Revision 1.9  2001/01/17 09:55:45  schubige
  Logger update

  Revision 1.8  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.7  2000/11/09 07:48:43  schubige
  early checkin for DCJava

  Revision 1.6  2000/10/19 08:03:45  schubige
  Intermediate graph component related checkin

  Revision 1.5  2000/10/17 15:35:59  schubige
  Added watcher preferences

  Revision 1.4  2000/10/10 16:32:11  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.3  2000/10/09 07:29:15  schubige
  Features, features, features

  Revision 1.2  2000/10/09 06:47:55  schubige
  Updated logger stuff

  Revision 1.1  2000/10/05 14:59:30  schubige
  Added loging stuff
  
*/
