package blater.nql.cli.parse;

/** Checks that the two JDBC option forms are not mixed or ambiguous. */
final class CliJdbcFormValidator {
  private CliJdbcFormValidator() {
  }

  static void validate(
      CliParser.RawArguments raw, boolean simpleIdentity, boolean exactIdentity) {
    reject(simpleIdentity && exactIdentity,
        "simple database options cannot be combined with exact JDBC options");
    reject(raw.user != null && raw.jdbcUsername != null,
        "--user and --jdbc-username are aliases and cannot be combined");
    reject(raw.password != null && raw.jdbcPassword != null,
        "--password and --jdbc-password are aliases and cannot be combined");
    reject(raw.jdbcDriver != null && raw.jdbcClassName != null,
        "--jdbc-driver and --jdbc-class-name are mutually exclusive");
  }

  private static void reject(boolean condition, String message) {
    CliParser.reject(condition, message);
  }
}
