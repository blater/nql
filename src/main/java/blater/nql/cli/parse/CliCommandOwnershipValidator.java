package blater.nql.cli.parse;

/** Routes command-specific ownership checks. */
final class CliCommandOwnershipValidator {
  private CliCommandOwnershipValidator() {
  }

  static void validate(
      CliParser.Command command, String subcommand, CliParser.RawArguments raw) {
    switch (command) {
      case RUN -> CliRunOptionValidator.validate(raw);
      case CONVERT -> CliConvertOptionValidator.validate(raw);
      case CATALOG -> CliCatalogOptionValidator.validate(raw);
      case CACHE -> CliCacheOptionValidator.validate(subcommand, raw);
      case CAPABILITIES -> CliCapabilitiesOptionValidator.validate(raw);
      case HELP, VERSION, IMPLICIT -> throw new IllegalStateException("invalid help route");
    }
  }
}
