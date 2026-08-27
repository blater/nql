package blater.nql.runner.sql.cache;

import blater.nql.domain.Hierarchy;
import blater.nql.runner.sql.SqlExecutor;

/** Stable table-writing boundary for hierarchy cache materialization. */
final class CacheHierarchyTableWriter {
  private final CacheHierarchyTableMaterializer materializer;

  CacheHierarchyTableWriter(SqlExecutor sqlExecutor) {
    materializer = new CacheHierarchyTableMaterializer(sqlExecutor);
  }

  void load(Hierarchy hierarchy) {
    materializer.load(hierarchy);
  }
}
