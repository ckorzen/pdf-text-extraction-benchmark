package de.freiburg.iif.mock;

import static de.freiburg.iif.mock.GeometricMock.answerContains;
import static de.freiburg.iif.mock.GeometricMock.answerOverlaps;
import static de.freiburg.iif.mock.GeometricMock.answerToString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.freiburg.iif.model.Geometric;
import de.freiburg.iif.model.Point;
import de.freiburg.iif.model.Rectangle;

/**
 * Methods to mock a rectangle.
 * 
 * @author Claudius Korzen.
 * 
 */
public class RectangleMock {
  /**
   * Answers a call to merge(any(Rectangle.class)).
   */
  public static Answer<Rectangle> answerMerge = new Answer<Rectangle>() {
    @Override
    public Rectangle answer(InvocationOnMock invocation) throws Throwable {
      Rectangle mock = (Rectangle) invocation.getMock();
      Rectangle arg = (Rectangle) invocation.getArguments()[0];

      float minX = Math.min(mock.getMinX(), arg.getMinX());
      float maxX = Math.max(mock.getMaxX(), arg.getMaxX());
      float minY = Math.min(mock.getMinY(), arg.getMinY());
      float maxY = Math.max(mock.getMaxY(), arg.getMaxY());

      return mockRectangle(minX, minY, maxX, maxY);
    }
  };

  /**
   * Returns a mock for interface Rectangle.
   * 
   * @param lowerLeft
   *          The lower left point.
   * @param upperRight
   *          The upper right point.
   * @return the created mock.
   */
  public static Rectangle mockRectangle(Point lowerLeft, Point upperRight) {
    return mockRectangle(lowerLeft.getX(), lowerLeft.getY(), upperRight.getX(),
        upperRight.getY());
  }

  /**
   * Returns a mock for interface Rectangle.
   * 
   * @param minX
   *          The minimum x value.
   * @param minY
   *          The minimum y value.
   * @param maxX
   *          The maximum x value.
   * @param maxY
   *          The maximum y value.
   * 
   * @return the created mock.
   */
  public static Rectangle mockRectangle(float minX, float minY, float maxX,
      float maxY) {
    Rectangle rect = mock(Rectangle.class);

    when(rect.getMinX()).thenReturn(minX);
    when(rect.getMinY()).thenReturn(minY);
    when(rect.getMaxX()).thenReturn(maxX);
    when(rect.getMaxY()).thenReturn(maxY);
    when(rect.getHeight()).thenReturn(maxY - minY);
    when(rect.getWidth()).thenReturn(maxX - minX);
    when(rect.getArea()).thenReturn((maxY - minY) * (maxX - minX));
    when(rect.getRectangle()).thenReturn(rect);
    when(rect.contains(any(Geometric.class))).thenAnswer(answerContains);
    when(rect.overlaps(any(Geometric.class))).thenAnswer(answerOverlaps);
    // when(rect.unite(any(Rectangle.class))).thenAnswer(answerMerge);
    when(rect.toString()).thenAnswer(answerToString);

    return rect;
  }
}
