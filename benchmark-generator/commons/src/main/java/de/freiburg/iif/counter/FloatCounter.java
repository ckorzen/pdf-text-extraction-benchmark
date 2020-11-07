package de.freiburg.iif.counter;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.hash.TFloatIntHashMap;

/**
 * A class to collect a set of float values and to compute some statistics
 * about this set.
 * 
 * @author Claudius Korzen
 */
public class FloatCounter extends TFloatIntHashMap {
  /** Flag that indicates whether the statistics need to be recomputed. */
  protected boolean isStatisticsOutdated = true;
  
  /** The largest value in the set of floats. */
  protected float largestFloat = -Float.MAX_VALUE;
  /** The smallest value in the set of floats. */
  protected float smallestFloat = Float.MAX_VALUE;
  /** (One of) the most frequent float(s). */ 
  protected float mostFrequentFloat = -Float.MAX_VALUE;
  /** All most frequent floats. */
  protected float[] allMostFrequentFloats;
  /** The frequency of the most frequent float. */
  protected int mostFrequentFloatCount = -Integer.MAX_VALUE;
  /** (One of) the least frequent float(s). */
  protected float leastFrequentFloat = Float.MAX_VALUE;
  /** All least frequent floats.  */
  protected float[] allLeastFrequentFloats;
  /** The frequency of the least frequent float. */
  protected int leastFrequentFloatCount = Integer.MAX_VALUE;
  /** The average value over all floats. */
  protected float averageValue = Float.MAX_VALUE;
  
  // ___________________________________________________________________________
  // Add methods.
  
  /**
   * Adds all given floats to this counter.
   */
  public void addAll(float[] floats) {
    if (floats == null) {
      return;
    }
    
    for (float f : floats) {
      add(f);
    }
  }
  
  /**
   * Adds the given float to this counter.
   */
  public void add(float f) {
    add(f, 1);
  }
    
  /**
   * Adds all floats in the given counter with its associated frequency to this
   * counter.
   */
  public void add(FloatCounter counter) {
    if (counter == null) {
      return;
    }
    
    for (float f : counter.getFloats()) {
      add(f, counter.getCount(f));
    }
  }
  
