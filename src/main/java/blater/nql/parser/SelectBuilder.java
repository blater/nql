package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.parser.script.NestStatement;

/** Entry point for building SELECT statements from the parsed grammar tree. */
final class SelectBuilder {
  private SelectBuilder() {
  }

  static NestStatement buildSelect(HiQLParser.SelectStatementContext context) {
    return SelectStatementAssembler.build(context);
  }
}
