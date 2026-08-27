package blater.nql.cli.parse;

import java.util.List;

/** Detects option families that are incompatible with root-only commands. */
final class CliOptionPresenceValidator {
  private CliOptionPresenceValidator() {
  }

  static void requireNoOperands(CliParser.RawArguments raw, String command) {
    CliParser.reject(!raw.positionals.isEmpty(), command + " accepts no operands");
  }

  static boolean hasNonHelpOptions(CliParser.RawArguments raw) {
    return List.of(
        raw.scriptFile != null, raw.scriptText != null, raw.inputFile != null,
        raw.inputText != null, raw.inputFormat != null, raw.pattern != null,
        raw.output != null, raw.reportFormat != null, raw.cache, raw.name != null,
        raw.cacheDirectory != null, raw.olderThan != null, raw.all, raw.config != null,
        raw.removedProperties != null, raw.paramsFile != null, !raw.params.isEmpty(),
        raw.parquetRoot != null, raw.parquetRecord != null, raw.debug,
        raw.noKeyInference, raw.capabilities, CliValueParser.hasJdbc(raw)).contains(true);
  }

  static boolean hasNonCapabilityOptions(CliParser.RawArguments raw) {
    return List.of(
        raw.scriptFile != null, raw.scriptText != null, raw.inputFile != null,
        raw.inputText != null, raw.inputFormat != null, raw.pattern != null,
        raw.output != null, raw.cache, raw.name != null, raw.cacheDirectoryExplicit,
        raw.olderThan != null, raw.all, raw.config != null, raw.removedProperties != null,
        raw.paramsFile != null, !raw.params.isEmpty(), raw.parquetRoot != null,
        raw.parquetRecord != null, raw.debug, raw.noKeyInference,
        CliValueParser.hasJdbc(raw)).contains(true);
  }

  static void rejectNonHelpOptions(CliParser.RawArguments raw, String command) {
    CliParser.reject(hasNonHelpOptions(raw), command + " accepts no options");
  }
}
