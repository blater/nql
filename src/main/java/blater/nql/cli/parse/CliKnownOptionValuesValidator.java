package blater.nql.cli.parse;

/** Validates values for options that are accepted by more than one command. */
final class CliKnownOptionValuesValidator {
  private CliKnownOptionValuesValidator() {
  }

  static void validate(CliParser.RawArguments raw) {
    if (raw.inputFormat != null) CliValueParser.inputType(raw.inputFormat);
    if (raw.output != null) CliValueParser.outputType(raw.output);
    if (raw.reportFormat != null) CliValueParser.reportFormat(raw.reportFormat);
    if (raw.databaseType != null) CliValueParser.knownDriver(raw.databaseType);
    if (raw.jdbcDriver != null) CliValueParser.knownDriver(raw.jdbcDriver);
    if (raw.olderThan != null) CliValueParser.age(raw.olderThan);
    if (raw.name != null) CliValueParser.cacheName(raw.name);
  }
}
