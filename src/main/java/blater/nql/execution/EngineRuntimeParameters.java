package blater.nql.execution;

import blater.nql.cli.NqlInvocation;

import java.util.Map;
import java.util.Objects;

/** Builds the narrow mutable parameter map consumed by query and mapping internals. */
public final class EngineRuntimeParameters {
  private EngineRuntimeParameters() {
  }

  public static Map<String, String> from(NqlInvocation invocation) {
    return RuntimeParameterMapper.map(Objects.requireNonNull(invocation, "invocation"));
  }

  public static Map<String, String> from(
      NqlInvocation invocation,
      MaterializedDataInput materializedInput) {
    return RuntimeParameterMapper.map(
        Objects.requireNonNull(invocation, "invocation"),
        Objects.requireNonNull(materializedInput, "materializedInput"));
  }
}
