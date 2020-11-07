package de.freiburg.iif.mock;

import static de.freiburg.iif.mock.GeometricMock.answerContains;
import static de.freiburg.iif.mock.GeometricMock.answerOverlaps;
import static de.freiburg.iif.mock.GeometricMock.answerToString;
import static de.freiburg.iif.mock.RectangleMock.mockRectangle;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.freiburg.iif.model.Geometric;
import de.freiburg.iif.model.Point;
import de.freiburg.iif.model.Rectangle;

/**
 * A Mock for Point.
 * 
 * @author Claudius Korzen.
 * 
 */
public class PointMock {
  /**
   * Answers a call to getRectangle().
   */
  public static Answer<Rectangle> answerGetBBox = new Answer<Rectangle>() {
    @Override
    public Rectangle answer(InvocationOnMock invocation) throws Throwable {
      Point mock = (Point) invocation.getMock();
      return mockRectangle(mock.getX(), mock.getY(), mock.getX(), mock.getY());
    }
  };

  /**
   * Returns a mock for interface Point.
   * 
   * @param x
   *          The x value.
   * @param y
   *          The y value.
   * @return the created mock.
   */
  public static Point mockPoint(float x, float y) {
    Point point = mock(Point.class);

    when(point.getX()).thenReturn(x);
    when(point.getY()).thenReturn(y);
    when(point.getRectangle()).thenAnswer(answerGetBBox);
    when(point.contains(any(Geometric.class))).thenAnswer(answerContains);
    when(point.overlaps(any(Geometric.class))).thenAnswer(answerOverlaps);
    when(point.toString()).thenAnswer(answerToString);

    return point;
  }
}
