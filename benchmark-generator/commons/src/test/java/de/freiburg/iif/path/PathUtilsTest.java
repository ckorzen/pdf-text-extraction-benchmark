package de.freiburg.iif.path;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the PathsUtil.
 *
 * @author Claudius Korzen
 */
public class PathUtilsTest {
  // ___________________________________________________________________________
  // Setup.

  /**
   * Setting up.
   */
  @Before
  public void setup() throws IOException {
    // Create a simple directory structure for testing cleanDirectory().
    Path base = Paths.get("src/test/resources/PathsUtilTest/cleanDirectory");
    createDirectoryStructure(base);

    // Create a simple directory structure for testing deleteDirectory().
    base = Paths.get("src/test/resources/PathsUtilTest/deleteDirectory");
    createDirectoryStructure(base);

    // Create a simple directory structure for testing deleteDirectory().
    base = Paths.get("src/test/resources/PathsUtilTest/delete");
    createDirectoryStructure(base);
  }

  /**
   * Creates a simple directory structure in the given base directory.
   */
  public void createDirectoryStructure(Path base) throws IOException {
    if (!Files.exists(base)) {
      Files.createDirectory(base);
      Files.createFile(base.resolve("file1.json"));
    }
    Path subdir1 = base.resolve("subdir-1");
    if (!Files.exists(subdir1)) {
      Files.createDirectory(subdir1);
      Files.createFile(subdir1.resolve("file2.txt"));
      Files.createFile(subdir1.resolve("file3.xml"));
    }
    Path subdir2 = base.resolve("subdir-2");
    if (!Files.exists(subdir2)) {
      Files.createDirectory(subdir2);
      Files.createFile(subdir2.resolve("file4.tsv"));
    }
  }

  /**
   * Tear down.
   */
  @After
  public void tearDown() throws IOException {
    // Create a simple directory structure for testing cleanDirectory().
    Path base = Paths.get("src/test/resources/PathsUtilTest/cleanDirectory");
    PathUtils.cleanDirectory(base);

    // Delete the directory for testing deleteDirectory() so that it can be
    // created again the next time.
    base = Paths.get("src/test/resources/PathsUtilTest/deleteDirectory");
    PathUtils.deleteDirectory(base);
    Assert.assertFalse(Files.exists(base));

    // Delete the directory for testing delete() so that it can be
    // created again the next time.
    base = Paths.get("src/test/resources/PathsUtilTest/delete");
    PathUtils.deleteDirectory(base);
    Assert.assertFalse(Files.exists(base));
  }

  // ___________________________________________________________________________
  // Test the readPathToString() method.

  // ___________________________________________________________________________
  // Test the readPathToString() method.

  /**
   * Test the readPathToString() method without a path.
   */
  @Test(expected = NullPointerException.class)
  public void testReadPathContentToStringWithNoPath() throws IOException {
    PathUtils.readPathContentToString(null);
  }

  /**
   * Test the readPathToString() method with invalid path.
   */
  @Test(expected = NoSuchFileException.class)
  public void testReadPathContentToStringWithInvalidPath() throws IOException {
    PathUtils.readPathContentToString(Paths.get("path/that/does/not/exist/"));
  }

  /**
   * Test the readPathToString() method with directory.
   */
  @Test(expected = IOException.class)
  public void testReadPathContentToStringWithDirectory() throws IOException {
    PathUtils.readPathContentToString(Paths.get("src/test/resources/input/"));
  }

  /**
   * Test the readPathToString() method with valid file.
   */
  @Test
  public void testReadPathContentToStringWithFile() throws IOException {
    String path = "src/test/resources/input/subdir-1/subdir-1-1/no-pdf.txt";
    Path p = Paths.get(path);
    Assert.assertEquals("This is no pdf file.",
        PathUtils.readPathContentToString(p));
  }

  // ___________________________________________________________________________
  // Test the cleanDirectory() method.

