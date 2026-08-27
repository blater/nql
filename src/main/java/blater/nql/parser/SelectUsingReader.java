package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;

import static blater.nql.util.Log.fatal;

/** Reads and validates SELECT-level USING metadata. */
final class SelectUsingReader {
  private SelectUsingReader() {
  }

  static void validatePlacement(HiQLParser.SelectStatementContext context) {
    for (int index = 1; index < context.selectBranch().size(); index++) {
      if (context.selectBranch(index).usingClause() != null) {
        fatal(HiqlSyntaxException.class,
            "using metadata is only valid on the first hierarchy union branch.");
      }
    }
  }

  static SelectUsingMetadata read(HiQLParser.UsingClauseContext context) {
    if (context == null) {
      return new SelectUsingMetadata(false, null);
    }
    boolean schemaOrRootPresent = false;
    String namespace = null;
    for (HiQLParser.UsingItemContext item : context.usingItem()) {
      if (item.K_SCHEMA() != null || item.K_XMLROOT() != null) {
        schemaOrRootPresent = true;
      } else if (item.K_NAMESPACE() != null) {
        namespace = namespace(item);
      }
    }
    return new SelectUsingMetadata(schemaOrRootPresent, namespace);
  }

  private static String namespace(HiQLParser.UsingItemContext item) {
    return item.STRING() != null
        ? ParseUtils.unquoteString(item.STRING().getText())
        : ParseUtils.unquoteIdentifier(item.QUOTED_IDENTIFIER().getText());
  }
}
