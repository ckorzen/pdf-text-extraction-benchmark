package iiuf.util;

/**
   Unicode to HTML translator.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class UTHTML 
  extends 
  UnicodeTranslator
  implements
  Unicode {
    
  final static String[] UNICODE2HTML = 
  {
    nbsp   , "&nbsp;",   // non breaking space
    iexcl  , "&iexcl;",  // inverted exclamation mark
    cent   , "&cent;",   // cent sign
    pound  , "&pound;",  // pund sign
    curren , "&curren;", // currency sign
    yen    , "&yen;",    // yen sign
    brvbar , "&brvbar;", // broken bar
    sect   , "&sect;",   // section sign
    Dot    , "&Dot;",    // diaeresis
    copy   , "&copy;",   // copyright sign
    ordf   , "&ordf;",   // feminine ordinal indicator
    Lt     , "&Lt;",     // left pointing double angle quotation mark ()
    not    , "&not;",    // not sign
    shy    , "&shy;",    // soft hyphen
    reg    , "&reg;",    // registered sign
    macr   , "&macr;",   // macron
    
    deg    , "&deg;",    // degree sogn
    plusmn , "&plusm;",  // plus minus sign
    sup2   , "&sup2;",   // superscript two
    sup3   , "&sup3;",   // superscript three
    acute  , "&acute;",  // acute accent
    mcro   , "&mcro;",   // micro sign
    para   , "&para;",   // pilcrow sign
    middot , "&middot;", // middle dot
    cedil  , "&cedil;",  // cedilla
    sup1   , "&sup1;",   // superscrip one
    ordm   , "&ordm;",   // masculine ordinal indicator
    Gt     , "&Gt;",     // right pointing double angle quotation mark
    frac14 , "&frac14;", // vulgar fraction one quarter
    frac12 , "&frac12;", // vulgar fraction one half
    frac34 , "&frac34;", // vulagar fraction three quarter
    iquest , "&iquest;", // inverted question mark
    
    Agrave , "&Agrave;",
    Aacute , "&Aacute;",
    Acirc  , "&Acirc;",
    Atilde , "&Atilde;",
    Auml   , "&Auml;",
    Aring  , "&Aring;",
    AElig  , "&AElig;",
    
    Ccedil , "&Ccedil;",
    
    Egrave , "&Egrave;",
    Eacute , "&Eacute;",
    Ecirc  , "&Ecirc;",
    Euml   , "&Euml;",
    
    Igrave , "&Igrave;",
    Iacute , "&Iacute;",
    Icirci , "&Icirci;",
    Iuml   , "&Iuml;",
    
    ETH    , "&ETH;",    // latin capital letter ETH
    Ntilde , "&Ntilde;",
    
    Ograve , "&Ograve;",
    Oacute , "&Oacute;", 
    Ocirc  , "&Ocirc;",
    Otilde , "&Otilde;",
    Ouml   , "&Ouml;",
    
    times  , "&times;",  // multiplication sign
    Ostrok , "&Ostrok;",

    Ugrave , "&Ugrave;",
    Uacute , "&Uacute;",
    Ucircr , "&Ucircr;",
    Uuml   , "&Uuml;",
    
    Yacute , "&Yacute;",
    THORN  , "&THORN;", // latin capital letter thorn

    szlig  , "&szlig;",
    
    agrave , "&agrave;",
    aacute , "&aacute;",
    acirc  , "&acirc;",
    atilde , "&atilde;",
    auml   , "&auml;",
    aring  , "&aring;",
    aelig  , "&aelig;",
    
    ccedil , "&ccedil;",
    
    egrave , "&egrave;",
    eacute , "&eacute;",
    ecirc  , "&ecirc;",
    euml   , "&euml;",
    
    igrave , "&igrave;",
    iacute , "&iacute;",
    icirc  , "&icirc;",
    iuml   , "&iuml;",
    
    eth    , "&eth;",   // latin small letter eth
    ntilde , "&ntilde;",

    ograve , "&ograve;",
    oacute , "&oacute;",
    ocirc  , "&ocirc;",
    otilde , "&otilde;",
    ouml   , "&ouml;",
    
    divide , "&divide;", // division sign
    ostrok , "&ostrok;",

    ugrave , "&ugrave;",
    ucute  , "&ucute;",
    ucircr , "&ucircr;",
    uuml   , "&uuml;",

    yacute , "&yacute;",
    thorn  , "&thorn;", // latin small letter thorn
    yuml   , "&yuml;",
    
    inodot      , "&inodot;", // LATIN SMALL LETTER DOTLESS I
    OElig       , "&OElig;", // LATIN CAPITAL LIGATURE OE
    oelig       , "&oelig;", // LATIN SMALL LIGATURE OE
    Yuml        , "&Yuml;", // LATIN CAPITAL LETTER Y WITH DIAERESIS
    fnof        , "&fnof;", // LATIN SMALL LETTER F WITH HOOK
    circ        , "&circ;", // MODIFIER LETTER CIRCUMFLEX ACCENT
    caron       , "&caron;", // CARON
    breve       , "&breve;", // BREVE
    dot         , "&dot;", // DOT ABOVE
    ring        , "&ring;", // RING ABOVE
    ogon        , "&ogon;", // OGONEK
    tilde       , "&tilde;", // SMALL TILDE
    dblac       , "&dblac;", // DOUBLE ACUTE ACCENT
    OHgr        , "&OHgr;", // GREEK CAPITAL LETTER OMEGA
    b_pi        , "&b_pi;", // GREEK SMALL LETTER PI
    ndash       , "&ndash;", // EN DASH
    mdash       , "&mdash;", // EM DASH
    rsquor      , "&rsquor;", // LEFT SINGLE QUOTATION MARK
    rsquo       , "&rsquo;", // RIGHT SINGLE QUOTATION MARK
    lsquor      , "&lsquor;", // SINGLE LOW-9 QUOTATION MARK
    ldquo       , "&ldquo;", // LEFT DOUBLE QUOTATION MARK
    rdquo       , "&rdquo;", // RIGHT DOUBLE QUOTATION MARK
    bdquo       , "&bdquo;", // DOUBLE LOW-9 QUOTATION MARK
    dagger      , "&dagger;", // DAGGER
    Dagger      , "&Dagger;", // DOUBLE DAGGER
    bull        , "&bull;", // BULLET
    hellip      , "&hellip;", // HORIZONTAL ELLIPSIS
    permil      , "&permil;", // PER MILLE SIGN
    lsaquo      , "&lsaquo;", // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
    rsaquo      , "&rsaquo;", // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
    frasl       , "&frasl;", // FRACTION SLASH
    euro        , "&euro;", // EURO SIGN
    trade       , "&trade;", // TRADE MARK SIGN
    part        , "&part;", // PARTIAL DIFFERENTIAL
    Delta       , "&Delta;", // INCREMENT
    prod        , "&prod;", // N-ARY PRODUCT
    sum         , "&sum;", // N-ARY SUMMATION
    radic       , "&radic;", // SQUARE ROOT
    infin       , "&infin;", // INFINITY
    int_        , "&int;", // INTEGRAL
    thkap       , "&thkap;", // ALMOST EQUAL TO
    ne          , "&nr;", // NOT EQUAL TO
    le          , "&le;", // LESS-THAN OR EQUAL TO
    ge          , "&ge;", // GREATER-THAN OR EQUAL TO
    loz         , "&loz;", // LOZENGE
    _APPLE_LOGO , "_APPLE_LOGO", // Apple logo
    filig       , "&filig;", // LATIN SMALL LIGATURE FI
    fllig       , "&fllig;", // LATIN SMALL LIGATURE FL
  };
  static String[] HTML2UNICODE = invTable(UNICODE2HTML);
  
  /** The default translator instance. */  
  public static UnicodeTranslator trans = new UTHTML();

  private UTHTML() {
    super(UNICODE2HTML);
  }
  
  public String getUnicode(String nstr) {
    return UnicodeTrans.trans(nstr, HTML2UNICODE);
  }
  
  public String getNative(String unicode) {
    return UnicodeTrans.trans(unicode, UNICODE2HTML);
  }

  public static void main(String[] argv) {
    System.out.println("getNative("  + argv[0] + ") = " + UTHTML.trans.getNative (argv[0]));
    System.out.println("getUnicode(" + argv[0] + ") = " + UTHTML.trans.getUnicode(argv[0]));
  }
}
/*
  $Log: UTHTML.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.1  2000/04/25 12:11:15  schubige
  pre bibtex restart commit

*/
