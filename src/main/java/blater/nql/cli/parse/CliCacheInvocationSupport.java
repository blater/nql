package blater.nql.cli.parse;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Handles cache-specific invocation values shared by cache binders. */
final class CliCacheInvocationSupport {
  private final Supplier<Path> userHome;

  CliCacheInvocationSupport(Supplier<Path> userHome) {
    this.userHome = userHome;
  }

  Path directory(CliParser.RawArguments raw) {
    String configured = raw.cacheDirectory;
    if (configured == null || configured.isBlank()) {
      configured = userHome.get().resolve(".nql").resolve("cache").toString();
    }
    return Path.of(configured).toAbsolutePath().normalize();
  }

  String singleName(CliParser.RawArguments raw, String command) {
    CliParser.reject(raw.name != null && !raw.positionals.isEmpty(),
        "positional cache name conflicts with --name");
    if (raw.positionals.size() > 1) {
      throw CliParser.usage(command + " accepts one cache name");
    }
    String name = raw.name != null ? raw.name
        : raw.positionals.isEmpty() ? null : raw.positionals.getFirst();
    if (name == null) {
      throw CliParser.usage(command + " requires a cache name");
    }
    return name;
  }

  void rejectDataOptions(CliParser.RawArguments raw, String command) {
    CliParser.reject(raw.inputFile != null || raw.inputText != null || raw.inputFormat != null,
        "input options are not valid for " + command);
    CliParser.reject(raw.paramsFile != null || !raw.params.isEmpty(),
        "parameters are not valid for " + command);
    CliParser.reject(raw.parquetRoot != null || raw.parquetRecord != null,
        "Parquet options are not valid for " + command);
  }
}
