package de.freiburg.iif.mock;

import static de.freiburg.iif.mock.GeometricMock.answerContains;
import static de.freiburg.iif.mock.GeometricMock.answerOverlaps;
import static de.freiburg.iif.mock.GeometricMock.answerToString;
import static de.freiburg.iif.mock.RectangleMock.mockRectangle;
import static de.freiburg.iif.mock.PointMock.mockPoint;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.freiburg.iif.model.Geometric;
import de.freiburg.iif.model.Line;
import de.freiburg.iif.model.Point;
import de.freiburg.iif.model.Rectangle;

/**
 * A mock for Line.
 * 
 * @author Claudius Korzen
 * 
 */
public class LineMock {
  /**
   * Answers a call to getRectangle().
   */
  public static Answer<Rectangle> answergetRectangle =
      new Answer<Rectangle>() {
        @Override
        public Rectangle answer(InvocationOnMock invocation) throws Throwable {
          Line mock = (Line) invocation.getMock();
          return mockRectangle(mock.getStartPoint(), mock.getEndPoint());
        }
      };

  /**
   * Returns a mock for interface Point.
   * 
   * @param x1
   *          The minimum x value.
   * @param y1
   *          The minimum y value.
   * @param x2
   *          The maximum x value.
   * @param y2
   *          The maximum y value.
   * 
   * @return the created mock.
   */
  public static Line mockLine(float x1, float y1, float x2, float y2) {
    Line line = mock(Line.class);

    Point startPoint = mockPoint(x1, y1);
    Point endPoint = mockPoint(x2, y2);

    when(line.getStartPoint()).thenReturn(startPoint);
    when(line.getEndPoint()).thenReturn(endPoint);
    when(line.getRectangle()).thenAnswer(answergetRectangle);
    when(line.contains(any(Geometric.class))).thenAnswer(answerContains);
    when(line.overlaps(any(Geometric.class))).thenAnswer(answerOverlaps);
    when(line.toString()).thenAnswer(answerToString);

    return line;
  }
}
