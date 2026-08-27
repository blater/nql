package blater.nql.runner.sql.cache;

import blater.nql.cli.CacheNameSelection;

import java.nio.file.Path;
import java.util.Objects;

/** Completes a claimed cache load before publishing it as the active cache. */
final class CacheFileLoadCoordinator {
  private final CacheFileAllocator allocator;
  private final CacheFileSelector selector;
  private final CacheFileCleaner cleaner;

  CacheFileLoadCoordinator(
      CacheFileAllocator allocator,
      CacheFileSelector selector,
      CacheFileCleaner cleaner) {
    this.allocator = Objects.requireNonNull(allocator, "allocator");
    this.selector = Objects.requireNonNull(selector, "selector");
    this.cleaner = Objects.requireNonNull(cleaner, "cleaner");
  }

  CacheHandle loadAndActivate(
      Path cacheDirectory,
      CacheNameSelection nameSelection,
      PersistentCache.CacheLoader loader) {
    Objects.requireNonNull(loader, "loader");
    CachePreparation preparation = allocator.prepare(cacheDirectory, nameSelection);
    CacheHandle handle = preparation.handle();
    try {
      loader.load(handle);
      requireDatabase(handle);
      preparation.close();
      selector.activate(handle, cacheDirectory);
      return handle;
    } catch (RuntimeException | Error failure) {
      cleanupAfterFailure(handle, preparation, failure);
      throw failure;
    }
  }

  private void requireDatabase(CacheHandle handle) {
    if (!CacheFileLayout.isCacheFile(handle.cacheFile())) {
      throw new IllegalStateException(
          "Cache loader completed without creating " + handle.cacheFile());
    }
  }

  private void cleanupAfterFailure(
      CacheHandle handle,
      CachePreparation preparation,
      Throwable failure) {
    try {
      cleaner.deleteCache(handle.cacheFile());
    } catch (RuntimeException cleanupFailure) {
      failure.addSuppressed(cleanupFailure);
    }
    try {
      preparation.close();
    } catch (RuntimeException releaseFailure) {
      failure.addSuppressed(releaseFailure);
    }
  }
}
