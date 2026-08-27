package blater.nql.runner.sql.dml.statementbuilder;

import blater.nql.parser.script.NestStatement;
import blater.nql.runner.sql.domain.SqlRow;
import blater.nql.runner.sql.domain.SqlStatement;

/*
 * Responsibility: Builds a parameterized UPDATE statement from one
 * parsed statement and one mapped SQL row.
 */
public class UpdateStatementBuilder {
  public UpdateStatementBuilder() {
  }

  public static SqlStatement build(NestStatement stmt, SqlRow row) {
    return UpdateStatementAssembly.build(stmt, row);
  }
}
