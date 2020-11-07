package de.freiburg.iif.cmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

/**
 * Some util methods to run commands using the runtime environment.
 *
 * @author Claudius Korzen
 */
public class CommandLine {
  /**
   * Runs the given command on the system's command line.
   */
  public static int runCommand(final String cmd) throws IOException, 
      InterruptedException, TimeoutException {
    return runCommand(cmd, null);
  }
  
  /**
   * Runs the given command on the system's command line.
   */
  public static int runCommand(final String cmd, final String[] environment) 
      throws IOException, InterruptedException, TimeoutException {
    return runCommand(cmd, null, null);
  }
  
  /**
   * Runs the given command on the system's command line.
   */
  public static int runCommand(final String cmd, final String[] environment,
      final Path workingDirectory) throws IOException, InterruptedException,
        TimeoutException {
    return runCommand(cmd, environment, workingDirectory, 0);
  }

  /**
   * Runs the given command on the system's command line. Define a
   * timeout > 0, so that a TimeoutException is thrown if this method doesn't 
   * finish in time.
   */
  public static int runCommand(final String cmd, final String[] environment,
      final Path workingDirectory, final long timeoutInMs)
        throws IOException, InterruptedException, TimeoutException {
    Runtime runtime = Runtime.getRuntime();
    File cwd = workingDirectory != null ? workingDirectory.toFile() : null;
    Process process = runtime.exec(cmd, environment, cwd);
    
    Worker worker = new Worker(process);
    worker.start();
    try {
      worker.join(timeoutInMs);
      if (worker.exit != null) {
        return worker.exit;
      } else {
        throw new TimeoutException();
      }
    } catch (InterruptedException ex) {
      worker.interrupt();
      Thread.currentThread().interrupt();
      throw ex;
    } finally {
      process.destroy();
    }
  }

  /**
   * A wrapper class for a given process.
   *
   * @author Claudius Korzen
   */
  private static class Worker extends Thread {
    /**
     * The process to wrap.
     */
    private final Process process;
    /**
     * The exit code of the process.
     */
    private Integer exit;

    /**
     * The default constructor.
     */
    private Worker(Process process) {
      this.process = process;
    }

    @Override
    public void run() {
      try {
        exit = process.waitFor();
      } catch (InterruptedException ignore) {
        return;
      }
    }
  }
}
