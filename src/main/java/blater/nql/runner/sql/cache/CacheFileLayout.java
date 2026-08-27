package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Defines the on-disk names and H2 connection details for typed caches. */
final class CacheFileLayout {
  static final String DATABASE_SUFFIX = ".mv.db";

  private static final List<String> ARTIFACT_SUFFIXES = List.of(
      ".mv.db", ".trace.db", ".lock.db", ".temp.db", ".newFile", ".tempFile");

  private CacheFileLayout() {
  }

  static Path root(Path cacheDirectory) {
    return Objects.requireNonNull(cacheDirectory, "cacheDirectory")
        .toAbsolutePath()
        .normalize();
  }

  static Path cacheFile(Path root, CacheName name) {
    return root.resolve(name.value() + DATABASE_SUFFIX);
  }

  static CacheName logicalName(Path root, Path cacheFile) {
    Path normalizedFile = cacheFile.toAbsolutePath().normalize();
    if (!root.equals(normalizedFile.getParent())) {
      throw new IllegalArgumentException(
          "Cache is outside the selected cache directory: " + cacheFile);
    }
    CacheName name = nameFromFile(normalizedFile);
    if (name == null) {
      throw new IllegalArgumentException("Invalid cache filename: " + cacheFile);
    }
    return name;
  }

  static CacheName nameFromFile(Path cacheFile) {
    String filename = cacheFile.getFileName().toString();
    if (!filename.endsWith(DATABASE_SUFFIX)) {
      return null;
    }
    try {
      return new CacheName(filename.substring(0, filename.length() - DATABASE_SUFFIX.length()));
    } catch (IllegalArgumentException failure) {
      return null;
    }
  }

  static boolean isCacheFile(Path cacheFile) {
    return Files.isRegularFile(cacheFile) && nameFromFile(cacheFile) != null;
  }

  static boolean hasArtifacts(Path cacheFile) {
    String base = databasePath(cacheFile);
    return ARTIFACT_SUFFIXES.stream()
        .map(suffix -> Path.of(base + suffix))
        .anyMatch(Files::exists);
  }

  static List<Path> artifactPaths(Path cacheFile) {
    String base = databasePath(cacheFile);
    return ARTIFACT_SUFFIXES.stream()
        .map(suffix -> Path.of(base + suffix))
        .toList();
  }

  static long modifiedMillis(Path cacheFile) {
    try {
      return Files.getLastModifiedTime(cacheFile).toMillis();
    } catch (IOException failure) {
      throw new IllegalStateException("Could not read cache timestamp: " + cacheFile, failure);
    }
  }

  static String jdbcUrl(Path cacheFile) {
    return "jdbc:h2:file:" + databasePath(cacheFile)
        + ";MODE=MySQL;NON_KEYWORDS=VALUE";
  }

  static String databasePath(Path cacheFile) {
    String path = cacheFile.toAbsolutePath().normalize().toString();
    if (!path.endsWith(DATABASE_SUFFIX)) {
      throw new IllegalArgumentException("Invalid H2 cache filename: " + cacheFile);
    }
    return path.substring(0, path.length() - DATABASE_SUFFIX.length());
  }

  static void createDirectories(Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (IOException failure) {
      throw new IllegalStateException("Could not create cache directory: " + directory, failure);
    }
  }
}
