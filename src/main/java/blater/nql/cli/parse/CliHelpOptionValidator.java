package blater.nql.cli.parse;

/** Coordinates validation for help and version invocations. */
final class CliHelpOptionValidator {
  private CliHelpOptionValidator() {
  }

  static void validate(
      CliParser.Command command, String subcommand, CliParser.RawArguments raw) {
    if (command == CliParser.Command.HELP) {
      CliOptionPresenceValidator.rejectNonHelpOptions(raw, "help");
      CliHelpTopicValidator.validate(raw.positionals);
      return;
    }
    if (command == CliParser.Command.VERSION) {
      CliOptionPresenceValidator.rejectNonHelpOptions(raw, "version");
      return;
    }
    CliCommandOwnershipValidator.validate(effectiveCommand(command, raw), subcommand, raw);
    CliKnownOptionValuesValidator.validate(raw);
  }

  private static CliParser.Command effectiveCommand(
      CliParser.Command command, CliParser.RawArguments raw) {
    return command == CliParser.Command.IMPLICIT
        ? CliImplicitBinder.implicitCommand(raw)
        : command;
  }
}
