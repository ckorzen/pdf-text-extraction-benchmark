package de.freiburg.iif.path;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple parser that parses directories for certain files.
 *
 * @author Claudius Korzen
 */
public class FileAndDirectoryParser {
  /**
   * The prefix of files to consider on parsing.
   */
  protected String prefix = "";

  /**
   * The suffix of files to consider on parsing.
   */
  protected String suffix = "";

  /**
   * Flag that indicates if we have to scan directories recursively.
   */
  protected boolean recursive;

  // ___________________________________________________________________________

  /**
   * Scans the given path and return all files that matches the given 
   * parameters.
   */
  public List<Path> parse(Path path) throws IOException {
    List<Path> files = new ArrayList<>();
        
    // If the path is a directory try to read it.
    if (Files.isDirectory(path)) {
      // The input is a directory. Read its files.
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
        for (Path p : ds) {
          parse(p, files);
        }
      }
    }
      
    if (Files.isRegularFile(path)) {
      if (considerFile(path)) {
        files.add(path);
      }
    }
    
    return files;
  }
  
  /**
   * Parses the given path and fills the parsed files into given list.
   */
  protected void parse(Path path, List<Path> found) throws IOException {
    if (path == null) {
      return;
    }

    if (!Files.isReadable(path)) {
      return;
    }
    
    // If the path is a directory try to read it.
    if (Files.isDirectory(path) && scanRecursive()) {
      // The input is a directory. Read its files.
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
        for (Path p : ds) {
          parse(p, found);
        }
      }
      return;
    }
      
    if (Files.isRegularFile(path) && considerFile(path)) {
      found.add(path);
    }
  }
  
  /**
   * Returns true, if we have to consider the given file.
   */
  protected boolean considerFile(Path path) {
    if (path == null) {
      return false;
    }
    
    Path filename = path.getFileName();
    if (filename == null) {
      return false;
    }
    
    String filenameStr = filename.toString();
    if (filenameStr == null) {
      return false;
    }
    
    filenameStr = filenameStr.toLowerCase();
    
    return filenameStr.startsWith(getPrefix()) 
        && filenameStr.endsWith(getSuffix());
  }
  
  // ___________________________________________________________________________
  
  /**
   * Returns the prefix of the files to consider on parsing.
   */
  public String getPrefix() {
    return prefix != null ? prefix.toLowerCase() : null;
  }

  /**
   * Sets the prefix of files to consider on parsing.
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Returns the lowercased suffix of files to consider on parsing.
   */
  public String getSuffix() {
    return suffix != null ? suffix.toLowerCase() : null;
  }

  /**
   * Sets the suffix of files to consider.
   */
  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  /**
   * Returns the recursive flag.
   */
  public boolean scanRecursive() {
    return recursive;
  }

  /**
   * Sets the recursive flag.
   */
  public void setScanRecursive(boolean recursive) {
    this.recursive = recursive;
  }
}
