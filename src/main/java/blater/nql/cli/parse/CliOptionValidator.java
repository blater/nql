package blater.nql.cli.parse;

/** Compatibility facade for command-line option validation. */
final class CliOptionValidator {
  private CliOptionValidator() {
  }

  static void requireNoOperands(CliParser.RawArguments raw, String command) {
    CliOptionPresenceValidator.requireNoOperands(raw, command);
  }

  static boolean hasNonHelpOptions(CliParser.RawArguments raw) {
    return CliOptionPresenceValidator.hasNonHelpOptions(raw);
  }

  static boolean hasNonCapabilityOptions(CliParser.RawArguments raw) {
    return CliOptionPresenceValidator.hasNonCapabilityOptions(raw);
  }

  static void rejectNonHelpOptions(CliParser.RawArguments raw, String command) {
    CliOptionPresenceValidator.rejectNonHelpOptions(raw, command);
  }

  static void validateHelpOptions(
      CliParser.Command command, String subcommand, CliParser.RawArguments raw) {
    CliHelpOptionValidator.validate(command, subcommand, raw);
  }

  static void validateRunOptionOwnership(CliParser.RawArguments raw) {
    CliRunOptionValidator.validate(raw);
  }

  static void validateConvertOptionOwnership(CliParser.RawArguments raw) {
    CliConvertOptionValidator.validate(raw);
  }

  static void validateCatalogOptionOwnership(CliParser.RawArguments raw) {
    CliCatalogOptionValidator.validate(raw);
  }

  static void validateCacheOptionOwnership(String subcommand, CliParser.RawArguments raw) {
    CliCacheOptionValidator.validate(subcommand, raw);
  }

  static void validateCapabilitiesOptionOwnership(CliParser.RawArguments raw) {
    CliCapabilitiesOptionValidator.validate(raw);
  }
}
