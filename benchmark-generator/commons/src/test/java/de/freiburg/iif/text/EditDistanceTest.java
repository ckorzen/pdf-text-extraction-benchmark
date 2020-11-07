package de.freiburg.iif.text;

import org.junit.Test;
import org.junit.Assert;

/**
 * Test class for edit distance computation.
 */
public class EditDistanceTest {

  /**
   * Test the method with dynamic programming.
   */
  @Test
  public void testComputeDistance() {
    Assert.assertEquals(4, EditDistance.computeDistance("BAUM", ""));
    Assert.assertEquals(4, EditDistance.computeDistance("", "BAUM"));
    Assert.assertEquals(5, EditDistance.computeDistance("MAUER", ""));
    Assert.assertEquals(5, EditDistance.computeDistance("", "MAUER"));
    Assert.assertEquals(3, EditDistance.computeDistance("BAUM", "MAUER"));
    Assert.assertEquals(3, EditDistance.computeDistance("MAUER", "BAUM"));
    Assert.assertEquals(5, EditDistance.computeDistance("MAUER", "AMERIKA"));
    Assert.assertEquals(5, EditDistance.computeDistance("AMERIKA", "MAUER"));

    Assert.assertEquals(1, EditDistance.computeDistance("AMERIKA", "ameri a"));
    Assert.assertEquals(1, EditDistance.computeDistance("AMERIKA", "a erika"));
  }

  /**
   * Test the method with dynamic programming.
   */
  @Test
  public void testComputeSimilarityScore() {
    float score = EditDistance.computeSimilarityScore("BAUM", "");
    Assert.assertEquals(0, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("", "BAUM");
    Assert.assertEquals(0, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("MAUER", "");
    Assert.assertEquals(0, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("", "MAUER");
    Assert.assertEquals(0, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("BAUM", "MAUER");
    Assert.assertEquals(2 / 5f, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("MAUER", "BAUM");
    Assert.assertEquals(2 / 5f, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("MAUER", "AMERIKA");
    Assert.assertEquals(2 / 7f, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("AMERIKA", "MAUER");
    Assert.assertEquals(2 / 7f, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("AMERIKA", "ameri a");
    Assert.assertEquals(6 / 7f, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("AMERIKA", "a erika");
    Assert.assertEquals(6 / 7f, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("", "");
    Assert.assertEquals(1, score, 0.0001f);

    score = EditDistance.computeSimilarityScore("AMERIKA", "AMERIKA");
    Assert.assertEquals(1, score, 0.0001f);
  }
}