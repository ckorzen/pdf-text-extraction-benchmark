package de.freiburg.iif.net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple multithreaded HTTP server. The server is bound to
 * an IP address and port number and listens for incoming TCP connections.
 * 
 * @author Claudius Korzen
 */
public abstract class HttpServer {
  /** 
   * The server worker. 
   */
  protected HttpServerWorker serverWorker;

  /**
   * Starts this server at the given port.
   * 
   * @param port
   *          the port of this server.
   * @throws IOException
   *           if starting the server fails.
   */
  public void start(int port) throws IOException {
    start(null, port);
  }

  /**
   * Starts this server at the given ip address and port.
   * 
   * @param address
   *          the ip address of this server.
   * @param port
   *          the port of this server.
   * @throws IOException
   *           if starting the server fails.
   */
  public void start(InetAddress address, int port) throws IOException {
    this.serverWorker = new HttpServerWorker(address, port) {
      @Override
      public void onHttpRequest(HttpRequest req) throws IOException {
        handleHttpRequest(req);
      }
    };
    new Thread(this.serverWorker).start();
  }

  /**
   * Stops this server listening for incoming connections.
   */
  public void stop() {
    if (this.serverWorker != null) {
      this.serverWorker.close();
    }
  }
  
  /**
   * Handles the given http request.
   * 
   * @param req the http request.
   * 
   * @throws IOException
   *           if handling the request fails.
   */
  public abstract void handleHttpRequest(HttpRequest req) throws IOException;
}

/**
 * Class implementing a simple HTTP server. The server is bound to an IP address
 * and port number and listens for incoming TCP connections.
 * 
 * @author Claudius Korzen
 */
abstract class HttpServerWorker implements Runnable {
  /** The server socket. */
  protected ServerSocket serverSocket;
  /** The ip address. */
  protected InetAddress address;
  /** The port. */
  protected int port;
  /** Flag to indicate, whether or not the server is stopped. */
  protected boolean isClosed;

  /**
   * The default constructor.
   * 
   * @param port
   *          the port of this server.
   */
  public HttpServerWorker(int port) {
    this(null, port);
  }

  /**
   * The default constructor.
   * 
   * @param address
   *          the ip address of this server.
   * @param port
   *          the port of this server.
   */
  public HttpServerWorker(InetAddress address, int port) {
    this.address = null;
    this.port = port;
  }

  @Override
  public void run() {
    try {
      // Try to setup the server socket
      this.serverSocket = new ServerSocket(this.port, 50, this.address);
    } catch (IOException e) {
      throw new RuntimeException("Can't open the server socket.", e);
    }

    while (!isClosed()) {
      Socket socket = null;
      try {
        // Listen to an incoming TCP connection.
        socket = this.serverSocket.accept();
      } catch (IOException e) {
        if (!isClosed()) {
          throw new RuntimeException("Error on listening to connection", e);
        }
      }

      // Handle the socket.
      handleClientSocket(socket);
    }
  }

