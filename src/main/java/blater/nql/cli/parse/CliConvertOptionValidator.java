package blater.nql.cli.parse;

/** Enforces options owned by the convert command. */
final class CliConvertOptionValidator {
  private CliConvertOptionValidator() {
  }

  static void validate(CliParser.RawArguments raw) {
    CliParser.reject(raw.scriptFile != null || raw.scriptText != null,
        "script options are not valid for conversion");
    CliParser.reject(raw.cache || raw.name != null, "cache options are not valid for conversion");
    CliParser.reject(raw.pattern != null, "--pattern is not valid for conversion");
    CliParser.reject(raw.reportFormat != null, "--report-format is not valid for conversion");
    CliParser.reject(raw.cacheDirectoryExplicit, "--cache-dir is not valid for conversion");
    CliParser.reject(raw.config != null, "--config is not valid for conversion");
    CliParser.reject(CliValueParser.hasJdbc(raw), "database options are not valid for conversion");
    CliParser.reject(raw.noKeyInference, "--no-key-inference is not valid for conversion");
    CliParser.reject(raw.all || raw.olderThan != null,
        "cache clear options are not valid for conversion");
  }
}
