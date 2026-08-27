package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;
import blater.nql.cli.CacheNameSelection;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** Service facade for persistent cache lifecycle operations. */
final class CacheStorageService {
  private final CacheFileStore fileStore = new CacheFileStore();

  CachePreparation prepare(Path cacheDirectory, CacheNameSelection nameSelection) {
    return fileStore.prepare(cacheDirectory, nameSelection);
  }

  CacheHandle loadAndActivate(
      Path cacheDirectory,
      CacheNameSelection nameSelection,
      PersistentCache.CacheLoader loader) {
    return fileStore.loadAndActivate(cacheDirectory, nameSelection, loader);
  }

  void activate(CacheHandle handle, Path cacheDirectory) {
    fileStore.activate(handle, cacheDirectory);
  }

  CacheHandle use(CacheName name, Path cacheDirectory) {
    return fileStore.use(name, cacheDirectory);
  }

  CacheHandle select(CacheName name, Path cacheDirectory) {
    return fileStore.select(name, cacheDirectory);
  }

  CacheLookup active(Path cacheDirectory) {
    return fileStore.active(cacheDirectory);
  }

  List<LogicalCacheEntry> listCaches(Path cacheDirectory) {
    return fileStore.listCaches(cacheDirectory);
  }

  int clearNamed(CacheName name, Path cacheDirectory) {
    return fileStore.clearNamed(name, cacheDirectory);
  }

  int clearAll(Path cacheDirectory) {
    return fileStore.clearAll(cacheDirectory);
  }

  int clearOlderThan(Duration duration, Path cacheDirectory) {
    return fileStore.clearOlderThan(duration, cacheDirectory);
  }

  Duration parseDuration(String value) {
    return fileStore.parseDuration(value);
  }
}
