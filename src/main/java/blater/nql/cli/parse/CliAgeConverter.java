package blater.nql.cli.parse;

import java.time.Duration;
import java.util.Locale;

/** Converts the human-readable age accepted by cache cleanup commands. */
final class CliAgeConverter {
  private CliAgeConverter() {
  }

  static Duration parse(String value) {
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    int unitStart = firstNonDigit(normalized);
    if (unitStart == 0) {
      throw usage("Invalid cache age: " + value);
    }
    long amount = amount(normalized, unitStart, value);
    try {
      return duration(amount, normalized.substring(unitStart).trim(), value);
    } catch (ArithmeticException exception) {
      throw new CliUsageException("Cache age is too large: " + value, exception);
    }
  }

  private static int firstNonDigit(String value) {
    int index = 0;
    while (index < value.length() && Character.isDigit(value.charAt(index))) {
      index++;
    }
    return index;
  }

  private static long amount(String value, int unitStart, String original) {
    try {
      return Long.parseLong(value.substring(0, unitStart));
    } catch (NumberFormatException exception) {
      throw new CliUsageException("Invalid cache age: " + original, exception);
    }
  }

  private static Duration duration(long amount, String unit, String original) {
    return switch (unit) {
      case "m", "min", "mins", "minute", "minutes" -> Duration.ofMinutes(amount);
      case "h", "hr", "hrs", "hour", "hours" -> Duration.ofHours(amount);
      case "d", "day", "days" -> Duration.ofDays(amount);
      default -> throw usage("Invalid cache age: " + original);
    };
  }

  private static CliUsageException usage(String message) {
    return new CliUsageException(message);
  }
}
