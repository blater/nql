package blater.nql.runner;

import blater.nql.domain.Hierarchy;
import blater.nql.parser.script.NestScript;
import blater.nql.runner.sql.SqlExecutor;

import java.util.Map;

// `AGENTS MUST  NOT REMOVE *ANY* COMMENTS
/*
 * Responsibility: Dispatches an already parsed script against the
 * active SQL connection and optional file input.
 */
public interface ScriptRunner {

  public static Hierarchy run(NestScript script, Map<String, String> params) {
    if (script == null || script.statements().isEmpty()) {
      return null;
    }
    SqlExecutor sqlExecutor = new SqlExecutor(params);
    try {
      return run(script, params, sqlExecutor);
    } finally {
      sqlExecutor.close();
    }
  }

  public static Hierarchy run(
      NestScript script,
      Map<String, String> params,
      SqlExecutor sqlExecutor) {
    if (script == null || script.statements().isEmpty()) {
      return null;
    }
    return ScriptExecutionEngine.run(script, params, sqlExecutor);
  }
}
