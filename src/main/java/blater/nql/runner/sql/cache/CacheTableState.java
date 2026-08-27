package blater.nql.runner.sql.cache;

import blater.nql.domain.ScalarKind;
import blater.nql.runner.sql.SqlExecutor;
import blater.nql.runner.sql.SqlRowCursor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Owns the schema, row buffer, and repeated-value tables for one relation. */
final class CacheTableState {
  private static final String SOURCE_ID_COLUMN = "id";
  private static final String GENERATED_ID_COLUMN = "_nql_id";
  private static final int INSERT_BATCH_SIZE = 500;

  private final CacheHierarchyRowWriter owner;
  private final SqlExecutor sqlExecutor;
  final String logicalName;
  private final String renderedName;
  final boolean valueTable;
  private final boolean sourceIdentity;
  private final Map<String, String> columnsByLogicalName = new LinkedHashMap<>();
  private final Map<String, FieldStorageType> fieldStorageTypesByLogicalName = new LinkedHashMap<>();
  private final List<Map<String, Object>> pendingRows = new ArrayList<>();
  private int nextGeneratedId = 1;
  boolean created;

  CacheTableState(
      CacheHierarchyRowWriter owner,
      SqlExecutor sqlExecutor,
      String logicalName,
      boolean valueTable,
      boolean sourceIdentity) {
    this.owner = owner;
    this.sqlExecutor = sqlExecutor;
    this.logicalName = logicalName;
    this.renderedName = CacheSqlIdentifier.render(logicalName);
    this.valueTable = valueTable;
    this.sourceIdentity = sourceIdentity;
  }

  Object writeObjectRow(
      String parentTable,
      Object parentId,
      Map<String, List<CacheScalarValue>> valuesByName,
      Set<String> repeatedNames) {
    Object rowId = rowId(valuesByName);
    String parentColumn = parentTable == null ? null : owner.parentIdColumn(parentTable);
    ensureIdentityColumns(rowId, parentColumn, parentTable);
    prepareFields(valuesByName, repeatedNames, parentColumn);

    Map<String, Object> row = objectRow(
        rowId, parentTable, parentId, parentColumn, valuesByName);
    pendingRows.add(row);
    flushIfFull();
    writeRepeatedFields(rowId, valuesByName, parentColumn);
    return rowId;
  }

  private Object rowId(Map<String, List<CacheScalarValue>> valuesByName) {
    CacheScalarValue sourceId = CacheHierarchyRowWriter.firstScalarValue(valuesByName, SOURCE_ID_COLUMN);
    return sourceIdentity
        ? sourceId.databaseValue(owner.identityKind(logicalName)) : nextId();
  }

  private void ensureIdentityColumns(Object rowId, String parentColumn, String parentTable) {
    ensureColumn(rowIdColumn(), owner.identityKind(logicalName));
    if (parentColumn != null) {
      ensureColumn(parentColumn, owner.identityKind(parentTable));
    }
  }

  private void prepareFields(
      Map<String, List<CacheScalarValue>> valuesByName,
      Set<String> repeatedNames,
      String parentColumn) {
    for (Map.Entry<String, List<CacheScalarValue>> field : valuesByName.entrySet()) {
      if (!isStructuralColumn(field.getKey(), parentColumn)) {
        prepareFieldStorage(
            field.getKey(), repeatedNames.contains(field.getKey()),
            owner.fieldKind(logicalName, field.getKey()));
      }
    }
  }

