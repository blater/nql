package blater.nql.runner.sql.dml.statementbuilder;

import blater.nql.parser.script.NestStatement;
import blater.nql.runner.SyntaxErrorType;
import blater.nql.runner.sql.domain.SqlColumn;
import blater.nql.runner.sql.domain.SqlRow;
import blater.nql.runner.sql.domain.SqlStatement;

import java.util.ArrayList;
import java.util.List;

/** Assembles the SQL and bind values for one update row. */
final class UpdateStatementAssembly {
  private UpdateStatementAssembly() {
  }

  static SqlStatement build(NestStatement stmt, SqlRow row) {
    UpdateParts parts = collectParts(row);
    SyntaxErrorType status = status(parts);
    String sql = sql(stmt, parts, status);
    return new SqlStatement(
        status,
        sql,
        parameters(parts),
        null,
        parts.uidInvolved(),
        row,
        stmt);
  }

  private static UpdateParts collectParts(SqlRow row) {
    UpdateParts parts = new UpdateParts();
    for (SqlColumn column : row.getColumns()) {
      parts.accept(column);
      if (parts.stopsReading()) {
        break;
      }
    }
    return parts;
  }

  private static SyntaxErrorType status(UpdateParts parts) {
    if (parts.uidInvolved()) {
      return SyntaxErrorType.OK;
    }
    if (!parts.hasKey() || parts.missingKey()) {
      return SyntaxErrorType.UPDATE_MISSING_KEY;
    }
    return parts.hasValues() ? SyntaxErrorType.OK : SyntaxErrorType.UPDATE_NO_VALUES;
  }

  private static String sql(
      NestStatement stmt,
      UpdateParts parts,
      SyntaxErrorType status) {
    String prefix = "update " + stmt.getTargetName() + " set ";
    if (status != SyntaxErrorType.OK || parts.uidInvolved()) {
      return prefix + parts.assignments();
    }
    return prefix + parts.assignments() + " where " + parts.conditions();
  }

  private static List<Object> parameters(UpdateParts parts) {
    List<Object> parameters = new ArrayList<>(parts.setParameters());
    parameters.addAll(parts.keyParameters());
    return parameters;
  }

  private static final class UpdateParts {
    private final StringBuilder assignments = new StringBuilder();
    private final StringBuilder conditions = new StringBuilder();
    private final List<Object> setParameters = new ArrayList<>();
    private final List<Object> keyParameters = new ArrayList<>();
    private boolean hasKey;
    private boolean missingKey;
    private boolean uidInvolved;

    void accept(SqlColumn column) {
      if (column.isKey()) {
        acceptKey(column);
      } else {
        acceptValue(column);
      }
    }

    private void acceptKey(SqlColumn column) {
      hasKey = true;
      if (column.isUid()) {
        uidInvolved = true;
      } else if (column.missingData()) {
        missingKey = true;
      } else {
        appendCondition(column);
      }
    }

    private void acceptValue(SqlColumn column) {
      if (!column.missingData()) {
        appendAssignment(column);
      }
    }

    private void appendAssignment(SqlColumn column) {
      appendSeparator(assignments, ", ");
      assignments.append(column.sqlName()).append(" = ").append(column.sqlExpression());
      setParameters.add(column.bindValue());
    }

    private void appendCondition(SqlColumn column) {
      appendSeparator(conditions, " and ");
      conditions.append(column.sqlName()).append(" = ").append(column.sqlExpression());
      keyParameters.add(column.bindValue());
    }

    private static void appendSeparator(StringBuilder builder, String separator) {
      if (!builder.isEmpty()) {
        builder.append(separator);
      }
    }

    boolean stopsReading() {
      return uidInvolved || missingKey;
    }

    boolean hasValues() {
      return !assignments.isEmpty();
    }

    boolean hasKey() {
      return hasKey;
    }

    boolean missingKey() {
      return missingKey;
    }

    boolean uidInvolved() {
      return uidInvolved;
    }

    String assignments() {
      return assignments.toString();
    }

    String conditions() {
      return conditions.toString();
    }

    List<Object> setParameters() {
      return setParameters;
    }

    List<Object> keyParameters() {
      return keyParameters;
    }
  }
}
