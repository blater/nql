package blater.nql.domain;

/** Answers structural questions about a mapping plan. */
final class HierarchyPathPlan {
  private HierarchyPathPlan() {
  }

  static KeyedPath keyedPath(MappingPlan plan, HierarchyPath path) {
    return plan.getKeyedPaths().stream()
        .filter(key -> key.path().equals(path))
        .findFirst()
        .orElse(null);
  }

  static KeyedPath repeatedPath(MappingPlan plan, HierarchyPath path) {
    return plan.getKeyedPaths().stream()
        .filter(key -> repeatsAt(key, path))
        .findFirst()
        .orElse(null);
  }

  static boolean flatRows(MappingPlan plan) {
    return !plan.getFields().isEmpty()
        && plan.getFields().stream().allMatch(
            field -> field.getPath().getPathParts().size() == 1);
  }

  static boolean isInferredOwner(MappingPlan plan, HierarchyPath path) {
    KeyedPath key = keyedPath(plan, path);
    return key != null && key.placement() instanceof RepetitionPlacement.AnonymousItem;
  }

  static boolean isObjectPath(MappingPlan plan, HierarchyPath path) {
    return !path.isRoot() && (keyedPath(plan, path) != null
        || plan.getFields().stream().anyMatch(field -> path.equals(field.getPath().parent())));
  }

  private static boolean repeatsAt(KeyedPath key, HierarchyPath path) {
    if (key.placement() instanceof RepetitionPlacement.NamedItem) {
      return path.equals(key.identityPath());
    }
    return key.placement() instanceof RepetitionPlacement.AnonymousItem anonymous
        && anonymous.containerPath().filter(path::equals).isPresent();
  }
}
