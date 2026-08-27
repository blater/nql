package blater.nql.runner.sql.cache;

import blater.nql.domain.Node;
import blater.nql.domain.ScalarKind;
import blater.nql.runner.sql.SqlExecutor;
import blater.nql.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Emits analyzed hierarchy nodes into relation and repeated-value tables. */
final class CacheHierarchyRowWriter {
  private final SqlExecutor sqlExecutor;
  private final CacheHierarchyAnalysis analysis;
  private final CacheHierarchyNodeShape shape;
  private final Map<String, CacheTableState> tables = new LinkedHashMap<>();

  CacheHierarchyRowWriter(SqlExecutor sqlExecutor, CacheHierarchyAnalysis analysis) {
    this.sqlExecutor = sqlExecutor;
    this.analysis = analysis;
    this.shape = new CacheHierarchyNodeShape();
  }

  void write(Node root) {
    writeNode(root, null, null, true, root.getName());
    for (CacheTableState table : new ArrayList<>(tables.values())) {
      table.flush();
      if (table.created) {
        Log.debug("Cache table [{}]", table.logicalName);
      }
    }
  }

  private void writeNode(
      Node node, String parentTable, Object parentId, boolean root, String rootName) {
    CacheNodeParts parts = shape.classify(node);
    String currentTable = parentTable;
    Object currentId = parentId;
    if (shape.shouldMaterialize(node, parts, root)) {
      String logicalName = shape.relationName(node, parentTable, rootName);
      CacheTableState table = table(logicalName, false);
      currentId = table.writeObjectRow(
          parentTable, parentId, parts.valuesByName(), parts.repeatedNames());
      currentTable = logicalName;
    }
    for (Node child : parts.objectChildren()) {
      writeNode(child, currentTable, currentId, false, rootName);
    }
  }

  CacheTableState table(String logicalName, boolean valueTable) {
    CacheTableState existing = tables.get(logicalName);
    if (existing != null) {
      if (existing.valueTable != valueTable) {
        Log.fatal(IllegalArgumentException.class,
            "Cache table name collision for [" + logicalName + "]");
      }
      return existing;
    }
    boolean sourceIdentity = !valueTable && analysis.hasSourceIdentity(logicalName);
    CacheTableState table = new CacheTableState(
        this, sqlExecutor, logicalName, valueTable, sourceIdentity);
    tables.put(logicalName, table);
    return table;
  }

  ScalarKind fieldKind(String tableName, String fieldName) {
    return analysis.fieldKind(tableName, fieldName);
  }

  ScalarKind identityKind(String tableName) {
    return analysis.identityKind(tableName);
  }

  String parentIdColumn(String parentTable) {
    return parentTable + "_id";
  }

  String repeatedTableName(String parentTable, String fieldName) {
    return parentTable + "_" + fieldName;
  }

  static CacheScalarValue firstScalarValue(
      Map<String, List<CacheScalarValue>> valuesByName, String fieldName) {
    List<CacheScalarValue> values = valuesByName.get(fieldName);
    return values == null || values.isEmpty() ? null : values.getFirst();
  }
}
