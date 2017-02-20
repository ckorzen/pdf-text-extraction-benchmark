// Base64FormatException.java
// $Id: Base64FormatException.java,v 1.1 2002/07/11 12:00:11 ohitz Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package iiuf.util;

/**
 * Exception for invalid BASE64 streams.
 */

public class Base64FormatException extends Exception {
    public Base64FormatException (String msg) {
	super(msg) ;
    }
}
