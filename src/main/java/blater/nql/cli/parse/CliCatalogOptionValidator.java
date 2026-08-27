package blater.nql.cli.parse;

/** Enforces options owned by the catalog command. */
final class CliCatalogOptionValidator {
  private CliCatalogOptionValidator() {
  }

  static void validate(CliParser.RawArguments raw) {
    CliParser.reject(raw.output != null, "--output is not valid for catalog");
    CliParser.reject(raw.scriptFile != null || raw.scriptText != null,
        "script options are not valid for catalog");
    CliParser.reject(raw.noKeyInference, "--no-key-inference is only valid for run");
    CliParser.reject(raw.all || raw.olderThan != null,
        "cache clear options are not valid for catalog");
  }
}
