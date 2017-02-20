package iiuf.util;

import java.util.*;
import java.io.File;


public class DirTree {

  static Vector _include = new Vector();
  static Vector _exclude = new Vector();
  static Vector files    = new Vector();
  static Vector _replace = new Vector();
  static String[] include;
  static String[] exclude;
  static String[] replace;
  
  public static void main(String[] argv) {
    for(int i = 0; i < argv.length; i++) {
      if(argv[i].startsWith("-")) {
	if(argv[i].equals("-i")) {
	  _include.addElement(argv[i + 1]);
	}
	else if(argv[i].equals("-x")) {
	  _exclude.addElement(argv[i + 1]);
	}
	else if(argv[i].equals("-r")) {
	  _replace.addElement(argv[i + 1]);
	  _replace.addElement(argv[i + 2]);
	  i++;
	}
	else {
	  System.err.println("Illegal option:" + argv[i]);
	  usage();
	}
	i++;
      }
      else 
	files.addElement(argv[i]);
    }
    
    include = new String[_include.size()];
    for(int i = 0; i < include.length; i++) include[i] = (String)_include.elementAt(i);
    exclude = new String[_exclude.size()];
    for(int i = 0; i < exclude.length; i++) exclude[i] = (String)_exclude.elementAt(i);
    replace = new String[_replace.size()];
    for(int i = 0; i < replace.length; i++) replace[i] = (String)_replace.elementAt(i);
    
    for(int i = 0; i < files.size(); i++) {
      Node root = buildTree((String)files.elementAt(i));
      System.out.println("<HTML>");
      System.out.println("<BODY>");
      System.out.println("<FONT SIZE = -2>");
      System.out.println(root);
      System.out.println("</FONT>");
      System.out.println("</BODY>");
      System.out.println("</HTML>");
    }
  }
  
  static void usage() {
    System.err.println(DirTree.class.getName() + "[-x part] [-i part] [-u part rplc] directroy...");
    System.err.println("\t[-x part] exclude file/directroy names containing <part>");
    System.err.println("\t[-i part] include file/directroy names containing <part>");
    System.err.println("\t[-r part rplc] replace <part> by <rplc> in HREF");
    System.exit(1);
  }

  static Node buildTree(String path) {
    
    File file = new File(path);
    
    Node Result = new Node(file.getName(), file.getAbsolutePath());
    
    if(file.isDirectory()) {
      String[] files = file.list();
      
      for(int i = 0; i < files.length; i++) {
	Node n = buildTree(path + File.separator + files[i]);
	if(include(files[i]) && !exclude(files[i]))
	  Result.addChild(n);
	else if(new File(path + File.separator + files[i]).isDirectory() && !exclude(files[i]))
	  Result.addChild(n);
      }
    }
    
    return Result;
  }
  
  static boolean exclude(String name) {
    for(int i = 0; i < exclude.length; i++)
      if(name.indexOf(exclude[i]) >= 0) return true;
    return false;
  }
  
  static boolean include(String name) {
    if(include.length == 0) return true;
    for(int i = 0; i < include.length; i++)
      if(name.indexOf(include[i]) >= 0) return true;
    return false;
  }
  
  static String replace(String string) {
    for(int i = 0; i < replace.length; i += 2)
      string = Strings.replace(string, replace[i], replace[i + 1]);
    return string;
  }
}

class Node {
  
  Node   parent;
  Vector children = new Vector();
  String name;
  String url;
  
  Node(String name_, String url_) {
    name = name_;
    url  = url_;
  }
  
  void addChild(Node node) {
    node.parent = this;
    children.addElement(node);
  }
  
  public String toString() {
    try{quickSort(children, 0, children.size() - 1);}
    catch(Exception e) {e.printStackTrace();}
    
    String s = "";
    
    s += "<LI><A href=\"" + DirTree.replace(url) + "\">" + name + "</A></LI>\n";
    
    if(children.size() > 0) {
      s += "<UL>";
      for(int i = 0; i < children.size(); i++)
	s += children.elementAt(i).toString() + "\n";
      s += "</UL>"; 
    }
    
    return s; 
  }

  /** This is a generic version of C.A.R Hoare's Quick Sort 
   * algorithm.  This will handle arrays that are already
   * sorted, and arrays with duplicate keys.<BR>
   *
   * If you think of a one dimensional array as going from
   * the lowest index on the left to the highest index on the right
   * then the parameters to this function are lowest index or
   * left and highest index or right.  The first time you call
   * this function it will be with the parameters 0, a.length - 1.
   *
   * @param a       an vector
   * @param lo0     left boundary of array partition
   * @param hi0     right boundary of array partition
   */
  static void quickSort(Vector a, int lo0, int hi0) throws Exception {
    int    lo = lo0;
    int    hi = hi0;
    Object mid;
    
    if ( hi0 > lo0) {
      
      /* Arbitrarily establishing partition element as the midpoint of
	 * the array.
	 */
      mid = a.elementAt(( lo0 + hi0 ) / 2 );
      
      // loop through the array until indices cross
      while( lo <= hi )
	{
	  /* find the first element that is greater than or equal to 
	   * the partition element starting from the left Index.
	   */
	  while( ( lo < hi0 ) && ( stringLt(a.elementAt(lo), mid) ))
	    ++lo;
	  
	  /* find an element that is smaller than or equal to 
	   * the partition element starting from the right Index.
	   */
	  while( ( hi > lo0 )  && ( stringGt(a.elementAt(hi), mid) ))
	    --hi;
	  
	  // if the indexes have not crossed, swap
	  if( lo <= hi ) 
            {
	      swap(a, lo, hi);
	      ++lo;
	      --hi;
            }
	}
      
      /* If the right index has not reached the left side of array
       * must now sort the left partition.
       */
      if( lo0 < hi )
	quickSort( a, lo0, hi );
      
      /* If the left index has not reached the right side of array
       * must now sort the right partition.
       */
      if( lo < hi0 )
	quickSort( a, lo, hi0 );
      
    }
  }
  
  private static void swap(Vector a, int i, int j) {
     Object T = a.elementAt(i); 
     a.setElementAt(a.elementAt(j), i);
     a.setElementAt(T, j);
  }
  
  private static boolean stringLt(Object o1, Object o2) {
    String s1 = ((Node)o1).name;
    String s2 = ((Node)o2).name;
    
    int l = s1.length() < s2.length() ? s1.length() : s2.length();
    
    for(int i = 0; i < l; i++) {
      if(s1.charAt(i) == s2.charAt(i)) continue;
      if(s1.charAt(i) < s2.charAt(i)) return true;
      break;
    }
    
    return false;
  }

  private static boolean stringGt(Object o1, Object o2) {
    String s1 = ((Node)o1).name;
    String s2 = ((Node)o2).name;
    
    return ! (stringLt(o1, o2) || s1.equals(s2));
  }
}


