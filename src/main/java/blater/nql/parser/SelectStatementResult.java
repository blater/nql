package blater.nql.parser;

import blater.nql.domain.MappingPlan;
import blater.nql.parser.script.NestStatement;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

import static blater.nql.util.Log.fatal;

/** Creates a statement from the parsed SELECT representation. */
final class SelectStatementResult {
  private SelectStatementResult() {
  }

  static NestStatement create(String sql, SelectStatementParts parts) {
    if (!parts.hasHierarchyFields() && parts.using().hasValues()) {
      fatal(HiqlSyntaxException.class,
          "using metadata requires at least one hierarchy mapping alias.");
    }
    if (!parts.hasHierarchyFields()) {
      return flatStatement(sql, parts);
    }
    SelectBlueprint.Compiled compiled = parts.blueprint().compile(List.of());
    return NestStatement.select(
        compiled.sql(), compiled.plan(), parts.using().namespace(), parts.blueprint());
  }

  private static NestStatement flatStatement(String sql, SelectStatementParts parts) {
    return NestStatement.select(
        sql, new MappingPlan(), parts.using().namespace(), parts.blueprint());
  }
}
