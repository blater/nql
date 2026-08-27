package blater.nql.runner;

import blater.nql.domain.Hierarchy;
import blater.nql.execution.EngineInputLoader;
import blater.nql.parser.script.NestScript;
import blater.nql.parser.script.NestStatement;
import blater.nql.runner.sql.Capture;
import blater.nql.runner.sql.SqlExecutor;
import blater.nql.runner.sql.dml.RunDelete;
import blater.nql.runner.sql.dml.RunInsert;
import blater.nql.runner.sql.dml.RunLiteralSql;
import blater.nql.runner.sql.dml.RunProcedure;
import blater.nql.runner.sql.dml.RunUpdate;
import blater.nql.runner.sql.dml.mapping.InputFileRowMapper;
import blater.nql.runner.sql.dml.mapping.MappingResult;
import blater.nql.runner.sql.domain.DmlExecutionResult;
import blater.nql.runner.sql.domain.SqlRow;
import blater.nql.runner.sql.query.RunQuery;
import blater.nql.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static blater.nql.util.ValueUtil.has;

/** Executes statements using a single caller-owned SQL executor. */
final class ScriptExecutionEngine {
  private ScriptExecutionEngine() { }

  static Hierarchy run(
      NestScript script,
      Map<String, String> params,
      SqlExecutor sqlExecutor) {
    ScriptExecutionState state = new ScriptExecutionState(params, sqlExecutor);
    for (NestStatement statement : script.statements()) {
      state.execute(statement);
    }
    return state.result();
  }

  private static final class ScriptExecutionState {
    private final Map<String, String> parameters;
    private final SqlExecutor sqlExecutor;
    private final InputFileRowMapper inputFileRowMapper = new InputFileRowMapper();
    private final Map<String, List<Map<String, Object>>> captureRowSets = new HashMap<>();
    private Hierarchy inputHierarchy;
    private Hierarchy hierarchy;

    private ScriptExecutionState(Map<String, String> parameters, SqlExecutor sqlExecutor) {
      this.parameters = parameters;
      this.sqlExecutor = sqlExecutor;
    }

    private void execute(NestStatement statement) {
      switch (statement.getType()) {
        case AUTOCOMMIT -> setAutoCommit(statement);
        case CAPTURE -> captureRowSets.putAll(
            Capture.captureTempRowset(statement, parameters, sqlExecutor));
        case CATALOG -> hierarchy = sqlExecutor.catalog(statement.getCatalogPattern());
        case SELECT -> hierarchy = RunQuery.runQuery(
            statement, parameters, hierarchy, sqlExecutor);
        case LITERAL -> RunLiteralSql.execute(statement, parameters, sqlExecutor);
        case INSERT, UPDATE, DELETE, PROC -> executeDml(statement);
      }
    }

    private void setAutoCommit(NestStatement statement) {
      boolean enabled = has(statement.getTargetName())
          && statement.getTargetName().equals("true");
      sqlExecutor.setAutoCommit(enabled);
    }

    private void executeDml(NestStatement statement) {
      if (inputDataIsFromFile(statement)) {
        executeInputFileDml(statement);
        return;
      }
      executeCapturedRows(statement);
    }

    private void executeInputFileDml(NestStatement statement) {
      if (inputHierarchy == null) {
        inputHierarchy = EngineInputLoader.load(parameters);
      }
      MappingResult mapping = inputFileRowMapper.map(
          inputHierarchy,
          statement.getMappings(),
          statement.getReturnMappings(),
          parameters);
      if (mapping.hasProblem()) {
        sqlExecutor.checkStatementError(mapping.problemStatus(), statement.getErrorHandling());
        return;
      }
      for (SqlRow row : mapping.rows()) {
        DmlExecutionResult result = executeDml(statement, row);
        inputFileRowMapper.applyWriteBack(row, result);
      }
    }

    private void executeCapturedRows(NestStatement statement) {
      // use rows captured from a preceding 'capture' statement; each is mapped to a SqlRow & DML run with it
      List<Map<String, Object>> rows = captureRowSets.get(statement.getSourceRowsetName());
      if (rows == null) {
        Log.fatal(IllegalArgumentException.class,
            "No temp rowset named: " + statement.getSourceRowsetName());
      }

      // run the statement against each captured row one by one
      // annoying for more than a couple of dozen rows, bad for > 1k rows, unusable for >10k
      //  todo:
      //   add captures at time of capture into in-memory temp table & reformulate the dml
      //   statement dynamically to reference the temp table.
      //   for small row sets, similar or less efficient; for >1K rows, hundreds of times more efficient;
      //   for >100K rows, thousands of times more efficient
      for (Map<String, Object> capturedRow : rows) {
        SqlRow row = Capture.toSqlRow(
            statement.getMappings(), capturedRow, parameters);
        executeDml(statement, row);
      }
    }

    private static boolean inputDataIsFromFile(NestStatement statement) {
      return statement.getSourceRowsetName() == null;
    }

    private DmlExecutionResult executeDml(NestStatement statement, SqlRow row) {
      return switch (statement.getType()) {
        case INSERT -> RunInsert.execute(statement, row, sqlExecutor);
        case UPDATE -> RunUpdate.execute(statement, row, sqlExecutor);
        case DELETE -> {
          RunDelete.execute(statement, row, sqlExecutor);
          yield DmlExecutionResult.EMPTY;
        }
        case PROC -> RunProcedure.execute(statement, row, sqlExecutor);
        default -> Log.fatal(IllegalStateException.class,
            "executeDml called with non-DML type: " + statement.getType());
      };
    }

    private Hierarchy result() {
      if (inputHierarchy != null) {
        return inputHierarchy;
      }
      // Refactor note: callers expect DML-only scripts to return an empty hierarchy, not null.
      return hierarchy == null ? new Hierarchy() : hierarchy;
    }
  }
}
