package blater.nql.cli.parse;

import blater.nql.cli.DataInput;
import blater.nql.cli.ExecutionTarget;

/** Selects the run execution target and validates target-specific options. */
final class CliRunTargetSelector {
  private CliRunTargetSelector() {
  }

  static ExecutionTarget select(
      CliBindingSupport support, CliParser.RawArguments raw, DataInput data) {
    if (raw.cache) {
      return raw.name == null
          ? new ExecutionTarget.ActiveCache(support.cacheDirectory(raw))
          : new ExecutionTarget.NamedCache(
              support.cacheDirectory(raw), CliValueParser.cacheName(raw.name));
    }
    if (CliValueParser.hasJdbc(raw)) {
      CliParser.reject(raw.cacheDirectoryExplicit,
          "--cache-dir is not valid for JDBC execution");
      return new ExecutionTarget.Jdbc(CliValueParser.jdbcConnection(raw));
    }
    if (data != null) {
      CliParser.reject(raw.cacheDirectoryExplicit,
          "--cache-dir is not valid for temporary data execution");
      return new ExecutionTarget.Temporary();
    }
    return new ExecutionTarget.InputOrActiveCache(support.cacheDirectory(raw));
  }
}
