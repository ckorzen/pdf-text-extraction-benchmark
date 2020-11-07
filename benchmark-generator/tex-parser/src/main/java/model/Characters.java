package model;

import java.util.HashSet;

public class Characters {
  public static final HashSet<Character> BASELINE_PUNCTUATION_MARKS;
  public static final HashSet<Character> MEANLINE_PUNCTUATION_MARKS;
  public static final HashSet<Character> DESCENDERS;
  public static final HashSet<Character> ASCENDERS;
  public static final HashSet<Character> BASELINE_CHARACTERS;
  public static final HashSet<Character> MEANLINE_CHARACTERS;
  public static final HashSet<String> MATH_SYMBOLS;
  public static final HashSet<String> MATH_OPERATORS;
  // Math symbols that should be surrounded by whitespaces on normalizing a formula.
  public static final HashSet<String> MATH_SYMBOLS_SURROUNDED_BY_SPACES;
  // Math symbols that should be followed (but not preceded) by a space on normalizing a formula.
  public static final HashSet<String> MATH_SYMBOLS_FOLLOWED_BY_SPACE;
  // Math symbols that should *not* be surrounded by whitespaces on normalizing a formula.
  public static final HashSet<String> MATH_SYMBOLS_NO_SPACES;

  // public static boolean isLetter(final PdfCharacter character) {
  // return isLetter(character.getUnicode());
  // }

  public static boolean isLetter(final String character) {
    return isLetter(character.charAt(0));
  }

  public static boolean isLetter(final char character) {
    return Character.isLetter(character);
  }

  // public static boolean isLatinLetter(final PdfCharacter character) {
  // return isLatinLetter(character.getUnicode());
  // }

  public static boolean isLatinLetter(final String character) {
    return isLatinLetter(character.charAt(0));
  }

  public static boolean isLatinLetter(final char ch) {
    return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
  }

  // public static boolean isLetterOrDigit(final PdfCharacter character) {
  // return isLetterOrDigit(character.getUnicode());
  // }

  public static boolean isLetterOrDigit(final String character) {
    return isLetterOrDigit(character.charAt(0));
  }

  public static boolean isLetterOrDigit(final char character) {
    return Character.isLetterOrDigit(character);
  }

  // public static boolean isLatinLetterOrDigit(final PdfCharacter character) {
  // return isLatinLetterOrDigit(character.getUnicode());
  // }

  public static boolean isLatinLetterOrDigit(final String character) {
    return isLatinLetterOrDigit(character.charAt(0));
  }

  public static boolean isLatinLetterOrDigit(final char character) {
    return isLatinLetter(character) || Character.isDigit(character);
  }

  // public static boolean isPunctuationMark(final PdfCharacter character) {
  // return isPunctuationMark(character.getUnicode());
  // }

  public static boolean isPunctuationMark(final String character) {
    return isPunctuationMark(character.charAt(0));
  }

  public static boolean isPunctuationMark(final char character) {
    return Characters.BASELINE_PUNCTUATION_MARKS.contains(character)
        || Characters.MEANLINE_PUNCTUATION_MARKS.contains(character);
  }

  // public static boolean isMeanlinePunctuationMark(final PdfCharacter character)
  // {
  // return isMeanlinePunctuationMark(character.getUnicode());
  // }

  public static boolean isMeanlinePunctuationMark(final String character) {
    return isMeanlinePunctuationMark(character.charAt(0));
  }

  public static boolean isMeanlinePunctuationMark(final char character) {
    return Characters.MEANLINE_PUNCTUATION_MARKS.contains(character);
  }

  // public static boolean isBaselinePunctuationMark(final PdfCharacter character)
  // {
  // return isBaselinePunctuationMark(character.getUnicode());
  // }

  public static boolean isBaselinePunctuationMark(final String character) {
    return isBaselinePunctuationMark(character.charAt(0));
  }

  public static boolean isBaselinePunctuationMark(final char character) {
    return Characters.BASELINE_PUNCTUATION_MARKS.contains(character);
  }

