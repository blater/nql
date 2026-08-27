package blater.nql.runner.sql.cache;

import blater.nql.domain.Node;
import blater.nql.util.Log;

/** Rejects hierarchy names reserved for cache-generated columns. */
final class CacheHierarchyInputNameValidator {
  private static final String RESERVED_PREFIX = "_nql_";

  private CacheHierarchyInputNameValidator() {
  }

  static void validate(Node node) {
    if (node.getName() != null
        && node.getName().toLowerCase(java.util.Locale.ROOT).startsWith(RESERVED_PREFIX)) {
      Log.fatal(IllegalArgumentException.class,
          "Input name [" + node.getName() + "] uses reserved prefix " + RESERVED_PREFIX);
    }
    for (Node child : node.getChildren()) {
      validate(child);
    }
  }
}
