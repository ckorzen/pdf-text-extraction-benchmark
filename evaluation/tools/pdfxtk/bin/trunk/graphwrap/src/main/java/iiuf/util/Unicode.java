package iiuf.util;

/**
   Latin-1 Unicode constants 00A0-00FF.
   
   Use <code>java iiuf.util.UnicodeTranslator</code> to dump a printable form of this table.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
      
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public interface Unicode {
  
  public final static String nbsp       = "\u00A0"; // non breaking space
  public final static String iexcl      = "\u00A1"; // inverted exclamation mark
  public final static String cent       = "\u00A2"; // cent sign
  public final static String pound      = "\u00A3"; // pund sign
  public final static String curren     = "\u00A4"; // currency sign
  public final static String yen        = "\u00A5"; // yen sign
  public final static String brvbar     = "\u00A6"; // broken bar
  public final static String sect       = "\u00A7"; // section sign
  public final static String Dot        = "\u00A8"; // diaeresis
  public final static String copy       = "\u00A9"; // copyright sign
  public final static String ordf       = "\u00AA"; // feminine ordinal indicator
  public final static String Lt         = "\u00AB"; // left pointing double angle quotation mark
  public final static String not        = "\u00AC"; // not sign
  public final static String shy        = "\u00AD"; // soft hyphen
  public final static String reg        = "\u00AE"; // registered sign
  public final static String macr       = "\u00AF"; // macron
  
  public final static String deg        = "\u00B0"; // degree sign
  public final static String plusmn     = "\u00B1"; // plus minus sign
  public final static String sup2       = "\u00B2"; // superscript two
  public final static String sup3       = "\u00B3"; // superscript three
  public final static String acute      = "\u00B4"; // acute accent
  public final static String mcro       = "\u00B5"; // micro sign
  public final static String para       = "\u00B6"; // pilcrow sign
  public final static String middot     = "\u00B7"; // middle dot
  public final static String cedil      = "\u00B8"; // cedilla
  public final static String sup1       = "\u00B9"; // superscrip one
  public final static String ordm       = "\u00BA"; // masculine ordinal indicator
  public final static String Gt         = "\u00BB"; // right pointing double angle quotation mark
  public final static String frac14     = "\u00BC"; // vulgar fraction one quarter
  public final static String frac12     = "\u00BD"; // vulgar fraction one half
  public final static String frac34     = "\u00BE"; // vulagar fraction three quarter
  public final static String iquest     = "\u00BF"; // inverted question mark
  
  public final static String Agrave     = "\u00C0";
  public final static String Aacute     = "\u00C1";
  public final static String Acirc      = "\u00C2";
  public final static String Atilde     = "\u00C3";
  public final static String Auml       = "\u00C4";
  public final static String Aring      = "\u00C5";
  public final static String AElig      = "\u00C6";
  
  public final static String Ccedil     = "\u00C7";
  
  public final static String Egrave     = "\u00C8";
  public final static String Eacute     = "\u00C9";
  public final static String Ecirc      = "\u00CA";
  public final static String Euml       = "\u00CB";
  
  public final static String Igrave     = "\u00CC";
  public final static String Iacute     = "\u00CD";
  public final static String Icirci     = "\u00CE";
  public final static String Iuml       = "\u00CF";
  
  public final static String ETH        = "\u00D0"; // latin capital letter ETH
  public final static String Ntilde     = "\u00D1";

  public final static String Ograve     = "\u00D2";
  public final static String Oacute     = "\u00D3";
  public final static String Ocirc      = "\u00D4";
  public final static String Otilde     = "\u00D5";
  public final static String Ouml       = "\u00D6";
  
  public final static String times      = "\u00D7"; // multiplication sign
  public final static String Ostrok     = "\u00D8";

  public final static String Ugrave     = "\u00D9";
  public final static String Uacute     = "\u00DA";
  public final static String Ucircr     = "\u00DB";
  public final static String Uuml       = "\u00DC";
  
  public final static String Yacute     = "\u00DD";
  public final static String THORN      = "\u00DE"; // latin capital letter thorn

  public final static String szlig      = "\u00DF";
  
  public final static String agrave     = "\u00E0";
  public final static String aacute     = "\u00E1";
  public final static String acirc      = "\u00E2";
  public final static String atilde     = "\u00E3";
  public final static String auml       = "\u00E4";
  public final static String aring      = "\u00E5";
  public final static String aelig      = "\u00E6";
  
  public final static String ccedil     = "\u00E7";
  
  public final static String egrave     = "\u00E8";
  public final static String eacute     = "\u00E9";
  public final static String ecirc      = "\u00EA";
  public final static String euml       = "\u00EB";
  
  public final static String igrave     = "\u00EC";
  public final static String iacute     = "\u00ED";
  public final static String icirc      = "\u00EE";
  public final static String iuml       = "\u00EF";
  
  public final static String eth        = "\u00F0"; // latin small letter eth
  public final static String ntilde     = "\u00F1";
  
  public final static String ograve     = "\u00F2";
  public final static String oacute     = "\u00F3";
  public final static String ocirc      = "\u00F4";
  public final static String otilde     = "\u00F5";
  public final static String ouml       = "\u00F6";
  
  public final static String divide     = "\u00F7"; // division sign
  public final static String ostrok     = "\u00F8";
  
  public final static String ugrave     = "\u00F9";
  public final static String ucute      = "\u00FA";
  public final static String ucircr     = "\u00FB";
  public final static String uuml       = "\u00FC";
  
  public final static String yacute     = "\u00FD";
  public final static String thorn      = "\u00FE"; // latin small letter thorn
  public final static String yuml       = "\u00FF";

  public final static String inodot      = "\u0131"; // LATIN SMALL LETTER DOTLESS I
  public final static String OElig       = "\u0152"; // LATIN CAPITAL LIGATURE OE
  public final static String oelig       = "\u0153"; // LATIN SMALL LIGATURE OE
  public final static String Yuml        = "\u0178"; // LATIN CAPITAL LETTER Y WITH DIAERESIS
  public final static String fnof        = "\u0192"; // LATIN SMALL LETTER F WITH HOOK
  public final static String circ        = "\u02C6"; // MODIFIER LETTER CIRCUMFLEX ACCENT
  public final static String caron       = "\u02C7"; // CARON
  public final static String breve       = "\u02D8"; // BREVE
  public final static String dot         = "\u02D9"; // DOT ABOVE
  public final static String ring        = "\u02DA"; // RING ABOVE
  public final static String ogon        = "\u02DB"; // OGONEK
  public final static String tilde       = "\u02DC"; // SMALL TILDE
  public final static String dblac       = "\u02DD"; // DOUBLE ACUTE ACCENT
  public final static String OHgr        = "\u03A9"; // GREEK CAPITAL LETTER OMEGA
  public final static String b_pi        = "\u03C0"; // GREEK SMALL LETTER PI
  public final static String ndash       = "\u2013"; // EN DASH
  public final static String mdash       = "\u2014"; // EM DASH
  public final static String rsquor      = "\u2018"; // LEFT SINGLE QUOTATION MARK
  public final static String rsquo       = "\u2019"; // RIGHT SINGLE QUOTATION MARK
  public final static String lsquor      = "\u201A"; // SINGLE LOW-9 QUOTATION MARK
  public final static String ldquo       = "\u201C"; // LEFT DOUBLE QUOTATION MARK
  public final static String rdquo       = "\u201D"; // RIGHT DOUBLE QUOTATION MARK
  public final static String bdquo       = "\u201E"; // DOUBLE LOW-9 QUOTATION MARK
  public final static String dagger      = "\u2020"; // DAGGER
  public final static String Dagger      = "\u2021"; // DOUBLE DAGGER
  public final static String bull        = "\u2022"; // BULLET
  public final static String hellip      = "\u2026"; // HORIZONTAL ELLIPSIS
  public final static String permil      = "\u2030"; // PER MILLE SIGN
  public final static String lsaquo      = "\u2039"; // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
  public final static String rsaquo      = "\u203A"; // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
  public final static String frasl       = "\u2044"; // FRACTION SLASH
  public final static String euro        = "\u20AC"; // EURO SIGN
  public final static String trade       = "\u2122"; // TRADE MARK SIGN
  public final static String part        = "\u2202"; // PARTIAL DIFFERENTIAL
  public final static String Delta       = "\u2206"; // INCREMENT
  public final static String prod        = "\u220F"; // N-ARY PRODUCT
  public final static String sum         = "\u2211"; // N-ARY SUMMATION
  public final static String radic       = "\u221A"; // SQUARE ROOT
  public final static String infin       = "\u221E"; // INFINITY
  public final static String int_        = "\u222B"; // INTEGRAL
  public final static String thkap       = "\u2248"; // ALMOST EQUAL TO
  public final static String ne          = "\u2260"; // NOT EQUAL TO
  public final static String le          = "\u2264"; // LESS-THAN OR EQUAL TO
  public final static String ge          = "\u2265"; // GREATER-THAN OR EQUAL TO
  public final static String loz         = "\u25CA"; // LOZENGE
  public final static String _APPLE_LOGO = "\uF8FF"; // Apple logo
  public final static String filig       = "\uFB01"; // LATIN SMALL LIGATURE FI
  public final static String fllig       = "\uFB02"; // LATIN SMALL LIGATURE FL
}
/*
  $Log: Unicode.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.6  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.4  1999/12/14 12:26:30  schubige
  Fixed find bug in variable

  Revision 1.3  1999/09/03 15:50:09  schubige
  Changed to new header & log conventions.
  
*/