  // public static boolean isDescender(final PdfCharacter character) {
  // return isDescender(character.getUnicode());
  // }

  public static boolean isDescender(final String character) {
    return isDescender(character.charAt(0));
  }

  public static boolean isDescender(final char character) {
    return Characters.DESCENDERS.contains(character);
  }

  // public static boolean isAscender(final PdfCharacter character) {
  // return isAscender(character.getUnicode());
  // }

  public static boolean isAscender(final String character) {
    return isAscender(character.charAt(0));
  }

  public static boolean isAscender(final char character) {
    return Character.isUpperCase(character) 
        || Character.isDigit(character) 
        || Characters.ASCENDERS.contains(character);
  }

  // public static boolean isMeanlineCharacter(final PdfCharacter character) {
  // return isMeanlineCharacter(character.getUnicode());
  // }

  public static boolean isMeanlineCharacter(final String character) {
    return isMeanlineCharacter(character.charAt(0));
  }

  public static boolean isMeanlineCharacter(final char character) {
    return Characters.MEANLINE_CHARACTERS.contains(character);
  }

  // public static boolean isBaselineCharacter(final PdfCharacter character) {
  // return isBaselineCharacter(character.getUnicode());
  // }

  public static boolean isBaselineCharacter(final String character) {
    return isBaselineCharacter(character.charAt(0));
  }

  public static boolean isBaselineCharacter(final char character) {
    return Characters.BASELINE_CHARACTERS.contains(character);
  }

  // public static boolean isUppercase(final PdfCharacter character) {
  // return isUppercase(character.getUnicode());
  // }

  public static boolean isUppercase(final String character) {
    return isUppercase(character.charAt(0));
  }

  public static boolean isUppercase(final char character) {
    return Character.isUpperCase(character);
  }

  // public static boolean isLowercase(final PdfCharacter character) {
  // return isLowercase(character.getUnicode());
  // }

  public static boolean isLowercase(final String character) {
    return isLowercase(character.charAt(0));
  }

  public static boolean isLowercase(final char character) {
    return Character.isLowerCase(character);
  }

  // public static boolean isMathSymbol(final PdfCharacter character) {
  // return isMathSymbol(character.getUnicode());
  // }

  public static boolean isMathSymbol(final String character) {
    return Characters.MATH_SYMBOLS.contains(character) || Characters.MATH_OPERATORS.contains(character);
  }

