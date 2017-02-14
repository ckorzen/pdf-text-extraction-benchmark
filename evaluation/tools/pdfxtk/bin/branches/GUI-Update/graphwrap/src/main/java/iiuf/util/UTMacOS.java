package iiuf.util;

/**
   Unicode MacOS translator.

   \u00F0 used for non-mapped codes.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class UTMacOS 
  extends 
  UnicodeTranslator
  implements
  Unicode {
  
  final static String[] UNICODE2MACOS = 
  {
    nbsp   , "\u00CA", // non breaking space
    iexcl  , "\u00C1", // inverted exclamation mark
    cent   , "\u00A2", // cent sign
    pound  , "\u00A3", // pund sign
    curren , "\u00F0", // currency sign
    yen    , "\u00B4", // yen sign
    brvbar , "\u00F0", // broken bar
    sect   , "\u00A4", // section sign
    Dot    , "\u00AC", // diaeresis
    copy   , "\u00A9", // copyright sign
    ordf   , "\u00BB", // feminine ordinal indicator
    Lt     , "\u00C7", // left pointing double angle quotation mark ()
    not    , "\u00C2", // not sign
    shy    , "\u00D0", // soft hyphen
    reg    , "\u00A8", // registered sign
    macr   , "\u00F8", // macron
    
    deg    , "\u00A1", // degree sign
    plusmn , "\u00B1", // plus minus sign
    sup2   , "\u00F0", // superscript two
    sup3   , "\u00F0", // superscript three
    acute  , "\u00AB", // acute accent
    mcro   , "\u00B5", // micro sign
    para   , "\u00A6", // pilcrow sign
    middot , "\u00E1", // middle dot
    cedil  , "\u00FC", // cedilla
    sup1   , "\u00F0", // superscrip one
    ordm   , "\u00BC", // masculine ordinal indicator
    Gt     , "\u00C8", // right pointing double angle quotation mark
    frac14 , "\u00F0", // vulgar fraction one quarter
    frac12 , "\u00F0", // vulgar fraction one half
    frac34 , "\u00F0", // vulagar fraction three quarter
    iquest , "\u00C0", // inverted question mark
    
    Agrave , "\u00CB",
    Aacute , "\u00E7",
    Acirc  , "\u00E5",
    Atilde , "\u00CC",
    Auml   , "\u0080",
    Aring  , "\u0081",
    AElig  , "\u00AE",
    
    Ccedil , "\u0082",
    
    Egrave , "\u00E9",
    Eacute , "\u0083",
    Ecirc  , "\u00E6",
    Euml   , "\u00E8",
    
    Igrave , "\u00ED",
    Iacute , "\u00EA",
    Icirci , "\u00EB",
    Iuml   , "\u00EC",
    
    ETH    , "\u00F0", // latin capital letter ETH
    Ntilde , "\u0084",

    Ograve , "\u00F1",
    Oacute , "\u00EE", 
    Ocirc  , "\u00EF",
    Otilde , "\u00CD",
    Ouml   , "\u0085",
    
    times  , "\u00F0", // multiplication sign
    Ostrok , "\u00AF",

    Ugrave , "\u00F4",
    Uacute , "\u00F2",
    Ucircr , "\u00F3",
    Uuml   , "\u0086",
    
    Yacute , "\u00F0",
    THORN  , "\u00F0", // latin capital letter thorn

    szlig  , "\u00A7",
    
    agrave , "\u0088",
    aacute , "\u0087",
    acirc  , "\u0089",
    atilde , "\u008B",
    auml   , "\u008A",
    aring  , "\u008C",
    aelig  , "\u00BE",
    
    ccedil , "\u008D",
    
    egrave , "\u008F",
    eacute , "\u008E",
    ecirc  , "\u0090",
    euml   , "\u0091",
    
    igrave , "\u0093",
    iacute , "\u0092",
    icirc  , "\u0094",
    iuml   , "\u0095",
    
    eth    , "\u00F0", // latin small letter eth
    ntilde , "\u0096",

    ograve , "\u0098",
    oacute , "\u0097",
    ocirc  , "\u0099",
    otilde , "\u009B",
    ouml   , "\u009A",
    
    divide , "\u00D6", // division sign
    ostrok , "\u00BF",

    ugrave , "\u009D",
    ucute  , "\u009C",
    ucircr , "\u009E",
    uuml   , "\u009F",

    yacute , "\u00F0",
    thorn  , "\u00F0", // latin small letter thorn
    yuml   , "\u00D8",
    
    "'",     "\u00AB",
    "'",     "\u00D5",
    "`",     "\u00D4",

    inodot      , "\u00F5", // LATIN SMALL LETTER DOTLESS I
    OElig       , "\u00CE", // LATIN CAPITAL LIGATURE OE
    oelig       , "\u00CF", // LATIN SMALL LIGATURE OE
    Yuml        , "\u00D9", // LATIN CAPITAL LETTER Y WITH DIAERESIS
    fnof        , "\u00C4", // LATIN SMALL LETTER F WITH HOOK
    circ        , "\u00F6", // MODIFIER LETTER CIRCUMFLEX ACCENT
    caron       , "\u00FF", // CARON
    breve       , "\u00F9", // BREVE
    dot         , "\u00FA", // DOT ABOVE
    ring        , "\u00FB", // RING ABOVE
    ogon        , "\u00FE", // OGONEK
    tilde       , "\u00F7", // SMALL TILDE
    dblac       , "\u00FD", // DOUBLE ACUTE ACCENT
    OHgr        , "\u00BD", // GREEK CAPITAL LETTER OMEGA
    b_pi        , "\u00B9", // GREEK SMALL LETTER PI
    ndash       , "\u00D0", // EN DASH
    mdash       , "\u00D1", // EM DASH
    rsquor      , "\u00D4", // LEFT SINGLE QUOTATION MARK
    rsquo       , "\u00D5", // RIGHT SINGLE QUOTATION MARK
    lsquor      , "\u00E2", // SINGLE LOW-9 QUOTATION MARK
    ldquo       , "\u00D2", // LEFT DOUBLE QUOTATION MARK
    rdquo       , "\u00D3", // RIGHT DOUBLE QUOTATION MARK
    bdquo       , "\u00E3", // DOUBLE LOW-9 QUOTATION MARK
    dagger      , "\u00A0", // DAGGER
    Dagger      , "\u00E0", // DOUBLE DAGGER
    bull        , "\u00A5", // BULLET
    hellip      , "\u00C9", // HORIZONTAL ELLIPSIS
    permil      , "\u00E4", // PER MILLE SIGN
    lsaquo      , "\u00DC", // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
    rsaquo      , "\u00DD", // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
    frasl       , "\u00DA", // FRACTION SLASH
    euro        , "\u00DB", // EURO SIGN
    trade       , "\u00AA", // TRADE MARK SIGN
    part        , "\u00B6", // PARTIAL DIFFERENTIAL
    Delta       , "\u00C6", // capital greek delta
    prod        , "\u00B8", // N-ARY PRODUCT
    sum         , "\u00B7", // N-ARY SUMMATION
    radic       , "\u00C3", // SQUARE ROOT
    infin       , "\u00B0", // INFINITY
    int_        , "\u00BA", // INTEGRAL
    thkap       , "\u00C5", // ALMOST EQUAL TO
    ne          , "\u00AD", // NOT EQUAL TO
    le          , "\u00B2", // LESS-THAN OR EQUAL TO
    ge          , "\u00B3", // GREATER-THAN OR EQUAL TO
    loz         , "\u00D7", // LOZENGE
    _APPLE_LOGO , "\u00F0", // Apple logo
    filig       , "\u00DE", // LATIN SMALL LIGATURE FI
    fllig       , "\u00DF", // LATIN SMALL LIGATURE FL
    
  };
  static String[] MACOS2UNICODE = invTable(UNICODE2MACOS);

  /** The default translator instance. */
  public static UnicodeTranslator trans = new UTMacOS();

  private UTMacOS() {
    super(UNICODE2MACOS);
  }

  public String getUnicode(String nstr) {
    return UnicodeTrans.trans(nstr, MACOS2UNICODE);
  }
  
  public String getNative(String unicode) {
    return UnicodeTrans.trans(unicode, UNICODE2MACOS);
  }

  public static void main(String[] argv) {
    System.out.println("getNative("  + argv[0] + ") = " + UTMacOS.trans.getNative (argv[0]));
    System.out.println("getUnicode(" + argv[0] + ") = " + UTMacOS.trans.getUnicode(argv[0]));
  }
}
/*
  $Log: UTMacOS.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.1  2000/04/25 12:11:15  schubige
  pre bibtex restart commit

*/
