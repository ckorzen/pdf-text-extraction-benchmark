package de.freiburg.iif.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Class that represents an external program to run on the command line.
 * 
 * @author Claudius Korzen
 *
 */
public class ExternalProgram {
  /** The environment variables to set needed by the command to execute. */
  protected Map<String, String> environment;
  /** The command to execute (e.g. "svn"). */
  protected final String command;
  /** The subcommands to execute (e.g. in "svn up", "up" is a subcommand . */
  protected List<String> subcommands;
  /** The options. */
  protected List<String> options;
  /** The arguments. */
  protected List<String> arguments;
  /** The timeout for the command to wait in ms. */
  protected int timeOutInMs = 0;

  /** The created process instance. */
  protected Process process;
  /** The flag that indicates whether this program is closed. */
  protected boolean isClosed;

  /** The output in case of an error. */
  protected String errorString;

  /**
   * Creates a new program with the given command.
   */
  public ExternalProgram(String command) {
    this.command = command;
    this.subcommands = new ArrayList<>();
    this.options = new ArrayList<>();
    this.arguments = new ArrayList<>();
    this.environment = new HashMap<>();
  }

  /**
   * Creates a new program with the given command.
   */
  public ExternalProgram(String command, String... subcommands) {
    this(command);
    setSubCommands(Arrays.asList(subcommands));
  }

  // ---------------------------------------------------------------------------

  /**
   * Adds the given key/value-pair as environment variable.
   */
  public void addEnvironmentVariable(String key, String value) {
    this.environment.put(key, value);
  }

  /**
   * Returns the environment variables of this program.
   */
  public Map<String, String> getEnvironmentVariables() {
    return this.environment;
  }

  // ---------------------------------------------------------------------------

  /**
   * Sets the subcommands of this program.
   */
  public void setSubCommands(List<String> subcommands) {
    this.subcommands = subcommands;
  }

  /**
   * Adds the given option to this program.
   */
  public void addSubCommand(String subcommand) {
    this.subcommands.add(subcommand);
  }

  // ---------------------------------------------------------------------------

  /**
   * Sets the options of this program.
   */
  public void setOptions(List<String> options) {
    this.options = options;
  }

  /**
   * Adds the given option to this program.
   */
  public void addOption(String option) {
    addOption(option, "");
  }

  /**
   * Adds the given option to this program.
   */
  public void addOption(String option, String value) {
    this.options.add(option);
    this.options.add(value);
  }

  // ---------------------------------------------------------------------------

  /**
   * Sets the arguments of this program.
   */
  public void setArguments(List<String> args) {
    this.arguments = args;
  }

  /**
   * Adds the given option to this program.
   */
  public void addArgument(String arg) {
    this.arguments.add(arg);
  }

  // ---------------------------------------------------------------------------

  /**
   * Sets the timeout to wait for this program.
   */
  public void setTimeOutInMs(int timeOutInMs) {
    this.timeOutInMs = timeOutInMs;
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns the command of this program.
   */
  public String getCommand() {
    return command;
  }

  /**
   * Returns the subcommand of this program.
   */
  public List<String> getSubCommands() {
    return subcommands;
  }

  /**
   * Returns the options of this program.
   */
  public List<String> getOptions() {
    return options;
  }

  /**
   * Returns the arguments of this program.
   */
  public List<String> getArguments() {
    return arguments;
  }

  /**
   * Returns thes timeout in ms.
   */
  public int getTimeOutInMs() {
    return timeOutInMs;
  }

  /**
   * Returns the error string.
   */
  public String getErrorString() {
    return this.errorString;
  }

  // ===========================================================================

  /**
   * Runs this program.
   */
  public int run() throws IOException, TimeoutException {
    return run(false);
  }

  /**
   * Runs this program.
   */
  public int run(boolean close) throws IOException, TimeoutException {
    this.process = createProcess();

    Worker worker = new Worker(this.process);
    worker.start();
    try {
      worker.join(this.timeOutInMs);
      if (worker.exit != null) {
        return worker.exit;
      } else {
        this.errorString = parseStream(process.getErrorStream());
        close();
        throw new TimeoutException();
      }
    } catch (InterruptedException ex) {
      worker.interrupt();
      Thread.currentThread().interrupt();
      close();
      throw new IOException(ex);
    } finally {
      this.errorString = parseStream(process.getErrorStream());
      if (close) {
        close();
      }
    }
  }

  /**
   * Closes this program.
   */
  public void close() {
    if (this.process != null) {
      this.process.destroy();
      this.isClosed = true;
    }
  }

  /**
   * Creates a Process instance.
   */
  protected Process createProcess() throws IOException {
    List<String> commandList = new ArrayList<>();

    // Add the command.
    commandList.add(getCommand());

    // Add all subcommands.
    commandList.addAll(getSubCommands());

    // Add all options.
    commandList.addAll(getOptions());

    // Add all arguments.
    commandList.addAll(getArguments());

    ProcessBuilder builder = new ProcessBuilder(commandList);

    // Add all environment variables.
    builder.environment().putAll(getEnvironmentVariables());

    return builder.start();
  }

  /**
   * Reads the given stream into a string.
   */
  public String parseStream(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();

    if (is != null) {
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(is))) {
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
          sb.append("\\n");
        }
      }
    }

    return sb.toString();
  }

  /**
   * Returns the result stream of running this program.
   */
  public InputStream getStream() {
    if (process == null) {
      throw new IllegalStateException(
          "You have to call run() first to get the result stream.");
    }

    if (isClosed) {
      throw new IllegalStateException("This program is already closed.");
    }

    if (process.exitValue() == 0) {
      return this.process.getInputStream();
    } else {
      return this.process.getErrorStream();
    }
  }

  // ===========================================================================

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
        // TODO: Because some native platforms only provide limited buffer size 
        // for standard input and output streams, failure to promptly write the
        // input stream or read the output stream of the subprocess may cause 
        // the subprocess to block, and even deadlock.

        // Fail to clear the buffer of input stream (which pipes to the output 
        // stream of subprocess) from Process may lead to a subprocess blocking.

        // From: http://stackoverflow.com/questions/5483830/process-waitfor-never-returns

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()))) {
//          String line;
//          while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//          }
          while ((reader.readLine()) != null) {
          }
        }
        exit = process.waitFor();
      } catch (IOException ignore) {
        return;
      } catch (InterruptedException ignore) {
        return;
      }
    }
  }
}
