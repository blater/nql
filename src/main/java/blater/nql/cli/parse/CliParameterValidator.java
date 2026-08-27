package blater.nql.cli.parse;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Validates parameter names and command-line assignments. */
final class CliParameterValidator {
  private CliParameterValidator() {
  }

  static void applyCommandLine(Iterable<String> assignments, Map<String, String> parameters) {
    Set<String> commandLineNames = new HashSet<>();
    for (String assignment : assignments) {
      int equals = assignment.indexOf('=');
      if (equals <= 0) {
        throw CliParser.usage(
            "--param requires a non-empty name=value assignment: " + assignment);
      }
      String name = assignment.substring(0, equals);
      validate(name);
      if (!commandLineNames.add(name)) {
        throw CliParser.usage("Duplicate --param name: " + name);
      }
      parameters.put(name, assignment.substring(equals + 1));
    }
  }

  static void validate(String name) {
    if (name.isBlank()) {
      throw CliParser.usage("Parameter name cannot be blank");
    }
    String normalized = name.toLowerCase(Locale.ROOT);
    if (normalized.startsWith("nql.") || normalized.startsWith("jdbc.")
        || normalized.startsWith("cache.") || normalized.startsWith("nsql_")
        || normalized.startsWith("nql_")) {
      throw CliParser.usage("Reserved task parameter name: " + name);
    }
  }
}
