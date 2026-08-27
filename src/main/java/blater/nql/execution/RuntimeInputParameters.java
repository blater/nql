package blater.nql.execution;

import blater.nql.cli.DataInput;
import blater.nql.cli.DataSourceSpec;
import blater.nql.cli.InputSelection;

import java.util.Map;
import java.util.Objects;

import static blater.nql.execution.EngineParameterNames.*;

/** Writes concrete input-source options into the engine parameter map. */
final class RuntimeInputParameters {
  private RuntimeInputParameters() {
  }

  static Materialization none() {
    return new Materialization.None();
  }

  static Materialization provided(MaterializedDataInput input) {
    return new Materialization.Provided(input);
  }

  static void add(
      Map<String, String> parameters,
      InputSelection input,
      Materialization materialization) {
    switch (input) {
      case InputSelection.None ignored -> rejectUnexpectedMaterialization(materialization);
      case InputSelection.Automatic ignored -> throw new IllegalStateException(
          "Runtime parameters require automatic stdin selection to be resolved first");
      case InputSelection.Provided provided -> addProvided(parameters, provided.input(), materialization);
    }
  }

  private static void rejectUnexpectedMaterialization(Materialization materialization) {
    if (materialization instanceof Materialization.Provided) {
      throw new IllegalStateException("Materialized data supplied for an invocation without input");
    }
  }

  private static void addProvided(
      Map<String, String> parameters,
      DataInput dataInput,
      Materialization materialization) {
    String filename = switch (materialization) {
      case Materialization.None ignored -> sourceFilename(dataInput.source());
      case Materialization.Provided staged -> stagedFilename(dataInput, staged.input());
    };
    parameters.put(INPUT_FILENAME, filename);
    parameters.put(INPUT_TYPE, dataInput.format().name().toLowerCase(java.util.Locale.ROOT));
  }

  private static String sourceFilename(DataSourceSpec source) {
    return switch (source) {
      case DataSourceSpec.File file -> file.path().toString();
      case DataSourceSpec.StandardInput ignored -> STANDARD_INPUT;
      case DataSourceSpec.Text ignored -> throw new IllegalStateException(
          "Literal input data must be materialized first");
    };
  }

  private static String stagedFilename(DataInput expected, MaterializedDataInput staged) {
    if (!staged.input().equals(expected)) {
      throw new IllegalStateException("Materialized data belongs to a different input source");
    }
    return staged.path().toString();
  }

  sealed interface Materialization permits Materialization.None, Materialization.Provided {
    record None() implements Materialization {
    }

    record Provided(MaterializedDataInput input) implements Materialization {
      public Provided {
        Objects.requireNonNull(input, "input");
      }
    }
  }
}
