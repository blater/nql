package blater.nql.execution;

import blater.nql.cli.Credentials;
import blater.nql.cli.DriverSelection;
import blater.nql.cli.ExecutionTarget;
import blater.nql.cli.JdbcConnectionSpec;

import java.util.Map;

import static blater.nql.execution.EngineParameterNames.*;

/** Writes database-target options into the engine parameter map. */
final class RuntimeTargetParameters {
  private RuntimeTargetParameters() {
  }

  static void add(Map<String, String> parameters, ExecutionTarget target) {
    switch (target) {
      case ExecutionTarget.Temporary ignored -> {
      }
      case ExecutionTarget.InputOrActiveCache ignored -> throw new IllegalStateException(
          "Runtime parameters require automatic execution target resolution first");
      case ExecutionTarget.ActiveCache ignored -> {
      }
      case ExecutionTarget.NamedCache ignored -> {
      }
      case ExecutionTarget.Jdbc jdbc -> addJdbc(parameters, jdbc.connection());
    }
  }

  private static void addJdbc(Map<String, String> parameters, JdbcConnectionSpec connection) {
    parameters.put(JDBC_DATABASE, connection.url());
    switch (connection.driver()) {
      case DriverSelection.Automatic ignored -> {
      }
      case DriverSelection.Known known -> parameters.put(JDBC_DRIVER, known.value());
      case DriverSelection.ClassName className ->
          parameters.put(JDBC_CLASS_NAME, className.value());
    }
    addCredential(parameters, JDBC_USERNAME, connection.credentials().username());
    addCredential(parameters, JDBC_PASSWORD, connection.credentials().password());
  }

  private static void addCredential(
      Map<String, String> parameters,
      String name,
      Credentials.Value credential) {
    if (credential instanceof Credentials.Value.Specified specified) {
      parameters.put(name, specified.value());
    }
  }
}
