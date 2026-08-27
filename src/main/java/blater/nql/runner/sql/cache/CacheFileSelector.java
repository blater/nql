package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Selects, activates, and lists logical caches. */
final class CacheFileSelector {
  private final CacheActiveSelection activeSelection = new CacheActiveSelection();

  void activate(CacheHandle handle, Path cacheDirectory) {
    Objects.requireNonNull(handle, "handle");
    Path root = CacheFileLayout.root(cacheDirectory);
    CacheName name = CacheFileLayout.logicalName(root, handle.cacheFile());
    if (!CacheFileLayout.isCacheFile(handle.cacheFile())) {
      throw new IllegalArgumentException("No existing cache found for [" + name.value() + "]");
    }
    activeSelection.write(root, name);
  }

  CacheHandle use(CacheName name, Path cacheDirectory) {
    CacheHandle handle = select(name, cacheDirectory);
    activate(handle, cacheDirectory);
    return handle;
  }

  CacheHandle select(CacheName name, Path cacheDirectory) {
    Objects.requireNonNull(name, "name");
    Path cacheFile = CacheFileLayout.cacheFile(CacheFileLayout.root(cacheDirectory), name);
    if (!CacheFileLayout.isCacheFile(cacheFile)) {
      throw new IllegalArgumentException("No existing cache found for [" + name.value() + "]");
    }
    return new CacheHandle(cacheFile, CacheFileLayout.jdbcUrl(cacheFile), false);
  }

  CacheLookup active(Path cacheDirectory) {
    return activeSelection.read(CacheFileLayout.root(cacheDirectory));
  }

  List<LogicalCacheEntry> listCaches(Path cacheDirectory) {
    Path root = CacheFileLayout.root(cacheDirectory);
    if (!Files.isDirectory(root)) {
      return List.of();
    }
    CacheLookup active = activeSelection.read(root);
    List<LogicalCacheEntry> entries = new ArrayList<>();
    try (Stream<Path> paths = Files.list(root)) {
      paths.filter(CacheFileLayout::isCacheFile)
          .sorted()
          .forEach(cacheFile -> addEntry(entries, cacheFile, active));
      return List.copyOf(entries);
    } catch (IOException failure) {
      throw new IllegalStateException("Could not list cache files: " + root, failure);
    }
  }

  CacheActiveSelection activeSelection() {
    return activeSelection;
  }

  private static void addEntry(
      List<LogicalCacheEntry> entries,
      Path cacheFile,
      CacheLookup active) {
    CacheName name = CacheFileLayout.nameFromFile(cacheFile);
    if (name != null) {
      boolean selected = active instanceof CacheLookup.Found found
          && found.name().equals(name);
      entries.add(new LogicalCacheEntry(
          name,
          CacheFileLayout.modifiedMillis(cacheFile),
          selected));
    }
  }
}
