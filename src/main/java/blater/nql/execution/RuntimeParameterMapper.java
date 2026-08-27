package blater.nql.execution;

import blater.nql.cli.CacheInvocation;
import blater.nql.cli.CapabilitiesInvocation;
import blater.nql.cli.CatalogInvocation;
import blater.nql.cli.ConvertInvocation;
import blater.nql.cli.HelpInvocation;
import blater.nql.cli.InputSelection;
import blater.nql.cli.NqlInvocation;
import blater.nql.cli.RunInvocation;
import blater.nql.cli.VersionInvocation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Translates typed invocations into the legacy engine parameter contract. */
final class RuntimeParameterMapper {
  private RuntimeParameterMapper() {
  }

  static Map<String, String> map(NqlInvocation invocation) {
    return map(invocation, RuntimeInputParameters.none());
  }

  static Map<String, String> map(
      NqlInvocation invocation,
      MaterializedDataInput materializedInput) {
    return map(invocation, RuntimeInputParameters.provided(materializedInput));
  }

  private static Map<String, String> map(
      NqlInvocation invocation,
      RuntimeInputParameters.Materialization materialization) {
    Map<String, String> parameters = new LinkedHashMap<>();
    InputSelection input = selectInput(parameters, invocation);
    RuntimeInputParameters.add(parameters, input, materialization);
    return parameters;
  }

  private static InputSelection selectInput(
      Map<String, String> parameters,
      NqlInvocation invocation) {
    return switch (invocation) {
      case RunInvocation run -> {
        RuntimeInvocationOptions.addRun(parameters, run);
        RuntimeTargetParameters.add(parameters, run.target());
        yield run.input();
      }
      case ConvertInvocation convert -> {
        RuntimeInvocationOptions.add(parameters, convert.options());
        RuntimeInvocationOptions.addOutput(parameters, convert.output());
        yield new InputSelection.Provided(convert.input());
      }
      case CatalogInvocation catalog -> {
        RuntimeInvocationOptions.add(parameters, catalog.options());
        RuntimeTargetParameters.add(parameters, catalog.target());
        yield catalog.input();
      }
      case CacheInvocation.Load load -> {
        RuntimeInvocationOptions.add(parameters, load.options());
        yield new InputSelection.Provided(load.input());
      }
      case CacheInvocation.Use use -> {
        RuntimeInvocationOptions.addDebug(parameters, use.debug());
        yield new InputSelection.None();
      }
      case CacheInvocation.ListCaches list -> {
        RuntimeInvocationOptions.addDebug(parameters, list.debug());
        yield new InputSelection.None();
      }
      case CacheInvocation.Clear clear -> {
        RuntimeInvocationOptions.addDebug(parameters, clear.debug());
        yield new InputSelection.None();
      }
      case HelpInvocation ignored -> new InputSelection.None();
      case VersionInvocation ignored -> new InputSelection.None();
      case CapabilitiesInvocation ignored -> new InputSelection.None();
    };
  }
}
