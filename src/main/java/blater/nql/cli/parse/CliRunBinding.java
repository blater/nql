package blater.nql.cli.parse;

import blater.nql.cli.DataInput;
import blater.nql.cli.InputSelection;
import blater.nql.cli.OutputSelection;
import blater.nql.cli.RunInvocation;

/** Builds a run invocation from validated raw command-line arguments. */
final class CliRunBinding {
  private CliRunBinding() {
  }

  static RunInvocation bind(CliBindingSupport support, CliParser.RawArguments raw) {
    validate(raw);
    CliRunOperands operands = CliRunOperands.read(raw);
    DataInput data = support.dataInput(raw, operands.data(), false);
    support.validateParquetOptions(raw, data == null ? support.implicitInputType(raw) : data.format());
    return new RunInvocation(
        operands.script(), input(support, raw, data),
        CliRunTargetSelector.select(support, raw, data), output(raw),
        raw.noKeyInference, support.invocationOptions(raw));
  }

  private static void validate(CliParser.RawArguments raw) {
    CliOptionValidator.validateRunOptionOwnership(raw);
    CliParser.reject(raw.name != null && !raw.cache, "--name requires --cache");
    CliParser.reject(raw.cache && CliValueParser.hasJdbc(raw),
        "run cannot combine --cache and JDBC");
    CliParser.reject(raw.scriptFile != null && raw.scriptText != null,
        "run accepts exactly one script source");
    CliParser.reject(raw.inputFile != null && raw.inputText != null,
        "run accepts at most one data source");
  }

  private static InputSelection input(
      CliBindingSupport support, CliParser.RawArguments raw, DataInput data) {
    return data == null
        ? new InputSelection.Automatic(support.implicitInputType(raw))
        : new InputSelection.Provided(data);
  }

  private static OutputSelection output(CliParser.RawArguments raw) {
    return raw.output == null ? new OutputSelection.ScriptOrDefault()
        : new OutputSelection.Explicit(CliValueParser.outputType(raw.output));
  }
}
