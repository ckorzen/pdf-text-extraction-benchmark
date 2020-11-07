package de.freiburg.iif.text;


/**
 * Class for computing the edit distance between two strings.
 */
public class EditDistance {
  /**
   * Computes a similarity score between 0.0 and 1.0f. A value of 1.0 means: 
   * x is equal to y and a value of 0.0 means: x and y have no characters in 
   * common.
   * 
   * @param x the first string.
   * @param y the second string.
   * @return the similarity score.
   */
  public static float computeSimilarityScore(String x, String y) { 
    int editDistance = computeDistance(x, y);
    
    if (x != null && y != null) {
      if (x.isEmpty() && y.isEmpty()) {
        return 1f;
      } 
      
      if (!x.isEmpty() || !y.isEmpty()) {
        float maxLength = Math.max(x.length(), y.length());
        return 1 - (editDistance / maxLength);
      }
    }
    return 0;
  }
  
  /**
   * Compute edit distance with dynamic programming. This is a memory efficient
   * version due to the use of only 2 vectors instead of a dynamic table.
   * 
   * @param x the first string.
   * @param y the second string.
   * @return the edit distance between x and y.
   */
  public static int computeDistance(String x, String y) {    
    x = x == null ? "" : x;
    y = y == null ? "" : y;
        
    x = x.toLowerCase();
    y = y.toLowerCase();
    
    char[] xChars = x.toCharArray();
    char[] yChars = y.toCharArray();
        
    int m = xChars.length;
    int n = yChars.length;
    int cost = 0;
    
    if (m == 0) {
      return n;
    }
    
    if (n == 0) {
      return m;
    }
    
    // Initialize the two arrays.
    int[] a0 = new int[m + 1];
    int[] a1 = new int[m + 1];
    int[] tmpArray = null;
    for (int i = 0; i <= m; i++) {
      a0[i] = i;
    }
    
    
    for (int i = 1; i <= n; i++) {
      a1[0] = i;
      for (int j = 1; j <= m; j++) {
        if ((xChars[j - 1] == yChars[i - 1])) {
          cost = 0;
        } else {
          cost = 1;
        }
                
        int a = a0[j] + 1;
        int b = a1[j - 1] + 1;
        int c = a0[j - 1] + cost;
          
        a1[j] = min(a, b, c);
      }
      
      // Swap the arrays.
      tmpArray = a0;
      a0 = a1;
      a1 = tmpArray;
    }
    return a0[m];
  }
   
  /**
   * Returns the smallest of three int values.
   * 
   * @param a the 1st int value.
   * @param b the 2nd int value.
   * @param c the 3rd int value.
   * @return the smallest of the three int values.
   */
  public static int min(int a, int b, int c) {
    return Math.min(a, Math.min(b, c));
  }
}