package blater.nql.cli.parse;

import blater.nql.cli.DataInput;
import blater.nql.cli.DataSourceSpec;
import blater.nql.inputreader.InputType;

import java.nio.file.Path;

/** Resolves invocation input sources and their implicit formats. */
final class CliInputSupport {
  DataInput dataInput(CliParser.RawArguments raw, String positional, boolean defaultStdin) {
    InputSource source = source(raw, positional, defaultStdin);
    if (source == null) {
      return null;
    }
    InputType type = raw.inputFormat == null
        ? inferredType(source.filename())
        : CliValueParser.inputType(raw.inputFormat);
    return new DataInput(source.spec(), type);
  }

  InputType implicitInputType(CliParser.RawArguments raw) {
    return raw.inputFormat == null ? InputType.JSON : CliValueParser.inputType(raw.inputFormat);
  }

  void validateParquetOptions(CliParser.RawArguments raw, InputType inputType) {
    if ((raw.parquetRoot != null || raw.parquetRecord != null)
        && inputType != InputType.PARQUET) {
      throw CliParser.usage("--parquet-root and --parquet-record require Parquet input");
    }
  }

  private static InputSource source(
      CliParser.RawArguments raw, String positional, boolean defaultStdin) {
    if (raw.inputFile != null) {
      return file(raw.inputFile);
    }
    if (raw.inputText != null) {
      return new InputSource(new DataSourceSpec.Text(raw.inputText), null);
    }
    if (positional != null) {
      return positional(positional);
    }
    return defaultStdin ? new InputSource(new DataSourceSpec.StandardInput(), null) : null;
  }

  private static InputSource file(String filename) {
    DataSourceSpec source = "-".equals(filename)
        ? new DataSourceSpec.StandardInput()
        : new DataSourceSpec.File(Path.of(filename));
    return new InputSource(source, filename);
  }

  private static InputSource positional(String value) {
    if ("-".equals(value)) {
      return new InputSource(new DataSourceSpec.StandardInput(), value);
    }
    DataSourceSpec source = CliParser.isDataFilename(value)
        ? new DataSourceSpec.File(Path.of(value))
        : new DataSourceSpec.Text(value);
    return new InputSource(source, value);
  }

  private static InputType inferredType(String filename) {
    return filename != null && CliParser.isDataFilename(filename)
        ? InputType.fromFilename(filename)
        : InputType.JSON;
  }

  private record InputSource(DataSourceSpec spec, String filename) {
  }
}
