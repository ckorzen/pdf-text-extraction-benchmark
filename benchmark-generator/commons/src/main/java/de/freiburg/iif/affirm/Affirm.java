package de.freiburg.iif.affirm;

/**
 * Out assert implementation.
 * 
 * @author Claudius Korzen
 *
 */
public class Affirm {
  public static void affirm(boolean condition) {
    affirm(condition, null);
  }
  
  public static void affirm(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
