package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;
import blater.nql.cli.CacheNameSelection;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** Coordinates the focused filesystem components behind the cache store. */
final class CacheFileRepository {
  private final CacheFileAllocator allocator = new CacheFileAllocator();
  private final CacheFileSelector selector = new CacheFileSelector();
  private final CacheFileCleaner cleaner = new CacheFileCleaner(selector);
  private final CacheFileLoadCoordinator loadCoordinator =
      new CacheFileLoadCoordinator(allocator, selector, cleaner);
  private final CacheAgeParser ageParser = new CacheAgeParser();

  CacheFileRepository() {
  }

  CachePreparation prepare(Path cacheDirectory, CacheNameSelection nameSelection) {
    return allocator.prepare(cacheDirectory, nameSelection);
  }

  CacheHandle loadAndActivate(
      Path cacheDirectory,
      CacheNameSelection nameSelection,
      PersistentCache.CacheLoader loader) {
    return loadCoordinator.loadAndActivate(cacheDirectory, nameSelection, loader);
  }

  void activate(CacheHandle handle, Path cacheDirectory) {
    selector.activate(handle, cacheDirectory);
  }

  CacheHandle use(CacheName name, Path cacheDirectory) {
    return selector.use(name, cacheDirectory);
  }

  CacheHandle select(CacheName name, Path cacheDirectory) {
    return selector.select(name, cacheDirectory);
  }

  CacheLookup active(Path cacheDirectory) {
    return selector.active(cacheDirectory);
  }

  List<LogicalCacheEntry> listCaches(Path cacheDirectory) {
    return selector.listCaches(cacheDirectory);
  }

  int clearNamed(CacheName name, Path cacheDirectory) {
    return cleaner.clearNamed(name, cacheDirectory);
  }

  int clearAll(Path cacheDirectory) {
    return cleaner.clearAll(cacheDirectory);
  }

  int clearOlderThan(Duration duration, Path cacheDirectory) {
    return cleaner.clearOlderThan(duration, cacheDirectory);
  }

  Duration parseDuration(String value) {
    return ageParser.parse(value);
  }
}