  /**
   * Test the cleanDirectory() method without a path.
   */
  @Test(expected = NullPointerException.class)
  public void testCleanDirectoryWithNoPath() throws IOException {
    PathUtils.cleanDirectory(null);
  }

  /**
   * Test the cleanDirectory() method with a file.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCleanDirectoryWithFile() throws IOException {
    Path path = Paths.get("src/test/resources/PathsUtilTest/cleanDirectory");
    PathUtils.cleanDirectory(path.resolve("file1.json"));
  }

  /**
   * Test the cleanDirectory() method with a directory, that doesn't exist.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCleanDirectoryWithInvalidDirectory() throws IOException {
    Path path = Paths.get("path/that/does/not/exist/");
    PathUtils.cleanDirectory(path);
  }

  /**
   * Test the cleanDirectory() method with a directory.
   */
  @Test
  public void testCleanDirectoryWithDirectory() throws IOException {
    Path dir = Paths.get("src/test/resources/PathsUtilTest/cleanDirectory");

    Assert.assertTrue(Files.exists(dir));
    Assert.assertTrue(Files.isDirectory(dir));

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      Iterator<Path> dirItr = stream.iterator();
      // Assert, that the directory is not empty before cleaning it.
      Assert.assertTrue(dirItr.hasNext());
    }
    
