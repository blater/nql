package blater.nql.cli.parse;

import blater.nql.cli.JdbcConnectionSpec;

/** Entry point for JDBC option conversion. */
final class CliJdbcConverter {
  private CliJdbcConverter() {
  }

  static boolean hasOptions(CliParser.RawArguments raw) {
    return raw.databaseType != null || raw.database != null || raw.host != null || raw.port != null
        || raw.user != null || raw.password != null || raw.jdbcUsername != null
        || raw.jdbcPassword != null || raw.jdbcDatabase != null || raw.jdbcDriver != null
        || raw.jdbcClassName != null;
  }

  static JdbcConnectionSpec connection(CliParser.RawArguments raw) {
    return CliJdbcConnection.create(raw);
  }
}
