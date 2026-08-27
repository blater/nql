package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

/** Converts SELECT branches into immutable blueprint branches. */
final class SelectBranchReader {
  private SelectBranchReader() {
  }

  static List<SelectBlueprint.Branch> read(List<HiQLParser.SelectBranchContext> contexts) {
    return contexts.stream().map(SelectBranchReader::readBranch).toList();
  }

  private static SelectBlueprint.Branch readBranch(HiQLParser.SelectBranchContext context) {
    List<SelectBlueprint.SelectItem> items = context.selectItem().stream()
        .map(SelectItemReader::read)
        .toList();
    String sqlTail = context.sqlTail() == null ? null : ParseUtils.textOf(context.sqlTail());
    return new SelectBlueprint.Branch(
        items, sqlTail, QueryShapeExtractor.extract(context));
  }
}