  private Map<String, Object> objectRow(
      Object rowId,
      String parentTable,
      Object parentId,
      String parentColumn,
      Map<String, List<CacheScalarValue>> valuesByName) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put(rowIdColumn(), rowId);
    if (parentColumn != null) {
      CacheScalarValue explicitParent = CacheHierarchyRowWriter.firstScalarValue(valuesByName, parentColumn);
      row.put(parentColumn, explicitParent == null || explicitParent.value() == null
          ? parentId : explicitParent.databaseValue(owner.identityKind(parentTable)));
    }
    for (Map.Entry<String, List<CacheScalarValue>> field : valuesByName.entrySet()) {
      if (isStructuralColumn(field.getKey(), parentColumn)
          || fieldStorageTypesByLogicalName.get(field.getKey()) != FieldStorageType.COLUMN) {
        continue;
      }
      CacheScalarValue value = CacheHierarchyRowWriter.firstScalarValue(valuesByName, field.getKey());
      row.put(field.getKey(), value == null
          ? null : value.databaseValue(owner.fieldKind(logicalName, field.getKey())));
    }
    return row;
  }

  private void writeRepeatedFields(
      Object rowId,
      Map<String, List<CacheScalarValue>> valuesByName,
      String parentColumn) {
    for (Map.Entry<String, List<CacheScalarValue>> field : valuesByName.entrySet()) {
      if (!isStructuralColumn(field.getKey(), parentColumn)
          && fieldStorageTypesByLogicalName.get(field.getKey()) == FieldStorageType.VALUE_TABLE) {
        writeRepeatedValueRows(rowId, field.getKey(), field.getValue());
      }
    }
  }

  private void writeRepeatedValueRows(
      Object parentId, String fieldName, List<CacheScalarValue> values) {
    for (CacheScalarValue value : values) {
      writeRepeatedValueRow(parentId, fieldName, value);
    }
  }

  private void writeRepeatedValueRow(
      Object parentId, String fieldName, CacheScalarValue value) {
    ScalarKind valueKind = owner.fieldKind(logicalName, fieldName);
    CacheTableState valueTable = ensureValueTable(fieldName, valueKind);
    valueTable.writeValueTableRow(logicalName, parentId, value, valueKind);
  }

  private void writeValueTableRow(
      String parentTable, Object parentId, CacheScalarValue value, ScalarKind valueKind) {
    ensureColumn(rowIdColumn(), ScalarKind.STRING);
    ensureColumn(owner.parentIdColumn(parentTable), owner.identityKind(parentTable));
    ensureColumn("value", valueKind);
    Map<String, Object> row = new LinkedHashMap<>();
    row.put(rowIdColumn(), nextId());
    row.put(owner.parentIdColumn(parentTable), parentId);
    row.put("value", value.databaseValue(valueKind));
    pendingRows.add(row);
    flushIfFull();
  }

  private void prepareFieldStorage(
      String fieldName, boolean repeated, ScalarKind scalarKind) {
    FieldStorageType storageType = fieldStorageTypesByLogicalName.get(fieldName);
    if (storageType == FieldStorageType.VALUE_TABLE) {
      ensureValueTable(fieldName, scalarKind);
    } else if (storageType == FieldStorageType.COLUMN && repeated) {
      promoteField(fieldName, scalarKind);
    } else if (storageType == null && repeated) {
      fieldStorageTypesByLogicalName.put(fieldName, FieldStorageType.VALUE_TABLE);
      ensureValueTable(fieldName, scalarKind);
    } else {
      fieldStorageTypesByLogicalName.putIfAbsent(fieldName, FieldStorageType.COLUMN);
      ensureColumn(fieldName, scalarKind);
    }
  }

  private void promoteField(String fieldName, ScalarKind scalarKind) {
    flush();
    ensureValueTable(fieldName, scalarKind);
    String renderedFieldColumn = columnsByLogicalName.get(fieldName);
    if (renderedFieldColumn == null) {
      fieldStorageTypesByLogicalName.put(fieldName, FieldStorageType.VALUE_TABLE);
      return;
    }
    String select = "select " + columnsByLogicalName.get(rowIdColumn())
        + " as \"cache_parent_id\", " + renderedFieldColumn
        + " as \"cache_field_value\" from " + renderedName
        + " where " + renderedFieldColumn + " is not null";
    try (SqlRowCursor rows = sqlExecutor.query(select)) {
      while (rows.next()) {
        writeRepeatedValueRow(
            rows.row().getValue("cache_parent_id"), fieldName,
            new CacheScalarValue(rows.row().getStringValue("cache_field_value"), scalarKind));
      }
    }
    columnsByLogicalName.remove(fieldName);
    sqlExecutor.execute("alter table " + renderedName + " drop column " + renderedFieldColumn);
    fieldStorageTypesByLogicalName.put(fieldName, FieldStorageType.VALUE_TABLE);
  }

  private CacheTableState ensureValueTable(String fieldName, ScalarKind valueKind) {
    CacheTableState valueTable = owner.table(owner.repeatedTableName(logicalName, fieldName), true);
    valueTable.ensureColumn(valueTable.rowIdColumn(), ScalarKind.STRING);
    valueTable.ensureColumn(owner.parentIdColumn(logicalName), owner.identityKind(logicalName));
    valueTable.ensureColumn("value", valueKind);
    return valueTable;
  }

  private void ensureColumn(String logicalColumnName, ScalarKind scalarKind) {
    if (columnsByLogicalName.containsKey(logicalColumnName)) {
      return;
    }
    String renderedColumnName = CacheSqlIdentifier.render(logicalColumnName);
    if (!created) {
      columnsByLogicalName.put(logicalColumnName, renderedColumnName);
      sqlExecutor.execute("create table " + renderedName + " (" + renderedColumnName
          + " " + sqlType(scalarKind) + ")");
      created = true;
      return;
    }
    flush();
    columnsByLogicalName.put(logicalColumnName, renderedColumnName);
    sqlExecutor.execute("alter table " + renderedName + " add column " + renderedColumnName
        + " " + sqlType(scalarKind));
  }

  private void flushIfFull() {
    if (pendingRows.size() >= INSERT_BATCH_SIZE) {
      flush();
    }
  }

  void flush() {
    if (pendingRows.isEmpty()) {
      return;
    }
    String columns = String.join(", ", columnsByLogicalName.values());
    String placeholders = columnsByLogicalName.keySet().stream()
        .map(ignored -> "?").collect(Collectors.joining(", "));
    String insert = "insert into " + renderedName + " (" + columns + ") values ("
        + placeholders + ")";
    List<List<Object>> rows = new ArrayList<>(pendingRows.size());
    for (Map<String, Object> pendingRow : pendingRows) {
      List<Object> values = new ArrayList<>(columnsByLogicalName.size());
      for (String column : columnsByLogicalName.keySet()) {
        values.add(pendingRow.get(column));
      }
      rows.add(values);
    }
    sqlExecutor.executeBatch(insert, rows);
    pendingRows.clear();
  }

  private boolean isStructuralColumn(String fieldName, String parentColumn) {
    return rowIdColumn().equals(fieldName) || parentColumn != null && parentColumn.equals(fieldName);
  }

  private String nextId() {
    return Integer.toString(nextGeneratedId++);
  }

  private String rowIdColumn() {
    return sourceIdentity ? SOURCE_ID_COLUMN : GENERATED_ID_COLUMN;
  }

  private static String sqlType(ScalarKind scalarKind) {
    return switch (scalarKind) {
      case STRING -> "varchar";
      case NUMBER -> "decfloat";
      case BOOLEAN -> "boolean";
    };
  }

  private enum FieldStorageType { COLUMN, VALUE_TABLE }
}
