package iiuf.util;

import java.util.Vector;

/**
   Unicode to TeX translator.
   
   Needs package textcomp for certain translations.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class UTTeX 
  extends 
  UnicodeTranslator
  implements
  Unicode {

  private final static String[] TEX2UNICODE_EXCL = {
    shy         , "-",  // soft hyphen
    ldquo       , "``", // LEFT DOUBLE QUOTATION MARK
    rdquo       , "''", // RIGHT DOUBLE QUOTATION MARK
    rsquor      , "`",  // LEFT SINGLE QUOTATION MARK
    rsquo       , "'",  // RIGHT SINGLE QUOTATION MARK
    filig       , "fi", // LATIN SMALL LIGATURE FI
    fllig       , "fl", // LATIN SMALL LIGATURE FL
  };

  private final static String[] UNICODE2TEX_EXP = 
  {
    nbsp   , "~",   // non breaking space
    iexcl  , "!'",  // inverted exclamation mark
    cent   , "\\textcent{}",   // cent sign
    pound  , "\\pounds{}",  // pund sign
    curren , "\\textcurrency{}", // currency sign
    yen    , "\\textyen{}",    // yen sign
    brvbar , "\\textbrokenbar{}", // broken bar
    sect   , "\\S{}",   // section sign
    Dot    , "\\textasciidieresis{}",    // diaeresis
    copy   , "\\copyright{}",   // copyright sign
    ordf   , "\\textorffeminine{}",   // feminine ordinal indicator
    Lt     , "\\guillemotleft{}",     // left pointing double angle quotation mark ()
    not    , "\\textlnot{}",    // not sign
    reg    , "\\textregistered{}",    // registered sign
    macr   , "\\textmacron",   // macron
    
    deg    , "$^\\circ$", // degree sign
    plusmn , "$^\\pm$",  // plus minus sign
    sup2   , "\\textwosuperior",   // superscript two
    sup3   , "\\textthreesuperior",   // superscript three
    acute  , "\\textasciiacute",  // acute accent
    mcro   , "\\textmu",   // micro sign
    para   , "\\P{}",   // pilcrow sign
    middot , "\\textperiodcentered", // middle dot
    cedil  , "\\c{}",  // cedilla
    sup1   , "\\textonesuperior",   // superscrip one
    ordm   , "\\textordmasculine",   // masculine ordinal indicator
    Gt     , "\\guillemotright",     // right pointing double angle quotation mark
    frac14 , "${}^1\\!/\\!_4$", // vulgar fraction one quarter
    frac12 , "${}^1\\!/\\!_2$", // vulgar fraction one half
    frac34 , "${}^3\\!/\\!_4$", // vulagar fraction three quarter
    iquest , "?'", // inverted question mark

    Agrave , "\\`A",
    Aacute , "\\'A",
    Acirc  , "\\^A",
    Atilde , "\\~A",
    Auml   , "\\\"A",
    Aring  , "\\AA{}",
    AElig  , "\\AE{}",
    
    Ccedil , "\\c{C}",
    
    Egrave , "\\`E",
    Eacute , "\\'E",
    Ecirc  , "\\^E",
    Euml   , "\\\"E", 
    
    Igrave , "\\`I",
    Iacute , "\\'I",
    Icirci , "\\^I",
    Iuml   , "\\\"I",
    
    ETH    , "\\DH{}",    // latin capital letter ETH
    Ntilde , "\\~N",

    Ograve , "\\`O",
    Oacute , "\\'O",
    Ocirc  , "\\^O",
    Otilde , "\\~O",
    Ouml   , "\\\"O",
    
    times  , "\\texttimes{}",  // multiplication sign
    Ostrok , "\\O{}",

    Ugrave , "\\`U",
    Uacute , "\\'U",
    Ucircr , "\\^U",
    Uuml   , "\\\"U",
    
    Yacute , "\\'Y",
    THORN  , "\\TH{}", // latin capital letter thorn

    szlig  , "\\ss",
    szlig  , "\"s",
    
    agrave , "\\`a",
    aacute , "\\'a",
    acirc  , "\\^a",
    atilde , "\\~a",
    auml   , "\\\"a",
    aring  , "\\aa{}",
    aelig  , "\\ae{}",
    
    ccedil , "\\c{c}",
    
    egrave , "\\`e",
    eacute , "\\'e",
    ecirc  , "\\^e",
    euml   , "\\\"e",
    
    igrave , "\\`i",
    iacute , "\\'i",
    icirc  , "\\^i",
    iuml   , "\\\"i",

    igrave , "\\`{\\i}",
    iacute , "\\'{\\i}",
    icirc  , "\\^{\\i}",
    iuml   , "\\\"{\\i}",
    
    eth    , "\\dh{}",   // latin small letter eth
    ntilde , "\\~n",

    ograve , "\\`o",
    oacute , "\\'o",
    ocirc  , "\\^o",
    otilde , "\\~o",
    ouml   , "\\\"o",
    
    divide , "\\textdiv", // division sign
    ostrok , "\\o{}",

    ugrave , "\\`u",
    ucute  , "\\'u",
    ucircr , "\\^u",
    uuml   , "\\\"u",
    
    yacute , "\\'y",
    thorn  , "\\th{}", // latin small letter thorn
    yuml   , "\\\"y",
    
    inodot      , "\\i", // LATIN SMALL LETTER DOTLESS I
    OElig       , "\\OE{}", // LATIN CAPITAL LIGATURE OE
    oelig       , "\\oe{}", // LATIN SMALL LIGATURE OE
    Yuml        , "\\\"Y", // LATIN CAPITAL LETTER Y WITH DIAERESIS
    fnof        , "(fnof:not translated)", // LATIN SMALL LETTER F WITH HOOK
    circ        , "\\c{}", // MODIFIER LETTER CIRCUMFLEX ACCENT
    caron       , "\\v{}", // CARON
    breve       , "\\u{}", // BREVE
    dot         , "\\.{}", // DOT ABOVE
    ring        , "(ring:not translated)", // RING ABOVE
    ogon        , "(ogon:not translated)", // OGONEK
    tilde       , "\\~{}", // SMALL TILDE
    dblac       , "\\H{}", // DOUBLE ACUTE ACCENT
    OHgr        , "$\\Omega$", // GREEK CAPITAL LETTER OMEGA
    b_pi        , "$\\pi$", // GREEK SMALL LETTER PI
    mdash       , "---", // EM DASH
    ndash       , "--", // EN DASH
    lsquor      , "\\glq", // SINGLE LOW-9 QUOTATION MARK
    bdquo       , "\\glqq", // DOUBLE LOW-9 QUOTATION MARK
    dagger      , "\\dag", // DAGGER
    Dagger      , "\\ddag", // DOUBLE DAGGER
    bull        , "$\\bullet$", // BULLET
    hellip      , "\\mbox{...}", // HORIZONTAL ELLIPSIS
    permil      , "\\textperthousand", // PER MILLE SIGN
    lsaquo      , "\\flq", // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
    rsaquo      , "\\frq", // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
    frasl       , "$/$", // FRACTION SLASH
    euro        , "\\texteuro", // EURO SIGN
    trade       , "$^(TM)$", // TRADE MARK SIGN
    trade       , "$^{TM}$", // TRADE MARK SIGN
    part        , "$\\partial$", // PARTIAL DIFFERENTIAL
    Delta       , "$\\Delta$", // capital greek delta
    prod        , "$\\prod$", // N-ARY PRODUCT
    sum         , "$\\sum$", // N-ARY SUMMATION
    radic       , "$\\surd$", // SQUARE ROOT
    infin       , "$\\infty$", // INFINITY
    int_        , "$\\int$", // INTEGRAL
    thkap       , "$\\approx$", // ALMOST EQUAL TO
    ne          , "$\\not=$", // NOT EQUAL TO
    le          , "$\\leq$", // LESS-THAN OR EQUAL TO
    ge          , "$\\geq$", // GREATER-THAN OR EQUAL TO
    loz         , "$\\diamondsuit$", // LOZENGE
    _APPLE_LOGO , "(APPLE_LOGO:not translated)", // Apple logo
  };
  
  private final static String[] UNICODE2TEX_NEXP = {
    "~"    , "$\\sim$",
    "~"    , "{\\verb\"~\"}",
    
    "#"    , "\\#",
    "$"    , "\\$",
    "%"    , "\\%",
    "_"    , "\\_",
    "{"    , "\\{",
    "}"    , "\\}",
    "&"    , "\\&",
  };
  
  private final static String[] _UNICODE2TEX = Strings.arraycat(TEX2UNICODE_EXCL,
								Strings.arraycat(UNICODE2TEX_NEXP, UNICODE2TEX_EXP));
  
  
  static String[] UNICODE2TEX = Strings.arraycat(TEX2UNICODE_EXCL,
						 Strings.arraycat(UNICODE2TEX_NEXP,
								  Strings.arraycat(mathExpand(UNICODE2TEX_EXP), 
										   expand(UNICODE2TEX_EXP))));

  private static String[] UNICODE2TEX2 = Strings.arraycat(UNICODE2TEX_NEXP,
							  Strings.arraycat(mathExpand(UNICODE2TEX_EXP), 
									   expand(UNICODE2TEX_EXP)));
  
  /** The default translator instance. */
  public static UnicodeTranslator trans = new UTTeX();
  
  private UTTeX() {
    super(_UNICODE2TEX);
    
    /*
    for(int i = 0; i < UNICODE2TEX.length; i+= 2)
      System.out.println(UNICODE2TEX[i] + "->" + UNICODE2TEX[i + 1]);
      */
  }
  
  private static String[] mathExpand(String[] source) {
    Vector   resultv = new Vector();
    for(int i = 1; i < source.length; i += 2) {
      if(!source[i].equals("\\$") && source[i].endsWith("$")) {
	resultv.addElement(source[i - 1]);
	resultv.addElement("\\ensuremath{" + source[i].substring(1, source[i].length() - 1) + "}");
	resultv.addElement(source[i - 1]);
	resultv.addElement(source[i]);
      }
      else {
	resultv.addElement(source[i - 1]);
	resultv.addElement(source[i]);
      }
    }
    
    String[] result = new String[resultv.size()];
    
    for(int i = 0; i < result.length; i++)
      result[i] = (String)resultv.elementAt(i);

    return result;
  }
  
  private static String[] expand(String[] source) {
    String[] result = new String[source.length];
    for(int i = 1; i < result.length; i+= 2) {
      result[i - 1] = source[i - 1];
      if(source[i].endsWith("}") || source[i].endsWith("$") || source[i].length() <= 1)
	result[i] = source[i];
      else
	result[i] = Strings.rightTrunc(source[i], 1) + "{" + source[i].substring(source[i].length() - 1) + "}";
    }
    
    return result;
  }
  
  public String getUnicode(String nstr) {
    return replace(nstr, UNICODE2TEX2);
  }
  
  private String replace(String in, String[] table) {
    String result = in;
    for(int i = 0; i < table.length; i += 2) {
      result = replaceOne(result, table[i + 1], table[i], table);
      if(!result.equals(in)) break;
    }
    return result;
  }
  
  private String replaceOne(String in, String that, String by, String[] table) {
    if(that.equals("")) return in;
    
    int index = in.indexOf(that);
    if(index == -1) return in;
    String[] prepost = {in.substring(0, index), in.substring(index + that.length())};
    
    for(;;) {
      String[] tmp = {prepost[0].trim(), prepost[1].trim()};
      
      if(!tmp[0].endsWith("{") || !tmp[1].startsWith("}")) break;
      
      prepost[0] = tmp[0].substring(0, tmp[0].length() - 1);
      prepost[1] = tmp[1].substring(1);
    } 
    
    return replace(prepost[0], table) + by + replace(prepost[1], table);
  }
  
  public String getNative(String unicode) {
    return UnicodeTrans.trans(unicode, UNICODE2TEX);
  }

  public static void main(String[] argv) {
    System.out.println("getNative("  + argv[0] + ") = " + UTTeX.trans.getNative (argv[0]));
    System.out.println("getUnicode(" + argv[0] + ") = " + UTTeX.trans.getUnicode(argv[0]));
  }
}
/*
  $Log: UTTeX.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.6  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/05/04 09:06:01  schubige
  *** empty log message ***

  Revision 1.4  2000/05/02 14:33:33  schubige
  intermediate checkin for iiuf.util.UTTeX sync.

  Revision 1.3  2000/05/01 12:41:42  schubige
  intermediate checkin after UT update

  Revision 1.2  2000/04/27 09:32:38  schubige
  intermediate checkin for sybase proxy lower() based where clause

  Revision 1.1  2000/04/25 12:11:15  schubige
  pre bibtex restart commit

*/
