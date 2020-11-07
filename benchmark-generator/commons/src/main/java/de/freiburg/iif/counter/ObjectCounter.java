package de.freiburg.iif.counter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * A class to count the frequencies of objects.
 * 
 * @author Claudius Korzen
 *
 * @param <K>
 *          the type of objects to count.
 */
public class ObjectCounter<K> extends TObjectIntHashMap<K> {
  /** Flag that indicates whether the statistics need to be recomputed. */
  protected boolean isStatisticsOutdated = true;

  /** (One of) the most frequent object(s). */
  protected K mostFrequentObject = null;
  /** All most frequent objects. */
  protected Set<K> allMostFrequentObjects = new HashSet<>();
  /** The frequency of the most frequent object. */
  protected int mostFrequentObjectCount = -Integer.MAX_VALUE;
  /** (One of) the least frequent object(s). */
  protected K leastFrequentObject = null;
  /** All least frequent objects. */
  protected Set<K> allLeastFrequentObjects = new HashSet<>();
  /** The frequency of the least frequent object. */
  protected int leastFrequentObjectCount = Integer.MAX_VALUE;

  // ___________________________________________________________________________
  // Add methods.

  /**
   * Adds all given objects to this counter.
   */
  public void addAll(List<K> objects) {
    if (objects == null) {
      return;
    }

    for (K object : objects) {
      add(object);
    }
  }

  /**
   * Adds the given object to this counter.
   */
  public void add(K object) {
    add(object, 1);
  }

  /**
   * Adds all objects in the given counter with its associated frequency to this
   * counter.
   */
  public void add(ObjectCounter<K> counter) {
    if (counter == null) {
      return;
    }

    for (K key : counter.getObjects()) {
      add(key, counter.getCount(key));
    }
  }

  /**
   * Adds the given object with given frequency to this counter.
   */
  public void add(K object, int freq) {
    int count = 0;
    if (containsKey(object)) {
      count = get(object);
    }
    put(object, count + freq);
    this.isStatisticsOutdated = true;
  }

  // ___________________________________________________________________________
  
  /**
   * Resets (clears) this counter.
   */
  public void reset() {
    clear();
    resetComputedValues();
  }

  // ___________________________________________________________________________
  // Getter methods.

  /**
   * Returns all different objects (that are the keys) in this counter.
   */
  public Set<K> getObjects() {
    return keySet();
  }

  /**
   * Returns the count of the given object.
   */
  public int getCount(K object) {
    return containsKey(object) ? get(object) : 0;
  }

  /**
   * Returns the count of the most frequent object.
   */
  public int getMostFrequentObjectCount() {
    if (isStatisticsOutdated) {
      count();
    }
    return mostFrequentObjectCount;
  }

  /**
   * Returns the most frequent object.
   */
  public K getMostFrequentObject() {
    if (isStatisticsOutdated) {
      count();
    }
    return mostFrequentObject;
  }

  /**
   * Returns all most frequent objects in a set.
   */
  public Set<K> getAllMostFrequentObjects() {
    if (isStatisticsOutdated) {
      count();
    }
    return allMostFrequentObjects;
  }
  
  /**
   * Returns the count of the least frequent object.
   */
  public int getLeastFrequentObjectCount() {
    if (isStatisticsOutdated) {
      count();
    }
    return leastFrequentObjectCount;
  }

  /**
   * Returns the least frequent object.
   */
  public K getLeastFrequentObject() {
    if (isStatisticsOutdated) {
      count();
    }
    return leastFrequentObject;
  }

  /**
   * Returns all least frequent objects in a set.
   */
  public Set<K> getAllLeastFrequentObjects() {
    if (isStatisticsOutdated) {
      count();
    }
    return allLeastFrequentObjects;
  }
  
  /**
   * Counts the frequencies of the objects.
   */
  protected void count() {
    resetComputedValues();

    for (K object : keySet()) {
      int count = get(object);
      
      if (count > this.mostFrequentObjectCount) {
        this.mostFrequentObject = object;
        this.mostFrequentObjectCount = get(object);
        this.allMostFrequentObjects.clear();
        this.allMostFrequentObjects.add(object);
      }

      if (count == this.mostFrequentObjectCount) {
        this.allMostFrequentObjects.add(object);
      }
      
      if (count < this.leastFrequentObjectCount) {
        this.leastFrequentObject = object;
        this.leastFrequentObjectCount = get(object);
        this.allLeastFrequentObjects.clear();
        this.allLeastFrequentObjects.add(object);
      }
      
      if (count == this.leastFrequentObjectCount) {
        this.allLeastFrequentObjects.add(object);
      }
    }
    this.isStatisticsOutdated = false;
  }

  /**
   * Resets the internal counters.
   */
  protected void resetComputedValues() {
    mostFrequentObject = null;
    allMostFrequentObjects = new HashSet<>();
    mostFrequentObjectCount = -Integer.MAX_VALUE;
    leastFrequentObject = null;
    allLeastFrequentObjects = new HashSet<>();
    leastFrequentObjectCount = Integer.MAX_VALUE;
  }
}
