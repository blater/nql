package blater.nql.cli.parse;

import blater.nql.cli.CacheName;
import blater.nql.inputreader.InputType;
import blater.nql.outputwriter.OutputType;
import blater.nql.report.ReportFormat;

import java.util.Locale;

/** Converts names used by format and cache-related options. */
final class CliFormatConverter {
  private CliFormatConverter() {
  }

  static InputType inputType(String value) {
    if (value == null || value.isBlank()) {
      throw usage("No input format supplied");
    }
    String normalized = normalize(value);
    return switch (normalized) {
      case "xml" -> InputType.XML;
      case "json" -> InputType.JSON;
      case "jsonl", "json-lines", "ndjson" -> InputType.JSONL;
      case "yaml", "yml" -> InputType.YAML;
      case "csv" -> InputType.CSV;
      case "tsv" -> InputType.TSV;
      case "toml" -> InputType.TOML;
      case "parquet" -> InputType.PARQUET;
      default -> throw usage("Unsupported input format: " + value);
    };
  }

  static OutputType outputType(String value) {
    if (value.equalsIgnoreCase("md")) {
      return OutputType.MARKDOWN;
    }
    try {
      return OutputType.fromName(value);
    } catch (RuntimeException exception) {
      throw new CliUsageException(exception.getMessage(), exception);
    }
  }

  static ReportFormat reportFormat(String value) {
    try {
      return ReportFormat.fromName(value);
    } catch (RuntimeException exception) {
      throw new CliUsageException(exception.getMessage(), exception);
    }
  }

  static CacheName cacheName(String value) {
    try {
      return new CacheName(value);
    } catch (IllegalArgumentException exception) {
      throw new CliUsageException(exception.getMessage(), exception);
    }
  }

  private static String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
  }

  private static CliUsageException usage(String message) {
    return new CliUsageException(message);
  }
}
