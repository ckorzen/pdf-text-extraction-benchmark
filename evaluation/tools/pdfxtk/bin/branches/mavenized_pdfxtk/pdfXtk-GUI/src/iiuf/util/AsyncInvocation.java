package iiuf.util;

import java.util.Vector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import iiuf.log.Log;

/**
   An async invocation queue.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class AsyncInvocation 
  extends
  Queue 
{
  public static class Result {
    private boolean   valid;
    private Object    result;
    private Throwable exception;
    
    public boolean isValid() {
      return valid;
    }
    
    public synchronized Object get() {
      if(valid) return result;
      else {
	try{wait();} catch(InterruptedException e) {Util.printStackTrace(e);}
      }
      return result;
    }

    public synchronized void set(Object result_) {
      result = result_;
      valid  = true;
      notify();
    }

    public synchronized void setException(Throwable exception_) {
      exception = exception_;
      valid     = true;
      notify();
    }    
  }
  
  public static class Invocation {
    public Method   method;
    public Object   object;
    public Object[] args;
    public Result   result = new Result();

    Invocation(Method method, Object object) {
      this(method, object, null);
    }
    
    Invocation(Method method_, Object object_, Object[] args_) {
      method = method_;
      object = object_;
      args   = args_;
    }
    
    Invocation create(Object arg) {
      return new Invocation(method, object, new Object[] {arg});
    }
    
    Invocation create(Object[] args) {
      return new Invocation(method, object, args);
    }
    
    void invoke() {
      try{result.set(method.invoke(object, args));} 
      catch(IllegalAccessException e)   {
	if(!((object.getClass().getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC))
	  Log.error("Class of target object must be public.");	
	Util.printStackTrace(e);
	result.setException(e);
      }
      catch(IllegalArgumentException e) {
	Util.printStackTrace(e); 
	result.setException(e);
      }
      catch(InvocationTargetException e) {
	result.setException(e.getTargetException());
      }
    }
  }
  
  private Vector asyncAccels = new Vector();  
  public void addAsyncAccelerator(AsyncAccelerator accel) {
    asyncAccels.add(accel);
  }
    
  public AsyncInvocation(String name) {
    super(name, Thread.MIN_PRIORITY);
  }
  
  private String argStr(Object[] args) {
    if(args.length == 0) return "";
    String result = args[0].toString();
    for(int i = 1; i < args.length; i++)
      result += "," + args[i].toString();
    return result;
  }
  
  protected boolean handle(Object o) {
    Invocation i = (Invocation)o;
    // Log.debug("invoking " + i.method + "(" + argStr(i.args) + ") on " + i.object);
     
    for(int j = 0; j < asyncAccels.size(); j++) {
      ((AsyncAccelerator)asyncAccels.elementAt(j)).handle(i, i.result);
      if(i.result.isValid()) return true;
    }
    i.invoke();
    return true;
  }
  
  public Result invoke(Object object, String method, Class argType, Object arg) {
    return invoke(object, method, new Class[] {argType}, new Object[] {arg});
  }
  
  public Result invoke(Object object, String method, Class[] argTypes, Object[] args) {
    try{
      Invocation i = new Invocation(object.getClass().getMethod(method, argTypes), object, args);
      Result     result = i.result;
      put(i);
      return result;
    }
    catch(Exception e) {Util.printStackTrace(e);}
    return null;
  }
}

/*
  $Log: AsyncInvocation.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:40  schubige
  Header update for 2001 and DIUF

  Revision 1.1  2000/11/09 07:53:09  schubige
  early checkin for DCJava
  
*/
