package blater.nql.domain;

import blater.nql.runner.sql.domain.QueryResultRow;

/** Compatibility facade for hierarchy path traversal and row-local identity. */
class HierarchyPathTraversal {
  private final HierarchyPathResolverEngine resolver = new HierarchyPathResolverEngine();

  HierarchyKeyIndex keyIndex() {
    return resolver.keyIndex();
  }

  Node resolveParent(
      Node root,
      Hierarchy.RootKind rootKind,
      MappingPlan plan,
      HierarchyPath path,
      QueryResultRow row,
      RowContext rowContext) {
    return resolver.resolveParent(root, rootKind, plan, path, row, rowContext);
  }

  KeyedPath keyedPath(MappingPlan plan, HierarchyPath path) {
    return resolver.keyedPath(plan, path);
  }

  KeyedPath repeatedPath(MappingPlan plan, HierarchyPath path) {
    return resolver.repeatedPath(plan, path);
  }

  boolean flatRows(MappingPlan plan) {
    return resolver.flatRows(plan);
  }

  void initializeInferredCollections(
      Node root, Hierarchy.RootKind rootKind, MappingPlan plan) {
    resolver.initializeInferredCollections(root, rootKind, plan);
  }

  static class RowContext extends HierarchyRowContext {
  }
}
