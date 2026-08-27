package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.parser.script.NestStatement;

/** Coordinates SELECT parsing and conversion into the public statement model. */
final class SelectStatementAssembler {
  private SelectStatementAssembler() {
  }

  static NestStatement build(HiQLParser.SelectStatementContext context) {
    SelectStatementParts parts = SelectStatementReader.read(context);
    return SelectStatementResult.create(ParseUtils.textOf(context).trim(), parts);
  }
}
