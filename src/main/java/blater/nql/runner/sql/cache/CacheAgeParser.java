package blater.nql.runner.sql.cache;

import java.time.Duration;

/** Parses the human-readable ages accepted by cache cleanup commands. */
final class CacheAgeParser {
  Duration parse(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("cache age duration is required");
    }
    String normalized = value.trim().toLowerCase();
    int split = leadingDigitsEnd(normalized);
    if (split == 0 || split == normalized.length()) {
      throw unsupported(value);
    }
    long amount = Long.parseLong(normalized.substring(0, split));
    return duration(amount, normalized.substring(split).trim(), value);
  }

  private static int leadingDigitsEnd(String value) {
    int split = 0;
    while (split < value.length() && Character.isDigit(value.charAt(split))) {
      split++;
    }
    return split;
  }

  private static Duration duration(long amount, String unit, String original) {
    return switch (unit) {
      case "m", "min", "mins", "minute", "minutes" -> Duration.ofMinutes(amount);
      case "h", "hr", "hrs", "hour", "hours" -> Duration.ofHours(amount);
      case "d", "day", "days" -> Duration.ofDays(amount);
      default -> throw unsupported(original);
    };
  }

  private static IllegalArgumentException unsupported(String value) {
    return new IllegalArgumentException("Unsupported cache age duration: " + value);
  }
}
