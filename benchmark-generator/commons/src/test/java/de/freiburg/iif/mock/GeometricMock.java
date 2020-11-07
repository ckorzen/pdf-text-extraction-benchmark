package de.freiburg.iif.mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.freiburg.iif.model.Geometric;
import de.freiburg.iif.model.Rectangle;

/**
 * A mock for Geometric.
 * 
 * @author Claudius Korzen.
 * 
 */
public class GeometricMock {
  /**
   * Answers a call to toString(any(Rectangle.class)).
   */
  public static Answer<String> answerToString = new Answer<String>() {
    @Override
    public String answer(InvocationOnMock invocation) throws Throwable {
      Geometric mock = (Geometric) invocation.getMock();

      Rectangle rect = mock.getRectangle();
      if (rect != null) {
        return "[" + rect.getMinX() + ", " + rect.getMinY() + ", "
            + rect.getMaxX() + ", " + rect.getMaxY() + "]";
      }

      return null;
    }
  };

  /**
   * Answers a call to contains(any(Geometric.class)).
   */
  public static Answer<Boolean> answerContains = new Answer<Boolean>() {
    @Override
    public Boolean answer(InvocationOnMock invocation) throws Throwable {
      Geometric mock = (Geometric) invocation.getMock();
      Geometric arg = (Geometric) invocation.getArguments()[0];

      if (arg == null) {
        return false;
      }

      if (arg.getRectangle().getMinX() < mock.getRectangle().getMinX()) {
        return false;
      }
      if (arg.getRectangle().getMaxX() > mock.getRectangle().getMaxX()) {
        return false;
      }
      if (arg.getRectangle().getMinY() < mock.getRectangle().getMinY()) {
        return false;
      }
      if (arg.getRectangle().getMaxY() > mock.getRectangle().getMaxY()) {
        return false;
      }
      return true;
    }
  };

  /**
   * Answers a call to overlaps(any(Geometric.class)).
   */
  public static Answer<Boolean> answerOverlaps = new Answer<Boolean>() {
    @Override
    public Boolean answer(InvocationOnMock invocation) throws Throwable {
      Geometric m = (Geometric) invocation.getMock();
      Geometric arg = (Geometric) invocation.getArguments()[0];

      // If one rectangle is on left side of other
      if (m.getRectangle().getMinX() >= arg.getRectangle().getMaxX()
          || m.getRectangle().getMaxX() <= arg.getRectangle().getMinX()) {
        return false;
      }

      // If one rectangle is above other
      if (m.getRectangle().getMaxY() <= arg.getRectangle().getMinY()
          || m.getRectangle().getMinY() >= arg.getRectangle().getMaxY()) {
        return false;
      }

      return true;
    }
  };
}
