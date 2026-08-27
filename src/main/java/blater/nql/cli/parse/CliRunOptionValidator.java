package blater.nql.cli.parse;

/** Enforces options owned by the run command. */
final class CliRunOptionValidator {
  private CliRunOptionValidator() {
  }

  static void validate(CliParser.RawArguments raw) {
    CliParser.reject(raw.pattern != null, "--pattern is only valid for catalog");
    CliParser.reject(raw.reportFormat != null, "--report-format is not valid for run");
    CliParser.reject(raw.all || raw.olderThan != null,
        "cache clear options are not valid for run");
  }
}
