package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;
import blater.nql.cli.CacheNameSelection;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** Coordinates the persistent-cache API with the filesystem repository. */
final class CacheFileStore {
  private final CacheFileRepository repository = new CacheFileRepository();

  CachePreparation prepare(Path cacheDirectory, CacheNameSelection nameSelection) {
    return repository.prepare(cacheDirectory, nameSelection);
  }

  CacheHandle loadAndActivate(
      Path cacheDirectory,
      CacheNameSelection nameSelection,
      PersistentCache.CacheLoader loader) {
    return repository.loadAndActivate(cacheDirectory, nameSelection, loader);
  }

  void activate(CacheHandle handle, Path cacheDirectory) {
    repository.activate(handle, cacheDirectory);
  }

  CacheHandle use(CacheName name, Path cacheDirectory) {
    return repository.use(name, cacheDirectory);
  }

  CacheHandle select(CacheName name, Path cacheDirectory) {
    return repository.select(name, cacheDirectory);
  }

  CacheLookup active(Path cacheDirectory) {
    return repository.active(cacheDirectory);
  }

  List<LogicalCacheEntry> listCaches(Path cacheDirectory) {
    return repository.listCaches(cacheDirectory);
  }

  int clearNamed(CacheName name, Path cacheDirectory) {
    return repository.clearNamed(name, cacheDirectory);
  }

  int clearAll(Path cacheDirectory) {
    return repository.clearAll(cacheDirectory);
  }

  int clearOlderThan(Duration duration, Path cacheDirectory) {
    return repository.clearOlderThan(duration, cacheDirectory);
  }

  Duration parseDuration(String value) {
    return repository.parseDuration(value);
  }
}
