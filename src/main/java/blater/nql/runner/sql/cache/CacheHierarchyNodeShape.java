package blater.nql.runner.sql.cache;

import blater.nql.domain.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Classifies node children and applies the cache table naming rules. */
final class CacheHierarchyNodeShape {
  CacheNodeParts classify(Node node) {
    Map<String, List<CacheScalarValue>> values = new LinkedHashMap<>();
    Set<String> repeated = new LinkedHashSet<>();
    List<Node> objects = new ArrayList<>();
    for (Node child : node.getChildren()) {
      if (isScalar(child)) {
        values.computeIfAbsent(child.getName(), ignored -> new ArrayList<>())
            .add(value(child));
        if (child.isArrayItem()) {
          repeated.add(child.getName());
        }
      } else if (!child.isAttribute()) {
        objects.add(child);
      }
    }
    for (Map.Entry<String, List<CacheScalarValue>> field : values.entrySet()) {
      if (field.getValue().size() > 1) {
        repeated.add(field.getKey());
      }
    }
    return new CacheNodeParts(values, repeated, objects);
  }

  boolean shouldMaterialize(Node node, CacheNodeParts parts, boolean root) {
    if (node.isAttribute() || node.isCollection() && node.getChildren().isEmpty()) {
      return false;
    }
    boolean emptyObject = !root && !node.hasValue() && node.getChildren().isEmpty();
    return !parts.valuesByName().isEmpty()
        || !root && (!parts.objectChildren().isEmpty() || emptyObject);
  }

  String relationName(Node node, String parentTable, String rootName) {
    if (parentTable == null && node.isArrayItem() && "item".equals(node.getName())
        && rootName != null
        && !Set.of("json", "yaml", "csv", "tsv", "toml").contains(rootName)) {
      return rootName;
    }
    return node.getName();
  }

  private boolean isScalar(Node node) {
    if (node.isAttribute()) {
      return true;
    }
    boolean hasElementChildren = node.getChildren().stream().anyMatch(child -> !child.isAttribute());
    return node.hasValue() && !hasElementChildren;
  }

  private CacheScalarValue value(Node node) {
    String value = node.isNull() ? null : node.getValue() == null ? "" : node.getValue();
    return new CacheScalarValue(value, node.getScalarKind());
  }
}
