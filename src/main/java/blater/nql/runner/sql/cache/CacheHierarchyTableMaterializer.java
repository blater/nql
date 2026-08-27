package blater.nql.runner.sql.cache;

import blater.nql.domain.Hierarchy;
import blater.nql.runner.sql.SqlExecutor;

/** Compatibility boundary for hierarchy-to-cache table materialization. */
final class CacheHierarchyTableMaterializer {
  private final CacheHierarchyMaterializationEngine engine;

  CacheHierarchyTableMaterializer(SqlExecutor sqlExecutor) {
    engine = new CacheHierarchyMaterializationEngine(sqlExecutor);
  }

  public void load(Hierarchy hierarchy) {
    engine.load(hierarchy);
  }
}
