package blater.nql.cli.parse;

import blater.nql.cli.InvocationOptions;
import blater.nql.cli.ParquetOverrides;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Creates immutable invocation options, including validated task parameters. */
final class CliInvocationOptionsFactory {
  InvocationOptions create(CliParser.RawArguments raw) {
    return new InvocationOptions(
        parameters(raw), raw.debug, new ParquetOverrides(parquetRoot(raw), parquetRecord(raw)));
  }

  private static ParquetOverrides.Value parquetRoot(CliParser.RawArguments raw) {
    return raw.parquetRoot == null
        ? new ParquetOverrides.Value.Inferred()
        : new ParquetOverrides.Value.Explicit(raw.parquetRoot);
  }

  private static ParquetOverrides.Value parquetRecord(CliParser.RawArguments raw) {
    return raw.parquetRecord == null
        ? new ParquetOverrides.Value.Inferred()
        : new ParquetOverrides.Value.Explicit(raw.parquetRecord);
  }

  private static Map<String, String> parameters(CliParser.RawArguments raw) {
    Map<String, String> parameters = new LinkedHashMap<>();
    if (raw.paramsFile != null) {
      CliPropertyFiles.read(Path.of(raw.paramsFile), "parameters").forEach((name, value) -> {
        CliParameterValidator.validate(name);
        parameters.put(name, value);
      });
    }
    CliParameterValidator.applyCommandLine(raw.params, parameters);
    return Map.copyOf(parameters);
  }
}
