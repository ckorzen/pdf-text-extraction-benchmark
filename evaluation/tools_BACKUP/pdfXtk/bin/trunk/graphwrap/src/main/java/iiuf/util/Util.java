package iiuf.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.rmi.RemoteException;

/**
   Various utilities.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Util {

  private static java.util.Random random = new java.util.Random();
  
  public static PrintStream out = System.err;
  
  public final static boolean contains(Object[] os, Object o) {
    for(int i = 0; i < os.length; i++)
      if(os[i] == o)
	return true;
    return false;
  }

  /** @return The minimum of a and b.*/
  public final static int min(int a, int b) {return a < b ? a : b;}
  
  /** @return The minimum of a, b and c.*/
  public final static int min(int a, int b, int c) {return min(a, min(b, c));}
  
  /** @return The minimum of the array a. */
  public final static int min(int[] a) {
    int result = Integer.MAX_VALUE;
    for(int i = 0; i < a.length; i++)
      if(a[i] < result) result = a[i];
    return result;
  }

  /** @return The minimum of the array a. */
  public final static double min(double[] a) {
    double result = Double.MAX_VALUE;
    for(int i = 0; i < a.length; i++)
      if(a[i] < result) result = a[i];
    return result;
  }

  /** @return The sum of the array a. */
  public final static int sum(int[] a) {
    int Result = 0;
    for(int i = 0; i < a.length; i++)
      Result += a[i];
    return Result;
  }
  
  /** Emits a system-beep */
  public static void beep() {
    java.awt.Toolkit.getDefaultToolkit().beep();
  }

  /** @return The maximum of a and b. */
  public final static long max(long a, long b) {return a > b ? a : b;}

  /** @return The maximum of a, b and c. */
  public final static long max(long a, long b, long c) {return max(a, max(b, c));}

  /** @return The maximum of a and b. */
  public final static int max(int a, int b) {return a > b ? a : b;}

  /** @return The maximum of a, b and c. */
  public final static int max(int a, int b, int c) {return max(a, max(b, c));}

  /** @return The maximum of the array a. */
  public final static int max(int[] a) {
    int result = Integer.MIN_VALUE;
    for(int i = 0; i < a.length; i++)
      if(a[i] > result) result = a[i];
    return result;
  }

  /** @return The maximum of the array a. */
  public final static double max(double[] a) {
    double result = Double.MIN_VALUE;
    for(int i = 0; i < a.length; i++)
      if(a[i] > result) result = a[i];
    return result;
  }
  
  /** @return Char c as a string. */
  public static String c2str(char c) {
    return c + "";
  }
  
  /** @return a + b without wrap around if result > Integer.MAX_VALUE. */
  public final static int plusNowrap(int a, int b) {
    long a_  = a; 
    long b_  = b;
    long max = Integer.MAX_VALUE;
    return a_ + b_ < max ? a + b : Integer.MAX_VALUE;
  }
  
  /**
     @return A positve unifromly distributed random number modulo range.
  */
  public final static int intRandom(int range) {
    int result = random.nextInt() % range;
    return result < 0 ? -result : result;
  }

  /**
     @return A positve unifromly distributed random number modulo range.
  */
  public final static double doubleRandom(double range) {
    double result = random.nextDouble() % range;
    return result < 0 ? -result : result;
  }

  /**
     @return A positve unifromly distributed random number modulo range.
  */
  public final static long longRandom(long range) {
    long result = random.nextLong() % range;
    return result < 0 ? -result : result;
  }
  
  /**
     Concatenates two String[][] arrays.
     
     @param array1 First array.
     @param array2 Second array.
  */
  public static String[][] arraycat(String[][] array1, String[][] array2) {
    String[][] Result = new String[array1.length + array2.length][];
    
    int i = 0;
    
    for(int j = 0; j < array1.length; j++)
      Result[i++] = array1[j];
    
    for(int j = 0; j < array2.length; j++)
      Result[i++] = array2[j];
    
    return Result;
  }
  
  /** 
      Delays for the number of milliseconds.
      
      @param milliseconds Number of milliseconds to delay.
  */
  public final static void delay(long milliseconds) {
    try {Thread.sleep(milliseconds);}
    catch(InterruptedException e) {}
  }
  
  /** @return The current time in millisecond. */
  public final static long time() {return System.currentTimeMillis();}
  
  /** Allows other threads to run */
  public final static void nice() {
    if(true) {
      try {Thread.sleep(10);}
      catch(InterruptedException e) {}
    }
    else {
      Thread.currentThread().yield();
    }
  }
  
  /**
    Executes the commands with args and environemnt in a separate process.
    
    @param command The command to ecxecute.
    @param args_   The arguments.
    @param environment The environment key-value pairs.
    @return The new process.
    */
  
  public final static Process exec(String command, String[] args_, String[] environment) 
    throws java.io.IOException {
      // setup params
      String[] args = new String[1];
      if(args_ != null) {
        args = new String[args_.length + 1];
	for(int i = 0; i < args_.length; i++)
	  args[i + 1] = args_[i];
      }
      
      // let's go...
      args[0] = command;

      // param debug
      /*
	for(int i = 0; i < args.length; i++)
	  out.println("[" + i + "]" + args[i]);
	
	for(int i = 0; i < environment.length; i++)
	  out.println("[" + i + "]" + environment[i]);
      */
      // end of param debug
      
      return Runtime.getRuntime().exec(args, environment); 
  }
  
  /**
    Starts the rmi registry if not running.
    default port is 1099, use the java.rmi.registry.port property to set another port
    
    @return The local registry.
    */
  public final static java.rmi.registry.Registry  registry() {
    int port = java.rmi.registry.Registry.REGISTRY_PORT;
    java.rmi.registry.Registry Result = null;
    try{java.rmi.registry.LocateRegistry.getRegistry();}
    catch(Exception e) {}
    try{port = Integer.parseInt(System.getProperty("java.rmi.registry.port"));}
    catch(Exception e) {}
    try{Result = java.rmi.registry.LocateRegistry.createRegistry(port);}
    catch(Exception e) {}
    return Result;
  }
  
  public final static InetAddress[] removeNull(InetAddress addrs[]) {
    int count = 0;
    for(int i = 0; i < addrs.length; i++)
      if(addrs[i] != null)
	count++;
    InetAddress[] result = new InetAddress[count];
    count = 0;
    for(int i = 0; i < addrs.length; i++)
      if(addrs[i] != null)
	result[count++] = addrs[i];
    return result;
  }
  
  static final public InetAddress[] minus(InetAddress[] a_, InetAddress[] b) {
    InetAddress[] a = (InetAddress[])a_.clone();
    for(int i = 0; i < b.length; i++)
      for(int j = 0; j < a.length; j++)
	if(b[i].equals(a[j]))
	  a[j] = null;
    
    return removeNull(a);
  }
  
  static final public InetAddress[] remove(InetAddress[] a_, InetAddress b) {
    InetAddress[] a = (InetAddress[])a_.clone();
    for(int i = 0; i < a.length; i++)
      if(b.equals(a[i])) {
	a[i] = null;
	break;
      }
    return removeNull(a);
  }
  
  public final static boolean inetAddressLeq(InetAddress a_, InetAddress b_) {
    byte[] a = a_.getAddress();
    byte[] b = b_.getAddress();
    for(int i = 0; i < a.length; i++)
      if(a[i] > b[i]) return false;
    return true;
  }
  
  public final static boolean inetAddressLt(InetAddress a, InetAddress b) {
    return !a.equals(b) && inetAddressLeq(a, b);
  }
  
  public final static boolean inetAddressGt(InetAddress a, InetAddress b) {
    return !inetAddressLeq(a, b);
  }
  
  public final synchronized static void println(long l) {
    println(new Long(l));
  }
    
  public final synchronized static void println(Object o) {
    println(o, false);
  }
  
  public final static void printStackTrace(Throwable t, PrintStream out) {
    if(t instanceof RemoteException) {
      out.println("---- Nested exception:");
      printStackTrace(((RemoteException)t).detail, out);
    }
    else
      t.printStackTrace(out);
  }

  public final static String toString(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  public final synchronized static void println(Object o, boolean trace) {
    iiuf.log.Log.info(o, trace);
  }
  
  public final synchronized static void printStackTrace(Throwable t) {
    iiuf.log.Log.stackTrace(iiuf.log.Const.LOG_ERR, t);
  }
  
  public static String data2string(Object data) {
    if(data == null)
      return "<null>";
    else if(data instanceof InetAddress)
      return ((InetAddress)data).getHostName();
    else if(data instanceof InetAddress[]) {
      InetAddress[] tmp = (InetAddress[])data;
      String result = "";
      for(int i = 0; i < tmp.length; i++)
	result += "[" + i + "]" + data2string(tmp[i]);
      return result;
    }
    else return data.toString();
  }
}
/*
  $Log: Util.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.14  2001/01/14 13:21:14  schubige
  Win NT update

  Revision 1.13  2001/01/12 08:26:21  schubige
  TJGUI update and some TreeView bug fixes

  Revision 1.12  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.11  2000/12/01 14:41:36  schubige
  SourceWatch beta 1

  Revision 1.10  2000/11/20 17:36:57  schubige
  tinja project ide

  Revision 1.9  2000/10/09 06:47:57  schubige
  Updated logger stuff

  Revision 1.8  2000/05/26 09:45:44  schubige
  Added iiuf.io.fs and iiuf.os

  Revision 1.7  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.6  2000/04/25 12:03:36  schubige
  Bibtex db project restart

  Revision 1.5  1999/10/07 11:02:13  schubige
  Added red black and binary tree classes

  Revision 1.4  1999/09/10 12:03:28  juillera
  Commit before vacation.

  Revision 1.3  1999/09/03 15:50:09  schubige
  Changed to new header & log conventions.
  
*/
