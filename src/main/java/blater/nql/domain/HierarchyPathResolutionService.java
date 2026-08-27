package blater.nql.domain;

import blater.nql.runner.sql.domain.QueryResultRow;

/** Compatibility facade for hierarchy path resolution and row-local identity. */
class HierarchyPathResolutionService {
  private final HierarchyPathTraversal traversal = new HierarchyPathTraversal();

  HierarchyKeyIndex keyIndex() {
    return traversal.keyIndex();
  }

  Node resolveParent(
      Node root,
      Hierarchy.RootKind rootKind,
      MappingPlan plan,
      HierarchyPath path,
      QueryResultRow row,
      RowContext rowContext) {
    return traversal.resolveParent(root, rootKind, plan, path, row, rowContext);
  }

  KeyedPath keyedPath(MappingPlan plan, HierarchyPath path) {
    return traversal.keyedPath(plan, path);
  }

  KeyedPath repeatedPath(MappingPlan plan, HierarchyPath path) {
    return traversal.repeatedPath(plan, path);
  }

  boolean flatRows(MappingPlan plan) {
    return traversal.flatRows(plan);
  }

  void initializeInferredCollections(
      Node root, Hierarchy.RootKind rootKind, MappingPlan plan) {
    traversal.initializeInferredCollections(root, rootKind, plan);
  }

  static class RowContext extends HierarchyPathTraversal.RowContext {
  }
}
