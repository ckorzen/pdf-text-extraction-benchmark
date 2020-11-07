package de.freiburg.iif.path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Some util methods regarding files / paths.
 *
 * @author Claudius Korzen
 *
 */
public class PathUtils {
  /**
   * Reads the given path to string.
   */
  public static String readPathContentToString(Path path) throws IOException {
    return new String(Files.readAllBytes(path));
  }

  /**
   * Cleans the given directory without deleting it.
   */
  public static void cleanDirectory(Path directory) throws IOException {
    if (!Files.exists(directory)) {
      String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!Files.isDirectory(directory)) {
      String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    IOException exception = null;
    try (DirectoryStream<Path> directoryStream =
        Files.newDirectoryStream(directory)) {
      for (Path p : directoryStream) {
        try {
          delete(p);
        } catch (IOException ioe) {
          exception = ioe;
        }
      }
    }

    if (exception != null) {
      throw exception;
    }
  }

  /**
   * Deletes a directory and its content recursively.
   */
  public static void deleteDirectory(Path directory) throws IOException {
    if (!Files.exists(directory)) {
      return;
    }

    cleanDirectory(directory);
    Files.delete(directory);
  }

  /**
   * Deletes a file or directory. A directory does not have to be empty.
   */
  public static void delete(Path fileOrDirectory) throws IOException {
    if (Files.isDirectory(fileOrDirectory)) {
      deleteDirectory(fileOrDirectory);
    } else {
      Files.deleteIfExists(fileOrDirectory);
    }
  }

  /**
   * Compares the contents of the two given paths. If both paths don't exist,
   * the contents aren't equal and this method returns false.
   */
  public static boolean contentEquals(Path p1, Path p2) throws IOException {
    if (!Files.exists(p1) || !Files.exists(p2)) {
      return false;
    }

    if (Files.isDirectory(p1) && Files.isDirectory(p2)) {
      return directoryContentEquals(p1, p2);
    }

    if (p1.equals(p2)) {
      // same filename => true
      return true;
    }

    if (Files.size(p1) != Files.size(p2)) {
      // different size =>false
      return false;
    }

    try (InputStream in1 = Files.newInputStream(p1);
        InputStream in2 = Files.newInputStream(p1)) {
      int expectedByte = in1.read();
      while (expectedByte != -1) {
        if (expectedByte != in2.read()) {
          return false;
        }
        expectedByte = in1.read();
      }
      if (in2.read() != -1) {
        return false;
      }
      return true;
    }
  }

  /**
   * Compare the contents of two directories to determine if they are equal or
   * not. If both paths don't exist, the contents aren't equal and this method
   * returns false.
   */
  public static boolean directoryContentEquals(Path dir1, Path dir2)
    throws IOException {
    boolean dir1Exists = Files.exists(dir1) && Files.isDirectory(dir1);
    boolean dir2Exists = Files.exists(dir2) && Files.isDirectory(dir2);

    if (dir1Exists && dir2Exists) {
      HashMap<Path, Path> dir1Paths = new HashMap<>();
      HashMap<Path, Path> dir2Paths = new HashMap<>();

      // Map the path relative to the base directory to the complete path.
      for (Path p : listPaths(dir1)) {
        dir1Paths.put(dir1.relativize(p), p);
      }

      for (Path p : listPaths(dir2)) {
        dir2Paths.put(dir2.relativize(p), p);
      }

      // The directories cannot be equal if the number of files aren't equal.
      if (dir1Paths.size() != dir2Paths.size()) {
        return false;
      }

      // For each file in dir1, check if also dir2 contains this file and if
      // their contents are equal.
      for (Entry<Path, Path> pathEntry : dir1Paths.entrySet()) {
        Path relativePath = pathEntry.getKey();
        Path absolutePath = pathEntry.getValue();
        if (!dir2Paths.containsKey(relativePath)) {
          return false;
        } else {
          if (!contentEquals(absolutePath, dir2Paths.get(relativePath))) {
            System.out.println(absolutePath);
            return false;
          }
        }
      }
      return true;
    }

    return false;
  }

  /**
   * Recursively finds all files with given extensions in the given directory
   * and all of its sub-directories.
   */
  public static List<Path> listPaths(Path file, String... extensions)
    throws IOException {
    if (file == null) {
      return null;
    }

    List<Path> paths = new ArrayList<>();
    listPaths(file, paths, extensions);

    return paths;
  }

  /**
   * Recursively finds all paths with given extensions in the given directory
   * and all of its sub-directories.
   */
  protected static void listPaths(Path path, List<Path> result,
      String... extensions)
    throws IOException {
    if (path == null) {
      return;
    }

    if (Files.isReadable(path)) {
      // If the path is a directory try to read it.
      if (Files.isDirectory(path)) {
        // The input is a directory. Read its files.
        try (DirectoryStream<Path> directoryStream =
            Files.newDirectoryStream(path)) {
          for (Path p : directoryStream) {
            listPaths(p, result, extensions);
          }
        }
      } else {
        String filename = path.getFileName().toString();
        if (extensions.length == 0) {
          result.add(path);
        } else {
          for (String extension : extensions) {
            if (filename.toLowerCase().endsWith(extension)) {
              result.add(path);
              break;
            }
          }
        }
      }
    }
  }

  /**
   * Renames the given path to the given new name. Existing files / directories
   * will be replaced.
   */
  public static Path rename(Path path, String name) throws IOException {
    Path target = path.resolveSibling(name);

    // If target is a directory, we have to delete it, because move will not
    // overwrite an existing directory (but an existing file).
    if (Files.isDirectory(target)) {
      deleteDirectory(target);
    }

    return Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Copies the given input file/directory to the given directory. Existing
   * files / directories will be replaced.
   */
  public static void copy(Path file, Path outputDir) throws Exception {
    if (Files.isRegularFile(file)) {
      Files.createDirectories(outputDir);
      Files.copy(file, outputDir.resolve(file.getFileName().toString()),
          StandardCopyOption.REPLACE_EXISTING);
    } else if (Files.isDirectory(file)) {
      Files.createDirectories(outputDir);
      try (DirectoryStream<Path> files = Files.newDirectoryStream(file)) {
        Iterator<Path> filesItr = files.iterator();
        while (filesItr.hasNext()) {
          Path f = filesItr.next();
          Path target = outputDir.resolve(f.getFileName().toString());

          if (Files.isDirectory(target)) {
            cleanDirectory(target);
          }

          Files.copy(f, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  /**
   * Moves the given input to the given directory. If the input is a directory,
   * all children files/directories will be moved to the target directory and
   * the input directory will be deleted.
   */
  public static void move(Path file, Path outputDir) throws Exception {
    if (Files.isRegularFile(file)) {
      Files.createDirectories(outputDir);
      Files.move(file, outputDir.resolve(file.getFileName().toString()),
          StandardCopyOption.REPLACE_EXISTING);
    } else if (Files.isDirectory(file)) {
      Files.createDirectories(outputDir);
      try (DirectoryStream<Path> files = Files.newDirectoryStream(file)) {
        Iterator<Path> filesItr = files.iterator();
        while (filesItr.hasNext()) {
          Path f = filesItr.next();
          Path target = outputDir.resolve(f.getFileName().toString());

          if (Files.isDirectory(target)) {
            cleanDirectory(target);
          }

          Files.move(f, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
      Files.delete(file);
    }
  }

  /**
   * Returns the basename of the given file, that is the filename without the
   * file-extension.
   */
  public static String getBasename(String path) {
    return getBasename(Paths.get(path));
  }

  /**
   * Returns the basename of the given file, that is the filename without the
   * file-extension.
   */
  public static String getBasename(Path file) {
    if (file != null) {
      String filename = file.getFileName().toString();
      String[] tokens = filename.split("\\.(?=[^\\.]+$)");
      return tokens[0];
    }
    return null;
  }

  /**
   * Returns the file extension of the given file.
   */
  public static String getExtension(Path file) {
    if (file != null) {
      String filename = file.getFileName().toString();
      String[] tokens = filename.split("\\.(?=[^\\.]+$)");
      return tokens.length > 1 ? tokens[1] : "";
    }
    return null;
  }

  /**
   * Reads the given directory non-recursively and returns a map with the names
   * and the streams of all files in this directory. Works also in jar files.
   */
  public static Map<String, InputStream> readDirectory(String path)
    throws IOException {
    Map<String, InputStream> streams = new HashMap<>();
    ClassLoader classLoader = PathUtils.class.getClassLoader();
    ProtectionDomain domain = PathUtils.class.getProtectionDomain();

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

        if (name.startsWith(path) && !name.equals(path)) {
          streams.put(name, classLoader.getResourceAsStream(name));
        }
      }
      jar.close();
    } else {
      // We are not in a jar file.
      File directory = null;
      try {
        // Read the directory.
        directory = new File(classLoader.getResource(path).toURI());
      } catch (Exception e) {
        return streams;
      }

      for (File file : directory.listFiles()) {
        try {
          streams.put(file.getName(), new FileInputStream(file));
        } catch (Exception e) {
          continue;
        }
      }
    }
    return streams;
  }

  /**
   * Returns the working directory of the given class (its usually the target
   * dir). Works also in jar files.
   */
  public static String getWorkingDirectory(Class<?> clazz) {
    ProtectionDomain domain = clazz.getProtectionDomain();
    CodeSource codeSource = domain.getCodeSource();
    Path file = Paths.get(codeSource.getLocation().getPath()).toAbsolutePath();

    if (Files.exists(file)) {
      Path parent = file.getParent();

      return parent.toString();
    }

    return null;
  }

}
