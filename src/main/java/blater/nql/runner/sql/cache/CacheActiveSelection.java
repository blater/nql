package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Reads and atomically publishes the selected cache name. */
final class CacheActiveSelection {
  private static final String ACTIVE_FILE = ".active";

  CacheLookup read(Path root) {
    Path activeFile = root.resolve(ACTIVE_FILE);
    if (!Files.isRegularFile(activeFile)) {
      return new CacheLookup.None();
    }
    try {
      CacheName name = new CacheName(Files.readString(activeFile, StandardCharsets.UTF_8).trim());
      Path cacheFile = CacheFileLayout.cacheFile(root, name);
      if (!CacheFileLayout.isCacheFile(cacheFile)) {
        clear(root);
        return new CacheLookup.None();
      }
      return new CacheLookup.Found(name, currentHandle(cacheFile));
    } catch (IOException | IllegalArgumentException failure) {
      clear(root);
      return new CacheLookup.None();
    }
  }

  void write(Path root, CacheName name) {
    CacheFileLayout.createDirectories(root);
    Path activeFile = root.resolve(ACTIVE_FILE);
    Path temporary = null;
    try {
      temporary = Files.createTempFile(root, ".active-", ".tmp");
      Files.writeString(
          temporary,
          name.value() + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.TRUNCATE_EXISTING);
      move(temporary, activeFile);
      temporary = null;
    } catch (IOException failure) {
      throw new IllegalStateException("Could not update active cache: " + activeFile, failure);
    } finally {
      deleteTemporary(temporary);
    }
  }

  void clearIfNamed(Path root, CacheName name) {
    CacheLookup selected = read(root);
    if (selected instanceof CacheLookup.Found found && found.name().equals(name)) {
      clear(root);
    }
  }

  void clear(Path root) {
    try {
      Files.deleteIfExists(root.resolve(ACTIVE_FILE));
    } catch (IOException failure) {
      throw new IllegalStateException("Could not clear active cache selection: " + root, failure);
    }
  }

  private static CacheHandle currentHandle(Path cacheFile) {
    return new CacheHandle(cacheFile, CacheFileLayout.jdbcUrl(cacheFile), false);
  }

  private static void move(Path temporary, Path activeFile) throws IOException {
    try {
      Files.move(
          temporary,
          activeFile,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException unsupported) {
      try {
        Files.move(temporary, activeFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException fallbackFailure) {
        fallbackFailure.addSuppressed(unsupported);
        throw fallbackFailure;
      }
    }
  }

  private static void deleteTemporary(Path temporary) {
    if (temporary == null) {
      return;
    }
    try {
      Files.deleteIfExists(temporary);
    } catch (IOException ignored) {
      // The active-selection failure remains the primary error.
    }
  }
}
