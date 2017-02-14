/** ---------------------------------------------------------------------------
 * ---                        FRIBOURG UNIVERSITY                          ---
 * ---                  COMPUTER SCIENCE LABORATORY                        ---
 * ---           Chemin du Musee 3, CH-1700 FRIBOURG, SWITZERLAND          ---
 * ---------------------------------------------------------------------------
 * TITLE:	$RCSfile: ListParser.java,v $
 * SUPPORT:	$Author: ohitz $
 * CREATION:	$Date: 2002/07/11 12:00:11 $
 * @VERSION:	$Revision: 1.1 $
 * OVERVIEW:    Make list from string
 * ------------------------------------------------------------------------ */
/**
    (c) 2000, 2001, IIUF, DIUF

    Make list from string

    @author $Author: ohitz $
    @version $Revision: 1.1 $
*/
/* ------------------------------------------------------------------------ */
package iiuf.util;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class ListParser {
  /*------------------------------------------------------------------------*/
  /** make a list from a string
      @param s string to parse to make a list
      @param separator concatenation of chars that separate the string in 
      token
      @return list of extracted tokens
  */
  public static LinkedList parseList(String s,
				     String separators) {
    LinkedList l = new LinkedList();

    StringTokenizer tokenizer = new StringTokenizer(s,separators);
    while (tokenizer.hasMoreTokens())
      l.add(tokenizer.nextToken());

    return l;    
  }
  /*------------------------------------------------------------------------*/
}
/*--------------------------------------------------------------------------*/
