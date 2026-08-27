package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheName;
import blater.nql.cli.CacheNameSelection;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** Public facade for persistent cache storage and lifecycle operations. */
public final class PersistentCache {
  private static final CacheStorageService SERVICE = CacheStorage.service();

  private PersistentCache() {
  }

  public static CachePreparation prepare(Path cacheDirectory, CacheNameSelection nameSelection) {
    return SERVICE.prepare(cacheDirectory, nameSelection);
  }

  public static CacheHandle loadAndActivate(
      Path cacheDirectory, CacheNameSelection nameSelection, CacheLoader loader) {
    return SERVICE.loadAndActivate(cacheDirectory, nameSelection, loader);
  }

  public static void activate(CacheHandle handle, Path cacheDirectory) {
    SERVICE.activate(handle, cacheDirectory);
  }

  public static CacheHandle use(CacheName name, Path cacheDirectory) {
    return SERVICE.use(name, cacheDirectory);
  }

  public static CacheHandle select(CacheName name, Path cacheDirectory) {
    return SERVICE.select(name, cacheDirectory);
  }

  public static CacheLookup active(Path cacheDirectory) {
    return SERVICE.active(cacheDirectory);
  }

  public static List<LogicalCacheEntry> listCaches(Path cacheDirectory) {
    return SERVICE.listCaches(cacheDirectory);
  }

  public static int clearNamed(CacheName name, Path cacheDirectory) {
    return SERVICE.clearNamed(name, cacheDirectory);
  }

  public static int clearAll(Path cacheDirectory) {
    return SERVICE.clearAll(cacheDirectory);
  }

  public static int clearOlderThan(Duration duration, Path cacheDirectory) {
    return SERVICE.clearOlderThan(duration, cacheDirectory);
  }

  public static Duration parseDuration(String value) {
    return SERVICE.parseDuration(value);
  }

  @FunctionalInterface
  public interface CacheLoader {
    void load(CacheHandle handle);
  }
}