  static {
    BASELINE_PUNCTUATION_MARKS = new HashSet<Character>();
    BASELINE_PUNCTUATION_MARKS.add('.');
    BASELINE_PUNCTUATION_MARKS.add('?');
    BASELINE_PUNCTUATION_MARKS.add('!');
    BASELINE_PUNCTUATION_MARKS.add(':');
    BASELINE_PUNCTUATION_MARKS.add(';');
    BASELINE_PUNCTUATION_MARKS.add(',');

    MEANLINE_PUNCTUATION_MARKS = new HashSet<Character>();
    MEANLINE_PUNCTUATION_MARKS.add('\'');
    MEANLINE_PUNCTUATION_MARKS.add('\"');
    MEANLINE_PUNCTUATION_MARKS.add('\u201c');
    MEANLINE_PUNCTUATION_MARKS.add('\u201d');
    MEANLINE_PUNCTUATION_MARKS.add('`');
    MEANLINE_PUNCTUATION_MARKS.add('´');
    MEANLINE_PUNCTUATION_MARKS.add('\u2019');

    DESCENDERS = new HashSet<Character>();
    DESCENDERS.add('j');
    DESCENDERS.add('g');
    DESCENDERS.add('p');
    DESCENDERS.add('q');
    DESCENDERS.add('y');
    DESCENDERS.add('f');
    DESCENDERS.add('i');
    DESCENDERS.add('Q');
    DESCENDERS.add('J');

    ASCENDERS = new HashSet<Character>();
    ASCENDERS.add('b');
    ASCENDERS.add('d');
    ASCENDERS.add('f');
    ASCENDERS.add('h');
    ASCENDERS.add('i');
    ASCENDERS.add('j');
    ASCENDERS.add('k');
    ASCENDERS.add('l');
    ASCENDERS.add('t');
    ASCENDERS.add('\u03b2');

    BASELINE_CHARACTERS = new HashSet<Character>();
    BASELINE_CHARACTERS.add('A');
    BASELINE_CHARACTERS.add('B');
    BASELINE_CHARACTERS.add('C');
    BASELINE_CHARACTERS.add('D');
    BASELINE_CHARACTERS.add('E');
    BASELINE_CHARACTERS.add('F');
    BASELINE_CHARACTERS.add('G');
    BASELINE_CHARACTERS.add('H');
    BASELINE_CHARACTERS.add('I');
    BASELINE_CHARACTERS.add('K');
    BASELINE_CHARACTERS.add('L');
    BASELINE_CHARACTERS.add('M');
    BASELINE_CHARACTERS.add('N');
    BASELINE_CHARACTERS.add('O');
    BASELINE_CHARACTERS.add('P');
    BASELINE_CHARACTERS.add('R');
    BASELINE_CHARACTERS.add('S');
    BASELINE_CHARACTERS.add('T');
    BASELINE_CHARACTERS.add('U');
    BASELINE_CHARACTERS.add('V');
    BASELINE_CHARACTERS.add('W');
    BASELINE_CHARACTERS.add('X');
    BASELINE_CHARACTERS.add('Y');
    BASELINE_CHARACTERS.add('Z');
    BASELINE_CHARACTERS.add('a');
    BASELINE_CHARACTERS.add('b');
    BASELINE_CHARACTERS.add('c');
    BASELINE_CHARACTERS.add('d');
    BASELINE_CHARACTERS.add('e');
    BASELINE_CHARACTERS.add('f');
    BASELINE_CHARACTERS.add('h');
    BASELINE_CHARACTERS.add('i');
    BASELINE_CHARACTERS.add('k');
    BASELINE_CHARACTERS.add('l');
    BASELINE_CHARACTERS.add('m');
    BASELINE_CHARACTERS.add('n');
    BASELINE_CHARACTERS.add('o');
    BASELINE_CHARACTERS.add('r');
    BASELINE_CHARACTERS.add('s');
    BASELINE_CHARACTERS.add('t');
    BASELINE_CHARACTERS.add('u');
    BASELINE_CHARACTERS.add('v');
    BASELINE_CHARACTERS.add('w');
    BASELINE_CHARACTERS.add('x');
    BASELINE_CHARACTERS.add('z');
    BASELINE_CHARACTERS.add('1');
    BASELINE_CHARACTERS.add('2');
    BASELINE_CHARACTERS.add('3');
    BASELINE_CHARACTERS.add('4');
    BASELINE_CHARACTERS.add('5');
    BASELINE_CHARACTERS.add('6');
    BASELINE_CHARACTERS.add('7');
    BASELINE_CHARACTERS.add('8');
    BASELINE_CHARACTERS.add('9');
    BASELINE_CHARACTERS.add('0');

    MEANLINE_CHARACTERS = new HashSet<Character>();
    MEANLINE_CHARACTERS.add('a');
    MEANLINE_CHARACTERS.add('c');
    MEANLINE_CHARACTERS.add('e');
    MEANLINE_CHARACTERS.add('g');
    MEANLINE_CHARACTERS.add('m');
    MEANLINE_CHARACTERS.add('n');
    MEANLINE_CHARACTERS.add('o');
    MEANLINE_CHARACTERS.add('p');
    MEANLINE_CHARACTERS.add('q');
    MEANLINE_CHARACTERS.add('r');
    MEANLINE_CHARACTERS.add('s');
    MEANLINE_CHARACTERS.add('u');
    MEANLINE_CHARACTERS.add('v');
    MEANLINE_CHARACTERS.add('w');
    MEANLINE_CHARACTERS.add('x');
    MEANLINE_CHARACTERS.add('y');
    MEANLINE_CHARACTERS.add('z');

    MATH_SYMBOLS = new HashSet<String>();
    MATH_SYMBOLS.add("\u221a");
    MATH_SYMBOLS.add("\u2211");
    MATH_SYMBOLS.add("\u222b");
    MATH_SYMBOLS.add("\u222e");
    MATH_SYMBOLS.add("¬");
    MATH_SYMBOLS.add("\u02dc");
    MATH_SYMBOLS.add("\u221d");
    MATH_SYMBOLS.add("\u25a0");
    MATH_SYMBOLS.add("\u25a1");
    MATH_SYMBOLS.add("\u220e");
    MATH_SYMBOLS.add("\u25ae");
    MATH_SYMBOLS.add("\u2023");
    MATH_SYMBOLS.add("1");
    MATH_SYMBOLS.add("2");
    MATH_SYMBOLS.add("3");
    MATH_SYMBOLS.add("4");
    MATH_SYMBOLS.add("5");
    MATH_SYMBOLS.add("6");
    MATH_SYMBOLS.add("7");
    MATH_SYMBOLS.add("8");
    MATH_SYMBOLS.add("9");
    MATH_SYMBOLS.add("0");
    MATH_SYMBOLS.add("{");
    MATH_SYMBOLS.add("}");
    MATH_SYMBOLS.add("\u230a");
    MATH_SYMBOLS.add("\u230b");
    MATH_SYMBOLS.add("\u2308");
    MATH_SYMBOLS.add("\u2309");
    MATH_SYMBOLS.add("[");
    MATH_SYMBOLS.add("]");
    MATH_SYMBOLS.add("(");
    MATH_SYMBOLS.add(")");
    MATH_SYMBOLS.add("\u27e8");
    MATH_SYMBOLS.add("\u27e9");
    MATH_SYMBOLS.add("|");
    MATH_SYMBOLS.add("\u2200");
    MATH_SYMBOLS.add("\u2102");
    MATH_SYMBOLS.add("\ud835\udd20");
    MATH_SYMBOLS.add("\u2202");
    MATH_SYMBOLS.add("\ud835\udd3c");
    MATH_SYMBOLS.add("\u2203");
    MATH_SYMBOLS.add("\u2208");
    MATH_SYMBOLS.add("\u2209");
    MATH_SYMBOLS.add("\u220b");
    MATH_SYMBOLS.add("\u210d");
    MATH_SYMBOLS.add("\u2115");
    MATH_SYMBOLS.add("\u2218");
    MATH_SYMBOLS.add("\u2119");
    MATH_SYMBOLS.add("\u211a");
    MATH_SYMBOLS.add("\u01eb");
    MATH_SYMBOLS.add("\u211d");
    MATH_SYMBOLS.add("\u2020");
    MATH_SYMBOLS.add("\u2124");
    MATH_SYMBOLS.add("\u03b1");
    MATH_SYMBOLS.add("\u03b2");
    MATH_SYMBOLS.add("\u03b3");
    MATH_SYMBOLS.add("\u0394");
    MATH_SYMBOLS.add("\u03b4");
    MATH_SYMBOLS.add("\u03b5");
    MATH_SYMBOLS.add("\u03b7");
    MATH_SYMBOLS.add("\u03bb");
    MATH_SYMBOLS.add("\u03bc");
    MATH_SYMBOLS.add("\u03c0");
    MATH_SYMBOLS.add("\u03c1");
    MATH_SYMBOLS.add("\u03c3");
    MATH_SYMBOLS.add("\u03a3");
    MATH_SYMBOLS.add("\u03c4");
    MATH_SYMBOLS.add("\u03c6");
    MATH_SYMBOLS.add("\u03c7");
    MATH_SYMBOLS.add("\u03a6");
    MATH_SYMBOLS.add("\u03c9");
    MATH_SYMBOLS.add("\u03a9");

    MATH_OPERATORS = new HashSet<String>();
    MATH_OPERATORS.add("+");
    MATH_OPERATORS.add("-");
    MATH_OPERATORS.add("\u2212");
    MATH_OPERATORS.add("±");
    MATH_OPERATORS.add("\u2213");
    MATH_OPERATORS.add("\u00d7");
    MATH_OPERATORS.add("\u22c5");
    MATH_OPERATORS.add("·");
    MATH_OPERATORS.add("\u00f7");
    MATH_OPERATORS.add("/");
    MATH_OPERATORS.add("\u2044");
    MATH_OPERATORS.add("\u2234");
    MATH_OPERATORS.add("\u2235");
    MATH_OPERATORS.add("\u221e");
    MATH_OPERATORS.add("=");
    MATH_OPERATORS.add("\u2260");
    MATH_OPERATORS.add("\u2248");
    MATH_OPERATORS.add("~");
    MATH_OPERATORS.add("\u2261");
    MATH_OPERATORS.add("\u225c");
    MATH_OPERATORS.add("\u225d");
    MATH_OPERATORS.add("\u2250");
    MATH_OPERATORS.add("\u2245");
    MATH_OPERATORS.add("\u2261");
    MATH_OPERATORS.add("\u21d4");
    MATH_OPERATORS.add("\u2194");
    MATH_OPERATORS.add("<");
    MATH_OPERATORS.add(">");
    MATH_OPERATORS.add("\u226a");
    MATH_OPERATORS.add("\u226b");
    MATH_OPERATORS.add("\u2264");
    MATH_OPERATORS.add("\u2265");
    MATH_OPERATORS.add("\u2266");
    MATH_OPERATORS.add("\u2267");
    MATH_OPERATORS.add("\u227a");
    MATH_OPERATORS.add("\u227b");
    MATH_OPERATORS.add("\u25c5");
    MATH_OPERATORS.add("\u25bb");
    MATH_OPERATORS.add("\u21d2");
    MATH_OPERATORS.add("\u2192");
    MATH_OPERATORS.add("\u2283");
    MATH_OPERATORS.add("\u2286");
    MATH_OPERATORS.add("\u2282");
    MATH_OPERATORS.add("\u2287");
    MATH_OPERATORS.add("\u2283");
    MATH_OPERATORS.add("\u2192");
    MATH_OPERATORS.add("\u21a6");
    MATH_OPERATORS.add("\u22a7");
    MATH_OPERATORS.add("\u22a2");
    MATH_OPERATORS.add("*");
    MATH_OPERATORS.add("\u221d");
    MATH_OPERATORS.add("\u2216");
    MATH_OPERATORS.add("\u2224");
    MATH_OPERATORS.add("\u2225");
    MATH_OPERATORS.add("\u2226");
    MATH_OPERATORS.add("\u22d5");
    MATH_OPERATORS.add("#");
    MATH_OPERATORS.add("\u2240");
    MATH_OPERATORS.add("\u21af");
    MATH_OPERATORS.add("\u203b");
    MATH_OPERATORS.add("\u2295");
    MATH_OPERATORS.add("\u22bb");
    MATH_OPERATORS.add("\u25a1");
    MATH_OPERATORS.add("\u2022");
    MATH_OPERATORS.add("\u22a4");
    MATH_OPERATORS.add("\u22a5");
    MATH_OPERATORS.add("\u222a");
    MATH_OPERATORS.add("\u2229");
    MATH_OPERATORS.add("\u2228");
    MATH_OPERATORS.add("\u2227");
    MATH_OPERATORS.add("\u00d7");
    MATH_OPERATORS.add("\u2297");
    MATH_OPERATORS.add("\u22c9");
    MATH_OPERATORS.add("\u22ca");
    MATH_OPERATORS.add("\u22c8");
    MATH_OPERATORS.add("sin");
    MATH_OPERATORS.add("cos");
    MATH_OPERATORS.add("tan");
    MATH_OPERATORS.add("exp");
    MATH_OPERATORS.add("log");
    MATH_OPERATORS.add("ln");
    MATH_OPERATORS.add("sec");
    MATH_OPERATORS.add("csc");
    MATH_OPERATORS.add("cot");
    MATH_OPERATORS.add("arcsin");
    MATH_OPERATORS.add("arccos");
    MATH_OPERATORS.add("arctan");
    MATH_OPERATORS.add("arcsec");
    MATH_OPERATORS.add("arccsc");
    MATH_OPERATORS.add("arccot");
    MATH_OPERATORS.add("sinh");
    MATH_OPERATORS.add("cosh");
    MATH_OPERATORS.add("tanh");
    MATH_OPERATORS.add("coth");
    MATH_OPERATORS.add("mod");
    MATH_OPERATORS.add("min");
    MATH_OPERATORS.add("max");
    MATH_OPERATORS.add("inf");
    MATH_OPERATORS.add("sup");
    MATH_OPERATORS.add("lim");
    MATH_OPERATORS.add("lim inf");
    MATH_OPERATORS.add("lim sup");
    MATH_OPERATORS.add("arg");
    MATH_OPERATORS.add("sgn");
    MATH_OPERATORS.add("deg");
    MATH_OPERATORS.add("dim");
    MATH_OPERATORS.add("hom");
    MATH_OPERATORS.add("ker");
    MATH_OPERATORS.add("gcd");
    MATH_OPERATORS.add("det");
    MATH_OPERATORS.add("Pr");

    // Math symbols that should be surrounded by whitespaces on normalizing a formula.
    MATH_SYMBOLS_SURROUNDED_BY_SPACES = new HashSet<>();
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("+");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("-");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("−");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("±");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∓");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("×");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("÷");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⋅");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("·");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("•");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("=");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≅");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≈");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≐");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≜");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≝");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≠");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≡");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≤");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≥");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≦");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≧");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≪");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≫");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≺");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≻");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("~");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∖");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("<");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add(">");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∈");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∉");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∋");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∘");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⋕");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("mod");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∤");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∥");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∦");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∧");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∨");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∩");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∪");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∴");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("∵");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("※");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("≀");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊂");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊃");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊆");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊇");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("→");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("↔");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊕");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊗");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("□");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊢");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊤");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊥");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("↦");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊧");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("↯");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("▻");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⊻");   
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("◅");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⋈");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⋉");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⋊");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⇒");
    MATH_SYMBOLS_SURROUNDED_BY_SPACES.add("⇔");

    // Math symbols that should be followed (but not preceded) by a space on normalizing a formula.
    MATH_SYMBOLS_FOLLOWED_BY_SPACE = new HashSet<>();
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add(",");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add(";");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add(".");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add(":");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arccos");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arccot");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arccsc");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arcsec");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arcsin");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arctan");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("arg");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("cos");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("cosh");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("cot");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("coth");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("csc");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("deggcd");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("det");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("dim");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("exp");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("hom");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("inf"); 
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("lim"); 
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("ln");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("log");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("min"); 
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("max");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("sec");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("sgn");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("sin");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("sinh");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("sup");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("tan");
    MATH_SYMBOLS_FOLLOWED_BY_SPACE.add("tanh");

    // Math symbols that should *not* be surrounded by whitespaces on normalizing a formula.
    MATH_SYMBOLS_NO_SPACES = new HashSet<>();
    MATH_SYMBOLS_NO_SPACES.add("⌊");
    MATH_SYMBOLS_NO_SPACES.add("⌈");
    MATH_SYMBOLS_NO_SPACES.add("⌉");
    MATH_SYMBOLS_NO_SPACES.add("⌋");
    MATH_SYMBOLS_NO_SPACES.add("(");
    MATH_SYMBOLS_NO_SPACES.add(")");
    MATH_SYMBOLS_NO_SPACES.add("[");
    MATH_SYMBOLS_NO_SPACES.add("]");
    MATH_SYMBOLS_NO_SPACES.add("⟨");
    MATH_SYMBOLS_NO_SPACES.add("⟩");
    MATH_SYMBOLS_NO_SPACES.add("{");
    MATH_SYMBOLS_NO_SPACES.add("}");
    MATH_SYMBOLS_NO_SPACES.add("|");
    MATH_SYMBOLS_NO_SPACES.add("/");
  }
}
