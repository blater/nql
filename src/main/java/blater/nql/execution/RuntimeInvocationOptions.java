package blater.nql.execution;

import blater.nql.cli.InvocationOptions;
import blater.nql.cli.OutputSelection;
import blater.nql.cli.ParquetOverrides;
import blater.nql.cli.RunInvocation;
import blater.nql.outputwriter.OutputType;

import java.util.Locale;
import java.util.Map;

import static blater.nql.execution.EngineParameterNames.*;

/** Writes invocation-level options into the engine parameter map. */
final class RuntimeInvocationOptions {
  private RuntimeInvocationOptions() {
  }

  static void addRun(Map<String, String> parameters, RunInvocation invocation) {
    add(parameters, invocation.options());
    if (invocation.noKeyInference()) parameters.put(NO_KEY_INFERENCE, Boolean.TRUE.toString());
    if (invocation.output() instanceof OutputSelection.Explicit explicit) {
      addOutput(parameters, explicit.format());
    }
  }

  static void add(Map<String, String> parameters, InvocationOptions options) {
    options.parameters().forEach((name, value) -> {
      if (isTaskParameter(name)) parameters.put(name, value);
    });
    addDebug(parameters, options.debug());
    addParquetOverrides(parameters, options.parquetOverrides());
  }

  static void addOutput(Map<String, String> parameters, OutputType output) {
    parameters.put(OUTPUT_TYPE, formatName(output));
  }

  static void addDebug(Map<String, String> parameters, boolean debug) {
    if (debug) parameters.put(DEBUG, Boolean.TRUE.toString());
  }

  private static boolean isTaskParameter(String name) {
    String normalized = name.toLowerCase(Locale.ROOT);
    return !normalized.startsWith("nql.")
        && !normalized.startsWith("jdbc.")
        && !normalized.startsWith("cache.")
        && !normalized.startsWith("nsql_")
        && !normalized.startsWith("nql_");
  }

  private static void addParquetOverrides(
      Map<String, String> parameters,
      ParquetOverrides overrides) {
    if (overrides.root() instanceof ParquetOverrides.Value.Explicit explicit) {
      parameters.put(PARQUET_ROOT, explicit.value());
    }
    if (overrides.record() instanceof ParquetOverrides.Value.Explicit explicit) {
      parameters.put(PARQUET_RECORD, explicit.value());
    }
  }

  private static String formatName(Enum<?> format) {
    return format.name().toLowerCase(Locale.ROOT);
  }
}
