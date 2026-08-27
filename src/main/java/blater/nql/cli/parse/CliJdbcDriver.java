package blater.nql.cli.parse;

import java.util.Locale;

/** JDBC driver aliases, defaults, and URL rendering. */
final class CliJdbcDriver {
  private CliJdbcDriver() {
  }

  static String known(String value) {
    if (value == null || value.isBlank()) {
      throw usage("No database type supplied");
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    return switch (normalized) {
      case "h2", "h2db" -> "h2";
      case "postgresql", "postgres", "postgres-jdbc" -> "postgresql";
      case "mysql", "mysql-connector-j" -> "mysql";
      case "mariadb" -> "mariadb";
      case "oracle", "ojdbc", "ojdbc11" -> "oracle";
      case "sqlserver", "mssql", "mssql-jdbc", "sql-server" -> "sqlserver";
      case "db2", "jcc" -> "db2";
      case "hana", "sap", "sap-hana", "ngdbc" -> "hana";
      case "informix" -> "informix";
      default -> throw usage("Unsupported JDBC driver: " + value);
    };
  }

  static String defaultUsername(String driver) {
    return switch (driver) {
      case "h2" -> "sa";
      case "postgresql", "mysql", "mariadb" -> System.getProperty("user.name");
      default -> null;
    };
  }

  static String url(String driver, String database, String host, String port) {
    String resolvedHost = host == null ? "localhost" : host;
    String resolvedPort = port == null ? defaultPort(driver) : port;
    return switch (driver) {
      case "h2" -> "jdbc:h2:" + database;
      case "postgresql" -> "jdbc:postgresql://" + resolvedHost + ":" + resolvedPort + "/" + database;
      case "mysql" -> "jdbc:mysql://" + resolvedHost + ":" + resolvedPort + "/" + database;
      case "mariadb" -> "jdbc:mariadb://" + resolvedHost + ":" + resolvedPort + "/" + database;
      case "oracle" -> "jdbc:oracle:thin:@//" + resolvedHost + ":" + resolvedPort + "/" + database;
      case "sqlserver" -> "jdbc:sqlserver://" + resolvedHost + ":" + resolvedPort
          + ";databaseName=" + database;
      case "db2" -> "jdbc:db2://" + resolvedHost + ":" + resolvedPort + "/" + database;
      case "hana" -> "jdbc:sap://" + resolvedHost + ":" + resolvedPort
          + "/?databaseName=" + database;
      case "informix" -> "jdbc:informix-sqli://" + resolvedHost + ":" + resolvedPort
          + "/" + database;
      default -> throw new IllegalStateException("Unhandled JDBC driver: " + driver);
    };
  }

  private static String defaultPort(String driver) {
    return switch (driver) {
      case "postgresql" -> "5432";
      case "mysql", "mariadb" -> "3306";
      case "oracle" -> "1521";
      case "sqlserver" -> "1433";
      case "db2" -> "50000";
      default -> null;
    };
  }

  private static CliUsageException usage(String message) {
    return new CliUsageException(message);
  }
}
