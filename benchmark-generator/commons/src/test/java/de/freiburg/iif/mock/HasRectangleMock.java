package de.freiburg.iif.mock;

import static de.freiburg.iif.mock.RectangleMock.mockRectangle;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;

/**
 * Methods to mock HasRectangle.
 * 
 * @author Claudius Korzen
 * 
 */
public class HasRectangleMock {
  /**
   * Answers a call to toString().
   */
  public static Answer<String> answerToString = new Answer<String>() {
    @Override
    public String answer(InvocationOnMock invocation) throws Throwable {
      HasRectangle mock = (HasRectangle) invocation.getMock();

      Rectangle rect = mock.getRectangle();

      if (rect != null) {
        return "[" + rect.getMinX() + ", " + rect.getMinY() + ", "
            + rect.getMaxX() + ", " + rect.getMaxY() + "]";
      }

      return null;
    }
  };

  /**
   * Returns a mock for interface HasRectangle.
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
  public static HasRectangle mockHasRectangle(float minX, float minY,
      float maxX, float maxY) {
    Rectangle rect = mockRectangle(minX, minY, maxX, maxY);

    HasRectangle hr = mock(HasRectangle.class);
    when(hr.getRectangle()).thenReturn(rect);
    when(hr.toString()).thenAnswer(answerToString);

    return hr;
  }
}