    PathUtils.cleanDirectory(dir);
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      Iterator<Path> dirItr = stream.iterator();
      // Assert, that the directory is now empty after cleaning it.
      Assert.assertFalse(dirItr.hasNext());
      // Assert, that the directory itself still exists.
      Assert.assertTrue(Files.exists(dir));
    }
  }

  // ___________________________________________________________________________
  // Test the deleteDirectory() method.

  /**
   * Test the deleteDirectory() method without a path.
   */
  @Test(expected = NullPointerException.class)
  public void testDeleteDirectoryWithNoPath() throws IOException {
    PathUtils.deleteDirectory(null);
  }

  /**
   * Test the deleteDirectory() method with a file.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDeleteDirectoryWithFile() throws IOException {
    Path path = Paths.get("src/test/resources/PathsUtilTest/deleteDirectory/");
    PathUtils.deleteDirectory(path.resolve("file1.json"));
  }

  /**
   * Test the deleteDirectory() method with a directory, that doesn't exist.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDeleteDirectoryWithInvalidDirectory() throws IOException {
    Path path = Paths.get("path/that/does/not/exist/");
    PathUtils.cleanDirectory(path);
  }

  /**
   * Test the deleteDirectory() method with a file and directory.
   */
  @Test
  public void testDeleteDirectory() throws IOException {
    Path path = Paths.get("src/test/resources/PathsUtilTest/deleteDirectory");
    // Assert, that the directory exists before deleting it.
    Assert.assertTrue(Files.exists(path));
    Assert.assertTrue(Files.isDirectory(path));

    PathUtils.deleteDirectory(path);
    // Assert, that the directory doesn't exist anymore after deleting it.
    Assert.assertFalse(Files.exists(path));
  }

  // ___________________________________________________________________________
  // Test the delete() method.

  /**
   * Test the delete() method without a path.
   */
  @Test(expected = NullPointerException.class)
  public void testDeleteWithNoPath() throws IOException {
    PathUtils.delete(null);
  }

  /**
   * Test the delete() method with an invalid file.
   */
  @Test
  public void testDeleteWithInvalidFile() throws IOException {
    Path path = Paths.get("path/that/does/not/exist/");
    PathUtils.delete(path); // should not throw an exception.
  }

  /**
   * Test the delete() method with a file.
   */
  @Test
  public void testDeleteWithFile() throws IOException {
    Path dir = Paths.get("src/test/resources/PathsUtilTest/deleteDirectory");
    Path file = dir.resolve("file1.json");

    // Assert, that the file exists before deleting it.
    Assert.assertTrue(Files.exists(file));
    Assert.assertTrue(Files.isRegularFile(file));

    PathUtils.delete(file);
    // Assert, that the file doesn't exist anymore after deleting it.
    Assert.assertFalse(Files.exists(file));
    // Assert, that the directory still exist.
    Assert.assertTrue(Files.exists(dir));
  }

  /**
   * Test the delete() method with a directory.
   */
  @Test
  public void testDeleteWithDirectory() throws IOException {
    Path dir = Paths.get("src/test/resources/PathsUtilTest/deleteDirectory");

    // Assert, that the directory exists before deleting it.
    Assert.assertTrue(Files.exists(dir));
    Assert.assertTrue(Files.isDirectory(dir));

    PathUtils.delete(dir);
    // Assert, that the directory doesn't exist anymore after deleting it.
    Assert.assertFalse(Files.exists(dir));
  }

  // ___________________________________________________________________________
  // Test the contentEquals() method.

  /**
   * Test the contentEquals() method with two null arguments.
   */
  @Test(expected = NullPointerException.class)
  public void testContentEqualsWithNull() throws IOException {
    PathUtils.contentEquals(null, null);
  }

  /**
   * Test the contentEquals() method with two invalid paths.
   */
  @Test
  public void testContentEqualsWithInvalidPaths() throws IOException {
    Path path1 = Paths.get("path/that/does/not/exist/");
    Path path2 = Paths.get("path/that/also/does/not/exist/");
    Assert.assertFalse(PathUtils.contentEquals(path1, path2));
  }

  /**
   * Test the contentEquals() method with a file and a directory.
   */
  @Test
  public void testContentEqualsWithFileAndDirectory() throws IOException {
    Path base = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path file = base.resolve("only-text.pdf");
    Path dir = base.resolve("subdir-1");
    Assert.assertFalse(PathUtils.contentEquals(file, dir));
  }

  /**
   * Test the contentEquals() method with two files.
   */
  @Test
  public void testContentEqualsWithTwoFiles() throws IOException {
    Path base1 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path base2 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-2");

    Path file1 = base1.resolve("only-text.pdf");
    Path file2 = base2.resolve("only-text.pdf");
    Path file3 = base2.resolve("text-with-figure.pdf");
    Assert.assertTrue(PathUtils.contentEquals(file1, file2));
    Assert.assertTrue(PathUtils.contentEquals(file2, file1));
    Assert.assertFalse(PathUtils.contentEquals(file1, file3));
    Assert.assertFalse(PathUtils.contentEquals(file3, file1));
    Assert.assertFalse(PathUtils.contentEquals(file2, file3));
    Assert.assertFalse(PathUtils.contentEquals(file3, file2));
  }

  /**
   * Test the contentEquals() method with two directories.
   */
  @Test
  public void testContentEqualsWithTwoDirs() throws IOException {
    Path dir1 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path dir2 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-2");
    Path dir3 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-3");

    Assert.assertTrue(PathUtils.contentEquals(dir1, dir2));
    Assert.assertTrue(PathUtils.contentEquals(dir2, dir1));
    Assert.assertFalse(PathUtils.contentEquals(dir1, dir3));
    Assert.assertFalse(PathUtils.contentEquals(dir3, dir1));
    Assert.assertFalse(PathUtils.contentEquals(dir2, dir3));
    Assert.assertFalse(PathUtils.contentEquals(dir3, dir2));
  }

  // ___________________________________________________________________________
  // Test the directoryContentEquals() method.

  /**
   * Test the directoryContentEquals() method with two null arguments.
   */
  @Test(expected = NullPointerException.class)
  public void testDirectoryContentEqualsWithNull() throws IOException {
    PathUtils.directoryContentEquals(null, null);
  }

  /**
   * Test the directoryContentEquals() method with two invalid paths.
   */
  @Test
  public void testDirectoryContentEqualsWithInvalidPaths()
    throws IOException {
    Path path1 = Paths.get("path/that/does/not/exist/");
    Path path2 = Paths.get("path/that/also/does/not/exist/");
    Assert.assertFalse(PathUtils.directoryContentEquals(path1, path2));
  }

  /**
   * Test the contentEquals() method with a file and a directory.
   */
  @Test
  public void testDirectoryContentEqualsWithFileAndDirectory()
    throws IOException {
    Path base = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path file = base.resolve("only-text.pdf");
    Path dir = base.resolve("subdir-1");
    Assert.assertFalse(PathUtils.directoryContentEquals(file, dir));
  }

  /**
   * Test the contentEquals() method with two files.
   */
  @Test
  public void testDirectoryContentEqualsWithTwoFiles() throws IOException {
    Path base1 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path base2 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-2");

    Path file1 = base1.resolve("only-text.pdf");
    Path file2 = base2.resolve("only-text.pdf");
    Assert.assertFalse(PathUtils.directoryContentEquals(file1, file2));
  }

  /**
   * Test the contentEquals() method with two directories.
   */
  @Test
  public void testDirectoryContentEqualsWithTwoDirs() throws IOException {
    Path dir1 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path dir2 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-2");
    Path dir3 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-3");

    Assert.assertTrue(PathUtils.directoryContentEquals(dir1, dir2));
    Assert.assertTrue(PathUtils.directoryContentEquals(dir2, dir1));
    Assert.assertFalse(PathUtils.directoryContentEquals(dir1, dir3));
    Assert.assertFalse(PathUtils.directoryContentEquals(dir3, dir1));
    Assert.assertFalse(PathUtils.directoryContentEquals(dir2, dir3));
    Assert.assertFalse(PathUtils.directoryContentEquals(dir3, dir2));
  }

  // ___________________________________________________________________________
  // Test the listPaths() method.

  /**
   * Test the listPaths() method with a null argument.
   */
  @Test
  public void testListPathsWithNull() throws IOException {
    Assert.assertNull(PathUtils.listPaths(null));
  }

  /**
   * Test the listPaths() method with an invalid path.
   */
  @Test
  public void testListPathsWithInvalidPath() throws IOException {
    Path path = Paths.get("path/that/does/not/exist/");
    List<Path> paths = PathUtils.listPaths(path);
    Assert.assertEquals(0, paths.size());
  }

  /**
   * Test the listPaths() method with a file.
   */
  @Test
  public void testListPathWithFile() throws IOException {
    Path dir = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path file = dir.resolve("only-text.pdf");
    List<Path> paths = PathUtils.listPaths(file);
    Assert.assertEquals(1, paths.size());
    Assert.assertEquals(file, paths.get(0));
  }

  /**
   * Test the contentEquals() method with directories.
   */
  @Test
  public void testListPathWithDirs() throws IOException {
    Path dir1 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-1");
    Path dir2 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-2");
    Path dir3 = Paths.get("src/test/resources/PathsUtilTest/contentEquals-3");

    List<Path> paths1 = PathUtils.listPaths(dir1);
    List<Path> paths2 = PathUtils.listPaths(dir2);
    List<Path> paths3 = PathUtils.listPaths(dir3);

    Assert.assertEquals(paths1.size(), paths2.size());
    Assert.assertNotEquals(paths1.size(), paths3.size());
    Assert.assertNotEquals(paths2.size(), paths3.size());

    Assert.assertEquals(8, paths1.size());
    Assert.assertEquals(8, paths2.size());
    Assert.assertEquals(7, paths3.size());

    Assert.assertEquals(0, PathUtils.listPaths(dir1, "xxx").size());
    Assert.assertEquals(6, PathUtils.listPaths(dir1, "pdf").size());
    Assert.assertEquals(7, PathUtils.listPaths(dir1, "pdf", "txt").size());
    Assert.assertEquals(8, PathUtils.listPaths(dir1, "").size());
  }
}
