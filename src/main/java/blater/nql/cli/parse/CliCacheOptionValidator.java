package blater.nql.cli.parse;

/** Enforces options shared by cache commands and their subcommand rules. */
final class CliCacheOptionValidator {
  private CliCacheOptionValidator() {
  }

  static void validate(String subcommand, CliParser.RawArguments raw) {
    CliParser.reject(raw.cache, "--cache is not valid inside a cache subcommand");
    CliParser.reject(raw.output != null, "cache commands use --report-format, not --output");
    CliParser.reject(raw.scriptFile != null || raw.scriptText != null,
        "script options are not valid for cache commands");
    CliParser.reject(raw.pattern != null, "--pattern is only valid for catalog");
    CliParser.reject(raw.noKeyInference, "--no-key-inference is only valid for run");
    CliParser.reject(CliValueParser.hasJdbc(raw), "database options are not valid for cache commands");
    if (subcommand != null && !subcommand.equals("load")) {
      rejectNonLoadOptions(subcommand, raw);
    }
  }

  private static void rejectNonLoadOptions(String subcommand, CliParser.RawArguments raw) {
    CliParser.reject(raw.inputFile != null || raw.inputText != null || raw.inputFormat != null,
        "input options are not valid for cache " + subcommand);
    CliParser.reject(raw.paramsFile != null || !raw.params.isEmpty(),
        "parameters are not valid for cache " + subcommand);
    CliParser.reject(raw.parquetRoot != null || raw.parquetRecord != null,
        "Parquet options are not valid for cache " + subcommand);
  }
}
