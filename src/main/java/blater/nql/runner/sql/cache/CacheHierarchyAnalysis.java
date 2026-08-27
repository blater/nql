package blater.nql.runner.sql.cache;

import blater.nql.domain.Hierarchy;
import blater.nql.domain.Node;
import blater.nql.domain.ScalarKind;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Determines relation identities and scalar types before SQL emission. */
final class CacheHierarchyAnalysis {
  private static final String SOURCE_ID_COLUMN = "id";

  private final CacheHierarchyNodeShape shape = new CacheHierarchyNodeShape();
  private final Map<String, Boolean> sourceIdentityByTable = new LinkedHashMap<>();
  private final Map<String, Map<String, ScalarKind>> scalarKindsByTable = new LinkedHashMap<>();

  void analyze(Hierarchy hierarchy) {
    CacheHierarchyInputNameValidator.validate(hierarchy.getRoot());
    analyzeNode(hierarchy.getRoot(), null, true, hierarchy.getRoot().getName());
  }

  private void analyzeNode(Node node, String parentTable, boolean root, String rootName) {
    CacheNodeParts parts = shape.classify(node);
    String currentTable = parentTable;
    if (shape.shouldMaterialize(node, parts, root)) {
      currentTable = shape.relationName(node, parentTable, rootName);
      CacheScalarValue sourceId = firstScalarValue(parts.valuesByName(), SOURCE_ID_COLUMN);
      sourceIdentityByTable.merge(
          currentTable, sourceId != null && sourceId.value() != null, Boolean::logicalAnd);
      mergeScalarKinds(currentTable, parts.valuesByName());
    }
    for (Node child : parts.objectChildren()) {
      analyzeNode(child, currentTable, false, rootName);
    }
  }

  private void mergeScalarKinds(
      String tableName, Map<String, List<CacheScalarValue>> valuesByName) {
    Map<String, ScalarKind> fields = scalarKindsByTable.computeIfAbsent(
        tableName, ignored -> new LinkedHashMap<>());
    for (Map.Entry<String, List<CacheScalarValue>> field : valuesByName.entrySet()) {
      ScalarKind kind = null;
      for (CacheScalarValue value : field.getValue()) {
        if (value.value() != null) {
          kind = ScalarKind.merge(kind, value.kind());
        }
      }
      if (kind != null) {
        fields.merge(field.getKey(), kind, ScalarKind::merge);
      }
    }
  }

  ScalarKind fieldKind(String tableName, String fieldName) {
    return scalarKindsByTable.getOrDefault(tableName, Map.of())
        .getOrDefault(fieldName, ScalarKind.STRING);
  }

  ScalarKind identityKind(String tableName) {
    return hasSourceIdentity(tableName) ? fieldKind(tableName, SOURCE_ID_COLUMN) : ScalarKind.STRING;
  }

  boolean hasSourceIdentity(String tableName) {
    return Boolean.TRUE.equals(sourceIdentityByTable.get(tableName));
  }

  static CacheScalarValue firstScalarValue(
      Map<String, List<CacheScalarValue>> valuesByName, String fieldName) {
    List<CacheScalarValue> values = valuesByName.get(fieldName);
    return values == null || values.isEmpty() ? null : values.getFirst();
  }
}
