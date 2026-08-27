package blater.nql.cli.parse;

import blater.nql.cli.Credentials;
import blater.nql.cli.DriverSelection;
import blater.nql.cli.JdbcConnectionSpec;

/** Builds a JDBC connection from either supported CLI option form. */
final class CliJdbcConnection {
  private CliJdbcConnection() {
  }

  static JdbcConnectionSpec create(CliParser.RawArguments raw) {
    boolean simple = hasSimpleIdentity(raw);
    boolean exact = hasExactIdentity(raw);
    CliJdbcFormValidator.validate(raw, simple, exact);
    String username = first(raw.user, raw.jdbcUsername);
    String password = first(raw.password, raw.jdbcPassword);
    if (simple) {
      return CliSimpleJdbcConnection.create(raw, username, password);
    }
    CliParser.reject(!exact,
        "database credentials require --jdbc-database or the --db/--database form");
    CliParser.reject(raw.jdbcDatabase == null,
        "--jdbc-database is required with an exact JDBC driver hint");
    return new JdbcConnectionSpec(
        raw.jdbcDatabase, exactDriver(raw), credentials(username, password));
  }

  private static boolean hasSimpleIdentity(CliParser.RawArguments raw) {
    return raw.databaseType != null || raw.database != null || raw.host != null || raw.port != null;
  }

  private static boolean hasExactIdentity(CliParser.RawArguments raw) {
    return raw.jdbcDatabase != null || raw.jdbcDriver != null || raw.jdbcClassName != null;
  }

  private static DriverSelection exactDriver(CliParser.RawArguments raw) {
    if (raw.jdbcClassName != null) {
      return new DriverSelection.ClassName(raw.jdbcClassName);
    }
    if (raw.jdbcDriver != null) {
      return new DriverSelection.Known(CliJdbcDriver.known(raw.jdbcDriver));
    }
    return new DriverSelection.Automatic();
  }

  private static String first(String preferred, String fallback) {
    return preferred == null ? fallback : preferred;
  }

  static Credentials credentials(String username, String password) {
    return new Credentials(credential(username), credential(password));
  }

  private static Credentials.Value credential(String value) {
    return value == null
        ? new Credentials.Value.Unspecified()
        : new Credentials.Value.Specified(value);
  }
}
