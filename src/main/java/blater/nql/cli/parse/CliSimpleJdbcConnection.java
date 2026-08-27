package blater.nql.cli.parse;

import blater.nql.cli.DriverSelection;
import blater.nql.cli.JdbcConnectionSpec;

/** Builds a connection from the short --db/--database option form. */
final class CliSimpleJdbcConnection {
  private CliSimpleJdbcConnection() {
  }

  static JdbcConnectionSpec create(
      CliParser.RawArguments raw, String username, String password) {
    CliParser.reject((raw.databaseType == null) != (raw.database == null),
        "--db and --database must be supplied together");
    CliParser.reject((raw.host != null || raw.port != null) && raw.databaseType == null,
        "--host and --port require --db");
    String driver = CliJdbcDriver.known(raw.databaseType);
    CliParser.reject(driver.equals("h2") && (raw.host != null || raw.port != null),
        "--host and --port are not valid for H2");
    CliParser.reject((driver.equals("hana") || driver.equals("informix")) && raw.port == null,
        "--port is required for " + driver);
    String resolvedUsername = username == null
        ? CliJdbcDriver.defaultUsername(driver) : username;
    return new JdbcConnectionSpec(
        CliJdbcDriver.url(driver, raw.database, raw.host, raw.port),
        new DriverSelection.Known(driver),
        CliJdbcConnection.credentials(resolvedUsername, password));
  }
}