  /**
   * Handles the given client socket.
   * 
   * @param socket
   *          the client socket to handle.
   * @return the thread, that handles the request.
   */
  protected Thread handleClientSocket(final Socket socket) {
    ClientSocketWorker worker = new ClientSocketWorker(socket) {
      @Override
      public void onHttpReq(HttpRequest req) throws IOException {
        onHttpRequest(req);
      }
    };

    // Handle the request.
    Thread thread = new Thread(worker);
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      public void uncaughtException(Thread th, Throwable ex) {
        handleHttpRequestException(socket, ex);
      }
    });
    thread.start();
    return thread;
  }
  
  /**
   * Stops this server.
   */
  public synchronized void close() {
    try {
      this.serverSocket.close();
      this.isClosed = true;
    } catch (IOException e) {
      throw new RuntimeException("Error on closing the server");
    }
  }

  /**
   * Returns true, if this server is stopped.
   * 
   * @return true, if this server is stopped.
   */
  protected synchronized boolean isClosed() {
    return this.isClosed;
  }
  
  /**
   * Handles a http request.
   * 
   * @param req the request to handle.
   * @throws IOException if handling the request fails.
   */
  public abstract void onHttpRequest(HttpRequest req) throws IOException;
  
  /**
   * Handles an exception on handling the http request.
   * 
   * @param socket the related socket thread.
   * @param reason the thrown exception.
   */
  public void handleHttpRequestException(Socket socket, Throwable reason) {
    if (reason != null && reason.getCause() != null) {
      reason = reason.getCause();
    }
    
    writeError(socket, reason);
    
    try {
      socket.close();
    } catch (IOException e) {
      System.err.println("Error on closing the connection: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  /**
   * Writes the given error to the socket's output stream.
   * 
   * @param socket the socket.
   * @param error the error to write.
   */
  protected void writeError(Socket socket, Throwable error) {
    try {
      OutputStream stream = socket.getOutputStream();
      writeln(stream, "HTTP/1.0 500 Internal Server Error");
      if (error != null) {
        writeln(stream, "Error: " + error.toString());
        writeln(stream, "Message: " + error.getMessage());  
      }
      stream.flush();
    } catch (IOException e) {
      throw new RuntimeException("Error on writing error.", e);
    }
  }
  
  /**
   * Appends a newline character to given string and writes it to the given 
   * output stream.
   * 
   * @param stream the output stream-
   * @param str the string.
   * @throws IOException if writing fails.
   */
  protected void writeln(OutputStream stream, String str) throws IOException {
    if (stream != null) {
      stream.write((str + "\n").getBytes());
    }
  }
}

/**
 * A worker, that handles a single client socket.
 * 
 * @author Claudius Korzen
 */
abstract class ClientSocketWorker implements Runnable {
  /** The socket to handle. */
  protected Socket socket;

  /**
   * The default constructor.
   * 
   * @param socket
   *          the socket to handle.
   */
  public ClientSocketWorker(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    HttpRequest req = null;

    try {
      req = readRequest();
    } catch (IOException e) {
      throw new RuntimeException("Error on reading request", e);
    }

    try {
      onHttpReq(req);
    } catch (IOException e) {
      throw new RuntimeException("Error on handling request.", e);
    }
    
    try {
      socket.close();
    } catch (IOException e) {
      throw new RuntimeException("Error on closing the connection.", e);
    }
  }

  /**
   * Reads the request.
   * 
   * @return the created http request.
   * 
   * @throws IOException
   *           if reading the request fails.
   */
  protected HttpRequest readRequest() throws IOException {
    InputStream is = this.socket.getInputStream();
    try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {

      String httpMethod = null;
      Map<String, String> headers = new HashMap<String, String>();
      byte[] payload = null;
  
      // Read the first line to get the http method.
      String line = r.readLine();
      if (line != null) {
        httpMethod = line.split(" ")[0].toUpperCase();
      } else {
        throw new IllegalArgumentException("The http header is malformed.");
      }
  
      // Read the headers
      while ((line = r.readLine()) != null) {
        // The headers are sepeated by an empty line.
        if (line.isEmpty()) {
          break;
        }
  
        int index = line.indexOf(": ");
        if (index > -1) {
          String key = line.substring(0, index);
          String value = line.substring(index + 2);
          headers.put(key, value);
        }
      }
  
      // Read the payload.
      String contentLengthStr = headers.get("Content-Length");
      if (contentLengthStr != null) {
        int contentLength = 0;
        try {
          contentLength = Integer.parseInt(contentLengthStr);
        } catch (NumberFormatException e) {
          throw new IOException("The field 'Content-Length' is malformed.");
        }
  
        if (contentLength > 0) {
          ByteArrayOutputStream os = new ByteArrayOutputStream();
  
          for (int i = 0; i < contentLength; i++) {
            os.write(r.read());
          }
          payload = os.toByteArray();
          os.close();
        }
      }
      return new HttpRequest(this.socket, httpMethod, headers, payload);
    }
  }

  /**
   * Handles the incoming http request.
   * 
   * @param req
   *          the http request.
   * @throws IOException
   *           if handling the request fails.
   */
  public abstract void onHttpReq(HttpRequest req) throws IOException;
};