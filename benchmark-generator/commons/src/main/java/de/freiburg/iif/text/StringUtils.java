package de.freiburg.iif.text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Some useful String methods.
 *
 * @author Claudius Korzen
 */
public class StringUtils {
  protected static HashSet<String> stopwords;

  /**
   * Returns true, if the given text contains any of the given search strings.
   */
  public static boolean containsAny(String text, char... searchChars) {
    if (text == null || searchChars == null) {
      return false;
    }

    char[] textChars = text.toCharArray();
    for (char textChar : textChars) {
      for (char s : searchChars) {
        if (textChar == s) {
          return true;
        }
      }
    }

    return false;
  }
  
  /**
   * Returns true, if the given text contains any of the given search strings.
   */
  public static boolean containsAny(String text, String... searchStrings) {
    return containsAny(text, Arrays.asList(searchStrings));
  }

  /**
   * Returns true, if the given text contains any of the given search strings.
   */
  public static boolean containsAny(String text, List<String> searchStrings) {
    if (text == null || searchStrings == null) {
      return false;
    }

    for (String s : searchStrings) {
      if (s == null) {
        continue;
      }

      if (text.contains(s)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true, if the given text starts with one of the given prefixes. 
   * Returns false, if the given text and/or the given list of prefixes is null.
   */
  public static boolean startsWith(String text, String... prefixes) {
    return startsWith(text, Arrays.asList(prefixes));
  }

  /**
   * Returns true, if the given text starts with one of the given prefixes. 
   * Returns false, if the given text and/or the given list of prefixes is null.
   */
  public static boolean startsWith(String text, Iterable<String> prefixes) {
    if (text == null || prefixes == null) {
      return false;
    }

    for (String prefix : prefixes) {
      if (text.startsWith(prefix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true, if the given text ends with one of the given suffixes. 
   * Returns false, if the given text and/or the given list of suffixes is null.
   */
  public static boolean endsWith(String text, String... suffixes) {
    return endsWith(text, Arrays.asList(suffixes));
  }

  /**
   * Returns true, if the given text ends with one of the given suffixes. 
   * Returns false, if the given text and/or the given list of suffixes is null.
   */
  public static boolean endsWith(String text, Iterable<String> suffixes) {
    if (text == null || suffixes == null) {
      return false;
    }

    for (String suffix : suffixes) {
      if (text.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true, if the given text is equal to one of the given patterns. 
   * Returns false, if the given text and/or the given list of patterns is null.
   */
  public static boolean equals(String text, char... patterns) {
    if (text == null || patterns == null) {
      return false;
    }

    if (text.isEmpty()) {
      return false;
    }
    
    for (char pattern : patterns) {
      if (text.charAt(0) == pattern) {
        return true;
      }
    }

    return false;
  }
  
  /**
   * Returns true, if the given text is equal to one of the given patterns. 
   * Returns false, if the given text and/or the given list of patterns is null.
   */
  public static boolean equals(String text, String... patterns) {
    return equals(text, Arrays.asList(patterns));
  }

  /**
   * Returns true, if the given text is equal to one of the given patterns. 
   * Returns false, if the given text and/or the given list of patterns is null.
   */
  public static boolean equals(String text, Iterable<String> patterns) {
    if (text == null || patterns == null) {
      return false;
    }

    for (String pattern : patterns) {
      if (text.equals(pattern)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Repeats the given string <repeat>-times.
   */
  public static String repeat(String string, int repeats) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < repeats; i++) {
      sb.append(string);
    }
    return sb.toString();
  }

  /**
   * Normalizes the given string, i.e. removes all non-alphanumeric characters
   * and transform all characters to lowercases. You can protect certain
   * non-alphanumeric characters from removing by specifying it as a protect.
   */
  public static String normalize(String pattern, boolean removeNumbers,
      boolean removeWhitespaces, boolean toLowercases, char... protects) {
    StringBuilder builder = new StringBuilder();

    HashSet<Character> protectSet = new HashSet<Character>();
    for (char protect : protects) {
      protectSet.add(protect);
    }

    boolean prevCharIsWhitespace = false;
    for (char c : pattern.toCharArray()) {
      if (protectSet.contains(c)) {
        builder.append(c);
        continue;
      }

      if (toLowercases) {
        c = Character.toLowerCase(c);
      }

      if (removeNumbers) {
        builder.append(Character.isLetter(c) ? c : "");
      } else {
        builder.append(Character.isLetterOrDigit(c) ? c : "");
      }

      if (!removeWhitespaces) {
        boolean charIsWhitespace = Character.isWhitespace(c);
        if (!prevCharIsWhitespace && charIsWhitespace) {
          builder.append(" ");
        }
        prevCharIsWhitespace = charIsWhitespace;
      }
    }
    return builder.toString().trim();
  }

  /**
   * Removes all whitespaces from given text.
   */
  public static String removeWhitespaces(String string) {
    if (string == null) {
      return null;
    }
    return string.replaceAll("\\s", "");
  }

  /**
   * Returns the longest part of the given text that is not inrrupted by any
   * punctuation marks.
   */
  public static String getLongestSentencePart(String string) {
    if (string == null) {
      return null;
    }

    String[] parts = string.split("[\\p{Punct}]");

    String longestPart = null;
    int longestLength = 0;
    for (String part : parts) {
      if (part.length() > longestLength) {
        longestLength = part.length();
        longestPart = part;
      }
    }

    return longestPart;
  }

  /**
   * Returns true, if the given word is a stop word.
   */
  public static boolean isStopWord(String word) {
    if (stopwords == null) {
      stopwords = readStopWordsFromFile();
    }

    return stopwords.contains(word);
  }

  /**
   * Reads the stopwords from file.
   */
  protected static HashSet<String> readStopWordsFromFile() {
    HashSet<String> stopWords = new HashSet<>();

    ClassLoader classLoader = StringUtils.class.getClassLoader();

    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        classLoader.getResourceAsStream("stop-words.txt")))) {
      String line = null;
      while ((line = br.readLine()) != null) {
        stopWords.add(line);
      }
    } catch (Exception e) {
      // TODO
    }
    return stopWords;
  }

  /**
   * Trims the given string on left side.
   */
  public static String ltrim(String s) {
    int i = 0;
    while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
    return s.substring(i);
  }

  /**
   * Trims the given string at right side.
   */
  public static String rtrim(String s) {
    int i = s.length() - 1;
    while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
      i--;
    }
    return s.substring(0, i + 1);
  }
  
  /**
   * Returns true if and only if the given string consists only of integer
   * numbers.
   */
  public static boolean isInteger(String str) {  
    try {  
      Integer.parseInt(str);  
    } catch(NumberFormatException nfe) {  
      return false;  
    }  
    return true;  
  }
  
  /**
   * Returns the first index within given haystack of the occurrence of one of 
   * the specified needles.
   */
  public static int indexOf(String haystack, char... needles) {
    return indexOf(haystack, 0, needles);
  }
  
  /**
   * Returns the first index within given haystack of the occurrence of one of 
   * the specified needles.
   */
  public static int indexOf(String haystack, int fromIndex, 
      char... needles) {
    if (haystack != null) {
      if (fromIndex < 0) {
        fromIndex = 0;
      } else if (fromIndex >= haystack.length()) {
        return -1;
      }
  
      final char[] chars = haystack.toCharArray();
      for (int i = fromIndex; i < chars.length; i++) {
        for (char needle : needles) {
          if (chars[i] == needle) {
            return i;
          }
        }
      }
    }
    return -1;
  }
  
  /**
   * Returns the indexes within given haystack of all occurrences of the 
   * specified needle.
   */
  public static List<Integer> indexesOf(String haystack, char... needles) {
    List<Integer> indexes = new ArrayList<>();
    
    int index = indexOf(haystack, needles);   
    
    while (index >= 0) {
      indexes.add(index);
      index = indexOf(haystack, index + 1, needles);
    }
    return indexes;
  }
}
