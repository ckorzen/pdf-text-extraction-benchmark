package de.freiburg.iif.counter;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * A class to count the frequencies of integers.
 * 
 * @author Claudius Korzen
 */
public class IntCounter extends TIntIntHashMap {
  /** Flag that indicates whether the statistics need to be recomputed. */
  protected boolean isStatisticsOutdated = true;

  /** The largest value in the set of ints. */
  protected int largestInt = -Integer.MAX_VALUE;
  /** The smallest value in the set of ints. */
  protected int smallestInt = Integer.MAX_VALUE;
  /** (One of) the most frequent int(s). */
  protected int mostFrequentInt = -Integer.MAX_VALUE;
  /** All most frequent ints. */
  protected int[] allMostFrequentInts;
  /** The frequency of the most frequent int. */
  protected int mostFrequentIntCount = -Integer.MAX_VALUE;
  /** (One of) the least frequent int(s). */
  protected int leastFrequentInt = Integer.MAX_VALUE;
  /** All least frequent ints. */
  protected int[] allLeastFrequentInts;
  /** The frequency of the least frequent int. */
  protected int leastFrequentIntCount = Integer.MAX_VALUE;
  /** The average value over all ints. */
  protected float averageValue = Float.MAX_VALUE;

  // ___________________________________________________________________________
  // Add methods.

  /**
   * Adds all given ints to this counter.
   */
  public void addAll(int[] ints) {
    if (ints == null) {
      return;
    }

    for (int integer : ints) {
      add(integer);
    }
  }

  /**
   * Adds the given integer to this counter.
   */
  public void add(int integer) {
    add(integer, 1);
  }

  /**
   * Adds all ints in the given counter with its associated frequency to this
   * counter.
   */
  public void add(IntCounter counter) {
    if (counter == null) {
      return;
    }

    for (int integer : counter.getInts()) {
      add(integer, counter.getCount(integer));
    }
  }

  /**
   * Adds the given integer with given frequency to this counter.
   */
  public void add(int integer, int freq) {
    int count = 0;
    if (containsKey(integer)) {
      count = get(integer);
    }
    put(integer, count + freq);
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
   * Returns all different ints (that are the keys) in this counter.
   */
  public int[] getInts() {
    return keys();
  }

  /**
   * Returns the count of the given integer.
   */
  public int getCount(int integer) {
    return containsKey(integer) ? get(integer) : 0;
  }

  /**
   * Returns the count of the most frequent object.
   */
  public int getMostFrequentIntegerCount() {
    if (isStatisticsOutdated) {
      count();
    }
    return mostFrequentIntCount;
  }

  /**
   * Returns the most frequent int.
   */
  public int getMostFrequentInteger() {
    if (isStatisticsOutdated) {
      count();
    }
    return mostFrequentInt;
  }

  /**
   * Returns all most frequent ints in an array.
   */
  public int[] getAllMostFrequentFloats() {
    if (isStatisticsOutdated) {
      count();
    }
    return allMostFrequentInts;
  }
  
  /**
   * Returns the count of the least frequent integer.
   */
  public int getLeastFrequentIntegerCount() {
    if (isStatisticsOutdated) {
      count();
    }
    return leastFrequentIntCount;
  }

  /**
   * Returns the least frequent integer.
   */
  public int getLeastFrequentInteger() {
    if (isStatisticsOutdated) {
      count();
    }
    return leastFrequentInt;
  }

  /**
   * Returns all least frequent ints in an array.
   */
  public int[] getAllLeastFrequentFloats() {
    if (isStatisticsOutdated) {
      count();
    }
    return allLeastFrequentInts;
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
   * Returns the smallest int value.
   */
  public float getSmallestInteger() {
    return this.smallestInt;
  }
  
  /**
   * Returns the smallest int value that occurs at least 'freq'-times.
   */
  public int getSmallestIntegerOccuringAtLeast(int freq) {
    int smallestInt = Integer.MAX_VALUE;
    for (int integer : keys()) {      
      if (get(integer) >= freq && integer < smallestInt) {
        smallestInt = integer;
      }
    }
    return smallestInt;
  }
  
  /**
   * Returns the largest int value.
   */
  public int getLargestInteger() {
    return this.largestInt;
  }
  
  /**
   * Returns the largest int value that occurs at most 'freq'-times.
   */
  public int getLargestIntegerOccuringAtMost(int freq) {
    int largestInt = -Integer.MAX_VALUE;
    for (int integer : keys()) {      
      if (get(integer) <= freq && integer > largestInt) {
        largestInt = integer;
      }
    }
    return largestInt;
  }
  
  /**
   * Counts the frequencies of the integers.
   */
  protected void count() {
    resetComputedValues();

    float sumInts = 0;
    float numInts = 0;
    TIntArrayList allMostFrequentInts = new TIntArrayList();
    TIntArrayList allLeastFrequentInts = new TIntArrayList();
    
    for (int integer : keys()) {
      int count = get(integer);

      if (integer > largestInt) {
        this.largestInt = integer;
      }
      
      if (integer < smallestInt) {
        this.smallestInt = integer;
      }
      
      if (count > this.mostFrequentIntCount) {
        this.mostFrequentInt = integer;
        this.mostFrequentIntCount = get(integer);
        allMostFrequentInts.clear();
        allMostFrequentInts.add(integer);
      }
      
      if (count == this.mostFrequentIntCount) {
        allMostFrequentInts.add(integer);
      }

      if (count < this.mostFrequentIntCount) {
        this.leastFrequentInt = integer;
        this.leastFrequentIntCount = get(integer);
        allLeastFrequentInts.clear();
        allMostFrequentInts.add(integer);
      }

      if (count == this.leastFrequentIntCount) {
        allLeastFrequentInts.add(integer);
      }
      
      sumInts += count * integer;
      numInts += count;
    }
    this.averageValue = numInts > 0 ? (sumInts / numInts) : 0;
    this.allMostFrequentInts = allMostFrequentInts.toArray();
    this.allLeastFrequentInts = allLeastFrequentInts.toArray();
    
    this.isStatisticsOutdated = false;
  }

  /**
   * Resets the internal counters.
   */
  protected void resetComputedValues() {
    largestInt = -Integer.MAX_VALUE;
    smallestInt = Integer.MAX_VALUE;
    mostFrequentInt = -Integer.MAX_VALUE;
    allMostFrequentInts = null;
    mostFrequentIntCount = -Integer.MAX_VALUE;
    leastFrequentInt = Integer.MAX_VALUE;
    allLeastFrequentInts = null;
    leastFrequentIntCount = Integer.MAX_VALUE;
    averageValue = Float.MAX_VALUE;
  }
}
