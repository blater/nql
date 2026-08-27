package blater.nql.runner.sql.cache;

/** Composition point for the persistent cache storage service. */
final class CacheStorage {
  private static final CacheStorageService SERVICE = new CacheStorageService();

  private CacheStorage() {
  }

  static CacheStorageService service() {
    return SERVICE;
  }
}
