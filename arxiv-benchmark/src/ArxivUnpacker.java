import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import de.freiburg.iif.paths.PathsUtil;

/**
 * Class to unpack the archive files of arXiv properly.
 *
 * @author Claudius Korzen
 */
public class ArxivUnpacker {
  /** 
   * The base directory.
   */
  protected static Path BASE_DIR = Paths.get("/nfs/raid1/arxiv");
  
  /** 
   * The tmp directory, needed to store intermediate archives. 
   */
  protected static Path TMP_DIR = BASE_DIR.resolve("tmp");
    
  /**
   * Filter that accepts all src chunks ala "arXiv_src_0001_001.tar"
   */
  protected static Filter<Path> SRC_CHUNKS_FILTER = new Filter<Path>() {
    @Override
    public boolean accept(Path entry) throws IOException {
      String filename = entry.getFileName().toString();
      return filename.startsWith("arXiv_src") && filename.endsWith(".tar");
    }
  };
  
  // ___________________________________________________________________________
  
  /**
   * Starts the unpacking of the archive files.
   */
  public static void main(String[] args) throws Exception {
    // Unpack the src chunks.
    List<Path> unpackedSrcArchives = unpackSrcChunks(TMP_DIR);
    // Unpack the archives.
    unpackSrcArchives(unpackedSrcArchives);
        
    // Move the files from tmp to base.
    PathsUtil.move(TMP_DIR, BASE_DIR);
  }
    
  /**
   * Unpacks the src chunks, as we have downloaded them from Amazon S3. The
   * filenames follow the pattern "arXiv_src_0001_001.tar". Returns all unpacked
   * files.
   */
  protected static List<Path> unpackSrcChunks(Path target) throws Exception {
    List<Path> unpackedFiles = new ArrayList<>();
    // Iterate over all src chunks in the base directory.
    try (DirectoryStream<Path> dir = 
        Files.newDirectoryStream(BASE_DIR, SRC_CHUNKS_FILTER)) {
      Iterator<Path> dirItr = dir.iterator();
      while (dirItr.hasNext()) {
        // Untar the chunk without deleting it.
        List<Path> files = untar(dirItr.next(), target, false);
        unpackedFiles.addAll(files);
      }
    }
    return unpackedFiles;
  }
  
  /**
   * Unpacks the gz archive files, which results from the previous step  
   * (unpacking the src chunks).
   */
  protected static void unpackSrcArchives(List<Path> inputFiles) 
      throws Exception {    
    for (Path inputFile : inputFiles) {
      String filename = inputFile.getFileName().toString();
      // Obtain the basename and the extension of the filename.
      String[] tokens = filename.split("\\.(?=[^\\.]+$)");
      String basename = tokens[0];
      String extension = tokens[1];
      
      // For a archive file /foo/bar/archive.gz, unpack it to /foo/bar/archive.
      Path outputDirectory = inputFile.resolveSibling(basename);
      
      switch (extension) {
        // Beware: In rare cases, the "archive" is a pdf file, which can be 
        // simply moved to the output directory.  
        case "pdf":
          PathsUtil.move(inputFile, outputDirectory);
          break;
        // If the archive is a gz archive, it can be either a multi file 
        // archive (in fact a tar.gz archive), or a single file archive.
        // Because we cannot distinguish both types from filenames, we try to 
        // untar the archive. This will fail for single file archive. 
        // So, in case of an error on untaring the archive, we assume, that the
        // archive is a single file archive.
        case "gz":
          try {
            // Try to untar the input. This will only succeed, if the archive is
            // a multiple file archive.
            gunzipAndUntar(inputFile, outputDirectory, true);
          } catch (Exception e) {
            // Untaring has failed. Assume, that the archive is a single file 
            // archive.
            processSingleFileSrcArchive(inputFile);
          }
          break;
        default:
          break;
      }
    }
  }
    