  /**
   * Adds the given float with given frequency to this counter.
   */
  public void add(float f, int freq) {
    if (Float.isNaN(f)) {
      return;
    }
    
    int count = 0;
    if (containsKey(f)) {
      count = get(f);
    }
    put(f, count + freq);
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
   * Returns all different floats (that are the keys) in this counter.
   */
  public float[] getFloats() {
    return keys();
  }
  
  /**
   * Returns the count of the given float.
   */
  public int getCount(float f) {
    return containsKey(f) ? get(f) : 0;
  }
  
  /**
   * Returns the count of the most frequent float.
   */
  public int getMostFrequentFloatCount() {
    if (isStatisticsOutdated) {
      count();
    }
    return mostFrequentFloatCount;
  }
  
  /**
   * Returns (one of) the most frequent float.
   */
  public float getMostFrequentFloat() {
    if (isStatisticsOutdated) {
      count();
    }
    return mostFrequentFloat;
  }
  
  /**
   * Returns all most frequent floats in an array.
   */
  public float[] getAllMostFrequentFloats() {
    if (isStatisticsOutdated) {
      count();
    }
    return allMostFrequentFloats;
  }
  
  /**
   * Returns the count of the least frequent float.
   */
  public int getLeastFrequentFloatCount() {
    if (isStatisticsOutdated) {
      count();
    }
    return leastFrequentFloatCount;
  }
  
  /**
   * Returns the least frequent float.
   */
  public float getLeastFrequentFloat() {
    if (isStatisticsOutdated) {
      count();
    }
    return leastFrequentFloat;
  }
  
  /**
   * Returns all least frequent floats in an array.
   */
  public float[] getAllLeastFrequentFloats() {
    if (isStatisticsOutdated) {
      count();
    }
    return allLeastFrequentFloats;
  }
  
  /**
   * Returns the average float.
   */
  public float getAverageValue() {
    if (isStatisticsOutdated) {
      count();
    }
    return averageValue;
  }
  
  /**
   * Returns the smallest float value.
   */
  public float getSmallestFloat() {
    return this.smallestFloat;
  }
  
  /**
   * Returns the smallest float value that occurs at least 'freq'-times.
   */
  public float getSmallestFloatOccuringAtLeast(int freq) {
    float smallestFloat = Float.MAX_VALUE;
    for (float f : keys()) {      
      if (get(f) >= freq && f < smallestFloat) {
        smallestFloat = f;
      }
    }
    return smallestFloat;
  }
  
  /**
   * Returns the smallest float value that occurs at most 'freq'-times.
   */
  public float getSmallestFloatOccuringAtMost(int freq) {
    float smallestFloat = Float.MAX_VALUE;
    for (float f : keys()) {      
      if (get(f) <= freq && f < smallestFloat) {
        smallestFloat = f;
      }
    }
    return smallestFloat;
  }
  
  /**
   * Returns the largest float value.
   */
  public float getLargestFloat() {
    return this.largestFloat;
  }
  
  /**
   * Returns the largest float value that occurs at most 'freq'-times.
   */
  public float getLargestFloatOccuringAtMost(int freq) {
    float largestFloat = -Float.MAX_VALUE;
    for (float f : keys()) {      
      if (get(f) <= freq && f > largestFloat) {
        largestFloat = f;
      }
    }
    return largestFloat;
  }
  
  /**
   * Returns the largest float value that occurs at least 'freq'-times.
   */
  public float getLargestFloatOccuringAtLeast(int freq) {
    float largestFloat = -Float.MAX_VALUE;
    for (float f : keys()) {      
      if (get(f) >= freq && f > largestFloat) {
        largestFloat = f;
      }
    }
    return largestFloat;
  }
  
  /**
   * Counts the frequencies of the integers.
   */
  protected void count() {
    resetComputedValues();
    
    float sumFloats = 0;
    float numFloats = 0;
    TFloatArrayList allMostFrequentFloats = new TFloatArrayList();
    TFloatArrayList allLeastFrequentFloats = new TFloatArrayList();
    
    for (float f : keys()) {
      int count = get(f);
            
      if (f > largestFloat) {
        this.largestFloat = f;
      }
      
      if (f < smallestFloat) {
        this.smallestFloat = f;
      }
      
      if (count > this.mostFrequentFloatCount) {
        this.mostFrequentFloat = f;
        this.mostFrequentFloatCount = count;
        allMostFrequentFloats.clear();
        allMostFrequentFloats.add(f);
      }
      
      if (count == this.mostFrequentFloatCount) {
        allMostFrequentFloats.add(f);
      }
      
      if (count < this.mostFrequentFloatCount) {
        this.leastFrequentFloat = f;
        this.leastFrequentFloatCount = count;
        allLeastFrequentFloats.clear();
        allMostFrequentFloats.add(f);
      }
      
      if (count == this.leastFrequentFloatCount) {
        allLeastFrequentFloats.add(f);
      }
      
      sumFloats += count * f;
      numFloats += count;
    }
    
    this.averageValue = numFloats > 0 ? (sumFloats / numFloats) : 0;
    this.allMostFrequentFloats = allMostFrequentFloats.toArray();
    this.allLeastFrequentFloats = allLeastFrequentFloats.toArray();
    
    this.isStatisticsOutdated = false;
  }
  
  /**
   * Resets the internal counters.
   */
  protected void resetComputedValues() {
    largestFloat = -Float.MAX_VALUE;
    smallestFloat = Float.MAX_VALUE;
    mostFrequentFloat = -Float.MAX_VALUE;
    allMostFrequentFloats = null;
    mostFrequentFloatCount = -Integer.MAX_VALUE;
    leastFrequentFloat = Float.MAX_VALUE;
    allLeastFrequentFloats = null;
    leastFrequentFloatCount = Integer.MAX_VALUE;
    averageValue = Float.MAX_VALUE;
  }
}

