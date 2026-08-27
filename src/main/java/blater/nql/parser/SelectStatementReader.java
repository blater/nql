package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

/** Reads the clauses that make up one SELECT statement. */
final class SelectStatementReader {
  private SelectStatementReader() {
  }

  static SelectStatementParts read(HiQLParser.SelectStatementContext context) {
    SelectUsingReader.validatePlacement(context);
    SelectUsingMetadata using = SelectUsingReader.read(context.selectBranch(0).usingClause());
    List<SelectBlueprint.Branch> branches = SelectBranchReader.read(context.selectBranch());
    SelectBranchValidator.validateRoots(branches);
    boolean hasHierarchyFields = hasHierarchyFields(branches);
    List<SelectBlueprint.OrderItem> orderItems = hasHierarchyFields
        ? SelectOrderReader.read(context.orderByClause())
        : List.of();
    List<SelectBlueprint.StructureKey> structureKeys = hasHierarchyFields
        ? SelectStructureReader.read(context.structureClause())
        : List.of();
    SelectBlueprint blueprint = new SelectBlueprint(branches, orderItems, structureKeys);
    return new SelectStatementParts(using, blueprint, hasHierarchyFields);
  }

  private static boolean hasHierarchyFields(List<SelectBlueprint.Branch> branches) {
    return branches.stream()
        .flatMap(branch -> branch.items().stream())
        .anyMatch(item -> item.outputPath() != null);
  }
}
