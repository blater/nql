package blater.nql.cli.parse;

import blater.nql.cli.CatalogInvocation;
import blater.nql.cli.CatalogPattern;
import blater.nql.cli.DataInput;
import blater.nql.cli.ExecutionTarget;
import blater.nql.cli.InputSelection;
import blater.nql.report.ReportFormat;

import java.util.ArrayList;
import java.util.List;

/** Builds a catalog invocation from validated raw command-line arguments. */
final class CliCatalogBinding {
  private CliCatalogBinding() {
  }

  static CatalogInvocation bind(CliBindingSupport support, CliParser.RawArguments raw) {
    validate(raw);
    CatalogOperands operands = operands(raw);
    DataInput input = support.dataInput(raw, operands.data(), false);
    support.validateParquetOptions(raw, inputType(support, raw, input));
    rejectParametersWithoutInput(raw, input);
    return new CatalogInvocation(
        inputSelection(support, raw, input), pattern(operands.pattern()),
        target(support, raw, input), reportFormat(raw), support.invocationOptions(raw));
  }

  private static void validate(CliParser.RawArguments raw) {
    CliOptionValidator.validateCatalogOptionOwnership(raw);
    CliParser.reject(raw.name != null && !raw.cache, "--name requires --cache");
    CliParser.reject(raw.cache && CliValueParser.hasJdbc(raw),
        "catalog cannot combine --cache and JDBC");
    CliParser.reject(raw.inputFile != null && raw.inputText != null,
        "catalog accepts at most one data source");
  }

  private static CatalogOperands operands(CliParser.RawArguments raw) {
    String data = null;
    List<String> unknown = new ArrayList<>();
    for (String positional : raw.positionals) {
      if (CliParser.isDataFilename(positional)) {
        CliParser.reject(data != null || raw.inputFile != null || raw.inputText != null,
            "catalog accepts one data source");
        data = positional;
      } else {
        unknown.add(positional);
      }
    }
    if (unknown.size() > 1) {
      if (data == null && raw.inputFile == null && raw.inputText == null) {
        data = unknown.removeFirst();
      } else {
        throw CliParser.usage("catalog accepts at most a data source and pattern");
      }
    }
    String pattern = raw.pattern;
    if (!unknown.isEmpty()) {
      CliParser.reject(pattern != null, "positional pattern conflicts with --pattern");
      pattern = unknown.getFirst();
    }
    return new CatalogOperands(data, pattern);
  }

  private static void rejectParametersWithoutInput(
      CliParser.RawArguments raw, DataInput input) {
    if (input == null) {
      CliParser.reject(raw.paramsFile != null || !raw.params.isEmpty(),
          "task parameters require catalog input data");
    }
  }

  private static blater.nql.inputreader.InputType inputType(
      CliBindingSupport support, CliParser.RawArguments raw, DataInput input) {
    return input == null ? support.implicitInputType(raw) : input.format();
  }

  private static InputSelection inputSelection(
      CliBindingSupport support, CliParser.RawArguments raw, DataInput input) {
    return input == null
        ? new InputSelection.Automatic(support.implicitInputType(raw))
        : new InputSelection.Provided(input);
  }

  private static CatalogPattern pattern(String value) {
    return value == null ? new CatalogPattern.All() : new CatalogPattern.Matching(value);
  }

  private static ReportFormat reportFormat(CliParser.RawArguments raw) {
    return raw.reportFormat == null
        ? ReportFormat.MARKDOWN : CliValueParser.reportFormat(raw.reportFormat);
  }

  private static ExecutionTarget target(
      CliBindingSupport support, CliParser.RawArguments raw, DataInput input) {
    if (input != null) {
      CliParser.reject(raw.cache || CliValueParser.hasJdbc(raw),
          "catalog data conflicts with another source");
      CliParser.reject(raw.cacheDirectoryExplicit,
          "--cache-dir is not valid for temporary catalog execution");
      return new ExecutionTarget.Temporary();
    }
    if (raw.cache) {
      return raw.name == null
          ? new ExecutionTarget.ActiveCache(support.cacheDirectory(raw))
          : new ExecutionTarget.NamedCache(
              support.cacheDirectory(raw), CliValueParser.cacheName(raw.name));
    }
    if (CliValueParser.hasJdbc(raw)) {
      CliParser.reject(raw.cacheDirectoryExplicit,
          "--cache-dir is not valid for JDBC catalog execution");
      return new ExecutionTarget.Jdbc(CliValueParser.jdbcConnection(raw));
    }
    return new ExecutionTarget.InputOrActiveCache(support.cacheDirectory(raw));
  }

  private record CatalogOperands(String data, String pattern) {
  }
}
