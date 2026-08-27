package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.domain.HierarchyPath;
import blater.nql.parser.script.QueryShape;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

/** Reads explicit hierarchy structure keys. */
final class SelectStructureReader {
  private SelectStructureReader() {
  }

  static List<SelectBlueprint.StructureKey> read(HiQLParser.StructureClauseContext context) {
    if (context == null) {
      return List.of();
    }
    List<SelectBlueprint.StructureKey> keys = new java.util.ArrayList<>();
    for (HiQLParser.StructureItemContext item : context.structureItem()) {
      HierarchyPath path = PathContextMapper.toHierarchyPath(item.path());
      SelectStructureValidator.validate(path, keys);
      List<String> expressions = item.structureKeyExpr().stream()
          .map(ParseUtils::textOf)
          .map(String::trim)
          .toList();
      List<QueryShape.ExpressionFacts> facts = item.structureKeyExpr().stream()
          .map(QueryShapeExtractor::expressionFacts)
          .toList();
      keys.add(new SelectBlueprint.StructureKey(
          path,
          blater.nql.domain.RepetitionPlacement.named(),
          new SelectBlueprint.CommonKeyExpressions(expressions, facts),
          blater.nql.domain.KeyOrigin.EXPLICIT));
    }
    return keys;
  }
}
