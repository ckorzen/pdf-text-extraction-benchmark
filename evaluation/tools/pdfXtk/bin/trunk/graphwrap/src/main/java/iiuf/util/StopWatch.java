package iiuf.util;

/**
   A handy stopwatch class.
   
   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/

public class StopWatch {
  final static int STATE_RUNNING = 0;
  final static int STATE_STOPPED = 1;

  int state;
  long started;
  long stopped;

  public StopWatch() {
    state = STATE_STOPPED;
  }

  /** Start the watch. */

  public StopWatch start() {
    started = System.currentTimeMillis();
    state = STATE_RUNNING;
    return this;
  }

  /** Stop the stopwatch.

      @return the number of milliseconds elapsed. */

  public long stop() {
    stopped = System.currentTimeMillis();
    if (state == STATE_RUNNING) {
      state = STATE_STOPPED;
    } else {
      // Stopwatch was not running, make sure it's 0
      started = stopped;
    }
    return getMillis();
  }

  /** If the watch is still running, returns the number of milliseconds elapsed
      since the start. If the watch is stopped, returns the number of milliseconds
      between start and stop.

      @return milliseconds */

  public long getMillis() {
    long stopped = System.currentTimeMillis();
    if (state == STATE_RUNNING) {
      return stopped - started;
    } else {
      return this.stopped - started;
    }
  }

  /** Returns the elapsed time in a human-readable format.

      @return time elapsed */

  public String toString() {
    long t = getMillis();
    long frac = t%1000;

    String fracPart = "";

    if (frac >= 100) {
      fracPart = ""+frac;
    } else if (frac >= 10) {
      fracPart = "0"+frac;
    } else {
      fracPart = "00"+frac;
    }

    return Long.toString(t/1000)+"."+fracPart+"s";
  }
}
