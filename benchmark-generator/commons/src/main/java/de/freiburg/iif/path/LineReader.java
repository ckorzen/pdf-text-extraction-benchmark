package de.freiburg.iif.path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class to read a given path or stream line by line.
 *
 * @author Claudius Korzen
 */
public abstract class LineReader {
  /**
   * The encoding to use on reading.
   */
  protected Charset encoding;

  /**
   * Creates a new LineReader.
   */
  public LineReader() {
    this.encoding = StandardCharsets.UTF_8;
  }

  /**
   * Creates a new LineReader that uses the given encoding on reading.
   */
  public LineReader(Charset encoding) {
    this.encoding = encoding;
  }

  /**
   * Reads the given path line by line. This method will check, if it is
   * executed inside a jar file and tries to find the resource in jar file.
   */
  public void read(String path) throws IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    ProtectionDomain domain = this.getClass().getProtectionDomain();

    // We need to distinguish, if we are in a jar file or not.
    CodeSource codeSource = domain.getCodeSource();
    Path jarFile = Paths.get(codeSource.getLocation().getPath());

    // Check, if we are in jar file.
    if (Files.isRegularFile(jarFile)) {
      final JarFile jar = new JarFile(jarFile.toFile());
      // Fetch all files in the jar.
      final Enumeration<JarEntry> entries = jar.entries();

      while (entries.hasMoreElements()) {
        final String name = entries.nextElement().getName();
        // filter according to the path

        if (name.equals(path)) {
          try (InputStream stream = classLoader.getResourceAsStream(name)) {
            readStream(stream);
          }
        }
      }
      jar.close();
    } else {
      try (InputStream is = classLoader.getResourceAsStream(path)) {
        readStream(is);
      }
    }
  }

  /**
   * Reads the given stream line by line.
   */
  public void read(InputStream stream) {
    readStream(stream);
  }

  /**
   * Reads the given stream line by line.
   */
  protected void readStream(InputStream stream) {
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(stream, this.encoding))) {
      String line;
      while ((line = br.readLine()) != null) {
        handleLine(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
  }

  /**
   * Handles the given line.
   */
  public abstract void handleLine(String line);
}
