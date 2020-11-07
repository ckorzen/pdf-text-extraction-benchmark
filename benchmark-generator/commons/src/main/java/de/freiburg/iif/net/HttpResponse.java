package de.freiburg.iif.net;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple http response.
 * 
 * @author Claudius Korzen
 */
public class HttpResponse {
  /** 
   * The output stream. 
   */
  protected OutputStream outputStream;
  
  /** 
   * The status code. 
   */
  protected int statusCode;
  
  /** 
   * The status. 
   */
  protected String status;
  
  /** 
   * The headers. 
   */
  protected Map<String, String> headers = new HashMap<String, String>();

  /** 
   * Flag to indicate whether the header is written to stream. 
   */
  protected boolean isHeaderWritten;

  /**
   * Creates a new http response object.
   * 
   * @param os
   *          the output stream.
   * @param statusCode
   *          the status code.
   * @param status
   *          the status.
   */
  public HttpResponse(OutputStream os, int statusCode, String status) {
    this.outputStream = os;
    this.statusCode = statusCode;
    this.status = status;
  }

  /**
   * Returns a success http response.
   * 
   * @param os
   *          the output stream.
   * 
   * @return a success http response.
   */
  public static HttpResponse success(OutputStream os) {
    return success(os, true);
  }

  /**
   * Returns a success http response.
   * 
   * @param os
   *          the output stream
   * @param enableCors
   *          set to true, if you wish to add cors headers.
   * 
   * @return a success http response.
   */
  public static HttpResponse success(OutputStream os, boolean enableCors) {
    HttpResponse response = new HttpResponse(os, 200, "OK");

    if (enableCors) {
      response.addHeader("Access-Control-Allow-Origin", "*");
    }

    return response;
  }

  /**
   * Adds the given key and value to the headers.
   * 
   * @param key
   *          the key.
   * @param value
   *          the value.
   */
  public void addHeader(String key, String value) {
    if (isHeaderWritten) {
      throw new IllegalStateException("You cannot add a header, because it was"
          + "already written to the stream.");
    }
    this.headers.put(key, value);
  }

  /**
   * Returns the http headers.
   * 
   * @return the headers.
   */
  public Map<String, String> getHeaders() {
    return this.headers;
  }

  /**
   * Returns the statusCode.
   * 
   * @return the statusCode.
   */
  public int getStatusCode() {
    return this.statusCode;
  }

  /**
   * Returns the status.
   * 
   * @return the status.
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * Adds the given string to the payload.
   * 
   * @param payload
   *          the string to add..
   * @throws IOException
   *           if writing to the stream fails.
   */
  public void addPayload(String payload) throws IOException {
    if (!isHeaderWritten) {
      writeHeader();
    }
    write(payload);
  }

  /**
   * Returns the output stream.
   * 
   * @return the output stream.
   * @throws IOException
   *           if opening the stream fails.
   */
  public OutputStream getPayloadStream() throws IOException {
    if (!isHeaderWritten) {
      writeHeader();
    }
    return this.outputStream;
  }

  /**
   * Writes the header.
   * 
   * @throws IOException
   *           if writing the header fails.
   */
  protected void writeHeader() throws IOException {
    // Write first line.
    write("HTTP/1.0 ");
    write(String.valueOf(statusCode));
    write(" ");
    writeln(status);

    // Write the headers.
    for (String key : headers.keySet()) {
      write(key);
      write(": ");
      writeln(headers.get(key));
    }

    // Write empty line.
    writeln("");

    isHeaderWritten = true;
  }

  /**
   * Appends a newline character to given string and writes it to the given
   * output stream.
   * 
   * @param str
   *          the string.
   * @throws IOException
   *           if writing fails.
   */
  protected void writeln(String str) throws IOException {
    write(str);
    write("\n");
  }

  /**
   * Writes the given string to the given output stream.
   * 
   * @param str
   *          the string.
   * @throws IOException
   *           if writing fails.
   */
  protected void write(String str) throws IOException {
    this.outputStream.write(str.getBytes());
  }

  /**
   * Flushes the output stream.
   * 
   * @throws IOException
   *           if flushing fails.
   */
  public void flush() throws IOException {
    if (!isHeaderWritten) {
      writeHeader();
    }
    this.outputStream.flush();
  }
}
