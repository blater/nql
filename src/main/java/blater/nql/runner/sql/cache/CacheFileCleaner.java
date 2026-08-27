package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Removes cache database artifacts and stale active selections. */
final class CacheFileCleaner {
  private final CacheFileSelector selector;
  private final CacheActiveSelection activeSelection;

  CacheFileCleaner(CacheFileSelector selector) {
    this.selector = Objects.requireNonNull(selector, "selector");
    activeSelection = selector.activeSelection();
  }

  int clearNamed(CacheName name, Path cacheDirectory) {
    Objects.requireNonNull(name, "name");
    Path root = CacheFileLayout.root(cacheDirectory);
    Path cacheFile = CacheFileLayout.cacheFile(root, name);
    boolean existed = CacheFileLayout.isCacheFile(cacheFile);
    deleteCache(cacheFile);
    activeSelection.clearIfNamed(root, name);
    return existed ? 1 : 0;
  }

  int clearAll(Path cacheDirectory) {
    Path root = CacheFileLayout.root(cacheDirectory);
    List<LogicalCacheEntry> entries = selector.listCaches(root);
    for (LogicalCacheEntry entry : entries) {
      deleteCache(CacheFileLayout.cacheFile(root, entry.name()));
    }
    activeSelection.clear(root);
    return entries.size();
  }

  int clearOlderThan(Duration duration, Path cacheDirectory) {
    Objects.requireNonNull(duration, "duration");
    if (duration.isNegative()) {
      throw new IllegalArgumentException("Cache age cannot be negative");
    }
    Path root = CacheFileLayout.root(cacheDirectory);
    long cutoffMillis = Instant.now().minus(duration).toEpochMilli();
    int cleared = 0;
    for (LogicalCacheEntry entry : selector.listCaches(root)) {
      if (entry.modifiedMillis() < cutoffMillis) {
        deleteCache(CacheFileLayout.cacheFile(root, entry.name()));
        activeSelection.clearIfNamed(root, entry.name());
        cleared++;
      }
    }
    return cleared;
  }

  void deleteCache(Path cacheFile) {
    RuntimeException firstFailure = null;
    for (Path artifact : CacheFileLayout.artifactPaths(cacheFile)) {
      try {
        Files.deleteIfExists(artifact);
      } catch (IOException failure) {
        IllegalStateException wrapped = new IllegalStateException(
            "Could not delete cache path: " + artifact, failure);
        if (firstFailure == null) {
          firstFailure = wrapped;
        } else {
          firstFailure.addSuppressed(wrapped);
        }
      }
    }
    if (firstFailure != null) {
      throw firstFailure;
    }
  }
}
