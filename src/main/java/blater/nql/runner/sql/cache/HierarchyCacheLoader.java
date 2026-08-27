package blater.nql.runner.sql.cache;

import blater.nql.domain.Hierarchy;
import blater.nql.runner.sql.SqlExecutor;

/** Public entry point for materializing a hierarchy into SQL cache tables. */
public final class HierarchyCacheLoader {
  private final CacheHierarchyTableWriter delegate;

  public HierarchyCacheLoader(SqlExecutor sqlExecutor) {
    delegate = new CacheHierarchyTableWriter(sqlExecutor);
  }

  public void load(Hierarchy hierarchy) {
    delegate.load(hierarchy);
  }
}
