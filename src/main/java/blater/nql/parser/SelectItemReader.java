package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.parser.script.SelectBlueprint;
import org.antlr.v4.runtime.Token;

import java.util.List;

/** Reads a SELECT expression, its SQL alias, and its optional mapping target. */
final class SelectItemReader {
  private SelectItemReader() {
  }

  static SelectBlueprint.SelectItem read(HiQLParser.SelectItemContext context) {
    List<Token> expressionTokens = SelectTokenReader.tokensIn(context.selectExpr());
    String expression = SelectTokenReader.joinText(expressionTokens);
    String alias = context.sqlAlias() == null
        ? null
        : ParseUtils.unquoteIdentifier(context.sqlAlias().name().getText());
    String name = alias != null ? alias : SelectTokenReader.trailingIdentifier(expressionTokens);
    SelectMapping mapping = SelectMappingReader.read(context.mappingAlias());
    return new SelectBlueprint.SelectItem(
        expression,
        name,
        mapping == null ? null : mapping.path(),
        mapping == null ? null : mapping.appendText(),
        mapping != null && mapping.absentOnNull(),
        QueryShapeExtractor.expressionFacts(context.selectExpr()));
  }
}
