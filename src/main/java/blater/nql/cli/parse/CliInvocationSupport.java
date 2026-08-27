package blater.nql.cli.parse;

import blater.nql.cli.DataInput;
import blater.nql.cli.InvocationOptions;
import blater.nql.inputreader.InputType;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Compatibility facade for shared invocation construction services. */
final class CliInvocationSupport {
  private final CliInputSupport input;
  private final CliInvocationOptionsFactory options;
  private final CliCacheInvocationSupport cache;

  CliInvocationSupport(Supplier<Path> userHome) {
    input = new CliInputSupport();
    options = new CliInvocationOptionsFactory();
    cache = new CliCacheInvocationSupport(userHome);
  }

  DataInput dataInput(CliParser.RawArguments raw, String positional, boolean defaultStdin) {
    return input.dataInput(raw, positional, defaultStdin);
  }

  InputType implicitInputType(CliParser.RawArguments raw) {
    return input.implicitInputType(raw);
  }

  void validateParquetOptions(CliParser.RawArguments raw, InputType inputType) {
    input.validateParquetOptions(raw, inputType);
  }

  InvocationOptions invocationOptions(CliParser.RawArguments raw) {
    return options.create(raw);
  }

  Path cacheDirectory(CliParser.RawArguments raw) {
    return cache.directory(raw);
  }

  String singleName(CliParser.RawArguments raw, String command) {
    return cache.singleName(raw, command);
  }

  void rejectDataOptions(CliParser.RawArguments raw, String command) {
    cache.rejectDataOptions(raw, command);
  }
}
