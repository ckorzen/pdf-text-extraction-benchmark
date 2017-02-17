package iiuf.util;

import java.util.Vector;
import java.util.StringTokenizer;

/**
   String utilities.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Strings {
  
  /** Whitespace (space, tab, cr, newline) */
  public static final String WS          = " \t\r\n";
  public static final String NUMERIC     = "0123456789";
  public static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
  public static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static final String ALPHA       = ALPHA_LOWER + ALPHA_UPPER;

  public static int count(String str, char c) {
    int result = 0;
    for(int i = 0; i < str.length(); i++)
      if(str.charAt(i) == c)
	result++;
    return result;
  }

  public static String[] split(String str, char splitchar) {
    if(str == null) return new String[0];
    
    int count = 1;
    for(int start = 0;
	(start = str.indexOf(splitchar, start)) != -1;
	count++)
      start++;

    String[] result = new String[count];

    count = 0;
    int start = 0;
    for(int end = 0;
	(end = str.indexOf(splitchar, start)) != -1;
	count++) {
      result[count] = str.substring(start, end);
      start = end + 1;
    }
    result[result.length - 1] = str.substring(start);

    return result;
  }
  
  private static void test_split(String test, char splitchar) {
    System.out.println("\"" + test + "\"->" + 
		       array2str(split(test, splitchar)));
  }

  public static String cat(String[] strings, char catchar) {
    String result = "";
    for(int i = 0; i < strings.length; i++)
      result += (i != 0 ? "" + catchar : "") + strings[i];
    return result;
  }
  
 public static boolean contains(String[] strings, String that) {
   for(int i = 0; i < strings.length; i++)
     if(that.equals(strings[i]))
       return true;
   return false;
 }
  
  public static String array2str(String[] strs) {
    String result = "";
    for(int i = 0; i < strs.length; i++)
      result += "[" + i + "]\"" + strs[i] + "\"";
    return result;
  }
    
  public static String[] removeDuplicates(String[] strings) {
    Vector result_v = new Vector();
    for(int i = 0; i < strings.length; i++)
      if(!result_v.contains(strings[i]))
        result_v.addElement(strings[i]);
    String[] result = new String[result_v.size()];
    for(int i = 0; i < result.length; i++)
      result[i] = (String)result_v.elementAt(i);
    return result;
  }
    
  public static String replace(String in, String that, String by) {
    if(that.equals("")) return in;
    int index = in.indexOf(that);
    if(index == -1) return in;
    else return 
	   in.substring(0, index) + 
	   by +
	   replace(in.substring(index + that.length()), 
		   that, 
		   by);
  }
  
  /**
     Concatenates two String[] arrays.
     
     @param array1 First array.
     @param array2 Second array.
  */
  public static String[] arraycat(String[] array1, String[] array2) {
    String[] result = new String[array1.length + array2.length];
    
    int i = 0;
    
    for(int j = 0; j < array1.length; j++)
    result[i++] = array1[j];
    
    for(int j = 0; j < array2.length; j++)
    result[i++] = array2[j];
    
    return result;
  }
  
  /**
     Returns the given string minus <code>amount</code> chars.
     
     If the length of the resulting string is negative, the empty
     String ("") is returnend.

     @param str The source string.
     @param amount The number of charactersto remove at the right end.
     @return The truncated string.
   */
  public static String rightTrunc(String str, int amount) {
    if(str.length() < amount) return "";
    return str.substring(0, str.length() - amount);
  }
  
  /**
     List of words separated by whitespace.
     
     Shortcut for <code>words(string, Strings.WS)</code>.
     
     @param string The source string.
     @return The list of words.
  */
  public static String[] words(String string) {
    return words(string, WS);
  }

  /**
     List of words separated by chars supplied from the <code>separators</code> string.
     
     @param string The source string.
     @param separators The set of chars separating words.
     @return The list of words.
  */
  public static String[] words(String string, String separators) {
    Vector          resultv = new Vector();
    StringTokenizer st      = new StringTokenizer(string, separators, false); 
    
    while(st.hasMoreTokens())
    resultv.addElement(st.nextToken());
    
    String[] result = new String[resultv.size()];
    for(int i = 0; i < result.length; i++)
    result[i] = (String)resultv.elementAt(i);
    
    return result;
  }
  
  /**
     Snatch the chars form <code>snatchars</code> and replace them by <code>snatchar</code>.
     
     @param string The source string.
     @param snatchars The chars to snatch.
     @param snatchar The chat that will replace a sequence of snatchchars.
     @return The snatched string.
   */
  public static String snatch(String string, String snatchchars, char snatchar) {
    StringTokenizer st = 
      new StringTokenizer(string, snatchchars, false); 
    
    String result = "";
    
    while(st.hasMoreTokens())
      result += st.nextToken() + snatchar;
    
    return result.trim();
  }
  
  public static String repeat(String pattern, int repetitions) {
    String result = "";
    for(int i = 0; i < repetitions; i++)
      result += pattern;
    return result;
  }
  
  /**
     Remove all occurences of the characters in <code>filter</code>.
     
     @param string The source string.
     @param filter The list of chars to remove as string.
     @return The source string with all occurences of the characters in <code>filter</code> removed.
   */
  public static String remove(String string, String filter) {
    String result = "";
    for(int i = 0; i < string.length(); i++)
      if(filter.indexOf(string.charAt(i)) == -1)
	result += string.charAt(i);
    
    return result;
  }
  
  /**
     Return only the chars contained in <code>filter</code>.

     @param string The source string.
     @param filter The list of chars to filter with as string.
     @return The source string consisting only of chars from <code>filter</code>.
  */
  public static String filter(String string, String filter) {
    String result = "";
    for(int i = 0; i < string.length(); i++)
      if(filter.indexOf(string.charAt(i)) >= 0)
	result += string.charAt(i);
    
    return result;
  }
  
  public static String toHex(String str) {
    String result = "";
    for(int i = 0; i < str.length(); i++)
      result += toHex(str.charAt(i));
    return result;
  }
  
  public static String toHex(byte[] data) {
    return toHex(data, 0, data.length, 1, -1, " ");
  }

  public static String toHex(byte[] data, int off, int length) {
    return toHex(data, off, length, 1, -1, " ");
  }
  
  public static String toHex(byte[] data, int off, int length, int groupSize) {
    return toHex(data, off, length, groupSize, -1, " ");
  }
  
  public static String toHex(byte[] data, int off, int length, int groupSize, int bytesPerLine, String groupSeparator) {
    String result = "";
    int c = 0;
    for(int i = off; i < off + length; i++) {
      if(c != 0 && (c % groupSize == 0))
	result += groupSeparator;
      if(c != 0 && bytesPerLine != -1 && (c % bytesPerLine == 0))
	result += "\n";
      result += toHex(data[i]);
      c++;
    } 
    return result;
  }

  public static String toHex(int i) {
    return toHex((byte)(i >> 24)) +  toHex((byte)(i >> 16)) +  toHex((byte)(i >> 8)) + toHex((byte)i);
  }

  public static String toHex(char c) {
    return toHex((byte)(c >> 8)) + toHex((byte)c);
  }

  private static final char[] HEXTAB = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
  
  public static String toHex(byte b) {
    return new StringBuffer().append(HEXTAB[(b >> 4) & 0xF]).append(HEXTAB[b & 0xF]).toString();
  }
  
  public static void main(String[] argv) {
    System.out.println("Testing split...");
    test_split(null,'|');
    test_split("",'|');
    test_split("|",'|');
    test_split("||",'|');
    test_split("a",'|');
    test_split("a|a",'|');
    test_split("ab|ab",'|');
    test_split("ab||ab",'|');
    test_split("ab|ab||",'|');
  }
  
  public final static String   ILLEGAL_NAME_CHARS  = " \t\n\b#$%^&-+*=|<>[]{}()!?;,.\"\'`/";
  public final static String[] ILLEGAL_CLASS_NAMES = {
    "while",        "while_",
    "class",        "class_",
    "else",         "else_",
    "extends",      "extends_",
    "if",           "if_",
    "import",       "import_",
    "int",          "int_",
    "new",          "new_",
    "null",         "null_",
    "private",      "private_",
    "public",       "public_",
    "return",       "return_",
    "this",         "this_",
    "void",         "void_",
    "abstract",     "abstract_",
    "boolean",      "boolean_",
    "break",        "break_",
    "byte",         "byte_",
    "byvalue",      "byvalue_",
    "case",         "case_",
    "cast",         "cast_",
    "catch",        "catch_",
    "char",         "char_",
    "const",        "const_",
    "continue",     "continue_",
    "default",      "default_",
    "do",           "do_",
    "double",       "double_",
    "false",        "false_",
    "final",        "final_",
    "finally",      "finally_",
    "float",        "float_",
    "for",          "for_",
    "future",       "future_",
    "generic",      "generic_",
    "goto",         "goto_",
    "implements",   "implements_",
    "inner",        "inner_",
    "instanceof",   "instanceof_",
    "interface",    "interface_",
    "long",         "long_",
    "native",       "native_",
    "operator",     "operator_",
    "outer",        "outer_",
    "package",      "package_",
    "protected",    "protected_",
    "rest",         "rest_",
    "short",        "short_",
    "static",       "static_",
    "super",        "super_",
    "switch",       "switch_",
    "synchronized", "synchronized_",
    "throw",        "throw_",
    "throws",       "throws_",
    "transient",    "transient_",
    "true",         "true_",
    "try",          "try_",
    "var",          "var_",
    "volatile",     "volatile_"};

  public static String toClassName(String string) {
    if(string == null) return null;
    String result = "";
    if(string.charAt(0) >= '0' && string.charAt(0) <= '9')
      result += "_";
    for(int i = 0; i < string.length(); i++)
      result += string.charAt(i) >= 128 ? '_' : string.charAt(i);
    for(int i = 0; i < ILLEGAL_CLASS_NAMES.length; i += 2)
      if(ILLEGAL_CLASS_NAMES[i].equals(string))
	result = ILLEGAL_CLASS_NAMES[i + 1];
    try{
      for(int i = 0; i < ILLEGAL_NAME_CHARS.length(); i++)
	result = result.replace(ILLEGAL_NAME_CHARS.charAt(i), '_');
    } catch(StringIndexOutOfBoundsException e) {e.printStackTrace();}
    
    String[] sresult = split(result, '_');
    
    result = "";
    for(int i = 0; i < sresult.length; i++)
      if(sresult[i].length() < 2)
	result += "_" + sresult[i];
      else {
	if(i == 0)
	  result += Character.toUpperCase(sresult[i].charAt(0)) + sresult[i].substring(1);
	else
	  if(Character.isLowerCase(sresult[i - 1].charAt(sresult[i - 1].length() - 1)))
	    result += Character.toUpperCase(sresult[i].charAt(0)) + sresult[i].substring(1);
	  else
	    result += "_" + sresult[i];
      }
    
    return result;
  }
}
/*
  $Log: Strings.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.20  2001/02/09 15:03:44  schubige
  rpcgen beta

  Revision 1.19  2001/02/01 15:57:05  schubige
  rpcgen working on tcp

  Revision 1.18  2001/01/14 13:21:14  schubige
  Win NT update

  Revision 1.17  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.16  2000/11/16 16:22:50  schubige
  javap / bytecode checkin

  Revision 1.15  2000/11/09 07:48:44  schubige
  early checkin for DCJava

  Revision 1.14  2000/05/04 09:06:01  schubige
  *** empty log message ***

  Revision 1.13  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.12  2000/04/27 09:32:38  schubige
  intermediate checkin for sybase proxy lower() based where clause

  Revision 1.11  2000/04/25 12:03:35  schubige
  Bibtex db project restart

  Revision 1.10  2000/01/18 11:15:39  schubige
  First beta release of vote server / votlet

  Revision 1.9  1999/11/26 08:44:26  schubige
  cleanup, move to awt package

  Revision 1.8  1999/10/07 11:02:13  schubige
  Added red black and binary tree classes

  Revision 1.7  1999/09/15 11:57:17  robadey
  Added bibtex 2 html stuff

  Revision 1.6  1999/09/15 07:28:12  schubige
  Some iiuf.util.Strings updates

  Revision 1.5  1999/09/14 11:48:13  schubige
  Updated some preferences realted classes

  Revision 1.4  1999/09/10 12:03:27  juillera
  Commit before vacation.

  Revision 1.3  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.

  Revision 1.2  1999/09/02 14:15:29  schubige
  added @serial tag or transient to make javadoc happy
  
  Revision 1.1.1.2  1999/09/02 09:24:21  schubige
  Added examdb stuff to cvs tree
*/
