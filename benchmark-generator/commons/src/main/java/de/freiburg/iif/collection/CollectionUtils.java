package de.freiburg.iif.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Some util methods for collections.
 *
 * @author Claudius Korzen
 */
public class CollectionUtils {  
  protected static final String DEFAULT_DELIMITER = " ";
  
  /**
   * Returns the given strings in a list.
   */
  public static List<String> toList(String... objects) {
    return Arrays.asList(objects);
  }
    
  /**
   * Returns the string that results from joining the elements in the given 
   * collection with the whitespace (" ").
   */
  public static String join(Object[] objects) {
    return join(Arrays.asList(objects), DEFAULT_DELIMITER);
  }
  
  /**
   * Returns the string that results from joining the elements in the given 
   * collection with the given delimiter.
   */
  public static String join(Object[] objects, String delimiter) {
    return join(Arrays.asList(objects), delimiter);
  }
  
  /**
   * Returns the string that results from joining the elements in the given 
   * collection with the whitespace (" ").
   */
  public static String join(Iterable<?> collection) {
    return join(collection, DEFAULT_DELIMITER);
  }
  
  /**
   * Returns the string that results from joining the elements in the given 
   * collection with the given delimiter.
   */
  public static String join(Iterable<?> iterable, String delimiter) {
    if (iterable == null) {
      return null;
    }
    
    if (delimiter == null) {
      delimiter = DEFAULT_DELIMITER;
    }
    
    StringBuilder result = new StringBuilder();
        
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object element = iterator.next();
      result.append(element != null ? element.toString() : null);
      result.append(iterator.hasNext() ? delimiter : "");
    }
    
    return result.toString();
  }
}
