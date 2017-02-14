package iiuf.util;

/**
   Unicode translator.
   
   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @see iiuf.util.UnicodeTranslator
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class UnicodeTrans 
  implements
  Unicode {
  
  public static String[] HTML2UNICODE  = UTHTML.HTML2UNICODE;
  public static String[] UNICODE2HTML  = UTHTML.UNICODE2HTML;

  public static String[] MACOS2UNICODE = UTMacOS.MACOS2UNICODE;
  public static String[] UNICODE2MACOS = UTMacOS.UNICODE2MACOS;

  public static String[] UNICODE2TEX   = UTTeX.UNICODE2TEX;

  /**
     Table based translations.
     
     @deprecated  Use </code>iiuf.util.UnicodeTranslator.getUnicode()</code> and <code>iiuf.util.UnicodeTranslator.getNative()</code> instead.
     @param in The source string.
     @param table The translation table.
     @return The source string with the replacements made according to the given table.
  */
  public static String trans(String string, String[] table) {
    String result = string;
    for(int i = 0; i < table.length; i += 2)
      result = Strings.replace(result, table[i], table[i + 1]);
    return result;
  }
  
  public static void main(String[] argv) {
    for(int i = 32; i < 256; i++)
      System.out.println(i + ":" + Integer.toHexString(i) + ":" + (char)i);
  }
}
/*
  $Log: UnicodeTrans.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.11  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.10  2000/11/29 12:49:41  schubige
  some 'deprecated' fixes

  Revision 1.9  2000/04/25 12:03:36  schubige
  Bibtex db project restart

  Revision 1.8  1999/12/14 12:26:30  schubige
  Fixed find bug in variable

  Revision 1.7  1999/12/02 16:07:34  schubige
  updated block, general cleanup

  Revision 1.6  1999/09/15 11:57:17  robadey
  Added bibtex 2 html stuff

  Revision 1.5  1999/09/15 07:28:12  schubige
  Some iiuf.util.Strings updates

  Revision 1.4  1999/09/14 14:49:42  schubige
  Added inverse table function

  Revision 1.3  1999/09/03 15:50:09  schubige
  Changed to new header & log conventions.
  
*/
