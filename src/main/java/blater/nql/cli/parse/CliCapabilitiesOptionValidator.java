package blater.nql.cli.parse;

/** Enforces the report-format-only contract of capabilities. */
final class CliCapabilitiesOptionValidator {
  private CliCapabilitiesOptionValidator() {
  }

  static void validate(CliParser.RawArguments raw) {
    CliParser.reject(
        CliOptionPresenceValidator.hasNonCapabilityOptions(raw),
        "capabilities accepts only --report-format");
  }
}