  /**
   * Processes a single file archive. Unzips the archive and tries to identify
   * the mimetype of the unpacked file (because we don't get the mimetype of 
   * the file explicitly).
   */
  protected static void processSingleFileSrcArchive(Path file) 
      throws Exception {   
    // Obtain the basename from the filename.
    String filename = file.getFileName().toString();
    String[] tokens = filename.split("\\.(?=[^\\.]+$)");
    String basename = tokens[0];
    
    // Unzip the archive and delete it afterwards.
    Path outputDir = file.getParent().resolve(basename);
    Path outputFile = outputDir.resolve(basename);
    gunzip(file, outputFile, true);
    
    // Try to identify the mimetype of the file by searching for characteristic
    // keywords in the content of the file.
    byte[] bytes = Files.readAllBytes(outputFile);
    String content = new String(bytes).replaceAll("\\s", "").toLowerCase();
    
    // There is a difference between tex and latex. Because pure tex doesn't
    // have very specific keywords, we rely on the keyword "\end" or "\bye",
    // which should be contained by all tex and latex files, 
    // e.g. "\end{document}"
    if (content.contains("\\end") || content.contains("\\bye")) {
      PathsUtil.rename(outputFile, basename + ".tex");
    } else if (content.contains("<html>")) {
      PathsUtil.rename(outputFile, basename + ".html");
    } else if (content.contains("%!ps-adobe-3.0 epsf")) {
      PathsUtil.rename(outputFile, basename + ".eps");
    } else if (content.contains("%!ps-adobe-3.0")) {
      PathsUtil.rename(outputFile, basename + ".ps");
    // If a file contains the flag "%auto-ignore", the paper was retired by 
    // their authors. 
    } else if (content.contains("%auto-ignore")) {
      PathsUtil.rename(outputFile, basename + ".txt");
    }
  }
  
  // ___________________________________________________________________________
  
  /**
   * Unpacks the given tar archive into the parent directory of the input
   * file. If the flag deleteArchive is true, the archive will be deleted 
   * afterwards.
   */
  protected static List<Path> untar(Path archive, boolean deleteArchive) 
      throws Exception {
    return untar(archive, archive.getParent(), deleteArchive);
  }
  
  /**
   * Unpacks the given tar archive into the given output directory.
   * If the flag deleteArchive is true, the archive will be deleted afterwards.
   */
  protected static List<Path> untar(Path archive, Path outputDir, 
      boolean deleteInput) throws Exception {    
    try (InputStream is = Files.newInputStream(archive)) {
      TarArchiveInputStream tar = new TarArchiveInputStream(is);
      List<Path> unpackedFiles = unpack(tar, outputDir);
      tar.close();
      if (deleteInput) {
        Files.delete(archive);
      }
    }
    return unpackedFiles; 
  }

  /**
   * Unpacks the given tar.gz archive into the parent directory of the input 
   * file. If the flag deleteArchive is true, the archive will be deleted 
   * afterwards.
   */
  protected static List<Path> gunzipAndUntar(Path archive, 
      boolean deleteArchive) throws Exception {
    return gunzipAndUntar(archive, archive.getParent(), deleteArchive);
  }
  
  /**
   * Unpacks the given tar.gz archive into the given output directory.
   * If the flag deleteArchive is true, the archive will be deleted afterwards.
   */
  protected static List<Path> gunzipAndUntar(Path archive, Path outputDir, 
      boolean deleteInput) throws Exception {
    try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(archive)) {
      TarArchiveInputStream tar = new TarArchiveInputStream(gzip);
      List<Path> unpackedFiles = unpack(tar, outputDir);
      tar.close();
      if (deleteInput) {
        Files.delete(archive);
      }
    }
    return unpackedFiles;
  }
  
  /**
   * Unpacks the given gz archive into the given output directory.
   * If the flag deleteArchive is true, the archive will be deleted afterwards.
   */
  protected static void gunzip(Path archive, Path outputFile, 
      boolean deleteArchive) throws Exception {
    try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(archive))) {
      // Make sure, that the output file exists.
      if (!Files.exists(outputFile)) {
        Files.createDirectories(outputFile.getParent());
        Files.createFile(outputFile);
      }
      try (OutputStream out = Files.newOutputStream(outputFile)) {
        IOUtils.copy(gzip, out);
      }
      if (deleteArchive) {
        Files.delete(archive);
      }
    }
  }
  
  /**
   * Untar an input file into an output file.
   */
  protected static List<Path> unpack(ArchiveInputStream is, Path outputDir)
    throws Exception {
    List<Path> unpackedFiles = new ArrayList<>();
    
    // Make sure, the output directory exists.
    Files.createDirectories(outputDir);
    ArchiveEntry entry = null;
    while ((entry = is.getNextEntry()) != null) {
      Path outputFile = outputDir.resolve(entry.getName());      
      if (entry.isDirectory()) {
        Files.createDirectories(outputFile);
      } else {
        // Create the file, if it doesn't exist.
        if (!Files.exists(outputFile)) {
          Files.createDirectories(outputFile.getParent());
          Files.createFile(outputFile);
        }
        
        // Write the content to file.
        try (OutputStream outputFileStream = Files.newOutputStream(outputFile)) {
          IOUtils.copy(is, outputFileStream);
        }
        unpackedFiles.add(outputFile);
      }
    }
    return unpackedFiles;
  }
}
