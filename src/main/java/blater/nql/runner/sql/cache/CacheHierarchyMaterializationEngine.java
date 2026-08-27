package blater.nql.runner.sql.cache;

import blater.nql.domain.Hierarchy;
import blater.nql.runner.sql.SqlExecutor;

/** Coordinates hierarchy analysis and SQL row emission for a cache load. */
final class CacheHierarchyMaterializationEngine {
  private final SqlExecutor sqlExecutor;

  CacheHierarchyMaterializationEngine(SqlExecutor sqlExecutor) {
    this.sqlExecutor = sqlExecutor;
  }

  public void load(Hierarchy hierarchy) {
    if (hierarchy == null || hierarchy.getRoot() == null) {
      return;
    }
    CacheHierarchyAnalysis analysis = new CacheHierarchyAnalysis();
    analysis.analyze(hierarchy);
    new CacheHierarchyRowWriter(sqlExecutor, analysis).write(hierarchy.getRoot());
  }
}
