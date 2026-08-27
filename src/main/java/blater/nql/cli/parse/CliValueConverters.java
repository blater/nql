package blater.nql.cli.parse;

import blater.nql.cli.CacheName;
import blater.nql.cli.JdbcConnectionSpec;
import blater.nql.inputreader.InputType;
import blater.nql.outputwriter.OutputType;
import blater.nql.report.ReportFormat;

import java.time.Duration;

/** Compatibility facade for the CLI's typed-value conversion services. */
final class CliValueConverters {
  private CliValueConverters() {
  }

  static InputType inputType(String value) {
    return CliFormatConverter.inputType(value);
  }

  static OutputType outputType(String value) {
    return CliFormatConverter.outputType(value);
  }

  static ReportFormat reportFormat(String value) {
    return CliFormatConverter.reportFormat(value);
  }

  static CacheName cacheName(String value) {
    return CliFormatConverter.cacheName(value);
  }

  static Duration age(String value) {
    return CliAgeConverter.parse(value);
  }

  static boolean hasJdbc(CliParser.RawArguments raw) {
    return CliJdbcConverter.hasOptions(raw);
  }

  static JdbcConnectionSpec jdbcConnection(CliParser.RawArguments raw) {
    return CliJdbcConverter.connection(raw);
  }

  static String knownDriver(String value) {
    return CliJdbcDriver.known(value);
  }
}
