package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

/** Reads the optional ORDER BY clause used by hierarchy SELECTs. */
final class SelectOrderReader {
  private SelectOrderReader() {
  }

  static List<SelectBlueprint.OrderItem> read(HiQLParser.OrderByClauseContext context) {
    if (context == null) {
      return List.of();
    }
    return context.orderItem().stream().map(SelectOrderReader::readItem).toList();
  }

  private static SelectBlueprint.OrderItem readItem(HiQLParser.OrderItemContext context) {
    String direction = context.K_DESC() != null ? "desc"
        : context.K_ASC() != null ? "asc" : null;
    return new SelectBlueprint.OrderItem(
        ParseUtils.textOf(context.orderExpr()).trim(), direction);
  }
}
