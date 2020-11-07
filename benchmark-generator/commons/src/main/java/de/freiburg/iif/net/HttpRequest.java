package de.freiburg.iif.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * A simple http request.
 * 
 * @author Claudius Korzen
 */
public class HttpRequest {
  /** 
   * The client socket. 
   */
  protected Socket clientSocket;
  
  /** 
   * The http method. 
   */
  protected String httpMethod;
  
  /** 
   * The headers. 
   */
  protected Map<String, String> headers;
  
  /** 
   * The payload. 
   */
  protected byte[] payload;
  
  /** 
   * The http response. 
   */
  protected HttpResponse response;

  /**
   * Creates a new http request object.
   * 
   * @param clientSocket
   *          the client socket.
   * @param httpMethod
   *          the http method.
   * @param headers
   *          the headers.
   * @param payload
   *          the payload.
   */
  public HttpRequest(Socket clientSocket, String httpMethod,
      Map<String, String> headers, byte[] payload) {
    this.clientSocket = clientSocket;
    this.httpMethod = httpMethod;
    this.headers = headers;
    this.payload = payload;
  }

  /**
   * Returns the output stream.
   * 
   * @return the output stream.
   * @throws IOException
   *           if opening the output stream fails.
   */
  public OutputStream getOutputStream() throws IOException {
    return this.clientSocket.getOutputStream();
  }

  /**
   * Returns the http method.
   * 
   * @return the http method.
   */
  public String getMethod() {
    return this.httpMethod;
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
   * Returns the payload.
   * 
   * @return the payload.
   */
  public byte[] getPayload() {
    return this.payload;
  }

  /**
   * Creates a http response for this http request.
   * 
   * @param statusCode
   *          the statuscode of the response to create.
   * @param status
   *          the status of the response to create.
   * @return the created http response.
   * @throws IOException
   *           if creating the response fails.
   */
  public HttpResponse createHttpResponse(int statusCode, String status)
    throws IOException {
    if (this.response == null) {
      OutputStream stream = this.clientSocket.getOutputStream();
      this.response = new HttpResponse(stream, statusCode, status);
      return this.response;
    }
    throw new IllegalStateException("You can't create multiple http responses");
  }
}