package blater.nql.runner.sql.cache;

import blater.nql.domain.ScalarKind;

import java.math.BigDecimal;

/** A scalar hierarchy value together with the type inferred for it. */
record CacheScalarValue(String value, ScalarKind kind) {
  Object databaseValue(ScalarKind targetKind) {
    if (value == null) {
      return null;
    }
    return switch (targetKind) {
      case STRING -> value;
      case NUMBER -> new BigDecimal(value);
      case BOOLEAN -> Boolean.valueOf(value);
    };
  }
}
